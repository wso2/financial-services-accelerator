/**
 * Copyright (c) 2023-2025, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.data.publisher.common.internal;

import com.wso2.openbanking.accelerator.common.config.FinancialServicesConfigurationService;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.FinancialServicesUtils;
import com.wso2.openbanking.accelerator.data.publisher.common.DataPublisherFactory;
import com.wso2.openbanking.accelerator.data.publisher.common.DataPublisherPool;
import com.wso2.openbanking.accelerator.data.publisher.common.EventQueue;
import com.wso2.openbanking.accelerator.data.publisher.common.OBThriftDataPublisher;
import com.wso2.openbanking.accelerator.data.publisher.common.FinancialServicesDataPublisher;
import com.wso2.openbanking.accelerator.data.publisher.common.constants.DataPublishingConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.Map;

import static com.wso2.openbanking.accelerator.data.publisher.common.constants.DataPublishingConstants.DATA_PUBLISHING_CONFIG_TAG;

/**
 * Data holder for Open Banking Analytics.
 */
public class OBAnalyticsDataHolder {

    private static volatile OBAnalyticsDataHolder instance;
    private FinancialServicesConfigurationService FinancialServicesConfigurationService;
    private Map<String, Object> configurationMap;
    private DataPublisherPool<FinancialServicesDataPublisher> pool;
    private int poolSize;
    private EventQueue eventQueue;
    private String FinancialServicesDataPublisherFQN;

    public static OBAnalyticsDataHolder getInstance() {

        if (instance == null) {
            synchronized (OBAnalyticsDataHolder.class) {
                if (instance == null) {
                    instance = new OBAnalyticsDataHolder();
                }
            }
        }
        return instance;
    }

    public Map<String, Object> getConfigurationMap() {

        return configurationMap;
    }

    public FinancialServicesConfigurationService getFinancialServicesConfigurationService() {

        return FinancialServicesConfigurationService;
    }

    public void setFinancialServicesConfigurationService(
            FinancialServicesConfigurationService FinancialServicesConfigurationService) {

        this.FinancialServicesConfigurationService = FinancialServicesConfigurationService;
        this.configurationMap = FinancialServicesConfigurationService.getConfigurations();
        this.setFinancialServicesDataPublisher();
    }

    /**
     * Initialize pool of data publishers.
     */
    public void initializePool() {

        GenericObjectPoolConfig<FinancialServicesDataPublisher> config = new GenericObjectPoolConfig<>();
        poolSize = Integer.parseInt((String) configurationMap.get(DataPublishingConstants.DATA_PUBLISHING_POOL_SIZE));
        int timeout = Integer.parseInt(
                (String) configurationMap.get(DataPublishingConstants.DATA_PUBLISHING_POOL_WAIT_TIME));
        config.setMaxIdle(poolSize);
        config.setMaxTotal(poolSize);
        config.setMaxWaitMillis(timeout);
        pool = new DataPublisherPool<>(new DataPublisherFactory<>(), config);
    }

    public DataPublisherPool<FinancialServicesDataPublisher> getDataPublisherPool() {

        return pool;
    }

    public void closePool() {

        pool.close();
    }

    public void initializeEventQueue() {

        int queueSize = Integer.parseInt((String) configurationMap.get(DataPublishingConstants.QUEUE_SIZE));
        int workerThreadCount =
                Integer.parseInt((String) configurationMap.get(DataPublishingConstants.WORKER_THREAD_COUNT));
        eventQueue = new EventQueue(queueSize, workerThreadCount);
    }

    public EventQueue getEventQueue() {

        return eventQueue;
    }

    @Generated(message = "Event queue setter for testing purposes")
    public void setEventQueue(EventQueue eventQueue) {

        this.eventQueue = eventQueue;
    }

    private void setFinancialServicesDataPublisher() {

        if (this.configurationMap != null && this.configurationMap.get(DATA_PUBLISHING_CONFIG_TAG) != null) {
            this.setFinancialServicesDataPublisherFQN(this.configurationMap.get(DATA_PUBLISHING_CONFIG_TAG).toString());
        }
    }

    /**
     * Retrieves the {@link FinancialServicesDataPublisher} instance.
     * If it is not already initialized, a default implementation {@link OBThriftDataPublisher} is assigned.
     *
     * @return the {@code FinancialServicesDataPublisher} instance
     */
    public FinancialServicesDataPublisher getFinancialServicesDataPublisher() {

        if (StringUtils.isBlank(this.FinancialServicesDataPublisherFQN)) {
            return new OBThriftDataPublisher();
        } else {
            return (FinancialServicesDataPublisher) FinancialServicesUtils.getClassInstanceFromFQN(FinancialServicesDataPublisherFQN);
        }
    }

    /**
     * Sets the Data Publisher Implementation class name.
     *
     * @param FinancialServicesDataPublisherFQN the Data Publisher Implementation class name
     */
    public void setFinancialServicesDataPublisherFQN(final String FinancialServicesDataPublisherFQN) {

        this.FinancialServicesDataPublisherFQN = FinancialServicesDataPublisherFQN;
    }
}
