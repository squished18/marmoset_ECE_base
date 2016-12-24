/**
 * Copyright (C) 2006, University of Maryland
 * All Rights Reserved
 * Created on Jun 14, 2006
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.utilities;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Queries;
import edu.umd.cs.marmoset.modelClasses.Snapshot;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.ZipFileAggregator;

/**
 * DownloadAllSnapshotsForProject
 * @author jspacco
 */
public class DownloadAllSnapshotsForProject
{

    /**
     * @param args
     */
    public static void main(String[] args)
    throws SQLException, IOException, ZipFileAggregator.BadInputZipFileException
    {
        Connection conn=null;
        try {
            conn=DatabaseUtilities.getConnection();
            
            String projectPK="30";
            FileOutputStream out=new FileOutputStream(System.getenv("HOME") + "/APROJECTS/fall2004/132/p2/output.zip");
            PrintWriter csv=new PrintWriter(System.getenv("HOME") + "/APROJECTS/fall2004/132/p2/results.csv");
            
            Project project=Project.lookupByProjectPK(projectPK,conn);
            
            // Look up map of studentRegistrationPK to studentRegistration
            Map<String,StudentRegistration> map=new HashMap<String,StudentRegistration>();
            for (StudentRegistration registration : StudentRegistration.lookupAllByCoursePK(project.getCoursePK(),conn)) {
                map.put(registration.getStudentRegistrationPK(), registration);
            }
            
            // Get all of the snapshots for this project
            List<Snapshot> snapshotList=Snapshot.lookupAllCompilableByProjectPKAndNumLinesChanged(
                projectPK,
                conn);
            System.out.println("num snapshots = " +snapshotList.size());
            
            // Create map from submissionPK to corresponding TestOutcomeCollection
            // XXX This could be refactored into a method
//            Map<String, TestOutcomeCollection> submissionOutcomeCollection = new HashMap<String, TestOutcomeCollection>();
//            for (Snapshot snapshot : snapshotList) {
//                if (snapshot.getCurrentTestRunPK() == null) continue;
//                TestOutcomeCollection collection = TestOutcomeCollection.lookupByTestRunPK(snapshot.getCurrentTestRunPK(), conn);
//                if (collection == null) continue;
//                submissionOutcomeCollection.put(snapshot.getSubmissionPK(), collection);
//                System.out.println(Runtime.getRuntime().freeMemory() +" out of " +Runtime.getRuntime().maxMemory());
//            }
//            System.out.println("num outcome collections = " +submissionOutcomeCollection.size());
            
            // Aggregate all of the zipped up files
            ZipFileAggregator zipFileAggregator=new ZipFileAggregator(out);
            
            // get the outcome from the canonical run; we'll use this to retrieve the names of the test cases
            TestOutcomeCollection canonicalCollection = TestOutcomeCollection.lookupCanonicalOutcomesByProjectPK(
                    project.getProjectPK(),
                    conn);

            // format and print the header
            String header = "classAccount-snapshotPK,timestamp,UTC,total";
            for (TestOutcome outcome : canonicalCollection) {
                if (outcome.getTestType().equals(TestOutcome.BUILD_TEST)) continue;
                header += "," +outcome.getTestType() +"_"+ outcome.getTestName();
            }
            csv.println(header);
            
            
            
            //int ii=0;
            for (Snapshot snapshot : snapshotList) {
                // Lookup the registration 
                StudentRegistration registration=map.get(snapshot.getStudentRegistrationPK());
                
                byte[] bytes;
                if (registration.getInstructorCapability() != null) {
                    // Canonical, instructor or TA submission; we can keep the comments
                    bytes=snapshot.downloadArchive(conn);
                } else {
                    // Student submission; strip comments and add bytes to the zipfileAggregator
                    bytes=CommentStripper.stripCommentsFromSubmissionArchive(
                        snapshot.getSubmissionPK(),
                        conn);
                }
                zipFileAggregator.addFileFromBytes(project.getProjectNumber() +"/"+ registration.getCvsAccount() +"-"+ snapshot.getSubmissionPK(), bytes);
                
                System.out.println("submissionPK = " +snapshot.getSubmissionPK());
                String datetime="null";
                String timestamp="null";
                Timestamp ts=snapshot.getSubmissionTimestamp();
                if (ts==null)
                    ts=snapshot.getCommitTimestamp();
                if (ts!=null) {
                    datetime=ts.toString();
                    timestamp=Long.toString(ts.getTime());
                }
                    
                // Prepare the first part of this line of data
                String result = registration.getCvsAccount() +"-"+snapshot.getSubmissionPK() +","+
                datetime +","+
                timestamp +","+
                snapshot.getValuePassedOverall();
            
                // Lookup the test outcomes and prepare the second part of the results
                //TestOutcomeCollection testOutcomeCollection=submissionOutcomeCollection.get(snapshot.getSubmissionPK());
                TestOutcomeCollection testOutcomeCollection=TestOutcomeCollection.lookupBySubmissionPK(snapshot.getSubmissionPK(),conn);
  
                for (TestOutcome outcome : testOutcomeCollection) {
                    // Skip anything that is not a cardinal test type (public,release,secret)
                    if (!outcome.isCardinalTestType()) continue;
                    
                    if (outcome.getOutcome().equals(TestOutcome.PASSED)) {
                        result += "," +outcome.getPointValue();
                    } else {
                        result += ",0";
                    }
                }
                
                csv.println(result);
                
                //if (ii++ >= 3) break; 
            }
            zipFileAggregator.close();
            out.flush();
            out.close();
            csv.flush();
            csv.close();
        } finally {
            DatabaseUtilities.releaseConnection(conn);
        }

    }

}
