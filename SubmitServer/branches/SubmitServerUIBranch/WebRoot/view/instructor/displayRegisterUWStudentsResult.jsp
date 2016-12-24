<%@ page language="java" %>
<%@ page import="ca.uwaterloo.cs.submitServer.RegistrationStatus" %>
<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<ss:head title="Results for registering students for ${course.courseName}
in semester ${course.semester}" />
<body>
  <ss:header />
  <ss:instructorBreadCrumb />
  <h1>Results for registering students from a classlist for ${course.courseName}
  in semester ${course.semester}</h1>
  <p>
  <c:choose>
    <c:when test="${registrationResult != null}">
      <c:choose>
        <c:when test="${not empty registrationResult}">
        <table>
          <tr>
            <th>Student</th>
            <th>Registration Status</th>
          </tr>
        <c:forEach var="student" items="${registrationResult}"
            varStatus="counter">
          <tr class="r${counter.index % 2}">
            <td>${student.firstName} ${student.lastName} (${student.userId})</td>
            <c:choose>
              <c:when test="${student.status == 'AlreadyRegistrated'}">
                <td>Already Registrated</td>
              </c:when>
              <c:when test="${student.status == 'NewlyRegistrated'}">
                <td>Newly Registrated</td>
              </c:when>
              <c:otherwise>
                <td>Unknown Value</td>
              </c:otherwise>
            </c:choose>
          </tr>
        </c:forEach>
        </table>
        </c:when>
        <c:otherwise>
          No student when imported from the classlist.
        </c:otherwise>
      </c:choose>
    </c:when>
    <c:otherwise>
      No results to display.
    </c:otherwise>
  </c:choose>
  </p>
  <ss:footer />
</body>
</html>
