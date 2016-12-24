
<%@ page language="java" import="java.sql.Timestamp"%>
<%@ page language="java" import="java.util.List"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head
	title="All submissions for ${course.courseName} project ${project.projectNumber}" />

<body>
<ss:header />
<ss:breadCrumb />

<ss:projectTitle/>

<br>
<c:url var="submitProjectLink" value="/view/submitProject.jsp">
					<c:param name="projectPK" value="${project.projectPK}" />
					<c:param name="projectJarfilePK" value="${project.projectJarfilePK}" />
				</c:url> <h3><a href="${submitProjectLink}">Submit</a></h3>
<h2>Submissions</h2>
<table>
	<tr>
		<th>#</th>
		<th>submitted</th>
		<%--
			Only display anything about automated testing if this project supports
			automated testing.  Many people use the server simply to store submissions.
		--%>
		<c:if test="${project.initialBuildStatus == 'new'}">
			<th>
				<c:choose>
				<c:when test="${projectJarfile.valuePublicTests > 0}">
					public tests <br>score
				</c:when>
				<c:otherwise>
				result
				</c:otherwise>
				</c:choose>
			</th>
			<c:if test="${projectJarfile.valueReleaseTests > 0}">
				<th>release tests <br>
				score</th>
				<th>release tested</th>
			</c:if>
			<th>
				detailed<br>
				test results
			</th>
		</c:if>
		<th>Download</th>
	</tr>

	<c:forEach var="submission" items="${submissionList}"
		varStatus="counter">
		<c:url var="submissionLink" value="/view/submission.jsp">
			<c:param name="submissionPK" value="${submission.submissionPK}" />
		</c:url>
		<c:url var="submissionAllTestsLink" value="/view/submissionAllTests.jsp">
			<c:param name="submissionPK" value="${submission.submissionPK}"/>
		</c:url>

		<tr class="r${counter.index % 2}">
			<td>${submission.submissionNumber}</td>

			<td><fmt:formatDate value="${submission.submissionTimestamp}"
				pattern="E',' dd MMM yyyy 'at' hh:mm a" /></td>
			<c:choose>
				<c:when
					test="${submission.buildStatus == 'accepted'}">
					</c:when>
				<c:when
				test="${submission.buildStatus == 'complete' && submission.compileSuccessful}">

				<td>
				<c:choose>
				<c:when test="${projectJarfile.valuePublicTests > 0}">
				${submission.valuePublicTestsPassed} / ${testOutcomesMap[submission.submissionPK].valuePublicTests}
				</c:when>
				<c:otherwise>
				compiled
				</c:otherwise>
				</c:choose>
				</td>
			<c:choose>
			<c:when test="${projectJarfile.valueReleaseTests == 0}">
			</c:when>
			<c:otherwise>
				<c:choose>
				<c:when test="${submission.releaseTestingRequested}">
					<td>${submission.valueReleaseTestsPassed} / ${testOutcomesMap[submission.submissionPK].valueReleaseTests}
					</td>
				</c:when>
				<c:otherwise>
					<td>?</td>
				</c:otherwise>
				</c:choose>
				<td><fmt:formatDate value="${submission.releaseRequest}"
					pattern="E',' dd MMM 'at' hh:mm a" /></td>
			</c:otherwise>
			</c:choose>
		<td><a href="${submissionLink}">view</a></td>
		</c:when>
		<c:when test="${submission.buildStatus == 'complete'}">
			<td colspan="${projectJarfile.valueReleaseTests > 0 ? 3 : 1}">did
				not compile</td>
			<td><a href="${submissionLink}">view</a></td>
		</c:when>
		<c:otherwise>
			<td colspan="${projectJarfile.valueReleaseTests > 0 ? 4 : 2}">not
			tested yet</td>
		</c:otherwise>
		</c:choose>

		<td><c:url var="downloadLink" value="/data/DownloadSubmission">
			<c:param name="submissionPK" value="${submission.submissionPK}" />
		</c:url> <a href="${downloadLink}"> download</a></td>
		</tr>
	</c:forEach>

</table>

<ss:inconsistentBackgroundRetestDescription />

<ss:footer />
</body>
</html>
