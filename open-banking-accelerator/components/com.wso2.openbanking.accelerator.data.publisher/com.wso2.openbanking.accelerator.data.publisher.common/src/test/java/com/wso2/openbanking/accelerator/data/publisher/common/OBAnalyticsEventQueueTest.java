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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Open Banking analytics event queue test.
 */
public class OBAnalyticsEventQueueTest {

    private static ByteArrayOutputStream outContent;
    private static Logger logger = null;
    private static PrintStream printStream;

    @BeforeClass
    public void beforeTests() {

        outContent = new ByteArrayOutputStream();
        printStream = new PrintStream(outContent);
        System.setOut(printStream);
        logger = LogManager.getLogger(OBAnalyticsEventQueueTest.class);
    }

    @Test
    public void testAddingDataToQueue() {

        outContent.reset();
        Map<String, Object> configs = new HashMap<>();
        configs.put("DataPublishing.WorkerThreadCount", "3");
        configs.put("DataPublishing.QueueSize", "10");
        configs.put("DataPublishing.Enabled", "true");

        OpenBankingConfigurationService openBankingConfigurationService =
                Mockito.mock(OpenBankingConfigurationService.class);
        Mockito.when(openBankingConfigurationService.getConfigurations()).thenReturn(configs);
        OBAnalyticsDataHolder.getInstance().setEventQueue(Mockito.mock(EventQueue.class));
        OBAnalyticsDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);

        OBDataPublisherUtil.publishData("testStream", "1.0", configs);
        Assert.assertTrue(outContent.toString().isEmpty());
    }

    @Test
    public void tryAddingToQueueWhenDataPublishingDisabled() {

        outContent.reset();
        Map<String, Object> configs = new HashMap<>();
        configs.put("DataPublishing.WorkerThreadCount", "3");
        configs.put("DataPublishing.QueueSize", "10");
        configs.put("DataPublishing.Enabled", "false");

        OpenBankingConfigurationService openBankingConfigurationService =
                Mockito.mock(OpenBankingConfigurationService.class);
        Mockito.when(openBankingConfigurationService.getConfigurations()).thenReturn(configs);
        OBAnalyticsDataHolder.getInstance().setEventQueue(Mockito.mock(EventQueue.class));
        OBAnalyticsDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);

        OBDataPublisherUtil.publishData("testStream", "1.0", configs);
        Assert.assertTrue(outContent.toString().contains("Data publishing is disabled. " +
                "Failed to obtain a data publisher instance."));
    }
}
