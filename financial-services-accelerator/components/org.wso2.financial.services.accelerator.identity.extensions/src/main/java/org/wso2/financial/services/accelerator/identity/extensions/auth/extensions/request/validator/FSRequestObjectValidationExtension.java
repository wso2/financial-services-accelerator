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

package org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;
import org.wso2.carbon.identity.openidconnect.RequestObjectValidatorImpl;
import org.wso2.carbon.identity.openidconnect.model.RequestObject;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.FSRequestObject;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.ValidationResponse;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The extension of RequestObjectValidatorImpl to enforce Open Banking specific validations of the
 * request object.
 */
public class FSRequestObjectValidationExtension extends RequestObjectValidatorImpl {

    private static final Log log = LogFactory.getLog(FSRequestObjectValidationExtension.class);
    // Get extension impl
    static FSRequestObjectValidator obDefaultRequestObjectValidator =
            IdentityExtensionsDataHolder.getInstance().getObRequestObjectValidator();

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

        try {
            if (isRegulatory(oAuth2Parameters)) {

                FSRequestObject fsRequestObject = new FSRequestObject(initialRequestObject);

                Map<String, Object> dataMap = new HashMap<>();
                final String allowedScopes = getAllowedScopes(oAuth2Parameters);
                if (StringUtils.isNotBlank(allowedScopes)) {
                    dataMap.put(IdentityCommonConstants.SCOPE, Arrays.asList(allowedScopes.split(" ")));
                }
                // perform BFSI customized validations
                ValidationResponse validationResponse = obDefaultRequestObjectValidator
                        .validateFSConstraints(fsRequestObject, dataMap);

                if (!validationResponse.isValid()) {
                    log.error(String.format("Request object validation failed: %s",
                            validationResponse.getViolationMessage().replaceAll("[\r\n]+", " ")));
                    // Exception will be caught and converted to auth error by IS at endpoint.
                    throw new RequestObjectException(RequestObjectException.ERROR_CODE_INVALID_REQUEST,
                            validationResponse.getViolationMessage());
                }
            }
            return validateIAMConstraints(initialRequestObject, oAuth2Parameters);

        } catch (RequestObjectException e) {
            log.error("Error while retrieving regulatory property from sp metadata", e);
            throw new RequestObjectException(RequestObjectException.ERROR_CODE_INVALID_REQUEST, e.getErrorMessage());
        }
    }

    /**
     * Validate IAM related logic.
     * @param requestObject        request object
     * @param oAuth2Parameters     oAuth2Parameters
     * @return boolean            true if valid request object
     * @throws RequestObjectException - RequestObjectException
     */
    @Generated(message = "super methods cannot be mocked")
    boolean validateIAMConstraints(RequestObject requestObject,
                                   OAuth2Parameters oAuth2Parameters) throws RequestObjectException {

        return super.validateRequestObject(requestObject, oAuth2Parameters);
    }


    /**
     * Called by validateRequestObject.
     *
     * @param requestObject      request object
     * @param oAuth2Parameters   oAuth2Parameters
     * @return                true if valid audience
     */
    @Generated(message = "Empty method")
    @Override
    protected boolean isValidAudience(RequestObject requestObject, OAuth2Parameters oAuth2Parameters) {

        // converted to validation layer
        return true;
    }

    /**
     * Called by validateRequestObject.
     *
     * @param oAuth2Parameters
     * @return
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    protected String getAllowedScopes(OAuth2Parameters oAuth2Parameters) throws RequestObjectException {

        try {
             String scopesFromSP = IdentityCommonUtils.getAppPropertyFromSPMetaData(oAuth2Parameters.getClientId(),
                     IdentityCommonConstants.SCOPE);
             if (StringUtils.isNotBlank(scopesFromSP)) {
                 return scopesFromSP;
             } else {
                    return "accounts payments fundsconfirmations";
             }
        } catch (FinancialServicesException e) {
            log.error("Error while retrieving scopes property from sp metadata", e);
            throw new RequestObjectException(e.getMessage(), e);
        }
    }

    /**
     * Get regulatory property from sp metadata.
     *
     * @param oAuth2Parameters oAuthParameters
     * @return
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    protected boolean isRegulatory(OAuth2Parameters oAuth2Parameters) throws RequestObjectException {

        return FinancialServicesUtils.isRegulatoryApp(oAuth2Parameters.getClientId());
    }
}
