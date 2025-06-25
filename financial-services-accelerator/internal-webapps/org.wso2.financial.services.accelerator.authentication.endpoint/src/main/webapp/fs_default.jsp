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

                        <%--Display consent open banking logo--%>
                        <jsp:include page="includes/logo.jsp"/>

                        <div class="row data-container">
                            <div class="clearfix"></div>
                            <form action="${pageContext.request.contextPath}/oauth2_authz_confirm.do" method="post" id="oauth2_authz_confirm"
                                  name="oauth2_authz_confirm" class="form-horizontal">
                                <div class="login-form">
                                    <div class="form-group ui form">
                                        <div class="col-md-12 ui box">
                                        
                                            <%--Display consent page header--%>
                                            <h3 class="ui header">
                                                <strong>${app}</strong> requests following details.
                                            </h3>

                                            <h4 class="section-heading-5 ui subheading">Data requested:</h4>

                                            <%--Display basic consent data--%>
                                            <jsp:include page="includes/basic-consent-data.jsp"/>

                                            <c:if test="${not empty permissions}">
                                                <%-- If permissions are specified --%>
                                                <jsp:include page="includes/accounts-with-permissions.jsp"/>
                                            </c:if>
                                            <c:if test="${empty permissions}">
                                                <%-- If permissions are not specified --%>
                                                <jsp:include page="includes/accounts.jsp"/>
                                            </c:if>
                                        </div>
                                    </div>

                                    <jsp:include page="includes/confirmation-dialogue.jsp"/>
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
