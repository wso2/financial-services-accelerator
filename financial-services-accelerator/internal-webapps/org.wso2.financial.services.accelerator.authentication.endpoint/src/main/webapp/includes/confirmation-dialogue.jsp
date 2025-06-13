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
                onclick="javascript: denyConsent(); return false;"/>
        <input type="button" class="btn btn-primary" id="back" name="back"
                onclick="history.back();"
                value="Go Back"/>
        <input type="hidden" id="hasApprovedAlways" name="hasApprovedAlways" value="false"/>
        <input type="hidden" name="sessionDataKeyConsent" value="${sessionDataKeyConsent}"/>
        <input type="hidden" name="isApproved" id="isApproved" value="false"/>
        <input type="hidden" name="type" id="type" value="${type}"/>
        <input type="hidden" name="isReauthorization" id="type" value="${isReauthorization}"/>
        <input type="hidden" name="encodedAccountsPermissionsData" value="${encodedAccountsPermissionsData}"/>
    </div>
</div>