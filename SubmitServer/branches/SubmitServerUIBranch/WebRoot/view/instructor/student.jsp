
<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head title="${student.firstname} ${student.lastname} -- ${studentRegistration.cvsAccount}/${course.courseName} " />

<body>
<ss:header />

<ss:instructorBreadCrumb />


<h1>${student.firstname} ${student.lastname} -- ${studentRegistration.cvsAccount}</h1>
<p>
<table>
	<tr>
		<th>project</th>
		<th>submissions</th>
		<th>extension</th>
		<th class="description">Title</th>
	</tr>

	<c:set var="numDisplayed" value="0" />
	<c:forEach var="project" items="${projectList}" varStatus="counter">
		<c:if test="${project.visibleToStudents}">
			<tr class="r${numDisplayed % 2}">

				<td><c:choose>

					<c:when test="${project.url != null}">
						<a href="<c:url value="${project.url}"/>">
						${project.projectNumber} </a>
					</c:when>

					<c:otherwise>
					${project.projectNumber}
					</c:otherwise>

				</c:choose></td>

				<td><c:url var="projectLink" value="/view/instructor/studentProject.jsp">
					<c:param name="studentPK" value="${studentRegistration.studentPK}" />
					<c:param name="projectPK" value="${project.projectPK}" />
				</c:url> <a href="${projectLink}"> view </a></td>

				<td> 
				<c:url var="grantExtensionLink" value="/view/instructor/grantExtension.jsp">
					<c:param name="studentRegistrationPK" value="${studentRegistration.studentRegistrationPK}"/>
					<c:param name="projectPK" value="${project.projectPK}"/>
				</c:url>
					<a href="${grantExtensionLink}">
					${projectToStudentSubmitStatusMap[project.projectPK].extension} 
					</a>
				</td>

				<td class="description">${project.title}</td>

			</tr>
			<c:set var="numDisplayed" value="${numDisplayed + 1}" />
		</c:if>
	</c:forEach>
</table>

<ss:footer/>
</body>
</html>
