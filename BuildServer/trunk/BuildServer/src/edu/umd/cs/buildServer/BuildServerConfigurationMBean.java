/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Nov 9, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.buildServer;

import java.util.List;


/**
 * BuildServerMBean
 * @author jspacco
 */
public interface BuildServerConfigurationMBean
{
    public int getNumServerLoopIterations();
    public void setNumServerLoopIterations(int num);
    public boolean getDoNotLoop();
    public void setDoNotLoop(boolean shutdownRequested);
    public String getJavaHome();
    public String getLogDirectory();
    public void setSupportedCourses(String supportedCourses);
    public String getSupportedCourses();
}
