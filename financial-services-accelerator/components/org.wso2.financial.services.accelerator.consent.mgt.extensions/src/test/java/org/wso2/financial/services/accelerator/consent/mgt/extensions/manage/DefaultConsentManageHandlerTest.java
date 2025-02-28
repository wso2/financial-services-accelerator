/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.extensions.manage;

import org.json.JSONObject;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionExporter;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.builder.ConsentManageBuilder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.impl.DefaultConsentManageHandler;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ConsentManageData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.DataProviders;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestUtil;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * Default consent manage handler test.
 */
public class DefaultConsentManageHandlerTest {

    DefaultConsentManageHandler defaultConsentManageHandler = new DefaultConsentManageHandler();
    @Mock
    ConsentManageData consentManageDataMock;
    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;
    private static Map<String, String> headers;
    private static Map<String, Object> configs;
    private MockedStatic<ConsentExtensionsDataHolder> consentExtensionsDataHolder;
    private MockedStatic<FinancialServicesConfigParser> configParser;

    @BeforeClass
    public void initTest() throws ConsentManagementException {

        consentManageDataMock = mock(ConsentManageData.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        consentExtensionsDataHolder = mockStatic(ConsentExtensionsDataHolder.class);
        configParser = mockStatic(FinancialServicesConfigParser.class);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceMock).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        doReturn(TestUtil.getSampleDetailedConsentResource()).when(consentCoreServiceMock)
                .createAuthorizableConsent(any(), any(), anyString(), anyString(),
                        anyBoolean());
        doReturn(TestUtil.getSampleConsentResource(TestConstants.AWAITING_AUTH_STATUS)).when(consentCoreServiceMock)
                .getConsent(anyString(), anyBoolean());
        doReturn(true).when(consentCoreServiceMock).revokeConsent(anyString(), anyString(), any(),
                anyBoolean());

        configs = new HashMap<String, Object>();
        configs.put(FinancialServicesConstants.MAX_INSTRUCTED_AMOUNT, "1000");
        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        headers = new HashMap<>();

        ConsentManageBuilder consentManageBuilder = Mockito.mock(ConsentManageBuilder.class);
        Mockito.when(consentManageBuilder.getConsentManageHandler()).thenReturn(defaultConsentManageHandler);
//        Mockito.when(consentManageBuilder.getConsentManageValidator())
//        .thenReturn(new DefaultConsentManageValidator());
        ConsentExtensionExporter.setConsentManageBuilder(consentManageBuilder);
    }

    @AfterClass
    public void tearDown() {
        // Closing the mockStatic after each test
        consentExtensionsDataHolder.close();
        configParser.close();
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandlePostWithoutClientId() {

        setConsentManageBuilder();
        JSONObject payload = new JSONObject(TestConstants.VALID_INITIATION);
        doReturn(payload).when(consentManageDataMock).getPayload();

        defaultConsentManageHandler.handlePost(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandlePostWithInvalidHeaders() {

        setConsentManageBuilderForErrorScenario();
        JSONObject payload = new JSONObject(TestConstants.VALID_INITIATION);
        doReturn(payload).when(consentManageDataMock).getPayload();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();

        defaultConsentManageHandler.handlePost(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandlePostWithoutRequestPath() {

        setConsentManageBuilder();
        JSONObject payload = new JSONObject(TestConstants.VALID_INITIATION);
        doReturn(payload).when(consentManageDataMock).getPayload();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();

        defaultConsentManageHandler.handlePost(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandlePostWithInvalidRequestPath() {

        setConsentManageBuilder();
        JSONObject payload = new JSONObject(TestConstants.VALID_INITIATION);
        doReturn(payload).when(consentManageDataMock).getPayload();
        doReturn(TestConstants.INVALID_REQUEST_PATH).when(consentManageDataMock).getRequestPath();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(headers).when(consentManageDataMock).getHeaders();

        defaultConsentManageHandler.handlePost(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandlePostWithInvalidPayload() {

        setConsentManageBuilder();
        doReturn(TestConstants.INVALID_REQUEST_PATH).when(consentManageDataMock).getRequestPath();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(TestConstants.INVALID_INITIATION_OBJECT).when(consentManageDataMock)
                .getPayload();
        defaultConsentManageHandler.handlePost(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class, dataProvider = "AccountInitiationDataProvider",
            dataProviderClass = DataProviders.class)
    public void testHandlePostWithInvalidInitiation(String initiation) {

        setConsentManageBuilder();
        JSONObject payload = new JSONObject(initiation);
        doReturn(payload).when(consentManageDataMock).getPayload();
        doReturn(ConsentExtensionConstants.ACCOUNT_CONSENT_PATH).when(consentManageDataMock)
                .getRequestPath();

        defaultConsentManageHandler.handlePost(consentManageDataMock);
    }

    @Test
    public void testHandlePostForAccounts() {

        setConsentManageBuilder();
        doReturn(headers).when(consentManageDataMock).getHeaders();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(ConsentExtensionConstants.ACCOUNT_CONSENT_PATH).when(consentManageDataMock)
                .getRequestPath();
        JSONObject payload = new JSONObject(TestConstants.VALID_INITIATION);
        doReturn(payload).when(consentManageDataMock).getPayload();

        defaultConsentManageHandler.handlePost(consentManageDataMock);
    }

    @Test
    public void testHandlePostForCOF() {

        setConsentManageBuilder();
        doReturn(headers).when(consentManageDataMock).getHeaders();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(ConsentExtensionConstants.COF_CONSENT_PATH).when(consentManageDataMock)
                .getRequestPath();
        JSONObject payload = new JSONObject(TestConstants.COF_RECEIPT);
        doReturn(payload).when(consentManageDataMock).getPayload();

        defaultConsentManageHandler.handlePost(consentManageDataMock);
    }

    @Test
    public void testHandlePostForPayments() {

        setConsentManageBuilder();
        doReturn(headers).when(consentManageDataMock).getHeaders();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(ConsentExtensionConstants.PAYMENT_CONSENT_PATH).when(consentManageDataMock)
                .getRequestPath();
        JSONObject payload = new JSONObject(TestConstants.PAYMENT_INITIATION);
        doReturn(payload).when(consentManageDataMock).getPayload();

        defaultConsentManageHandler.handlePost(consentManageDataMock);
    }


    @Test(expectedExceptions = ConsentException.class)
    public void testHandleGetWithoutClientId() {

        setConsentManageBuilder();
        doReturn(null).when(consentManageDataMock).getClientId();
        defaultConsentManageHandler.handleGet(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleGetWithInvalidHeaders() {

        setConsentManageBuilderForErrorScenario();

        JSONObject payload = new JSONObject(TestConstants.VALID_INITIATION);
        doReturn(payload).when(consentManageDataMock).getPayload();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();

        defaultConsentManageHandler.handleGet(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleGetWithoutRequestPath() {

        setConsentManageBuilder();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(null).when(consentManageDataMock).getRequestPath();

        defaultConsentManageHandler.handleGet(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleGetWithInvalidRequestPath() {

        setConsentManageBuilder();
        JSONObject payload = new JSONObject(TestConstants.VALID_INITIATION);
        doReturn(payload).when(consentManageDataMock).getPayload();
        doReturn(TestConstants.INVALID_REQUEST_PATH).when(consentManageDataMock).getRequestPath();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(headers).when(consentManageDataMock).getHeaders();

        defaultConsentManageHandler.handleGet(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleGetWithInvalidConsentId() {

        setConsentManageBuilder();
        doReturn(TestConstants.REQUEST_PATH_WITH_INVALID_CONSENT_ID).when(consentManageDataMock).getRequestPath();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(TestConstants.INVALID_INITIATION_OBJECT).when(consentManageDataMock)
                .getPayload();
        defaultConsentManageHandler.handleGet(consentManageDataMock);
    }

    @Test
    public void testHandleGet() {

        setConsentManageBuilder();
        JSONObject payload = new JSONObject();
        doReturn(payload).when(consentManageDataMock).getPayload();
        doReturn(TestConstants.ACCOUNT_CONSENT_GET_PATH).when(consentManageDataMock)
                .getRequestPath();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();

        defaultConsentManageHandler.handleGet(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleDeleteWithoutClientId() {

        setConsentManageBuilder();
        doReturn(null).when(consentManageDataMock).getClientId();
        defaultConsentManageHandler.handleDelete(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleDeleteWithInvalidHeaders() {

        setConsentManageBuilderForErrorScenario();
        JSONObject payload = new JSONObject(TestConstants.VALID_INITIATION);
        doReturn(payload).when(consentManageDataMock).getPayload();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();

        defaultConsentManageHandler.handleDelete(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleDeleteWithoutRequestPath() {

        setConsentManageBuilder();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(null).when(consentManageDataMock).getRequestPath();

        defaultConsentManageHandler.handleDelete(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleDeleteWithInvalidRequestPath() {

        setConsentManageBuilder();
        JSONObject payload = new JSONObject(TestConstants.VALID_INITIATION);
        doReturn(payload).when(consentManageDataMock).getPayload();
        doReturn(TestConstants.INVALID_REQUEST_PATH).when(consentManageDataMock).getRequestPath();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(headers).when(consentManageDataMock).getHeaders();

        defaultConsentManageHandler.handleDelete(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleDeleteWithInvalidConsentId() {

        setConsentManageBuilder();
        doReturn(TestConstants.REQUEST_PATH_WITH_INVALID_CONSENT_ID).when(consentManageDataMock).getRequestPath();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(TestConstants.INVALID_INITIATION_OBJECT).when(consentManageDataMock)
                .getPayload();
        defaultConsentManageHandler.handleDelete(consentManageDataMock);
    }

    @Test
    public void testHandleDelete() {

        setConsentManageBuilder();
        JSONObject payload = new JSONObject();
        doReturn(payload).when(consentManageDataMock).getPayload();
        doReturn(TestConstants.ACCOUNT_CONSENT_GET_PATH).when(consentManageDataMock)
                .getRequestPath();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();

        defaultConsentManageHandler.handleDelete(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandlePut() {

        defaultConsentManageHandler.handlePut(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandlePatch() {

        defaultConsentManageHandler.handlePatch(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleFileUploadPost() {

        defaultConsentManageHandler.handleFileUploadPost(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleFileGet() {

        defaultConsentManageHandler.handleFileGet(consentManageDataMock);
    }


    private static void setConsentManageBuilder() {
        ConsentManageBuilder consentManageBuilder = Mockito.mock(ConsentManageBuilder.class);
        ConsentExtensionExporter.setConsentManageBuilder(consentManageBuilder);
    }

    private static void setConsentManageBuilderForErrorScenario() {
        ConsentManageBuilder consentManageBuilder = Mockito.mock(ConsentManageBuilder.class);
        ConsentExtensionExporter.setConsentManageBuilder(consentManageBuilder);
    }
}
