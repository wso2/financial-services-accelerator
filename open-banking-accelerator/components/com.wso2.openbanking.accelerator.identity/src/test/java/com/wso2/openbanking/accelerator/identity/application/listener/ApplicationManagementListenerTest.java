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

package com.wso2.openbanking.accelerator.identity.application.listener;

import com.wso2.openbanking.accelerator.common.config.TextFileReader;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.dcr.validation.DCRCommonConstants;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.listener.application.ApplicationUpdaterImpl;
import com.wso2.openbanking.accelerator.identity.listener.application.OBApplicationManagementListener;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthAdminServiceImpl;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test for application management listener.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@WithCarbonHome
@PrepareForTest({CarbonContext.class})
public class ApplicationManagementListenerTest extends  PowerMockTestCase {

    private static final Log log = LogFactory.getLog(ApplicationManagementListenerTest.class);

    ServiceProvider serviceProvider;

    IdentityExtensionsDataHolder identityExtensionsDataHolder;

    OAuthAdminServiceImpl oAuthAdminService;

    ApplicationManagementService applicationManagementService;

    ApplicationUpdaterImpl applicationUpdater;

    @InjectMocks
    OBApplicationManagementListener applicationManagementListener = new OBApplicationManagementListener();

    @InjectMocks
    ApplicationUpdaterImpl applicationUpdaterImpl = new ApplicationUpdaterImpl();


    @BeforeClass
    public void beforeClass() {

        identityExtensionsDataHolder = Mockito.mock(IdentityExtensionsDataHolder.class);
        oAuthAdminService = Mockito.mock(OAuthAdminServiceImpl.class);;
        applicationManagementService = Mockito.mock(ApplicationManagementService.class);;
        applicationUpdater = Mockito.mock(ApplicationUpdaterImpl.class);;

        Map<String, Object> confMap = new HashMap<>();
        List<String> regulatoryIssuers = new ArrayList<>();
        regulatoryIssuers.add("OpenBanking Ltd");
        regulatoryIssuers.add("CDR");
        confMap.put(IdentityCommonConstants.PRIMARY_AUTHENTICATOR_DISPLAYNAME, "Basic");
        confMap.put(IdentityCommonConstants.PRIMARY_AUTHENTICATOR_NAME, "BasicAuthenticator");
        confMap.put(IdentityCommonConstants.IDENTITY_PROVIDER_NAME, "SMSAuthentication");
        confMap.put(IdentityCommonConstants.IDENTITY_PROVIDER_STEP, "2");
        confMap.put(DCRCommonConstants.REGULATORY_ISSUERS, regulatoryIssuers);
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(confMap);
        IdentityExtensionsDataHolder.getInstance().setApplicationManagementService(applicationManagementService);
        IdentityExtensionsDataHolder.getInstance().setOauthAdminService(oAuthAdminService);
        serviceProvider = new ServiceProvider();
        ServiceProviderProperty serviceProviderProperty = new ServiceProviderProperty();
        serviceProviderProperty.setDisplayName(DCRCommonConstants.SOFTWARE_STATEMENT);
        serviceProviderProperty.setName(DCRCommonConstants.SOFTWARE_STATEMENT);
        serviceProviderProperty.setValue("dfdfdfd");
        ServiceProviderProperty[] spPropertyArray = new ServiceProviderProperty[1];
        spPropertyArray[0] = serviceProviderProperty;
        serviceProvider.setSpProperties(spPropertyArray);
        serviceProvider.setApplicationName("testApp");

        TextFileReader textFileReader = TextFileReader.getInstance();
        textFileReader.setDirectoryPath("src/test/resources");
    }

    @Test
    public void testPostApplicationCreation() throws IdentityApplicationManagementException,
            IdentityOAuthAdminException {

        when(identityExtensionsDataHolder.getOauthAdminService()).thenReturn(oAuthAdminService);
        when(identityExtensionsDataHolder.getApplicationManagementService()).
                thenReturn(applicationManagementService);

        when(oAuthAdminService.getOAuthApplicationDataByAppName(anyString()))
                .thenReturn(new OAuthConsumerAppDTO());

        when(identityExtensionsDataHolder.getAbstractApplicationUpdater())
                .thenReturn(applicationUpdater);

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

        Map<String, Object> confMap = new HashMap<>();
        List<String> regulatoryIssuers = new ArrayList<>();
        regulatoryIssuers.add("OpenBanking Ltd");
        regulatoryIssuers.add("CDR");
        confMap.put(DCRCommonConstants.REGULATORY_ISSUERS, regulatoryIssuers);

        when(identityExtensionsDataHolder.getOauthAdminService()).thenReturn(oAuthAdminService);
        when(identityExtensionsDataHolder.getConfigurationMap()).thenReturn(confMap);
        when(oAuthAdminService.getAllOAuthApplicationData()).thenReturn(oAuthConsumerAppDTOS);
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
    public void testSetAuthenticators() throws OpenBankingException, IdentityApplicationManagementException {

        IdentityProvider[] federatedIdPs = new IdentityProvider[1];
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName("SMSAuthentication");
        federatedIdPs[0] = identityProvider;

        Map<String, Object> confMap = IdentityExtensionsDataHolder.getInstance().getConfigurationMap();
        confMap.put(IdentityCommonConstants.IDENTITY_PROVIDER_STEP, "2");
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(confMap);

        Mockito.when(identityExtensionsDataHolder.getApplicationManagementService()).
                thenReturn(applicationManagementService);

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                new LocalAndOutboundAuthenticationConfig();

        Mockito.when(applicationManagementService.getAllIdentityProviders(anyString())).
                thenReturn(federatedIdPs);
        applicationUpdaterImpl.setAuthenticators(true, "carbon@super", serviceProvider,
                localAndOutboundAuthenticationConfig);
        Assert.assertNotNull(localAndOutboundAuthenticationConfig.getAuthenticationSteps());

    }

    @Test
    public void testSetAuthenticatorsWithFederatedIdp() throws OpenBankingException,
            IdentityApplicationManagementException {

        IdentityProvider[] federatedIdPs = new IdentityProvider[1];
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName("SMSAuthentication");
        federatedIdPs[0] = identityProvider;

        Map<String, Object> confMap = IdentityExtensionsDataHolder.getInstance().getConfigurationMap();
        confMap.put(IdentityCommonConstants.IDENTITY_PROVIDER_STEP, "1");
        IdentityExtensionsDataHolder.getInstance().setConfigurationMap(confMap);

        Mockito.when(identityExtensionsDataHolder.getApplicationManagementService()).
                thenReturn(applicationManagementService);

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                new LocalAndOutboundAuthenticationConfig();

        Mockito.when(applicationManagementService.getAllIdentityProviders(anyString())).
                thenReturn(federatedIdPs);
        applicationUpdaterImpl.setAuthenticators(true, "carbon@super", serviceProvider,
                localAndOutboundAuthenticationConfig);
        Assert.assertNotNull(localAndOutboundAuthenticationConfig.getAuthenticationSteps());

    }

    @Test
    public void testSetConditionalAuthScript() throws OpenBankingException {

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                new LocalAndOutboundAuthenticationConfig();

        applicationUpdaterImpl.setConditionalAuthScript(true, serviceProvider,
                localAndOutboundAuthenticationConfig);

        Assert.assertNotNull(localAndOutboundAuthenticationConfig.getAuthenticationScriptConfig());

    }

    @Test
    public void testDoPreUpdateApplicationOnAppUpdate() throws OpenBankingException, IdentityOAuthAdminException,
            IdentityApplicationManagementException {

        System.setProperty("carbon.home", "/");
        mockStatic(CarbonContext.class);
        CarbonContext carbonContext = mock(CarbonContext.class);
        when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        when(CarbonContext.getThreadLocalCarbonContext().getUsername()).thenReturn("admin");

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                new LocalAndOutboundAuthenticationConfig();
        when(identityExtensionsDataHolder.getOauthAdminService()).thenReturn(oAuthAdminService);
        when(identityExtensionsDataHolder.getApplicationManagementService()).
                thenReturn(applicationManagementService);
        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
        Mockito.doNothing().when(oAuthAdminService).updateConsumerApplication(anyObject());
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
    public void testDoPreUpdateApplicationOnAppCreate() throws OpenBankingException, IdentityOAuthAdminException {

        System.setProperty("carbon.home", "/");
        mockStatic(CarbonContext.class);
        CarbonContext carbonContext = mock(CarbonContext.class);
        when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        when(CarbonContext.getThreadLocalCarbonContext().getUsername()).thenReturn("admin");

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                new LocalAndOutboundAuthenticationConfig();
        when(identityExtensionsDataHolder.getOauthAdminService()).thenReturn(oAuthAdminService);
        when(identityExtensionsDataHolder.getApplicationManagementService()).
                thenReturn(applicationManagementService);
        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
        Mockito.doNothing().when(oAuthAdminService).updateConsumerApplication(anyObject());
        List<ServiceProviderProperty> spProperties = new ArrayList<>(Arrays.asList
                (serviceProvider.getSpProperties()));
        spProperties.add(IdentityCommonUtil.getServiceProviderProperty("AppCreateRequest",
                "true"));
        serviceProvider.setSpProperties(spProperties.toArray(new ServiceProviderProperty[0]));
        serviceProvider.setRequestPathAuthenticatorConfigs(new RequestPathAuthenticatorConfig[0]);
        applicationUpdaterImpl.doPreUpdateApplication(true, oAuthConsumerAppDTO, serviceProvider,
                localAndOutboundAuthenticationConfig, "carbon@super", "admin");
    }

}
