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

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% response.setCharacterEncoding("UTF-8"); %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>

<%
    session.setAttribute("initiatedAccountsForConsent", request.getAttribute("initiatedAccountsForConsent"));
    session.setAttribute("selectAccounts", request.getAttribute("selectAccounts"));
    session.setAttribute("reAuthenticationDisclaimer", request.getAttribute("reAuthenticationDisclaimer"));
    session.setAttribute("dataRequested", request.getAttribute("dataRequested"));
    session.setAttribute("buttonGoBack", request.getAttribute("buttonGoBack"));
    session.setAttribute("defaultSelect", request.getAttribute("defaultSelect"));
    session.setAttribute("buttonDeny", request.getAttribute("buttonDeny"));
    session.setAttribute("consumerAccounts", request.getAttribute("consumerAccounts"));
    session.setAttribute("type", request.getAttribute("type"));
    session.setAttribute("basicConsentData", request.getAttribute("basicConsentData"));
    session.setAttribute("hasMultiplePermissions", request.getAttribute("hasMultiplePermissions"));
    session.setAttribute("textDirection", request.getAttribute("textDirection"));
    session.setAttribute("ifStopDataSharing", request.getAttribute("ifStopDataSharing"));
    session.setAttribute("permissions", request.getAttribute("permissions"));
    session.setAttribute("allowMultipleAccounts", request.getAttribute("allowMultipleAccounts"));
    session.setAttribute("appRequestsDetails", request.getAttribute("appRequestsDetails"));
    session.setAttribute("requestedPermissions", request.getAttribute("requestedPermissions"));
    session.setAttribute("noConsumerAccounts", request.getAttribute("noConsumerAccounts"));
    session.setAttribute("isReauthorization", request.getAttribute("isReauthorization"));
    session.setAttribute("buttonNext", request.getAttribute("buttonNext"));
    session.setAttribute("doYouConfirm", request.getAttribute("doYouConfirm"));
    session.setAttribute("handleAccountSelectionSeparately", request.getAttribute("handleAccountSelectionSeparately"));
    session.setAttribute("onFollowingAccounts", request.getAttribute("onFollowingAccounts"));
    session.setAttribute("buttonOk", request.getAttribute("buttonOk"));
%>

<html>
    <head>
        <jsp:include page="includes/head.jsp"/>
        <script src="js/auth-functions.js"></script>
    </head>

    <body dir="${textDirection}">
        <div class="page-content-wrapper" style="position: relative; min-height: 100vh;">
            <div class="container-fluid " style="padding-bottom: 40px">
                <div class="container">
                    <div class="login-form-wrapper">

                        <%--Display consent open banking logo--%>
                        <jsp:include page="includes/logo.jsp"/>

                        <div class="row data-container">
                            <div class="clearfix"></div>
                            <form action="${pageContext.request.contextPath}/fs_default.do" method="post" id="oauth2_authz_account_selection"
                                  name="oauth2_authz_account_selection" class="form-horizontal">
                                <div class="login-form">
                                    <div class="form-group ui form">
                                        <div class="col-md-12 ui box">

                                            <%--Display consent page header--%>
                                            <h3 class="ui header">
                                                ${appRequestsDetails}
                                            </h3>

                                            <%--Display accounts to select--%>
                                            <jsp:include page="includes/accounts.jsp"/>
                                        </div>
                                    </div>

                                    <div class="form-group ui form row">
                                        <div class="ui body col-md-12">
                                            <input type="button" class="btn btn-primary" id="next" name="next"
                                                    onclick="javascript: accountsSelected(); return false;"
                                                    value="${buttonNext}"/>
                                        </div>
                                    </div>

                                    <jsp:include page="includes/privacy-footer.jsp"/>
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
