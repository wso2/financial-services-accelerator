<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>

<c:choose>
    <c:when test="${not empty initiatedAccountsForConsent}">
        <h5 class="ui body col-md-12">
            Access to following accounts shall be authorized:
        </h5>
        <b>
            <ul class="scopes-list padding padding-left-triple">
                <c:forEach items="${initiatedAccountsForConsent}" var="account">
                    <li>${account.displayName}</li>
                </c:forEach>
            </ul>
        </b>
    </c:when>
    <c:when test="${not empty consumerAccounts}">
        <h5 class="ui body col-md-12">
            Select the accounts to which you wish to authorize access:
        </h5>
        <jsp:include page="account-selection.jsp">
            <jsp:param name="accountSelectorClass" value="col-md-12"/>
            <jsp:param name="ignorePreSelect" value="false"/>
        </jsp:include>
    </c:when>
    <c:otherwise>
        <!-- 3 -->
        <b>No consumer accounts provided for authroization.</b>
    </c:otherwise>
</c:choose>