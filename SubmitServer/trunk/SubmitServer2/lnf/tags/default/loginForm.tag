<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
  
    <c:url var="loginLink" value="/authenticate/PerformLogin"/>
	<form name="PerformLogin" method="post" action="${loginLink}" >
		<c:if test="${not empty param.target}">
	    <input type="hidden" name="target" value="${param.target}">
	    </c:if>
	    <table class="form">
	    <tr><th colspan=2>Login with your Directory ID and password
	    <tr><td class="label">Directory ID:<td class="input"> <input type="text" name="campusUID"/>
		<tr><td class="label">Directory Password: <td class="input"> <input type="password" name="uidPassword"/>
		<tr><td colspan=2 class="submit"><input type="submit" value="Login" name="Login"/>
		<tr><td colspan=2 class="description">	
<ul><li>Your Directory ID is <b>not</b> your UMID, SID or SSN.
<li>Your Directory password is <b>not</b> your ARES/Testudo PIN.
<li>You may discover your Directory ID by visiting:<br>
<a href="https://www.ldap.umd.edu/cgi-bin/chpwd?searchbyumid">https://www.ldap.umd.edu/cgi-bin/chpwd?searchbyumid</a>
<li>You may set your Directory password by visiting:<br>
<a href="https://www.ldap.umd.edu/cgi-bin/chpwd">https://www.ldap.umd.edu/cgi-bin/chpwd</a>
</ul>
		
		</table>
	</form>
	


<p>NOTICE: Unauthorized access to this computer is in violation of Article 27.
Sections 45A and 146 of the Annotated Code of MD. The university may monitor
use of this system as permitted by state and federal law, including the
Electronic Communications Privacy Act, 18 U.S.C. sections 2510 et seq. Anyone
using this system acknowledges that all use is subject to University of
Maryland Acceptable Use Guidelines available at
<a href="http://www.inform.umd.edu/aug">http://www.inform.umd.edu/aug</a>.

