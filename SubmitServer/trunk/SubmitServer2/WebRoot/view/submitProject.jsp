<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>

<ss:head
	title="Submit project ${project.projectNumber} for ${course.courseName} in ${course.semester}" />

<body>
<ss:header />
<ss:breadCrumb />

<c:if test="${project.projectJarfilePK == 0 or projectJarfile.jarfileStatus != 'active'}">
<h2>
<font color=red><b>NOTE:</b></font>
This project is not yet active for automated testing of submissions.  This probably means
that the instructor has not yet uploaded a working reference implementation and assigned 
point values to each test case.
<p>
You can still submit your implementation, but the server will not test your 
submission until the project has been activated by the instructor.
</h2>
</c:if>

<h1>File upload project submission</h1>
<p>Submit project ${project.projectNumber} for ${course.courseName} in
${course.semester}
<p>
<p>
<form name="submitform" enctype="multipart/form-data"
	action="<c:url value="/action/SubmitProjectViaWeb"/>" method="POST"><input type="hidden"
	name="projectPK" value="${project.projectPK}" /> <input type="hidden"
	name="submitClientTool" value="web" />
<table class="form">
<tr><td>File to Submit: <td class="input"><input type="file" name="file" size=40 />
<tr><td class="submit" colspan=2"><input type="submit" value="Submit project!">
</table>
</form>

<ss:footer />
</body>
</html>
