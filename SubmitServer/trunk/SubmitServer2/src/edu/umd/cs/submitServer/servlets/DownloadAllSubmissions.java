package edu.umd.cs.submitServer.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.CopyUtils;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.ZipFileAggregator;

public class DownloadAllSubmissions extends SubmitServerServlet
{

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        Connection conn=null;
        
        try {
            conn=getConnection();

            Project project = (Project)request.getAttribute("project");
            Map<String,String> map=StudentRegistration.lookupStudentRegistrationMapByProjectPK(
                project.getProjectPK(),
                conn);
            
            // Write everything to a byte-array output stream.
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            
            // Zip aggregator
            ZipFileAggregator zipAggregator = new ZipFileAggregator(baos);

            List<Submission> allSubmissions=Submission.lookupAllByProjectPK(project.getProjectPK(),conn);
            for (Submission submission : allSubmissions) {
                try {
                    zipAggregator.addFileFromBytes(map.get(submission.getStudentRegistrationPK()) +"-"+ submission.getSubmissionNumber(),
                        submission.downloadArchive(conn));
                } catch (ZipFileAggregator.BadInputZipFileException ignore) {
                    // Ignore, since students could submit things that aren't zipfiles
                    // and I don't want the entire download to fail because of that.
                    getSubmitServerServletLog().warn(ignore.getMessage(), ignore);
                }
            }
            
            zipAggregator.close();

            // write the zipfile to the client
            response.setContentType("application/zip");
            response.setContentLength((int)baos.size());

            // take into account MS IE's inability to download zipfiles
            String filename = project.getProjectNumber() +"-all.zip";
            String disposition = "attachment; filename=\""+filename+"\"";
            response.setHeader("Content-Disposition",disposition);
            response.setHeader("Cache-Control","private");
            response.setHeader("Pragma","IE is broken");

            ServletOutputStream out = response.getOutputStream();

            CopyUtils.copy(baos.toByteArray(),out);
            
            out.flush();
            out.close();
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }
}
