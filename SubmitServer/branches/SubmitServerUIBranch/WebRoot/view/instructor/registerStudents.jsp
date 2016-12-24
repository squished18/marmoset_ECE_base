<%@ page language="java"%>
<%@ page language="java"%>
<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head
	title="Register students for ${course.courseName} in semester ${course.semester}" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<form action="<c:url value="/action/instructor/RegisterStudents"/>"
	method="POST" enctype="multipart/form-data">

<table class="form">
	<tr>
		<th colspan="2">Register students for ${course.courseName} by
		uploading a file containing comma-separated fields. <input type="hidden"
			name="coursePK" value="${course.coursePK}" />
	<tr>
		<td class="label">File containing student registrations:
		<td class="input"><input type="file" size="40" name="file" />
		
		<c:if test="${initParam['authentication.service']=='edu.umd.cs.submitServer.GenericStudentPasswordAuthenticationService'}">
		<input type="hidden" name="authenticateType" value="generic" />
		</c:if>

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
		<td colspan=2 class="submit"><input type="submit"
			value="Register students" />
	<tr>
		<td colspan=2 class="description">
		<ss:registerStudentsFileFormat />
</table>
</form>

<ss:footer />
</body>
</html>
