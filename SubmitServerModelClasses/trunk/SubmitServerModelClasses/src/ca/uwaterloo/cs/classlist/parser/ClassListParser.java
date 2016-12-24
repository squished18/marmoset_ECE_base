package ca.uwaterloo.cs.classlist.parser;

import ca.uwaterloo.cs.classlist.UWStudent;
import ca.uwaterloo.cs.classlist.StudentParseException;

import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Scanner;
import java.util.List;
import java.util.LinkedList;

/**
 * This class represents a class list parser.
 *
 * @author ccchong
 */
public class ClassListParser extends Object {

  /**
   * Construct a new instance of class list parser.
   */
  private ClassListParser() {
    super();
  }

  /**
   * Return a new class list parser.
   *
   * @return a new class list parser
   */
  public static ClassListParser newInstance() {
    return new ClassListParser();
  }

  public List<UWStudent> parse(InputStream in) throws ClassListParseException {
    if (in == null) {
      throw new ClassListParseException("The input stream cannot be null!");
    }
    return this.parse(new InputStreamReader(in));
  }

  /**
   * Parses the given reader into a class list.
   *
   * @param in the input reader
   * @throws ClassListParseException thrown when an error in parsing occur
   * @return the class list parsed from the data in the reader
   */
  public List<UWStudent> parse(Reader in) throws ClassListParseException {
    if (in == null) {
      throw new ClassListParseException("The input stream cannot be null!");
    }

    Scanner input = new Scanner(in);
    List<UWStudent> result = new LinkedList<UWStudent>();
    while (input.hasNextLine()) {
      String line = input.nextLine();
      String cleanLine = line.trim();
      if (cleanLine.length() == 0) {
        // skip
      } else if (cleanLine.charAt(0) == '#') {
        // skip comment
      } else {
        try {
          UWStudent student = new UWStudent(cleanLine);
          result.add(student);
        } catch (StudentParseException e) {
          throw new ClassListParseException("Cannot parse student", e);
        }
      }
    }
    return result;
  }
}
