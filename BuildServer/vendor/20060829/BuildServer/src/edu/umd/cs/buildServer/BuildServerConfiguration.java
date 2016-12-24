/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Nov 9, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.buildServer;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * BuildServerConfiguration
 * Contains all information passed to the BuildServer through the config.properties
 * file.  Ultimately this class should be created directly from the config.properties file,
 * replacing the Configuration class completely.
 * @author jspacco
 */
public class BuildServerConfiguration implements BuildServerConfigurationMBean
{
    private static final String BUILD_SERVER_HOME = "build.server.home";
    private static final String FINDBUGS_HOME = "findbugs.home";
    private static final String PMD_HOME = "pmd.home";
    
    private String javaHome;
    private String buildServerHome;
    private String buildServerRoot;
    private String hostname;
    private String semester;
    private String[] supportedCoursesArr;
    private List<String> supportedCourseList;
    private String supportedCourses;
    
    private String submitServerProtocol;
    private String submitServerHost;
    private String port;
    private String serverPassword;

    private String submitServerRequestprojectPath="/buildServer/RequestSubmission";
    private String submitServerGetprojectjarPath="/buildServer/GetProjectJarfile";
    private String submitServerReporttestresultsPath="/buildServer/ReportTestOutcomes";
    private String submitServerHandlebuildserverlogmessagePath;

    private String buildDirectory;
    private String testFilesDirectory;
    private String jarCacheDirectory;
    private String logDirectory;
    
    private boolean debugVerbose=true;
    private boolean doNotLoop=true;
    private boolean debugJavaSecurity=false;
    
    private int numServerLoopIterations;
    
    private String cloverDBPath;
    
    public BuildServerConfiguration() {}

    public void loadAllProperties(Configuration config)
    throws MissingConfigurationPropertyException
    {
        // Basic configuration information about a buildServer.
        // XXX Should I create my own directories or require that they be specified?
        // The only thing I really need are the logfiles.  Those really could go into
        // some kind of central place.  It would be nice if one big piece of java code managed
        // all of its affairs.
        setJavaHome(config.getRequiredProperty("java.home"));
        setBuildServerHome(config.getRequiredProperty(BUILD_SERVER_HOME));
        setBuildServerRoot(config.getRequiredProperty(ConfigurationKeys.BUILDSERVER_ROOT));
        
        // Semester and course
        setSemester(config.getRequiredProperty(ConfigurationKeys.SEMESTER));
        setSupportedCourses(config.getRequiredProperty(ConfigurationKeys.SUPPORTED_COURSE_LIST));
        
        // Protocol, hostname, port and password
        // TODO Manage password more securely
        setSubmitServerProtocol(config.getRequiredProperty(ConfigurationKeys.SUBMIT_SERVER_PROTOCOL));
        setSubmitServerHost(config.getRequiredProperty(ConfigurationKeys.SUBMIT_SERVER_HOST));
        setPort(config.getRequiredProperty(ConfigurationKeys.SUBMIT_SERVER_PORT));
        setServerPassword(config.getRequiredProperty(ConfigurationKeys.SUBMIT_SERVER_PASSWORD));

        // XXX These properties have been standardized for quite some time and haven't changed.
        // Thus we use the defaults unless something else is requested.
        if (config.hasProperty(ConfigurationKeys.SUBMIT_SERVER_REQUESTPROJECT_PATH))
            setSubmitServerRequestprojectPath(config.getRequiredProperty(ConfigurationKeys.SUBMIT_SERVER_REQUESTPROJECT_PATH));
        if (config.hasProperty(ConfigurationKeys.SUBMIT_SERVER_GETPROJECTJAR_PATH))
            setSubmitServerGetprojectjarPath(config.getRequiredProperty(ConfigurationKeys.SUBMIT_SERVER_GETPROJECTJAR_PATH));
        if (config.hasProperty(ConfigurationKeys.SUBMIT_SERVER_REPORTTESTRESULTS_PATH))
            setSubmitServerGetprojectjarPath(config.getRequiredProperty(ConfigurationKeys.SUBMIT_SERVER_REPORTTESTRESULTS_PATH));
        // If hanldeBuildServerLogMessages is empty, then we're not logging back to the submitServer
        // So this property must be read; we can't just use the default
        setSubmitServerGetprojectjarPath(config.getRequiredProperty(ConfigurationKeys.SUBMIT_SERVER_HANDLEBUILDSERVERLOGMESSAGE_PATH));
        
        /*
         build.directory=${build.server.home}/build
         test.files.directory=${build.server.home}/testfiles
         jar.cache.directory=${build.server.home}/jarcache
         log.directory=console
         #log.directory=${build.server.home}/log
         */
        setBuildDirectory(config.getRequiredProperty(ConfigurationKeys.BUILD_DIRECTORY));
        setTestFilesDirectory(config.getRequiredProperty(ConfigurationKeys.TEST_FILES_DIRECTORY));
        setJarCacheDirectory(config.getRequiredProperty(ConfigurationKeys.JAR_CACHE_DIRECTORY));
        setLogDirectory(config.getRequiredProperty(ConfigurationKeys.LOG_DIRECTORY));
        
        setDebugVerbose(config.getOptionalBooleanProperty(ConfigurationKeys.DEBUG_VERBOSE));
        setDebugJavaSecurity(config.getOptionalBooleanProperty(ConfigurationKeys.DEBUG_SECURITY));
        setDoNotLoop(config.getBooleanProperty(ConfigurationKeys.DEBUG_DO_NOT_LOOP));
    }

    /**
     * @return Returns the buildServerHome.
     */
    public String getBuildServerHome() {
        return buildServerHome;
    }
    /**
     * @param buildServerHome The buildServerHome to set.
     */
    public void setBuildServerHome(String buildServerHome) {
        this.buildServerHome = buildServerHome;
    }
    /**
     * @return Returns the buildServerRoot.
     */
    public String getBuildServerRoot() {
        return buildServerRoot;
    }
    /**
     * @param buildServerRoot The buildServerRoot to set.
     */
    public void setBuildServerRoot(String buildServerRoot) {
        this.buildServerRoot = buildServerRoot;
    }
    /**
     * @return Returns the javaHome.
     */
    public String getJavaHome() {
        return javaHome;
    }
    /**
     * @param javaHome The javaHome to set.
     */
    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    /**
     * @return Returns the buildDirectory.
     */
    public String getBuildDirectory()
    {
        return buildDirectory;
    }

    /**
     * @param buildDirectory The buildDirectory to set.
     */
    public void setBuildDirectory(String buildDirectory)
    {
        this.buildDirectory = buildDirectory;
    }

    /**
     * @return Returns the debugDonotloop.
     */
    public boolean getDoNotLoop()
    {
        return doNotLoop;
    }

    /**
     * @param debugDonotloop The debugDonotloop to set.
     */
    public void setDoNotLoop(boolean debugDonotLoop)
    {
        this.doNotLoop = debugDonotLoop;
    }

    /**
     * @return Returns the debugJavaSecurity.
     */
    public boolean isDebugJavaSecurity()
    {
        return debugJavaSecurity;
    }

    /**
     * @param debugJavaSecurity The debugJavaSecurity to set.
     */
    public void setDebugJavaSecurity(boolean debugJavaSecurity)
    {
        this.debugJavaSecurity = debugJavaSecurity;
    }

    /**
     * @return Returns the debugVerbose.
     */
    public boolean isDebugVerbose()
    {
        return debugVerbose;
    }

    /**
     * @param debugVerbose The debugVerbose to set.
     */
    public void setDebugVerbose(boolean debugVerbose)
    {
        this.debugVerbose = debugVerbose;
    }

    /**
     * @return Returns the hostname.
     */
    public String getHostname()
    {
        return hostname;
    }

    /**
     * @param hostname The hostname to set.
     */
    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }

    /**
     * @return Returns the jarCacheDirectory.
     */
    public String getJarCacheDirectory()
    {
        return jarCacheDirectory;
    }

    /**
     * @param jarCacheDirectory The jarCacheDirectory to set.
     */
    public void setJarCacheDirectory(String jarCacheDirectory)
    {
        this.jarCacheDirectory = jarCacheDirectory;
    }

    /**
     * @return Returns the logDirectory.
     */
    public String getLogDirectory()
    {
        return logDirectory;
    }

    /**
     * @param logDirectory The logDirectory to set.
     */
    public void setLogDirectory(String logDirectory)
    {
        this.logDirectory = logDirectory;
    }

    /**
     * @return Returns the port.
     */
    public String getPort()
    {
        return port;
    }

    /**
     * @param port The port to set.
     */
    public void setPort(String port)
    {
        this.port = port;
    }

    /**
     * @return Returns the semester.
     */
    public String getSemester()
    {
        return semester;
    }

    /**
     * @param semester The semester to set.
     */
    public void setSemester(String semester)
    {
        this.semester = semester;
    }

    /**
     * @return Returns the serverPassword.
     */
    public String getServerPassword()
    {
        return serverPassword;
    }

    /**
     * @param serverPassword The serverPassword to set.
     */
    public void setServerPassword(String serverPassword)
    {
        this.serverPassword = serverPassword;
    }

    /**
     * @return Returns the submitServerGetprojectjarPath.
     */
    public String getSubmitServerGetprojectjarPath()
    {
        return submitServerGetprojectjarPath;
    }

    /**
     * @param submitServerGetprojectjarPath The submitServerGetprojectjarPath to set.
     */
    public void setSubmitServerGetprojectjarPath(
        String submitServerGetprojectjarPath)
    {
        this.submitServerGetprojectjarPath = submitServerGetprojectjarPath;
    }

    /**
     * @return Returns the submitServerHandlebuildserverlogmessagePath.
     */
    public String getSubmitServerHandlebuildserverlogmessagePath()
    {
        return submitServerHandlebuildserverlogmessagePath;
    }

    /**
     * @param submitServerHandlebuildserverlogmessagePath The submitServerHandlebuildserverlogmessagePath to set.
     */
    public void setSubmitServerHandlebuildserverlogmessagePath(
        String submitServerHandlebuildserverlogmessagePath)
    {
        this.submitServerHandlebuildserverlogmessagePath = submitServerHandlebuildserverlogmessagePath;
    }

    /**
     * @return Returns the submitServerHost.
     */
    public String getSubmitServerHost()
    {
        return submitServerHost;
    }

    /**
     * @param submitServerHost The submitServerHost to set.
     */
    public void setSubmitServerHost(String submitServerHost)
    {
        this.submitServerHost = submitServerHost;
    }

    /**
     * @return Returns the submitServerProtocol.
     */
    public String getSubmitServerProtocol()
    {
        return submitServerProtocol;
    }

    /**
     * @param submitServerProtocol The submitServerProtocol to set.
     */
    public void setSubmitServerProtocol(String submitServerProtocol)
    {
        this.submitServerProtocol = submitServerProtocol;
    }

    /**
     * @return Returns the submitServerReporttestresultsPath.
     */
    public String getSubmitServerReporttestresultsPath()
    {
        return submitServerReporttestresultsPath;
    }

    /**
     * @param submitServerReporttestresultsPath The submitServerReporttestresultsPath to set.
     */
    public void setSubmitServerReporttestresultsPath(
        String submitServerReporttestresultsPath)
    {
        this.submitServerReporttestresultsPath = submitServerReporttestresultsPath;
    }

    /**
     * @return Returns the submitServerRequestprojectPath.
     */
    public String getSubmitServerRequestprojectPath()
    {
        return submitServerRequestprojectPath;
    }

    /**
     * @param submitServerRequestprojectPath The submitServerRequestprojectPath to set.
     */
    public void setSubmitServerRequestprojectPath(
        String submitServerRequestprojectPath)
    {
        this.submitServerRequestprojectPath = submitServerRequestprojectPath;
    }

    /**
     * @return Returns the supportedCourses.
     */
    public String getSupportedCourses()
    {
        return supportedCourses;
    }

    /**
     * @param supportedCourses The supportedCourses to set.
     */
    public void setSupportedCourses(String supportedCourses)
    {
        supportedCourseList=new LinkedList<String>();
        this.supportedCourses = supportedCourses;
        StringTokenizer tokenizer = new StringTokenizer(supportedCourses);
        
        while (tokenizer.hasMoreTokens()) {
            String token=tokenizer.nextToken();
            supportedCourseList.add(token);
        }
    }
    public List<String> getSupportedCoursesList() {
        return supportedCourseList;
    }
    /**
     * @return Returns the testFilesDirectory.
     */
    public String getTestFilesDirectory() {
        return testFilesDirectory;
    }
    /**
     * @param testFilesDirectory The testFilesDirectory to set.
     */
    public void setTestFilesDirectory(String testFilesDirectory) {
        this.testFilesDirectory = testFilesDirectory;
    }

    public String getCloverDBPath()
    {
        return cloverDBPath;
    }

    public void setCloverDBPath(String cloverDBPath)
    {
        this.cloverDBPath = cloverDBPath;
    }

    public int getNumServerLoopIterations()
    {
        return numServerLoopIterations;
    }

    public void setNumServerLoopIterations(int numServerLoopIterations)
    {
        this.numServerLoopIterations = numServerLoopIterations;
    }
}
