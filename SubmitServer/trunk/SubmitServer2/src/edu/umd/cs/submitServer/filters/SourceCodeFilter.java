/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Apr 8, 2005
 *
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.UserSession;

/**
 * @author jspacco
 *
 */
public class SourceCodeFilter extends SubmitServerFilter
{

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;
        HttpSession session = request.getSession();
        UserSession userSession = (UserSession)session.getAttribute(USER_SESSION);
        Connection conn=null;
        try {
            conn=getConnection();
            
            RequestParser parser = new RequestParser(request);
            String sourceFileName = parser.getParameter("sourceFileName");

            Integer startHighlight = parser.getOptionalInteger("startHighlight");
            Integer numToHighlight = parser.getOptionalInteger("numToHighlight");
            Integer numContext = parser.getOptionalInteger("numContext");
            
            request.setAttribute("sourceFileName", sourceFileName);
			request.setAttribute("startHighlight", startHighlight);
			request.setAttribute("numToHighlight", numToHighlight);
			request.setAttribute("numContext", numContext);
			
            TestProperties testProperties=(TestProperties)request.getAttribute("testProperties");
            
			// Instructors can see code coverage results for any class of tests
			// Students can only see coverage results for student/public tests
			
			String testType = parser.getParameter(TEST_TYPE);
			String testNumber = parser.getParameter(TEST_NUMBER);
            String hybridTestType = parser.getParameter(HYBRID_TEST_TYPE);
			// Setting test type and testNumber as request attributes so that JSPs can use them
			// more easily
			request.setAttribute(TEST_TYPE, testType);
            request.setAttribute(TEST_NUMBER, testNumber);
            request.setAttribute(HYBRID_TEST_TYPE, hybridTestType);
			TestOutcomeCollection currentTestOutcomes = (TestOutcomeCollection)request.getAttribute("testOutcomeCollection");
			CodeCoverageResults codeCoverageResults=null;
			
			try {
			// Instructor's view of coverage
            if (testProperties != null &&
                testProperties.isJava() &&
                userSession.canActivateCapabilities() &&
                testProperties.isPerformCodeCoverage())
			{
				if (TestOutcome.FINDBUGS_TEST.equals(testType) || TestOutcome.UNCOVERED_METHOD.equals(testType)) {
					// display union of public and student tests
				    codeCoverageResults = currentTestOutcomes.getOverallCoverageResultsForPublicAndStudentTests();
                }
				else if ("all".equals(testNumber)) {
					if (TestOutcome.STUDENT_TEST.equals(testType)) {
						// Get coverage for all the student tests
						codeCoverageResults = currentTestOutcomes.getOverallCoverageResultsForStudentTests();
						getSubmitServerFilterLog().trace("instructor " +testType+ ", "+testNumber);
					} else if (TestOutcome.PUBLIC_TEST.equals(testType)) {
						// Get coverage for all the public tests.
                        codeCoverageResults = currentTestOutcomes.getOverallCoverageResultsForPublicTests();
						getSubmitServerFilterLog().trace("instructor " +testType+ ", "+testNumber);
					} else if (TestOutcome.RELEASE_TEST.equals(testType)) {
						// Get coverage for all the release tests.
                        codeCoverageResults = currentTestOutcomes.getOverallCoverageResultsForReleaseTests();
						getSubmitServerFilterLog().trace("instructor " +testType+ ", "+testNumber);
					} else if (TestOutcome.SECRET_TEST.equals(testType)) {
						// Get coverage for all the secret tests.
                        codeCoverageResults = currentTestOutcomes.getOverallCoverageResultsForSecretTests();
                        getSubmitServerFilterLog().trace("instructor " +testType+ ", "+testNumber);
                    } else if (SubmitServerConstants.RELEASE_UNIQUE.equals(testType)) {
                        // Get coverage for all the release tests
                        // excluding anything covered by a public or student test
                        codeCoverageResults = currentTestOutcomes.getOverallCoverageResultsForReleaseTests();
                        CodeCoverageResults publicStudentCoverage=currentTestOutcomes.getOverallCoverageResultsForPublicAndStudentTests();
                        codeCoverageResults.excluding(publicStudentCoverage);
                        getSubmitServerFilterLog().trace("instructor " +testType+ ", "+testNumber);
                    } else if (SubmitServerConstants.PUBLIC_STUDENT.equals(testType)) {
                        codeCoverageResults=currentTestOutcomes.getOverallCoverageResultsForPublicAndStudentTests();
                        getSubmitServerFilterLog().trace("instructor " +testType+ ", "+testNumber);
                    } else if (SubmitServerConstants.CARDINAL.equals(testType)) {
                        codeCoverageResults=currentTestOutcomes.getOverallCoverageResultsForCardinalTests();
                        request.setAttribute(SubmitServerConstants.TEST_TYPE, "public/release/secret");
                        getSubmitServerFilterLog().trace("instructor " +testType+ ", "+testNumber);
                    } else {
						// TODO Create a link so instructors can get all test results
						// TODO Handle combinations of test cases
						codeCoverageResults = currentTestOutcomes.getOverallCoverageResultsForCardinalTests();
                        request.setAttribute(SubmitServerConstants.TEST_TYPE, "public/release/secret");
                        getSubmitServerFilterLog().trace("instructor " +testType+ ", "+testNumber);
					}
				} else {
					if (testNumber != null && testType != null) {
					    if (RELEASE_UNIQUE.equals(hybridTestType)) {
//                          // For release-unique, exclude coverage for all public/student written tests
                            codeCoverageResults = currentTestOutcomes.getOutcomeByTestTypeAndTestNumber(TestOutcome.RELEASE_TEST, testNumber).getCodeCoverageResults();
                            codeCoverageResults.excluding(currentTestOutcomes.getOverallCoverageResultsForPublicAndStudentTests());
                        } else if (FAILING_ONLY.equals(hybridTestType)) {
                            // For failing-only, exclude coverage by all passing tests
                            codeCoverageResults=currentTestOutcomes.getOutcomeByTestTypeAndTestNumber(testType, testNumber).getCodeCoverageResults();
                            codeCoverageResults.excluding(currentTestOutcomes.getOverallCoverageResultsForAllPassingTests());
                        } else {
                            // otherwise just get coverage for the request test
                            codeCoverageResults = currentTestOutcomes.getOutcomeByTestTypeAndTestNumber(testType, testNumber).getCodeCoverageResults();
                        }
						getSubmitServerFilterLog().trace("instructor specific " +testType+ ", "+testNumber);
					}
				}
			} else if (testProperties != null &&
                testProperties.getLanguage().equalsIgnoreCase(Project.JAVA) &&
                !userSession.canActivateCapabilities() &&
                testProperties.isPerformCodeCoverage()) {
				// TODO students should be allowed to see the combination of student/public tests
                if (TestOutcome.FINDBUGS_TEST.equals(testType) ||
                    TestOutcome.UNCOVERED_METHOD.equals(testType))
                {
                    // for FindBugs or UncoveredMethod, show coverage for all public/student tests 
                    codeCoverageResults = currentTestOutcomes.getOverallCoverageResultsForPublicAndStudentTests();
                } else if ("all".equals(testNumber)) {
					if (TestOutcome.STUDENT_TEST.equals(testType)) {
						// Get coverage for all the student tests
						codeCoverageResults = currentTestOutcomes.getOverallCoverageResultsForStudentTests();
						getSubmitServerFilterLog().trace("student " +testType+ ", "+testNumber);
					} else if (TestOutcome.PUBLIC_TEST.equals(testType)) {
						// Get coverage for all the public tests
						codeCoverageResults = currentTestOutcomes.getOverallCoverageResultsForPublicTests();
						getSubmitServerFilterLog().trace("public " +testType+ ", "+testNumber);
					}
				} else {
					if (TestOutcome.STUDENT_TEST.equals(testType) || 
							TestOutcome.PUBLIC_TEST.equals(testType))
					{
						codeCoverageResults = currentTestOutcomes.getOutcomeByTestTypeAndTestNumber(testType, testNumber).getCodeCoverageResults();
						getSubmitServerFilterLog().trace("specific student " +testType+ ", "+testNumber);
					}
				}
			}
			} catch(Exception e) {
				getSubmitServerFilterLog().error("Error getting code coverage", e);
			}
			if (codeCoverageResults != null)
				request.setAttribute("codeCoverageResults", codeCoverageResults);
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
        chain.doFilter(request, response);
    }

}
