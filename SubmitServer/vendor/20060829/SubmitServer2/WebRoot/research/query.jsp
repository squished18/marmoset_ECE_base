
<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head title="${course.courseName} Submit Server" />

<body>
<ss:header />

<ss:instructorBreadCrumb />

<h2>
Search for a particular class of warnings
</h2>

<form action="<c:url value="/research/query.jsp"/>" method="GET">

<table>

<tr>
	<td>test type</td>
	<td> <select name=testType>
		<option value=findbugs>findbugs</option>
		<option value=pmd>pmd</option>
		<option value=tests>public/release tests</option>
		</select>
	</td>

<tr>
	<td> name of FB detector/PMD style pattern:</td>
	<td> <input type=text name=testName> </td>

<tr>
	<td rowspan=3>priority for FB/PMD, <br>
		exception class name for <br>
		public/release tests:</td>
	<td> <input type=text name=optionalQuality> </td>

<tr>
		<td colspan=2 class="submit"><input type="submit"
			value="Execute query" />
		</td>

</table>

</form>

<c:if test="${testType != null}">
Type: ${testType} <br>
Name: ${testName} <br>
${optionalQuality} <br>
<p>
<table class="testResults">
<tr>
		<th> type </th>
		<th> outcome </th>
		<th> name </th>
		<th> short result </th>
		<th> other </th>
		<th> long result </th>
	</tr>
<c:forEach var="outcome" items="${outcomeList}">
	<tr>
		<td> ${outcome.testType} </td>
		<td> ${outcome.outcome} </td>
		<td> ${outcome.testName} </td>
		<td> ${outcome.shortTestResult} </td>
		<td> ${outcome.exceptionClassName} </td>
		<td> ${outcome.hotlink} </td>
	</tr>
</c:forEach>
</table>
<c:url var="nextPageLink" value="/research/query.jsp">
	<c:param name="testName" value="${testName}"/>
	<c:param name="testType" value="${testType}"/>
	<c:param name="optionalQuality" value="${optionalQuality}"/>
	<c:param name="offset" value="${offset + numRecords}"/>
	<c:param name="numRecords" value="${numRecords}"/>
</c:url>

<a href="${nextPageLink}"> Next Page</a>

</c:if>


</body>
</html>
