package ca.uwaterloo.cs.submitServer.servlets;

import edu.umd.cs.submitServer.servlets.SubmitServerServlet;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Scanner;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.uwaterloo.cs.classlist.parser.ClassListParser;
import ca.uwaterloo.cs.classlist.parser.ClassListParseException;
import ca.uwaterloo.cs.classlist.UWStudent;
import ca.uwaterloo.cs.submitServer.UWStudentForUpload;
import ca.uwaterloo.cs.submitServer.RegistrationResult;

import edu.umd.cs.marmoset.modelClasses.Course;

import edu.umd.cs.submitServer.MultipartRequest;
import edu.umd.cs.submitServer.SubmitServerConstants;

import org.apache.commons.fileupload.FileItem;

/**
 * This class represents the servlet used for uploading UW students
 * from the .classlist file.
 *
 * @author ccchong
 */
public class RegisterUWStudents extends SubmitServerServlet {

  private static final String LIMIT_CHAR_NAME = "isLimitUserIDLength";
  private static final String LIMIT_CHAR_LENGTH_NAME = "limitUserIDLength";
  private static final String TO_TRUNCATE_YES = "yes";
  private static final String TO_TRUNCATE_NO = "no";
  private static final String DISPLAY_JSP_FORWARD_URL = 
    "/view/instructor/displayRegisterUWStudentsResult.jsp";
  private static final String LIST_RESULT_REQ_ATTRIBUTE =
    "registrationResult";

  /**
   * Handle http Post request.
   *
   * @param request the request send by the client to server
   * @param response the response send by the server to the client
   * @throws ServletException if an error occured
   * @throws IOException if an error occur
   */
  public void doPost(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
    FileItem fileItem = null;
    BufferedReader reader = null;
    Connection conn = null;
    try {
      conn = this.getConnection();
      MultipartRequest multipartRequest = (MultipartRequest) 
        req.getAttribute(MULTIPART_REQUEST);

      // Get the file item to be parsed
      fileItem = multipartRequest.getFileItem();

      // parse the classlist uploaded
      ClassListParser parser = ClassListParser.newInstance();
      List<UWStudent> list = parser.parse(fileItem.getInputStream());

      // iterate the list of students
      Iterator<UWStudent> iter = list.iterator();

      String toTrim = multipartRequest.getParameter(LIMIT_CHAR_NAME);
      int trimLength = 
        Integer.parseInt(multipartRequest.getParameter(LIMIT_CHAR_LENGTH_NAME));

      assert(toTrim.equals(TO_TRUNCATE_YES) || toTrim.equals(TO_TRUNCATE_NO));

      boolean truncate = toTrim.equals(TO_TRUNCATE_YES);

      // Get the course to be uploaded
      Course course = (Course) req.getAttribute(SubmitServerConstants.COURSE);

      // Create a new list that will be set as an attribute to 
      // pass to the display JSP
      List<RegistrationResult> registrationResult = 
        new LinkedList<RegistrationResult>();

      // iterate over the list of student and upload the data
      while (iter.hasNext()) {
        UWStudent curr = iter.next();
        registrationResult.add(UWStudentForUpload.registerStudent(course, 
              curr, conn, truncate, trimLength)); 
      }

      // Getting ready for forwarding to the new page
      RequestDispatcher dispatcher = 
        getServletContext().getRequestDispatcher(DISPLAY_JSP_FORWARD_URL);

      // Set the parameter on the request object
      req.setAttribute(LIST_RESULT_REQ_ATTRIBUTE, registrationResult);

      // Forward the request to the display JSP to display the result
      // of importing the students
      dispatcher.forward(req, res);

    } catch (ClassListParseException clpe) {
      throw new ServletException(clpe);
    } catch (SQLException sqle) {
      throw new ServletException(sqle);
    }
  }
}
