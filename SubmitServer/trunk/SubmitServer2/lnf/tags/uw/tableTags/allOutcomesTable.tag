<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>

<h2>Test Results</h2>
<h3>Note: For test outcomes, <i>failed</i> means <i>wrong</i> and <i>error</i> means <i>crashed</i>.</h3>

<table class="testResults">
	<c:set var="numDisplayed" value="${numDisplayed + 1}" />

	<tr>
		<th>type</th>
		<th>test #</th>
		<th>outcome</th>
		<th>source<br>coverage</th>
		<th>points</th>
		<th>name</th>
		<th>short result</th>
		<th>long result</th>
		<c:if test="${userSession.superUser}">
			<th>raw coverage xml</th>
		</c:if>
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
			<td> <a href="${coverageLink}"> source </a></td>
			
			<td>${test.pointValue}</td>
			<td>${test.shortTestName}</td>
			<td class="description">
			
			<c:out value="${test.shortTestResult}" /></td>
			<td class="description">
	
			<c:if test="${test.outcome!='passed'}">
<span style="background: #FFE799; font-family: monospace; white-space: pre; border: 1px solid #FFDB66; letter-spacing: 1px">
<c:out value="${test.hotlink}" escapeXml="false"/>
</span>
			</c:if>
			<c:if test="${test.outcome=='passed'}">
			<c:out value="${test.hotlink}" escapeXml="false"/>
			</c:if>
<!-- 
			<c:choose>
			<c:when test="${testProperties.language=='java' or testProperties.language=='Java'}">

			<c:if test="${test.outcome!='passed'}">
<span style="background: #FFE799; font-family: monospace; white-space: pre; border: 1px solid #FFDB66; letter-spacing: 1px">
<c:out value="${test.hotlink}" />
</span>
			</c:if>
			<c:if test="${test.outcome=='passed'}">
			<c:out value="${test.hotlink}" />
			</c:if>

			</c:when>
			<c:otherwise>
			<c:if test="${test.outcome!='passed'}">
<span style="background: #FFE799; font-family: monospace; white-space: pre; border: 1px solid #FFDB66; letter-spacing: 1px">
<c:out	value="${test.longTestResult}" />
</span>
			</c:if>

			<c:if test="${test.outcome=='passed'}">
			<pre><c:out value="${test.longTestResult}" /></pre>
			</c:if>

			</c:otherwise>
			</c:choose>
-->
			</td>
			<c:if test="${userSession.superUser}">
			<td>
				<c:url var="rawCoverageLink" value="/research/PrintRawCoverageXmlResults">
					<c:param name="testRunPK" value="${test.testRunPK}"/>
					<c:param name="testName" value="${test.testName}"/>
					<c:param name="testNumber" value="${test.testNumber}"/>
				</c:url>
				<a href="${rawCoverageLink}"> raw coverage </a>
			</td>
			</c:if>
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
			<c:url var="releaseUniqueCoverageLink" value="/view/sourceFiles.jsp">
				<c:if test="${test.testNumber != null and test.testType != null}">
					<c:param name="testType" value="${test.testType}"/>
					<c:param name="testNumber" value="${test.testNumber}"/>
					<c:param name="testRunPK" value="${test.testRunPK}"/>
					<c:param name="hybridTestType" value="release-unique"/>
				</c:if>
			</c:url>
			<c:url var="failingOnlyLink" value="/view/sourceFiles.jsp">
				<c:if test="${test.testNumber != null and test.testType != null}">
					<c:param name="testType" value="${test.testType}"/>
					<c:param name="testNumber" value="${test.testNumber}"/>
					<c:param name="testRunPK" value="${test.testRunPK}"/>
					<c:param name="hybridTestType" value="failing-only"/>
				</c:if>
			</c:url>
			<td>
			<a href="${coverageLink}"> source </a><br>
			<a href="${releaseUniqueCoverageLink}"> (unique)</a>
			<br><a href="${failingOnlyLink}"> (failing-only) </a>
			</td>

			<td>${test.pointValue}</td>
			<td>${test.shortTestName}</td>
			<td class="description">
				<%-- ${test.outcome}, ${test.exceptionSourceCoveredElsewhere}, ${test.coarsestCoverageLevel} <br> --%>
				<c:if test="${test.outcome=='error' and test.exceptionSourceCoveredElsewhere}">
					The source of this exception is covered by a student or public test.<br>
				</c:if>
				<c:if test="${test.coarsestCoverageLevel=='METHOD'}">
					This release test covers methods that are <b>not</b> covered by any
					public or student tests!<br>
				</c:if>
				<c:out value="${test.shortTestResult}" />
			</td>
			<td class="description">
				<c:if test="${test.outcome!='passed'}">
<span style="background: #FFE799; font-family: monospace; white-space: pre; border: 1px solid #FFDB66; letter-spacing: 1px">
<c:out value="${test.hotlink}" escapeXml="false"/>
</span>
				</c:if>
				<c:if test="${test.outcome=='passed'}">
					<c:out value="${test.hotlink}" escapeXml="false"/>
				</c:if>
			</td>
			<c:if test="${userSession.superUser}">
			<td>
				<c:url var="rawCoverageLink" value="/research/PrintRawCoverageXmlResults">
					<c:param name="testRunPK" value="${test.testRunPK}"/>
					<c:param name="testName" value="${test.testName}"/>
					<c:param name="testNumber" value="${test.testNumber}"/>
				</c:url>
				<a href="${rawCoverageLink}"> raw coverage </a>
			</td>
			</c:if>
		</tr>
	</c:forEach>

	<c:forEach var="test" items="${testOutcomeCollection.secretOutcomes}">

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
			
			<td>${test.pointValue}</td>
			<td>${test.shortTestName}</td>
			<td class="description">
				<c:if test="${test.outcome=='error' and test.exceptionSourceCoveredElsewhere}">
					The source of this exception is covered by a student or public test.<br>
				</c:if>
				<c:if test="${test.coarsestCoverageLevel=='METHOD'}">
					This secret test covers methods that are <b>not</b> covered by any
					public or student tests!<br>
				</c:if>
				<c:out value="${test.shortTestResult}" />
			</td>
			<td class="description">
				<c:if test="${test.outcome!='passed'}">
<span style="background: #FFE799; font-family: monospace; white-space: pre; border: 1px solid #FFDB66; letter-spacing: 1px">
<c:out value="${test.hotlink}" escapeXml="false"/>
</span>
				</c:if>
				<c:if test="${test.outcome=='passed'}">
					<c:out value="${test.hotlink}" escapeXml="false"/>
				</c:if>
			</td>
			<c:if test="${userSession.superUser}">
			<td>
				<c:url var="rawCoverageLink" value="/research/PrintRawCoverageXmlResults">
					<c:param name="testRunPK" value="${test.testRunPK}"/>
					<c:param name="testName" value="${test.testName}"/>
					<c:param name="testNumber" value="${test.testNumber}"/>
				</c:url>
				<a href="${rawCoverageLink}"> raw coverage </a>
			</td>
			</c:if>
		</tr>
	</c:forEach>

</table>
