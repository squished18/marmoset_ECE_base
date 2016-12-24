/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Apr 27, 2006
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.modelClasses;

/**
 * ProjectSkeleton
 * @author jspacco
 */
public class ProjectSkeleton
{
    private String archivePK;
    private String projectPK;
    private byte[] skeleton;
    
    /** List of columns in this table */
    final static String[] ATTRIBUTES_LIST = {
            "project_pk", "skeleton"
    };
    
    
    
    final static String TABLE_NAME = "project_skeleton";

    /**
     * @return Returns the archivePK.
     */
    public String getArchivePK()
    {
        return archivePK;
    }

    /**
     * @param archivePK The archivePK to set.
     */
    public void setArchivePK(String archivePK)
    {
        this.archivePK = archivePK;
    }

    /**
     * @return Returns the projectPK.
     */
    public String getProjectPK()
    {
        return projectPK;
    }

    /**
     * @param projectPK The projectPK to set.
     */
    public void setProjectPK(String projectPK)
    {
        this.projectPK = projectPK;
    }

    /**
     * @return Returns the skeleton.
     */
    public byte[] getSkeleton()
    {
        return skeleton;
    }

    /**
     * @param skeleton The skeleton to set.
     */
    public void setSkeleton(byte[] skeleton)
    {
        this.skeleton = skeleton;
    }
    
    
}
