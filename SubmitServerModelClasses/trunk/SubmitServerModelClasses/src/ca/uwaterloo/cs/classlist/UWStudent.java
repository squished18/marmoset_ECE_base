package ca.uwaterloo.cs.classlist;

/**
 * This class represents a University of Waterloo student.
 *
 * @author ccchong
 */
public class UWStudent extends Object {

  // constants used by this class
  private static final int NUM_FIELDS_PARSE = 15;
  private static final String SPLIT_CHAR = ":";

  // data fields
  private int id;
  private String userId;
  private String firstName;
  private String lastName;
  private int lec;
  private String study;
  private String plan;
  private String group;
  private String year;
  private String degree;
  private String initials;
  private String status;
  private String time;
  private String sections;

  /**
   * Construct a new UW Student with the given string.  The string
   * is required to be in the format of the classlist file.
   *
   * @param input the inpust string in classlist file format
   */
  public UWStudent(String input) throws StudentParseException {
    super();
    String[] data = input.split(SPLIT_CHAR);
    if (data.length != NUM_FIELDS_PARSE) {
      throw new StudentParseException("Unexpected number of fields (" + 
          data.length + ") to parse");
    }
    try {
      this.id = Integer.parseInt(data[0]);
    } catch (NumberFormatException e) {
      throw new StudentParseException("Cannot parse the student id.", e);
    }
    this.userId = data[1];

    // Get the first name by chopping of the last name
    this.firstName = data[2].split(", ")[1];

    try {
      this.lec = Integer.parseInt(data[3]);
      int session = 0;
      try {
        session = Integer.parseInt(data[13]);
      } catch (NumberFormatException e) {
        throw new StudentParseException("Cannot parse session.", e);
      }
    } catch (NumberFormatException e) {
      throw new StudentParseException("Cannot parse the lecture section.", e);
    }

    this.study = data[4];
    this.plan = data[5];
    this.group = data[6];
    this.year = data[7];
    this.degree = data[8];
    this.initials = data[9];
    this.lastName = data[10];
    this.status = data[11];
    this.time = data[12];
    this.sections = data[14];
  }

  /**
   * Return the ID of the student.
   *
   * @return the id of the student
   */
  public int getID() {
    return this.id;
  }

  /**
   * Return the user ID of the student.
   *
   * @return the user ID of the student
   */
  public String getUserID() {
    return this.userId;
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
   * Return the lecture section of the student.
   *
   * @return the lecture section of the student
   */
  public int getLectureSection() {
    return this.lec;
  }

  /**
   * Return the study plan of the student.
   *
   * @return the study plan of the student
   */
  public String getStudyPlan() {
    return this.study;
  }

  /**
   * Return the plan of the student.
   *
   * @return the plan of the student
   */
  public String getPlan() {
    return this.plan;
  }

  /**
   * Return the group of the student.
   *
   * @return the group of the student
   */
  public String getGroup() {
    return this.group;
  }

  /**
   * Return the year of the student.
   *
   * @return the year of the student
   */
  public String getYear() {
    return this.year;
  }

  /**
   * Return the degree of the student.
   *
   * @return the degree of the student
   */
  public String getDegree() {
    return this.degree;
  }

  /**
   * Return the initials of the student.
   *
   * @return the initials of the student
   */
  public String getInitials() {
    return this.initials;
  }

  /**
   * Return the status of the student.
   *
   * @return the status of the student
   */
  public String getStatus() {
    return this.status;
  }

  /**
   * Return time status of the student.  Note that F refers to full time
   * and P refers to part time.
   *
   * @return the time status of the student
   */
  public String getTime() {
    return this.time;
  }

  /**
   * Return the sections of the student.
   *
   * @return the sections of the student
   */
  public String getSections() {
    return this.sections;
  }
}
