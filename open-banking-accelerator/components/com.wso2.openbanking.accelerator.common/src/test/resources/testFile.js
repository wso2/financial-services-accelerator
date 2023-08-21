<!--
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
 -->
var psuChannel = 'Online Banking';

function onLoginRequest(context) {
    reportingData(context, "AuthenticationAttempted", false, psuChannel);

    executeStep(1, {
        onSuccess: function (context) {
            var supportedAcrValues = ['urn:openbanking:psd2:sca', 'urn:openbanking:psd2:ca'];
            var selectedAcr = selectAcrFrom(context, supportedAcrValues);
            reportingData(context, "AuthenticationSuccessful", false, psuChannel);

            if (isACREnabled()) {

                context.selectedAcr = selectedAcr;
                if (isTRAEnabled()) {
                    if (selectedAcr === 'urn:openbanking:psd2:ca') {
                        executeTRAFunction(context);
                    } else {
                        executeStep(2, {
                            onSuccess: function (context) {
                                reportingData(context, "AuthenticationSuccessful", true, psuChannel);
                            },
                            onFail: function (context) {
                                reportingData(context, "AuthenticationFailed", false, psuChannel);
                            }
                        });
                    }
                } else {
                    if (selectedAcr == 'urn:openbanking:psd2:sca') {
                        executeStep(2, {
                            onSuccess: function (context) {
                                reportingData(context, "AuthenticationSuccessful", true, psuChannel);
                            },
                            onFail: function (context) {
                                reportingData(context, "AuthenticationFailed", false, psuChannel);
                            }
                        });
                    }
                }

            } else {
                if (isTRAEnabled()) {
                    executeTRAFunction(context);
                } else {
                    executeStep(2, {
                        onSuccess: function (context) {
                            reportingData(context, "AuthenticationSuccessful", true, psuChannel);
                        },
                        onFail: function (context) {
                            reportingData(context, "AuthenticationFailed", false, psuChannel);
                        }
                    });
                }
            }
        },
        onFail: function (context) { //basic auth fail
            reportingData(context, "AuthenticationFailed", false, psuChannel);
            //retry
        }
    });
}
