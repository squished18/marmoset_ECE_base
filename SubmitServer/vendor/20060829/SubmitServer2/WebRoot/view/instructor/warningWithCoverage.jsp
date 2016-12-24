<%@ page language="java" %>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<ss:head title="Findbugs warning with coverage for ${submission.submissionPK} ${testOutcome.testType} ${testOutcome.testNumber}"/>  
  <body>
  	<ss:header/>
  		<ss:instructorBreadCrumb/>

	<h2>FindBugs warning</h2>
	<p>
	<table>
		<tr>
			<th>Location</th>
			<th>Warning</th>
			<th>Priority</th>
			<th>Link to longer description</th>

			<tr class="r${numDisplayed % 2}">
				<c:set var="numDisplayed" value="${numDisplayed + 1}" />
				<td class="description">
					<c:out value="${warning.hotlink}" escapeXml="false"/>
				</td>

				<td class="description">
				<%--<pre>--%>
					<c:out
					value="${warning.longTestResult}" />
				<%--</pre>--%>
					</td>
				<td class="description">${warning.exceptionClassName}</td>
				<td><a href="${initParam.findbugsDescriptionsURL}#${warning.testName}">${warning.testName}</a></td>
			</tr>
	</table>

	<h2>Test results that cover this warning</h2>
<p>
<table class="testResults">
	<c:set var="numDisplayed" value="${numDisplayed + 1}" />

	<tr>
		<th>type</th>
		<th>test #</th>
		<th>outcome</th>
		<th>all source</th>
		<th>covered file</th>
		<th>points</th>
		<th>name</th>
		<th>short result</th>
		<th>long result</th>
	</tr>
	<c:forEach var="test" items="${testOutcomeCollection.publicOutcomes}">

	<tr class="r${numDisplayed % 2}">
		<c:set var="numDisplayed" value="${numDisplayed + 1}" />
		<td>${test.testType}</td>
		<td>${test.testNumber}</td>
		<td>${test.outcome}</td>
			
		<c:url var="coverageLink" value="/view/sourceFiles.jsp">
			<c:if test="${test.testNumber != null and test.testType != null}">
				<c:param name="testType" value="${test.testType}"/>
				<c:param name="testNumber" value="${test.testNumber}"/>
			</c:if>
			<c:param name="submissionPK" value="${submission.submissionPK}"/>
		</c:url>
		<td> <a href="${coverageLink}"> all source </a></td>
		
		<c:url var="coverageOnlyLink" value="/view/sourceCode.jsp">
			<c:param name="testType" value="${test.testType}" />
			<c:param name="testNumber" value="${test.testNumber}" />
			<c:param name="sourceFileName" value="${warning.fileName}" />
			<c:param name="testRunPK" value="${param.testRunPK}"/>
		</c:url>
		<td> <a href="${coverageOnlyLink}"> covered file</a></td>
		
		<td>${test.pointValue}</td>
		<td>${test.shortTestName}</td>
		<td class="description">
		
		<c:out value="${test.shortTestResult}" /></td>
		<td class="description">
		<%--<pre>--%>
			<c:out value="${test.hotlink}" escapeXml="false"/>
		<%--</pre>--%>
		</td>
	</tr>
	</c:forEach>


	<c:forEach var="test" items="${testOutcomeCollection.releaseOutcomes}">

		<tr class="r${numDisplayed % 2}">
			<c:set var="numDisplayed" value="${numDisplayed + 1}" />
			<td>${test.testType}</td>
			<td>${test.testNumber}</td>
			<td>${test.outcome}</td>
			
			<c:url var="coverageLink" value="/view/sourceFiles.jsp">
				<c:if test="${test.testNumber != null and test.testType != null}">
					<c:param name="testType" value="${test.testType}"/>
					<c:param name="testNumber" value="${test.testNumber}"/>
				</c:if>
				<c:param name="submissionPK" value="${submission.submissionPK}"/>
			</c:url>
			<td> <a href="${coverageLink}"> source </a></td>
			
			<c:url var="coverageOnlyLink" value="/view/sourceCode.jsp">
				<c:param name="testType" value="${test.testType}" />
				<c:param name="testNumber" value="${test.testNumber}" />
				<c:param name="sourceFileName" value="${warning.fileName}" />
				<c:param name="testRunPK" value="${param.testRunPK}"/>
			</c:url>
			<td> <a href="${coverageOnlyLink}"> covered file</a></td>

			<td>${test.pointValue}</td>
			<td>${test.shortTestName}</td>
			<td class="description"><c:out value="${test.shortTestResult}" /></td>
			<td class="description">
			<%--<pre>--%>
				<c:out value="${test.hotlink}" escapeXml="false"/>
			<%--</pre>--%>
			</td>
		</tr>
	</c:forEach>

	<c:forEach var="test" items="${testOutcomeCollection.secretOutcomes}">

		<tr class="r${numDisplayed % 2}">
			<c:set var="numDisplayed" value="${numDisplayed + 1}" />
			<td>${test.testType}</td>
			<td>${test.testNumber}</td>
			<td>${test.outcome}</td>
					<td>${test.pointValue}</td>
			<td>${test.shortTestName}</td>
			<td class="description"><c:out value="${test.shortTestResult}" /></td>
			<td class="description">
			<pre>
				<c:out value="${test.hotlink}" escapeXml="false"/>
			</pre>
			</td>
		</tr>
	</c:forEach>
	
	</body>
</html>
