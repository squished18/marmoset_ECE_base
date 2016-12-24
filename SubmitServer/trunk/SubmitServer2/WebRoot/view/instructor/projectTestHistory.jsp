<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>


<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head title="Test results history project ${project.projectNumber}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<ss:projectTitle />
<ss:projectMenu />

<c:choose>
	<c:when test="${project.initialBuildStatus == 'accepted'}">
		<h1>Upload only project</h1>
		<p>This project is set up to accept uploads only, and not perform any
		server based compilation and testing</p>
	</c:when>
	<c:otherwise>		
		<p>Note: Table excludes all submissions from accounts with instructor privileges
		<p><jsp:include page="/data/instructor/TestResultHistory" flush="" />
	</c:otherwise>

</c:choose>
<p>
<ss:footer />
</body>
</html>
