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
package com.wso2.openbanking.accelerator.keymanager;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.keymanager.internal.KeyManagerDataHolder;
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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
/**
 * Test class for KeyManagerUtil.
 */
@PrepareForTest({OpenBankingConfigParser.class, KeyManagerDataHolder.class, IdentityTenantUtil.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class KeyManagerUtilTest extends PowerMockTestCase {

    String dummyPropertyName1 = "dummyName1";
    String dummyPropertyName2 = "dummyName2";
    String dummyValue1 = "dummyValue1";
    String dummyValue2 = "dummyValue2";

    Map<String, String> property = new HashMap<>();

    @Mock
    OpenBankingConfigParser openBankingConfigParser;

    @Mock
    RealmService realmService;

    @Mock
    private KeyManagerDataHolder keyManagerDataHolder;

    @Mock
    private APIManagerConfigurationService apiManagerConfigurationService;

    @Mock
    private APIManagerConfiguration config;

    @Mock
    private UserRealm userRealm;

    @Mock
    private AbstractUserStoreManager abstractUserStoreManager;

    @Mock
    private RealmConfiguration realmConfiguration;

    @BeforeClass
    public void init() {

        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod()
    public void before() {

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        openBankingConfigParser = PowerMockito.mock(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);

    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }


    @Test
    public void getEmptyOBKeyManagerExtensionImplTest() throws APIManagementException {

        when(openBankingConfigParser.getOBKeyManagerExtensionImpl())
                .thenReturn("");
        Assert.assertNull(KeyManagerUtil.getOBKeyManagerExtensionImpl());
    }

    @Test
    public void getCorrectOBKeyManagerExtensionImplTest() throws APIManagementException {

        when(openBankingConfigParser.getOBKeyManagerExtensionImpl())
                .thenReturn("com.wso2.openbanking.accelerator.keymanager.OBKeyManagerImpl");
        Assert.assertTrue(KeyManagerUtil.getOBKeyManagerExtensionImpl() instanceof OBKeyManagerImpl);
    }

    @Test(description = "Get the value from the JSON input for the properties defined in the config")
    public void testGetValuesForAdditionalProperties() throws Exception {

        Map<String, Map<String, String>> keyManagerAdditionalProperties = new HashMap<>();
        keyManagerAdditionalProperties.put(dummyPropertyName1, property);
        keyManagerAdditionalProperties.put(dummyPropertyName2, property);

        PowerMockito.when(openBankingConfigParser.getKeyManagerAdditionalProperties())
                .thenReturn(keyManagerAdditionalProperties);

        HashMap<String, String> result = new HashMap<>();
        result.put(dummyPropertyName1, dummyValue1);
        result.put(dummyPropertyName2, dummyValue2);

        OAuthAppRequest oAuthAppRequest = new OAuthAppRequest();
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.addParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES,
                "{\"dummyName1\" : \"dummyValue1\" , \"dummyName2\" : \"dummyValue2\"}");
        oAuthAppRequest.setOAuthApplicationInfo(oAuthApplicationInfo);

        Assert.assertEquals(result, KeyManagerUtil.getValuesForAdditionalProperties(oAuthAppRequest));
    }

    @Test(description = "Get the value from the invalid JSON input for the properties defined in the config")
    public void testGetValuesForAdditionalPropertiesFailure() {

        Map<String, Map<String, String>> keyManagerAdditionalProperties = new HashMap<>();
        keyManagerAdditionalProperties.put(dummyPropertyName1, property);
        keyManagerAdditionalProperties.put(dummyPropertyName2, property);

        PowerMockito.when(openBankingConfigParser.getKeyManagerAdditionalProperties())
                .thenReturn(keyManagerAdditionalProperties);

        HashMap<String, String> result = new HashMap<>();
        result.put(dummyPropertyName1, dummyValue1);
        result.put(dummyPropertyName2, dummyValue2);

        OAuthAppRequest oAuthAppRequest = new OAuthAppRequest();
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.addParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES,
                "\"dummyName1\" : \"dummyValue1\" , \"dummyName2\" : \"dummyValue2\"");
        oAuthAppRequest.setOAuthApplicationInfo(oAuthApplicationInfo);
        try {
            KeyManagerUtil.getValuesForAdditionalProperties(oAuthAppRequest);
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), APIManagementException.class);
        }

    }

    @Test(description = "Add existing role to admin")
    private void testAddExistingApplicationRoleToAdmin() throws UserStoreException, APIManagementException {

        int dummyTenantId = 1;
        mockStatic(IdentityTenantUtil.class);
        mockStatic(KeyManagerDataHolder.class);
        PowerMockito.when(IdentityTenantUtil.getTenantId(Mockito.anyString())).thenReturn(dummyTenantId);

        when(KeyManagerDataHolder.getInstance()).thenReturn(keyManagerDataHolder);
        Mockito.when(keyManagerDataHolder.getApiManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(config);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).thenReturn("userName");

        PowerMockito.when(keyManagerDataHolder.getRealmService()).thenReturn(realmService);
        PowerMockito.when(realmService.getTenantUserRealm(Mockito.anyInt())).thenReturn(userRealm);
        PowerMockito.when(userRealm.getUserStoreManager()).thenReturn(abstractUserStoreManager);

        Mockito.when(abstractUserStoreManager.isUserInRole(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        KeyManagerUtil.addApplicationRoleToAdmin("dummy");
    }

}
