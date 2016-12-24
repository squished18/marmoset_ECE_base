<%@ page isErrorPage="true" language="java"%>
<%@ page import="edu.umd.cs.submitServer.UserSession"%>
<%@ page import="edu.umd.cs.submitServer.servlets.SubmitServerServlet"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.StringWriter"%>
<%@ page import="org.apache.log4j.Logger"%>

<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<%@ taglib prefix="log" uri="http://jakarta.apache.org/taglibs/log-1.0" %>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <ss:head title="Error Page"/>
  <body>
  <ss:header/>
  <ss:loginBreadCrumb/>

<c:url var="bugImage" value="/images/Bug.gif"/>
<p><img src="${bugImage}">

<%
// TODO There must be an easier way to log this information!

// URL of request
String url = request.getRequestURI();
if (request.getQueryString() != null) 
	url += "?" + request.getQueryString();
	
// studentPK of the student who initiated the request
UserSession userSession = session == null ? null : (UserSession) session.getAttribute("userSession");
if (userSession != null)
	url += " for studentPK " + userSession.getStudentPK();

// Get the servletException logger
Logger logger = Logger.getLogger("edu.umd.cs.submitServer.logging.servletExceptionLog");
//System.out.println("error.jsp found logger named " +logger.getName());

// Build appropriate message
String msg = 
"URL: " +url +"\n"+
"Referer: " + request.getHeader("referer") +"\n"+
"Status code: " +pageContext.getErrorData().getStatusCode();

// Log the message to the logger I have under my control.
// So far it looks like setting 'swallowOutput="true"' in the
// <Context> part of server.xml keeps this info from being
// replicated into catalina.out.  I don't yet know how to get a handle
// on the Logger that used to be appending this information to
// catalina.out, and I don't much care right now because I'm able to
// log everything that I want.
logger.fatal(msg, pageContext.getErrorData().getThrowable());

%>

<h1>Oops!</h1>

<table class="stacktrace">
<tr><th class="description">${pageContext.errorData.throwable}</th>
<c:forEach  var="trace" items="${pageContext.errorData.throwable.stackTrace}" varStatus="counter">
<tr class="r${counter.index % 2}">
<td class="description">${trace}
</c:forEach>
</table>


     <ss:footer/>
</html>
