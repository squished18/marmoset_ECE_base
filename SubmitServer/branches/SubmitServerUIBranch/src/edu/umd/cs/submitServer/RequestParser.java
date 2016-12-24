/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 9, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer;

import java.sql.Timestamp;

import javax.servlet.http.HttpServletRequest;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.utilities.MarmosetUtilities;

/**
 * @author jspacco
 *
 */
public class RequestParser
{
    private HttpServletRequest request;
    
    public RequestParser(HttpServletRequest request)
    {
        this.request = request;
    }
    
    public String getStringParameter(String name)
    throws InvalidRequiredParameterException
    {
        String param = request.getParameter(name);
        if (param == null || param.equals(""))
        {
            throw new InvalidRequiredParameterException(name + " is a required parameter and it was " +param);
        }
        return param;
    }
    
    public Timestamp getTimestampParameter(String name)
    throws InvalidRequiredParameterException
    {
        String param = request.getParameter(name);
        if (param == null || param.equals(""))
        {
            throw new InvalidRequiredParameterException(name + " is a required parameter");
        }
        Timestamp timestamp =null;
        try {
            timestamp = Timestamp.valueOf(param);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequiredParameterException("name was of an invalid form: " +e.toString());
        }
        return timestamp;
    }
    
    public double getDoubleParameter(String name)
    throws InvalidRequiredParameterException
    {
        String param = request.getParameter(name);
        if (param == null || param.equals(""))
        {
            throw new InvalidRequiredParameterException(name + " is a required parameter");
        }
        double d;
        try {
            d = Double.parseDouble(param);
            return d;
        } catch (IllegalArgumentException e) {
            throw new InvalidRequiredParameterException("name was of an invalid form: " +e.toString());
        }
    }
    
    public boolean getOptionalBooleanParameter(String name)
    {
        String param = request.getParameter(name);
        if (param == null)
            return false;
        return MarmosetUtilities.isTrue(param);
    }
    
    public boolean getBooleanParameter(String name)
    throws InvalidRequiredParameterException
    {
        String param = request.getParameter(name);
        if (param == null || param.equals(""))
        {
            throw new InvalidRequiredParameterException(name + " is a required parameter");
        }
        return MarmosetUtilities.isTrue(param);
    }
    
    public int getIntParameter(String name)
    throws InvalidRequiredParameterException
    {
        String param = request.getParameter(name);
        if (param == null || param.equals(""))
        {
            throw new InvalidRequiredParameterException(name + " is a required parameter");
        }
        int i;
        try {
            i = Integer.parseInt(param);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequiredParameterException("name was of an invalid form: " +e.toString());
        }
        return i;
    }
    
    public Course getCourse()
    throws InvalidRequiredParameterException
    {
        Course course = new Course();
        // these two CANNOT be null
        course.setSemester(getStringParameter("semester"));
        course.setCourseName(getStringParameter("courseName"));
        
        // these can be null
        course.setDescription(request.getParameter("description"));
        course.setUrl(request.getParameter("url"));
        return course;
    }

    /**
     * @return
     */
    public Project getProject()
    throws InvalidRequiredParameterException
    {
        Project project = new Project();
        
        // may be null
        boolean visibleToStudents = getOptionalBooleanParameter("visibleToStudents");
        project.setVisibleToStudents(visibleToStudents);
        project.setProjectPK(getParameter("projectPK"));
        // if no value is found, return "0"
        project.setProjectJarfilePK(getParameter("projectJarfilePK", "0"));
        
        project.setCoursePK(getStringParameter("coursePK"));
        project.setProjectNumber(getStringParameter("projectNumber"));
        
        project.setOntime(getTimestampParameter("ontime"));
        project.setLate(getTimestampParameter("late"));
        project.setTitle(getStringParameter("title"));
        
        project.setPostDeadlineOutcomeVisibility(getStringParameter("postDeadlineOutcomeVisibility"));
        
        String bestSubmissionPolicy=getStringParameter("bestSubmissionPolicy");
        project.setBestSubmissionPolicy(bestSubmissionPolicy.equals("")?null:bestSubmissionPolicy);
        project.setReleasePolicy(getStringParameter("releasePolicy"));
        project.setNumReleaseTestsRevealed(getIntParameter("numReleaseTestsRevealed"));
        project.setStackTracePolicy(getStringParameter("stackTracePolicy"));
        
        project.setReleaseTokens(getStringParameter("releaseTokens"));
        project.setRegenerationTime(getStringParameter("regenerationTime"));
        project.setInitialBuildStatus(getStringParameter("initialBuildStatus"));
        
        project.setKindOfLatePenalty(getStringParameter("kindOfLatePenalty"));
        
        // ensure that lateMultiplier has at least a default value since it can't be null
        String lateMultiplier = request.getParameter("lateMultiplier");
        if (lateMultiplier == null || lateMultiplier.equals(""))
            project.setLateMultiplier(0.0);
        else
            project.setLateMultiplier(getDoubleParameter("lateMultiplier"));    
                    
        // ensure that lateConstant has at least a default value since it can't be null
        String lateConstant = request.getParameter("lateConstant");
        if (lateConstant == null || lateConstant.equals(""))
            project.setLateConstant(0);
        else
            project.setLateConstant(getIntParameter("lateConstant"));
        
        project.setCanonicalStudentRegistrationPK(getStringParameter("canonicalStudentRegistrationPK"));
        
        // these could be null
        project.setArchivePK(request.getParameter("archivePK"));
        project.setUrl(request.getParameter("url"));
        project.setDescription(request.getParameter("description"));
        
        return project;
    }

    /**
     * Looks for a request parameter with the given key.  If the given key does not map
     * to a value, returns the default value. 
     * @param key the request parameter key
     * @param defaultValue the default value to return if the given key has no value
     * @return the value mapped to by this key, or the defaultValue if no such value exists.
     */
    private String getParameter(String key, String defaultValue)
    {
        String value = request.getParameter(key);
        if (value == null)
            return defaultValue;
        return value;
    }

    /**
     * Returns the parameter regardless of whether it's null or empty
     * @param name
     * @return
     */
    public String getParameter(String name)
    {
        return request.getParameter(name);
    }

    /**
     * Returns the parameter bound to key as an Integer.  Returns null if no such
     * request parameter exists.
     * @param key
     * @return the parameter as an Integer, or null if no such parameter exists.
     */
    public Integer getOptionalInteger(String key)
    {
        String value = request.getParameter(key);
        if (value == null || value.equals(""))
            return null;
        return new Integer(value);
    }
}
