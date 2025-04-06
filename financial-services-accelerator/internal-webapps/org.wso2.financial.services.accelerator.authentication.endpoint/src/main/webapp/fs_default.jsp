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
        <script src="js/auth-functions.js"></script>
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
                            <%
                                session.setAttribute("configParamsMap", request.getAttribute("data_requested"));
                                Map<String, List<String>> consentData = (Map<String, List<String>>) request.getAttribute("data_requested");
                            %>

                            <div class="clearfix"></div>
                            <form action="${pageContext.request.contextPath}/oauth2_authz_confirm.do" method="post" id="oauth2_authz_confirm"
                                  name="oauth2_authz_confirm" class="form-horizontal">
                                <div class="login-form">
                                    <div class="form-group ui form">
                                        <div class="col-md-12 ui box">
                                            <h3 class="ui header">

                                                <%-- Change heading based on the consent type --%>
                                                <c:choose>
                                                    <c:when test="${consent_type eq 'default'}">
                                                        <strong>${app}</strong> requests following details.
                                                    </c:when>
                                                    <c:when test="${consent_type eq 'accounts'}">
                                                        <strong>${app}</strong> requests account details on your account.
                                                    </c:when>
                                                    <c:when test="${consent_type eq 'fundsconfirmations'}">
                                                        <strong>${app}</strong> requests access to confirm the availability of funds in your account.
                                                    </c:when>
                                                    <c:when test="${consent_type eq 'payments'}">
                                                        <strong>${app}</strong> requests consent to do a payment transaction ${intentSubText}
                                                    </c:when>
                                                </c:choose>
                                            </h3>

                                            <h4 class="section-heading-5 ui subheading">Data requested:</h4>
                                            <!--Display requested data-->
                                            <c:forEach items="<%=consentData%>" var="record">
                                                <div class="padding" style="border:1px solid #555;">
                                                    <b>${record.key}</b>
                                                    <ul class="scopes-list padding">
                                                        <c:forEach items="${record.value}" var="record_data">
                                                            <li>${record_data}</li>
                                                        </c:forEach>
                                                    </ul>
                                                </div>
                                            </c:forEach>
                                        </div>

                                        <%-- Setting data based on the consent type --%>
                                        <c:choose>
                                            <%-- Setting accounts related data --%>
                                            <c:when test="${consent_type eq 'accounts' || consent_type eq 'default'}">
                                                <div class="form-group ui form select">
                                                    <c:if test="${not empty account_data}">
                                                        <h5 class="ui body col-md-12">
                                                            Select the accounts you wish to authorize:
                                                        </h5>
                                                        <div class="col-md-12" >
                                                            <c:forEach items="${account_data}" var="record">
                                                                <label for="${record['display_name']}">
                                                                    <input type="checkbox" id="${record['display_name']}" name="chkAccounts"
                                                                           value="${record['account_id']}" onclick="updateAcc()"
                                                                    />
                                                                        ${record['display_name']}
                                                                </label>
                                                                <br>
                                                            </c:forEach>
                                                        </div>
                                                    </c:if>
                                                </div>
                                            </c:when>

                                            <%-- Setting confirmation of funds related data --%>
                                            <c:when test="${consent_type eq 'fundsconfirmations'}">
                                                <c:if test="${isReauthorization eq true}">
                                                    <div class="form-group ui form">
                                                        <strong>
                                                            Note: You are about to re-authorize an intent which had been authorized before.<br/>
                                                        </strong>
                                                    </div>
                                                </c:if>
                                            </c:when>

                                            <%-- Setting payments related data --%>
                                            <c:when test="${consent_type eq 'payments'}">
                                                <c:if test="${not empty selectedAccount}">
                                                    <div class="form-group ui form">
                                                        <div class="col-md-12 ui box">
                                                            <strong> Selected Account: ${selectedAccount} </strong>
                                                        </div>
                                                    </div>
                                                    <input type="hidden" name="selectedAccount" id="selectedAccount" value="${selectedAccount}"/>
                                                </c:if>

                                                <c:if test="${empty selectedAccount && not empty account_data}">
                                                    <div class="form-group ui form select">
                                                        <h5 class="ui body col-md-12">
                                                            Select the accounts you wish to authorize:
                                                        </h5>
                                                        <div class="col-md-12">
                                                            <select name="selectedAccount" id="selectedAccount">
                                                                <option hidden disabled selected value> -- Select an Account  -- </option>
                                                                <c:forEach items="${account_data}" var="record">
                                                                    <option  value="${record['account_id']}">
                                                                            ${record['display_name']}</option>
                                                                </c:forEach>
                                                            </select>
                                                        </div>
                                                    </div>
                                                </c:if>
                                            </c:when>

                                            <%-- When consent type is not specified default view --%>
                                            <c:otherwise>
                                                <c:if test="${not empty account_data}">
                                                    <div class="form-group ui form select">
                                                        <h5 class="ui body col-md-12">
                                                            Select the accounts you wish to authorize:
                                                        </h5>
                                                        <div class="col-md-12" >
                                                            <c:forEach items="${account_data}" var="record">
                                                                <label for="${record['display_name']}">
                                                                    <input type="checkbox" id="${record['display_name']}" name="chkAccounts"
                                                                           value="${record['account_id']}" onclick="updateAcc()"
                                                                    />
                                                                        ${record['display_name']}
                                                                </label>
                                                                <br>
                                                            </c:forEach>
                                                        </div>
                                                    </div>
                                                </c:if>
                                            </c:otherwise>
                                        </c:choose>
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
                                                   onclick="javascript: approvedConsent(); return false;"
                                                   value="Confirm"/>
                                            <input class="btn btn-primary" type="reset" value="Deny"
                                                   onclick="javascript: deny(); return false;"/>
                                            <input type="button" class="btn btn-primary" id="back" name="back"
                                                   onclick="history.back();"
                                                   value="Go Back"/>
                                            <input type="hidden" id="hasApprovedAlways" name="hasApprovedAlways" value="false"/>
                                            <input type="hidden" name="sessionDataKeyConsent" value="${sessionDataKeyConsent}"/>
                                            <input type="hidden" name="consent" id="consent" value="deny"/>
                                            <input type="hidden" name="app" id="app" value="${app}"/>
                                            <input type="hidden" name="type" id="type" value="${consent_type}"/>
                                            <input type="hidden" name="accounts[]" id="account" value="">
                                            <input type="hidden" name="accNames" id="accountName" value=""/>
                                            <input type="hidden" name="paymentAccount" id="paymentAccount"
                                                   value="${selectedAccount}"/>
                                            <input type="hidden" name="cofAccount" id="cofAccount" value="${AccountId}"/>
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
