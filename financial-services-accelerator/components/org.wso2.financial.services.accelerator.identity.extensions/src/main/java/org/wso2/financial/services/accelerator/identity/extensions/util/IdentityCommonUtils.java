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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.nimbusds.jose.JWSAlgorithm;
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
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.identity.openidconnect.RequestObjectService;
import org.wso2.carbon.identity.openidconnect.model.RequestedClaim;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesRuntimeException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.OperationEnum;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;
import org.wso2.financial.services.accelerator.identity.extensions.dcr.cache.JwtJtiCache;
import org.wso2.financial.services.accelerator.identity.extensions.dcr.cache.JwtJtiCacheKey;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
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
import java.util.stream.Collectors;

/**
 * Common utility class for Identity Extensions.
 */
public class IdentityCommonUtils {

    private static final Log log = LogFactory.getLog(IdentityCommonUtils.class);

    /**
     * Method to obtain the Object when the full class path object config is given.
     *
     * @param configObject full class path config object
     * @return new object instance
     */
    @Generated(message = "Ignoring since method contains no logics")
    public static Object getClassInstanceFromFQN(Object configObject) {

        if (configObject == null || StringUtils.isBlank(configObject.toString())) {
            return null;
        }

        String classpath = configObject.toString();
        try {
            return Class.forName(classpath).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            log.error(String.format("Class not found: %s",  classpath.replaceAll("[\r\n]", "")));
            throw new FinancialServicesRuntimeException("Cannot find the defined class", e);
        } catch (InstantiationException | InvocationTargetException |
                 NoSuchMethodException | IllegalAccessException e) {
            //Throwing a runtime exception since we cannot proceed with invalid objects
            throw new FinancialServicesRuntimeException("Defined class" + classpath + "cannot be instantiated.", e);
        }
    }


    /**
     * Remove the internal scopes from the space delimited list of authorized scopes.
     *
     * @param scopes Authorized scopes of the token
     * @return scopes by removing the internal scopes
     */
    public static String[] removeInternalScopes(String[] scopes) {

        String consentIdClaim = IdentityExtensionsDataHolder.getInstance().getConfigurationMap()
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
            serviceProvider = Optional.ofNullable(IdentityExtensionsDataHolder.getInstance()
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
    public static String getConsentIDFromSessionData(String sessionDataKey) {

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
    static String retrieveConsentIDFromReqObjService(RequestObjectService service, String sessionDataKey) {

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
    static String retrieveConsentIDFromClaimList(List<RequestedClaim> requestedClaims) {

        String consentID = StringUtils.EMPTY;
        for (RequestedClaim claim : requestedClaims) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Claim: %s, value: %s", claim.getName().replaceAll("[\r\n]", ""),
                        claim.getValue().replaceAll("[\r\n]", "")));
            }

            if (IdentityCommonConstants.OPENBANKING_INTENT_ID.equals(claim.getName())) {
                consentID = claim.getValue();
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Consent-ID retrieved: %s", consentID.replaceAll("[\r\n]", "")));
                }
                break;
            }
        }
        return consentID;
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
            throw new IdentityOAuth2Exception(response.getErrorMessage(), response.getErrorDescription());
        }
    }

    /**
     * Get approved scopes by invoking the service extension endpoint
     *
     * @param oauthAuthzMsgCtx
     * @param consentId
     * @return
     * @throws IdentityOAuth2Exception
     */
    public static String[] getApprovedScopesWithServiceExtension(OAuthAuthzReqMessageContext oauthAuthzMsgCtx,
                                                           String consentId)
            throws IdentityOAuth2Exception, FinancialServicesException {

        // Construct the payload
        JSONObject data = new JSONObject();
        data.put(IdentityCommonConstants.SCOPES, Arrays.toString(oauthAuthzMsgCtx.getApprovedScope()));
        data.put(IdentityCommonConstants.CONSENT_ID, consentId);

        ExternalServiceRequest externalServiceRequest = new ExternalServiceRequest(UUID.randomUUID().toString(),
                data, OperationEnum.GET_APPROVED_SCOPES);

        // Invoke external service
        ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest,
                ServiceExtensionTypeEnum.POST_USER_AUTHORIZATION);

        IdentityCommonUtils.serviceExtensionActionStatusValidation(response);

        JsonNode responseData = response.getData();
        if (responseData == null || !responseData.has("approvedScopes")) {
            throw new IdentityOAuth2Exception("Missing approvedScopes array in response payload.");
        }

        ArrayNode approvedScopesArray = (ArrayNode) responseData.get("approvedScopes");
        List<String> scopesList = new ArrayList<>();
        approvedScopesArray.forEach(node -> scopesList.add(node.asText()));

        return scopesList.toArray(new String[0]);
    }

    /**
     * Get refresh token validity period by invoking service extension endpoint
     *
     * @param oauthAuthzMsgCtx
     * @param consentId
     * @return
     * @throws IdentityOAuth2Exception
     */
    public static long getRefreshTokenValidityPeriodWithServiceExtension(OAuthAuthzReqMessageContext oauthAuthzMsgCtx,
                                                                   String consentId)
            throws IdentityOAuth2Exception, FinancialServicesException {

        // Construct the payload
        JSONObject data = new JSONObject();
        data.put(IdentityCommonConstants.SCOPES, oauthAuthzMsgCtx.getApprovedScope());
        data.put(IdentityCommonConstants.CONSENT_ID, consentId);

        ConsentCoreService consentCoreService = IdentityExtensionsDataHolder.getInstance().getConsentCoreService();
        ConsentResource consentResource = null;
        try {
            consentResource = consentCoreService.getConsent(consentId, false);
            data.put(IdentityCommonConstants.VALIDITY_PERIOD, consentResource.getValidityPeriod());
        } catch (ConsentManagementException e) {
            log.error(String.format("Error while retrieving the consent for consent id: %s",
                    consentId.replaceAll("[\r\n]", "")));
            throw new IdentityOAuth2Exception("Error while retrieving the consent");
        }

        ExternalServiceRequest externalServiceRequest = new ExternalServiceRequest(UUID.randomUUID().toString(),
                data, OperationEnum.GET_REFRESH_TOKEN_VALIDITY_PERIOD);

        // Invoke external service
        ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest,
                ServiceExtensionTypeEnum.POST_USER_AUTHORIZATION);

        IdentityCommonUtils.serviceExtensionActionStatusValidation(response);

        JsonNode responseData = response.getData();
        if (responseData == null || !responseData.has("refreshTokenValidityPeriod")) {
            throw new IdentityOAuth2Exception("Missing refreshTokenValidityPeriod in response payload.");
        }

        return responseData.get("refreshTokenValidityPeriod").asLong();
    }

    public static void appendParametersToTokenResponseWithServiceExtension(
            OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO, OAuthTokenReqMessageContext tokReqMsgCtx)
            throws FinancialServicesException, IdentityOAuth2Exception {

        // Construct the payload
        JSONObject data = new JSONObject();
        data.put(IdentityCommonConstants.GRANT_TYPE, tokReqMsgCtx.getOauth2AccessTokenReqDTO().getGrantType());
        data.put(IdentityCommonConstants.SCOPES, tokReqMsgCtx.getScope());

        ExternalServiceRequest externalServiceRequest = new ExternalServiceRequest(
                UUID.randomUUID().toString(), data, OperationEnum.APPEND_PARAMETERS_TO_TOKEN_RESPONSE);

        // Invoke external service
        ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest,
                ServiceExtensionTypeEnum.PRE_ACCESS_TOKEN_GENERATION);

        IdentityCommonUtils.serviceExtensionActionStatusValidation(response);

        JsonNode responseData = response.getData();
        if (responseData == null || !responseData.has("parameters")) {
            throw new IdentityOAuth2Exception("Missing parameters in response payload.");
        }

        for (JsonNode claimNode : responseData.get("parameters")) {
            if (!claimNode.hasNonNull("key") || !claimNode.hasNonNull("value")) {
                continue;
            }

            String key = claimNode.get("key").asText();
            String value = claimNode.get("value").asText();

            // Add only if key is not empty
            if (!key.isEmpty()) {
                oAuth2AccessTokenRespDTO.addParameter(key, value);
            }
        }
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

}
