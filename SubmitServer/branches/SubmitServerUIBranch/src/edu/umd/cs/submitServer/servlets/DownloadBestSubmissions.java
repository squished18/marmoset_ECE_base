/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Feb 8, 2005
 *
 */
package edu.umd.cs.submitServer.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.CopyUtils;
import org.apache.commons.io.IOUtils;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.StudentSubmitStatus;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.ZipFileAggregator;

/**
 * @author jspacco
 *
 */
public class DownloadBestSubmissions extends SubmitServerServlet
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
        File tempfile = null;
        FileOutputStream fileOutputStream=null;
        FileInputStream fis=null;
        try {
            conn=getConnection();

            // get the project and all the student registrations
            Map bestSubmissionMap = (Map)request.getAttribute("bestSubmissionMap");

            Project project = (Project)request.getAttribute("project");
            Set registrationSet = (Set)request.getAttribute("studentRegistrationSet");
            Map studentSubmitStatusMap = (Map)request.getAttribute("studentSubmitStatusMap");
            
            // write everything to a tempfile, then send the tempfile
            tempfile = File.createTempFile("temp", "zipfile");
            fileOutputStream = new FileOutputStream(tempfile);
            
            // zip aggregator
            ZipFileAggregator zipAggregator = new ZipFileAggregator(fileOutputStream);

            for (Iterator ii=registrationSet.iterator(); ii.hasNext();)
            {
                StudentRegistration registration = (StudentRegistration)ii.next();
                StudentSubmitStatus studentSubmitStatus = 
                    (StudentSubmitStatus)studentSubmitStatusMap.get(registration.getStudentRegistrationPK());
                
                Submission submission = (Submission)bestSubmissionMap.get(registration.getStudentRegistrationPK());
                if (submission != null)
                {
                    try {
                        byte[] bytes = submission.downloadArchive(conn);
                        zipAggregator.addFileFromBytes(registration.getCvsAccount() +"-"+ submission.getStatus(), bytes);
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
            fis= new FileInputStream(tempfile);

            CopyUtils.copy(fis,out);
            
            out.flush();
            out.close();
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
            if (!tempfile.delete())
                getSubmitServerServletLog().warn("Unable to delete temporary file " +tempfile.getAbsolutePath());
            IOUtils.closeQuietly(fileOutputStream);
            IOUtils.closeQuietly(fis);
        }
    }

}
