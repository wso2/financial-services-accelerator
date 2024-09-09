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

import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ConsentManageData;

/**
 * Consent manage handler interface.
 */
public interface ConsentManageHandler {

    /**
     * Function to handle GET requests received to the consent manage endpoint.
     *
     * @param consentManageData Object containing data regarding the request
     * @throws ConsentException Error object with data required for the error response
     */
    void handleGet(ConsentManageData consentManageData) throws ConsentException;

    /**
     * Function to handle POST requests received to the consent manage endpoint.
     *
     * @param consentManageData Object containing data regarding the request
     * @throws ConsentException Error object with data required for the error response
     */
    void handlePost(ConsentManageData consentManageData) throws ConsentException;

    /**
     * Function to handle DELETE requests received to the consent manage endpoint.
     *
     * @param consentManageData Object containing data regarding the request
     * @throws ConsentException Error object with data required for the error response
     */
    void handleDelete(ConsentManageData consentManageData) throws ConsentException;

    /**
     * Function to handle PUT requests received to the consent manage endpoint.
     *
     * @param consentManageData Object containing data regarding the request
     * @throws ConsentException Error object with data required for the error response
     */
    void handlePut(ConsentManageData consentManageData) throws ConsentException;

    /**
     * Function to handle PATCH requests received to the consent manage endpoint.
     *
     * @param consentManageData Object containing data regarding the request
     * @throws ConsentException Error object with data required for the error response
     */
    void handlePatch(ConsentManageData consentManageData) throws ConsentException;

    /**
     * Function to handle file upload POST requests received to the consent manage endpoint.
     *
     * @param consentManageData Object containing data regarding the request
     * @throws ConsentException Error object with data required for the error response
     */
    void handleFileUploadPost(ConsentManageData consentManageData) throws ConsentException;

    /**
     * Function to handle file GET requests received to the consent manage endpoint.
     *
     * @param consentManageData Object containing data regarding the request
     * @throws ConsentException Error object with data required for the error response
     */
    void handleFileGet(ConsentManageData consentManageData) throws ConsentException;

    /**
     * Function to get the Consent Manage Validator.
     */
    ConsentManageValidator getConsentManageValidator();
}
