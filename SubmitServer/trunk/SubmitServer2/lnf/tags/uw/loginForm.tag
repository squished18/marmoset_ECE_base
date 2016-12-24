<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>

	<c:url var="loginLink" value="/authenticate/PerformLogin"/>
	<form name="PerformLogin" method="post" action="${loginLink}" >

		<c:if test="${not empty param.target}">
	    	<input type="hidden" name="target" value="${param.target}">
    		</c:if>

		<p>Hi <%= request.getRemoteUser() %>, please click below to
		continue to marmoset. If you get an error message and can't access the main
		screen, please contact the course staff to get the required authorization to
		access marmoset.

		<input type="hidden" name="campusUID" value="<%= request.getRemoteUser() %>">
		<input type="hidden" name="uidPassword" value="<%= request.getRemoteUser() %>">
		<p><input type="submit" value="Continue" name="Continue"/>

	</form>

	


