<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<h1>Results for each test case for each students' most recent submission</h1>

		<c:url var="sortByTime" value="/view/instructor/projectTestResults.jsp">
				<c:param name="projectPK" value="${project.projectPK}" />
				<c:param name="sortKey" value="time" />
			</c:url>
<c:url var="sortByName" value="/view/instructor/projectTestResults.jsp">
				<c:param name="projectPK" value="${project.projectPK}" />
			</c:url>
<c:url var="sortByAcct" value="/view/instructor/projectTestResults.jsp">
				<c:param name="projectPK" value="${project.projectPK}" />
				<c:param name="sortKey" value="account" />
			</c:url>
			<c:url var="sortByScore" value="/view/instructor/projectTestResults.jsp">
				<c:param name="projectPK" value="${project.projectPK}" />
				<c:param name="sortKey" value="score" />
			</c:url>
		<p>
		<table title="projectTestResults">
		
		<%--
		Jumping through hoops here to support FindBugs...
		Using a c:choose to see if this is a Java or C/non-Java project.
		FindBugs results only apply to Java projects, and therefore Java projects
		need another column.
		--%>
		<c:choose>
		<c:when test="${testProperties.language=='java'}">
			${ss:formattedColumnHeaders(9, canonicalTestOutcomeCollection)}
		</c:when>
		<c:otherwise>
			${ss:formattedColumnHeaders(8, canonicalTestOutcomeCollection)}
		</c:otherwise>
		</c:choose>

		<tr>
		<th class="number" rowspan="2"><a title="position in list">#</a></th>
		<th rowspan="2"><a href="${sortByName}" title="sort by name">Name</a></th>
		<th rowspan="2"><a href="${sortByAcct}" title="sort by account">Acct</a></th>
		<th rowspan="2" class="number"><a title="# of submissions for this project">#<br>subs</a></th>
		<th rowspan="2"><a href="${sortByTime}" title="sort by time of last submission">submitted at</a></th>
		<th class="number" rowspan="2"><a href="${sortByScore}" title="sort by score">Score</a>
		<th rowspan="2"># inconsistent<br>background<br>retests</th>
		<c:if test="${testProperties.language=='java'}">
		<th rowspan="2"># FindBugs<br>warnings</th>
		<th rowspan="2"># student<br>written tests</th>
		</c:if>
			${ss:formattedTestHeaderTop(canonicalTestOutcomeCollection)}
				
			</tr>
			<tr>
				${ss:formattedTestHeader(canonicalTestOutcomeCollection)}
			</tr>
			<c:forEach var="studentRegistration"
				items="${studentRegistrationSet}" varStatus="counter">
				<tr class="r${counter.index % 2}">
				<td class="number">${1+counter.index}
					<td class="description"><c:if
						test="${studentRegistration.instructorLevel > 0}">* </c:if>${studentRegistration.lastname},
					${studentRegistration.firstname}</td>
					<td class="description"><c:url var="studentProjectLink"
						value="/view/instructor/studentProject.jsp">
						<c:param name="projectPK" value="${project.projectPK}" />
						<c:param name="studentPK" value="${studentRegistration.studentPK}" />
					</c:url> <a href="${studentProjectLink}" title="view all submissions by ${studentRegistration.cvsAccount}">
					${studentRegistration.cvsAccount} </a></td>
					<td class="number">${studentSubmitStatusMap[studentRegistration.studentRegistrationPK].numberSubmissions}
			
					<td><c:url var="submissionLink"
						value="/view/instructor/submission.jsp">
						<c:param name="submissionPK"
							value="${lastSubmission[studentRegistration.studentRegistrationPK].submissionPK}" />
					</c:url> <a href="${submissionLink}" title="view this submission"> <fmt:formatDate
						value="${lastSubmission[studentRegistration.studentRegistrationPK].submissionTimestamp}"
						pattern="E',' dd MMM 'at' hh:mm a" /></a></td>
						
						<td class="number">${lastSubmission[studentRegistration.studentRegistrationPK].valuePassedOverall}</td>
						
						<td>
						<c:url var="submissionAllTestsLink" value="/view/instructor/submissionAllTests.jsp">
							<c:param name="submissionPK" value="${lastSubmission[studentRegistration.studentRegistrationPK].submissionPK}"/>
						</c:url>
						<a href="${submissionAllTestsLink}">
						${backgroundRetestMap[lastSubmission[studentRegistration.studentRegistrationPK].submissionPK].numFailedBackgroundRetests}
						</a>
						</td>
						<c:if test="${testProperties.language=='java'}">
							<td>${lastSubmission[studentRegistration.studentRegistrationPK].numFindBugsWarnings}</td>
							<td>${lastOutcomeCollection[studentRegistration.studentRegistrationPK].numStudentWrittenTests}</td>
						</c:if>
						
				${ss:formattedTestResults(canonicalTestOutcomeCollection,lastOutcomeCollection[studentRegistration.studentRegistrationPK])}
						
					
				</tr>
			</c:forEach>

		</table>