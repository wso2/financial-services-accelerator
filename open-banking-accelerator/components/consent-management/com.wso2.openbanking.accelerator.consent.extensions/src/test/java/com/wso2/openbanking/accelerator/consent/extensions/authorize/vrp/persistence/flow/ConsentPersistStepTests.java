/**
 * Copyright (c) 2021-2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
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
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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
        consentPersistDataMock = mock(ConsentPersistData.class);
        consentDataMock = mock(ConsentData.class);
        consentResourceMock = mock(ConsentResource.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);

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

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeMethod
    public void initMethod() {

        openBankingConfigParserMock = mock(OpenBankingConfigParser.class);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);
    }

    @Test(priority = 1, expectedExceptions = ConsentException.class)
    public void testConsentPersistWithoutConsentId() {

        doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 3, expectedExceptions = ConsentException.class)
    public void testConsentPersistWithoutAuthResource() {

        doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
        doReturn("1234").when(consentDataMock).getConsentId();
        doReturn(consentResourceMock).when(consentDataMock).getConsentResource();

        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 6, expectedExceptions = ConsentException.class)
    public void testAccountConsentPersistWithoutAccountIDs() throws Exception {

        doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
        doReturn(ConsentAuthorizeTestConstants.CONSENT_ID).when(consentDataMock).getConsentId();
        doReturn(ConsentAuthorizeTestConstants.USER_ID).when(consentDataMock).getUserId();
        doReturn(ConsentAuthorizeTestConstants.CLIENT_ID).when(consentDataMock).getClientId();
        doReturn(consentResourceMock).when(consentDataMock).getConsentResource();
        doReturn(ConsentAuthorizeTestConstants.getAuthResource()).when(consentDataMock).getAuthResource();
        doReturn(ConsentAuthorizeTestConstants.ACCOUNTS).when(consentResourceMock).getConsentType();
        doReturn(true).when(consentPersistDataMock).getApproval();

        JSONObject payload = (JSONObject) parser
                .parse(ConsentAuthorizeTestConstants.ACCOUNT_PERSIST_PAYLOAD_WITHOUT_ACCOUNT_ID);
        doReturn(payload).when(consentPersistDataMock).getPayload();

        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 7, expectedExceptions = ConsentException.class)
    public void testAccountConsentPersistWithNonStringAccountIDs() throws Exception {

        doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
        doReturn(ConsentAuthorizeTestConstants.CONSENT_ID).when(consentDataMock).getConsentId();
        doReturn(ConsentAuthorizeTestConstants.USER_ID).when(consentDataMock).getUserId();
        doReturn(ConsentAuthorizeTestConstants.CLIENT_ID).when(consentDataMock).getClientId();
        doReturn(consentResourceMock).when(consentDataMock).getConsentResource();
        doReturn(ConsentAuthorizeTestConstants.getAuthResource()).when(consentDataMock).getAuthResource();
        doReturn(ConsentAuthorizeTestConstants.ACCOUNTS).when(consentResourceMock).getConsentType();
        doReturn(true).when(consentPersistDataMock).getApproval();

        JSONObject payload = (JSONObject) parser
                .parse(ConsentAuthorizeTestConstants.PAYLOAD_WITH_NON_STRING_ACCOUNTID);
        doReturn(payload).when(consentPersistDataMock).getPayload();

        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 9, expectedExceptions = ConsentException.class)
    public void testCOFConsentPersistWithoutCOFAccount() throws Exception {

        doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
        doReturn(ConsentAuthorizeTestConstants.CONSENT_ID).when(consentDataMock).getConsentId();
        doReturn(ConsentAuthorizeTestConstants.USER_ID).when(consentDataMock).getUserId();
        doReturn(ConsentAuthorizeTestConstants.CLIENT_ID).when(consentDataMock).getClientId();
        doReturn(consentResourceMock).when(consentDataMock).getConsentResource();
        doReturn(ConsentAuthorizeTestConstants.getAuthResource()).when(consentDataMock).getAuthResource();
        doReturn(ConsentAuthorizeTestConstants.FUNDS_CONFIRMATIONS).when(consentResourceMock)
                .getConsentType();
        doReturn(true).when(consentPersistDataMock).getApproval();

        JSONObject payload = (JSONObject) parser
                .parse(ConsentAuthorizeTestConstants.COF_PERSIST_PAYLOAD_WITHOUT_COF_ACC);
        doReturn(payload).when(consentPersistDataMock).getPayload();

        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 10, expectedExceptions = ConsentException.class)
    public void testCOFConsentPersistWithNonStringCOFAccount() throws Exception {

        doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
        doReturn(ConsentAuthorizeTestConstants.CONSENT_ID).when(consentDataMock).getConsentId();
        doReturn(ConsentAuthorizeTestConstants.USER_ID).when(consentDataMock).getUserId();
        doReturn(ConsentAuthorizeTestConstants.CLIENT_ID).when(consentDataMock).getClientId();
        doReturn(consentResourceMock).when(consentDataMock).getConsentResource();
        doReturn(ConsentAuthorizeTestConstants.getAuthResource()).when(consentDataMock).getAuthResource();
        doReturn(ConsentAuthorizeTestConstants.FUNDS_CONFIRMATIONS).when(consentResourceMock)
                .getConsentType();
        doReturn(true).when(consentPersistDataMock).getApproval();

        JSONObject payload = (JSONObject) parser
                .parse(ConsentAuthorizeTestConstants.COF_PERSIST_PAYLOAD_WITH_NON_STRING_COF_ACC);
        doReturn(payload).when(consentPersistDataMock).getPayload();

        consentPersistStep.execute(consentPersistDataMock);
    }

    @Test(priority = 11, expectedExceptions = ConsentException.class)
    public void testCOFPersistThrowingExceptionWhenConsentBinding() throws Exception {

        doReturn(consentDataMock).when(consentPersistDataMock).getConsentData();
        doReturn(ConsentAuthorizeTestConstants.CONSENT_ID).when(consentDataMock).getConsentId();
        doReturn(ConsentAuthorizeTestConstants.USER_ID).when(consentDataMock).getUserId();
        doReturn(ConsentAuthorizeTestConstants.CLIENT_ID).when(consentDataMock).getClientId();
        doReturn(consentResourceMock).when(consentDataMock).getConsentResource();
        doReturn(ConsentAuthorizeTestConstants.getAuthResource()).when(consentDataMock).getAuthResource();
        doReturn(ConsentAuthorizeTestConstants.FUNDS_CONFIRMATIONS).when(consentResourceMock)
                .getConsentType();
        doReturn(false).when(consentPersistDataMock).getApproval();
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject payload = (JSONObject) parser
                .parse(ConsentAuthorizeTestConstants.COF_PERSIST_PAYLOAD);
        doReturn(payload).when(consentPersistDataMock).getPayload();

        consentPersistStep.execute(consentPersistDataMock);
    }

}
