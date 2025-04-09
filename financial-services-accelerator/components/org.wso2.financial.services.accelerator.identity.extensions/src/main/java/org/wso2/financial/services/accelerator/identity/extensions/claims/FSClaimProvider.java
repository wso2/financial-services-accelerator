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

package org.wso2.financial.services.accelerator.identity.extensions.claims;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.openidconnect.ClaimProvider;
import org.wso2.financial.services.accelerator.common.constant.ErrorConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.OperationEnum;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * FS specific claim provider.
 */
public class FSClaimProvider implements ClaimProvider {

    private static final Log log = LogFactory.getLog(FSClaimProvider.class);
    private static ClaimProvider claimProvider;

    @Override
    public Map<String, Object> getAdditionalClaims(OAuthAuthzReqMessageContext authAuthzReqMessageContext,
                                                   OAuth2AuthorizeRespDTO authorizeRespDTO)
            throws IdentityOAuth2Exception {

        if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum
                .PRE_ID_TOKEN_GENERATION)) {
            // Perform FS customized behaviour with service extension
            try {
                return getAdditionalIdTokenClaimsWithServiceExtension(
                        authAuthzReqMessageContext, authorizeRespDTO);
            } catch (FinancialServicesException e) {
                log.error(ErrorConstants.EXTERNAL_SERVICE_DEFAULT_ERROR, e);
                throw new IdentityOAuth2Exception(ErrorConstants.EXTERNAL_SERVICE_DEFAULT_ERROR);
            } catch (JsonProcessingException e) {
                log.error(ErrorConstants.JSON_PROCESSING_ERROR, e);
                throw new IdentityOAuth2Exception(ErrorConstants.JSON_PROCESSING_ERROR);
            }
        } else if (getClaimProvider() != null) {
            // Perform FS customized behaviour
            return getClaimProvider().getAdditionalClaims(authAuthzReqMessageContext, authorizeRespDTO);
        } else {
            // Perform FS default behaviour
            return getDefaultAdditionalIdTokenClaims(authAuthzReqMessageContext, authorizeRespDTO);
        }
    }

    @Override
    public Map<String, Object> getAdditionalClaims(OAuthTokenReqMessageContext tokenReqMessageContext,
                                                   OAuth2AccessTokenRespDTO tokenRespDTO)
            throws IdentityOAuth2Exception {

        Map<String, Object> additionalClaims;
        if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum
                .PRE_ID_TOKEN_GENERATION)) {
            // Perform FS customized behaviour with service extension
            try {
                additionalClaims = getAdditionalIdTokenClaimsWithServiceExtension(
                        tokenReqMessageContext, tokenRespDTO);
            } catch (FinancialServicesException e) {
                log.error(ErrorConstants.EXTERNAL_SERVICE_DEFAULT_ERROR, e);
                throw new IdentityOAuth2Exception(ErrorConstants.EXTERNAL_SERVICE_DEFAULT_ERROR);
            }
        } else if (getClaimProvider() != null) {
            // Perform FS customized behaviour
            additionalClaims = getClaimProvider().getAdditionalClaims(tokenReqMessageContext, tokenRespDTO);
        } else {
            // Perform FS default behaviour
            additionalClaims = getDefaultAdditionalIdTokenClaims(tokenReqMessageContext, tokenRespDTO);
        }

        tokenRespDTO.setAuthorizedScopes(updateScopeInTokenResponseBody(tokenRespDTO.getAuthorizedScopes()));
        return additionalClaims;
    }

    /**
     * Update the scope attribute in the token response body by removing internal scopes.
     *
     * @param scopes
     * @return updated scopes
     */
    private String updateScopeInTokenResponseBody(String scopes) {

        String[] updatedScopesArray = IdentityCommonUtils.removeInternalScopes(scopes.split(" "));

        StringBuilder scopesString = new StringBuilder();
        for (String scope : updatedScopesArray) {
            scopesString.append(scope).append(" ");
        }

        return scopesString.toString().trim();
    }

    public static void setClaimProvider(ClaimProvider claimProvider) {

        FSClaimProvider.claimProvider = claimProvider;
    }

    public static ClaimProvider getClaimProvider() {

        return claimProvider;
    }

    private Map<String, Object> getDefaultAdditionalIdTokenClaims(
            OAuthAuthzReqMessageContext authAuthzReqMessageContext, OAuth2AuthorizeRespDTO authorizeRespDTO)
            throws IdentityOAuth2Exception {

        // Prior to FAPI support in IS, "s_hash" claim was added and "at_hash" claim was removed
        return new HashMap<>();
    }

    private Map<String, Object> getDefaultAdditionalIdTokenClaims(
            OAuthTokenReqMessageContext tokenReqMessageContext, OAuth2AccessTokenRespDTO tokenRespDTO) {

        return new HashMap<>();
    }

    private Map<String, Object> getAdditionalIdTokenClaimsWithServiceExtension(
            OAuthAuthzReqMessageContext authAuthzReqMessageContext, OAuth2AuthorizeRespDTO authorizeRespDTO)
            throws IdentityOAuth2Exception, FinancialServicesException, JsonProcessingException {

        // Construct the payload
        JSONObject data = new JSONObject();
        data.put(IdentityCommonConstants.USER_ID, authAuthzReqMessageContext.getAuthorizationReqDTO()
                .getUser().getUserName());
        data.put(IdentityCommonConstants.CONSENT_ID, IdentityCommonUtils.getConsentId(authAuthzReqMessageContext));

        ExternalServiceRequest externalServiceRequest = new ExternalServiceRequest(
                UUID.randomUUID().toString(), data, OperationEnum.ADDITIONAL_ID_TOKEN_CLAIMS_FOR_AUTHZ_RESPONSE);

        // Invoke external service
        ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest,
                ServiceExtensionTypeEnum.PRE_ID_TOKEN_GENERATION);

        return processResponseAndGetClaims(response);
    }

    private Map<String, Object> getAdditionalIdTokenClaimsWithServiceExtension(
            OAuthTokenReqMessageContext tokenReqMessageContext, OAuth2AccessTokenRespDTO tokenRespDTO)
            throws IdentityOAuth2Exception, FinancialServicesException {

        // Construct the payload
        JSONObject data = new JSONObject();
        data.put(IdentityCommonConstants.USER_ID, tokenReqMessageContext.getAuthorizedUser().getUserName());
        data.put(IdentityCommonConstants.CONSENT_ID, IdentityCommonUtils
                .getConsentId(tokenReqMessageContext.getScope()));

        ExternalServiceRequest externalServiceRequest = new ExternalServiceRequest(
                UUID.randomUUID().toString(), data, OperationEnum.ADDITIONAL_ID_TOKEN_CLAIMS_FOR_TOKEN_RESPONSE);

        // Invoke external service
        ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest,
                ServiceExtensionTypeEnum.PRE_ID_TOKEN_GENERATION);

        return processResponseAndGetClaims(response);
    }

    private Map<String, Object> processResponseAndGetClaims(ExternalServiceResponse response)
            throws IdentityOAuth2Exception {

        IdentityCommonUtils.serviceExtensionActionStatusValidation(response);

        JsonNode responseData = response.getData();
        if (responseData == null || !responseData.has("claims")) {
            throw new IdentityOAuth2Exception("Missing claims in response payload.");
        }

        Map<String, Object> additionalClaims = new HashMap<>();
        for (JsonNode claimNode : responseData.get("claims")) {
            if (!claimNode.hasNonNull("key") || !claimNode.hasNonNull("value")) {
                continue;
            }

            String key = claimNode.get("key").asText();
            Object value = claimNode.get("value").asText();

            // Add only if key is not empty
            if (!key.isEmpty()) {
                additionalClaims.put(key, value);
            }
        }

        return additionalClaims;
    }

}
