/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Apr 18, 2005
 *
 */
package edu.umd.cs.dbunit;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;

import edu.umd.cs.marmoset.modelClasses.Course;

/**
 * @author jspacco
 *
 */
public class CourseTest extends DatabaseTestCase
{
    public static final String JUNIT_DIR = "junit";
    private IDatabaseConnection connection=null;
    
    /**
     * Gets the real database connection
     * @return a real connection to the database
     * @throws Exception
     */
    protected Connection getRealDatabaseConnection()
    throws Exception {
        return getConnection().getConnection();
    }
    
    /* (non-Javadoc)
     * @see org.dbunit.DatabaseTestCase#getConnection()
     */
    protected IDatabaseConnection getConnection() throws Exception
    {
        Class.forName("org.gjt.mm.mysql.Driver");

        Connection jdbcConnection = 
            DriverManager.getConnection(
       	  	"jdbc:mysql://boo3.umiacs.umd.edu/dbunit", "root", "blondie1980");
               
        return new DatabaseConnection(jdbcConnection);
    }
    /* (non-Javadoc)
     * @see org.dbunit.DatabaseTestCase#getDataSet()
     */
    protected IDataSet getDataSet() throws Exception
    {
        return new FlatXmlDataSet(new FileInputStream(JUNIT_DIR + "/courses.xml"));
    }
    /* (non-Javadoc)
     * @see org.dbunit.DatabaseTestCase#getSetUpOperation()
     */
    protected DatabaseOperation getSetUpOperation() throws Exception
    {
        return DatabaseOperation.CLEAN_INSERT;
    }
    /* (non-Javadoc)
     * @see org.dbunit.DatabaseTestCase#getTearDownOperation()
     */
    protected DatabaseOperation getTearDownOperation() throws Exception
    {
        return DatabaseOperation.NONE;
    }
    
    
    
    /* (non-Javadoc)
     * @see org.dbunit.DatabaseTestCase#closeConnection(org.dbunit.database.IDatabaseConnection)
     */
    protected void closeConnection(IDatabaseConnection arg0) throws Exception
    {
        super.closeConnection(arg0);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        if (connection == null) {
            connection = getConnection();
        }
    }
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        closeConnection(connection);
    }
    
    public void testLookupAllByCoursePK()
    throws Exception
    {
        Connection conn = connection.getConnection();
        String studentRegistrationPK = "1";
        Course course = Course.lookupByCoursePK(studentRegistrationPK, conn);
        assertTrue(course.getCourseName().equals("CMSC132"));
        assertTrue(course.getSemester().equals("Spring 2005"));
    }
    
    public void testInsert()
    throws Exception
    {
        Connection conn = connection.getConnection();
        Course course = new Course();
        course.setCourseName("courseName");
        course.setDescription("description");
        course.setSection("1");
        course.setSemester("semester");
        course.setUrl("url");
        
        course.insert(conn);
        
        String coursePK = course.getCoursePK();
        assertEquals(coursePK, "1");
        
        Course insertedCourse = Course.lookupByCoursePK(coursePK, conn);
        
        assertEquals(course, insertedCourse);
    }
}
