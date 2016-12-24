/*
 * Created on Aug 30, 2004
 */
package edu.umd.cs.marmoset.modelClasses;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;


/**
 * TODO refactor the lookup[New|Pending|ReTest]Submission...() methods so that they return the
 * submission, and move them into Submission.  Then write another static method that looks up
 * the other information.
 *
 * Canned queries.
 */
public final class Queries {
	private Queries() {
	    // block instantiation of this class
	}
    
    /**
     * Max number of times a submission should be successfully retested.
     * TODO Make this configurable in web.xml.
     */
    public static final int MAX_SUCCESSFUL_RETESTS=2;
    
    private static boolean getFromPreparedStatement(PreparedStatement stmt, 
	        Submission submission,
	        ProjectJarfile projectJarfile)
	throws SQLException
	{
	    try {
	        ResultSet rs = stmt.executeQuery();
	        
	        if (rs.next())
	        {
	            int index=1;
	            index = submission.fetchValues(rs, index);
	            index = projectJarfile.fetchValues(rs, index);
	            return true;
	        }
	        return false;
	    }
	    finally {
	        closeStatement(stmt);
	    }
	}
	
	/**
	 * Format an array of attributes as a fully qualified attribute
	 * list, suitable for use in a SELECT query.
	 * 
	 * @param table name of table
	 * @param attrNameList array of attribute names for table
	 * @return fully qualified attribute list for query
	 */
	public static String getAttributeList(String table, String[] attrNameList) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < attrNameList.length; ++i) {
			buf.append(table);
			buf.append('.');
			buf.append(attrNameList[i]);
			if (i < attrNameList.length - 1)
				buf.append(", ");
		}
		return buf.toString();
	}

	/**
	 * Retrieves the last autoincrement value produced by this connection
	 * (i.e. the last autoincrement value used when a new row was added by
	 * this connection).
	 * <p>
	 * This method does not need to be called in a transaction 
	 * because connections should not be shared by threads.
	 * 
	 * @param conn connection to the database
	 * @return the value of the last autoinsert for this connection
	 * @throws SQLException
	 */
	public static String lastInsertId(Connection conn)
	throws SQLException
	{
	    String query = " SELECT LAST_INSERT_ID() ";
        
	    PreparedStatement stmt=null;
	    try {
            stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.first())
            {
                return rs.getString(1);
            }
            throw new SQLException("SELECT LAST_INSERT_ID() didn't work");
        } finally {
            if (stmt != null) stmt.close();
        }
	}
	
	public static void closeStatement(PreparedStatement stmt)
	{
	    try {
	        if (stmt != null) stmt.close();
	    } catch (SQLException ignore) {
	        // ignore
	    }
	}
    
    public static String makeInsertStatementUsingSetSyntax(String[] attributes, String tableName)
    {
        StringBuffer buf=new StringBuffer();
        buf.append(" INSERT INTO " +tableName+ " \n");
        buf.append(" SET \n");
        for (int ii=0; ii<attributes.length-1; ii++) {
            buf.append(" " +attributes[ii]+ " = ?, \n");
        }
        buf.append(" " +attributes[attributes.length-1]+ " = ? \n");
        return buf.toString();
    }
	
	public static String makeInsertStatement(int numAttributes, String attributes, String tableName)
	{
	    StringBuffer result = new StringBuffer();
	    result.append(" INSERT INTO " +tableName+ " \n");
	    result.append(" ( " +attributes+ " ) \n");
	    result.append(" VALUES \n");
	    result.append(" ( DEFAULT, \n");
	    // leave a slot at the beginning for the primary key and one at the end with no comma
	    for (int ii=0; ii < numAttributes-2; ii++)
	    {
	        result.append(" ?, \n");
	    }
	    result.append(" ? ) \n");
	    return result.toString();
	}
	
	public static String makeUpdateStatementWithWhereClause(
	        String[] attributesList,
	        String tableName,
	        String whereClause)
	{
	    StringBuffer update = makeGenericUpdateStatement(attributesList, tableName);
	    update.append(whereClause);
	    return update.toString();
	}
	
	private static StringBuffer makeGenericUpdateStatement(String[] attributesList, String tableName)
	{
	    StringBuffer result = new StringBuffer();
	    result.append(" UPDATE " +tableName+ " \n");
	    result.append(" SET \n");
	    // ignore first slot which is the PK
	    // leave a [ col_name = ? ] at the end with no comma
	    for (int ii=1; ii < attributesList.length-1; ii++)
	    {
	        result.append(attributesList[ii] +" = ?, \n");
	    }
	    result.append(attributesList[attributesList.length-1] +" = ? \n");
	    return result;
	}

    /**
     * Gets the most recent canonincal submission along with a new project jarfile.
     * 
     * @param conn
     * @param submission
     * @param project
     * @param course
     * @param studentRegistration
     * @param projectJarfile
     * @return
     */
    public static boolean lookupNewProjectJarfile(Connection conn,
            Submission submission,
            ProjectJarfile projectJarfile,
            String[] allowedCourses,
            int maxBuildDurationInMinutes)
    throws SQLException
    {
        //SQL timestamp of build requests that have,
		// as of this moment, taken too long.
		Timestamp buildTimeout = new Timestamp(
				System.currentTimeMillis() - (maxBuildDurationInMinutes * 60L * 1000L));
        
        String courseRestrictions = makeCourseRestrictionsWhereClause(allowedCourses);
		
		String query =
            " SELECT " +
            Submission.ATTRIBUTES +", "+
            ProjectJarfile.ATTRIBUTES +" "+
            " FROM submissions, project_jarfiles, projects " +
            " WHERE (" +
            "		 (project_jarfiles.jarfile_status = ?) " +
            "		 OR (project_jarfiles.jarfile_status = ? AND project_jarfiles.date_posted < ?)" +
            "       ) " +
            courseRestrictions +
            " AND project_jarfiles.project_pk = projects.project_pk " +
            " AND submissions.project_pk = projects.project_pk " +
            " AND submissions.student_registration_pk = projects.canonical_student_registration_pk " +
            " ORDER BY submissions.submission_number DESC " +
            " LIMIT 1 " +
            " LOCK IN SHARE MODE ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, ProjectJarfile.NEW);
        stmt.setString(2, ProjectJarfile.PENDING);
        stmt.setTimestamp(3, buildTimeout);
        
        return getFromPreparedStatement(stmt,
                submission,
                projectJarfile);
    }

    /**
     * @param allowedCourses
     * @return
     */
    public static String makeCourseRestrictionsWhereClause(String[] allowedCourses)
    {
        String whereClause = "";
        if (allowedCourses != null && allowedCourses.length > 0)
        {
            whereClause += " AND ( ( projects.course_pk = " +allowedCourses[0] + " ) ";
            for (int ii=1; ii < allowedCourses.length; ii++)
            {
                whereClause += " OR ( projects.course_pk = " +allowedCourses[ii]+ " ) ";
            }
            whereClause += " ) ";
        }
        return whereClause;
    }

    /**
     * Looks up the first submission run against an outdated test-setup.  This method
     * is designed for speed and efficiently and <b>DOES NOT</b> necessarily return
     * the most recent outdated submission because the sort in the DB is too slow.
     * @param conn Connection to database.
     * @param submission
     * @param projectJarfile
     * @param allowedCourses
     * @param maxBuildDurationInMinutes
     * @return
     * @throws SQLException
     */
    public static boolean lookupOutdatedProjectJarfileSubmissionForResearchServer(Connection conn,
        Submission submission,
        ProjectJarfile projectJarfile,
        String[] allowedCourses,
        int maxBuildDurationInMinutes)
    throws SQLException
    {
        Timestamp buildTimeout = new Timestamp(System.currentTimeMillis() - 
        (maxBuildDurationInMinutes * 60L * 1000L));
        
        String courseRestrictions = makeCourseRestrictionsWhereClause(allowedCourses);
        
        String query =
            " SELECT " +
            Submission.ATTRIBUTES +", "+
            ProjectJarfile.ATTRIBUTES +" "+
            " FROM submissions, project_jarfiles, test_runs, projects " +
            " WHERE submissions.current_test_run_pk = test_runs.test_run_pk " +
            courseRestrictions +
            " AND project_jarfiles.project_jarfile_pk = projects.project_jarfile_pk " +
            " AND projects.canonical_student_registration_pk != submissions.student_registration_pk " +
            " AND submissions.project_pk = projects.project_pk " +
            " AND test_runs.project_jarfile_pk != projects.project_jarfile_pk " +
            " AND ( " +
            "  (submissions.build_status = ? AND submissions.build_request_timestamp < ? )" +
            "     OR submissions.build_status = ? " +
            "     OR submissions.build_status = ? " +
            " ) " +
            " LIMIT 1 " +
            " LOCK IN SHARE MODE ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        
        // It should not be possible for one of these to be in the "new" state...
        // For a submission to have a current_test_run_pk by definition it has been tested.
        stmt.setString(1, Submission.PENDING);
        stmt.setTimestamp(2, buildTimeout);
        stmt.setString(3, Submission.COMPLETE);
        stmt.setString(4, Submission.RETEST);
        
        
        return getFromPreparedStatement(stmt, submission, projectJarfile);
    }
    
    /**
     * Finds the most recent submission that was run for a previous project jarfile.
     * Also retrieves the newest project jarfile.
     * <p>
     * Will find submissions marked for "retest" as well as any "pending" submissions
     * that have timed out.  Will return "complete" tests for an older jarfile.  Should
     * not return anything in the "new" state as these submission should be caught by
     * an earlier call to lookupNewSubmission().  Also should not return anything marked
     * "pending" that has not timed out.
     * @param conn
     * @param submission
     * @param projectJarfile
     * @param allowedCourses an array of the course names served by the buildserver
     * making the request
     * @param maxBuildDurationInMinutes how long should a submission remain pending 
     * before being sent to another buildServer?
     * @return
     * @throws SQLException
     */
    public static boolean lookupOutdatedProjectJarfileSubmission(Connection conn,
            Submission submission,
            ProjectJarfile projectJarfile,
            String[] allowedCourses,
            int maxBuildDurationInMinutes)
    throws SQLException
    {
    	Timestamp buildTimeout = new Timestamp(System.currentTimeMillis() - 
    		(maxBuildDurationInMinutes * 60L * 1000L));
    		
        String courseRestrictions = makeCourseRestrictionsWhereClause(allowedCourses);
        
        String query =
            " SELECT " +
            Submission.ATTRIBUTES +", "+
            ProjectJarfile.ATTRIBUTES +" "+
            " FROM submissions, project_jarfiles, test_runs, projects " +
            " WHERE submissions.current_test_run_pk = test_runs.test_run_pk " +
            courseRestrictions +
            " AND project_jarfiles.project_jarfile_pk = projects.project_jarfile_pk " +
            " AND projects.canonical_student_registration_pk != submissions.student_registration_pk " +
            " AND submissions.project_pk = projects.project_pk " +
            " AND test_runs.project_jarfile_pk != projects.project_jarfile_pk " +
            " AND ( " +
            "  (submissions.build_status = ? AND submissions.build_request_timestamp < ? )" +
            "     OR submissions.build_status = ? " +
            "     OR submissions.build_status = ? " +
            " ) " +
            " ORDER BY submissions.submission_timestamp DESC " +
            " LIMIT 1 " +
            " LOCK IN SHARE MODE ";
        
		PreparedStatement stmt = conn.prepareStatement(query);
		
		// It should not be possible for one of these to be in the "new" state...
		// For a submission to have a current_test_run_pk by definition it has been tested.
		stmt.setString(1, Submission.PENDING);
		stmt.setTimestamp(2, buildTimeout);
		stmt.setString(3, Submission.COMPLETE);
		stmt.setString(4, Submission.RETEST);
		
		
		return getFromPreparedStatement(stmt, submission, projectJarfile);
    }
    
    public static boolean lookupExplicitBackgroundRetest(
    	Connection conn,
    	Submission submission,
    	ProjectJarfile projectJarfile,
    	String[] allowedCourses,
    	boolean isBackgroundRetestingEnabled)
    throws SQLException
    {
    	if (!isBackgroundRetestingEnabled)
    		return false;
    	String courseRestrictions = makeCourseRestrictionsWhereClause(allowedCourses);
    	String query =
            " SELECT " +
            Submission.ATTRIBUTES +", "+
            ProjectJarfile.ATTRIBUTES +" "+
            " FROM submissions, project_jarfiles, projects " +
            " WHERE submissions.build_status = ? " +
            " AND submissions.num_build_tests_passed > 0 " +
            " AND submissions.project_pk = projects.project_pk " +
            " AND projects.project_jarfile_pk = project_jarfiles.project_jarfile_pk " +
            courseRestrictions +
            " ORDER BY rand() " +
            " LIMIT 1 " +
            " LOCK IN SHARE MODE ";
    	
    	PreparedStatement stmt = conn.prepareStatement(query);
    	stmt.setString(1, Submission.BACKGROUND);
    	
    	return getFromPreparedStatement(stmt, submission, projectJarfile);
    }
    
    /**
     * If background re-testing is enabled, pick a random submission for one of the allowed 
     * courses and its current projectJarfile.  Will only retest a submission 6 times
     * against the current projectJarfile.  If the first 2 retests are "successful" 
     * (meaning that the results are exactly the same as the results for the
     * current_test_run_pk) then no further retesting is performed.  Submissions
     * must compile to be eligible for background retesting.
     * 
     * @param conn
     * @param submission
     * @param projectJarfile
     * @param allowedCourses
     * @param isBackgroundRetestingEnabled
     * @return True if background re-testing is enabled and we found a submission and jarfile;
     * 		false otherwise.
     * @throws SQLException
     */
    public static boolean lookupSubmissionForBackgroundRetest(
    	Connection conn,
    	Submission submission,
    	ProjectJarfile projectJarfile,
    	String[] allowedCourses,
    	boolean isBackgroundRetestingEnabled)
    throws SQLException
    {
    	if (!isBackgroundRetestingEnabled)
    		return false;
    	String courseRestrictions = makeCourseRestrictionsWhereClause(allowedCourses);
    	// Find a random submission
    	// XXX rand() is a MySQL-specific database function call
    	String query =
            " SELECT " +
            Submission.ATTRIBUTES +", "+
            ProjectJarfile.ATTRIBUTES +" "+
            " FROM submissions, project_jarfiles, projects " +
            " WHERE submissions.build_status = ? " +
            " AND submissions.num_build_tests_passed > 0 " +
            " AND submissions.project_pk = projects.project_pk " +
            " AND projects.project_jarfile_pk = project_jarfiles.project_jarfile_pk " +
            courseRestrictions +
            " AND ( " +
            "       (NOT EXISTS (SELECT * FROM background_retests " + 
            "        WHERE background_retests.project_jarfile_pk = project_jarfiles.project_jarfile_pk " +
            "        AND background_retests.submission_pk = submissions.submission_pk)) " +
            "     OR " +
            "       (EXISTS (SELECT * FROM background_retests " +
            "        WHERE background_retests.project_jarfile_pk = project_jarfiles.project_jarfile_pk " +
            "        AND background_retests.submission_pk = submissions.submission_pk " +
            "        AND ((num_successful_background_retests + num_failed_background_retests) < 6) " +
            "        AND NOT (num_successful_background_retests >= ? AND num_failed_background_retests = 0) " +
            "           )  " +
            "       ) " +
            "   ) " +
            " ORDER BY rand() " +
            " LIMIT 1 " +
            " LOCK IN SHARE MODE ";
    	
    	PreparedStatement stmt = conn.prepareStatement(query);
    	stmt.setString(1, Submission.COMPLETE);
        stmt.setInt(2, MAX_SUCCESSFUL_RETESTS);
    	
    	return getFromPreparedStatement(stmt, submission, projectJarfile);
    }
    
    /**
     * Gets the oldest submission marked "new".
     * @param conn
     * @param submission
     * @param projectJarfile
     * @param allowedCourses
     * @return
     * @throws SQLException
     */
    public static boolean lookupNewSubmission(Connection conn,
            Submission submission,
            ProjectJarfile projectJarfile,
            String[] allowedCourses)
    throws SQLException
    {
        String courseRestrictions = makeCourseRestrictionsWhereClause(allowedCourses);
        
        String query =
            " SELECT " +
            Submission.ATTRIBUTES +", "+
            ProjectJarfile.ATTRIBUTES +" "+
            " FROM submissions, project_jarfiles, projects " +
            " WHERE submissions.build_status = ? " +
            " AND submissions.project_pk = projects.project_pk " +
            courseRestrictions +
            " AND projects.project_jarfile_pk = project_jarfiles.project_jarfile_pk " +
            " ORDER BY submissions.submission_timestamp DESC " +
            " LIMIT 1 " +
            " LOCK IN SHARE MODE ";
        
		PreparedStatement stmt = conn.prepareStatement(query);
		
		stmt.setString(1, Submission.NEW);
		
		return getFromPreparedStatement(stmt, submission, projectJarfile);
    }
    
    /**
     * Gets the oldest submission marked "new".
     * @param conn
     * @param submission
     * @param projectJarfile
     * @param allowedCourses
     * @return
     * @throws SQLException
     */
    public static boolean lookupMostRecentNewSubmission(Connection conn,
            Submission submission,
            ProjectJarfile projectJarfile,
            String[] allowedCourses)
    throws SQLException
    {
        String courseRestrictions = makeCourseRestrictionsWhereClause(allowedCourses);
        
        String query =
            " SELECT " +
            Submission.ATTRIBUTES +", "+
            ProjectJarfile.ATTRIBUTES +" "+
            " FROM submissions, project_jarfiles, projects " +
            " WHERE submissions.build_status = ? " +
            " AND submissions.project_pk = projects.project_pk " +
            courseRestrictions +
            " AND projects.project_jarfile_pk = project_jarfiles.project_jarfile_pk " +
            " AND NOT EXISTS " +
            "      (SELECT * " +
            "       FROM submissions as s2 " +
            "       WHERE submissions.student_registration_pk = s2.student_registration_pk " +
            "       AND s2.submission_timestamp > submissions.submission_timestamp) " +
            " ORDER BY submissions.submission_timestamp ASC " +
            " LOCK IN SHARE MODE ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        
        stmt.setString(1, Submission.NEW);
        
        return getFromPreparedStatement(stmt, submission, projectJarfile);
    }
    
    /**
     * Looks up a submission for marked for retest.
     * @param conn
     * @param submission
     * @param projectJarfile
     * @param allowedCourses
     * @param maxBuildDurationInMinutes
     * @return
     * @throws SQLException
     */
    public static boolean lookupReTestSubmission(Connection conn,
            Submission submission,
            ProjectJarfile projectJarfile,
            String[] allowedCourses)
    throws SQLException
    {
		String courseRestrictions = makeCourseRestrictionsWhereClause(allowedCourses);
		
		String query =
            " SELECT " +
            Submission.ATTRIBUTES +", "+
            ProjectJarfile.ATTRIBUTES +" "+
            " FROM submissions, project_jarfiles, projects " +
            " WHERE submissions.build_status = ? " +
            courseRestrictions +
            " AND submissions.project_pk = projects.project_pk " +
            " AND projects.project_jarfile_pk = project_jarfiles.project_jarfile_pk " +
            " LIMIT 1 " +
            " LOCK IN SHARE MODE ";

		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, Submission.RETEST);
		
		return getFromPreparedStatement(stmt, submission, projectJarfile);
    }  
    
    /**
     * Counts the number of submissions that need to be retested for a new test setup
     * for a given projectPK.
     * @param projectPK the projectPK of the project that needs to be retested
     * @param conn the connection to the database
     * @return the number of submissions that need to be retested for a new test setup.
     * @throws SQLException
     */
    public static int countNumSubmissionsForRetest(String projectPK, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT count(*) " +
            " FROM submissions, projects, test_runs, project_jarfiles " +
            " WHERE submissions.project_pk = projects.project_pk " +
            " AND submissions.current_test_run_pk = test_runs.test_run_pk " +
            " AND test_runs.project_jarfile_pk != projects.project_jarfile_pk " +
            " AND project_jarfiles.project_jarfile_pk = projects.project_jarfile_pk " +
            " AND projects.project_pk = ? " +
            " AND projects.canonical_student_registration_pk != submissions.student_registration_pk ";
        PreparedStatement stmt =null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, projectPK);
            
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } finally {
            closeStatement(stmt);
        }
    }
    
    public static int countNumSubmissionsToTest(String projectPK, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT count(*) " +
            " FROM submissions, projects " +
            " WHERE submissions.project_pk = projects.project_pk " +
            " AND submissions.current_test_run_pk is null " +
            " AND projects.project_pk = ? " +
            " AND projects.canonical_student_registration_pk != submissions.student_registration_pk ";
        PreparedStatement stmt =null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, projectPK);
            
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } finally {
            closeStatement(stmt);
        }
    }
    
    /**
     * Gets the most recent submission that has timed out (i.e. been marked "pending" 
     * for too long)
     * @param conn
     * @param submission
     * @param projectJarfile
     * @param allowedCourses
     * @param maxBuildDurationInMinutes the amount of time in minutes a submission can
     * be marked "pending" before we send it out for testing again
     * @return
     * @throws SQLException
     */
    public static boolean lookupPendingSubmission(Connection conn,
            Submission submission,
            ProjectJarfile projectJarfile,
            String[] allowedCourses,
            int maxBuildDurationInMinutes)
    throws SQLException
    {
        // SQL timestamp of build requests that have,
		// as of this moment, taken too long.
		Timestamp buildTimeout = new Timestamp(
				System.currentTimeMillis() - (maxBuildDurationInMinutes * 60L * 1000L));

		String courseRestrictions = makeCourseRestrictionsWhereClause(allowedCourses);
		
		String query =
            " SELECT " +
            Submission.ATTRIBUTES +", "+
            ProjectJarfile.ATTRIBUTES +" "+
            " FROM submissions, project_jarfiles, projects " +
            " WHERE ((submissions.build_status = ? " +
            "        AND submissions.build_request_timestamp < ? )" +
            "       OR submissions.build_status = ?) " +
            courseRestrictions +
            " AND submissions.project_pk = projects.project_pk " +
            " AND projects.project_jarfile_pk = project_jarfiles.project_jarfile_pk " +
            //" ORDER BY submissions.submission_timestamp ASC " +
            " LIMIT 1 " +
            " LOCK IN SHARE MODE ";

		
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, Submission.PENDING);
		stmt.setTimestamp(2, buildTimeout);
		stmt.setString(3, Submission.RETEST);
		
		return getFromPreparedStatement(stmt, submission, projectJarfile);
    }

    public static boolean lookupOutdatedReleaseTestedSubmission(Connection conn,
        Submission submission,
        ProjectJarfile projectJarfile,
        String[] allowedCourses,
        int maxBuildDurationInMinutes)
    throws SQLException
    {
        Timestamp buildTimeout = new Timestamp(System.currentTimeMillis() - 
        (maxBuildDurationInMinutes * 60L * 1000L));
        
        String courseRestrictions = makeCourseRestrictionsWhereClause(allowedCourses);
        
        String query =
            " SELECT " +
            Submission.ATTRIBUTES +", "+
            ProjectJarfile.ATTRIBUTES +" "+
            " FROM submissions, project_jarfiles, test_runs, projects " +
            " WHERE submissions.current_test_run_pk = test_runs.test_run_pk " +
            courseRestrictions +
            " AND project_jarfiles.project_jarfile_pk = projects.project_jarfile_pk " +
            " AND submissions.project_pk = projects.project_pk " +
            " AND test_runs.project_jarfile_pk != projects.project_jarfile_pk " +
            " AND ( " +
            "  (submissions.build_status = ? AND submissions.build_request_timestamp < ? )" +
            "     OR submissions.build_status = ? " +
            "     OR submissions.build_status = ? " +
            " ) " +
            " AND submissions.release_request IS NOT NULL " +
            " AND submissions.student_registration_pk != projects.canonical_student_registration_pk " +
            " ORDER BY submissions.submission_timestamp DESC " +
            " LIMIT 1 " +
            " LOCK IN SHARE MODE ";
        
        PreparedStatement stmt =conn.prepareStatement(query);
        
        // It should not be possible for one of these to be in the "new" state...
        // For a submission to have a current_test_run_pk by definition it has been tested.
        stmt.setString(1, Submission.PENDING);
        stmt.setTimestamp(2, buildTimeout);
        stmt.setString(3, Submission.COMPLETE);
        stmt.setString(4, Submission.RETEST);
        
        return getFromPreparedStatement(stmt, submission, projectJarfile);
    }

    public static boolean lookupMostRecentOutdatedSubmission(Connection conn, 
        Submission submission,
        ProjectJarfile projectJarfile,
        String[] allowedCourses, 
        int maxBuildDurationInMinutes)
    throws SQLException
    {
        Timestamp buildTimeout = new Timestamp(System.currentTimeMillis() - 
        (maxBuildDurationInMinutes * 60L * 1000L));
        
        String courseRestrictions = makeCourseRestrictionsWhereClause(allowedCourses);
        
        String query =
            " SELECT " +
            Submission.ATTRIBUTES +", "+
            ProjectJarfile.ATTRIBUTES +" "+
            " FROM submissions, project_jarfiles, test_runs, projects " +
            " WHERE submissions.current_test_run_pk = test_runs.test_run_pk " +
            courseRestrictions +
            " AND project_jarfiles.project_jarfile_pk = projects.project_jarfile_pk " +
            " AND submissions.project_pk = projects.project_pk " +
            " AND test_runs.project_jarfile_pk != projects.project_jarfile_pk " +
            " AND projects.canonical_student_registration_pk != submissions.student_registration_pk " +
            " AND ( " +
            "  (submissions.build_status = ? AND submissions.build_request_timestamp < ? )" +
            "     OR submissions.build_status = ? " +
            "     OR submissions.build_status = ? " +
            " ) " +
            " AND NOT EXISTS " +
            "       (SELECT * FROM submissions AS s2 " +
            "       WHERE submissions.student_registration_pk = s2.student_registration_pk " +
            "       AND s2.submission_timestamp > submissions.submission_timestamp) " +
            " ORDER BY submissions.submission_timestamp DESC " +
            " LIMIT 1 " +
            " LOCK IN SHARE MODE ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        
        stmt.setString(1, Submission.PENDING);
        stmt.setTimestamp(2, buildTimeout);
        stmt.setString(3, Submission.COMPLETE);
        stmt.setString(4, Submission.RETEST);
        
        return getFromPreparedStatement(stmt, submission, projectJarfile);
    }

    /**
     * Check if the current database specified by the given connection contains a table
     * with the given name.
     * @param tableName The name of the table.
     * @param conn Connection to the database. 
     * @return True if the current database contains a table with the given name;
     *      false otherwise.
     * @throws SQLException
     */
    public static boolean hasTable(String tableName, Connection conn)
    throws SQLException
    {
        PreparedStatement stmt=null;
        try {
            stmt=conn.prepareStatement(" show tables ");
            ResultSet rs=stmt.executeQuery();
            
            while (rs.next()) {
                if (tableName.equals(rs.getString(1)))
                    return true;
            }
            return false;
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    public static boolean hasTableAndColumn(String tableName, String columnName, Connection conn)
    throws SQLException
    {
        if (tableName==null)
            throw new IllegalArgumentException("tableName must not be null!");
        if (columnName==null)
            throw new IllegalArgumentException("columnName must not be null!");
        if (!hasTable(tableName,conn))
            return false;
        PreparedStatement stmt=null;
        try {
            stmt=conn.prepareStatement(" describe " +tableName);
            ResultSet rs=stmt.executeQuery();
            while (rs.next()) {
                if(columnName.equals(rs.getString(1)))
                    return true;
            }
            return false;
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    public static void rollbackIfUnsuccessful(boolean transactionSuccess, Connection conn)
    {
        try {
            if (!transactionSuccess)
                conn.rollback();
        } catch (SQLException ignore) {
            // ignore
        }
    }
}
