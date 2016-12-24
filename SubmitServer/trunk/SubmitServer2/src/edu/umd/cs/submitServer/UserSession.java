/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 9, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer;

import java.util.HashSet;
import java.util.Set;

/**
 * @author jspacco
 *
 */
public class UserSession
{
    private String studentPK;
    private boolean superUser;
    private boolean capabilitiesActivated = true;
    private Set<String> instructorCapabilitySet = new HashSet<String>();
	private Set<String> instructorActionCapabilitySet = new HashSet<String>();
	private boolean backgroundDataComplete=false;
	private String givenConsent;

    public void setInstructorCapability(String coursePK)
    {
        instructorCapabilitySet.add(coursePK);
    }
    public void setInstructorActionCapability(String coursePK)
    {
        instructorActionCapabilitySet.add(coursePK);
    }
    public boolean hasInstructorActionCapability(String coursePK)
    {
        return capabilitiesActivated && instructorActionCapabilitySet.contains(coursePK);
    }
    
    public boolean hasInstructorCapability(String coursePK)
    {
        return capabilitiesActivated && instructorCapabilitySet.contains(coursePK);
    }
    
    public boolean canActivateCapabilities()
    {
        return !instructorCapabilitySet.isEmpty();
    }
    
    public boolean getCapabilitiesActivated()
    {
        return capabilitiesActivated;
    }
    
    public void setCapabilitiesActivated(boolean newValue)
    {
    		capabilitiesActivated = newValue;
    }
    
    /**
	 * @param studentPK The studentPK to set.
	 */
	public void setStudentPK(String studentPK) {
		this.studentPK = studentPK;
	}
	/**
	 * @return Returns the studentPK.
	 */
	public String getStudentPK() {
		return studentPK;
	}
	/**
	 * @return Returns the superUser.
	 */
	public boolean isSuperUser() {
	    return superUser;
	}
	/**
	 * @param superUser The superUser to set.
	 */
	public void setSuperUser(boolean superUser) {
		this.superUser = superUser;
	}
    /**
     * @return Returns the backgroundDataComplete.
     */
    public boolean isBackgroundDataComplete()
    {
        return backgroundDataComplete;
    }
    /**
     * @param backgroundDataComplete The backgroundDataComplete to set.
     */
    public void setBackgroundDataComplete(boolean backgroundDataComplete)
    {
        this.backgroundDataComplete = backgroundDataComplete;
    }
    public String getGivenConsent()
    {
        return givenConsent;
    }
    public void setGivenConsent(String returnedConsentForm)
    {
        this.givenConsent = returnedConsentForm;
    }
}
