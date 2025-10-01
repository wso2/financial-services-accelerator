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

<%-- View accounts selected from the account selection page --%>
<c:choose>
    <c:when test="${not empty paramValues.accounts}">
        <h5 class="ui body col-md-12">
            ${onFollowingAccounts}
        </h5>
        <b>
            <ul class="scopes-list padding padding-left-triple">
                <c:forEach items="${paramValues.accounts}" var="account">
                    <li>${account}</li>
                    <%-- Passing the selected accounts parameter to the persist flow --%>
                    <input type="hidden" name="accounts" value="${account}" />
                </c:forEach>
            </ul>
        </b>
    </c:when>
    <%-- Display error --%>
    <c:otherwise>
        <b>${noConsumerAccounts}</b>
    </c:otherwise>
</c:choose>
