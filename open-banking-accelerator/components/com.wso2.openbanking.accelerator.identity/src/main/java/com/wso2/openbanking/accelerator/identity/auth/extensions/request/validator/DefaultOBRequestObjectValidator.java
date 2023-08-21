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

package com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator;

import com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.models.OBRequestObject;
import com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.models.ValidationResponse;
import net.minidev.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.RequestObjectException;

import java.util.Map;

/**
 * The extension class for enforcing OB Request Object Validations. For Tool kits to extend.
 */
public class DefaultOBRequestObjectValidator extends OBRequestObjectValidator {

    private static final String CLAIMS = "claims";
    private static final String[] CLAIM_FIELDS = new String[]{"id_token", "userinfo"};
    private static final String OPENBANKING_INTENT_ID = "openbanking_intent_id";
    private static final String VALUE = "value";
    private static final String CLIENT_ID = "client_id";
    private static final String SCOPE = "scope";

    private static final Log log = LogFactory.getLog(DefaultOBRequestObjectValidator.class);

    public DefaultOBRequestObjectValidator() {
    }

    /**
     * Extension point for tool kits. Perform validation and return the error message if any, else null.
     *
     * @param obRequestObject request object
     * @param dataMap         provides scope related data needed for validation from service provider meta data
     * @return the response object with error message.
     */
    @Override
    public ValidationResponse validateOBConstraints(OBRequestObject obRequestObject, Map<String, Object> dataMap) {

        ValidationResponse superValidationResponse = super.validateOBConstraints(obRequestObject, dataMap);

        if (superValidationResponse.isValid()) {
            try {
                if (isClientIdAndScopePresent(obRequestObject)) {
                    // consent id and client id is matching
                    return new ValidationResponse(true);
                }
                return new ValidationResponse(false,
                        "Consent Id in the request does not match with the client Id");
            } catch (RequestObjectException e) {
                return new ValidationResponse(false, e.getMessage());
            }
        } else {
            return superValidationResponse;
        }
    }

    /**
     * Extract clientId and scope from ob request object and check whether it's present.
     *
     * @param obRequestObject
     * @return result received from validateConsentIdWithClientId method
     * @throws RequestObjectException if error occurred while validating
     */
    private boolean isClientIdAndScopePresent(OBRequestObject obRequestObject) throws RequestObjectException {
        JSONObject jsonObject = obRequestObject.getSignedJWT().getPayload().toJSONObject();
        final String clientId = jsonObject.getAsString(CLIENT_ID);
        String scope = jsonObject.getAsString(SCOPE);
        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(scope)) {
            log.error("Client id or scope cannot be empty");
            throw new RequestObjectException("Client id or scope cannot be empty");
        }
        return true;
    }
}
