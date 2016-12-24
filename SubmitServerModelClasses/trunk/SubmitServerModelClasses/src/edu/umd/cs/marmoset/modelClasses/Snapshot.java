/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Feb 14, 2005
 *
 */

package edu.umd.cs.marmoset.modelClasses;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.CopyUtils;
import org.apache.commons.io.FileUtils;

import edu.umd.cs.marmoset.utilities.JProcess;

/**
 * A snapshot submission, which is a submission with a non-null commit timestamp.
 * Using the composition pattern rather than inheritance for the relationship
 * between Submission and Snapshot.
 * 
 * TODO handle hashCode and equals methods, should be sufficient to delegate down
 * to the submission field.
 * 
 * TODO refactor getFromPreparedStatement() so that it cannot leak statements
 * TODO refactor getListFromPreparedStatement() so that it cannot leak statements
 * 
 * @author jspacco
 *
 */
public class Snapshot
{
    /**
     * List of all columns of this the table in the database represented by this class.
     */
     static final String[] ATTRIBUTE_LIST = {
            "submission_pk",
    		"student_registration_pk",
    		"project_pk",
    		"num_test_outcomes",
    		"current_test_run_pk",
    		"submission_number",
    		"submission_timestamp",
    		"cvstag_timestamp",
    		"build_request_timestamp",
    		"build_status",
    		"submit_client",
    		"release_request",
    		"release_eligible",
    		"num_passed_overall",
    		"num_build_tests_passed",
    		"num_public_tests_passed",
    		"num_release_tests_passed",
    		"num_secret_tests_passed",
    		"num_findbugs_warnings",
    		"archive_pk",
    		"commit_timestamp",
    		"num_lines_changed",
    		"net_change",
            "time_since_last_commit",
            "time_since_last_compilable_commit",
    		"test_delta",
    		"findbugs_delta",
    		"faults_delta",
    		"diff_file",
    		"commit_cvstag",
    		"commit_number",
    		"previous_md5sum_classfiles",
    		"previous_md5sum_sourcefiles",
            "previous_submission_pk",
    		"new_faults",
    		"removed_faults",
    		"total_faults"
    };
    
    /**
     * Fully-qualified names of the columns of the database.
     */
    public static final String ATTRIBUTES = Queries.getAttributeList(
            "submissions",
            ATTRIBUTE_LIST);
    
    /**
     * Name of the table in the database represented by this class.
     */
    public static final String TABLE_NAME = "submissions";

    public static final String EQ = "=";
    public static final String LT = "<";
    
    private Submission submission;
    protected Timestamp commitTimestamp;
    protected Integer numLinesChanged;
    protected Integer netChange;
    protected String timeSinceLastCommit;
    protected String timeSinceLastCompilableCommit;
    protected Integer testDelta;
    protected Integer findbugsDelta;
    protected Integer faultsDelta;
    protected String diffFile;
    protected String commitCvstag;
    protected Integer commitNumber;
    protected String previousMd5sumClassfiles;
    protected String previousMd5sumSourcefiles;
    protected String previousSubmissionPK;
    protected Integer newFaults;
	protected Integer removedFaults;
	protected Integer totalFaults;

    public Snapshot() { submission = new Submission(); }

    /**
     * Inserts a new row into the database with a fresh primary key.
     * @param conn
     * @throws SQLException
     */
    public void insert(Connection conn)
    throws SQLException
    {
        // check for and disallow duplicates
        // a duplicate snapshot has the same student_registration_pk, project_pk and commit_cvstag
        Snapshot snapshot = Snapshot.lookupByStudentRegistrationPKAndProjectPKAndCommitCvstag(
                getStudentRegistrationPK(),
                getProjectPK(),
                getCommitCvstag(),
                getCvsTagTimestamp(),
                conn);
        if (snapshot != null)
            throw new SQLException("Trying to insert duplicate row: studentRegistrationPK: " +
            		getStudentRegistrationPK()+
            		", projectPK: " +
            		getProjectPK() +
            		", commitCvstag: " +
            		getCommitCvstag());
        
        String insert = Queries.makeInsertStatement(ATTRIBUTE_LIST.length, ATTRIBUTES, TABLE_NAME);

	    PreparedStatement stmt = null;
	    try {
	        
	        // if haven't cached any bytes for upload, throw an exception
	        if (!submission.hasCachedArchive())
	            throw new IllegalStateException("there is no archive for upload, you should call setArchiveForUpload first");
	        stmt = conn.prepareStatement(insert);

	        // insert the bytes we have as a new archive in that table
	        // and then set the archivePK of the new archive
	        setArchivePK(submission.uploadCachedArchive(conn));
	        
	        // now put the values (including the archivePK we set in the last statement)
	        // into the prepared statement
	        putValues(stmt, 1);
	        
	        // and put into the database
	        stmt.executeUpdate();

	        // set PK to the value of the last autoincrement for this connection
            // this will be the PK used for the inset we just performed
	        setSubmissionPK(Queries.lastInsertId(conn));
	    } finally {
	        Queries.closeStatement(stmt);
	    }
            
    }
    
    /**
     * Checks for a "duplicate" where a dupliace snapshot has the same:
     * student_registration_pk and project_pk AND <br>
     * (commitCvstag OR cvsTagTimestamp)
     * @param studentRegistrationPK
     * @param projectPK
     * @param commitCvstag
     * @param cvsTagTimestamp
     * @param conn the connection to the database
     * @return the duplicate Snapshot if it's found; null otherwise
     */
    private static Snapshot lookupByStudentRegistrationPKAndProjectPKAndCommitCvstag(
            String studentRegistrationPK,
            String projectPK,
            String commitCvstag,
            String cvsTagTimestamp,
            Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +ATTRIBUTES+
            " FROM " +TABLE_NAME+
            " WHERE student_registration_pk = ? " +
            " AND project_pk = ? " +
            " AND (commit_cvstag = ? " +
            "      OR cvstag_timestamp = ?) ";
        
        PreparedStatement stmt=conn.prepareStatement(query);
        int index=1;
        stmt.setString(index++, studentRegistrationPK);
        stmt.setString(index++, projectPK);
        stmt.setString(index++, commitCvstag);
        stmt.setString(index++, cvsTagTimestamp);
        return getFromPreparedStatement(stmt);
    }

    /**
     * Update the row in the database represented by this instance.
     * @param conn the connection to the database
     * @throws SQLException
     */
    public void update(Connection conn)
    throws SQLException
    {
        if (getSubmissionPK() == null)
	        throw new IllegalStateException("You cannot try to update a submission with a null submissionPK");
	    
	    String update = Queries.makeUpdateStatementWithWhereClause(ATTRIBUTE_LIST, TABLE_NAME, " WHERE submission_pk = ? ");

	    PreparedStatement stmt = conn.prepareStatement(update);
	    
	    // put the values of this object into the prepared statement
	    int index = putValues(stmt, 1);
	    // and se the primary key as the final criteria of the WHERE clause
	    stmt.setString(index, getSubmissionPK());
	    
	    stmt.executeUpdate();
	    try {
	        stmt.close();
	    } catch (SQLException ignore) {
	        // ignore
	    }
    }
    
    protected int putValues(PreparedStatement stmt, int index)
    throws SQLException
    {
       	index = submission.putValues(stmt, index);
       	stmt.setTimestamp(index++, getCommitTimestamp());
       	stmt.setObject(index++, getNumLinesChanged());
       	stmt.setObject(index++, getNetChange());
       	stmt.setString(index++, getTimeSinceLastCommit());
        stmt.setString(index++, getTimeSinceLastCompilableCommit());
       	stmt.setObject(index++, getTestDelta());
       	stmt.setObject(index++, getFindbugsDelta());
       	stmt.setObject(index++, getFaultsDelta());
       	stmt.setString(index++, getDiffFile());
       	stmt.setString(index++, getCommitCvstag());
       	stmt.setObject(index++, getCommitNumber());
       	stmt.setString(index++, getPreviousMd5sumClassfiles());
       	stmt.setString(index++, getPreviousMd5sumSourcefiles());
        stmt.setString(index++, getPreviousSubmissionPK());
       	stmt.setObject(index++, getNewFaults());
       	stmt.setObject(index++, getRemovedFaults());
       	stmt.setObject(index++, getTotalFaults());
       	return index;
    }
    
    public int fetchValues(ResultSet rs, int startingFrom)
    throws SQLException
    {
        startingFrom = submission.fetchValues(rs, startingFrom);
        setCommitTimestamp(rs.getTimestamp(startingFrom++));
        setNumLinesChanged((Integer)rs.getObject(startingFrom++));
        setNetChange((Integer)rs.getObject(startingFrom++));
        setTimeSinceLastCommit(rs.getString(startingFrom++));
        setTimeSinceLastCompilableCommit(rs.getString(startingFrom++));
        setTestDelta((Integer)rs.getObject(startingFrom++));
        setFindbugsDelta((Integer)rs.getObject(startingFrom++));
        setFaultsDelta((Integer)rs.getObject(startingFrom++));
        setDiffFile(rs.getString(startingFrom++));
        setCommitCvstag(rs.getString(startingFrom++));
        setCommitNumber((Integer)rs.getObject(startingFrom++));
        setPreviousMd5sumClassfiles(rs.getString(startingFrom++));
        setPreviousMd5sumSourcefiles(rs.getString(startingFrom++));
        setPreviousSubmissionPK(rs.getString(startingFrom++));
        setNewFaults((Integer)rs.getObject(startingFrom++));
        setRemovedFaults((Integer)rs.getObject(startingFrom++));
        setTotalFaults((Integer)rs.getObject(startingFrom++));
        return startingFrom;
    }
    
    /**
     * @param conn
     * @return
     * @throws SQLException
     */
    public byte[] downloadArchive(Connection conn) throws SQLException
    {
        return submission.downloadArchive(conn);
    }
    /**
     * @return
     */
    public int getAdjustedScore()
    {
        return submission.getAdjustedScore();
    }
    /**
     * @return
     */
    public String getArchivePK()
    {
        return submission.getArchivePK();
    }
    /**
     * @return
     */
    public Timestamp getBuildRequestTimestamp()
    {
        return submission.getBuildRequestTimestamp();
    }
    /**
     * @return
     */
    public String getBuildStatus()
    {
        return submission.getBuildStatus();
    }
    /**
     * @return
     */
    public String getCurrentTestRunPK()
    {
        return submission.getCurrentTestRunPK();
    }
    /**
     * @return
     */
    public String getCvsTagTimestamp()
    {
        return submission.getCvsTagTimestamp();
    }
    /**
     * @return
     */
    public String getFormattedSubmissionTimestamp()
    {
        return submission.getFormattedSubmissionTimestamp();
    }
    /**
     * @return
     */
    public int getNumFindBugsWarnings()
    {
        return submission.getNumFindBugsWarnings();
    }
    /**
     * @return
     */
    public int getNumTestRuns()
    {
        return submission.getNumTestRuns();
    }
    /**
     * @return
     */
    public String getProjectPK()
    {
        return submission.getProjectPK();
    }
    /**
     * @return
     */
    public Timestamp getReleaseRequest()
    {
        return submission.getReleaseRequest();
    }
    /**
     * @return
     */
    public String getStatus()
    {
        return submission.getStatus();
    }
    /**
     * @return
     */
    public String getStudentRegistrationPK()
    {
        return submission.getStudentRegistrationPK();
    }
    /**
     * @return
     */
    public String getSubmissionNumber()
    {
        return submission.getSubmissionNumber();
    }
    /**
     * @return
     */
    public String getSubmissionPK()
    {
        return submission.getSubmissionPK();
    }
    /**
     * @return
     */
    public Timestamp getSubmissionTimestamp()
    {
        return submission.getSubmissionTimestamp();
    }
    /**
     * @return
     */
    public String getSubmitClient()
    {
        return submission.getSubmitClient();
    }
    /**
     * @return
     */
    public int getValuePassedOverall()
    {
        return submission.getValuePassedOverall();
    }
    /**
     * @return
     */
    public int getValuePublicTestsPassed()
    {
        return submission.getValuePublicTestsPassed();
    }
    /**
     * @return
     */
    public int getValueReleaseTestsPassed()
    {
        return submission.getValueReleaseTestsPassed();
    }
    /**
     * @return
     */
    public int getValueSecretTestsPassed()
    {
        return submission.getValueSecretTestsPassed();
    }
    /**
     * @return Returns the commitCvstag.
     */
    public String getCommitCvstag()
    {
        return commitCvstag;
    }
    /**
     * @param commitCvstag The commitCvstag to set.
     */
    public void setCommitCvstag(String commitCvstag)
    {
        this.commitCvstag = commitCvstag;
    }
    /**
     * @return Returns the findbugsDelta.
     */
    public Integer getFindbugsDelta()
    {
        return findbugsDelta;
    }
    /**
     * @param findbugsDelta The findbugsDelta to set.
     */
    public void setFindbugsDelta(Integer findbugsDelta)
    {
        this.findbugsDelta = findbugsDelta;
    }
    /**
     * @return
     */
    public boolean isCompileSuccessful()
    {
        return submission.isCompileSuccessful();
    }
    /**
     * @return
     */
    public boolean isReleaseEligible()
    {
        return submission.isReleaseEligible();
    }
    /**
     * @return
     */
    public boolean isReleaseTestingRequested()
    {
        return submission.isReleaseTestingRequested();
    }
    /**
     * @param project
     */
    public void setAdjustedScore(Project project)
    {
        submission.setAdjustedScore(project);
    }
    public String getTimeSinceLastCompilableCommit() {
        return timeSinceLastCompilableCommit;
    }
    public void setTimeSinceLastCompilableCommit(String timeSinceLastCompilableCommit) {
        this.timeSinceLastCompilableCommit = timeSinceLastCompilableCommit;
    }
    /**
     * @param bytes
     */
    public void setArchiveForUpload(byte[] bytes)
    {
        submission.setArchiveForUpload(bytes);
    }
    /**
     * @param archivePK
     */
    public void setArchivePK(String archivePK)
    {
        submission.setArchivePK(archivePK);
    }
    /**
     * @param buildRequestTimestamp
     */
    public void setBuildRequestTimestamp(Timestamp buildRequestTimestamp)
    {
        submission.setBuildRequestTimestamp(buildRequestTimestamp);
    }
    /**
     * @param buildStatus
     */
    public void setBuildStatus(String buildStatus)
    {
        submission.setBuildStatus(buildStatus);
    }
    /**
     * @param success
     */
    public void setCompileSuccessful(boolean success)
    {
        submission.setCompileSuccessful(success);
    }
    /**
     * @param currentTestRunPK
     */
    public void setCurrentTestRunPK(String currentTestRunPK)
    {
        submission.setCurrentTestRunPK(currentTestRunPK);
    }
    /**
     * @param cvsTagTimestamp
     */
    public void setCvsTagTimestamp(String cvsTagTimestamp)
    {
        submission.setCvsTagTimestamp(cvsTagTimestamp);
    }
    /**
     * @param numFindBugsWarnings
     */
    public void setNumFindBugsWarnings(int numFindBugsWarnings)
    {
        submission.setNumFindBugsWarnings(numFindBugsWarnings);
    }
    /**
     * @param numTestOutcomes
     */
    public void setNumTestRuns(int numTestOutcomes)
    {
        submission.setNumTestRuns(numTestOutcomes);
    }
    /**
     * @param projectPK
     */
    public void setProjectPK(String projectPK)
    {
        submission.setProjectPK(projectPK);
    }
    /**
     * @param releaseEligible
     */
    public void setReleaseEligible(boolean releaseEligible)
    {
        submission.setReleaseEligible(releaseEligible);
    }
    /**
     * @param releaseRequest
     */
    public void setReleaseRequest(Timestamp releaseRequest)
    {
        submission.setReleaseRequest(releaseRequest);
    }
    /**
     * @param project
     */
    public void setStatus(Project project)
    {
        submission.setStatus(project);
    }
    /**
     * @param studentRegistrationPK
     */
    public void setStudentRegistrationPK(String studentRegistrationPK)
    {
        submission.setStudentRegistrationPK(studentRegistrationPK);
    }
    /**
     * @param submissionNumber
     */
    public void setSubmissionNumber(String submissionNumber)
    {
        submission.setSubmissionNumber(submissionNumber);
    }
    /**
     * @param submissionPK
     */
    public void setSubmissionPK(String submissionPK)
    {
        submission.setSubmissionPK(submissionPK);
    }
    /**
     * @param submissionTimestamp
     */
    public void setSubmissionTimestamp(Timestamp submissionTimestamp)
    {
        submission.setSubmissionTimestamp(submissionTimestamp);
    }
    /**
     * @param pluginVersion
     */
    public void setSubmitClient(String pluginVersion)
    {
        submission.setSubmitClient(pluginVersion);
    }
    /**
     * @param numPassedOverall
     */
    public void setValuePassedOverall(int numPassedOverall)
    {
        submission.setValuePassedOverall(numPassedOverall);
    }
    /**
     * @param numPublicTestsPassed
     */
    public void setValuePublicTestsPassed(int numPublicTestsPassed)
    {
        submission.setValuePublicTestsPassed(numPublicTestsPassed);
    }
    /**
     * @param numReleaseTestsPassed
     */
    public void setValueReleaseTestsPassed(int numReleaseTestsPassed)
    {
        submission.setValueReleaseTestsPassed(numReleaseTestsPassed);
    }
    /**
     * @param numSecretTestsPassed
     */
    public void setValueSecretTestsPassed(int numSecretTestsPassed)
    {
        submission.setValueSecretTestsPassed(numSecretTestsPassed);
    }
    /**
     * @return Returns the commitTimestamp.
     */
    public Timestamp getCommitTimestamp()
    {
        return commitTimestamp;
    }
    /**
     * @param commitTimestamp The commitTimestamp to set.
     */
    public void setCommitTimestamp(Timestamp commitTimestamp)
    {
        this.commitTimestamp = commitTimestamp;
    }
    public String getDiffFile()
    {
        return diffFile;
    }
    public void setDiffFile(String diffFile)
    {
        this.diffFile = diffFile;
    }
    public String getTimeSinceLastCommit()
    {
        return timeSinceLastCommit;
    }
    public void setTimeSinceLastCommit(String timeSinceLastCommit)
    {
        this.timeSinceLastCommit = timeSinceLastCommit;
    }
    public Integer getNetChange()
    {
        return netChange;
    }
    public String getPreviousSubmissionPK()
    {
        return previousSubmissionPK;
    }
    public void setPreviousSubmissionPK(String previousSubmissionPK) {
        this.previousSubmissionPK = previousSubmissionPK;
    }
    public void setNetChange(Integer netChange) {
        this.netChange = netChange;
    }
    public Integer getNumLinesChanged() {
        return numLinesChanged;
    }
    public void setNumLinesChanged(Integer numLinesChanged) {
        this.numLinesChanged = numLinesChanged;
    }
    public Integer getTestDelta() {
        return testDelta;
    }
    public void setTestDelta(Integer testDelta) {
        this.testDelta = testDelta;
    }

    public Integer getCommitNumber()
    {
        return commitNumber;
    }
    public void setCommitNumber(Integer commitNumber)
    {
        this.commitNumber = commitNumber;
    }
    /**
     * @return Returns the previousMd5sumClassfiles.
     */
    public String getPreviousMd5sumClassfiles()
    {
        return previousMd5sumClassfiles;
    }
    /**
     * @param previousMd5sumClassfiles The previousMd5sumClassfiles to set.
     */
    public void setPreviousMd5sumClassfiles(String previousMd5sumClassfiles)
    {
        this.previousMd5sumClassfiles = previousMd5sumClassfiles;
    }
    /**
     * @return Returns the previousMd5sumSourcefiles.
     */
    public String getPreviousMd5sumSourcefiles()
    {
        return previousMd5sumSourcefiles;
    }
    /**
     * @param previousMd5sumSourcefiles The previousMd5sumSourcefiles to set.
     */
    public void setPreviousMd5sumSourcefiles(String previousMd5sumSourcefiles)
    {
        this.previousMd5sumSourcefiles = previousMd5sumSourcefiles;
    }
    /**
     * @return Returns the faultsDelta.
     */
    public Integer getFaultsDelta()
    {
        return faultsDelta;
    }
    /**
     * @param faultsDelta The faultsDelta to set.
     */
    public void setFaultsDelta(Integer faultsDelta)
    {
        this.faultsDelta = faultsDelta;
    }
    /**
     * @return Returns the newFaults.
     */
    public Integer getNewFaults()
    {
        return newFaults;
    }
    /**
     * @param newFaults The newFaults to set.
     */
    public void setNewFaults(Integer newFaults)
    {
        this.newFaults = newFaults;
    }
    /**
     * @return Returns the removedFaults.
     */
    public Integer getRemovedFaults()
    {
        return removedFaults;
    }
    /**
     * @param removedFaults The removedFaults to set.
     */
    public void setRemovedFaults(Integer removedFaults)
    {
        this.removedFaults = removedFaults;
    }
    /**
     * @return Returns the totalFaults.
     */
    public Integer getTotalFaults()
    {
        return totalFaults;
    }
    /**
     * @param totalFaults The totalFaults to set.
     */
    public void setTotalFaults(Integer totalFaults)
    {
        this.totalFaults = totalFaults;
    }
    /**
     * @param project
     * @param conn
     * @return
     */
    public static Map<String, Snapshot> lookupLastSnapshotMap(Project project, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +ATTRIBUTES+ 
            " FROM submissions " +
            " WHERE submissions.project_pk = ? " +
            " AND submissions.commit_timestamp IS NOT NULL " +
            " ORDER BY commit_timestamp desc ";
    
        return getLastSnapshotMapFromQuery(query, project, conn);
    }
    
    /**
     * Gets a map from studentRegistrationPK to the last submission the student made.
     * @param stmt
     * @param queryTerm TODO
     * @param conn TODO
     * @return
     * @throws SQLException
     */
    private static Map<String, Snapshot> getLastSnapshotMapFromQuery(String query, Project project,
    		Connection conn) throws SQLException {
    
    	PreparedStatement stmt = null;
    	try {
    		stmt = conn.prepareStatement(query);
    		stmt.setString(1, project.getProjectPK());
    
    		Map<String, Snapshot> result = new HashMap<String, Snapshot>();
    		ResultSet rs = stmt.executeQuery();
    		while (rs.next()) {
    			String studentRegistrationPK = getStudentRegistrationPKFromResultSet(rs);
    			if (result.containsKey(studentRegistrationPK)) continue;
    			Snapshot snapshot = new Snapshot();
    			snapshot.fetchValues(rs, 1);
    			// set late status
    			snapshot.setStatus(project);
    			// adjust final score based on late status and late penalty
    			snapshot.setAdjustedScore(project);
    			result.put(studentRegistrationPK, snapshot);
    		}
    		return result;
    	} finally {
    		Queries.closeStatement(stmt);
    	}
    }

    /**
     * @param rs
     * @return
     */
    private static String getStudentRegistrationPKFromResultSet(ResultSet rs)
    throws SQLException
    {
        return rs.getString(2);
    }

    /**
     * @param project
     * @param conn
     * @return
     */
    public static Map<String, Snapshot> lookupLastOntimeSnapshotMap(Project project, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +ATTRIBUTES+ 
            " FROM submissions, student_submit_status, projects " +
            " WHERE submissions.project_pk = ? " +
            " AND submissions.commit_timestamp IS NOT NULL " +
            " AND student_submit_status.student_registration_pk = submissions.student_registration_pk " +
            " AND submissions.project_pk = projects.project_pk " +
            " AND commit_timestamp <= DATE_ADD(projects.ontime, INTERVAL student_submit_status.extension HOUR) " +
            " ORDER BY commit_timestamp desc ";
        
        return getLastSnapshotMapFromQuery(query, project, conn);
    }

    /**
     * @param project
     * @param conn
     * @return
     */
    public static Map<String, Snapshot> lookupLastLateSnapshotMap(Project project, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +ATTRIBUTES+ 
            " FROM submissions " +
            " WHERE submissions.project_pk = ? " +
            " AND submissions.commit_timestamp IS NOT NULL " +
            " ORDER BY commit_timestamp desc ";
    
        return getLastSnapshotMapFromQuery(query, project, conn);
    }

    /**
     * @param project
     * @param conn
     * @return
     */
    public static Map<String, Snapshot> lookupLastVeryLateSnapshotMap(Project project, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +ATTRIBUTES+ 
            " FROM submissions, student_submit_status, projects " +
            " WHERE submissions.project_pk = ? " +
            " AND submissions.commit_timestamp IS NOT NULL " +
            " AND student_submit_status.student_registration_pk = submissions.student_registration_pk " +
            " AND submissions.project_pk = projects.project_pk " +
            " AND commit_timestamp > DATE_ADD(projects.late, INTERVAL student_submit_status.extension HOUR) " +
            " ORDER BY commit_timestamp desc ";
        
        return getLastSnapshotMapFromQuery(query, project, conn);
    }

    /**
     * @param submissionPK
     * @param conn
     * @return
     */
    public static Snapshot lookupBySubmissionPK(String submissionPK, Connection conn)
    throws SQLException
    {
        String query = " SELECT " +ATTRIBUTES+ " "+
		" FROM "+
		" submissions "+
		" WHERE submissions.submission_pk = ? ";
		
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, submissionPK);
		
		return getFromPreparedStatement(stmt);
    }
    /**
	 * Executes the given preparedStatement to return a Submission.
	 * @param stmt the preparedStatement
	 * @return the submission returned by the preparedStatement; null if no submission
	 * is found.
	 * @throws SQLException
	 */
	private static Snapshot getFromPreparedStatement(PreparedStatement stmt)
	throws SQLException
	{
	    try {
	        ResultSet rs = stmt.executeQuery();
	        
	        if (rs.first())
	        {
	            Snapshot snapshot = new Snapshot();
	            snapshot.fetchValues(rs, 1);
	            return snapshot;
	        }
	        return null;
	    }
	    finally {
	        Queries.closeStatement(stmt);
	    }
	}

	/**
	 * Creates a map from submissionPK to the testRun object for the current test run
	 * (current_test_run_pk) for a given studentPK and projectPK.
	 * <p>
	 * <b><font color=red>NOTE:</font></b> Don't screw up the order of the parameters!
	 * projectPK, and then studentPK, and they're both Strings so there's no type-checking
	 * to make sure we get it right!
	 * @param projectPK the projectPK
	 * @param studentPK the studentPK
	 * @param conn the connection to the datbase
	 * @return a map from submissionPK to the current testRun
	 * @throws SQLException
	 */
	public static Map<String, TestRun> getCurrentTestRunMapByProjectPKAndStudentPK(
            String projectPK,
            String studentPK,
            Connection conn)
    throws SQLException
    {
	    List<Snapshot> snapshotList = Snapshot.lookupAllByProjectPKAndStudentPK(projectPK, studentPK, conn);
        Map<String, TestRun> testRunMap = new HashMap<String, TestRun>();
        for (Snapshot snapshot : snapshotList) {
            TestRun testRun = TestRun.lookupByTestRunPK(snapshot.getCurrentTestRunPK(), conn);
            testRunMap.put(snapshot.getSubmissionPK(), testRun);
        }
        return testRunMap;
    }
	
	/**
     * @param projectPK
     * @param studentPK
     * @param conn
     * @return
     */
    public static List<Snapshot> lookupAllByProjectPKAndStudentPK(String projectPK, String studentPK, Connection conn)
    throws SQLException
    {
        String query = "SELECT " +ATTRIBUTES+ " "+
    	" FROM " +
    	" submissions, student_registration " +
    	" WHERE student_registration.student_pk = ? " +
    	" AND commit_timestamp IS NOT NULL " +
    	" AND student_registration.student_registration_pk = submissions.student_registration_pk " +
    	" AND submissions.project_pk = ? " +
    	" ORDER BY submissions.commit_timestamp DESC ";
    	
    	PreparedStatement stmt = conn.prepareStatement(query);
    	
    	stmt.setString(1, studentPK);
    	stmt.setString(2, projectPK);
    	
    	return getListFromPreparedStatement(stmt);
    }
    
    public static List<Snapshot> lookupAllCompilableByProjectPK(String projectPK, Connection conn)
    throws SQLException
    {
        String query = "SELECT " +ATTRIBUTES+ " "+
        " FROM " +
        " submissions " +
        " WHERE project_pk = ? " +
        " AND num_build_tests_passed > 0 " +
        " ORDER BY submissions.commit_timestamp ASC ";
        
        PreparedStatement stmt=conn.prepareStatement(query);
        
        stmt.setString(1, projectPK);
        return getListFromPreparedStatement(stmt);
    }

    /**
     * @param stmt
     * @return
     */
    private static List<Snapshot> getListFromPreparedStatement(PreparedStatement stmt)
    throws SQLException
    {
        try {
    		ResultSet rs = stmt.executeQuery();
    		
    		List<Snapshot> snapshotList = new ArrayList<Snapshot>();
    		
    		while (rs.next())
    		{
    			Snapshot snapshot = new Snapshot();
    			snapshot.fetchValues(rs, 1);
    			snapshotList.add(snapshot);
    		}
    		return snapshotList;
    	} finally {
    		Queries.closeStatement(stmt);
    	}
    }

    /**
     * @param studentPK
     * @param projectPK
     * @param conn
     * @return
     */
    public static List<Snapshot> lookupAllByStudentRegistrationPKAndProjectPK(String studentRegistrationPK, String projectPK, Connection conn)
    throws SQLException
    {
        String query = "SELECT " +ATTRIBUTES+ " "+
    	" FROM " +
    	" submissions, student_registration " +
    	" WHERE student_registration.student_registration_pk = ? " +
    	" AND commit_cvstag IS NOT NULL " +
    	" AND student_registration.student_registration_pk = submissions.student_registration_pk " +
    	" AND submissions.project_pk = ? " +
    	" ORDER BY submissions.commit_timestamp ASC ";
    	
    	PreparedStatement stmt = conn.prepareStatement(query);
    	
    	stmt.setString(1, studentRegistrationPK);
    	stmt.setString(2, projectPK);
    	
    	return getListFromPreparedStatement(stmt);
    }

    /**
     * @param conn
     * @return
     */
    public static List<Snapshot> lookupAllInDatabase(Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +ATTRIBUTES+
            " FROM submissions ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        
        return getListFromPreparedStatement(stmt);
    }

    public static List<Snapshot> lookupAllUniqueSnapshotsByStudentRegistrationPKAndProjectPK(
            String studentRegistrationPK,
            String projectPK,
            Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +ATTRIBUTES+
            " FROM " +TABLE_NAME+
            " WHERE num_build_tests_passed > 0 " +
            " AND student_registration_pk = ? " +
            " AND project_pk = ? " +
            " AND previous_md5sum_classfiles IS NULL " +
            " AND faults_delta IS NOT NULL ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, studentRegistrationPK);
        stmt.setString(2, projectPK);
        
        return getListFromPreparedStatement(stmt);
    }
    
    /**
     * @param projectNumber
     * @param conn
     * @return
     */
    public static List<Snapshot> lookupAllUniqueSnapshotsByProjectNumber(String projectPK, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +ATTRIBUTES+
            " FROM " +TABLE_NAME+
            " WHERE num_build_tests_passed > 0 " +
            " AND previous_md5sum_classfiles IS NULL " +
            " AND project_pk = ? " +
            " AND faults_delta IS NOT NULL ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, projectPK);
        
        return getListFromPreparedStatement(stmt);
    }

    /**
     * @param conn
     * @return
     */
    public static List<Snapshot> lookupAllUniqueSnapshots(Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +ATTRIBUTES+
            " FROM " +TABLE_NAME+
            " WHERE num_build_tests_passed > 0 " +
            " AND previous_md5sum_classfiles IS NULL " +
            " AND faults_delta IS NOT NULL " +
            " ORDER BY student_registration_pk, project_pk, commit_number ";
        
//        TreeSet sortedSnapshots = new TreeSet(new Comparator() {
//           public int compare(Object o1, Object o2) {
//               Snapshot s1 = (Snapshot)o1;
//               Snapshot s2 = (Snapshot)o2;
//               int studentRegistrationComparison = s1.getStudentRegistrationPK().compareTo(s2.getStudentRegistrationPK());
//               if (studentRegistrationComparison != 0)
//                   return studentRegistrationComparison;
//               int projectComparison = s1.getProjectPK().compareTo(s2.getProjectPK());
//               if (projectComparison != 0)
//                   return projectComparison;
//               return s1.getCommitNumber().compareTo(s2.getCommitNumber());
//           }
//        });
        List<Snapshot> result = new LinkedList<Snapshot>();
        
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Snapshot snapshot = new Snapshot();
                snapshot.fetchValues(rs, 1);
                result.add(snapshot);
            }
            return result;
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    public static Snapshot submitOneProject(String cvsAccount, 
            String projectNumber,
            String semester,
            long commitCvstag,
            String courseName,
            String cvsTagTimestamp,
            String submitClientTool,
            byte[] snapshotBytes,
            Connection conn)
    throws SQLException
    {
        // NOTE: cvsTagTimestamp will be null for unsubmitted snapshots
    
        // could be useful to convert from GMT to localtime
        Timestamp commitTimestamp = new Timestamp(commitCvstag);
        
        // start new transaction
        boolean transactionSuccess=false;
        try {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            
            // look up project
            Project project = Project.lookupByCourseProjectSemester(
                    courseName,
                    projectNumber,
                    semester,
                    conn);
            
            // look up studentRegistration
            StudentRegistration studentRegistration = StudentRegistration.lookupByCvsAccountAndCoursePK(
                    cvsAccount,
                    project.getCoursePK(),
                    conn);
            if (studentRegistration == null) {
                // FIXME throw a more descriptive exception than SQLException
                throw new SQLException(cvsAccount +" is not registered for " +courseName+
                        " in " +semester);
            }
            
            // ensure that this snapshot has not already been uploaded
            Snapshot previouslyUploaded = Snapshot.lookupByCommitCvsTagStudentRegistrationPKAndProjectPK(
                    commitCvstag,
                    studentRegistration.getStudentRegistrationPK(),
                    project.getProjectPK(),
                    conn);
            
            if (previouslyUploaded != null) {
                // FIXME throw an exception instead of returning null
               throw new SQLException("Already submitted, commitCvstag: " +commitCvstag+
                        ", studentRegistrationPK: "
                        +studentRegistration.getStudentRegistrationPK()+
                        ", projectPK: " +project.getProjectPK());
            }

            // find StudentSubmitStatus record
            StudentSubmitStatus studentSubmitStatus = 
                StudentSubmitStatus.lookupByStudentRegistrationPKAndProjectPK(
                        studentRegistration.getStudentRegistrationPK(),
                        project.getProjectPK(),
                        conn);
            
            // if StudentSubmitStatus doesn't exist, then create it
            if (studentSubmitStatus == null) {
                studentSubmitStatus = StudentSubmitStatus.createAndInsert(
                        project.getProjectPK(),
                        studentRegistration.getStudentRegistrationPK(),
                        conn);
            }
            
            int commitNumber = studentSubmitStatus.getNumberCommits() + 1;
            // fetch how many commits have already been made
            studentSubmitStatus.setNumberCommits(commitNumber);
            int submitNumber = -1;
            // If snapshot was also submission, increment the numberSubmissions
            if (cvsTagTimestamp != null)
            {
                submitNumber = studentSubmitStatus.getNumberSubmissions() + 1;
                studentSubmitStatus.setNumberSubmissions(submitNumber);
            }
            studentSubmitStatus.update(conn);
            
            // prepare new snapshot record
            Snapshot snapshot = new Snapshot();
            snapshot.setStudentRegistrationPK(studentRegistration.getStudentRegistrationPK());
            
            snapshot.setProjectPK(project.getProjectPK());
            
            // number of intermediate commits
            snapshot.setCommitNumber(new Integer(commitNumber));
            // set the commitTimestamp and submissionTimestamp based on the uploaded long value
            snapshot.setCommitTimestamp(commitTimestamp);
            // set the commitCvstag-- this is the ONLY way we can match submissions
            // in the DB with information in the CVS repository
            snapshot.setCommitCvstag(Long.toString(commitCvstag));
            
            // cvs submission tag; this will be null for unsubmitted snapshots
            if (cvsTagTimestamp != null)
            {
                snapshot.setCvsTagTimestamp(cvsTagTimestamp);
                snapshot.setSubmissionNumber(new Integer(submitNumber).toString());
                snapshot.setSubmissionTimestamp(commitTimestamp);
            }
            
            snapshot.setBuildStatus(project.getInitialBuildStatus());
            snapshot.setSubmitClient(submitClientTool);
            
            // set the byte array as the archive
            snapshot.setArchiveForUpload(snapshotBytes);
            
            snapshot.insert(conn);
            conn.commit();
            transactionSuccess = true;
            return snapshot;
        } finally {
            Queries.rollbackIfUnsuccessful(transactionSuccess,conn);
        }
    }

    /**
     * @param commitCvstag
     * @param studentRegistrationPK
     * @param projectPK
     * @param conn
     * @return
     */
    private static Snapshot lookupByCommitCvsTagStudentRegistrationPKAndProjectPK(
            long commitCvstag,
            String studentRegistrationPK,
            String projectPK,
            Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +ATTRIBUTES+
            " FROM " +TABLE_NAME+
            " WHERE student_registration_pk = ? " +
            " AND project_pk = ? " +
            " AND commit_cvstag = ? ";
        
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1, studentRegistrationPK);
        stmt.setString(2, projectPK);
        stmt.setString(3, String.valueOf(commitCvstag));
        
        return getFromPreparedStatement(stmt);
    }
    
    public static Snapshot lookupPreviousSnapshot(String submissionPK, Connection conn)
    throws SQLException
    {
        String query =
            " SELECT s2.* " +
            " FROM submissions as s1, submissions as s2 " +
            " WHERE s1.student_registration_pk = s2.student_registration_pk " +
            " AND s1.project_pk = s2.project_pk " +
            " AND s1.submission_pk = ? " +
            " AND s1.commit_number > s2.commit_number " +
            //" AND s2.previous_md5sum_classfiles is null " +
            " AND s2.num_build_tests_passed > 0 " +
            " AND s1.commit_number != 1 " +
            " ORDER BY s2.commit_number DESC " +
            " LIMIT 1 ";
        
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1, submissionPK);
        return getFromPreparedStatement(stmt);
    }
    
    /**
     * Computes the textual diff between this snapshot and a previously given snapshot.
     * @param previous The previous snapshot to diff with.
     * @param conn Connection to the database.
     * @throws SQLException
     * @throws IOException
     */
    public void diffWithPrevious(Snapshot previous, Connection conn)
    throws SQLException, IOException
    {
//      compute the diff between the old and new versions
        byte[] oldBytes = previous.downloadArchive(conn);
        byte[] newBytes = downloadArchive(conn);
        String diff = diffTwoSubmissions(oldBytes, newBytes);
        // set new diff file
        setDiffFile(diff);
        
        // parse the diff file
        StringBuffer numLinesChanged = new StringBuffer();
        StringBuffer netChanged = new StringBuffer();
        StringBuffer numLinesDeleted = new StringBuffer();
        parseDiffFile(diff, numLinesChanged, netChanged, numLinesDeleted);
        
        // set the necessary info for the diff file
        setNumLinesChanged(new Integer(numLinesChanged.toString()));
        setNetChange(new Integer(netChanged.toString()));
    }

    /**
     * @param dir
     * @param zis
     */
    private static void unzipToDirectory(File dir, ZipInputStream zis)
    throws IOException
    {
        ZipEntry e;
        while((e=zis.getNextEntry())!= null) {
            if (e.isDirectory())
            {
                File newDir = new File(dir, e.getName());
                newDir.mkdirs();
            }
            else
            {
                File outfile = new File(dir, e.getName());
                FileOutputStream out = new FileOutputStream(outfile);
                byte [] b = new byte[512];
                int len = 0;
                while ( (len=zis.read(b))!= -1 ) {
                    out.write(b,0,len);
                }
                out.close();
            }
        }
        zis.close();
    }

    /**
     * @param oldBytes
     * @param newBytes
     * @return
     */
    static String diffTwoSubmissions(byte[] oldBytes, byte[] newBytes)
    throws IOException
    {
        File tempDir = null;
        try {
            tempDir=new File(new File("/tmp"), "temp." +System.currentTimeMillis());
            if (!tempDir.mkdirs())
                throw new RuntimeException("Unable to create directory " +tempDir.getAbsolutePath());
            tempDir.deleteOnExit();
            //System.out.println(tempDir.getAbsolutePath());
            File oldDir = new File(tempDir, "old");
            oldDir.mkdir();
            File newDir = new File(tempDir, "new");
            newDir.mkdir();
            
            FileOutputStream oldFos = new FileOutputStream(new File(tempDir, "old.zip"));
            ByteArrayInputStream oldBais = new ByteArrayInputStream(oldBytes); 
            CopyUtils.copy(oldBais, oldFos);
            oldFos.close();
            
            FileOutputStream newFos = new FileOutputStream(new File(tempDir, "new.zip"));
            ByteArrayInputStream newBais = new ByteArrayInputStream(newBytes); 
            CopyUtils.copy(newBais, newFos);
            newFos.close();
            
            ZipInputStream oldZip = new ZipInputStream(new ByteArrayInputStream(oldBytes));
            unzipToDirectory(oldDir, oldZip);
            
            ZipInputStream newZip = new ZipInputStream(new ByteArrayInputStream(newBytes));
            unzipToDirectory(newDir, newZip);
            
            String[] diffCmd = {"diff",
            "-rbw",
            oldDir.getAbsolutePath(),
            newDir.getAbsolutePath()
            };
            
            JProcess jproc = new JProcess(diffCmd);
            int exitCode=jproc.waitFor(0);
            return jproc.getOut();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (tempDir!=null)
                FileUtils.deleteDirectory(tempDir);
        }
    }

    /**
     * Parses the text of the given as a String, changing the three outputs (numLinesChanged,
     * netChanged, numLinesDeleted) by side-effect to reflect the parsed-up results of
     * the diff file.
     * @param diff The String representation of the diff file to be parsed.
     * @param numLinesChanged
     * @param netChanged
     * @param numLinesDeleted
     */
    private static void parseDiffFile(String diff, StringBuffer numLinesChanged, StringBuffer netChanged, StringBuffer numLinesDeleted)
    {
        String[] lines = diff.split("\n");
        
        String regexp = "((\\d+)(,(\\d+))?([acd])(\\d+)(,(\\d+))?)";
        Pattern pattern = Pattern.compile(regexp);
        
        int totalLinesDeleted=0;
        int totalLinesAddedOrChanged=0;
        int netFileSizeChange=0;
        
        for (int ii=0; ii < lines.length; ii++)
        {
            String line = lines[ii];
            Matcher match = pattern.matcher(line);
            if (match.matches())
            {
                
                // my $stmt = $1;
                String stmt = match.group(1);
                // my $srcRangeStart = $2;
                String srcRangeStart = match.group(2);
                //#my $srcRangeStartWithComma = $3;
                //my $srcRangeEnd = $4;
                String srcRangeEnd = match.group(4);
                //my $type = $5;
                String type = match.group(5);
                //my $dstRangeStart = $6;
                String dstRangeStart = match.group(6);
                //#my $dstRangeEndWithComma = $7;
                //my $dstRangeEnd = $8;
                String dstRangeEnd = match.group(8);
                
                if (type.equals("a"))
                {
                    int linesAdded=1;
                    if (dstRangeEnd != null && !dstRangeEnd.equals(""))
                    {
                        //# At this point we know that more than one line was added
                        //# So use the ranges to compute how many lines were added
                        //# we need to add 1 because 4,5a4,9 means we changed 2 lines but 5-4 = 1
                        linesAdded = Integer.parseInt(dstRangeEnd) - Integer.parseInt(dstRangeStart) + 1;
                    }
                    totalLinesAddedOrChanged += linesAdded;
                    netFileSizeChange += linesAdded;
                }
                else if (type.equals("c"))
                {
                    //# lines changed
                    //#
                    //# Similar to the above case for adding lines
                    //# Initially, assume we've changed one line
                    //# Then, if the dstRange only specifies a single line, we're done
                    //# Otherwise we compute the number of lines affected
                    //# 
                    int srcLinesChanged = 1;
                    if (srcRangeEnd != null && !srcRangeEnd.equals(""))
                    {
                        //# we need to add 1 because 4,5c4,9 means we changed 2 lines but 5-4 = 1
                        srcLinesChanged = Integer.parseInt(srcRangeEnd) - Integer.parseInt(srcRangeStart) + 1;
                    }
                    
                    int dstLinesChanged = 1;
                    if (dstRangeEnd != null && !dstRangeEnd.equals(""))
                    {
                        dstLinesChanged = Integer.parseInt(dstRangeEnd) - Integer.parseInt(dstRangeStart) + 1;
                    }
                    //#
                    //# if we replace 3 lines with 5 lines, that's 5 lines changed (3 changed plus 2 added, net increase of 2 lines)
                    //# whereas if we replace 7 lines with 4 lines, that's 4 lines changed (3 deleted plus 4 changed, net decrease of 3 line)
                    //# either way, we've always changed "dstLinesChanged" number of lines
                    //# and we've changed the net file size by (dstLinesChanged - srcLinesChanged) lines of code
                    //#
                    totalLinesAddedOrChanged += dstLinesChanged;
                    //# 
                    int netChange = dstLinesChanged - srcLinesChanged;
                    netFileSizeChange += netChange;
                    //# if we caused a net decrease in the file size, make a note of that
                    if (netChange < 0)
                    {
                        totalLinesDeleted += Math.abs(netChange);
                    }
                }
                else if (type.equals("d"))
                {
                    //#print "$stmt\n";
                    //# lines deleted
                    //#
                    int linesDeleted = 1;
                    if (srcRangeEnd != null && !srcRangeEnd.equals(""))
                    {
                        //# add 1 because 4,9d10 means we deleted 5 lines
                        linesDeleted = Integer.parseInt(srcRangeEnd) - Integer.parseInt(srcRangeStart) + 1;
                    }
                    
                    //# 	    my $linesDeleted = 1;
                    //# 	    if ($dstRangeEnd ne "")
                    //# 	    {
                    //# 		$linesDeleted = $dstRangeEnd = $dstRangeStart + 1;
                    //# 	    }
                    totalLinesDeleted += linesDeleted;
                    netFileSizeChange -= linesDeleted;
                }
            }
        }
        numLinesChanged.append(totalLinesAddedOrChanged);
        numLinesDeleted.append(totalLinesDeleted);
        netChanged.append(netFileSizeChange);
    }

    public static List<Snapshot> lookupCompilableSnapshotsByStudentRegistrationPKAndProjectPK(
        String studentRegistrationPK,
        String projectPK,
        Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +ATTRIBUTES+
            " FROM " +TABLE_NAME+
            " WHERE num_build_tests_passed > 0 " +
            " AND student_registration_pk = ? " +
            " AND project_pk = ? " +
            " ORDER BY commit_number ";
        List<Snapshot> result = new LinkedList<Snapshot>();
        
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, studentRegistrationPK);
            stmt.setString(2, projectPK);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Snapshot snapshot = new Snapshot();
                snapshot.fetchValues(rs, 1);
                result.add(snapshot);
            }
            return result;
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    public static void lookupSnapshotsAndTestRunsByProjectPKAndStudentRegistrationPK(
        List<Snapshot> snapshotList,
        Map<String,TestRun> testRunMap,
        String projectPK,
        String studentRegistrationPK,
        Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +ATTRIBUTES+ "," +TestRun.ATTRIBUTES+ " "+
            " FROM " +TABLE_NAME+ ", " +TestRun.TABLE_NAME + " "+
            " WHERE submissions.current_test_run_pk = test_runs.test_run_pk " +
            " AND project_pk = ? " +
            " AND student_registration_pk = ? " +
            " ORDER BY commit_number ASC ";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, projectPK);
            stmt.setString(2, studentRegistrationPK);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Snapshot snapshot = new Snapshot();
                TestRun testRun=new TestRun();
                int index=snapshot.fetchValues(rs, 1);
                testRun.fetchValues(rs, index);
                snapshotList.add(snapshot);
                testRunMap.put(snapshot.getSubmissionPK(), testRun);
            }
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    public static List<Snapshot> lookupAllByProjectPKAndNumLinesChanged(
        String projectPK,
        String numLinesChanged,
        String operator,
        Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +ATTRIBUTES+
            " FROM submissions " +
            " WHERE project_pk = ? " +
            " AND num_lines_changed " +operator+ " ? ";
        
        List<Snapshot> snapshotList=new LinkedList<Snapshot>();
        PreparedStatement stmt=null;
        try {
            stmt=conn.prepareStatement(query);
            stmt.setString(1, projectPK);
            stmt.setString(2, numLinesChanged);
            ResultSet rs=stmt.executeQuery();
            while (rs.next()) {
                Snapshot snapshot=new Snapshot();
                snapshot.fetchValues(rs, 1);
                snapshotList.add(snapshot);
            }
            return snapshotList;
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    public static List<Snapshot> lookupAllCompilableByProjectPKAndNumLinesChanged(
        String projectPK,
        Connection conn)
    throws SQLException
    {
        String sql=
            " SELECT * " +
            " FROM submissions " +
            " WHERE submissions.project_pk = ? " +
            " AND submissions.num_build_tests_passed > 0 ";
        List<Snapshot> snapshotList=new LinkedList<Snapshot>();
        PreparedStatement stmt=null;
        try {
            stmt=conn.prepareStatement(sql);
            stmt.setString(1, projectPK);
            ResultSet rs=stmt.executeQuery();
            while (rs.next()) {
                Snapshot snapshot=new Snapshot();
                snapshot.fetchValues(rs, 1);
                snapshotList.add(snapshot);
            }
            return snapshotList;
        } finally {
            Queries.closeStatement(stmt);
        }
        
    }
    
    /**
     * Get a list of the lists of all snapshots for each student containing a 
     * particular warning code prefix (i.e. all null-pointer warnings start with 
     * the warningPrefix NP).
     * 
     * @param warningPrefix The warning-code prefix of the warning (i.e. NP for all 
     * null-pointer warnings but NP_ALWAYS_NULL for only the always null warnings).
     * @param conn Connection to the database.
     * @return List of lists of all snapshots by each student with a warning
     *  with the given prefix.
     * @throws SQLException
     */
    public static List<List<Snapshot>> lookupMetaSnapshotsWithFindBugsWarning(
        String warningPrefix,
        Connection conn)
    throws SQLException
    {
        String sql=
            " select distinct " +ATTRIBUTES+
            " from test_outcomes, submissions " +
            " where test_type = 'findbugs' " + 
            " and test_name like ? " +
            " and test_outcomes.test_run_pk = submissions.current_test_run_pk " +
            " and submissions.num_build_tests_passed > 0 " +
            " and submissions.previous_md5sum_classfiles is null " +
            " order by submissions.project_pk, student_registration_pk, commit_number ";
        PreparedStatement stmt=null;
        try {
            stmt=conn.prepareStatement(sql);
            stmt.setString(1,warningPrefix+"%");
            
            return getMetaListFromStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    public static List<List<Snapshot>> lookupMetaSnapshotsWithRuntimeException(
        String exceptionClassName,
        Connection conn)
    throws SQLException
    {
        String sql=
            " select distinct " +ATTRIBUTES+
            " from test_outcomes, submissions " +
            " where test_type in ('public','release','secret') " + 
            " and outcome = 'error' " +
            " and exception_class_name = ? " +
            " and test_outcomes.test_run_pk = submissions.current_test_run_pk " +
            " and submissions.num_build_tests_passed > 0 " +
            " and submissions.previous_md5sum_classfiles is null " +
            " order by submissions.project_pk, student_registration_pk, commit_number ";
        PreparedStatement stmt=null;
        try {
            stmt=conn.prepareStatement(sql);
            stmt.setString(1,exceptionClassName);
            
            return getMetaListFromStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    /**
     * Returns a list of a list of snapshots returned by the given statement.
     * The statement will call for snapshots to be ordered by project_pk, then
     * student_registration_pk, then commit_number so that the resulting
     * snapshots can easily be broken up into lists of snapshots for a each
     * student.
     * 
     * @param stmt
     * @return
     * @throws SQLException
     */
    private static List<List<Snapshot>> getMetaListFromStatement(PreparedStatement stmt) throws SQLException
    {
        List<List<Snapshot>> resultList=new LinkedList<List<Snapshot>>();
        
        List<Snapshot> currentList=null;
        String studentRegistrationPK=null;
        
        ResultSet rs=stmt.executeQuery();
        
        while (rs.next()) {
            Snapshot snapshot=new Snapshot();
            snapshot.fetchValues(rs,1);
            if (studentRegistrationPK==null) {
                // We haven't seen any snapshots yet
                currentList=new LinkedList<Snapshot>();
                currentList.add(snapshot);
                studentRegistrationPK=snapshot.getStudentRegistrationPK();
            } else if (studentRegistrationPK.equals(snapshot.getStudentRegistrationPK())) {
                // This snapshot is the same student we saw previously
                // So add it to the current list
                currentList.add(snapshot);
            } else {
                // Snapshot is for a new student
                // Add the existing "current" list to the result list
                // Create a new current list
                // And set the cached studentRegistrationPK to reflect this snapshot
                resultList.add(currentList);
                currentList=new LinkedList<Snapshot>();
                studentRegistrationPK=snapshot.getStudentRegistrationPK();
            }
        }
        // If there's anything left in the current list, add it to the result list
        if (currentList!=null && currentList.size() > 0)
            resultList.add(currentList);
        return resultList;
    }
    
}
