/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Feb 17, 2005
 *
 */
package edu.umd.cs.submitServer.servlets;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.CopyUtils;

import edu.umd.cs.marmoset.modelClasses.IO;
import edu.umd.cs.marmoset.modelClasses.Snapshot;

/**
 * @author jspacco
 *
 */
public class PrintDiffFile extends SubmitServerServlet
{

    /**
     * The doGet method of the servlet. <br>
     *
     * This method is called when a form has its tag value method equals to get.
     * 
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        Connection conn=null;
        try {
            conn=getConnection();
            String submissionPK = request.getParameter("submissionPK");
            Snapshot snapshot = Snapshot.lookupBySubmissionPK(submissionPK, conn);
            if (snapshot == null)
                throw new ServletException("Snapshot " +submissionPK+ " does not exist!");
            if (snapshot.getDiffFile() == null) {
                // if the diff file is null, then compute it on the fly!
                Snapshot previous = Snapshot.lookupPreviousSnapshot(snapshot.getSubmissionPK(), conn);
                if (previous == null)
                    snapshot.setDiffFile("No previous snapshot!");
                else {
					// compute the diff between the old and new versions
					byte[] oldBytes = previous.downloadArchive(conn);
					byte[] newBytes = snapshot.downloadArchive(conn);
					String diff = diffTwoSubmissions(oldBytes, newBytes);
					snapshot.setDiffFile(diff);
				}
            }
            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();
            out.println(snapshot.getDiffFile());
            out.flush();
            out.close();
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }

    /**
     * @param oldBytes
     * @param newBytes
     * @return
     */
    private static String diffTwoSubmissions(byte[] oldBytes, byte[] newBytes)
    throws IOException
    {
        File tmp = new File("/tmp");
        File tempDir = new File(tmp, "temp." +System.currentTimeMillis());
        if (!tempDir.mkdirs())
            throw new RuntimeException("Unable to create directory " +tempDir.getAbsolutePath());
        tempDir.deleteOnExit();
        System.out.println(tempDir.getAbsolutePath());
        File oldDir = new File(tempDir, "old");
        oldDir.mkdir();
        File newDir = new File(tempDir, "new");
        newDir.mkdir();
        
        FileOutputStream oldFos = new FileOutputStream(new File(tempDir, "old.zip"));
        ByteArrayInputStream oldBais = new ByteArrayInputStream(oldBytes); 
        CopyUtils.copy(oldBais,oldFos);
        oldFos.close();
        
        FileOutputStream newFos = new FileOutputStream(new File(tempDir, "new.zip"));
        ByteArrayInputStream newBais = new ByteArrayInputStream(newBytes); 
        CopyUtils.copy(newBais,newFos);
        newFos.close();
        
        ZipInputStream oldZip = new ZipInputStream(new ByteArrayInputStream(oldBytes));
        unzipToDirectory(oldDir, oldZip);
        
        ZipInputStream newZip = new ZipInputStream(new ByteArrayInputStream(newBytes));
        unzipToDirectory(newDir, newZip);
        
        String[] diffCmd = {"diff",
                "-rbw",
                oldDir.getAbsolutePath(),
                newDir.getAbsolutePath()
        };

        StringBuffer stdOut = new StringBuffer();
        StringBuffer stdErr = new StringBuffer();
        Process p = IO.execAndDumpToStringBuffer(diffCmd, stdOut, stdErr);
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!tempDir.delete())
        {
            Process p2 = Runtime.getRuntime().exec(
                    new String[] { "/bin/rm", "-rf", tempDir.getAbsolutePath()});
        }
        return stdOut.toString();
    }
    
    /**
     * @param dir
     * @param zis
     */
    private static void unzipToDirectory(File dir, ZipInputStream zis)
    throws IOException
    {
        ZipEntry e;
        while((e=zis.getNextEntry())!= null) {
            if (e.isDirectory())
            {
                File newDir = new File(dir, e.getName());
                newDir.mkdirs();
            }
            else
            {
                //System.out.println("unzipping " + e.getName());
                File outfile = new File(dir, e.getName());
                FileOutputStream out = new FileOutputStream(outfile);
                byte [] b = new byte[512];
                int len = 0;
                while ( (len=zis.read(b))!= -1 ) {
                    out.write(b,0,len);
                }
                out.close();
            }
        }
        zis.close();
    }
}
