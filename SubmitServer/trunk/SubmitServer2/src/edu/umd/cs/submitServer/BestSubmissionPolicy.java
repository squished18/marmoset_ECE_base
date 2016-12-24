/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Nov 12, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;

/**
 * BestSubmissionPolicy
 * Defines how to select the 'best' submission for each category of ontime, late, very-late and last.
 * FIXME: All methods named "lookupLast..." should be renamed to "lookupBest..."
 * @author jspacco
 */
public abstract class BestSubmissionPolicy
{
    public abstract Map<String, Submission> lookupLastSubmissionMap(Project project,Connection conn) throws SQLException;
    public abstract Map<String, Submission> lookupLastOntimeSubmissionMap(Project project,Connection conn) throws SQLException;
    /**
     * 
     * @param project
     * @param conn
     * @return
     * @throws SQLException
     */
    public abstract Map<String, Submission> lookupLastLateSubmissionMap(Project project,Connection conn) throws SQLException;
    
    public abstract Map<String, Submission> lookupLastVeryLateSubmissionMap(Project project,Connection conn) throws SQLException;
    
    static int getExtensionFromResultSet(ResultSet rs)
    throws SQLException
    {
        return rs.getInt("student_submit_status.extension");
    }
    
    static String getStudentRegistrationPKFromResultSet(ResultSet resultSet) throws SQLException {
        return resultSet.getString(2);
    }
    
    /**
     * Builds a map of the best adjusted score from the ontime and late submissions.
     * @param studentRegistrationList list of the studentRegistrations
     * @param ontimeMap the ontime submissions
     * @param lateMap the late submissions (with adjusted scores)
     * @return
     */
    public Map<String, Submission> getBestSubmissionMap(
            Set<StudentRegistration> studentRegistrationList,
            Map<String,Submission> ontimeMap,
            Map<String,Submission> lateMap)
    {
        Map<String, Submission> bestMap = new HashMap<String, Submission>();
        for (StudentRegistration registration : studentRegistrationList) {
            String studentRegistrationPK = registration.getStudentRegistrationPK();
            
            Submission ontimeSubmission = ontimeMap.get(studentRegistrationPK);
            Submission lateSubmission = lateMap.get(studentRegistrationPK);
            
            if (ontimeSubmission == null)
            {
                if (lateSubmission != null)
                {
                    bestMap.put(studentRegistrationPK, lateSubmission);
                }
            }
            else
            {
                // onTimeSubmission != null
                if (lateSubmission != null) {
                    int lateScore = Math.max(0, lateSubmission.getAdjustedScore());
                    if (lateScore >= ontimeSubmission.getAdjustedScore()) {
                        //lateScore better than ontime score
                        bestMap.put(studentRegistrationPK, lateSubmission);
                    } else {
                        // ontime score better than late score
                        bestMap.put(studentRegistrationPK, ontimeSubmission);
                    }
                } else {
                    // no late score exists
                    bestMap.put(studentRegistrationPK, ontimeSubmission);
                }
                
                if (lateSubmission != null && lateSubmission.getAdjustedScore() > 0 &&
                    lateSubmission.getAdjustedScore() >= ontimeSubmission.getAdjustedScore())
                {
                    bestMap.put(studentRegistrationPK, lateSubmission);
                }
            }
        }
        return bestMap;
    }
}
