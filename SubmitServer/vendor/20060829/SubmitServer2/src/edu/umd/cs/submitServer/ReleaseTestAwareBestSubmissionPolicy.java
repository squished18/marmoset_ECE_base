/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Feb 15, 2006
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;

/**
 * ReleaseTestAwareBestSubmissionPolicy
 * @author jspacco
 */
public class ReleaseTestAwareBestSubmissionPolicy extends DefaultBestSubmissionPolicy
{

    /* (non-Javadoc)
     * @see edu.umd.cs.submitServer.DefaultBestSubmissionPolicy#lookupLastLateSubmissionMap(edu.umd.cs.marmoset.modelClasses.Project, java.sql.Connection)
     */
    @Override
    public Map<String, Submission> lookupLastLateSubmissionMap(Project project, Connection conn) throws SQLException
    {
        Map<String,Submission> lastMap=super.lookupLastLateSubmissionMap(project, conn);
        String query = 
            " SELECT " +Submission.ATTRIBUTES+ ", student_submit_status.extension " +
            " FROM submissions, student_submit_status, projects " +
            " WHERE submissions.project_pk = ? " +
            " AND submissions.submission_timestamp IS NOT NULL " +
            " AND student_submit_status.project_pk = submissions.project_pk " +
            " AND student_submit_status.student_registration_pk = submissions.student_registration_pk " + 
            " AND submissions.project_pk = projects.project_pk " +
            " AND submission_timestamp > DATE_ADD(projects.ontime, INTERVAL student_submit_status.extension HOUR) " +
            " AND submission_timestamp <= DATE_ADD(projects.late, INTERVAL student_submit_status.extension HOUR) " +
            " AND submissions.release_request IS NOT NULL " +
            " ORDER BY submissions.num_passed_overall desc, submission_timestamp desc ";
        
        Map<String,Submission> releaseMap=lookupLastSubmissionMapAndExtensionFromQuery(query, project, conn);
        return chooseHigherAdjustedScore(lastMap,releaseMap);
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.submitServer.DefaultBestSubmissionPolicy#lookupLastOntimeSubmissionMap(edu.umd.cs.marmoset.modelClasses.Project, java.sql.Connection)
     */
    @Override
    public Map<String, Submission> lookupLastOntimeSubmissionMap(Project project, Connection conn) throws SQLException
    {
        // Most-recent ontime submissions
        Map<String,Submission> ontimeMap= super.lookupLastOntimeSubmissionMap(project, conn);
        
//      Look up the best-scoring release-tested submissions
        String query = 
            " SELECT " +Submission.ATTRIBUTES+ ", student_submit_status.extension " +
            " FROM submissions, student_submit_status, projects " +
            " WHERE submissions.project_pk = ? " +
            " AND submissions.submission_timestamp IS NOT NULL " +
            " AND student_submit_status.project_pk = submissions.project_pk " +
            " AND student_submit_status.student_registration_pk = submissions.student_registration_pk " +
            " AND submissions.project_pk = projects.project_pk " +
            " AND submission_timestamp <= DATE_ADD(projects.ontime, INTERVAL student_submit_status.extension HOUR) " +
            " AND submissions.release_request IS NOT NULL " +
            " ORDER BY submissions.num_passed_overall desc, submission_timestamp desc ";
        Map<String,Submission> releaseTestedOntimeMap=
            lookupLastSubmissionMapAndExtensionFromQuery(query,project,conn);
        
        return chooseHigherAdjustedScore(ontimeMap, releaseTestedOntimeMap);
    }

    /**
     * Replaces the current "bestSubmission" from the bestSubmissionMap
     * with the highest scoring release-tested submission, if the score of the
     * release-tested submission is higher.
     * @param bestSubmissionMap
     * @param releaseTestedMap
     * @return The bestSubmissionMap with the submissions replaced where necessary with the
     *      highest-scoring release tested submission.
     */
    private Map<String,Submission> chooseHigherAdjustedScore(
        Map<String, Submission> bestSubmissionMap,
        Map<String, Submission> releaseTestedMap)
    {
        for (Map.Entry<String,Submission> entry : bestSubmissionMap.entrySet()) {
            String submissionPK=entry.getKey();
            Submission submission=entry.getValue();
            // If the best release-tested submission is higher, replace it
            if (releaseTestedMap.containsKey(submissionPK)) {
                Submission releaseTestedSubmission=releaseTestedMap.get(submissionPK);
                if (releaseTestedSubmission.getAdjustedScore() > submission.getAdjustedScore()) {
                    bestSubmissionMap.put(submissionPK, releaseTestedSubmission);
                }
            }
        }
        return bestSubmissionMap;
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.submitServer.BestSubmissionPolicy#getBestSubmissionMap(java.util.Set, java.util.Map, java.util.Map)
     */
    @Override
    public Map<String, Submission> getBestSubmissionMap(Set<StudentRegistration> studentRegistrationList, Map<String, Submission> ontimeMap, Map<String, Submission> lateMap)
    {
        return super.getBestSubmissionMap(studentRegistrationList, ontimeMap, lateMap);
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.submitServer.DefaultBestSubmissionPolicy#lookupLastSubmissionMap(edu.umd.cs.marmoset.modelClasses.Project, java.sql.Connection)
     */
    @Override
    public Map<String, Submission> lookupLastSubmissionMap(Project project, Connection conn) throws SQLException
    {
        return super.lookupLastSubmissionMap(project, conn);
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.submitServer.DefaultBestSubmissionPolicy#lookupLastVeryLateSubmissionMap(edu.umd.cs.marmoset.modelClasses.Project, java.sql.Connection)
     */
    @Override
    public Map<String, Submission> lookupLastVeryLateSubmissionMap(Project project, Connection conn) throws SQLException
    {
        return super.lookupLastVeryLateSubmissionMap(project, conn);
    }


}
