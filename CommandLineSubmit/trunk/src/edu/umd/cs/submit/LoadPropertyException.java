package edu.umd.cs.submit;

/**
 * This class represents an exception when loading a property occured.
 *
 * @author ccchong
 */
public class LoadPropertyException extends Exception {

  /**
   * Construct a new instance of load property exception.
   *
   * @param msg the error message
   */
  public LoadPropertyException(String msg) {
    super(msg);
  }
}
