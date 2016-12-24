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
 * BestBestSubmissionPolicy
 * @author jspacco
 */
public class BestBestSubmissionPolicy extends DefaultBestSubmissionPolicy
{

    /* (non-Javadoc)
     * @see edu.umd.cs.submitServer.DefaultBestSubmissionPolicy#lookupLastLateSubmissionMap(edu.umd.cs.marmoset.modelClasses.Project, java.sql.Connection)
     */
    @Override
    public Map<String, Submission> lookupLastLateSubmissionMap(Project project, Connection conn) throws SQLException
    {
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
       //     " AND submissions.release_request IS NOT NULL " +
            " ORDER BY submissions.num_passed_overall desc, submission_timestamp desc ";
        
        return lookupLastSubmissionMapAndExtensionFromQuery(query, project, conn);
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.submitServer.DefaultBestSubmissionPolicy#lookupLastOntimeSubmissionMap(edu.umd.cs.marmoset.modelClasses.Project, java.sql.Connection)
     */
    @Override
    public Map<String, Submission> lookupLastOntimeSubmissionMap(Project project, Connection conn) throws SQLException
    {
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
//            " AND submissions.release_request IS NOT NULL " +
            " ORDER BY submissions.num_passed_overall desc, submission_timestamp desc ";
        return lookupLastSubmissionMapAndExtensionFromQuery(query,project,conn);
    }


    /* (non-Javadoc)
     * @see edu.umd.cs.submitServer.BestSubmissionPolicy#getBestSubmissionMap(java.util.Set, java.util.Map, java.util.Map)
     */
    /*
    @Override
    public Map<String, Submission> getBestSubmissionMap(Set<StudentRegistration> studentRegistrationList, Map<String, Submission> ontimeMap, Map<String, Submission> lateMap)
    {
        return super.getBestSubmissionMap(studentRegistrationList, ontimeMap, lateMap);
    }
    */

    /* (non-Javadoc)
     * @see edu.umd.cs.submitServer.DefaultBestSubmissionPolicy#lookupLastSubmissionMap(edu.umd.cs.marmoset.modelClasses.Project, java.sql.Connection)
     */
    /*
    @Override
    public Map<String, Submission> lookupLastSubmissionMap(Project project, Connection conn) throws SQLException
    {
        return super.lookupLastSubmissionMap(project, conn);
    }
    */

    /* (non-Javadoc)
     * @see edu.umd.cs.submitServer.DefaultBestSubmissionPolicy#lookupLastVeryLateSubmissionMap(edu.umd.cs.marmoset.modelClasses.Project, java.sql.Connection)
     */
    /*
    @Override
    public Map<String, Submission> lookupLastVeryLateSubmissionMap(Project project, Connection conn) throws SQLException
    {
        return super.lookupLastVeryLateSubmissionMap(project, conn);
    }
    */

    public Map<String, Submission> lookupLastVeryLateSubmissionMap(
            Project project,
            Connection conn)
    throws SQLException
    {
        String query = 
        " SELECT " +Submission.ATTRIBUTES+ ", student_submit_status.extension "+
        " FROM submissions, student_submit_status, projects " +
        " WHERE submissions.project_pk = ? " +
        " AND submissions.submission_timestamp IS NOT NULL " +
        " AND student_submit_status.project_pk = submissions.project_pk " +
        " AND student_submit_status.student_registration_pk = submissions.student_registration_pk " +
        " AND submissions.project_pk = projects.project_pk " +
        " AND (submissions.num_build_tests_passed > 0 OR " +
        "       projects.initial_build_status = 'accepted') " +
        " AND submission_timestamp > DATE_ADD(projects.late, INTERVAL student_submit_status.extension HOUR) " +
        " ORDER BY submissions.num_passed_overall desc, submission_timestamp desc ";
        //" ORDER BY submission_timestamp desc ";
    
        return lookupLastSubmissionMapAndExtensionFromQuery(query, project, conn);
    }

}
