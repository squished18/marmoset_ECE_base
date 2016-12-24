<%@ page language="java" %>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<ss:head title="Edit registration for ${student.firstname} ${student.lastname} in ${course.courseName}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<form name="editStudentRegistrationForm"
	action="<c:url value="/action/instructor/EditStudentRegistration"/>"
	method="post">
<input type="hidden" name="studentRegistrationPK" value="${studentRegistration.studentRegistrationPK}"/>
<table>
	<tr>
		<th> First Name </th>
		<th> Last Name </th>
		<th> DirectoryID </th>
		<th> Employee Num</th>
		<th> Class Account <br>(use directoryID for courses <br>without class accounts)</th>
		<c:if test="${initParam['authentication.service']=='edu.umd.cs.submitServer.GenericStudentPasswordAuthenticationService' || student.password != NULL}">
		<th> Password <br>(keep this blank to leave password unchanged)</th>
		</c:if>
	</tr>
	<tr>
		<td> <input name="firstname" type="text" value="${student.firstname}"/> </td>
		<td> <input name="lastname" type="text" value="${student.lastname}"/> </td>
		<td> <input name="campusUID" type="text" value="${student.campusUID}"/> </td>
		<td> <input name="employeeNum" type="text" value="${student.employeeNum}"/> </td>
		<td> <input name="cvsAccount" type="text" value="${studentRegistration.cvsAccount}"/> </td>
		<c:if test="${initParam['authentication.service']=='edu.umd.cs.submitServer.GenericStudentPasswordAuthenticationService' || student.password != NULL}">
		<td> <input name="password" type="text" value=""/> </td>
		</c:if>
	</tr>
	<tr>
		<td colspan="6"> <input type="submit" value="Update record!"/> </td>
	</tr>
</table>
</form>
<h3> ${param.editStudentRegistrationMessage} </h3>

</body>
</html>
