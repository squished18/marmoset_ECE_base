<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head
	title="Instructor view of submission ${submission.submissionPK}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<ss:projectTitle />
<h2>${student.firstname} ${student.lastname} : ${studentRegistration.cvsAccount},
Submission # ${submission.submissionNumber}, <fmt:formatDate
	value="${submission.submissionTimestamp}" pattern="dd MMM, hh:mm a" /></h2>
	

	<p>
	<c:url var="viewSourceLink" value="/view/sourceFiles.jsp">
		<c:param name="submissionPK" value="${submission.submissionPK}" />
	</c:url>
	<a href="${viewSourceLink}">Source</a>

&nbsp;|&nbsp;

	<c:url var="downloadLink" value="/data/DownloadSubmission">
		<c:param name="submissionPK" value="${submission.submissionPK}" />
	</c:url>
	<a href="${downloadLink}"> Download</a>

&nbsp;|&nbsp;

	<c:url var="studentViewLink" value="/view/submission.jsp">
		<c:param name="submissionPK" value="${submission.submissionPK}" />
	</c:url>
	<a href="${studentViewLink}">Student view</a>
&nbsp;|&nbsp;
	<a href="${grantExtensionLink}"> Grant  extension </a>
	<c:if test="${studentSubmitStatus.extension != 0}">
	(Currently: ${studentSubmitStatus.extension} hours)
	</c:if>
&nbsp;|&nbsp;

<c:choose>
	<c:when test="${submission.buildStatus == 'pending' or submission.buildStatus == 'retest'}">
	being retested
	</c:when>
	<c:otherwise>
	
	<c:url var="reTestSubmissionLink" value="/view/instructor/confirmChangeSubmissionBuildStatus.jsp">
		<c:param name="submissionPK" value="${submission.submissionPK}" />
		<c:param name="title" value="Are you sure you want to mark submission ${submission.submissionPK} for retest?"/>
		<c:param name="buildStatus" value="retest"/>
	</c:url>
	<a href="${reTestSubmissionLink}">Retest</a>
	</c:otherwise>
</c:choose>

<c:choose>
	<c:when test="${submission.buildStatus != 'broken'}">
	&nbsp;|&nbsp;
	<c:url var="markBrokenLink" value="/view/instructor/confirmChangeSubmissionBuildStatus.jsp">
		<c:param name="submissionPK" value="${submission.submissionPK}" />
		<c:param name="title" value="Are you sure you want to mark submission ${submission.submissionPK} broken?  Broken submissions are not tested unless you specifically mark them for retest."/>
		<c:param name="buildStatus" value="broken"/>
	</c:url>
	<a href="${markBrokenLink}">mark broken</a>
	</c:when>
	<c:otherwise>
	<p>
	This submission has been marked broken.  It will never be tested by a buildServer unless it
	is specifically marked for retest by the 'retest' option.</p>
	</c:otherwise>
</c:choose>

<c:if test="${user.superUser and initParam['research.server'] =='true'}">
<p>
Research Features (these may not work)
&nbsp;|&nbsp;
	<c:url var="tarantulaView" value="/research/tarantulaSourceFiles.jsp">
		<c:param name="submissionPK" value="${submission.submissionPK}" />
	</c:url>

	<a href="${tarantulaView}">Tarantula</a>
&nbsp;|&nbsp;
	<c:url var="diffFileLink" value="/research/PrintDiffFile">
			<c:param name="submissionPK" value="${submission.submissionPK}"/>
	</c:url>
	<a href="${diffFileLink}"> diff file</a>
&nbsp;|&nbsp;
	<c:url var="researchViewLink" value="/research/studentProject.jsp">
		<c:param name="projectPK" value="${project.projectPK}"/>
		<c:param name="studentPK" value="${student.studentPK}"/>
	</c:url>
	<a href="${researchViewLink}"> research view</a>

<c:if test="${previousSnapshot != null}">
&nbsp;|&nbsp;
	<c:url var="previousSnapshotLink" value="/view/instructor/submission.jsp">
		<c:param name="submissionPK" value="${previousSnapshot.submissionPK}" />
	</c:url>
	<a href="${previousSnapshotLink}"> Previous Snapshot </a>
</c:if>
</c:if>


<c:choose>
<c:when test="${requestScope.testRun == null}">
	<h2>No test results available</h2>
</c:when>

	<c:when test="${!testOutcomeCollection.compileSuccessful}">
		<h2>Compile/Build unsuccessful</h2>
		<p>
		<pre>
			<c:out value="${testOutcomeCollection.buildOutcome.longTestResult}" />
		</pre>
	</c:when>
	<c:otherwise>

<ss:findBugsTable />

	<c:if test="${(testProperties.language == 'java' or testProperties.language=='Java')
		and testProperties.performCodeCoverage}">
	<h2>
		Code Coverage Results
	</h2>
	<table>
		<tr>
		<th>Test type</th>
		<th>statements</th>
		<th>conditionals</th>
		<th>methods</th>
	    </tr>
    	<c:if test="${studentCoverageStats != null}">
	    	<tr>
<c:url var="studentLink" value="/view/sourceFiles.jsp">
	<c:param name="testType" value="student"/>
	<c:param name="testNumber" value="all"/>
	<c:param name="testRunPK" value="${testRun.testRunPK}"/>
</c:url>
			<td> <a href="${studentLink}">  Student tests </a></td>
			${studentCoverageStats.HTMLTableRow}
	    	</tr>
	    </c:if>
		<c:if test="${publicCoverageStats != null}">
	    	<tr>
<c:url var="publicLink" value="/view/sourceFiles.jsp">
	<c:param name="testType" value="public"/>
	<c:param name="testNumber" value="all"/>
	<c:param name="testRunPK" value="${testRun.testRunPK}"/>
</c:url>
			<td> <a href="${publicLink}"> Public tests </a> </td>
			${publicCoverageStats.HTMLTableRow}
    		</tr>
    	</c:if>
		<c:if test="${publicAndStudentCoverageStats != null}">
	    	<tr>
<c:url var="publicAndStudentLink" value="/view/sourceFiles.jsp">
	<c:param name="testType" value="public-student"/>
	<c:param name="testNumber" value="all"/>
	<c:param name="testRunPK" value="${testRun.testRunPK}"/>
</c:url>
    		<td> <a href="${publicAndStudentLink}"> Public/Student tests </a> </td>
			${publicAndStudentCoverageStats.HTMLTableRow}
	    	</tr>
	    </c:if>
	    <c:if test="${cardinalCoverageStats != null}">
	    	<tr>
<c:url var="cardinalLink" value="/view/sourceFiles.jsp">
	<c:param name="testType" value="cardinal"/>
	<c:param name="testNumber" value="all"/>
	<c:param name="testRunPK" value="${testRun.testRunPK}"/>
</c:url>    	
    		<td> <a href="${cardinalLink}"> Public/Release/Secret tests </a> </td>
			${cardinalCoverageStats.HTMLTableRow}
	    	</tr>
	    </c:if>
	    <c:if test="${releaseCoverageStats.HTMLTableRow!=null}">
	    	<tr>
<c:url var="releaseLink" value="/view/sourceFiles.jsp">
	<c:param name="testType" value="release"/>
	<c:param name="testNumber" value="all"/>
	<c:param name="testRunPK" value="${testRun.testRunPK}"/>
</c:url>
    		<td> <a href="${releaseLink}"> Release tests </a> </td>
			${releaseCoverageStats.HTMLTableRow}
	    	</tr>
	    </c:if>
	    <c:if test="${releaseUniqueStats != null}">
	    	<tr>
<c:url var="releaseUniqueLink" value="/view/sourceFiles.jsp">
	<c:param name="testType" value="release-unique"/>
	<c:param name="testNumber" value="all"/>
	<c:param name="testRunPK" value="${testRun.testRunPK}"/>
</c:url>
	    	<td> <a href="${releaseUniqueLink}" title="covered by a release test but not by any public or student test"> Unique Release  </a><br>

	    	 </td>
	    	${releaseUniqueStats.HTMLTableRow}
	    	</tr>
	    </c:if>
		<c:if test="${intersectionCoverageStats != null}">
	    	<tr>
<c:url var="intersectionLink" value="/view/sourceFiles.jsp">
	<c:param name="testType" value="intersection"/>
	<c:param name="testNumber" value="all"/>
	<c:param name="testRunPK" value="${testRun.testRunPK}"/>
</c:url>
    		<td> <b> (Public U Release) <br>intersect<br> (Public U Student) </b> </td>
			${intersectionCoverageStats.HTMLTableRow}
	    	</tr>
	    </c:if>
	</table>
</c:if>
	
	<ss:studentWrittenTestOutcomesTable />
	
	<ss:uncoveredMethodsTable />

	<ss:allOutcomesTable />


<p>${testOutcomeCollection.valuePublicTestsPassed}/${
		testOutcomeCollection.valuePublicTests} points for public test cases.
<p>${testOutcomeCollection.valueReleaseTestsPassed}/${
		testOutcomeCollection.valueReleaseTests} points for release test cases.
<p>${testOutcomeCollection.valueSecretTestsPassed}/${
		testOutcomeCollection.valueSecretTests} points for secret test cases.
		
</c:otherwise>
</c:choose>


<c:if test="${not empty testOutcomeCollection.pmdOutcomes && userSession.superUser}">
	<ss:pmdOutcomesTable />
</c:if>

<c:if test="${testRunList != null}">
	<c:url value="submissionAllTests.jsp"
		var="submissionAllTestsLink">
		<c:param name="submissionPK" value="${submission.submissionPK}" />
	</c:url>
	<p><a href="${submissionAllTestsLink}"> See other test results for this
	submission </a>
</c:if>

<ss:submissionDetails />

<!-- 
<p> Submission client was ${submission.submitClient}
<c:if test="${testRun != null}">
Tested against test-setup #${testRun.projectJarfilePK},  
on ${testRun.testTimestamp} by ${testRun.testMachine}
 testRun&nbsp;#${testRun.testRunPK}
</c:if>
-->

<ss:footer />
</body>
</html>
