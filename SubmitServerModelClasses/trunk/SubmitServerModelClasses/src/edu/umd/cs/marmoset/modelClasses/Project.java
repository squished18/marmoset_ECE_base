/*
 * Created on Aug 30, 2004
 */
package edu.umd.cs.marmoset.modelClasses;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.CopyUtils;

/**
 * Object to represent a row in the projects table.
 * @author daveho
 * @author jspacco
 */
public class Project implements Serializable {
	private static final String PROJECT_STARTER_FILE_ARCHIVES = "project_starter_file_archives";
    private String projectPK;
	private String coursePK;
	private String projectJarfilePK="0";
	private String projectNumber;
	private Timestamp ontime;
	private Timestamp late;
	private String title;
	private String url;
	private String description;
	private String releaseTokens;
	private String regenerationTime;
	private String initialBuildStatus;
	private boolean visibleToStudents;
	private String postDeadlineOutcomeVisibility=POST_DEADLINE_OUTCOME_VISIBILITY_NOTHING;
	private String kindOfLatePenalty;
	private double lateMultiplier;
	private int lateConstant;
	private String canonicalStudentRegistrationPK;
    private String bestSubmissionPolicy;
    private String releasePolicy;
    private String stackTracePolicy;
    private int numReleaseTestsRevealed;
    private String archivePK;
    
    private transient byte[] cachedArchive;
    
    private static final long serialVersionUID = 1;
    private static final int serialMinorVersion = 1;
	
	public static final String ACCEPTED = "accepted";
	public static final String NEW = "new";
	public static final String CONSTANT = "constant";
	public static final String MULTIPLIER = "multiplier";
	
	public static final String JAVA = "java";
	public static final String OTHER = "other";
	
	public static final String POST_DEADLINE_OUTCOME_VISIBILITY_NOTHING="nothing";
	public static final String POST_DEADLINE_OUTCOME_VISIBILITY_EVERYTHING="everything";
    
    public static final String AFTER_PUBLIC="after_public";
    public static final String ANYTIME="anytime";
    public static final String TEST_NAME_ONLY="test_name_only";
    public static final String EXCEPTION_LOCATION="exception_location";
    public static final String RESTRICTED_EXCEPTION_LOCATION="restricted_exception_location";
    public static final String FULL_STACK_TRACE="full_stack_trace";
    
    public static final int UNLIMITED_RELEASE_TESTS=-1;
	
	/**
	 * List of all attributes of projects table.
	 */
	 final static String[] ATTRIBUTE_NAME_LIST = {
		"project_pk",
		"course_pk",
		"project_jarfile_pk",
		"project_number",
		"ontime",
		"late",
		"title",
		"URL",
		"description",
		"release_tokens",
		"regeneration_time",
		"initial_build_status",
		"visible_to_students",
		"post_deadline_outcome_visibility",
		"kind_of_late_penalty",
		"late_multiplier",
		"late_constant",
		"canonical_student_registration_pk",
        "best_submission_policy",
        "release_policy",
        "stack_trace_policy",
        "num_release_tests_revealed",
        "archive_pk"
	};
	
	/**
	 * Fully-qualified attributes for projects table.
	 */
	 public static final String ATTRIBUTES =
		Queries.getAttributeList("projects", ATTRIBUTE_NAME_LIST);
	
	/**
	 * Constructor.  All fields will have default values.
	 */
	public Project() {
	}

	/**
	 * Is this project configured for testing?
	 * 
	 * @return true if the project is configured for testing, false otherwise
	 */
	public boolean isTestingRequired()
	{
	    return initialBuildStatus.equals(NEW);
	}
	
	/**
	 * @return Returns the regenerationTime.
	 */
	public String getRegenerationTime() {
		return regenerationTime;
	}
	/**
	 * @param regenerationTime The regenerationTime to set.
	 */
	public void setRegenerationTime(String regenerationTime) {
		this.regenerationTime = regenerationTime;
	}
	/**
	 * @return Returns the tokens.
	 */
	public String getReleaseTokens() {
		return releaseTokens;
	}
	/**
	 * @param tokens The tokens to set.
	 */
	public void setReleaseTokens(String tokens) {
		this.releaseTokens = tokens;
	}
	/**
	 * @return Returns the coursePK.
	 */
	public String getCoursePK() {
		return coursePK;
	}
	/**
	 * @param coursePK The coursePK to set.
	 */
	public void setCoursePK(String coursePK) {
		this.coursePK = coursePK;
	}
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return Returns the late.
	 */
	public Timestamp getLate() {
		return late;
	}
	/**
	 * @return The late deadline in utc millis.
	 */
	public long getLateMillis() {
		return late.getTime();
	}
	/**
	 * @param late The late to set.
	 */
	public void setLate(Timestamp late) {
		this.late = late;
	}
	/**
	 * @return Returns the projectNumber.
	 */
	public String getProjectNumber() {
		return projectNumber;
	}
	/**
	 * @param projectNumber The projectNumber to set.
	 */
	public void setProjectNumber(String projectNumber) {
		this.projectNumber = projectNumber;
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
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url The url to set.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return Returns the initialBuildStatus.
	 */
	public String getInitialBuildStatus() {
		return initialBuildStatus;
	}
	/**
	 * @param initialBuildStatus The initialBuildStatus to set.
	 */
	public void setInitialBuildStatus(String initialBuildStatus) {
		this.initialBuildStatus = initialBuildStatus;
	}
	
    /**
     * @return Returns the kindOfLatePenalty.
     */
    public String getKindOfLatePenalty() {
        return kindOfLatePenalty;
    }
    /**
     * @param kindOfLatePenalty The kindOfLatePenalty to set.
     */
    public void setKindOfLatePenalty(String kindOfLatePenalty) {
        this.kindOfLatePenalty = kindOfLatePenalty;
    }
    /**
     * @return Returns the lateConstant.
     */
    public int getLateConstant() {
        return lateConstant;
    }
    /**
     * @param lateConstant The lateConstant to set.
     */
    public void setLateConstant(int lateConstant) {
        this.lateConstant = lateConstant;
    }
    /**
     * @return Returns the lateMultiplier.
     */
    public double getLateMultiplier() {
        return lateMultiplier;
    }
    /**
     * @param lateMultiplier The lateMultiplier to set.
     */
    public void setLateMultiplier(double lateMultiplier) {
        this.lateMultiplier = lateMultiplier;
    }
    /**
     * @return Returns the visibleToStudents.
     */
    public boolean getVisibleToStudents() {
        return visibleToStudents;
    }
    /**
     * @param visibleToStudents The visibleToStudents to set.
     */
    public void setVisibleToStudents(boolean visibleToStudents) {
        this.visibleToStudents = visibleToStudents;
    }
    /**
	 * @return Returns the postMortemRevelationLevel.
	 */
	public String getPostDeadlineOutcomeVisibility() {
		return postDeadlineOutcomeVisibility;
	}
	/**
	 * @param postMortemRevelationLevel The postMortemRevelationLevel to set.
	 */
	public void setPostDeadlineOutcomeVisibility(String postMortemRevelationLevel) {
		this.postDeadlineOutcomeVisibility = postMortemRevelationLevel;
	}

	/**
     * @return Returns the ontime.
     */
    public Timestamp getOntime() {
        return ontime;
    }
    
    /**
     * @return The ontime deadline in utc millis.
     */
    public long getOntimeMillis() {
    	return ontime.getTime();
    }
    /**
     * @param ontime The ontime to set.
     */
    public void setOntime(Timestamp ontime) {
        this.ontime = ontime;
    }
    
    /**
     * @return Returns the canonicalStudentRegistrationPK.
     */
    public String getCanonicalStudentRegistrationPK()
    {
        return canonicalStudentRegistrationPK;
    }
    /**
     * @param canonicalStudentRegistrationPK The canonicalStudentRegistrationPK to set.
     */
    public void setCanonicalStudentRegistrationPK(
            String canonicalStudentRegistrationPK)
    {
        this.canonicalStudentRegistrationPK = canonicalStudentRegistrationPK;
    }
    /**
     * @return Returns the bestSubmissionPolicy.
     */
    public String getBestSubmissionPolicy() {
        return bestSubmissionPolicy;
    }
    /**
     * @param bestSubmissionPolicy The bestSubmissionPolicy to set.
     */
    public void setBestSubmissionPolicy(String bestSubmissionPolicy) {
        this.bestSubmissionPolicy = bestSubmissionPolicy;
    }
    /**
     * @return Returns the numReleaseTestsRevealed.
     */
    public int getNumReleaseTestsRevealed() {
        return numReleaseTestsRevealed;
    }
    /**
     * @param numReleaseTestsRevealed The numReleaseTestsRevealed to set.
     */
    public void setNumReleaseTestsRevealed(int numReleaseTestsRevealed) {
        this.numReleaseTestsRevealed = numReleaseTestsRevealed;
    }
    /**
     * @return Returns the releasePolicy.
     */
    public String getReleasePolicy() {
        return releasePolicy;
    }
    /**
     * @param releasePolicy The releasePolicy to set.
     */
    public void setReleasePolicy(String releasePolicy) {
        this.releasePolicy = releasePolicy;
    }
    /**
     * @return Returns the stackTracePolicy.
     */
    public String getStackTracePolicy() {
        return stackTracePolicy;
    }
    /**
     * @param stackTracePolicy The stackTracePolicy to set.
     */
    public void setStackTracePolicy(String stackTracePolicy) {
        this.stackTracePolicy = stackTracePolicy;
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

    public String checkOnTime(Timestamp ts)
    {
        // TODO what about if it's exactly at the on-time date?
        if (!ts.after(getOntime()))
            return "on-time";
        if (ts.after(getOntime()) && !ts.after(late))
            return "late";
        return "very late";
    }
	/**
	 * Populate a Submission from a ResultSet that is positioned
	 * at a row of the submissions table.
	 * 
	 * @param resultSet the ResultSet containing the row data
	 * @param startingFrom index specifying where to start fetching attributes from;
	 *   useful if the row contains attributes from multiple tables
	 */
	public void fetchValues(ResultSet resultSet, int startingFrom) throws SQLException {
		setProjectPK(resultSet.getString(startingFrom++));
		setCoursePK(resultSet.getString(startingFrom++));
		setProjectJarfilePK(resultSet.getString(startingFrom++));
		setProjectNumber(resultSet.getString(startingFrom++));
		setOntime(resultSet.getTimestamp(startingFrom++));
		setLate(resultSet.getTimestamp(startingFrom++));
		setTitle(resultSet.getString(startingFrom++));
		setUrl(resultSet.getString(startingFrom++));
		setDescription(resultSet.getString(startingFrom++));
		setReleaseTokens(resultSet.getString(startingFrom++));
		setRegenerationTime(resultSet.getString(startingFrom++));
		setInitialBuildStatus(resultSet.getString(startingFrom++));
		setVisibleToStudents(resultSet.getBoolean(startingFrom++));
		setPostDeadlineOutcomeVisibility(resultSet.getString(startingFrom++));
		setKindOfLatePenalty(resultSet.getString(startingFrom++));
		setLateMultiplier(resultSet.getDouble(startingFrom++));
		setLateConstant(resultSet.getInt(startingFrom++));
		setCanonicalStudentRegistrationPK(resultSet.getString(startingFrom++));
        setBestSubmissionPolicy(resultSet.getString(startingFrom++));
        setReleasePolicy(resultSet.getString(startingFrom++));
        setStackTracePolicy(resultSet.getString(startingFrom++));
        // Using -1 to represent infinity
        int num=resultSet.getInt(startingFrom++);
        if (num==-1)
            num=Integer.MAX_VALUE;
        setNumReleaseTestsRevealed(num);
        setArchivePK(resultSet.getString(startingFrom++));
	}

	public void insert(Connection conn)
	throws SQLException
	{
	    String insert = Queries.makeInsertStatement(
	            ATTRIBUTE_NAME_LIST.length,
	            ATTRIBUTES,
	            "projects");
	    	    
	    PreparedStatement stmt=null;
	    try {
	        stmt = conn.prepareStatement(insert);
	                
	        int index=1;
	        putValues(stmt, index);

	        stmt.executeUpdate();
	        
	        // set PK to the last autoincrement field set by this connection
	        setProjectPK(Queries.lastInsertId(conn));
	        
	    } finally {
	        Queries.closeStatement(stmt);
	    }
	}
	
	private int putValues(PreparedStatement stmt, int index)
	throws SQLException
	{
	    stmt.setString(index++, getCoursePK());
		stmt.setString(index++, getProjectJarfilePK());
		stmt.setString(index++, getProjectNumber());
		stmt.setTimestamp(index++, getOntime());
		stmt.setTimestamp(index++, getLate());
		stmt.setString(index++, getTitle());
		stmt.setString(index++, getUrl());
		stmt.setString(index++, getDescription());
		stmt.setString(index++, getReleaseTokens());
		stmt.setString(index++, getRegenerationTime());
		stmt.setString(index++, getInitialBuildStatus());
		stmt.setBoolean(index++, getVisibleToStudents());
		stmt.setString(index++, getPostDeadlineOutcomeVisibility());
		stmt.setString(index++, getKindOfLatePenalty());
		stmt.setDouble(index++, getLateMultiplier());
		stmt.setInt(index++, getLateConstant());
		stmt.setString(index++, getCanonicalStudentRegistrationPK());
        stmt.setString(index++, getBestSubmissionPolicy());
        stmt.setString(index++, getReleasePolicy());
        stmt.setString(index++, getStackTracePolicy());
        // Using -1 to represent infinity in the database
        if (getNumReleaseTestsRevealed()==Integer.MAX_VALUE)
            stmt.setInt(index++, -1);
        else
            stmt.setInt(index++, getNumReleaseTestsRevealed());
        stmt.setString(index++, getArchivePK());
		return index;
	}
    
	public void update(Connection conn)
	throws SQLException
	{
	    String whereClause = " WHERE project_pk = ? ";
	        
	    String update = Queries.makeUpdateStatementWithWhereClause( 
	            ATTRIBUTE_NAME_LIST,
	            "projects",
	            whereClause);
	    
	    PreparedStatement stmt = null;
	    try {
	        stmt = conn.prepareStatement(update);
	        int index=1;
	        index = putValues(stmt, index);
	        stmt.setString(index, getProjectPK());

	        stmt.executeUpdate();
	    } finally {
	        Queries.closeStatement(stmt);
	    }
	}
	
	/**
	 * Gets a project based on its projectPK.  This method looks for a project that should exist
	 * because it is referenced from someplace else within the database.  
	 * 
	 * @param projectPK the PK of the project
	 * @param conn the connection to the database.
	 * @return returns the project object.  Will never return null but rather throw an exception 
	 * if the project is not found. 
	 * @throws SQLException if the project is not found, throws an exception and also logs
	 * that the internal database state is corrupt.
	 */
	public static Project getByProjectPK(String projectPK, Connection conn)
	throws SQLException
	{
	    Project project = lookupByProjectPK(projectPK, conn);
	    if (project == null)
	    {
	        // TODO Log internal database state corruption
	        throw new SQLException("Internal Database problem, cannot find project with PK: " +projectPK);
	    }
	    return project;
	}
	
	/**
	 * Gets a project based on a submissionPK.  This method looks for a project referenced
	 * by a submission in our database.  If the project is not found, this represents an
	 * internal database integrity problem, and we throw an SQLException stating this.
	 * 
	 * @param submissionPK the submission PK
	 * @param conn the connection to the database
	 * @return the project object if it is found.  This method cannot return null; an exception 
	 * will be thrown if the project is not found.
	 * @throws SQLException
	 */
	public static Project getBySubmissionPK(String submissionPK, Connection conn)
	throws SQLException
	{
	    Project project = lookupBySubmissionPK(submissionPK, conn);
	    if (project == null)
	    {
	        // TODO Log internal database state corruption
	        throw new SQLException("Internal Database problem: Unable to find project referenced by submission with PK: " +submissionPK);
	    }
	    return project;
	}
	
	public static Project lookupBySubmissionPK(String submissionPK, Connection conn)
	throws SQLException
	{
	    String query = "SELECT " +ATTRIBUTES+ " " +
	    "FROM projects, submissions " +
	    "WHERE submissions.submission_pk = ? " +
	    "AND projects.project_pk = submissions.project_pk ";
	    
	    PreparedStatement stmt = conn.prepareStatement(query);
	    stmt.setString(1, submissionPK);
	    
	    return getFromPreparedStatement(stmt);
	}
	
	public static Project lookupByProjectPK(String projectPK, Connection conn)
	throws SQLException
	{
		String query = " SELECT " +ATTRIBUTES+ " "+
		" FROM " +
		" projects " +
		" WHERE projects.project_pk = ? ";
		
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, projectPK);

		return getFromPreparedStatement(stmt);
	}
	
	public static Project lookupByCourseProjectSemester(
        String courseName,
        String projectNumber,
        String semester,
        Connection conn)
	throws SQLException
	{
		String query = "SELECT " +ATTRIBUTES+
		" FROM "+
		" projects, courses "+
		" WHERE courses.coursename = ? "+
		" AND courses.semester = ? "+
		//"AND (courses.section IS NULL OR courses.section = ?) "+
		" AND courses.course_pk = projects.course_pk "+
		" AND projects.project_number = ? ";
		
		PreparedStatement stmt = null;
		
		stmt = conn.prepareStatement(query);
		stmt.setString(1, courseName);
		stmt.setString(2, semester);
		stmt.setString(3, projectNumber);
		//Debug.print("lookupProjectByCourseProjectSemesterSection()" + stmt.toString());
		
		return getFromPreparedStatement(stmt);
	}
	
	/**
	 * Helper method that uses a prepared statement to fetch a project from the DB.
	 * This method automatically closes the prepared statement.  Note that no code other
	 * than set...() methods should be called on the statement before it is passed to this
	 * method, or a resource leak could happen.
	 * 
	 * @param stmt the prepared statement to execute
	 * @return the project if it's found; null otherwise
	 * @throws SQLException
	 */
	private static Project getFromPreparedStatement(PreparedStatement stmt)
	throws SQLException
	{
		try {
			ResultSet rs = stmt.executeQuery();
			
			if (rs.next())
			{
				Project project = new Project();
				project.fetchValues(rs, 1);
				return project;
			}
			return null;
		} finally {
			Queries.closeStatement(stmt);
		}
	}
	
	public static void reTestProject(String projectPK, Connection conn)
	throws SQLException
	{
	    String update =
	        " UPDATE submissions " +
	        " SET build_status = ? " +
	        " WHERE submissions.project_pk = ? ";
	    
	    PreparedStatement stmt = null;
	    boolean transactionSuccess = false;
	    try {
	        stmt = conn.prepareStatement(update);
	        stmt.setString(1, Submission.RETEST);
	        stmt.setString(2, projectPK);
	        
	        conn.setAutoCommit(false);
	        conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
	        
	        stmt.executeUpdate();
	        
	        conn.commit();
	        transactionSuccess=true;
	    } finally {
	        if (!transactionSuccess) {
	            try {
	                Debug.error("Rolling back reTestProject()");
	                conn.rollback();
	            } catch (SQLException ignore) {
	                Debug.error("Failed to roll back reTestProject()");
	                // ignore
	            }
	        }
	        try {
	            if (stmt != null) stmt.close();
	        } catch (SQLException ignore) {
	            // ignore
	        }
	    }
	}

    /**
     * @return True if this submission collection is eligible for release or build/quick tests
     * false otherwise.
     */
    public static boolean isTestingRequired(String projectPK, Connection conn)
    throws SQLException
    {
        // We cannot rely on the build_status of the submissions because we might
        // have an empty set of submissions, in which case we won't know whether
        // testing was required or not.
        // So we have to fetch the project record from the database.
        Project project = getByProjectPK(projectPK, conn);
        return project.getInitialBuildStatus().equals(Submission.NEW);
    }
    /**
     * @return Returns the projectJarfilePK.
     */
    public String getProjectJarfilePK() {
        return projectJarfilePK;
    }
    /**
     * @param projectJarfilePK The projectJarfilePK to set.
     */
    public void setProjectJarfilePK(String projectJarfilePK) {
        this.projectJarfilePK = projectJarfilePK;
    }
    
    public String toString()
    {
        StringBuffer buf=new StringBuffer();
        buf.append("projectPK ="+projectPK+"\n");
        buf.append("coursePK ="+coursePK+"\n");
        buf.append("projectJarfilePK ="+projectJarfilePK+"\n");
        buf.append("projectNumber ="+projectNumber+"\n");
        buf.append("ontime ="+ontime+"\n");
        buf.append("late ="+late+"\n");
        buf.append("title ="+title+"\n");
        buf.append("url ="+url+"\n");
        buf.append("description ="+description+"\n");
        buf.append("releaseTokens ="+releaseTokens+"\n");
        buf.append("regenerationTime ="+regenerationTime+"\n");
        buf.append("initialBuildStatus ="+initialBuildStatus+"\n");
        buf.append("visibleToStudents ="+visibleToStudents+"\n");
        buf.append("postDeadlineOutcomeVisibility="+postDeadlineOutcomeVisibility+"\n");
        buf.append("kindOfLatePenalty ="+kindOfLatePenalty+"\n");
        buf.append("lateMultiplier ="+lateMultiplier+"\n");
        buf.append("lateConstant ="+lateConstant+"\n");
        buf.append("canonicalStudentRegistrationPK ="+canonicalStudentRegistrationPK+"\n");
        buf.append("bestSubmissionPolicy ="+bestSubmissionPolicy+"\n");
        buf.append("releasePolicy ="+releasePolicy+"\n");
        buf.append("stackTracePolicy ="+stackTracePolicy+"\n");
        buf.append("numReleaseTestsRevealed ="+numReleaseTestsRevealed+"\n");
        return buf.toString();
    }

    public Course getCorrespondingCourse(Connection conn)
    {
        try {
            Course course = Course.getByCoursePK(getCoursePK(), conn);
            if (course==null)
                throw new SQLException();
            return course;
        } catch (SQLException e) {
            throw new IllegalStateException("Internal database is corrupted!  I cannot " +
                    " find coursePK=" +getCoursePK()+
                    " that corresponds to projectPK=" +getProjectPK(), e);
        }
    }
/*
    public static List<Project> lookupAllByCoursePK(
        String coursePK,
        Connection conn)
    throws SQLException
    {
        String query=
            " SELECT " +ATTRIBUTES+
            " FROM projects " +
            " WHERE course_pk = ? " +
            " ORDER BY ontime ASC ";
        List<Project> projectList=new LinkedList<Project>();
        PreparedStatement stmt=null;
        try {
            stmt=conn.prepareStatement(query);
            stmt.setString(1, coursePK);
            ResultSet rs=stmt.executeQuery();
            while (rs.next()) {
                Project project=new Project();
                project.fetchValues(rs, 1);
                projectList.add(project);
            }
        } finally {
            Queries.closeStatement(stmt);
        }
        return projectList;
    }
*/    
    /**
     * Uploads the bytes of a cached archive to the database.
     * @param conn the connection to the database
     * @return the archivePK of the newly uploaded archive
     * @throws SQLException
     */
    public String uploadCachedArchive(Connection conn)
    throws SQLException
    {
        setArchivePK(Archive.uploadBytesToArchive(PROJECT_STARTER_FILE_ARCHIVES, cachedArchive, conn));
        return getArchivePK();
    }
    
    public void updateCachedArchive(byte[] bytes, Connection conn)
    throws SQLException
    {
        Archive.updateBytesInArchive(PROJECT_STARTER_FILE_ARCHIVES, archivePK, cachedArchive, conn);
    }
    /**
     * Does this project have an archive cached as bytes ready for upload to the database?
     * @return true if this project has a cached archive of starter files, false otherewise
     */
    public boolean getHasCachedArchive()
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
        return Archive.downloadBytesFromArchive(PROJECT_STARTER_FILE_ARCHIVES, getArchivePK(), conn);
    }

    public void exportProject(Connection conn, OutputStream out)
    throws SQLException, IOException
    {
        ProjectJarfile projectJarfile=ProjectJarfile.lookupByProjectJarfilePK(getProjectJarfilePK(),conn);
        if (projectJarfile==null) {
            throw new SQLException(getProjectNumber() +" does not have an active test-setup");
        }

        ZipOutputStream zipOutputStream=new ZipOutputStream(out);
        
        // Test-setup
        zipOutputStream.putNextEntry(new ZipEntry(getProjectNumber() +"-test-setup.zip"));
        zipOutputStream.write(projectJarfile.downloadArchive(conn));
        
        // Canonical
        Submission canonical=Submission.lookupBySubmissionPK(
            (TestRun.lookupByTestRunPK(projectJarfile.getTestRunPK(),conn)).getSubmissionPK(),conn);
        zipOutputStream.putNextEntry(new ZipEntry(getProjectNumber() + "-canonical.zip"));
        zipOutputStream.write(canonical.downloadArchive(conn));
        
        // Serialize the project object itself and include it
        zipOutputStream.putNextEntry(new ZipEntry(getProjectNumber() +"-project.out"));
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream=new ObjectOutputStream(baos);
        objectOutputStream.writeObject(this);
        objectOutputStream.flush();
        objectOutputStream.close();
        zipOutputStream.write(baos.toByteArray());
        
        // project starter files, if any
        if (getArchivePK() != null) {
            zipOutputStream.putNextEntry(new ZipEntry(getProjectNumber() +"-project-starter-files.zip"));
            zipOutputStream.write(downloadArchive(conn));
        }

        zipOutputStream.close();
    }
    
    private void readObject(ObjectInputStream stream)
    throws IOException, ClassNotFoundException
    {
        int thisMinorVersion = stream.readInt();
        if (thisMinorVersion != serialMinorVersion) throw new IOException("Illegal minor version " + thisMinorVersion + ", expecting minor version " + serialMinorVersion);
        stream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream stream)
    throws IOException
    {
        stream.writeInt(serialMinorVersion);
        stream.defaultWriteObject();
    }
    
    public static List<Project> lookupAllByCoursePK(String coursePK,
    		Connection conn) throws SQLException
    {
    	String query = "SELECT " +ATTRIBUTES+
    	" FROM projects "+
    	" WHERE projects.course_pk = ? " +
        " ORDER BY ontime ASC ";
    	
    	PreparedStatement stmt = null;
    	try {
    	    stmt = conn.prepareStatement(query);
    	    stmt.setString(1, coursePK);
            return Project.getProjectsFromPreparedStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    public static List<Project> lookupAllByStudentPKAndCoursePK(String studentPK,
    		String coursePK, Connection conn)
    throws SQLException
    {
    	String query = " SELECT " +ATTRIBUTES+
    	" FROM "+
    	" projects, student_registration "+
    	" WHERE student_registration.student_pk = ? "+
    	" AND student_registration.course_pk = projects.course_pk "+
    	" AND projects.course_pk = ?";
    	
    	PreparedStatement stmt = null;
    	try {
    	    stmt = conn.prepareStatement(query);
    	    stmt.setString(1, studentPK);
    	    stmt.setString(2, coursePK);
            return getProjectsFromPreparedStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    private static List<Project> getProjectsFromPreparedStatement(PreparedStatement stmt)
    throws SQLException
    {
        List<Project> projects = new LinkedList<Project>();
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
        {
            Project project = new Project();
            project.fetchValues(rs, 1);
            projects.add(project);
        }
        stmt.close();
        return projects;
    }

    public static Project importProject(InputStream in,
        Course course,
        StudentRegistration canonicalStudentRegistration,
        Connection conn)
    throws SQLException, IOException, ClassNotFoundException
    {
        Project project=new Project();
        ZipInputStream zipIn=new ZipInputStream(in);
        
        // Start transaction
        conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        
        byte[] canonicalBytes=null;
        byte[] testSetupBytes=null;
        byte[] projectStarterFileBytes=null;
        
        while (true) {
            ZipEntry entry=zipIn.getNextEntry();
            if (entry==null) break;
            if (entry.getName().contains("project.out")) {
                // Found the serialized project!
                ObjectInputStream objectInputStream=new ObjectInputStream(zipIn);
                
                project=(Project)objectInputStream.readObject();

                // Set the PKs to null, the values that get serialized are actually from
                // a different database with a different set of keys
                project.setProjectPK(null);
                project.setProjectJarfilePK("0");
                project.setArchivePK(null);
                project.setVisibleToStudents(false);
                
                // These two PKs need to be passed in when we import/create the project
                project.setCoursePK(course.getCoursePK());
                project.setCanonicalStudentRegistrationPK(canonicalStudentRegistration.getStudentRegistrationPK());
                
                // Insert the project so that we have a projectPK for other methods
                project.insert(conn);
                
            } else if (entry.getName().contains("canonical")) {
                // Found the canonical submission...
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                CopyUtils.copy(zipIn, baos);
                canonicalBytes=baos.toByteArray();
            } else if (entry.getName().contains("test-setup")) {
                // Found the test-setup!
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                CopyUtils.copy(zipIn, baos);
                testSetupBytes=baos.toByteArray();
            } else if (entry.getName().contains("project-starter-files")) {
                // Found project starter files
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                CopyUtils.copy(zipIn, baos);
                projectStarterFileBytes=baos.toByteArray();
            }
        }
        
        Timestamp submissionTimestamp=new Timestamp(System.currentTimeMillis());
        
        // Now "upload" bytes as an archive for the project starter files, if it exists
        if (projectStarterFileBytes!=null) {
            project.setArchiveForUpload(projectStarterFileBytes);
            project.uploadCachedArchive(conn);
        }
        
        // Now "submit" these bytes as a canonical submission
        // TODO read the submissionTimestamp from the serialized project in the archive
        Submission submission=Submission.submit(
            canonicalBytes,
            canonicalStudentRegistration,
            project,
            "t" + submissionTimestamp.getTime(),
            "ProjectImportTool, serialMinorVersion",
            Integer.toString(serialMinorVersion,100),
            submissionTimestamp,
            conn);
        
        // Now "upload" the test-setup bytes as an archive
        String comment="Project Import Tool uploaded at " +submissionTimestamp;
        ProjectJarfile projectJarfile=ProjectJarfile.submit(testSetupBytes, project, comment, conn);
        project.setProjectJarfilePK(projectJarfile.getProjectJarfilePK());
        projectJarfile.setTestRunPK(submission.getCurrentTestRunPK());
        
        
        projectJarfile.update(conn);
        
        return project;
    }
}
