<%@ page language="java" %>
<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>

<ss:head
	title="Administrative actions" />
<body>
<ss:header />
<ss:instructorBreadCrumb />

<h1>Administrative actions</h1>
<h2>Create course</h2>
<p>
<c:url var="createCourseLink" value="/action/admin/CreateCourse"/>
	<form action="${createCourseLink}" method="post" name="createCourseForm">
	<table class="form">
		<tr><th colspan=2>Create new course</th></tr>
		<tr>
			<td class="label">Course Name:</td>
			<td class="input"><input type="text" name="courseName"></td>
		</tr>
		<tr>
			<td class="label">Semester:</td>
			<td class="input">
				<input type="text" name="semester" value="${initParam['semester']}"/>
			<%--
				<select name="semester">
				<option value="Spring2005"> Spring2005 </option>
				</select>
			--%>
			</td>
		</tr>
		<tr>
			<td class="label">Description <br>(can be empty): </td>
			<td class="input">
				<textarea cols="40" rows="6" name="description"></textarea>	
			</td>
		</tr>
		<tr>
			<td class="label">URL:</td>
			<td class="input"><input type="text" name="url" size="60"></td>
		</tr>
		<tr  class="submit"><td colspan=2>
			<input type="submit" value="Create course">
	</table>

	
	</form>
	
	<h2>Authenticate as...</h2>
<p>This allows you to log in as any other user, and allow you to view the submit server
as that user would. 
Once you have authenticated as another user, you will have to log out and log in as yourself in order
to perform actions as yourself.


<p>
<table>
<tr><th>Name</th><th>directory name</th><th>Authenticate</th>

	<c:forEach var="student" items="${allStudents}"
		varStatus="counter">
		<c:url var="loginLink" value="/authenticate/PerformLogin"/>

		<tr class="r${counter.index % 2}">
		<td class="description">${student.lastname}, ${student.firstname} 
		<td class="description">${student.campusUID} 

		<td>		<form name="PerformLogin" method="post" action="${loginLink}" >
				<input type="hidden" name="campusUID" value="${student.campusUID}"/>
				<input type="submit" value="as"/>
				</form>
				</td>

				</tr>

				</c:forEach>
</table>


	<ss:footer/>
  </body>
</html>
