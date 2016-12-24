<%@ page language="java" %>
<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>

<ss:head
	title="${param.title}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<h1>${param.title}</h1>
Click OK to accept or the back button on your
browser to cancel.
<c:url var="changeSubmissionBuildStatusLink" value="/action/instructor/ChangeSubmissionBuildStatus"/>

<form method="POST" action="${changeSubmissionBuildStatusLink}">
	<input type="hidden" name="submissionPK" value="${param.submissionPK}">
	<input type="hidden" name="buildStatus" value="${param.buildStatus}">
	<input type="submit" value="OK">
</form>

<ss:footer />
</body>
</html>