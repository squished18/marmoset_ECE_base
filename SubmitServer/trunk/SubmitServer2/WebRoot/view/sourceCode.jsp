
<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head title="View source code for submission ${submission.submissionPK}" />
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
<%--
	sourceFileName: ${sourceFileName}<br>
	starthightlight: ${startHighlight}<br>
	numToHighlight: ${numToHighlight}<br>
	numContext: ${numContext}<br>

	out.println("numToHightlight in script: " + request.getAttribute("numToHighlight"));

--%>
	<c:if test="${testType != null and testNumber != null}">
		<h2>Coverage information for ${hybridTestType} ${testType} test #${testNumber}: ${testName}</h2>
	</c:if>
	<p>
	${ss:displaySourceCode(connection, submission, sourceFileName, 
		startHighlight, numToHighlight, numContext, codeCoverageResults)}
    
	</body>
</html>
