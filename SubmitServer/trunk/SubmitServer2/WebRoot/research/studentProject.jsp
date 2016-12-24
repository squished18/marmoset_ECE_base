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


<h2>Submissions</h2>
<table>
	
	${ss:formattedColumnHeaders(16, canonicalTestOutcomeCollection)}
	
	<tr>
		<th rowspan=2>#</th>
		<th rowspan=2>snapshot date</th>
		<th rowspan=2>submission date</th>
		<th rowspan=2>release tested</th>
		<th rowspan=2>Results</th>
		<th rowspan=2>view source</th>
		<th rowspan=2>Download</th>
		<th rowspan=2># stud. <br>tests</th>
		<th rowspan=2>FindBugs<br>delta</th>
		<th rowspan=2>test delta</th>
		<th rowspan=2>faults delta</th>
		<th rowspan=2># lines<br>changed</th>
		<th rowspan=2>net change</th>
		<th rowspan=2>time since<br>last commit</th>
		<th rowspan=2>diff file</th>
		<th rowspan=2>previous md5sum<br>classfiles</th>

		${ss:formattedTestHeaderTop(canonicalTestOutcomeCollection)}
				
		</tr>
	<tr>
		${ss:formattedTestHeader(canonicalTestOutcomeCollection)}
				
		</tr>


	<c:forEach var="submission" items="${snapshotList}"
		varStatus="counter">
		<c:url var="submissionLink"
						value="/view/instructor/submission.jsp">
						<c:param name="submissionPK" value="${submission.submissionPK}" />
					</c:url>
		<tr class="r${counter.index % 2}">
			<td>${submission.commitNumber}</td>

			<td> <fmt:formatDate value="${submission.commitTimestamp}"
				pattern="dd MMM, hh:mm a" /></td>

			<td><fmt:formatDate value="${submission.submissionTimestamp}"
				pattern="dd MMM, hh:mm a" /></td>


					
					<td><fmt:formatDate value="${submission.releaseRequest}"
						pattern="dd MMM hh:mm a" /></td>
						
					<td> <a href="${submissionLink}">
					${submission.submissionPK}
					</a></td>

			<td>
			<c:url var="viewSourceLink" value="/view/sourceFiles.jsp">
				<c:param name="submissionPK" value="${submission.submissionPK}" />
			</c:url>
			<a href="${viewSourceLink}"> view source</a>
			</td>

			<td><c:url var="downloadLink" value="/data/DownloadSubmission">
				<c:param name="submissionPK" value="${submission.submissionPK}" />
			</c:url> <a href="${downloadLink}"> download</a>
			</td>
			
			<td>
				${snapshotOutcomeCollection[submission.submissionPK].numStudentWrittenTests}
			</td>
			
			<c:choose>
				<c:when
					test="${submission.buildStatus == 'complete' && submission.compileSuccessful}">
			<td>
				${submission.findbugsDelta}
			</td>

			<td>
				${submission.testDelta}
			</td>
			
			<td>
				${submission.faultsDelta}
			</td>
			
</c:when>
<c:when test="${submission.buildStatus == 'complete'}">
					<td colspan=3><a href="${submissionLink}">did not compile</a></td>
				</c:when>
				<c:otherwise>
					<td colspan=3>not tested yet</td>
				</c:otherwise>
			</c:choose>
			<td>
				${submission.numLinesChanged}
			</td>
			
			<td>
				${submission.netChange}
			</td>
			
			<td>
				${submission.timeSinceLastCommit}
			</td>
						
			<td>
				<c:url var="diffFileLink" value="/research/PrintDiffFile">
					<c:param name="submissionPK" value="${submission.submissionPK}"/>
				</c:url>
				<a href="${diffFileLink}"> diff file</a>
			</td>
			
			<td>
				<c:url var="previousMd5sumClassfiles" value="/view/instructor/submission.jsp">
					<c:param name="submissionPK" value="${submission.previousMd5sumClassfiles}"/>
				</c:url>
				<a href="${previousMd5sumClassfiles}"> ${submission.previousMd5sumClassfiles}</a>
				<br>
				<%--
				${testRunMap[submission.submissionPK].md5sumClassfiles}
				--%>
			</td>
			
			${ss:formattedTestResults(canonicalTestOutcomeCollection,snapshotOutcomeCollection[submission.submissionPK])}

		</tr>
	</c:forEach>

</table>

<ss:footer />
</body>
</html>
