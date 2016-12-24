/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Apr 13, 2005
 *
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.BackgroundData;
import edu.umd.cs.marmoset.modelClasses.IncorrectBackgroundDataException;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.UserSession;

/**
 * @author jspacco
 *
 */
public class UpdateBackgroundData extends SubmitServerServlet
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
        Connection conn = null;
        HttpSession session = request.getSession();
        UserSession userSession = (UserSession)session.getAttribute(USER_SESSION);
        try {
            conn = getConnection();
            
            RequestParser parser = new RequestParser(request);
            
            BackgroundData backgroundData = parseBackgroundDataFromRequest(
                    parser,
                    userSession,
                    conn);
            
            backgroundData.insertOrUpdate(conn);
            userSession.setBackgroundDataComplete(backgroundData.isComplete());
            
            //String redirectUrl = request.getContextPath() + "/view/index.jsp";
            //response.sendRedirect(redirectUrl);
            
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<html><head><title>");
            out.println("Thanks for filling out the background data survey");
            out.println("</title></head><body>");
            
            out.println("Thanks for providing us with data!<br>");
            out.println("You entered: <br><br>");
            out.println(backgroundData);
            
            out.println("<p><a href=\"/view/index.jsp\"> Click here to return to the main menu</a><p>");
            
            out.println("</body></html>");
            
            out.flush();
            out.close();
        } catch (IncorrectBackgroundDataException e) {
            // If this exception is thrown, then the form may be partially-completed
            // so, re-direct back to the background.jsp page, which will format a message
            // to the user about which fields still should be filled out
            String redirectUrl = request.getContextPath() + "/view/background.jsp";
            response.sendRedirect(redirectUrl);
            return;
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
    }
        
    /**
     * Creates a background Data object from the attributes set in the HttpServletRequest
     * controlled by the RequestParser.  The backgroundData
     * @param parser
     * @param userSession
     * @return
     */
    private static BackgroundData parseBackgroundDataFromRequest(
            RequestParser parser,
            UserSession userSession,
            Connection conn)
    throws SQLException, IncorrectBackgroundDataException
    {
        // extract studentPK from the HttpSession
		String studentPK = userSession.getStudentPK();

        // XXX ???these might all be null
		String gender = parser.getParameter("gender");
        String age = parser.getParameter("age");
        String highSchoolCountry = parser.getParameter("highSchoolCountry");

        // Read EthnicRacialAssociation field.
        // If this field is "na", they selected "Prefer not to answer"
        // If this field is null, then we assume they have answered the
        // question, so we set EthnicRacialAssociation to "yes"
        // and start checking the possible values.
        String EthnicRacialAssociation = parser.getParameter("EthnicRacialAssociation");
        
        //System.err.println("EthnicRacialAssociation: " +EthnicRacialAssociation);
        // Fetch from the form the fields that have been set.
        String AmericanIndian = parser.getParameter("AmericanIndian");
        String Asian = parser.getParameter("Asian");
        String Black = parser.getParameter("Black");
        String Caucasian = parser.getParameter("Caucasian");
        String LatinoLatina = parser.getParameter("LatinoLatina");
                
        if (EthnicRacialAssociation == null &&
                (AmericanIndian != null ||
                        Asian != null ||
                        Black != null ||
                        Caucasian != null ||
                        LatinoLatina != null))
        {
            EthnicRacialAssociation= "yes";
        }

        if (EthnicRacialAssociation == null ||
                EthnicRacialAssociation.equals("na"))
        {
            AmericanIndian = null;
            Asian = null;
            Black = null;
            Caucasian = null;
            LatinoLatina = null;
        }
        
        // Fetch prior programming experience.
        String priorProgrammingExperience = parser.getParameter("priorProgrammingExperience");
        String otherInstitution = null;
        String aExamScore = null;
        String abExamScore = null;
        // Only check the value of priorProgrammingExperience if it is not null
        if (priorProgrammingExperience != null)
        {
            if (priorProgrammingExperience.equals(BackgroundData.COMMUNITY_COLLEGE))
            {
                // name of community college (if community college was selected for prior programming experience).
                otherInstitution = parser.getParameter("communityCollege");
            }
            else if (priorProgrammingExperience.equals(BackgroundData.OTHER_UM_INSTITUTION))
            {
                // name of other UM institution
                otherInstitution = parser.getParameter("otherUMInstitution");
            }
            else if (priorProgrammingExperience.equals(BackgroundData.OTHER_NON_UM_INSTITUTION))
            {
                // name of other non-UM institution
                otherInstitution = parser.getParameter("otherNonUMInstitution");
            }
            else if (priorProgrammingExperience.equals(BackgroundData.HIGH_SCHOOL_AP_COURSE))
            {
                // Fetch AP Exam scores from form.
                // We ignore them if these are empty strings.
                //if (Utilities.isNonEmpty(parser, "aExamScore"))
                //{
                  //  aExamScore = parser.getParameter("aExamScore");
                //}

                aExamScore = parser.getParameter("aExamScore");
                if (aExamScore == null || aExamScore.equals(""))
                {
                    aExamScore = "";
                }
                abExamScore = parser.getParameter("abExamScore");
                if (abExamScore == null || abExamScore.equals(""))
                {
                    abExamScore = "";
                }
                
                System.err.println("aExamScore: " +aExamScore+ ", abExamScore: " +abExamScore);
            }
        }
        
        // UM placement exam information
        String placementExam = parser.getParameter("placementExam");
        String placementExamResult = null;
        if (placementExam != null && !placementExam.equals(BackgroundData.NONE))
        {
            placementExamResult = parser.getParameter("placementExamResult");
        }
        
        // get the major
        String major = parser.getParameter("major");

        // store all this data in a background data object
        BackgroundData backgroundData = new BackgroundData();
        
        backgroundData.setStudentPK(studentPK);
        backgroundData.setGender(gender);
        backgroundData.setEthnicRacialAssociation(EthnicRacialAssociation);
        backgroundData.setAmericanIndian(AmericanIndian);
        backgroundData.setAsian(Asian);
        backgroundData.setBlack(Black);
        backgroundData.setCaucasian(Caucasian);
        backgroundData.setLatinoLatina(LatinoLatina);
        backgroundData.setAge(age);
        backgroundData.setHighSchoolCountry(highSchoolCountry);
        backgroundData.setPriorProgrammingExperience(priorProgrammingExperience);
        backgroundData.setOtherInstitution(otherInstitution);
        backgroundData.setAExamScore(aExamScore);
        backgroundData.setAbExamScore(abExamScore);
        backgroundData.setPlacementExam(placementExam);
        backgroundData.setPlacementExamResult(placementExamResult);
        backgroundData.setMajor(major);

        // First, insert whatever data they gave us, even if it's incomplete
        // We never want to throw away data!
        backgroundData.insertOrUpdate(conn);
        
        // Now verifyFormat() and throw an exception if necessary
        backgroundData.verifyFormat();
        
        return backgroundData;
    }
}
