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

package org.wso2.financial.services.apim.mediation.policies.dynamic.client.registration.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;

/**
 * Utility class for Dynamic Client Registration (DCR) operations.
 */
public class DCRUtil {

    private static final Log log = LogFactory.getLog(DCRUtil.class);

    /**
     * Build Message and extract payload.
     *
     * @param axis2MC          message context
     * @param contentType      content type of the request
     * @return optional json message
     * @throws DCRHandlingException thrown if unable to build
     */
    @Generated(message = "Excluding from code coverage since can not call this method due to external service call")
    public static Optional<String> buildMessagePayloadFromMessageContext(
            org.apache.axis2.context.MessageContext axis2MC, String contentType) throws DCRHandlingException {

        String requestPayload = null;
        boolean isMessageContextBuilt = isMessageContextBuilt(axis2MC);
        if (!isMessageContextBuilt) {
            // Build Axis2 Message.
            try {
                RelayUtils.buildMessage(axis2MC);
            } catch (IOException | XMLStreamException e) {
                throw new DCRHandlingException("Unable to build axis2 message", "server_error", "500", e);
            }
        }

        if (contentType != null) {
            if (contentType.contains(DCRConstants.JWT_CONTENT_TYPE)) {

                OMElement payload = axis2MC.getEnvelope().getBody().getFirstElement();
                if (payload != null) {
                    requestPayload = payload.getText();
                } else {
                    requestPayload = StringUtils.EMPTY;
                }
            } else {
                // Get JSON Stream and cast to string
                try {
                    InputStream jsonPayload = JsonUtil.getJsonPayload(axis2MC);
                    if (jsonPayload != null) {
                        requestPayload = IOUtils.toString(JsonUtil.getJsonPayload(axis2MC),
                                StandardCharsets.UTF_8.name());
                    }

                } catch (IOException e) {
                    throw new DCRHandlingException("Unable to read payload stream", "server_error", "500", e);
                }
            }
        }
        return Optional.ofNullable(requestPayload);
    }

    /**
     * Util method to check whether the message context is already built.
     *
     * @param axis2MC axis2 message context
     * @return true if message context is already built
     */
    public static boolean isMessageContextBuilt(org.apache.axis2.context.MessageContext axis2MC) {

        boolean isMessageContextBuilt = false;
        Object messageContextBuilt = axis2MC.getProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED);
        if (messageContextBuilt != null) {
            isMessageContextBuilt = (Boolean) messageContextBuilt;
        }
        return isMessageContextBuilt;
    }

    /**
     * Decode request JWT.
     *
     * @param jwtToken jwt sent by the tpp
     * @param jwtPart  expected jwt part (header, body)
     * @return json object containing requested jwt part
     * @throws ParseException if an error occurs while parsing the jwt
     */
    public static String decodeRequestJWT(String jwtToken, String jwtPart) throws ParseException {

        JWSObject plainObject = JWSObject.parse(jwtToken);

        if (DCRConstants.JWT_HEAD.equals(jwtPart)) {
            return plainObject.getHeader().toString();
        } else if (DCRConstants.JWT_BODY.equals(jwtPart)) {
            return plainObject.getPayload().toString();
        }

        return StringUtils.EMPTY;

    }

    /**
     * Method to validate the signature of the request.
     *
     * @param payload            JWT payload
     * @param decodedSSA         Decoded SSA
     * @param jwksEndpointName   JWKS endpoint name
     * @throws ParseException        When parsing fails
     * @throws JOSEException          When JOSE processing fails
     * @throws BadJOSEException       When JOSE processing fails
     * @throws MalformedURLException  When JWKS URL is malformed
     */
    @Generated(message = "Excluding from code coverage since can not call this method due to external service call")
    public static void validateRequestSignature(String payload, JSONObject decodedSSA,
                                                        String jwksEndpointName, int jwksConnectionTimeOut)
            throws ParseException, JOSEException, BadJOSEException, MalformedURLException {

        //validate request signature
        String jwksEndpoint = decodedSSA.getString(jwksEndpointName);
        SignedJWT signedJWT = SignedJWT.parse(payload);
        String alg = signedJWT.getHeader().getAlgorithm().getName();

        validateJWTSignature(payload, jwksEndpoint, alg, jwksConnectionTimeOut);
    }

    /**
     * Validate the signed JWT by querying a jwks.
     *
     * @param jwtString              signed json web token
     * @param jwksUri                endpoint displaying the key set for the signing certificates
     * @param algorithm              the signing algorithm for jwt
     * @param jwksConnectionTimeOut  connection timeout for the jwks endpoint
     * @throws ParseException        if an error occurs while parsing the jwt
     * @throws BadJOSEException      if the jwt is invalid
     * @throws JOSEException         if an error occurs while processing the jwt
     * @throws MalformedURLException if an error occurs while creating the URL
     *                               object
     */
    @Generated(message = "Excluding from code coverage since can not call this method due to external service call")
    public static void validateJWTSignature(String jwtString, String jwksUri, String algorithm,
                                                    int jwksConnectionTimeOut)
            throws ParseException, BadJOSEException, JOSEException, MalformedURLException {

        int defaultConnectionTimeout = 3000;
        int defaultReadTimeout = 3000;
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        JWT jwt = JWTParser.parse(jwtString);
        // set the Key Selector for the jwks_uri.
        Map<String, RemoteJWKSet<SecurityContext>> jwkSourceMap = new ConcurrentHashMap<>();
        RemoteJWKSet<SecurityContext> jwkSet = jwkSourceMap.get(jwksUri);
        if (jwkSet == null) {
            int connectionTimeout = jwksConnectionTimeOut;
            int readTimeout = jwksConnectionTimeOut;
            int sizeLimit = RemoteJWKSet.DEFAULT_HTTP_SIZE_LIMIT;
            if (connectionTimeout == 0 && readTimeout == 0) {
                connectionTimeout = defaultConnectionTimeout;
                readTimeout = defaultReadTimeout;
            }
            DefaultResourceRetriever resourceRetriever = new DefaultResourceRetriever(
                    connectionTimeout,
                    readTimeout,
                    sizeLimit);
            jwkSet = new RemoteJWKSet<>(new URL(jwksUri), resourceRetriever);
            jwkSourceMap.put(jwksUri, jwkSet);
        }
        // The expected JWS algorithm of the access tokens (agreed out-of-band).
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.parse(algorithm);
        // Configure the JWT processor with a key selector to feed matching public RSA
        // keys sourced from the JWK set URL.
        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, jwkSet);
        jwtProcessor.setJWSKeySelector(keySelector);
        // Process the token, set optional context parameters.
        SimpleSecurityContext securityContext = new SimpleSecurityContext();
        jwtProcessor.process((SignedJWT) jwt, securityContext);
    }

    /**
     * Convert the given JWT claims set to a JSON string.
     *
     * @param messageContext           MessageContext containing the request.
     * @param decodedRequest           JSONObject containing the decoded request.
     * @param decodedSSA               JSONObject containing the decoded SSA.
     * @param useSoftwareIdAsAppName   Flag to indicate whether to use software ID as application name.
     * @param clientNameAttributeName  The attribute name for the client name.
     * @param jwksEndpointName         The name of the JWKS endpoint.
     */
    public static void appendISDcrRequestPayloadAttributes(MessageContext messageContext, JSONObject decodedRequest,
                                                             JSONObject decodedSSA, boolean useSoftwareIdAsAppName,
                                                             String clientNameAttributeName, String jwksEndpointName) {

        String appName = getApplicationName(decodedRequest, decodedSSA, useSoftwareIdAsAppName,
                clientNameAttributeName);

        messageContext.setProperty(DCRConstants.CLIENT_NAME, appName);
        if (decodedSSA != null) {
            messageContext.setProperty(DCRConstants.JWKS_URI, decodedSSA.getString(jwksEndpointName));
            messageContext.setProperty(DCRConstants.APP_DISPLAY_NAME, getSafeApplicationName(decodedSSA
                    .getString(clientNameAttributeName)));
        } else {
            if (decodedRequest.has(jwksEndpointName)) {
                decodedRequest.put(DCRConstants.JWKS_URI, decodedRequest.getString(jwksEndpointName));
                messageContext.setProperty(DCRConstants.JWKS_URI, decodedRequest.getString(jwksEndpointName));
            }
            if (decodedRequest.has(clientNameAttributeName)) {
                messageContext.setProperty(DCRConstants.APP_DISPLAY_NAME, getSafeApplicationName(decodedRequest
                        .getString(clientNameAttributeName)));
            }
        }
        if (decodedRequest.has(DCRConstants.TOKEN_EP_AUTH_METHOD) &&
                DCRConstants.PRIVATE_KEY_JWT.equals(decodedRequest.getString(DCRConstants.TOKEN_EP_AUTH_METHOD))) {
            messageContext.setProperty(DCRConstants.TOKEN_EP_ALLOW_REUSE_PVT_KEY_JWT, Boolean.FALSE.toString());
        }
    }

    /**
     * Retrieves the application name from the registration request.
     *
     * @param request     registration or update request
     * @param decodedSSA  Decoded SSA
     * @return The application name
     */
    public static String getApplicationName(JSONObject request, JSONObject decodedSSA, boolean useSoftwareIdAsAppName,
                                            String clientNameAttributeName) {
        if (useSoftwareIdAsAppName) {
            if (decodedSSA != null && decodedSSA.has(DCRConstants.SOFTWARE_ID)) {
                return decodedSSA.getString(DCRConstants.SOFTWARE_ID);
            }
            // If the request does not contain a software statement, get the software Id directly from the request
            return request.get(DCRConstants.SOFTWARE_ID).toString();
        } else {
            if (decodedSSA != null && decodedSSA.has(clientNameAttributeName)) {
                return getSafeApplicationName(decodedSSA.getString(clientNameAttributeName));
            }
            return getSafeApplicationName(request.getString(clientNameAttributeName));
        }
    }

    /**
     * Modify the application name to match IS conditions.
     *
     * @param applicationName  The application name
     * @return The modified application name
     */
    public static String getSafeApplicationName(String applicationName) {

        if (StringUtils.isEmpty(applicationName)) {
            throw new IllegalArgumentException("Application name should be a valid string");
        }

        String sanitizedInput = applicationName.trim().replaceAll(DCRConstants.DISALLOWED_CHARS_PATTERN,
                DCRConstants.SUBSTITUTE_STRING);
        return StringUtils.abbreviate(sanitizedInput, DCRConstants.ABBREVIATED_STRING_LENGTH);
    }

    /**
     * Return JSON ResponseError for SynapseHandler.
     *
     * @param messageContext messages context.
     * @param code           response code.
     * @param jsonPayload    json payload.
     */
    @Generated(message = "Excluding from code coverage since can not call this method due to external service call")
    public static void returnSynapseHandlerJSONError(MessageContext messageContext, String code, String jsonPayload) {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
        try {
            RelayUtils.discardRequestMessage(axis2MC);
        } catch (AxisFault axisFault) {
            log.error("ResponseError occurred while discarding the message", axisFault);
        }
        setJsonFaultPayloadToMessageContext(messageContext, jsonPayload);
        sendSynapseHandlerFaultResponse(messageContext, code);
    }

    /**
     * Setting JSON payload as fault message to messageContext.
     * @param messageContext messages context.
     * @param payload json payload.
     */
    @Generated(message = "Excluding from code coverage since can not call this method due to external service call")
    private static void setJsonFaultPayloadToMessageContext(MessageContext messageContext, String payload) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();

        axis2MessageContext.setProperty(DCRConstants.DISABLE_CHUNKING, Boolean.TRUE);
        axis2MessageContext.setProperty(PassThroughConstants.NO_ENTITY_BODY, Boolean.FALSE);

        axis2MessageContext.setProperty(Constants.Configuration.MESSAGE_TYPE, MediaType.APPLICATION_JSON);
        axis2MessageContext.setProperty(Constants.Configuration.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        try {
            JsonUtil.getNewJsonPayload(axis2MessageContext, payload, Boolean.TRUE, Boolean.TRUE);
        } catch (AxisFault axisFault) {
            log.error("Unable to set JSON payload to fault message", axisFault);
        }
    }

    /**
     * Send synapseHandler fault response.
     * @param messageContext messages context.
     * @param status error code.
     */
    @Generated(message = "Excluding from code coverage since can not call this method due to external service call")
    private static void sendSynapseHandlerFaultResponse(MessageContext messageContext, String status) {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();

        axis2MC.setProperty(NhttpConstants.HTTP_SC, status);
        messageContext.setResponse(true);
        messageContext.setProperty(DCRConstants.RESPONSE, Boolean.TRUE.toString());
        messageContext.setTo(null);
        axis2MC.removeProperty(Constants.Configuration.CONTENT_TYPE);
        Axis2Sender.sendBack(messageContext);
    }

    /**
     * Method to get the formatted error response for jws signature response.
     *
     * @param code           error code
     * @param errorMessage   error message
     * @return String error response
     */
    public static String getErrorResponse(String code, String errorMessage) {

        JSONObject errorObj = new JSONObject();
        errorObj.put(DCRConstants.ERROR, code);
        errorObj.put(DCRConstants.ERROR_DESCRIPTION, errorMessage);
        return errorObj.toString();
    }
}
