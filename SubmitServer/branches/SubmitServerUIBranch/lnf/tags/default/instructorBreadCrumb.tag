<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt" %>
<div class="breadcrumb">
<div class="logout">
<p><a href="<c:url value='/authenticate/Logout'/>">Logout</a></p>
</div>
<p>${user.campusUID} :

<c:if test="${user.superUser}">
 <a href="<c:url value='/view/admin/index.jsp'/>" title="Access superuser capabilities">SuperUser</a> | 
 <a href="<c:url value='/status/QueryBuildServerStatus'/>" title="Query the build server status"> BuildServerStatus</a> | 
</c:if>

<c:if test="${initParam['authentication.service']=='edu.umd.cs.submitServer.GenericStudentPasswordAuthenticationService' || user.password != NULL}">
	<c:url var="changePasswordLink" value="/view/changePassword.jsp">
		<c:param name="studentPK" value="${user.studentPK}"/>
	</c:url>
	<a href="${changePasswordLink}"> Change Password </a> |
</c:if>

<c:if test="${user.superUser}">
	<a href="<c:url value='/docs/MarmosetHelp/'/>" title="Access documentation" target="_blank"> Admin Documentation </a> |
</c:if>
<c:if test="${!user.superUser}">
	<a href="<c:url value='/docs/MarmosetHelp/submitserver_usersguide.html'/>" title="Access documentation" target="_blank"> Instructor Documentation </a> |
</c:if>


<c:if test="${!singleCourse}">
 <a href="<c:url value='/view/index.jsp'/>" title="view all courses you are registered for">All courses</a> | 
</c:if>

		<c:url var="courseStudentLink" value="/view/course.jsp">
                    <c:param name="coursePK" value="${course.coursePK}"/>
			</c:url>
			<a href="${courseStudentLink}" title="Your view as a student of this course">Student view</a>
			
<c:url var="courseLink" value="/view/instructor/course.jsp">
                    <c:param name="coursePK" value="${course.coursePK}"/>
			</c:url>
			| <a href="${courseLink}" title="Instructor overview of ${course.courseName}">
					${course.courseName}
			</a> 
		
<c:if test="${project != null}">
<c:url var="projectLink" value="/view/instructor/project.jsp">
                    <c:param name="projectPK" value="${project.projectPK}"/>
			</c:url>
			| <a href="${projectLink}" 
			title="Instructor overview of project ${project.projectNumber}">
					${project.projectNumber}
			</a> 
			
	
			
			
			<c:if test="${instructorViewOfStudent || submission != null}">
<c:url var="studentLink" value="/view/instructor/studentProject.jsp">
                      <c:param name="projectPK" value="${project.projectPK}"/>
                     <c:param name="studentPK" value="${student.studentPK}"/>
			</c:url>
			| <a href="${studentLink}" title="Instructor view of this students work on project">
					${student.firstname} ${student.lastname} (${studentRegistration.cvsAccount})
			</a> 
			
			
			<c:if test="${submission != null}">
<c:url var="submissionLink" value="/view/instructor/submission.jsp">
                    <c:param name="submissionPK" value="${submission.submissionPK}"/>
			</c:url>
			| <a href="${submissionLink}" title="Instructor view of this submission">
					<fmt:formatDate value="${submission.submissionTimestamp}" pattern="dd MMM, hh:mm a"/>
			</a> 
</c:if>	
</c:if>	
			
</c:if>
</p>
</div>