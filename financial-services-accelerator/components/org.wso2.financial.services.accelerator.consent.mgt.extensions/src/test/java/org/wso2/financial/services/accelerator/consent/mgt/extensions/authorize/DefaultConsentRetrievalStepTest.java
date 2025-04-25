/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize;

import org.json.JSONObject;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentFile;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.impl.DefaultConsentRetrievalStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestUtil;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Default consent retrieval step test.
 */
public class DefaultConsentRetrievalStepTest {

    private static DefaultConsentRetrievalStep consentRetrievalStep;
    @Mock
    private static ConsentData consentDataMock;
    @Mock
    ConsentFile consentFileMock;
    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;

    ArrayList<AuthorizationResource> authResources;
    private static MockedStatic<ConsentExtensionsDataHolder> consentExtensionsDataHolder;

    @BeforeClass
    public void initClass() throws ConsentManagementException {
        consentDataMock = mock(ConsentData.class);
        consentFileMock = mock(ConsentFile.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        consentExtensionsDataHolder = Mockito.mockStatic(ConsentExtensionsDataHolder.class);

        authResources = new ArrayList<>();

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceMock).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        doReturn(TestUtil.getSampleConsentResource(TestConstants.AWAITING_AUTH_STATUS)).when(consentCoreServiceMock)
                .getConsent(anyString(), anyBoolean());
        ArrayList<AuthorizationResource> authResources = new ArrayList<>();
        authResources.add(TestUtil.getSampleAuthorizationResource(TestConstants.SAMPLE_CONSENT_ID,
                TestConstants.SAMPLE_AUTH_ID));
        doReturn(authResources).when(consentCoreServiceMock).searchAuthorizations(anyString());

        // Create default config parser mock (returns true for isPreInitiatedConsent) for shared tests
        try (MockedStatic<FinancialServicesConfigParser> configParser =
                     Mockito.mockStatic(FinancialServicesConfigParser.class)) {
            FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
            Mockito.doReturn(true).when(configParserMock).isPreInitiatedConsent();
            configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);
            consentRetrievalStep = new DefaultConsentRetrievalStep();
        }
    }

    @AfterClass
    public void tearDown() {
        // Closing the mockStatic after each test
        consentExtensionsDataHolder.close();
    }

    @Test
    public void testGetConsentDataSetForNonRegulatory() {

        JSONObject jsonObject = new JSONObject();
        doReturn(false).when(consentDataMock).isRegulatory();
        consentRetrievalStep.execute(consentDataMock, jsonObject);

        assertTrue(jsonObject.isEmpty());
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testConsentRetrievalWithNonJWTRequestObject() {

        JSONObject jsonObject = new JSONObject();
        doReturn(true).when(consentDataMock).isRegulatory();
        doReturn("request=non-jwt").when(consentDataMock).getSpQueryParams();
        consentRetrievalStep.execute(consentDataMock, jsonObject);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testConsentRetrievalWithInvalidRequestObject() {

        String request = "request=" + TestConstants.INVALID_REQUEST_OBJECT;
        JSONObject jsonObject = new JSONObject();
        doReturn(true).when(consentDataMock).isRegulatory();
        doReturn(request).when(consentDataMock).getSpQueryParams();
        consentRetrievalStep.execute(consentDataMock, jsonObject);
    }

    @Test
    public void testConsentRetrievalWithValidRequestObject() {

        String request = "request=" + TestConstants.VALID_REQUEST_OBJECT;
        JSONObject jsonObject = new JSONObject();
        doReturn(true).when(consentDataMock).isRegulatory();
        doReturn(request).when(consentDataMock).getSpQueryParams();

        consentRetrievalStep.execute(consentDataMock, jsonObject);
        assertNotNull(jsonObject.get("consentData"));
        assertNotNull(jsonObject.get("accounts"));
    }

    @Test
    public void testConsentRetrievalForNonInitiatedConsents() {

        try (MockedStatic<FinancialServicesConfigParser> localMock =
                     Mockito.mockStatic(FinancialServicesConfigParser.class)) {
            FinancialServicesConfigParser configParserMock = Mockito.mock(FinancialServicesConfigParser.class);
            Mockito.doReturn(false).when(configParserMock).isPreInitiatedConsent();
            localMock.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

            DefaultConsentRetrievalStep localStep = new DefaultConsentRetrievalStep();

            String request = "request=" + TestConstants.VALID_REQUEST_OBJECT;
            JSONObject jsonObject = new JSONObject();
            doReturn(true).when(consentDataMock).isRegulatory();
            doReturn(request).when(consentDataMock).getSpQueryParams();

            localStep.execute(consentDataMock, jsonObject);

            assertNotNull(jsonObject.get("consentData"));
            assertNotNull(jsonObject.get("accounts"));
        }
    }

    @Test
    public void testGetConsentDataSetForAccounts() {

        String request = "request=" + TestConstants.VALID_REQUEST_OBJECT;
        doReturn(request).when(consentDataMock).getSpQueryParams();
        doReturn(true).when(consentDataMock).isRegulatory();
        JSONObject jsonObject = new JSONObject();

        consentRetrievalStep.execute(consentDataMock, jsonObject);
        assertNotNull(jsonObject.get("consentData"));
        assertNotNull(jsonObject.get("accounts"));
    }

    @Test
    public void testGetConsentDataSetForPayments() throws ConsentManagementException {

        String request = "request=" + TestConstants.VALID_REQUEST_OBJECT;
        doReturn(request).when(consentDataMock).getSpQueryParams();
        doReturn(true).when(consentDataMock).isRegulatory();
        doReturn(TestUtil.getSampleConsentResource(TestConstants.PAYMENTS, TestConstants.PAYMENT_INITIATION,
                TestConstants.AWAITING_AUTH_STATUS)).when(consentCoreServiceMock).getConsent(anyString(), anyBoolean());
        JSONObject jsonObject = new JSONObject();

        consentRetrievalStep.execute(consentDataMock, jsonObject);
        assertNotNull(jsonObject.get("consentData"));
        assertNotNull(jsonObject.get("accounts"));
    }

    @Test
    public void testGetConsentDataSetForCOF() throws ConsentManagementException {

        String request = "request=" + TestConstants.VALID_REQUEST_OBJECT;
        doReturn(request).when(consentDataMock).getSpQueryParams();
        doReturn(true).when(consentDataMock).isRegulatory();
        doReturn(TestUtil.getSampleConsentResource(TestConstants.FUNDS_CONFIRMATIONS, TestConstants.COF_RECEIPT,
                TestConstants.AWAITING_AUTH_STATUS)).when(consentCoreServiceMock).getConsent(anyString(), anyBoolean());
        JSONObject jsonObject = new JSONObject();

        consentRetrievalStep.execute(consentDataMock, jsonObject);
        assertNotNull(jsonObject.get("consentData"));
        assertNotNull(jsonObject.get("accounts"));
    }
}
