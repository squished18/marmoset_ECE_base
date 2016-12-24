/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Dec 8, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Submission;

/**
 * BackgroundRetestAwareBestSubmissionPolicy
 * @author jspacco
 */
public class BackgroundRetestAwareBestSubmissionPolicy extends DefaultBestSubmissionPolicy
{

    /* (non-Javadoc)
     * @see edu.umd.cs.submitServer.BestSubmissionPolicy#lookupLastSubmissionMap(edu.umd.cs.marmoset.modelClasses.Project, java.sql.Connection)
     */
    @Override
    public Map<String, Submission> lookupLastSubmissionMap(Project project,
        Connection conn) throws SQLException
    {
        Map<String,Submission> map=super.lookupLastSubmissionMap(project,conn);
        adjustSubmissionCollection(map.values(), conn);
        return map;
    }
    
    private static void adjustSubmissionCollection(Collection<Submission> collection, Connection conn)
    throws SQLException
    {
        for (Submission submission : collection) {
            submission.setAdjustScoreBasedOnFailedBackgroundRetests(conn);
        }
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.submitServer.BestSubmissionPolicy#lookupLastOntimeSubmissionMap(edu.umd.cs.marmoset.modelClasses.Project, java.sql.Connection)
     */
    @Override
    public Map<String, Submission> lookupLastOntimeSubmissionMap(
        Project project, Connection conn) throws SQLException
    {
        Map<String,Submission> map=super.lookupLastOntimeSubmissionMap(project,conn);
        adjustSubmissionCollection(map.values(), conn);
        return map;
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.submitServer.BestSubmissionPolicy#lookupLastLateSubmissionMap(edu.umd.cs.marmoset.modelClasses.Project, java.sql.Connection)
     */
    @Override
    public Map<String, Submission> lookupLastLateSubmissionMap(Project project,
        Connection conn) throws SQLException
    {
        Map<String,Submission> map=super.lookupLastLateSubmissionMap(project,conn);
        adjustSubmissionCollection(map.values(),conn);
        return map;
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.submitServer.BestSubmissionPolicy#lookupLastVeryLateSubmissionMap(edu.umd.cs.marmoset.modelClasses.Project, java.sql.Connection)
     */
    @Override
    public Map<String, Submission> lookupLastVeryLateSubmissionMap(
        Project project, Connection conn) throws SQLException
    {
        Map<String,Submission> map=super.lookupLastVeryLateSubmissionMap(project,conn);
        adjustSubmissionCollection(map.values(),conn);
        return map;
    }

}
