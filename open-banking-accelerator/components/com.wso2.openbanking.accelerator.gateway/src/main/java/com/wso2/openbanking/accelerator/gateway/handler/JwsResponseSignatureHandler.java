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

package com.wso2.openbanking.accelerator.gateway.handler;

import com.nimbusds.jose.JOSEException;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.error.OpenBankingErrorCodes;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.gateway.executor.exception.OpenBankingExecutorException;
import com.wso2.openbanking.accelerator.gateway.util.GatewayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Handler class for Signing Responses.
 */
public class JwsResponseSignatureHandler extends AbstractSynapseHandler {

    private static final Log log = LogFactory.getLog(JwsResponseSignatureHandler.class);

    private String xWso2ApiVersion = null;
    private String xWso2ApiType = null;
    private final String signatureHeaderName = getSignatureHeaderName();
    public static final String ERRORS_TAG = "errors";
    public static final String INTERNAL_SERVER_ERROR = "Internal server error";

    /**
     * Constructor for JwsResponseSignatureHandler.
     */
    @Generated(message = "Ignoring since method contains no logics")
    public JwsResponseSignatureHandler() {

        log.debug("Initializing JwsResponseSignatureHandler to append jws response signature.");
    }

    /**
     * Handle request message coming into the engine.
     *
     * @param messageContext incoming request message context
     * @return whether mediation flow should continue
     */
    @Override
    @Generated(message = "Ignoring since method contains no logics")
    public boolean handleRequestInFlow(MessageContext messageContext) {

        return true;

    }

    /**
     * Handle request message going out from the engine.
     *
     * @param messageContext outgoing request message context
     * @return whether mediation flow should continue
     */
    @Override
    @Generated(message = "Ignoring since method contains no logics")
    public boolean handleRequestOutFlow(MessageContext messageContext) {

        return true;
    }

    /**
     * Handle response message coming into the engine.
     *
     * @param messageContext incoming response message context
     * @return whether mediation flow should continue
     */
    @Override
    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    public boolean handleResponseInFlow(MessageContext messageContext) {

        return appendJwsSignatureToResponse(messageContext);
    }

    /**
     * Handle response message going out from the engine.
     *
     * @param messageContext outgoing response message context
     * @return whether mediation flow should continue
     */
    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MC =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (messageContext.getEnvelope() != null && messageContext.getEnvelope().getBody() != null &&
                StringUtils.contains(messageContext.getEnvelope().getBody().toString(),
                        "Schema validation failed")) {
            // Add jws header for schema errors, This is due to schema validation happens after responseInFlow.
            // So we need to regenerate the jws for schema validation error responses.
            return appendJwsSignatureToResponse(messageContext);
        } else if (headers.containsKey(signatureHeaderName) && headers.get(signatureHeaderName) != null) {
            return true;
        } else {
            // Add jws header, if it's not added yet.
            return appendJwsSignatureToResponse(messageContext);
        }
    }

    /**
     * Method to append Jws Signature to the response.
     *
     * @param messageContext response/request message context.
     * @return jws signature response is successfully appended.
     */
    private boolean appendJwsSignatureToResponse(MessageContext messageContext) {

        setXWso2ApiVersion((String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));
        setXWso2ApiType((String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT));

        try {
            boolean applicable = isApplicable(messageContext);
            if (!applicable) {
                log.debug("Signature generation is not applicable for this response");
                return true;
            } else {
                log.debug("Generating signature for the response");
            }
        } catch (RuntimeException e) {
            log.debug("Internal Server Error, Unable to append jws signature", e);
            GatewayUtils.returnSynapseHandlerJSONError(messageContext, OpenBankingErrorCodes.SERVER_ERROR_CODE,
                    getFormattedSignatureHandlingErrorResponse(messageContext, OpenBankingErrorCodes.SERVER_ERROR_CODE,
                            INTERNAL_SERVER_ERROR, "Internal Server Error, Unable to append jws signature"));
        }

        // Build the payload from messageContext.
        org.apache.axis2.context.MessageContext axis2MC =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Optional<String> payloadString;
        try {
            payloadString = GatewayUtils.buildMessagePayloadFromMessageContext(axis2MC, headers);
        } catch (OpenBankingException e) {
            log.error("Unable to build response payload", e);
            GatewayUtils.returnSynapseHandlerJSONError(messageContext, OpenBankingErrorCodes.SERVER_ERROR_CODE,
                    getFormattedSignatureHandlingErrorResponse(messageContext, OpenBankingErrorCodes.SERVER_ERROR_CODE,
                            INTERNAL_SERVER_ERROR, "Internal Server Error, Unable to build response payload"));
            return true;
        }

        if (payloadString.isPresent()) {
            try {
                // If the signature header already exists, remove it before adding a new one.
                // Headers are case-insensitive.
                Iterator headersIterator = headers.entrySet().iterator();
                while (headersIterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) headersIterator.next();
                    if (entry.getKey() instanceof String &&
                            ((String) entry.getKey()).equalsIgnoreCase(signatureHeaderName)) {
                        headersIterator.remove();
                        log.debug("Removing existing signature header: " + entry.getKey());
                    }
                }
                headers.put(signatureHeaderName, generateJWSSignature(payloadString));
            } catch (JOSEException | OpenBankingException e) {
                log.error("Unable to sign response", e);
                GatewayUtils.returnSynapseHandlerJSONError(messageContext, OpenBankingErrorCodes.SERVER_ERROR_CODE,
                        getFormattedSignatureHandlingErrorResponse(messageContext,
                                OpenBankingErrorCodes.SERVER_ERROR_CODE, INTERNAL_SERVER_ERROR,
                                "Internal Server Error, Unable to sign the response"));
                return true;
            }
        } else {
            log.debug("Signature cannot be generated as the payload is invalid or not present.");
        }
        axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
        return true;
    }

    /**
     * Method to change the expected request header name containing the JWS.
     *
     * @return String signature header name.
     */
    @Generated(message = "Excluding from unit tests since there is no logics to test")
    public String getSignatureHeaderName() {

        return "x-jws-signature";
    }

    /**
     * Provide the child classes to decide whether the signature generation is required for requestPath.
     *
     * @param messageContext OB response Object
     * @return boolean returns if request needs to be signed
     */
    @Generated(message = "Excluding from unit tests since there is a call to a method in Common Module")
    public boolean isApplicable(MessageContext messageContext) {

        return OpenBankingConfigParser.getInstance().isJwsResponseSigningEnabled();
    }

    /**
     * Method to Generate JWS signature.
     *
     * @param payloadString payload.
     * @return String jws signature.
     */
    public String generateJWSSignature(Optional<String> payloadString)
            throws OpenBankingException, JOSEException {

        String jwsSignatureHeader = null;
        if (payloadString.isPresent() && StringUtils.isNotBlank(payloadString.get())) {
            HashMap<String, Object> criticalParameters = getCriticalHeaderParameters();
            try {
                jwsSignatureHeader = GatewayUtils.constructJWSSignature(payloadString.get(), criticalParameters);
            } catch (OpenBankingExecutorException e) {
                throw new OpenBankingException(e.getMessage());
            }
        } else {
            log.debug("Signature cannot be generated as the payload is invalid.");
        }
        return jwsSignatureHeader;
    }

    /**
     * HashMap to be returned with crit header keys and values.
     * can be extended at toolkit level.
     *
     * @return HashMap crit header parameters
     */
    @Generated(message = "Excluding from unit test coverage")
    public HashMap<String, Object> getCriticalHeaderParameters() {

        return new HashMap<>();
    }

    /**
     * Method to get the formatted error response for jws signature response.
     *
     * @param messageContext messageContext
     * @param code           error code
     * @param title          error title
     * @param errorMessage   error message
     * @return String error response
     */
    @Generated(message = "Excluding from unit test coverage")
    public String getFormattedSignatureHandlingErrorResponse(MessageContext messageContext, String code, String title,
                                                             String errorMessage) {

        JSONObject payload = new JSONObject();
        JSONArray errorList = new JSONArray();
        JSONObject errorObj = new JSONObject();
        errorObj.put("Code", code);
        errorObj.put("Title", title);
        errorObj.put("Message", errorMessage);
        errorList.put(errorObj);
        return payload.put(ERRORS_TAG, errorList).toString();
    }

    @Generated(message = "Excluding from unit test coverage")
    public void setXWso2ApiVersion(String xWso2ApiVersion) {

        this.xWso2ApiVersion = xWso2ApiVersion;
    }

    @Generated(message = "Excluding from unit test coverage")
    public String getXWso2ApiVersion() {

        return this.xWso2ApiVersion;
    }

    @Generated(message = "Excluding from unit test coverage")
    public String getXWso2ApiType() {

        return xWso2ApiType;
    }

    @Generated(message = "Excluding from unit test coverage")
    public void setXWso2ApiType(String xWso2ApiType) {

        this.xWso2ApiType = xWso2ApiType;
    }
}
