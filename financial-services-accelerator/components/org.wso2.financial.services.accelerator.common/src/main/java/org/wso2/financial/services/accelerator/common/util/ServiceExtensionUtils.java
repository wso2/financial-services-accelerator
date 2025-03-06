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

package org.wso2.financial.services.accelerator.common.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesRuntimeException;
import org.wso2.financial.services.accelerator.common.extension.ExternalServiceRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

/**
 * Service extension utils.
 */
public class ServiceExtensionUtils {

    private static final Log log = LogFactory.getLog(ServiceExtensionUtils.class);

    /**
     * Method to invoke external service call.
     *
     * @param externalServiceRequest
     * @param service
     * @return
     */
    public static JSONObject invokeExternalServiceCall(ExternalServiceRequest externalServiceRequest, String service) {
        try {
            String externalServicesPayload = (new JSONObject(externalServiceRequest)).toString();

            // TODO: use the service to construct the URL
            HttpPost httpPost = new HttpPost("");
            StringEntity params = new StringEntity(externalServicesPayload);
            httpPost.setEntity(params);
            httpPost.setHeader(FinancialServicesConstants.CONTENT_TYPE_TAG,
                    FinancialServicesConstants.JSON_CONTENT_TYPE);
            httpPost.setHeader(FinancialServicesConstants.ACCEPT, FinancialServicesConstants.JSON_CONTENT_TYPE);

            // TODO: need to see security requirement

            CloseableHttpResponse response = HTTPClientUtils.getHttpClient().execute(httpPost);

            if (response.getStatusLine().getStatusCode() != 200) {
                String errorMsg = IOUtils.toString(response.getEntity().getContent());
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Error occurred while invoking the external service. " +
                                    "Status code: %s, Error: %s", response.getStatusLine().getStatusCode(),
                            errorMsg.replaceAll("[\r\n]", "")));
                }
                throw new FinancialServicesException("Error occurred while invoking the external service: " + errorMsg);
            }
            InputStream in = response.getEntity().getContent();
            return new JSONObject(IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8)));
        } catch (FinancialServicesException | IOException e) {
            throw new FinancialServicesRuntimeException("Error Occurred while invoking the external service", e);
        }
    }

    /**
     * Extract string payload from request object.
     *
     * @param request The request object
     * @return String payload
     * @throws FinancialServicesException Payload read errors
     */
    public static String getStringPayload(HttpServletRequest request) throws FinancialServicesException {
        try {
            return IOUtils.toString(request.getInputStream());
        } catch (IOException e) {
            log.error("Error while extracting the payload", e);
            throw new FinancialServicesException("Error while extracting the payload", e);
        }
    }

    public static void setBasicAuthHeader(HttpPost httpPost, String userName, String password) {

        httpPost.setHeader(FinancialServicesConstants.AUTH_HEADER, getBasicAuthHeader(userName, password));
    }

    public static void setOauth2AuthHeader(HttpPost httpPost, String tokenEP, String userName, String password)
            throws URISyntaxException, IOException, FinancialServicesException {

        httpPost.setHeader(FinancialServicesConstants.AUTH_HEADER,
                getClientCredentialGrantToken(tokenEP, userName, password));
    }

    /**
     * Method to obtain client credential grant token.
     *
     * @param tokenEP
     * @param clientId
     * @param clientSecret
     * @return
     * @throws URISyntaxException
     * @throws FinancialServicesException
     * @throws IOException
     */
    public static String getClientCredentialGrantToken(String tokenEP, String clientId, String clientSecret)
            throws URISyntaxException, FinancialServicesException, IOException {

        HttpPost httpPost = new HttpPost(tokenEP);
        URI uri = new URIBuilder(httpPost.getURI())
                .addParameter("grant_type", "client_credentials")
                .build();
        httpPost.setURI(uri);

        httpPost.setHeader(FinancialServicesConstants.CONTENT_TYPE_TAG,
                FinancialServicesConstants.URL_ENCODED_CONTENT_TYPE);
        httpPost.setHeader(FinancialServicesConstants.AUTH_HEADER, getBasicAuthHeader(clientId, clientSecret));

        CloseableHttpResponse response = HTTPClientUtils.getHttpClient().execute(httpPost);

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new FinancialServicesException("Error occurred while obtaining the token");
        }
        InputStream in = response.getEntity().getContent();
        JSONObject tokenResponse = new JSONObject(IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8)));
        return tokenResponse.getString("access_token");
    }

    /**
     * Method to obtain basic auth header.
     *
     * @param username Username of Auth header
     * @param password Password of Auth header
     * @return basic auth header
     */
    public static String getBasicAuthHeader(String username, String password) {

        byte[] authHeader = Base64.getEncoder().encode((username + FinancialServicesConstants.COLON + password)
                .getBytes(StandardCharsets.UTF_8));
        return FinancialServicesConstants.BASIC_TAG + new String(authHeader, StandardCharsets.UTF_8);
    }

    /**
     * Check whether a given path exists in a JSONObject.
     *
     * @param jsonObject  JSONObject to check
     * @param path        Path to check
     * @return        Whether the path exists
     */
    public static boolean pathExists(JSONObject jsonObject, String path) {
        String[] keys = path.split("\\.");
        JSONObject current = jsonObject;

        for (int i = 0; i < keys.length; i++) {
            if (!current.has(keys[i])) {
                return false;
            }
            if (i == keys.length - 1) {
                return true;
            }
            current = current.optJSONObject(keys[i]);
            if (current == null && i != keys.length - 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieve the value from a JSONObject for a given path.
     *
     * @param jsonObject  JSONObject to retrieve the value from
     * @param path        Path to the value
     * @return          Value for the given path
     */
    public static Object retrieveValueFromJSONObject(JSONObject jsonObject, String path) {
        String[] keys = path.split("\\.");
        JSONObject current = jsonObject;

        for (int i = 0; i < keys.length - 1; i++) {
            if (!current.has(keys[i]) || !(current.get(keys[i]) instanceof JSONObject)) {
                return null;
            }
            current = current.getJSONObject(keys[i]);
        }

        return current.has(keys[keys.length - 1]) ? current.get(keys[keys.length - 1]) : null;
    }

}
