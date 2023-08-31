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

package com.wso2.openbanking.accelerator.identity.auth.extensions.response.handler.impl;

import com.wso2.openbanking.accelerator.identity.auth.extensions.response.handler.OBResponseTypeHandler;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.openidconnect.RequestObjectService;
import org.wso2.carbon.identity.openidconnect.model.RequestedClaim;

import java.util.Arrays;
import java.util.List;

/**
 * Default extension implementation. Used to do the accelerator testing. Mimics a UK flow.
 */
public class OBDefaultResponseTypeHandlerImpl implements OBResponseTypeHandler {

    private static final String OPENBANKING_INTENT_ID = "openbanking_intent_id";
    private static final Log log = LogFactory.getLog(OBDefaultResponseTypeHandlerImpl.class);

    /**
     * return the new refresh validity period.
     *
     * @param oAuthAuthzReqMessageContext
     * @return
     */
    public long updateRefreshTokenValidityPeriod(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) {

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
                    log.error("Consent-ID retrieved from request object claims is empty");
                    return scopes;
                }

                String consentIdClaim = IdentityExtensionsDataHolder.getInstance().getConfigurationMap()
                        .get(IdentityCommonConstants.CONSENT_ID_CLAIM_NAME).toString();
                String consentScope = consentIdClaim + consentID;
                if (!Arrays.asList(scopes).contains(consentScope)) {
                    String[] updatedScopes = (String[]) ArrayUtils.addAll(scopes, new String[]{consentScope});
                    if (log.isDebugEnabled()) {
                        log.debug("Updated scopes: " + Arrays.toString(updatedScopes));
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
                    log.error("Failed to retrieve ConsentID from query parameters");
                }
            } else {
                log.error("Failed to retrieve Request Object Service");
            }
        } else {
            log.error("Invalid Session Data Key");
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
            consentID = iterateClaims(requestedClaims);
            if (consentID.isEmpty()) {
                requestedClaims = service.getRequestedClaimsForSessionDataKey(sessionDataKey, true);
                consentID = iterateClaims(requestedClaims);
            }

        } catch (RequestObjectException ex) {
            log.error("Exception occurred", ex);
        }
        return consentID;
    }

    /**
     * Iterate the claims list to identify the consent-ID.
     *
     * @param requestedClaims list of claims
     * @return consent id
     */
    String iterateClaims(List<RequestedClaim> requestedClaims) {

        String consentID = StringUtils.EMPTY;
        for (RequestedClaim claim : requestedClaims) {
            if (log.isDebugEnabled()) {
                log.debug("Claim: " + claim.getName() + ", value: " + claim.getValue());
            }

            if (OPENBANKING_INTENT_ID.equals(claim.getName())) {
                consentID = claim.getValue();
                if (log.isDebugEnabled()) {
                    log.debug("Consent-ID retrieved: " + consentID);
                }
                break;
            }
        }
        return consentID;
    }
}
