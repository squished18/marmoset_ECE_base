/**
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Oct 17, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer;

import java.sql.Connection;
import java.sql.SQLException;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.utilities.MarmosetUtilities;



/**
 * StudentForUpload is a convenience class for parsing the fields required to create/update
 * Student and studentRegistration records from a variety of sources, such as uploaded
 * files or http requests.
 * <p>
 * TODO Note that there is a strong coupling between the Maryland-specific implementations of
 * the student and studentRegistration classes and this class that will need to be dealt with
 * before shipping this code someplace else.
 * class. 
 * @author jspacco
 */
public class StudentForUpload
{
	public final String campusUID;
	public final String employeeNum;
	public final String firstname;
	public final String lastname;
	public final String cvsAccount;
	public final String section;
    public final String password;
    
    // [NAT P003] Boolean variables to indicate if a random password is generated and active
    private boolean hasGeneratedPassword = false;
    public boolean hasGeneratedPassword() { return hasGeneratedPassword; }
    
    /**
     * @return a string with the generated student password if applicable. 
     * 		   Use this to display the password if one is generated.
     */
    public String toPasswordString() {
    	if (hasGeneratedPassword) return campusUID + ":       " + password;
    	else return "";
    }
    // [end NAT P003]
	
	//Last,First,UID,section,ClassAcct,DirectoryID,Overall,Grade,
	//Aguilera, Jorge,107215970,0202,cs132035,jaguiler,0,,
	
	/**
	 * @param line - the line to parse
	 * @param delimiter - the delimiter between tokens
	 * @param genPassword - if true, then generate a password
	 * @throws IllegalStateException
	 */
	public StudentForUpload(String line, String delimiter, boolean genPassword)
	throws IllegalStateException
	{
	    String tokens[] = line.split(delimiter);
		
	    // remove leading/trailing whitespace
        lastname = tokens[0].replaceAll("^\\s+","").replaceAll("\\s+$","");
		if (lastname.equals(""))
			throw new IllegalStateException("lastname CANNOT be empty!");
		// remove leading/trailing whitespace
        firstname = tokens[1].replaceAll("^\\s+","").replaceAll("\\s+$","");;
		if (firstname.equals(""))
			throw new IllegalStateException("firstname CANNOT be empty!");
		
		employeeNum = tokens[2].replaceAll("\\s+", "");
		if (!employeeNum.matches("\\d+"))
		    throw new IllegalStateException(employeeNum +" doesn't look like an EmployeeNumber " +
		    		" in the EmployeeNumber field.  Are you sure that you're uploading a file " +
		    		" for the SubmitServer and not for grades.cs.umd.edu?");
		// TODO add a column to studentRegistration record for the section number
		section = tokens[3];
		
		cvsAccount = tokens[4].replaceAll("\\s+", "");
		if (cvsAccount.equals(""))
			throw new IllegalStateException("Class account CANNOT be empty!");
		campusUID= tokens[5].replaceAll("\\s+", "");
		if (campusUID.equals(""))
			throw new IllegalStateException("Campus UID CANNOT be empty!");
        if (tokens.length < 7 || tokens[6].replaceAll("\\s+", "").equals("")) {
    		// [NAT P003] Use a random password if authenticate option is so set
            if (genPassword) {
            	password = MarmosetUtilities.nextRandomPassword();
            	hasGeneratedPassword = true;
            }
            else
            	password=null;
    		// [end NAT P003]
        }
        else
            password=tokens[6].replaceAll("\\s+",""); // + "xxx";
	}
	
	public StudentForUpload(RequestParser parser)
	throws ClientRequestException
	{
		lastname = parser.getStringParameter("lastname");
		firstname = parser.getStringParameter("firstname");
		employeeNum = parser.getStringParameter("employeeNum");
		cvsAccount = parser.getStringParameter("cvsAccount");
		campusUID=parser.getStringParameter("campusUID");
		// XXX section is optional until I get the section integrated into 
		section = parser.getParameter("section");
		// Password can be null, but CANNOT be empty
        
        if (parser.getParameter("password")==null || "".equals(parser.getParameter("password")) ||
            "0".equals(parser.getParameter("password"))) {
         
        	// [NAT P003] look for authenticate field to determine if random password is needed
        	if ("generic".equals(parser.getParameter("authenticateType"))) {
        		password=MarmosetUtilities.nextRandomPassword();
        		hasGeneratedPassword = true;
        	}
        	else
        		password=null;
        	// [end NAT P003]
        }
        else
            password=parser.getParameter("password");
	}
    
    public Student makeStudent() {
        Student student=new Student();
        student.setCampusUID(campusUID);
        student.setEmployeeNum(employeeNum);
        student.setFirstname(firstname);
        student.setLastname(lastname);
        student.setPassword(password);
        return student;
    }
	
	public String toString()
	{
		return "campusUID: " +campusUID+
		", employeeNum: " +employeeNum+
		", firstname: " +firstname+
		", lastname: " +lastname+
		", classAccount: " +cvsAccount+
        ", password: " +password.replaceAll(".", "*");
	}

    /**
     * Register a student. Side Effect: If registration is unsuccessful, the function
     * turns off s.hasGeneratedPassword
     * @param conn
     * @param course
     * @param out
     * @param s
     * @throws SQLException
     */
    public static String registerStudent(Course course, StudentForUpload s, String accountType, Connection conn)
    throws SQLException
    {
    	Student student = Student.lookupByCampusUID(s.campusUID, conn);
    	if (student == null)
    	{
    	    // if we didn't find this student, create a new record
    	    student = new Student();
    	}
    	// set/update these fields
    	student.setCampusUID(s.campusUID);
    	student.setEmployeeNum(s.employeeNum);
    	student.setFirstname(s.firstname);
    	student.setLastname(s.lastname);
        student.setPassword(s.password);
    	
    	// either insert a new record or update an existing Student record
    	if (student.getStudentPK() == null)
    	    student.insert(conn);
    	else
    	    student.update(conn);
    	
    	StudentRegistration registration = StudentRegistration.lookupByStudentPKAndCoursePK(
    	        student.getStudentPK(),
    	        course.getCoursePK(),
    	        conn);
    	
    	if (registration == null)
    	{
    	    registration = new StudentRegistration();
    	    registration.setCoursePK(course.getCoursePK());
    	    registration.setCvsAccount(s.cvsAccount);
    	    registration.setStudentPK(student.getStudentPK());
    	    if ("TA".equalsIgnoreCase(accountType))
                registration.setInstructorCapability(StudentRegistration.READ_ONLY_CAPABILITY);
    	    else
    	        registration.setInstructorCapability(null);
    	    registration.setFirstname(s.firstname);
    	    registration.setLastname(s.lastname);
    	    registration.insert(conn);
    	    return "adding " +student.getFirstname()+ " "+
    		        student.getLastname()+ " with DirectoryID " +
    		        student.getCampusUID()+ " and cvsAccount " +
    		        registration.getCvsAccount()+ " and registered for " +
    		        course.getCourseName()+ " to the database";
    	}
    	else
    	{
    		// [NAT P003] Since registration was unsuccessful, turn off generated password
    		s.hasGeneratedPassword = false;
    		// [end NAT P003]
    		
    		return student.getFirstname() +" "+ student.getLastname() +
    				" with DirectoryID " +student.getCampusUID()+
    				" and cvsAccount " +registration.getCvsAccount()+ 
    				" is already registered for "+ course.getCourseName();
    	}
    }
}