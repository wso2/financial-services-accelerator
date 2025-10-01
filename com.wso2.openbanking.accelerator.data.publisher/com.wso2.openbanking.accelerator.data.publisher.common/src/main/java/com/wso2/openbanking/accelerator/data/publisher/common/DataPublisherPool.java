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

package com.wso2.openbanking.accelerator.data.publisher.common;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Data Publisher Pool class.
 * @param <FinancialServicesDataPublisher>
 */
public class DataPublisherPool<FinancialServicesDataPublisher> extends GenericObjectPool<FinancialServicesDataPublisher> {

    public DataPublisherPool(PooledObjectFactory<FinancialServicesDataPublisher> factory,
                             GenericObjectPoolConfig<FinancialServicesDataPublisher> config) {

        super(factory, config);
    }
}
