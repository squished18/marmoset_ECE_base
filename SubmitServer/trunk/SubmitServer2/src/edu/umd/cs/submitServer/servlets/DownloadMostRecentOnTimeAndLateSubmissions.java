/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Mar 1, 2005
 *
 */
package edu.umd.cs.submitServer.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.CopyUtils;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.StudentSubmitStatus;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.ZipFileAggregator;

/**
 * @author jspacco
 *
 */
public class DownloadMostRecentOnTimeAndLateSubmissions extends
        SubmitServerServlet
{

    /**
     * The doGet method of the servlet. <br>
     *
     * This method is called when a form has its tag value method equals to get.
     * 
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        Connection conn=null;
        FileOutputStream fileOutputStream=null;
        try {
            conn=getConnection();

            // get the project and all the student registrations
            Map lastOnTime = (Map)request.getAttribute("lastOnTime");
            Map lastLate = (Map)request.getAttribute("lastLate");

            Project project = (Project)request.getAttribute("project");
            Set<StudentRegistration> registrationSet = (Set<StudentRegistration>)request.getAttribute("studentRegistrationSet");
            Map studentSubmitStatusMap = (Map)request.getAttribute("studentSubmitStatusMap");
            
            // write everything to a tempfile, then send the tempfile
            File tempfile = File.createTempFile("temp", "zipfile");
            fileOutputStream = new FileOutputStream(tempfile);
            
            // zip aggregator
            ZipFileAggregator zipAggregator = new ZipFileAggregator(fileOutputStream);

            for (StudentRegistration registration : registrationSet) {
                StudentSubmitStatus studentSubmitStatus = 
                    (StudentSubmitStatus)studentSubmitStatusMap.get(registration.getStudentRegistrationPK());
                
                Submission ontime = (Submission)lastOnTime.get(registration.getStudentRegistrationPK());
                
                if (ontime != null)
                {
                    try {
                        byte[] bytes = ontime.downloadArchive(conn);
                        zipAggregator.addFileFromBytes(registration.getCvsAccount(), bytes);
                    } catch (ZipFileAggregator.BadInputZipFileException ignore) {
                        // ignore, since students could submit things that aren't zipfiles
                    	getSubmitServerServletLog().warn(ignore.getMessage(), ignore);
                    }
                }
                
                Submission late = (Submission)lastLate.get(registration.getStudentRegistrationPK());
                
                if (late != null)
                {
                    try {
                        byte[] bytes = late.downloadArchive(conn);
                        zipAggregator.addFileFromBytes(registration.getCvsAccount() + "-late", bytes);
                    } catch (ZipFileAggregator.BadInputZipFileException ignore) {
                        // ignore, since students could submit things that aren't zipfiles
                    	getSubmitServerServletLog().warn(ignore.getMessage(), ignore);
                    }
                }
            }
            
            zipAggregator.close();
            

            // write the zipfile to the client
            response.setContentType("application/zip");
            response.setContentLength((int)tempfile.length());

            // take into account MS IE's inability to download zipfiles
            String filename = "p" +project.getProjectNumber() +".zip";
            String disposition = "attachment; filename=\""+filename+"\"";
            response.setHeader("Content-Disposition",disposition);
            response.setHeader("Cache-Control","private");
            response.setHeader("Pragma","IE is broken");

            ServletOutputStream out = response.getOutputStream();
            FileInputStream fis = new FileInputStream(tempfile);

            CopyUtils.copy(fis,out);

            out.flush();
            out.close();
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
            if (fileOutputStream!=null) fileOutputStream.close();
        }
    }

}
