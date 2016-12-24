/*
 * Created on Jan 24, 2005
 *
 */
package edu.umd.cs.submit;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.protocol.Protocol;

/**
 * This class represents the main entry point of the command line
 * submit tool.
 *
 * @author pugh, ccchong
 */
public class CommandLineSubmit {

  public static final String VERSION = "0.1.1";
  public static final int HTTP_TIMEOUT = 
    Integer.getInteger("HTTP_TIMEOUT", 30).intValue()*1000;
  static boolean debug = false;
  private static final boolean SUBMIT_SINGLE_FILE = true;
  private static final int MAX_SUBMIT_TRIES = 3;

  // Denote the usages, should change this so that it is course 
  // independent
  /*
  private static final String USAGE_STRING = 
    "Usage: 241submit <-u userid> projectName <fileName>" + 
    "\nUse the -u userid option if your marmoset id is different from " + 
    "your unix userid";
    */

  private static final String USAGE_STRING = 
    "Usage: marmoset_submit projectName <fileName>";

  private static final String ERROR_STRING =
    "Fatal error while Submitting to marmoset!  Exiting...";

  /**
   * Print the usage to the given print stream.
   *
   * @param out the output stream to output
   */
  private static void printUsage(PrintStream out) {
    out.println(USAGE_STRING);
  }

  /**
   * Print the usage to standard error.
   */
  private static void printUsage() {
    printUsage(System.err);
  }

  /**
   * Print the usage and exit the program.
   */
  private static void printUsageAndExit() {
    printUsage();
    System.exit(1);
  }
   
  /**
   * arg[0] should be the project name
   * arg[1] to arg[N] all filenames
   */
  public static void main(String[] args) throws Exception {
    // See marmoset_submit.c for list of parameters
    if (args.length != 6) {
      System.err.println(ERROR_STRING);
      System.exit(1);
    }

    String course   = args[0];
    String term     = args[1] + " " + args[2];
    String userName = args[3];
    int arrayIndex  = 4;

    Submission submit = new Submission();
    submit.setCourseName(course);
    submit.setSemester(term);
    submit.setUserName(userName);
    submit.setProjectNumber(args[arrayIndex]);
    arrayIndex++;

/*
    String fileSeparator = System.getProperty("file.separator");
    if (fileSeparator == null) {
      fileSeparator = "/";
    }

    // hack to bail out when there is more than one file
    if (SUBMIT_SINGLE_FILE) {
      if (args.length != 2 && args.length != 4) {
        printUsageAndExit();
      }
    }

    // read args[0] to args[N] and convert to files
    for (int i = arrayIndex; i < args.length; i++) {
      if (args[i].indexOf(fileSeparator) != -1) {
        System.err.println("ERROR: Submit accepts files without " + 
            fileSeparator + " in their name.");
        System.exit(1);
      }
*/

      File f = new File(args[arrayIndex]);

      // Check if we can read the file
      if (!f.canRead()) {
        System.err.println("ERROR: Dont have access or can't read file: " + f);
        System.exit(1);
      }

      // Check if the file is actually a file
      if (!f.isFile()) {
        System.err.println("ERROR: Not a file:" + f);
        System.exit(1);
      }

      // Check the file if it is .zip or .jar
      String extension = f.getName();
      /*
      if ((!extension.endsWith(".zip")) && (!extension.endsWith(".jar"))) {
        System.err.println("ERROR: Not a .zip or .jar file: " + f);
	System.exit(1);
      }*/

      // If debugging, send out a debug message
      if(debug) {
        System.err.println("Adding file: " + args[arrayIndex]);
      }

      // add to submission
      submit.addFile(f);
//    }

    // Check if the file argument is not the -u option.  If it is not
    // get the user name that the user is currently logged in.
    /*
    if (!args[0].equals("-u")) {
    }*/

    // retrieve the user name since marmoset will always use the 8 character
    // unix ID
    // username = System.getProperty("user.name");
    
    // DEBUG hard coded user name for testing
    //    username = "nanaeem";
/*
    while (username == null) {
      System.err.println("Please enter your Submit Server User ID " + 
          "and Password");
      System.err.print("User ID: ");
      System.err.flush();
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      username = in.readLine();
    }
    submit.setUserName(username);    
*/
             
    System.err.println("Submitting project " + submit.getProjectNumber() + 
        " for " + submit.getCourseName() + ", " + submit.getSemester() + 
	" for user: " + submit.getUserName());
        
    //Negotiate password
    Properties userProps = null;

    // running the negotiate password
    userProps = negotiatePassword(submit);

    if (userProps == null) {
      System.err.println(ERROR_STRING);
      System.exit(1);
    }

    /*
    int times =0;
    while (true) {

      times++;
            
      if(userProps != null) {
        break;
      }

      if (times >= 3) {
        System.err.println("Exceeded attempts.");
        System.exit(1);
      }
            
      System.err.println("Unable to negotiate one time password");
      System.err.print("Enter userid: ");
      System.err.flush();
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      username = in.readLine();
      submit.setUserName(username);
    } */

    if (debug) {
      System.err.println("Password negotiation successful");
    }
        
    String cvsAccount = userProps.getProperty("cvsAccount");
    String oneTimePassword = userProps.getProperty("oneTimePassword");
        
    // remove next two lines when above is uncomented
    // hard coded invalid account and password for testing
    //     String cvsAccount = "nomair";
    //   String oneTimePassword = "slimShady";

    // gather submit information
    String submitURL = submit.getSubmitURL();       
    String courseName = submit.getCourseName();
    String semester = submit.getSemester();
    String projectNumber = submit.getProjectNumber();

    // create submit properties
    Properties submitProps = new Properties();
    submitProps.setProperty("submitURL", submitURL);
    submitProps.setProperty("courseName", courseName);
    submitProps.setProperty("semester", semester);
    submitProps.setProperty("projectNumber", projectNumber);
    submitProps.setProperty("cvsAccount", cvsAccount);
    submitProps.setProperty("oneTimePassword", oneTimePassword);
        
    // retireve the assembled zip file as a byte array
    // Old code: ByteArrayOutputStream bytes = submit.getZippedByteArray();
    ByteArrayOutputStream bytes = submit.getByteArray();
    if (debug) {
      System.err.println("Created zip files. Submitting");
    }

    // Need to do SSL handshake here, since SSL could be use with
    // other protocol
    Protocol easyhttps = new Protocol("https", 
        new EasySSLProtocolSocketFactory(), 443);
    Protocol.registerProtocol("easyhttps", easyhttps);

    trySubmit(submitProps, bytes, extension, MAX_SUBMIT_TRIES);
  }

  private static void trySubmit(Properties submitProps, ByteArrayOutputStream bytes, String extension, int tries) throws IOException {
    MultipartPostMethod filePost = 
      new MultipartPostMethod(submitProps.getProperty("submitURL"));
        
    // add properties
    for (Iterator submitProperties = submitProps.entrySet().iterator(); 
        submitProperties.hasNext();) {
      Map.Entry e = (Map.Entry) submitProperties.next();
      String key = (String) e.getKey();
      String value = (String) e.getValue();
      if (!key.equals("submitURL")){
        filePost.addParameter(key, value);
        if (debug) {
          System.err.println("Adding entry " + key + " with value: " + value);
        }
      }
    }
    filePost.addParameter("submitClientTool", "CommandLineTool");
    filePost.addParameter("submitClientVersion", VERSION);
    byte[] allInput = bytes.toByteArray();
    filePost.addPart(new FilePart("submittedFiles", 
          new ByteArrayPartSource(extension, allInput)));

    // prepare httpclient
    HttpClient client = new HttpClient();
    client.setConnectionTimeout(HTTP_TIMEOUT);
    int status = client.executeMethod(filePost);
    if (status != HttpStatus.SC_OK) {
      if(tries <= 0){
        System.err.println(filePost.getResponseBodyAsString());
        System.exit(1);
      }

      trySubmit(submitProps, bytes, extension, tries-1);
    }else{
      System.err.println(filePost.getResponseBodyAsString());
    }
  }

  private static Properties negotiatePassword(Submission submit) 
    throws Exception { 
    String url = submit.getPasswordURL();

    PostMethod post = new PostMethod(url);
    post.addParameter("campusUID", submit.getUserName());

    /*
    String userPassword = ReadPassword.readConsoleSecure("Enter password for " +
        submit.getUserName()+": ");
    post.addParameter("uidPassword", userPassword);
        */
    // since the new marmoset just use HTTP authentication in a sense,
    // Not needed and leave empty
    post.addParameter("uidPassword", "xxx");

    post.addParameter("courseName", submit.getCourseName());
    post.addParameter("semester", submit.getSemester());
    post.addParameter("projectNumber", submit.getProjectNumber());

    HttpClient client = new HttpClient();
    client.setConnectionTimeout(HTTP_TIMEOUT);

    if (debug) {
      System.err.println("Preparing to execute method");
    }
    int status = client.executeMethod(post);
    if (debug) {
      System.err.println("Post finished with status: " + status);
    }

/*    	if (status != HttpStatus.SC_OK) {
    		System.out.println("Unable to negotiate one-time password for user: "+submit.getUserName());
    		//throw new HttpException("Unable to negotiate one-time password with the server: "+ post.getResponseBodyAsString());
    	}
*/
    InputStream inputStream = post.getResponseBodyAsStream();  
    Properties userProps = new Properties();
    userProps.load(inputStream);

    if (userProps.getProperty("cvsAccount") != null && 
        userProps.getProperty("oneTimePassword") != null) {
      return userProps;
    } else {
      return null;
    }
  }
}
