/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Jun 24, 2005
 *
 */
package edu.umd.cs.submitServer.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import edu.umd.cs.marmoset.modelClasses.EclipseLaunchEvent;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.MultipartRequest;

/**
 * @author jspacco
 *
 */
public class LogEclipseLaunchEvent extends SubmitServerServlet
{
	private static final String ECLIPSE_LAUNCH_EVENT_PASSWORD = "eclipse.launch.event.password";
	private static String password;
	private static String semester;
	
	private String getPassword() {
		if (password==null) {
			password = getServletContext().getInitParameter(ECLIPSE_LAUNCH_EVENT_PASSWORD);
		}
		return password;
	}
	
	private String getSemester() {
		if (semester==null) {
			semester = getServletContext().getInitParameter("semester");
		}
		return semester;
	}

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
    	Connection conn=null;
    	BufferedReader reader=null;
    	try {
            conn=getConnection();
    		MultipartRequest multipartRequest = (MultipartRequest)request.getAttribute(MULTIPART_REQUEST);
            
            long clientTime = multipartRequest.getLongParameter("clientTime");
            long serverTime = System.currentTimeMillis();
            // Compute the "skew" between client and server in minutes.
            // This implicitly throw out things that are < 1 min so we lose the regular
            // lagtime it takes to upload the submission and post the launch events.
            int skew = (int)((serverTime - clientTime)/1000/60);
            
    		StudentRegistration registration = (StudentRegistration)request.getAttribute("studentRegistration");
            
            FileItem fileItem = multipartRequest.getFileItem();
            reader=new BufferedReader(new InputStreamReader(fileItem.getInputStream()));
            StringBuffer result=new StringBuffer();
            while (true) {
                String line = reader.readLine();
                if (line==null) break;
                result.append(line +"\n");
                // eclipseLaunchEvent	date	timestamp	projectName	event
                String tokens[] = line.split("\t");
                String timestampStr= tokens[1];
                String md5sum = tokens[2];
                String projectName = tokens[3];
                String event = tokens[4];
                
                getSubmitServerServletLog().debug(timestampStr +"\t"+ md5sum +"\t"+
                		projectName +"\t"+ event);
                
                EclipseLaunchEvent eclipseLaunchEvent= new EclipseLaunchEvent();
                eclipseLaunchEvent.setStudentRegistrationPK(registration.getStudentRegistrationPK());
                eclipseLaunchEvent.setProjectNumber(projectName);
                eclipseLaunchEvent.setEvent(event);
                long timestamp = Long.valueOf(timestampStr);
                eclipseLaunchEvent.setTimestamp(new Timestamp(timestamp));
                eclipseLaunchEvent.setMd5sum(md5sum);
                eclipseLaunchEvent.setSkew(skew);
                eclipseLaunchEvent.insert(conn);
            }
            getSubmitServerServletLog().debug("Uploaded EclipseLaunchEvents: " +result);
        } catch (InvalidRequiredParameterException e) {
            throw new ServletException(e);
        } catch (SQLException e) {
        	handleSQLException(e);
        	throw new ServletException(e);
        } finally {
        	releaseConnection(conn);
        	if (reader != null) reader.close();
        }
    }

}
