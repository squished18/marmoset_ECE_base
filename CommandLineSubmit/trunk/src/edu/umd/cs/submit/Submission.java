package edu.umd.cs.submit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class represents a submission.
 */
public class Submission {

  private static final String PROPERTY_FILE_NAME = "submit.properties";
  //private static final String PROPERTY_FILE_NAME = "edu/umd/cs/submit/submit.properties";
  private static final String SUBMIT_SERVER_URL_KEY = "submit.server.url";
  //private static final String SUBMIT_COURSE_KEY = "submit.course";
  //private static final String SUBMIT_SEMESTER_KEY = "submit.semester";

  private static final String SUBMIT_PROJECT_URL =
    "/eclipse/SubmitProjectViaEclipse"; 
  private static final String NEGOTIATE_PASSWORD_URL = 
    "/eclipse/NegotiateOneTimePassword";

  // dynamically build these url
  private String submitURL;
  private String passwordURL;
  private String username;
  private String projectNumber;
  private List<File> files;

  // Course name needs to be the name used in marmoset for the course
	private String courseName;
  // semester needs to be the name used in marmoset
  private String semester;

  /**
   * Construct a new submission object.
   *
   * @throws IOException thrown when IO error occurs when loading properties
   * @throws LoadPropertyException thrown when error occurs in loading 
   *         properties
   */
	public Submission() throws IOException, LoadPropertyException {
    super();
		files = new ArrayList<File>();
    InputStream in = Submission.class.getClassLoader().getResourceAsStream(PROPERTY_FILE_NAME);
    if (in == null) {
      throw new RuntimeException("Cannot get resource: " + PROPERTY_FILE_NAME);
    }
    Properties prop = new Properties();
    prop.load(in);
    //this.courseName = this.loadProperty(prop, SUBMIT_COURSE_KEY);
    //this.semester = this.loadProperty(prop, SUBMIT_SEMESTER_KEY);
    String submitServerURL = this.loadProperty(prop, SUBMIT_SERVER_URL_KEY);
    this.submitURL = submitServerURL + SUBMIT_PROJECT_URL;
    this.passwordURL = submitServerURL + NEGOTIATE_PASSWORD_URL;
	}

  /**
   * Load the course name from the given property.
   */
  private String loadProperty(Properties prop, String propName) 
      throws LoadPropertyException {
    String result = prop.getProperty(propName, null);
    if (result == null) {
      throw new LoadPropertyException("Cannot load the property: " + 
          propName);
    }
    return result;
  }


  /**  
   * Set the course name to the given name
   *
   * @param courseName name of the course
   */
	public void setCourseName(String courseName){
		this.courseName = courseName;
	}

  /**  
   * Return the name of the course.
   *
   * @return the name of the course.
   */
	public String getCourseName(){
		return courseName;
	}

  /**
   * Set the semester to the specified value
   *
   * @param semester semster of the course offering
   */
  public void setSemester(String semester){
    this.semester = semester;
  }

  /**
   * Return the semster of the course offering.
   *
   * @return the semster of the course offering
   */
  public String getSemester(){
    return semester;
  }

  /**
   * Return the submit URL.
   *
   * @return the submit url
   */
  public String getSubmitURL(){
    return submitURL;
  }

  /**
   * Return the password URL.
   *
   * @return the password url
   */
  public String getPasswordURL(){
    return passwordURL;
  }

  /**
   * Set the name of the user to the specified name.
   *
   * @param username name of the user
   */
  public void setUserName(String username){
    this.username = username;
  }

  /**
   * Return the user name.
   *
   * @return the user name
   */
  public String getUserName(){
    return username;		
  }

  /**
   * Set the project number to the given project number.
   *
   * @param projectNumber the project number
   */
  public void setProjectNumber(String projectNumber){
    this.projectNumber = projectNumber;
  }

  /**
   * Return the project number for this submission.
   *
   * @return the project number for this submission
   */
  public String getProjectNumber(){
    return projectNumber;
  }

  /**
   * Add the given file to this submission.
   *
   * @param f the file to be added
   */
  public void addFile(File f){
    files.add(f);
  }

  /**
   * Return a byte array output stream representing the zipped file for
   * this submission.
   *
   * @return a byte array output stream representing the zipped file for
   *         this submission
   */
  public ByteArrayOutputStream getZippedByteArray() throws Exception{
    byte[] buf = new byte[4096];
    ByteArrayOutputStream bytes = new ByteArrayOutputStream(4096);
    ZipOutputStream zipfile = new ZipOutputStream(bytes);
    zipfile.setComment("Submitted zipfile for " + getCourseName() + 
        " project number "+ getProjectNumber() + " for user "+ getUserName());
    for (Iterator<File> i = files.iterator(); i.hasNext();) {
      File resource = i.next();            
      String relativePath = resource.getCanonicalPath();
      // System.out.println(relativePath);

      /*
       * Create file with the full relative path
       */
      //ZipEntry entry = new ZipEntry(relativePath);
            
      ZipEntry entry = new ZipEntry(resource.getName());

      entry.setTime(resource.lastModified());

      zipfile.putNextEntry(entry);
      InputStream in = new FileInputStream(resource);
      try {
        while (true) {
          int n = in.read(buf);
          if (n < 0) {
            break;
          }
          zipfile.write(buf, 0, n);
        }
      } finally {
        in.close();
      }
      zipfile.closeEntry();

    } // for each file
    zipfile.close();
        
    return bytes;
  }

  /**
   * Return a byte array output stream representing the file for
   * this submission.
   *
   * @return a byte array output stream representing the zipped file for
   *         this submission
   */
  public ByteArrayOutputStream getByteArray() throws Exception{
    byte[] buf = new byte[4096];
    ByteArrayOutputStream bytes = new ByteArrayOutputStream(4096);
    //zipfile.setComment("Submitted zipfile for " + getCourseName() + 
     //   " project number "+ getProjectNumber() + " for user "+ getUserName());
    for (Iterator<File> i = files.iterator(); i.hasNext();) {
      File resource = i.next();            
      String relativePath = resource.getCanonicalPath();
      // System.out.println(relativePath);

      /*
       * Create file with the full relative path
       */
      //ZipEntry entry = new ZipEntry(relativePath);
            
      //ZipEntry entry = new ZipEntry(resource.getName());

      //entry.setTime(resource.lastModified());

      //zipfile.putNextEntry(entry);
      InputStream in = new FileInputStream(resource);
      try {
        while (true) {
          int n = in.read(buf);
          if (n < 0) {
            break;
          }
          //zipfile.write(buf, 0, n);
       	  bytes.write(buf, 0, n); 
	}
      } finally {
        in.close();
      }
      //zipfile.closeEntry();

    } // for each file
    //zipfile.close();
        
    return bytes;
  }

  /**
   * Return a list of files for this submission.
   *
   * @return a list of files for this submission
   */
  public List<File> getFiles(){
    return files;
  }
}
