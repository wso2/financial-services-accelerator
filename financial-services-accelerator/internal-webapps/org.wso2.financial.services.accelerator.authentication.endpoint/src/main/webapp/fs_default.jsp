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

<%
    Map<String, Object> consentData = (Map<String, Object>) request.getAttribute("consentData");
    Map<String, Object> consumerData = (Map<String, Object>) request.getAttribute("consumerData");

    Map<String, List<String>> basicConsentData = (Map<String, List<String>>) consentData.get("basicConsentData");
    Map<String, Object> requestedPermissions = (Map<String, Object>) consentData.getOrDefault("requestedPermissions", null);
    List<Object> initiatedAccountsForConsent = (List<Object>) consentData.getOrDefault("initiatedAccountsForConsent", null);
    Boolean isReauthorization = (Boolean) consentData.getOrDefault("isReauthorization", false);
    Boolean allowMultipleAccounts = (Boolean) consentData.getOrDefault("allowMultipleAccounts", false);
    String consent_type = (String) consentData.get("type");

    // Expand requested permissions
    List<Map<String, Object>> permissions = null;
    Boolean displayConsumerAccountsPerPermission = false;
    if (requestedPermissions != null) {
        permissions = (List<Map<String, Object>>) requestedPermissions.get("permissions");
        displayConsumerAccountsPerPermission = (Boolean) requestedPermissions.getOrDefault("displayConsumerAccountsPerPermission", true);
    }

    // Extract consumer accounts
    List<Map<String, Object>> consumerAccounts = null;
    if (consumerData != null) {
        consumerAccounts = (List<Map<String, Object>>) consumerData.getOrDefault("accounts", null);
    }

    // Logging
    System.out.println("[JSP DEBUG] consentData is " + (consentData == null ? "null" : "present"));
    System.out.println("[JSP DEBUG] consumerData is " + (consumerData == null ? "null" : "present"));
    System.out.println("[JSP DEBUG] basicConsentData is " + (basicConsentData == null ? "null" : "present"));
    System.out.println("[JSP DEBUG] requestedPermissions is " + (requestedPermissions == null ? "null" : "present"));
    System.out.println("[JSP DEBUG] permissions is " + (permissions == null ? "null" : "present"));
    System.out.println("[JSP DEBUG] initiatedAccountsForConsent is " + (initiatedAccountsForConsent == null ? "null" : "present"));
    System.out.println("[JSP DEBUG] consumerAccounts is " + (consumerAccounts == null ? "null" : "present"));
    System.out.println("[JSP DEBUG] isReauthorization = " + isReauthorization);
    System.out.println("[JSP DEBUG] allowMultipleAccounts = " + allowMultipleAccounts);
    System.out.println("[JSP DEBUG] displayConsumerAccountsPerPermission = " + displayConsumerAccountsPerPermission);

    // Set request attributes for EL access
    request.setAttribute("displayConsumerAccountsPerPermission", displayConsumerAccountsPerPermission);
    request.setAttribute("permissions", permissions);
    request.setAttribute("initiatedAccountsForConsent", initiatedAccountsForConsent);
    request.setAttribute("consumerAccounts", consumerAccounts);
    request.setAttribute("allowMultipleAccounts", allowMultipleAccounts);

    // Consent type mapping
    if (consent_type.contains("payment")) {
        request.setAttribute("generic_consent_type", "payments");
    } else if (consent_type.contains("account")) {
        request.setAttribute("generic_consent_type", "accounts");
    } else if (consent_type.contains("funds") && consent_type.contains("confirmation")) {
        request.setAttribute("generic_consent_type", "fundsconfirmations");
    } else {
        request.setAttribute("generic_consent_type", "default");
    }
%>

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
                                                <%-- Change heading based on the consent type --%>
                                                <c:choose>
                                                    <c:when test="${generic_consent_type eq 'default'}">
                                                        <strong>${app}</strong> requests following details.
                                                    </c:when>
                                                    <c:when test="${generic_consent_type eq 'accounts'}">
                                                        <strong>${app}</strong> requests account details on your account.
                                                    </c:when>
                                                    <c:when test="${generic_consent_type eq 'fundsconfirmations'}">
                                                        <strong>${app}</strong> requests access to confirm the availability of funds in your account.
                                                    </c:when>
                                                    <c:when test="${generic_consent_type eq 'payments'}">
                                                        <strong>${app}</strong> requests consent to do a payment transaction ${intentSubText}
                                                    </c:when>
                                                </c:choose>
                                            </h3>

                                            <h4 class="section-heading-5 ui subheading">Data requested:</h4>
                                            <!--Display basic consent data-->
                                            <c:forEach items="<%=basicConsentData%>" var="record">
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
                                                <c:choose>
                                                    <c:when test="${!displayConsumerAccountsPerPermission}">
                                                        <%-- If all permissions are grouped together --%>
                                                        <%-- Initiated accounts per permissions ignored --%>
                                                        <div class="padding" style="border:1px solid #555;">
                                                            <b>Requested Permissions:</b>
                                                            <ul class="scopes-list padding">
                                                                <c:forEach items="${permissions}" var="permission">
                                                                    <c:forEach items="${permission.displayValues}" var="displayValue">
                                                                        <li>${displayValue}</li>
                                                                    </c:forEach>
                                                                </c:forEach>
                                                            </ul>

                                                            <c:choose>
                                                                <c:when test="${not empty initiatedAccountsForConsent}">
                                                                    <%-- If initiated accounts are specified for consent, display them --%>
                                                                    <b>On following accounts:</b>
                                                                    <ul class="scopes-list padding">
                                                                        <c:forEach items="${initiatedAccountsForConsent}" var="account">
                                                                            <li>${account.displayName}</li>
                                                                        </c:forEach>
                                                                    </ul>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <%-- If no initiated accounts are specified, allow selection from consumerAccounts --%>
                                                                    <c:choose>
                                                                        <c:when test="${not empty consumerAccounts}">
                                                                            <b>Select the accounts you wish to authorize:</b>
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
                                                                            <!-- 1 -->
                                                                            <b>No consumer accounts provided for authroization.</b>
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <%-- If permissions are separated --%>
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
                                                                    <c:when test="${not empty initiatedAccounts}">
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
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:if>
                                            <c:if test="${empty permissions}">
                                                <%-- If requested permissions are not specified --%>
                                                <c:choose>
                                                    <c:when test="${not empty consumerAccounts}">
                                                        <div class="form-group ui form select">
                                                            <h5 class="ui body col-md-12">
                                                                Select the accounts you wish to authorize:
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
                                                        </div>
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
