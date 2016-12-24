/**
 * Copyright (C) 2006, University of Maryland
 * All Rights Reserved
 * Created on Jul 15, 2006
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.utilities;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.FuzzyBugComparator;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.workflow.BugHistory;
import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.modelClasses.FileNameLineNumberPair;
import edu.umd.cs.marmoset.modelClasses.Snapshot;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;


/**
 * FalsePositiveChecker
 * @author jspacco
 */
public class EvaluateFindBugsAccuracyUsingMarmosetDataset
{

    private static void usage() {
        System.err.println("Usage: java edu.umd.cs.marmoset.utilities.EvaluateEvaluateFindBugsAccuracyUsingMarmosetDataset " +
            " <exceptionClassName> <warningPrefix> <FP or FN>");
    }
    
    private static boolean OVERRIDE=false;
    private static String[] override = new String[] {"java.lang.StackOverflowError","IL","FN"};
    /**
     * @param args
     */
    public static void main(String[] args)
    throws SQLException, IOException
    {
        if (OVERRIDE)
            args = override;
        if (args.length < 3) {
            usage();
            System.exit(1);
        }
        String exceptionClassName=args[0];
        String warningPrefix=args[1];
        String type=args[2];
        
        Connection conn=null;
        try {
            conn=DatabaseUtilities.getConnection("jdbc:mysql://marmoset2:7306/submitserver");
            
            if (type.equals("FP"))
                checkFalsePositives(warningPrefix, exceptionClassName, conn);
            else if (type.equals("FN"))
                checkFalseNegatives(warningPrefix, exceptionClassName, conn);
            else {
                usage();
                return;
            }
            
        } finally {
            DatabaseUtilities.releaseConnection(conn);
        }
    }

    public static void checkFalseNegatives(
        String warningPrefix,
        String exceptionClassName,
        Connection conn)
    throws SQLException, IOException
    {
        AccuracyContainer accuracyContainerPerException=new AccuracyContainer();
        
        long startTime=System.currentTimeMillis();
        List<List<Snapshot>> metaList=Snapshot.lookupMetaSnapshotsWithRuntimeException(
            exceptionClassName,
            conn);
        long time=System.currentTimeMillis()-startTime;
        System.out.println("Lookup took " +(time/1000)+ " seconds.");
        for (List<Snapshot> list : metaList) {
            
            if (list.size() < 1){
                //System.out.println("Empty list in the meta-list; that doesn't make much sense!");
                continue;
            }
            // Handle very first element specially
            evaluateExceptionsForFalseNegativesPerException(
                list.get(0),
                warningPrefix,
                exceptionClassName,
                accuracyContainerPerException,
                conn);
            for (int ii=1; ii < list.size(); ii++) {
                Snapshot current=list.get(ii);
                Snapshot previous=list.get(ii-1);
                
                // Skip if this is part of the same "cluster"
                if (previous.getSubmissionPK().equals(current.getPreviousSubmissionPK())) {
                    //System.out.println("skip " +current.getSubmissionPK());
                    continue;
                }

                evaluateExceptionsForFalseNegativesPerException(
                    current,
                    warningPrefix,
                    exceptionClassName,
                    accuracyContainerPerException,
                    conn);
            }
        }
        System.out.println("accurate warnings: " +accuracyContainerPerException.accurateResults);
        System.out.println("false positives: " +accuracyContainerPerException.falseResults);
    }
    
    /**
     * @param current
     * @param warningPrefix
     * @param exceptionClassName
     * @param accuracyContainer
     * @param conn
     * @throws SQLException
     * @throws IOException
     */
    private static void evaluateExceptionsForFalseNegativesPerException(Snapshot current, String warningPrefix, String exceptionClassName, AccuracyContainer accuracyContainer, Connection conn) throws SQLException, IOException
    {
        // XXX This is the one I'm working on...
        // Check that each exception covers a warning (we're measuring per exception)
        TestOutcomeCollection currentOutcomeCollection=
            TestOutcomeCollection.lookupBySubmissionPK(current.getSubmissionPK(),conn);
        
        List<TestOutcome> warningList=currentOutcomeCollection.getFindBugsOutcomesWithWarningPrefix(warningPrefix);
        
        // We're going to map the shortTestResult to its corresponding TestOutcome 
        // (shortTestResult is essentially the first line of the stack trace and is
        // a reasonable way of detecting when two exceptions are "the same".
        // Otherwise we're stuck with measure per exception (which overcounts)
        // or per snapshot, which is highly inaccurate.
        Map<ExceptionKey,CodeCoverageResults> map=new HashMap<ExceptionKey,CodeCoverageResults>();
        
        // Figure out:
        // XXX Documentation is out of date
        // 1) Which exceptions are "unique" (where a unique exception has a shortTestResult
        //      unique to this TestOutcomeCollection (shortTestResult is the most
        //      specific line of the stack trace that's in student code))
        // 2) The overall coverage for each "unique" exception:  We need the overall
        //      coverage for all exceptions that are 
        for (TestOutcome outcome : currentOutcomeCollection.getAllTestOutcomes()) {
            // Find each test case failing due to the exception type we're interested in. 
            if (outcome.isError() &&
                exceptionClassName.equals(outcome.getExceptionClassName())) {
                // If we've never seen this exception-location before,
                // then add a fresh CodeCoverageResults container
                
                // NOTE: If this is the IL (infinite-loop) warning, then
                // we're going to use the most common line of the full stack-trace instead
                // since this is much more likely to be the actual problem 
                ExceptionKey exceptionKey=getExceptionKey(outcome,warningPrefix);
                if (exceptionKey.getStackTraceElement()==null) {
                    System.err.println("null ExceptionKey for " +current.getSubmissionPK()+
                        ": "+outcome.toConciseString());
                    continue;
                }
                
                if (!map.containsKey(exceptionKey)) {
                    map.put(exceptionKey,new CodeCoverageResults());
                }
                // Now union the codeCoverageResults for this exception
                // with any other coverage information we have from previous
                // exceptions
                CodeCoverageResults codeCoverageResults=map.get(exceptionKey);
                codeCoverageResults.union(outcome.getCodeCoverageResults());
            }
        }
        
        // Keep in mind that shortTestResult means "innermost stack trace frame"
        // Now iterate through our map of shortTestResults 
        // to codeCoverageResults and see if any test case that triggered this exception
        // covers an appropriate warning
        for (Map.Entry<ExceptionKey,CodeCoverageResults> entry : map.entrySet()) {
            ExceptionKey exceptionKey=entry.getKey();
            CodeCoverageResults codeCoverageResults=entry.getValue();
            
            String line=current.getProjectPK() +"\t"+
            current.getStudentRegistrationPK() +"\t"+    
            current.getSubmissionPK() +"\t"+
            exceptionKey +"\t";

            boolean accurateWarning=false;
            for (TestOutcome warning : warningList) {
                FileNameLineNumberPair pair=TestOutcome.getFileNameLineNumberPair(warning.getShortTestResult());
                // Assert that the warnings we're looking for have line-number
                // information!
                if (codeCoverageResults.coversFileAtLineNumber(
                    pair.getFileName(),pair.getLineNumber()))
                {
                    System.out.println(line +"\t"+ 
                        warning.getShortTestResult() +"\t"+
                        "ACCURATE EXCEPTION");
                    
                    // Yay, an accurate warning!
                    // The warning is covered by one of the test cases that fail
                    // due to the exception type we're interested in.
                    accuracyContainer.accurateResults++;
                    accurateWarning=true;
                    break;
                }
            }
            
            if (!accurateWarning) {
                System.out.println(line +"\tFALSE NEGATIVE");
                
                // If we didn't find a warning that the failing test case
                // covers, then mark this exception as a false negative
                accuracyContainer.falseResults++;
            }
        }
    }
    
    private static ExceptionKey getExceptionKey(TestOutcome outcome, String warningPrefix)
    {
        if (warningPrefix.startsWith("IL")) {
            return new ExceptionKey(getMostCommonStackTraceFrame(outcome));
        }
        return new ExceptionKey(outcome.getInnermostStackTraceElement());
    }

    private static void inc(Map<String,Integer> map, String key)
    {
        if (!map.containsKey(key)) {
            map.put(key,0);
        }
        map.put(key, map.get(key)+1);
    }
    
    private static String getMostCommonStackTraceFrame(TestOutcome outcome)
    {
        Map<String,Integer> map=new HashMap<String,Integer>();
        try {
            TextFileReader reader=new TextFileReader(new StringReader(outcome.getLongTestResult()));
            for (String line : reader) {
                inc(map,line);
            }
            
            int max=0;
            String shortTestResult=null;
            for (Map.Entry<String,Integer> entry : map.entrySet()) {
                if (entry.getValue() > max) {
                    max=entry.getValue();
                    shortTestResult=entry.getKey();
                }
            }
            return shortTestResult;
        } catch (IOException e) {
            return outcome.getShortTestResult();
        }
    }

    /**
     * @param current
     * @param warningPrefix
     * @param exceptionClassName
     * @param accuracyContainer
     * @param conn
     * @throws SQLException
     * @throws IOException
     */
    private static void evaluateExceptionsForFalseNegativesPerSnapshot(
        Snapshot current,
        String warningPrefix,
        String exceptionClassName,
        AccuracyContainer accuracyContainer,
        Connection conn)
    throws SQLException, IOException
    {
        // Check that each exception covers a warning (we're measuring per exception)
        TestOutcomeCollection currentOutcomeCollection=
            TestOutcomeCollection.lookupBySubmissionPK(current.getSubmissionPK(),conn);
        
        List<TestOutcome> warningList=currentOutcomeCollection.getFindBugsOutcomesWithWarningPrefix(warningPrefix);
        
        boolean accurateWarning=false;
        for (TestOutcome outcome : currentOutcomeCollection) {
            // Find each test case failing due to an exception 
            // of the type we're interested in.
            if (exceptionClassName.equals(outcome.getExceptionClassName())) {
                // Check that one of the warnings of the type we're interested
                // in is covered by the failing test case.
                for (TestOutcome warning : warningList) {
                    FileNameLineNumberPair pair=TestOutcome.getFileNameLineNumberPair(warning.getShortTestResult());
                    // Assert that the warnings we're looking for have line-number
                    // information!
                    if (outcome.coversFileAtLineNumber(
                        pair.getFileName(),pair.getLineNumber()))
                    {
                        System.out.println("Match for " +current.getSubmissionPK()+
                            ", warning: " +warning.getShortTestResult());
                        
                        // Yay, an accurate warning!
                        // The warning is covered by the test case failing due
                        // to the exception type we're interested in.
                        accuracyContainer.accurateResults++;
                        accurateWarning=true;
                        break;
                    }
                }
                if (accurateWarning) {
                    break;
                }
            }
        }
        if (!accurateWarning) {
            System.out.println("FP for entire snapshot: " +current.getSubmissionPK());
            // If we didn't find a warning that the failing test case
            // covers, then mark this exception as a false negative
            accuracyContainer.falseResults++;
        }
    }
    
    public static void checkFalsePositives(
        String warningPrefix,
        String exceptionClassName,
        Connection conn)
    throws SQLException, IOException
    {
        AccuracyContainer accuracyContainer=new AccuracyContainer();
        
        List<List<Snapshot>> metaList=Snapshot.lookupMetaSnapshotsWithFindBugsWarning(
            warningPrefix,
            conn);
        for (List<Snapshot> list : metaList) {
            for (int ii=0; ii < list.size()-1; ii++) {
                Snapshot current=list.get(ii);
                Snapshot next=list.get(ii+1);

                evaluateWarningsForFalsePositives(
                    current,
                    next, 
                    warningPrefix,
                    exceptionClassName,
                    accuracyContainer,
                    conn);
            }
            if (list.size() > 0) {
                evaluateWarningsForFalsePositives(
                list.get(list.size()-1),
                null,
                warningPrefix,
                exceptionClassName,
                accuracyContainer,
                conn);
            }
            
        }
        System.out.println("accurate warnings: " +accuracyContainer.accurateResults);
        System.out.println("false positives: " +accuracyContainer.falseResults);
    }
    
    private static class AccuracyContainer
    {
        int accurateResults=0;
        int falseResults=0;
    }
    /**
     * @param current
     * @param next
     * @param warningPrefix
     * @param exceptionClassName
     * @param accuracyContainer
     * @param conn
     * @throws SQLException
     * @throws IOException
     */
    private static void evaluateWarningsForFalsePositives(
        Snapshot current,
        Snapshot next,
        String warningPrefix,
        String exceptionClassName,
        AccuracyContainer accuracyContainer,
        Connection conn)
    throws SQLException, IOException
    {
        TestOutcomeCollection currentOutcomeCollection=TestOutcomeCollection.lookupBySubmissionPK(current.getSubmissionPK(),conn);
        SortedBugCollection currentBugCollection=getCoveredFindBugsWarningsFromCollection(
            warningPrefix,
            currentOutcomeCollection);
        
        Collection<BugInstance> collection=null;
        if (next!=null) {
            // If next!=null, then compute the removed warnings between current snapshot
            // and the next snapshot; those are the interesting warnings we're using
            // to compute our stats
            TestOutcomeCollection nextOutcomeCollection=TestOutcomeCollection.lookupBySubmissionPK(next.getSubmissionPK(),conn);
            SortedBugCollection nextBugCollection=getCoveredFindBugsWarningsFromCollection(
            warningPrefix,
            nextOutcomeCollection);
            
            BugHistory bugHistory=new BugHistory(currentBugCollection, nextBugCollection);
            bugHistory.setComparator(new FuzzyBugComparator());
            bugHistory.performSetOperation(BugHistory.REMOVED_WARNINGS);
            
            collection=bugHistory.getResultCollection().getCollection();
        } else {
            // If next==null, simply use the bug collection for the current snapshot
            collection=currentBugCollection.getCollection();
        }
            
        // More than one bug may have been removed
        // So we need to look at each removed bug instance separately 
        // (i.e. we cannot simply check that any warning matches any exception and say we're done)
        for (BugInstance bugInstance : collection) {
            String line=current.getProjectPK() +"\t"+
                current.getStudentRegistrationPK() +"\t"+    
                current.getSubmissionPK() +"\t"+
                toUsefulString(bugInstance) +"\t"+
                ((next!=null) ?
                    next.getSubmissionPK() :
                    "in final snapshot");
            boolean accurateWarning=false;
            // Check for a test outcome that fails due to a runtime-exception that
            // corresponds to the warning we've been looking at
            for (TestOutcome outcome : currentOutcomeCollection.getFailingCardinalOutcomesDueToException(exceptionClassName)) {
                // HACK alert:  There's already code written in TestOutcome that parses
                // the filename and lineNumber out of the string representation 
                // of a FB warning
                FileNameLineNumberPair pair=TestOutcome.getFileNameLineNumberPair(bugInstance.getPrimarySourceLineAnnotation().toString());
                if (pair!=null) {
                    if (outcome.coversFileAtLineNumber(
                        pair.getFileName(),
                        pair.getLineNumber()))
                   {
                        // Bingo!  Found an outcome failing with the matching exception
                        // that also covers the warning!
                        // increment accurate warnings
                        accuracyContainer.accurateResults++;
                        System.out.println(line + "\tACCURATE");
                        accurateWarning=true;
                        break;
                   }
                }
            }
            if (!accurateWarning) {
                // None of the failing test cases with corresponding exception
                // covered the correct type of warning;
                // increment false positives
                accuracyContainer.falseResults++;
                System.out.println(line + "\tFALSE_POSITIVE");
            }
        }
    }

    private static String toUsefulString(BugInstance bugInstance)
    {
        return bugInstance.getType() +": "+bugInstance.getPriority()+ ": " +
                bugInstance.getPrimarySourceLineAnnotation();
    }
    
    public static SortedBugCollection getCoveredFindBugsWarningsFromCollection(
        String warningNamePrefix,
        TestOutcomeCollection collection)
    throws IOException
    {
        SortedBugCollection sortedBugCollection=new SortedBugCollection();
        for (TestOutcome outcome : collection.getFindBugsOutcomes()) {
            BugInstance bugInstance=(BugInstance)outcome.getDetails();
            // We only want warnings that start with the warning prefix
            // and that are covered by at least one test case
            if (outcome.getTestName().startsWith(warningNamePrefix) &&
                collection.getTestOutcomesCoveredByFindbugsWarning(outcome).size() > 0)
            {
                sortedBugCollection.add(bugInstance);
            }
        }
        
        return sortedBugCollection;
    }

}
