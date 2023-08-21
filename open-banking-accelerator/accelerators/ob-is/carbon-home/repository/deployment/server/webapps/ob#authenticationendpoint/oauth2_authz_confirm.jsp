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

<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<html>
<head></head>
<body>

    <p>You will be redirected back to the <%=Encode.forHtmlContent(request.getParameter("app"))%>. If the
        redirection fails, please click the post button.....</p>

    <form method="post" id="oauth2_authz" name="oauth2_authz" action="../../oauth2/authorize">
        <input type="hidden" id="hasApprovedAlways" name="hasApprovedAlways" value="${hasApprovedAlways}"/>
        <input type="hidden" name="sessionDataKeyConsent" value="${sessionDataKeyConsent}"/>
        <input type="hidden" name="consent" id="consent" value="${consent}"/>
        <input type="hidden" name="user" id="user" value="${user}"/>
        <button type="submit">POST</button>
    </form>

    <script type="text/javascript">
        document.forms[0].submit();
    </script>

</body>
</html>
