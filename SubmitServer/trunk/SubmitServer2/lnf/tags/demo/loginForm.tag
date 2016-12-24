<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
  
<c:url var="loginLink" value="/authenticate/PerformLogin"/>
	<form name="PerformLogin" method="post" action="${loginLink}" >
		<c:if test="${not empty param.target}">
	    <input type="hidden" name="target" value="${param.target}">
	    </c:if>
	    <table class="form">
	    <tr><th colspan=2>Login with your Email Address and Password
	    <tr><td class="label">Email Address:<td class="input"> <input type="text" name="campusUID"/>
		<tr><td class="label">Password: <td class="input"> <input type="password" name="uidPassword"/>
		<tr><td colspan=2 class="submit"><input type="submit" value="Login" name="Login"/>
		<tr><td colspan=2 class="description">	
		</table>
	</form>
	
	<c:if test="${passwordSent == true}">
	<p>
	<h1>Your password has been emailed to ${emailAddress}</h1>
	<p> Please log in when you get your password!
	<p>
	</c:if>
	<p>
	Or...
	<p>
	
	<form name="RegisterDemoAccount" method="post" action="/authenticate/RegisterDemoAccount">
		<table>
		<tr><th colspan=2>Register a new Demo account!</th></tr>
		<tr><td class="label">Email Address:</td><td class="input"> <input type="text" name="emailAddress"/></td></tr>
		<tr><td class="label">First name:</td><td class="input"> <input type="text" name="firstname"/></td></tr>
		<tr><td class="label">Last name:</td><td class="input"> <input type="text" name="lastname"/></td></tr>
		<tr><td class="label">Course name:</td>
		<td class="input"> 
		<select name="coursePK">
			<c:forEach var="course" items="${courseList}">
			<option value="${course.coursePK}"> ${course.courseName} </option>
			</c:forEach>
		</select>
		</td>
		<tr><td colspan=2 class="submit"><input type="submit" value="Register" name="Register"/>
		<tr><td colspan=2 class="description">
		</table>
	</form>
	Your username will be your email address.<br>
	We will email a password to the email address you provide.<br>
	<b> DO NOT USE</b> a secure password because we're sending the passwords in the clear.<br>
	You can use the password we send you to log in.<br>
	TODO: Add a link for people who forget their passwords.

	<p>	
	<c:url var="marmosetProjectLink" value="http://www.cs.umd.edu/~jspacco/marmoset/index.html"/>
	<h2><a href="${marmosetProjectLink}"> More information about the Marmoset Project </a></h2>
	