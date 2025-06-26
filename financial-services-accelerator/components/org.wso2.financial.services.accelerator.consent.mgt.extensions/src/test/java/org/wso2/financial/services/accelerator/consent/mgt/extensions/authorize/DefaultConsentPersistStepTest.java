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
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.impl.DefaultConsentPersistStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.AccountDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentDataDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentPersistData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsumerAccountDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsumerDataDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.PermissionDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.PopulateConsentAuthorizeScreenDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestUtil;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * Default consent persist step test.
 */
public class DefaultConsentPersistStepTest {

    @Mock
    private static DefaultConsentPersistStep consentPersistStep;
    @Mock
    private static ConsentPersistData consentPersistDataMock;
    @Mock
    private static ConsentData consentDataMock;
    @Mock
    private static ConsentResource consentResourceMock;
    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;

    private static MockedStatic<FinancialServicesConfigParser> configParser;
    private MockedStatic<ConsentExtensionsDataHolder> consentExtensionsDataHolder;

    @BeforeClass
    public void initTest() throws ConsentManagementException {

        configParser = Mockito.mockStatic(FinancialServicesConfigParser.class);
        FinancialServicesConfigParser configParserMock = Mockito.mock(FinancialServicesConfigParser.class);
        Mockito.doReturn(true).when(configParserMock).isPreInitiatedConsent();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        consentPersistStep = new DefaultConsentPersistStep();
        consentPersistDataMock = mock(ConsentPersistData.class);
        consentDataMock = mock(ConsentData.class);
        consentResourceMock = mock(ConsentResource.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        consentExtensionsDataHolder = mockStatic(ConsentExtensionsDataHolder.class);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceMock).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        doReturn(TestUtil.getSampleConsentResource(TestConstants.AWAITING_AUTH_STATUS)).when(consentCoreServiceMock)
                .getConsent(anyString(), anyBoolean());
        doReturn(true).when(consentCoreServiceMock).bindUserAccountsToConsent(
                any(ConsentResource.class), anyString(), anyString(), anyMap(), anyString(), anyString());
    }

    @AfterClass
    public void tearDown() {
        // Closing the mockStatic after each test
        consentExtensionsDataHolder.close();
        configParser.close();
    }

    @Test(priority = 1, expectedExceptions = ConsentException.class)
    public void testConsentPersistWithoutConsentData() {

        doReturn(null).when(consentPersistDataMock).getConsentData();
        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 1, expectedExceptions = ConsentException.class)
    public void testConsentPersistWithoutConsentId() {

        doReturn(null).when(consentDataMock).getConsentId();
        doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 2, expectedExceptions = ConsentException.class)
    public void testConsentPersistWithoutConsentResource() {

        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentDataMock).getConsentId();
        doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();

        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 3, expectedExceptions = ConsentException.class)
    public void testConsentPersistWithoutAuthResource() {

        doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentDataMock).getConsentId();
        doReturn(consentResourceMock).when(consentDataMock).getConsentResource();

        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 4)
    public void testConsentPersistSuccessScenarioWithApprovalTrue() {
        PopulateConsentAuthorizeScreenDTO storedResponse = new PopulateConsentAuthorizeScreenDTO();
        ConsentDataDTO consentData = new ConsentDataDTO();
        ConsumerDataDTO consumerData = new ConsumerDataDTO();
        ConsumerAccountDTO account =  new ConsumerAccountDTO();
        PermissionDTO permission = new PermissionDTO();

        account.setDisplayName("acc-1");
        account.setAccountId("acc-1");
        consumerData.setAccounts(Collections.singletonList(account));

        permission.setDisplayValues(Collections.singletonList("ReadAccountBalance"));
        consentData.setPermissions(Collections.singletonList(permission));

        storedResponse.setConsentData(consentData);
        storedResponse.setConsumerData(consumerData);

        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put(ConsentAuthorizeConstants.EXTERNAL_API_PRE_CONSENT_AUTHORIZE_RESPONSE, storedResponse);

        mockCommonConsentData(true,
                String.format(TestConstants.ACCOUNT_PERSIST_PAYLOAD_WITH_ACCOUNT, "P3RM1SS10N-H4SH", "acc-1"),
                metadataMap);

        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 5)
    public void testConsentPersistSuccessScenarioWithApprovalFalse() {
        PopulateConsentAuthorizeScreenDTO storedResponse = new PopulateConsentAuthorizeScreenDTO();
        ConsentDataDTO consentData = new ConsentDataDTO();
        ConsumerDataDTO consumerData = new ConsumerDataDTO();
        ConsumerAccountDTO account =  new ConsumerAccountDTO();
        PermissionDTO permission = new PermissionDTO();

        account.setDisplayName("acc-1");
        account.setAccountId("acc-1");
        consumerData.setAccounts(Collections.singletonList(account));

        permission.setDisplayValues(Collections.singletonList("ReadAccountBalance"));
        consentData.setPermissions(Collections.singletonList(permission));

        storedResponse.setConsentData(consentData);
        storedResponse.setConsumerData(consumerData);

        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put(ConsentAuthorizeConstants.EXTERNAL_API_PRE_CONSENT_AUTHORIZE_RESPONSE, storedResponse);

        mockCommonConsentData(false,
                String.format(TestConstants.ACCOUNT_PERSIST_PAYLOAD_WITHOUT_ACCOUNT, "P3RM1SS10N-H4SH"),
                metadataMap);

        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 6)
    public void testConsentPersistSuccessScenarioForPayments() {
        PopulateConsentAuthorizeScreenDTO storedResponse = new PopulateConsentAuthorizeScreenDTO();
        ConsentDataDTO consentData = new ConsentDataDTO();
        ConsumerDataDTO consumerData = new ConsumerDataDTO();
        ConsumerAccountDTO account =  new ConsumerAccountDTO();

        account.setDisplayName("acc-1");
        account.setAccountId("acc-1");
        consumerData.setAccounts(Collections.singletonList(account));

        storedResponse.setConsentData(consentData);
        storedResponse.setConsumerData(consumerData);

        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put(ConsentAuthorizeConstants.EXTERNAL_API_PRE_CONSENT_AUTHORIZE_RESPONSE, storedResponse);

        mockCommonConsentData(true,
                String.format(TestConstants.PAYMENT_PERSIST_PAYLOAD, "acc-1"),
                metadataMap);

        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 7)
    public void testConsentPersistSuccessScenarioForCoF() {
        PopulateConsentAuthorizeScreenDTO storedResponse = new PopulateConsentAuthorizeScreenDTO();
        ConsentDataDTO consentData = new ConsentDataDTO();
        AccountDTO account =  new AccountDTO();

        account.setDisplayName("acc-1");
        account.setAccountId("acc-1");
        consentData.setInitiatedAccountsForConsent(Collections.singletonList(account));

        storedResponse.setConsentData(consentData);

        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put(ConsentAuthorizeConstants.EXTERNAL_API_PRE_CONSENT_AUTHORIZE_RESPONSE, storedResponse);

        mockCommonConsentData(true,
                String.format(TestConstants.COF_PERSIST_PAYLOAD),
                metadataMap);

        consentPersistStep.execute(consentPersistDataMock);
    }

    private void mockCommonConsentData(boolean approval, String payloadJSON, Map<String, Object> metadataMap) {
        doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentDataMock).getConsentId();
        doReturn("sample-user").when(consentDataMock).getUserId();
        doReturn(consentResourceMock).when(consentDataMock).getConsentResource();
        doReturn(TestUtil.getSampleAuthorizationResource(TestConstants.SAMPLE_CONSENT_ID,
                TestConstants.SAMPLE_AUTH_ID)).when(consentDataMock).getAuthResource();

        doReturn(approval).when(consentPersistDataMock).getApproval();
        doReturn(new JSONObject(payloadJSON)).when(consentPersistDataMock).getPayload();

        doReturn(metadataMap).when(consentDataMock).getMetaDataMap();
    }
}
