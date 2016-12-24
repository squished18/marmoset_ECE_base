<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>

<h1>Students without submissions</h1>
<p>
<table>
	<tr>
	<th><a href="${sortByName}">Name</a></th>
		<th><a href="${sortByAcct}">Acct</a></th>

		<th>extension</th>
	</tr>

	<c:forEach var="studentRegistration" items="${studentsWithoutSubmissions}"
		varStatus="counter">
		<tr class="r${counter.index % 2}">


		<td class="description"><c:if test="${studentRegistration.instructorLevel > 0}">* </c:if>${studentRegistration.lastname}, ${studentRegistration.firstname}
	        </td>
		<td class="description">${studentRegistration.cvsAccount} </td>

		<td>
			<c:url var="grantExtensionLink" value="/view/instructor/grantExtension.jsp">
				<c:param name="studentRegistrationPK" value="${studentRegistration.studentRegistrationPK}"/>
				<c:param name="projectPK" value="${project.projectPK}"/>
			</c:url>
			<a href="${grantExtensionLink}">
			0
			</a>
		</td>
		</tr>
	</c:forEach>
</table>
