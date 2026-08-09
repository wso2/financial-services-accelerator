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

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.consent.endpoint.util.ConsentUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionExporter;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.validate.builder.ConsentValidateBuilder;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidator;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;
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
 * This specifies a RESTful API for consent validation to be used at consent enforcement of resource
 * retrieval/submission requests.
 */
@SuppressFBWarnings("JAXRS_ENDPOINT")
// Suppressed content - Endpoints
// Suppression reason - False Positive : These endpoints are secured with access control
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
            log.info("Consent validator " + consentValidator.getClass().getName() + "initialized");
        }

        if (consentValidator != null) {
            log.info("Consent validator " + consentValidator.getClass().getName() + "initialized");
        } else {
            log.warn("Consent validator is null");
        }
    }

    /**
     * Validate by sending consent data.
     */
    @POST
    @Path("/")
    @Consumes({"application/jwt; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    public Response validate(@Context HttpServletRequest request, @Context HttpServletResponse response) {

        String payload = ConsentUtils.getStringPayload(request);
        JSONObject requestData;
        Object requestDataObj;

        if (IdentityCommonUtil.getConsentJWTPayloadValidatorConfigEnabled()) {
            try {
                IdentityCommonUtil.validateJWTSignatureWithPublicKey(payload, requestSignatureAlias);
                requestData = JWTUtils.decodeRequestJWT(payload, "body");
            } catch (OpenBankingException e) {
                log.error("Error while validating JWT signature", e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            } catch (ParseException e) {
                log.error("Error while decoding validation JWT", e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } else {
            try {
                requestDataObj = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(payload);
            } catch (net.minidev.json.parser.ParseException e) {
                log.error("Unable to parse the request payload", e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Unable to parse the request payload");
            }
            if (!(requestDataObj instanceof JSONObject)) {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Payload is not a JSON object");
            } else {
                requestData = (JSONObject) requestDataObj;
            }
        }

        JSONObject requestHeaders = (JSONObject) requestData.get("headers");
        Set<String> headerNames = requestHeaders.keySet();
        Iterator<String> headersIterator = headerNames.iterator();
        TreeMap<String, String> headersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        while (headersIterator.hasNext()) {
            String headerName = headersIterator.next();
            headersMap.put(headerName, requestHeaders.getAsString(headerName));
        }

        JSONObject requestPayload = (JSONObject) requestData.get("body");
        String requestPath = requestData.getAsString("electedResource");
        String consentId = requestData.getAsString("consentId");
        String userId = requestData.getAsString("userId");
        String clientId = null;
        if (requestData.containsKey("clientId")) {
            clientId = requestData.getAsString("clientId");
        }
        Map<String, String> resourceParams = (Map<String, String>) requestData.get("resourceParams");

        if (consentId == null) {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent Id is mandatory for consent validation");
        }

        try {
            //Adding query parameters to the resource map
            resourceParams = ConsentUtils.addQueryParametersToResourceParamMap(resourceParams);
        } catch (URISyntaxException e) {
            log.error("Error while extracting query parameters", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Error while extracting query parameters");
        }

        ConsentValidateData consentValidateData = new ConsentValidateData(requestHeaders, requestPayload,
                requestPath, consentId, userId, clientId, resourceParams, headersMap);

        try {
            DetailedConsentResource consentResource = consentCoreService.getDetailedConsent(consentId);
            consentValidateData.setComprehensiveConsent(consentResource);
        } catch (ConsentManagementException e) {
            log.error("Exception while getting consent", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Exception while getting consent");
        }

        ConsentValidationResult validationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateData, validationResult);

        JSONObject information = ConsentExtensionUtils.detailedConsentToJSON(
                consentValidateData.getComprehensiveConsent());
        information.put("additionalConsentInfo", validationResult.getConsentInformation());
        validationResult.setConsentInformation(information);

        JSONObject responsePayload;
        try {
            responsePayload = validationResult.generatePayload();
            responsePayload.appendField("consentInformation",
                    IdentityCommonUtil.signJWTWithDefaultKey(validationResult.getConsentInformation().toJSONString()));
        } catch (Exception e) {
            log.error("Error occurred while getting private key", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Error while getting private key");
        }
        return Response.status(HttpServletResponse.SC_OK).entity(responsePayload).build();
    }
}
