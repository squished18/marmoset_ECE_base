<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
  
<c:url var="loginLink" value="/authenticate/PerformLogin"/>
	<form name="PerformLogin" method="post" action="${loginLink}" >
		<c:if test="${not empty param.target}">
	    <input type="hidden" name="target" value="${param.target}">
	    </c:if>
	    <table class="form">
	    <tr><th colspan=2>Login with your Marmoset ID and password
	    <tr><td class="label">Marmoset ID:<td class="input"> <input type="text" name="campusUID"/>
		<tr><td class="label">Marmoset Password: <td class="input"> <input type="password" name="uidPassword"/>
		<tr><td colspan=2 class="submit"><input type="submit" value="Login" name="Login"/>
		<tr><td colspan=2 class="description">	
<ul>
<li>Your Marmoset ID is <b>not</b> your UD ID, CIS ID or SSN.
</ul>
		
		</table>
	</form>
	
<P><a href="http://www.cs.umd.edu/users/jspacco/marmoset/">Marmoset</a> is an 
active research project by Jaime Spacco and 
Bill Pugh at the University of Maryland, and David Hovemeyer at Vassar College.