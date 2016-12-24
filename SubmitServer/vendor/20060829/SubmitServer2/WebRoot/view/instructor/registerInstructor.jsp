<%@ page language="java" %>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<ss:head title="Register Instructor for ${course.courseName}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

Registering a new instructor will create two (2) additional accounts:
<ul>
	<li> A canonical account that is used to submit reference implementations of a project.
		The canonical account will be named &lt;<ss:campusUID/>&gt;-canonical
	<li> A student test account with only student privileges that the instructor 
		can use to view <i>exactly</i> what a student will see for any part of the system.  Most
		of the pages in the system can be viewed from the perspective of a student; the student
		account is more of a fail-safe mechanism.  The student test account will be named
		&lt;<ss:campusUID/>&gt;-student.
</ul>


<form name="RegisterInstructorForm"
	action="<c:url value="/action/instructor/RegisterInstructor"/>"
	method="post">
	<input type="hidden" name="coursePK" value="${course.coursePK}"/>
<table>
	<tr>
		<th> firstname </th>
		<th> lastname </th>
		<th> <ss:campusUID /> </th>
		<th> <ss:employeeNum /></th>
		<th> class account (use <ss:campusUID/> for courses without class accounts)</th>
		<c:if test="${initParam['authentication.service']=='edu.umd.cs.submitServer.GenericStudentPasswordAuthenticationService'}">
		<%--
			We only want a field for the password if we're using the generic password
			authentication service.
		--%>
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
			<td> <input name="password" type="text" value=""/><input type="hidden" name="authenticateType" value="generic" /> </td>
		</c:if>
	</tr>

	<c:if test="${initParam['authentication.service']!='edu.umd.cs.submitServer.GenericStudentPasswordAuthenticationService'}">		
	<tr>
		<td colspan="6">
		<center>
		<table>
			<tr>
				<td>Use default student authentication</td>
				<td><input selected="true" type="radio" name="authenticateType" value="default">
			</tr>
			<tr>
				<td>Use generic marmoset authentication</td>
				<td><input type="radio" name="authenticateType" value="generic">
			</tr>
		</table>		
		</center>
		</td>
	</tr>
	</c:if>
	
	<tr>
		<td colspan="6"> <input type="submit" value="Register Instructor!"/> </td>
	</tr>
</table>
</form>
<h3> ${param.registerInstructorMessage} </h3>

</body>
</html>
