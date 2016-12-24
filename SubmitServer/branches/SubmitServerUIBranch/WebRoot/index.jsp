
<%@ page language="java" %>
<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <ss:head title="Submit Server Login page"/>
  <body>
  <ss:header/>
  <ss:loginBreadCrumb/>
  
  <c:choose>
     
    <c:when test="${missingIDOrPasswordException == true}">
      <ss:missingIdOrPasswordMessage/>
    </c:when>
    
    <c:when test="${canNotFindDirectoryID == true}">
      <ss:noSuchIdMessage/>
    </c:when>
  
    <c:when test="${badPassword == true}">
      <ss:authenticationFailedMessage/>
    </c:when>
  
    <c:when test="${otherError == true}">
      <ss:authenticationFailedMessageGeneric/>
    </c:when>
  
  </c:choose>

  <ss:loginForm/>

  <ss:footer/>
  </body>
</html>
