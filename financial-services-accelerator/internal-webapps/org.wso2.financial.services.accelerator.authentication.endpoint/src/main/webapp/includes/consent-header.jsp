<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>

<c:choose>
    <c:when test="${isReauthorization}">
        <h3 class="ui header">
            <strong>${app}</strong> requests to re-authenticate following details.
        </h3>
    </c:when>
    <c:otherwise>
        <h3 class="ui header">
            <strong>${app}</strong> requests following details.
        </h3>
    </c:otherwise>
</c:choose>