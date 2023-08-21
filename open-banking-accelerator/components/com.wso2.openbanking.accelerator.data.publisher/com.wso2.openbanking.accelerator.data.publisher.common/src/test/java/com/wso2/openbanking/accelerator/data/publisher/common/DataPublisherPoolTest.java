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

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import com.wso2.openbanking.accelerator.data.publisher.common.internal.OBAnalyticsDataHolder;
import com.wso2.openbanking.accelerator.data.publisher.common.util.OBDataPublisherUtil;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data publisher pool test.
 */
public class DataPublisherPoolTest {

    private DataPublisherPool<OpenBankingDataPublisher> pool;

    @Test(priority = 1)
    public void testInitializePool() {

        Map<String, Object> configs = new HashMap<>();
        configs.put("DataPublishing.PoolSize", "3");
        configs.put("DataPublishing.PoolWaitTimeMs", "500");
        configs.put("DataPublishing.Enabled", "true");
        OpenBankingConfigurationService openBankingConfigurationService =
                Mockito.mock(OpenBankingConfigurationService.class);
        Mockito.when(openBankingConfigurationService.getConfigurations()).thenReturn(configs);
        OBAnalyticsDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);

        OBAnalyticsDataHolder.getInstance().initializePool();
        pool = OBAnalyticsDataHolder.getInstance().getDataPublisherPool();
        Assert.assertEquals(pool.getCreatedCount(), 0);
        Assert.assertEquals(pool.getMaxTotal(), 3);
        Assert.assertEquals(pool.getMaxIdle(), 3);
        Assert.assertEquals(pool.getBorrowedCount(), 0);
    }

    @Test(priority = 2)
    public void testBorrowInstances() {

        OpenBankingDataPublisher instance = OBDataPublisherUtil.getDataPublisherInstance();
        Assert.assertEquals(pool.getCreatedCount(), 1);
        Assert.assertEquals(pool.getBorrowedCount(), 1);
        Assert.assertEquals(pool.getNumIdle(), 0);
        OBDataPublisherUtil.releaseDataPublishingInstance(instance);
        Assert.assertEquals(pool.getNumIdle(), 1);
    }

    @Test(priority = 3)
    public void tryBorrowOverMaxLimit() throws InterruptedException {

        List<OpenBankingDataPublisher> instances = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            instances.add(OBDataPublisherUtil.getDataPublisherInstance());
        }
        Assert.assertEquals(pool.getCreatedCount(), 3);
        Assert.assertEquals(pool.getBorrowedCount(), 4);
    }

    @Test(priority = 100)
    public void testPoolClose() {
        OBAnalyticsDataHolder.getInstance().closePool();
        Assert.assertTrue(pool.isClosed());
    }
}
