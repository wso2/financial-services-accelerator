<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>

<c:choose>
    <c:when test="${allowMultipleAccounts}">
        <div class="${accountSelectorClass}" >
            <c:forEach items="${consumerAccounts}" var="record">
                <label for="${record['displayName']}${idSuffix}">
                    <input type="checkbox"
                        id="${record['displayName']}${idSuffix}"
                        name="chkAccounts"
                        value="${record['accountId']}"
                        <c:if test="${ignorePreSelect ne 'true' and record['selected']}">checked</c:if>
                        />
                        ${record['displayName']}
                </label>
                <br>
            </c:forEach>
        </div>
    </c:when>
    <c:otherwise>
        <div class="${accountSelectorClass}">
            <select name="selectedAccount${idSuffix}" id="selectedAccount${idSuffix}">
                <option hidden disabled selected value> -- Select an Account  -- </option>
                <c:forEach items="${consumerAccounts}" var="record">
                    <option  value="${record['accountId']}">
                            ${record['displayName']}</option>
                </c:forEach>
            </select>
        </div>
    </c:otherwise>
</c:choose>