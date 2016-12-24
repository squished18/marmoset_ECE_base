<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>


<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head
	title="Test results with failed background retests for project ${project.projectNumber}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<ss:projectTitle/>

<c:choose>
	<c:when test="${project.initialBuildStatus == 'accepted'}">
		<h1>Upload only project</h1>
		<p>This project is set up to accept uploads only, and not perform any
		server based compilation and testing</p>
	</c:when>

	<c:otherwise>

	<ss:projectMenu />
	<p>
These submissions were re-tested against the current test setup (using the "background re-test" 
mechanism) and returned different results.
		<table>

		<tr>
		<th>num</th>
		<th>class account</th>
		<th>submissionPK</th>
		<th>num successful<br>background retests</th>
		<th># inconsistent<br>background retests</th>
		</tr>

		<c:forEach var="submission" items="${failedBackgroundRetestSubmissionList}" varStatus="counter">
			<tr class="r${counter.index % 2}">
			<td class="number">${1+counter.index}</td>

			<c:url var="studentLink" value="/view/instructor/studentProject.jsp">
				<c:param name="studentPK" value="${studentRegistrationMap[submission.studentRegistrationPK].studentPK}"/>
				<c:param name="projectPK" value="${project.projectPK}"/>
			</c:url>
			<td><a href="${studentLink}">${studentRegistrationMap[submission.studentRegistrationPK].cvsAccount}</a></td>

			<c:url var="submissionAllTestsLink" value="/view/instructor/submissionAllTests.jsp">
				<c:param name="submissionPK" value="${submission.submissionPK}"/>
			</c:url>
			<td><a href="${submissionAllTestsLink}">${submission.submissionPK}</a></td>
			<td>${backgroundRetestMap[submission.submissionPK].numSuccessfulBackgroundRetests}</td>
			<td>${backgroundRetestMap[submission.submissionPK].numFailedBackgroundRetests}</td>
			</tr>
		</c:forEach>

		</table>
	</c:otherwise>

</c:choose></p>
<ss:footer />
</body>
</html>
