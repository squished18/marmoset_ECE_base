<%@ page language="java"%>
<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>
<c:if test="${singleCourse && instructorCapability && !initParam['demo.server']=='true'}">
	<c:redirect url="/view/instructor/course.jsp">
		<c:param name="coursePK" value="${courseList[0].coursePK}" />
	</c:redirect>
</c:if>
<c:if test="${singleCourse}">
	<c:redirect url="/view/course.jsp">
		<c:param name="coursePK" value="${courseList[0].coursePK}" />
	</c:redirect>
</c:if>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>

<ss:head title="Submit Server Home Page" />

<body>
<ss:header />
<ss:breadCrumb />

<p>Welcome ${user.firstname} ${user.lastname}
<h1>Courses</h1>
<ul>
	<c:choose>
		<c:when test="${user.superUser}">
			<c:set var="courseURL" value="/view/instructor/course.jsp"/>
		</c:when>
		<c:otherwise>
			<c:set var="courseURL" value="/view/course.jsp"/>	
		</c:otherwise>
 	</c:choose>
	
	<c:forEach var="course" items="${courseList}">
		<li><c:url var="courseLink" value="${courseURL}">
				<c:param name="coursePK" value="${course.coursePK}" />
			</c:url>
			<a href="${courseLink}"> ${course.courseName} (${course.semester}):
		${course.description} </a>
	</c:forEach>
</ul>

<ss:footer />
</body>
</html>
