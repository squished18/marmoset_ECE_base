package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.submitServer.IncorrectCourseProjectManagerPluginVersionException;
import edu.umd.cs.submitServer.MultipartRequest;

public class SubmitProjectViaBlueJ extends SubmitServerServlet
{

    /**
     * The doPost method of the servlet. <br>
     *
     * This method is called when a form has its tag value method equals to post.
     * 
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
//      set by filter
        MultipartRequest multipartRequest = (MultipartRequest)request.getAttribute(MULTIPART_REQUEST);
        
        // it's recommended we use a minimum version of the course project manager client
        String submitClientTool = multipartRequest.getOptionalStringParameter("submitClientTool");
        String submitClientVersion = multipartRequest.getOptionalStringParameter("submitClientVersion");
        String minimumCPMVersion = getServletContext().getInitParameter("minimum.bluej.to.marmoset.version");
        if (submitClientTool == null || submitClientVersion == null
                || submitClientTool.equals("BlueJ")
                && (submitClientVersion.compareTo(minimumCPMVersion) < 0)) {
            throw new ServletException(
                    new IncorrectCourseProjectManagerPluginVersionException(
                    "Please upgrade to the latest version of the BlueJ-To-Marmoset " +
                    "Submission Extension.  You are using " +submitClientVersion+
                    " and it is recommended that you use at least " +
                    minimumCPMVersion));
        }
        
        if (true) {
            getSubmitServerServletLog().warn(multipartRequest);
            return;
        }
        
        request.setAttribute("webBasedUpload", Boolean.FALSE);
        // forward to the UploadSubmission servlet for the heavy lifting
        String uploadSubmission = "/action/UploadSubmission";
        RequestDispatcher dispatcher = request.getRequestDispatcher(uploadSubmission);
        dispatcher.forward(request, response);
    }

}
