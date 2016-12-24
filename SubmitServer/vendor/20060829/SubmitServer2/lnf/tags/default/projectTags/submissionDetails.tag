<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>

<p> submissionPk = ${submission.submissionPK}, client was ${submission.submitClient},
<c:if test="${testRun != null}">
Tested against test-setup #${testRun.projectJarfilePK},  
on ${testRun.testTimestamp} by ${testRun.testMachine}
 testRun&nbsp;#${testRun.testRunPK}
</c:if>