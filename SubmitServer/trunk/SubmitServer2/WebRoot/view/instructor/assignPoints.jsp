<%@ page language="java" %><%@ page language="java" %>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<ss:head title="Assign points for project ${project.title} in ${course.courseName}"/>

<SCRIPT LANGUAGE="JavaScript">
function getValue(num)
{
	num = parseInt(num);
    if(isNaN(num))
		num = 0;
	return num;
}

function change()
{
	num = 0;
	for (var ii = 0; ii <  document.inputForm.elements.length; ii++) {
		if (document.inputForm.elements[ii].name != "testRunPK" &&
			document.inputForm.elements[ii].name != "comment" &&
			document.inputForm.elements[ii].name != "total")
		{
			num = num + getValue(document.inputForm.elements[ii].value);
		}
	}
	document.inputForm.total.value = num;
}

/*
// In theory it would be nice to compute the totals after the page loads
// But this requires more complicated javascript than I have time or patience for right now...
function addLoadEvent(func) {
  var oldonload = window.onload;
  if (typeof window.onload != 'function') {
    window.onload = func;
  } else {
    window.onload = function() {
      oldonload();
      func();
    }
  }
}
addLoadEvent(change);

addLoadEvent(function() {
});
*/
</SCRIPT>

	
  <body>
  	<ss:header/>
  		<ss:instructorBreadCrumb/>
  	  <h1>Project ${project.projectNumber}: ${project.title}</h1>
    <p> ${project.description}

    <form class="form" name="inputForm" action="<c:url value="/action/instructor/AssignPoints"/>" method="POST">
    <h2>Testing setup</h2>
    <ul>
    <li> Testing setup 
     tested  <fmt:formatDate value="${projectJarfile.datePosted}" pattern="E',' dd MMM 'at' hh:mm a"/>
    <li> Test-setup comment: <input type="text" name="comment" value="${projectJarfile.comment}" size="30"/>
    <li> Solution submitted by ${studentRegistration.cvsAccount} 
    at <fmt:formatDate value="${submission.submissionTimestamp}" pattern="E',' dd MMM 'at' hh:mm a"/>
  </ul>
    


    <p>
  	
	<input type="hidden" name="testRunPK" value="${testRun.testRunPK}">
  	<table>
  		<tr>
  			<th> test # </th>
  			<th> type </th>
  			<th> name </th>
  			<th> point value </th>
  		</tr>
		<c:forEach var="outcome" items="${testOutcomeCollection.allTestOutcomes}" varStatus="counter">
			<c:if test="${outcome.testType != 'build'}">
				<tr class="$r{counter.index % 2 == 1 ? 'odd' : 'even'}">
				<td> ${outcome.testNumber} </td>
				<td> ${outcome.testType} </td>
				<td class="label"> ${outcome.testName} </td>
				<td> <input type="text" name="${outcome.testName}" class="pointValue" size="3" value="${canonicalTestOutcomeMap[outcome.testName].pointValue}" onchange="change()"/></td>
				</tr>
			</c:if>
		</c:forEach>
		<tr>
			<td colspan=3>total</td>
			<td><input type="text" name="total" size="3" readonly="true"></td>
		</tr>
		<tr class="submit">
		<td class="label" colspan=4> 
		<input type=submit value="Assign points, edit test-setup comment and activate test setup"></td>
		</tr>
		
  	</table>
  	</form>

<ss:footer/>
  </body>
</html>
