
<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head title="${course.courseName} Submit Server" />

<body>
<ss:header />

<ss:breadCrumb />

<h1><a href="${course.url}">${course.courseName}</a>,
${course.semester}: ${course.description}</h1>
<p>Welcome ${user.firstname}
<h2>Projects</h2>
<p>
<table>
	<tr>
		<th>project</th>
		<th>submissions</th>
		<th>web<br>
			submission</th>
		<th>download<br>
			starter<br>
			files
		</th>
		<th>Due</th>
		<th class="description">Title</th>
	</tr>

	<c:set var="numDisplayed" value="0" />
	<c:forEach var="project" items="${projectList}" varStatus="counter">
		<c:if test="${project.visibleToStudents || instructorActionCapability || instructorCapability}">
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

				<td><c:url var="projectLink" value="/view/project.jsp">
					<c:param name="projectPK" value="${project.projectPK}" />
				</c:url> <a href="${projectLink}"> view </a></td>

				<td><c:url var="submitProjectLink" value="/view/submitProject.jsp">
					<c:param name="projectPK" value="${project.projectPK}" />
					<c:param name="projectJarfilePK" value="${project.projectJarfilePK}" />
				</c:url> <a href="${submitProjectLink}"> submit </a></td>
				
				<td>
					<c:if test="${project.archivePK != null}">
					<c:url var="downloadStarterFilesLink" value="/data/DownloadProjectStarterFiles">
						<c:param name="projectPK" value="${project.projectPK}"/>
					</c:url>
					<a href="${downloadStarterFilesLink}"> download </a></td>
					</c:if>
				</td>
				
				<td><fmt:formatDate value="${project.ontime}"
					pattern="dd MMM, hh:mm a" /></td>
				<td class="description">${project.title}</td>

			</tr>
			<c:set var="numDisplayed" value="${numDisplayed + 1}" />
		</c:if>
	</c:forEach>
</table>

<ss:footer/>
</body>
</html>
