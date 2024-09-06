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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
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
    }

    @AfterClass
    public void tearDown() {
        // Closing the mockStatic after each test
        consentExtensionsDataHolder.close();
        configParser.close();
    }

    @Test(priority = 1, expectedExceptions = ConsentException.class)
    public void testHandlePostWithoutClientId() {

        JSONObject payload = new JSONObject(TestConstants.VALID_INITIATION);
        doReturn(payload).when(consentManageDataMock).getPayload();

        defaultConsentManageHandler.handlePost(consentManageDataMock);
    }

    @Test(priority = 2, expectedExceptions = ConsentException.class)
    public void testHandlePostWithoutRequestPath() {

        JSONObject payload = new JSONObject(TestConstants.VALID_INITIATION);
        doReturn(payload).when(consentManageDataMock).getPayload();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();

        defaultConsentManageHandler.handlePost(consentManageDataMock);
    }

    @Test(priority = 3, expectedExceptions = ConsentException.class)
    public void testHandlePostWithInvalidRequestPath() {

        JSONObject payload = new JSONObject(TestConstants.VALID_INITIATION);
        doReturn(payload).when(consentManageDataMock).getPayload();
        doReturn(TestConstants.INVALID_REQUEST_PATH).when(consentManageDataMock).getRequestPath();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(headers).when(consentManageDataMock).getHeaders();

        defaultConsentManageHandler.handlePost(consentManageDataMock);
    }

    @Test(priority = 4, expectedExceptions = ConsentException.class)
    public void testHandlePostWithInvalidPayload() {

        doReturn(TestConstants.INVALID_REQUEST_PATH).when(consentManageDataMock).getRequestPath();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(TestConstants.INVALID_INITIATION_OBJECT).when(consentManageDataMock)
                .getPayload();
        defaultConsentManageHandler.handlePost(consentManageDataMock);
    }

    @Test(priority = 5, expectedExceptions = ConsentException.class, dataProvider = "AccountInitiationDataProvider",
            dataProviderClass = DataProviders.class)
    public void testHandlePostWithInvalidInitiation(String initiation) {

        JSONObject payload = new JSONObject(initiation);
        doReturn(payload).when(consentManageDataMock).getPayload();
        doReturn(ConsentExtensionConstants.ACCOUNT_CONSENT_PATH).when(consentManageDataMock)
                .getRequestPath();

        defaultConsentManageHandler.handlePost(consentManageDataMock);
    }

    @Test(priority = 6)
    public void testHandlePostForAccounts() {

        doReturn(headers).when(consentManageDataMock).getHeaders();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(ConsentExtensionConstants.ACCOUNT_CONSENT_PATH).when(consentManageDataMock)
                .getRequestPath();
        JSONObject payload = new JSONObject(TestConstants.VALID_INITIATION);
        doReturn(payload).when(consentManageDataMock).getPayload();

        defaultConsentManageHandler.handlePost(consentManageDataMock);
    }

    @Test(priority = 7)
    public void testHandlePostForCOF() {

        doReturn(headers).when(consentManageDataMock).getHeaders();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(ConsentExtensionConstants.COF_CONSENT_PATH).when(consentManageDataMock)
                .getRequestPath();
        JSONObject payload = new JSONObject(TestConstants.COF_RECEIPT);
        doReturn(payload).when(consentManageDataMock).getPayload();

        defaultConsentManageHandler.handlePost(consentManageDataMock);
    }

    @Test(priority = 8)
    public void testHandlePostForPayments() {

        doReturn(headers).when(consentManageDataMock).getHeaders();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(ConsentExtensionConstants.PAYMENT_CONSENT_PATH).when(consentManageDataMock)
                .getRequestPath();
        JSONObject payload = new JSONObject(TestConstants.PAYMENT_INITIATION);
        doReturn(payload).when(consentManageDataMock).getPayload();

        defaultConsentManageHandler.handlePost(consentManageDataMock);
    }


    @Test(priority = 15, expectedExceptions = ConsentException.class)
    public void testHandleGetWithoutClientId() {

        doReturn(null).when(consentManageDataMock).getClientId();
        defaultConsentManageHandler.handleGet(consentManageDataMock);
    }

    @Test(priority = 16, expectedExceptions = ConsentException.class)
    public void testHandleGetWithoutRequestPath() {

        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(null).when(consentManageDataMock).getRequestPath();

        defaultConsentManageHandler.handleGet(consentManageDataMock);
    }

    @Test(priority = 17, expectedExceptions = ConsentException.class)
    public void testHandleGetWithInvalidRequestPath() {

        JSONObject payload = new JSONObject(TestConstants.VALID_INITIATION);
        doReturn(payload).when(consentManageDataMock).getPayload();
        doReturn(TestConstants.INVALID_REQUEST_PATH).when(consentManageDataMock).getRequestPath();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(headers).when(consentManageDataMock).getHeaders();

        defaultConsentManageHandler.handleGet(consentManageDataMock);
    }

    @Test(priority = 18, expectedExceptions = ConsentException.class)
    public void testHandleGetWithInvalidConsentId() {

        doReturn(TestConstants.REQUEST_PATH_WITH_INVALID_CONSENT_ID).when(consentManageDataMock).getRequestPath();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(TestConstants.INVALID_INITIATION_OBJECT).when(consentManageDataMock)
                .getPayload();
        defaultConsentManageHandler.handleGet(consentManageDataMock);
    }

    @Test(priority = 19)
    public void testHandleGet() {

        JSONObject payload = new JSONObject();
        doReturn(payload).when(consentManageDataMock).getPayload();
        doReturn(TestConstants.ACCOUNT_CONSENT_GET_PATH).when(consentManageDataMock)
                .getRequestPath();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();

        defaultConsentManageHandler.handleGet(consentManageDataMock);
    }

    @Test(priority = 20, expectedExceptions = ConsentException.class)
    public void testHandleDeleteWithoutClientId() {

        doReturn(null).when(consentManageDataMock).getClientId();
        defaultConsentManageHandler.handleDelete(consentManageDataMock);
    }

    @Test(priority = 21, expectedExceptions = ConsentException.class)
    public void testHandleDeleteWithoutRequestPath() {

        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(null).when(consentManageDataMock).getRequestPath();

        defaultConsentManageHandler.handleDelete(consentManageDataMock);
    }

    @Test(priority = 22, expectedExceptions = ConsentException.class)
    public void testHandleDeleteWithInvalidRequestPath() {

        JSONObject payload = new JSONObject(TestConstants.VALID_INITIATION);
        doReturn(payload).when(consentManageDataMock).getPayload();
        doReturn(TestConstants.INVALID_REQUEST_PATH).when(consentManageDataMock).getRequestPath();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(headers).when(consentManageDataMock).getHeaders();

        defaultConsentManageHandler.handleDelete(consentManageDataMock);
    }

    @Test(priority = 23, expectedExceptions = ConsentException.class)
    public void testHandleDeleteWithInvalidConsentId() {

        doReturn(TestConstants.REQUEST_PATH_WITH_INVALID_CONSENT_ID).when(consentManageDataMock).getRequestPath();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();
        doReturn(TestConstants.INVALID_INITIATION_OBJECT).when(consentManageDataMock)
                .getPayload();
        defaultConsentManageHandler.handleDelete(consentManageDataMock);
    }

    @Test(priority = 24)
    public void testHandleDelete() {

        JSONObject payload = new JSONObject();
        doReturn(payload).when(consentManageDataMock).getPayload();
        doReturn(TestConstants.ACCOUNT_CONSENT_GET_PATH).when(consentManageDataMock)
                .getRequestPath();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentManageDataMock).getClientId();

        defaultConsentManageHandler.handleDelete(consentManageDataMock);
    }

    @Test(priority = 25, expectedExceptions = ConsentException.class)
    public void testHandlePut() {

        defaultConsentManageHandler.handlePut(consentManageDataMock);
    }

    @Test(priority = 26, expectedExceptions = ConsentException.class)
    public void testHandlePatch() {

        defaultConsentManageHandler.handlePatch(consentManageDataMock);
    }

    @Test(priority = 27, expectedExceptions = ConsentException.class)
    public void testHandleFileUploadPost() {

        defaultConsentManageHandler.handleFileUploadPost(consentManageDataMock);
    }

    @Test(priority = 28, expectedExceptions = ConsentException.class)
    public void testHandleFileGet() {

        defaultConsentManageHandler.handleFileGet(consentManageDataMock);
    }
}
