package ca.uwaterloo.cs.classlist;

/**
 * This class represents an exception occuring during a parsing of the
 * student's data.
 *
 * @author ccchong
 */
public class StudentParseException extends Exception {

  /**
   * Construct a new student parse exception with the specified message.
   *
   * @param msg the error message
   */
  public StudentParseException(String msg) {
    super(msg);
  }

  /**
   * Construct a new student parse exception with the specified message
   * and the exception causing the problem.
   *
   * @param msg the error message
   * @param e the original exception
   */
  public StudentParseException(String msg, Exception e) {
    super(msg, e);
  }
}
