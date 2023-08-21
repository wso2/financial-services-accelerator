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

package com.wso2.openbanking.accelerator.common.test.distributed.caching;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants;
import com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedMember;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Unit test for open banking distributed cache with TCP discovery method.
 */
@PowerMockIgnore({"jdk.internal.reflect.*", "javax.management.*"})
@PrepareForTest({OpenBankingConfigParser.class})
public class OpenBankingDistributedCacheTCPTest extends PowerMockTestCase {


    private static TestOpenBankingDistributedCache cacheTCP;

    @Mock
    OpenBankingConfigParser openBankingConfigParser;


    @BeforeClass
    public void beforeTests() {
        MockitoAnnotations.initMocks(this);

        Map<String, Object> configsTCP = new HashMap<>();
        configsTCP.put(OpenBankingDistributedCacheConstants.ENABLED, "true");
        configsTCP.put(OpenBankingDistributedCacheConstants.HOST_NAME, "localhost");
        configsTCP.put(OpenBankingDistributedCacheConstants.PORT, "5721");
        configsTCP.put(OpenBankingDistributedCacheConstants.DISCOVERY_MECHANISM, "TCP");
        ArrayList<String> members = new ArrayList<>();
        members.add("localhost:5722");
        configsTCP.put(OpenBankingDistributedCacheConstants.MEMBERS, members);
        configsTCP.put(OpenBankingDistributedCacheConstants.PROPERTY_LOGGING_TYPE, "none");

        Mockito.when(openBankingConfigParser.getConfiguration()).thenReturn(configsTCP);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);

        cacheTCP = new TestOpenBankingDistributedCache("test-cache-tcp");

    }

    @Test(priority = 1)
    public void addGetTestTCP() {
        TestOpenBankingDistributedCacheKey key = new TestOpenBankingDistributedCacheKey("test-cache-key");
        cacheTCP.addToCache(key, "cache-body");
        String fromCache = null;
        if (!cacheTCP.isEmpty()) {
            fromCache = cacheTCP.getFromCache(key);
        }

        Assert.assertEquals(fromCache, "cache-body");
    }

    @Test(priority = 2)
    public void removeTestTCP() {
        TestOpenBankingDistributedCacheKey key = new TestOpenBankingDistributedCacheKey("test-cache-key");
        cacheTCP.removeFromCache(key);

        String fromCache = cacheTCP.getFromCache(key);

        Assert.assertNull(fromCache);
    }

    @Test(priority = 3)
    public void cacheEvictionTestTCP() throws InterruptedException {
        TestOpenBankingDistributedCacheKey key = new TestOpenBankingDistributedCacheKey("test-cache-key");
        cacheTCP.addToCache(key, "cache-body");
        TimeUnit.MINUTES.sleep(2);

        String fromCache = cacheTCP.getFromCache(key);
        Assert.assertNull(fromCache);
    }


    @AfterClass
    public void after() {
        OpenBankingDistributedMember.of().shutdown();
    }
}
