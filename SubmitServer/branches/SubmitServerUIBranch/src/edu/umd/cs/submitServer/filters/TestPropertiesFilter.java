/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Dec 5, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.filters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.zip.ZipInputStream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import edu.umd.cs.marmoset.modelClasses.MissingRequiredTestPropertyException;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.ProjectJarfile;
import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.marmoset.modelClasses.TestRun;

/**
 * TestPropertiesFilter
 * @author jspacco
 */
public class TestPropertiesFilter extends SubmitServerFilter
{

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
    throws IOException, ServletException
    {
        HttpServletRequest request=(HttpServletRequest)req;
        Connection conn=null;
        try {
            conn=getConnection();
            Project project=(Project)request.getAttribute(PROJECT);
            ProjectJarfile projectJarfile=null;
            if (project!=null) {
                projectJarfile=ProjectJarfile.lookupByProjectJarfilePK(
                    project.getProjectJarfilePK(),
                    conn);
            }
            if (projectJarfile==null) {
                TestRun testRun=(TestRun)request.getAttribute("testRun");
                if (testRun!=null)
                    projectJarfile=ProjectJarfile.lookupByProjectJarfilePK(testRun.getProjectJarfilePK(),conn);
            }
            if (projectJarfile!=null) {
                ZipInputStream zipIn=new ZipInputStream(new ByteArrayInputStream(projectJarfile.downloadArchive(conn)));
                TestProperties testProperties=new TestProperties();
                testProperties.load(zipIn);
                request.setAttribute(TEST_PROPERTIES, testProperties);
            }
        } catch (MissingRequiredTestPropertyException e) {
            throw new ServletException(e);
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
        chain.doFilter(request,resp);
    }
    
}
