/*
 * Created on Jan 6, 2005
 *
*/
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.submitServer.UserSession;

/**
 * @author pugh
 * 
 */
public class AuthenticationFilter extends SubmitServerFilter {
	
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		HttpSession session = request.getSession();
		UserSession userSession = (UserSession) session.getAttribute(USER_SESSION);
		
		if (session.isNew() || userSession == null)
		{
			// main login page
		    // I'm redirecting to slash rather than a specific page (like index.jsp)
		    // so that other servers (like marmosetdemo.cs.umd.edu) can set their own special
		    // splash/login pages with different features 
		    String login = request.getContextPath() + "/";
			
			// if the request is "get" or a "post", save the target for a later re-direct
			// after authentication
            // TODO we can't actually re-direct POST methods, we lose the parameters
			if (request.getMethod().equals("GET") || request.getMethod().equals("POST"))
			{
			    // if the request was get or post, save the URL as target parameter
			    String target = request.getRequestURI();
				if (request.getQueryString() != null)
					target += "?"+ request.getQueryString();
				login = login + "?target="
						+ URLEncoder.encode(target, "UTF-8");
			}

			response.sendRedirect(login);
		}
		else
		{
		    //System.out.println("AuthenticationFilter chain.doFilter()");
			Connection conn = null;
			try {
				conn = getConnection();
				Student student = Student.getByStudentPK(userSession.getStudentPK(), conn);
				request.setAttribute(STUDENT, student);
				
			} catch (SQLException e) {
			    handleSQLException(e);
			    throw new ServletException(e);
			} finally {
			    releaseConnection(conn);
			}
		    chain.doFilter(req, resp);
		}
	}
}