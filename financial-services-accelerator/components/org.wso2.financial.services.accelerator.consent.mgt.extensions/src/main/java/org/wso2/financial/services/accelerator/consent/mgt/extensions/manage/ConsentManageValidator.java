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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.manage;

import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ConsentManageData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ConsentPayloadValidationResult;

/**
 * Consent create validator interface.
 */
public interface ConsentManageValidator {

    /**
     * Method to validate initiation request payload.
     *
     * @param consentManageData   Consent Manage Data Object
     * @param consentType         Consent Type
     * @return ConsentPayloadValidationResult     Validation Result
     */
    ConsentPayloadValidationResult validateRequestPayload(ConsentManageData consentManageData, String consentType);

    /**
     * Method to validate initiation request headers.
     *
     * @param consentManageData   Consent Manage Data Object
     * @return ConsentPayloadValidationResult     Validation Result
     */
    ConsentPayloadValidationResult validateRequestHeaders(ConsentManageData consentManageData);

    /**
     * Method to get the consent type of the request.
     *
     * @param consentManageData  Consent Manage Data Object
     * @return Consent Type
     */
    String getConsentType(ConsentManageData consentManageData) throws ConsentManagementException;
}
