/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.accelerator.consent.extensions.authorize.vrp.retrieval.flow;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.CarbonUtils;
import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.impl.DefaultConsentRetrievalStep;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.utils.ConsentRetrievalUtil;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentServiceUtil;
import com.wso2.openbanking.accelerator.consent.extensions.utils.ConsentExtensionTestUtils;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.wso2.openbanking.accelerator.consent.extensions.authorize.vrp.retrieval.flow.ConsentAuthorizeTestConstants.VRP_WITHOUT_DATA;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test class for consentRetrievalUtil.
 */
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
@PrepareForTest({OpenBankingConfigParser.class, OpenBankingConfigParser.class, ConsentServiceUtil.class})
public class VRPConsentRetrievalUtilTest extends PowerMockTestCase {

    @InjectMocks
    private final DefaultConsentRetrievalStep defaultConsentRetrievalStep = new DefaultConsentRetrievalStep();
    private final ConsentRetrievalUtil consentRetrievalUtil = new ConsentRetrievalUtil();
    @Mock
    private static ConsentData consentDataMock;
    @Mock
    private static ConsentResource consentResourceMock;
    @Mock
    private static AuthorizationResource authorizationResourceMock;
    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;
    @Mock
    OpenBankingConfigParser openBankingConfigParser;
    @Mock
    OpenBankingConfigParser openBankingConfigParse;
    private static Map<String, Object> configMap;
    ArrayList<AuthorizationResource> authResources;


    @BeforeClass
    public void initClass() {
        MockitoAnnotations.initMocks(this);
        consentDataMock = mock(ConsentData.class);
        consentResourceMock = mock(ConsentResource.class);
        authorizationResourceMock = mock(AuthorizationResource.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        configMap = new HashMap<>();
        authResources = new ArrayList<AuthorizationResource>();
    }

    @BeforeClass
    public void setUp() throws ReflectiveOperationException {

        MockitoAnnotations.initMocks(this);
        new CarbonUtils();
        System.setProperty("some.property", "property.value");
        System.setProperty("carbon.home", ".");
        ConsentExtensionTestUtils.injectEnvironmentVariable("CARBON_HOME", ".");

        consentResourceMock = mock(ConsentResource.class);

    }

    @BeforeMethod
    public void initMethod() {
        openBankingConfigParser = mock(OpenBankingConfigParser.class);
        doReturn(configMap).when(openBankingConfigParser).getConfiguration();

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParser);

    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test
    public void testGetConsentDataSetForNonRegulatory() {

        JSONObject jsonObject = new JSONObject();
        Mockito.doReturn(false).when(consentDataMock).isRegulatory();
        defaultConsentRetrievalStep.execute(consentDataMock, jsonObject);
        Assert.assertTrue(jsonObject.isEmpty());
    }

    @Test
    public void testConsentRetrievalWithEmptyConsentData() {

        JSONObject jsonObject = new JSONObject();
        Mockito.doReturn(true).when(consentDataMock).isRegulatory();
        defaultConsentRetrievalStep.execute(consentDataMock, jsonObject);

        Assert.assertNotNull(jsonObject.get(ConsentExtensionConstants.IS_ERROR));
        String errorMsg = (String) jsonObject.get(ConsentExtensionConstants.IS_ERROR);
        Assert.assertFalse(errorMsg.contains(ErrorConstants.REQUEST_OBJ_EXTRACT_ERROR));
    }

    @Test
    public void testConsentRetrievalWithNonJWTRequestObject() {

        JSONObject jsonObject = new JSONObject();
        Mockito.doReturn(true).when(consentDataMock).isRegulatory();
        Mockito.doReturn("request=qwertyuijhbvbn").when(consentDataMock).getSpQueryParams();
        defaultConsentRetrievalStep.execute(consentDataMock, jsonObject);

        Assert.assertNotNull(jsonObject.get(ConsentExtensionConstants.IS_ERROR));
        String errorMsg = (String) jsonObject.get(ConsentExtensionConstants.IS_ERROR);
        Assert.assertTrue(errorMsg.contains(ErrorConstants.REQUEST_OBJ_NOT_SIGNED));
    }

    @Test
    public void testConsentRetrievalWithInvalidRequestObject() {

        String request = "request=" + ConsentAuthorizeTestConstants.INVALID_REQUEST_OBJECT;
        JSONObject jsonObject = new JSONObject();
        Mockito.doReturn(true).when(consentDataMock).isRegulatory();
        Mockito.doReturn(request).when(consentDataMock).getSpQueryParams();
        defaultConsentRetrievalStep.execute(consentDataMock, jsonObject);

        Assert.assertNotNull(jsonObject.get(ConsentExtensionConstants.IS_ERROR));
        String errorMsg = (String) jsonObject.get(ConsentExtensionConstants.IS_ERROR);
        Assert.assertTrue(errorMsg.contains(ErrorConstants.NOT_JSON_PAYLOAD));
    }

    @Test
    public void testConsentRetrievalWithValidRequestObject() throws ConsentManagementException {

        String request = "request=" + ConsentAuthorizeTestConstants.VALID_REQUEST_OBJECT;
        JSONObject jsonObject = new JSONObject();
        Mockito.doReturn(true).when(consentDataMock).isRegulatory();
        Mockito.doReturn(request).when(consentDataMock).getSpQueryParams();

        Mockito.doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(consentResourceMock).getCurrentStatus();
        Mockito.doReturn(ConsentExtensionConstants.ACCOUNTS).when(consentResourceMock).getConsentType();
        Mockito.doReturn(ConsentAuthorizeTestConstants.VALID_INITIATION_OBJECT).when(consentResourceMock)
                .getReceipt();
        Mockito.doReturn(consentResourceMock).when(consentCoreServiceMock)
                .getConsent(Mockito.anyString(), Mockito.anyBoolean());
        Mockito.doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(authorizationResourceMock)
                .getAuthorizationStatus();
        authResources.add(authorizationResourceMock);
        Mockito.doReturn(authResources).when(consentCoreServiceMock)
                .searchAuthorizations(Mockito.anyString());

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        PowerMockito.when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);

        defaultConsentRetrievalStep.execute(consentDataMock, jsonObject);

        Assert.assertNotNull(jsonObject.get(ConsentExtensionConstants.IS_ERROR));
    }

    @Test
    public void testGetConsentDataSetForAccounts() {

        Mockito.doReturn(ConsentExtensionConstants.ACCOUNTS).when(consentResourceMock).getConsentType();
        Mockito.doReturn(ConsentAuthorizeTestConstants.VALID_INITIATION_OBJECT).when(consentResourceMock)
                .getReceipt();
        Mockito.doReturn(ConsentAuthorizeTestConstants.AWAITING_AUTH_STATUS).when(consentResourceMock)
                .getCurrentStatus();

        JSONArray accountConsentData = ConsentRetrievalUtil.getConsentData(consentResourceMock);

        Assert.assertNotNull(accountConsentData);
    }

    @Test(dataProvider = "PaymentConsentDataDataProvider", dataProviderClass = ConsentExtensionDataProvider.class)
    public void testGetConsentDataSetForPayments(String paymentReceipt) {

        Mockito.doReturn(configMap).when(openBankingConfigParse).getConfiguration();

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParse);

        Mockito.doReturn(ConsentExtensionConstants.PAYMENTS).when(consentResourceMock).getConsentType();
        Mockito.doReturn(paymentReceipt).when(consentResourceMock).getReceipt();
        Mockito.doReturn(ConsentAuthorizeTestConstants.CREATED_TIME).when(consentResourceMock)
                .getCreatedTime();
        Mockito.doReturn(ConsentAuthorizeTestConstants.AWAITING_AUTH_STATUS).when(consentResourceMock)
                .getCurrentStatus();

        JSONArray paymentConsentData = consentRetrievalUtil.getConsentData(consentResourceMock);

        Assert.assertNotNull(paymentConsentData);
    }

    @Test
    public void testGetConsentDataSetForCOF() {

        Mockito.doReturn(ConsentExtensionConstants.FUNDSCONFIRMATIONS).when(consentResourceMock).getConsentType();
        Mockito.doReturn(ConsentAuthorizeTestConstants.COF_RECEIPT).when(consentResourceMock)
                .getReceipt();
        Mockito.doReturn(ConsentAuthorizeTestConstants.AWAITING_AUTH_STATUS).when(consentResourceMock)
                .getCurrentStatus();

        JSONArray cofConsentData = ConsentRetrievalUtil.getConsentData(consentResourceMock);

        Assert.assertNotNull(cofConsentData);
    }

    @Test
    public void testGetConsentDataSetForVRPData() {

        Mockito.doReturn(ConsentExtensionConstants.VRP).when(consentResourceMock).getConsentType();
        Mockito.doReturn(ConsentAuthorizeTestConstants.VRP_INITIATION).when(consentResourceMock)
                .getReceipt();
        Mockito.doReturn(ConsentAuthorizeTestConstants.AWAITING_AUTH_STATUS).when(consentResourceMock)
                .getCurrentStatus();

        JSONArray vrpConsentData = ConsentRetrievalUtil.getConsentData(consentResourceMock);

        Assert.assertNotNull(vrpConsentData);
    }

    @Test
    public void testAppendDummyAccountID() {

        JSONArray result = ConsentRetrievalUtil.appendDummyAccountID();

        Assert.assertNotNull(result);

        Assert.assertEquals(result.size(), 2);

        JSONObject accountOne = (JSONObject) result.get(0);
        Assert.assertEquals(accountOne.get("account_id"), "12345");
        Assert.assertEquals(accountOne.get("display_name"), "Salary Saver Account");

        JSONObject accountTwo = (JSONObject) result.get(1);
        Assert.assertEquals(accountTwo.get("account_id"), "67890");
        Assert.assertEquals(accountTwo.get("display_name"), "Max Bonus Account");
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testConsentDataWithInvalidJson() {
        // Arrange
        String invalidJsonString = "Invalid JSON String";
        Mockito.when(consentResourceMock.getReceipt()).thenReturn(invalidJsonString);

        // Act
        JSONArray consentDataJSON = ConsentRetrievalUtil.getConsentData(consentResourceMock);
        Assert.assertNotNull(consentDataJSON);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testGetConsentDataSetForVRPDataParameter() {

        Mockito.doReturn(ConsentExtensionConstants.VRP).when(consentResourceMock).getConsentType();
        Mockito.doReturn(VRP_WITHOUT_DATA).when(consentResourceMock)
                .getReceipt();
        Mockito.doReturn(ConsentAuthorizeTestConstants.AWAITING_AUTH_STATUS).when(consentResourceMock)
                .getCurrentStatus();

        JSONArray vrpConsentData = ConsentRetrievalUtil.getConsentData(consentResourceMock);

        Assert.assertNotNull(vrpConsentData);


        Assert.assertNotNull(ConsentExtensionConstants.IS_ERROR);
        String errorMsg = ConsentExtensionConstants.IS_ERROR;
        Assert.assertTrue(errorMsg.contains(ErrorConstants.NOT_JSON_PAYLOAD));
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testGetConsentDataSetForVRPDataWithoutControlParameters() {

        Mockito.doReturn(ConsentExtensionConstants.VRP).when(consentResourceMock).getConsentType();
        Mockito.doReturn(ConsentAuthorizeTestConstants.VRP_WITHOUT_CONTROLPARAMETERS).when(consentResourceMock)
                .getReceipt();
        Mockito.doReturn(ConsentAuthorizeTestConstants.AWAITING_AUTH_STATUS).when(consentResourceMock)
                .getCurrentStatus();

        JSONArray vrpConsentData = ConsentRetrievalUtil.getConsentData(consentResourceMock);

        Assert.assertNotNull(vrpConsentData);


        Assert.assertNotNull(ConsentExtensionConstants.IS_ERROR);
        String errorMsg = ConsentExtensionConstants.IS_ERROR;
        Assert.assertTrue(errorMsg.contains(ErrorConstants.NOT_JSON_PAYLOAD));
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testGetConsentDataSetForAccount() {

        Mockito.doReturn(ConsentExtensionConstants.ACCOUNTS).when(consentResourceMock).getConsentType();
        Mockito.doReturn(ConsentAuthorizeTestConstants.INVALID_INITIATION_OBJECT).when(consentResourceMock)
                .getReceipt();
        Mockito.doReturn(ConsentAuthorizeTestConstants.AWAITING_AUTH_STATUS).when(consentResourceMock)
                .getCurrentStatus();

        JSONArray accountConsentData = ConsentRetrievalUtil.getConsentData(consentResourceMock);
        Assert.assertNotNull(accountConsentData);

        Assert.assertNotNull(ConsentExtensionConstants.IS_ERROR);
        String errorMsg = ConsentExtensionConstants.IS_ERROR;
        Assert.assertTrue(errorMsg.contains(ErrorConstants.CONSENT_EXPIRED));
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testGetConsentDataSetForCOFs() {

        Mockito.doReturn(ConsentExtensionConstants.FUNDSCONFIRMATIONS).when(consentResourceMock).getConsentType();
        Mockito.doReturn(ConsentAuthorizeTestConstants.INVALID_COF_RECEIPT).when(consentResourceMock)
                .getReceipt();
        Mockito.doReturn(ConsentAuthorizeTestConstants.AWAITING_AUTH_STATUS).when(consentResourceMock)
                .getCurrentStatus();

        JSONArray cofConsentData = ConsentRetrievalUtil.getConsentData(consentResourceMock);

        Assert.assertNotNull(cofConsentData);
        Assert.assertNotNull(ConsentExtensionConstants.IS_ERROR);
        String errorMsg = ConsentExtensionConstants.IS_ERROR;
        Assert.assertTrue(errorMsg.contains(ErrorConstants.CONSENT_EXPIRED));
    }


    @Test(expectedExceptions = ConsentException.class)
    public void testGetConsentDataSetForCOFNull() {

        Mockito.doReturn(ConsentExtensionConstants.FUNDSCONFIRMATIONS).when(consentResourceMock).getConsentType();
        Mockito.doReturn(ConsentAuthorizeTestConstants.NULL_COF_RECEIPT).when(consentResourceMock)
                .getReceipt();
        Mockito.doReturn(ConsentAuthorizeTestConstants.AWAITING_AUTH_STATUS).when(consentResourceMock)
                .getCurrentStatus();

        JSONArray cofConsentData = ConsentRetrievalUtil.getConsentData(consentResourceMock);

        Assert.assertNotNull(cofConsentData);
        Assert.assertNotNull(ConsentExtensionConstants.IS_ERROR);
        String errorMsg = ConsentExtensionConstants.IS_ERROR;
        Assert.assertTrue(errorMsg.contains(ErrorConstants.CONSENT_EXPIRED));
    }

}


