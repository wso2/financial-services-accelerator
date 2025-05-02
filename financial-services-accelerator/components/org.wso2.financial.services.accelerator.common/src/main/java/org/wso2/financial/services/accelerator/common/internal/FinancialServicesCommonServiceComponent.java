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

package org.wso2.financial.services.accelerator.common.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationService;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationServiceImpl;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.util.HTTPClientUtils;

/**
 * Method to register Financial Services common OSGi Services.
 */
@Component(
        name = "org.wso2.financial.services.accelerator.common",
        immediate = true
)
public class FinancialServicesCommonServiceComponent {

    private static final Log log = LogFactory.getLog(FinancialServicesCommonServiceComponent.class);
    private PoolingHttpClientConnectionManager connectionManager;
    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";

    @Activate
    protected void activate(ComponentContext context) {

        FinancialServicesConfigurationService financialServicesConfigurationService
                = new FinancialServicesConfigurationServiceImpl();
        context.getBundleContext().registerService(FinancialServicesConfigurationService.class.getName(),
                financialServicesConfigurationService, null);
        initConnectionManagerForHttpsProtocol();
        log.debug("Financial Services common component is activated successfully");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("Financial Services common component is deactivated");
    }

    /**
     * Initialize the connection manager for HTTPS protocol.
     */
    private void initConnectionManagerForHttpsProtocol() {

        int maxTotal = FinancialServicesConfigParser.getInstance().getConnectionPoolMaxConnections();
        int maxPerRoute = FinancialServicesConfigParser.getInstance().getConnectionPoolMaxConnectionsPerRoute();

        try {
            SSLConnectionSocketFactory sslsf = HTTPClientUtils.createSSLConnectionSocketFactory();
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                    .<ConnectionSocketFactory>create()
                    .register(HTTP_PROTOCOL, new PlainConnectionSocketFactory())
                    .register(HTTPS_PROTOCOL, sslsf)
                    .build();

            connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

            connectionManager.setMaxTotal(maxTotal);
            connectionManager.setDefaultMaxPerRoute(maxPerRoute);
            FinancialServicesCommonDataHolder.getInstance().setConnectionManager(connectionManager);
        } catch (FinancialServicesException e) {
            if (connectionManager != null) {
                connectionManager.close();
            }
            log.error("Error occurred while creating the connection manager", e);
        }
    }
}
