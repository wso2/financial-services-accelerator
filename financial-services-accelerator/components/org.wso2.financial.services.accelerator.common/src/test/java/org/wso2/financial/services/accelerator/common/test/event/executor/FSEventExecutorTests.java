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

package org.wso2.financial.services.accelerator.common.test.event.executor;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.event.executor.DefaultFSEventExecutor;
import org.wso2.financial.services.accelerator.common.event.executor.FSEventQueue;
import org.wso2.financial.services.accelerator.common.event.executor.model.FSEvent;
import org.wso2.financial.services.accelerator.common.internal.FinancialServicesCommonDataHolder;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;




/**
 * Test for Open Banking event executor.
 */

public class FSEventExecutorTests {

    private static ByteArrayOutputStream outContent;
    private static Logger logger = null;
    private static PrintStream printStream;

    @Mock
    private FinancialServicesCommonDataHolder financialServicesCommonDataHolderMock;
    MockedStatic<FinancialServicesCommonDataHolder> financialServicesDataHolderMockedStatic;


    @BeforeClass
    public void initTest() {
        financialServicesCommonDataHolderMock = Mockito.mock(FinancialServicesCommonDataHolder.class);
    }


    @BeforeClass
    private void mockStaticClasses() {
        financialServicesDataHolderMockedStatic = mockStatic(FinancialServicesCommonDataHolder.class);
        financialServicesDataHolderMockedStatic.when(FinancialServicesCommonDataHolder::getInstance)
                .thenReturn(financialServicesCommonDataHolderMock);

    }

    @BeforeClass
    public void beforeTests() {

        outContent = new ByteArrayOutputStream();
        printStream = new PrintStream(outContent);
        System.setOut(printStream);
        logger = LogManager.getLogger(FSEventExecutorTests.class);
    }

    @Test
    public void testAddingDataToQueue() {

        outContent.reset();
        Map<String, Object> configs = new HashMap<>();
        configs.put("Event.WorkerThreadCount", "3");
        configs.put("Event.QueueSize", "10");

        FSEvent obEvent = new FSEvent("revoked", new HashMap<>());


        when(financialServicesCommonDataHolderMock.getFSEventQueue()).thenReturn(new FSEventQueue(Integer
                .parseInt(configs.get("Event.QueueSize").toString()), Integer.parseInt(configs
                .get("Event.WorkerThreadCount").toString())));

        Map<Integer, String>  obEventExecutors = new HashMap<>();
        obEventExecutors.put(1, DefaultFSEventExecutor.class.getName());
        when(financialServicesCommonDataHolderMock.getFSEventExecutors()).thenReturn(obEventExecutors);


        FSEventQueue fsEventQueue = financialServicesCommonDataHolderMock.getFSEventQueue();

        fsEventQueue.put(obEvent);
        // there should be an error log or a warning if the queue is full.
        Assert.assertFalse(outContent.toString().isEmpty());
    }

}
