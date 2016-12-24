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

<h1>Project ${project.projectNumber}: ${project.title}</h1>


<form method="get" action="<c:url value="/research/snapshot.jsp"/>">
Course:
	<select name="coursePK" onchange="<c:url value="/research/snapshot.jsp"/>">
		<c:forEach var="course" items="courseList">
		${course}
<%--
		<option value="${course.coursePK}">${course.courseName}</option>
--%>
		</c:forEach>
	</select>
<br>

Project: 
	<select name="projectPK" onchange="<c:url value="/research/snapshot.jsp"/>">
		<c:forEach var="project" items="projectList">
		${project}
		<%--
		<option value="${project.projectPK}">${project.projectNumber}</option>
		--%>
		</c:forEach>
	</select>
<br>

<table>
	<tr>
		<th># lines changed</th>
	</tr>
	<tr>
		<td><input type="text" name="numLinesChanged" value="${param.numLinesChanged}"></td>
	</tr>
</table>
<input type="hidden" name="projectPK" value="${project.projectPK}">
<input type="submit" value="OK">
</form>

<table>

	${ss:formattedColumnHeaders(16, canonicalTestOutcomeCollection)}
	

<%--
	<tr>
		<th></th>
		<th><input name="snapshotDate" type="text"></th>
		<th><input name="submissionDate" type="text"></th>
		<th><input name="releaseTestDate" type="text"></th>
		<th><input name="submissionPK" type="text"></th>
		<th></th>
		<th></th>
		<th><input name="findBugsDelta" type="text"></th>
		<th><input name="testDelta" type="text"></th>
		<th><input name="faultsDelta" type="text"></th>
		<th><input name="numLinesChanged" type="text"></th>
		<th><input name="netChange" type="text"></th>
		<th><input name="timeSinceLastCommit" type="text"></th>
		<th></th>
		<th><input name="previousMd5sumClassfiles" type="text"></th>
		<th><input name="previousMd5sumSourcefiles" type="text"></th>
	</tr>
--%>
	<tr>
		<th>#</th>
		<th>snapshot date</th>
		<th>submission date</th>
		<th>release tested</th>
		<th>Results</th>
		<th>view source</th>
		<th>Download</th>
		<th>FindBugs<br>delta</th>
		<th>test delta</th>
		<th>faults delta</th>
		<th># lines<br>changed</th>
		<th>net change</th>
		<th>time since<br>last commit</th>
		<th>diff file</th>
		<th>previous md5sum<br>classfiles</th>
		<th>previous md5sum<br>sourcefiles</th>
		${ss:formattedTestHeaderTop(canonicalTestOutcomeCollection)}
	</tr>
	<c:forEach var="snapshot" items="${snapshotList}">
	<tr>
		<td>${snapshot.commitNumber}</td>
		<td>${snapshot.commitTimestamp}</td>
		<td>${snapshot.releaseRequest}</td>
		<td>${snapshot.submissionPK}</td>
		<td></td>
		<td></td>
		<td>${snapshot.findbugsDelta}</td>
		<td>${snapshot.numLinesChanged}</td>
		<td></td>
		<td></td>
		<td></td>
		<td></td>
		<td></td>
	</tr>
	</c:forEach>
</table>

<ss:footer />
</body>
</html>
