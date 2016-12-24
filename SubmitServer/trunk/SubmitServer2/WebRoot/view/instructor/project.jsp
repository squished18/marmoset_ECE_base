<%@ page language="java" %>
<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head
	title="Overview of ontime/late/very late results for all student submissions for project ${project.projectNumber}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<ss:projectTitle />
<ss:projectMenu />

<%--
	<li>
	<c:url var="reComputePointValuesLink" value="/action/instructor/ReComputePointValues">
		<c:param name="projectPK" value="${project.projectPK}"/>
	</c:url>
	<a href="${reComputePointValuesLink}"> 
	Re-compute point values
	</a>
--%>

<ss:projectOverviewTable />
<ss:studentsWithoutSubmissionsTable />

<ss:footer />
</body>
</html>
