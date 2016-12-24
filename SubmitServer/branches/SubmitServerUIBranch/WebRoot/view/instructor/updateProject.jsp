<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head title="Update project ${project.projectNumber} for ${course.courseName}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<p>Updating ${project.projectNumber} for ${course.courseName} in semester
${course.semester}:

<form class="form"
	action="<c:url value="/action/instructor/UpdateProject"/>"
	method="POST" name="updateProjectForm">

	<input type="hidden" name="coursePK" value="${course.coursePK}"/>
	<input type="hidden" name="projectPK" value="${project.projectPK}"/>
	<input type="hidden" name="visibleToStudents" value="${project.visibleToStudents}"/>
	<input type="hidden" name="projectJarfilePK" value="${project.projectJarfilePK}"/>
	<input type="hidden" name="previousInitialBuildStatus" value="${project.initialBuildStatus}"/>
	<input type="hidden" name="archivePK" value="${project.archivePK}"/>
	
<table>
	<tr>
		<td><b>project Number</b></td>
		<td><INPUT TYPE="text" NAME="projectNumber" value="${project.projectNumber}"></td>
	</tr>
	<tr>
		<td><b>on-time deadline</b></td>
		<td><INPUT TYPE="text" NAME="ontime" VALUE="${project.ontime}"></td>
	</tr>
	<tr>
		<td><b>late deadline</b></td>
		<td><INPUT TYPE="text" NAME="late" VALUE="${project.late}"></td>
	</tr>
	<tr>
		<td>
			<b>Post-Deadline Outcome Visibility</b><br>
			Set to 'everything' if you want <br>
			students to see<b> all </b> outcomes <br>
			(public, release and secret) <br>
			after the deadline passes.
		</td>
		<td>
		<input type="radio" name="postDeadlineOutcomeVisibility" value="everything" ${ss:checked(project.postDeadlineOutcomeVisibility,'everything')}> Everything<br>
		<input type="radio" name="postDeadlineOutcomeVisibility" value="nothing" ${ss:checked(project.postDeadlineOutcomeVisibility,'nothing')}> Nothing<br>
		</td>
	</tr>
	<tr>
		<td><b>project title</b></td>
		<td><INPUT TYPE="text" NAME="title" VALUE="${project.title}"></td>
	</tr>
	<tr>
		<td><b>URL</b></td>
		<td><INPUT TYPE="text" NAME="url" VALUE="${project.url}"></td>
	</tr>
	<tr>
		<td><b>description</B></td>
		<td><INPUT TYPE="text" NAME="description" VALUE="${project.description}"></td>
	</tr>

	<tr>
		<td> <b>stack trace policy</b><br>
			(how much information to reveal for a release test)
		</td>
		<td class="left"> 
			<input type="radio" name="stackTracePolicy" value="test_name_only" ${ss:checked(project.stackTracePolicy,'test_name_only')} >
			name of test only (default)<br>
			<input type="radio" name="stackTracePolicy" value="exception_location" ${ss:checked(project.stackTracePolicy,'exception_location')}>
			the line number in student's code where a runtime exception happens<br>
			<input type="radio" name="stackTracePolicy" value="restricted_exception_location" ${ss:checked(project.stackTracePolicy,'restricted_exception_location')}>
			(Java-only) the line number in student's code where a runtime exception happens, if it's covered by a student or public test<br>
			<input type="radio" name="stackTracePolicy" value="full_stack_trace" ${ss:checked(project.stackTracePolicy,'full_stack_trace')}>
			the entire stack trace for Java or full output printed to stdout for C<br>
		</td>
	</tr>
	
	<tr>
		<td><b>release policy</b><br>
			(when students can request release tests)
		</td>
		<td class="left">
			<input type="radio" name="releasePolicy" value="after_public"
			${ss:checked(project.releasePolicy,'after_public')}>
			after passing all public tests (default)<br>
			<input type="radio" value="anytime"
			${ss:checked(project.releasePolicy,'anytime')}>
			anytime<br>
	</tr>

	<tr>
		<td><b>best submission policy</b></td>
		<td class="left">
			<input type="radio" name="bestSubmissionPolicy" value="edu.umd.cs.submitServer.BestBestSubmissionPolicy"
			${ss:checked(project.bestSubmissionPolicy,'edu.umd.cs.submitServer.BestBestSubmissionPolicy')}>
				Best: Best submission for a category (ontime or late) is the submission with the max mark.<br>
			<input type="radio" name="bestSubmissionPolicy" value="edu.umd.cs.submitServer.DefaultBestSubmissionPolicy"
			${ss:checked(project.bestSubmissionPolicy,'edu.umd.cs.submitServer.DefaultBestSubmissionPolicy')}>
				Default: Best submission for a category (ontime or late) is the last compilable submission.<br>
			<input type="radio" name="bestSubmissionPolicy" value="edu.umd.cs.submitServer.ReleaseTestAwareBestSubmissionPolicy"
			${ss:checked(project.bestSubmissionPolicy,'edu.umd.cs.submitServer.ReleaseTestAwareBestSubmissionPolicy')}>
				Release Test Aware: Best submission for a category (ontime or late) is 
				max of last compilable version and best release-tested submission.<br>
			<!-- 
			Note that if a project has secret tests, students will not know which
			submission is their "best" one.
			-->
		</td>
	</tr>
	<tr>
		<td><b># release tests to reveal</b><br>
			(how many release tests to reveal the students when they use a token)
		</td>
		<td>
			<select name="numReleaseTestsRevealed">
	<%--
		Hack alert:  "all of them" is stored in the DB as -1, but comes out as Integer.MAX_VALUE
		So I'm putting it first so that it's the default selected one if none of the others
		are selected.
	--%>
			<option value="-1">all of them</option>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,1)}>1</OPTION>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,2)}>2</OPTION>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,3)}>3</OPTION>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,4)}>4</OPTION>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,5)}>5</OPTION>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,6)}>6</OPTION>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,7)}>7</OPTION>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,8)}>8</OPTION>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,9)}>9</OPTION>
			<OPTION ${ss:selected(project.numReleaseTestsRevealed,10)}>10</OPTION>
			</select>
		</td>
	</tr>
	<tr>
		<td><b># release tokens</b></td>
		<td><INPUT TYPE="text" NAME="releaseTokens" VALUE="${project.releaseTokens}"></td>
	</tr>

	<tr>
		<td><b>regeneration time</b> (hours)</td>
		<td><INPUT TYPE="text" NAME="regenerationTime" VALUE="${project.regenerationTime}"></td>
	</tr>
	<tr>
		<td><b>Project kind</b><br>
		Projects marked 'Upload and Test' <br>
		will be automatically tested, <br>
		while projects marked <br>
		'Upload-only' are not tested.<br>
		NOTE: Changing the project kind to 'Upload and Test' after students have submitted
		to an 'Upload-only' project is not a good idea and will not cause previous submissions
		to be tested properly.
		</td>
		<td>
		<input type="radio" name="initialBuildStatus" value="new" ${ss:checked(project.initialBuildStatus,'new')}> Upload and Test<br>
		<input type="radio" name="initialBuildStatus" value="accepted" ${ss:checked(project.initialBuildStatus,'accepted')}> Upload-only
		</td>
	</tr>
	<tr>
		<td><b>kind of late penalty</b></td>
		<td><SELECT name="kindOfLatePenalty">
			<option ${ss:checked(project.kindOfLatePenalty,'constant')}>constant</option>
			<option ${ss:checked(project.kindOfLatePenalty,'multiplier')}>multiplier</option>
			</SELECT>
		</td>
	</tr>
	<tr>
		<td>
			<b>Late Constant</b><br>
			How many points	to subtract<br>
			from a late submission
		</td>
		<td><input type="text" name="lateConstant" value="${project.lateConstant}" /></td>
	</tr>

	<tr>
		<td>
			<b>Late Multiplier</b><br>
			Fraction by which to multiply<br>
			a late submission
		</td>
		<td><input type="text" name="lateMultiplier" value="${project.lateMultiplier}" /></td>
	</tr>

	<tr>
		<td><b>canonical class account</b></td>
		<td>
			<c:forEach var="studentRegistration" items="${canonicalAccountCollection}">
				<c:if test="${studentRegistration.studentRegistrationPK == project.canonicalStudentRegistrationPK}">
					${studentRegistration.cvsAccount} <br><b>NOTE:</b>You cannot change the canonical account
					<input type="hidden" name="canonicalStudentRegistrationPK" value="${project.canonicalStudentRegistrationPK}"/>
				</c:if>
			</c:forEach>
		</td>
	</tr>

	<tr>
		<td colspan=2 align=center><input type=submit value="Update Project"></td>
	</tr>
</table>

<%--
<c:forEach var="studentRegistration" items="${studentRegistrationList}">
${studentRegistration.cvsAccount}, level: ${studentRegistration.instructorLevel}<br>
</c:forEach>
--%>
</form>
<ss:footer />

</body>
</html>
