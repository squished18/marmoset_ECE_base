/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 13, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author jspacco
 *
 */
public class SubmitProjectViaWeb extends SubmitServerServlet
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
        // HttpSession session = request.getSession();
        request.setAttribute("webBasedUpload", Boolean.TRUE);
            // forward to the UploadSubmission servlet for the heavy lifting
            RequestDispatcher dispatcher = request.getRequestDispatcher("/action/UploadSubmission");
            dispatcher.forward(request, response);

    }

}
