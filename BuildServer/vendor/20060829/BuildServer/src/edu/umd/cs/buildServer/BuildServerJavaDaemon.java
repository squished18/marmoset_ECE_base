/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Nov 9, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.buildServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.httpclient.HttpException;

/**
 * BuildServerJavaDaemon
 
 * XXX This code is experimental and nowhere near complete.
 * 
 * Replaces the "runBuildServer" perl script and the associated shell scripts for killing
 * a BulidServer.
 * 
 * This class is designed to read configuration information and then start up buildServers
 * in separate threads.
 * 
 * Will be the main java class running and will expose
 * an MXBean interface for remote management of the buildServer threads.
 * 
 * Will have to handle 
 * 
 * @author jspacco
 */
public class BuildServerJavaDaemon extends BuildServer
{
    /**
     * 
     */
    public BuildServerJavaDaemon() {
        super();
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.BuildServer#initConfig()
     */
    @Override
    public void initConfig() throws IOException
    {
        String filename=System.getenv("HOME") +"/workspace/BuildServer/bs1.fromage.cs.umd.edu/config.properties";
        getConfig().load(new FileInputStream(filename));
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.BuildServer#prepareToExecute()
     */
    @Override
    protected void prepareToExecute()
    throws MissingConfigurationPropertyException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.BuildServer#getProjectSubmission()
     */
    @Override
    protected ProjectSubmission getProjectSubmission()
    throws MissingConfigurationPropertyException, IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.BuildServer#downloadSubmissionZipFile(edu.umd.cs.buildServer.ProjectSubmission)
     */
    @Override
    protected void downloadSubmissionZipFile(ProjectSubmission projectSubmission)
    throws IOException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.BuildServer#releaseConnection(edu.umd.cs.buildServer.ProjectSubmission)
     */
    @Override
    protected void releaseConnection(ProjectSubmission projectSubmission)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.BuildServer#downloadProjectJarFile(edu.umd.cs.buildServer.ProjectSubmission)
     */
    @Override
    protected void downloadProjectJarFile(ProjectSubmission projectSubmission)
    throws MissingConfigurationPropertyException, HttpException, IOException,
    BuilderException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see edu.umd.cs.buildServer.BuildServer#reportTestResults(edu.umd.cs.buildServer.ProjectSubmission)
     */
    @Override
    protected void reportTestResults(ProjectSubmission projectSubmission)
    throws MissingConfigurationPropertyException
    {
        // TODO Auto-generated method stub

    }
    
    public static void main2(String args[])
    throws IOException, MissingConfigurationPropertyException
    {
        BuildServerJavaDaemon buildServer = new BuildServerJavaDaemon();
        
        buildServer.initConfig();
        buildServer.configureBuildServerForMBeanManagement();
        
        //buildServer.executeServerLoop();
        
        getBuildServerLog().trace("Done.");
    }
    
    public static void main(String args[])
    throws IOException, MissingConfigurationPropertyException
    {
        Configuration config=new Configuration();
        String filename=System.getenv("HOME") +"/workspace/BuildServer/bs1.fromage.cs.umd.edu/config.properties";
        BuildServerConfiguration buildServerConfiguration=new BuildServerConfiguration();
        config.load(new FileInputStream(filename));
        
        try {
            buildServerConfiguration.loadAllProperties(config);
            // Get MBeanServer
            MBeanServer platformMBeanserver=ManagementFactory.getPlatformMBeanServer();
            // Register the BuildServerMBean
            ObjectName buildServerName = new ObjectName("edu.umd.cs.buildServer:id=BuildServerManager"); 
            platformMBeanserver.registerMBean(buildServerConfiguration,buildServerName);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        } catch (MBeanRegistrationException e) {
            throw new RuntimeException(e);
        } catch (NotCompliantMBeanException e) {
            throw new RuntimeException(e);
        } catch (InstanceAlreadyExistsException e) {
            throw new RuntimeException(e);
        } catch (MissingConfigurationPropertyException e) {
            //getLog().warn("Unable to configure (experimental) BuildServerConfiguration object");
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
