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
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.identity.extensions.cache.IdentityCache;
import org.wso2.financial.services.accelerator.identity.extensions.cache.IdentityCacheKey;
import org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.cache.JwtJtiCache;
import org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.cache.JwtJtiCacheKey;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;

/**
 * Common utility class for Identity Extensions.
 */
public class IdentityCommonUtils {

    private static final Log log = LogFactory.getLog(IdentityCommonUtils.class);
    private static final IdentityExtensionsDataHolder identityExtensionsDataHolder =
            IdentityExtensionsDataHolder.getInstance();
    private static IdentityCache identityCache;

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

        List<String> preInitiatedConsentScopes = getConfiguredScopeList(identityExtensionsDataHolder
                .getConfigurationMap().get(FinancialServicesConstants.PRE_INITIATED_CONSENT_SCOPES));
        List<String> scopeBasedConsentScopes = getConfiguredScopeList(identityExtensionsDataHolder
                .getConfigurationMap().get(FinancialServicesConstants.SCOPE_BASED_CONSENT_SCOPES));

        boolean ifPreInitiatedConsentFlow = FinancialServicesUtils.isPreInitiatedConsentFlow(
                oauthAuthzMsgCtx.getApprovedScope(), preInitiatedConsentScopes, scopeBasedConsentScopes);

        if (!ifPreInitiatedConsentFlow) {
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
     * Get configured scope list from the config object.
     *
     * @param configObj config object
     * @return configured scope list
     */
    private static List<String> getConfiguredScopeList(Object configObj) {

        List<String> configuredScopeList = new ArrayList<>();
        if (configObj instanceof List) {
            configuredScopeList.addAll((List) configObj);
        } else if (configObj instanceof String) {
            configuredScopeList.add((String) configObj);
        }

        return configuredScopeList;
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

    /**
     * Check whether the client ID belongs to a regulatory app.
     *
     * @param clientId client ID
     * @return true if the client ID belongs to a regulatory app
     * @throws FinancialServicesException If an error occurs while checking the client ID
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public static boolean isRegulatoryApp(String clientId) throws FinancialServicesException {

        if (StringUtils.isNotEmpty(clientId)) {
            // Skip My account and Console service providers with non opaque clientIds
            if (clientId.equals("CONSOLE") || clientId.equals("MY_ACCOUNT")) {
                return false;
            }

            if (identityCache == null) {
                log.debug("Creating new Identity cache");
                identityCache = new IdentityCache();
            }

            IdentityCacheKey identityCacheKey = IdentityCacheKey.of(clientId
                    .concat("_").concat(FinancialServicesConstants.REGULATORY));
            Object regulatoryProperty = null;

            regulatoryProperty = identityCache.getFromCacheOrRetrieve(identityCacheKey,
                    () -> getAppPropertyFromSPMetaData(clientId, FinancialServicesConstants.REGULATORY));

            if (regulatoryProperty != null) {
                identityCache.addToCache(identityCacheKey, regulatoryProperty);
                return Boolean.parseBoolean(regulatoryProperty.toString());
            } else {
                throw new FinancialServicesException("Unable to retrieve regulatory property from sp metadata");
            }
        } else {
            throw new FinancialServicesException("Client id not found");
        }
    }

    /**
     * Utility method get the application property from SP Meta Data.
     *
     * @param clientId ClientId of the application
     * @param property Property of the application
     * @return the property value from SP metadata
     * @throws FinancialServicesException If an error occurs while retrieving the property
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public static String getAppPropertyFromSPMetaData(String clientId, String property)
            throws FinancialServicesException {

        String spProperty = null;

        if (StringUtils.isNotEmpty(clientId)) {
            Optional<ServiceProvider> serviceProvider;
            try {
                serviceProvider = Optional.ofNullable(IdentityExtensionsDataHolder.getInstance()
                        .getApplicationManagementService().getServiceProviderByClientId(clientId,
                                IdentityApplicationConstants.OAuth2.NAME, getSpTenantDomain(clientId)));
                if (serviceProvider.isPresent()) {
                    spProperty = Arrays.stream(serviceProvider.get().getSpProperties())
                            .collect(Collectors.toMap(ServiceProviderProperty::getName,
                                    ServiceProviderProperty::getValue))
                            .get(property);
                }
            } catch (IdentityApplicationManagementException e) {
                log.error(String.format("Error occurred while retrieving OAuth2 application data for clientId %s",
                        clientId.replaceAll("[\r\n]", "")), e);
                throw new FinancialServicesException("Error occurred while retrieving OAuth2 application data for " +
                        "clientId", e);
            }
        } else {
            log.error("Client id not found");
            throw new FinancialServicesException("Client id not found");
        }

        return spProperty;
    }

    /**
     * Get Tenant Domain String for the client id.
     * @param clientId the client id of the application
     * @return tenant domain of the client
     * @throws FinancialServicesException  if an error occurs while retrieving the tenant domain
     */
    @Generated(message = "Ignoring because OAuth2Util cannot be mocked with no constructors")
    public static String getSpTenantDomain(String clientId) throws FinancialServicesException {

        try {
            return OAuth2Util.getTenantDomainOfOauthApp(clientId);
        } catch (InvalidOAuthClientException | IdentityOAuth2Exception e) {
            throw new FinancialServicesException("Error retrieving service provider tenant domain for client_id: "
                    + clientId, e);
        }
    }

}
