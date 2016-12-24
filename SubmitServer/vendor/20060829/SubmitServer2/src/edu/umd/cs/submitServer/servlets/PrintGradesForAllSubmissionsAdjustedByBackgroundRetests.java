package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

public class PrintGradesForAllSubmissionsAdjustedByBackgroundRetests extends
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
        try {
            conn=getConnection();
            
            Project project = (Project)request.getAttribute("project");
            Map<String, StudentRegistration> registrationMap = (Map<String,StudentRegistration>)request.getAttribute("studentRegistrationMap");
            
            response.setContentType("text/plain");
            response.setHeader("Content-Disposition", "attachment; filename=project-" + project.getProjectNumber()+ "-grades.csv");
            response.setHeader("Cache-Control","private");
            response.setHeader("Pragma","IE is broken");
            PrintWriter out = response.getWriter();
            
            // get the outcome from the canonical run; we'll use this to retrieve the names of the test cases
            TestOutcomeCollection canonicalCollection = TestOutcomeCollection.lookupCanonicalOutcomesByProjectPK(
                    project.getProjectPK(),
                    conn);

            // format and print the header
            String header = "classAccount,timestamp,UTC,total";
            for (TestOutcome outcome : canonicalCollection) {
                if (outcome.getTestType().equals(TestOutcome.BUILD_TEST)) continue;
                header += "," +outcome.getTestType() +"_"+ outcome.getTestName();
            }
            out.println(header);
            
            // Look up all submissions for this project
            List<Submission> allSubmissions = Submission.lookupAllByProjectPK(project.getProjectPK(), conn);
            
            for (Submission submission : allSubmissions) {
                // Get the studentRegistration associated with this submission
                StudentRegistration registration = registrationMap.get(submission.getStudentRegistrationPK());
                // Only interested in student submissions
                if (registration != null && 
                        registration.getInstructorLevel() == StudentRegistration.STUDENT_CAPABILITY_LEVEL)
                {
                    // Adjust scores for background retests
                    TestOutcomeCollection testOutcomeCollection = submission.setAdjustScoreBasedOnFailedBackgroundRetests(conn);
                    String result = registration.getCvsAccount() +","+
                        submission.getSubmissionTimestamp() +","+
                        submission.getSubmissionTimestamp().getTime() +","+
                        submission.getValuePassedOverall();
                    for (TestOutcome outcome : testOutcomeCollection) {
                        // Skip anything that is not a cardinal test type (public,release,secret)
                        if (!outcome.isCardinalTestType()) continue;
                        
                        if (outcome.getOutcome().equals(TestOutcome.PASSED))
                        {
                            result += "," +outcome.getPointValue();
                        }
                        else
                        {
                            result += ",0";
                        }
                    }
                    out.println(result);
                }
            }

            out.flush();
            out.close();
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }
}
