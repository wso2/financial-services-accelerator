<%--
~ Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>

<html>
    <head>
        <jsp:include page="includes/head.jsp"/>
    </head>

    <body>
        <div class="page-content-wrapper" style="position: relative; min-height: 100vh;">
            <div class="container-fluid " style="padding-bottom: 40px">
                <div class="container">
                    <div class="login-form-wrapper">
                        <div class="row">
                            <img src="images/logo-dark.svg"
                                 class="img-responsive brand-spacer login-logo" alt="WSO2 Open Banking"/>
                        </div>
                        <div class="row data-container">
                            <div class="clearfix"></div>
                            <form action="${pageContext.request.contextPath}/oauth2_authz_confirm.do" method="post" id="oauth2_authz_confirm"
                                  name="oauth2_authz_confirm" class="form-horizontal">
                                <div class="login-form">
                                    <div class="form-group ui form">
                                        <div class="col-md-12 ui box">
                                            <h3 class="ui header">
                                                <strong>${app}</strong> requests following details.
                                            </h3>

                                            <h4 class="section-heading-5 ui subheading">Data requested:</h4>
                                            <!--Display basic consent data-->
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

                                            <c:if test="${not empty permissions}">
                                            <%-- If requested permissions are specified --%>
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
                                                                        <c:choose>
                                                                            <c:when test="${allowMultipleAccounts}">
                                                                                <div class="padding-left padding-top" >
                                                                                    <c:forEach items="${consumerAccounts}" var="record">
                                                                                        <label for="${record['displayName']}${permissionLoop.index}">
                                                                                            <input type="checkbox" id="${record['displayName']}${permissionLoop.index}" name="chkAccounts"
                                                                                                value="${record['accountId']}"/>
                                                                                                ${record['displayName']}
                                                                                        </label>
                                                                                        <br>
                                                                                    </c:forEach>
                                                                                </div>
                                                                            </c:when>
                                                                            <c:otherwise>
                                                                                <div class="padding-left padding-top">
                                                                                    <select name="selectedAccount${permissionLoop.index}" id="selectedAccount${permissionLoop.index}">
                                                                                        <option hidden disabled selected value> -- Select an Account  -- </option>
                                                                                        <c:forEach items="${consumerAccounts}" var="record">
                                                                                            <option  value="${record['accountId']}">
                                                                                                    ${record['displayName']}</option>
                                                                                        </c:forEach>
                                                                                    </select>
                                                                                </div>
                                                                            </c:otherwise>
                                                                        </c:choose>
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
                                            </c:if>
                                            <c:if test="${empty permissions}">
                                                <%-- If requested permissions are not specified --%>
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
                                                        <c:choose>
                                                            <c:when test="${allowMultipleAccounts}">
                                                                <div class="col-md-12" >
                                                                    <c:forEach items="${consumerAccounts}" var="record">
                                                                        <label for="${record['displayName']}">
                                                                            <input type="checkbox" id="${record['displayName']}" name="chkAccounts"
                                                                                value="${record['accountId']}"/>
                                                                                ${record['displayName']}
                                                                        </label>
                                                                        <br>
                                                                    </c:forEach>
                                                                </div>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <div class="col-md-12">
                                                                    <select name="selectedAccount" id="selectedAccount">
                                                                        <option hidden disabled selected value> -- Select an Account  -- </option>
                                                                        <c:forEach items="${consumerAccounts}" var="record">
                                                                            <option  value="${record['accountId']}">
                                                                                    ${record['displayName']}</option>
                                                                        </c:forEach>
                                                                    </select>
                                                                </div>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <!-- 3 -->
                                                        <b>No consumer accounts provided for authroization.</b>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:if>
                                        </div>
                                    </div>

                                    <div class="form-group ui form">
                                        <div class="col-md-12 ui box">
                                            If you want to stop sharing data, you can request us to stop sharing data on your data sharing
                                            dashboard.
                                            </br>
                                            Do you confirm that we can share your data with ${app}?
                                        </div>
                                    </div>

                                    <div class="form-group ui form row">
                                        <div class="ui body col-md-12">
                                            <input type="button" class="btn btn-primary" id="approve" name="approve"
                                                   value="Confirm"/>
                                            <input class="btn btn-primary" type="reset" value="Deny"/>
                                            <input type="button" class="btn btn-primary" id="back" name="back"
                                                   onclick="history.back();"
                                                   value="Go Back"/>
                                        </div>
                                    </div>

                                    <div class="form-group ui form row">
                                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                            <div class="well policy-info-message" role="alert margin-top-5x">
                                                <div>
                                                    ${privacyDescription}
                                                    <a href="privacy_policy.do" target="policy-pane">
                                                        ${privacyGeneral}
                                                    </a>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>

            <jsp:include page="includes/footer.jsp"/>
        </div>
    </body>
</html>
