/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.common.internal;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.event.executor.OBEventQueue;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;

import java.util.Map;

/**
 * Data holder for Open Banking Common module.
 */
public class OpenBankingCommonDataHolder {

    private static volatile OpenBankingCommonDataHolder instance;
    private OBEventQueue obEventQueue;
    private Map<Integer, String> obEventExecutors;
    private ApplicationManagementService applicationManagementService;
    private int commonCacheAccessExpiry;
    private int commonCacheModifiedExpiry;

    private OpenBankingCommonDataHolder() {

        int queueSize = Integer.parseInt((String) OpenBankingConfigParser.getInstance().getConfiguration()
                .get(OpenBankingConstants.EVENT_QUEUE_SIZE));
        int workerThreadCount =
                Integer.parseInt((String) OpenBankingConfigParser.getInstance().getConfiguration()
                        .get(OpenBankingConstants.EVENT_WORKER_THREAD_COUNT));
        obEventQueue = new OBEventQueue(queueSize, workerThreadCount);
        obEventExecutors = OpenBankingConfigParser.getInstance().getOpenBankingEventExecutors();
        setCommonCacheAccessExpiry((String) OpenBankingConfigParser.getInstance().getConfiguration()
                .get(OpenBankingConstants.COMMON_IDENTITY_CACHE_ACCESS_EXPIRY));
        setCommonCacheModifiedExpiry((String) OpenBankingConfigParser.getInstance().getConfiguration()
                .get(OpenBankingConstants.COMMON_IDENTITY_CACHE_MODIFY_EXPIRY));
    }

    public static OpenBankingCommonDataHolder getInstance() {

        if (instance == null) {
            synchronized (OpenBankingCommonDataHolder.class) {
                if (instance == null) {
                    instance = new OpenBankingCommonDataHolder();
                }
            }
        }
        return instance;
    }

    public Map<Integer, String> getOBEventExecutors() {

        return obEventExecutors;
    }

    public void setOBEventExecutor(Map<Integer, String> obEventExecutors) {

        this.obEventExecutors = obEventExecutors;
    }

    public OBEventQueue getOBEventQueue() {

        return obEventQueue;
    }

    public void setOBEventQueue(OBEventQueue obEventQueue) {

        this.obEventQueue = obEventQueue;
    }

    /**
     * To get the instance of {@link ApplicationManagementService}.
     *
     * @return applicationManagementService
     */
    public ApplicationManagementService getApplicationManagementService() {

        return applicationManagementService;
    }

    /**
     * To set the ApplicationManagementService.
     *
     * @param applicationManagementService instance of {@link ApplicationManagementService}
     */
    public void setApplicationManagementService(ApplicationManagementService applicationManagementService) {

        this.applicationManagementService = applicationManagementService;
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
}
