<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head
	title="Project ${project.projectNumber} for ${course.courseName}" />

<body>
<ss:header />
<ss:instructorBreadCrumb />

<ss:projectTitle />

<h2>${student.firstname} ${student.lastname} : ${studentRegistration.cvsAccount} </h2>




<c:if test="${user.superUser and initParam['research.server']=='true'}">
<c:url var="researchView" value="/research/studentProject.jsp">
	<c:param name="projectPK" value="${project.projectPK}" />
	<c:param name="studentPK" value="${studentRegistration.studentPK}" />
</c:url>

<p>
<a href="${researchView}">Research view of ${studentRegistration.cvsAccount}'s submissions for  project ${project.projectNumber}</a>
</c:if>

<p>
<c:url var="grantExtensionLink" value="/view/instructor/grantExtension.jsp">
	<c:param name="studentRegistrationPK" value="${studentRegistration.studentRegistrationPK}"/>
	<c:param name="projectPK" value="${project.projectPK}"/>
</c:url>

<p>
Current Extension: ${studentSubmitStatus.extension}<br>
<a href="${grantExtensionLink}"> Grant ${student.firstname} ${student.lastname} 
an extension on project ${project.projectNumber}</a>
<p>					

language: ${testProperties.language}

<h2>Submissions</h2>
<table>
	<c:choose>
	<c:when test="${testProperties.language=='java' or testProperties.language=='Java'}">
	${ss:formattedColumnHeaders(9, canonicalTestOutcomeCollection)}
	</c:when>
	<c:otherwise>
	${ss:formattedColumnHeaders(7, canonicalTestOutcomeCollection)}	
	</c:otherwise>
	</c:choose>

	<tr>
		<th rowspan=2>#</th>
		<th rowspan=2>date submitted</th>
		<th rowspan=2>Results</th>
		<th rowspan=2>release tested</th>
		<th rowspan=2># inconsistent<br>background<br>retests</th>
		<th rowspan=2>view source</th>
		<th rowspan=2>Download</th>

		<c:if test="${testProperties.language=='java'}">
		<th rowspan=2># FindBugs<br>warnings</th>
		<th rowspan=2># Student<br>written<br>tests</th>
		</c:if>

		${ss:formattedTestHeaderTop(canonicalTestOutcomeCollection)}
				
		</tr>
	<tr>
		${ss:formattedTestHeader(canonicalTestOutcomeCollection)}
				
		</tr>


	<c:forEach var="submission" items="${submissionList}"
		varStatus="counter">
		<c:url var="submissionLink"
						value="/view/instructor/submission.jsp">
						<c:param name="submissionPK" value="${submission.submissionPK}" />
					</c:url>
		<tr class="r${counter.index % 2}">
			<td>${submission.submissionNumber}</td>

			<td><fmt:formatDate value="${submission.submissionTimestamp}"
				pattern="dd MMM yyyy hh:mm a" /></td>

			<c:choose>
				<c:when
					test="${submission.buildStatus == 'complete' && submission.compileSuccessful}">

					<td> <a href="${submissionLink}">
					${submission.valuePublicTestsPassed} /
					${submission.valueReleaseTestsPassed} /
					${submission.valueSecretTestsPassed} /
					${submission.numFindBugsWarnings} </a></td>
					<td><fmt:formatDate value="${submission.releaseRequest}"
						pattern="dd MMM yyyy hh:mm a" /></td>



				</c:when>
				<c:when test="${submission.buildStatus == 'complete'}">
					<td colspan=2><a href="${submissionLink}">did not compile</a></td>
				</c:when>
				<c:otherwise>
					<td colspan=2>not tested yet</td>
				</c:otherwise>
			</c:choose>
			
			<td>
			<c:url var="submissionAllTestsLink" value="/view/instructor/submissionAllTests.jsp">
				<c:param name="submissionPK" value="${submission.submissionPK}"/>
			</c:url>
			<a href="${submissionAllTestsLink}">
			${backgroundRetestMap[submission.submissionPK].numFailedBackgroundRetests}
			</a>
			</td>

			<td>
			<c:url var="viewSourceLink" value="/view/sourceFiles.jsp">
				<c:param name="submissionPK" value="${submission.submissionPK}" />
			</c:url>
			<a href="${viewSourceLink}"> view</a>
			</td>


			<td><c:url var="downloadLink" value="/data/DownloadSubmission">
				<c:param name="submissionPK" value="${submission.submissionPK}" />
			</c:url> <a href="${downloadLink}"> download</a></td>
			
			<c:if test="${testProperties.language=='java'}">
			<td>${testOutcomesMap[submission.submissionPK].numFindBugsWarnings}</td>
			<td>${testOutcomesMap[submission.submissionPK].numStudentWrittenTests}</td>
			</c:if>
						
			${ss:formattedTestResults(canonicalTestOutcomeCollection,testOutcomesMap[submission.submissionPK])}
						
		

		</tr>
	</c:forEach>

</table>

<ss:footer />
</body>
</html>
