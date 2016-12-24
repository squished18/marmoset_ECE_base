/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Feb 1, 2005
 *
 */
package edu.umd.cs.buildServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This class uses contacts a servlet to log messages back to the submitServer.
 * It serializes and sends the entire LoggingEvent object.  The location of the
 * submitServer and the path to the servlet are all contained in the config file;
 * thus a Configuration object must be passed to this appender so that it can find the
 * server.
 * 
 * @author jspacco
 *
 */
public class ServletAppender extends AppenderSkeleton implements Appender, ConfigurationKeys
{
    //private static final String logURL = "https://fromage.cs.umd.edu:8443/buildServer/HandleBuildServerLogMessage";
    private static final int HTTP_TIMEOUT=10*1000;
    private Configuration config;
    private static boolean APPEND_TO_SUBMIT_SERVER;
    private static String buildServerIdentity;

    /* (non-Javadoc)
     * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    protected void append(LoggingEvent event)
    {
        if (!APPEND_TO_SUBMIT_SERVER)
            return;
        try {
        	Throwable throwable=null;
        	if (event.getThrowableInformation() != null) {
        		String[] throwableStringRep = event.getThrowableStrRep();
        		StringBuffer stackTrace = new StringBuffer();
        		for (String stackTraceString : throwableStringRep) {
        			stackTrace.append(stackTraceString);
        		}
        		throwable = new Throwable(stackTrace.toString());
        	}
        	
        	LoggingEvent newLoggingEvent = new LoggingEvent(
        			event.getFQNOfLoggerClass(),
        			event.getLogger(),
        			event.getLevel(),
        			buildServerIdentity +": "+ event.getMessage(),
        			throwable);
        	
            HttpClient client = new HttpClient();
            client.setConnectionTimeout(HTTP_TIMEOUT);
            String logURL = config.getRequiredProperty(SUBMIT_SERVER_PROTOCOL) +"://"+
            config.getRequiredProperty(SUBMIT_SERVER_HOST) +":"+
            config.getRequiredProperty(SUBMIT_SERVER_PORT) +"/"+
            config.getRequiredProperty(SUBMIT_SERVER_HANDLEBUILDSERVERLOGMESSAGE_PATH);
            
            MultipartPostMethod post = new MultipartPostMethod(logURL);
            // add password
            post.addParameter("password",config.getRequiredProperty(SUBMIT_SERVER_PASSWORD));
            
            ByteArrayOutputStream sink = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(sink);
                
            out.writeObject(newLoggingEvent);
            out.flush();
            // add serialized logging event object
            post.addPart(new FilePart("event",
                    new ByteArrayPartSource("event.out", sink.toByteArray())));
            
            int status = client.executeMethod(post);
            if (status != HttpStatus.SC_OK)
            {
                throw new IOException("Couldn't contact server: " +status);
            }
        } catch (MissingConfigurationPropertyException e) {
            // TODO any way to log these without an infinite loop?
        	System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        } catch (IOException e) {
            // TODO any way to log these without an infinite loop?
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.Appender#requiresLayout()
     */
    public boolean requiresLayout()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.Appender#close()
     */
    public void close()
    {
        // TODO Auto-generated method stub

    }
    /**
     * @return Returns the config.
     */
    public Configuration getConfig()
    {
        return config;
    }
    /**
     * @param config The config to set.
     */
    public void setConfig(Configuration config)
    throws MissingConfigurationPropertyException
    {
        this.config = config;
        if (config.getOptionalProperty(SUBMIT_SERVER_HANDLEBUILDSERVERLOGMESSAGE_PATH) == null)
            APPEND_TO_SUBMIT_SERVER = false;
        else
            APPEND_TO_SUBMIT_SERVER=true;
        
        // Hostname should have been set in the config file we were passed.
        // If not, then just use localhost.
        String hostname = config.getOptionalProperty(HOSTNAME);
        if (hostname == null) {
        	try {
        		hostname = InetAddress.getLocalHost().toString();
        	} catch (UnknownHostException e) {
        		hostname = "unknown-BuildServer";
        	}
        }
        
        String supportedCourseList = config.getRequiredProperty(SUPPORTED_COURSE_LIST);
        String semester = config.getOptionalProperty(SEMESTER);
        if (semester==null)
        	semester = "default-semester";
        
        buildServerIdentity = hostname +" "+ supportedCourseList +" "+ semester;
    }
}
