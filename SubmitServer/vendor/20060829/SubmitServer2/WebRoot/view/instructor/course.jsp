
<%@ page language="java"%>


<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head title="Instructor view of course ${course.courseName}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<h1>Intructor view of <a href="${course.url}">${course.courseName}</a>,
${course.semester}: ${course.description}</h1>
<p>Welcome ${user.firstname}
<h2>Projects</h2>
<p>
<table>
	<tr>
		<th>Project</th>
		<th>Overview</th>
		<th>testing<br>
		setup</th>
		<th> # to test</th>
		<th> # retesting</th>
		<th>Visible</th>
		<th>Due</th>
		<th class="description">Title</th>
	</tr>

	<c:forEach var="project" items="${projectList}" varStatus="counter">
		<tr class="r${counter.index % 2}">

			<td>${project.projectNumber}</td>

			<td><c:url var="projectLink" value="/view/instructor/project.jsp">
				<c:param name="projectPK" value="${project.projectPK}" />
			</c:url> <a href="${projectLink}"> view </a></td>

			<td>
			<c:choose>
			<c:when test="${project.initialBuildStatus == 'new' && project.projectJarfilePK > 0}">
			active
			</c:when>
			<c:when test="${project.initialBuildStatus == 'new'}">
			<c:url var="uploadProjectJarfileLink"
				value="/view/instructor/uploadProjectJarfile.jsp">
				<c:param name="projectPK" value="${project.projectPK}" />
			</c:url> 
			<a href="${uploadProjectJarfileLink}"> upload </a>
			</c:when>
			</c:choose>
			</td>
			
			<td>
				${ss:numToTest(project.projectPK, connection)}
			</td>

			<td>
				${ss:numForRetest(project.projectPK, connection)}
			</td>

			<td>${project.visibleToStudents}</td>
			<td><fmt:formatDate value="${project.ontime}"
				pattern="dd MMM, hh:mm a" /></td>
			<td class="description">${project.title}</td>
		</tr>
	</c:forEach>
	<tr> <td colspan=8> <b>NOTE:</b> The instructor's "modify" privilege is required to create
	projects.
	</td>
	</tr>
	<tr>
		<td colspan=8><c:url var="createProjectLink"
			value="/view/instructor/createProject.jsp">
			<c:param name="coursePK" value="${course.coursePK}" />
		</c:url> <a href="${createProjectLink}"> create new project </a></td>
	</tr>
	<tr>
		<td colspan=8>
		Import a project 
		<form name="importProjectForm" enctype="multipart/form-data" method="post" action="/action/instructor/ImportProject">
		Canonical Account: 
		<select name="canonicalStudentRegistrationPK">
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
		<br>
		<input type="file" name="file" size=40 /><br>
		<input type=submit value="Import project!">
		<input type="hidden" name="coursePK" value="${course.coursePK}">
		</form>
		</td>
	</tr>
<!-- 
	<tr>
		<td colspan=8>

		</td>
	</tr>
-->
</table>

<h2>People</h2>
<table>
	<tr> <td colspan=6> <b>NOTE:</b> The instructor's "modify" privilege is required to
	register new students or TAs for the course
	</td>
	</tr>
	<tr>
		<td colspan=6><c:url var="registerStudentsLink"
			value="/view/instructor/registerStudents.jsp">
			<c:param name="coursePK" value="${course.coursePK}" />
		</c:url> <a href="${registerStudentsLink}"> Register students for this
		course by uploading a text file </a></td>
	</tr>
	<tr>
		<td colspan=6><c:url var="registerOneStudentLink"
			value="/view/instructor/registerOneStudent.jsp">
			<c:param name="coursePK" value="${course.coursePK}" />
		</c:url> <a href="${registerOneStudentLink}"> Register one student or TA for this
		course using a web interface</a></td>
	</tr>
	<tr>
		<td colspan=6>
			<c:url var="registerInstructorLink" value="/view/instructor/registerInstructor.jsp">
				<c:param name="coursePK" value="${course.coursePK}"/>
			</c:url>
			<a href="${registerInstructorLink}"> Register an Instructor for this course 
			using a web interface</a>
		</td>
	</tr>

	<tr>
	<th>Name</th>
	<th>class account</th>
	<th>instructor rights</th>
	<th>add TA <br>permission</th>
	<th>remove <br>TA permission</th>
	<th>edit student<br>registration</th>
	</tr>
	<c:forEach var="studentRegistration" items="${studentRegistrationSet}" varStatus="counter">
			<tr class="r${counter.index % 2}">
			<c:url var="studentLink" value="/view/instructor/student.jsp">
				<c:param name="studentPK" value="${studentRegistration.studentPK}" />
				<c:param name="coursePK" value="${course.coursePK}" />
			</c:url>
			<td class="description"><c:if test="${studentRegistration.instructorLevel > 0}">* </c:if> 
			<a href="${studentLink}">${studentRegistration.lastname}, ${studentRegistration.firstname} </a>
			<td>
			<a href="${studentLink}">${studentRegistration.cvsAccount}</a>
			<td>
			 ${studentRegistration.instructorCapability}
			</td>
			<td>
	<%--
	NOTE: 	// Using request parameter 'targetStudentRegistrationPK' rather than 
            // 'studentRegistrationPK' because the ExtractParametersFilter fetches
            // the studentRegistration record and then the InstructorActionFilter
            // assumes that is who is making the request
	--%>
			<c:url var="addTAPermissionLink" value="/action/instructor/ChangePermission">
				<c:param name="targetStudentRegistrationPK" value="${studentRegistration.studentRegistrationPK}"/>
				<c:param name="permissionType" value="read-only"/>
				<c:param name="action" value="add"/>
				<c:param name="coursePK" value="${studentRegistration.coursePK}"/>
			</c:url>
			<a href="${addTAPermissionLink}"> add </a>
			</td>
			<td>
			<c:url var="removeTAPermissionLink" value="/action/instructor/ChangePermission">
				<c:param name="targetStudentRegistrationPK" value="${studentRegistration.studentRegistrationPK}"/>
				<c:param name="permissionType" value="read-only"/>
				<c:param name="action" value="remove"/>
				<c:param name="coursePK" value="${studentRegistration.coursePK}"/>
			</c:url>
			<a href="${removeTAPermissionLink}"> remove </a>
			</td>
			<td>
			<c:url var="editRegistrationLink" value="/view/instructor/editStudentRegistration.jsp">
				<c:param name="studentRegistrationPK" value="${studentRegistration.studentRegistrationPK}"/>
			</c:url>
			<a href="${editRegistrationLink}"> edit </a>
			</td>
			</c:forEach>
			</table>
<ss:footer />
</body>
</html>
