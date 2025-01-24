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

package org.wso2.financial.services.accelerator.gateway.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.wso2.carbon.apimgt.common.gateway.dto.RequestContextDTO;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesRuntimeException;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.common.util.JWTUtils;
import org.wso2.financial.services.accelerator.gateway.cache.GatewayCacheKey;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIResponseContext;
import org.wso2.financial.services.accelerator.gateway.internal.GatewayDataHolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Utility methods used in gateway modules.
 */
public class GatewayUtils {

    private static final Map<String, Object> configs = GatewayDataHolder.getInstance()
            .getFinancialServicesConfigurationService().getConfigurations();

    private static final List<String> dcrListAttributes = Arrays.asList(GatewayConstants.GRANT_TYPES,
            GatewayConstants.REDIRECT_URIS, GatewayConstants.RESPONSE_TYPES);
    private static final List<String> dcrResponseParams = GatewayDataHolder.getInstance()
            .getFinancialServicesConfigurationService().getDCRResponseParameters();

    private GatewayUtils() {

    }

    /**
     * Method to check whether the given string is a valid JWT token.
     *
     * @param jwtString JWT token string
     * @return true if the given string is a valid JWT token, false otherwise
     */
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
     * Method to obtain swagger definition from publisher API.
     *
     * @param apiId ID of the API
     * @return String of swagger definition
     */
    @Generated(message = "Cannot test without running APIM. Integration test will be written for this")
    public static String getSwaggerDefinition(String apiId) {

        String publisherHostName =
                GatewayDataHolder.getInstance().getFinancialServicesConfigurationService()
                        .getConfigurations()
                        .get(FinancialServicesConstants.PUBLISHER_HOSTNAME).toString();

        String publisherAPIURL = publisherHostName.endsWith(GatewayConstants.SLASH) ?
                publisherHostName + GatewayConstants.PUBLISHER_API_PATH + apiId + GatewayConstants.SWAGGER_ENDPOINT :
                publisherHostName + GatewayConstants.SLASH + GatewayConstants.PUBLISHER_API_PATH + apiId +
                        GatewayConstants.SWAGGER_ENDPOINT;
        try {
            URIBuilder uriBuilder = new URIBuilder(publisherAPIURL);
            HttpGet httpGet = new HttpGet(uriBuilder.build().toString());
            String userName = getAPIMgtConfig(GatewayConstants.API_KEY_VALIDATOR_USERNAME);
            String password = getAPIMgtConfig(GatewayConstants.API_KEY_VALIDATOR_PASSWORD);

            httpGet.setHeader(GatewayConstants.AUTH_HEADER, GatewayUtils.getBasicAuthHeader(userName, password));
            HttpResponse response = null;
            response = GatewayDataHolder.getHttpClient().execute(httpGet);
            InputStream in = response.getEntity().getContent();
            return IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8));
        } catch (IOException | FinancialServicesException | URISyntaxException e) {
            throw new FinancialServicesRuntimeException("Failed to retrieve swagger definition from API", e);
        }
    }

    /**
     * Retrieve OpenAPI definition from the cache or from the publisher API.
     *
     * @param requestContextDTO  Request context DTO
     * @return OpenAPI definition
     */
    @Generated(message = "Excluding from unit tests since it includes cache initialization")
    public static OpenAPI retrieveOpenAPI(RequestContextDTO requestContextDTO) {

        String apiId = requestContextDTO.getApiRequestInfo().getApiId();
        Object cacheObject = GatewayDataHolder.getGatewayCache()
                .getFromCache(GatewayCacheKey.of(apiId));
        if (cacheObject == null) {
            String swaggerDefinition = GatewayUtils.getSwaggerDefinition(apiId);
            OpenAPIParser parser = new OpenAPIParser();
            OpenAPI openAPIDefinition =  parser.readContents(swaggerDefinition, null, null).getOpenAPI();
            GatewayDataHolder.getGatewayCache().addToCache(GatewayCacheKey.of(apiId), openAPIDefinition);
            return openAPIDefinition;
        }

        return (OpenAPI) cacheObject;
    }

    /**
     * Method to read API mgt configs when key is given.
     *
     * @param key config key
     * @return config value
     */
    public static String getAPIMgtConfig(String key) {

        return GatewayDataHolder.getInstance().getApiManagerConfigurationService()
                .getAPIManagerConfiguration().getFirstProperty(key);
    }

    /**
     * Method to obtain basic auth header.
     *
     * @param username Username of Auth header
     * @param password Password of Auth header
     * @return basic auth header
     */
    public static String getBasicAuthHeader(String username, String password) {

        byte[] authHeader = Base64.getEncoder().encode((username + GatewayConstants.COLON + password)
                .getBytes(StandardCharsets.UTF_8));
        return GatewayConstants.BASIC_TAG + new String(authHeader, StandardCharsets.UTF_8);
    }

    /**
     * Method to extract the text payload from the given XML payload.
     *
     * @param payload   XML payload
     * @return        Text payload
     */
    public static String getTextPayload(String payload) {

        return XML.toJSONObject(payload).getJSONObject(GatewayConstants.SOAP_BODY)
                .getJSONObject(GatewayConstants.SOAP_BODY_TEXT).getString(GatewayConstants.SOAP_BODY_CONTENT);
    }

    /**
     * Check the content type and http method of the request.
     *
     * @param contentType - contentType
     * @param httpMethod - httpMethod
     * @return true if the request is eligible
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
     * @return true if the response is eligible
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
     * Method to extract JWT payload section as a string.
     *
     * @param jwtString full JWT
     * @return Payload section of JWT
     */
    public static String getPayloadFromJWT(String jwtString) {

        return jwtString.split("\\.")[1];
    }

    /**
     * Method to validate the signature of the request.
     *
     * @param payload      JWT payload
     * @param decodedSSA   Decoded SSA
     * @return            JWT claims set
     * @throws ParseException        When parsing fails
     * @throws JOSEException          When JOSE processing fails
     * @throws BadJOSEException       When JOSE processing fails
     * @throws MalformedURLException  When JWKS URL is malformed
     */
    @Generated(message = "Excluding from unit tests since there is an external http call")
    public static JWTClaimsSet validateRequestSignature(String payload, JSONObject decodedSSA)
            throws ParseException, JOSEException, BadJOSEException, MalformedURLException {

        String jwksEndpointName = configs.get(FinancialServicesConstants.JWKS_ENDPOINT_NAME).toString();
        //validate request signature
        String jwksEndpoint = decodedSSA.getString(jwksEndpointName);
        SignedJWT signedJWT = SignedJWT.parse(payload);
        String alg = signedJWT.getHeader().getAlgorithm().getName();

        return JWTUtils.validateJWTSignature(payload, jwksEndpoint, alg);
    }

    /**
     * Convert the given JWT claims set to a JSON string.
     *
     * @param jwtClaimsSet The JWT claims set.
     *
     * @return The JSON string.
     */
    public static String constructISDcrRequestPayload(JWTClaimsSet jwtClaimsSet, JSONObject decodedSSA) {

        JSONObject request = new JSONObject(jwtClaimsSet.getClaims());

        // Convert the iat and exp claims into seconds
        if (jwtClaimsSet.getIssueTime() != null) {
            request.put(GatewayConstants.IAT, jwtClaimsSet.getIssueTime().getTime() / 1000);
        }
        if (jwtClaimsSet.getExpirationTime() != null) {
            request.put(GatewayConstants.EXP, jwtClaimsSet.getExpirationTime().getTime() / 1000);
        }
        String appName =  getApplicationName(jwtClaimsSet, decodedSSA);

        request.put(GatewayConstants.CLIENT_NAME, appName);
        request.put(GatewayConstants.JWKS_URI, decodedSSA.getString(configs
                .get(FinancialServicesConstants.JWKS_ENDPOINT_NAME).toString()));
        request.put(GatewayConstants.TOKEN_TYPE, GatewayConstants.JWT);
        request.put(GatewayConstants.REQUIRE_SIGNED_OBJ, true);
        request.put(GatewayConstants.TLS_CLIENT_CERT_ACCESS_TOKENS, true);
        request.put(GatewayConstants.APP_DISPLAY_NAME, getSafeApplicationName(decodedSSA
                .getString(configs.get(FinancialServicesConstants.SSA_CLIENT_NAME).toString())));
        request.put(GatewayConstants.TLS_CLIENT_CERT_ACCESS_TOKENS, true);

        return request.toString();
    }

    /**
     * Method to construct DCR response from request payload and IS DCR response.
     *
     * @param fsapiResponseContext The response context
     *
     * @return The JSON string.
     */
    public static String constructDCRResponseForCreate(FSAPIResponseContext fsapiResponseContext) {

        // Retrieve the request payload from context props and make it the response
        String requestPayload = fsapiResponseContext.getContextProperty(GatewayConstants.REQUEST_PAYLOAD).toString();
        JSONObject dcrResponse = new JSONObject(requestPayload);

        // Remove the unnecessary claims from the response
        dcrResponse.remove(GatewayConstants.IAT);
        dcrResponse.remove(GatewayConstants.EXP);
        dcrResponse.remove(GatewayConstants.AUD);
        dcrResponse.remove(GatewayConstants.JTI);
        dcrResponse.remove(GatewayConstants.ISS);

        // Add the client ID, client secret and client ID issued at time from the IS DCR response
        String dcrISResponse = fsapiResponseContext.getResponsePayload();
        JsonObject dcrISResponseObj = JsonParser.parseString(dcrISResponse).getAsJsonObject();
        dcrResponse.put(GatewayConstants.CLIENT_ID, dcrISResponseObj.get(GatewayConstants.CLIENT_ID).getAsString());
        dcrResponse.put(GatewayConstants.CLIENT_SECRET, dcrISResponseObj.get(GatewayConstants.CLIENT_SECRET)
                .getAsString());
        dcrResponse.put(GatewayConstants.CLIENT_ID_ISSUED_AT, Instant.now().getEpochSecond());

        // Add the other attributes from the IS DCR response to the DCR response
        dcrResponseParams.stream().filter(dcrISResponseObj::has).forEach(param -> {
            if (!dcrResponse.has(param)) {
                if (dcrListAttributes.contains(param)) {
                    dcrResponse.put(param, dcrISResponseObj.get(param).getAsJsonArray());
                } else {
                    dcrResponse.put(param, dcrISResponseObj.get(param).getAsString());
                }
            }
        });

        return dcrResponse.toString();
    }

    /**
     * Method to construct DCR response from IS DCR response.
     *
     * @param fsapiResponseContext The response context
     *
     * @return The JSON string.
     */
    public static String constructDCRResponseForRetrieval(FSAPIResponseContext fsapiResponseContext) {

        JSONObject dcrResponse = new JSONObject();

        // Retrieve IS DCR response paylaod
        String dcrISResponse = fsapiResponseContext.getResponsePayload();
        JsonObject dcrISResponseObj = JsonParser.parseString(dcrISResponse).getAsJsonObject();

        // Add the client ID and client secret from the IS DCR response
        dcrResponse.put(GatewayConstants.CLIENT_ID, dcrISResponseObj.get(GatewayConstants.CLIENT_ID).getAsString());
        dcrResponse.put(GatewayConstants.CLIENT_SECRET, dcrISResponseObj.get(GatewayConstants.CLIENT_SECRET)
                .getAsString());

        // Add the other attributes configured from the IS DCR response to the DCR response
        dcrResponseParams.stream().filter(dcrISResponseObj::has).forEach(param -> {
            if (dcrListAttributes.contains(param)) {
                dcrResponse.put(param, dcrISResponseObj.get(param).getAsJsonArray());
            } else {
                dcrResponse.put(param, dcrISResponseObj.get(param).getAsString());
            }
        });

        return dcrResponse.toString();
    }

    /**
     * Retrieves the application name from the registration request.
     *
     * @param request     registration or update request
     * @param decodedSSA  Decoded SSA
     * @return The application name
     */
    public static String getApplicationName(JWTClaimsSet request, JSONObject decodedSSA) {
        boolean useSoftwareIdAsAppName = Boolean.parseBoolean(configs
                .get(FinancialServicesConstants.DCR_USE_SOFTWAREID_AS_APPNAME).toString());
        if (useSoftwareIdAsAppName) {
            // If the request does not contain a software statement, get the software Id directly from the request
            if (request.getClaims().containsKey(GatewayConstants.SOFTWARE_STATEMENT)) {
                return decodedSSA.getString(GatewayConstants.SOFTWARE_ID);
            }

            return request.getClaims().get(GatewayConstants.SOFTWARE_ID).toString();
        }
        return getSafeApplicationName(decodedSSA
                .getString(configs.get(FinancialServicesConstants.SSA_CLIENT_NAME).toString()));
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

        String sanitizedInput = applicationName.trim().replaceAll(GatewayConstants.DISALLOWED_CHARS_PATTERN,
                GatewayConstants.SUBSTITUTE_STRING);

        return StringUtils.abbreviate(sanitizedInput, GatewayConstants.ABBREVIATED_STRING_LENGTH);

    }
}
