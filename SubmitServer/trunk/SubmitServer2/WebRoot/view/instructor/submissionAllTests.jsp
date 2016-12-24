<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head
	title="All test runs for submissionPK ${submission.submissionPK}" />
<body>

<ss:header />
<ss:instructorBreadCrumb />

<h1>Project ${project.projectNumber}: ${project.title}</h1>
<h2>${student.firstname} ${student.lastname}</h2>
<h2>Submission # ${submission.submissionNumber}, <fmt:formatDate
	value="${submission.submissionTimestamp}" pattern="dd MMM, hh:mm a" /></h2>


<table>
	${ss:formattedColumnHeaders(7, canonicalTestOutcomeCollection)}
	<tr>
		<th rowspan=2>date ran</th>
		<th rowspan=2>test machine</th>
		<th rowspan=2>successful<br>background<br>retests</th>
		<th rowspan=2># inconsistent<br>background<br>retests</th>
		<th rowspan=2>testsetup tested</th>
		<th rowspan=2>comment / <br>download link</th>
		<th rowspan=2>view results</th>
		${ss:formattedTestHeaderTop(canonicalTestOutcomeCollection)}
	</tr>
	
	<tr>
		${ss:formattedTestHeader(canonicalTestOutcomeCollection)}
	</tr>
	
	<c:forEach var="testRun" items="${testRunList}" varStatus="counter">
		<c:choose>
		<c:when test="${testRun.testRunPK == submission.currentTestRunPK}">
		<tr class="highlight">
		</c:when>
		<c:otherwise>
		<tr class="r${counter.index % 2}">
		</c:otherwise>
		</c:choose>
			<td><fmt:formatDate value="${testRun.testTimestamp}"
				pattern="dd MMM KK:mm:ss a" /></td>

			<td>${testRun.testMachine}</td>
			
			<td>${backgroundRetestMap[testRun.projectJarfilePK].numSuccessfulBackgroundRetests}</td>
			
			<td>${backgroundRetestMap[testRun.projectJarfilePK].numFailedBackgroundRetests}</td>

			<td><fmt:formatDate
				value="${projectJarfileMap[testRun.projectJarfilePK].datePosted}"
				pattern="E',' dd MMM 'at' hh:mm a" /></td>

			<td>
				<c:url var="downloadTestSetupLink" value="/data/instructor/DownloadProjectJarfile">
					<c:param name="projectJarfilePK" value="${testRun.projectJarfilePK}" />
				</c:url>
				<a href="${downloadTestSetupLink}">
					${testRun.projectJarfilePK}: 
					${projectJarfileMap[testRun.projectJarfilePK].comment}
				</a>
			</td>

			<td><c:url var="submissionLink"
				value="/view/instructor/submission.jsp">
				<c:param name="testRunPK" value="${testRun.testRunPK}" />
			</c:url> <a href="${submissionLink}"> view test results </a></td>
			
${ss:formattedTestResults(canonicalTestOutcomeCollection,testOutcomeCollectionMap[testRun.testRunPK])} 


		</tr>
	</c:forEach>
	</table>
	<ss:footer />
</body>
</html>
