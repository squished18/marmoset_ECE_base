<%@ page language="java" %>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<ss:head title="Register student for ${course.courseName}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<h2> Registering a new student for ${course.courseName}
</h2>

<form name="RegisterOneStudentForm"
	action="<c:url value="/action/instructor/RegisterOneStudent"/>"
	method="post">
	<input type="hidden" name="coursePK" value="${course.coursePK}"/>
<table>
	<tr>
		<th> firstname </th>
		<th> lastname </th>
		<th> <ss:campusUID /> </th>
		<th> <ss:employeeNum /></th>
		<th> class account <br>(use <ss:campusUID /> for courses <br>without class accounts)</th>
		<%--
			We only want a field for the password if we're using the generic password
			authentication service.
		--%>
		<c:if test="${initParam['authentication.service']=='edu.umd.cs.submitServer.GenericStudentPasswordAuthenticationService'}">
		<th> password </th>
		</c:if>
	</tr>
	<tr>
		<td> <input name="firstname" type="text" value=""/> </td>
		<td> <input name="lastname" type="text" value=""/> </td>
		<td> <input name="campusUID" type="text" value=""/> </td>
		<td> <input name="employeeNum" type="text" value=""/> </td>
		<td> <input name="cvsAccount" type="text" value=""/> </td>
		<c:if test="${initParam['authentication.service']=='edu.umd.cs.submitServer.GenericStudentPasswordAuthenticationService'}">
		<td> <input name="password" type="password" value=""/><input type="hidden" name="authenticateType" value="generic" /> </td>
		</c:if>
	</tr>
	<tr>
		<td colspan="6">
		<center>
		<table>
			<tr>
				<td>student account</td>
				<td><input checked="true" type="radio" name="accountType" value="student">
			</tr>
			<tr>
				<td>TA account</td>
				<td><input type="radio" name="accountType" value="TA">
			</tr>
		</table>

		<c:if test="${initParam['authentication.service']!='edu.umd.cs.submitServer.GenericStudentPasswordAuthenticationService'}">		
		<table>
			<tr>
				<td>Use default student authentication</td>
				<td><input selected="true" type="radio" name="authenticateType" value="default" />
			</tr>
			<tr>
				<td>Use generic marmoset authentication</td>
				<td><input type="radio" name="authenticateType" value="generic" />
			</tr>
		</table>
		</c:if>
		
		</center>
		</td>
	</tr>
	<tr>
		<td colspan="6"> <input type="submit" value="Register Student!"/> </td>
	</tr>
</table>
</form>
</body>
</html>
