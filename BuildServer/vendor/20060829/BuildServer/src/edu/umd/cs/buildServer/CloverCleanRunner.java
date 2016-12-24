/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on May 29, 2005
 *
 */
package edu.umd.cs.buildServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

/**
 * NOTE: This requires that clover.jar be copied into $ANT_HOME/lib
 * 
 * @author jspacco
 *
 */
public class CloverCleanRunner
{
    private File buildXml;
    private File cloverDB;
    private static Logger log;
    private static Logger getLog() {
    	if (log==null) {
    		log=Logger.getLogger(BuildServer.class);
    	}
    	return log;
    }
    
    public File getBuildXml() {
        return buildXml;
    }
    public void setBuildXml(File buildXml) {
        this.buildXml = buildXml;
    }
    public File getCloverDB() {
        return cloverDB;
    }
    public void setCloverDB(File cloverDB) {
        this.cloverDB = cloverDB;
    }

    public void execute()
    throws IOException    
    {
        try {
            String cmd[] = {"ant", "-Dinitstring="+cloverDB.getAbsolutePath(), "-f", buildXml.getAbsolutePath()};
            StringBuffer out = new StringBuffer();
            StringBuffer err = new StringBuffer();
            
            Process p = execAndDumpToStringBuffer(cmd, out, err);
            p.waitFor();
            getLog().trace("out for CloverCleanRunner: " +out);
            getLog().trace("err for CloverCleanRunner: " +err);
        } catch (InterruptedException e) {
            throw new IOException("CloverCleanRunner was interrupted!");
        }
    }
    
    public static void main(String[] args)
    throws IOException
    {
        CloverCleanRunner antRunner = new CloverCleanRunner();
        File buildFile = new File("/tmp/APROJECTS/cloverTest/BuildServer16293/build.xml");
        antRunner.setBuildXml(buildFile);
        File cloverDB = new File("/tmp/clover/myclover-db");
        antRunner.setCloverDB(cloverDB);

        antRunner.execute();
    }
    
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
                	getLog().error("Exception getting output stream from proc process:", e);
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
                	getLog().error("Exception getting output stream from proc process:", e);
                }
                
            }
        };
        t2.start();
        
        return proc;
    }
}
