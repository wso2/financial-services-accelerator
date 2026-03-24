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

package org.wso2.financial.services.accelerator.identity.extensions.client.registration.application.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.AuthorizedAPIManagementService;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthAdminServiceImpl;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.config.TextFileReader;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.util.DCRUtils;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Test for application management listener.
 */
public class ApplicationManagementListenerTest {

    private static final Log log = LogFactory.getLog(ApplicationManagementListenerTest.class);

    ServiceProvider serviceProvider;
    IdentityExtensionsDataHolder identityExtensionsDataHolder;
    OAuthAdminServiceImpl oAuthAdminService;
    ApplicationManagementService applicationManagementService;
    ApplicationUpdaterImpl applicationUpdater;
    FinancialServicesConfigParser financialServicesConfigParserMock;

    @InjectMocks
    FSApplicationManagementListener applicationManagementListener = new FSApplicationManagementListener();

    @InjectMocks
    ApplicationUpdaterImpl applicationUpdaterImpl = new ApplicationUpdaterImpl();
    private static MockedStatic<IdentityExtensionsDataHolder> identityExtensionsDataHolderMockedStatic;
    private static MockedStatic<DCRUtils> dcrUtilsMockedStatic;
    private static MockedStatic<CarbonContext> carbonContextMockedStatic;
    private static MockedStatic<FinancialServicesConfigParser> financialServicesConfigParserMockedStatic;


    @BeforeClass
    public void beforeClass() throws IdentityApplicationManagementException, IdentityOAuthAdminException,
            APIResourceMgtException {

        identityExtensionsDataHolder = IdentityExtensionsDataHolder.getInstance();
        oAuthAdminService = Mockito.mock(OAuthAdminServiceImpl.class);;
        applicationManagementService = Mockito.mock(ApplicationManagementService.class);;
        applicationUpdater = Mockito.mock(ApplicationUpdaterImpl.class);

        Mockito.doNothing().when(oAuthAdminService).updateConsumerApplication(any());
        Mockito.when(oAuthAdminService.getOAuthApplicationDataByAppName(anyString()))
                .thenReturn(new OAuthConsumerAppDTO());
        APIResource apiResource = Mockito.mock(APIResource.class);
        Mockito.when(apiResource.getId()).thenReturn("134565432");
        Mockito.when(apiResource.getScopes()).thenReturn(new ArrayList<>());
        APIResourceManager apiResourceManager = Mockito.mock(APIResourceManager.class);
        Mockito.doReturn(apiResource).when(apiResourceManager).getAPIResourceByIdentifier(anyString(), anyString());

        AuthorizedAPIManagementService authorizedAPIMgmtService = Mockito.mock(AuthorizedAPIManagementService.class);
        Mockito.doNothing().when(authorizedAPIMgmtService).addAuthorizedAPI(anyString(), any(), anyString());

        serviceProvider = new ServiceProvider();
        ServiceProviderProperty serviceProviderProperty = new ServiceProviderProperty();
        serviceProviderProperty.setDisplayName(IdentityCommonConstants.SOFTWARE_STATEMENT);
        serviceProviderProperty.setName(IdentityCommonConstants.SOFTWARE_STATEMENT);
        serviceProviderProperty.setValue("dfdfdfd");
        ServiceProviderProperty[] spPropertyArray = new ServiceProviderProperty[1];
        spPropertyArray[0] = serviceProviderProperty;
        serviceProvider.setSpProperties(spPropertyArray);
        serviceProvider.setApplicationName("testApp");
        serviceProvider.setLocalAndOutBoundAuthenticationConfig(new LocalAndOutboundAuthenticationConfig());

        Mockito.doReturn(serviceProvider).when(applicationManagementService).getServiceProvider(anyInt());

        Map<String, Object> confMap = new HashMap<>();
        confMap.put(FinancialServicesConstants.PRIMARY_AUTHENTICATOR_DISPLAY_NAME, "basic");
        confMap.put(FinancialServicesConstants.PRIMARY_AUTHENTICATOR_NAME, "BasicAuthenticator");
        confMap.put(FinancialServicesConstants.IDENTITY_PROVIDER_NAME, "SMSAuthentication");
        confMap.put(FinancialServicesConstants.IDENTITY_PROVIDER_STEP, "2");
        confMap.put(FinancialServicesConstants.CONDITIONAL_AUTH_SCRIPT_NAME, "common.auth.script.js");
        identityExtensionsDataHolder.setConfigurationMap(confMap);
        identityExtensionsDataHolder.setApplicationManagementService(applicationManagementService);
        identityExtensionsDataHolder.setOauthAdminService(oAuthAdminService);
        identityExtensionsDataHolder.setAbstractApplicationUpdater(new ApplicationUpdaterImpl());
        identityExtensionsDataHolder.setApiResourceManager(apiResourceManager);
        identityExtensionsDataHolder.setAuthorizedAPIManagementService(authorizedAPIMgmtService);

        TextFileReader textFileReader = TextFileReader.getInstance();
        textFileReader.setDirectoryPath("src/test/resources");

        identityExtensionsDataHolderMockedStatic = Mockito.mockStatic(IdentityExtensionsDataHolder.class);
        identityExtensionsDataHolderMockedStatic.when(IdentityExtensionsDataHolder::getInstance)
                .thenReturn(identityExtensionsDataHolder);

        dcrUtilsMockedStatic = Mockito.mockStatic(DCRUtils.class);
        dcrUtilsMockedStatic.when(() -> DCRUtils.getOAuthConsumerAppDTO(anyString()))
                .thenReturn(new OAuthConsumerAppDTO());

        System.setProperty("carbon.home", "/");
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class);
        Mockito.doReturn("admin").when(carbonContext).getUsername();
        carbonContextMockedStatic = Mockito.mockStatic(CarbonContext.class);
        carbonContextMockedStatic.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(carbonContext);

        financialServicesConfigParserMock = Mockito.mock(FinancialServicesConfigParser.class);
        financialServicesConfigParserMockedStatic = Mockito.mockStatic(FinancialServicesConfigParser.class);
        financialServicesConfigParserMockedStatic.when(FinancialServicesConfigParser::getInstance)
                .thenReturn(financialServicesConfigParserMock);
        Mockito.when(financialServicesConfigParserMock.isSetAuthenticatorsOnAppUpdateEnabled()).thenReturn(true);
    }

    @AfterClass
    public void afterClass() {
        identityExtensionsDataHolderMockedStatic.close();
        dcrUtilsMockedStatic.close();
        carbonContextMockedStatic.close();
        financialServicesConfigParserMockedStatic.close();
    }

    @Test
    public void testPreApplicationCreation() throws IdentityApplicationManagementException {

        boolean isSuccess = applicationManagementListener.doPreCreateApplication(serviceProvider,
                "carbon@super", "admin");
        Assert.assertTrue(isSuccess);
    }

    @Test
    public void testPostApplicationCreation() throws IdentityApplicationManagementException {

        boolean isSuccess = applicationManagementListener.doPostCreateApplication(serviceProvider,
                "carbon@super", "admin");
        Assert.assertTrue(isSuccess);

    }

    @Test
    public void testPreUpdateApplicationCreation() throws IdentityOAuthAdminException,
            IdentityApplicationManagementException {

        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
        oAuthConsumerAppDTO.setApplicationName("testApp");
        OAuthConsumerAppDTO[] oAuthConsumerAppDTOS = new OAuthConsumerAppDTO[1];
        oAuthConsumerAppDTOS[0] = oAuthConsumerAppDTO;

        Mockito.when(oAuthAdminService.getAllOAuthApplicationData()).thenReturn(oAuthConsumerAppDTOS);
        applicationManagementListener.doPreUpdateApplication(serviceProvider, "carbon@super",
                "admin");
    }

    @Test
    public void testPostGetApplicationCreation() throws IdentityApplicationManagementException {

        applicationManagementListener.doPostGetServiceProvider(serviceProvider, "appName",
                "carbon@super");

    }

    @Test
    public void testPreDeleteApplicationCreation() throws IdentityApplicationManagementException {

        applicationManagementListener.doPreDeleteApplication("appName", "carbon@super",
                "admin");

    }

    @Test
    public void testPostDeleteApplication() throws IdentityApplicationManagementException {

        applicationManagementListener.doPostDeleteApplication(new ServiceProvider(), "carbon@super",
                "admin");

    }

    @Test
    public void testSetAuthenticators() throws IdentityApplicationManagementException, FinancialServicesException {

        IdentityProvider[] federatedIdPs = new IdentityProvider[1];
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName("SMSAuthentication");
        federatedIdPs[0] = identityProvider;

        Map<String, Object> confMap = IdentityExtensionsDataHolder.getInstance().getConfigurationMap();
        confMap.put(FinancialServicesConstants.IDENTITY_PROVIDER_STEP, "2");
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(confMap);

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                new LocalAndOutboundAuthenticationConfig();

        Mockito.when(applicationManagementService.getAllIdentityProviders(anyString())).
                thenReturn(federatedIdPs);
        applicationUpdaterImpl.setAuthenticators(true, "carbon@super", serviceProvider,
                localAndOutboundAuthenticationConfig);
        Assert.assertNotNull(localAndOutboundAuthenticationConfig.getAuthenticationSteps());

    }

    @Test
    public void testSetAuthenticatorsWithFederatedIdp() throws IdentityApplicationManagementException,
            FinancialServicesException {

        IdentityProvider[] federatedIdPs = new IdentityProvider[1];
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName("SMSAuthentication");
        federatedIdPs[0] = identityProvider;

        Map<String, Object> confMap = IdentityExtensionsDataHolder.getInstance().getConfigurationMap();
        confMap.put(FinancialServicesConstants.IDENTITY_PROVIDER_STEP, "1");
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(confMap);

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                new LocalAndOutboundAuthenticationConfig();

        Mockito.when(applicationManagementService.getAllIdentityProviders(anyString())).
                thenReturn(federatedIdPs);
        applicationUpdaterImpl.setAuthenticators(true, "carbon@super", serviceProvider,
                localAndOutboundAuthenticationConfig);
        Assert.assertNotNull(localAndOutboundAuthenticationConfig.getAuthenticationSteps());

    }

    @Test
    public void testSetConditionalAuthScript() throws FinancialServicesException {

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                new LocalAndOutboundAuthenticationConfig();

        applicationUpdaterImpl.setConditionalAuthScript(true, serviceProvider,
                localAndOutboundAuthenticationConfig);

        Assert.assertNotNull(localAndOutboundAuthenticationConfig.getAuthenticationScriptConfig());

    }

    @Test
    public void testDoPreUpdateApplicationOnAppUpdate() throws IdentityApplicationManagementException,
            FinancialServicesException {

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                new LocalAndOutboundAuthenticationConfig();
        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
        List<ServiceProviderProperty> spProperties = new ArrayList<>(Arrays.asList
                (serviceProvider.getSpProperties()));
        serviceProvider.setSpProperties(spProperties.toArray(new ServiceProviderProperty[0]));
        serviceProvider.setRequestPathAuthenticatorConfigs(new RequestPathAuthenticatorConfig[0]);
        Mockito.when(applicationManagementService.getServiceProvider(serviceProvider.getApplicationID()))
                .thenReturn(serviceProvider);
        applicationUpdaterImpl.doPreUpdateApplication(true, oAuthConsumerAppDTO, serviceProvider,
                localAndOutboundAuthenticationConfig, "carbon@super", "admin");
    }

    @Test
    public void testDoPreUpdateApplicationOnAppCreate() throws FinancialServicesException {


        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                new LocalAndOutboundAuthenticationConfig();
        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
        List<ServiceProviderProperty> spProperties = new ArrayList<>(Arrays.asList
                (serviceProvider.getSpProperties()));
        ServiceProviderProperty serviceProviderProperty = new ServiceProviderProperty();
        serviceProviderProperty.setValue("true");
        serviceProviderProperty.setName("AppCreateRequest");
        serviceProviderProperty.setDisplayName("AppCreateRequest");
        serviceProvider.setSpProperties(spProperties.toArray(new ServiceProviderProperty[0]));
        serviceProvider.setRequestPathAuthenticatorConfigs(new RequestPathAuthenticatorConfig[0]);
        applicationUpdaterImpl.doPreUpdateApplication(true, oAuthConsumerAppDTO, serviceProvider,
                localAndOutboundAuthenticationConfig, "carbon@super", "admin");
    }

}
