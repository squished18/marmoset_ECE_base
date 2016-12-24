/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Feb 14, 2005
 *
 */
package edu.umd.cs.submitServer;

/**
 * @author jspacco
 *
 */
public interface SubmitServerConstants
{
    public static final String SNAPSHOT_PASSWORD = "snapshot.password";
    public static final String AUTHENTICATION_LOG = "edu.umd.cs.submitServer.logging.authenticationLog";
    public static final String AUTHENTICATION_SERVICE = "authentication.service";
    public static final String SKIP_LDAP = "skipLDAP";
    
    // Keys used for setting/getting http request attributes
    public static final String STUDENT_REGISTRATION_SET="studentRegistrationSet";
    public static final String TEST_PROPERTIES = "testProperties";
    public static final String PROJECT = "project";
    public static final String PROJECT_LIST = "projectList";
    public static final String COURSE = "course";
    public static final String USER = "user";
    public static final String USER_SESSION = "userSession";
    public static final String STUDENT = "student";
    public static final String TEST_OUTCOME_COLLECTION= "testOutcomeCollection";
    public static final String TEST_RUN = "testRun";
    public static final String SUBMISSION = "submission";
    public static final String PROJECT_JARFILE= "projectJarfile";
    
    public static final String LAST_LATE="lastLate";
    
    public static final String ECLIPSE_SUBMIT_PATH="/eclipse/SubmitProjectViaEclipse";
    
    public static final String DEFAULT_BEST_SUBMISSION_POLICY="edu.umd.cs.submitServer.DefaultBestSubmissionPolicy";
    public static final String PUBLIC_STUDENT = "public-student";
    public static final String RELEASE_UNIQUE = "release-unique";
    public static final String CARDINAL = "cardinal";
    public static final String FAILING_ONLY = "failing-only";
    public static final String HYBRID_TEST_TYPE = "hybridTestType";
    public static final String TEST_NUMBER = "testNumber";
    public static final String TEST_TYPE = "testType";
    public static final String MULTIPART_REQUEST = "multipartRequest";
    public static final String STUDENT_REGISTRATION = "studentRegistration";
    public static final String STUDENT_SUBMIT_STATUS= "studentSubmitStatus";
    public static final String COURSE_PK = "coursePK";
    public static final String SORT_KEY = "sortKey";
    public static final String SOURCE_FILE_LIST = "sourceFileList";
    public static final String SUBMISSION_LIST = "submissionList";
    public static final String TEST_RUN_LIST = "testRunList";
    public static final String TEST_OUTCOMES_MAP = "testOutcomesMap";
    public static final String INSTRUCTOR_CAPABILITY = "instructorCapability";
    public static final String COURSE_LIST = "courseList";
    
}
