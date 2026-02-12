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

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationService;
import org.wso2.financial.services.accelerator.data.publisher.internal.FSAnalyticsDataHolder;
import org.wso2.financial.services.accelerator.data.publisher.util.FSDataPublisherUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data publisher pool test.
 */
public class DataPublisherPoolTest {

    private DataPublisherPool<FinancialServicesDataPublisher> pool;

    @Test(priority = 1)
    public void testInitializePool() {

        Map<String, Object> configs = new HashMap<>();
        configs.put("DataPublishing.PoolSize", "3");
        configs.put("DataPublishing.PoolWaitTimeMs", "500");
        configs.put("DataPublishing.Enabled", "true");
        FinancialServicesConfigurationService financialServicesConfigurationService =
                Mockito.mock(FinancialServicesConfigurationService.class);
        Mockito.when(financialServicesConfigurationService.getConfigurations()).thenReturn(configs);
        FSAnalyticsDataHolder.getInstance().
                setFinancialServicesConfigurationService(financialServicesConfigurationService);

        FSAnalyticsDataHolder.getInstance().initializePool();
        pool = FSAnalyticsDataHolder.getInstance().getDataPublisherPool();
        Assert.assertEquals(pool.getCreatedCount(), 0);
        Assert.assertEquals(pool.getMaxTotal(), 3);
        Assert.assertEquals(pool.getMaxIdle(), 3);
        Assert.assertEquals(pool.getBorrowedCount(), 0);
    }

    @Test(priority = 2)
    public void testBorrowInstances() {

        FinancialServicesDataPublisher instance = FSDataPublisherUtil.getDataPublisherInstance();
        Assert.assertEquals(pool.getCreatedCount(), 1);
        Assert.assertEquals(pool.getBorrowedCount(), 1);
        Assert.assertEquals(pool.getNumIdle(), 0);
        FSDataPublisherUtil.releaseDataPublishingInstance(instance);
        Assert.assertEquals(pool.getNumIdle(), 1);
    }

    @Test(priority = 3)
    public void tryBorrowOverMaxLimit() throws InterruptedException {

        List<FinancialServicesDataPublisher> instances = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            instances.add(FSDataPublisherUtil.getDataPublisherInstance());
        }
        Assert.assertEquals(pool.getCreatedCount(), 3);
        Assert.assertEquals(pool.getBorrowedCount(), 4);
    }

    @Test(priority = 100)
    public void testPoolClose() {
        FSAnalyticsDataHolder.getInstance().closePool();
        Assert.assertTrue(pool.isClosed());
    }
}
