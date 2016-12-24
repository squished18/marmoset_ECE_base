package ca.uwaterloo.cs.classlist.parser;

/**
 * This class represents an exception occurs in parsing a class list.
 *
 * @author ccchong
 */
public class ClassListParseException extends Exception {

  /**
   * Construct a new class list parser exception with specified message.
   *
   * @param msg the error message
   */
  public ClassListParseException(String msg) {
    super(msg);
  }

  /**
   * Construct a new class list parser exception with specified message.
   *
   * @param msg the error message
   * @param e the root exception
   */
  public ClassListParseException(String msg, Exception e) {
    super(msg, e);
  }
}
