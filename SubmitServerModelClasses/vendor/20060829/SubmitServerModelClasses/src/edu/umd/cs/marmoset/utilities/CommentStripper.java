/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Jun 28, 2005
 *
 */
package edu.umd.cs.marmoset.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.CopyUtils;
import org.apache.commons.io.IOUtils;

import edu.umd.cs.marmoset.modelClasses.Snapshot;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.ZipFileAggregator;
import edu.umd.cs.marmoset.parser.JavaTokenScanner;
import edu.umd.cs.marmoset.parser.Token;
import edu.umd.cs.marmoset.parser.TokenScanner;
import edu.umd.cs.marmoset.parser.TokenType;

/**
 * @author jspacco
 *
 */
public class CommentStripper
{

    private static void usage(String msg)
    {
        if (msg != null && !msg.equals(""))
            System.err.println(msg);
        System.err.println("Usage: java CommentStripper <projectPK> <outputDir> [ <dbServerURL> ]");
        System.exit(1);
    }
    
    public static void main(String[] args)
    throws Exception
    {
        if (args.length < 2) {
            usage(null);
        }
        String projectPK=args[0];
        String dir = args[1];
        
        File outputDir = new File(dir);
        if (!outputDir.isDirectory()) {
            usage(outputDir.getAbsolutePath() +" does not exist.");
        }
        String dbServer = System.getProperty("db.server",
            "jdbc:mysql://marmoset2.umiacs.umd.edu:7306/submitserver");
        if (args.length > 2)
            dbServer = args[2];

        Connection conn=null;
        try {
            conn=DatabaseUtilities.getConnection(dbServer);
            ZipFileAggregator zipAggr = new ZipFileAggregator(new FileOutputStream(
                new File(outputDir, projectPK + ".zip")));

            List<Snapshot> submissionList = Snapshot.lookupAllCompilableByProjectPK(projectPK, conn);
            System.out.println("Found " +submissionList.size()+ " snapshots");
            for (Snapshot submission : submissionList) {
                byte[] strippedBytes = stripCommentsFromSubmissionArchive(submission.getSubmissionPK(), conn);
                zipAggr.addFileFromBytes(submission.getSubmissionPK(), strippedBytes);
            }
            zipAggr.close();
            System.out.println("Done!");
            
        } finally {
            DatabaseUtilities.releaseConnection(conn);
        }
	}
    /**
     * @param submissionPK The submissionPK of the submission to have its comments stripped.
     * @param dir Output directory.
     * @param conn The connection to the database.
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static byte[] stripCommentsFromSubmissionArchive(
        String submissionPK,
        Connection conn)
    throws SQLException, FileNotFoundException, IOException
    {
        ZipOutputStream zipOut=null;
        ZipInputStream zipIn=null;
        try {
            Submission submission = Submission.lookupBySubmissionPK(submissionPK, conn);
            zipIn = new ZipInputStream(new ByteArrayInputStream(submission.downloadArchive(conn)));
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            zipOut = new ZipOutputStream(baos);
            
            while (true) {
                ZipEntry entry = zipIn.getNextEntry();
                if (entry==null) break;
                if (entry.isDirectory()) continue;
                if (!entry.getName().endsWith(".java")) continue;
                ZipEntry strippedEntry = new ZipEntry(entry.getName());
                String result = stripCommentsFromJavaSourceFile(new InputStreamReader(zipIn));
                zipOut.putNextEntry(strippedEntry);
                CopyUtils.copy(new ByteArrayInputStream(result.getBytes()), zipOut);
                zipOut.closeEntry();
            }
            return baos.toByteArray();
        } finally {
            IOUtils.closeQuietly(zipIn);
            IOUtils.closeQuietly(zipOut);
        }
    }
    /**
     * @param reader
     * @throws IOException
     */
    private static String stripCommentsFromJavaSourceFile(Reader reader) throws IOException
    {
        StringBuffer buf=new StringBuffer();
        
        TokenScanner scanner = new JavaTokenScanner();
		Token token;
		while ((token = scanner.scan(reader)) != null) {
			if (token.getType().equals(TokenType.SINGLE_LINE_COMMENT)) {
			    buf.append("// comment deleted");
                //System.out.print("// comment deleted");
			} else if (token.getType().equals(TokenType.MULTI_LINE_COMMENT)) {
			    //System.out.println("// multiline comment should be deleted");
                buf.append(countLines(token.getLexeme()));
                //System.out.print(countLines(token.getLexeme()));
			} else {
			    buf.append(token.getLexeme());
                //System.out.print(token.getLexeme());
			}
		}
        return buf.toString();
    }
	private static String countLines(String text)
	{
	    StringBuffer result = new StringBuffer();
	    int index = text.indexOf('\n');
	    while (index != -1) {
	        result.append("//comment removed\n");
	        index = text.indexOf('\n', index+1);
	    }
	    return result.toString();
	}
}
