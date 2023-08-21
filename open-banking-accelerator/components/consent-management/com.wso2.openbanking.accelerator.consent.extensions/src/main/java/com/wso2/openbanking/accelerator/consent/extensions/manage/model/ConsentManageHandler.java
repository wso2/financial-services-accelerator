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

package com.wso2.openbanking.accelerator.consent.extensions.manage.model;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;

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
    public void handleGet(ConsentManageData consentManageData) throws ConsentException;

    /**
     * Function to handle POST requests received to the consent manage endpoint.
     *
     * @param consentManageData Object containing data regarding the request
     * @throws ConsentException Error object with data required for the error response
     */
    public void handlePost(ConsentManageData consentManageData) throws ConsentException;

    /**
     * Function to handle DELETE requests received to the consent manage endpoint.
     *
     * @param consentManageData Object containing data regarding the request
     * @throws ConsentException Error object with data required for the error response
     */
    public void handleDelete(ConsentManageData consentManageData) throws ConsentException;

    /**
     * Function to handle PUT requests received to the consent manage endpoint.
     *
     * @param consentManageData Object containing data regarding the request
     * @throws ConsentException Error object with data required for the error response
     */
    public void handlePut(ConsentManageData consentManageData) throws ConsentException;

    /**
     * Function to handle PATCH requests received to the consent manage endpoint.
     *
     * @param consentManageData Object containing data regarding the request
     * @throws ConsentException Error object with data required for the error response
     */
    public void handlePatch(ConsentManageData consentManageData) throws ConsentException;

    /**
     * Function to handle file upload POST requests received to the consent manage endpoint.
     * Added as a default method to overcome the issues of existing customers since this was added as an update.
     *
     * @param consentManageData Object containing data regarding the request
     * @throws ConsentException Error object with data required for the error response
     */
    default void handleFileUploadPost(ConsentManageData consentManageData) throws ConsentException {

        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, "File upload is not supported");
    }

    /**
     * Function to handle file GET requests received to the consent manage endpoint.
     * Added as a default method to overcome the issues of existing customers since this was added as an update.
     *
     * @param consentManageData Object containing data regarding the request
     * @throws ConsentException Error object with data required for the error response
     */
    default void handleFileGet(ConsentManageData consentManageData) throws ConsentException {

        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, "File retrieval is not supported");
    }

}
