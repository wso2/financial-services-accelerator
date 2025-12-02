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

package org.wso2.financial.services.accelerator.keymanager.utils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.HTTPClientUtils;
import org.wso2.financial.services.accelerator.keymanager.internal.KeyManagerDataHolder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Util class for access Identity server APIs through key manager.
 */
public class IdentityServerUtils {

    /**
     * Method to get the application ID from the client ID. Uses Application Management API in IS
     * (https://{km_host}:{km_port}/api/server/v1/applications/) with filter 'clientId eq clientId_value'
     * to fetch the application ID.
     *
     * @param keyManagerConfiguration   key manager configs configured in APIM
     * @param clientId  Client ID of the application
     * @return          Application ID if found, otherwise null
     * @throws FinancialServicesException If an error occurs while fetching the application ID
     */
    @SuppressFBWarnings("HTTP_PARAMETER_POLLUTION")
    public static String getAppIdFromClientId(final KeyManagerConfiguration keyManagerConfiguration,
                                              final String clientId) throws FinancialServicesException {

        try {
            URIBuilder builder = new URIBuilder(getKeyManagerApplicationMgtEndpoint(keyManagerConfiguration));
            builder.addParameter(FSKeyManagerConstants.FILTER, FSKeyManagerConstants.FILTER_KEY + clientId);
            HttpGet httpGet = new HttpGet(builder.build());

            httpGet.setHeader(FinancialServicesConstants.AUTH_HEADER,
                    getBasicAuthHeaderFromKeyManagerConfig(keyManagerConfiguration));
            CloseableHttpResponse response = HTTPClientUtils.getHttpsClient().execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new FinancialServicesException("Error while getting app id from client id");
            }
            InputStream in = response.getEntity().getContent();
            JSONObject responseObj = new JSONObject(IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8)));
            JSONArray appArray = responseObj.getJSONArray("applications");
            if (appArray.length() > 0) {
                return appArray.getJSONObject(0).getString("id");
            }
            return null;
        } catch (IOException | URISyntaxException e) {
            throw new FinancialServicesException("Error while getting app id from client id", e);
        }
    }

    /**
     * Method to get the SP application details from the client ID. Uses Application Management API in IS
     * (https://{km_host}:{km_port}/api/server/v1/applications/{app_id}) to retrieve the SP Application.
     *
     * @param keyManagerConfiguration   key manager configs configured in APIM
     * @param clientId   Client ID of the application
     * @return           JSONObject containing the SP application details
     * @throws FinancialServicesException  If an error occurs while fetching the SP application details
     */
    public static JSONObject getSPApplicationFromClientId(final KeyManagerConfiguration keyManagerConfiguration,
                                                          final String clientId) throws FinancialServicesException {

        final String appId = getAppIdFromClientId(keyManagerConfiguration, clientId);

        try {
            final String url = getKeyManagerApplicationMgtEndpoint(keyManagerConfiguration) + appId;
            URIBuilder builder = new URIBuilder(url);
            HttpGet httpGet = new HttpGet(builder.build());

            httpGet.setHeader(FinancialServicesConstants.AUTH_HEADER,
                    getBasicAuthHeaderFromKeyManagerConfig(keyManagerConfiguration));
            CloseableHttpResponse response = HTTPClientUtils.getHttpsClient().execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new FinancialServicesException("Error while getting sp application from client id");
            }
            InputStream in = response.getEntity().getContent();
            return new JSONObject(IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8)));
        } catch (IOException | URISyntaxException e) {
            throw new FinancialServicesException("Error while getting sp application from client id", e);
        }
    }

    /**
     * Method to update the SP application in the Identity server with the provided certificate. Uses Application
     * Management API in IS (https://{km_host}:{km_port}/api/server/v1/applications/).
     *
     * @param keyManagerConfiguration   key manager configs configured in APIM
     * @param clientId      Client ID of the application
     * @param certificate   Certificate content in PEM format to be updated in the SP application
     * @throws FinancialServicesException  If an error occurs while updating the SP application
     */
    public static void updateSPApplication(final KeyManagerConfiguration keyManagerConfiguration,
                                           final String clientId, final String certificate)
            throws FinancialServicesException {

        final String appId = getAppIdFromClientId(keyManagerConfiguration, clientId);

        try {
            String url = getKeyManagerApplicationMgtEndpoint(keyManagerConfiguration) + appId;
            URIBuilder builder = new URIBuilder(url);
            HttpPatch httpPatch = new HttpPatch(builder.build());

            JSONObject appUpdatePayload = constructAppUpdatePayload(certificate);
            StringEntity params = new StringEntity(appUpdatePayload.toString());
            httpPatch.setEntity(params);
            httpPatch.setHeader(FinancialServicesConstants.CONTENT_TYPE_TAG,
                    FinancialServicesConstants.JSON_CONTENT_TYPE);
            httpPatch.setHeader(FinancialServicesConstants.ACCEPT,
                    FinancialServicesConstants.JSON_CONTENT_TYPE);
            httpPatch.setHeader(FinancialServicesConstants.AUTH_HEADER,
                    getBasicAuthHeaderFromKeyManagerConfig(keyManagerConfiguration));
            CloseableHttpResponse response = HTTPClientUtils.getHttpsClient().execute(httpPatch);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new FinancialServicesException("Error while getting sp application from client id");
            }
        } catch (IOException | URISyntaxException e) {
            throw new FinancialServicesException("Error while getting sp application from client id", e);
        }
    }

    /**
     * Method to construct the payload for updating the SP application with the certificate.
     *
     * @param certificateContent  Content of the certificate in PEM format
     * @return  JSONObject containing the payload for updating the SP application
     */
    private static JSONObject constructAppUpdatePayload(String certificateContent) {

        JSONObject appUpdatePayload = new JSONObject();
        JSONObject advancedConfigurations = new JSONObject();
        JSONObject certificate = new JSONObject();
        certificate.put(FSKeyManagerConstants.TYPE, FSKeyManagerConstants.PEM);
        certificate.put(FSKeyManagerConstants.VALUE, certificateContent);
        advancedConfigurations.put(FSKeyManagerConstants.CERTIFICATE, certificate);
        appUpdatePayload.put(FSKeyManagerConstants.ADVANCED_CONFIGURATIONS, advancedConfigurations);
        return appUpdatePayload;
    }

    /**
     * Method to update the DCR application in the Identity server. Used DCR API in IS
     * ("https://{km_host}:{km_port}/api/identity/oauth2/dcr/v1.1/register/").
     *
     * @param clientId                Client ID of the application
     * @param appName                 Application name
     * @param attributes              Map of attributes to be updated
     * @throws FinancialServicesException   If an error occurs while updating the application
     */
    public static void updateDCRApplication(final KeyManagerConfiguration keyManagerConfiguration,
                                            String clientId, String appName, Map<String, Object> attributes)
            throws FinancialServicesException {

        JSONObject spApplication = constructDCRUpdatePayload(appName, attributes);

        try {
            String url = getKeyManagerBaseUrl(keyManagerConfiguration) + FSKeyManagerConstants.DCR_EP + clientId;
            URIBuilder builder = new URIBuilder(url);
            HttpPut httpPut = new HttpPut(builder.build());
            StringEntity params = new StringEntity(spApplication.toString());
            httpPut.setEntity(params);
            httpPut.setHeader(FinancialServicesConstants.CONTENT_TYPE_TAG,
                    FinancialServicesConstants.JSON_CONTENT_TYPE);

            httpPut.setHeader(FinancialServicesConstants.AUTH_HEADER,
                    getBasicAuthHeaderFromKeyManagerConfig(keyManagerConfiguration));
            CloseableHttpResponse response = HTTPClientUtils.getHttpsClient().execute(httpPut);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new FinancialServicesException("Error while updating sp application");
            }
        } catch (IOException | URISyntaxException e) {
            throw new FinancialServicesException("Error while updating sp application", e);
        }
    }

    /**
     * Method to construct the DCR payload for updating the SP application.
     *
     * @param appName                  Application name
     * @param attributes               Map of attributes
     * @return JSONObject             DCR payload
     */
    private static JSONObject constructDCRUpdatePayload(String appName, Map<String, Object> attributes) {

        JSONObject spApplication = new JSONObject();
        spApplication.put(FSKeyManagerConstants.CLIENT_NAME, appName);
        spApplication.put(FSKeyManagerConstants.ADDITIONAL_ATTRIBUTES, attributes);
        if (attributes.containsKey(FinancialServicesConstants.REGULATORY) &&
                Boolean.parseBoolean(attributes.get(FinancialServicesConstants.REGULATORY).toString())) {
            spApplication.put(FSKeyManagerConstants.TLS_CLIENT_CERT_BOUND_ACCESS_TOKENS, true);
        }

        return spApplication;
    }

    /**
     * Method to read API mgt configs when key is given.
     *
     * @param key config key
     * @return config value
     */
    private static String getAPIMgtConfig(String key) {

        return KeyManagerDataHolder.getInstance().getApiManagerConfigurationService()
                .getAPIManagerConfiguration().getFirstProperty(key);
    }

    /**
     * Method to get the Identity server URL.
     *
     * @return Identity server URL
     */
    private static String getIdentitySeverUrl() {
        return KeyManagerDataHolder.getInstance().getApiManagerConfigurationService().getAPIManagerConfiguration()
                .getFirstProperty(FSKeyManagerConstants.API_KEY_VALIDATOR_URL).split(FSKeyManagerConstants.SERVICE)[0];
    }

    /**
     * Method to construct the service provider properties list from the SP properties and additional properties.
     *
     * @param spProperties          JSONArray containing SP properties
     * @param additionalProperties  HashMap containing additional properties
     * @return   Map containing the constructed service provider properties
     */
    public static Map<String, Object> constructSPPropertiesList(JSONArray spProperties,
                                                                HashMap<String, String> additionalProperties) {
        // Implementation goes here
        Map<String, Object> serviceProviderProperties = new HashMap<>();
        for (int i = 0; i < spProperties.length(); i++) {
            JSONObject property = spProperties.getJSONObject(i);
            serviceProviderProperties.put(property.getString(FSKeyManagerConstants.NAME),
                    property.getString(FSKeyManagerConstants.VALUE));
        }
        serviceProviderProperties.putAll(additionalProperties);

        return serviceProviderProperties;
    }

    /**
     * Method to get the SP metadata from the SP app details.
     *
     * @param appData  SP app data
     * @return SP metadata
     */
    public static JSONArray getSPMetadataFromSPApp(JSONObject appData) {
        JSONArray spData = new JSONArray();
        if (appData.has(FSKeyManagerConstants.ADVANCED_CONFIGURATIONS)) {
            JSONObject configs = appData.getJSONObject(FSKeyManagerConstants.ADVANCED_CONFIGURATIONS);
            if (configs.has(FSKeyManagerConstants.ADDITIONAL_SP_PROPERTIES)) {
                spData = configs.getJSONArray(FSKeyManagerConstants.ADDITIONAL_SP_PROPERTIES);
            }
        }
        return spData;
    }

    /**
     * Method to get the regulatory property from the SP metadata.
     *
     * @param appData  SP app data
     * @return regulatory property
     */
    public static String getRegulatoryPropertyFromSPMetadata(JSONObject appData) {
        JSONArray spData = getSPMetadataFromSPApp(appData);
        String regulatoryProperty = null;

        for (int i = 0; i < spData.length(); i++) {
            JSONObject spObj = spData.getJSONObject(i);
            if (spObj.has(FSKeyManagerConstants.NAME) &&
                    spObj.getString(FSKeyManagerConstants.NAME).equals(FinancialServicesConstants.REGULATORY)) {
                regulatoryProperty = spObj.getString(FSKeyManagerConstants.VALUE);
                break;
            }
        }
        return regulatoryProperty;
    }

    /**
     * Method to get a specific SP property from the SP metadata.
     *
     * @param propertyName  Name of the property to retrieve
     * @param spProperties  JSONArray containing SP properties
     * @return property value if found, otherwise null
     */
    public static String getSpPropertyFromSPMetaData(String propertyName, JSONArray spProperties) {

        String propertyValue = null;

        for (int i = 0; i < spProperties.length(); i++) {
            JSONObject spObj = spProperties.getJSONObject(i);
            if (spObj.has(FSKeyManagerConstants.NAME) &&
                    spObj.getString(FSKeyManagerConstants.NAME).equals(propertyName)) {
                propertyValue = spObj.getString(FSKeyManagerConstants.VALUE);
                break;
            }
        }
        return propertyValue;
    }

    /**
     * Method to get the key manager /applications endpoint.
     *
     * @return the URL of the /applications REST endpoint.
     */
    public static String getKeyManagerApplicationMgtEndpoint(final KeyManagerConfiguration keyManagerConfiguration) {

        return getKeyManagerBaseUrl(keyManagerConfiguration) + FSKeyManagerConstants.APP_MGMT_API_URL;
    }

    /**
     * Method to get the basic auth header value from key manager configs.
     *
     * @return basic auth header value.
     */
    public static String getBasicAuthHeaderFromKeyManagerConfig(KeyManagerConfiguration keyManagerConfiguration) {

        String userName = (String) keyManagerConfiguration.getParameter(APIConstants.KEY_MANAGER_USERNAME);
        char[] password = ((String) keyManagerConfiguration.getParameter(APIConstants.KEY_MANAGER_PASSWORD))
                .toCharArray();
        return FinancialServicesUtils.getBasicAuthHeader(userName, password);
    }

    /**
     * Method to get the key manager base URL.
     *
     * @return key manager base URL.
     */
    private static String getKeyManagerBaseUrl(final KeyManagerConfiguration keyManagerConfiguration) {

        final String keyManagerAuthEndpoint =
                (String) keyManagerConfiguration.getParameter(APIConstants.KeyManager.AUTHORIZE_ENDPOINT);
        if (StringUtils.isNotEmpty(keyManagerAuthEndpoint)) {
            return keyManagerAuthEndpoint.split(FSKeyManagerConstants.OAUTH2)[0];
        }
        return getIdentitySeverUrl();
    }

}
