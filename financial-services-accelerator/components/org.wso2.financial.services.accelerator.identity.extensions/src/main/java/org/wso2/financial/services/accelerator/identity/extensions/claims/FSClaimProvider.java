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
                return additionalIdTokenClaimsAuthzResponseWithServiceExtension(
                        authAuthzReqMessageContext, authorizeRespDTO);
            } catch (FinancialServicesException e) {
                log.error("Error while invoking external service extension", e);
                throw new IdentityOAuth2Exception("Error while invoking external service extension");
            }
        } else if (getClaimProvider() != null) {
            // Perform FS customized behaviour
            return getClaimProvider().getAdditionalClaims(authAuthzReqMessageContext, authorizeRespDTO);
        } else {
            // Perform FS default behaviour
            return defaultAdditionalIdTokenClaimsAuthzResponse(authAuthzReqMessageContext, authorizeRespDTO);
        }
    }

    @Override
    public Map<String, Object> getAdditionalClaims(OAuthTokenReqMessageContext tokenReqMessageContext,
                                                   OAuth2AccessTokenRespDTO tokenRespDTO)
            throws IdentityOAuth2Exception {

        if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum
                .PRE_ID_TOKEN_GENERATION)) {
            // Perform FS customized behaviour with service extension
            try {
                return additionalIdTokenClaimsTokenResponseWithServiceExtension(
                        tokenReqMessageContext, tokenRespDTO);
            } catch (FinancialServicesException e) {
                log.error("Error while invoking external service extension", e);
                throw new IdentityOAuth2Exception("Error while invoking external service extension");
            }
        } else if (getClaimProvider() != null) {
            // Perform FS customized behaviour
            return getClaimProvider().getAdditionalClaims(tokenReqMessageContext, tokenRespDTO);
        } else {
            // Perform FS default behaviour
            return defaultAdditionalIdTokenClaimsTokenResponse(tokenReqMessageContext, tokenRespDTO);
        }
    }

    public static void setClaimProvider(ClaimProvider claimProvider) {

        FSClaimProvider.claimProvider = claimProvider;
    }

    public static ClaimProvider getClaimProvider() {

        return claimProvider;
    }

    private Map<String, Object> defaultAdditionalIdTokenClaimsAuthzResponse(
            OAuthAuthzReqMessageContext authAuthzReqMessageContext, OAuth2AuthorizeRespDTO authorizeRespDTO)
            throws IdentityOAuth2Exception {

        // Prior to FAPI support in IS, "s_hash" claim was added and "at_hash" claim was removed
        return new HashMap<>();
    }

    private Map<String, Object> defaultAdditionalIdTokenClaimsTokenResponse(
            OAuthTokenReqMessageContext tokenReqMessageContext, OAuth2AccessTokenRespDTO tokenRespDTO) {

        return new HashMap<>();
    }

    private Map<String, Object> additionalIdTokenClaimsAuthzResponseWithServiceExtension(
            OAuthAuthzReqMessageContext authAuthzReqMessageContext, OAuth2AuthorizeRespDTO authorizeRespDTO)
            throws IdentityOAuth2Exception, FinancialServicesException {

        // Construct the payload
        JSONObject data = new JSONObject();
        data.put(IdentityCommonConstants.USER_ID, authAuthzReqMessageContext.getAuthorizationReqDTO()
                .getUser().getUserName());

        // TODO: Retrieve consent ID if required

        ExternalServiceRequest externalServiceRequest = new ExternalServiceRequest(
                UUID.randomUUID().toString(), data, OperationEnum.ADDITIONAL_ID_TOKEN_CLAIMS_FOR_AUTHZ_RESPONSE);

        // Invoke external service
        ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest,
                ServiceExtensionTypeEnum.PRE_ID_TOKEN_GENERATION);

        return processResponseAndGetClaims(response);
    }

    private Map<String, Object> additionalIdTokenClaimsTokenResponseWithServiceExtension(
            OAuthTokenReqMessageContext tokenReqMessageContext, OAuth2AccessTokenRespDTO tokenRespDTO)
            throws IdentityOAuth2Exception, FinancialServicesException {

        // Construct the payload
        JSONObject data = new JSONObject();
        data.put(IdentityCommonConstants.USER_ID, tokenReqMessageContext.getAuthorizedUser().getUserName());

        // TODO: Retrieve consent ID if required

        ExternalServiceRequest externalServiceRequest = new ExternalServiceRequest(
                UUID.randomUUID().toString(), data, OperationEnum.ADDITIONAL_ID_TOKEN_CLAIMS_FOR_TOKEN_RESPONSE);

        // Invoke external service
        ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest,
                ServiceExtensionTypeEnum.PRE_ID_TOKEN_GENERATION);

        return processResponseAndGetClaims(response);
    }

    private Map<String, Object> processResponseAndGetClaims(ExternalServiceResponse response)
            throws IdentityOAuth2Exception {

        Map<String, Object> additionalClaims = new HashMap<>();

        if (response == null) {
            log.error("Null response received from external service.");
            throw new IdentityOAuth2Exception("Null response received from external service.");
        }

        IdentityCommonUtils.serviceExtensionActionStatusValidation(response);

        JsonNode responseData = response.getData();
        if (responseData == null) {
            log.error("Missing payload in response from external service.");
            throw new IdentityOAuth2Exception("Missing payload in response from external service.");
        }

        JsonNode claims = responseData.get("claims");
        if (claims == null) {
            log.error("Missing claims array in response payload.");
            throw new IdentityOAuth2Exception("Missing claims array in response payload.");
        }

        for (Object claimObject : claims) {
            if (claimObject instanceof JSONObject) {
                JSONObject claim = (JSONObject) claimObject;
                String key = claim.optString("key");
                Object value = claim.opt("value");
                if (!key.isEmpty() && value != null) {
                    additionalClaims.put(key, value);
                }
            }
        }

        return additionalClaims;
    }

}
