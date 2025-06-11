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