/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Mar 25, 2005
 *
 */
package edu.umd.cs.buildServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestProperties;

/**
 * @author jspacco
 *
 */
public class BuildServerUtilities
{
    static Process execAndDumpToStringBuffer(String cmd[], final StringBuffer out, final StringBuffer err) throws IOException
    {
        final Process proc = Runtime.getRuntime().exec(cmd);
        
        Thread t = new Thread()
        { 
            public void run()
            {
                try
                {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    String line;
                    while ( (line = reader.readLine()) != null )
                    {
                        out.append(line +"\n");
                    }
                }
                catch (IOException e) {
                    System.err.println("Exception getting output stream from proc process:" + e);
                }
            }
        };
        t.start();
        
        Thread t2 = new Thread()
        { 
            public void run()
            {
                try 
                {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                    String line;
                    while ( (line = reader.readLine()) != null )
                    {
                        err.append(line +"\n");
                    }
                }
                catch (IOException e) {
                    System.err.println("Exception getting output stream from proc process:" + e);
                }
                
            }
        };
        t2.start();
        
        return proc;
    }
    
    public static void main(String args[])
    throws Exception
    {
        File dir=new File("/fs/fromage/jspacco/workspace/BuildServer/bs1.fromage.cs.umd.edu/build/obj");
        TestProperties testProperties=new TestProperties();
        testProperties.load(new File("/fs/fromage/jspacco/workspace/BuildServer/bs1.fromage.cs.umd.edu/testfiles/test.properties"));
        List<File> list=listNonCloverClassFilesInDirectory(dir,testProperties);
        for (File f : list) {
            System.out.println(f);
        }
    }

    static List<File> listNonCloverClassFilesInDirectory(File dir, final TestProperties testProperties)
    {
        List<File> results = new ArrayList<File>();
        listDirContents(dir, new FileFilter() {
            public boolean accept(File file)
            {
                String absolutePath = file.getAbsolutePath();
				if (CodeCoverageResults.isJUnitTestSuite(absolutePath)) return false;
                // Skip the classfiles containing JUnit tests since covering test cases isn't interesting
                for (String testClass : TestOutcome.getDynamicTestTypes()) {
                    String className=testProperties.getTestClass(testClass);
                    if (className!=null) {
                        // Make the classname look like a path
                        className=new File(className).getName().replace('.',File.separatorChar);
 
                        // If this is one of the test classes, then skip it!
                        if (absolutePath.contains(className))
                            return false;
                    }
                }
                
                // XXX MAJOR HACK: have to figure out what to exclude from properties file
                if (file.getName().contains("TestingSupport"))
                    return false;
                // Skip anything that looks like a clover classfile
                if (file.getName().matches(".*class") && !file.getName().contains("CLOVER"))
                    return true;
                return false;
            }
        }, results);
        return results;
    }
    
    static List<File> listClassFilesInDirectory(File dir)
    {
        List<File> results = new ArrayList<File>();
        listDirContents(dir, new FileFilter() {
            public boolean accept(File file)
            {
                if (file.getName().matches(".*class"))
	                return true;
	            return false;
            }
        }, results);
        return results;
    }
    
    /**
     * Appends all the files in a given directory or its subdirectories that match
     * the given filter. 
     * @param dir the directory
     * @param filter the filter determines whether a file should be added to the results list
     * @param results the list to append the matching files to
     */
    private static void listDirContents(File dir, FileFilter filter, List<File> results)
    {
        File[] fileArr = dir.listFiles();
        for (int ii=0; ii < fileArr.length; ii++)
        {
            File file = fileArr[ii];
            if (file.isDirectory())
            {
                listDirContents(file, filter, results);
            }
            if (file.isFile())
            {
                if (filter.accept(file))
                    results.add(file);
            }
        }
    }
    

    
    public static File createTempDirectory()
    throws IOException
    {
        File tmpDir = File.createTempFile("prefix", "suffix");
        tmpDir.delete();
        tmpDir.mkdirs();
        return tmpDir;
    }
    
    public static List<File> listDirContents(File dir)
    {
    	List<File> result = new LinkedList<File>();
    	File[] files = dir.listFiles();
    	for (File file : files) {
    		if (file.isDirectory()) {
    			listDirContentsHelper(file, result);
    		} else {
    			result.add(file);
    		}
    	}
    	return result;
    }
    private static void listDirContentsHelper(File dir, List<File> result)
    {
    	File[] files = dir.listFiles();
    	if (files != null) {
    		for (File file : files) {
    			if (file.isDirectory()) {
    				listDirContentsHelper(file, result);
    			} else {
    				result.add(file);
    			}
    		}
    	}
    }
}
