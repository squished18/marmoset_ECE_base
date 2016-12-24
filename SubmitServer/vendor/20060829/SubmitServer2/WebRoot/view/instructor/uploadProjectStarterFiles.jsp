<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>

<ss:head
	title="Upload a new Zip or Jar archive of starter files for project ${project.projectNumber} for ${course.courseName} in ${course.semester}" />

<body>
<ss:header />
<ss:instructorBreadCrumb />

Upload a new Zip or Jar archive of starter files for project ${project.projectNumber} 
for ${course.courseName} in ${course.semester}
<p>
A "starter file archive" is a collection of resources students will use to start
their project.  For makefile-based projects (in C, Ruby, OCaml, etc), starter 
files usually include a Makefile, public test cases and possibly some partially-written
source files that need to be completed.
<p>

<form name="submitform" enctype="multipart/form-data"
	action="<c:url value="/action/instructor/UploadProjectStarterFiles"/>" method="POST"><input type="hidden"
	name="projectPK" value="${project.projectPK}" /> <input type="hidden"
	name="submitClientTool" value="web" />
<table class="form">
<tr><th colspan=2>Jar or Zip file of Starter Files</th>
<tr><td>Jar or Zip file to Upload: <td class="input"><input type="file" name="file" size=40 />
<tr><td class="submit" colspan=2"><input type="submit" value="Upload starter files!">
</table>
</form>

<ss:footer />

</body>
</html>