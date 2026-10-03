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
<%
    // Security Check: Verify that OTP was successfully validated before allowing submission
    Boolean otpVerified = (Boolean) session.getAttribute("otpVerified");
    if (otpVerified == null || !otpVerified) {
        // OTP was not verified - reject this request
        response.sendRedirect("fs_default.do?error=unauthorized");
        return;
    }

    // Clear the verification flag to prevent replay attacks
    session.removeAttribute("otpVerified");

    // Read pending consent from session and render a form that auto-submits to the existing confirmation servlet
    Map<String, List<String>> pending = null;
    if (session != null) {
        Object p = session.getAttribute("pendingConsent");
        if (p != null && p instanceof Map) {
            pending = (Map<String, List<String>>) p;
        }
        // Clear the pending consent from session after reading
        session.removeAttribute("pendingConsent");
    }
%>
<html>
<head>
    <jsp:include page="includes/head.jsp"/>
</head>
<body>
<% if (pending == null) { %>
    <div class="ui message">No pending consent data found. Please go back and try again.</div>
<% } else { %>
    <form id="finalConsentForm" method="post" action="oauth2_authz_confirm.do">
        <% for (Map.Entry<String, List<String>> e : pending.entrySet()) {
            String key = e.getKey();
            for (String v : e.getValue()) { %>
                <input type="hidden" name="<%= key %>" value="<%= v %>" />
        <%  } }
        %>
    </form>
    <script>
        // submit the reconstructed form
        document.getElementById('finalConsentForm').submit();
    </script>
<% } %>
</body>
</html>

