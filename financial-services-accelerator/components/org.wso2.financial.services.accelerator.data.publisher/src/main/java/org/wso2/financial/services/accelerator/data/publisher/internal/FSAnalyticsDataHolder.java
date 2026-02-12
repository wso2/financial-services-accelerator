/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.data.publisher.internal;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationService;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.data.publisher.DataPublisherFactory;
import org.wso2.financial.services.accelerator.data.publisher.DataPublisherPool;
import org.wso2.financial.services.accelerator.data.publisher.EventQueue;
import org.wso2.financial.services.accelerator.data.publisher.FinancialServicesDataPublisher;
import org.wso2.financial.services.accelerator.data.publisher.FinancialServicesThriftDataPublisher;
import org.wso2.financial.services.accelerator.data.publisher.constants.DataPublishingConstants;


import java.util.Map;

import static org.wso2.financial.services.accelerator.data.publisher.constants.DataPublishingConstants.DATA_PUBLISHING_CONFIG_TAG;

/**
 * Data holder for Open Banking Analytics.
 */
public class FSAnalyticsDataHolder {

    private static volatile FSAnalyticsDataHolder instance;
    private FinancialServicesConfigurationService financialServicesConfigurationService;
    private Map<String, Object> configurationMap;
    private DataPublisherPool<FinancialServicesDataPublisher> pool;
    private int poolSize;
    private EventQueue eventQueue;
    private String financialServicesDataPublisherFQN;

    public static FSAnalyticsDataHolder getInstance() {

        if (instance == null) {
            synchronized (FSAnalyticsDataHolder.class) {
                if (instance == null) {
                    instance = new FSAnalyticsDataHolder();
                }
            }
        }
        return instance;
    }

    public Map<String, Object> getConfigurationMap() {

        return configurationMap;
    }

    public FinancialServicesConfigurationService getFinancialServicesConfigurationService() {

        return financialServicesConfigurationService;
    }

    public void setFinancialServicesConfigurationService(
            FinancialServicesConfigurationService financialServicesConfigurationService) {

        this.financialServicesConfigurationService = financialServicesConfigurationService;
        this.configurationMap = financialServicesConfigurationService.getConfigurations();
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
     * If it is not already initialized, a default implementation {@link FinancialServicesThriftDataPublisher}
     * is assigned.
     *
     * @return the {@code FinancialServicesDataPublisher} instance
     */
    public FinancialServicesDataPublisher getFinancialServicesDataPublisher() {

        if (StringUtils.isBlank(this.financialServicesDataPublisherFQN)) {
            return new FinancialServicesThriftDataPublisher();
        } else {
            return (FinancialServicesDataPublisher)
                    FinancialServicesUtils.getClassInstanceFromFQN(financialServicesDataPublisherFQN);
        }
    }

    /**
     * Sets the Data Publisher Implementation class name.
     *
     * @param financialServicesDataPublisherFQN the Data Publisher Implementation class name
     */
    public void setFinancialServicesDataPublisherFQN(final String financialServicesDataPublisherFQN) {

        this.financialServicesDataPublisherFQN = financialServicesDataPublisherFQN;
    }
}
