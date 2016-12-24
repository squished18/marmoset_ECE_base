/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Nov 12, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Queries;
import edu.umd.cs.marmoset.modelClasses.Submission;

/**
 * DefaultBestSubmissionPolicy
 * Default implementation that always returns the last submission before each deadline.
 * TODO Get rid of all the helper crap in here.
 * @author jspacco
 */
public class DefaultBestSubmissionPolicy extends BestSubmissionPolicy
{

    /**
     * Finds the last compiling submission.
     * @param projectPK
     * @param conn
     * @return the last ontime submission; null if there were no ontime submissions
     */
    public Map<String, Submission> lookupLastOntimeSubmissionMap(
            Project project,
            Connection conn)
    throws SQLException
    {
        String query = 
        " SELECT " +Submission.ATTRIBUTES+ ", student_submit_status.extension " +
        " FROM submissions, student_submit_status, projects " +
        " WHERE submissions.project_pk = ? " +
        " AND submissions.submission_timestamp IS NOT NULL " +
        " AND student_submit_status.project_pk = submissions.project_pk " +
        " AND student_submit_status.student_registration_pk = submissions.student_registration_pk " +
        " AND submissions.project_pk = projects.project_pk " +
        " AND (submissions.num_build_tests_passed > 0 OR " +
        "       projects.initial_build_status = 'accepted') " +
        " AND submission_timestamp <= DATE_ADD(projects.ontime, INTERVAL student_submit_status.extension HOUR) " +
        " ORDER BY submission_timestamp desc ";
    
        return lookupLastSubmissionMapAndExtensionFromQuery(query, project, conn);
    
    }
    
    /**
     * @param project
     * @param stmt
     * @return
     * @throws SQLException
     */
    private static Map<String, Submission> getLastSubmissionMapFromStmt(Project project, PreparedStatement stmt)
    throws SQLException {
        Map<String, Submission> result = new HashMap<String, Submission>();
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String studentRegistrationPK = getStudentRegistrationPKFromResultSet(rs);
            if (result.containsKey(studentRegistrationPK)) continue;
            Submission submission = new Submission();
            submission.fetchValues(rs, 1);
            // set late status
            submission.setStatus(project);
            // adjust final score based on late status and late penalty
            submission.setAdjustedScore(project);
            result.put(studentRegistrationPK, submission);
        }
        return result;
    }
    
    protected static Map<String, Submission> lookupLastSubmissionMapAndExtensionFromQuery(
        String query,
        Project project,
        Connection conn)
    throws SQLException
    {
        Map<String, Submission> result = new HashMap<String, Submission>();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, project.getProjectPK());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String studentRegistrationPK = getStudentRegistrationPKFromResultSet(rs);
                int extension = getExtensionFromResultSet(rs);
                if (result.containsKey(studentRegistrationPK)) continue;
                Submission submission = new Submission();
                submission.fetchValues(rs, 1);
                // set late status, taking extension into account
                submission.setStatus(project, extension);
                // adjust final score based on late status and late penalty
                submission.setAdjustedScore(project);
                result.put(studentRegistrationPK, submission);
            }
            return result;
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    public Map<String, Submission> lookupLastLateSubmissionMap(
    Project project,
    Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +Submission.ATTRIBUTES+ ", student_submit_status.extension " +
            " FROM submissions, student_submit_status, projects " +
            " WHERE submissions.project_pk = ? " +
            " AND submissions.submission_timestamp IS NOT NULL " +
            " AND student_submit_status.project_pk = submissions.project_pk " +
            " AND student_submit_status.student_registration_pk = submissions.student_registration_pk " + 
            " AND submissions.project_pk = projects.project_pk " +
            " AND (submissions.num_build_tests_passed > 0 OR " +
            "       projects.initial_build_status = 'accepted') " +
            " AND submission_timestamp > DATE_ADD(projects.ontime, INTERVAL student_submit_status.extension HOUR) " +
            " AND submission_timestamp <= DATE_ADD(projects.late, INTERVAL student_submit_status.extension HOUR) " +
            " ORDER BY submission_timestamp desc ";
        
        return lookupLastSubmissionMapAndExtensionFromQuery(query, project, conn);
    }
    
    /**
     * Create a map from studentRegistrationPK to the last very late submission for
     * a given project.
     * @param project the project for which we want the last very late submission
     * @param conn the connection to the database
     * @return a map from studentRegistrationPK to the last very late submission.
     * @throws SQLException
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
        " ORDER BY submission_timestamp desc ";
    
        return lookupLastSubmissionMapAndExtensionFromQuery(query, project, conn);
    }
    
    /**
     * Finds the last submission.
     * @param studentRegistrationPK
     * @param projectPK
     * @param conn
     * @return the last very-late submission; null if there were no very late submissions
     */
    public Map<String, Submission> lookupLastSubmissionMap(
            Project project,
            Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +Submission.ATTRIBUTES+ 
            " FROM submissions " +
            " WHERE submissions.project_pk = ? " +
            " AND submissions.submission_timestamp IS NOT NULL " +
            " ORDER BY submission_timestamp desc ";
    
        return getLastSubmissionMapFromQuery(query, project, conn);
    }
    
    
    /**
     * Gets a map from studentRegistrationPK to the last submission the student made.
     * @param stmt
     * @param queryTerm TODO
     * @param conn TODO
     * @return
     * @throws SQLException
     */
    private static Map<String, Submission> getLastSubmissionMapFromQuery(String query, Project project,
            Connection conn) throws SQLException {
    
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, project.getProjectPK());
    
            return getLastSubmissionMapFromStmt(project, stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }
}
