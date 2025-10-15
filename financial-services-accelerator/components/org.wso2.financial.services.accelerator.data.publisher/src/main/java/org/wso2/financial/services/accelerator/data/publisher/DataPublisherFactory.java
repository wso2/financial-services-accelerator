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

package org.wso2.financial.services.accelerator.data.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.wso2.financial.services.accelerator.data.publisher.internal.FSAnalyticsDataHolder;

/**
 * Data Publisher Factory class.
 *
 * @param <FinancialServicesDataPublisher>
 */
public class DataPublisherFactory<FinancialServicesDataPublisher> extends
        BasePooledObjectFactory<FinancialServicesDataPublisher> {

    private static final Log log = LogFactory.getLog(DataPublisherFactory.class);
    @Override
    public FinancialServicesDataPublisher create() {
        log.debug("Creating new FinancialServicesDataPublisher instance");
        return (FinancialServicesDataPublisher) FSAnalyticsDataHolder.getInstance().getFinancialServicesDataPublisher();
    }

    @Override
    public PooledObject<FinancialServicesDataPublisher> wrap(FinancialServicesDataPublisher
                                                                     financialServicesDataPublisher) {
        log.debug("Wrapping FinancialServicesDataPublisher instance in pooled object");
        return new DefaultPooledObject<FinancialServicesDataPublisher>(financialServicesDataPublisher);
    }
}
