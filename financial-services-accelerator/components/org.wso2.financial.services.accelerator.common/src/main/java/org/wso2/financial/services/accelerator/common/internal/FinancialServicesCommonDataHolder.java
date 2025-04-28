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

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;

import java.security.KeyStore;

/**
 * Data holder for Common module.
 */
public class FinancialServicesCommonDataHolder {

    private static volatile FinancialServicesCommonDataHolder instance;
    private int commonCacheAccessExpiry;
    private int commonCacheModifiedExpiry;
    private KeyStore trustStore = null;
    private PoolingHttpClientConnectionManager connectionManager;
    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";

    private FinancialServicesCommonDataHolder() {

        setCommonCacheAccessExpiry((String) FinancialServicesConfigParser.getInstance().getConfiguration()
                .get(FinancialServicesConstants.COMMON_IDENTITY_CACHE_ACCESS_EXPIRY));
        setCommonCacheModifiedExpiry((String) FinancialServicesConfigParser.getInstance().getConfiguration()
                .get(FinancialServicesConstants.COMMON_IDENTITY_CACHE_MODIFY_EXPIRY));
    }

    public static FinancialServicesCommonDataHolder getInstance() {

        if (instance == null) {
            synchronized (FinancialServicesCommonDataHolder.class) {
                if (instance == null) {
                    instance = new FinancialServicesCommonDataHolder();
                }
            }
        }
        return instance;
    }

    public int getCommonCacheAccessExpiry() {

        return commonCacheAccessExpiry;
    }

    public void setCommonCacheAccessExpiry(String expTime) {

        this.commonCacheAccessExpiry = expTime == null ? 60 : Integer.parseInt(expTime);
    }

    public int getCommonCacheModifiedExpiry() {

        return commonCacheModifiedExpiry;
    }

    public void setCommonCacheModifiedExpiry(String expTime) {

        this.commonCacheModifiedExpiry = expTime == null ? 60 : Integer.parseInt(expTime);
    }

    public KeyStore getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(KeyStore trustStore) {
        this.trustStore = trustStore;
    }

    public PoolingHttpClientConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void setConnectionManager(PoolingHttpClientConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
}
