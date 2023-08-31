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
    <jsp:include page="head.jsp"/>
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
                