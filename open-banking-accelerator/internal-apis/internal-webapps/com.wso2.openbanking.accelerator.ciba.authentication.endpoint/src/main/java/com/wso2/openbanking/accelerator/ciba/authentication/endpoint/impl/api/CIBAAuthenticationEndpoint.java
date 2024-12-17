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
package com.wso2.openbanking.accelerator.ciba.authentication.endpoint.impl.api;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.wso2.openbanking.accelerator.ciba.authentication.endpoint.impl.exception.CIBAAuthenticationEndpointException;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.CarbonUtils;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.builder.ConsentStepsBuilder;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentRetrievalStep;
import com.wso2.openbanking.accelerator.consent.extensions.ciba.model.CIBAAuthenticationEndpointErrorResponse;
import com.wso2.openbanking.accelerator.consent.extensions.ciba.model.CIBAAuthenticationEndpointInterface;
import com.wso2.openbanking.accelerator.consent.extensions.common.AuthErrorCode;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentCache;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionExporter;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authenticator.push.common.PushAuthContextManager;
import org.wso2.carbon.identity.application.authenticator.push.common.PushJWTValidator;
import org.wso2.carbon.identity.application.authenticator.push.common.exception.PushAuthTokenValidationException;
import org.wso2.carbon.identity.application.authenticator.push.common.impl.PushAuthContextManagerImpl;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.DeviceHandler;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.exception.PushDeviceHandlerClientException;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.exception.PushDeviceHandlerServerException;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.impl.DeviceHandlerImpl;
import org.wso2.carbon.identity.application.authenticator.push.dto.AuthDataDTO;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.ciba.common.AuthReqStatus;
import org.wso2.carbon.identity.oauth.ciba.dao.CibaDAOFactory;
import org.wso2.carbon.identity.oauth.ciba.exceptions.CibaCoreException;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator.CIBAPushAuthenticator.createErrorResponse;

/**
 * Implementation class for the CIBA authentication endpoint API.
 */
@Deprecated
@Path("/")
public class CIBAAuthenticationEndpoint {

    private static final Log log = LogFactory.getLog(CIBAAuthenticationEndpoint.class);
    private static CIBAAuthenticationEndpointInterface cibaAuthenticationEndpointInterfaceTK;
    private static List<ConsentPersistStep> consentPersistSteps = null;
    private static List<ConsentRetrievalStep> consentRetrievalSteps = null;

    public CIBAAuthenticationEndpoint() {

        initializeConsentSteps();
    }

    @SuppressFBWarnings("JAXRS_ENDPOINT")
    // Suppressed content - Endpoint
    // Suppression reason - False Positive : This endpoint is secured with access control lists in the configuration
    // Suppressed warning count - 1
    @POST
    @Path("/push-auth/authenticate")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    public Response handleCIBAAuthenticationRequest(@Context HttpServletRequest request,
                                                    @Context HttpServletResponse response, @Context UriInfo uriInfo) {

        try {
            log.info("CIBA authentication call received");
            handleMobileResponse(request, response);
        } catch (CIBAAuthenticationEndpointException e) {
            // create error response
            CIBAAuthenticationEndpointErrorResponse errorResponse = createErrorResponse(e.getHttpStatusCode(),
                    e.getErrorCode(), e.getErrorDescription());
            return Response.status(errorResponse.getHttpStatusCode() != 0 ?
                            errorResponse.getHttpStatusCode() : e.getHttpStatusCode())
                    .entity(errorResponse.getPayload()).build();
        }

        return Response.status(HttpStatus.SC_ACCEPTED).build();
    }

    @SuppressFBWarnings("JAXRS_ENDPOINT")
    // Suppressed content - Endpoint
    // Suppression reason - False Positive : This endpoint is secured with access control lists in the configuration
    // Suppressed warning count - 1
    @GET
    @Path("/push-auth/discovery-data")
    @Produces({"application/json; charset=utf-8"})
    public Response handleDiscoveryRequest(@Context HttpServletRequest request,
                                           @Context HttpServletResponse response,
                                           @Context HttpHeaders headers) {

        try {
            log.info("CIBA discovery call received");
            JSONObject deviceRegistrationData = handleDiscovery(request, response, headers);
            return Response.status(HttpStatus.SC_ACCEPTED)
                    .entity(deviceRegistrationData).build();

        } catch (CIBAAuthenticationEndpointException e) {
            // create error response
            CIBAAuthenticationEndpointErrorResponse errorResponse = createErrorResponse(e.getHttpStatusCode(),
                    e.getErrorCode(), e.getErrorDescription());
            return Response.status(errorResponse.getHttpStatusCode() != 0 ?
                            errorResponse.getHttpStatusCode() : e.getHttpStatusCode())
                    .entity(errorResponse.getPayload()).build();
        }
    }

    @SuppressFBWarnings("HTTP_PARAMETER_POLLUTION")
    // Suppressed content - CIBAAuthenticationEndpointConstants.DEVICE_REGISTRATION_URL
    // Suppression reason - False Positive : This is a hard coded, trusted path. It is not a user input
    // Suppressed warning count - 1
    private JSONObject handleDiscovery(HttpServletRequest request, HttpServletResponse response, HttpHeaders headers)
            throws CIBAAuthenticationEndpointException {

        List<String> authHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
        String userToken = null;
        // Make the API call with user's access token
        if (authHeaders.size() != 0) {
            userToken = authHeaders.get(0);
        }
        String registrationUrl = CarbonUtils.getCarbonServerUrl() +
                CIBAAuthenticationEndpointConstants.DEVICE_REGISTRATION_URL;
        HttpUriRequest deviceRegistrationRequest = new HttpGet(registrationUrl);
        deviceRegistrationRequest.setHeader(CIBAAuthenticationEndpointConstants.AUTH_HEADER_NAME, userToken);
        JSONObject deviceRegistrationData = sendRequest(deviceRegistrationRequest);
        // Change authentication endpoint to OB CIBA webapp as it handles the CIBA authenticate call
        deviceRegistrationData.put(CIBAAuthenticationEndpointConstants.AUTHENTICATION_ENDPOINT,
                CIBAAuthenticationEndpointConstants.AUTHENTICATION_ENDPOINT_URL_PREFIX
                        + deviceRegistrationData.getAsString(
                        CIBAAuthenticationEndpointConstants.AUTHENTICATION_ENDPOINT));
        return deviceRegistrationData;
    }

    public JSONObject sendRequest(HttpUriRequest request)
            throws CIBAAuthenticationEndpointException {

        String responseStr = null;
        try {
            CloseableHttpClient client = HTTPClientUtils.getHttpsClient();
            HttpResponse response = client.execute(request);
            responseStr = EntityUtils.toString(response.getEntity());

            if ((response.getStatusLine().getStatusCode() / 100) != 2) {
                if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    log.debug("Received unauthorized(401) response. body: " + responseStr);
                    throw new CIBAAuthenticationEndpointException(HttpStatus.SC_UNAUTHORIZED,
                            CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_UNAUTHORIZED.getMessage(),
                            "Received unauthorized Response: " + responseStr);
                }
            } else {
                // received success (200 range) response
                Object responseJSON;
                try {
                    responseJSON = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(responseStr);
                    if (!(responseJSON instanceof JSONObject)) {
                        log.error("Discovery call response is not a JSON object");
                        throw new CIBAAuthenticationEndpointException(HttpStatus.SC_BAD_REQUEST,
                                CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_BAD_REQUEST.getMessage(),
                                "Discovery call response is not a JSON object");
                    }
                } catch (net.minidev.json.parser.ParseException e) {
                    throw new CIBAAuthenticationEndpointException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                            CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_SERVER_ERROR.getMessage(),
                            "Unable to parse the response", e);
                }

                JSONObject responseData = (JSONObject) responseJSON;
                return responseData;
            }

        } catch (IOException e) {
            log.error("Exception occurred while reading request. Caused by, ", e);
        } catch (OpenBankingException e) {
            log.error("Exception occurred while generating http client. Caused by, ", e);
        }
        throw new CIBAAuthenticationEndpointException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_SERVER_ERROR.getMessage(),
                "Unexpected response received for the request. path: " +
                        request.getURI() + " response:" + responseStr);
    }

    /**
     * Initialize consent builder.
     */
    private static synchronized void initializeConsentSteps() {

        if (consentRetrievalSteps == null || consentPersistSteps == null) {
            ConsentStepsBuilder consentStepsBuilder = ConsentExtensionExporter.getConsentStepsBuilder();

            if (consentStepsBuilder != null) {
                consentRetrievalSteps = consentStepsBuilder.getConsentRetrievalSteps();
                consentPersistSteps = consentStepsBuilder.getConsentPersistSteps();
            }

            if (consentRetrievalSteps != null && !consentRetrievalSteps.isEmpty()) {
                log.info("Consent retrieval steps are not null or empty");
            } else {
                log.warn("Consent retrieval steps are null or empty");
            }
            if (consentPersistSteps != null && !consentPersistSteps.isEmpty()) {
                log.info("Consent persist steps are not null or empty");
            } else {
                log.warn("Consent persist steps are null or empty");
            }
        } else {
            log.debug("Retrieval and persist steps are available");
        }
    }

    /**
     * Persist user consent data.
     *
     * @param request        HTTP request
     * @param response       HTTP response
     * @param sessionDataKey Session Data Key
     * @param payload        Json payload
     * @throws ConsentException
     */
    private static void persistConsent(HttpServletRequest request, HttpServletResponse response,
                                       String sessionDataKey, JSONObject payload) throws ConsentException {

        ConsentData consentData = ConsentCache.getConsentDataFromCache(sessionDataKey);
        if (consentData == null) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Unable to get consent data");
        }

        if (payload == null) {
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                    "Payload unavailable", consentData.getState());
        }

        boolean approval;
        if (payload.containsKey(CIBAAuthenticationEndpointConstants.APPROVAL)) {
            try {
                if (payload.get(CIBAAuthenticationEndpointConstants.APPROVAL) instanceof Boolean) {
                    approval = (Boolean) payload.get(CIBAAuthenticationEndpointConstants.APPROVAL);
                } else {
                    approval = Boolean.parseBoolean((String) payload.get(CIBAAuthenticationEndpointConstants.APPROVAL));
                }
            } catch (ClassCastException e) {
                log.error("Error while processing consent persistence authorize", e);
                throw new ConsentException(ResponseStatus.BAD_REQUEST,
                        CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_PERSIST_INVALID_AUTHORIZE.getMessage());
            }
        } else {
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                    CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_PERSIST_APPROVAL_MANDATORY.getMessage(),
                    consentData.getState());
        }

        Map<String, String> headers = ConsentExtensionUtils.getHeaders(request);
        ConsentPersistData consentPersistData = new ConsentPersistData(payload, headers, approval, consentData);

        executePersistence(consentPersistData);

        if (!approval) {
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.ACCESS_DENIED,
                    "User denied the consent", consentData.getState());
        }

    }

    /**
     * Execute consent persistence.
     *
     * @param consentPersistData Consent Persistence data
     * @throws ConsentException
     */
    private static void executePersistence(ConsentPersistData consentPersistData) throws ConsentException {

        for (ConsentPersistStep step : consentPersistSteps) {
            if (log.isDebugEnabled()) {
                log.debug("Executing persistence step " + step.getClass().toString());
            }
            step.execute(consentPersistData);
        }
    }

    /**
     * Handles authentication request received from mobile app.
     *
     * @param request  HTTP request
     * @param response HTTP response
     * @throws CIBAAuthenticationEndpointException
     */
    public static void handleMobileResponse(HttpServletRequest request, HttpServletResponse response)
            throws CIBAAuthenticationEndpointException {

        setCIBAExtension();

        String responseJsonString;
        try {
            responseJsonString = IOUtils.toString(request.getInputStream());
        } catch (IOException e) {
            throw new CIBAAuthenticationEndpointException(HttpStatus.SC_BAD_REQUEST,
                    CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_BAD_REQUEST.getMessage(),
                    "Error in reading the request", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("CIBA authenticate call from mobile received: " + responseJsonString);
        }

        Object responseDataJSON;
        try {
            responseDataJSON = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(responseJsonString);
            if (!(responseDataJSON instanceof JSONObject)) {
                log.error("response is not a JSON object");
                throw new CIBAAuthenticationEndpointException(HttpStatus.SC_BAD_REQUEST,
                        CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_BAD_REQUEST.getMessage(),
                        "response is not a JSON object");
            }
        } catch (net.minidev.json.parser.ParseException e) {
            throw new CIBAAuthenticationEndpointException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_SERVER_ERROR.getMessage(),
                    "Unable to parse the response", e);
        }

        JSONObject responseData = (JSONObject) responseDataJSON;
        String token = responseData.getAsString(CIBAAuthenticationEndpointConstants.AUTH_RESPONSE);

        if (StringUtils.isEmpty(token)) {
            if (log.isDebugEnabled()) {
                log.debug(CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_AUTH_RESPONSE_TOKEN_NOT_FOUND);
            }
            throw new CIBAAuthenticationEndpointException(HttpStatus.SC_BAD_REQUEST,
                    CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_AUTH_RESPONSE_TOKEN_NOT_FOUND
                            .getCode(),
                    CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_AUTH_RESPONSE_TOKEN_NOT_FOUND
                            .getMessage());
        } else {
            String deviceId = getDeviceIdFromToken(token);
            String sessionDataKey = getSessionDataKeyFromToken(token, deviceId);

            if (StringUtils.isEmpty(sessionDataKey)) {
                String errorMessage = CIBAAuthenticationEndpointConstants.ErrorMessages
                                .ERROR_CODE_SESSION_DATA_KEY_NOT_FOUND + deviceId;
                if (log.isDebugEnabled()) {
                    log.debug(errorMessage);
                }

                throw new CIBAAuthenticationEndpointException(HttpStatus.SC_BAD_REQUEST,
                        CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_SESSION_DATA_KEY_NOT_FOUND
                                .getCode(),
                        errorMessage);
            } else {
                addToContext(sessionDataKey, token);

                try {
                    processAuthenticationRequest(request, response, sessionDataKey);
                } catch (AuthenticationFailedException e) {
                    throw new CIBAAuthenticationEndpointException(HttpStatus.SC_BAD_REQUEST,
                            CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_BAD_REQUEST.getMessage(),
                            "Authentication Failed", e);
                }

                response.setStatus(HttpServletResponse.SC_ACCEPTED);

                log.info("Completed processing authentication request from mobile app for session data key "
                        + sessionDataKey);

            }
        }
    }

    /**
     * Retrieve the config for CIBA consent persistence toolkit extension class for.
     */
    private static void setCIBAExtension() {

        try {
            cibaAuthenticationEndpointInterfaceTK = (CIBAAuthenticationEndpointInterface)
                    Class.forName(OpenBankingConfigParser.getInstance()
                            .getCibaServletExtension()).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            log.error("CIBA Webapp extension not found", e);
        }
    }

    /**
     * Process authentication request received from mobile app.
     *
     * @param sessionDataKey Session Data Key
     * @throws CIBAAuthenticationEndpointException
     */
    protected static void processAuthenticationRequest(HttpServletRequest request,
                                                       HttpServletResponse response, String sessionDataKey) throws
            AuthenticationFailedException, CIBAAuthenticationEndpointException {

        SessionDataCacheEntry cacheEntry = ConsentCache.getCacheEntryFromSessionDataKey(sessionDataKey);

        AuthenticatedUser user = cacheEntry.getLoggedInUser();

        PushAuthContextManager contextManager = new PushAuthContextManagerImpl();
        AuthenticationContext sessionContext = contextManager.getContext(sessionDataKey);
        AuthDataDTO authDataDTO = (AuthDataDTO) sessionContext
                .getProperty(CIBAAuthenticationEndpointConstants.CONTEXT_AUTH_DATA);

        String authResponseToken = authDataDTO.getAuthToken();
        String serverChallenge = authDataDTO.getChallenge();

        String deviceId = getDeviceIdFromToken(authResponseToken);
        String publicKey = getPublicKey(deviceId);

        PushJWTValidator validator = new PushJWTValidator();
        JWTClaimsSet claimsSet;
        try {
            claimsSet = validator.getValidatedClaimSet(authResponseToken, publicKey);
        } catch (PushAuthTokenValidationException e) {
            String errorMessage = String
                    .format("Error occurred when trying to validate the JWT signature from device: %s of user: %s.",
                            deviceId, user.toFullQualifiedUsername());
            throw new AuthenticationFailedException(errorMessage, e);
        }
        if (claimsSet != null) {
            if (validator.validateChallenge(claimsSet, serverChallenge, deviceId)) {
                String authStatus;
                String metadataJsonString;
                JSONArray accountIds;
                try {
                    authStatus =
                            validator.getClaimFromClaimSet(claimsSet,
                                    CIBAAuthenticationEndpointConstants.TOKEN_RESPONSE, deviceId);
                    metadataJsonString = (validator.getClaimFromClaimSet(claimsSet,
                            CIBAAuthenticationEndpointConstants.METADATA, deviceId));

                    Object metadataJSON = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(metadataJsonString);
                    if (!(metadataJSON instanceof JSONObject)) {
                        log.error("metadata is not a JSON object");
                        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                                "metadata is not a JSON object");
                    }
                    JSONObject metadata = (JSONObject) metadataJSON;

                    accountIds =
                            (JSONArray) metadata.get(CIBAAuthenticationEndpointConstants.METADATA_ACCOUNT_IDS);
                } catch (PushAuthTokenValidationException | net.minidev.json.parser.ParseException e) {
                    String errorMessage = "Error in getting claims from the auth response token received from device: "
                            + deviceId;
                    throw new AuthenticationFailedException(errorMessage, e);
                }

                boolean approval;
                if (authStatus.equals(CIBAAuthenticationEndpointConstants.AUTH_REQUEST_STATUS_SUCCESS)) {
                    approval = true;
                } else if (authStatus.equals(CIBAAuthenticationEndpointConstants.AUTH_REQUEST_STATUS_DENIED)) {
                    approval = false;
                } else {
                    log.error("Invalid authorization status :" + authStatus);
                    String errorMessage = "Authentication failed! Incorrect auth status " + authStatus + " for user " +
                            user.toFullQualifiedUsername();
                    throw new AuthenticationFailedException(errorMessage);
                }

                JSONObject payload = new JSONObject();
                payload.put(CIBAAuthenticationEndpointConstants.APPROVAL, approval);
                // Authorize call is skipped in consent persist call in CIBA
                payload.put(CIBAAuthenticationEndpointConstants.AUTHORIZE, false);
                payload.put(CIBAAuthenticationEndpointConstants.ACCOUNT_IDS, accountIds);

                // add TK data
                if (cibaAuthenticationEndpointInterfaceTK != null) {
                    payload = cibaAuthenticationEndpointInterfaceTK
                            .updateConsentData(payload);
                }

                persistConsent(request, response, sessionDataKey, payload);
                persistAuthorization(sessionDataKey, authStatus);
            } else {
                String errorMessage = String
                        .format("Authentication failed! JWT challenge validation for device: %s of user: %s.",
                                deviceId, user);
                throw new AuthenticationFailedException(errorMessage);
            }

        } else {
            String errorMessage = String
                    .format("Authentication failed! JWT signature is not valid for device: %s of user: %s.",
                            deviceId, user);
            throw new AuthenticationFailedException(errorMessage);
        }

        try {
            contextManager.clearContext(validator.getClaimFromClaimSet(claimsSet,
                    CIBAAuthenticationEndpointConstants.TOKEN_SESSION_DATA_KEY, deviceId));
        } catch (PushAuthTokenValidationException e) {
            String errorMessage = "Error in getting claim " +
                    CIBAAuthenticationEndpointConstants.TOKEN_SESSION_DATA_KEY + " from the auth response token " +
                    "received from device: " + deviceId;
            throw new AuthenticationFailedException(errorMessage, e);
        }
    }

    /**
     * Persist authorization response.
     *
     * @param sessionDataKey Session Data Key
     * @param authStatus     User action for the authorization request
     * @throws CIBAAuthenticationEndpointException
     */
    public static void persistAuthorization(String sessionDataKey, String authStatus)
            throws CIBAAuthenticationEndpointException {

        SessionDataCacheEntry cacheEntry = ConsentCache.getCacheEntryFromSessionDataKey(sessionDataKey);

        if (cacheEntry != null) {
            AuthenticatedUser user = cacheEntry.getLoggedInUser();
            OAuth2Parameters oAuth2Parameters = cacheEntry.getoAuth2Parameters();
            String nonce = oAuth2Parameters.getNonce();

            try {
                if (CIBAAuthenticationEndpointConstants.AUTH_REQUEST_STATUS_SUCCESS.equals(authStatus)) {
                    String authCodeKey = CibaDAOFactory.getInstance().getCibaAuthMgtDAO().getCibaAuthCodeKey(nonce);

                    // Update successful authentication.
                    CibaDAOFactory.getInstance().getCibaAuthMgtDAO()
                            .persistAuthenticationSuccess(authCodeKey, user);
                } else if (CIBAAuthenticationEndpointConstants.AUTH_REQUEST_STATUS_DENIED.equals(authStatus)) {
                    String authCodeKey = CibaDAOFactory.getInstance().getCibaAuthMgtDAO().getCibaAuthCodeKey(nonce);
                    CibaDAOFactory.getInstance().getCibaAuthMgtDAO().updateStatus(authCodeKey, AuthReqStatus.FAILED);
                } else {
                    String errorMessage = "Invalid authorization status: " + authStatus;
                    throw new CIBAAuthenticationEndpointException(HttpStatus.SC_BAD_REQUEST,
                            CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_BAD_REQUEST.getMessage(),
                            errorMessage);
                }
            } catch (CibaCoreException e) {
                String errorMessage = "Error while persisting CIBA auth status for session data key " + sessionDataKey;
                throw new CIBAAuthenticationEndpointException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_SERVER_ERROR.getMessage(),
                        errorMessage, e);
            }
        }
    }

    /**
     * Derive the Device ID from the auth response token header.
     *
     * @param token Auth response token
     * @return Device ID
     * @throws CIBAAuthenticationEndpointException if the token string fails to parse to JWT
     */
    protected static String getDeviceIdFromToken(String token) throws CIBAAuthenticationEndpointException {

        try {
            return String.valueOf(JWTParser.parse(token).getHeader().getCustomParam(
                    CIBAAuthenticationEndpointConstants.TOKEN_DEVICE_ID));
        } catch (ParseException e) {
            throw new CIBAAuthenticationEndpointException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_GET_DEVICE_ID_FAILED.getCode(),
                    CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_GET_DEVICE_ID_FAILED.getMessage(),
                    e);
        }
    }

    /**
     * Derive the SessionDataKey from the auth response token.
     *
     * @param token    Auth response token
     * @param deviceId Unique ID of the device trying to authenticate
     * @return SessionDataKey
     * @throws CIBAAuthenticationEndpointException if the auth response token fails to parse to JWT or the public key
     *                                             for the device is not retrieved or if the token is not valid
     */
    private static String getSessionDataKeyFromToken(String token, String deviceId) throws
            CIBAAuthenticationEndpointException {

        DeviceHandler deviceHandler = new DeviceHandlerImpl();
        PushJWTValidator validator = new PushJWTValidator();

        try {
            String publicKey = deviceHandler.getPublicKey(deviceId);
            JWTClaimsSet claimsSet = validator.getValidatedClaimSet(token, publicKey);
            return claimsSet.getStringClaim(CIBAAuthenticationEndpointConstants.TOKEN_SESSION_DATA_KEY);
        } catch (PushDeviceHandlerServerException | PushDeviceHandlerClientException e) {
            String errorMessage = CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_GET_PUBLIC_KEY_FAILED
                    .toString() + deviceId;
            throw new CIBAAuthenticationEndpointException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_GET_PUBLIC_KEY_FAILED.getCode(),
                    errorMessage, e);
        } catch (PushAuthTokenValidationException e) {
            String errorMessage = CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_TOKEN_VALIDATION_FAILED
                    .toString() + deviceId;
            throw new CIBAAuthenticationEndpointException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_TOKEN_VALIDATION_FAILED.getCode(),
                    errorMessage, e);
        } catch (ParseException e) {
            throw new CIBAAuthenticationEndpointException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_PARSE_JWT_FAILED.getCode(),
                    CIBAAuthenticationEndpointConstants.ErrorMessages.ERROR_CODE_PARSE_JWT_FAILED.getMessage(), e);
        }
    }

    /**
     * Add the received auth response token to the authentication context.
     *
     * @param sessionDataKey Unique key to identify the session
     * @param token          Auth response token
     */
    private static void addToContext(String sessionDataKey, String token) {

        PushAuthContextManager contextManager = new PushAuthContextManagerImpl();
        AuthenticationContext context = contextManager.getContext(sessionDataKey);

        AuthDataDTO authDataDTO = (AuthDataDTO) context
                .getProperty(CIBAAuthenticationEndpointConstants.CONTEXT_AUTH_DATA);
        authDataDTO.setAuthToken(token);
        context.setProperty(CIBAAuthenticationEndpointConstants.CONTEXT_AUTH_DATA, authDataDTO);
        contextManager.storeContext(sessionDataKey, context);
    }

    /**
     * Get the public key for the device by the device ID.
     *
     * @param deviceId Unique ID for the device
     * @return Public key string
     * @throws AuthenticationFailedException if an error occurs while getting the public key
     */
    protected static String getPublicKey(String deviceId) throws AuthenticationFailedException {

        DeviceHandler deviceHandler = new DeviceHandlerImpl();
        try {
            return deviceHandler.getPublicKey(deviceId);
        } catch (PushDeviceHandlerServerException | PushDeviceHandlerClientException e) {
            throw new AuthenticationFailedException("Error occurred when trying to get the public key for device: "
                    + deviceId + ".");
        }
    }

}
