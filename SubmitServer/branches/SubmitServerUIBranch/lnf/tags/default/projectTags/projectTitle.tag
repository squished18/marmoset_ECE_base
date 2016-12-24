<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>

<h1>Project ${project.projectNumber}: ${project.title}</h1>
<p>${project.description} 

<p><b>Deadline:</b>
	<fmt:formatDate value="${project.ontime}" pattern="E',' dd MMM 'at' hh:mm a" />
<c:if test="${project.ontime != project.late}">
(Late:
	<fmt:formatDate value="${project.late}" pattern="E',' dd MMM 'at' hh:mm a" />)
</c:if>	