<%--
 ~ Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>

<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="includes/consent_top.jsp"/>
<%
    session.setAttribute("configParamsMap", request.getAttribute("data_requested"));
    Map<String, List<String>> consentData = (Map<String, List<String>>) request.getAttribute("data_requested");
%>
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
                    <%-- Setting payments related data --%>
                    <c:when test="${consent_type eq 'payments'}">
                        <c:if test="${not empty selectedAccount}">
                            <div class="form-group ui form">
                                <div class="col-md-12 ui box">
                                    <strong> Selected Account: ${selectedAccount} </strong>
                                </div>
                            </div>
                        </c:if>
                        <c:if test="${empty selectedAccount && not empty account_data}">
                            <div class="form-group ui form select">
                                <h5 class="ui body col-md-12">
                                    Select the accounts you wish to authorise:
                                </h5>
                                <div class="col-md-12">
                                    <c:forEach items="${account_data}" var="record">
                                        <label for="${record['display_name']}">
                                            <input type="checkbox" id="${record['display_name']}" name="chkAccounts"
                                                   value="${record['account_id']}" onclick="updatePaymentAcc()()"
                                            />
                                                ${record['display_name']}
                                        </label>
                                        <br>
                                    </c:forEach>
                                </div>
                            </div>
                        </c:if>
                    </c:when>
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
                           onclick="javascript: approvedAU(); return false;"
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
                    <input type="hidden" name="accounts[]" id="account" value="">
                    <input type="hidden" name="type" id="type" value="${consent_type}"/>
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
    </form>
</div>
<jsp:include page="includes/consent_bottom.jsp"/>
