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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.HTTPClientUtils;
import org.wso2.financial.services.accelerator.keymanager.internal.KeyManagerDataHolder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Util class for access Identity server APIs through key manager.
 */
public class IdentityServerUtils {

    @SuppressFBWarnings("HTTP_PARAMETER_POLLUTION")
    public static String getAppIdFromClientId(String clientId) throws FinancialServicesException {

        try {
            String url = getIdentitySeverUrl() + FSKeyManagerConstants.APP_RETRIEVAL_URL;
            URIBuilder builder = new URIBuilder(url);
            URI uri = builder.build();
            HttpGet httpGet = new HttpGet(uri.toString() + "?filter=clientId+eq+".concat(clientId));

            String userName = getAPIMgtConfig(FSKeyManagerConstants.API_KEY_VALIDATOR_USERNAME);
            String password = getAPIMgtConfig(FSKeyManagerConstants.API_KEY_VALIDATOR_PASSWORD);
            httpGet.setHeader(FinancialServicesConstants.AUTH_HEADER,
                    FinancialServicesUtils.getBasicAuthHeader(userName, password));
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
        } catch (IOException e) {
            throw new FinancialServicesException("Error while getting app id from client id", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONObject getSPApplicationFromClientId(String clientId) throws FinancialServicesException {

        String appId = getAppIdFromClientId(clientId);

        try {
            String url = getIdentitySeverUrl() + FSKeyManagerConstants.APP_RETRIEVAL_URL + appId;
            URIBuilder builder = new URIBuilder(url);
            HttpGet httpGet = new HttpGet(builder.build());

            String userName = getAPIMgtConfig(FSKeyManagerConstants.API_KEY_VALIDATOR_USERNAME);
            String password = getAPIMgtConfig(FSKeyManagerConstants.API_KEY_VALIDATOR_PASSWORD);
            httpGet.setHeader(FinancialServicesConstants.AUTH_HEADER,
                    FinancialServicesUtils.getBasicAuthHeader(userName, password));
            CloseableHttpResponse response = HTTPClientUtils.getHttpsClient().execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new FinancialServicesException("Error while getting sp application from client id");
            }
            InputStream in = response.getEntity().getContent();
            return new JSONObject(IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new FinancialServicesException("Error while getting sp application from client id", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to update the SP application in the Identity server.
     *
     * @param clientId                Client ID of the application
     * @param appName                 Application name
     * @param attributes              Map of attributes to be updated
     * @param oAuthApplicationInfo    OAuth application info
     * @throws FinancialServicesException   If an error occurs while updating the application
     */
    public static void updateSPApplication(String clientId, String appName, Map<String, Object> attributes,
                                                 OAuthApplicationInfo oAuthApplicationInfo)
            throws FinancialServicesException {

        JSONObject spApplication = constructDCRUpdatePayload(appName, attributes, oAuthApplicationInfo);

        try {
            String url = getIdentitySeverUrl() + FSKeyManagerConstants.DCR_EP + clientId;
            URIBuilder builder = new URIBuilder(url);
            HttpPut httpPut = new HttpPut(builder.build());
            StringEntity params = new StringEntity(spApplication.toString());
            httpPut.setEntity(params);
            httpPut.setHeader(FinancialServicesConstants.CONTENT_TYPE_TAG,
                    FinancialServicesConstants.JSON_CONTENT_TYPE);

            String userName = getAPIMgtConfig(FSKeyManagerConstants.API_KEY_VALIDATOR_USERNAME);
            String password = getAPIMgtConfig(FSKeyManagerConstants.API_KEY_VALIDATOR_PASSWORD);
            httpPut.setHeader(FinancialServicesConstants.AUTH_HEADER,
                    FinancialServicesUtils.getBasicAuthHeader(userName, password));
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
     * @param oAuthApplicationInfo     OAuth application info
     * @return JSONObject             DCR payload
     */
    private static JSONObject constructDCRUpdatePayload(String appName, Map<String, Object> attributes,
                                                        OAuthApplicationInfo oAuthApplicationInfo) {

        JSONObject spApplication = new JSONObject();
        spApplication.put("client_name", appName);
        spApplication.put("tls_client_certificate_bound_access_tokens", true);
        spApplication.put("additionalAttributes", attributes);

//        JSONObject additionalPropertiesJSON = new JSONObject((String) oAuthApplicationInfo
//                .getParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES));
//        for (String key : additionalPropertiesJSON.keySet()) {
//            // Existing values in the additional properties JSON object will start with "ext_" prefix.
//            // Updating values will come without the prefix, example if refresh_token_expiry_time is modified the
//            // existing value will come as ext_refresh_token_lifetime, new value will come as refresh_token_expiry
//            _time.
//            // To update the values in IS DCR application we need add the updating value with the prefix.
//            if (key.startsWith("ext_")) {
//                String mappedKey = FSKeyManagerConstants.APP_CONFIG_MAPPING.get(key);
//                if (additionalPropertiesJSON.has(mappedKey)) {
//                    spApplication.put(key, additionalPropertiesJSON.getString(mappedKey));
//                }
//            }
//        }

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

    public static Map<String, Object> constructSPPropertiesList(JSONArray spProperties,
                                                                HashMap<String, String> additionalProperties) {
        // Implementation goes here
        Map<String, Object> serviceProviderProperties = new HashMap<>();
        for (int i = 0; i < spProperties.length(); i++) {
            JSONObject property = spProperties.getJSONObject(i);
            serviceProviderProperties.put(property.getString("name"), property.getString("value"));
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
        if (appData.has("advancedConfigurations")) {
            JSONObject configs = appData.getJSONObject("advancedConfigurations");
            if (configs.has("additionalSpProperties")) {
                spData = configs.getJSONArray("additionalSpProperties");
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
            if (spObj.has("name") && spObj.getString("name").equals("regulatory")) {
                regulatoryProperty = spObj.getString("value");
                break;
            }
        }
        return regulatoryProperty;
    }

    public static String getSpPropertyFromSPMetaData(String propertyName, JSONArray spProperties) {

        String propertyValue = null;

        for (int i = 0; i < spProperties.length(); i++) {
            JSONObject spObj = spProperties.getJSONObject(i);
            if (spObj.has("name") && spObj.getString("name").equals(propertyName)) {
                propertyValue = spObj.getString("value");
                break;
            }
        }
        return propertyValue;
    }

}
