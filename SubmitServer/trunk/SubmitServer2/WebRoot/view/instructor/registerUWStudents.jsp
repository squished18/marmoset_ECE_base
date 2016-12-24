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

<form action="<c:url value="/action/instructor/RegisterUWStudents"/>"
	method="POST" enctype="multipart/form-data">
  <input type="hidden" name="coursePK" value="${course.coursePK}" />

<table class="form">
  <tr>
    <th colspan="2">Register students for ${course.courseName} by
		uploading a file in a colon separated file following the format
    described below: 
    </tr>
  </tr>
  <tr>
		<td class="label">File containing student registrations:</td>
		<td class="input"><input type="file" size="40" name="file" /></td>
		
    <%--
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
  --%>
	
	<tr>
   <td colspan="2">
   <input checked="true" type="radio" name="isLimitUserIDLength" value="yes" />
   Limit user ID to first 
   <input type="text" name="limitUserIDLength" value="8" size="10"/>
   characters.<br />
   <input type="radio" name="isLimitUserIDLength" value="no"/> Do not
   limit user ID length.<br />
   </td>
 </tr>
   <tr>
		<td colspan=2 class="submit">
      <input type="submit" value="Register Students" />
    </td>
  </tr>
	<tr>
		<td colspan=2 class="description">
<p>
The file is located on the course account.  For example, if my course is
CS 241, the classlist file would be located at /u/cs241/.classlist.
</p>

<p>
The classlist file format for this file is:
<pre>#Id:Userid:Name:Lecture:Study:Plan:Group:Year:Degree:Initials:Family:Status:Time:Session:Sections
id:userid:name:lec:study:plan:group:year:degree:initials:family:status:time:session:sections
</pre>
</p>

<p>
You can directly upload the cliasslist file available on the course account; 
This means that you do <b>not</b> need to remove comments in the classlist 
file.
</p>

<p>
This import tool will properly handle the students already register in
the course.  Also, any student that was register in the course but dropped
the course in the new classlist will <b>not</b> be dropped in the database
as the student can reregister into the course.
</p>

<p>
If the file format does not match, please contact the marmoset adminstrator 
know about the change in file format.
</p>
    </td>
  </tr>
</table>
</form>

<ss:footer />
</body>
</html>
