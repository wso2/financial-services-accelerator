/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.ErrorConstants;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;


/**
 * Service extension utils.
 */
public class ServiceExtensionUtils {

    private static final Log log = LogFactory.getLog(ServiceExtensionUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Method to check whether the service extension is enabled and if the given service type is enabled.
     *
     * @param serviceExtensionTypeEnum  The service extension type to check.
     * @return  true if the service extension is enabled and the given service type is enabled, false otherwise.
     */
    public static boolean isInvokeExternalService(ServiceExtensionTypeEnum serviceExtensionTypeEnum) {

        FinancialServicesConfigParser configParser = FinancialServicesConfigParser.getInstance();
        boolean isServiceExtensionsEndpointEnabled = configParser.isServiceExtensionsEndpointEnabled();
        List<ServiceExtensionTypeEnum> serviceExtensionTypes = configParser.getServiceExtensionTypes();
        return isServiceExtensionsEndpointEnabled && serviceExtensionTypes.contains(serviceExtensionTypeEnum);
    }

    /**
     * Method to invoke external service call.
     * Implements the retry mechanism for the external service call.
     *
     * @param externalServiceRequest  The request containing the details of the external service call.
     * @param serviceType             Type of the service extension to be invoked.
     * @return ExternalServiceResponse containing the response from the external service.
     * @throws FinancialServicesException If an error occurs during the external service call or if the response is not
     */
    public static ExternalServiceResponse invokeExternalServiceCall(ExternalServiceRequest externalServiceRequest,
                                                                    ServiceExtensionTypeEnum serviceType)
            throws FinancialServicesException {

        FinancialServicesConfigParser configParser = FinancialServicesConfigParser.getInstance();
        final int maxRetries = configParser.getServiceExtensionsEndpointRetryCount();

        // Read timeout
        final int socketTimeoutMillis = configParser.getServiceExtensionsEndpointReadTimeoutInSeconds() * 1000;

        // Connection timeout
        final int connectTimeoutMillis = configParser.getServiceExtensionsEndpointConnectTimeoutInSeconds() * 1000;

        int attempt = 0;
        while (attempt < maxRetries) {
            attempt++;
            try {
                String externalServicesPayload = (new JSONObject(externalServiceRequest)).toString();
                String constructedUrl = constructExtensionEndpoint(serviceType);

                if (log.isDebugEnabled()) {
                    String sanitizedUrl = constructedUrl.replaceAll("[\r\n]", "");
                    log.debug("Invoking external service [requestUrl=" + sanitizedUrl + "]");
                }

                HttpPost httpPost = new HttpPost(constructedUrl);
                StringEntity params = new StringEntity(externalServicesPayload);
                httpPost.setEntity(params);
                httpPost.setHeader(FinancialServicesConstants.CONTENT_TYPE_TAG,
                        FinancialServicesConstants.JSON_CONTENT_TYPE);
                httpPost.setHeader(FinancialServicesConstants.ACCEPT,
                        FinancialServicesConstants.JSON_CONTENT_TYPE);

                // Set timeouts
                RequestConfig requestConfig = RequestConfig.custom()
                        .setSocketTimeout(socketTimeoutMillis)
                        .setConnectTimeout(connectTimeoutMillis)
                        .setConnectionRequestTimeout(connectTimeoutMillis)
                        .build();
                httpPost.setConfig(requestConfig);

                // Set security credentials
                String securityType = configParser.getServiceExtensionsEndpointSecurityType();
                if (FinancialServicesConstants.BASIC_AUTH.equals(securityType)) {
                    setBasicAuthHeader(httpPost,
                            configParser.getServiceExtensionsEndpointSecurityBasicAuthUsername(),
                            configParser.getServiceExtensionsEndpointSecurityBasicAuthPassword());
                } else if (FinancialServicesConstants.OAUTH2.equals(securityType)) {
                    setOauth2AuthHeader(httpPost,
                            configParser.getServiceExtensionsEndpointSecurityOauth2Token());
                } else {
                    throw new FinancialServicesException("Invalid security type for service extensions endpoint");
                }

                try (CloseableHttpResponse response = HTTPClientUtils.getHttpsClient().execute(httpPost)) {
                    HttpEntity entity = response.getEntity();
                    if (entity == null || entity.getContent() == null) {
                        throw new FinancialServicesException("No response content received from external service");
                    }

                    String responseContent = IOUtils.toString(entity.getContent(),
                            String.valueOf(StandardCharsets.UTF_8));
                    int statusCode = response.getStatusLine().getStatusCode();

                    if (log.isDebugEnabled()) {
                        String sanitizedUrl = constructedUrl.replaceAll("[\r\n]", "");
                        log.debug("External service response received [requestUrl=" + sanitizedUrl + ", statusCode=" +
                                statusCode + "]");
                    }

                    if (statusCode != 200) {
                        log.error(String.format(ErrorConstants.EXTERNAL_SERVICE_DEFAULT_ERROR +
                                        "Status code: %s, Error: %s", statusCode,
                                responseContent.replaceAll("[\r\n]", "")));
                        if (statusCode == 400 || statusCode == 500) {
                            ExternalServiceResponse externalServiceResponse = mapResponse(responseContent,
                                    ExternalServiceResponse.class);

                            throw new FinancialServicesException(externalServiceResponse.getData()
                                    .path(FinancialServicesConstants.ERROR_DESCRIPTION)
                                    .asText(ErrorConstants.EXTERNAL_SERVICE_DEFAULT_ERROR));
                        }
                        throw new FinancialServicesException(ErrorConstants.EXTERNAL_SERVICE_DEFAULT_ERROR);
                    }

                    return mapResponse(responseContent, ExternalServiceResponse.class);
                }

            } catch (IOException e) {
                if (isRetryableException(e)) {
                    log.warn(String.format("Attempt %d failed to call external service: %s",
                            attempt, e.getMessage().replaceAll("[\r\n]", "")));
                    if (attempt >= maxRetries) {
                        throw new FinancialServicesException("External service call failed after retries", e);
                    }
                } else {
                    log.error(ErrorConstants.EXTERNAL_SERVICE_DEFAULT_ERROR, e);
                    throw new FinancialServicesException(ErrorConstants.EXTERNAL_SERVICE_DEFAULT_ERROR);
                }
            }
        }

        throw new FinancialServicesException("External service call failed");
    }

    /**
     * Method to check whether the exception is retryable.
     *
     * @param e   Exception to be checked
     * @return  true if the exception is retryable, false otherwise
     */
    private static boolean isRetryableException(Exception e) {
        return e instanceof SocketTimeoutException ||
                e instanceof ConnectException ||
                e instanceof UnknownHostException;
    }

    /**
     * Method to map a json object to a model class.
     *
     * @param jsonResponse   JSON response string to be mapped
     * @param clazz          Class type to which the JSON response should be mapped
     * @param <T>            Type of the class to which the JSON response should be mapped
     * @return              Mapped object of type T
     * @throws JsonProcessingException If an error occurs during JSON processing
     */
    public static <T> T mapResponse(String jsonResponse, Class<T> clazz) throws JsonProcessingException {

        return objectMapper.readValue(jsonResponse, clazz);
    }

    /**
     * Method to construct the endpoint URL for the service extension.
     *
     * @param serviceType  Type of the service extension
     * @return  Constructed endpoint URL
     */
    private static String constructExtensionEndpoint(ServiceExtensionTypeEnum serviceType) {

        String baseUrl = FinancialServicesConfigParser.getInstance().getServiceExtensionsEndpointBaseUrl();
        return baseUrl + "/" + serviceType.toString().replaceAll("_", "-");
    }

    /**
     * Method to set the basic auth header for the HTTP POST request.
     *
     * @param httpPost  HTTP POST request to set the header
     * @param userName  Username for basic authentication
     * @param password  Password for basic authentication
     */
    public static void setBasicAuthHeader(HttpPost httpPost, String userName, String password) {

        httpPost.setHeader(FinancialServicesConstants.AUTH_HEADER, getBasicAuthHeader(userName, password));
    }

    /**
     * Method to set the OAuth2 authentication header for the HTTP POST request.
     *
     * @param httpPost  HTTP POST request to set the header
     * @param token     OAuth2 token to be set in the header
     */
    public static void setOauth2AuthHeader(HttpPost httpPost, String token) {

        httpPost.setHeader(FinancialServicesConstants.AUTH_HEADER, FinancialServicesConstants.BEARER_TAG + token);
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
     * @param jsonObject JSONObject to check
     * @param path       Path to check
     * @return Whether the path exists
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
     * @param jsonObject JSONObject to retrieve the value from
     * @param path       Path to the value
     * @return Value for the given path
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
