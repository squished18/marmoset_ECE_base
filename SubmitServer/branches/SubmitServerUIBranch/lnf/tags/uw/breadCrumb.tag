<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt" %>
<div class="breadcrumb">
<div class="logout">
<p><a href="<c:url value='/authenticate/Logout'/>">Logout</a></p>
</div>
<p>${user.campusUID} :

<c:if test="${user.superUser}">
 <a href="<c:url value='/view/admin/index.jsp'/>"> SuperUser </a> |
</c:if>

<c:if test="${initParam['authentication.service']=='edu.umd.cs.submitServer.GenericStudentPasswordAuthenticationService' || user.password != NULL}">
	<c:url var="changePasswordLink" value="/view/changePassword.jsp">
		<c:param name="studentPK" value="${user.studentPK}"/>
	</c:url>
	<a href="${changePasswordLink}"> Change Password </a> |
</c:if>


<c:if test="${!singleCourse}">
 <a href="<c:url value='/view/index.jsp'/>" title="All courses for which you are registered">
					All Courses
			</a> 
			<c:if test="${course != null}">
			|
			</c:if>
</c:if>
<c:if test="${course != null}">
	<c:url var="courseLink" value="/view/course.jsp">
		<c:param name="coursePK" value="${course.coursePK}"/>
	</c:url>
	<a href="${courseLink}"  title="overview of ${course.courseName}">
			${course.courseName}
	</a> 
<c:if test="${instructorCapability}">
	<c:url var="instructorLink" value="/view/instructor/course.jsp">
		<c:param name="coursePK" value="${course.coursePK}"/>
	</c:url>
	| <a href="${instructorLink}" title="Instructor overview of the course">Instructor view</a>
</c:if>
		
<c:if test="${project != null}">
	<c:url var="projectLink" value="/view/project.jsp">
		<c:param name="projectPK" value="${project.projectPK}"/>
	</c:url>
	| <a href="${projectLink}" title="Overview of project ${project.projectNumber}">
	${project.projectNumber}
	</a> 
		
	<c:if test="${submission != null}">
		<c:url var="submissionLink" value="/view/submission.jsp">
			<c:param name="submissionPK" value="${submission.submissionPK}"/>
		</c:url>
			| <a href="${submissionLink}" title="details of this submission">
					<fmt:formatDate value="${submission.submissionTimestamp}" pattern="dd MMM, hh:mm a"/>
			</a> 
	</c:if>

</c:if>

</c:if>

</div>

<%--
<c:choose>
	<c:when test="${takenSurvey == 'true'}">
 Thanks for taking the end of semester submit server survey! 
	</c:when>
	<c:otherwise>
<a href="/view/studentSurvey.jsp">
<h1> Please give us comments/complaints/other feedback about the Submit Server! </h1>
</a>
	</c:otherwise>
</c:choose>
--%>

<%--
<c:set var="givenConsent" value="${sessionScope['userSession'].givenConsent}"/>

<c:if test="${givenConsent == 'pending'}">
	<jsp:include page="/consentForm.html" flush="true"/>
</c:if>

<c:if test="${givenConsent != 'no' and givenConset != 'under18' and !sessionScope['userSession'].backgroundDataComplete}">
	<h2>
	<a href="<c:url value="/view/background.jsp"/>"> Please click here to fill out (or complete) a background survey</a>
	</h2>
</c:if>
--%>



