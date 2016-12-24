package edu.umd.cs.submitServer.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.CopyUtils;

import edu.umd.cs.marmoset.modelClasses.Project;

public class DownloadProjectStarterFiles extends SubmitServerServlet
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
        Project project=(Project)request.getAttribute(PROJECT);
        Connection conn=null;
        try {
            conn = getConnection();
            
            byte[] bytes = project.downloadArchive(conn);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            
            // Inform client of content type and length
            response.setContentLength(bytes.length);
            response.setContentType("application/zip");

            String filename = project.getProjectNumber() +"-starter-files.zip";

            String disposition =  "attachment; filename=\""+filename+"\"";
            response.setHeader("Content-Disposition",disposition);
            response.setHeader("Cache-Control","private");
            response.setHeader("Pragma","IE is broken");

            OutputStream out = response.getOutputStream();
            CopyUtils.copy(bais,out);
            out.flush();
            out.close();
            bais.close();
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }

}
