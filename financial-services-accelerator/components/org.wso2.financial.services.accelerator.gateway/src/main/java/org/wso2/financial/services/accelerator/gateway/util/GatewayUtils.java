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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesRuntimeException;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.gateway.internal.GatewayDataHolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility methods used in gateway modules.
 */
public class GatewayUtils {

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

        String publisherAPIURL = publisherHostName.endsWith("/") ?
                publisherHostName + GatewayConstants.PUBLISHER_API_PATH + apiId + GatewayConstants.SWAGGER_ENDPOINT :
                publisherHostName + "/" + GatewayConstants.PUBLISHER_API_PATH + apiId +
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
     * Method to read API mgt configs when key is given.
     *
     * @param key config key
     * @return config value
     */
    public static String getAPIMgtConfig(String key) {

        return GatewayDataHolder.getInstance()
                .getApiManagerConfigurationService().getAPIManagerConfiguration().getFirstProperty(key);
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

    public static String getTextPayload(String payload) {

        return XML.toJSONObject(payload).getJSONObject("soapenv:Body").getJSONObject("text").getString("content");

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
     * Method to extract JWT payload section as a string.
     *
     * @param jwtString full JWT
     * @return Payload section of JWT
     */
    public static String getPayloadFromJWT(String jwtString) {

        return jwtString.split("\\.")[1];
    }
}
