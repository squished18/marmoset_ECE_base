<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>

<c:if test="${not empty testOutcomeCollection.findBugsOutcomes}">
	<h2>FindBugs warnings</h2>
	<p>
	<table>
			<tr>
				<th>Location</th>
				<th>Unit tests covered</th>
				<th>Warning</th>
				<th>Priority</th>
				<th>Link to longer description</th>
			</tr>
			<c:forEach var="test" items="${testOutcomeCollection.findBugsOutcomes}">
			<tr class="r${numDisplayed % 2}">
				<c:set var="numDisplayed" value="${numDisplayed + 1}" />

				<td class="description">
					<c:out value="${test.hotlink}" escapeXml="false"/>
				</td>
							
				<c:url var="warningWithCoverageLink" value="/view/instructor/warningWithCoverage.jsp">
					<c:param name="longTestResult" value="${test.longTestResult}"/>
					<c:param name="warningName" value="${test.testName}"/>
					<c:param name="priority" value="${test.exceptionClassName}"/>
					<c:param name="shortTestResult" value="${test.shortTestResult}"/>
					<c:param name="testRunPK" value="${submission.currentTestRunPK}"/>
				</c:url>
				<td>
					<a href="${warningWithCoverageLink}">
					unit tests<br>covered
					</a>
				</td>

				<td class="description">
				<%--<pre>--%>
					<c:out value="${test.longTestResult}" />
				<%--</pre>--%>
				</td>

				<td class="description">${test.exceptionClassName}</td>
				<td><a href="${initParam.findbugsDescriptionsURL}#${test.testName}">${test.testName}</a></td>
			</tr>
			</c:forEach>
	</table>
</c:if>
