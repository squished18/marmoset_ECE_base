package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.ZipFileAggregator;
import edu.umd.cs.marmoset.modelClasses.ZipFileAggregator.BadInputZipFileException;

public class ExportProject extends SubmitServerServlet
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
        try {
            conn=getConnection();
            Project project=(Project)request.getAttribute(PROJECT);
            Course course=(Course)request.getAttribute(COURSE);
            
            String filename=project.getProjectNumber() +
                "-" +course.getCourseName()+
                "-" +course.getSemester().replace(" ", "")+
                "-export.zip";
            String disposition =  "attachment; filename=\""+filename+"\"";
            response.setHeader("Content-Disposition",disposition);
            response.setHeader("Cache-Control","private");
            response.setHeader("Pragma","IE is broken");
            
            ServletOutputStream out=response.getOutputStream();
            
            project.exportProject(conn, out);

        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }

}
