package ca.uwaterloo.cs.submitServer;

/**
 * This class represents the registration status of a student.  This object
 * is used for displaying pretty results with a JSP.
 *
 * @author ccchong
 */
public class RegistrationResult extends Object {
  private RegistrationStatus status;
  private String firstName;
  private String lastName;
  private String userId;

  /**
   * Construct a new Registration result object.
   *
   * @param firstName first name of the student
   * @param lastName last name of the student
   * @param userId the user id of the student
   * @param status the status of the student
   */
  public RegistrationResult(String firstName, String lastName, String userId,
      RegistrationStatus status) {
    super();
    this.firstName = firstName;
    this.lastName = lastName;
    this.userId = userId;
    this.status = status;
  }

  /**
   * Return the registration status of the student.
   *
   * @return the registration status of the student
   */
  public RegistrationStatus getStatus() {
    return this.status;
  }

  /**
   * Return the first name of the student.
   *
   * @return the first name of the student
   */
  public String getFirstName() {
    return this.firstName;
  }

  /**
   * Return the last name of the student.
   *
   * @return the last name of the student
   */
  public String getLastName() {
    return this.lastName;
  }

  /**
   * Return the user ID of the student.
   *
   * @return the user id of the student
   */
  public String getUserId() {
    return this.userId;
  }

  /**
   * Return a String representation of the Registration Result object.
   *
   * @return a string representation of registration result object.
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(this.getFirstName());
    buf.append(" ");
    buf.append(this.getLastName());
    buf.append(" (");
    buf.append(this.getUserId());
    buf.append(") is ");
    switch (this.getStatus()) {
      case AlreadyRegistrated:
        buf.append("already register in the course.");
        break;
      case NewlyRegistrated:
        buf.append("now register into the course.");
        break;
      default:
        throw new RuntimeException("Unsupported status for student: " + 
            this.getStatus());
    }
    return buf.toString();
  }
}
