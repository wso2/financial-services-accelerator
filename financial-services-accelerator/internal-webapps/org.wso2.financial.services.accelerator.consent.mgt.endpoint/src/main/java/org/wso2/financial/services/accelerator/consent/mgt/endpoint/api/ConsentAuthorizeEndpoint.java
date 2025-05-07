/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.endpoint.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils.ConsentCache;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils.ConsentConstants;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils.ConsentUtils;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils.PATCH;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.ConsentPersistStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.ConsentRetrievalStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.builder.ConsentStepsBuilder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentPersistData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.AuthErrorCode;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionExporter;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * ConsentAuthorizeEndpoint.
 * This specifies a REST API to be used in a code/hybrid flow consent approval.
 */
@SuppressFBWarnings("JAXRS_ENDPOINT")
// Suppressed content - Endpoints
// Suppression reason - False Positive : These endpoints are secured with access
// control
// as defined in the IS deployment.toml file
// Suppressed warning count - 2
@Path("/authorize")
public class ConsentAuthorizeEndpoint {

    private static final Log log = LogFactory.getLog(ConsentAuthorizeEndpoint.class);
    private static FinancialServicesConfigParser configParser = null;
    private static boolean isPreInitiatedConsent = false;
    private static List<ConsentPersistStep> consentPersistSteps = null;
    private static List<ConsentRetrievalStep> consentRetrievalSteps = null;
    private static final ConsentCoreServiceImpl consentCoreService = new ConsentCoreServiceImpl();

    public ConsentAuthorizeEndpoint() {

        configParser = FinancialServicesConfigParser.getInstance();
        isPreInitiatedConsent = configParser.isPreInitiatedConsent();
        initializeConsentSteps();
    }

    private static synchronized void initializeConsentSteps() {

        if (consentRetrievalSteps != null && consentPersistSteps != null) {
            log.info("Retrieval and persist steps are already initialized.");
            return;
        }
        ConsentStepsBuilder consentStepsBuilder = ConsentExtensionExporter.getConsentStepsBuilder();

        if (consentStepsBuilder != null) {
            consentRetrievalSteps = consentStepsBuilder.getConsentRetrievalSteps();
            consentPersistSteps = consentStepsBuilder.getConsentPersistSteps();
        }

        if (consentRetrievalSteps != null && !consentRetrievalSteps.isEmpty()) {
            log.info("Consent retrieval steps successfully initialized.");
        } else {
            log.warn("Consent retrieval steps have not been initialized.");
        }
        if (consentPersistSteps != null && !consentPersistSteps.isEmpty()) {
            log.info("Consent persist steps successfully initialized.");
        } else {
            log.warn("Consent persist steps have not been initialized.");
        }
    }

    /**
     * Retrieve data for consent page.
     */
    @GET
    @Path("/retrieve/{session-data-key}")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json; charset=utf-8"})
    public Response retrieve(@Context HttpServletRequest request, @Context HttpServletResponse response,
                             @PathParam("session-data-key") String sessionDataKey) throws ConsentException,
            ConsentManagementException, UserStoreException {

        String loggedInUser;
        String app;
        String spQueryParams;
        String scopeString;

        SessionDataCacheEntry cacheEntry = ConsentCache.getCacheEntryFromSessionDataKey(sessionDataKey);
        OAuth2Parameters oAuth2Parameters = cacheEntry.getoAuth2Parameters();

        // Extracting client ID for regulatory identification and redirect URI for error
        // redirects
        String clientId = oAuth2Parameters.getClientId();
        String state = oAuth2Parameters.getState();
        URI redirectURI;
        try {
            redirectURI = new URI(oAuth2Parameters.getRedirectURI());
        } catch (URISyntaxException e) {
            // Unlikely to happen. In case it happens, error response is sent
            throw new ConsentException(null, AuthErrorCode.INVALID_REQUEST,
                    "Invalid redirect URI", state);
        }

        Map<String, Serializable> sensitiveDataMap = ConsentUtils.getSensitiveDataWithConsentKey(sessionDataKey);

        if ("false".equals(sensitiveDataMap.get(ConsentExtensionConstants.IS_ERROR))) {
            String loggedInUserId = (String) sensitiveDataMap.get("loggedInUser");
            loggedInUser = FinancialServicesUtils.resolveUsernameFromUserId(loggedInUserId);
            app = (String) sensitiveDataMap.get("application");
            spQueryParams = (String) sensitiveDataMap.get("spQueryParams");
            scopeString = (String) sensitiveDataMap.get("scope");
            if (!scopeString.contains("openid")) {
                String[] scopes = cacheEntry.getParamMap().get("scope");
                if (scopes != null && scopes.length != 0 && scopes[0].contains("openid")) {
                    scopeString = scopes[0];
                }
            }
            // Add request object as an SPQueryParam for PAR requests. Used in consent retrieval step.
            if (StringUtils.isNotBlank(spQueryParams) && spQueryParams.contains("redirect_uri=")) {
                Map<String, String[]> paramMap = cacheEntry.getParamMap();
                String[] requestParams = paramMap != null ? paramMap.get("request") : null;

                if (requestParams != null && requestParams.length > 0 && requestParams[0] != null) {
                    String requestObject = requestParams[0];

                    if (!spQueryParams.endsWith("&")) {
                        spQueryParams += "&";
                    }
                    spQueryParams += "request=" + requestObject;
                }
            }
        } else {
            String isError = (String) sensitiveDataMap.get(ConsentExtensionConstants.IS_ERROR);
            // Have to throw standard error because cannot access redirect URI with this
            // error
            log.error(String.format("Error while getting endpoint parameters. %s",
                    isError.replaceAll("\n\r", "")));
            throw new ConsentException(redirectURI, AuthErrorCode.SERVER_ERROR,
                    ConsentConstants.ERROR_SERVER_ERROR, state);
        }
        JSONObject jsonObject = new JSONObject();
        ConsentData consentData = new ConsentData(sessionDataKey, loggedInUser, spQueryParams, scopeString, app,
                ConsentUtils.getHeaders(request));
        consentData.setSensitiveDataMap(sensitiveDataMap);
        consentData.setRedirectURI(redirectURI);

        if (clientId == null) {
            log.error("Client Id not available");
            // Unlikely error. Included just in case.
            throw new ConsentException(redirectURI, AuthErrorCode.INVALID_REQUEST,
                    "Client Id not available", state);
        }
        consentData.setClientId(clientId);
        consentData.setState(state);

        try {
            consentData.setRegulatory(FinancialServicesUtils.isRegulatoryApp(clientId));
        } catch (RequestObjectException e) {
            log.error("Error while getting regulatory data", e);
            throw new ConsentException(redirectURI, AuthErrorCode.SERVER_ERROR,
                    "Error while obtaining regulatory data", state);
        }

        executeRetrieval(consentData, jsonObject);
        if (isPreInitiatedConsent && consentData.getType() == null) {
            log.error(ConsentConstants.ERROR_NO_CONSENT_TYPE);
            throw new ConsentException(redirectURI, AuthErrorCode.SERVER_ERROR,
                    ConsentConstants.ERROR_SERVER_ERROR, state);
        }
        if (consentData.getApplication() == null) {
            log.error(ConsentConstants.ERROR_NO_APP_DATA);
            throw new ConsentException(redirectURI, AuthErrorCode.SERVER_ERROR,
                    ConsentConstants.ERROR_SERVER_ERROR, state);
        }
        ConsentUtils.setCommonDataToResponse(consentData, jsonObject);
        Gson gson = new Gson();
        String consent = gson.toJson(consentData);
        Map<String, String> authorizeData = new HashMap<>();
        authorizeData.put(consentData.getSessionDataKey(), consent);
        ConsentCache.addConsentDataToCache(sessionDataKey, consentData);
        if (ConsentConstants.STORE_CONSENT) {
            if (consentCoreService.getConsentAttributesByName(sessionDataKey).isEmpty()) {
                consentCoreService.storeConsentAttributes(consentData.getConsentId(), authorizeData);
            }
        }
        return Response.ok(jsonObject.toString(), MediaType.APPLICATION_JSON).build();
    }

    /**
     * Persist user consent data.
     */
    @PATCH
    @Path("/persist/{session-data-key}")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    public Response persist(@Context HttpServletRequest request, @Context HttpServletResponse response,
                            @PathParam("session-data-key") String sessionDataKey,
                            @QueryParam("authorize") String authorize)
            throws ConsentException, ConsentManagementException, URISyntaxException {

        ConsentData consentData = ConsentCache.getConsentDataFromCache(sessionDataKey);
        URI location;
        try {
            if (consentData == null) {
                if (ConsentConstants.STORE_CONSENT) {
                    Map<String, String> consentDetailsMap = consentCoreService
                            .getConsentAttributesByName(sessionDataKey);
                    if (consentDetailsMap.isEmpty()) {
                        throw new ConsentException(consentData.getRedirectURI(),
                                AuthErrorCode.SERVER_ERROR,
                                "Unable to get consent data", consentData.getState());
                    }
                    Set<String> keys = consentDetailsMap.keySet();
                    String consentId = new ArrayList<>(keys).get(0);
                    JsonObject consentDetails = JsonParser.parseString(consentDetailsMap.get(consentId))
                            .getAsJsonObject();
                    consentData = ConsentUtils.getConsentDataFromAttributes(consentDetails, sessionDataKey);

                    if (consentDetailsMap.isEmpty()) {
                        throw new ConsentException(consentData.getRedirectURI(),
                                AuthErrorCode.SERVER_ERROR, "Unable to get consent data",
                                consentData.getState());
                    }
                } else {
                    throw new ConsentException(consentData.getRedirectURI(),
                            AuthErrorCode.SERVER_ERROR, "Unable to get consent data",
                            consentData.getState());
                }
            }
            JSONObject payload;
            try {
                payload = ConsentUtils.getJSONObjectPayload(request);
            } catch (ConsentException e) {
                throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                        ConsentConstants.ERROR_NO_DATA_IN_SESSION_CACHE, consentData.getState());
            }
            Map<String, String> headers = ConsentUtils.getHeaders(request);

            if (payload == null) {
                throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.INVALID_REQUEST,
                        "Payload unavailable", consentData.getState());
            }

            boolean approval;
            if (payload.has(ConsentExtensionConstants.APPROVAL)) {
                try {
                    if (payload.get(ConsentExtensionConstants.APPROVAL) instanceof Boolean) {
                        approval = (Boolean) payload.get(ConsentExtensionConstants.APPROVAL);
                    } else {
                        approval = Boolean.parseBoolean((String) payload.get(ConsentExtensionConstants.APPROVAL));
                    }
                } catch (ClassCastException e) {
                    log.error("Error while processing consent persistence approval", e);
                    throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.INVALID_REQUEST,
                            ConsentConstants.ERROR_PERSIST_INVALID_APPROVAL, consentData.getState());
                }

            } else {
                throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.INVALID_REQUEST,
                        ConsentConstants.ERROR_PERSIST_APPROVAL_MANDATORY, consentData.getState());
            }

            ConsentPersistData consentPersistData = new ConsentPersistData(payload, headers, approval, consentData);

            if (payload.has(ConsentExtensionConstants.COOKIES)) {
                Object cookies = payload.get(ConsentExtensionConstants.COOKIES);
                if (cookies instanceof Map) {
                    consentPersistData.setBrowserCookies((Map<String, String>) cookies);
                } else {
                    JSONObject cookiesJson = (JSONObject) cookies;
                    Map<String, String> cookiesMap = new HashMap<>();
                    cookiesJson.keySet().forEach(key -> {
                        String value = cookiesJson.getString(key);
                        cookiesMap.put(key, value);
                    });
                    consentPersistData.setBrowserCookies(cookiesMap);
                }
            }

            executePersistence(consentPersistData);

            if (!approval) {
                throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.ACCESS_DENIED,
                        "User denied the consent", consentData.getState());
            } else if (authorize != null && !StringUtils.equals("true", authorize)) {
                if (StringUtils.equals(StringUtils.EMPTY, authorize) || !StringUtils.equals("false", authorize)) {
                    /*
                     * "authorize" parameter comes as an empty string only when a value was not
                     * defined for the parameter in
                     * the URL. Throwing an error since a value must be present for the query
                     * parameter. Also, the value should
                     * only be true or false
                     */
                    throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.INVALID_REQUEST,
                            ConsentConstants.ERROR_INVALID_VALUE_FOR_AUTHORIZE_PARAM, consentData.getState());
                } else {
                    return Response.ok().build();
                }
            } else {
                location = ConsentUtils.authorizeRequest(Boolean.toString(consentPersistData.getApproval()),
                        consentPersistData.getBrowserCookies(), consentData);
            }
        } finally {
            if (ConsentConstants.STORE_CONSENT && consentData != null) {
                // remove all session data related to the consent from consent attributes
                ArrayList<String> keysToDelete = new ArrayList<>();
                Map<String, String> consentAttributes = consentCoreService
                        .getConsentAttributes(consentData.getConsentId()).getConsentAttributes();
                consentAttributes.forEach((key, value) -> {
                    if (ConsentUtils.isValidJson(value) && value.contains("sessionDataKey")) {
                        keysToDelete.add(key);
                    }
                });
                consentCoreService.deleteConsentAttributes(consentData.getConsentId(),
                        keysToDelete);
            }
        }

        return Response.status(ConsentExtensionConstants.STATUS_FOUND).location(location).build();
    }

    /**
     * Method to execute retrieval steps.
     *
     * @param consentData Consent data
     * @param jsonObject  JSON object
     * @throws ConsentException if there is an error in executing the retrieval
     *                          steps
     */
    private void executeRetrieval(ConsentData consentData, JSONObject jsonObject) {

        for (ConsentRetrievalStep step : consentRetrievalSteps) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Executing retrieval step %s",
                        step.getClass().toString().replaceAll("\n\r", "")));
            }
            step.execute(consentData, jsonObject);
        }
    }

    /**
     * Method to execute persist steps.
     *
     * @param consentPersistData Consent Persist data
     * @throws ConsentException if there is an error in executing the persist steps
     */
    private void executePersistence(ConsentPersistData consentPersistData) throws ConsentException {

        for (ConsentPersistStep step : consentPersistSteps) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Executing persistence step %s",
                        step.getClass().toString().replaceAll("\n\r", "")));
            }
            step.execute(consentPersistData);
        }
    }
}
