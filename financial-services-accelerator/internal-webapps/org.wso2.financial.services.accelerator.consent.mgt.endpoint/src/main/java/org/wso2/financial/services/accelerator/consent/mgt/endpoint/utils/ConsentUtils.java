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

package org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.local.auth.api.core.ParameterResolverService;
import org.wso2.carbon.utils.HTTPClientUtils;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementRuntimeException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.AuthErrorCode;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.security.interfaces.RSAPrivateKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

/**
 * Utils class for consent management endpoint.
 */
public class ConsentUtils {

    private static final Log log = LogFactory.getLog(ConsentUtils.class);
    private static final Gson gson = new Gson();

    /**
     * Extract headers from a request object.
     *
     * @param request The request object
     * @return Map of header key value pairs
     */
    @SuppressFBWarnings("SERVLET_HEADER")
    // Suppressed content - Endpoints
    // Suppression reason - False Positive : These endpoints are secured with access
    // control
    // as defined in the IS deployment.toml file
    // Suppressed warning count - 1
    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

    /**
     * Util method to extract the payload from HTTP request object. Can be
     * JSONObject or JSONArray
     *
     * @param request The HTTP request object
     * @return Object payload can be either an instance of JSONObject or JSONArray
     *         only. Can be a ConsentException if
     *         is and error scenario. Error is returned instead of throwing since
     *         the error response should be handled by the
     *         toolkit is the manage scenario.
     */
    public static Object getPayload(HttpServletRequest request) {
        try {
            return new JSONObject(getStringPayload(request));
        } catch (JSONException e) {
            try {
                return new JSONArray(getStringPayload(request));
            } catch (JSONException ex) {
                log.error(ConsentConstants.ERROR_PAYLOAD_PARSE + ". Returning null", ex);
                return null;
            }
        }
    }

    /**
     * Extract string payload from request object.
     *
     * @param request The request object
     * @return String payload
     * @throws ConsentException Payload read errors
     */
    public static String getStringPayload(HttpServletRequest request) throws ConsentException {
        try {
            return IOUtils.toString(request.getInputStream());
        } catch (IOException e) {
            log.error(ConsentConstants.ERROR_PAYLOAD_READ, e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, ConsentConstants.ERROR_PAYLOAD_READ);
        }
    }

    /**
     * Util method to extract the payload from HTTP request object. Can be only
     * JSONObject
     *
     * @param request The request object
     * @return JSONObject payload can only be an instance of JSONObject
     * @throws ConsentException Parser errors and payload type is not JSON object
     */
    public static JSONObject getJSONObjectPayload(HttpServletRequest request) throws ConsentException {
        Object payload = getPayload(request);
        // JSONArray not supported here. If requirement arises, cast the object to
        // JSONArray from here
        if (!(payload instanceof JSONObject)) {
            return null;
        }
        return (JSONObject) payload;
    }

    /**
     * Util method to extract the payload from a HTTP request object.
     *
     * @param request The HTTP request object
     * @return Object payload can be xml or json. Can be a ConsentException if is
     *         and error scenario.
     *         Error is returned instead of throwing since the error response should
     *         be handled by the
     *         toolkit is the manage scenario.
     */
    public static Object getFileUploadPayload(HttpServletRequest request) {
        try {
            String payload = getStringPayload(request);
            if (payload == null) {
                log.debug("Payload is empty. Returning null");
            }
            return payload;
        } catch (ConsentException e) {
            // Not throwing error since error should be formatted by manage toolkit
            log.error(String.format("%s. Returning null", e.getMessage().replaceAll("\n\r", "")), e);
            return null;
        }
    }

    /**
     * Get the sensitive data corresponding to the session data consent key.
     *
     * @param sessionDataKeyConsent The session data key corresponding to the data
     *                              hidden from redirect URLs
     * @return The hidden sensitive data as key-value pairs.
     */
    public static Map<String, Serializable> getSensitiveDataWithConsentKey(String sessionDataKeyConsent) {

        return getSensitiveData(sessionDataKeyConsent);
    }

    /**
     * Get the sensitive data corresponding to the session data key or the session
     * data consent key.
     *
     * @param key The session data key or session data consent key corresponding to
     *            the data hidden from redirect URLs
     * @return The hidden sensitive data as key-value pairs.
     */
    public static Map<String, Serializable> getSensitiveData(String key) {

        Map<String, Serializable> sensitiveDataSet = new HashMap<>();

        Object serviceObj = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(ParameterResolverService.class, null);
        if (serviceObj instanceof ParameterResolverService) {
            ParameterResolverService resolverService = (ParameterResolverService) serviceObj;

            Set<String> filter = Collections.emptySet();

            sensitiveDataSet.putAll((resolverService)
                    .resolveParameters(ConsentConstants.CONSENT_KEY, key, filter));

            if (sensitiveDataSet.isEmpty()) {
                sensitiveDataSet.putAll((resolverService)
                        .resolveParameters(ConsentConstants.REQUEST_KEY, key, filter));
            }

            if (sensitiveDataSet.isEmpty()) {
                log.error("No available data for key provided");
                sensitiveDataSet.put(ConsentExtensionConstants.IS_ERROR, "No available data for key provided");
                return sensitiveDataSet;
            }

            sensitiveDataSet.put(ConsentExtensionConstants.IS_ERROR, "false");
            return sensitiveDataSet;

        } else {
            log.error("Could not retrieve ParameterResolverService OSGi service");
            sensitiveDataSet.put(ConsentExtensionConstants.IS_ERROR, "Could not retrieve parameter service");
            return sensitiveDataSet;
        }
    }

    /**
     * Method to set common data to the response object.
     * 
     * @param consentData Consent data object
     * @param jsonObject  JSON object to which the data should be appended
     */
    public static void setCommonDataToResponse(ConsentData consentData, JSONObject jsonObject) {

        if (!jsonObject.has(ConsentExtensionConstants.TYPE)) {
            jsonObject.put(ConsentExtensionConstants.TYPE, consentData.getType());
        }
        if (!jsonObject.has(ConsentExtensionConstants.APPLICATION)) {
            jsonObject.put(ConsentExtensionConstants.APPLICATION, consentData.getApplication());
        }
    }

    /**
     * @param consentDetails json object of consent data
     * @param sessionDataKey session data key
     * @return ConsentData object
     * @throws URISyntaxException if the URI is invalid
     */
    public static ConsentData getConsentDataFromAttributes(JsonObject consentDetails, String sessionDataKey)
            throws URISyntaxException {

        JsonObject sensitiveDataMap = consentDetails.get(ConsentExtensionConstants.SENSITIVE_DATA_MAP)
                .getAsJsonObject();
        ConsentData consentData = new ConsentData(sessionDataKey,
                sensitiveDataMap.get(ConsentExtensionConstants.LOGGED_IN_USER).getAsString(),
                sensitiveDataMap.get(ConsentExtensionConstants.SP_QUERY_PARAMS).getAsString(),
                consentDetails.get(ConsentExtensionConstants.SCOPES).getAsString(),
                sensitiveDataMap.get(ConsentExtensionConstants.APPLICATION).getAsString(),
                gson.fromJson(consentDetails.get(ConsentExtensionConstants.REQUEST_HEADERS), Map.class));
        consentData.setSensitiveDataMap(gson.fromJson(sensitiveDataMap, Map.class));
        URI redirectURI = new URI(consentDetails.get(ConsentExtensionConstants.REQUEST_URI).getAsString());
        consentData.setRedirectURI(redirectURI);
        consentData.setUserId(consentDetails.get(ConsentExtensionConstants.USER_ID).getAsString());
        consentData.setConsentId(consentDetails.get(ConsentExtensionConstants.CONSENT_ID).getAsString());
        consentData.setClientId(consentDetails.get(ConsentExtensionConstants.CLIENT_ID).getAsString());
        consentData.setRegulatory(Boolean.parseBoolean(consentDetails.get(ConsentExtensionConstants.REGULATORY)
                .getAsString()));
        ConsentResource consentResource = gson.fromJson(consentDetails.get(ConsentExtensionConstants.CONSENT_RESOURCE),
                ConsentResource.class);
        consentData.setConsentResource(consentResource);
        AuthorizationResource authorizationResource = gson.fromJson(consentDetails
                .get(ConsentExtensionConstants.AUTH_RESOURCE), AuthorizationResource.class);
        consentData.setAuthResource(authorizationResource);
        consentData.setMetaDataMap(gson.fromJson(consentDetails.get(ConsentExtensionConstants.META_DATA), Map.class));
        consentData.setType(consentDetails.get(ConsentExtensionConstants.TYPE).getAsString());
        return consentData;
    }

    /**
     * Send authorize request in order to complete the authorize flow and get the
     * redirect.
     *
     * @param consent     The approval/denial of the consent of the user
     * @param cookies     The session cookies used in auth flow
     * @param consentData Consent data object which contains consent information
     * @return The redirect URI to end the authorize flow
     */
    public static URI authorizeRequest(String consent, Map<String, String> cookies, ConsentData consentData) {

        String authorizeURL = IdentityUtil.getProperty("OAuth.OAuth2AuthzEPUrl");
        try (CloseableHttpClient client = HTTPClientUtils.createClientWithCustomVerifier().build()) {

            BasicCookieStore cookieStore = new BasicCookieStore();
            String cookieDomain = new URI(authorizeURL).getHost();
            for (Map.Entry<String, String> cookieValue : cookies.entrySet()) {
                BasicClientCookie cookie = new BasicClientCookie(cookieValue.getKey(), cookieValue.getValue());
                cookie.setDomain(cookieDomain);
                cookie.setPath("/");
                cookie.setSecure(true);
                cookieStore.addCookie(cookie);
            }
            HttpPost authorizeRequest = new HttpPost(authorizeURL);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(ConsentExtensionConstants.HAS_APPROVED_ALWAYS, "false"));
            params.add(new BasicNameValuePair(ConsentExtensionConstants.SESSION_DATA_KEY_CONSENT,
                    consentData.getSessionDataKey()));
            params.add(new BasicNameValuePair(ConsentExtensionConstants.CONSENT, consent));
            params.add(new BasicNameValuePair(ConsentExtensionConstants.USER, consentData.getUserId()));
            HttpContext localContext = new BasicHttpContext();
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params);
            authorizeRequest.setEntity(entity);
            HttpResponse authorizeResponse = client.execute(authorizeRequest, localContext);

            if (authorizeResponse.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_MOVED_TEMP) {
                throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                        "Error while getting authorize redirect", consentData.getState());
            } else {
                // Extract the location header from the authorization redirect
                return new URI(authorizeResponse.getLastHeader(ConsentExtensionConstants.LOCATION).getValue());
            }
        } catch (IOException e) {
            log.error("Error while sending authorize request to complete the authorize flow", e);
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                    "Error while sending authorize request to complete the authorize flow", consentData.getState());
        } catch (URISyntaxException e) {
            log.error("Authorize response URI syntax error", e);
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                    "Internal server error", consentData.getState());
        }
    }

    public static JSONObject detailedConsentToJSON(DetailedConsentResource detailedConsentResource) {
        JSONObject consentResource = new JSONObject();

        consentResource.put(ConsentExtensionConstants.CC_CONSENT_ID, detailedConsentResource.getConsentID());
        consentResource.put(ConsentExtensionConstants.CLIENT_ID, detailedConsentResource.getClientID());
        try {
            consentResource.put(ConsentExtensionConstants.RECEIPT,
                    new JSONObject(detailedConsentResource.getReceipt()));
        } catch (JSONException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Exception occurred while parsing" +
                    " receipt");
        }
        consentResource.put(ConsentExtensionConstants.CONSENT_TYPE, detailedConsentResource.getConsentType());
        consentResource.put(ConsentExtensionConstants.CURRENT_STATUS,
                detailedConsentResource.getCurrentStatus());
        consentResource.put(ConsentExtensionConstants.CONSENT_FREQUENCY,
                detailedConsentResource.getConsentFrequency());
        consentResource.put(ConsentExtensionConstants.VALIDITY_PERIOD,
                detailedConsentResource.getValidityPeriod());
        consentResource.put(ConsentExtensionConstants.CREATED_TIMESTAMP,
                detailedConsentResource.getCreatedTime());
        consentResource.put(ConsentExtensionConstants.UPDATED_TIMESTAMP,
                detailedConsentResource.getUpdatedTime());
        consentResource.put(ConsentExtensionConstants.RECURRING_INDICATOR,
                detailedConsentResource.isRecurringIndicator());
        JSONObject attributes = new JSONObject();
        Map<String, String> attMap = detailedConsentResource.getConsentAttributes();
        for (Map.Entry<String, String> entry : attMap.entrySet()) {
            attributes.put(entry.getKey(), entry.getValue());
        }
        consentResource.put(ConsentExtensionConstants.CONSENT_ATTRIBUTES, attributes);
        JSONArray authorizationResources = new JSONArray();
        ArrayList<AuthorizationResource> authArray = detailedConsentResource.getAuthorizationResources();
        for (AuthorizationResource resource : authArray) {
            JSONObject resourceJSON = new JSONObject();
            resourceJSON.put(ConsentExtensionConstants.AUTH_ID, resource.getAuthorizationID());
            resourceJSON.put(ConsentExtensionConstants.CC_CONSENT_ID, resource.getConsentID());
            resourceJSON.put(ConsentExtensionConstants.USER_ID, resource.getUserID());
            resourceJSON.put(ConsentExtensionConstants.AUTH_STATUS, resource.getAuthorizationStatus());
            resourceJSON.put(ConsentExtensionConstants.AUTH_TYPE, resource.getAuthorizationType());
            resourceJSON.put(ConsentExtensionConstants.UPDATE_TIME, resource.getUpdatedTime());
            authorizationResources.put(resourceJSON);
        }
        consentResource.put(ConsentExtensionConstants.AUTH_RESOURCES, authorizationResources);
        JSONArray consentMappingResources = new JSONArray();
        ArrayList<ConsentMappingResource> mappingArray = detailedConsentResource.getConsentMappingResources();
        for (ConsentMappingResource resource : mappingArray) {
            JSONObject resourceJSON = new JSONObject();
            resourceJSON.put(ConsentExtensionConstants.MAPPING_ID, resource.getMappingID());
            resourceJSON.put(ConsentExtensionConstants.AUTH_ID, resource.getAuthorizationID());
            resourceJSON.put(ConsentExtensionConstants.ACCOUNT_ID, resource.getAccountID());
            resourceJSON.put(ConsentExtensionConstants.PERMISSION, resource.getPermission());
            resourceJSON.put(ConsentExtensionConstants.MAPPING_STATUS, resource.getMappingStatus());
            consentMappingResources.put(resourceJSON);
        }
        consentResource.put(ConsentExtensionConstants.MAPPING_RESOURCES, consentMappingResources);
        return consentResource;
    }

    /**
     * Sign a string body using the carbon default key pair.
     * Skipped in unit tests since @KeystoreManager cannot be mocked
     *
     * @param body the body that needs to be signed as a string
     * @return string value of the signed JWT
     * @throws Exception error if the tenant is invalid
     */
    public static String signJWTWithDefaultKey(String body) throws Exception {
        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(-1234);
        Key privateKey = keyStoreManager.getDefaultPrivateKey();
        return generateJWT(body, privateKey);
    }

    /**
     * Util method to generate JWT using a payload and a private key. RS256 is the
     * algorithm used
     *
     * @param payload    The payload body to be signed
     * @param privateKey The private key for the JWT to be signed with
     * @return String signed JWT
     */
    public static String generateJWT(String payload, Key privateKey) {

        if (privateKey == null || payload == null) {
            log.debug("Null value passed for payload or key. Cannot generate JWT");
            throw new ConsentManagementRuntimeException("Payload and key cannot be null");
        }

        if (!(privateKey instanceof RSAPrivateKey)) {
            throw new ConsentManagementRuntimeException("Private key should be an instance of RSAPrivateKey");
        }

        JWSSigner signer = new RSASSASigner((RSAPrivateKey) privateKey);
        JWSHeader.Builder headerBuilder = new JWSHeader.Builder(JWSAlgorithm.RS256);

        SignedJWT signedJWT = null;
        try {
            signedJWT = new SignedJWT(headerBuilder.build(), JWTClaimsSet.parse(payload));
            signedJWT.sign(signer);
        } catch (ParseException | JOSEException e) {
            throw new ConsentManagementRuntimeException("Error occurred while signing JWT");
        }
        return signedJWT.serialize();
    }

    /**
     * This method returns the configuration value on whether the JWT payload
     * validation needs to be performed in the
     * consent validation endpoint.
     * 
     * @return config value
     */
    public static boolean getConsentJWTPayloadValidatorConfigEnabled() {
        return Boolean.parseBoolean(FinancialServicesConfigParser.getInstance().getConsentValidationConfig());
    }

    /**
     * Extract and add query parameters from a URL to existing resource map.
     * Resource parameter map will contain the resource path(ex:
     * /aisp/accounts/{AccountId}?queryParam=queryParamValue),
     * http method, context(ex: /open-banking/v3.1/aisp)
     *
     * @param resourceParams Map containing the resource parameters
     * @return Extracted query parameter map
     */
    public static Map<String, String> addQueryParametersToResourceParamMap(JSONObject resourceParams)
            throws URISyntaxException {

        if (resourceParams.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, String> resourceParamsMap = new HashMap<>();
        resourceParams.keySet()
                .forEach(key -> resourceParamsMap.put(key, resourceParams.getString(key)));

        URI url = new URI(resourceParams.getString(ConsentExtensionConstants.RESOURCE));
        resourceParamsMap.put(ConsentExtensionConstants.RESOURCE_PATH, url.getRawPath());

        if (url.getRawQuery() != null) {
            String[] params = url.getRawQuery().split("&");

            for (String param : params) {
                if (param.split("=").length == 2) {
                    String name = param.split("=")[0];
                    String value = param.split("=")[1];
                    resourceParamsMap.put(name, value);
                }
            }
        }
        return resourceParamsMap;
    }

    /**
     * Check whether the given string is a valid JSON or not.
     * 
     * @param json JSON string
     * @return true if the string is a valid JSON, false otherwise
     */
    public static boolean isValidJson(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException e) {
            try {
                new JSONArray(json);
            } catch (JSONException ne) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get only the allowed headers from the provided header map.
     *
     * @param headers            Map of all headers
     * @param allowedHeaderNames List of allowed header names
     * @return Map of allowed headers
     */
    public static Map<String, String> getAllowedHeaders(Map<String, String> headers, List<String> allowedHeaderNames) {
        return headers.entrySet().stream()
                .filter(entry -> allowedHeaderNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Validates the structure of the response retrieved from consent retrieval steps
     *
     * @param jsonObject response received from executing consent retrieval steps
     * @param redirectURI redirect URI to end the authorize flow
     * @param state state to return to the redirect URI
     * @throws ConsentException if the payload format is invalid
     */
    public static void validateRetrievalPayload(JSONObject jsonObject, URI redirectURI, String state)
            throws ConsentException {
        // consentData is mandatory
        if (!jsonObject.has("consentData")) {
            log.error("Retrieval payload missing mandatory attribute consentData");
            throw new ConsentException(redirectURI, AuthErrorCode.INVALID_REQUEST,
                    "Retrieval payload missing mandatory attribute \"consentData\"", state);
        }
        JSONObject consentData = jsonObject.getJSONObject("consentData");

        // type is mandatory and must be string
        if (!consentData.has("type") || !(consentData.get("type") instanceof String)) {
            log.error("Retrieval payload missing mandatory string attribute \"type\"");
            throw new ConsentException(redirectURI, AuthErrorCode.INVALID_REQUEST,
                    "Retrieval payload missing mandatory string attribute \"type\"", state);
        }

        // basicConsentData is mandatory and must be array
        if (!consentData.has("basicConsentData") || !(consentData.get("basicConsentData") instanceof JSONArray)) {
            log.error("Retrieval payload missing mandatory array attribute \"basicConsentData\"");
            throw new ConsentException(redirectURI, AuthErrorCode.INVALID_REQUEST,
                    "Retrieval payload missing mandatory array attribute \"basicConsentData\"", state);
        }
        JSONArray basicConsentData = consentData.getJSONArray("basicConsentData");

        for (int i = 0; i < basicConsentData.length(); i++) {
            JSONObject item = basicConsentData.getJSONObject(i);
            if (!item.has("title") || !(item.get("title") instanceof String)) {
                log.error("Each item in basicConsentData must contain a string attribute \"title\"");
                throw new ConsentException(redirectURI, AuthErrorCode.INVALID_REQUEST,
                        "Each item in basicConsentData must contain a string attribute \"title\"", state);
            }

            if (!item.has("data") || !(item.get("data") instanceof JSONArray)) {
                log.error("Each item in basicConsentData must contain an array attribute \"data\"");
                throw new ConsentException(redirectURI, AuthErrorCode.INVALID_REQUEST,
                        "Each item in basicConsentData must contain an array attribute \"data\"", state);
            }
            JSONArray dataArray = item.getJSONArray("data");
            for (int j = 0; j < dataArray.length(); j++) {
                if (!(dataArray.get(j) instanceof String)) {
                    log.error("Items in basicConsentData.data array must be strings");
                    throw new ConsentException(redirectURI, AuthErrorCode.INVALID_REQUEST,
                            "Items in basicConsentData.data array must be strings", state);
                }
            }
        }

        if (consentData.has("requestedPermissions")) {
            JSONObject requestedPermissions = consentData.getJSONObject("requestedPermissions");

            if (!requestedPermissions.has("permissions") || !(requestedPermissions.get("permissions") instanceof JSONArray)) {
                log.error("requestedPermissions must contain mandatory array attribute \"permissions\"");
                throw new ConsentException(redirectURI, AuthErrorCode.INVALID_REQUEST,
                        "requestedPermissions must contain mandatory array attribute \"permissions\"", state);
            }

            JSONArray permissions = requestedPermissions.getJSONArray("permissions");
            for (int i = 0; i < permissions.length(); i++) {
                JSONObject permission = permissions.getJSONObject(i);

                if (!permission.has("displayValues") || !(permission.get("displayValues") instanceof JSONArray)) {
                    log.error("Each permission must contain mandatory array attribute \"displayValues\"");
                    throw new ConsentException(redirectURI, AuthErrorCode.INVALID_REQUEST,
                            "Each permission must contain mandatory array attribute \"displayValues\"", state);
                }

                JSONArray displayValues = permission.getJSONArray("displayValues");
                for (int j = 0; j < displayValues.length(); j++) {
                    if (!(displayValues.get(j) instanceof String)) {
                        log.error("Items in permission.displayValues must be strings");
                        throw new ConsentException(redirectURI, AuthErrorCode.INVALID_REQUEST,
                                "Items in permission.displayValues must be strings", state);
                    }
                }

                if (permission.has("initiatedAccounts")) {
                    JSONArray initiatedAccounts = permission.getJSONArray("initiatedAccounts");
                    for (int j = 0; j < initiatedAccounts.length(); j++) {
                        JSONObject acc = initiatedAccounts.getJSONObject(j);
                        validateAccount(acc, "permission.initiatedAccounts[" + j + "]", redirectURI, state);
                    }
                }
            }

            if (requestedPermissions.has("initiatedAccountsForConsent")) {
                JSONArray accounts = requestedPermissions.getJSONArray("initiatedAccountsForConsent");
                for (int i = 0; i < accounts.length(); i++) {
                    JSONObject acc = accounts.getJSONObject(i);
                    validateAccount(acc, "requestedPermissions.initiatedAccountsForConsent[" + i + "]", redirectURI, state);
                }
            }

            if (requestedPermissions.has("displayConsumerAccountsPerPermission") &&
                    !(requestedPermissions.get("displayConsumerAccountsPerPermission") instanceof Boolean)) {
                log.error("displayConsumerAccountsPerPermission must be a boolean");
                throw new ConsentException(redirectURI, AuthErrorCode.INVALID_REQUEST,
                        "displayConsumerAccountsPerPermission must be a boolean", state);
            }
        }

        if (consentData.has("isReauthorization") &&
                !(consentData.get("isReauthorization") instanceof Boolean)) {
            log.error("isReauthorization must be a boolean");
            throw new ConsentException(redirectURI, AuthErrorCode.INVALID_REQUEST,
                    "isReauthorization must be a boolean", state);
        }

        if (jsonObject.has("consumerData")) {
            JSONArray consumerData = jsonObject.getJSONArray("consumerData");
            for (int i = 0; i < consumerData.length(); i++) {
                JSONObject acc = consumerData.getJSONObject(i);
                validateAccount(acc, "consumerData[" + i + "]", redirectURI, state);

                if (acc.has("selected") && !(acc.get("selected") instanceof Boolean)) {
                    log.error("consumerData.selected must be a boolean");
                    throw new ConsentException(redirectURI, AuthErrorCode.INVALID_REQUEST,
                            "consumerData.selected must be a boolean", state);
                }
            }
        }
    }

    /**
     * Helper method to validate account objects
     *
     * @param acc account object
     * @param context context of the account object
     * @param redirectURI redirect URI to end the authorize flow
     * @param state state to return to the redirect URI
     * @throws ConsentException if validation an account object failed
     */
    private static void validateAccount(JSONObject acc, String context, URI redirectURI, String state)
            throws ConsentException {
        if (!acc.has("displayName") || !(acc.get("displayName") instanceof String)) {
            log.error(context.replaceAll("[\r\n]","") + " missing mandatory string " +
                    "attribute \"displayName\"");
            throw new ConsentException(redirectURI, AuthErrorCode.INVALID_REQUEST,
                    context.replaceAll("[\r\n]","") + " missing mandatory string " +
                            "attribute \"displayName\"", state);
        }

        if (!acc.has("accountId") || !(acc.get("accountId") instanceof String)) {
            log.error(context.replaceAll("[\r\n]","") + " missing mandatory string " +
                    "attribute \"accountId\"");
            throw new ConsentException(redirectURI, AuthErrorCode.INVALID_REQUEST,
                    context.replaceAll("[\r\n]","") + " missing mandatory string " +
                            "attribute \"accountId\"", state);
        }
    }
}
