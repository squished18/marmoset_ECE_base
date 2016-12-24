/*
 * Created on Jan 19, 2005
 *
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.ProjectJarfile;
import edu.umd.cs.marmoset.modelClasses.Queries;
import edu.umd.cs.marmoset.modelClasses.Snapshot;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.StudentSubmitStatus;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestRun;
import edu.umd.cs.submitServer.MultipartRequest;
import edu.umd.cs.submitServer.ReleaseInformation;
import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.UserSession;

/**
 * Requires a projectPK and optionally a studentRegistrationPK.
 * 
 * This filter stores studentRegistration, student, course, project,
 * studentSubmitStatus and submissionList attributes in the request.
 */
public class ExtractParametersFilter extends SubmitServerFilter {

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
    throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        HttpSession session = request.getSession();
        UserSession userSession = (UserSession) session.getAttribute(USER_SESSION);
//        System.out.println(request.getRequestURI() + "?" + request.getQueryString());
//        System.out.println(request.getHeader("referer"));

        String sortKey = request.getParameter(SORT_KEY);
        String testRunPK = request.getParameter("testRunPK");
        String submissionPK = request.getParameter("submissionPK");
        String projectPK = request.getParameter("projectPK");
        String coursePK = request.getParameter(COURSE_PK);
        String studentPK = request.getParameter("studentPK");
        String studentRegistrationPK = request.getParameter("studentRegistrationPK");
        String projectJarfilePK = request.getParameter("projectJarfilePK");
        
		MultipartRequest multipartRequest = (MultipartRequest) request
				.getAttribute(MULTIPART_REQUEST);
		if (multipartRequest != null) {
			projectPK = multipartRequest.getOptionalStringParameter("projectPK");
			coursePK = multipartRequest.getOptionalStringParameter(COURSE_PK);
			studentPK = multipartRequest.getParameter("studentPK");
		}
 
        request.setAttribute(SubmitServerConstants.INSTRUCTOR_CAPABILITY, 
                Boolean.FALSE);
        request.setAttribute("instructorActionCapability", 
                Boolean.FALSE);
        


        TestRun testRun = null;
        TestOutcomeCollection testOutcomeCollection = null;
        Submission submission = null;
        ReleaseInformation releaseInformation = null;
        List submissionList = null;
        Project project = null;
        Course course = null;
        Student student = null;
        Student user = null;
        StudentRegistration studentRegistration = null;
        List<Project> projectList = null;
        StudentSubmitStatus studentSubmitStatus = null;
        
        
       
        Connection conn = null;
        try {
            conn = getConnection();
            user = Student.lookupByStudentPK(userSession.getStudentPK(), conn);
            request.setAttribute(USER, user);

            List<Course> courseList=Course.lookupAllByStudentPK(userSession.getStudentPK(),conn);
            request.setAttribute(SubmitServerConstants.COURSE_LIST, courseList);
            if (courseList.size() == 1) {
                request.setAttribute("singleCourse", Boolean.TRUE);
                Course onlyCourse = (Course) courseList.get(0);
                coursePK = onlyCourse.getCoursePK();
            }
            else
                request.setAttribute("singleCourse", Boolean.FALSE);
            
            
            
            
            if (studentRegistrationPK != null) {
          	  studentRegistration = StudentRegistration.lookupByStudentRegistrationPK(studentRegistrationPK, conn);
          	  studentPK = studentRegistration.getStudentPK();
          	  coursePK = studentRegistration.getCoursePK();
            }
            boolean studentSpecifiedByInstructor = studentPK != null;

            if (studentPK == null)
              studentPK = userSession.getStudentPK();

            if (testRunPK != null) {
                // Get Test Run
                testRun = TestRun.lookupByTestRunPK(testRunPK, conn);
                submissionPK = testRun.getSubmissionPK();
                submission = Submission.lookupBySubmissionPK(submissionPK, conn);
            }
            if (submissionPK != null) {
                // Get Submission
                submission = Submission.lookupBySubmissionPK(submissionPK, conn);
                // get the defaultTestRun, unless we already got a test run from the previous
                // if block
                if (testRun == null)
                {
                    testRun = TestRun.lookupByTestRunPK(submission.getCurrentTestRunPK(), conn);
                }
                // if we found a testRun, get its testOutcomeCollection
                if (testRun != null)
                {
                    testOutcomeCollection = TestOutcomeCollection.lookupByTestRunPK(
                        testRun.getTestRunPK(), conn);
                }

                projectPK = submission.getProjectPK();
                studentRegistration = StudentRegistration.lookupBySubmissionPK(
                        submissionPK, conn);
                if (submission.getNumTestRuns() >= 1)
                {
                    List<TestRun> testRunList = TestRun.lookupAllBySubmissionPK(
                        submissionPK,
                        conn);
                    request.setAttribute(TEST_RUN_LIST, testRunList);

                    //Collections.reverse(testRunList);
                    // map projectJarfilePKs to their corresponding project jarfiles
                    Map<String, ProjectJarfile> projectJarfileMap = new HashMap<String, ProjectJarfile>();
                    for (TestRun tr : testRunList) {
                        // make sure we have already mapped the project jarfile to its PK
                        if (!projectJarfileMap.containsKey(tr.getProjectJarfilePK()))
                        {
                            ProjectJarfile jarfile = ProjectJarfile.lookupByProjectJarfilePK(
                                    tr.getProjectJarfilePK(),
                                    conn);
                            projectJarfileMap.put(jarfile.getProjectJarfilePK(), jarfile);
                        }
                    }
                    request.setAttribute("projectJarfileMap", projectJarfileMap);
                }
            }

            if (projectJarfilePK != null && !projectJarfilePK.equals("0")) {
                ProjectJarfile projectJarfile = ProjectJarfile.lookupByProjectJarfilePK(projectJarfilePK, conn);
                request.setAttribute(PROJECT_JARFILE, projectJarfile);
                projectPK = projectJarfile.getProjectPK();
            }
            
            if (projectPK != null) {
                // Get Project
                project = Project.getByProjectPK(projectPK, conn);

                coursePK = project.getCoursePK();
                if (!project.getVisibleToStudents()
                        && !userSession.hasInstructorCapability(coursePK))
                    throw new ServletException("Project is not visible");
                
                request.setAttribute(PROJECT, project);
              
                StudentRegistration canonicalAccount = StudentRegistration.lookupByStudentRegistrationPK(
                        project.getCanonicalStudentRegistrationPK(),conn);
                request.setAttribute("canonicalAccount", canonicalAccount);
                

                if (submissionPK == null || !studentSpecifiedByInstructor) {
                    // Get Collection
                    submissionList = Submission
                            .lookupAllByStudentPKAndProjectPK(studentPK, projectPK, conn);

                    releaseInformation = new ReleaseInformation(project,
                            submissionList);
                }
            }

            if (coursePK != null && !studentSpecifiedByInstructor 
                    && userSession.hasInstructorCapability(coursePK)) {
            	// Add a collection of the the canonical accounts
            	List<StudentRegistration> canonicalAccountCollection = 
                	StudentRegistration.lookupCanonicalAccountsByCoursePK(coursePK, conn);
                request.setAttribute("canonicalAccountCollection", canonicalAccountCollection);
                // get list of students
                
                // studentRegistrationSet is the sorted component based on the sort key
                List<StudentRegistration> studentRegistrationCollection;
                if (projectPK != null) {
                    
                    studentRegistrationCollection = 
                        StudentRegistration.lookupAllWithAtLeastOneSubmissionByProjectPK(projectPK, conn);
                    TreeSet<StudentRegistration> studentRegistrationSet = new TreeSet<StudentRegistration>(StudentRegistration.getComparator(sortKey));
                    studentRegistrationSet.addAll(studentRegistrationCollection);
                    
                    Map<String,StudentRegistration> studentRegistrationMap = new HashMap<String,StudentRegistration>();
                    for (StudentRegistration registration : studentRegistrationSet) {
                        studentRegistrationMap.put(registration.getStudentRegistrationPK(), registration);
                    }
                    request.setAttribute("studentRegistrationMap", studentRegistrationMap);
                    
                    // TODO ensure that studentRegistrationCollection can be removed
                    request.setAttribute("studentRegistrationCollection", studentRegistrationCollection);
                    request.setAttribute(STUDENT_REGISTRATION_SET, studentRegistrationSet );
                    request.setAttribute("studentSubmitStatusMap", StudentSubmitStatus.lookupAllByProjectPK(projectPK,conn));
                    
                    List<StudentRegistration> noSubmissions = StudentRegistration.lookupAllByCoursePK(coursePK, conn);
                    TreeSet<StudentRegistration> studentsWithoutSubmissions = new TreeSet<StudentRegistration>(StudentRegistration.getComparator(sortKey));
                    studentsWithoutSubmissions.addAll(noSubmissions);
                    studentsWithoutSubmissions.removeAll(studentRegistrationCollection);
                    request.setAttribute("studentsWithoutSubmissions", studentsWithoutSubmissions );
                    
                }
                else {
                    studentRegistrationCollection = StudentRegistration.lookupAllByCoursePK(coursePK, conn);
                    // TODO ensure that studentRegistrationCollection can be removed
                    request.setAttribute("studentRegistrationCollection", studentRegistrationCollection);

                    TreeSet<StudentRegistration> studentRegistrationSet = new TreeSet<StudentRegistration>(StudentRegistration.getComparator(sortKey));
                    studentRegistrationSet.addAll(studentRegistrationCollection);
                    request.setAttribute("studentRegistrationSet", studentRegistrationSet);
                }
            
            }
            
            if (coursePK != null && studentPK != null) {
                Map<String, StudentSubmitStatus> projectToStudentSubmitStatusMap = lookupStudentSubmitStatusMapByCoursePKAndStudentPK(
                        coursePK,
                        studentPK,
                        conn);
                request.setAttribute("projectToStudentSubmitStatusMap", projectToStudentSubmitStatusMap);
            }

            if (studentRegistration != null)
                studentPK = studentRegistration.getStudentPK();
            else {
                if (studentPK == null)
                    studentPK = userSession.getStudentPK();
                studentRegistration = StudentRegistration.lookupByStudentPKAndCoursePK(
                        studentPK, coursePK, conn);
            }
            
            if (studentRegistration != null && project != null)
            {
                studentSubmitStatus = StudentSubmitStatus.lookupByStudentRegistrationPKAndProjectPK(
                        studentRegistration.getStudentRegistrationPK(),
                        project.getProjectPK(),
                        conn);
            }
            
            student = Student.lookupByStudentPK(studentPK, conn);
            request.setAttribute(STUDENT, student);
            if (coursePK != null) {
                // Get Course and all of its projects
                course = Course.lookupByCoursePK(coursePK, conn);
                projectList = Project.lookupAllByCoursePK(coursePK, conn);
                Collections.reverse(projectList);
                request.setAttribute(COURSE, course);
                request.setAttribute(PROJECT_LIST, projectList);
                request.setAttribute(SubmitServerConstants.INSTRUCTOR_CAPABILITY, 
                        Boolean.valueOf(userSession.hasInstructorCapability(coursePK)));
                request.setAttribute("instructorActionCapability", 
                        Boolean.valueOf(userSession.hasInstructorActionCapability(coursePK)));
            }
            
            

            boolean instructorViewOfStudent = !userSession.getStudentPK().equals(studentPK);

            if (instructorViewOfStudent
                    && !userSession.hasInstructorCapability(coursePK))
            {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Error");
                return;
            }

            request.setAttribute("instructorViewOfStudent", Boolean.valueOf(instructorViewOfStudent));
            if (testRun != null)
                request.setAttribute(TEST_RUN, testRun);
            if (testOutcomeCollection != null)
                request.setAttribute(TEST_OUTCOME_COLLECTION, testOutcomeCollection);
            if (submission != null) {
                request.setAttribute("submission", submission);

                // TODO Write methods that lookup the previous and next submissions
                // (either from the snapshot object or from the DB) and store those
                // as an attribute
                //Snapshot previousSnapshot = Snapshot.lookupPreviousSnapshot(submission.getSubmissionPK(), conn);
                Snapshot previousSnapshot=null;
                if (previousSnapshot != null)
                    request.setAttribute("previousSnapshot", previousSnapshot);
            }
            if (releaseInformation != null)
                request.setAttribute("releaseInformation", releaseInformation);
            if (submissionList != null) {
                Collections.reverse(submissionList);
                request.setAttribute(SUBMISSION_LIST, submissionList);
            }
              
            if (studentRegistration != null)
                request.setAttribute(STUDENT_REGISTRATION, studentRegistration);
            
            if (studentSubmitStatus != null)
                request.setAttribute(STUDENT_SUBMIT_STATUS, studentSubmitStatus);

            if (course != null)
                request.setAttribute(COURSE, course);
            if (user != null)
                request.setAttribute(USER, user);
            if (projectList != null)
                request.setAttribute(PROJECT_LIST, projectList);
            if (sortKey != null)
                request.setAttribute(SORT_KEY, sortKey);
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }

        chain.doFilter(request, response);
    }

    /**
     * Returns a map from projectPKs to studentSubmitStatus records for a given student
     * and course.  The parameters to this are sort of a hack becuase this method is only
     * called in this filter when coursePK != null and studentPK != null to get the
     * submitStatus records to pass to /view/instructor/student.jsp
     * @param coursePK
     * @param studentPK
     * @param conn
     * @return
     * @throws SQLException
     */
    public static Map<String, StudentSubmitStatus> lookupStudentSubmitStatusMapByCoursePKAndStudentPK(
            String coursePK,
            String studentPK,
            Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +StudentSubmitStatus.ATTRIBUTES+
            " FROM student_submit_status, student_registration " +
            " WHERE student_submit_status.student_registration_pk = student_registration.student_registration_pk " +
            " AND student_registration.course_pk = ? " +
            " AND student_registration.student_pk = ? ";

        PreparedStatement stmt=null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, coursePK);
            stmt.setString(2, studentPK);
            
            ResultSet rs = stmt.executeQuery();
            
            Map<String, StudentSubmitStatus> map = new LinkedHashMap<String, StudentSubmitStatus>();
            while (rs.next())
            {
                StudentSubmitStatus studentSubmitStatus = new StudentSubmitStatus();
                studentSubmitStatus.fetchValues(rs, 1);
                map.put(studentSubmitStatus.getProjectPK(), studentSubmitStatus);
            }
            return map;
        }
        finally {
            Queries.closeStatement(stmt);
        }
    }
}
