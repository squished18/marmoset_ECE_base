/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 13, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.utilities.MarmosetUtilities;

/**
 * @author jspacco
 * 
 * XXX only handles <b>ONE</b> file.
 */
public class MultipartRequest
{
    private FileItem fileItem;
    private Map<String, Object> parameters = new HashMap<String, Object>();

    public static MultipartRequest parseRequest(HttpServletRequest request, int maxSize)
    throws IOException, ServletException
    {
        DiskFileUpload upload = new DiskFileUpload();
        upload.setSizeMax(maxSize);
        
        List items;
		try {
			// Parse the request
			items = upload.parseRequest(request);
		} catch (FileUploadBase.SizeLimitExceededException e) { 
			//Debug.error("File is to big! " + e);
			Debug.error("File upload is too big");
			throw new ServletException(e);
		} catch (FileUploadException e) {
		    Debug.error("FileUploadException: " +e);
		    throw new ServletException(e);
		}
		
		MultipartRequest multipartRequest = new MultipartRequest();
		for (Iterator ii = items.iterator(); ii.hasNext(); )
		{
			FileItem item = (FileItem) ii.next();
			if (item.isFormField())
			{
			    multipartRequest.setParameter(item.getFieldName(), item.getString());
			}
			else
			{
			    multipartRequest.setFileItem(item);
			}
		}
		return multipartRequest;
    }
    
    public void setParameter(String key, Object value)
    {
        parameters.put(key, value);
    }
    
    public String getStringParameter(String name)
    throws InvalidRequiredParameterException
    {
        String param = (String)parameters.get(name);
        if (param == null || param.equals(""))
        {
            throw new InvalidRequiredParameterException(name + " is a required parameter and it was " +param);
        }
        return param;
    }
    
    public String getOptionalStringParameter(String name)
    
    {
        String param = (String)parameters.get(name);
        if (param == null || param.equals(""))
        {
           return null;
        }
        return param;
    }
    
    public boolean getOptionalBooleanParameter(String name)
    {
        String param = (String)parameters.get(name);
        if (param == null)
            return false;
        return MarmosetUtilities.isTrue(param);
    }
    
    /**
     * @return Returns the fileItem.
     */
    public FileItem getFileItem()
    {
        return fileItem;
    }
    /**
     * @param fileItem The fileItem to set.
     */
    public void setFileItem(FileItem fileItem)
    {
        this.fileItem = fileItem;
    }

    /**
     * Finds the value mapped to by the given key.  Can return null or the empty string.
     * @param key the key
     * @return the value mapped to by the given key.  Will return null if the key is unmapped.
     */
    public String getParameter(String key)
    {
        return (String)parameters.get(key);
    }
    
    public String getParameter(String key, String defaultValue)
    {
        if (parameters.containsKey(key))
            return (String)parameters.get(key);
        return defaultValue;
    }

    /**
     * Returns the value mapped to by the given key as a boolean.
     * boolean true is represented by 'yes' or 'true' (case-insensitively) while false is
     * anything else.  The value string cannot be null or empty.
     * @param key the key
     * @return true if the key maps to a true value (where true is 'yes' or 'true' 
     * case-insensitively); false otherwise
     */
    public boolean getBooleanParameter(String key)
    throws InvalidRequiredParameterException
    {
        String value = getStringParameter(key);
        value = value.toUpperCase();
        if (value.equals("YES") || value.equals("TRUE"))
            return true;
        return false;
    }

    /**
     * @param string name of the parameter
     * @return
     */
    public int getIntParameter(String key)
    throws InvalidRequiredParameterException
    {
        try {
            String value = getStringParameter(key);
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InvalidRequiredParameterException(e.getMessage());
        }
    }

    /**
     * @param string
     * @return
     */
    public long getLongParameter(String key)
    throws InvalidRequiredParameterException
    {
        try {
            String value = getStringParameter(key);
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new InvalidRequiredParameterException(e.getMessage());
        }
    }
    
    public double getDoubleParameter(String name)
    throws InvalidRequiredParameterException
    {
        String param = getStringParameter(name);
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

    public boolean hasKey(String key) {
        return getOptionalStringParameter(key) != null;
    }
    
    public String toString() {
        return "parameters: " +parameters+"\nfileitem: "+fileItem;
    }
    
    public Timestamp getTimestampParameter(String name)
    throws InvalidRequiredParameterException
    {
        String param = getStringParameter(name);
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
        String lateMultiplier = getStringParameter("lateMultiplier");
        if (lateMultiplier == null || lateMultiplier.equals(""))
            project.setLateMultiplier(0.0);
        else
            project.setLateMultiplier(getDoubleParameter("lateMultiplier"));    
                    
        // ensure that lateConstant has at least a default value since it can't be null
        String lateConstant = getStringParameter("lateConstant");
        if (lateConstant == null || lateConstant.equals(""))
            project.setLateConstant(0);
        else
            project.setLateConstant(getIntParameter("lateConstant"));
        
        project.setCanonicalStudentRegistrationPK(getStringParameter("canonicalStudentRegistrationPK"));
        
        // these are unimportant and could be null
        project.setUrl(getStringParameter("url"));
        project.setDescription(getStringParameter("description"));
        
        return project;
    }
}
