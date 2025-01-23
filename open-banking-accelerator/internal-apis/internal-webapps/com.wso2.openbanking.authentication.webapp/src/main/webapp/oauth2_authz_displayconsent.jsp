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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.time.LocalDateTime" %>
<jsp:include page="includes/consent_top.jsp"/>

<%
    String sessionDataKeyConsent = Encode.forHtmlContent(request.getParameter("sessionDataKeyConsent"));
    String accounts = Encode.forHtmlContent(request.getParameter("accountsArry[]"));
    String accountNames = Encode.forHtmlContent(request.getParameter("accNames"));
    String appName = Encode.forHtmlContent(request.getParameter("app"));
    String consentId = Encode.forHtmlContent(request.getParameter("id"));
    String userName = Encode.forHtmlContent(request.getParameter("user"));
    String[] accountList = accountNames.split(":");
    String consentExpiryDate = Encode.forHtmlContent(request.getParameter("consent-expiry-date"));
    String consentExpiry = consentExpiryDate.split("T")[0];
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDateTime now = LocalDateTime.now();
    String currentDate = dtf.format(now);
    Map<String, List<String>> consentData = (Map<String, List<String>>) session.getAttribute("configParamsMap");
    session.setAttribute("configParamsMap", consentData);
%>
<div class="col-xs-12 col-sm-12 col-md-9 col-lg-9 data-container">
    <div class="clearfix"></div>
    <form action="${pageContext.request.contextPath}/oauth2_authz_confirm.do" method="post" id="oauth2_authz_confirm"
          name="oauth2_authz_confirm" class="form-horizontal">
        <div class="login-form">
            <div class="form-group ui form">
                <div class="col-md-12 ui box">
                    <h3 class="ui header"><strong><%=appName%>
                    </strong> requests account details on your account.</h3>
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

                    <div class="padding-top">
                        <h5 class="section-heading-5 ui subheading">
                            Accounts selected:
                        </h5>
                        <div class="padding-left">
                            <ul class="scopes-list padding">
                                <!--Display selected accounts-->
                                <c:forEach items="<%=accountList%>" var="account">
                                    <li>${account}</li>
                                </c:forEach>
                            </ul>
                        </div>
                    </div>
                    <div class="ui">
                        If you want to stop sharing data, you can request us to stop sharing data on your data sharing
                        dashboard.
                        </br>
                        Do you confirm that we can share your data with <%=appName%>?
                    </div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-md-12">
                    <div class="padding" style="border:1px solid #555;">
                        <input type="button" class="btn btn-primary" id="approve" name="approve"
                               onclick="javascript: approvedAU(); return false;"
                               value="Confirm"/>
                        <input class="btn btn-primary" type="reset" value="Deny"
                               onclick="javascript: deny(); return false;"/>
                        <input type="button" class="btn btn-primary" id="back" name="back"
                               onclick="history.back();"
                               value="Go Back"/>
                        <input type="hidden" id="hasApprovedAlways" name="hasApprovedAlways" value="false"/>
                        <input type="hidden" name="sessionDataKeyConsent" value="<%=sessionDataKeyConsent%>"/>
                        <input type="hidden" name="consent" id="consent" value="deny"/>
                        <input type="hidden" name="app" id="app" value="<%=appName%>"/>
                        <input type="hidden" name="type" id="type" value="accounts"/>
                        <input type="hidden" name="accounts[]" id="account" value="<%=accounts%>">
                    </div>
                </div>
            </div>

            <div class="form-group">
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

<jsp:include page="includes/consent_bottom.jsp"/>
