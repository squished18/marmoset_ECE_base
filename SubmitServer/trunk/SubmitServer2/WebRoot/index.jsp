<%@ page language="java" %>
<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<!-- This file is a test version for index.jsp in the same directory -->

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

  <!-- Omar's additions -->


        <p>Hi <%= request.getRemoteUser() %>, please login as one of the users you are allowed to
	authenticate as using the table below. If you get an error message and can't access the main
        screen, please contact the course staff to get the required authorization to
        access marmoset.

	<p>                                                                                                
	<table>                                                                                            
	<tr><th>Name</th><th>directory name</th><th>Authenticate</th>                                      
                                                                                                   
	<c:url var="loginLink" value="/authenticate/PerformLogin"/>
        <c:forEach var="student" items="${studentGroups}"                                            
                varStatus="counter">                                                               
                <c:url var="loginLink" value="/authenticate/PerformLogin"/>                        
                                                                                                   
                <tr class="r${counter.index % 2}">                                                 
                <td class="description">${student.firstname} ${student.lastname}                  
                <td class="description">${student.campusUID}                                       
                                                                                                   
                <td>            <form name="PerformLogin" method="post" action="${loginLink}" >    
                                <input type="hidden" name="campusUID" value="${student.campusUID}"/>
                		<input type="hidden" name="uidPassword" value="<%= request.getRemoteUser() %>">
                                <input type="submit" value="as"/>                                  
                                </form>                                                            
                                </td>                                                              
                                                                                                   
                                </tr>                                                              
                                                                                                   
                                </c:forEach>                                                       
	</table> 

  <!-- <ss:loginForm/> -->


  <ss:footer/>
  </body>
</html>
