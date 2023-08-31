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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.identity.dcr.exception.DCRValidationException;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationRequest;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationResponse;
import com.wso2.openbanking.accelerator.identity.dcr.model.SoftwareStatementBody;
import com.wso2.openbanking.accelerator.identity.dcr.utils.ValidatorUtils;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Default implementation for dcr registration VALIDATOR class.
 */
public class DefaultRegistrationValidatorImpl extends RegistrationValidator {

    private static final Log log = LogFactory.getLog(DefaultRegistrationValidatorImpl.class);

    @Override
    public void validatePost(RegistrationRequest registrationRequest) throws DCRValidationException {

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

    /**
     * method to set the software statement payload according to the specification.
     *
     * @param registrationRequest model containing the dcr registration details
     * @param decodedSSA          decoded json string of the softwarestatement payload
     */
    public void setSoftwareStatementPayload(RegistrationRequest registrationRequest, String decodedSSA) {

        SoftwareStatementBody softwareStatementPayload = new GsonBuilder().create()
                .fromJson(decodedSSA, SoftwareStatementBody.class);
        registrationRequest.setSoftwareStatementBody(softwareStatementPayload);

    }

    @Override
    @Generated(message = "Excluding from code coverage since it requires to load JWKS URI and invoke service calls")
    public String getRegistrationResponse(Map<String, Object> spMetaData) {

        // Append registration access token and registration client URI to the DCR response if the config is enabled
        if (IdentityCommonUtil.getDCRModifyResponseConfig()) {

            String tlsCert = spMetaData.get(IdentityCommonConstants.TLS_CERT).toString();

            String clientId = spMetaData.get(IdentityCommonConstants.CLIENT_ID).toString();

            if (!spMetaData.containsKey(IdentityCommonConstants.REGISTRATION_ACCESS_TOKEN)) {
                // add the access token to the response
                spMetaData.put(IdentityCommonConstants.REGISTRATION_ACCESS_TOKEN,
                        ValidatorUtils.generateAccessToken(clientId, tlsCert));
            }

            // add the dcr url to the response with the client id appended at the end
            spMetaData.put(IdentityCommonConstants.REGISTRATION_CLIENT_URI,
                    ValidatorUtils.getRegistrationClientURI() + clientId);
        }

        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(spMetaData);
        RegistrationResponse registrationResponse = gson.fromJson(jsonElement, RegistrationResponse.class);
        return gson.toJson(registrationResponse);

    }
}
