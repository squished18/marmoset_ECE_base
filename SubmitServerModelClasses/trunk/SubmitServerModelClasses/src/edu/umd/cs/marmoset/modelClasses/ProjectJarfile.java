/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 9, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author jspacco
 *
 */
public class ProjectJarfile
{
    private String projectJarfilePK;
    private String projectPK="0";
    private String jarfileStatus=NEW;
    private int version=0;
    private Timestamp datePosted;
    private String comment;
    private String testRunPK;
    private int numTotalTests;
    private int valuePublicTests;
    private int valueReleaseTests;
    private int valueSecretTests;
    private String archivePK;
    private byte[] cachedArchive;
    
    /** Names of columns for this table. */
     static final String[] ATTRIBUTES_LIST = {
        "project_jarfile_pk",
        "project_pk",
        "jarfile_status",
        "version",
        "date_posted",
        "comment",
        "test_run_pk",
        "num_total_tests",
        "num_build_tests",
        "num_public_tests",
        "num_release_tests",
        "num_secret_tests",
        "archive_pk"
    };
    
    /** Name of this table in the database */
    public static final String TABLE_NAME = "project_jarfiles";
    
    /** Fully-qualified attributes for project_jarfiles table. */
    public static final String ATTRIBUTES = Queries.getAttributeList(TABLE_NAME, ATTRIBUTES_LIST);
    public static final String NEW = "new";
    public static final String PENDING = "pending";
    public static final String FAILED = "failed";
    public static final String TESTED = "tested";
    public static final String ACTIVE = "active";
    public static final String INACTIVE = "inactive";
    public static final String BROKEN = "broken";
    
    
	public void setArchiveForUpload(byte[] bytes)
	{
	    cachedArchive = bytes;
	}
	
    /**
     * @return Returns the comment.
     */
    public String getComment() {
        return comment;
    }
    /**
     * @param comment The comment to set.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
    /**
     * @return Returns the testRunPK.
     */
    public String getTestRunPK()
    {
        return testRunPK;
    }
    /**
     * @param testRunPK The testRunPK to set.
     */
    public void setTestRunPK(String testRunPK)
    {
        this.testRunPK = testRunPK;
    }
    /**
     * @return Returns the datePosted.
     */
    public Timestamp getDatePosted() {
        return datePosted;
    }
    /**
     * @param datePosted The datePosted to set.
     */
    public void setDatePosted(Timestamp datePosted) {
        this.datePosted = datePosted;
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
     * @return Returns the jarfileStatus.
     */
    public String getJarfileStatus()
    {
        return jarfileStatus;
    }
    /**
     * @param jarfileStatus The jarfileStatus to set.
     */
    public void setJarfileStatus(String jarfileStatus)
    {
        this.jarfileStatus = jarfileStatus;
    }
    /**
     * @return Returns the version.
     */
    public int getVersion() {
        return version;
    }
    /**
     * @param version The version to set.
     */
    public void setVersion(int version) {
        this.version = version;
    }

    public int getValuePublicTests() {
        return valuePublicTests;
    }
    public void setValuePublicTests(int numPublicTests) {
        this.valuePublicTests = numPublicTests;
    }
    public int getValueReleaseTests() {
        return valueReleaseTests;
    }
    public void setValueReleaseTests(int numReleaseTests) {
        this.valueReleaseTests = numReleaseTests;
    }
    public int getValueSecretTests() {
        return valueSecretTests;
    }
    public void setValueSecretTests(int numSecretTests) {
        this.valueSecretTests = numSecretTests;
    }
    public int getValueTotalTests() {
        return numTotalTests;
    }
    public void setValueTotalTests(int numTotalTests) {
        this.numTotalTests = numTotalTests;
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
     * Fetches a ProjectJarfile row from the database using the given ResultSet
     * starting at the given index.
     * <p>
     * <b>NOTE:</b> This retrieves only the archivePK, not the actual bytes, which
     * must be loaded separately using the {@link dowloadArchive(Connection)} method. 
     *  
     * @param rs the ResultSet
     * @param startingFrom the index
     * @return the index of the next column in the ResultSet
     * @throws SQLException
     */
    public int fetchValues(ResultSet rs, int startingFrom)
    throws SQLException
    {
        setProjectJarfilePK(rs.getString(startingFrom++));
        setProjectPK(rs.getString(startingFrom++));
        setJarfileStatus(rs.getString(startingFrom++));
        setVersion(rs.getInt(startingFrom++));
        setDatePosted(rs.getTimestamp(startingFrom++));
        setComment(rs.getString(startingFrom++));
        setTestRunPK(rs.getString(startingFrom++));
        setValueTotalTests(rs.getInt(startingFrom++));
        if (rs.getInt(startingFrom++) != 1) {
            // FIXME:
        }
        setValuePublicTests(rs.getInt(startingFrom++));
        setValueReleaseTests(rs.getInt(startingFrom++));
        setValueSecretTests(rs.getInt(startingFrom++));
        setArchivePK(rs.getString(startingFrom++));
        return startingFrom;
    }
    /**
     * @param projectJarfilePK2
     * @param conn
     * @return
     */
    public static ProjectJarfile lookupByProjectJarfilePK(String projectJarfilePK, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +ATTRIBUTES+
            " FROM project_jarfiles " +
            " WHERE project_jarfile_pk = ? ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
            
        stmt.setString(1, projectJarfilePK);
        return getFromPreparedStatement(stmt);
    }
    
    /**
     * Returns a collection of project jarfiles for a given projectPK.
     * @param projectPK
     * @param conn
     * @return
     */
    public static Collection<ProjectJarfile> lookupAllByProjectPK(String projectPK, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +ATTRIBUTES+
            " FROM project_jarfiles " +
            " WHERE project_pk = ? ";
        
        PreparedStatement stmt = conn.prepareStatement(query);
            
        stmt.setString(1, projectPK);
        ResultSet rs = stmt.executeQuery();
        
        Collection<ProjectJarfile> allProjectJarFiles = new LinkedList<ProjectJarfile>();
        while (rs.next()) {
            ProjectJarfile jarFile = new ProjectJarfile();
            jarFile.fetchValues(rs, 1);
            allProjectJarFiles.add(jarFile);
            //Debug.print("Got project with PK " +project.getProjectPK());
        }
        return allProjectJarFiles;
        
    }
    
    private static ProjectJarfile getFromPreparedStatement(PreparedStatement stmt)
    throws SQLException
    {
        try {
            ResultSet rs = stmt.executeQuery();
            if (rs.first())
            {
                ProjectJarfile projectJarfile = new ProjectJarfile();
                projectJarfile.fetchValues(rs, 1);
                return projectJarfile;
            }
            return null;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ignore) {
                // ignore
            }
        }
    }
    
    /**
     * @param conn
     */
    public void insert(Connection conn)
    throws SQLException
    {
        String insert = Queries.makeInsertStatement(
                ATTRIBUTES_LIST.length,
                ATTRIBUTES,
                "project_jarfiles");
        PreparedStatement stmt=null;
        try {
            stmt = conn.prepareStatement(insert);
            
            if (cachedArchive == null)
                throw new IllegalStateException("there is no archive for upload, you should call setArchiveForUpload first");
            setArchivePK(Archive.uploadBytesToArchive("project_jarfile_archives", cachedArchive, conn));
            
            int index=1;
            putValues(stmt, index);
            
            stmt.executeUpdate();
            
            // set PK to the last autoincrement value for this connection
            // this will be the PK of the newly inserted row
            setProjectJarfilePK(Queries.lastInsertId(conn));
            
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
	public byte[] downloadArchive(Connection conn)
    throws SQLException
    {
	    return Archive.downloadBytesFromArchive("project_jarfile_archives", getArchivePK(), conn);
    }

    
    public void update(Connection conn)
    throws SQLException
    {
        String update = Queries.makeUpdateStatementWithWhereClause(
                ATTRIBUTES_LIST,
                "project_jarfiles",
                " WHERE project_jarfile_pk = ? ");
        
        PreparedStatement stmt=null;
        try {
            stmt = conn.prepareStatement(update);
            int index=1;
            index=putValues(stmt, index);
            stmt.setString(index, getProjectJarfilePK());

            int rows = stmt.executeUpdate();

        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    public static void resetAllFailedTestSetups(String projectPK, Connection conn)
    throws SQLException
    {
        String update = 
            " UPDATE project_jarfiles " +
            " SET " +
            " jarfile_status = ? " +

            " WHERE (jarfile_status = ? || jarfile_status = ? || jarfile_status = ?)" +
            " AND project_pk = ? ";
        PreparedStatement stmt=null;
        try {
            // update statement
            stmt = conn.prepareStatement(update);
            stmt.setString(1, NEW );
            // where clause
            stmt.setString(2, PENDING );
            stmt.setString(3, TESTED );
            stmt.setString(4, FAILED);
            stmt.setString(5, projectPK);
   
            stmt.executeUpdate();
        } finally {
                if (stmt != null) stmt.close();
        }
    }
    
    private int putValues(PreparedStatement stmt, int index)
    throws SQLException
    {
        stmt.setString(index++, getProjectPK());
        stmt.setString(index++, getJarfileStatus());
        stmt.setInt(index++, getVersion());
        stmt.setTimestamp(index++, getDatePosted());
        stmt.setString(index++, getComment());
        stmt.setString(index++, getTestRunPK());
        stmt.setInt(index++, getValueTotalTests());
        stmt.setInt(index++, 1);
        stmt.setInt(index++, getValuePublicTests());
        stmt.setInt(index++, getValueReleaseTests());
        stmt.setInt(index++, getValueSecretTests());
        stmt.setString(index++, getArchivePK());
        return index;
    }

    /**
     * Upload a new projectJarfile (test-setup) into the database given a byte array
     * of the zip archive of the test-setup file, the project the test-setup is associated with,
     * and a comment describing the uploaded archive.
     * @param byteArray Byte array containing of the zip archive of the test-setup file.
     * @param project The project the test-setup is to be associated with.
     * @param comment A comment (possibly empty or null) describing the reason for uploading
     *      a new archive.
     * @param conn The connection to the database.
     * @return The result ProjectJarfile object.
     * @throws SQLException If something doesn't work.
     */
    public static ProjectJarfile submit(
        byte[] byteArray,
        Project project,
        String comment,
        Connection conn) throws SQLException
    {
        // create new projectJarfile record
        ProjectJarfile projectJarfile = new ProjectJarfile();
        projectJarfile.setComment(comment);
        projectJarfile.setDatePosted(new Timestamp(System.currentTimeMillis()));
        projectJarfile.setProjectPK(project.getProjectPK());
        projectJarfile.setArchiveForUpload(byteArray);
    
        // insert the new jarfile in its default state
        projectJarfile.insert(conn);
        return projectJarfile;
    }
}
