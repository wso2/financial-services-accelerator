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



package com.wso2.openbanking.accelerator.consent.extensions.manage.vrp;


import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.CarbonUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentServiceUtil;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.extensions.manage.impl.ConsentManageRequestHandler;
import com.wso2.openbanking.accelerator.consent.extensions.manage.impl.VRPConsentRequestHandler;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.extensions.manage.validator.VRPConsentRequestValidator;
import com.wso2.openbanking.accelerator.consent.extensions.utils.ConsentExtensionTestUtils;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentAttributes;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test class for VRPConsentRequestHandler
 */
@PowerMockIgnore({"jdk.internal.reflect.*",
"com.wso2.openbanking.accelerator.consent.extensions.common.*"})
@PrepareForTest({OpenBankingConfigParser.class, ConsentServiceUtil.class,
        ConsentExtensionsDataHolder.class})
public class VRPConsentHandlerTest extends PowerMockTestCase {

    @InjectMocks
    private final VRPConsentRequestHandler handler = new VRPConsentRequestHandler();

    @Mock
    private ConsentManageData consentManageData;

    @Mock
    OpenBankingConfigParser openBankingConfigParser;

    @Mock
    ConsentCoreServiceImpl consentCoreServiceImpl;

    private static Map<String, String> configMap;


    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeClass
    public void setUp() throws ReflectiveOperationException {
        MockitoAnnotations.initMocks(this);

        configMap = new HashMap<>();

        new CarbonUtils();
        System.setProperty("some.property", "property.value");
        System.setProperty("carbon.home", ".");
        ConsentExtensionTestUtils.injectEnvironmentVariable("CARBON_HOME", ".");

        consentManageData = mock(ConsentManageData.class);
    }

    @BeforeMethod
    public void initMethod() {

        openBankingConfigParser = mock(OpenBankingConfigParser.class);
        doReturn(configMap).when(openBankingConfigParser).getConfiguration();

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParser);

    }


    @Test(expectedExceptions = ConsentException.class)
    public void testHandleConsentManageGetWithValidConsentIdAndMatchingClientId() throws ConsentManagementException {
        UUID consentIdUUID = UUID.randomUUID();
        doReturn("vrp-consent/".concat(consentIdUUID.toString())).when(consentManageData).getRequestPath();
        ConsentResource consent = mock(ConsentResource.class);
        doReturn("5678").when(consent).getClientID();

        consentCoreServiceImpl = mock(ConsentCoreServiceImpl.class);
        doReturn(consent).when(consentCoreServiceImpl).getConsent(anyString(), anyBoolean());

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceImpl);

        String expectedClientId = "matchingClientId";
        doReturn(expectedClientId).when(consentManageData).getClientId();

        handler.handleConsentManageGet(consentManageData);
    }


    @Test(expectedExceptions = ConsentException.class)
    public void testHandleConsentManageDeleteWithValidConsent() throws ConsentManagementException {

        UUID consentIdUUID = UUID.randomUUID();
        doReturn("vrp-consent/".concat(consentIdUUID.toString())).when(consentManageData).getRequestPath();
        ConsentResource consent = mock(ConsentResource.class);
        doReturn("5678").when(consent).getClientID();

        consentCoreServiceImpl = mock(ConsentCoreServiceImpl.class);
        doReturn(consent).when(consentCoreServiceImpl).getConsent(anyString(), anyBoolean());

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceImpl);

        String expectedClientId = "6788";
        doReturn(expectedClientId).when(consentManageData).getClientId();

        handler.handleConsentManageDelete(consentManageData);
    }


    @Test
    public void testHandleConsentManagePost_ValidPayload() {

        ConsentManageData mockConsentManageData = mock(ConsentManageData.class);

        Object mockPayload = mock(JSONObject.class);
        when(mockConsentManageData.getPayload()).thenReturn(mockPayload);

        JSONObject mockValidationResponse = new JSONObject();
        mockValidationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        when(VRPConsentRequestValidator.validateVRPPayload(mockPayload)).thenReturn(mockValidationResponse);

        when(mockConsentManageData.getHeaders()).thenReturn(new HashMap<>());

        VRPConsentRequestHandler handler = new VRPConsentRequestHandler();

        handler.handleConsentManagePost(mockConsentManageData);
    }

}
