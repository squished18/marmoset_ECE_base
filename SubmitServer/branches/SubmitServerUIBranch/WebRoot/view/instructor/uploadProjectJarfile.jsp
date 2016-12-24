<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head
	title="Upload a new testing setup for projectPK ${project.projectPK} " />
<body>
<ss:header />
<ss:instructorBreadCrumb />


<c:url var="uploadProjectJarFileLink"
	value="/action/instructor/UploadProjectJarfile" />

<form name="submitform" action="${uploadProjectJarFileLink}"
	enctype="multipart/form-data" method="POST"><input type="hidden"
	name="projectPK" value="${project.projectPK}">
<p>Comment:<br>
<textarea cols="40" rows="8" name="comment"></textarea>
<p>Jarfile: <input type="file" name="file" size=40>
<p><input type="submit" value="Upload Project Jarfile">
</form>

<ss:footer />
</body>
</html>
