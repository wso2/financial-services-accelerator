/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
package com.wso2.openbanking.accelerator.consent.extensions.common.idempotency;

/**
 * Constants related to idempotency operations.
 */
public class IdempotencyConstants {

    public static final String CONTENT_TYPE_TAG = "content-type";
    public static final String X_IDEMPOTENCY_KEY = "x-idempotency-key";
    public static final String IDEMPOTENCY_KEY_NAME = "IdempotencyKey";
    public static final String ERROR_PAYLOAD_NOT_SIMILAR = "Payloads are not similar. Hence this is not a valid" +
            " idempotent request";
    public static final String ERROR_AFTER_ALLOWED_TIME = "Request received after the allowed time., Hence this is" +
            " not a valid idempotent request";
    public static final String ERROR_MISMATCHING_CLIENT_ID = "Client ID sent in the request does not match with the" +
            " client ID in the retrieved consent. Hence this is not a valid idempotent request";
    public static final String ERROR_NO_CONSENT_DETAILS = "No consent details found for the consent ID %s, Hence this" +
            " is not a valid idempotent request";
    public static final String JSON_COMPARING_ERROR = "Error occurred while comparing JSON payloads";
    public static final String CONSENT_RETRIEVAL_ERROR = "Error while retrieving detailed consent data";
}
