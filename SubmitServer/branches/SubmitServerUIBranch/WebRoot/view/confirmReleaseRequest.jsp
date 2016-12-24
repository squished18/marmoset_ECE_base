<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head
	title="Confirm release request for submission ${param.submissionPK}" />

<body>
<ss:header />
<ss:breadCrumb />


<h1>Are you sure you want to release test submission
${param.submissionPK}?</h1>
Click OK to release test this submission or the back button on your
browser to cancel.
<c:url var="releaseRequestLink" value="/action/RequestReleaseTest" />

<form method="POST" action="${releaseRequestLink}"><input type="hidden"
	name="submissionPK" value="${param.submissionPK}"> <input type="submit"
	value="OK"></form>

<ss:footer />
</body>
</html>
