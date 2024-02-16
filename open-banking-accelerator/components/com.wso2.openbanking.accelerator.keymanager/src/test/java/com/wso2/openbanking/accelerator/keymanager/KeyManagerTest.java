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
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.util.ServiceProviderUtils;
import com.wso2.openbanking.accelerator.keymanager.internal.KeyManagerDataHolder;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
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
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceIdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
/**
 * Test class for KeyManager.
 */
@PrepareForTest({OpenBankingConfigParser.class, APIManagementException.class, ServiceProviderUtils.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class KeyManagerTest extends PowerMockTestCase {

    @Mock
    private OAuthAdminServiceStub oAuthAdminServiceStub;

    @Mock
    private AuthenticationAdminStub authenticationAdminStub;

    @Mock
    private UserAdminStub userAdminStub;

    @Mock
    private KeyManagerDataHolder keyManagerDataHolder;

    @Mock
    private ServiceClient serviceClient;

    @Mock
    private OperationContext operationContext;

    @Mock
    private ServiceContext serviceContext;

    @Mock
    private APIManagerConfiguration config;

    @Mock
    private APIManagerConfigurationService apiManagerConfigurationService;

    @InjectMocks
    OBKeyManagerImpl obKeyManager = new OBKeyManagerImpl();

    @Mock
    OpenBankingConfigParser openBankingConfigParser;

    @Mock
    ApplicationManagementServiceImpl applicationManagementServiceImpl;

    @Mock
    OAuthAdminService oAuthAdminService;

    @Mock
    ServiceProviderUtils serviceProviderUtils;

    @Mock
    org.wso2.carbon.identity.application.common.model.ServiceProvider serviceProvider;

    @Mock
    org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO oAuthConsumerAppDTO;

    String dummyPropertyName1 = "dummyName1";
    String dummyPropertyName2 = "dummyName2";
    String dummyValue1 = "dummyValue1";
    String dummyValue2 = "dummyValue2";

    String defaultPropertyName1 = "defaultName1";
    String defaultPropertyName2 = "defaultName2";
    String defaultValue1 = "defaultValue1";
    String defaultValue2 = "defaultValue2";

    String dummyString = "dummyString";

    Map<String, String> property = new HashMap<>();

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
    public void testGetNewApplicationAccessToken() throws APIManagementException, RemoteException,
            OAuthAdminServiceIdentityOAuthAdminException,
            LoginAuthenticationExceptionException, IdentityApplicationManagementException {

        OBKeyManagerImpl obKeyManager = spy(new OBKeyManagerImplMock());
        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
        oAuthConsumerAppDTO.setApplicationName("AppName");

        AccessTokenRequest tokenRequest = new AccessTokenRequest();
        tokenRequest.setClientId("0001");

        ServiceProvider serviceProvider = new ServiceProvider();
        ServiceProviderProperty serviceProviderProperty = new ServiceProviderProperty();
        serviceProviderProperty.setDisplayName(OpenBankingConstants.REGULATORY);
        serviceProviderProperty.setName(OpenBankingConstants.REGULATORY);
        serviceProviderProperty.setValue("true");
        ServiceProviderProperty[] spPropertyArray = new ServiceProviderProperty[1];
        spPropertyArray[0] = serviceProviderProperty;
        serviceProvider.setSpProperties(spPropertyArray);

        KeyManagerDataHolder.getInstance().setUserAdminStub(userAdminStub);
        Options userAdminOptions = new Options();
        userAdminOptions.setManageSession(true);
        userAdminOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, "sessionCookie");

        Mockito.when(userAdminStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(serviceClient.getOptions()).thenReturn(userAdminOptions);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(config);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL)).thenReturn("KmBackEndURL");
        KeyManagerDataHolder.getInstance().setApiManagerConfiguration(apiManagerConfigurationService);
        KeyManagerDataHolder.getInstance().setAuthenticationAdminStub(authenticationAdminStub);
        KeyManagerDataHolder.getInstance().setOauthAdminServiceStub(oAuthAdminServiceStub);

        AccessTokenRequest accessTokenRequest = new AccessTokenRequest();

        Mockito.when(keyManagerDataHolder.getApiManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(config);
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).thenReturn("userName");
        Mockito.when(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD)).thenReturn("password");

        Mockito.when(keyManagerDataHolder.getAuthenticationAdminStub()).thenReturn(authenticationAdminStub);
        Mockito.when(authenticationAdminStub.login(anyString(), anyString(), anyString())).thenReturn(true);
        Mockito.when(authenticationAdminStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(serviceClient.getLastOperationContext()).thenReturn(operationContext);
        Mockito.when(operationContext.getServiceContext()).thenReturn(serviceContext);
        Mockito.when(serviceContext.getProperty(HTTPConstants.COOKIE_STRING)).thenReturn("cookie");

        Mockito.when(keyManagerDataHolder.getOauthAdminServiceStub()).thenReturn(oAuthAdminServiceStub);
        Mockito.when(oAuthAdminServiceStub._getServiceClient()).thenReturn(serviceClient);
        Mockito.when(oAuthAdminServiceStub.getOAuthApplicationData(anyString()))
                .thenReturn(oAuthConsumerAppDTO);

        Mockito.when(obKeyManager.getApplicationMgmtServiceImpl()).thenReturn(applicationManagementServiceImpl);
        Mockito.when(applicationManagementServiceImpl.getServiceProviderByClientId(
                anyString(), anyString(), anyString())).thenReturn(serviceProvider);
        AccessTokenInfo accessTokenInfo = obKeyManager.getNewApplicationAccessToken(accessTokenRequest);
        Assert.assertTrue(accessTokenInfo == null);

    }

    @Test(description = "Add the values for additional properties defined in the config to oAuthApplicationInfo")
    public void testUpdateAdditionalProperties() {

        Map<String, Map<String, String>> keyManagerAdditionalProperties = new HashMap<>();
        keyManagerAdditionalProperties.put(dummyPropertyName1, property);
        keyManagerAdditionalProperties.put(dummyPropertyName2, property);

        when(openBankingConfigParser.getKeyManagerAdditionalProperties()).thenReturn(keyManagerAdditionalProperties);
        spy(ServiceProvider.class);

        List<org.wso2.carbon.identity.application.common.model.ServiceProviderProperty> spProperties =
                new ArrayList<>();
        org.wso2.carbon.identity.application.common.model.ServiceProviderProperty spProperty1 =
                new org.wso2.carbon.identity.application.common.model.ServiceProviderProperty();
        org.wso2.carbon.identity.application.common.model.ServiceProviderProperty spProperty2 =
                new org.wso2.carbon.identity.application.common.model.ServiceProviderProperty();
        spProperty1.setName(dummyPropertyName1);
        spProperty1.setValue(dummyValue1);
        spProperty2.setName(dummyPropertyName2);
        spProperty2.setValue(dummyValue2);

        spProperties.add(spProperty1);
        spProperties.add(spProperty2);

        HashMap<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put(defaultPropertyName1, defaultValue1);
        additionalProperties.put(defaultPropertyName2, defaultValue2);

        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.addParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES,
                additionalProperties);
        oAuthApplicationInfo = obKeyManager.updateAdditionalProperties(oAuthApplicationInfo, spProperties);

        additionalProperties.put(dummyPropertyName1, dummyValue1);
        additionalProperties.put(dummyPropertyName2, dummyValue2);

        Assert.assertEquals(additionalProperties, (HashMap<String, String>) oAuthApplicationInfo.
                getParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES));
    }

    @Test
    public void testUpdateSpProperties() throws Exception {

        OBKeyManagerImpl obKeyManager = spy(new OBKeyManagerImplMock());

        Mockito.when(obKeyManager.getApplicationMgmtServiceImpl()).thenReturn(applicationManagementServiceImpl);
        Mockito.when(obKeyManager.getOAuthAdminService()).thenReturn(oAuthAdminService);
        Mockito.when(oAuthAdminService.getOAuthApplicationDataByAppName(Mockito.anyString()))
                .thenReturn(oAuthConsumerAppDTO);

        org.wso2.carbon.identity.application.common.model.ServiceProviderProperty[] spProperties =
                new org.wso2.carbon.identity.application.common.model.ServiceProviderProperty[2];
        org.wso2.carbon.identity.application.common.model.ServiceProviderProperty spProperty1 =
                new org.wso2.carbon.identity.application.common.model.ServiceProviderProperty();
        org.wso2.carbon.identity.application.common.model.ServiceProviderProperty spProperty2 =
                new org.wso2.carbon.identity.application.common.model.ServiceProviderProperty();
        spProperty1.setName(defaultPropertyName1);
        spProperty1.setValue(defaultValue1);
        spProperty2.setName(defaultPropertyName2);
        spProperty2.setValue(defaultValue2);

        spProperties[0] = (spProperty1);
        spProperties[1] = (spProperty2);

        ServiceProvider serviceProvider = spy(ServiceProvider.class);
        doNothing().when(applicationManagementServiceImpl).updateApplication(Mockito.anyObject(), Mockito.anyString(),
                Mockito.anyString());

        serviceProvider.setSpProperties(spProperties);
        serviceProvider.setApplicationName(dummyString);

        Mockito.when(applicationManagementServiceImpl.getServiceProvider(dummyString, dummyString))
                .thenReturn(serviceProvider);

        String overriddenDummyValue = "overriddenDummyValue";
        HashMap<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put(dummyPropertyName1, dummyValue1);
        additionalProperties.put(dummyPropertyName2, dummyValue2);
        additionalProperties.put(defaultPropertyName2, overriddenDummyValue);

        List<org.wso2.carbon.identity.application.common.model.ServiceProviderProperty> updatedSpProperties =
                new ArrayList<>(Arrays.asList(spProperties));
        org.wso2.carbon.identity.application.common.model.ServiceProviderProperty spProperty3 =
                new org.wso2.carbon.identity.application.common.model.ServiceProviderProperty();
        org.wso2.carbon.identity.application.common.model.ServiceProviderProperty spProperty4 =
                new org.wso2.carbon.identity.application.common.model.ServiceProviderProperty();
        org.wso2.carbon.identity.application.common.model.ServiceProviderProperty spProperty5 =
                new org.wso2.carbon.identity.application.common.model.ServiceProviderProperty();
        spProperty3.setName(dummyPropertyName1);
        spProperty3.setValue(dummyValue1);
        spProperty4.setName(dummyPropertyName2);
        spProperty4.setValue(dummyValue2);
        spProperty5.setName(defaultPropertyName2);
        spProperty5.setValue(overriddenDummyValue);

        updatedSpProperties.add(spProperty3);
        updatedSpProperties.add(spProperty4);
        updatedSpProperties.remove(spProperty2);
        updatedSpProperties.add(spProperty5);

        org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO oAuthConsumerAppDTOdummy =
                new org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO();
        ServiceProvider serviceProviderDummy = new ServiceProvider();
        HashMap<String, String> dummyMap = new HashMap<>();

        doNothing().when(obKeyManager).doPreUpdateSpApp(oAuthConsumerAppDTOdummy, serviceProviderDummy, dummyMap,
                true);

        obKeyManager.updateSpProperties(
                dummyString, dummyString, dummyString, additionalProperties, true);

        List<org.wso2.carbon.identity.application.common.model.ServiceProviderProperty>
                updatedSpPropertiesFromFunction = new ArrayList<>(Arrays.asList(serviceProvider.getSpProperties()));

        // Two sp property arrays have same sp property elements but as different object. Therefore,
        // their comparison needs to be explicitly done
        Assert.assertTrue(compareSpPropertyList(updatedSpProperties, updatedSpPropertiesFromFunction));
    }

    /**
     * Compare two arraylist for equality of size and equality of the attributes of each object in the array.
     *
     * @param originalList original list
     * @param comparedList compared list
     * @return whether elements in the array are equal or not
     */
    private Boolean compareSpPropertyList(List<org.wso2.carbon.identity.application.common.model.
            ServiceProviderProperty> originalList, List<org.wso2.carbon.identity.application.common.model.
            ServiceProviderProperty> comparedList) {

        if (originalList.size() != comparedList.size()) {
            return false;
        } else {
            int equalElementCount = 0;
            for (org.wso2.carbon.identity.application.common.model.ServiceProviderProperty originalProperty
                    : originalList) {
                org.wso2.carbon.identity.application.common.model.ServiceProviderProperty updatedProperty
                        = comparedList.stream().filter(serviceProviderProperty -> serviceProviderProperty.getName()
                        .equalsIgnoreCase(originalProperty.getName())).findAny().orElse(null);
                if (originalProperty.getValue() == updatedProperty.getValue()) {
                    equalElementCount++;
                }
            }
            return originalList.size() == equalElementCount;
        }
    }

    @DataProvider
    public Object[][] validateOAuthAppCreationPropertiesDataProvider() {

        Map<String, Map<String, String>> keyManagerAdditionalProperties = new HashMap<>();
        keyManagerAdditionalProperties.put(dummyPropertyName1, property);
        keyManagerAdditionalProperties.put(dummyPropertyName2, property);

        Map<String, Map<String, String>> keyManagerWithoutAdditionalProperties = new HashMap<>();

        List<ConfigurationDto> applicationConfigurationsList = new ArrayList();
        ConfigurationDto optionalApplicationConfiguration = new ConfigurationDto(dummyPropertyName1,
                dummyPropertyName1, "", "", APIConstants.KeyManager.NOT_APPLICABLE_VALUE, true, false,
                Collections.EMPTY_LIST, false);
        ConfigurationDto mandatoryApplicationConfiguration = new ConfigurationDto(dummyPropertyName2,
                dummyPropertyName2, "", "", APIConstants.KeyManager.NOT_APPLICABLE_VALUE, false, false,
                Collections.EMPTY_LIST, false);
        // Configurations defined in the config
        applicationConfigurationsList
                .add(optionalApplicationConfiguration);
        applicationConfigurationsList
                .add(mandatoryApplicationConfiguration);

        List<ConfigurationDto> applicationConfigurationsListWithCorrectDefaultValue = new ArrayList();
        ConfigurationDto applicationConfigurationWithCorrectDefaultValue = new ConfigurationDto(dummyPropertyName1,
                dummyPropertyName1, "", "", "500", false, false,
                Collections.EMPTY_LIST, false);
        applicationConfigurationsListWithCorrectDefaultValue.add(applicationConfigurationWithCorrectDefaultValue);

        // Input values for properties from the UI
        String inputJsonWithValuesForMandatoryProperties =
                "{\"dummyName1\" : \"dummyValue1\" , \"dummyName2\" : \"dummyValue2\"}";
        String inputJsonWithoutValuesForMandatoryProperties = "{\"dummyName2\" : \"dummyValue2\"}";
        String inputNonJsonStringForAdditionalProperties = "dummy string";
        // Only N/A or numbers greater than zero are allowed for additional property values if they are not defined
        // separately in config
        String inputJsonWithIncorrectValueForDefaultProperties =
                "{\"dummyName1\" : \"dummyValue1\" , \"dummyName2\" : \"dummyValue2\"}";
        String inputJsonWithCorrectValueForDefaultProperties = "{\"dummyName1\" : \"800\"}";
        String inputJsonWithEmptyValueForDefaultProperties = "{\"dummyName1\" : \"\"}";
        String inputJsonWithIncorrectNumberValueForDefaultProperties = "{\"dummyName1\" : \"-800\"}";
        String inputWithInvalidJsonString = "\"dummyName1\" : \"-800\"";

        return new Object[][]{
                {keyManagerAdditionalProperties, applicationConfigurationsList,
                        inputJsonWithValuesForMandatoryProperties, null},
                {keyManagerAdditionalProperties, applicationConfigurationsList,
                        inputJsonWithoutValuesForMandatoryProperties, APIManagementException.class},
                {keyManagerAdditionalProperties, applicationConfigurationsList,
                        inputWithInvalidJsonString, APIManagementException.class},
                {keyManagerAdditionalProperties, applicationConfigurationsList,
                        inputNonJsonStringForAdditionalProperties, APIManagementException.class},
                {keyManagerWithoutAdditionalProperties, applicationConfigurationsList,
                        inputJsonWithIncorrectValueForDefaultProperties, APIManagementException.class},
                {keyManagerWithoutAdditionalProperties, applicationConfigurationsList,
                        inputJsonWithIncorrectValueForDefaultProperties, APIManagementException.class},
                {keyManagerWithoutAdditionalProperties, applicationConfigurationsList,
                        inputJsonWithCorrectValueForDefaultProperties, null},
                {keyManagerWithoutAdditionalProperties, applicationConfigurationsList,
                        inputJsonWithCorrectValueForDefaultProperties, null},
                {keyManagerWithoutAdditionalProperties, applicationConfigurationsList,
                        inputJsonWithIncorrectNumberValueForDefaultProperties, APIManagementException.class},
                {keyManagerWithoutAdditionalProperties, applicationConfigurationsList, null, null},
                {keyManagerWithoutAdditionalProperties, applicationConfigurationsList,
                        inputJsonWithEmptyValueForDefaultProperties, null}
        };
    }

    @Test(dataProvider = "validateOAuthAppCreationPropertiesDataProvider",
            description = "Validate user inputs for application creation")
    public void testValidateOAuthAppCreationProperties(Map<String, Map<String, String>>
                                                               keyManagerAdditionalProperties,
                                                       List<ConfigurationDto> applicationConfigurationsList,
                                                       String valuesForProperties,
                                                       Class<? extends Exception> exceptionType) {

        try {
            Mockito.when(openBankingConfigParser.getKeyManagerAdditionalProperties())
                    .thenReturn(keyManagerAdditionalProperties);
            KeyManagerConnectorConfiguration keyManagerConnectorConfiguration =
                    mock(KeyManagerConnectorConfiguration.class);
            KeyManagerDataHolder.getInstance().addKeyManagerConnectorConfiguration(obKeyManager.getType(),
                    keyManagerConnectorConfiguration);

            Mockito.when(keyManagerDataHolder.getKeyManagerConnectorConfiguration(obKeyManager.getType()))
                    .thenReturn(keyManagerConnectorConfiguration);
            Mockito.when(keyManagerConnectorConfiguration.getApplicationConfigurations())
                    .thenReturn(applicationConfigurationsList);

            String dummyString = "dummy";

            mockStatic(ServiceProviderUtils.class);
            Mockito.when(serviceProviderUtils.getSpTenantDomain(dummyString)).thenReturn(dummyString);
            OBKeyManagerImpl obKeyManager = spy(new OBKeyManagerImplMock());

            Mockito.when(obKeyManager.getApplicationMgmtServiceImpl()).thenReturn(applicationManagementServiceImpl);
            Mockito.when(applicationManagementServiceImpl.getServiceProvider(dummyString, dummyString))
                    .thenReturn(serviceProvider);

            OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
            oAuthApplicationInfo.addParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES,
                    valuesForProperties);

            obKeyManager.validateOAuthAppCreationProperties(oAuthApplicationInfo);
            Assert.assertTrue(exceptionType == null);
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), exceptionType);
        }
    }

    @Test
    public void testGetSpPropertyFromSPMetaData() {

        org.wso2.carbon.identity.application.common.model.ServiceProviderProperty[] spProperties =
                new org.wso2.carbon.identity.application.common.model.ServiceProviderProperty[2];
        org.wso2.carbon.identity.application.common.model.ServiceProviderProperty spProperty1 =
                new org.wso2.carbon.identity.application.common.model.ServiceProviderProperty();
        org.wso2.carbon.identity.application.common.model.ServiceProviderProperty spProperty2 =
                new org.wso2.carbon.identity.application.common.model.ServiceProviderProperty();
        spProperty1.setName(defaultPropertyName1);
        spProperty1.setValue(defaultValue1);
        spProperty2.setName(defaultPropertyName2);
        spProperty2.setValue(defaultValue2);

        spProperties[0] = (spProperty1);
        spProperties[1] = (spProperty2);

        org.wso2.carbon.identity.application.common.model.ServiceProviderProperty property =
                obKeyManager.getSpPropertyFromSPMetaData(defaultPropertyName1, spProperties);

        Assert.assertTrue(property != null);
    }

}

class OBKeyManagerImplMock extends OBKeyManagerImpl {

    @Override
    protected OAuthAdminService getOAuthAdminService() {

        return mock(OAuthAdminService.class);
    }

}
