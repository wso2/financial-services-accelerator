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

package org.wso2.financial.services.accelerator.identity.extensions.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.cache.JwtJtiCache;
import org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.cache.JwtJtiCacheKey;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;

/**
 * Common utility class for Identity Extensions.
 */
public class IdentityCommonUtils {

    private static final Log log = LogFactory.getLog(IdentityCommonUtils.class);
    private static final IdentityExtensionsDataHolder identityExtensionsDataHolder =
            IdentityExtensionsDataHolder.getInstance();

    /**
     * Remove the internal scopes from the scopes array.
     *
     * @param scopes Authorized scopes of the token
     * @return scopes array after removing the internal scopes
     */
    public static String[] removeInternalScopes(String[] scopes) {

        String consentIdClaim = getConfiguredConsentIdClaimName();

        if (scopes != null && scopes.length > 0) {
            List<String> scopesList = new LinkedList<>(Arrays.asList(scopes));
            scopesList.removeIf(s -> s.startsWith(consentIdClaim));
            scopesList.removeIf(s -> s.startsWith(IdentityCommonConstants.FS_PREFIX));
            scopesList.removeIf(s -> s.startsWith(IdentityCommonConstants.TIME_PREFIX));
            scopesList.removeIf(s -> s.startsWith(IdentityCommonConstants.CERT_PREFIX));
            return scopesList.toArray(new String[scopesList.size()]);
        }
        return scopes;
    }

    /**
     * Util method to generate SP meta data using service provider.
     *
     * @param serviceProvider The service provider
     * @return SP meta data as a Map
     */
    public static Map<String, Object> getSpMetaData(ServiceProvider serviceProvider) {

        Map<String, String> originalData = Arrays.stream(serviceProvider.getSpProperties())
                .filter(serviceProviderProperty -> StringUtils.isNotEmpty(serviceProviderProperty.getValue()))
                .collect(Collectors.toMap(ServiceProviderProperty::getName, ServiceProviderProperty::getValue));

        Map<String, Object> spMetaDataMap = new HashMap<>();

        for (Map.Entry<String, String> data : originalData.entrySet()) {

            if (data.getValue().contains(IdentityCommonConstants.ARRAY_ELEMENT_SEPARATOR)) {
                ArrayList<String> dataList = new ArrayList<>(Arrays.asList(data.getValue()
                        .split(IdentityCommonConstants.ARRAY_ELEMENT_SEPARATOR)));
                spMetaDataMap.put(data.getKey(), dataList);
            } else {
                spMetaDataMap.put(data.getKey(), data.getValue());
            }
        }
        return spMetaDataMap;
    }

    /**
     * Check whether the given jti value is replayed.
     *
     * @param jtiValue - jti value
     * @return true if the JTI is replayed, false otherwise
     */
    public static boolean isJTIReplayed(String jtiValue) {

        // Validate JTI. Continue if jti is not present in cache
        if (getJtiFromCache(jtiValue) != null) {
            return true;
        }

        // Add jti value to cache
        JwtJtiCacheKey jtiCacheKey = JwtJtiCacheKey.of(jtiValue);
        JwtJtiCache.getInstance().addToCache(jtiCacheKey, jtiValue);
        return false;
    }

    /**
     * Try to retrieve the given jti value from cache.
     *
     * @param jtiValue - jti value
     * @return the cached JTI value or null if not found
     */
    public static String getJtiFromCache(String jtiValue) {

        JwtJtiCacheKey cacheKey = JwtJtiCacheKey.of(jtiValue);
        return JwtJtiCache.getInstance().getFromCache(cacheKey);
    }

    /**
     * Update the approved scopes with the consent ID scope for internal purposes.
     * 
     * @param oAuthAuthzReqMessageContext OAuth authorization request message context
     * @return Updated scopes array with consent ID scope
     * @throws ConsentManagementException 
     * @throws JsonProcessingException
     */
    public static String[] updateApprovedScopes(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) 
            throws ConsentManagementException, JsonProcessingException {

        if (oAuthAuthzReqMessageContext != null && oAuthAuthzReqMessageContext.getAuthorizationReqDTO() != null) {

            String[] scopes = oAuthAuthzReqMessageContext.getApprovedScope();
            if (scopes != null && !Arrays.asList(scopes).contains("api_store")) {
                String consentId = IdentityCommonUtils.getConsentIdFromAuthzRequestContext(oAuthAuthzReqMessageContext);
                if (consentId.isEmpty()) {
                    log.warn("Consent-ID retrieved from request is empty");
                    return scopes;
                }

                String consentIdClaim = getConfiguredConsentIdClaimName();
                String consentScope = consentIdClaim + consentId;
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
     * Retrieves the consent ID from the authorization request context.
     * The method determines the source of the consent ID based on the configuration
     * and extracts it accordingly. It supports multiple sources such as the common
     * authentication ID, essential claims, or request parameters.
     *
     * @param oauthAuthzMsgCtx The OAuth authorization message context containing the request details.
     * @return The extracted consent ID, or null if it cannot be determined.
     * @throws JsonProcessingException
     * @throws ConsentManagementException
     */
    public static String getConsentIdFromAuthzRequestContext(OAuthAuthzReqMessageContext oauthAuthzMsgCtx)
            throws JsonProcessingException, ConsentManagementException {

        boolean isPreInitiatedConsent = Boolean.parseBoolean((String) identityExtensionsDataHolder.getConfigurationMap()
                .get(FinancialServicesConstants.IS_PRE_INITIATED_CONSENT));

        if (!isPreInitiatedConsent) {
            String commonAuthId = getCommonAuthIdFromCookies(oauthAuthzMsgCtx.getAuthorizationReqDTO().getCookie());
            return getConsentIdFromCommonAuthId(commonAuthId);
        }

        String authFlowConsentIdSource = (String) identityExtensionsDataHolder.getConfigurationMap()
                .get(FinancialServicesConstants.AUTH_FLOW_CONSENT_ID_SOURCE);
        if (FinancialServicesConstants.REQUEST_OBJECT.equals(authFlowConsentIdSource)) {
            return FinancialServicesUtils
                    .getConsentIdFromEssentialClaims(oauthAuthzMsgCtx.getAuthorizationReqDTO().getEssentialClaims());
        }

        if (FinancialServicesConstants.REQUEST_PARAM.equals(authFlowConsentIdSource)) {
            return getConsentIdFromRequestParam(oauthAuthzMsgCtx.getAuthorizationReqDTO());
        }

        return null;
    }

    /**
     * Get commonAuthId cookie from cookies array.
     *
     * @param cookies cookies array
     * @return commonAuthId cookie
     */
    @SuppressFBWarnings("COOKIE_USAGE")
    // Suppressed content - Cookie handling to extract commonAuthId from cookies array
    // Suppression reason - False Positive : The cookie is read-only and not modified or created.
    // It is securely handled by the identity server framework, which enforces appropriate
    // security flags (e.g., Secure, HttpOnly) during cookie creation.
    // Suppressed warning count - 1
    static String getCommonAuthIdFromCookies(Cookie[] cookies) {

        String commonAuthId = StringUtils.EMPTY;

        if (cookies == null || cookies.length == 0) {
            return commonAuthId;
        }

        ArrayList<Cookie> cookieList = new ArrayList<>(Arrays.asList(cookies));
        for (Cookie cookie : cookieList) {
            if (IdentityCommonConstants.COMMON_AUTH_ID.equals(cookie.getName())) {
                commonAuthId = cookie.getValue();
                break;
            }
        }
        return commonAuthId;
    }

    private static String getConsentIdFromCommonAuthId(String commonAuthId) throws ConsentManagementException {

        List<String> consentIds = identityExtensionsDataHolder.getConsentCoreService()
                .getConsentIdByConsentAttributeNameAndValue(IdentityCommonConstants.COMMON_AUTH_ID, commonAuthId);

        if (!consentIds.isEmpty()) {
            return consentIds.get(0);
        }

        return null;
    }

    private static String getConsentIdFromRequestParam(OAuth2AuthorizeReqDTO oAuth2AuthorizeReqDTO) {

        String key = (String) identityExtensionsDataHolder.getConfigurationMap()
                .get(FinancialServicesConstants.CONSENT_ID_EXTRACTION_KEY);

        // TODO: need to support other request parameters based on requirements
        switch (key) {
            case FinancialServicesConstants.SCOPE:
                return FinancialServicesUtils.getConsentIdFromScopesRequestParam(oAuth2AuthorizeReqDTO.getScopes());
            default:
                return null;
        }
    }

    /**
     * Add consent ID to the token response.
     * @param oAuth2AccessTokenRespDTO OAuth2 access token response DTO
     */
    public static void addConsentIdToTokenResponse(OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO) {

        boolean shouldAddConsentIdClaimToTokenResponse = Boolean
                .parseBoolean((String) identityExtensionsDataHolder.getConfigurationMap()
                        .get(FinancialServicesConstants.APPEND_CONSENT_ID_TO_ACCESS_TOKEN));
        if (shouldAddConsentIdClaimToTokenResponse) {
            String consentId = getConsentIdFromScopesArray(oAuth2AccessTokenRespDTO.getAuthorizedScopes()
                    .split(FinancialServicesConstants.SPACE_SEPARATOR));
            String consentIdClaimName = getConfiguredConsentIdClaimName();
            oAuth2AccessTokenRespDTO.addParameter(consentIdClaimName, consentId);
        }
    }

    /**
     * Get consent id from the scopes.
     *
     * @param scopes Scopes
     * @return Consent ID
     */
    public static String getConsentIdFromScopesArray(String[] scopes) {

        String consentIdClaim = getConfiguredConsentIdClaimName();

        for (String scope : scopes) {
            if (scope.startsWith(consentIdClaim)) {
                return scope.substring(consentIdClaim.length());
            }
        }

        return null;
    }

    /**
     * Get the consent id claim name from config.
     * @return consent ID custom claim name
     */
    public static String getConfiguredConsentIdClaimName() {

        return (String) identityExtensionsDataHolder.getConfigurationMap()
                .get(FinancialServicesConstants.CONSENT_ID_CLAIM_NAME);
    }

}
