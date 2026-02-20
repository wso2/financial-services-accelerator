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

package org.wso2.financial.services.accelerator.consent.mgt.endpoint.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.util.JWTUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils.ConsentUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionExporter;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.ConsentValidator;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.builder.ConsentValidateBuilder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.model.ConsentValidateData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.model.ConsentValidationResult;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * ConsentValidationEndpoint.
 *
 * This specifies a REST API for consent validation to be used at consent
 * enforcement of resource
 * retrieval/submission requests.
 */
@SuppressFBWarnings("JAXRS_ENDPOINT")
// Suppressed content - Endpoints
// Suppression reason - False Positive : These endpoints are secured with access
// control
// as defined in the IS deployment.toml file
// Suppressed warning count - 1
@Path("/validate")
public class ConsentValidationEndpoint {

    private static final Log log = LogFactory.getLog(ConsentValidationEndpoint.class);
    private static final ConsentCoreServiceImpl consentCoreService = new ConsentCoreServiceImpl();
    private static ConsentValidator consentValidator = null;
    private static String requestSignatureAlias;

    public ConsentValidationEndpoint() {

        if (consentValidator == null) {
            initializeConsentValidator();
        }
    }

    private static void initializeConsentValidator() {

        ConsentValidateBuilder consentValidateBuilder = ConsentExtensionExporter.getConsentValidateBuilder();

        if (consentValidateBuilder != null) {
            consentValidator = consentValidateBuilder.getConsentValidator();
            requestSignatureAlias = consentValidateBuilder.getRequestSignatureAlias();
            log.info(String.format("Consent validator %s initialized",
                    consentValidator.getClass().getName().replaceAll("\n\r", "")));
        }

        if (consentValidator == null) {
            log.warn("Consent validator is null");
        }
    }

    /**
     * Validate by sending consent data.
     */
    @POST
    @Path("/validate")
    @Consumes({ "application/jwt; charset=utf-8" })
    @Produces({ "application/json; charset=utf-8" })
    public Response validate(@Context HttpServletRequest request, @Context HttpServletResponse response) {

        String payload = ConsentUtils.getStringPayload(request);
        JSONObject requestData = new JSONObject();

        if (ConsentUtils.getConsentJWTPayloadValidatorConfigEnabled()) {
            try {
                JWTUtils.validateJWTSignatureWithPublicKey(payload, requestSignatureAlias);
                String decodedRequest = JWTUtils.decodeRequestJWT(payload, ConsentExtensionConstants.BODY) != null
                        ? JWTUtils.decodeRequestJWT(payload, ConsentExtensionConstants.BODY)
                        : null;
                if (Objects.nonNull(decodedRequest)) {
                    requestData = new JSONObject(decodedRequest);
                } else {
                    log.error("Error while decoding the JWT request payload");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST,
                            "Error while decoding the JWT request payload");
                }
            } catch (ConsentManagementException e) {
                log.error("Error while validating JWT signature", e);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, e.getMessage());
            } catch (JSONException | ParseException e) {
                log.error("Error while decoding validation JWT", e);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, e.getMessage());
            }
        } else {
            try {
                requestData = new JSONObject(payload);
            } catch (JSONException e) {
                log.error("Unable to parse the request payload", e);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Unable to parse the request payload");
            }
        }

        JSONObject requestHeaders = (JSONObject) requestData.get(ConsentExtensionConstants.HEADERS);
        Set<String> headerNames = requestHeaders.keySet();
        Iterator<String> headersIterator = headerNames.iterator();
        TreeMap<String, String> headersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        while (headersIterator.hasNext()) {
            String headerName = headersIterator.next();
            headersMap.put(headerName, requestHeaders.getString(headerName));
        }

        JSONObject requestPayload = requestData.has(ConsentExtensionConstants.BODY)
                ? requestData.getJSONObject(ConsentExtensionConstants.BODY)
                : null;
        String requestPath = requestData.getString(ConsentExtensionConstants.ELECTED_RESOURCE);
        String consentId = requestData.getString(ConsentExtensionConstants.CC_CONSENT_ID);
        String userId = requestData.getString(ConsentExtensionConstants.USER_ID);
        String clientId = requestData.getString(ConsentExtensionConstants.CLIENT_ID);

        JSONObject resourceParams = requestData.getJSONObject(ConsentExtensionConstants.RESOURCE_PARAMS);

        if (consentId == null) {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent Id is mandatory for consent validation");
        }

        Map<String, String> resourceParamsMap = new HashMap<>();
        try {
            // Adding query parameters to the resource map
            resourceParamsMap = ConsentUtils.addQueryParametersToResourceParamMap(resourceParams);
        } catch (URISyntaxException e) {
            log.error("Error while extracting query parameters", e);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Error while extracting query parameters");
        }

        ConsentValidateData consentValidateData = new ConsentValidateData(requestHeaders, requestPayload,
                requestPath, consentId, userId, clientId, resourceParamsMap, headersMap);

        try {
            DetailedConsentResource consentResource = consentCoreService.getDetailedConsent(consentId);
            consentValidateData.setComprehensiveConsent(consentResource);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Successfully retrieved consent details for consent ID: %s",
                        consentId.replaceAll("\n\r", "")));
            }
        } catch (ConsentManagementException e) {
            log.error("Consent details not found for the given consent id", e);
            throw new ConsentException(ResponseStatus.BAD_REQUEST,
                    "Consent details not found for the given consent id");
        }

        ConsentValidationResult validationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateData, validationResult);

        JSONObject information = ConsentUtils.detailedConsentToJSON(
                consentValidateData.getComprehensiveConsent());
        information.put(ConsentExtensionConstants.ADDITIONAL_CONSENT_INFO, validationResult.getConsentInformation());
        validationResult.setConsentInformation(information);

        JSONObject responsePayload;
        try {
            responsePayload = validationResult.generatePayload();
            String consentInfoPayload = validationResult.getConsentInformation().toString();
            if (ConsentUtils.isResponsePayloadSigningEnabled()) {
                responsePayload.put(ConsentExtensionConstants.CONSENT_INFO,
                        ConsentUtils.signJWTWithDefaultKey(consentInfoPayload));
            } else {
                responsePayload.put(ConsentExtensionConstants.CONSENT_INFO, consentInfoPayload);
            }
        } catch (Exception e) {
            log.error("Error occurred while getting private key", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Error while getting private key");
        }
        return Response.status(HttpServletResponse.SC_OK).entity(responsePayload.toString()).build();
    }
}
