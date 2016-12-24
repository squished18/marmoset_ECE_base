<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
  
    <c:url var="loginLink" value="/authenticate/PerformLogin"/>
	<form name="PerformLogin" method="post" action="${loginLink}" >
		<c:if test="${not empty param.target}">
	    <input type="hidden" name="target" value="${param.target}">
	    </c:if>
	    <table class="form">
	    <tr><th colspan=2>Login with your User ID and password
	    <tr><td class="label">User ID:<td class="input"> <input type="text" name="campusUID"/>
		<tr><td class="label">Password: <td class="input"> <input type="password" name="uidPassword"/>
		<tr><td colspan=2 class="submit"><input type="submit" value="Login" name="Login"/>
		<tr><td colspan=2 class="description">	
<ul><li>Your User ID is your UWDIR user ID. If your UWDIR user ID is longer than eight characters, use the long form (e.g. gvcormack rather than gvcormac).
<li>Your password is initially your student ID number. Change it after your first login.
</ul>
		
		</table>
	</form>
	



