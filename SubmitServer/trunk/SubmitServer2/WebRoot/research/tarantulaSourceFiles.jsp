<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head title="Tarantula for submission ${submission.submissionPK}" />
	<body>
	<ss:header />
	<ss:instructorBreadCrumb />
  
	<body>
	
	<h1>Source files in submission ${submission.submissionPK}</h1>
	
	<c:if test="${testType != null and testNumber != null}">
		<h2>Coverage information for ${testType} test #${testNumber}: ${testName}</h2>
	</c:if>
	<p>

	<table>
	<tr class="r${counter.index % 2}">
		<th> Filename </th>
		<c:if test="${userSession.capabilitiesActivated and testProperties.language == 'java'}">
		<th>coverage<br>statements</th>
		<th>coverage<br>conditionals</th>
		<th>coverage<br>methods</th>
    	<th>coverage<br>total</th>
		</c:if>
	</tr>
	<c:forEach var="sourceFileName" items="${sourceFileList}" varStatus="counter">
		<tr class="r${counter.index % 2}">
			<td>
				<%--
					Weirdness Alert:  the testType and testNumber can't go after sourceFileName;
					does this have anything to do with the slash being mis-encoded in the path
					to the source file name?
				--%>				
				<c:url var="sourceFileURL" value="/research/TarantulaSourceDisplay">
					<c:param name="submissionPK" value="${submission.submissionPK}" />
					<c:if test="${param.testType != null and param.testNumber != null}">
						<c:param name="testType" value="${param.testType}" />
						<c:param name="testNumber" value="${param.testNumber}" />
					</c:if>
					<c:param name="sourceFileName" value="${sourceFileName}" />
				</c:url>
				<a href="${sourceFileURL}">${sourceFileName}</a>
			</td>
			<c:if test="${userSession.capabilitiesActivated and 
				filenameToCoverageStatsMap[sourceFileName] != null and
				testProperties.language == 'java'}">
				${filenameToCoverageStatsMap[sourceFileName].HTMLTableRow}
			</c:if>
		</tr>
	</c:forEach>
	<tr class="r2">
	<td> <b> All files </b> <br>excluding JUnit tests</td>
	${coverageResults.overallCoverageStats.HTMLTableRow}
	</tr>
	
	</table>

	</body>
</html>
