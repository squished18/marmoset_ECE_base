<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>

<h1>Student submissions</h1>

<c:url var="sortByTime" value="${pageContext.request.requestURI}">
				<c:param name="projectPK" value="${project.projectPK}" />
				<c:param name="sortKey" value="time" />
			</c:url>
<c:url var="sortByName" value="${pageContext.request.requestURI}">
				<c:param name="projectPK" value="${project.projectPK}" />
			</c:url>
<c:url var="sortByAcct" value="${pageContext.request.requestURI}">
				<c:param name="projectPK" value="${project.projectPK}" />
				<c:param name="sortKey" value="account" />
			</c:url>
<p>
<table>
	<tr>
		<th>#</th>
		<th><a href="${sortByName}">Name</a></th>
		<th><a href="${sortByAcct}">Acct</a></th>
		<th>#<br>subs</th>
		<th># inconsistent<br>background<br>retests</th>
		<th><a href="${sortByTime}">last submission</a></th>
		<th>on time</th>
		<th>late</th>
		<th>very late</th>
		<th>extension</th>
	</tr>

	<c:forEach var="studentRegistration" items="${studentRegistrationSet}"
		varStatus="counter">
		<tr class="r${counter.index % 2}">
		
		<td>${1+counter.index}</td>

		<td class="description"><c:if test="${studentRegistration.instructorLevel > 0}">* </c:if>${studentRegistration.lastname}, ${studentRegistration.firstname}
		
		</td>
			<td class="description"><c:url var="studentProjectLink"
				value="/view/instructor/studentProject.jsp">
				<c:param name="projectPK" value="${project.projectPK}" />
				<c:param name="studentPK" value="${studentRegistration.studentPK}" />
			</c:url> <a href="${studentProjectLink}">
			${studentRegistration.cvsAccount} </a></td>

			<td>
			${studentSubmitStatusMap[studentRegistration.studentRegistrationPK].numberSubmissions}
			</td>
			
			<td>
				<c:url var="submissionAllTestsLink" value="/view/instructor/submissionAllTests.jsp">
					<c:param name="submissionPK" value="${lastSubmission[studentRegistration.studentRegistrationPK].submissionPK}"/>
				</c:url>
				<a href="${submissionAllTestsLink}">
				${backgroundRetestMap[lastSubmission[studentRegistration.studentRegistrationPK].submissionPK].numFailedBackgroundRetests}
				</a>
			</td>
			
			<td><fmt:formatDate value="${lastSubmission[studentRegistration.studentRegistrationPK].submissionTimestamp}"
					pattern="E',' dd MMM yyyy 'at' hh:mm a" />
			</td>

			<td><c:if
				test="${not empty lastOnTime[studentRegistration.studentRegistrationPK]}">
		
		<c:choose>
					<c:when
						test="${lastOnTime[studentRegistration.studentRegistrationPK].buildStatus eq 'complete'}">

	       ${lastOnTime[studentRegistration.studentRegistrationPK].valuePublicTestsPassed}
    	  / ${lastOnTime[studentRegistration.studentRegistrationPK].valueReleaseTestsPassed}
	      / ${lastOnTime[studentRegistration.studentRegistrationPK].valueSecretTestsPassed}
	      <c:if test="${testProperties.language=='java'}">
    	  / ${lastOnTime[studentRegistration.studentRegistrationPK].numFindBugsWarnings}
    	  </c:if>
	      </c:when>
					<c:otherwise>
      				untested
      				</c:otherwise>
		</c:choose>
				
			</c:if>
			<td><c:if
				test="${not empty lastLate[studentRegistration.studentRegistrationPK]}">
				<c:choose>
					<c:when
						test="${lastLate[studentRegistration.studentRegistrationPK].buildStatus eq 'complete'}">


       ${lastLate[studentRegistration.studentRegistrationPK].valuePublicTestsPassed}
      / ${lastLate[studentRegistration.studentRegistrationPK].valueReleaseTestsPassed}
      / ${lastLate[studentRegistration.studentRegistrationPK].valueSecretTestsPassed}
      / ${lastLate[studentRegistration.studentRegistrationPK].numFindBugsWarnings}
        </c:when>
					<c:otherwise>
      untested
      </c:otherwise>
				</c:choose>
			</c:if>
			<td><c:if
				test="${not empty lastVeryLate[studentRegistration.studentRegistrationPK]}">
				<c:choose>
					<c:when
						test="${lastVeryLate[studentRegistration.studentRegistrationPK].buildStatus eq 'complete'}">

       ${lastVeryLate[studentRegistration.studentRegistrationPK].valuePublicTestsPassed}
      / ${lastVeryLate[studentRegistration.studentRegistrationPK].valueReleaseTestsPassed}
      / ${lastVeryLate[studentRegistration.studentRegistrationPK].valueSecretTestsPassed}
      / ${lastVeryLate[studentRegistration.studentRegistrationPK].numFindBugsWarnings}
        </c:when>
					<c:otherwise>
      untested
      </c:otherwise>
				</c:choose>
			</c:if>

		<td>
			<c:url var="grantExtensionLink" value="/view/instructor/grantExtension.jsp">
				<c:param name="studentRegistrationPK" value="${studentRegistration.studentRegistrationPK}"/>
				<c:param name="projectPK" value="${project.projectPK}"/>
			</c:url>
			<a href="${grantExtensionLink}">
			${studentSubmitStatusMap[studentRegistration.studentRegistrationPK].extension}
			</a>
		</td>
		</tr>
	</c:forEach>
</table>