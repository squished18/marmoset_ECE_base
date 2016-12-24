/**
 * Copyright (C) 2006, University of Maryland
 * All Rights Reserved
 * Created on May 8, 2006
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Queries;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.submitServer.SubmitServerConstants;

/**
 * SurveyFilter
 * @author jspacco
 */
public class SurveyFilter extends SubmitServerFilter
{

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
    throws IOException, ServletException
    {
        HttpServletRequest request=(HttpServletRequest)req;
        HttpServletResponse response=(HttpServletResponse)resp;
        Connection conn=null;
        PreparedStatement stmt=null;
        Student student=(Student)request.getAttribute(STUDENT);
        try {
            conn=getConnection();
            
            boolean takenSurvey=false;
            boolean hasReleaseTests=false;
            if (student != null && Queries.hasTable("survey_responses", conn)) {
                // TODO assert that the necessary tables actually exist
                String sql="SELECT count(*) FROM survey_responses where student_pk = ? ";
                stmt=conn.prepareStatement(sql);
                stmt.setString(1,student.getStudentPK());
                ResultSet rs=stmt.executeQuery();
                if (rs.next()) {
                    takenSurvey=rs.getInt(1) > 0;
                }
                
                List<StudentRegistration> list=StudentRegistration.lookupAllByStudentPK(
                    student.getStudentPK(),
                    conn);
                String coursePK=null;
                for (StudentRegistration reg : list) {
                    coursePK=reg.getCoursePK();
                    if (coursePK.equals("11") ||
                        coursePK.equals("12") ||
                        coursePK.equals("13") ||
                        coursePK.equals("14"))
                    {
                        request.setAttribute(SubmitServerConstants.COURSE_PK, coursePK);
                        hasReleaseTests=true;
                    }
                    
                }
                if (request.getAttribute(SubmitServerConstants.COURSE_PK)==null) {
                    request.setAttribute(SubmitServerConstants.COURSE_PK, coursePK);
                }
            }
            request.setAttribute("takenSurvey", takenSurvey);
            request.setAttribute("hasReleaseTests", hasReleaseTests);
            
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
            Queries.closeStatement(stmt);
        }
        chain.doFilter(request, response);
    }

}
