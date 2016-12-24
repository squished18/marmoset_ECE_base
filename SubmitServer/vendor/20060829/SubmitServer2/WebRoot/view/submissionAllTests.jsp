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
<ss:breadCrumb />

<ss:projectTitle />

<p>Submission #${submission.submissionNumber}, submitted at <fmt:formatDate
	value="${submission.submissionTimestamp}"
	pattern="E',' dd MMM 'at' hh:mm a" />
<h1>All testing results<br></h1>

<p>
<table>
	<tr>
		<th colspan=3>Test setup
		<th rowspan=2>date ran</th>
		<th rowspan=2>view results</th>
	<tr>
		<th>version</th>
		<th>test setup tested</th>
		<th class="description">comment</th>
	</tr>

	<c:forEach var="testRun" items="${testRunList}" varStatus="counter">
		<tr class="r${counter.index % 2}">


			<td>${projectJarfileMap[testRun.projectJarfilePK].version}</td>
			<td><fmt:formatDate
				value="${projectJarfileMap[testRun.projectJarfilePK].datePosted}"
				pattern="E',' dd MMM 'at' hh:mm a" /></td>

			<td class="description">
			${projectJarfileMap[testRun.projectJarfilePK].comment}</td>
			<td><fmt:formatDate value="${testRun.testTimestamp}"
				pattern="dd MMM KK:mm:ss a" /></td>
			<td><c:url var="submissionLink" value="/view/submission.jsp">
				<c:param name="testRunPK" value="${testRun.testRunPK}" />
			</c:url> <a href="${submissionLink}"> <c:choose>
				<c:when test="${testRun.compileSuccessful}">
            ${testRun.valuePublicTestsPassed} 
					<c:choose>
					<c:when test="${submission.releaseTestingRequested && testOutcomeCollection.passedAllPublicTests}">
						/ ${submission.valueReleaseTestsPassed}
					</c:when>
					<c:otherwise>
					/ ?
					</c:otherwise>
					</c:choose>

				</c:when>
				<c:otherwise>
			did not compile
			</c:otherwise>
			</c:choose> </a></td>

		</tr>
	</c:forEach>
</table>

<ss:inconsistentBackgroundRetestDescription/>

<ss:footer />
</body>
</html>
