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

package org.wso2.financial.services.accelerator.gateway.internal;

import org.apache.http.impl.client.CloseableHttpClient;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationService;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.HTTPClientUtils;
import org.wso2.financial.services.accelerator.gateway.cache.GatewayCache;
import org.wso2.financial.services.accelerator.gateway.executor.core.AbstractRequestRouter;

import java.util.Map;

/**
 * Data holder for executor core
 */
public class GatewayDataHolder {

    private static volatile GatewayDataHolder instance;
    private static volatile CloseableHttpClient httpClient;
    private static volatile GatewayCache gatewayCache;
    private FinancialServicesConfigurationService financialServicesConfigurationService;
    private int gatewayCacheAccessExpiry;
    private int gatewayCacheModifiedExpiry;
    private APIManagerConfigurationService apiManagerConfigurationService;
    private AbstractRequestRouter requestRouter;

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

    public static CloseableHttpClient getHttpClient() throws FinancialServicesException {

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

    public FinancialServicesConfigurationService getFinancialServicesConfigurationService() {

        return financialServicesConfigurationService;
    }

    public void setFinancialServicesConfigurationService(
            FinancialServicesConfigurationService financialServicesConfigurationService) {

        this.financialServicesConfigurationService = financialServicesConfigurationService;
        if (financialServicesConfigurationService != null) {
            Map<String, Object> configurations = financialServicesConfigurationService.getConfigurations();
            setGatewayCacheAccessExpiry((String) configurations.get(FinancialServicesConstants.GATEWAY_CACHE_EXPIRY));
            setGatewayCacheModifiedExpiry((String) configurations
                    .get(FinancialServicesConstants.GATEWAY_CACHE_MODIFIED_EXPIRY));
            AbstractRequestRouter configuredRequestRouter = (AbstractRequestRouter) FinancialServicesUtils
                    .getClassInstanceFromFQN(configurations.get(FinancialServicesConstants.REQUEST_ROUTER).toString());
            configuredRequestRouter.build();
            this.setRequestRouter(configuredRequestRouter);
        }
    }

    public AbstractRequestRouter getRequestRouter() {

        return requestRouter;
    }

    public void setRequestRouter(AbstractRequestRouter requestRouter) {

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

    public void setApiManagerConfiguration(APIManagerConfigurationService apiManagerConfigurationService) {

        this.apiManagerConfigurationService = apiManagerConfigurationService;
    }

    public APIManagerConfigurationService getApiManagerConfigurationService() {

        return apiManagerConfigurationService;
    }
}
