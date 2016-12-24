<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c" %>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt" %>

<br clear="all">
<div class="footer">
<jsp:useBean id="now" class="java.util.Date" />
<fmt:formatDate value="${now}" pattern="dd MMM, hh:mm a"/>
</div>