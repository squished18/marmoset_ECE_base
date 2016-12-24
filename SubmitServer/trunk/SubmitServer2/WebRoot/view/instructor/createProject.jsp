<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<%-- uri="http://java.sun.com/jstl/core" --%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head title="Create new project for ${course.courseName}" />

<body>
<ss:header />
<ss:instructorBreadCrumb />

<p>Creating a new project for ${course.courseName} in semester
${course.semester}: <%--
	Previous projects names in this course:
	<c:forEach var="project" items="${projectList}">
	${project.projectNumber}
	<p>
	</c:forEach>
	--%>
<p>

<form class="form"
	action="<c:url value="/action/instructor/CreateProject"/>"
	method="POST" name="createProjectForm">

	<input type="hidden" name="coursePK" value="${course.coursePK}">
	<input type="hidden" name="postDeadlineOutcomeVisibility" value="nothing"/>
	<input type="hidden" name="visible" value="no">
	
	
<table>
	<tr>
		<td>project Number</td>
		<td><INPUT TYPE="text" NAME="projectNumber"></td>
	</tr>
	<tr>
		<td>on-time deadline</td>
		<td><INPUT TYPE="text" NAME="ontime" VALUE="yyyy-mm-dd hh:mm:ss"></td>
	</tr>
	<tr>
		<td>late deadline</td>
		<td><INPUT TYPE="text" NAME="late" VALUE="yyyy-mm-dd hh:mm:ss"></td>
	</tr>
	<tr>
		<td>project title</td>
		<td><INPUT TYPE="text" NAME="title"></td>
	</tr>
	<tr>
		<td>URL</td>
		<td><INPUT TYPE="text" NAME="url"></td>
	</tr>
	<tr>
		<td>description</td>
		<td><INPUT TYPE="text" NAME="description"></td>
	</tr>

	<tr>
		<td> stack trace policy<br>
			(how much information to reveal for a release test)
		</td>
		<td class="left">
		<input type="radio" name="stackTracePolicy" value="test_name_only" checked="checked">
		name of test only (default)
		<br>
		<input type="radio" name="stackTracePolicy" value="exception_location">
		the line number in student's code where a runtime exception happens
		<br>
		<input type="radio" name="stackTracePolicy" value="restricted_exception_location">
		(Java-only) the line number in student's code where a runtime exception happens, if it's covered by a student or public test
		<br>
		<input type="radio" name="stackTracePolicy" value="full_stack_trace">
		the entire stack trace for Java or full output printed to stdout for C
		<br>
		
<!-- 
		 <select name="stackTracePolicy">
			<option value="test_name_only" selected>name of test only (default)</option>
			<option value="exception_location"> the line number in student's 
				code where a runtime exception happens
			</option>
			<option value="restricted_exception_location">
			(Java-only) the line number in student's code where a runtime exception happens, if it's covered by a student or public test
			</option>
			<option value="full_stack_trace"> the entire stack trace for Java or full output printed to stdout for C</option>
			</select>
-->
		</td>
	</tr>
	<tr>
		<td>release policy<br>
			(when students can request release tests)
		</td>
		<td>
			<select name="releasePolicy">
			<option value="after_public">after passing all public tests (default)</option>
			<option value="anytime">anytime</option>
			</select>
	</tr>	

	<tr>
		<td>best submission policy</td>
		<td class="left">
                <input type="radio" name="bestSubmissionPolicy" value="edu.umd.cs.submitServer.BestBestSubmissionPolicy" checked="checked">
                Best: Best submission for a category (ontime or late) is the submission with the max mark.<br>
		<input type="radio" name="bestSubmissionPolicy" value="edu.umd.cs.submitServer.DefaultBestSubmissionPolicy">
		Default: Best submission for a category (ontime or late) is the last compilable
		submission.
		<br>
		<input type="radio" name="bestSubmissionPolicy" value="edu.umd.cs.submitServer.ReleaseTestAwareBestSubmissionPolicy">
		Release Test Aware: Best submission for a category (ontime or late) is the
		max of the last compilable version and the best release-tested submission.
		<br>
		
<%--
		<select name="bestSubmissionPolicy">
		
			<option value="edu.umd.cs.submitServer.DefaultBestSubmissionPolicy" selected>
				Default: Best submission for a category (ontime or late) is the last compilable
					submission.
			</option>
			<option value="edu.umd.cs.submitServer.ReleaseTestAwareBestSubmissionPolicy">
				Release Test Aware: Best submission for a category (ontime or late) is the<br>
					max of the last compilable version and the best release-tested submission.
			</option>
			<!-- 
			Note that if a project has secret tests, students will not know which
			submission is their "best" one.
			-->
			</select>
--%>
		</td>
	</tr>
	<tr>
		<td># release tests to reveal<br>
			(how many release tests to reveal the students when they use a token)
		</td>
		<td>
			<select name="numReleaseTestsRevealed">
			<OPTION>1</OPTION>
			<OPTION selected value="2">2 (default)</OPTION>
			<OPTION>3</OPTION>
			<OPTION>4</OPTION>
			<OPTION>5</OPTION>
			<OPTION>6</OPTION>
			<OPTION>7</OPTION>
			<OPTION>8</OPTION>
			<OPTION>9</OPTION>
			<OPTION>10</OPTION>
			<option value="-1">all of them</option>
			</select>
		</td>
	</tr>
	<tr>
		<td>number of release tokens</td>
		<td><SELECT NAME="releaseTokens">
			<OPTION>1</OPTION>
			<OPTION>2</OPTION>
			<OPTION SELECTED>3</OPTION>
			<OPTION>4</OPTION>
			<OPTION>5</OPTION>
			<OPTION>6</OPTION>
		</SELECT></td>
	</tr>

	<tr>
		<td>regeneration time (hours)</td>
		<td><SELECT NAME="regenerationTime">
			<OPTION selected>12</OPTION>
			<OPTION>24</OPTION>
			<OPTION>36</OPTION>
			<OPTION>48</OPTION>
		</SELECT></td>
	</tr>
	<tr>
		<td>Project kind<br>
		</td>
		<td><SELECT NAME="initialBuildStatus">
			<OPTION value="new" SELECTED>compiled (and optionally tested) on server</OPTION>
			<OPTION value="accepted">upload only</OPTION>
		</SELECT></td>
	</tr>
	<tr>
		<td>kind of late penalty:</td>
		<td><select name="kindOfLatePenalty">
			<option selected>constant</option>
			<option>multiplier</option>
		</select></td>
	</tr>

	<tr>
		<td>
			Late Constant<br>
			How many points	to subtract<br>
			from a late submission
		</td>
		<td><input type="text" name="lateConstant" value="0" /></td>
	</tr>

	<tr>
		<td>
			Late Multiplier<br>
			Fraction by which to multiply<br>
			a late submission
		</td>
		<td><input type="text" name="lateMultiplier" value="0" /></td>
	</tr>

	<tr>
		<td>canonical class account</td>
		<td><select name="canonicalStudentRegistrationPK">
			<c:forEach var="studentRegistration" items="${canonicalAccountCollection}">
				<c:if test="${studentRegistration.canonical}">
				<c:choose>
					<c:when test="${studentRegistration.studentRegistrationPK == project.canonicalStudentRegistrationPK}">
					<option value="${studentRegistration.studentRegistrationPK}" selected="selected">
					${studentRegistration.cvsAccount} </option>
					</c:when>
					<c:otherwise>
					<option value="${studentRegistration.studentRegistrationPK}">
					${studentRegistration.cvsAccount} </option>
					</c:otherwise>
				</c:choose>
				</c:if>
			</c:forEach>
		</select>
		</td>
	</tr>

	<tr>
		<td colspan=2><input type=submit value="Create Project"></td>
	</tr>
</table>
</form>
<ss:footer />
</body>
</html>
