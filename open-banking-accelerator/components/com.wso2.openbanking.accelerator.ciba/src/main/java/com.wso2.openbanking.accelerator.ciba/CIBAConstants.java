/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.accelerator.ciba;

/**
 * CIBA Test Constants class.
 */
public class CIBAConstants {

    public static final String INVALID_REQUEST = "invalid_request";
    public static final String INTENT_CLAIM = "openbanking_intent_id";
    public static final String VALUE_TAG = "value";
    public static final String CONSENT_ID_PREFIX = "OB_CONSENT_ID_";

    //Error Messages
    public static final String PARSE_ERROR_MESSAGE =
            "Request object invalid: Unable to parse the request object as json";
    public static final String EMPTY_CONTENT_ERROR = "Request object invalid: Empty value for intent";
    public static final String MESSAGE_CONTEXT_EMPTY_ERROR = "OAuth Token Request Message Context is empty";
    public static final String SCOPE_ADDING_ERROR = "Error while adding consent ID to scopes";
}
