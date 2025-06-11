<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>

<c:forEach items="${basicConsentData}" var="record">
    <div class="padding" style="border:1px solid #555;">
        <b>${record.key}</b>
        <ul class="scopes-list padding">
            <c:forEach items="${record.value}" var="record_data">
                <li>${record_data}</li>
            </c:forEach>
        </ul>
    </div>
</c:forEach>