/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Mar 4, 2005
 *
 */
package edu.umd.cs.buildServer;

/**
 * Extra information about the code being built.
 * Currently we collect MD5SUMs for Java code and do nothing for C code. 
 * 
 * @author jspacco
 */
public class BuildInformation
{
    private String md5sumClassfiles;
    private String md5sumSourcefiles;

    public BuildInformation() {}
    
    /**
     * @return Returns the md5sum.
     */
    public String getMd5sumClassfiles()
    {
        return md5sumClassfiles;
    }
    /**
     * @return Returns the sourcefilesMd5sum.
     */
    public String getMd5sumSourcefiles()
    {
        return md5sumSourcefiles;
    }
    /**
     * @param sourcefilesMd5sum The sourcefilesMd5sum to set.
     */
    public void setMd5sumSourcefiles(String sourcefilesMd5sum)
    {
        this.md5sumSourcefiles = sourcefilesMd5sum;
    }
    /**
     * @param classfilesMd5sum The classfilesMd5sum to set.
     */
    public void setMd5sumClassfiles(String classfilesMd5sum)
    {
        this.md5sumClassfiles = classfilesMd5sum;
    }
}
