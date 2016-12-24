package ca.uwaterloo.cs.submitServer;

import java.sql.Connection;
import java.sql.SQLException;

import ca.uwaterloo.cs.classlist.UWStudent;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;

/**
 * This is a utility class used for uploading students.
 *
 * @author ccchong
 */
public class UWStudentForUpload extends Object {

  /**
   * Construct a new student for upload.
   */
  private UWStudentForUpload() {
    super();
  }

  /**
   * Register the given student to the given course with specified 
   * account type.
   *
   * @param course the course to be registered into
   * @param s the student to be register into the course
   * @param conn the connection
   * @param truncate true if we need to truncate the user id
   * @param numTrim the number of character to be trimmed
   * @return a registration result object used to store the status of
   *         the student registration
   * @throws SQLException thrown when error from querying the database occurs
   */
  public static RegistrationResult registerStudent(Course course, UWStudent s,
      Connection conn, boolean truncate, int numTrim) 
        throws SQLException {

    // construct the user id to be uploaded
    String userId = s.getUserID();

    // truncate the user name if we are to truncate the user name
    if (truncate) {
      // check if the string actually need to be truncated before truncating
      if (userId.length() > numTrim) {
        userId = userId.substring(0, numTrim);
      }
    }

    Student student = Student.lookupByCampusUID(userId, conn);

    // If there is no such student in the database, create a new
    // student object representing a row in the database
    if (student == null) {
      student = new Student();
    }

    student.setCampusUID(userId); // quest login id
    student.setEmployeeNum(s.getID() + "");  // student number
    student.setFirstname(s.getFirstName());
    student.setLastname(s.getLastName());

    // Check if the record is new or if it is old
    if (student.getStudentPK() == null) {
      // initialize the password to student number
      student.setPassword(s.getID() + "");   
      student.insert(conn);
    } else {
      student.update(conn);
    }

    // Check for entry in the student registration table
    StudentRegistration registration = 
      StudentRegistration.lookupByStudentPKAndCoursePK(student.getStudentPK(), 
          course.getCoursePK(), conn);

    if (registration == null) {
      registration = new StudentRegistration();
    }
    registration.setCoursePK(course.getCoursePK());
    registration.setCvsAccount(userId);
    registration.setStudentPK(student.getStudentPK());
    registration.setInstructorCapability(null);
    registration.setFirstname(s.getFirstName());
    registration.setLastname(s.getLastName());

    if (registration.getStudentRegistrationPK() == null) {
      registration.insert(conn);
      return new RegistrationResult(s.getFirstName(), s.getLastName(),
          userId, RegistrationStatus.NewlyRegistrated);
    } else {
      registration.update(conn);
      return new RegistrationResult(s.getFirstName(), s.getLastName(),
          userId, RegistrationStatus.AlreadyRegistrated);
    }
  }
}
