/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.apim.mediation.policies.dynamic.client.registration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONObject;
import org.wso2.financial.services.apim.mediation.policies.dynamic.client.registration.util.DCRConstants;
import org.wso2.financial.services.apim.mediation.policies.dynamic.client.registration.util.DCRHandlingException;
import org.wso2.financial.services.apim.mediation.policies.dynamic.client.registration.util.DCRUtil;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

/**
 * Mediator for dynamic client registration.
 */
public class DynamicClientRegistrationRequestMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(DynamicClientRegistrationRequestMediator.class);

    private boolean validateRequestJWT;
    private String jwksEndpointName;
    private String clientNameAttributeName;
    private boolean useSoftwareIdAsAppName;
    private int jwksEndpointTimeout;

    @Override
    public boolean mediate(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        Map<String, Object> headers = (Map<String, Object>)
                axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        String httpMethod = (String) messageContext.getProperty(DCRConstants.HTTP_METHOD);

        // For DCR retrieval, update and delete requests, check whether the token is bound to the correct client id
        if (HttpMethod.GET.equals(httpMethod) || HttpMethod.PUT.equals(httpMethod) ||
                HttpMethod.DELETE.equals(httpMethod)) {
            log.debug("Checking whether the token is bound to the correct client id");
            boolean isValid =  validateClientId(messageContext, headers);
            if (!isValid) {
                log.error("Client ID validation failed. Returning error response.");
                return false;
            }
        }

        // Check if the HTTP method is POST or PUT to process DCR JWT validation and request alteration
        if (HttpMethod.POST.equals(httpMethod) || HttpMethod.PUT.equals(httpMethod)) {
            try {
                log.debug("Processing DCR JWT validation and request alteration.");
                String contentType = headers.containsKey(DCRConstants.CONTENT_TYPE_TAG) ?
                        headers.get(DCRConstants.CONTENT_TYPE_TAG).toString() : null;
                Optional<String> requestPayload = DCRUtil.buildMessagePayloadFromMessageContext(axis2MessageContext,
                        contentType);
                if (requestPayload.isPresent()) {
                    if (Objects.requireNonNull(contentType).contains(DCRConstants.JWT_CONTENT_TYPE)) {
                        // Perform JWT Validation and Payload conversion and modification to JWT payloads
                        validateAndModifyPayload(messageContext, requestPayload.get(), headers);
                    } else {
                        // Perform Payload conversion and modification to JSON payloads
                        modifyJsonPayload(messageContext, requestPayload.get(), headers);
                    }
                } else {
                    log.error("Request payload is empty or not present.");
                    DCRUtil.returnSynapseHandlerJSONError(messageContext, DCRConstants.BAD_REQUEST_CODE,
                            DCRUtil.getErrorResponse(DCRConstants.INVALID_REQUEST,
                                    "Request payload is empty or not present."));
                    return false;
                }

            } catch (DCRHandlingException e) {
                log.error("Error occurred while processing DCR request", e);
                DCRUtil.returnSynapseHandlerJSONError(messageContext, e.getHttpCode(),
                        DCRUtil.getErrorResponse(e.getErrorCode(), e.getMessage()));
                return false;
            }
        } else {
            log.debug("HTTP method is not POST or PUT. Skipping DCR JWT validation and request alteration.");
        }

        return true;
    }

    public boolean isValidateRequestJWT() {
        return validateRequestJWT;
    }

    public void setValidateRequestJWT(boolean validateRequestJWT) {
        this.validateRequestJWT = validateRequestJWT;
    }

    public String getJwksEndpointName() {
        return jwksEndpointName;
    }

    public void setJwksEndpointName(String jwksEndpointName) {
        this.jwksEndpointName = jwksEndpointName;
    }

    public String getClientNameAttributeName() {
        return clientNameAttributeName;
    }

    public void setClientNameAttributeName(String clientNameAttributeName) {
        this.clientNameAttributeName = clientNameAttributeName;
    }

    public boolean useSoftwareIdAsAppName() {
        return useSoftwareIdAsAppName;
    }

    public void setUseSoftwareIdAsAppName(boolean useSoftwareIdAsAppName) {
        this.useSoftwareIdAsAppName = useSoftwareIdAsAppName;
    }

    public int getJwksEndpointTimeout() {
        return jwksEndpointTimeout;
    }

    public void setJwksEndpointTimeout(int jwksEndpointTimeout) {
        this.jwksEndpointTimeout = jwksEndpointTimeout;
    }

    /**
     * Validate the request JWT and modify the payload if necessary.
     *
     * @param messageContext    Message context of the request
     * @param requestPayload    The request payload containing the JWT
     * @param headers           Map of headers in the request
     * @throws DCRHandlingException When an error occurs while validating or modifying the payload
     */
    private void validateAndModifyPayload(MessageContext messageContext, String requestPayload,
                                          Map<String, Object> headers)
            throws DCRHandlingException {

        //decode request jwt
        String decodedRequest = null;
        try {
            decodedRequest = DCRUtil.decodeRequestJWT(requestPayload, DCRConstants.JWT_BODY);
        } catch (ParseException e) {
            log.error("Error occurred while decoding the provided jwt", e);
            throw new DCRHandlingException(DCRConstants.INVALID_REQUEST, "Malformed request JWT",
                    DCRConstants.BAD_REQUEST_CODE, e);
        }

        //Check whether decodedRequest is null
        if (decodedRequest == null) {
            log.error("Provided jwt is malformed and cannot be decoded");
            throw new DCRHandlingException(DCRConstants.INVALID_REQUEST,
                    "Provided jwt is malformed and cannot be decoded", DCRConstants.BAD_REQUEST_CODE);
        }

        JSONObject decodedRequestObj = new JSONObject(decodedRequest);
        JSONObject decodedSSA = null;
        //Check whether the SSA exists and decode the SSA
        if (decodedRequestObj.has(DCRConstants.SOFTWARE_STATEMENT) &&
                decodedRequestObj.getString(DCRConstants.SOFTWARE_STATEMENT) != null) {
            try {
                String ssa = DCRUtil.decodeRequestJWT(decodedRequestObj
                                .getString(DCRConstants.SOFTWARE_STATEMENT),
                        DCRConstants.JWT_BODY);
                decodedSSA = new JSONObject(ssa);
            } catch (ParseException e) {
                log.error("Error occurred while decoding the provided jwt", e);
                throw new DCRHandlingException(DCRConstants.INVALID_SSA,
                        "Malformed Software Statement JWT found", DCRConstants.BAD_REQUEST_CODE, e);
            }

            try {
                // Validate the request signature
                DCRUtil.validateRequestSignature(requestPayload, decodedSSA, jwksEndpointName,
                        jwksEndpointTimeout);
            } catch (BadJOSEException | JOSEException | MalformedURLException e) {
                log.error("Error occurred while validating the signature", e);
                throw new DCRHandlingException(DCRConstants.INVALID_REQUEST,
                        "Invalid request signature. " + e.getMessage(), DCRConstants.BAD_REQUEST_CODE, e);
            } catch (ParseException e) {
                log.error("Error occurred while decoding the provided jwt", e);
                throw new DCRHandlingException(DCRConstants.INVALID_REQUEST, "Malformed request JWT",
                        DCRConstants.BAD_REQUEST_CODE, e);
            }
        }
        appendModifiedPayload(messageContext, decodedRequestObj, decodedSSA, headers);
    }

    /**
     * Modify the JSON payload.
     *
     * @param messageContext   Message context of the request
     * @param requestPayload   The request payload containing the JSON data
     * @param headers          Map of headers in the request
     * @throws DCRHandlingException  When an error occurs while modifying the JSON payload
     */
    private void modifyJsonPayload(MessageContext messageContext, String requestPayload,
                                       Map<String, Object> headers) throws DCRHandlingException {

        JSONObject requestPayloadObj = new JSONObject(requestPayload);
        JSONObject decodedSSA = null;
        //Check whether the SSA exists and decode the SSA
        if (requestPayloadObj.has(DCRConstants.SOFTWARE_STATEMENT) &&
                requestPayloadObj.getString(DCRConstants.SOFTWARE_STATEMENT) != null) {
            try {
                String ssa = DCRUtil.decodeRequestJWT(requestPayloadObj
                                .getString(DCRConstants.SOFTWARE_STATEMENT),
                        DCRConstants.JWT_BODY);
                decodedSSA = new JSONObject(ssa);
            } catch (ParseException e) {
                log.error("Error occurred while decoding the provided jwt", e);
                throw new DCRHandlingException(DCRConstants.INVALID_SSA,
                        "Malformed Software Statement JWT found", DCRConstants.BAD_REQUEST_CODE, e);
            }
        }
        appendModifiedPayload(messageContext, requestPayloadObj, decodedSSA, headers);
    }

    /**
     * Append the modified payload to the message context.
     *
     * @param messageContext      Message context of the request
     * @param decodedRequestObj   Decoded request
     * @param decodedSSA          Decoded Software Statement Assertion (SSA)
     * @param headers             Map of headers in the request
     * @throws DCRHandlingException  When an error occurs while appending the modified payload
     */
    private void appendModifiedPayload(MessageContext messageContext, JSONObject decodedRequestObj,
                                       JSONObject decodedSSA, Map<String, Object> headers)
            throws DCRHandlingException {

        try {
            org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext();
            // Append IS DCR request payload attributes to message context
            DCRUtil.appendISDcrRequestPayloadAttributes(messageContext, decodedRequestObj, decodedSSA,
                    useSoftwareIdAsAppName, clientNameAttributeName, jwksEndpointName);

            // Set the modified payload to the context
            axis2MessageContext.setProperty(Constants.Configuration.MESSAGE_TYPE,
                    MediaType.APPLICATION_JSON);
            axis2MessageContext.setProperty(Constants.Configuration.CONTENT_TYPE,
                    MediaType.APPLICATION_JSON);
            headers.remove(DCRConstants.CONTENT_TYPE_TAG);
            headers.put(DCRConstants.CONTENT_TYPE_TAG, DCRConstants.JSON_CONTENT_TYPE);
            axis2MessageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS,
                    headers);
            JsonUtil.getNewJsonPayload(axis2MessageContext, decodedRequestObj.toString(), true, true);
        } catch (AxisFault e) {
            log.error("Error occurred appending the modified payload", e);
            throw  new DCRHandlingException(DCRConstants.SERVER_ERROR, "Error occurred appending the modified payload",
                    DCRConstants.SERVER_ERROR_CODE, e);
        }
    }

    /**
     * Validate whether the token is bound to the client id sent in the request. Applicable for DCR retrieval,
     * update and delete requests.
     *
     * @param messageContext   Message context of the request
     * @param headers          Map of headers in the request
     * @return               true if the client id is valid, false otherwise
     */
    private boolean validateClientId(MessageContext messageContext, Map<String, Object> headers) {

        String resource = messageContext.getProperty(DCRConstants.API_UT_RESOURCE).toString();
        if (StringUtils.isBlank(resource)) {
            log.error("Invalid resource path in the request");
            DCRUtil.returnSynapseHandlerJSONError(messageContext, DCRConstants.BAD_REQUEST_CODE,
                    DCRUtil.getErrorResponse(DCRConstants.INVALID_REQUEST,
                            "Invalid resource path in the request"));
            return false;
        }

        String[] resourceParts = resource.split(DCRConstants.SLASH);
        String clientIdSentInRequest = resourceParts.length > 2 ? resourceParts[2] : null;

        if (!headers.containsKey(DCRConstants.AUTHORIZATION)) {
            log.error("Authorization header is missing in the request");
            DCRUtil.returnSynapseHandlerJSONError(messageContext, DCRConstants.UNAUTHORIZED_CODE,
                    DCRUtil.getErrorResponse(DCRConstants.UNAUTHORIZED,
                            "Authorization header is missing in the request"));
            return false;
        }

        String authHeader = headers.get(DCRConstants.AUTHORIZATION).toString().split(" ")[1];

        String extractedAuthHeader = null;
        try {
            extractedAuthHeader = DCRUtil.decodeRequestJWT(authHeader, DCRConstants.JWT_BODY);
        } catch (ParseException e) {
            log.error("Error occurred while decoding the provided auth header", e);
            DCRUtil.returnSynapseHandlerJSONError(messageContext, DCRConstants.BAD_REQUEST_CODE,
                    DCRUtil.getErrorResponse(DCRConstants.INVALID_REQUEST,
                            "Error occurred while decoding the provided auth header"));
            return false;
        }

        JSONObject decodedAuthHeader = new JSONObject(extractedAuthHeader);
        String clientIdBoundToToken = decodedAuthHeader.getString(DCRConstants.CLIENT_ID);

        if (!clientIdBoundToToken.equals(clientIdSentInRequest)) {
            log.error("Token is not bound to the client id sent in the request");
            DCRUtil.returnSynapseHandlerJSONError(messageContext, DCRConstants.UNAUTHORIZED_CODE,
                    DCRUtil.getErrorResponse(DCRConstants.UNAUTHORIZED,
                            "Token is not bound to the client id sent in the request"));
            return false;
        }
        return true;
    }

}
