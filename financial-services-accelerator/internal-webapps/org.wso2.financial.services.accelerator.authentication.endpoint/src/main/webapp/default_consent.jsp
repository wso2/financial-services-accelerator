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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.apache.commons.lang.ArrayUtils" %>
<%@ page import="java.util.stream.Stream" %>

<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <jsp:include page="includes/head.jsp"/>
    <script src="libs/jquery_3.5.0/jquery-3.5.0.js"></script>
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
                    <div class="col-md-12">

                        <!-- content -->
                        <div class="container col-xs-10 col-sm-6 col-md-6 col-lg-5
                        col-centered wr-content wr-login col-centered">
                            <div>
                                <h2 class="wr-title uppercase padding-double
                                white boarder-bottom-blue margin-none">
                                    ${openidUserClaims}
                                </h2>
                            </div>

                            <div class="boarder-all ">
                                <div class="clearfix"></div>
                                <div class="padding-double login-form">
                                    <form action="../../oauth2/authorize" method="post" id="profile" name="oauth2_authz"
                                          class="form-horizontal">

                                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                            <div class="alert alert-warning" role="alert">
                                                <p class="margin-bottom-double">
                                                    <strong>${app}
                                                    </strong>
                                                    ${requestAccessProfile}
                                                </p>
                                            </div>
                                        </div>

                                        <c:if test="${userClaimsConsentOnly eq false}">
                                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 white">

                                                <c:if test="${not empty OIDCScopes}">

                                                    <h5 class="section-heading-5">
                                                            ${requestedScopes}
                                                    </h5>
                                                    <div class="border-gray" style="border-bottom: none;">
                                                        <ul class="scopes-list padding">
                                                            <c:forEach items="${OIDCScopes}" var="record">
                                                                <li>${record}</li>
                                                            </c:forEach>
                                                        </ul>
                                                    </div>
                                                </c:if>
                                            </div>
                                        </c:if>


                                        <!-- Prompting for consent is only needed if we have mandatory or requested
                                        claims without any consent -->
                                        <c:if test="${not empty mandatoryClaims || not empty requestedClaims}">

                                            <input type="hidden" name="user_claims_consent" id="user_claims_consent"
                                                   value="true"/>
                                            <!-- validation -->
                                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                                <h5 class="section-heading-5">
                                                        ${requestedAttributes}
                                                </h5>
                                                <div class="border-gray margin-bottom-double">
                                                    <div class="claim-alert" role="alert">
                                                        <p class="margin-bottom-double">
                                                                ${bySelectingFollowingAttributes}
                                                        </p>
                                                    </div>
                                                    <div class="padding">
                                                        <div class="select-all">
                                                            <div class="checkbox">
                                                                <label>
                                                                    <input type="checkbox" name="consent_select_all"
                                                                           id="consent_select_all"/>
                                                                    Select All
                                                                </label>
                                                            </div>
                                                        </div>
                                                        <div class="claim-list">

                                                            <c:forEach items="${mandatoryClaims}" var="record">

                                                                <div class="checkbox claim-cb">
                                                                    <label>
                                                                        <input class="mandatory-claim" type="checkbox"
                                                                               name="consent_${record['claimId']}"
                                                                               id="consent_${record['claimId']}"
                                                                               required/>
                                                                            ${record['displayName']}
                                                                        <span class="required font-medium">*</span>
                                                                    </label>
                                                                </div>
                                                            </c:forEach>

                                                            <c:forEach items="${requestedClaims}" var="record">

                                                                <div class="checkbox claim-cb">
                                                                    <label>
                                                                        <input type="checkbox"
                                                                               name="consent_${record['claimId']}"
                                                                               id="consent_${record['claimId']}"/>
                                                                            ${record['displayName']}
                                                                    </label>
                                                                </div>
                                                            </c:forEach>


                                                        </div>
                                                        <div class="text-left padding-top-double">
                                                        <span class="mandatory">
                                                                ${mandatoryClaimsRecommendation}</span>
                                                            <span class="required font-medium">( * )</span>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </c:if>

                                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                            <div class="alert alert-warning padding-10 margin-bottom-double"
                                                 role="alert">
                                                <div>
                                                    ${privacyDescription}
                                                    <a href="privacy_policy.do" target="policy-pane">
                                                        ${privacyGeneral}
                                                    </a>
                                                </div>
                                            </div>
                                        </div>

                                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                            <table width="100%" class="styledLeft margin-top-double">
                                                <tbody>
                                                <tr>
                                                    <td class="buttonRow" colspan="2">
                                                        <input type="hidden" name="sessionDataKeyConsent"
                                                               value="${sessionDataKeyConsent}"/>
                                                        <input type="hidden" name="consent" id="consent" value="deny"/>
                                                        <div style="text-align:left;">
                                                            <input type="button" class="btn  btn-primary" id="approve"
                                                                   name="approve"
                                                                   onclick="approvedDefaultClaim(); return false;"
                                                                   value="${continueDefault}"/>
                                                            <input class="btn" type="reset"
                                                                   onclick="denyDefaultClaim(); return false;"
                                                                   value="${deny}"/>
                                                        </div>
                                                    </td>
                                                </tr>
                                                </tbody>
                                            </table>
                                        </div>
                                    </form>
                                    <div class="clearfix"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    
    <script src="libs/bootstrap_3.4.1/js/bootstrap.min.js"></script>
    <jsp:include page="includes/footer.jsp"/>
</body>
</html>

