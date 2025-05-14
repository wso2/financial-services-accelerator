/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils;

import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;

/**
 * Constants class for consent management endpoint.
 */
public class ConsentConstants {

        public static final String CONSENT_KEY = "OauthConsentKey";
        public static final String REQUEST_KEY = "AuthRequestKey";
        public static final String ERROR_PAYLOAD_READ = "Error while reading payload";
        public static final String ERROR_PAYLOAD_PARSE = "Error while parsing payload";
        public static final String ERROR_PERSIST_INVALID_APPROVAL = "Invalid value for approval. Should be true/false";
        public static final String ERROR_PERSIST_APPROVAL_MANDATORY = "Mandatory body parameter approval is " +
                "unavailable";
        public static final String ERROR_NO_TYPE_AND_APP_DATA = "Type and application data is unavailable";
        public static final String ERROR_NO_CONSENT_TYPE = "Consent type is not available";
        public static final String ERROR_NO_APP_DATA = "Application data is not available";
        public static final String ERROR_SERVER_ERROR = "Internal server error";
        public static final String ERROR_NO_DATA_IN_SESSION_CACHE = "Data unavailable in session cache corresponding" +
                " to the key provided";
        public static final String ERROR_INVALID_VALUE_FOR_AUTHORIZE_PARAM = "\"authorize\" parameter is not defined " +
                        "properly or invalid";
        public static final String PRESERVE_CONSENT = FinancialServicesConfigParser.getInstance().getPreserveConsent();
        public static final boolean STORE_CONSENT = Boolean.parseBoolean(PRESERVE_CONSENT);

        public static final String AUTHORIZATION = "authorization";
        public static final String CUSTOMER_CARE_OFFICER_SCOPE = "consents:read_all";
}
