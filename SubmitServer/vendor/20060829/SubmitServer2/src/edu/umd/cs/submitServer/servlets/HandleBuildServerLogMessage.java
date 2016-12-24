/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Feb 1, 2005
 *
 */
package edu.umd.cs.submitServer.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import edu.umd.cs.submitServer.MultipartRequest;

/**
 * @author jspacco
 *
 */
public class HandleBuildServerLogMessage extends SubmitServerServlet
{
    /** The logger for messages from the buildserver. */
    private  Logger buildServerLogger;

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
        MultipartRequest multipartRequest = (MultipartRequest)request.getAttribute(MULTIPART_REQUEST);
        FileItem fileItem = null;
        LoggingEvent loggingEvent = null;
        ObjectInputStream in = null;
        try {
            fileItem = multipartRequest.getFileItem();
            byte[] data = fileItem.get();
            
            in = new ObjectInputStream(new ByteArrayInputStream(data));
            loggingEvent = (LoggingEvent)in.readObject();
            
            buildServerLogger.callAppenders(loggingEvent);
        } catch (ClassNotFoundException e) {
            throw new ServletException("Cannot find class: " +e.getMessage(), e);
        } finally {
            if (fileItem != null) fileItem.delete();
            if (in != null) in.close();
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init()
     */
    public void init() throws ServletException
    {
        super.init();
        buildServerLogger = Logger.getLogger(getClass());
    }
}
