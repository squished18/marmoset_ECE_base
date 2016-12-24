<%@ page language="java" import="java.util.List" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<ss:head
		title="Project Utilities for project ${project.projectNumber} in ${course.courseName}" />
	<body>
	<ss:header />
	<ss:instructorBreadCrumb />
	
	<ss:projectTitle/>
	<ss:projectMenu/>


<c:if test="${project.initialBuildStatus == 'accepted'}">
<h1>Upload only project</h1>
<p>This project is set up to accept uploads only, and not perform
any server based compilation and testing
</c:if>
<p>

Currently there are ${ss:numForRetest(project.projectPK, connection)} submissions
requiring re-test because a new test setup was uploaded.

<p>

There are <%= ((List)request.getAttribute("failedBackgroundRetestSubmissionList")).size() %>
submissions that returned different results after a background retest.

<h2> Instructor Utilities for project ${project.projectNumber}</h2>

<h3>Project Maintenance</h3>

<ul>
	<li><p style="font-weight: bold">Project Visibility</p>
		<c:choose>
		<c:when test="${project.visibleToStudents}">
			<p><span class="statusmessage">Visible to Students</span></p>
		</c:when>
		<c:otherwise>
			<p><span class="statusmessage">Invisible to Students</span></p>
			<c:url var="makeVisibleLink" value="/action/instructor/MakeProjectVisible" />
			<form method="post" action="${makeVisibleLink}"><input type="hidden"
				name="projectPK" value="${project.projectPK}" /> <input type="submit"
				value="make visible" /> (this action cannot be undone)</form>
		</c:otherwise>
		</c:choose>
	</li>

	<c:if test="${instructorActionCapability}">
	<li><p style="font-weight: bold">Test-Outcome Visibility</p>
		
		<form name="updatePostDeadlineOutcomeVisibilityForm" action="/action/instructor/UpdatePostDeadlineOutcomeVisibility" method="POST">
			<p><span class="statusmessage">Visibility of Test-Outcomes after the Late Deadline is '${project.postDeadlineOutcomeVisibility}'</span></p>
			<input type="hidden" name="projectPK" value="${project.projectPK}"/>
			<c:choose>
				<c:when test="${project.postDeadlineOutcomeVisibility == 'everything'}">
					<p class="notemessage">
						<b>NOTE:</b> Please be careful with this option!<br>
						All test outcomes are visible to students after the late deadline.
					</p>
				</c:when>
				<c:otherwise>
					<p class="notemessage">
						<b>NOTE: </b>Currently, all test outcomes (public, release and
						secret) are hidden from students even after the late deadline has
						passed.
						<br>
						If you set the post-deadline outcome visibility to 'everything',
						students will see all test outcomes (public, release and secret)
						once the project deadline has passed.
					</p>
				</c:otherwise>
			</c:choose>
			<p>
			<input type="radio" name="newPostDeadlineOutcomeVisibility" value="everything" ${ss:checked(project.postDeadlineOutcomeVisibility,'everything')}> Everything
			<input type="radio" name="newPostDeadlineOutcomeVisibility" value="nothing" ${ss:checked(project.postDeadlineOutcomeVisibility,'nothing')}> Nothing
			</p>
			<input type="submit" value="change post-deadline visibility of outcomes">
		</form>
	</li>
	</c:if>
	
	<li><p>
		<c:url var="updateProjectLink" value="/view/instructor/updateProject.jsp">
			<c:param name="projectPK" value="${project.projectPK}"/>
		</c:url>
		<a href="${updateProjectLink}"> Update this project </a></p></li>
		
	<li><p>
		<c:url var="uploadProjectStarterFilesLink" value="/view/instructor/uploadProjectStarterFiles.jsp">
			<c:param name="projectPK" value="${project.projectPK}"/>
		</c:url>
		<a href="${uploadProjectStarterFilesLink}"> Upload new project starter files </a></p></li>
	
	<li><p>
		<c:url var="exportProjectLink" value="/data/instructor/ExportProject">
			<c:param name="projectPK" value="${project.projectPK}"/>
		</c:url>
		<a href="${exportProjectLink}"> Export a project to a durable format 
		(includes the currently active test-setup, canonical, the project starter files (if any),
		and a serialized version of the </a></p></li>
	
	<li><p>
		<c:url var="createDotSubmitFileLink" value="/data/instructor/CreateDotSubmitFile">
			<c:param name="projectPK" value="${project.projectPK}"/>
		</c:url>
		<a href="${createDotSubmitFileLink}"> Create a .submit file for this project </a></p></li>
</ul>

<h3>Student Submissions</h3>
<ul>
	<li><p>	<c:url var="downloadBestSubmissionsLink" value="/data/instructor/DownloadBestSubmissions">
			<c:param name="projectPK" value="${project.projectPK}"/>
		</c:url>
		<a href="${downloadBestSubmissionsLink}"> 
		Download all students' best submissions
		</a></p></li>
	
	<li><p> <c:url var="downloadMostRecentOnTimeAndLateLink" value="/data/instructor/DownloadMostRecentOnTimeAndLateSubmissions">
			<c:param name="projectPK" value="${project.projectPK}"/>
		</c:url>
		<a href="${downloadMostRecentOnTimeAndLateLink}"> 
		Download all students' most recent on-time and late submissions
		</a></p></li>
		
	<li><p> <c:url var="downloadAllSubmissionsLink" value="/data/instructor/DownloadAllSubmissions">
			<c:param name="projectPK" value="${project.projectPK}"/>
		</c:url>
		<a href="${downloadAllSubmissionsLink}">
		Download all submissions for all students for this project
		</a></li></p></li>
</ul>

<h3>Student Grades</h3>
<ul>	
	<li><p>
		<c:url var="printTestDetailsLink" value="/data/instructor/PrintTestDetailsForDatabase">
			<c:param name="projectPK" value="${project.projectPK}"/>
		</c:url>
		<a href="${printTestDetailsLink}">Print test names and points in CSV format for upload to the grades database (grades.cs.umd.edu)</a></p></li>
	
	<li><p>
		<c:url var="printGradesLink" value="/data/instructor/PrintGrades">
			<c:param name="projectPK" value="${project.projectPK}"/>
		</c:url>
		<a href="${printGradesLink}"> Print grades in CSV format for spreadsheet use</a></p></li>
	
	<li><p>
		<c:url var="printGradesForDatabaseLink" value="/data/instructor/PrintGradesForDatabase">
			<c:param name="projectPK" value="${project.projectPK}"/>
		</c:url>
		<a href="${printGradesForDatabaseLink}"> Print grades in CSV format for upload to the grades database (grades.cs.umd.edu)</a></p></li>
	
	<li><p>
		<c:url var="printGradesForAllSubmissions" value="/data/instructor/PrintGradesForAllSubmissions">
			<c:param name="projectPK" value="${project.projectPK}"/>
		</c:url>
		<a href="${printGradesForAllSubmissions}"> Print grades for <i>ALL</i> submissions in CSV format for spreadsheet use</a></p></li>
		
	<li><p>
		<c:url var="printGradesForAllSubmissionAdjustedByBackgroundRetests" value="/data/instructor/PrintGradesForAllSubmissionsAdjustedByBackgroundRetests">
			<c:param name="projectPK" value="${project.projectPK}"/>
		</c:url>
		<a href="${printGradesForAllSubmissionAdjustedByBackgroundRetests}">
			Print grades for <i>ALL</i> submissions in CSV format for spreadsheet use, adjusted by the results of background retests
		</a></p></li>
</ul>

<h3>Code Coverage</h3>

<ul>	
	<li><p>
		<c:url var="downloadCodeCoverageLink" value="/data/instructor/DownloadCodeCoverageResultsForProject">
			<c:param name="projectPK" value="${project.projectPK}"/>
		</c:url>
		<a href="${downloadCodeCoverageLink}">
			Download code coverage results in CSV format (tab delimited)
		</a></p></li>
	
	<li><p>
		<c:url var="downloadCodeCoverageByPackageLink" value="/data/instructor/DownloadCodeCoverageResultsByPackage">
			<c:param name="projectPK" value="${project.projectPK}"/>
		</c:url>
		<a href="${downloadCodeCoverageByPackageLink}"> Download code coverage results split up by
			packages in CSV format (tab delimited)
		</a></p></li>


<%--
<li><p>
	<c:url var="testResultsLink" value="/view/instructor/projectTestResults.jsp">
		<c:param name="projectPK" value="${project.projectPK}" />
		<c:param name="sortKey" value="${sortKey}" />
	</c:url>
	<a href="${testResultsLink}">Alternative view giving test results details</a>

<li><p>
	<c:url var="testHistoryLink" value="/view/instructor/projectTestHistory.jsp">
		<c:param name="projectPK" value="${project.projectPK}" />
	</c:url>
	<a href="${testHistoryLink}">Historical view of overall progress on test cases</a>

<li><p>
	<c:url var="failedBackgroundRetestLink" value="/view/instructor/failedBackgroundRetests.jsp">
		<c:param name="projectPK" value="${project.projectPK}" />
	</c:url>
	<a href="${failedBackgroundRetestLink}">View failed background retests for this project</a>
--%>
	
</ul>

<p>

<c:if test="${project.initialBuildStatus == 'new'}">
<h1>Canonical Submissions</h1>
<table>
	<tr>
		<th>#</th>
		<th># inconsistent<br>background<br>retests</th>
		<th>timestamp</th>
		<th>public</th>
		<th>release</th>
		<th>secret</th>
	</tr>
	<c:forEach var="submission" items="${canonicalSubmissions}" varStatus="counter">
		<tr class="r${counter.index % 2}">
		<c:url var="submissionLink" value="/view/instructor/submission.jsp">
			<c:param name="submissionPK" value="${submission.submissionPK}" />
		</c:url>
			<td>${submission.submissionNumber}</td>
			<td></td>
			<td><a href="${submissionLink}"> ${submission.submissionTimestamp} </a></td>
			<td>${submission.valuePublicTestsPassed}</td>
			<td>${submission.valueReleaseTestsPassed}</td>
			<td>${submission.valueSecretTestsPassed}</td>
		</tr>
	</c:forEach>
</table>


<h1>Testing setups</h1>

<c:choose>
	<c:when test="${empty allTestSetups}">
		<p>No test setups for this project
	</c:when>
	<c:otherwise>
		<p>
		<table>
			<tr>
				<th>Version</th>
				<th>Tested</th>
				<th>Comment</th>
				<th>status</th>

				<th>activate/<br>inactivate</th>

				<th>Mark test-setup broken.
				<br>Broken test-setups are never retested.
				<br>Not usually necessary for test-setups
				<br>marked 'failed'.
				
				</th>

				<th>Download</th>
			</tr>
			<c:forEach var="testSetup" items="${allTestSetups}" varStatus="counter">
				<c:if test="${testSetup.jarfileStatus != 'broken'}">
				<c:choose>
					<c:when test="${testSetup.jarfileStatus == 'active'}">
					<tr class="highlight">
					</c:when>
					<c:otherwise>
					<tr class="r${counter.index % 2}">
					</c:otherwise>
				</c:choose>
					<td>${testSetup.version}</td>
					
					<td class="description"><fmt:formatDate
						value="${testSetup.datePosted}" pattern="dd MMM, hh:mm a" />
					</td>
					
					<td class="description">${testSetup.comment}</td>

					<td>
					<c:choose>
						<c:when test="${testSetup.jarfileStatus != 'new' && testSetup.jarfileStatus != 'pending'}">
						
							<c:url var="canonicalRunLink" value="/view/instructor/submission.jsp">
								<c:param name="testRunPK" value="${testSetup.testRunPK}"/>
							</c:url>
							<a href="${canonicalRunLink}">${testSetup.jarfileStatus}</a>
						</c:when>
						<c:otherwise>${testSetup.jarfileStatus}</c:otherwise>
					</c:choose>
					</td>
					
					<td>
					<c:choose>
						<c:when test="${testSetup.jarfileStatus == 'tested'}">
							<c:url var="assignPointsLink"
								value="/view/instructor/assignPoints.jsp">
								<c:param name="testRunPK" value="${testSetup.testRunPK}" />
							</c:url>
							<a href="${assignPointsLink}">assign points</a>
						</c:when>
						<c:when test="${testSetup.jarfileStatus == 'active'}">
							<c:url var="deactivateLink" value="/action/instructor/ChangeTestSetupStatus">
								<c:param name="projectJarfilePK" value="${testSetup.projectJarfilePK}"/>
								<c:param name="jarfileStatus" value="inactive"/>
							</c:url>
							<a href="${deactivateLink}">inactivate</a>
						</c:when>
						<%--
						<c:when test="${testSetup.jarfileStatus == 'inactive'}">
							<c:url var="activateLink" value="/action/instructor/ChangeTestSetupStatus">
								<c:param name="projectJarfilePK" value="${testSetup.projectJarfilePK}"/>
								<c:param name="jarfileStatus" value="active"/>
							</c:url>
							<a href="${activateLink}">activate</a>
						</c:when>
						--%>
					</c:choose>
					</td>
					
					<td>
						<c:if test="${testSetup.jarfileStatus != 'active' and testSetup.jarfileStatus != 'inactive'}">
						<c:url var="markBrokenLink" value="/action/instructor/ChangeTestSetupStatus">
							<c:param name="projectJarfilePK" value="${testSetup.projectJarfilePK}"/>
							<c:param name="jarfileStatus" value="broken"/>
						</c:url>
						<a href="${markBrokenLink}"> mark broken </a>
						</c:if>
					</td>
					
					<td>
						<c:url var="downloadProjectJarfileLink" 
							value="/data/instructor/DownloadProjectJarfile">
							<c:param name="projectJarfilePK" 
								value="${testSetup.projectJarfilePK}"/>
						</c:url>
						<a href="${downloadProjectJarfileLink}"> download </a>
					</td>
				</tr>
				</c:if>
			</c:forEach>
		</table>
	</c:otherwise>
</c:choose>

<h2>New test setups</h2>
<c:url var="uploadProjectJarFileLink"
	value="/action/instructor/UploadProjectJarfile" />

<p>
<form name="submitform" action="${uploadProjectJarFileLink}"
	enctype="multipart/form-data" method="POST"><input type="hidden"
	name="projectPK" value="${project.projectPK}">
	
	<table class="form"><tr><th colspan=2>Upload new test setup</th>
	<tr><td>Comment:<td class="input">
<textarea cols="40" rows="1" name="comment"></textarea>
<tr><td>Jar/Zip file: <td><input type="file" name="file" size=40>
<tr><td class="submit" colspan=2><input type="submit" value="Upload Test Setup">
</table>
</form>

<h2>Canonical solutions</h2>
<c:url var="canonicalStudentProjectLink" value="/view/instructor/studentProject.jsp">
	<c:param name="projectPK" value="${project.projectPK}" />
	<c:param name="studentRegistrationPK" value="${canonicalAccount.studentRegistrationPK}" />
</c:url>


<p>
New and failed testing setups are validated by testing against latest submission 
by  <a href="${canonicalStudentProjectLink}">${canonicalAccount.cvsAccount}</a>. Submissions
can be uploaded via Eclipse, the command line tool, or web based upload.
<p>
<b>Any</b> instructor
can upload a canonical submission through this interface; you <b>do not</b> need to own the 
canonical account (and have the password) to make a canonical submission through this web
interface.

<p>
<form name="submitform" enctype="multipart/form-data"
	action="<c:url value="/action/SubmitProjectViaWeb"/>" method="POST"><input type="hidden"
	name="projectPK" value="${project.projectPK}" /> 
	<input type="hidden"
	name="studentRegistrationPK" value="${canonicalAccount.studentRegistrationPK}" />
	<input type="hidden"
	name="studentPK" value="${canonicalAccount.studentPK}" />
	<input type="hidden"
	name="submitClientTool" value="web" />
	<input type="hidden" name="isCanonicalSubmission" value="true" />
<table class="form">
<tr><th colspan=2>Project submission via jar file for canonical account</th>
<tr><td>Jar File to Submit: <td class="input"><input type="file" name="file" size=40 />
<tr><td class="submit" colspan=2"><input type="submit" value="Submit project!">
</table>
</form>

</c:if>

    
  </body>
</html>
