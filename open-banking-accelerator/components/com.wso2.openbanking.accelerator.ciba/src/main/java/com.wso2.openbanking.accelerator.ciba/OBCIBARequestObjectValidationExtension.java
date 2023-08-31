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

import com.wso2.openbanking.accelerator.common.util.Generated;
import net.minidev.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;
import org.wso2.carbon.identity.openidconnect.CIBARequestObjectValidatorImpl;
import org.wso2.carbon.identity.openidconnect.model.RequestObject;

import java.text.ParseException;

/**
 * The extension of RequestObjectValidatorImpl to enforce Open Banking specific validations of the
 * request object.
 */
public class OBCIBARequestObjectValidationExtension extends CIBARequestObjectValidatorImpl {

    /**
     * Validations related to clientId, response type, exp, redirect URL, mandatory params,
     * issuer, audience are done. Called after signature validation.
     *
     * @param initialRequestObject request object
     * @param oAuth2Parameters     oAuth2Parameters
     * @throws RequestObjectException - RequestObjectException
     */
    @Override
    public boolean validateRequestObject(RequestObject initialRequestObject, OAuth2Parameters oAuth2Parameters)
            throws RequestObjectException {

        JSONObject intent;
        try {
            intent = initialRequestObject.getClaimsSet().getJSONObjectClaim(CIBAConstants.INTENT_CLAIM);
        } catch (ParseException e) {
            throw new RequestObjectException(CIBAConstants.INVALID_REQUEST,
                    CIBAConstants.PARSE_ERROR_MESSAGE, e);
        }
        if (StringUtils.isEmpty(intent.getAsString(CIBAConstants.VALUE_TAG))) {
            throw new RequestObjectException(CIBAConstants.INVALID_REQUEST,  CIBAConstants.EMPTY_CONTENT_ERROR);
        }

        return validateIAMConstraints(initialRequestObject, oAuth2Parameters);
    }

    /**
     * Validate IAM related logic.
     * @param requestObject
     * @param oAuth2Parameters
     * @return is IAM related constraints are validate
     * @throws RequestObjectException
     */
    @Generated(message = "super methods cannot be mocked")
    boolean validateIAMConstraints(RequestObject requestObject,
                                   OAuth2Parameters oAuth2Parameters) throws RequestObjectException {

        return super.validateRequestObject(requestObject, oAuth2Parameters);
    }

}
