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

package com.wso2.openbanking.accelerator.gateway.internal;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.HTTPClientUtils;
import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.data.publisher.common.constants.DataPublishingConstants;
import com.wso2.openbanking.accelerator.gateway.cache.GatewayCache;
import com.wso2.openbanking.accelerator.gateway.executor.core.AbstractRequestRouter;
import com.wso2.openbanking.accelerator.gateway.throttling.ThrottleDataPublisher;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import org.apache.http.impl.client.CloseableHttpClient;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Data holder for executor core.
 */
public class GatewayDataHolder {

    private static volatile GatewayDataHolder instance;
    private static volatile CloseableHttpClient httpClient;
    private static volatile GatewayCache gatewayCache;
    private OpenBankingConfigurationService openBankingConfigurationService;
    private Map<String, Object> configurations;
    private APIManagerConfigurationService apiManagerConfigurationService;
    private AbstractRequestRouter requestRouter;
    private Map<String, Object> urlMap;
    private ThrottleDataPublisher throttleDataPublisher;
    private int gatewayCacheAccessExpiry;
    private int gatewayCacheModifiedExpiry;
    private String keyStoreLocation;
    private char[] keyStorePassword;
    private String keyAlias;
    private String keyPassword;
    private boolean isAPIMAnalyticsEnabled;
    private boolean isOBDataPublishingEnabled;
    private String workerThreadCount;
    private String clientTransportCertHeaderName;
    private boolean isUrlEncodeClientTransportCertHeaderEnabled;

    private GatewayDataHolder() {

    }

    public static GatewayDataHolder getInstance() {

        if (instance == null) {
            synchronized (GatewayDataHolder.class) {
                if (instance == null) {
                    instance = new GatewayDataHolder();
                }
            }
        }
        return instance;
    }

    public static CloseableHttpClient getHttpClient() throws OpenBankingException {

        if (httpClient == null) {
            synchronized (GatewayDataHolder.class) {
                if (httpClient == null) {
                    httpClient = HTTPClientUtils.getHttpsClient();
                }
            }
        }
        return httpClient;
    }

    public static GatewayCache getGatewayCache() {

        if (gatewayCache == null) {
            synchronized (GatewayDataHolder.class) {
                if (gatewayCache == null) {
                    gatewayCache = new GatewayCache();
                }
            }
        }
        return gatewayCache;
    }

    private static void setGatewayCache(GatewayCache cache) {
        gatewayCache = cache;
    }

    public OpenBankingConfigurationService getOpenBankingConfigurationService() {

        return openBankingConfigurationService;
    }

    public void setOpenBankingConfigurationService(
            OpenBankingConfigurationService openBankingConfigurationService) {

        this.openBankingConfigurationService = openBankingConfigurationService;
        if (openBankingConfigurationService != null) {
            this.configurations = openBankingConfigurationService.getConfigurations();
            AbstractRequestRouter configuredRequestRouter = (AbstractRequestRouter)
                    OpenBankingUtils.getClassInstanceFromFQN(configurations.get(GatewayConstants.REQUEST_ROUTER)
                            .toString());
            setGatewayCacheAccessExpiry((String) configurations.get(GatewayConstants.GATEWAY_CACHE_EXPIRY));
            setGatewayCacheModifiedExpiry((String) configurations
                    .get(GatewayConstants.GATEWAY_CACHE_MODIFIEDEXPIRY));
            this.urlMap = constructURLMap();
            configuredRequestRouter.build();
            this.setRequestRouter(configuredRequestRouter);
            if (configurations.get(GatewayConstants.GATEWAY_THROTTLE_DATAPUBLISHER) != null) {
                this.setThrottleDataPublisher((ThrottleDataPublisher) OpenBankingUtils
                        .getClassInstanceFromFQN(configurations.get(GatewayConstants.GATEWAY_THROTTLE_DATAPUBLISHER)
                                .toString()));
            }

            setAPIMAnalyticsEnabled((String) configurations.get(DataPublishingConstants.APIM_ANALYTICS_ENABLED));
            setOBDataPublishingEnabled((String) configurations.get(DataPublishingConstants.DATA_PUBLISHING_ENABLED));
            setWorkerThreadCount((String) configurations.get(DataPublishingConstants.WORKER_THREAD_COUNT));
            setClientTransportCertHeaderName((String) configurations.get(OpenBankingConstants.
                    CLIENT_TRANSPORT_CERT_HEADER_NAME));
            setUrlEncodeClientTransportCertHeaderEnabled((String) configurations.get(OpenBankingConstants.
                    URL_ENCODE_CLIENT_TRANSPORT_CERT_HEADER_ENABLED));
        }
    }

    public AbstractRequestRouter getRequestRouter() {

        return requestRouter;
    }

    public void setRequestRouter(
            AbstractRequestRouter requestRouter) {

        this.requestRouter = requestRouter;
    }

    public int getGatewayCacheAccessExpiry() {

        return gatewayCacheAccessExpiry;
    }

    public void setGatewayCacheAccessExpiry(String expTime) {

        this.gatewayCacheAccessExpiry = expTime == null ? 60 : Integer.parseInt(expTime);
    }

    public int getGatewayCacheModifiedExpiry() {

        return gatewayCacheModifiedExpiry;
    }

    public void setGatewayCacheModifiedExpiry(String expTime) {

        this.gatewayCacheModifiedExpiry = expTime == null ? 60 : Integer.parseInt(expTime);
    }

    public String getKeyStoreLocation() {

        return keyStoreLocation == null ? ServerConfiguration.getInstance()
                .getFirstProperty(GatewayConstants.KEYSTORE_LOCATION_TAG) : keyStoreLocation;

    }

    public void setKeyStoreLocation(String keyStoreLocation) {

        this.keyStoreLocation = keyStoreLocation;
    }

    public char[] getKeyStorePassword() {

        if (this.keyStorePassword == null) {
            char[] password = ServerConfiguration.getInstance()
                    .getFirstProperty(GatewayConstants.KEYSTORE_PASSWORD_TAG).toCharArray();
            this.keyStorePassword = password;
            return Arrays.copyOf(this.keyStorePassword, this.keyStorePassword.length);
        } else {
            return Arrays.copyOf(this.keyStorePassword, this.keyStorePassword.length);
        }
    }

    public void setKeyStorePassword(char[] keyStorePassword) {

        if (keyStorePassword != null) {
            this.keyStorePassword = Arrays.copyOf(keyStorePassword, keyStorePassword.length);
        }
    }

    public String getKeyAlias() {

        return keyAlias == null ? ServerConfiguration.getInstance()
                .getFirstProperty(GatewayConstants.SIGNING_ALIAS_TAG) : keyAlias;
    }

    public void setKeyAlias(String keyAlias) {

        this.keyAlias = keyAlias;
    }

    public String getKeyPassword() {

        return keyPassword == null ? ServerConfiguration.getInstance()
                .getFirstProperty(GatewayConstants.SIGNING_KEY_PASSWORD) : keyPassword;
    }

    public void setKeyPassword(String keyPassword) {

        this.keyPassword = keyPassword;
    }

    public void setApiManagerConfiguration(APIManagerConfigurationService apiManagerConfigurationService) {

        this.apiManagerConfigurationService = apiManagerConfigurationService;
    }

    public APIManagerConfigurationService getApiManagerConfigurationService() {

        return apiManagerConfigurationService;
    }

    public Map<String, Object> getUrlMap() {

        return urlMap;
    }

    public void setUrlMap(Map<String, Object> configurations) {

        this.urlMap = configurations;
    }

    private Map<String, Object> constructURLMap() {

        Map<String, Object> urlMap = new HashMap<>();
        //get admin credentials
        APIManagerConfiguration config = apiManagerConfigurationService.getAPIManagerConfiguration();

        String adminUsername = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        urlMap.put(GatewayConstants.USERNAME, adminUsername);

        char[] adminPassword = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD).toCharArray();
        urlMap.put(GatewayConstants.PASSWORD, adminPassword);

        //read APIM store hostname
        String apimStoreHostName = configurations.get(OpenBankingConstants.STORE_HOSTNAME).toString();
        if (!apimStoreHostName.endsWith("/")) {
            apimStoreHostName = apimStoreHostName.concat("/");
        }

        //set the url for obtaining a token
        String tokenURL = configurations.get(OpenBankingConstants.TOKEN_ENDPOINT).toString();
        urlMap.put(GatewayConstants.TOKEN_URL, tokenURL);

        //set the url for apim application creation
        String applicationCreationURL =
                apimStoreHostName.concat(configurations.get(OpenBankingConstants.APIM_APPCREATION).toString());
        urlMap.put(GatewayConstants.APP_CREATE_URL, applicationCreationURL);

        // set the url for mapping apim app keys to IAM service provider keys
        String mapApplicationKeysURL =
                apimStoreHostName.concat(configurations.get(OpenBankingConstants.APIM_KEYGENERATION).toString());
        urlMap.put(GatewayConstants.KEY_MAP_URL, mapApplicationKeysURL);

        //set the url to retrieve all published APIs
        String retrieveAPIsURL =
                apimStoreHostName.concat(configurations.get(OpenBankingConstants.APIM_GETAPIS).toString());
        urlMap.put(GatewayConstants.API_RETRIEVE_URL, retrieveAPIsURL);

        //set the url to subscribe to APIS
        String subscribeAPIsURL =
                apimStoreHostName.concat(configurations.get(OpenBankingConstants.APIM_SUBSCRIBEAPIS).toString());
        urlMap.put(GatewayConstants.API_SUBSCRIBE_URL, subscribeAPIsURL);

        //set the url to retrieve the subscriptions for an application
        //set the url to get subscribed APIS
        String retrieveSubscriptionURL =
                apimStoreHostName.concat(configurations.get(OpenBankingConstants.APIM_GETSUBSCRIPTIONS).toString());
        urlMap.put(GatewayConstants.API_GET_SUBSCRIBED, retrieveSubscriptionURL);

        String iamHostName = GatewayDataHolder.getInstance()
                .getApiManagerConfigurationService().getAPIManagerConfiguration()
                .getFirstProperty("APIKeyValidator.ServerURL").split("/services")[0];

        urlMap.put(GatewayConstants.IAM_HOSTNAME, iamHostName);

        String iamDCREndpoint = IdentityUtil.getProperty("OAuth.OAuth2DCREPUrl").split("/api/")[1];
        iamDCREndpoint = iamHostName.concat("/api/").concat(iamDCREndpoint);
        urlMap.put(GatewayConstants.IAM_DCR_URL, iamDCREndpoint);
        return urlMap;
    }

    public ThrottleDataPublisher getThrottleDataPublisher() {

        return throttleDataPublisher;
    }

    public void setThrottleDataPublisher(
            ThrottleDataPublisher throttleDataPublisher) {

        this.throttleDataPublisher = throttleDataPublisher;
    }

    public boolean isAPIMAnalyticsEnabled() {

        return isAPIMAnalyticsEnabled;
    }

    public void setAPIMAnalyticsEnabled(String apimAnalyticsEnabled) {

        isAPIMAnalyticsEnabled = Boolean.parseBoolean(apimAnalyticsEnabled);
    }

    public boolean isOBDataPublishingEnabled() {

        return isOBDataPublishingEnabled;
    }

    public void setOBDataPublishingEnabled(String obDataPublishingEnabled) {

        isOBDataPublishingEnabled = Boolean.parseBoolean(obDataPublishingEnabled);
    }

    public void setWorkerThreadCount(String workerThreadCount) {
        this.workerThreadCount = workerThreadCount;
    }

    public String getWorkerThreadCount() {

        return workerThreadCount;
    }

    public String getClientTransportCertHeaderName() {
        return clientTransportCertHeaderName;
    }

    public void setClientTransportCertHeaderName(String clientTransportCertHeaderName) {
        this.clientTransportCertHeaderName = clientTransportCertHeaderName;
    }

    public boolean isUrlEncodeClientTransportCertHeaderEnabled() {
        return isUrlEncodeClientTransportCertHeaderEnabled;
    }

    public void setUrlEncodeClientTransportCertHeaderEnabled(String isUrlEncodeClientTransportCertHeaderEnabled) {
        this.isUrlEncodeClientTransportCertHeaderEnabled =
                Boolean.parseBoolean(isUrlEncodeClientTransportCertHeaderEnabled);
    }
}
