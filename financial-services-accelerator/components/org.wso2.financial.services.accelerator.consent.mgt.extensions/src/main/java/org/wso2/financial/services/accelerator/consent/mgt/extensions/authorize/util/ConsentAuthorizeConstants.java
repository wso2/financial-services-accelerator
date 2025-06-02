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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util;

/**
 * Constants for the consent authorize module.
 */
public class ConsentAuthorizeConstants {

    public static final String DATE_PARSE_MSG = "Parsed OffsetDateTime: %s, current OffsetDateTime: %s";
    public static final String EXP_DATE_PARSE_ERROR = "Error occurred while parsing the expiration date. ";
    public static final String ACC_CONSENT_RETRIEVAL_ERROR = "Error occurred while retrieving the account initiation" +
            " request details";
    public static final String CONSENT_EXPIRED = "Provided consent is expired";
    public static final String ACCOUNT_ID_NOT_FOUND_ERROR = "Account IDs not available in persist request";
    public static final String ACCOUNT_ID_FORMAT_ERROR = "Account IDs format error in persist request";

    // For payload from populate consent authorize screen and payload forwarded to default consent page
    public static final String TYPE = "type";
    public static final String BASIC_CONSENT_DATA = "basicConsentData";
    public static final String REQUESTED_PERMISSIONS = "requestedPermissions";
    public static final String PERMISSIONS = "permissions";
    public static final String DISPLAY_VALUES = "displayValues";
    public static final String DISPLAY_NAME = "displayName";
    public static final String ACCOUNT_ID = "accountId";
    public static final String SELECTED = "selected";
}
