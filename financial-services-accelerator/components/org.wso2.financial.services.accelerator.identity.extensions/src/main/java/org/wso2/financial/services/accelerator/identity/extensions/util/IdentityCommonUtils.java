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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.identity.openidconnect.RequestObjectService;
import org.wso2.carbon.identity.openidconnect.model.RequestedClaim;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.cache.JwtJtiCache;
import org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.cache.JwtJtiCacheKey;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
     * Remove the internal scopes from the space delimited list of authorized scopes.
     *
     * @param scopes Authorized scopes of the token
     * @return scopes by removing the internal scopes
     */
    public static String[] removeInternalScopes(String[] scopes) {

        String consentIdClaim = identityExtensionsDataHolder.getConfigurationMap()
                .get(FinancialServicesConstants.CONSENT_ID_CLAIM_NAME).toString();

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
     * Method to obtain Hash Value for a given String, default algorithm SHA256withRSA.
     *
     * @param value String value that required to be Hashed
     * @return Hashed String
     * @throws IdentityOAuth2Exception
     */
    public static String getHashValue(String value, String digestAlgorithm) throws IdentityOAuth2Exception {

        if (digestAlgorithm == null) {
            JWSAlgorithm digAlg = OAuth2Util.mapSignatureAlgorithmForJWSAlgorithm(
                    OAuthServerConfiguration.getInstance().getIdTokenSignatureAlgorithm());
            digestAlgorithm = OAuth2Util.mapDigestAlgorithm(digAlg);
        }
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(digestAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IdentityOAuth2Exception("Error creating the hash value. Invalid Digest Algorithm: " +
                    digestAlgorithm);
        }
        //generating hash value
        md.update(value.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        int leftHalfBytes = digest.length / 2;
        byte[] leftmost = new byte[leftHalfBytes];
        System.arraycopy(digest, 0, leftmost, 0, leftHalfBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(leftmost)
                .replace("\n", "").replace("\r", "");
    }

    /**
     * Get the configured certificate header name.
     *
     * @return value of the cert header name configuration
     */
    public static String getMTLSAuthHeader() {

        return Optional.ofNullable(IdentityUtil.getProperty(IdentityCommonConstants.MTLS_AUTH_HEADER))
                .orElse("CONFIG_NOT_FOUND");
    }

    /**
     * Parse the certificate content.
     *
     * @param content the content to be passed
     * @return the parsed certificate
     * @throws FinancialServicesException  if an error occurs while parsing the certificate
     */
    public static X509Certificate parseCertificate(String content) throws FinancialServicesException {

        try {
            if (StringUtils.isNotBlank(content)) {
                // removing illegal base64 characters before decoding
                content = removeIllegalBase64Characters(content);
                byte[] bytes = Base64.getDecoder().decode(content);

                return (java.security.cert.X509Certificate) CertificateFactory.getInstance(IdentityCommonConstants.X509)
                        .generateCertificate(new ByteArrayInputStream(bytes));
            }
            log.error("Certificate passed through the request is empty");
            return null;
        } catch (CertificateException | IllegalArgumentException e) {
            throw new FinancialServicesException("Certificate passed through the request not valid", e);
        }
    }

    /**
     * Remove illegal base64 characters from input string.
     *
     * @param value certificate as a string
     * @return certificate without illegal base64 characters
     */
    private static String removeIllegalBase64Characters(String value) {
        if (value.contains(IdentityCommonConstants.BEGIN_CERT)
                && value.contains(IdentityCommonConstants.END_CERT)) {

            // extracting certificate content
            value = value.substring(value.indexOf(IdentityCommonConstants.BEGIN_CERT)
                            + IdentityCommonConstants.BEGIN_CERT.length(),
                    value.indexOf(IdentityCommonConstants.END_CERT));
        }
        // remove spaces, \r, \\r, \n, \\n, ], [ characters from certificate string
        return value.replaceAll("\\\\r|\\\\n|\\r|\\n|\\[|]| ", StringUtils.EMPTY);
    }

    /**
     * Utility method get the application certificate stored as a application property from SP Meta Data.
     * @param clientId ClientId of the application
     * @return the service provider certificate
     * @throws FinancialServicesException
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public static String getCertificateContent(String clientId) throws FinancialServicesException {

        Optional<ServiceProvider> serviceProvider;
        try {
            serviceProvider = Optional.ofNullable(identityExtensionsDataHolder
                    .getApplicationManagementService().getServiceProviderByClientId(clientId,
                            IdentityApplicationConstants.OAuth2.NAME, getSpTenantDomain(clientId)));
            if (serviceProvider.isPresent()) {
                return serviceProvider.get().getCertificateContent();
            }
        } catch (IdentityApplicationManagementException e) {
            log.error(String.format("Error occurred while retrieving OAuth2 application data for clientId %s",
                    clientId.replaceAll("[\r\n]", "")), e);
            throw new FinancialServicesException("Error occurred while retrieving OAuth2 application data for clientId"
                    , e);
        }
        return "";
    }

    /**
     * Utility method get the application property from SP Meta Data.
     *
     * @param clientId ClientId of the application
     * @param property Property of the application
     * @return the property value from SP metadata
     * @throws FinancialServicesException
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public static String getAppPropertyFromSPMetaData(String clientId, String property)
            throws FinancialServicesException {

        String spProperty = null;

        if (StringUtils.isNotEmpty(clientId)) {
            Optional<ServiceProvider> serviceProvider;
            try {
                serviceProvider = Optional.ofNullable(identityExtensionsDataHolder
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
                throw new FinancialServicesException("Error occurred while retrieving OAuth2 application data for" +
                        " clientId", e);
            }
        } else {
            log.error(IdentityCommonConstants.CLIENT_ID_ERROR);
            throw new FinancialServicesException(IdentityCommonConstants.CLIENT_ID_ERROR);
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

    public static int getTenantIdOfUser(String userId) throws FinancialServicesException {
        try {
            return IdentityTenantUtil.getTenantIdOfUser(userId);
        } catch (IdentityRuntimeException e) {
            throw new FinancialServicesException("Error retrieving tenant id for user: " + userId, e);
        }
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
     * @return
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
     * @return
     */
    public static String getJtiFromCache(String jtiValue) {

        JwtJtiCacheKey cacheKey = JwtJtiCacheKey.of(jtiValue);
        return JwtJtiCache.getInstance().getFromCache(cacheKey);
    }

    /**
     * return the new approved scope.
     *
     * @param oAuthAuthzReqMessageContext
     * @return
     */
    public static String[] updateApprovedScopes(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext,
                                                String consentID) {

        if (oAuthAuthzReqMessageContext != null && oAuthAuthzReqMessageContext.getAuthorizationReqDTO() != null) {

            String[] scopes = oAuthAuthzReqMessageContext.getApprovedScope();
            if (scopes != null && !Arrays.asList(scopes).contains("api_store")) {
                if (consentID.isEmpty()) {
                    log.warn("Consent-ID retrieved from request is empty");
                    return scopes;
                }

                String consentIdClaim = identityExtensionsDataHolder.getConfigurationMap()
                        .get(FinancialServicesConstants.CONSENT_ID_CLAIM_NAME)
                        .toString();
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
     * Validates the action status of the external service response
     *
     * @param response
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
     * Method to decide if a refresh token should be issued with service extension.
     *
     * @param oAuthTokenReqMessageContext
     * @return
     * @throws FinancialServicesException
     * @throws IdentityOAuth2Exception
     */
    public static boolean issueRefreshTokenWithServiceExtension(OAuthTokenReqMessageContext oAuthTokenReqMessageContext)
            throws FinancialServicesException, IdentityOAuth2Exception {

        String consentId = IdentityCommonUtils.getConsentId(oAuthTokenReqMessageContext.getScope());
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

        IdentityCommonUtils.serviceExtensionActionStatusValidation(response);

        JsonNode responseData = response.getData();
        if (responseData == null || !responseData.has(IdentityCommonConstants.ISSUE_REFRESH_TOKEN)) {
            throw new IdentityOAuth2Exception("Missing issueRefreshToken in response payload.");
        }

        return responseData.get(IdentityCommonConstants.ISSUE_REFRESH_TOKEN).asBoolean();
    }

    /**
     * Call Request Object Service and retrieve openbanking intent id.
     *
     * @param key session data key
     * @return openbanking intent id
     */
    public static Optional<RequestedClaim>  retrieveIntentIDFromReqObjService(String key, String request)
            throws IdentityOAuth2Exception {
        Optional<RequestedClaim> intentClaim;
        List<RequestedClaim> requestedClaimsForIDToken;
        String errorMsg = "";
        try {
            if ("authorize".equals(request)) {
                requestedClaimsForIDToken = getRequestedClaims(
                        new SessionDataCacheKey(key).getSessionDataId(), false);
                errorMsg = "Failed to obtain Claims from session data key :";
            } else {
                requestedClaimsForIDToken = getRequestedClaims(key);
                errorMsg = "Failed to obtain Claims from token id :";
            }

            intentClaim = requestedClaimsForIDToken.stream()
                    .filter(claimParam -> IdentityCommonConstants.OPENBANKING_INTENT_ID.equals(claimParam.getName()))
                    .findFirst();
        } catch (IdentityOAuth2Exception e) {
            throw new IdentityOAuth2Exception(errorMsg + key);
        }

        return intentClaim;
    }

    /**
     * Method to obtain Claims from request object.
     *
     * @param sessionDataKey session data key of request
     * @param isUserInfo     boolean value indicating whether user info claims are required or not
     * @return List of claims
     * @throws IdentityOAuth2Exception when failed to obtain claims using the service
     */
    @Generated(message = "Ignoring since the method require OSGi services to function.")
    public static List<RequestedClaim> getRequestedClaims(String sessionDataKey, boolean isUserInfo)
            throws IdentityOAuth2Exception {

        List<RequestedClaim> requestedClaims = new ArrayList<>();
        if (sessionDataKey != null && !sessionDataKey.isEmpty()) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Obtaining request claims (without userinfo values ) from OSGi Service");
                }

                Object serviceObj = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .getOSGiService(RequestObjectService.class, null);

                if (serviceObj instanceof RequestObjectService) {
                    RequestObjectService requestObjectService = (RequestObjectService) serviceObj;
                    requestedClaims = requestObjectService.getRequestedClaimsForSessionDataKey(sessionDataKey,
                            isUserInfo);
                }
            } catch (RequestObjectException e) {
                throw new IdentityOAuth2Exception("Failed to obtain Claims from session data key :" +
                        sessionDataKey);
            }
        }
        return requestedClaims;
    }

    /**
     * Method to obtain Claims from request object.
     *
     * @param token token
     * @return List of claims
     * @throws IdentityOAuth2Exception when failed to obtain claims using the service
     */
    @Generated(message = "Ignoring since the method require OSGi services to function.")
    public static List<RequestedClaim> getRequestedClaims(String token) throws IdentityOAuth2Exception {

        List<RequestedClaim> requestedClaims = new ArrayList<>();
        if (token != null && !token.isEmpty()) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Obtaining request claims from OSGi Service");
                }
                Object serviceObj = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .getOSGiService(RequestObjectService.class, null);

                if (serviceObj instanceof RequestObjectService) {
                    RequestObjectService requestObjectService = (RequestObjectService) serviceObj;
                    requestedClaims = requestObjectService.getRequestedClaimsForIDToken(token);
                }
            } catch (RequestObjectException e) {
                throw new IdentityOAuth2Exception("Failed to obtain Claims from token", e);
            }
        }
        return requestedClaims;
    }

    /**
     * Get consent id from the request object or common auth id.
     *
     * @param oauthAuthzMsgCtx OAuth authorization message context
     * @return Consent ID
     * @throws JsonProcessingException
     * @throws ConsentManagementException
     */
    public static String getConsentId(OAuthAuthzReqMessageContext oauthAuthzMsgCtx) throws JsonProcessingException,
            ConsentManagementException {

        boolean isPreInitiatedConsent = Boolean.parseBoolean(identityExtensionsDataHolder.getConfigurationMap()
                .get(FinancialServicesConstants.IS_PRE_INITIATED_CONSENT).toString());

        if (!isPreInitiatedConsent) {
            String commonAuthId = getCommonAuthId(oauthAuthzMsgCtx);
            return getConsentIdFromCommonAuthId(commonAuthId);
        }

        String authFlowConsentIdSource = (String) identityExtensionsDataHolder.getConfigurationMap()
                .get(FinancialServicesConstants.AUTH_FLOW_CONSENT_ID_SOURCE);
        if (IdentityCommonConstants.REQUEST_OBJECT.equals(authFlowConsentIdSource)) {
            return getConsentIdFromEssentialClaims(oauthAuthzMsgCtx.getAuthorizationReqDTO().getEssentialClaims());
        }

        if (IdentityCommonConstants.REQUEST_PARAM.equals(authFlowConsentIdSource)) {
            return getConsentIdFromRequestParam(oauthAuthzMsgCtx.getAuthorizationReqDTO());
        }

        return null;
    }

    private static String getConsentIdFromCommonAuthId(String commonAuthId) throws ConsentManagementException {

        List<String> consentIds = identityExtensionsDataHolder.getConsentCoreService()
                .getConsentIdByConsentAttributeNameAndValue(IdentityCommonConstants.COMMON_AUTH_ID, commonAuthId);

        if (!consentIds.isEmpty()) {
            return consentIds.get(0);
        }

        return null;
    }

    /**
     * Get CommonAuthId from the incoming authorize request.
     *
     * @param oAuthAuthzReqMessageContext
     * @return commonAuthId
     */
    @SuppressFBWarnings("COOKIE_USAGE")
    // Suppressed content - Cookie handling to extract CommonAuthId from incoming request
    // Suppression reason - False Positive : The cookie is read-only and not modified or created.
    // It is securely handled by the identity server framework, which enforces appropriate
    // security flags (e.g., Secure, HttpOnly) during cookie creation.
    // Suppressed warning count - 1
    private static String getCommonAuthId(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) {

        String commonAuthId = StringUtils.EMPTY;
        Cookie[] cookies = oAuthAuthzReqMessageContext.getAuthorizationReqDTO().getCookie();

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

    private static String getConsentIdFromEssentialClaims(String essentialClaims)
            throws JsonProcessingException {

        String jsonPath = (String) identityExtensionsDataHolder.getConfigurationMap()
                .get(FinancialServicesConstants.CONSENT_ID_EXTRACTION_JSON_PATH);

        if (StringUtils.isBlank(essentialClaims) || StringUtils.isBlank(jsonPath)) {
            return null; // Return null if input is invalid
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(essentialClaims);
        JsonNode targetNode = node.at(jsonPath);
        return extractConsentIdFromRegex(targetNode.asText());
    }

    private static String getConsentIdFromRequestParam(OAuth2AuthorizeReqDTO oAuth2AuthorizeReqDTO) {

        String key = (String) identityExtensionsDataHolder.getConfigurationMap()
                .get(FinancialServicesConstants.CONSENT_ID_EXTRACTION_KEY);

        // TODO: need to support other request parameters based on requirements
        switch (key) {
            case IdentityCommonConstants.SCOPE:
                return getConsentIdFromScope(oAuth2AuthorizeReqDTO.getScopes());
            default:
                return null;
        }
    }

    private static String getConsentIdFromScope(String[] scopes) {

        StringBuilder scopesString = new StringBuilder();
        for (String scope : scopes) {
            scopesString.append(scope).append(" ");
        }

        return extractConsentIdFromRegex(scopesString.toString().trim());
    }

    private static String extractConsentIdFromRegex(String value) {

        if (StringUtils.isBlank(value)) {
            return value;
        }

        String patternString = (String) identityExtensionsDataHolder.getConfigurationMap()
                .get(FinancialServicesConstants.CONSENT_ID_EXTRACTION_REGEX_PATTERN);

        if (StringUtils.isBlank(patternString)) {
            return value;
        }

        Pattern pattern = Pattern.compile(patternString);

        Matcher matcher = pattern.matcher(value);
        return matcher.find() ? matcher.group() : null;
    }

    /**
     * Get consent id from the scopes.
     *
     * @param scopes Scopes
     * @return Consent ID
     */
    public static String getConsentId(String[] scopes) {

        String consentIdClaim = identityExtensionsDataHolder.getConfigurationMap()
                .get(FinancialServicesConstants.CONSENT_ID_CLAIM_NAME).toString();

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
    public static String getConsentIdClaimName() {

        return (String) identityExtensionsDataHolder.getConfigurationMap()
                .get(FinancialServicesConstants.CONSENT_ID_CLAIM_NAME);
    }

    /**
     * Add consent ID to the token response.
     * @param oAuth2AccessTokenRespDTO
     */
    public static void addConsentIdToTokenResponse(OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO) {

        boolean shouldAddConsentIdClaimToTokenResponse = Boolean
                .parseBoolean((String) IdentityExtensionsDataHolder.getInstance().getConfigurationMap()
                        .get(FinancialServicesConstants.APPEND_CONSENT_ID_TO_ACCESS_TOKEN));
        if (shouldAddConsentIdClaimToTokenResponse) {
            String consentId = getConsentId(oAuth2AccessTokenRespDTO.getAuthorizedScopes().split(" "));
            String consentIdClaimName = IdentityCommonUtils.getConsentIdClaimName();
            oAuth2AccessTokenRespDTO.addParameter(consentIdClaimName, consentId);
        }
    }

}
