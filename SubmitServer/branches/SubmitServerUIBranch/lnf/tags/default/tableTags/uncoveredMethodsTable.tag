<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>

<c:if test="${not empty testOutcomeCollection.uncoveredMethods}">
	<h2> These methods are not covered by a Public or student-written test </h2>
	<table class="testResults">
		<tr>
			<th>#</th>
			<th>class</th>
			<th>method</th>
			<th>line number</th>
			<th>link</th>
		</tr>
		<c:forEach var="outcome" items="${testOutcomeCollection.uncoveredMethods}">
		<tr>
			<td>${outcome.testNumber}</td>
			<td class="left">${outcome.exceptionClassName}</td>
			<td class="left">${outcome.htmlTestName}</td>
			<td class="right">${outcome.pointValue}</td>
			<td class="left">${outcome.hotlink}</td>
		</tr>
		</c:forEach>
	</table>
</c:if>