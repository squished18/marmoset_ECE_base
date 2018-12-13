package ca.uwaterloo.cs.submitServer.filters;

import java.util.Base64;

import javax.servlet.FilterChain;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import edu.umd.cs.submitServer.filters.SubmitServerFilter;

import ca.uwaterloo.cs.submitServer.DummyAuthWrapper;

/*
 * This filter allows the server to be operated without needing
 * authentication by using HTTP Basic Authentication to prompt the
 * browser for login. Obviously, since no password verification
 * happens, this is meant for testing and development environments
 * only.
 *
 * @author jtparkin
 *
 */

public class DummyAuthFilter extends SubmitServerFilter
{

    public void doFilter(ServletRequest req,
                         ServletResponse resp,
                         FilterChain chain)
        throws ServletException, IOException
    {
        HttpServletResponse response = (HttpServletResponse) resp;
        HttpServletRequest  request  = (HttpServletRequest)  req;

        // Don't try and get the build server to authenticate
        // It will only access URIs that contain buildServer
        if (request.getRequestURI().contains("buildServer"))
            chain.doFilter(request, response);

        String auth_header = request.getHeader("Authorization");
        if (auth_header == null) {
            /*
             * If the header is null, then there's no credentials
             * being sent by the browser. We send a 401 with a
             * WWW-Authenticate header, which will prompt the browser
             * to give a login box.
             *
             */

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate",
                               "BASIC realm=\"Marmoset Testing\"");
            chain.doFilter(request, response);
        }
        else {
            /* Since there is a header, we decode it to get the
             * username. Note that the setRemoteUser method is part of
             * the DummyAuthWrapper that wraps an HttpServletRequest
             * in order to allow easy setting of the remote
             * user. Normally the remote user could not be changed in
             * a regular HttpRequest and would get set by the Apache
             * connector.
             *
             * See ca/uwaterloo/cs/submitServer/DummyAuthWrapper.java
             *
             */

            DummyAuthWrapper auth_request = new DummyAuthWrapper(request);
            auth_request.setRemoteUser(decodeHeader(auth_header));
            chain.doFilter(auth_request, response);
        }
    }

    private String decodeHeader(String header)
        throws ServletException
    {
        /*
         * The format of the header is simply:
         * "Basic (base64)user:password"
         *
         * So we need to split on " ", decode the second token as
         * base64 and then split the result on ":".
         *
         */
        byte[] decoded_bytes = Base64.getDecoder().decode(header.split(" ")[1]);
        try {
            String decoded = new String(decoded_bytes, "UTF-8");
            return decoded.split(":")[0];
        } catch(UnsupportedEncodingException e) {
            /*
             * This should never happen, I don't know of any modern
             * software that doesn't support UTF-8.
             */
            throw new ServletException("No UTF-8 support!");
        }
    }
}
