/*
 * Created on Aug 30, 2004
 */
package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.umd.cs.marmoset.utilities.MarmosetUtilities;

/**
 * Object to represent a single row in the submissions table.
 * <p>
 * <b>NOTE:</b>
 * A submission is required to have a non-null submission_timestamp field.
 * This is the sole mechanism that differentiates a submission from a "snapshot"
 * submission (i.e. something that's dumped into the database from CVS)
 * 
 * 
 * @author daveho
 * @author jspacco
 *
 */
public class Submission implements ITestSummary {
    public static final String TABLE_NAME = "submissions";
    
	// Possible values for build_status

    public static final String NEW = "new";
	public static final String PENDING = "pending";
	public static final String COMPLETE = "complete";
	public static final String ACCEPTED = "accepted";	
	public static final String RETEST = "retest";
	public static final String BROKEN = "broken";
	public static final String BACKGROUND = "background";
	
	public static final String SUCCESSFUL = "Successful";	
	public static final String ON_TIME= "On-time";	
	public static final String LATE= "Late";
	public static final String VERY_LATE= "Very Late";
	
	// Value if the submission test machine is not known
	public static final String UNKNOWN_TEST_MACHINE = "unknown";
	
	// XXX these defaults correspond to the database defaults!
	private String submissionPK;
	private String studentRegistrationPK = "0";
	private String projectPK = "0";
	private int numTestRuns = 0;
	private String currentTestRunPK;
	private String submissionNumber = "0";
	private Timestamp submissionTimestamp;
	private String cvsTagTimestamp;
	private Timestamp buildRequestTimestamp;
	private String buildStatus = "new";
	private String submitClient = "unknown";
	private Timestamp releaseRequest;
	private boolean releaseEligible;
	private int valuePassedOverall;
	private boolean compileSuccessful;
    private int valuePublicTestsPassed;
    private int valueReleaseTestsPassed;
    private int valueSecretTestsPassed;
    private int numFindBugsWarnings;
	private String archivePK;
	/**
	 * This is a write-only field.  Users can set a byte array as the cached archive
	 * for upload, but they can only retrieve the archive via the 
	 * {@link #downloadArchive(Connection) downloadArchive} method.
	 * <p>
	 * In fact, 'cachedArchive' is a midleading because we only cache the archive
	 * before we upload it.  We specifically <b>don't</b> perform any caching of
	 * downloads.  The only way to retrieve the bytes of archive is to download them
	 * directly from the database each time.
	 */
	private byte[] cachedArchive;
    private String status;
    private int adjustedScore;

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
	    return MarmosetUtilities.hashString(submissionPK) + MarmosetUtilities.hashString("submission");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o == null) return false;
	    if (this.getClass() != o.getClass())
	        return false;
	    Submission other = (Submission) o;
	    return MarmosetUtilities.stringEquals(submissionPK, other.getSubmissionPK());
	}

	/**
	 * List of all attributes of submissions table.
	 */
	  static final String[] ATTRIBUTE_NAME_LIST = {
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
		"archive_pk"
	};
	
	/**
	 * Fully-qualified attributes for submissions table.
	 */
	public static final String ATTRIBUTES =
		Queries.getAttributeList(TABLE_NAME, ATTRIBUTE_NAME_LIST);
	
	/**
	 * Constructor.
	 * All fields will have default values.
	 */
	public Submission() {}
	
	/**
	 * @return true if this submission has had a release request, false otherwise.
	 */
	public boolean isReleaseTestingRequested() {
	    return getReleaseRequest() != null;
	}
    /**
     * @return Returns the numPassedOverall.
     */
    public int getValuePassedOverall()
    {
        return valuePassedOverall;
    }
    /**
     * @param numPassedOverall The numPassedOverall to set.
     */
    public void setValuePassedOverall(int numPassedOverall)
    {
        this.valuePassedOverall = numPassedOverall;
    }
    /**
     * @return Returns the numBuildTestsPassed.
     */
    public boolean isCompileSuccessful()
    {
        return compileSuccessful;
    }
    /**
     * @param success The numBuildTestsPassed to set.
     */
    public void setCompileSuccessful(boolean success)
    {
        this.compileSuccessful = success;
    }
    /**
     * @return Returns the numFindBugsWarnings.
     */
    public int getNumFindBugsWarnings()
    {
        return numFindBugsWarnings;
    }
    /**
     * @param numFindBugsWarnings The numFindBugsWarnings to set.
     */
    public void setNumFindBugsWarnings(int numFindBugsWarnings)
    {
        this.numFindBugsWarnings = numFindBugsWarnings;
    }
    /**
     * @return Returns the numPublicTestsPassed.
     */
    public int getValuePublicTestsPassed()
    {
        return valuePublicTestsPassed;
    }
    /**
     * @param numPublicTestsPassed The numPublicTestsPassed to set.
     */
    public void setValuePublicTestsPassed(int numPublicTestsPassed)
    {
        this.valuePublicTestsPassed = numPublicTestsPassed;
    }
    /**
     * @return Returns the numReleaseTestsPassed.
     */
    public int getValueReleaseTestsPassed()
    {
        return valueReleaseTestsPassed;
    }
    /**
     * @param numReleaseTestsPassed The numReleaseTestsPassed to set.
     */
    public void setValueReleaseTestsPassed(int numReleaseTestsPassed)
    {
        this.valueReleaseTestsPassed = numReleaseTestsPassed;
    }
    /**
     * @return Returns the numSecretTestsPassed.
     */
    public int getValueSecretTestsPassed()
    {
        return valueSecretTestsPassed;
    }
    /**
     * @param numSecretTestsPassed The numSecretTestsPassed to set.
     */
    public void setValueSecretTestsPassed(int numSecretTestsPassed)
    {
        this.valueSecretTestsPassed = numSecretTestsPassed;
    }
	/**
	 * @return Returns the releaseRequest.
	 */
	public Timestamp getReleaseRequest() {
		return releaseRequest;
	}
	/**
	 * @param releaseRequest The releaseRequest to set.
	 */
	public void setReleaseRequest(Timestamp releaseRequest) {
		this.releaseRequest = releaseRequest;
	}
    public boolean isReleaseEligible() {
        return releaseEligible;
    }
    public void setReleaseEligible(boolean releaseEligible) {
        this.releaseEligible = releaseEligible;
    }
	/**
	 * @return Returns the buildRequestTimestamp.
	 */
	public Timestamp getBuildRequestTimestamp() {
		return buildRequestTimestamp;
	}
	/**
	 * @param buildRequestTimestamp The buildRequestTimestamp to set.
	 */
	public void setBuildRequestTimestamp(Timestamp buildRequestTimestamp) {
		this.buildRequestTimestamp = buildRequestTimestamp;
	}
	/**
	 * @return Returns the buildStatus.
	 */
	public String getBuildStatus() {
		return buildStatus;
	}
	/**
	 * @param buildStatus The buildStatus to set.
	 */
	public void setBuildStatus(String buildStatus) {
		this.buildStatus = buildStatus;
	}
	/**
	 * @return Returns the cvsTagTimestamp.
	 */
	public String getCvsTagTimestamp() {
		return cvsTagTimestamp;
	}
	/**
	 * @param cvsTagTimestamp The cvsTagTimestamp to set.
	 */
	public void setCvsTagTimestamp(String cvsTagTimestamp) {
		this.cvsTagTimestamp = cvsTagTimestamp;
	}
	/**
	 * @return Returns the projectPK.
	 */
	public String getProjectPK() {
		return projectPK;
	}
    
	/**
	 * @param projectPK The projectPK to set.
	 */
	public void setProjectPK(String projectPK) {
		this.projectPK = projectPK;
	}
	/**
	 * @return Returns the studentRegistrationPK.
	 */
	public String getStudentRegistrationPK() {
		return studentRegistrationPK;
	}
	/**
	 * @param studentRegistrationPK The studentRegistrationPK to set.
	 */
	public void setStudentRegistrationPK(String studentRegistrationPK) {
		this.studentRegistrationPK = studentRegistrationPK;
	}
	/**
	 * @return Returns the submissionNumber.
	 */
	public String getSubmissionNumber() {
		return submissionNumber;
	}
	/**
	 * @param submissionNumber The submissionNumber to set.
	 */
	public void setSubmissionNumber(String submissionNumber) {
		this.submissionNumber = submissionNumber;
	}
	/**
	 * @return Returns the submissionPK.
	 */
	public String getSubmissionPK() {
		return submissionPK;
	}
	/**
	 * @param submissionPK The submissionPK to set.
	 */
	public void setSubmissionPK(String submissionPK) {
		this.submissionPK = submissionPK;
	}
	/**
	 * @return Returns the formatted submissionTimestamp.
	 */
	public String getFormattedSubmissionTimestamp() {
		return Formats.date.format(submissionTimestamp);
	}
	
	/**
	 * @return Returns the submissionTimestamp.
	 */
	public Timestamp getSubmissionTimestamp() {
		return submissionTimestamp;
	}
	/**
	 * @param submissionTimestamp The submissionTimestamp to set.
	 */
	public void setSubmissionTimestamp(Timestamp submissionTimestamp) {
		this.submissionTimestamp = submissionTimestamp;
	}

	/**
	 * @return Returns the pluginVersion.
	 */
	public String getSubmitClient() {
		return submitClient;
	}
	/**
	 * @param pluginVersion The pluginVersion to set.
	 */
	public void setSubmitClient(String pluginVersion) {
		this.submitClient = pluginVersion;
	}

	/**
	 * Uploads the bytes of a cached archive to the database.
	 * @param conn the connection to the database
	 * @return the archivePK of the newly uploaded archive
	 * @throws SQLException
	 */
	public String uploadCachedArchive(Connection conn)
	throws SQLException
	{
	    return Archive.uploadBytesToArchive("submission_archives", cachedArchive, conn);
	}
    
    public void updateCachedArchive(byte[] bytes, Connection conn)
    throws SQLException
    {
        Archive.updateBytesInArchive("submission_archives", archivePK, cachedArchive, conn);
    }
	/**
	 * Does this submission have an archive cached as bytes ready for upload to the database?
	 * @return true if this submission has a cached archive, false otherewise
	 */
	public boolean hasCachedArchive()
	{
	    return cachedArchive != null;
	}
	/**
	 * Sets the byte array of the archive for upload to the database.
	 * @param bytes array of bytes of the cached archive
	 */
	public void setArchiveForUpload(byte[] bytes)
	{
	    cachedArchive = bytes;
	}
	
	/**
	 * Downloads the bytes of the archive from the database and returns them directly.
	 * @param conn the connection to the database
	 * @return an array of bytes of the cached archive
	 * @throws SQLException
	 */
	public byte[] downloadArchive(Connection conn)
    throws SQLException
    {
	    return Archive.downloadBytesFromArchive("submission_archives", getArchivePK(), conn);
    }
	
    /**
     * @return Returns the numTestOutcomes.
     */
    public int getNumTestRuns() {
        return numTestRuns;
    }
    /**
     * @param numTestOutcomes The numTestOutcomes to set.
     */
    public void setNumTestRuns(int numTestOutcomes) {
        this.numTestRuns = numTestOutcomes;
    }
    /**
     * @return Returns the currentTestRunPK.
     */
    public String getCurrentTestRunPK()
    {
        return currentTestRunPK;
    }
    /**
     * @param currentTestRunPK The currentTestRunPK to set.
     */
    public void setCurrentTestRunPK(String currentTestRunPK)
    {
        this.currentTestRunPK = currentTestRunPK;
    }
    /**
     * @return Returns the adjustedScore.
     */
    public int getAdjustedScore()
    {
        return adjustedScore;
    }
    /**
     * @param adjustedScore The adjustedScore to set.
     */
    public void setAdjustedScore(Project project)
    {
        // check if this submission is late...
        if (submissionTimestamp.after(project.getOntime()))
        {
            if (project.getKindOfLatePenalty().equals(Project.CONSTANT))
            {
                adjustedScore = Math.max(0, getValuePassedOverall() - project.getLateConstant());
            }
            else if (project.getKindOfLatePenalty().equals(Project.MULTIPLIER))
            {
                adjustedScore = Math.max(0, (int)(getValuePassedOverall() * project.getLateMultiplier()));
            }
            else {
                throw new IllegalStateException("Late penalties for a project must be " +
                        "either " +Project.CONSTANT +" or "+ Project.MULTIPLIER);
            }
        }
        else
        {
            adjustedScore = getValuePassedOverall();
        }
    }
    /**
     * @return Returns the status.
     */
    public String getStatus()
    {
        return status;
    }
    /**
     * @param status The status to set.
     */
    public void setStatus(Project project)
    {
        if (project.getOntime().after(submissionTimestamp))
        {
            status = ON_TIME;
        }
        else if (submissionTimestamp.after(project.getOntime()) && 
                submissionTimestamp.before(project.getLate()))
        {
            status = LATE;
        }
        else status = VERY_LATE;
    }
    
    public void setStatus(Project project, int extension)
    {
        if (extension == 0)
        {
            setStatus(project);
            return;
        }
        Timestamp extendedOntime = new Timestamp(project.getOntime().getTime() + (extension*3600*1000));
        Timestamp extendedLate = new Timestamp(project.getLate().getTime() + (extension*3600*1000));
        if (extendedOntime.after(submissionTimestamp))
        {
            status = ON_TIME + "-extended-" +extension;
        }
        else if (submissionTimestamp.after(extendedOntime) && 
                submissionTimestamp.before(extendedLate))
        {
            status = LATE + "-extended-" +extension;
        }
        else status = VERY_LATE;
    }

    /**
	 * Populate a Submission from a ResultSet that is positioned
	 * at a row of the submissions table.
	 * 
	 * @param resultSet the ResultSet containing the row data
	 * @param startingFrom index specifying where to start fetching attributes from;
	 *   useful if the row contains attributes from multiple tables
	 */
	public int fetchValues(ResultSet resultSet, int startingFrom) throws SQLException {
		setSubmissionPK(resultSet.getString(startingFrom++));
		setStudentRegistrationPK(resultSet.getString(startingFrom++));
		setProjectPK(resultSet.getString(startingFrom++));
		setNumTestRuns(resultSet.getInt(startingFrom++));
		setCurrentTestRunPK(resultSet.getString(startingFrom++));
		setSubmissionNumber(resultSet.getString(startingFrom++));
		setSubmissionTimestamp(resultSet.getTimestamp(startingFrom++));
		setCvsTagTimestamp(resultSet.getString(startingFrom++));
		setBuildRequestTimestamp(resultSet.getTimestamp(startingFrom++));
		setBuildStatus(resultSet.getString(startingFrom++));
		setSubmitClient(resultSet.getString(startingFrom++));
		setReleaseRequest(resultSet.getTimestamp(startingFrom++));
		setReleaseEligible(resultSet.getBoolean(startingFrom++));
		setValuePassedOverall(resultSet.getInt(startingFrom++));
        setCompileSuccessful(resultSet.getInt(startingFrom++) > 0);
        setValuePublicTestsPassed(resultSet.getInt(startingFrom++));
        setValueReleaseTestsPassed(resultSet.getInt(startingFrom++));
        setValueSecretTestsPassed(resultSet.getInt(startingFrom++));
        setNumFindBugsWarnings(resultSet.getInt(startingFrom++));
        setArchivePK(resultSet.getString(startingFrom++));
		return startingFrom;
	}
	
	public static String getStudentRegistrationPKFromResultSet(ResultSet resultSet) throws SQLException {
		return resultSet.getString(2);
	}
	
	static int getExtensionFromResultSet(ResultSet rs)
	throws SQLException
	{
	    return rs.getInt("student_submit_status.extension");
	}
	
	/**
	 * If a submission with 
	 * @param conn
	 * @throws SQLException
	 */
	public void insert(Connection conn)
	throws SQLException
	{
	    String insert = Queries.makeInsertStatement(ATTRIBUTE_NAME_LIST.length, ATTRIBUTES, TABLE_NAME);
        if (!hasCachedArchive())
            throw new IllegalStateException("there is no archive for upload, you should call setArchiveForUpload first");

	    PreparedStatement stmt = null;
	    try {
	         
	        // insert the bytes we have as a new archive in that table	    
	        stmt = conn.prepareStatement(insert);
	        
	        setArchivePK(uploadCachedArchive(conn));
	        
	        putValues(stmt, 1);
	        
	        stmt.executeUpdate();

	        // set PK to the value of the last autoincrement for this connection
            // this will be the PK used for the inset we just performed
	        setSubmissionPK(Queries.lastInsertId(conn));
	    } finally {
	        Queries.closeStatement(stmt);
	    }
	}
	
	public void update(Connection conn)
	throws SQLException
	{
	    if (getSubmissionPK() == null)
	        throw new IllegalStateException("You cannot try to update a submission with a null submissionPK");
	    
	    String update = Queries.makeUpdateStatementWithWhereClause(ATTRIBUTE_NAME_LIST, TABLE_NAME, " WHERE submission_pk = ? ");

	    PreparedStatement stmt = conn.prepareStatement(update);
	    
	    int index = putValues(stmt, 1);
	    stmt.setString(index, getSubmissionPK());
	    
	    stmt.executeUpdate();
	    try {
	        stmt.close();
	    } catch (SQLException ignore) {
	        // ignore
	    }
	}

	/**
	 * Populates a prepared statement with all of the fields in this class starting at
	 * a given index.  Will return the index of the next open slot in the statement.
	 * @param stmt the PreparedStatement to populate
	 * @param index the index of the leaf of the insert graph to start at
	 * @throws SQLException
	 */
	int putValues(PreparedStatement stmt, int index)
	throws SQLException
	{
	    stmt.setString(index++, getStudentRegistrationPK());
	    stmt.setString(index++, getProjectPK());
	    stmt.setInt(index++, getNumTestRuns());
	    stmt.setString(index++, getCurrentTestRunPK());
	    stmt.setString(index++, getSubmissionNumber());
	    stmt.setTimestamp(index++, getSubmissionTimestamp());
	    stmt.setString(index++, getCvsTagTimestamp());
	    stmt.setTimestamp(index++, getBuildRequestTimestamp());
	    stmt.setString(index++, getBuildStatus());
	    stmt.setString(index++, getSubmitClient());
	    stmt.setTimestamp(index++, getReleaseRequest());
	    stmt.setBoolean(index++, isReleaseEligible());
	    stmt.setInt(index++, getValuePassedOverall());
        stmt.setInt(index++, isCompileSuccessful() ? 1 : 0);
        stmt.setInt(index++, getValuePublicTestsPassed());
        stmt.setInt(index++, getValueReleaseTestsPassed());
        stmt.setInt(index++, getValueSecretTestsPassed());
        stmt.setInt(index++, getNumFindBugsWarnings());
	    stmt.setString(index++, getArchivePK());
	    
	    return index;
	}

	/**
	 * Finds a submission based on the submissionPK
	 * 
	 * @param submissionPK the primary key of the submission
	 * @param conn the database connection to use
	 * @return the Submission object (that represents the row) if a row with submissionPK
	 * exists, null if it doesn't exist 
	 * 
	 * @throws SQLException
	 */
	public static Submission lookupByStudentPKAndSubmissionPK(
			String studentPK,
			String submissionPK,
			Connection conn)
		throws SQLException
	{
		String query = " SELECT " +ATTRIBUTES+ " "+
		" FROM "+
		" submissions, students, student_registration "+
		" WHERE submissions.submission_pk= ? " +
		" AND submissions.submission_timestamp IS NOT NULL " +
		" AND students.student_pk = ? " +
		" AND submissions.student_registration_pk = student_registration.student_registration_pk " +
		" AND students.student_pk = student_registration.student_pk ";
		
		PreparedStatement stmt = null;
		
		stmt = conn.prepareStatement(query);
		stmt.setString(1, submissionPK);
		stmt.setString(2, studentPK);
		
		return getFromPreparedStatement(stmt);
	}
	
	/**
	 * Retrieves a submission from a database by its submissionPK.
	 * @param submissionPK
	 * @param conn
	 * @return the submission; null if no submission exists with the given submissionPK
	 * @throws SQLException
	 */
	public static Submission lookupBySubmissionPK(
			String submissionPK,
			Connection conn)
		throws SQLException
	{
		// FIXME: Should submission_timestamp != null be enforced here?
	    String query = " SELECT " +ATTRIBUTES+ " "+
		" FROM "+
		" submissions "+
		" WHERE submissions.submission_pk= ? ";
		
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
	private static Submission getFromPreparedStatement(PreparedStatement stmt)
	throws SQLException
	{
	    try {
	        ResultSet rs = stmt.executeQuery();
	        
	        if (rs.first())
	        {
	            Submission submission = new Submission();
	            submission.fetchValues(rs, 1);
	            return submission;
	        }
	        return null;
	    }
	    finally {
	        Queries.closeStatement(stmt);
	    }
	}
    
    /**
     * Gets a submission from a prepared statement, but doesn't close the statement.
     * Eventually this method should replace all calls to getFromPreparedStatement,
     * which can possibly leak statements.
     * @param stmt
     * @return
     * @throws SQLException
     */
    private static Submission getFromPreparedStatementDontClose(PreparedStatement stmt)
    throws SQLException
    {
        ResultSet rs = stmt.executeQuery();
        
        if (rs.first())
        {
            Submission submission = new Submission();
            submission.fetchValues(rs, 1);
            return submission;
        }
        return null;
    }
	
    /**
     * @return Returns the archivePK.
     */
    public String getArchivePK()
    {
        return archivePK;
    }
    /**
     * @param archivePK The archivePK to set.
     */
    public void setArchivePK(String archivePK)
    {
        this.archivePK = archivePK;
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
    
    /**
     * Finds the last submission.
     * @param studentRegistrationPK
     * @param projectPK
     * @param conn
     * @return the last very-late submission; null if there were no very late submissions
     */
    static public Map<String, Submission> lookupLastSubmissionBeforeTimestampMap(
            Project project,
            Timestamp when,
            Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +ATTRIBUTES+ 
            " FROM submissions " +
            " WHERE submissions.project_pk = ? " +
            " AND submissions.submission_timestamp < ? " +
            " ORDER BY submission_timestamp desc ";
    
        PreparedStatement stmt = null;
        try {
        	stmt = conn.prepareStatement(query);
        	stmt.setString(1, project.getProjectPK());
           stmt.setTimestamp(2, when);
        
        	return getLastSubmissionMapFromStmt(project, stmt);
        } finally {
        	Queries.closeStatement(stmt);
        }
    }

    
    
    

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object arg0) {
        Submission that = (Submission) arg0;
        return getValuePassedOverall() - that.getValuePassedOverall();
    }

    /**
     * Looks up a submission whose currentTestRunPK is equal to the given testRunPK.
     * @param testRunPK the testRunPK
     * @param conn the connection to the database
     * @return the submission; null if no such submission exists
     */
    public static Submission lookupByTestRunPK(String testRunPK, Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +ATTRIBUTES+
            " FROM submissions " +
            " WHERE submissions.current_test_run_pk = ? ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, testRunPK);
        return getFromPreparedStatement(stmt);
    }

    public static List<Submission> lookupAllByStudentPKAndProjectPK(
            String studentPK,
    		String projectPK,
    		Connection conn)
    throws SQLException
    {
    	String query = "SELECT " +ATTRIBUTES+ " "+
    	" FROM " +
    	" submissions, student_registration " +
    	" WHERE student_registration.student_pk = ? " +
    	" AND submission_timestamp IS NOT NULL " +
    	" AND student_registration.student_registration_pk = submissions.student_registration_pk " +
    	" AND submissions.project_pk = ? " +
    	" ORDER BY submissions.submission_timestamp ASC ";
    	
    	PreparedStatement stmt = conn.prepareStatement(query);
    	
    	stmt.setString(1, studentPK);
    	stmt.setString(2, projectPK);
    	
    	return getListFromPreparedStatement(stmt);
    }
    
    public static List<Submission> lookupAllReleaseTestedStudentSubmissionsByProjectPK(
        String projectPK,
        Connection conn)
    throws SQLException
    {
        String query = "SELECT " +ATTRIBUTES+ " "+
        " FROM " +
        " submissions, student_registration " +
        " WHERE submission_timestamp IS NOT NULL " +
        " AND submissions.project_pk = ? " +
        " AND submissions.release_request IS NOT NULL " +
        " AND submissions.student_registration_pk = student_registration.student_registration_pk " +
        " AND student_registration.instructor_capability IS NULL ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        
        stmt.setString(1, projectPK);
        
        return getListFromPreparedStatement(stmt);
    }
    
    public static List<Submission> lookupAllStudentSnapshotsByProjectPK(
    String projectPK,
    Connection conn)
    throws SQLException
    {
        String query = "SELECT " +ATTRIBUTES+ " "+
        " FROM " +
        " submissions, student_registration, projects " +
        " WHERE submissions.project_pk = ? " +
        " AND submissions.student_registration_pk = student_registration.student_registration_pk " +
        " AND student_registration.instructor_capability IS NULL " +
        " AND submissions.project_pk = projects.project_pk " +
        " AND projects.initial_build_status = 'new'";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        
        stmt.setString(1, projectPK);
        
        return getListFromPreparedStatement(stmt);
    }
    
    public static List<Submission> lookupAllStudentSubmissionsByProjectPK(
    String projectPK,
    Connection conn)
    throws SQLException
    {
        String query = "SELECT " +ATTRIBUTES+ " "+
        " FROM " +
        " submissions, student_registration, projects " +
        " WHERE submission_timestamp IS NOT NULL " +
        " AND submissions.project_pk = ? " +
        " AND submissions.student_registration_pk = student_registration.student_registration_pk " +
        " AND student_registration.instructor_capability IS NULL " +
        " AND submissions.project_pk = projects.project_pk " +
        " AND projects.initial_build_status = 'new'";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        
        stmt.setString(1, projectPK);
        
        return getListFromPreparedStatement(stmt);
    }
    
    public static List<Submission> lookupAllByProjectPK(
    		String projectPK,
    		Connection conn)
    throws SQLException
    {
    	String query = "SELECT " +ATTRIBUTES+ " "+
    	" FROM " +
    	" submissions " +
    	" WHERE submission_timestamp IS NOT NULL " +
    	" AND submissions.project_pk = ? ";
    	
    	PreparedStatement stmt = conn.prepareStatement(query);
    	
    	stmt.setString(1, projectPK);
    	
    	return getListFromPreparedStatement(stmt);
    }

    public static List<Submission> lookupAllForReleaseTesting(
    		String studentPK,
    		String projectPK,
    		Connection conn)
    throws SQLException
    {
    	String query = " SELECT " +ATTRIBUTES+ " "+
    	" FROM " +
    	" submissions, student_registration " +
    	" WHERE student_registration.student_pk = ? " +
    	" AND submission_timestamp IS NOT NULL " +
    	" AND student_registration.student_registration_pk = submissions.student_registration_pk " +
    	" AND submissions.project_pk = ? "+
    	" AND submissions.release_request IS NOT NULL " +
    	" ORDER BY submissions.release_request DESC";
    			
    	PreparedStatement stmt = conn.prepareStatement(query);
    
    	stmt.setString(1, studentPK);
    	stmt.setString(2, projectPK);
    	
    	return getListFromPreparedStatement(stmt);
    }

    private static List<Submission> getListFromPreparedStatement(PreparedStatement stmt)
    throws SQLException
    {
        try {
    		ResultSet rs = stmt.executeQuery();
    		
    		List<Submission> submissions = new ArrayList<Submission>();
    		
    		while (rs.next())
    		{
    			Submission submission = new Submission();
    			submission.fetchValues(rs, 1);
    			submissions.add(submission);
    		}
    		return submissions;
    	} finally {
    		Queries.closeStatement(stmt);
    	}
    }
    
    private static void deleteFindBugsForTestRunPK(String testRunPK, Connection conn)
    throws SQLException
    {
    	String query=
    		" DELETE FROM test_outcomes " +
    		" WHERE test_run_pk = ? " +
    		" AND test_type = 'findbugs' ";
    	PreparedStatement stmt=null;
    	try {
    		stmt = conn.prepareStatement(query);
    		stmt.setString(1, testRunPK);
    		stmt.execute();
    	} finally {
    		Queries.closeStatement(stmt);
    	}
    }
    
    public static void replaceFindBugsOutcomes(
    		TestOutcomeCollection newBugWarnings,
    		String submissionPK,
    		Connection conn)
    throws SQLException
    {
    	conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        boolean transactionSuccess=false;
        try {
            Submission submission = lookupBySubmissionPK(submissionPK, conn);
            if (submission == null)
                throw new SQLException("Cannot find submissionPK = " +submissionPK);
            if (!submission.isCompileSuccessful())
                throw new IllegalStateException("SubmissionPK = " +submissionPK+ " did not compile!");

            TestRun testRun = TestRun.lookupByTestRunPK(
            		submission.getCurrentTestRunPK(),
            		conn);
            
            // Delete the old findbugs warnings.
            deleteFindBugsForTestRunPK(testRun.getTestRunPK(), conn);
            
            // Set the testRunPKs so that these warnings to the correct place.
            for (TestOutcome warning : newBugWarnings.getFindBugsOutcomes()) {
            	warning.setTestRunPK(testRun.getTestRunPK());
            }
            // Insert the new findbugs warnings.
            newBugWarnings.insert(conn);
        } finally {
        	try {
        		if (!transactionSuccess)
        			conn.rollback();
        	} catch (SQLException ignore) {
        		// ignore
        	}
        }
    }
    
    /**
     * Loads a new set of findbugs outcomes.  The algorithm is as follows:<br>
     * <ul>
     * <li> create a new outcomeCollection is a copy of the existing 
     * outcomeCollection minus the FindBugs outcomes
     * <li> insert the new FindBugs outcomes into the new outcomeCollection
     * <li> create and insert into the database a new testRun that is the copy 
     * of the exiting testRun but with an updated FindBugs count
     * <li> set the new outcomeCollection's testRunPK to match the new testRun record just inserted
     * <li> insert the new outcomeCollection into the DB
     * <li> update the submission's currentTestRunPK
     * <li> update the submission's numTestOutcomes
     * <li> update the submission's numFindBugsWarnings
     * </ul>
     * @param newFindBugsCollection the collection of new FindBugs warnings
     * @param submissionPK the submissionPK of the submission to be updated
     * @param conn the connection to the database
     * @throws SQLException
     */
    public static void loadNewFindBugsOutcomes(
            TestOutcomeCollection newFindBugsCollection,
            String submissionPK,
            Connection conn)
    throws SQLException
    {
        // set the lowest transaction level
        conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        boolean transactionSuccess=false;
        try {
            Submission submission = lookupBySubmissionPK(submissionPK, conn);
            if (submission == null)
                throw new SQLException("Cannot find submissionPK = " +submissionPK);
            if (!submission.isCompileSuccessful())
                throw new IllegalStateException("SubmissionPK = " +submissionPK+ " did not compile!");

            TestRun testRun = TestRun.lookupByTestRunPK(submission.getCurrentTestRunPK(), conn);
            TestOutcomeCollection testOutcomeCollection = TestOutcomeCollection.lookupByTestRunPK(
                    testRun.getTestRunPK(),
                    conn);
            
            // create a new collection of testOutcomes and copy over everything but the findbugs outcomes
            TestOutcomeCollection newCollection = new TestOutcomeCollection();
            for (Iterator<TestOutcome> ii=testOutcomeCollection.iterator(); ii.hasNext();)
            {
                TestOutcome outcome = ii.next();
                if (!outcome.getTestType().equals(TestOutcome.FINDBUGS_TEST)) {
                    newCollection.add(outcome);
                }
            }
            
            // insert the new findbugs outcomes
            for (Iterator<TestOutcome> ii=newFindBugsCollection.iterator(); ii.hasNext();)
            {
                newCollection.add(ii.next());
            }
            
            // clone the testRun object, set the fields that have changed
            TestRun newTestRun = (TestRun)testRun.clone();
            newTestRun.setTestRunPK(null);
            newTestRun.setNumFindBugsWarnings(newFindBugsCollection.size());
            newTestRun.setTestTimestamp(new Timestamp(System.currentTimeMillis()));
            
            //System.err.println("old testRunPK: " +testRun.getTestRunPK());

            // insert the new testRun row
            newTestRun.insert(conn);
            
            //System.err.println("new testRunPK: " +newTestRun.getTestRunPK());
            
            // update the current test run row for this submission
            newCollection.updateTestRunPK(newTestRun.getTestRunPK());
            newCollection.insert(conn);
            
            // set the currentTestRunPK for this submission to the newly created testRun record
            // and increment the number of test runs
            // and set the new number of findbugs warnings
            submission.setCurrentTestRunPK(newTestRun.getTestRunPK());
            submission.setNumTestRuns(submission.getNumTestRuns() + 1);
            submission.setNumFindBugsWarnings(newFindBugsCollection.size());
            submission.update(conn);
            
            conn.commit();
            transactionSuccess=true;
            
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (!transactionSuccess)
                    conn.rollback();
            } catch (SQLException ignore) {
                // ignore
            }
        }
    }
    
    public static void lookupAllWithFailedBackgroundRetestsByProjectPK(
        String projectPK,
        List<Submission> submissionList,
        Map<String,BackgroundRetest> backgroundRetestMap,
        Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +ATTRIBUTES+ ", "+ BackgroundRetest.ATTRIBUTES+
            " FROM submissions, projects, background_retests " +
            " WHERE submissions.project_pk = projects.project_pk " +
            " AND projects.project_pk = ? " +
            " AND submissions.submission_pk = background_retests.submission_pk " +
            " AND projects.project_jarfile_pk = background_retests.project_jarfile_pk "+
            " AND num_failed_background_retests > 0 ";
        PreparedStatement stmt=null;
        try {
            stmt=conn.prepareStatement(query);
            stmt.setString(1,projectPK);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Submission submission = new Submission();
                int index = submission.fetchValues(rs, 1);
                submissionList.add(submission);
                BackgroundRetest backgroundRetest = new BackgroundRetest();
                backgroundRetest.fetchValues(rs, index);
                backgroundRetestMap.put(submission.getSubmissionPK(), backgroundRetest);
            }
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    /**
     * Adjusts the scores of this submission based on the results of background retests.
     * If any of the background retests have failed, then the entire test case is
     * marked as failed. 
     * @param conn Connection to the database.
     * @return The adjusted TestOutcomeCollection.
     * @throws SQLException
     */
    public TestOutcomeCollection setAdjustScoreBasedOnFailedBackgroundRetests(Connection conn)
    throws SQLException
    {
        Project project=Project.lookupByProjectPK(getProjectPK(),conn);
        TestRun currentTestRun=TestRun.lookupByTestRunPK(getCurrentTestRunPK(),conn);
        List<TestOutcomeCollection> allTestOutcomeCollections =
            TestOutcomeCollection.lookupAllBySubmissionPKAndProjectJarfilePK(
                getSubmissionPK(),
                currentTestRun.getProjectJarfilePK(),
                conn);
        TestOutcomeCollection bestCollection=allTestOutcomeCollections.get(0);
        // If we have more than 1 test run for this submission...
        // Set the scores for the "best" collection of test outcomes to the
        // minimum achieved in any collection of test outcomes.
        if (allTestOutcomeCollections.size() > 1) {
            for (int ii=1; ii < allTestOutcomeCollections.size(); ii++) {
                TestOutcomeCollection collectionToCompare=allTestOutcomeCollections.get(ii);
                for (TestOutcome bestOutcome : bestCollection.getIterableForCardinalTestTypes()) {
                    if (bestOutcome.isPassed()) {
                        TestOutcome outcomeToCompare=collectionToCompare.getOutcomeByTestTypeAndTestNumber(
                            bestOutcome.getTestType(),
                            Integer.toString(bestOutcome.getTestNumber()));
                        if (outcomeToCompare.isFailed()) {
                            bestOutcome.setOutcome(outcomeToCompare.getOutcome());
                        }
                    }
                }
            }
        }
        // Adjust all the scores of this submission.
        setValuePublicTestsPassed(bestCollection.getValuePublicTestsPassed());
        setValueReleaseTestsPassed(bestCollection.getValueReleaseTestsPassed());
        setValueSecretTestsPassed(bestCollection.getValueSecretTestsPassed());
        setValuePassedOverall(bestCollection.getValuePassedOverall());
        // Set the adjusted score, including any late penalties.
        setStatus(project);
        setAdjustedScore(project);
        return bestCollection;
    }

    public static List<Submission> lookupAllByStudentRegistrationPKAndProjectPK(
        String canonicalStudentRegistrationPK,
        String projectPK,
        Connection conn)
    throws SQLException       
    {
        String query=
            " SELECT " +ATTRIBUTES+
            " FROM submissions " +
            " WHERE student_registration_pk = ? " +
            " AND project_pk = ? " +
            " ORDER BY submission_number ";
        
        PreparedStatement stmt=conn.prepareStatement(query);
        stmt.setString(1, canonicalStudentRegistrationPK);
        stmt.setString(2, projectPK);
        return getListFromPreparedStatement(stmt);
    }
    
    public static Submission submitOneSubmission(
        byte[] bytesForUpload,
        String cvsTagTimestamp,
        String cvsAccount,
        String projectNumber,
        String courseName,
        String semester,
        Connection conn)
    throws SQLException
    {
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
                throw new SQLException(cvsAccount +" is not registered for " +courseName+" in " +semester);
            }
            
//          ensure that this snapshot has not already been uploaded
            Submission previouslyUploaded = Submission.lookupByCvsTagTimestamp(
                cvsTagTimestamp,
                studentRegistration.getStudentRegistrationPK(),
                project.getProjectPK(),
                conn);
            
            if (previouslyUploaded != null) {
                // FIXME throw an exception instead of returning null
               throw new SQLException("Already submitted, cvsTagTimestamp: " +cvsTagTimestamp+
                        ", studentRegistrationPK: "
                        +studentRegistration.getStudentRegistrationPK()+
                        ", projectPK: " +project.getProjectPK());
            }
            
//          find StudentSubmitStatus record
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
            
            int submitNumber = -1;
            submitNumber = studentSubmitStatus.getNumberSubmissions() + 1;
            studentSubmitStatus.setNumberSubmissions(submitNumber);
            studentSubmitStatus.update(conn);
            
            // prepare new snapshot record
            Submission submission=new Submission();
            submission.setStudentRegistrationPK(studentRegistration.getStudentRegistrationPK());
            
            submission.setProjectPK(project.getProjectPK());
            
            // Set the cvsTagTimestamp
            submission.setCvsTagTimestamp(cvsTagTimestamp);
            
            submission.setCvsTagTimestamp(cvsTagTimestamp);
            submission.setSubmissionNumber(new Integer(submitNumber).toString());
            // Figure out the submissionTimestamp
            // Note that the cvsTagTimestamp will be:
            // tXXXXXXXXXXXXXXXXX (basically "t" prepended to a call to System.currentTimeMillis()
            long submissionTimesetamp=Long.parseLong(cvsTagTimestamp.substring(1));
            submission.setSubmissionTimestamp(new Timestamp(submissionTimesetamp));
            
            submission.setBuildStatus(project.getInitialBuildStatus());
            // This is DirectSubmissionUpload
            submission.setSubmitClient("directSubmissionUpload");
            
            // set the byte array as the archive
            submission.setArchiveForUpload(bytesForUpload);
            
            submission.insert(conn);
            conn.commit();
            transactionSuccess = true;
            return submission;
        } finally {
            Queries.rollbackIfUnsuccessful(transactionSuccess, conn);
        }
    }
        
    
    private static Submission lookupByCvsTagTimestamp(String cvsTagTimestamp,
        String studentRegistrationPK,
        String projectPK,
        Connection conn)
    throws SQLException
    {
        String sql =
            " SELECT " +ATTRIBUTES+
            " FROM submissions " +
            " WHERE cvstag_timestamp = ? " +
            " AND student_registration_pk = ? " +
            " AND project_pk = ? ";
        PreparedStatement stmt=null;
        try {
            stmt=conn.prepareStatement(sql);
            stmt.setString(1, cvsTagTimestamp);
            stmt.setString(2, studentRegistrationPK);
            stmt.setString(3, projectPK);
            return getFromPreparedStatementDontClose(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    /**
     * Submit a new submission to the database using a byte array containing the zip archive
     * of the submission and other parameters describing the submission.
     * @param bytesForUpload A byte array containing the zip archive of the submission.
     * @param studentRegistration The student registration making the submission.
     * @param project The project this submission is for.
     * @param cvstagTimestamp The cvstag timestamp.
     * @param submitClientTool The client tool used for the submission (could be Eclipse, web,
     *      ProjectImportTool, DirectSnapshotUploadTool, etc)
     * @param submitClientVersion The version of the client upload tool.
     * @param submissionTimestamp The timestamp when the submission happened.  This is passed 
     *      in as a parameter because some of the submissionClientTools will date submissions
     *      differently, for example, the DirectSnapshotUpload tool dumps CVS snapshots
     *      and therefore uses the dates of when the snapshots were recorded. 
     * @param conn Connection to the database.
     * @return The submission object that's been uploaded into the database.
     * @throws SQLException If something goes wrong communicating with the database.
     */
    public static Submission submit(
        byte[] bytesForUpload,
        StudentRegistration studentRegistration,
        Project project,
        String cvstagTimestamp,
        String submitClientTool,
        String submitClientVersion,
        Timestamp submissionTimestamp,
        Connection conn)
    throws SQLException
    {
        // find StudentSubmitStatus record
        StudentSubmitStatus studentSubmitStatus = StudentSubmitStatus
                .lookupByStudentRegistrationPKAndProjectPK(
                        studentRegistration.getStudentRegistrationPK(),
                        project.getProjectPK(), conn);

        // if the submitStatus record doesn't exist, then create it
        if (studentSubmitStatus == null) {
            studentSubmitStatus = StudentSubmitStatus.createAndInsert(
                    project.getProjectPK(),
                    studentRegistration.getStudentRegistrationPK(),
                    conn);
        }

        if (project.getCanonicalStudentRegistrationPK().equals(
                studentRegistration.getStudentRegistrationPK()))
            ProjectJarfile.resetAllFailedTestSetups(project.getProjectPK(),
                    conn);
        int submissionNumber = studentSubmitStatus.getNumberSubmissions() + 1;
        // figure out how many submissions have already been made
        studentSubmitStatus.setNumberSubmissions(submissionNumber);
        studentSubmitStatus.update(conn);

        // prepare new submission record
        Submission submission = new Submission();
        submission.setStudentRegistrationPK(studentRegistration
                .getStudentRegistrationPK());
        submission.setProjectPK(project.getProjectPK());
        //submission.setNumTestOutcomes(0);
        submission.setSubmissionNumber(Integer.toString(submissionNumber));
        submission.setSubmissionTimestamp(submissionTimestamp);
        // OK if this is null
        submission.setCvsTagTimestamp(cvstagTimestamp);
        submission.setBuildStatus(project.getInitialBuildStatus());
        // figure out the type and version of the submit client
        if (submitClientVersion != null)
            submitClientTool += "-" + submitClientVersion;
        submission.setSubmitClient(submitClientTool);

        // set the byte array as the archive
        submission.setArchiveForUpload(bytesForUpload);

        submission.insert(conn);
        return submission;
    }
}
