package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Queries;
import edu.umd.cs.marmoset.modelClasses.Student;

public class ProcessStudentSurveyResults extends SubmitServerServlet
{

    /**
     * The doPost method of the servlet. <br>
     *
     * This method is called when a form has its tag value method equals to post.
     * 
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
    		Connection conn=null;
    		boolean transactionSuccess=false;
    		PreparedStatement stmt=null;
    		try {
    			conn=getConnection();
    			
    			Student student=(Student)request.getAttribute(STUDENT);
                
                conn.setAutoCommit(false);
    			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
    			
    			///Map<String,String[]> map=;
    			makePreparedStatementFromMap(request.getParameterMap(), conn);
                
                stmt=conn.prepareStatement("INSERT INTO survey_responses SET student_pk = ?");
                stmt.setString(1, student.getStudentPK());
                stmt.executeUpdate();
    			
    			conn.commit();
    			transactionSuccess=true;
                
                response.sendRedirect(request.getContextPath() +"/view"); 
    			
    		} catch (SQLException e) {
                throw new ServletException(e);
    		} finally {
    			rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess,conn);
    			Queries.closeStatement(stmt);
    		}
    }
    
    private static void makePreparedStatementFromMap(
    		Map<String,String[]> map,
    		Connection conn)
    throws SQLException
    {
        PreparedStatement stmt=null;
        try {
            String sql=" INSERT INTO spring2006_survey " +
            " SET ";
            
            List<String> list=new LinkedList<String>();
            
            // Default is anonymous, so if anonymous is null assume they want to be anonymous
            boolean anonymous=map.get("anonymous")==null||Boolean.valueOf(map.get("anonymous")[0]);
            Iterator<String> it=map.keySet().iterator();
            while (it.hasNext()) {
                String s=it.next();
                if (s.endsWith("__survey")) {
                    sql += s.replace("__survey", "")+
                    " = ? ";
                    if (it.hasNext()) {
                        sql += ", ";
                    }
                    // Make sure that if the student wanted things anonymous, that we throw
                    // away their student_pk
                    if (anonymous && s.contains("student_pk")) {
                        list.add("0");
                    } else
                        list.add(map.get(s)[0]);
                }
            }
            stmt=conn.prepareStatement(sql);
            int index=1;
            for (String s : list) {
                stmt.setString(index++, s);
            }
            
            stmt.executeUpdate();
            
        } finally {
            Queries.closeStatement(stmt);
        }
    }
}
