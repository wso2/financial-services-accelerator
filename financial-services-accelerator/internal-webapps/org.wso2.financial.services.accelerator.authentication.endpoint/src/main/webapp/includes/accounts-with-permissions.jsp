<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>

<c:forEach items="${permissions}" var="permission" varStatus="permissionLoop">
    <div class="padding" style="border:1px solid #555;">
        <b>Requested Permissions:</b>
        <ul class="scopes-list padding">
            <c:forEach items="${permission.displayValues}" var="displayValue">
                <li>${displayValue}</li>
            </c:forEach>
        </ul>

        <%-- Ignores initiated accounts per permission if initiated accounts for consent are given --%>
        <c:choose>
            <c:when test="${not empty initiatedAccountsForConsent}">
                <%-- View consent initiated accounts --%>
                <b>On following accounts:</b>
                <ul class="scopes-list padding">
                    <c:forEach items="${initiatedAccountsForConsent}" var="account">
                        <li>${account.displayName}</li>
                    </c:forEach>
                </ul>
            </c:when>
            <c:when test="${not empty permission.initiatedAccounts}">
                <%-- View accounts initiated per permission --%>
                <b>On following accounts:</b>
                <ul class="scopes-list padding">
                    <c:forEach items="${permission.initiatedAccounts}" var="account">
                        <li>${account.displayName}</li>
                    </c:forEach>
                </ul>
            </c:when>
            <c:otherwise>
                <%-- View accounts from consumer data for selection --%>
                <c:choose>
                    <c:when test="${not empty consumerAccounts}">
                        <b>Select the accounts you wish to authorize:</b>
                        <jsp:include page="account-selection.jsp">
                            <jsp:param name="accountSelectorClass" value="padding-left padding-top"/>
                            <jsp:param name="idSuffix" value="${permissionLoop.index}"/>
                            <jsp:param name="ignorePreSelect" value="true"/>
                        </jsp:include>
                    </c:when>
                    <c:otherwise>
                        <!-- 2 -->
                        <b>No consumer accounts provided for authroization.</b>
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>
    </div>
</c:forEach>