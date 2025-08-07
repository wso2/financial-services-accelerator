<%--
~ Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
~
~ WSO2 LLC. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~     http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>

<c:forEach items="${permissions}" var="permission" varStatus="permissionLoop">
    <input type="hidden" name="permission-${permissionLoop.index}" value="${permission.uid}"/>
    <div class="padding" style="border:1px solid #555;">
        <b>${requestedPermissions}</b>
        <ul class="scopes-list padding">
            <c:forEach items="${permission.displayValues}" var="displayValue">
                <li>${displayValue}</li>
            </c:forEach>
        </ul>

        <%-- Ignores initiated accounts per permission if initiated accounts for consent are given --%>
        <c:choose>
            <c:when test="${not empty initiatedAccountsForConsent}">
                <%-- View consent initiated accounts --%>
                <b>${onFollowingAccounts}</b>
                <ul class="scopes-list padding">
                    <c:forEach items="${initiatedAccountsForConsent}" var="account">
                        <li>${account.displayName}</li>
                    </c:forEach>
                </ul>
            </c:when>
            <c:when test="${not empty permission.initiatedAccounts}">
                <%-- View accounts initiated per permission --%>
                <b>${onFollowingAccounts}</b>
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
                        <b>${selectAccounts}</b>
                        <jsp:include page="account-selection.jsp">
                            <jsp:param name="accountSelectorClass" value="padding-left padding-top"/>
                            <jsp:param name="idSuffix" value="${permissionLoop.index}"/>
                            <jsp:param name="ignorePreSelect" value="${hasMultiplePermissions}"/>
                        </jsp:include>
                    </c:when>
                    <c:otherwise>
                        <b>${noConsumerAccounts}</b>
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>
    </div>
</c:forEach>
                                            
<%--Display re-authentication disclaimer--%>
<jsp:include page="re-authentication-disclaimer.jsp"/>
