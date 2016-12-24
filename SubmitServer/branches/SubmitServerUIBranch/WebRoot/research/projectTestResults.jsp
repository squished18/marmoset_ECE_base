<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>


<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head
	title="Test results from all students for project ${project.projectNumber}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<ss:projectTitle />
<ss:projectMenu />

<%--
<h1>Project ${project.projectNumber}: ${project.title}</h1>
<p>${project.description}</p>
<p>Deadline: <fmt:formatDate value="${project.ontime}"
	pattern="E',' dd MMM 'at' hh:mm a" /> <c:if
	test="${project.ontime != project.late}">
	<p>Late deadline: <fmt:formatDate value="${project.late}"
		pattern="E',' dd MMM 'at' hh:mm a" /></p>
</c:if>
--%>
 <c:choose>
	<c:when test="${project.initialBuildStatus == 'accepted'}">
		<h1>Upload only project</h1>
		<p>This project is set up to accept uploads only, and not perform any
		server based compilation and testing</p>
	</c:when>

	<c:otherwise>
		<h1>Test Results from all Students</h1>
		<c:url var="sortByTime" value="/research/projectTestResults.jsp">
				<c:param name="projectPK" value="${project.projectPK}" />
				<c:param name="sortKey" value="time" />
			</c:url>
<c:url var="sortByName" value="/research/projectTestResults.jsp">
				<c:param name="projectPK" value="${project.projectPK}" />
			</c:url>
<c:url var="sortByAcct" value="/research/projectTestResults.jsp">
				<c:param name="projectPK" value="${project.projectPK}" />
				<c:param name="sortKey" value="account" />
			</c:url>
			<c:url var="sortByScore" value="/research/projectTestResults.jsp">
				<c:param name="projectPK" value="${project.projectPK}" />
				<c:param name="sortKey" value="score" />
			</c:url>
		<p>
		<table>
		${ss:formattedColumnHeaders(4, canonicalTestOutcomeCollection)}
		<tr>
		<th class="number"><a title="position in list">#</a></th>
		<th><a href="${sortByName}" title="sort by name">Name</a></th>
		<th><a href="${sortByAcct}" title="sort by account">Acct</a></th>
		<th><a title="# of snapshots for this project">#<br>snapshots</a></th>
		<%--
		<th><a href="${sortByTime}" title="sort by time of last submission">submitted at</a></th>
		<th class="number" rowspan="2"><a href="${sortByScore}" title="sort by score">Score</a>
				${ss:formattedTestHeaderTop(canonicalTestOutcomeCollection)}
				
			</tr>
			<tr>
				${ss:formattedTestHeader(canonicalTestOutcomeCollection)}
			</tr>
		--%>
			<c:forEach var="studentRegistration"
				items="${studentRegistrationSet}" varStatus="counter">
				<tr class="r${counter.index % 2}">
				<td class="number">${1+counter.index}
					<td class="description"><c:if
						test="${studentRegistration.instructorLevel > 0}">* </c:if>${studentRegistration.lastname},
					${studentRegistration.firstname}</td>
					<td class="description">
					<c:url var="studentProjectLink"
						value="/research/studentProject.jsp">
						<c:param name="projectPK" value="${project.projectPK}" />
						<c:param name="studentPK" value="${studentRegistration.studentPK}" />
					</c:url> <a href="${studentProjectLink}" title="view all submissions by ${studentRegistration.cvsAccount}">
					${studentRegistration.cvsAccount} </a></td>
					<td>${studentSubmitStatusMap[studentRegistration.studentRegistrationPK].numberCommits}
			<%--
					<td><c:url var="submissionLink"
						value="/view/research/submission.jsp">
						<c:param name="submissionPK"
							value="${lastSubmission[studentRegistration.studentRegistrationPK].submissionPK}" />
					</c:url> <a href="${submissionLink}" title="view this submission"> <fmt:formatDate
						value="${lastSubmission[studentRegistration.studentRegistrationPK].submissionTimestamp}"
						pattern="E',' dd MMM 'at' hh:mm a" /></a></td>
						
						<td class="number">${lastSubmission[studentRegistration.studentRegistrationPK].valuePassedOverall}</td>
						
				${ss:formattedTestResults(canonicalTestOutcomeCollection,lastOutcomeCollection[studentRegistration.studentRegistrationPK])}
			--%>		

				</tr>
			</c:forEach>

		</table>
	</c:otherwise>

</c:choose></p>

</body>
</html>
