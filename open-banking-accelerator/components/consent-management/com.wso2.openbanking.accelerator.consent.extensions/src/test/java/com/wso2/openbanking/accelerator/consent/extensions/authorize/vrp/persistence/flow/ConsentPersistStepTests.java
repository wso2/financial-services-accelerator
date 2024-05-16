/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.consent.extensions.authorize.vrp.persistence.flow;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.util.CarbonUtils;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.impl.DefaultConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentServiceUtil;
import com.wso2.openbanking.accelerator.consent.extensions.utils.ConsentAuthorizeTestConstants;
import com.wso2.openbanking.accelerator.consent.extensions.utils.ConsentExtensionTestUtils;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockObjectFactory;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test class for Consent Persistence.
 */
@PrepareForTest({OpenBankingConfigParser.class, ConsentServiceUtil.class})
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
public class ConsentPersistStepTests {

    @Mock
    OpenBankingConfigParser openBankingConfigParserMock;
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
    private static Map<String, String> configMap;
    JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

    @BeforeClass
    public void initTest() throws ReflectiveOperationException {

        MockitoAnnotations.initMocks(this);

        consentPersistStep = new DefaultConsentPersistStep();
        consentPersistDataMock =  Mockito.mock(ConsentPersistData.class);
        consentDataMock =  Mockito.mock(ConsentData.class);
        consentResourceMock =  Mockito.mock(ConsentResource.class);
        consentCoreServiceMock =  Mockito.mock(ConsentCoreServiceImpl.class);

        configMap = new HashMap<>();
        configMap.put("ErrorURL", "https://localhost:8243/error");

        //to execute util class initialization
        new CarbonUtils();
        System.setProperty("some.property", "property.value");
        System.setProperty("carbon.home", ".");
        ConsentExtensionTestUtils.injectEnvironmentVariable("CARBON_HOME", ".");
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new PowerMockObjectFactory();
    }

    @BeforeMethod
    public void initMethod() {

        openBankingConfigParserMock =  Mockito.mock(OpenBankingConfigParser.class);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);
    }

    @Test(priority = 1, expectedExceptions = ConsentException.class)
    public void testConsentPersistWithoutConsentId() {

        Mockito.doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 3, expectedExceptions = ConsentException.class)
    public void testConsentPersistWithoutAuthResource() {

        Mockito.doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
        Mockito.doReturn("1234").when(consentDataMock).getConsentId();
        Mockito.doReturn(consentResourceMock).when(consentDataMock).getConsentResource();

        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 6, expectedExceptions = ConsentException.class)
    public void testAccountConsentPersistWithoutAccountIDs() throws Exception {

        Mockito.doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
        Mockito.doReturn(ConsentAuthorizeTestConstants.CONSENT_ID).when(consentDataMock).getConsentId();
        Mockito.doReturn(ConsentAuthorizeTestConstants.USER_ID).when(consentDataMock).getUserId();
        Mockito.doReturn(ConsentAuthorizeTestConstants.CLIENT_ID).when(consentDataMock).getClientId();
        Mockito.doReturn(consentResourceMock).when(consentDataMock).getConsentResource();
        Mockito.doReturn(ConsentAuthorizeTestConstants.getAuthResource()).when(consentDataMock).getAuthResource();
        Mockito.doReturn(ConsentAuthorizeTestConstants.ACCOUNTS).when(consentResourceMock).getConsentType();
        Mockito.doReturn(true).when(consentPersistDataMock).getApproval();

        JSONObject payload = (JSONObject) parser
                .parse(ConsentAuthorizeTestConstants.ACCOUNT_PERSIST_PAYLOAD_WITHOUT_ACCOUNT_ID);
        Mockito.doReturn(payload).when(consentPersistDataMock).getPayload();

        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 7, expectedExceptions = ConsentException.class)
    public void testAccountConsentPersistWithNonStringAccountIDs() throws Exception {

        Mockito.doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
        Mockito.doReturn(ConsentAuthorizeTestConstants.CONSENT_ID).when(consentDataMock).getConsentId();
        Mockito.doReturn(ConsentAuthorizeTestConstants.USER_ID).when(consentDataMock).getUserId();
        Mockito.doReturn(ConsentAuthorizeTestConstants.CLIENT_ID).when(consentDataMock).getClientId();
        Mockito.doReturn(consentResourceMock).when(consentDataMock).getConsentResource();
        Mockito.doReturn(ConsentAuthorizeTestConstants.getAuthResource()).when(consentDataMock).getAuthResource();
        Mockito.doReturn(ConsentAuthorizeTestConstants.ACCOUNTS).when(consentResourceMock).getConsentType();
        Mockito.doReturn(true).when(consentPersistDataMock).getApproval();

        JSONObject payload = (JSONObject) parser
                .parse(ConsentAuthorizeTestConstants.PAYLOAD_WITH_NON_STRING_ACCOUNTID);
        Mockito.doReturn(payload).when(consentPersistDataMock).getPayload();

        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 9, expectedExceptions = ConsentException.class)
    public void testCOFConsentPersistWithoutCOFAccount() throws Exception {

        Mockito.doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
        Mockito.doReturn(ConsentAuthorizeTestConstants.CONSENT_ID).when(consentDataMock).getConsentId();
        Mockito.doReturn(ConsentAuthorizeTestConstants.USER_ID).when(consentDataMock).getUserId();
        Mockito.doReturn(ConsentAuthorizeTestConstants.CLIENT_ID).when(consentDataMock).getClientId();
        Mockito.doReturn(consentResourceMock).when(consentDataMock).getConsentResource();
        Mockito.doReturn(ConsentAuthorizeTestConstants.getAuthResource()).when(consentDataMock).getAuthResource();
        Mockito.doReturn(ConsentAuthorizeTestConstants.FUNDS_CONFIRMATIONS).when(consentResourceMock)
                .getConsentType();
        Mockito.doReturn(true).when(consentPersistDataMock).getApproval();

        JSONObject payload = (JSONObject) parser
                .parse(ConsentAuthorizeTestConstants.COF_PERSIST_PAYLOAD_WITHOUT_COF_ACC);
        Mockito.doReturn(payload).when(consentPersistDataMock).getPayload();

        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 10, expectedExceptions = ConsentException.class)
    public void testCOFConsentPersistWithNonStringCOFAccount() throws Exception {

        Mockito.doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
        Mockito.doReturn(ConsentAuthorizeTestConstants.CONSENT_ID).when(consentDataMock).getConsentId();
        Mockito.doReturn(ConsentAuthorizeTestConstants.USER_ID).when(consentDataMock).getUserId();
        Mockito.doReturn(ConsentAuthorizeTestConstants.CLIENT_ID).when(consentDataMock).getClientId();
        Mockito.doReturn(consentResourceMock).when(consentDataMock).getConsentResource();
        Mockito.doReturn(ConsentAuthorizeTestConstants.getAuthResource()).when(consentDataMock).getAuthResource();
        Mockito.doReturn(ConsentAuthorizeTestConstants.FUNDS_CONFIRMATIONS).when(consentResourceMock)
                .getConsentType();
        Mockito.doReturn(true).when(consentPersistDataMock).getApproval();

        JSONObject payload = (JSONObject) parser
                .parse(ConsentAuthorizeTestConstants.COF_PERSIST_PAYLOAD_WITH_NON_STRING_COF_ACC);
        Mockito.doReturn(payload).when(consentPersistDataMock).getPayload();

        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 11, expectedExceptions = ConsentException.class)
    public void testCOFPersistThrowingExceptionWhenConsentBinding() throws Exception {

        Mockito.doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
        Mockito.doReturn(ConsentAuthorizeTestConstants.CONSENT_ID).when(consentDataMock).getConsentId();
        Mockito.doReturn(ConsentAuthorizeTestConstants.USER_ID).when(consentDataMock).getUserId();
        Mockito.doReturn(ConsentAuthorizeTestConstants.CLIENT_ID).when(consentDataMock).getClientId();
        Mockito.doReturn(consentResourceMock).when(consentDataMock).getConsentResource();
        Mockito.doReturn(ConsentAuthorizeTestConstants.getAuthResource()).when(consentDataMock).getAuthResource();
        Mockito.doReturn(ConsentAuthorizeTestConstants.FUNDS_CONFIRMATIONS).when(consentResourceMock)
                .getConsentType();
        Mockito.doReturn(false).when(consentPersistDataMock).getApproval();
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject payload = (JSONObject) parser
                .parse(ConsentAuthorizeTestConstants.COF_PERSIST_PAYLOAD);
        Mockito.doReturn(payload).when(consentPersistDataMock).getPayload();

        consentPersistStep.execute(consentPersistDataMock);
}

//    @Test
//    public void testAccountConsentPersistSuccessScenarioWithApprovalTrue()
//            throws ParseException, ConsentManagementException {
//
//        Mockito.doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
//        Mockito.doReturn(ConsentAuthorizeTestConstants.CONSENT_ID).when(consentDataMock).getConsentId();
//        Mockito.doReturn(ConsentAuthorizeTestConstants.USER_ID).when(consentDataMock).getUserId();
//        Mockito.doReturn(ConsentAuthorizeTestConstants.CLIENT_ID).when(consentDataMock).getClientId();
//        Mockito.doReturn(consentResourceMock).when(consentDataMock).getConsentResource();
//        Mockito.doReturn(ConsentAuthorizeTestConstants.getAuthResource()).when(consentDataMock).getAuthResource();
//        Mockito.doReturn(ConsentAuthorizeTestConstants.ACCOUNTS).when(consentResourceMock).getConsentType();
//        Mockito.doReturn(true).when(consentPersistDataMock).getApproval();
//
//        Mockito.doReturn(true).when(consentCoreServiceMock).bindUserAccountsToConsent(
//                Mockito.<ConsentResource>anyObject(), Mockito.anyString(), Mockito.anyString(), Mockito.anyMap(),
//                Mockito.anyString(), Mockito.anyString());
//
//        PowerMockito.mockStatic(ConsentServiceUtil.class);
//        PowerMockito.when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);
//
//        JSONObject payload = (JSONObject) parser.parse(ConsentAuthorizeTestConstants.ACCOUNT_PERSIST_PAYLOAD);
//        Mockito.doReturn(payload).when(consentPersistDataMock).getPayload();
//
//        consentPersistStep.execute(consentPersistDataMock);
//    }
}




