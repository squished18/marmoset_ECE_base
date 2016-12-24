<%@ page language="java"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head
	title="Submission ${submission.submissionNumber} for project ${project.projectNumber}" />
<body>
<ss:header />
<ss:breadCrumb />

<ss:projectTitle />

<h2>${student.firstname} ${student.lastname} : ${studentRegistration.cvsAccount}</h2>
<h2>Submission #${submission.submissionNumber}, submitted at <fmt:formatDate
	value="${submission.submissionTimestamp}"
	pattern="E',' dd MMM 'at' hh:mm a" /></h2>
<c:if
	test="${submission.currentTestRunPK != testRun.testRunPK}">
	<p>Results from previous testing against version
	${projectJarfile.version} of the test setup
</c:if> 

<c:choose>

	<c:when test="${testRun == null}">
		<p>No test results available
	</c:when> 

	<c:when test="${!testOutcomeCollection.compileSuccessful}">
		<h2>Compile/Build unsuccessful</h2>
		<p>
<%--
	TODO: Capture and display any output from compilation.
	Make compilation a scorable test type.
--%>
		<pre><c:out
			value="${testOutcomeCollection.buildOutcome.longTestResult}" /></pre>
	</c:when>
	<c:otherwise>
<!-- 		<ss:findBugsTable /> -->
		<c:if test="${not empty testOutcomeCollection.findBugsOutcomes}">
			<h2>FindBugs warnings</h2>
			<p>
			<table>
				<tr>
					<th>Location
					<th>Warning</th>
					<th>Link to longer description</th>
				</tr>
				<c:forEach var="test"
					items="${testOutcomeCollection.findBugsOutcomes}">

					<tr class="r${numDisplayed % 2}">
						<c:set var="numDisplayed" value="${numDisplayed + 1}" />

						<td class="description">
							<c:out value="${test.hotlink}" escapeXml="false"/>
						</td>
						<td class="description"><pre><c:out
							value="${test.longTestResult}" /></pre></td>
						<td><a href="${initParam.findbugsDescriptionsURL}#${test.testName}">${test.testName}</a></td>
					</tr>
				</c:forEach>
			</table>
		</c:if>

<c:set var="now" value="<%=new Long(System.currentTimeMillis())%>"/>

<c:if test="${testProperties.language == 'java' and testProperties.performCodeCoverage}">
	<h2>
		Code Coverage Results
	</h2>
	<table>
		<tr>
		<th>Test type</th>
		<th>statements</th>
		<th>conditionals</th>
		<th>methods</th>
	    </tr>
    	<c:if test="${studentCoverageStats != null}">
	    	<tr>
<c:url var="studentLink" value="/view/sourceFiles.jsp">
	<c:param name="testType" value="student"/>
	<c:param name="testNumber" value="all"/>
	<c:param name="testRunPK" value="${testRun.testRunPK}"/>
</c:url>
			<td> <a href="${studentLink}">  Student tests </a></td>
			${studentCoverageStats.HTMLTableRow}
	    	</tr>
	    </c:if>
		<c:if test="${publicCoverageStats != null}">
	    	<tr>
<c:url var="publicLink" value="/view/sourceFiles.jsp">
	<c:param name="testType" value="public"/>
	<c:param name="testNumber" value="all"/>
	<c:param name="testRunPK" value="${testRun.testRunPK}"/>
</c:url>
			<td> <a href="${publicLink}"> Public tests </a> </td>
			${publicCoverageStats.HTMLTableRow}
    		</tr>
    	</c:if>
		<c:if test="${publicAndStudentCoverageStats != null}">
	    	<tr>
<c:url var="publicAndStudentLink" value="/view/sourceFiles.jsp">
	<c:param name="testType" value="public-student"/>
	<c:param name="testNumber" value="all"/>
	<c:param name="testRunPK" value="${testRun.testRunPK}"/>
</c:url>
    		<td> <a href="${publicAndStudentLink}"> Public/Student tests </a> </td>
			${publicAndStudentCoverageStats.HTMLTableRow}
	    	</tr>
	    </c:if>
	    <%--
	    TODO: Code covered only by release/secret tests!
	    TODO: Methods covered/uncovered
	    --%>
	</table>
</c:if>


<ss:studentWrittenTestOutcomesTable />

<ss:uncoveredMethodsTable />

<c:choose>
<%-- 
	XXX If the Instructor has set the postDeadlineVisibility to 'everything'
	then we want to show all test results for everything.

	TODO Factor into a separate table; this is the *exact* table available to instructor/TAs
--%>
<c:when test="${project.postDeadlineOutcomeVisibility == 'everything' and project.lateMillis + (studentSubmitStatus.extension*60*60*1000) < now}">
	<ss:allOutcomesTable />
</c:when>

<%--
	
--%>
<c:when test="${not empty testOutcomeCollection.publicOutcomes || 
not empty testOutcomeCollection.releaseOutcomes}">
		<h2>Test Results</h2>
		<p>
		<table class="testResults">

			<c:set var="numDisplayed" value="0" />
			<tr>
				<th>type</th>
				<th>test #</th>
				<th>outcome</th>
				<c:if test="${testProperties.language=='java' and testProperties.performCodeCoverage}">
				
				</c:if>
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
					<td>${test.studentOutcome}</td>
					<td>${test.pointValue}</td>

					<td>${test.shortTestName}</td>
					<td class="description">
						<c:out value="${test.shortTestResult}" />
					</td>
					<td class="description">
					<c:choose>
					<c:when test="${testProperties.performCodeCoverage and testProperties.language=='java'}">
						<c:out value="${test.hotlink}" />
					</c:when>
					<c:otherwise>
						<pre><c:out	value="${test.longTestResult}" /></pre>
					</c:otherwise>
					</c:choose>
					</td>
				</tr>
			</c:forEach>

<%--
 or project.releasePolicy == 'anytime'
--%>
			<c:if
				test="${submission.releaseTestingRequested && submission.releaseEligible}">

				<c:set var="failedTests" value="0" />

				<c:forEach var="release"
					items="${testOutcomeCollection.releaseOutcomes}">

					<tr class="r${numDisplayed % 2}">
	
					<c:choose>
					<%--
					<c:when test="${release.passed}">
						<td>${release.testType}</td>
						<td>${release.testNumber}</td>
						<td>${release.studentOutcome}</td>
						<td>${release.pointValue}</td>
						<td></td>
						<td></td>
						<td></td>
					</c:when>
					--%>
					<c:when test="${release.failed}">
						<c:set var="numDisplayed" value="${numDisplayed + 1}" />
						<c:set var="failedTests" value="${failedTests + 1}" />
						<c:choose>
							<c:when test="${failedTests <= project.numReleaseTestsRevealed}">
								<td>${release.testType}</td>
								<td>${release.testNumber}</td>
								<td>${release.studentOutcome}</td>
								<td>${release.pointValue}</td>
								<td>${release.shortTestName}</td>
								<td></td>
								<c:choose>
                                                                                    <c:when test="${project.stackTracePolicy == 'exception_location'}">
                                                                                            <td>${release.exceptionLocation}</td>
                                                                                    </c:when>
                                                                                    <c:when test="${project.stackTracePolicy == 'restricted_exception_location'}">
                                                                                            <c:choose>
                                                                                                    <%--
                                                                                                    <c:when test="${release.exceptionSourceCoveredElsewhere}">
                                                                                                    --%>
                                                                                                    <c:when test="${ss:isApproximatelyCovered(testOutcomeCollection,release)}">
                                                                                                    <td>${release.exceptionLocation}</td>
                                                                                                    </c:when>
                                                                                                    <c:otherwise>
                                                                                                    <td>This test generates an exception in your code, 
                                                                                                            but no public or student-written tests cover the line of
                                                                                                            code that throws the exception.  Write more/better test cases and 
                                                                                                            we will reveal more information about why your code fails this test case.
                                                                                                    </td>
                                                                                                    </c:otherwise>
                                                                                            </c:choose>
                                                                                    </c:when>
                                                                                    
                                                                                    <c:when test="${project.stackTracePolicy == 'full_stack_trace'}">
                                                    <td class="description"><pre><c:out	value="${release.longTestResult}" /></pre></td>
                                                                                            <%-- <td>${release.hotlink}</td> --%>
                                                                                    </c:when>
                                                                                    <c:otherwise>
                                                                                    <td></td>
                                                                                    </c:otherwise>
								</c:choose>
							</c:when>
							<c:otherwise>
								<td>${release.testType}</td>
								<td>?</td>
								<td>${release.studentOutcome}</td>
								<td>?</td>
								<td>?</td>
								<td></td>
								<td></td>
							</c:otherwise>
						</c:choose>
					</c:when>
					</c:choose>
					</tr>
				</c:forEach>
			</c:if>
			<%-- end if release test requested --%>
		</table>

		<p>You received ${testOutcomeCollection.valuePublicTestsPassed}/${
		testOutcomeCollection.valuePublicTests} points for public test cases.


		<c:choose>
			<c:when
				test="${submission.releaseTestingRequested && submission.releaseEligible}">
				<p>
				<p>You received
				${testOutcomeCollection.valueReleaseTestsPassed}/${testOutcomeCollection.valueReleaseTests}
				points for release tests.
			</c:when>
			<%-- end if !release test requested --%>
			<c:otherwise>
				<c:if
					test="${submission.releaseEligible && submission.currentTestRunPK == testRun.testRunPK}">
					<p>You passed all the public tests, so this submission is eligible for release testing. <c:if
						test="${releaseInformation.releaseRequestOK}">
						<c:url var="releaseRequestLink"
							value="/view/confirmReleaseRequest.jsp">
							<c:param name="submissionPK" value="${submission.submissionPK}" />
						</c:url>
						<p>
						<h3>
						<a href="${releaseRequestLink}"> Click here to release test this
						submission </a>
						</h3>
					</c:if>
				</c:if>
			</c:otherwise>
		</c:choose>
		<p>You currently have ${releaseInformation.tokensRemaining} release
		tokens available. <c:if
			test="${not empty releaseInformation.regenerationSchedule}">
			<p>Release token(s) will regenerate at:
			<ul>
				<c:forEach var="timestamp"
					items="${releaseInformation.regenerationSchedule}">
					<li><fmt:formatDate value="${timestamp}"
						pattern="E',' dd MMM 'at' hh:mm a" /><br>
				</c:forEach>
			</ul>
		</c:if>
		</c:when> 
		<c:otherwise><p>Submission compiled</c:otherwise>
	</c:choose> 
	</c:otherwise>

</c:choose> <c:if test="${testRunList != null}">
	<c:url value="/view/submissionAllTests.jsp"
		var="submissionAllTestsLink">
		<c:param name="submissionPK" value="${submission.submissionPK}" />
	</c:url>

<ss:submissionDetails />

</c:if>
<ss:footer />
</body>
</html>
