/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 16, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

import edu.umd.cs.marmoset.modelClasses.BackgroundRetest;
import edu.umd.cs.marmoset.modelClasses.CodeMetrics;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.ProjectJarfile;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestRun;
import edu.umd.cs.marmoset.utilities.JavaMail;
import edu.umd.cs.submitServer.InvalidRequiredParameterException;
import edu.umd.cs.submitServer.MultipartRequest;

/**
 * @author jspacco
 *
 */
public class ReportTestOutcomes extends SubmitServerServlet
{
	private static Logger failedLog;
	private static Logger getFailedBackgroundRetestLog()
	{
		if (failedLog==null) {
			failedLog=Logger.getLogger("edu.umd.cs.submitServer.servlets.failedBackgroundRetestLog");
		}
		return failedLog;
	}
    
    private static Logger successfulLog;
    private static Logger getSuccessfulBackgroundRetestLog()
    {
        if (successfulLog==null) {
            successfulLog = Logger.getLogger("edu.umd.cs.submitServer.servlets.successfulBackgroundRetestLog");
        }
        return successfulLog;
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
        // will be set by MultipartRequestFilter
        MultipartRequest multipartRequest = (MultipartRequest)request.getAttribute(MULTIPART_REQUEST);

        // Insert test outcomes into database,
        // create new TestRun
		// and update submission as having been tested.
		Connection conn = null;
		FileItem fileItem=null;
		boolean transactionSuccess= false;
		try {
		    conn=getConnection();

		    // Get submission pk and the submission
		    String submissionPK = multipartRequest.getStringParameter("submissionPK");
		    Submission submission = Submission.lookupBySubmissionPK(submissionPK, conn);
		    
		    // Get the projectJarfilePK
		    String projectJarfilePK = multipartRequest.getStringParameter("projectJarfilePK");
		    boolean newProjectJarfile = multipartRequest.getBooleanParameter("newProjectJarfile");
            boolean isBackgroundRetest = multipartRequest.getBooleanParameter("isBackgroundRetest");
		    
		    if (submission==null)
		    {
		        throw new ServletException("submissionPK " +submissionPK+ " does not refer to a submission in the database");
		    }
		    
		    // Get test machine (if specified)
		    String testMachine = multipartRequest.getOptionalStringParameter("testMachine");
		    if (testMachine == null)
		        testMachine = "unknown";
            getBuildServerMonitor().logReceiptOfBuildServerMessage(testMachine);
		    
		    // Get md5sum of classfiles (if specified)
            CodeMetrics codeMetrics=new CodeMetrics();
            codeMetrics.setMd5sumSourcefiles(multipartRequest.getOptionalStringParameter("md5sumClassfiles"));
            codeMetrics.setMd5sumClassfiles(multipartRequest.getOptionalStringParameter("md5sumSourcefiles"));
            if (multipartRequest.hasKey("codeSegmentSize")) {
                codeMetrics.setCodeSegmentSize(multipartRequest.getIntParameter("codeSegmentSize"));
            }
		    
		    // Get the fileItem 
		    fileItem = multipartRequest.getFileItem();
		    
		    // Read into TestOutcomeCollection in memory
		    TestOutcomeCollection testOutcomeCollection = new TestOutcomeCollection();

            // TODO Refactor this code!
            int testOutcomeSerializedSize = -1;
            int available1 = 0;
            int available2 = 0;
            ObjectInputStream in=null;
		    try {
		        byte[] data = fileItem.get();
		        testOutcomeSerializedSize = data.length;
		        in = new ObjectInputStream(new ByteArrayInputStream(data));
                available1 = in.available();
		        testOutcomeCollection.read(in);
                available2 = in.available();
		    } catch (IOException e) {
		        getSubmitServerServletLog().error("Could not read test outcomes from build server");
		        throw new ServletException(e);
		    } finally {
		    	if (in != null) in.close();
		    }
		    String logMe = "sz = " + testOutcomeSerializedSize + ", av1 = " + available1 + ", av2 = " + available2 + ", length = " + testOutcomeCollection.size();
            getSubmitServerServletLog().debug("report test outcome " + logMe);  // FIXME : get rid of when everything is working
            
		    // Make sure test outcome collection is not empty
		    if (testOutcomeCollection.isEmpty()) {
		        String msg = "No test outcomes received; " + logMe;
		        response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
		        return;
		    }
		    
		    logHotspotErrors(submissionPK, projectJarfilePK, testMachine, testOutcomeCollection);

		    // Begin a transaction.
		    // We can set this to a low isolation level because
		    //   - We don't read anything
		    //   - The inserts/updates we perform should not affect
		    //     rows visible to any other transaction
		    conn = getConnection();
		    conn.setAutoCommit(false);
		    conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		    
		    //
		    // * Create new TestRun row
		    // * increment numTestOutcomes in submissions table
		    // * set currentTestRunPK in submissions table
		    // * set testRunPK in all the testOutcomes
		    // * write the testoutcomes to the disk

		    Project project = Project.getByProjectPK(submission.getProjectPK(), conn);
            ProjectJarfile projectJarfile = ProjectJarfile.lookupByProjectJarfilePK(projectJarfilePK,conn);
            
            // TODO Handle partial credit for grades here?
            if (!newProjectJarfile) {
                // Set point totals
                TestRun canonicalTestRun = TestRun.lookupByTestRunPK(projectJarfile
                        .getTestRunPK(), conn);
                TestOutcomeCollection canonicalTestOutcomeCollection = TestOutcomeCollection
                        .lookupByTestRunPK(canonicalTestRun.getTestRunPK(), conn);
                Map<String, TestOutcome> canonicalTestOutcomeMap = new HashMap<String, TestOutcome>();
                for (TestOutcome testOutcome : canonicalTestOutcomeCollection.getAllOutcomes())
                {
                    canonicalTestOutcomeMap.put(testOutcome.getTestName(), testOutcome);
                }
                for (TestOutcome testOutcome : testOutcomeCollection.getAllOutcomes())
                {
                    // TODO should this check isTestType()???
                	if (!testOutcome.getTestType().equals(TestOutcome.FINDBUGS_TEST)
                			&& !testOutcome.getTestType().equals(TestOutcome.UNCOVERED_METHOD)
                            && canonicalTestOutcomeMap.containsKey(testOutcome
                                    .getTestName())) {
                        TestOutcome canonicalTestOutcome = canonicalTestOutcomeMap
                                .get(testOutcome.getTestName());
                        testOutcome.setPointValue(canonicalTestOutcome.getPointValue());
                    }
                }
            } else {
				// set all point values to 1
				for (TestOutcome testOutcome : testOutcomeCollection) {
					testOutcome.setPointValue(1);
				}
			}
            
            // Background Retests:
            // If this was a background re-test, then we need to take special steps.
            // This was a background-retest if:
            // 1) The BuildServer says that this was a background retest 
            // 2) or the status has been left as "complete" or has been explicitly marked "background"
            if (isBackgroundRetest ||
                (!newProjectJarfile &&
                    (submission.getBuildStatus().equals(Submission.COMPLETE) ||
                     submission.getBuildStatus().equals(Submission.BACKGROUND))))
            {
            	BackgroundRetest backgroundRetest = 
        			BackgroundRetest.lookupBySubmissionPKAndProjectJarfilePK(
        				submission.getSubmissionPK(), projectJarfilePK, conn);
        		if (backgroundRetest == null) {
        			backgroundRetest = new BackgroundRetest();
        			backgroundRetest.setSubmissionPK(submissionPK);
        			backgroundRetest.setProjectJarfilePK(projectJarfilePK);
        			backgroundRetest.insert(conn);
        		}
                
            	// Look up current testOutcomeCollection
        		TestOutcomeCollection currentTestOutcomeCollection =
            		TestOutcomeCollection.lookupByTestRunPK(
            			submission.getCurrentTestRunPK(),
            			conn);
                
                // Look up the testRun for the current TestOutcomeCollection
                TestRun currentTestRun = TestRun.lookupByTestRunPK(submission.getCurrentTestRunPK(), conn);
                if (!currentTestRun.getProjectJarfilePK().equals(projectJarfilePK)) {
                    // Retest was against a different jarfile than the current one;
                    // for now just ignore this run.
                    // TODO Find the testOutcomeCollection for correct jarfile and use that.
                    if (submission.getBuildStatus().equals(Submission.BACKGROUND)) {
                        submission.setBuildStatus(Submission.COMPLETE);
                        submission.update(conn);
                    }
                    transactionSuccess=true;
                    conn.commit();
                    return;
                }
                
            	// Compare the cardinal test outcomes from the two outcomeCollections 
        		String differences = compareCardinalOutcomes(
                    currentTestOutcomeCollection,
                    testOutcomeCollection,
                    submissionPK,
                    projectJarfilePK,
                    testMachine);
                   
                if (differences==null) {
        			// If the results are the same, great!  We have more confidence that this result is correct.
        			backgroundRetest.setNumSuccessfulBackgroundRetests(backgroundRetest.getNumSuccessfulBackgroundRetests()+1);
                    getSuccessfulBackgroundRetestLog().info("Corroborating run for submissionPK = " +submissionPK+
                        ", projectJarfilePK = " +projectJarfilePK+ " performed by " +testMachine);
                } else if (differences.equals("skip")) {
                    // There may have been differences but we don't care
                    // We don't re-test "could_not_run" results
                    conn.commit();
                    transactionSuccess= true;
                    return;
                } else {
            		// If the results differ, log which test cases were different 
            		backgroundRetest.setNumFailedBackgroundRetests(backgroundRetest.getNumFailedBackgroundRetests()+1);
            		getFailedBackgroundRetestLog().warn(differences);
            		// XXX Should I insert differing outcomes into the database as re-tests?
            	}
            	backgroundRetest.update(conn);
                
            	// If this was marked for an explicit background retest we need to set the 
            	// submission's buildStatus to complete.
            	if (!submission.getBuildStatus().equals(Submission.COMPLETE)) {
            		submission.setBuildStatus(Submission.COMPLETE);
            		submission.update(conn);
            	}
    		    
                // If there were no differences, commit what we've done and exit.
                if (differences==null) {
                    conn.commit();
                    transactionSuccess= true;
                    return;
                }
            }
            
		    // Create new TestRun row.
		    TestRun testRun = new TestRun();
		    testRun.setSubmissionPK(submissionPK);
		    testRun.setProjectJarfilePK(projectJarfilePK);
		    testRun.setTestMachine(testMachine);
		    testRun.setTestTimestamp(new Timestamp(System.currentTimeMillis()));
		    testRun.setValuePassedOverall(testOutcomeCollection.getValuePassedOverall());
		    testRun.setCompileSuccessful(testOutcomeCollection.isCompileSuccessful());
		    testRun.setValuePublicTestsPassed(testOutcomeCollection.getValuePublicTestsPassed());
		    testRun.setValueReleaseTestsPassed(testOutcomeCollection.getValueReleaseTestsPassed());
		    testRun.setValueSecretTestsPassed(testOutcomeCollection.getValueSecretTestsPassed());
		    testRun.setNumFindBugsWarnings(testOutcomeCollection.getNumFindBugsWarnings());
		    // set the md5sum for this testRun
            // XXX currently the md5sums are stored in both testRuns and codeMetrics tables 
		    if (codeMetrics.getMd5sumClassfiles() != null &&
                codeMetrics.getMd5sumSourcefiles() != null)
            {
		        testRun.setMd5sumClassfiles(codeMetrics.getMd5sumClassfiles());
                testRun.setMd5sumSourcefiles(codeMetrics.getMd5sumSourcefiles());
		    }

		    // perform insert
		    testRun.insert(conn);
            
            // Insert a new codeMetrics row if we have codeMetrics data to insert
            // codeMetrics data is keyed to the testRunPK
            if (codeMetrics.getCodeSegmentSize() > 0) {
                codeMetrics.setTestRunPK(testRun.getTestRunPK());
                codeMetrics.insert(conn);
            }
            
		    // update the testRunPK of the testOutcomes we've been sent from the BuildServer
		    // with the testRunPK of the row we just inserted 
		    testOutcomeCollection.updateTestRunPK(testRun.getTestRunPK());

		    // increment the number of test outcomes
		    submission.setNumTestRuns(submission.getNumTestRuns() + 1);
		    
		    // Next, insert the test outcomes
		    testOutcomeCollection.insert(conn);
		    
		    // if this was a new project jarfile being tested against the canonical account
		    if (newProjectJarfile)
		    {
		        // lookup the pending project

                projectJarfile.setValueTotalTests(testOutcomeCollection.getValuePassedOverall());
                projectJarfile.setValuePublicTests(testOutcomeCollection.getValuePublicTests());
                projectJarfile.setValueReleaseTests(testOutcomeCollection.getValueReleaseTests());
                projectJarfile.setValueSecretTests(testOutcomeCollection.getValueSecretTests());
             
                projectJarfile.setTestRunPK(testRun.getTestRunPK());
                
		        if (!testOutcomeCollection.isCompileSuccessful() || testOutcomeCollection.getNumFailedOverall() > 0)
		        {
		            // If any tests have failed, then set status to failed
		            projectJarfile.setJarfileStatus(ProjectJarfile.FAILED);
		        }
		        else
		        {
		            // TODO count num tests passed and set build/public/release stats
		            // set status to OK for this jarfile
		            projectJarfile.setJarfileStatus(ProjectJarfile.TESTED);
		             
		        }
		        // update pending project_jarfile to reflect the changes just made
		        projectJarfile.update(conn);
		    }

		    // Only change information about the current test_run if this this run 
		    // used the most recent projectJarfile
		    if (!isBackgroundRetest &&
                    (!submission.getBuildStatus().equals(Submission.COMPLETE) || 
                        testRun.getProjectJarfilePK().equals(project.getProjectJarfilePK())))
		    {
		        // update the pass/fail/warning stats
                submission.setCurrentTestRunPK(testRun.getTestRunPK());
		        submission.setReleaseEligible(testOutcomeCollection.isReleaseEligible());
		        submission.setValuePassedOverall(testOutcomeCollection.getValuePassedOverall());
		        submission.setCompileSuccessful(testOutcomeCollection.isCompileSuccessful());
		        submission.setValuePublicTestsPassed(testOutcomeCollection.getValuePublicTestsPassed());
		        submission.setValueReleaseTestsPassed(testOutcomeCollection.getValueReleaseTestsPassed());
		        submission.setValueSecretTestsPassed(testOutcomeCollection.getValueSecretTestsPassed());
		        submission.setNumFindBugsWarnings(testOutcomeCollection.getNumFindBugsWarnings());
                if (!isBackgroundRetest) {
                    // If we're re-setting the currentTestRunPK, then find any existing
                    // backgroundRetests and clear them.  Background re-tests are always compared
                    // to the current set of testOutcomes.
                    BackgroundRetest backgroundRetest = BackgroundRetest.lookupBySubmissionPKAndProjectJarfilePK(
                        submission.getSubmissionPK(),
                        projectJarfile.getProjectJarfilePK(),
                        conn);
                    if (backgroundRetest!=null) {
                        backgroundRetest.setNumSuccessfulBackgroundRetests(0);
                        backgroundRetest.setNumFailedBackgroundRetests(0);
                        backgroundRetest.update(conn);
                    }
                }                
		    }
		    // perform update
            // Update the status of the submission
            submission.setBuildStatus(Submission.COMPLETE);
		    submission.update(conn);

		    conn.commit();
		    transactionSuccess= true;
		} catch (InvalidRequiredParameterException e) {
		    throw new ServletException(e);
		} catch (SQLException e) {
		    throw new ServletException(e);
		} finally {
		    rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess, conn);
		    if (fileItem != null) fileItem.delete();
		}
    }

	/**
	 * @param submissionPK
	 * @param projectJarfilePK
	 * @param testMachine
	 * @param testOutcomeCollection
	 */
	private void logHotspotErrors(String submissionPK, String projectJarfilePK, String testMachine, TestOutcomeCollection testOutcomeCollection)
	{
		for (TestOutcome outcome : testOutcomeCollection) {
			if (outcome.getLongTestResult().contains(
					"An unexpected error has been detected by HotSpot Virtual Machine"))
			{
				getSubmitServerServletLog().error("SubmissionPK " +submissionPK+
						" for test-setup " +projectJarfilePK+
						" from buildServer " +testMachine+
						" had a HotSpot exception: " +outcome);
				try {
					JavaMail.sendMessage("jspacco@cs.umd.edu", "jspacco@cs.umd.edu", "smtp.cs.umd.edu", "HotSpot error!", outcome.toString());
				} catch (MessagingException e) {
					getSubmitServerServletLog().error("Unable to email me a warning about a HotSpot exception on one of the buildServers", e);
				}
			}
		}
	}
    
	/**
     * Compares two testOutcomeCollections that were run against the same projectJarfilePK
     * and produces a String suitable for entry in a log4j log summarizing the differences
     * between the two collections.  Used by the background retesting mechanism.
     * TODO It's ugly that this returns a String!
     * 
	 * @param oldCollection The collection currently in the database.
	 * @param newCollection The collection returned after a background re-test.
	 * @param submissionPK The submissionPK of the submission being retested.
	 * @param projectJarfilePK The projectJarfilePK of the projectJarfile used for the re-test.
	 * @return null if the two testOutcomeCollections are the same; a String suitable
     *      for writing in a log if there are differences
	 */
	private static String compareCardinalOutcomes(TestOutcomeCollection oldCollection,
        TestOutcomeCollection newCollection,
        String submissionPK,
        String projectJarfilePK,
        String testMachine)
	{
        for (TestOutcome newOutcome : newCollection) {
            // If any of the tests were marked "COULD_NOT_RUN" then don't record anything
            // into the DB
            if (newOutcome.getOutcome().equals(TestOutcome.COULD_NOT_RUN)) {
                getFailedBackgroundRetestLog().warn(newOutcome.getOutcome() +
                    " result for submissionPK " +
                    submissionPK+ " and projectJarfilePK " +projectJarfilePK);
                return null;
            }   
        }
        // TODO Handle when one compiles and the next doesn't compile.
        // NOTE: We're ignoring a lot of other info here like md5sums, clover info, etc.
        StringBuffer buf = new StringBuffer();
	    for (TestOutcome oldOutcome : oldCollection.getIterableForCardinalTestTypes()) {
            TestOutcome newOutcome = newCollection.getTest(oldOutcome.getTestName());
            
	        if (oldOutcome.getOutcome().equals(TestOutcome.COULD_NOT_RUN)) {
	            getFailedBackgroundRetestLog().warn("Not currently able to compare could not runs");
                return "skip";
            }
            if (newOutcome==null) {
	            // We're screwed here
	            throw new IllegalStateException("Can't find "+oldOutcome.getTestName()+
	            " in new testOutcomeCollection after background retest for " +
                " submissionPK " +submissionPK+ " and projectJarfilePK " +projectJarfilePK);
	        }
	        if (!oldOutcome.getOutcome().equals(newOutcome.getOutcome())) {
	            buf.append("\t" +oldOutcome.getTestName()+
	            ": outcome in database is " +oldOutcome.getOutcome()+
	            "; background retest produced "+newOutcome.getOutcome()+"\n");
	        }
	    }
	    if (buf.length()>0) {
	        buf.insert(0, "submissionPK = " +submissionPK+ 
	            ", projectJarfilePK = " +projectJarfilePK+" on "+
	            testMachine +":\n");
	        // Strip off last newline
            buf.delete(buf.length()-1,buf.length());
            return buf.toString();
	    }
	    return null;
	}
}
