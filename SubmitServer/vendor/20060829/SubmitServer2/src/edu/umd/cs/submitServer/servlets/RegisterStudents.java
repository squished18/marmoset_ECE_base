/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 21, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.utilities.MarmosetUtilities;
import edu.umd.cs.submitServer.MultipartRequest;
import edu.umd.cs.submitServer.StudentForUpload;

/**
 * @author jspacco
 *
 */
public class RegisterStudents extends SubmitServerServlet
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
        Connection conn=null;
        BufferedReader reader=null;
        FileItem fileItem =null;
        try {
            conn=getConnection();
            
            // MultipartRequestFilter is required
            MultipartRequest multipartRequest = (MultipartRequest)request.getAttribute(MULTIPART_REQUEST);
            
            Course course = (Course)request.getAttribute("course");
            boolean genPassword = "generic".equals(multipartRequest.getParameter("authenticateType")); // [NAT P003]
            
            // open the uploaded file
            fileItem = multipartRequest.getFileItem();
            reader = new BufferedReader(new InputStreamReader(fileItem.getInputStream()));
            
            // prepare a response
            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();

            // [NAT P003] Init storage for display of generated passwords
            StringBuilder passwordList = new StringBuilder("Some passwords were " +
            		"automatically generated. Please save this information:\n");
            boolean printPasswords = false;
            // [end NAT P003]
            

			int lineNumber = 1;

			while (true) {
			    String line = reader.readLine();
                if (line == null) break;
                
                // hard-coded skip of first two lines from grades.cs.umd.edu
                if (line.startsWith("Last,First,UID,section,ClassAcct,DirectoryID")) break;
                if (line.startsWith(",,,,,")) break;
                
				lineNumber++;

				// strip out comments starting with #
				line = line.split("#")[0];
				
				// skip blank lines
				if (line.trim().equals("")) // [NAT] more robust, trim off spaces
					continue;

				try {
					StudentForUpload s = new StudentForUpload(line, delimiter, genPassword);
					
					out.println(StudentForUpload.registerStudent(course, s, "student", conn));
					
					if (s.hasGeneratedPassword()) {
						passwordList.append(s.toPasswordString() + "\n");
						printPasswords = true;
					}
					
				} catch (IllegalStateException e) {
				    out.println(e.getMessage());
				    e.printStackTrace(out);
				} catch (Exception e1) {
				    out.println("Problem processing line: '" +line+ 
							"' at line number: " +lineNumber);
					e1.printStackTrace(out);
				}
			}
			
			if (printPasswords) {
				out.println();
				out.println(passwordList.toString());
			}
			
            out.flush();
            out.close();
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
            if (reader != null) reader.close();
            if (fileItem != null) fileItem.delete();
        }
    }

	/**
     * The delimiter used in the uploaded files of students for registration.
     */
     String delimiter = ",";
    
	/* (non-Javadoc)
	 * @see edu.umd.cs.submitServer.servlets.SubmitServerServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		super.init();
		String delimiter= getServletContext().getInitParameter("register.students.delimiter");
		if (delimiter!=null)
			this.delimiter=delimiter;
	}

}
