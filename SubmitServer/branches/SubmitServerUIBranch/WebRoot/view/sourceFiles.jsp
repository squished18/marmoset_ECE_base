<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head title="View source files for submission ${submission.submissionPK}" />
	<body>
	<ss:header />
	<c:choose>
	<c:when test="${instructorCapability == 'true'}">
		<ss:instructorBreadCrumb />
	</c:when>
	<c:otherwise>
		<ss:breadCrumb />
	</c:otherwise>
  	</c:choose>
  	
	<body>
	<%--
	The sourceFilesTable is a separate tag so that someday we could have /view/sourceFiles.jsp
	and also /view/instructor/sourceFiles.jsp although currenlty it's easier to use a
	<c:choose> tag to pick the correct breadcrumb.
	--%>
	<ss:sourceFilesTable />

	</body>
</html>
