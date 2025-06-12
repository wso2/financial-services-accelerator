/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.keymanager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationService;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.keymanager.internal.KeyManagerDataHolder;
import org.wso2.financial.services.accelerator.keymanager.util.KeyManagerTestConstants;
import org.wso2.financial.services.accelerator.keymanager.utils.FSKeyManagerConstants;
import org.wso2.financial.services.accelerator.keymanager.utils.IdentityServerUtils;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test class for FSKeyManagerImpl.
 */
public class FSKeyManagerImplTest {

    MockedStatic<IdentityServerUtils> identityServerUtilsMockedStatic;
    MockedStatic<KeyManagerDataHolder> keyManagerDataHolderMockedStatic;
    MockedStatic<FinancialServicesConfigParser> configParserMockedStatic;
    FSKeyManagerImpl fsKeyManager;
    KeyManagerDataHolder keyManagerDataHolder;
    FinancialServicesConfigurationService fsConfigurationService;
    KeyManagerConnectorConfiguration keyManagerConnectorConfiguration;

    @BeforeClass
    public void setUp() {
        identityServerUtilsMockedStatic = Mockito.mockStatic(IdentityServerUtils.class);

        keyManagerDataHolder = KeyManagerDataHolder.getInstance();

        fsConfigurationService = Mockito.mock(FinancialServicesConfigurationService.class);
        Mockito.doReturn(KeyManagerTestConstants.KEY_MANAGER_CONFIGS).when(fsConfigurationService)
                .getKeyManagerConfigs();

        keyManagerDataHolderMockedStatic = Mockito.mockStatic(KeyManagerDataHolder.class);
        keyManagerDataHolderMockedStatic.when(KeyManagerDataHolder::getInstance)
                .thenReturn(keyManagerDataHolder);
        keyManagerDataHolder.setConfigurationService(fsConfigurationService);

        keyManagerConnectorConfiguration = Mockito.mock(KeyManagerConnectorConfiguration.class);
        Mockito.doReturn(KeyManagerTestConstants.getConfigurationDtos()).when(keyManagerConnectorConfiguration)
                .getApplicationConfigurations();
        keyManagerDataHolder.addKeyManagerConnectorConfiguration(FSKeyManagerConstants.CUSTOM_KEYMANAGER_TYPE,
                keyManagerConnectorConfiguration);

        configParserMockedStatic = Mockito.mockStatic(FinancialServicesConfigParser.class);
        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn("org.wso2.financial.services.accelerator.keymanager.FSKeyManagerExtensionImpl")
                .when(configParserMock).getKeyManagerExtensionImpl();
        configParserMockedStatic.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        fsKeyManager = new FSKeyManagerImpl();
    }

    @AfterClass
    public void tearDown() {
        identityServerUtilsMockedStatic.close();
        keyManagerDataHolderMockedStatic.close();
        configParserMockedStatic.close();
    }

    @Test
    public void testGetNewApplicationAccessToken() throws APIManagementException {
        identityServerUtilsMockedStatic.when(() -> IdentityServerUtils.getSPApplicationFromClientId(anyString()))
                .thenReturn(new JSONObject());
        identityServerUtilsMockedStatic.when(() -> IdentityServerUtils.getRegulatoryPropertyFromSPMetadata(any()))
                .thenReturn("true");

        AccessTokenRequest accessTokenRequest = Mockito.mock(AccessTokenRequest.class);

        AccessTokenInfo token = fsKeyManager.getNewApplicationAccessToken(accessTokenRequest);
        Assert.assertNull(token);
    }

    @Test
    public void testGetNewApplicationAccessTokenWithException() {
        identityServerUtilsMockedStatic.when(() -> IdentityServerUtils.getSPApplicationFromClientId(anyString()))
                .thenThrow(new FinancialServicesException("Error retrieving SP application"));

        AccessTokenRequest accessTokenRequest = Mockito.mock(AccessTokenRequest.class);

        try {
            fsKeyManager.getNewApplicationAccessToken(accessTokenRequest);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error retrieving SP application");
        }
    }

    @Test
    public void testGetKeyManagerType() {
        String keyManagerType = fsKeyManager.getType();
        Assert.assertEquals(keyManagerType, "fsKeyManager");
    }

    @Test
    public void testValidateOAuthAppCreationProperties() throws APIManagementException {
        OAuthApplicationInfo oAuthApplicationInfo = Mockito.mock(OAuthApplicationInfo.class);
        Mockito.doReturn(KeyManagerTestConstants.JSON_ADDITIONAL_PROPERTIES)
                .when(oAuthApplicationInfo).getParameter(anyString());

        fsKeyManager.validateOAuthAppCreationProperties(oAuthApplicationInfo);
    }

    @Test
    public void testValidateOAuthAppCreationPropertiesWithInvalidAdditionalProperties() throws APIManagementException {
        OAuthApplicationInfo oAuthApplicationInfo = Mockito.mock(OAuthApplicationInfo.class);
        Mockito.doReturn(KeyManagerTestConstants.INVALID_JSON_ADDITIONAL_PROPERTIES)
                .when(oAuthApplicationInfo).getParameter(anyString());

        try {
            fsKeyManager.validateOAuthAppCreationProperties(oAuthApplicationInfo);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Additional properties is not a valid JSON string");
        }
    }

    @Test
    public void testValidateOAuthAppCreationPropertiesWithNullAdditionalProperties() throws APIManagementException {
        OAuthApplicationInfo oAuthApplicationInfo = Mockito.mock(OAuthApplicationInfo.class);
        Mockito.doReturn(KeyManagerTestConstants.JSON_ADDITIONAL_PROPERTIES_WITH_NULL)
                .when(oAuthApplicationInfo).getParameter(anyString());

        try {
            fsKeyManager.validateOAuthAppCreationProperties(oAuthApplicationInfo);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Missing required properties to create/update oauth "));
        }
    }

    @Test
    public void testValidateOAuthAppCreationPropertiesWithInvalidPCKE() throws APIManagementException {
        OAuthApplicationInfo oAuthApplicationInfo = Mockito.mock(OAuthApplicationInfo.class);
        Mockito.doReturn(KeyManagerTestConstants.JSON_ADDITIONAL_PROPERTIES_INVALID_PKCE_VALUE)
                .when(oAuthApplicationInfo).getParameter(anyString());

        try {
            fsKeyManager.validateOAuthAppCreationProperties(oAuthApplicationInfo);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Expected boolean value, but found"));
        }
    }

    @Test
    public void testValidateOAuthAppCreationPropertiesWithInvalidLifetime() throws APIManagementException {
        OAuthApplicationInfo oAuthApplicationInfo = Mockito.mock(OAuthApplicationInfo.class);
        Mockito.doReturn(KeyManagerTestConstants.JSON_ADDITIONAL_PROPERTIES_INVALID_LIFETIME)
                .when(oAuthApplicationInfo).getParameter(anyString());

        try {
            fsKeyManager.validateOAuthAppCreationProperties(oAuthApplicationInfo);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Application configuration lifetime values cannot be parsed " +
                    "to a long value."));
        }
    }

    @Test
    public void testValidateOAuthAppCreationPropertiesWithoutKeyManagerConfigs() throws APIManagementException {
        OAuthApplicationInfo oAuthApplicationInfo = Mockito.mock(OAuthApplicationInfo.class);
        Mockito.doReturn(KeyManagerTestConstants.JSON_ADDITIONAL_PROPERTIES_INVALID_LIFETIME)
                .when(oAuthApplicationInfo).getParameter(anyString());

        keyManagerDataHolder.removeKeyManagerConnectorConfiguration(FSKeyManagerConstants.CUSTOM_KEYMANAGER_TYPE);

        try {
            fsKeyManager.validateOAuthAppCreationProperties(oAuthApplicationInfo);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid Key Manager Type "));
        }
        keyManagerDataHolder.addKeyManagerConnectorConfiguration(FSKeyManagerConstants.CUSTOM_KEYMANAGER_TYPE,
                keyManagerConnectorConfiguration);
    }

    @Test
    public void testUpdateAdditionalProperties() {

        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.addParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES, new HashMap<>());

        OAuthApplicationInfo modifiedOAuthApplicationInfo = fsKeyManager.updateAdditionalProperties(
                oAuthApplicationInfo, new JSONArray(KeyManagerTestConstants.SP_PROPERTY_ARRAY));

        Map<String, String> additionalProperties = (HashMap<String, String>) modifiedOAuthApplicationInfo
                .getParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES);

        Assert.assertFalse(additionalProperties.isEmpty());
    }
}
