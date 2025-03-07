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

package org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.response.handler.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.openidconnect.RequestObjectService;
import org.wso2.carbon.identity.openidconnect.model.RequestedClaim;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.response.handler.FSResponseTypeHandler;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;

import java.util.Arrays;
import java.util.List;

/**
 * Default extension implementation. Used to do the accelerator testing. Mimics a UK flow.
 */
public class FSDefaultResponseTypeHandlerImpl implements FSResponseTypeHandler {

    private static final String OPENBANKING_INTENT_ID = "openbanking_intent_id";
    private static final Log log = LogFactory.getLog(FSDefaultResponseTypeHandlerImpl.class);

    /**
     * return the new refresh validity period.
     *
     * @param oAuthAuthzReqMessageContext
     * @return
     */
    public long updateRefreshTokenValidityPeriod(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) {

        /**
         * TODO: call external service and set the refresh token validity period
         */

        return oAuthAuthzReqMessageContext.getRefreshTokenvalidityPeriod();
    }

    /**
     * return the new approved scope.
     *
     * @param oAuthAuthzReqMessageContext
     * @return
     */
    public String[] updateApprovedScopes(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) {

        if (oAuthAuthzReqMessageContext != null && oAuthAuthzReqMessageContext.getAuthorizationReqDTO() != null) {

            String[] scopes = oAuthAuthzReqMessageContext.getApprovedScope();
            if (scopes != null && !Arrays.asList(scopes).contains("api_store")) {

                String sessionDataKey = oAuthAuthzReqMessageContext.getAuthorizationReqDTO().getSessionDataKey();
                String consentID = getConsentIDFromSessionData(sessionDataKey);
                if (consentID.isEmpty()) {
                    log.warn("Consent-ID retrieved from request object claims is empty");
                    return scopes;
                }

                String consentIdClaim = IdentityExtensionsDataHolder.getInstance().getConfigurationMap()
                        .get(FinancialServicesConstants.CONSENT_ID_CLAIM_NAME).toString();
                String consentScope = consentIdClaim + consentID;
                if (!Arrays.asList(scopes).contains(consentScope)) {
                    String[] updatedScopes = ArrayUtils.addAll(scopes, consentScope);
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Updated scopes: %s", Arrays.toString(updatedScopes)
                                .replaceAll("[\r\n]", "")));
                    }
                    return updatedScopes;
                }
            }

        } else {
            return new String[0];
        }

        return oAuthAuthzReqMessageContext.getApprovedScope();
    }

    /**
     * Call sessionDataAPI and retrieve request object, decode it and return consentID.
     *
     * @param sessionDataKey sessionDataKeyConsent parameter from authorize request
     * @return consentID
     */
    String getConsentIDFromSessionData(String sessionDataKey) {

        String consentID = StringUtils.EMPTY;
        if (sessionDataKey != null && !sessionDataKey.isEmpty()) {
            RequestObjectService requestObjectService = IdentityExtensionsDataHolder.getInstance()
                    .getRequestObjectService();
            if (requestObjectService != null) {
                consentID = retrieveConsentIDFromReqObjService(requestObjectService, sessionDataKey);
                if (consentID.isEmpty()) {
                    log.warn("Failed to retrieve ConsentID from query parameters");
                }
            } else {
                log.warn("Failed to retrieve Request Object Service");
            }
        } else {
            log.warn("Invalid Session Data Key");
        }
        return consentID;
    }

    /**
     * Call Request Object Service and retrieve consent id.
     *
     * @param service        request object service
     * @param sessionDataKey session data key
     * @return consentID
     */
    String retrieveConsentIDFromReqObjService(RequestObjectService service, String sessionDataKey) {

        String consentID = StringUtils.EMPTY;
        try {
            List<RequestedClaim> requestedClaims = service.getRequestedClaimsForSessionDataKey(sessionDataKey,
                    false);
            consentID = retrieveConsentIDFromClaimList(requestedClaims);
            if (consentID.isEmpty()) {
                requestedClaims = service.getRequestedClaimsForSessionDataKey(sessionDataKey, true);
                consentID = retrieveConsentIDFromClaimList(requestedClaims);
            }

        } catch (RequestObjectException ex) {
            log.warn("Exception occurred", ex);
        }
        return consentID;
    }

    /**
     * Iterate the claims list to identify the consent-ID.
     *
     * @param requestedClaims list of claims
     * @return consent id
     */
    String retrieveConsentIDFromClaimList(List<RequestedClaim> requestedClaims) {

        String consentID = StringUtils.EMPTY;
        for (RequestedClaim claim : requestedClaims) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Claim: %s, value: %s", claim.getName().replaceAll("[\r\n]", ""),
                        claim.getValue().replaceAll("[\r\n]", "")));
            }

            if (OPENBANKING_INTENT_ID.equals(claim.getName())) {
                consentID = claim.getValue();
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Consent-ID retrieved: %s", consentID.replaceAll("[\r\n]", "")));
                }
                break;
            }
        }
        return consentID;
    }
}
