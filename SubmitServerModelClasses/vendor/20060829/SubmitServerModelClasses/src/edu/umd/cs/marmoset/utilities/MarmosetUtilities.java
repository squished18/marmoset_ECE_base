/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Feb 24, 2006
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.utilities;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.CopyUtils;
import org.apache.log4j.Logger;

import edu.umd.cs.marmoset.modelClasses.Submission;

/**
 * MarmosetUtilities
 * @author jspacco
 */
public final class MarmosetUtilities
{
    private MarmosetUtilities() {}

    // [NAT P002]
    // Generage a random password
	private static SecureRandom rng = new SecureRandom();

	private static long nextRandomLong() {
		synchronized (rng) {
			return rng.nextLong();
		}
	}
	
	/**
	 * @return a random password
	 */
	public static String nextRandomPassword() {
		String s = Long.toHexString(nextRandomLong());
		return s.substring(s.length()-8);
	}
    // [end NAT P002]
	
    /**
     * Convert the string-representation of a stackTraceElement back to a StackTraceElement object.
     * @param stackTraceLine The string rep of a stackTraceElement.
     * @return The corresponding StackTraceElement object; null if a StackTraceElement cannot be
     *  reconstructed from the given string.
     */
    public static StackTraceElement parseStackTrace(String stackTraceLine)
    {
        // Try with source info
        String regexp="(.*)\\.([\\w<>]+)\\((\\w+\\.java):(\\d+)\\)";
        Pattern pattern=Pattern.compile(regexp);
        Matcher matcher=pattern.matcher(stackTraceLine);
        if (matcher.matches()) {
            String className=matcher.group(1);
            String methodName=matcher.group(2);
            String fileName=matcher.group(3);
            String s=matcher.group(4);
            int lineNumber=Integer.parseInt(s);
            return new StackTraceElement(className,methodName,fileName,lineNumber);
        }
        
        // Try without source info
        String regexpUnknown="(.*)\\.([\\w<>]+)\\(Unknown Source\\)";
        pattern=Pattern.compile(regexpUnknown);
        matcher=pattern.matcher(stackTraceLine);
        if (matcher.matches()) {
            String className=matcher.group(1);
            String methodName=matcher.group(2);
            String fileName=null;
            int lineNumber=-1;
            return new StackTraceElement(className,methodName,fileName,lineNumber);
        }
        
        // Try for native methods
        String regexpNative="(.*)\\.([\\w<>]+)\\(Native Method\\)";
        pattern=Pattern.compile(regexpNative);
        matcher=pattern.matcher(stackTraceLine);
        if (matcher.matches()) {
            String className=matcher.group(1);
            String methodName=matcher.group(2);
            String fileName=null;
            int lineNumber=-2;
            return new StackTraceElement(className,methodName,fileName,lineNumber);
        }
        
        //throw new IllegalStateException("Unable to parse stack trace: " +stackTraceLine);
        return null;
    }

    /**
     * @param conn
     * @param submissionPK
     * @param filename
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void fixSubmissionZipfile(Connection conn, String submissionPK, String filename) throws SQLException, FileNotFoundException, IOException
    {
        Submission submission=Submission.lookupBySubmissionPK(submissionPK,conn);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        FileInputStream fis=new FileInputStream(filename);
        CopyUtils.copy(fis,baos);
        byte[] bytes=baos.toByteArray();
        
        submission.setArchiveForUpload(bytes);
        submission.updateCachedArchive(bytes,conn);
    }

    /**
     * @param param
     * @return
     */
    public static boolean isTrue(String param)
    {
        param = param.toUpperCase();
        if (param.equals("TRUE") || param.equals("YES"))
            return true;
        return false;
    }

    /**
     * Uses the kill command to kill this process as a group leader with: <br>
     * kill -9 -&lt;pid&gt;
     * <p>
     * If kill -9 -&lt;pid&gt; fails, then this method will call 
     * @param process
     */
    public static void destroyProcessGroup(Process process, Logger log)
    {
        int pid=0;
        try {
            pid = getPid(process);
            
            log.debug("PID to be killed = " +pid);
            
            String command = "kill -9 -" +pid;
            
            String[] cmd = command.split("\\s+");
            
            Process kill = Runtime.getRuntime().exec(cmd);
            log.warn("Trying to kill the process group leader: " +command);
            kill.waitFor();
        } catch (IOException e) {
            // if we can't execute the kill command, then try to destroy the process
            log.warn("Unable to execute kill -9 -" +pid+ "; now calling process.destroy()");
       	} catch (InterruptedException e) {
            log.error("kill -9 -" +pid+ " process was interrupted!  Now calling process.destroy()");
        } catch (IllegalAccessException e) {
            log.error("Illegal field access to PID field; calling process.destroy()", e);
        } catch (NoSuchFieldException e) {
            log.error("Cannot find PID field; calling process.destroy()", e);
        } finally {
            // call process.destroy() whether or not "kill -9 -<pid>" worked
            // in order to maintain proper internal state
            process.destroy();
        }
    }

    /**
     * Uses reflection to extract the pid, a private field of the private class UNIXProcess.
     * This will fail on any non-Unix platform that doesn't use UNIXProcess.  It may
     * fail if the UNIXProcess class changes at all.  It may fail anyway for unpredictable
     * reasons.
     * @param process The process
     * @return the pid of this process
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static int getPid(Process process)
    throws NoSuchFieldException, IllegalAccessException
    {
        Class<? extends Process> processClass = process.getClass();
        Field pidField = processClass.getDeclaredField("pid");
        pidField.setAccessible(true);
        return pidField.getInt(process);
    }

    public static String commandToString(List<String> args) {
    	StringBuffer buf = new StringBuffer();
    	for (Iterator<String> i = args.iterator(); i.hasNext(); ) {
    		String arg = i.next();
    		if (buf.length() > 0)
    			buf.append(' ');
    		buf.append(arg);
    	}
    	return buf.toString();
    }

    public static String commandToString(String[] command)
    {
        StringBuffer buf=new StringBuffer();
        for (String s : command) {
            buf.append(s + " ");
        }
        return buf.toString();
    }

    public static boolean stringEquals(String s1, String s2) {
    	if (s1 != null) {
    		if (s2 != null)
    			return s1.equals(s2);
    		else
    			return false;
    	} else {
    		return s2 == null;
    	}
    }

    public static int hashString(String s) {
    	return s == null ? 0 : s.hashCode();
    }
}

