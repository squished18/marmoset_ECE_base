/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Apr 26, 2005
 *
 */
package edu.umd.cs.buildServer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;

/**
 * @author jspacco
 *
 */
public class StudentTestFinderRunner implements ISubmissionInspectionStep
{
    private TestOutcomeCollection collection = new TestOutcomeCollection();
    private ProjectSubmission projectSubmission;
    private Set<JUnitTestCase> publicTestSet;

    /**
     * @return the log
     */
    public Logger getLog() {
		return projectSubmission.getLog();
	}

    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.ISubmissionInspectionStep#setProjectSubmission(edu.umd.cs.buildServer.ProjectSubmission)
     */
    public void setProjectSubmission(ProjectSubmission projectSubmission)
    {
        this.projectSubmission = projectSubmission;
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.ISubmissionInspectionStep#execute()
     */
    public void execute() throws BuilderException
    {
        getLog().info("execute!");
        
        JUnitTestCaseFinder testFinder = new JUnitTestCaseFinder();
        
        File objectFileDir = getProjectSubmission().getBuildOutputDirectory();
		
        List<File> studentClassFileList =
            BuildServerUtilities.listClassFilesInDirectory(getProjectSubmission().getBuildOutputDirectory());
        
        for (Iterator<File> i = studentClassFileList.iterator(); i.hasNext();) {
            File classFile = i.next();
            try {
                testFinder.addClassPathEntry(objectFileDir.getAbsolutePath());
                testFinder.setInspectedClass(classFile);
                testFinder.findTestCases();
                if (testFinder.getTestCaseCollection().isEmpty())
        		{
        		    getLog().warn("No student tests found in " +classFile);
        		    continue;
        		}
                getLog().info("Found " + testFinder.getTestCaseCollection().size() +
        				" unit tests in class " + classFile);
            } catch (IOException e) {
                getLog().warn("Could not inspect class file " + classFile + " for JUnit tests", e);
                return;
            } catch (ClassNotFoundException e) {
                getLog().warn("Could not inspect class file " + classFile + " for JUnit tests", e);
                return;
            }
        }

        publicTestSet = buildPublicTestSet();
        for (Iterator<JUnitTestCase> ii=testFinder.getTestCaseCollection().iterator(); ii.hasNext();)
        {
            JUnitTestCase testCase = ii.next();
            if (publicTestSet.contains(testCase)) {
				getLog().info("Test " + testCase + " appears to be a public test");
				continue;
			}
            TestOutcome outcome = new TestOutcome();
            outcome.setTestRunPK(projectSubmission.getSubmissionPK());
            outcome.setTestType("student");
            outcome.setTestName(testCase.getMethodName());
            getTestOutcomeCollection().add(outcome);
            System.out.println(testCase.getMethodName());
        }
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.ISubmissionInspectionStep#getTestOutcomeCollection()
     */
    public TestOutcomeCollection getTestOutcomeCollection()
    {
        return collection;
    }

    /**
     * @return the projectSubmission
     */
    public ProjectSubmission getProjectSubmission()
    {
        return projectSubmission;
    }
    
    private Set<JUnitTestCase> buildPublicTestSet()
    {
		Set<JUnitTestCase> publicTestSet = new HashSet<JUnitTestCase>();
		String publicTestClass = projectSubmission.getTestProperties().getTestClass(TestOutcome.PUBLIC_TEST);
		if (publicTestClass != null) {
			JUnitTestCaseFinder publicTestFinder = new JUnitTestCaseFinder();
			publicTestFinder.addClassPathEntry(
					getProjectSubmission().getProjectJarFile().getAbsolutePath());
			try {
				publicTestFinder.setInspectedClass(publicTestClass);
				publicTestFinder.findTestCases();
				
				publicTestSet.addAll(publicTestFinder.getTestCaseCollection());
			} catch (ClassNotFoundException e) {
				getLog().warn("Could not inspect public tests", e);
			}
		}
		return publicTestSet;
	}
}
