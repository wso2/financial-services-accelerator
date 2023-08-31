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
package com.wso2.openbanking.accelerator.identity.dcr.validation;

import com.wso2.openbanking.accelerator.identity.dcr.exception.DCRValidationException;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationRequest;

import java.util.Map;

/**
 * Abstract class to perform spec specific validation for each crud operation.
 * Implementation class for this should be configured in open-banking.xml
 */
public abstract class RegistrationValidator {

    private static RegistrationValidator registrationValidator;

    public static RegistrationValidator getRegistrationValidator() {

        return registrationValidator;
    }

    public static void setRegistrationValidator(RegistrationValidator registrationValidator) {

        RegistrationValidator.registrationValidator = registrationValidator;
    }

    /**
     * method to set the software statement payload according to the specification.
     *
     * @param registrationRequest model containing the dcr registration details
     * @param decodedSSA          decoded json string of the softwarestatement payload
     */
    public abstract void setSoftwareStatementPayload(RegistrationRequest registrationRequest, String decodedSSA);

    /**
     * validate the request parameters when creating a registration.
     *
     * @param registrationRequest request
     * @throws DCRValidationException if any validation failure occurs
     */
    public abstract void validatePost(RegistrationRequest registrationRequest) throws DCRValidationException;

    /**
     * do any validations before retrieving created registration details.
     *
     * @param clientId client ID of the registered application
     * @throws DCRValidationException if any validation failure occurs
     */
    public abstract void validateGet(String clientId) throws DCRValidationException;

    /**
     * do any validations before deleting a created application.
     *
     * @param clientId client ID of the registered application
     * @throws DCRValidationException if any validation failure occurs
     */
    public abstract void validateDelete(String clientId) throws DCRValidationException;

    /**
     * validate the request parameters when creating a registration.
     *
     * @param registrationRequest request
     * @throws DCRValidationException if any validation failure occurs
     */
    public abstract void validateUpdate(RegistrationRequest registrationRequest) throws DCRValidationException;

    /**
     * method to return the response according to the implemented specification when retrieving registered data.
     *
     * @param clientMetaData object map containing the registered client meta data
     * @return JSON string  containing attributes of client that should be returned
     */
    public abstract String getRegistrationResponse(Map<String, Object> clientMetaData);

}
