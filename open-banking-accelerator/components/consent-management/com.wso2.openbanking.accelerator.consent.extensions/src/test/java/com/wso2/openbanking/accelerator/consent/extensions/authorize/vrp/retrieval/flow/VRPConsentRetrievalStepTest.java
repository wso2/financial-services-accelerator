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
package com.wso2.openbanking.accelerator.consent.extensions.authorize.vrp.retrieval.flow;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.CarbonUtils;
import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.impl.DefaultConsentRetrievalStep;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentServiceUtil;
import com.wso2.openbanking.accelerator.consent.extensions.utils.ConsentExtensionTestUtils;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentFile;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
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

/**
 * Test class for  Consent Retrieval Step.
 */
@PrepareForTest({OpenBankingConfigParser.class, OpenBankingConfigParser.class, ConsentServiceUtil.class})
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
public class VRPConsentRetrievalStepTest extends PowerMockTestCase {

    private static DefaultConsentRetrievalStep defaultConsentRetrievalStep;
    @Mock
    private static ConsentData consentDataMock;
    @Mock
    private static ConsentResource consentResourceMock;
    @Mock
    private static AuthorizationResource authorizationResourceMock;
    @Mock
    ConsentFile consentFileMock;
    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;
    @Mock
    OpenBankingConfigParser openBankingConfigParserMock;

    private static Map<String, Object> configMap;
    ArrayList<AuthorizationResource> authResources;

    @BeforeClass
    public void initClass() {

        MockitoAnnotations.initMocks(this);

        defaultConsentRetrievalStep = new DefaultConsentRetrievalStep();
        consentDataMock = Mockito.mock(ConsentData.class);
        consentResourceMock = Mockito.mock(ConsentResource.class);
        authorizationResourceMock = Mockito.mock(AuthorizationResource.class);
        consentFileMock = Mockito.mock(ConsentFile.class);
        consentCoreServiceMock = Mockito.mock(ConsentCoreServiceImpl.class);

        configMap = new HashMap<>();
        openBankingConfigParserMock = Mockito.mock(OpenBankingConfigParser.class);


        authResources = new ArrayList<AuthorizationResource>();
    }

    @BeforeMethod
    public void initMethod() throws ReflectiveOperationException {

        //to execute util class initialization
        new CarbonUtils();
        System.setProperty("some.property", "property.value");
        System.setProperty("carbon.home", ".");
        ConsentExtensionTestUtils.injectEnvironmentVariable("CARBON_HOME", ".");

        OpenBankingConfigParser openBankingConfigParserMock =  Mockito.mock(OpenBankingConfigParser.class);
        Mockito.doReturn("jdbc/WSO2OB_DB").when(openBankingConfigParserMock).getDataSourceName();

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);

        Mockito.doReturn(configMap).when(openBankingConfigParserMock).getConfiguration();

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
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
    public void testGetConsentDataSetForAccounts() throws ConsentManagementException, ParseException {

        Mockito.doReturn(ConsentExtensionConstants.ACCOUNTS).when(consentResourceMock).getConsentType();
        Mockito.doReturn(ConsentAuthorizeTestConstants.VALID_INITIATION_OBJECT).when(consentResourceMock)
                .getReceipt();
        Mockito.doReturn(ConsentAuthorizeTestConstants.AWAITING_AUTH_STATUS).when(consentResourceMock)
                .getCurrentStatus();

        JSONArray accountConsentData = defaultConsentRetrievalStep.getConsentDataSet(consentResourceMock);

        Assert.assertNotNull(accountConsentData);
    }



    @Test(dataProvider = "PaymentConsentDataDataProvider", dataProviderClass = ConsentExtensionDataProvider.class)
    public void testGetConsentDataSetForPayments(String paymentReceipt) throws ConsentManagementException,
            ParseException {

        Mockito.doReturn(configMap).when(openBankingConfigParserMock).getConfiguration();

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);

        Mockito.doReturn(ConsentExtensionConstants.PAYMENTS).when(consentResourceMock).getConsentType();
        Mockito.doReturn(paymentReceipt).when(consentResourceMock).getReceipt();
        Mockito.doReturn(ConsentAuthorizeTestConstants.CREATED_TIME).when(consentResourceMock)
                .getCreatedTime();
        Mockito.doReturn(ConsentAuthorizeTestConstants.AWAITING_AUTH_STATUS).when(consentResourceMock)
                .getCurrentStatus();

        JSONArray paymentConsentData = defaultConsentRetrievalStep.getConsentDataSet(consentResourceMock);

        Assert.assertNotNull(paymentConsentData);
    }

    @Test
    public void testGetConsentDataSetForFilePayments() throws ConsentManagementException, ParseException {

        Mockito.doReturn(configMap).when(openBankingConfigParserMock).getConfiguration();

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        PowerMockito.when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);

        Mockito.doReturn(ConsentExtensionConstants.PAYMENTS).when(consentResourceMock).getConsentType();
        Mockito.doReturn(ConsentAuthorizeTestConstants.CREATED_TIME).when(consentResourceMock)
                .getCreatedTime();
        Mockito.doReturn(ConsentAuthorizeTestConstants.AWAITING_AUTH_STATUS).when(consentResourceMock)
                .getCurrentStatus();

        JSONArray paymentConsentData = defaultConsentRetrievalStep.getConsentDataSet(consentResourceMock);

        Assert.assertNotNull(paymentConsentData);
    }


    @Test
    public void testGetConsentDataSetForCOF() throws ConsentManagementException, ParseException {

        Mockito.doReturn(ConsentExtensionConstants.FUNDSCONFIRMATIONS).when(consentResourceMock).getConsentType();
        Mockito.doReturn(ConsentAuthorizeTestConstants.COF_RECEIPT).when(consentResourceMock)
                .getReceipt();
        Mockito.doReturn(ConsentAuthorizeTestConstants.AWAITING_AUTH_STATUS).when(consentResourceMock)
                .getCurrentStatus();

        JSONArray cofConsentData = defaultConsentRetrievalStep.getConsentDataSet(consentResourceMock);

        Assert.assertNotNull(cofConsentData);
    }

    @Test
    public void testGetConsentDataSetForVRP() throws ConsentManagementException, ParseException {

        Mockito.doReturn(ConsentExtensionConstants.VRP).when(consentResourceMock).getConsentType();
        Mockito.doReturn(ConsentAuthorizeTestConstants.VRP_INITIATION).when(consentResourceMock)
                .getReceipt();
        Mockito.doReturn(ConsentAuthorizeTestConstants.AWAITING_AUTH_STATUS).when(consentResourceMock)
                .getCurrentStatus();

        JSONArray cofConsentData = defaultConsentRetrievalStep.getConsentDataSet(consentResourceMock);

        Assert.assertNotNull(cofConsentData);
    }


}
