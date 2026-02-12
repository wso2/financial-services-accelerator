<%@ page import="static org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.utils.Utils.i18n" %><%--
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
<%@ page import="java.util.*" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <base href="${pageContext.request.contextPath}/">
    <jsp:include page="includes/head.jsp"/>
    <script src="js/auth-functions.js"></script>
</head>
<body dir="${textDirection}">
    <div class="page-content-wrapper" style="position: relative; min-height: 100vh;">
        <div class="container-fluid" style="padding-bottom: 40px">
            <div class="container">
                <div class="login-form-wrapper">
                    <jsp:include page="includes/logo.jsp"/>

                    <div class="row">
                        <div class="col-md-5 col-md-offset-3">
                            <div class="row data-container">
                                <div class="clearfix"></div>
                                <div class="login-form">
                                    <div class="form-group">
                                        <div class="col-md-12">
                                            <h3 class="section-heading-3" style="color: white; text-align: center;">SMS OTP Verification</h3>

                                            <c:if test="${not empty otpError}">
                                                <div class="alert alert-danger" role="alert">${otpError}</div>
                                            </c:if>

                                            <p style="color: white; text-align: left;">An SMS containing a one-time code was sent to your registered phone number. Enter it below to continue.</p>

                                            <form method="post" action="consent_smsotp.do" class="form-horizontal" id="otpForm">
                                                <input type="hidden" name="otpAction" value="verify"/>
                                                <div class="form-group" style="text-align: center; padding: 0 20px;">
                                                    <input id="smsOtp" name="smsOtp" type="text" class="form-control" placeholder="Enter OTP" maxlength="6" required style="background-color: white; color: black; width: 100%;" />
                                                </div>
                                                <div class="form-group" style="text-align: right; padding: 0 20px;">
                                                    <button class="btn btn-primary" type="submit" style="margin: 5px;">Verify</button>
                                                    <button class="btn btn-default" type="button" onclick="denyConsent();" style="margin: 5px;">Cancel</button>
                                                </div>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Hidden form for consent denial (used by denyConsent() function) -->
                    <form id="oauth2_authz_confirm" method="post" action="oauth2_authz_confirm.do" style="display: none;">
                        <input type="hidden" id="consent" name="consent" value="deny"/>
                        <input type="hidden" name="sessionDataKeyConsent" value="${sessionDataKeyConsent}"/>
                        <input type="hidden" name="type" value="${type}"/>
                        <input type="hidden" name="hasApprovedAlways" value="false"/>
                    </form>
                </div>
            </div>
        </div>
        <jsp:include page="includes/footer.jsp"/>
    </div>
</body>
</html>

