/**
 * Copyright (c) 2023-2024, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.gateway.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.util.Base64URL;
import com.wso2.openbanking.accelerator.common.error.OpenBankingErrorCodes;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingRuntimeException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.gateway.cache.GatewayCacheKey;
import com.wso2.openbanking.accelerator.gateway.executor.exception.OpenBankingExecutorException;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.internal.GatewayDataHolder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;

/**
 * Utility methods used in gateway.
 */
public class GatewayUtils {
    private static final Log log = LogFactory.getLog(GatewayUtils.class);

    private static final String SOAP_ENV_START_TAG = "<soapenv:Body " +
            "xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">";
    private static final String SOAP_ENV_END_TAG = "</soapenv:Body>";

    /**
     * Method to decode the base64 encoded JSON payload.
     *
     * @param payload base64 encoded payload
     * @return Decoded JSON Object
     * @throws UnsupportedEncodingException When encoding is not UTF-8
     */
    public static JSONObject decodeBase64(String payload) throws UnsupportedEncodingException {

        return new JSONObject(new String(Base64.getDecoder().decode(payload),
                String.valueOf(StandardCharsets.UTF_8)));
    }

    /**
     * Method to extract JWT payload section as a string.
     *
     * @param jwtString full JWT
     * @return Payload section of JWT
     */
    public static String getPayloadFromJWT(String jwtString) {

        return jwtString.split("\\.")[1];
    }

    /**
     * Method to retrieve payload as a String from XML.
     * This method expects 'xml-multiple' instruction in xmlPayload to transform single element json arrays.
     * synapse.commons.json.output.xmloutMultiplePI=true should be set in synapse.properties file.
     *
     * This method is deprecated. Use getXMLPayloadToSign method instead.
     *
     * @param xmlPayload Payload in XML format
     * @return String version of JSON object
     */
    @Deprecated
    public static String getPayloadFromXML(String xmlPayload) throws OpenBankingException {

        String jsonString = null;
        try {
            OMElement omElement = AXIOMUtil.stringToOM(xmlPayload);
            jsonString = JsonUtil.toJsonString(omElement).toString();
        } catch (AxisFault e) {
            log.error("Error occurred while reading the xml payload");
            throw new OpenBankingException("Error occurred while reading the xml payload", e);
        } catch (XMLStreamException e) {
            log.error("Error occurred while transforming the xml payload to json");
            throw new OpenBankingException("Error occurred while transforming the xml payload to json", e);
        }
        JSONObject jsonObject = new JSONObject(jsonString);
        return jsonObject.has("Body") ? jsonObject.get("Body").toString() : null;
    }

    /**
     * Method to retrieve payload from response with xml Payload.
     *
     * @param xmlPayload Payload in XML format
     * @return String payload
     */
    public static String getXMLPayloadToSign(String xmlPayload) throws OpenBankingException {

        try {
            OMElement omElement = AXIOMUtil.stringToOM(xmlPayload);
            OMElement firstElement = (OMElement) omElement.getFirstOMChild();
            if (firstElement != null) {
                return firstElement.toString();
            } else {
                return "";
            }
        } catch (XMLStreamException e) {
            log.error("Error occurred while transforming the xml payload.");
            throw new OpenBankingException("Error occurred while transforming the xml payload", e);
        }
    }

    public static String getTextPayload(String payload) {

        return XML.toJSONObject(payload).getJSONObject("soapenv:Body").getJSONObject("text").getString("content");

    }

    /**
     * Method to obatain basic auth header.
     *
     * @param username Username of Auth header
     * @param password Password of Auth header
     * @return basic auth header
     */
    public static String getBasicAuthHeader(String username, String password) {

        byte[] authHeader = Base64.getEncoder().encode((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return GatewayConstants.BASIC_TAG + new String(authHeader, StandardCharsets.UTF_8);
    }

    /**
     * Method to obtain swagger definition from publisher API.
     *
     * @param apiId ID of the API
     * @return String of swagger definition
     */
    @Generated(message = "Cannot test without running APIM. Integration test will be written for this")
    public static String getSwaggerDefinition(String apiId) {

        String publisherHostName =
                GatewayDataHolder.getInstance().getOpenBankingConfigurationService()
                        .getConfigurations()
                        .get(GatewayConstants.PUBLISHER_HOSTNAME).toString();

        String publisherAPIURL = publisherHostName.endsWith("/") ?
                publisherHostName + GatewayConstants.PUBLISHER_API_PATH + apiId + GatewayConstants.SWAGGER_ENDPOINT :
                publisherHostName + "/" + GatewayConstants.PUBLISHER_API_PATH + apiId +
                        GatewayConstants.SWAGGER_ENDPOINT;

        try (CloseableHttpClient httpClient = GatewayDataHolder.getHttpClient()) {
            HttpGet httpGet = new HttpGet(publisherAPIURL);
            String userName = getAPIMgtConfig(GatewayConstants.API_KEY_VALIDATOR_USERNAME);
            String password = getAPIMgtConfig(GatewayConstants.API_KEY_VALIDATOR_PASSWORD);

            httpGet.setHeader(GatewayConstants.AUTH_HEADER, GatewayUtils.getBasicAuthHeader(userName, password));
            HttpResponse response = null;
            response = httpClient.execute(httpGet);
            InputStream in = response.getEntity().getContent();
            return IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8));
        } catch (IOException | OpenBankingException e) {
            throw new OpenBankingRuntimeException("Failed to retrieve swagger definition from API", e);
        }
    }

    /**
     * Method to read API mgt configs when key is given.
     *
     * @param key config key
     * @return config value
     */
    public static String getAPIMgtConfig(String key) {

        return GatewayDataHolder.getInstance()
                .getApiManagerConfigurationService().getAPIManagerConfiguration().getFirstProperty(key);
    }

    public static boolean isValidJWTToken(String jwtString) {

        String[] jwtPart = jwtString.split("\\.");
        if (jwtPart.length != 3) {
            return false;
        }
        try {
            decodeBase64(jwtPart[0]);
            decodeBase64(jwtPart[1]);
        } catch (UnsupportedEncodingException | JSONException | IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    /**
     * Check the content type and http method of the request.
     *
     * @param contentType - contentType
     * @param httpMethod - httpMethod
     * @return
     */
    public static boolean isEligibleRequest(String contentType, String httpMethod) {

        return (contentType.startsWith(GatewayConstants.JSON_CONTENT_TYPE) ||
                contentType.startsWith(GatewayConstants.APPLICATION_XML_CONTENT_TYPE) ||
                contentType.startsWith(GatewayConstants.TEXT_XML_CONTENT_TYPE)) &&
                (GatewayConstants.POST_HTTP_METHOD.equals(httpMethod) || GatewayConstants.PUT_HTTP_METHOD
                        .equals(httpMethod));
    }

    /**
     * Check the content type and http method of the response.
     *
     * @param contentType - contentType
     * @param httpMethod - httpMethod
     * @return
     */
    public static boolean isEligibleResponse(String contentType, String httpMethod) {

        return (contentType.startsWith(GatewayConstants.JSON_CONTENT_TYPE) ||
                contentType.startsWith(GatewayConstants.APPLICATION_XML_CONTENT_TYPE) ||
                contentType.startsWith(GatewayConstants.TEXT_XML_CONTENT_TYPE)) &&
                (GatewayConstants.GET_HTTP_METHOD.equals(httpMethod) || GatewayConstants.
                        POST_HTTP_METHOD.equals(httpMethod) || GatewayConstants.PUT_HTTP_METHOD.equals(httpMethod)
                        || GatewayConstants.PATCH_HTTP_METHOD.equals(httpMethod) || GatewayConstants.
                        DELETE_HTTP_METHOD.equals(httpMethod));
    }

    /**
     * Method to extract request payload from OBAPIRequestContext.
     *
     * @param obapiRequestContext
     * @param requestHeaders
     * @return
     */
    public static Optional<String> extractRequestPayload(OBAPIRequestContext obapiRequestContext,
                                                         Map<String, String> requestHeaders)
            throws OpenBankingException {

        Optional<String> payloadString = Optional.empty();

        if (requestHeaders.containsKey(GatewayConstants.CONTENT_TYPE_TAG)) {
            if (requestHeaders.get(GatewayConstants.CONTENT_TYPE_TAG).contains(
                    GatewayConstants.TEXT_XML_CONTENT_TYPE)
                    || requestHeaders.get(GatewayConstants.CONTENT_TYPE_TAG).contains(
                    GatewayConstants.APPLICATION_XML_CONTENT_TYPE)) {
                try {
                    payloadString = Optional.of(GatewayUtils.getXMLPayloadToSign(
                            obapiRequestContext.getMsgInfo().getPayloadHandler().consumeAsString()));
                } catch (Exception e) {
                    throw new OpenBankingException("Internal Server Error, Unable to process Payload");
                }
            } else {
                payloadString = Optional.ofNullable(obapiRequestContext.getRequestPayload());
            }
        } else {
            payloadString = Optional.ofNullable(obapiRequestContext.getRequestPayload());
        }

        return payloadString;
    }

    /**
     * Method to extract response payload from OBAPIResponseContext.
     *
     * @param obapiResponseContext
     * @param responseHeaders
     * @return
     */
    public static Optional<String> extractResponsePayload(OBAPIResponseContext obapiResponseContext,
                                                          Map<String, String> responseHeaders)
            throws OpenBankingException {

        Optional<String> payloadString = Optional.empty();

        if (responseHeaders.containsKey(GatewayConstants.CONTENT_TYPE_TAG)) {
            if (responseHeaders.get(GatewayConstants.CONTENT_TYPE_TAG).contains(
                    GatewayConstants.TEXT_XML_CONTENT_TYPE)
                    || responseHeaders.get(GatewayConstants.CONTENT_TYPE_TAG).contains(
                    GatewayConstants.APPLICATION_XML_CONTENT_TYPE)) {
                try {
                    payloadString = Optional.of(GatewayUtils.getXMLPayloadToSign(
                            obapiResponseContext.getMsgInfo().getPayloadHandler().consumeAsString()));
                } catch (Exception e) {
                    throw new OpenBankingException("Internal Server Error, Unable to process Payload");
                }
            } else {
                payloadString = Optional.ofNullable(obapiResponseContext.getResponsePayload());
            }
        } else {
            payloadString = Optional.ofNullable(obapiResponseContext.getResponsePayload());
        }

        return payloadString;
    }

    /**
     * Returns the JWS with a detached payload.
     * @param payloadString response payload
     * @param criticalParameters Critical parameters
     * @return String Detached Jws Signature
     * @throws JOSEException throws JOSEException Exception
     * @throws OpenBankingExecutorException throws OpenBanking Executor Exception
     */
    @Generated(message = "Excluding from unit tests since it is covered by other methods")
    public static String constructJWSSignature(String payloadString, HashMap<String, Object> criticalParameters)
            throws OpenBankingExecutorException, JOSEException {

        String detachedJWS;

        Optional<Key> signingKey;

        // Get from config parser
        JWSAlgorithm algorithm = GatewaySignatureHandlingUtils.getSigningAlgorithm();

        // Get signing certificate of ASPSP from keystore
        signingKey = GatewaySignatureHandlingUtils.getSigningKey();

        if (signingKey.isPresent()) {
            // Create a new JWSSigner
            JWSSigner signer;
            Key privateKey = signingKey.get();

            // Retrieve kid or empty string for signingKeyId
            String signingKeyId = GatewaySignatureHandlingUtils.getSigningKeyId();

            if (StringUtils.isBlank(signingKeyId)) {
                throw new OpenBankingExecutorException("The kid is not present to sign.");
            }

            JWSHeader jwsHeader = GatewaySignatureHandlingUtils.constructJWSHeader(signingKeyId,
                    criticalParameters, algorithm);
            JWSObject jwsObject = GatewaySignatureHandlingUtils.constructJWSObject(jwsHeader,
                    payloadString);

            if (privateKey.getAlgorithm().equals("RSA")) {
                // If the signing key is an RSA Key
                signer = new RSASSASigner((PrivateKey) privateKey);
            } else if (privateKey.getAlgorithm().equals("EC")) {
                // If the signing key is an EC Key
                signer = new ECDSASigner((ECPrivateKey) privateKey);
            } else {
                throw new JOSEException("The \"" + privateKey.getAlgorithm() +
                        "\" algorithm is not supported by the Solution");
            }

            try {
                // Check if payload is b64 encoded or un-encoded
                if (GatewaySignatureHandlingUtils.isB64HeaderVerifiable(jwsObject)) {
                    // b64=true
                    jwsObject.sign(signer);
                    String serializedJws = jwsObject.serialize();
                    detachedJWS = GatewaySignatureHandlingUtils.createDetachedJws(serializedJws);
                } else {
                    // b64=false
                    // Produces the signature with un-encoded payload.
                    // which is the encoded header + ".." + the encoded signature
                    Base64URL signature = signer.sign(jwsHeader,
                            GatewaySignatureHandlingUtils.getSigningInput(jwsHeader, payloadString));
                    detachedJWS = GatewaySignatureHandlingUtils.createDetachedJws(jwsHeader, signature);
                }
            } catch (JOSEException | UnsupportedEncodingException e) {
                throw new OpenBankingExecutorException("Unable to compute JWS signature", e);
            }
            return detachedJWS;
        } else {
            throw new OpenBankingExecutorException("Signing key is not present");
        }
    }

    /**
     * Method to handle internal server errors in JWS Signature validation.
     *
     * @param obapiRequestContext OB response context object
     * @param message error message
     */
    @Generated(message = "Excluding from unit tests since the method is for exception" +
            "handling")
    public static void handleRequestInternalServerError(
            OBAPIRequestContext obapiRequestContext, String message, String errorCode) {

        OpenBankingExecutorError error = new OpenBankingExecutorError(errorCode,
                "Internal server error", message, OpenBankingErrorCodes.SERVER_ERROR_CODE);
        ArrayList<OpenBankingExecutorError> executorErrors = obapiRequestContext.getErrors();
        executorErrors.add(error);
        obapiRequestContext.setError(true);
        obapiRequestContext.setErrors(executorErrors);
    }

    /**
     * Method to handle internal server errors in JWS Signature validation.
     *
     * @param obapiResponseContext OB response context object
     * @param message error message
     */
    @Generated(message = "Excluding from unit tests since the method is for exception" +
            "handling")
    public static void handleResponseInternalServerError(
            OBAPIResponseContext obapiResponseContext, String message, String errorCode) {

        OpenBankingExecutorError error = new OpenBankingExecutorError(errorCode,
                "Internal server error", message, OpenBankingErrorCodes.SERVER_ERROR_CODE);
        ArrayList<OpenBankingExecutorError> executorErrors = obapiResponseContext.getErrors();
        executorErrors.add(error);
        obapiResponseContext.setError(true);
        obapiResponseContext.setErrors(executorErrors);
    }

    /**
     * Method to get the userName with tenant domain.
     *
     * @param userName username
     * @return username with tenant domain
     */
    public static String getUserNameWithTenantDomain(String userName) {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (userName.endsWith(tenantDomain)) {
            return userName;
        } else {
            return userName + "@" + tenantDomain;
        }
    }

    /**
     * Retrieve security definitions defined in the swagger.
     * This method will return cached values if present, else will read swagger and cache the result.
     *
     * @param obApiRequestContext ob api request context
     * @return list of allowed auth flows for the elected resource
     */
    @Generated(message = "Ignoring since the method has covered in other tests")
    public static List<String> getAllowedOAuthFlows(OBAPIRequestContext obApiRequestContext) {

        List<String> oauthFlows = new ArrayList<>();
        String httpMethod = obApiRequestContext.getMsgInfo().getHttpMethod();
        String cacheKey = obApiRequestContext.getMsgInfo().getElectedResource() + ":" + httpMethod;
        GatewayCacheKey apiSecurityCacheKey = GatewayCacheKey.of(cacheKey);

        try {
            oauthFlows =
                    (List<String>) GatewayDataHolder.getGatewayCache().getFromCacheOrRetrieve(apiSecurityCacheKey,
                            () -> {
                                OpenAPI openAPI = obApiRequestContext.getOpenAPI();
                                String electedResource = obApiRequestContext.getMsgInfo().getElectedResource();
                                return getAllowedOAuthFlowsFromSwagger(openAPI, electedResource, httpMethod);
                            });
        } catch (OpenBankingException e) {
            log.error("Unable to cache or retrieve from API Security Cache", e);
        }
        return oauthFlows;
    }

    /**
     * Read allowed security schemes defined in the swagger for the given resource.
     *
     * @param openAPI         open API object
     * @param electedResource elected resource
     * @param httpMethod      http method
     * @return allowed security scheme
     */
    public static List<String> getAllowedOAuthFlowsFromSwagger(OpenAPI openAPI, String electedResource,
                                                               String httpMethod) {

        Map<String, SecurityScheme> securitySchemes = openAPI.getComponents().getSecuritySchemes();
        HashMap<String, ArrayList<String>> oAuthFlows = new HashMap<>();

        for (Object scheme : securitySchemes.keySet()) {
            OAuthFlows flows = securitySchemes.get(scheme.toString()).getFlows();

            if (flows != null) {
                ArrayList<String> allowedFlowsPerScheme = new ArrayList<>();

                if (flows.getAuthorizationCode() != null) {
                    allowedFlowsPerScheme.add(GatewayConstants.AUTHORIZATION_CODE);
                }
                if (flows.getImplicit() != null) {
                    allowedFlowsPerScheme.add(GatewayConstants.IMPLICIT);
                }
                if (flows.getClientCredentials() != null) {
                    allowedFlowsPerScheme.add(GatewayConstants.CLIENT_CREDENTIALS);
                }
                if (flows.getPassword() != null) {
                    allowedFlowsPerScheme.add(GatewayConstants.PASSWORD_GRANT);
                }
                oAuthFlows.put(scheme.toString(), allowedFlowsPerScheme);
            }
        }

        // get security flows defined for the resource
        PathItem electedPath = openAPI.getPaths().get(electedResource);
        List resourceSecurity = null;
        if (GatewayConstants.GET.equalsIgnoreCase(httpMethod)) {
            resourceSecurity = electedPath.getGet().getSecurity();
        } else if (GatewayConstants.POST.equalsIgnoreCase(httpMethod)) {
            resourceSecurity = electedPath.getPost().getSecurity();
        } else if (GatewayConstants.PUT.equalsIgnoreCase(httpMethod)) {
            resourceSecurity = electedPath.getPut().getSecurity();
        } else if (GatewayConstants.PATCH.equalsIgnoreCase(httpMethod)) {
            resourceSecurity = electedPath.getPatch().getSecurity();
        } else if (GatewayConstants.DELETE.equalsIgnoreCase(httpMethod)) {
            resourceSecurity = electedPath.getDelete().getSecurity();
        }

        ArrayList<String> allowedFlows = new ArrayList<>();
        List<String> securityRequirementList = new ArrayList<>();
        if (resourceSecurity != null) {
            for (Object security : resourceSecurity) {
                // Adding the keys of each security requirement to a list
                securityRequirementList.addAll(new ArrayList<>(((SecurityRequirement) security).keySet()));
            }
        }

        for (String requirement : securityRequirementList) {
            if (GatewayConstants.DEFAULT.equalsIgnoreCase(requirement) ||
                    GatewayConstants.OPENID.equalsIgnoreCase(requirement)) {
                continue;
            }
            if (oAuthFlows.containsKey(requirement)) {
                allowedFlows.addAll(oAuthFlows.get(requirement));
            }
        }

        return allowedFlows;
    }


    /**
     * Get bearer token payload.
     *
     * @param transportHeaders transport headers
     * @return jwt token payload
     * @throws OpenBankingExecutorException when authorization header not found in transport headers.
     */
    public static String getBearerTokenPayload(Map<String, String> transportHeaders)
            throws OpenBankingExecutorException {

        String authorizationHeader = "Authorization";
        if (transportHeaders.containsKey(authorizationHeader)) {
            try {
                return transportHeaders.get(authorizationHeader).split(" ")[1].split("\\.")[1];
            } catch (ArrayIndexOutOfBoundsException e) {
                log.debug("Invalid authorization header format", e);
                throw new OpenBankingExecutorException("Invalid Credentials.",
                        String.valueOf(GatewayConstants.API_AUTH_INVALID_CREDENTIALS),
                        GatewayConstants.INVALID_CREDENTIALS);
            }
        } else {
            log.debug("Missing Authorization header");
            throw new OpenBankingExecutorException("Missing Credentials.",
                    String.valueOf(GatewayConstants.API_AUTH_MISSING_CREDENTIALS),
                    GatewayConstants.MISSING_CREDENTIALS);
        }
    }

    /**
     * Get type of the token (application/ application user).
     *
     * @param tokenPayload jwt token payload
     * @return token type
     */
    public static String getTokenType(String tokenPayload) throws OpenBankingExecutorException {

        try {
            JSONObject decodedPayload = new JSONObject(new String(Base64.getUrlDecoder().decode(tokenPayload),
                    StandardCharsets.UTF_8));
            return decodedPayload.getString(GatewayConstants.AUTHORIZED_USER_TYPE_CLAIM_NAME);
        } catch (RuntimeException e) {
            log.error("Invalid tokenPayload", e);
            throw new OpenBankingExecutorException("Invalid Credentials.",
                    String.valueOf(GatewayConstants.API_AUTH_INVALID_CREDENTIALS),
                    GatewayConstants.INVALID_CREDENTIALS);
        }
    }

    /**
     * Validate the grant type against the swagger allowed auth flows.
     *
     * @param tokenType         grant type in the token
     * @param allowedOAuthFlows oauth flows allowed by swagger
     * @throws OpenBankingExecutorException when incorrect grant type is provided
     */
    public static void validateGrantType(String tokenType, List<String> allowedOAuthFlows)
            throws OpenBankingExecutorException {

        if ((GatewayConstants.APPLICATION.equalsIgnoreCase(tokenType) &&
                allowedOAuthFlows.contains(GatewayConstants.CLIENT_CREDENTIALS)) ||
                (GatewayConstants.APPLICATION_USER.equalsIgnoreCase(tokenType) &&
                        allowedOAuthFlows.contains(GatewayConstants.AUTHORIZATION_CODE))) {
            log.debug("Valid Access Token type found");
        } else {
            log.error("Incorrect Access Token Type is provided");
            throw new OpenBankingExecutorException(GatewayConstants.INVALID_GRANT_TYPE,
                    OpenBankingErrorCodes.INVALID_GRANT_TYPE_CODE, "Incorrect Access Token Type provided");
        }
    }

    /**
     * Build Message and extract payload.
     *
     * @param axis2MC message context
     * @return optional json message
     * @throws OpenBankingException thrown if unable to build
     */
    public static Optional<String> buildMessagePayloadFromMessageContext(
            org.apache.axis2.context.MessageContext axis2MC, Map headers) throws OpenBankingException {

        String requestPayload = null;
        boolean isMessageContextBuilt = isMessageContextBuilt(axis2MC);
        if (!isMessageContextBuilt) {
            // Build Axis2 Message.
            try {
                RelayUtils.buildMessage(axis2MC);
            } catch (IOException | XMLStreamException e) {
                throw new OpenBankingException("Unable to build axis2 message", e);
            }
        }

        if (headers.containsKey(GatewayConstants.CONTENT_TYPE_TAG)) {
            if (headers.get(GatewayConstants.CONTENT_TYPE_TAG).toString().contains(
                    GatewayConstants.TEXT_XML_CONTENT_TYPE)
                    || headers.get(GatewayConstants.CONTENT_TYPE_TAG).toString().contains(
                    GatewayConstants.APPLICATION_XML_CONTENT_TYPE)
                    || headers.get(GatewayConstants.CONTENT_TYPE_TAG).toString().contains(
                    GatewayConstants.JWT_CONTENT_TYPE)) {

                OMElement payload = axis2MC.getEnvelope().getBody().getFirstElement();
                if (payload != null) {
                    requestPayload = payload.toString();
                } else {
                    requestPayload = "";
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
                    throw new OpenBankingException("Unable to read payload stream", e);
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
     * Return JSON ResponseError for SynapseHandler.
     *
     * @param messageContext messages context.
     * @param code           response code.
     * @param jsonPayload    json payload.
     */
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
    private static void setJsonFaultPayloadToMessageContext(MessageContext messageContext, String payload) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();

        axis2MessageContext.setProperty(Constants.Configuration.MESSAGE_TYPE, MediaType.APPLICATION_JSON);

        try {
            JsonUtil.getNewJsonPayload(axis2MessageContext, payload, true, true);
        } catch (AxisFault axisFault) {
            log.error("Unable to set JSON payload to fault message", axisFault);
        }
    }

    /**
     * Send synapseHandler fault response.
     * @param messageContext messages context.
     * @param status error code.
     */
    private static void sendSynapseHandlerFaultResponse(MessageContext messageContext, String status) {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();

        axis2MC.setProperty(NhttpConstants.HTTP_SC, status);
        messageContext.setResponse(true);
        messageContext.setProperty("RESPONSE", "true");
        messageContext.setTo(null);
        axis2MC.removeProperty(Constants.Configuration.CONTENT_TYPE);
        Axis2Sender.sendBack(messageContext);
    }

    /**
     * Method to get json error body in OAuth2 format.
     * @return json error body
     */
    public static String getOAuth2JsonErrorBody(String error, String errorDescription) {

        JSONObject errorJSON = new JSONObject();
        errorJSON.put("error", error);
        errorJSON.put("error_description", errorDescription);
        return errorJSON.toString();
    }

    /**
     * Convert X509Certificate to PEM encoded string.
     *
     * @param certificate X509Certificate
     * @return PEM encoded string
     */
    public static String getPEMEncodedCertificateString(X509Certificate certificate)
            throws CertificateEncodingException {

        StringBuilder certificateBuilder = new StringBuilder();
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encoded = certificate.getEncoded();
        String base64Encoded = encoder.encodeToString(encoded);

        certificateBuilder.append(GatewayConstants.BEGIN_CERT);
        certificateBuilder.append(base64Encoded);
        certificateBuilder.append(GatewayConstants.END_CERT);

        return certificateBuilder.toString().replaceAll("\n", "+");
    }

    /**
     * Extract Certificate from Message Context.
     *
     * @param ctx Message Context
     * @return X509Certificate
     */
    public static X509Certificate extractAuthCertificateFromMessageContext(
            org.apache.axis2.context.MessageContext ctx) {

        Object sslCertObject = ctx.getProperty(GatewayConstants.AXIS2_MTLS_CERT_PROPERTY);
        if (sslCertObject != null) {
            X509Certificate[] certs = (X509Certificate[]) sslCertObject;
            return certs[0];
        } else {
            return null;
        }
    }

}
