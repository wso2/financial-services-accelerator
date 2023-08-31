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
package com.wso2.openbanking.accelerator.identity.dcr.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.wso2.openbanking.accelerator.identity.dcr.exception.DCRValidationException;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationRequest;
import com.wso2.openbanking.accelerator.identity.dcr.model.SoftwareStatementBody;
import com.wso2.openbanking.accelerator.identity.dcr.utils.ValidatorUtils;
import com.wso2.openbanking.accelerator.identity.dcr.validation.DefaultRegistrationValidatorImpl;

import java.util.Map;

/**
 * Extended validator implementation.
 */
public class ExtendedValidatorImpl extends DefaultRegistrationValidatorImpl {

    @Override
    public void validatePost(RegistrationRequest registrationRequest) throws DCRValidationException {

        ExtendedRegistrationRequest request = new ExtendedRegistrationRequest(registrationRequest);
        ValidatorUtils.getValidationViolations(request);
    }

    @Override
    public void validateGet(String clientId) throws DCRValidationException {

    }

    @Override
    public void validateDelete(String clientId) throws DCRValidationException {

    }

    @Override
    public void validateUpdate(RegistrationRequest registrationRequest) throws DCRValidationException {

    }

    @Override
    public String getRegistrationResponse(Map<String, Object> clientMetaData) {

        clientMetaData.put("additional_attribute_1", "111111");
        clientMetaData.put("additional_attribute_2", "222222");

        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(clientMetaData);
        ExtendedRegistrationResponse registrationResponse =
                gson.fromJson(jsonElement, ExtendedRegistrationResponse.class);
        return gson.toJson(registrationResponse);
    }

    @Override
    public void setSoftwareStatementPayload(RegistrationRequest registrationRequest, String decodedSSA) {

        SoftwareStatementBody softwareStatementPayload = new GsonBuilder().create()
                .fromJson(decodedSSA, ExtendedSoftwareStatementBody.class);
        registrationRequest.setSoftwareStatementBody(softwareStatementPayload);

    }
}
