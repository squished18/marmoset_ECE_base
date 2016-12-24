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

<h2> Change password for ${student.firstname} ${student.lastname} </h2>
<c:url var="changePasswordLink" value="/action/ChangePassword"/>
<form method="POST" name="changePassword" action="${changePasswordLink}">
<input type="hidden" name="studentPK" value="${student.studentPK}"/>
<table>
<tr>
	<td> Current Password: </td>
	<td> <input type="password" name="currentPassword"/> </td>
</tr>
<tr>
	<td> New Password: </td>
	<td> <input type="password" name="newPassword"/> </td>
</tr>
<tr>
	<td> Confirm New Password: </td>
	<td> <input type="password" name="confirmNewPassword"/> </td>
</tr>
<tr>
	<td colspan="2"> <input type="submit" value="Change Password!"/>
</tr>
</table>

</form>
<ss:footer/>
</body>
</html>
