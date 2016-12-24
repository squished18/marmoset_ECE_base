package edu.umd.cs.submitServer;



import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.utilities.DatabaseUtilities;
import edu.umd.cs.submitServer.ReleaseTestAwareBestSubmissionPolicy;

/**
 * ReleaseTestAwareBestSubmissionPolicy
 * @author jspacco
 */
public class ReleaseTestAwareBestSubmissionPolicyTest
{

    /**
     * @param args
     */
    public static void main(String[] args)
    throws SQLException
    {
        System.out.println("Hello world");
        
        ReleaseTestAwareBestSubmissionPolicy policy=new ReleaseTestAwareBestSubmissionPolicy();
        Connection conn=null;
        try {
            conn=DatabaseUtilities.getConnection();
            
            String projectPK="21";
            Project project=Project.lookupByProjectPK(projectPK,conn);
            System.out.println("project = " +project);
            
            Map<String,Submission> ontimeMap=policy.lookupLastOntimeSubmissionMap(project,conn);
            Map<String,Submission> lateMap=policy.lookupLastLateSubmissionMap(project,conn);
            Map<String,Submission> veryLateMap=policy.lookupLastVeryLateSubmissionMap(project,conn);
            
            Set<StudentRegistration> set=new TreeSet<StudentRegistration>();
            set.addAll(StudentRegistration.lookupAllWithAtLeastOneSubmissionByProjectPK(projectPK,conn));
            Map<String,Submission> best=policy.getBestSubmissionMap(set,ontimeMap,lateMap);
            
            for (Submission submission: best.values()) {
                if (submission.getStudentRegistrationPK().equals("113") ||
                    submission.getAdjustedScore() < 0)
                    System.out.println(submission.getStudentRegistrationPK() +" => "+
                        submission.getAdjustedScore()
                        + ", submissionPK = " +submission.getSubmissionPK());
            }
            
        } finally {
            DatabaseUtilities.releaseConnection(conn);
        }

    }

}
