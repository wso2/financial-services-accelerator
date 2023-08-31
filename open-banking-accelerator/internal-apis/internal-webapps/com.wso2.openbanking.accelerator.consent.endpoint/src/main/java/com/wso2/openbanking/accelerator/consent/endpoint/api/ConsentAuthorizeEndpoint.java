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

package com.wso2.openbanking.accelerator.consent.endpoint.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.endpoint.util.ConsentConstants;
import com.wso2.openbanking.accelerator.consent.endpoint.util.ConsentUtils;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.builder.ConsentStepsBuilder;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentRetrievalStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.AuthErrorCode;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentCache;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionExporter;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.jaxrs.PATCH;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;

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
 * This specifies a RESTful API to be used in a code/hybrid flow consent approval.
 */
@SuppressFBWarnings("JAXRS_ENDPOINT")
// Suppressed content - Endpoints
// Suppression reason - False Positive : These endpoints are secured with access control
// as defined in the IS deployment.toml file
// Suppressed warning count - 2
@Path("/authorize")
public class ConsentAuthorizeEndpoint {

    private static final Log log = LogFactory.getLog(ConsentAuthorizeEndpoint.class);

    private static final String ERROR_PERSIST_INVALID_APPROVAL = "Invalid value for approval. Should be true/false";
    private static final String ERROR_PERSIST_APPROVAL_MANDATORY = "Mandatory body parameter approval is unavailable";
    private static final String ERROR_NO_TYPE_AND_APP_DATA = "Type and application data is unavailable";
    private static final String ERROR_SERVER_ERROR = "Internal server error";
    private static final String ERROR_NO_DATA_IN_SESSION_CACHE =
            "Data unavailable in session cache corresponding to the key provided";
    private static final String  ERROR_CONSENT_DATA_RETRIEVAL = "Error while retrieving data consent data";
    private static final String ERROR_INVALID_VALUE_FOR_AUTHORIZE_PARAM = "\"authorize\" parameter is not defined " +
            "properly or invalid";
    private static final int STATUS_FOUND = 302;
    private static final String IS_ERROR = "isError";
    private static final String APPROVAL = "approval";
    private static final String COOKIES = "cookies";
    private static List<ConsentPersistStep> consentPersistSteps = null;
    private static List<ConsentRetrievalStep> consentRetrievalSteps = null;
    private static ConsentCoreServiceImpl consentCoreService = new ConsentCoreServiceImpl();
    private static final String preserveConsent = (String) OpenBankingConfigParser.getInstance().getConfiguration()
            .get(ConsentConstants.PRESERVE_CONSENT);
    private static final boolean storeConsent = preserveConsent == null ? false : Boolean.parseBoolean(preserveConsent);

    public ConsentAuthorizeEndpoint() {
        initializeConsentSteps();
    }

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
     * Retrieve data for consent page.
     */
    @GET
    @Path("/retrieve/{session-data-key}")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json; charset=utf-8"})
    public Response retrieve(@Context HttpServletRequest request, @Context HttpServletResponse response,
                             @PathParam("session-data-key") String sessionDataKey) throws ConsentException,
            ConsentManagementException {

        String loggedInUser;
        String app;
        String spQueryParams;
        String scopeString;

        SessionDataCacheEntry cacheEntry = ConsentCache.getCacheEntryFromSessionDataKey(sessionDataKey);
        OAuth2Parameters oAuth2Parameters = cacheEntry.getoAuth2Parameters();
        URI redirectURI;
        try {
            redirectURI = new URI(oAuth2Parameters.getRedirectURI());
        } catch (URISyntaxException e) {
            //Unlikely to happen. In case it happens, error response is sent
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Invalid redirect URI");
        }
        //Extracting client ID for regulatory identification and redirect URI for error redirects
        String clientId = oAuth2Parameters.getClientId();
        String state = oAuth2Parameters.getState();

        Map<String, Serializable> sensitiveDataMap =
                ConsentExtensionUtils.getSensitiveDataWithConsentKey(sessionDataKey);

        if ("false".equals(sensitiveDataMap.get(IS_ERROR))) {
            loggedInUser = (String) sensitiveDataMap.get("loggedInUser");
            app = (String) sensitiveDataMap.get("application");
            spQueryParams = (String) sensitiveDataMap.get("spQueryParams");
            scopeString = (String) sensitiveDataMap.get("scope");
            if (!scopeString.contains("openid")) {
                String[] scopes = cacheEntry.getParamMap().get("scope");
                if (scopes != null && scopes.length != 0 && scopes[0].contains("openid")) {
                    scopeString = scopes[0];
                }
            }
        } else {
            String isError = (String) sensitiveDataMap.get(IS_ERROR);
            //Have to throw standard error because cannot access redirect URI with this error
            log.error("Error while getting endpoint parameters. " + isError);
            throw new ConsentException(redirectURI, AuthErrorCode.SERVER_ERROR, ERROR_SERVER_ERROR, state);
        }

        JSONObject jsonObject = new JSONObject();
        ConsentData consentData = new ConsentData(sessionDataKey, loggedInUser, spQueryParams, scopeString, app,
                ConsentExtensionUtils.getHeaders(request));
        consentData.setSensitiveDataMap(sensitiveDataMap);
        consentData.setRedirectURI(redirectURI);

        if (clientId == null) {
            log.error("Client Id not available");
            //Unlikely error. Included just in case.
            throw new ConsentException(redirectURI, AuthErrorCode.SERVER_ERROR, ERROR_SERVER_ERROR, state);
        }
        consentData.setClientId(clientId);
        consentData.setState(state);

        try {
            consentData.setRegulatory(IdentityCommonUtil.getRegulatoryFromSPMetaData(clientId));
        } catch (OpenBankingException e) {
            log.error("Error while getting regulatory data", e);
            throw new ConsentException(redirectURI, AuthErrorCode.SERVER_ERROR, "Error while obtaining regulatory data",
                    state);
        }

        executeRetrieval(consentData, jsonObject);
        if (consentData.getType() == null || consentData.getApplication() == null) {
            log.error(ERROR_NO_TYPE_AND_APP_DATA);
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                    ERROR_SERVER_ERROR, state);
        }
        ConsentExtensionUtils.setCommonDataToResponse(consentData, jsonObject);
        Gson gson = new Gson();
        String consent = gson.toJson(consentData);
        Map<String, String> authorizeData = new HashMap<>();
        authorizeData.put(consentData.getSessionDataKey(), consent);
        ConsentCache.addConsentDataToCache(sessionDataKey, consentData);
        if (storeConsent) {
            if (consentCoreService.getConsentAttributesByName(sessionDataKey).isEmpty()) {
                consentCoreService.storeConsentAttributes(consentData.getConsentId(), authorizeData);
            }
        }
        return Response.ok(jsonObject.toJSONString(), MediaType.APPLICATION_JSON).build();
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
                if (storeConsent) {
                    Map<String, String> consentDetailsMap =
                            consentCoreService.getConsentAttributesByName(sessionDataKey);
                    if (consentDetailsMap.isEmpty()) {
                        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Unable to get consent data");
                    }
                    Set<String> keys = consentDetailsMap.keySet();
                    String consentId = new ArrayList<>(keys).get(0);
                    JsonObject consentDetails = new JsonParser()
                            .parse(consentDetailsMap.get(consentId)).getAsJsonObject();
                    consentData = ConsentUtils.getConsentDataFromAttributes(consentDetails, sessionDataKey);

                    if (consentDetailsMap.isEmpty()) {
                        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Unable to get consent data");
                    }
                } else {
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Unable to get consent data");
                }
            }
            JSONObject payload;
            try {
                payload = ConsentUtils.getJSONObjectPayload(request);
            } catch (ConsentException e) {
                throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                        ERROR_NO_DATA_IN_SESSION_CACHE, consentData.getState());
            }
            Map<String, String> headers = ConsentExtensionUtils.getHeaders(request);

            if (payload == null) {
                throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                        "Payload unavailable", consentData.getState());
            }

            boolean approval;
            if (payload.containsKey(APPROVAL)) {
                try {
                    if (payload.get(APPROVAL) instanceof Boolean) {
                        approval = (Boolean) payload.get(APPROVAL);
                    } else {
                        approval = Boolean.parseBoolean((String) payload.get(APPROVAL));
                    }
                } catch (ClassCastException e) {
                    log.error("Error while processing consent persistence approval", e);
                    throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                            ERROR_PERSIST_INVALID_APPROVAL, consentData.getState());
                }
            } else {
                throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                        ERROR_PERSIST_APPROVAL_MANDATORY, consentData.getState());
            }

            ConsentPersistData consentPersistData = new ConsentPersistData(payload, headers, approval, consentData);

            if (payload.containsKey(COOKIES)) {
                consentPersistData.setBrowserCookies((Map<String, String>) payload.get(COOKIES));
            }

            executePersistence(consentPersistData);

            if (!approval) {
                throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.ACCESS_DENIED,
                        "User denied the consent", consentData.getState());
            } else if (authorize != null && !StringUtils.equals("true", authorize)) {
                if (StringUtils.equals(StringUtils.EMPTY, authorize) || !StringUtils.equals("false", authorize)) {
            /* "authorize" parameter comes as an empty string only when a value was not defined for the parameter in
               the URL. Throwing an error since a value must be present for the query parameter. Also, the value should
               only be true or false */
                    throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.INVALID_REQUEST,
                            ERROR_INVALID_VALUE_FOR_AUTHORIZE_PARAM, consentData.getState());
                } else {
                    return Response.ok().build();
                }
            } else {
                location = ConsentUtils.authorizeRequest(Boolean.toString(consentPersistData.getApproval())
                        , consentPersistData.getBrowserCookies(), consentData);
            }
        } finally {
            if (storeConsent && consentData != null) {
                // remove all session data related to the consent from consent attributes
                ArrayList<String> keysToDelete = new ArrayList<>();

                Map<String, String> consentAttributes = consentCoreService.
                        getConsentAttributes(consentData.getConsentId()).getConsentAttributes();

                consentAttributes.forEach((key, value) -> {
                    if (JSONValue.isValidJson(value) && value.contains("sessionDataKey")) {
                        keysToDelete.add(key);
                    }
                });

                consentCoreService.deleteConsentAttributes(consentData.getConsentId(),
                        keysToDelete);
            }
        }

        return Response.status(STATUS_FOUND).location(location).build();
    }

    private void executeRetrieval(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        for (ConsentRetrievalStep step : consentRetrievalSteps) {
            if (log.isDebugEnabled()) {
                log.debug("Executing retrieval step " + step.getClass().toString());
            }
            step.execute(consentData, jsonObject);
        }
    }

    private void executePersistence(ConsentPersistData consentPersistData) throws ConsentException {

        for (ConsentPersistStep step : consentPersistSteps) {
            if (log.isDebugEnabled()) {
                log.debug("Executing persistence step " + step.getClass().toString());
            }
            step.execute(consentPersistData);
        }
    }
}
