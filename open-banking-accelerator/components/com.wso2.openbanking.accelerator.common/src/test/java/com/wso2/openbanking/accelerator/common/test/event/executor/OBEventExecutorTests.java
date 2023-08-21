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

package com.wso2.openbanking.accelerator.common.test.event.executor;

import com.wso2.openbanking.accelerator.common.event.executor.DefaultOBEventExecutor;
import com.wso2.openbanking.accelerator.common.event.executor.OBEventQueue;
import com.wso2.openbanking.accelerator.common.event.executor.model.OBEvent;
import com.wso2.openbanking.accelerator.common.internal.OpenBankingCommonDataHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test for Open Banking event executor.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({OpenBankingCommonDataHolder.class})
public class OBEventExecutorTests extends PowerMockTestCase {

    private static ByteArrayOutputStream outContent;
    private static Logger logger = null;
    private static PrintStream printStream;

    @BeforeClass
    public void beforeTests() {

        outContent = new ByteArrayOutputStream();
        printStream = new PrintStream(outContent);
        System.setOut(printStream);
        logger = LogManager.getLogger(OBEventExecutorTests.class);
    }

    @Test
    public void testAddingDataToQueue() {

        outContent.reset();
        Map<String, Object> configs = new HashMap<>();
        configs.put("Event.WorkerThreadCount", "3");
        configs.put("Event.QueueSize", "10");

        OBEvent obEvent = new OBEvent("revoked", new HashMap<>());

        OpenBankingCommonDataHolder openBankingCommonDataHolderMock = mock(OpenBankingCommonDataHolder.class);
        mockStatic(OpenBankingCommonDataHolder.class);
        when(OpenBankingCommonDataHolder.getInstance()).thenReturn(openBankingCommonDataHolderMock);

        when(openBankingCommonDataHolderMock.getOBEventQueue()).thenReturn(new OBEventQueue(Integer
                .parseInt(configs.get("Event.QueueSize").toString()), Integer.parseInt(configs
                .get("Event.WorkerThreadCount").toString())));

        Map<Integer, String>  obEventExecutors = new HashMap<>();
        obEventExecutors.put(1, DefaultOBEventExecutor.class.getName());
        when(openBankingCommonDataHolderMock.getOBEventExecutors()).thenReturn(obEventExecutors);


        OBEventQueue obEventQueue = openBankingCommonDataHolderMock.getOBEventQueue();

        obEventQueue.put(obEvent);
        // there should be an error log or a warning if the queue is full.
        Assert.assertTrue(outContent.toString().isEmpty());
    }
}
