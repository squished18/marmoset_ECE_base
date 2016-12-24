<%@ page language="java" %><%@ page language="java" %>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<ss:head title="Grant an extension to ${student.firstname} ${student.lastname}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />
<body>
Grant an extension to ${student.firstname} ${student.lastname} for project ${project.projectNumber}
<p>
<b><font color=red>NOTE:</font></b> Extension are given in hours
<form class="form" name="form" action="<c:url value="/action/instructor/GrantExtension"/>" method="POST">
<!--
	<select>
	<option> 6</option>
	<option> 12</option>
	<option> 18</option>
	<option> 24</option>
	<option> 36</option>
	</select>
-->
	<input type="text" name="extension">
	<input type="hidden" name="studentRegistrationPK" value="${studentRegistration.studentRegistrationPK}">
	<input type="hidden" name="projectPK" value="${project.projectPK}">
	<br>
	<input type="submit" value="Grant extension">
</form>
</body>
</html>
