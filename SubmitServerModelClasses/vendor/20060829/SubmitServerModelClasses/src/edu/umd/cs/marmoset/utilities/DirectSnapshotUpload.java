/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on May 26, 2005
 *
 */
package edu.umd.cs.marmoset.utilities;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.CopyUtils;
import org.apache.commons.io.IOUtils;

import edu.umd.cs.marmoset.modelClasses.Snapshot;


/**
 * @author jspacco
 *  
 */
public class DirectSnapshotUpload
{
    private static final String submitClientTool = "DirectUpload-0.1";
    
    //private static String semester; // = System.getProperty("semester", "Research");
    //private static String courseName; // = System.getProperty("courseName", "fall2004-132");
    //private static final String PREFIX = System.getProperty("PREFIX", "/fs/pugh/jspacco/Marmoset/Fall2005/extracted-433");
    
    private static void upload(File inputFile, 
        String[] projectArr,
        String courseName,
        String semester,
        String prefix,
        String dbUrl)
    throws SQLException, IOException
    {
        Connection conn=null;
        
        try {
            conn=DatabaseUtilities.getConnection(dbUrl);
            System.out.println("Got database connection from " +dbUrl);
            
            // read the file full of cvs accounts into a collection
            List<String> accountList = new LinkedList<String>();
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            while (true) {
                String cvsAccount = reader.readLine();
                if (cvsAccount == null) break;
                cvsAccount = cvsAccount.replaceAll("#.*", "");
                if ("".equals(cvsAccount))
                    continue;
                accountList.add(cvsAccount);
            }
            reader.close();
            
            // iterate through the list of projects
            for (String projectNumber : projectArr) {
                // iterate through each cvsAccount
                for (String cvsAccount : accountList) {
                    String location = prefix +"/"+ cvsAccount+ "/" +projectNumber;
                    File dir = new File(location);
                    if (!dir.isDirectory()) {
                        System.err.println(location + " is not a directory");
                        continue;
                    }
                    
                    File[] fileArr = dir.listFiles();
                    System.out.println(cvsAccount + " made " +fileArr.length+ " submissions for " +projectNumber);
                    int inserted=0;
                    for (File submission : fileArr) {
                        //System.out.println(file);
                        try {
                            String result=submit(submission, courseName, semester, conn);
                            if (result!=null) {
                                System.out.print(".");
                                inserted++;
                            }
                        } catch (SQLException e) {
                            System.out.println("SQLException! " +e);
                            //e.printStackTrace();
                        } catch (IOException e) {
                            System.out.println("IOException! " +e);
                            //e.printStackTrace();
                        }
                    }
                    System.out.println("\nInserted " +inserted);
                }
            }
        } finally {
            DatabaseUtilities.releaseConnection(conn);
        }
    }
    
    public static void main(String[] args)
    throws SQLException, IOException
    {
//      list of projects
        //String[] projectArr = {"proj3", "proj4", "proj5", "proj6", "proj7", "proj8"};
        //String[] projectArr = {"proj3", "proj4"};
        
        if (args.length < 5) {
            System.err.println("Usage: java DirectSnapshotUpload \n " +
                    "<participants file> \n" +
                    "<coureName> \n" +
                    "<semester> \n" +
                    "<directory w/ snapshots> \n" +
                    "<database url> \n" +
                    "<p1> [ <p2> ... <pN> ]");
            System.exit(1);
        }
        
        //File file=new File("/tmp/433-fall2005-participants.txt");
        File file=new File(args[0]);
        String courseName=args[1];
        String semester=args[2];
        String prefix=args[3];
        String dbUrl=args[4];
        
        String[] projectArr = new String[args.length-5];
        for (int ii=5; ii < args.length; ii++) {
            projectArr[ii-5] = args[ii];
        }
        
        upload(file, projectArr, courseName, semester, prefix, dbUrl);
    }
    
    /**
     * Upload a file directly into the DB. <p>
     * p1.cs132046.1126921978000.t1126907821390.zip
     * Filenames are of the format &lt;project&gt;.&lt;class_account&gt;.&lt;timestamp&gt;.&lt;submission_timestamp&gt;.zip
     * Note that submission_timestamp may be emtpy
     * @param file The file
     * @param conn Connection to the database
     * @return
     * @throws IOException
     * @throws SQLException
     */
    private static String submit(File file, String courseName, String semester, Connection conn)
    throws IOException, SQLException
    {
        String filename = file.getName();
        String[] tokens = filename.split("\\.");
        String projectNumber = tokens[0];
        String cvsAccount = tokens[1];
        long commitCvstag = Long.parseLong(tokens[2]);
        // cvsTagTimestamp can be null but *NOT* empty in the DB!
        String cvsTagTimestamp = tokens[3];
        if ("".equals(cvsTagTimestamp))
            cvsTagTimestamp = null;
        String zipExtension = tokens[4];
        assert zipExtension.equals(".zip");
        
        Timestamp commitTimestamp = new Timestamp(commitCvstag);
        
//      System.out.println(projectNumber +", "+
//      cvsAccount +", "+
//      commitCvstag +", "+
//      cvsTagTimestamp +", "+
//      extension);
        
        // copy the fileItem into a byte array
        FileInputStream fis = null;
        ByteArrayOutputStream bytes =null;
        try {
            fis = new FileInputStream(file);
            bytes = new ByteArrayOutputStream();
            CopyUtils.copy(fis, bytes);
            
            Snapshot snapshot = Snapshot.submitOneProject(
            cvsAccount,
            projectNumber,
            semester,
            commitCvstag,
            courseName,
            cvsTagTimestamp,
            submitClientTool,
            bytes.toByteArray(),
            conn);

            String result = "semester: " +semester+"\n"+
            "courseName: " +courseName+"\n"+
            "commitCvstag: " +commitCvstag+"\n"+
            "commitTimestamp: " +commitTimestamp+"\n"+
            "cvsTagTimestamp: " +cvsTagTimestamp+"\n"+
            "projectNumber: " +projectNumber+"\n"+
            "cvsAccount: " +cvsAccount;
            
            if (snapshot == null) {
                return "A duplicate!\n" + result;
            }
            return "Successfully inserted\n" +result;
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(bytes);
        }
    }
}
