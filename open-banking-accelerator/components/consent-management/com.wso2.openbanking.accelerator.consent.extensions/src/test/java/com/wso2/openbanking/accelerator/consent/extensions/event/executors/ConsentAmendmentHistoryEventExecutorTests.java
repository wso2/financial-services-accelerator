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

package com.wso2.openbanking.accelerator.consent.extensions.event.executors;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.event.executor.model.OBEvent;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.Mockito;
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
import java.util.UUID;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
/**
 * Test class for ConsentAmendmentHistoryEventExecutor.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({OpenBankingConfigParser.class, ConsentExtensionsDataHolder.class})
public class ConsentAmendmentHistoryEventExecutorTests extends PowerMockTestCase {

    private static ByteArrayOutputStream outContent;
    private static Logger logger = null;
    private static PrintStream printStream;
    private ConsentCoreServiceImpl consentCoreServiceImpl;
    private String sampleConsentID;

    @BeforeClass
    public void initTest() {

        consentCoreServiceImpl = Mockito.mock(ConsentCoreServiceImpl.class);
    }

    @BeforeClass
    public void beforeTests() {

        sampleConsentID = UUID.randomUUID().toString();
        outContent = new ByteArrayOutputStream();
        printStream = new PrintStream(outContent);
        System.setOut(printStream);
        logger = LogManager.getLogger(ConsentAmendmentHistoryEventExecutorTests.class);
    }

    @Test
    public void testProcessEventSuccess() throws Exception {

        ConsentAmendmentHistoryEventExecutor consentAmendmentHistoryEventExecutorSpy =
                Mockito.spy(new ConsentAmendmentHistoryEventExecutor());

        outContent.reset();

        OpenBankingConfigParser openBankingConfigParserMock = mock(OpenBankingConfigParser.class);
        ConsentExtensionsDataHolder consentExtensionsDataHolderMock = mock(ConsentExtensionsDataHolder.class);
        mockStatic(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
        when(openBankingConfigParserMock.isConsentAmendmentHistoryEnabled()).thenReturn(true);

        mockStatic(ConsentExtensionsDataHolder.class);
        when(ConsentExtensionsDataHolder.getInstance()).thenReturn(consentExtensionsDataHolderMock);
        when(consentExtensionsDataHolderMock.getConsentCoreService()).thenReturn(consentCoreServiceImpl);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("Reason", "Amended by the user");
        eventData.put("ConsentId", sampleConsentID);
        eventData.put("ClientId", "dummyClientId");

        Map<String, Object> consentDataMap = new HashMap<>();
        consentDataMap.put("ConsentResource", new DetailedConsentResource());
        consentDataMap.put("ConsentAmendmentHistory", new DetailedConsentResource());
        consentDataMap.put("ConsentAmendmentTime", System.currentTimeMillis());
        eventData.put("ConsentDataMap", consentDataMap);

        OBEvent obEvent = new OBEvent("amended", eventData);
        Mockito.doReturn(true).when(consentCoreServiceImpl)
                .storeConsentAmendmentHistory(Mockito.anyString(), Mockito.anyObject(), Mockito.anyObject());
        consentAmendmentHistoryEventExecutorSpy.processEvent(obEvent);

        Assert.assertTrue(outContent.toString().contains("Consent Amendment History of consentID:"));
    }

    @Test
    public void testProcessEventFailure() throws ConsentManagementException {

        ConsentAmendmentHistoryEventExecutor consentAmendmentHistoryEventExecutorSpy =
                Mockito.spy(new ConsentAmendmentHistoryEventExecutor());

        outContent.reset();

        OpenBankingConfigParser openBankingConfigParserMock = mock(OpenBankingConfigParser.class);
        ConsentExtensionsDataHolder consentExtensionsDataHolderMock = mock(ConsentExtensionsDataHolder.class);
        mockStatic(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
        when(openBankingConfigParserMock.isConsentAmendmentHistoryEnabled()).thenReturn(true);

        mockStatic(ConsentExtensionsDataHolder.class);
        when(ConsentExtensionsDataHolder.getInstance()).thenReturn(consentExtensionsDataHolderMock);
        when(consentExtensionsDataHolderMock.getConsentCoreService()).thenReturn(consentCoreServiceImpl);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("Reason", "Amended by the user");
        eventData.put("ConsentId", sampleConsentID);
        eventData.put("ClientId", "dummyClientId");

        Map<String, Object> consentDataMap = new HashMap<>();
        consentDataMap.put("ConsentResource", new DetailedConsentResource());
        consentDataMap.put("ConsentAmendmentHistory", new DetailedConsentResource());
        consentDataMap.put("ConsentAmendmentTime", System.currentTimeMillis());
        eventData.put("ConsentDataMap", consentDataMap);

        OBEvent obEvent = new OBEvent("amended", eventData);
        Mockito.doThrow(ConsentManagementException.class).when(consentCoreServiceImpl)
                .storeConsentAmendmentHistory(Mockito.anyString(), Mockito.anyObject(), Mockito.anyObject());
        consentAmendmentHistoryEventExecutorSpy.processEvent(obEvent);

        Assert.assertTrue(outContent.toString().contains("An error occurred while persisting consent amendment " +
                "history data."));
    }
}
