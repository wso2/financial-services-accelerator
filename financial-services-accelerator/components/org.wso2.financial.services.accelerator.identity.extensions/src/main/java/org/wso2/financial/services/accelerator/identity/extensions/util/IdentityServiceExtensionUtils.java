/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.identity.extensions.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.FSRequestObject;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.ValidationResponse;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;

import java.util.UUID;

/**
 * Service extension utility class for Identity Extensions.
 */
public class IdentityServiceExtensionUtils {

    private static final Log log = LogFactory.getLog(IdentityServiceExtensionUtils.class);
    private static final IdentityExtensionsDataHolder identityExtensionsDataHolder =
            IdentityExtensionsDataHolder.getInstance();

    /**
     * Validates the action status of the external service response.
     *
     * @param response External service response
     * @throws IdentityOAuth2Exception
     */
    public static void serviceExtensionActionStatusValidation(ExternalServiceResponse response)
            throws IdentityOAuth2Exception {

        if (!StatusEnum.SUCCESS.equals(response.getStatus())) {
            if (response.getData() == null) {
                log.error("Unable to locate \"data\" in the response payload");
                throw new IdentityOAuth2Exception(FinancialServicesConstants.DEFAULT_ERROR_MESSAGE,
                        FinancialServicesConstants.DEFAULT_ERROR_DESCRIPTION);
            }

            String errMsg = response.getData().path(FinancialServicesConstants.ERROR_MESSAGE)
                    .asText(FinancialServicesConstants.DEFAULT_ERROR_MESSAGE);
            String errDesc = response.getData().path(FinancialServicesConstants.ERROR_DESCRIPTION)
                    .asText(FinancialServicesConstants.DEFAULT_ERROR_DESCRIPTION);
            throw new IdentityOAuth2Exception(errMsg, errDesc);
        }
    }

    /**
     * Validates the given FSRequestObject using a service extension.
     *
     * @param fsRequestObject The request object to be validated.
     * @return A ValidationResponse indicating whether the request is valid.
     */
    public static ValidationResponse validateRequestObjectWithServiceExtension(FSRequestObject fsRequestObject)
            throws FinancialServicesException {

        // Construct the payload
        org.json.JSONObject data = new org.json.JSONObject();
        data.put(FinancialServicesConstants.REQUEST_OBJECT, fsRequestObject.getClaimsSet().toJSONObject());

        ExternalServiceRequest externalServiceRequest = new ExternalServiceRequest(UUID.randomUUID().toString(), data);

        // Invoke external service
        ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest,
                ServiceExtensionTypeEnum.VALIDATE_AUTHORIZATION_REQUEST);

        try {
            serviceExtensionActionStatusValidation(response);
            return new ValidationResponse(true);
        } catch (IdentityOAuth2Exception e) {
            String errorDescription = e.getMessage();
            log.error(errorDescription.replaceAll("[\r\n]+", " "));
            return new ValidationResponse(false, errorDescription);
        }
    }

    /**
     * Method to decide if a refresh token should be issued with service extension.
     *
     * @param oAuthTokenReqMessageContext OAuth token request message context
     * @return true if refresh token should be issued, false otherwise
     * @throws FinancialServicesException
     * @throws IdentityOAuth2Exception
     */
    public static boolean issueRefreshTokenWithServiceExtension(OAuthTokenReqMessageContext oAuthTokenReqMessageContext)
            throws FinancialServicesException, IdentityOAuth2Exception {

        String consentId = IdentityCommonUtils.getConsentIdFromScopesArray(oAuthTokenReqMessageContext.getScope());
        ConsentResource consentResource = identityExtensionsDataHolder.getConsentCoreService()
                .getConsent(consentId, false);

        // Construct the payload
        JSONObject data = new JSONObject();
        data.put(IdentityCommonConstants.GRANT_TYPE, oAuthTokenReqMessageContext.getOauth2AccessTokenReqDTO()
                .getGrantType());
        data.put(IdentityCommonConstants.CONSENT_CREATED_TIME, consentResource.getCreatedTime());
        data.put(IdentityCommonConstants.CONSENT_VALIDITY_PERIOD, consentResource.getValidityPeriod());
        data.put(IdentityCommonConstants.DEFAULT_REFRESH_TOKEN_VALIDITY_PERIOD, oAuthTokenReqMessageContext
                .getRefreshTokenvalidityPeriod());

        ExternalServiceRequest externalServiceRequest = new ExternalServiceRequest(UUID.randomUUID().toString(), data);

        // Invoke external service
        ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest,
                ServiceExtensionTypeEnum.ISSUE_REFRESH_TOKEN);

        serviceExtensionActionStatusValidation(response);

        JsonNode responseData = response.getData();
        if (responseData == null || !responseData.has(IdentityCommonConstants.ISSUE_REFRESH_TOKEN)) {
            throw new IdentityOAuth2Exception("Missing issueRefreshToken in response payload.");
        }

        return responseData.path(IdentityCommonConstants.ISSUE_REFRESH_TOKEN).asBoolean();
    }

}
