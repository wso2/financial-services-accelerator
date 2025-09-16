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

package com.wso2.openbanking.accelerator.identity.authenticator;

import com.wso2.openbanking.accelerator.common.exception.OBThrottlerException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.authenticator.util.OBIdentifierAuthenticatorTestData;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import com.wso2.openbanking.accelerator.throttler.service.OBThrottleService;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.builder.FileBasedConfigurationBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.InvalidCredentialsException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authenticator.basicauth.BasicAuthenticator;
import org.wso2.carbon.identity.application.authenticator.basicauth.BasicAuthenticatorConstants;
import org.wso2.carbon.identity.core.model.IdentityErrorMsgContext;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit test cases for the OB Identifier Authenticator.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({FileBasedConfigurationBuilder.class, IdentityUtil.class, IdentityExtensionsDataHolder.class,
        IdentityTenantUtil.class, MultitenantUtils.class,
        AuthenticatedUser.class, HTTPClientUtils.class})
public class OBIdentifierAuthenticatorTest {

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private AuthenticationContext mockAuthnCtxt;
    private Map<String, AuthenticatedIdPData> dummyIdpData = new HashMap<>();
    private ArrayList<AuthenticatorConfig> dummyAuthenticatorList = new ArrayList<>();
    private AuthenticatorConfig mockAuthConfig;
    private BasicAuthenticator mockBasiAuthenticator;
    private AuthenticatedIdPData mockAuthIdpData;
    private Map<String, String> dummyAuthParams = new HashMap<>();
    private FileBasedConfigurationBuilder mockFileBasedConfigurationBuilder;
    private IdentityErrorMsgContext mockIdentityErrorMsgContext;
    private IdentityExtensionsDataHolder identityExtensionsDataHolder;
    private OBThrottleService obThrottleService;
    private SequenceConfig mockSequenceConfig;
    private ApplicationConfig mockApplicationConfig;
    private RealmService mockRealmService;
    private UserRealm mockUserRealm;
    private org.wso2.carbon.user.core.UserRealm mockUserCoreRealm;
    private UserStoreManager mockUserStoreManager;
    private AuthenticatedUser authenticatedUser;
    private Boolean isUserTenantDomainMismatch = true;
    private String redirect;
    private String dummyUserName = "dummyUserName";
    private String dummyQueryParam = "dummyQueryParams";
    private String dummyLoginPage = "dummyLoginPageurl";
    private String dummyString = "dummyString";
    private int dummyTenantId = -1234;
    private String dummyVal = "dummyVal";
    private String dummySessionDataKey = "dummySessionDataKey";
    private OBIdentifierAuthenticator obIdentifierAuthenticator;

    @BeforeTest
    public void setup() {

        obIdentifierAuthenticator = new OBIdentifierAuthenticator();
        mockBasiAuthenticator = new BasicAuthenticator();
        obThrottleService = mock(OBThrottleService.class);
        identityExtensionsDataHolder = mock(IdentityExtensionsDataHolder.class);
    }

    @DataProvider(name = "UsernameAndPasswordProvider")
    public Object[][] getWrongUsernameAndPassword() {

        return new String[][]{
                {"admin@wso2.com", null, "true"},
                {null, "continue", "true"},
                {null, null, "false"},
                {"admin@wso2.com", "reset", "true"},
                {"", "", "true"}
        };
    }

    @Test(dataProvider = "UsernameAndPasswordProvider")
    public void canHandleTestCase(String userName, String identifierConsent, String expected) {

        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getParameter(BasicAuthenticatorConstants.USER_NAME)).thenReturn(userName);
        when(mockRequest.getParameter("identifier_consent")).thenReturn(identifierConsent);
        assertEquals(Boolean.valueOf(expected).booleanValue(), obIdentifierAuthenticator.canHandle(mockRequest),
                "Invalid can handle response for the request.");
    }

    @Test
    public void processSuccessTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);
        when(mockAuthnCtxt.isLogoutRequest()).thenReturn(true);
        assertEquals(obIdentifierAuthenticator.process(mockRequest, mockResponse, mockAuthnCtxt),
                AuthenticatorFlowStatus.SUCCESS_COMPLETED);
    }

    @Test
    public void processSeamlessPromptFalseTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);

        dummyIdpData.put("LOCAL", OBIdentifierAuthenticatorTestData.getSampleAuthenticatedIdPData());
        dummyAuthenticatorList.add(new AuthenticatorConfig());
        dummyAuthParams.put("promptConfirmationWindow", "false");

        when(mockAuthnCtxt.isLogoutRequest()).thenReturn(false);
        when(mockAuthnCtxt.getPreviousAuthenticatedIdPs()).thenReturn(dummyIdpData);
        when(mockAuthnCtxt.getAuthenticatorParams(obIdentifierAuthenticator.getName())).thenReturn(dummyAuthParams);
        assertEquals(obIdentifierAuthenticator.process(mockRequest, mockResponse, mockAuthnCtxt),
                AuthenticatorFlowStatus.SUCCESS_COMPLETED);
    }

    @Test
    public void processSeamlessPromptTrueWithConsentContinueTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);

        dummyIdpData.put("LOCAL", OBIdentifierAuthenticatorTestData.getSampleAuthenticatedIdPData());
        dummyAuthenticatorList.add(new AuthenticatorConfig());
        dummyAuthParams.put("promptConfirmationWindow", "true");

        when(mockAuthnCtxt.isLogoutRequest()).thenReturn(false);
        when(mockAuthnCtxt.getPreviousAuthenticatedIdPs()).thenReturn(dummyIdpData);
        when(mockAuthnCtxt.getAuthenticatorParams(obIdentifierAuthenticator.getName())).thenReturn(dummyAuthParams);
        when(mockRequest.getParameter(Mockito.anyString())).thenReturn("continue");
        assertEquals(obIdentifierAuthenticator.process(mockRequest, mockResponse, mockAuthnCtxt),
                AuthenticatorFlowStatus.SUCCESS_COMPLETED);
    }

    @Test
    public void processSeamlessPromptTrueWithConsentNullTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);

        dummyIdpData.put("LOCAL", OBIdentifierAuthenticatorTestData.getSampleAuthenticatedIdPData());
        dummyAuthenticatorList.add(new AuthenticatorConfig());
        dummyAuthParams.put("promptConfirmationWindow", "true");

        when(mockAuthnCtxt.isLogoutRequest()).thenReturn(false);
        when(mockAuthnCtxt.getPreviousAuthenticatedIdPs()).thenReturn(dummyIdpData);
        when(mockAuthnCtxt.getAuthenticatorParams(obIdentifierAuthenticator.getName())).thenReturn(dummyAuthParams);

        mockStatic(FileBasedConfigurationBuilder.class);
        mockFileBasedConfigurationBuilder = mock(FileBasedConfigurationBuilder.class);
        when(FileBasedConfigurationBuilder.getInstance()).thenReturn(mockFileBasedConfigurationBuilder);
        when(FileBasedConfigurationBuilder.getInstance().getAuthenticatorBean(anyString())).thenReturn(null);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                isUserTenantDomainMismatch = (Boolean) invocation.getArguments()[1];
                return null;
            }
        }).when(mockAuthnCtxt).setProperty(anyString(), anyBoolean());

        when(ConfigurationFacade.getInstance().getAuthenticationEndpointURL()).thenReturn(dummyLoginPage);
        when(mockAuthnCtxt.getContextIdIncludedQueryParams()).thenReturn(dummyQueryParam);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                redirect = (String) invocation.getArguments()[0];
                return null;
            }
        }).when(mockResponse).sendRedirect(anyString());

        assertEquals(obIdentifierAuthenticator.process(mockRequest, mockResponse, mockAuthnCtxt),
                AuthenticatorFlowStatus.INCOMPLETE);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class)
    public void processSeamlessPromptTrueWithExceptionTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);

        dummyIdpData.put("LOCAL", OBIdentifierAuthenticatorTestData.getSampleAuthenticatedIdPData());
        dummyAuthenticatorList.add(new AuthenticatorConfig());
        dummyAuthParams.put("promptConfirmationWindow", "true");

        when(mockAuthnCtxt.isLogoutRequest()).thenReturn(false);
        when(mockAuthnCtxt.getPreviousAuthenticatedIdPs()).thenReturn(dummyIdpData);
        when(mockAuthnCtxt.getAuthenticatorParams(obIdentifierAuthenticator.getName())).thenReturn(dummyAuthParams);

        mockStatic(FileBasedConfigurationBuilder.class);
        mockFileBasedConfigurationBuilder = mock(FileBasedConfigurationBuilder.class);
        when(FileBasedConfigurationBuilder.getInstance()).thenReturn(mockFileBasedConfigurationBuilder);
        when(FileBasedConfigurationBuilder.getInstance().getAuthenticatorBean(anyString())).thenReturn(null);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                isUserTenantDomainMismatch = (Boolean) invocation.getArguments()[1];
                return null;
            }
        }).when(mockAuthnCtxt).setProperty(anyString(), anyBoolean());

        when(ConfigurationFacade.getInstance().getAuthenticationEndpointURL()).thenReturn(dummyLoginPage);
        when(mockAuthnCtxt.getContextIdIncludedQueryParams()).thenReturn(dummyQueryParam);
        Mockito.doThrow(IOException.class).when(mockResponse).sendRedirect(anyString());

        obIdentifierAuthenticator.process(mockRequest, mockResponse, mockAuthnCtxt);
    }

    @Test
    public void initiateAuthenticationRequestGeneralTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("throttleLimit", "3");
        paramMap.put("throttleTimePeriod", "180");
        paramMap.put("showAuthFailureReason", "true");

        authenticatorConfig.setParameterMap(paramMap);

        mockStatic(FileBasedConfigurationBuilder.class);
        mockFileBasedConfigurationBuilder = mock(FileBasedConfigurationBuilder.class);
        when(FileBasedConfigurationBuilder.getInstance()).thenReturn(mockFileBasedConfigurationBuilder);
        when(mockFileBasedConfigurationBuilder.getAuthenticatorBean(anyString())).thenReturn(authenticatorConfig);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                isUserTenantDomainMismatch = (Boolean) invocation.getArguments()[1];
                return null;
            }
        }).when(mockAuthnCtxt).setProperty(anyString(), anyBoolean());

        when(ConfigurationFacade.getInstance().getAuthenticationEndpointURL()).thenReturn(dummyLoginPage);
        when(mockAuthnCtxt.getContextIdIncludedQueryParams()).thenReturn(dummyQueryParam);
        when(mockAuthnCtxt.isRetrying()).thenReturn(true);

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getClientIpAddress(mockRequest)).thenReturn("127.0.0.1");


        when(obThrottleService.isThrottled(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.doNothing().when(obThrottleService)
                .updateThrottleData(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                redirect = (String) invocation.getArguments()[0];
                return null;
            }
        }).when(mockResponse).sendRedirect(anyString());

        mockIdentityErrorMsgContext = mock(IdentityErrorMsgContext.class);
        when(mockIdentityErrorMsgContext.getErrorCode()).thenReturn("dummyErrorCode");
        when(IdentityUtil.getIdentityErrorMsg()).thenReturn(mockIdentityErrorMsgContext);

        mockStatic(IdentityExtensionsDataHolder.class);
        when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolder);
        when(identityExtensionsDataHolder.getOBThrottleService()).thenReturn(obThrottleService);
        when(mockRequest.getParameter("username")).thenReturn(dummyUserName);

        obIdentifierAuthenticator.initiateAuthenticationRequest(mockRequest, mockResponse, mockAuthnCtxt);
    }

    @Test
    public void initiateAuthenticationRequestIsThrottledFalseTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("throttleLimit", "3");
        paramMap.put("throttleTimePeriod", "180");
        paramMap.put("showAuthFailureReason", "true");

        authenticatorConfig.setParameterMap(paramMap);

        mockStatic(FileBasedConfigurationBuilder.class);
        mockFileBasedConfigurationBuilder = mock(FileBasedConfigurationBuilder.class);
        when(FileBasedConfigurationBuilder.getInstance()).thenReturn(mockFileBasedConfigurationBuilder);
        when(mockFileBasedConfigurationBuilder.getAuthenticatorBean(anyString())).thenReturn(authenticatorConfig);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                isUserTenantDomainMismatch = (Boolean) invocation.getArguments()[1];
                return null;
            }
        }).when(mockAuthnCtxt).setProperty(anyString(), anyBoolean());

        when(ConfigurationFacade.getInstance().getAuthenticationEndpointURL()).thenReturn(dummyLoginPage);
        when(mockAuthnCtxt.getContextIdIncludedQueryParams()).thenReturn(dummyQueryParam);
        when(mockAuthnCtxt.isRetrying()).thenReturn(true);

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getClientIpAddress(mockRequest)).thenReturn("127.0.0.1");
        when(mockAuthnCtxt.getProperty("InvalidEmailUsername")).thenReturn(true);


        when(obThrottleService.isThrottled(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        Mockito.doNothing().when(obThrottleService)
                .updateThrottleData(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                redirect = (String) invocation.getArguments()[0];
                return null;
            }
        }).when(mockResponse).sendRedirect(anyString());

        mockIdentityErrorMsgContext = mock(IdentityErrorMsgContext.class);
        when(mockIdentityErrorMsgContext.getErrorCode()).thenReturn("dummyErrorCode");
        when(IdentityUtil.getIdentityErrorMsg()).thenReturn(mockIdentityErrorMsgContext);

        mockStatic(IdentityExtensionsDataHolder.class);
        when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolder);
        when(identityExtensionsDataHolder.getOBThrottleService()).thenReturn(obThrottleService);
        when(mockRequest.getParameter("username")).thenReturn(dummyUserName);

        obIdentifierAuthenticator.initiateAuthenticationRequest(mockRequest, mockResponse, mockAuthnCtxt);
    }

    @Test
    public void initiateAuthenticationRequestAuthFailureFalseTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("throttleLimit", "3");
        paramMap.put("throttleTimePeriod", "180");
        paramMap.put("showAuthFailureReason", "false");

        authenticatorConfig.setParameterMap(paramMap);

        mockStatic(FileBasedConfigurationBuilder.class);
        mockFileBasedConfigurationBuilder = mock(FileBasedConfigurationBuilder.class);
        when(FileBasedConfigurationBuilder.getInstance()).thenReturn(mockFileBasedConfigurationBuilder);
        when(mockFileBasedConfigurationBuilder.getAuthenticatorBean(anyString())).thenReturn(authenticatorConfig);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                isUserTenantDomainMismatch = (Boolean) invocation.getArguments()[1];
                return null;
            }
        }).when(mockAuthnCtxt).setProperty(anyString(), anyBoolean());

        when(ConfigurationFacade.getInstance().getAuthenticationEndpointURL()).thenReturn(dummyLoginPage);
        when(mockAuthnCtxt.getContextIdIncludedQueryParams()).thenReturn(dummyQueryParam);
        when(mockAuthnCtxt.isRetrying()).thenReturn(true);

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getClientIpAddress(mockRequest)).thenReturn("127.0.0.1");


        when(obThrottleService.isThrottled(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.doNothing().when(obThrottleService)
                .updateThrottleData(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                redirect = (String) invocation.getArguments()[0];
                return null;
            }
        }).when(mockResponse).sendRedirect(anyString());

        mockIdentityErrorMsgContext = mock(IdentityErrorMsgContext.class);
        when(mockIdentityErrorMsgContext.getErrorCode()).thenReturn("dummyErrorCode");
        when(IdentityUtil.getIdentityErrorMsg()).thenReturn(mockIdentityErrorMsgContext);

        mockStatic(IdentityExtensionsDataHolder.class);
        when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolder);
        when(identityExtensionsDataHolder.getOBThrottleService()).thenReturn(obThrottleService);
        when(mockRequest.getParameter("username")).thenReturn(dummyUserName);

        obIdentifierAuthenticator.initiateAuthenticationRequest(mockRequest, mockResponse, mockAuthnCtxt);
    }

    @Test
    public void initiateAuthenticationRequestNullErrorContextTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("throttleLimit", "3");
        paramMap.put("throttleTimePeriod", "180");
        paramMap.put("showAuthFailureReason", "false");

        authenticatorConfig.setParameterMap(paramMap);

        mockStatic(FileBasedConfigurationBuilder.class);
        mockFileBasedConfigurationBuilder = mock(FileBasedConfigurationBuilder.class);
        when(FileBasedConfigurationBuilder.getInstance()).thenReturn(mockFileBasedConfigurationBuilder);
        when(mockFileBasedConfigurationBuilder.getAuthenticatorBean(anyString())).thenReturn(authenticatorConfig);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                isUserTenantDomainMismatch = (Boolean) invocation.getArguments()[1];
                return null;
            }
        }).when(mockAuthnCtxt).setProperty(anyString(), anyBoolean());

        when(ConfigurationFacade.getInstance().getAuthenticationEndpointURL()).thenReturn(dummyLoginPage);
        when(mockAuthnCtxt.getContextIdIncludedQueryParams()).thenReturn(dummyQueryParam);
        when(mockAuthnCtxt.isRetrying()).thenReturn(true);

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getClientIpAddress(mockRequest)).thenReturn("127.0.0.1");


        when(obThrottleService.isThrottled(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.doNothing().when(obThrottleService)
                .updateThrottleData(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                redirect = (String) invocation.getArguments()[0];
                return null;
            }
        }).when(mockResponse).sendRedirect(anyString());

        mockStatic(IdentityExtensionsDataHolder.class);
        when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolder);
        when(identityExtensionsDataHolder.getOBThrottleService()).thenReturn(obThrottleService);
        when(mockRequest.getParameter("username")).thenReturn(dummyUserName);

        obIdentifierAuthenticator.initiateAuthenticationRequest(mockRequest, mockResponse, mockAuthnCtxt);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class)
    public void initiateAuthenticationRequestThrottlerExceptionTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("throttleLimit", "3");
        paramMap.put("throttleTimePeriod", "180");
        paramMap.put("showAuthFailureReason", "false");

        authenticatorConfig.setParameterMap(paramMap);

        mockStatic(FileBasedConfigurationBuilder.class);
        mockFileBasedConfigurationBuilder = mock(FileBasedConfigurationBuilder.class);
        when(FileBasedConfigurationBuilder.getInstance()).thenReturn(mockFileBasedConfigurationBuilder);
        when(mockFileBasedConfigurationBuilder.getAuthenticatorBean(anyString())).thenReturn(authenticatorConfig);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                isUserTenantDomainMismatch = (Boolean) invocation.getArguments()[1];
                return null;
            }
        }).when(mockAuthnCtxt).setProperty(anyString(), anyBoolean());

        when(ConfigurationFacade.getInstance().getAuthenticationEndpointURL()).thenReturn(dummyLoginPage);
        when(mockAuthnCtxt.getContextIdIncludedQueryParams()).thenReturn(dummyQueryParam);
        when(mockAuthnCtxt.isRetrying()).thenReturn(true);

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getClientIpAddress(mockRequest)).thenReturn("127.0.0.1");


        when(obThrottleService.isThrottled(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.doThrow(OBThrottlerException.class).when(obThrottleService)
                .updateThrottleData(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                redirect = (String) invocation.getArguments()[0];
                return null;
            }
        }).when(mockResponse).sendRedirect(anyString());

        mockStatic(IdentityExtensionsDataHolder.class);
        when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolder);
        when(identityExtensionsDataHolder.getOBThrottleService()).thenReturn(obThrottleService);
        when(mockRequest.getParameter("username")).thenReturn(dummyUserName);

        obIdentifierAuthenticator.initiateAuthenticationRequest(mockRequest, mockResponse, mockAuthnCtxt);
    }

    @Test
    public void initiateAuthenticationRequestAccountNotConfirmedErrorCodeTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("throttleLimit", "3");
        paramMap.put("throttleTimePeriod", "180");
        paramMap.put("showAuthFailureReason", "false");

        authenticatorConfig.setParameterMap(paramMap);

        mockStatic(FileBasedConfigurationBuilder.class);
        mockFileBasedConfigurationBuilder = mock(FileBasedConfigurationBuilder.class);
        when(FileBasedConfigurationBuilder.getInstance()).thenReturn(mockFileBasedConfigurationBuilder);
        when(mockFileBasedConfigurationBuilder.getAuthenticatorBean(anyString())).thenReturn(authenticatorConfig);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                isUserTenantDomainMismatch = (Boolean) invocation.getArguments()[1];
                return null;
            }
        }).when(mockAuthnCtxt).setProperty(anyString(), anyBoolean());

        when(ConfigurationFacade.getInstance().getAuthenticationEndpointURL()).thenReturn(dummyLoginPage);
        when(mockAuthnCtxt.getContextIdIncludedQueryParams()).thenReturn(dummyQueryParam);
        when(mockAuthnCtxt.isRetrying()).thenReturn(true);

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getClientIpAddress(mockRequest)).thenReturn("127.0.0.1");

        Map<String, Object> mockedThreadLocalMap = new HashMap<>();
        mockedThreadLocalMap.put("user-domain-recaptcha", dummyVal);
        IdentityUtil.threadLocalProperties.set(mockedThreadLocalMap);
        when(IdentityUtil.addDomainToName(Mockito.anyString(), Mockito.anyString())).thenReturn(dummyUserName);

        mockIdentityErrorMsgContext = mock(IdentityErrorMsgContext.class);
        when(mockIdentityErrorMsgContext.getErrorCode()).thenReturn("17005");
        when(IdentityUtil.getIdentityErrorMsg()).thenReturn(mockIdentityErrorMsgContext);

        when(obThrottleService.isThrottled(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.doNothing().when(obThrottleService)
                .updateThrottleData(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                redirect = (String) invocation.getArguments()[0];
                return null;
            }
        }).when(mockResponse).sendRedirect(anyString());

        mockStatic(IdentityExtensionsDataHolder.class);
        when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolder);
        when(identityExtensionsDataHolder.getOBThrottleService()).thenReturn(obThrottleService);
        when(mockRequest.getParameter("username")).thenReturn(dummyUserName);

        obIdentifierAuthenticator.initiateAuthenticationRequest(mockRequest, mockResponse, mockAuthnCtxt);
    }

    @Test
    public void initiateAuthenticationRequestInvalidClientErrorCodeTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("throttleLimit", "3");
        paramMap.put("throttleTimePeriod", "180");
        paramMap.put("showAuthFailureReason", "true");

        authenticatorConfig.setParameterMap(paramMap);

        mockStatic(FileBasedConfigurationBuilder.class);
        mockFileBasedConfigurationBuilder = mock(FileBasedConfigurationBuilder.class);
        when(FileBasedConfigurationBuilder.getInstance()).thenReturn(mockFileBasedConfigurationBuilder);
        when(mockFileBasedConfigurationBuilder.getAuthenticatorBean(anyString())).thenReturn(authenticatorConfig);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                isUserTenantDomainMismatch = (Boolean) invocation.getArguments()[1];
                return null;
            }
        }).when(mockAuthnCtxt).setProperty(anyString(), anyBoolean());

        when(ConfigurationFacade.getInstance().getAuthenticationEndpointURL()).thenReturn(dummyLoginPage);
        when(mockAuthnCtxt.getContextIdIncludedQueryParams()).thenReturn(dummyQueryParam);
        when(mockAuthnCtxt.isRetrying()).thenReturn(true);

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getClientIpAddress(mockRequest)).thenReturn("127.0.0.1");

        mockIdentityErrorMsgContext = mock(IdentityErrorMsgContext.class);
        when(mockIdentityErrorMsgContext.getErrorCode()).thenReturn("17002");
        when(IdentityUtil.getIdentityErrorMsg()).thenReturn(mockIdentityErrorMsgContext);

        when(obThrottleService.isThrottled(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.doNothing().when(obThrottleService)
                .updateThrottleData(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                redirect = (String) invocation.getArguments()[0];
                return null;
            }
        }).when(mockResponse).sendRedirect(anyString());

        mockStatic(IdentityExtensionsDataHolder.class);
        when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolder);
        when(identityExtensionsDataHolder.getOBThrottleService()).thenReturn(obThrottleService);
        when(mockRequest.getParameter("username")).thenReturn(dummyUserName);

        obIdentifierAuthenticator.initiateAuthenticationRequest(mockRequest, mockResponse, mockAuthnCtxt);
    }

    @Test
    public void initiateAuthenticationRequestLockedUserWithNullReasonTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("throttleLimit", "3");
        paramMap.put("throttleTimePeriod", "180");
        paramMap.put("showAuthFailureReason", "true");

        authenticatorConfig.setParameterMap(paramMap);

        mockStatic(FileBasedConfigurationBuilder.class);
        mockFileBasedConfigurationBuilder = mock(FileBasedConfigurationBuilder.class);
        when(FileBasedConfigurationBuilder.getInstance()).thenReturn(mockFileBasedConfigurationBuilder);
        when(mockFileBasedConfigurationBuilder.getAuthenticatorBean(anyString())).thenReturn(authenticatorConfig);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                isUserTenantDomainMismatch = (Boolean) invocation.getArguments()[1];
                return null;
            }
        }).when(mockAuthnCtxt).setProperty(anyString(), anyBoolean());

        when(ConfigurationFacade.getInstance().getAuthenticationEndpointURL()).thenReturn(dummyLoginPage);
        when(mockAuthnCtxt.getContextIdIncludedQueryParams()).thenReturn(dummyQueryParam);
        when(mockAuthnCtxt.isRetrying()).thenReturn(true);

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getClientIpAddress(mockRequest)).thenReturn("127.0.0.1");

        mockIdentityErrorMsgContext = mock(IdentityErrorMsgContext.class);
        when(mockIdentityErrorMsgContext.getErrorCode()).thenReturn("17003");
        when(IdentityUtil.getIdentityErrorMsg()).thenReturn(mockIdentityErrorMsgContext);

        when(obThrottleService.isThrottled(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.doNothing().when(obThrottleService)
                .updateThrottleData(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                redirect = (String) invocation.getArguments()[0];
                return null;
            }
        }).when(mockResponse).sendRedirect(anyString());

        mockStatic(IdentityExtensionsDataHolder.class);
        when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolder);
        when(identityExtensionsDataHolder.getOBThrottleService()).thenReturn(obThrottleService);
        when(mockRequest.getParameter("username")).thenReturn(dummyUserName);

        obIdentifierAuthenticator.initiateAuthenticationRequest(mockRequest, mockResponse, mockAuthnCtxt);
    }

    @Test
    public void initiateAuthenticationRequestLockedUserWithReasonTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("throttleLimit", "3");
        paramMap.put("throttleTimePeriod", "180");
        paramMap.put("showAuthFailureReason", "true");

        authenticatorConfig.setParameterMap(paramMap);

        mockStatic(FileBasedConfigurationBuilder.class);
        mockFileBasedConfigurationBuilder = mock(FileBasedConfigurationBuilder.class);
        when(FileBasedConfigurationBuilder.getInstance()).thenReturn(mockFileBasedConfigurationBuilder);
        when(mockFileBasedConfigurationBuilder.getAuthenticatorBean(anyString())).thenReturn(authenticatorConfig);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                isUserTenantDomainMismatch = (Boolean) invocation.getArguments()[1];
                return null;
            }
        }).when(mockAuthnCtxt).setProperty(anyString(), anyBoolean());

        when(ConfigurationFacade.getInstance().getAuthenticationEndpointURL()).thenReturn(dummyLoginPage);
        when(mockAuthnCtxt.getContextIdIncludedQueryParams()).thenReturn(dummyQueryParam);
        when(mockAuthnCtxt.isRetrying()).thenReturn(true);

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getClientIpAddress(mockRequest)).thenReturn("127.0.0.1");

        mockIdentityErrorMsgContext = mock(IdentityErrorMsgContext.class);
        when(mockIdentityErrorMsgContext.getErrorCode()).thenReturn("17003:reason");
        when(IdentityUtil.getIdentityErrorMsg()).thenReturn(mockIdentityErrorMsgContext);

        when(obThrottleService.isThrottled(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.doNothing().when(obThrottleService)
                .updateThrottleData(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                redirect = (String) invocation.getArguments()[0];
                return null;
            }
        }).when(mockResponse).sendRedirect(anyString());

        mockStatic(IdentityExtensionsDataHolder.class);
        when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolder);
        when(identityExtensionsDataHolder.getOBThrottleService()).thenReturn(obThrottleService);
        when(mockRequest.getParameter("username")).thenReturn(dummyUserName);

        obIdentifierAuthenticator.initiateAuthenticationRequest(mockRequest, mockResponse, mockAuthnCtxt);
    }

    @Test
    public void initiateAuthenticationRequestLockedUserWithRetryAttemptsAndNullReasonTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("throttleLimit", "3");
        paramMap.put("throttleTimePeriod", "180");
        paramMap.put("showAuthFailureReason", "true");

        authenticatorConfig.setParameterMap(paramMap);

        mockStatic(FileBasedConfigurationBuilder.class);
        mockFileBasedConfigurationBuilder = mock(FileBasedConfigurationBuilder.class);
        when(FileBasedConfigurationBuilder.getInstance()).thenReturn(mockFileBasedConfigurationBuilder);
        when(mockFileBasedConfigurationBuilder.getAuthenticatorBean(anyString())).thenReturn(authenticatorConfig);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                isUserTenantDomainMismatch = (Boolean) invocation.getArguments()[1];
                return null;
            }
        }).when(mockAuthnCtxt).setProperty(anyString(), anyBoolean());

        when(ConfigurationFacade.getInstance().getAuthenticationEndpointURL()).thenReturn(dummyLoginPage);
        when(mockAuthnCtxt.getContextIdIncludedQueryParams()).thenReturn(dummyQueryParam);
        when(mockAuthnCtxt.isRetrying()).thenReturn(true);

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getClientIpAddress(mockRequest)).thenReturn("127.0.0.1");

        mockIdentityErrorMsgContext = mock(IdentityErrorMsgContext.class);
        when(mockIdentityErrorMsgContext.getErrorCode()).thenReturn("17003");
        when(IdentityUtil.getIdentityErrorMsg()).thenReturn(mockIdentityErrorMsgContext);

        when(mockIdentityErrorMsgContext.getMaximumLoginAttempts()).thenReturn(4);
        when(mockIdentityErrorMsgContext.getFailedLoginAttempts()).thenReturn(2);

        when(obThrottleService.isThrottled(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.doNothing().when(obThrottleService)
                .updateThrottleData(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                redirect = (String) invocation.getArguments()[0];
                return null;
            }
        }).when(mockResponse).sendRedirect(anyString());

        mockStatic(IdentityExtensionsDataHolder.class);
        when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolder);
        when(identityExtensionsDataHolder.getOBThrottleService()).thenReturn(obThrottleService);
        when(mockRequest.getParameter("username")).thenReturn(dummyUserName);

        obIdentifierAuthenticator.initiateAuthenticationRequest(mockRequest, mockResponse, mockAuthnCtxt);
    }

    @Test
    public void initiateAuthenticationRequestLockedUserWithRetryAttemptsAndReasonTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("throttleLimit", "3");
        paramMap.put("throttleTimePeriod", "180");
        paramMap.put("showAuthFailureReason", "true");

        authenticatorConfig.setParameterMap(paramMap);

        mockStatic(FileBasedConfigurationBuilder.class);
        mockFileBasedConfigurationBuilder = mock(FileBasedConfigurationBuilder.class);
        when(FileBasedConfigurationBuilder.getInstance()).thenReturn(mockFileBasedConfigurationBuilder);
        when(mockFileBasedConfigurationBuilder.getAuthenticatorBean(anyString())).thenReturn(authenticatorConfig);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                isUserTenantDomainMismatch = (Boolean) invocation.getArguments()[1];
                return null;
            }
        }).when(mockAuthnCtxt).setProperty(anyString(), anyBoolean());

        when(ConfigurationFacade.getInstance().getAuthenticationEndpointURL()).thenReturn(dummyLoginPage);
        when(mockAuthnCtxt.getContextIdIncludedQueryParams()).thenReturn(dummyQueryParam);
        when(mockAuthnCtxt.isRetrying()).thenReturn(true);

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getClientIpAddress(mockRequest)).thenReturn("127.0.0.1");

        mockIdentityErrorMsgContext = mock(IdentityErrorMsgContext.class);
        when(mockIdentityErrorMsgContext.getErrorCode()).thenReturn("17003:reason");
        when(IdentityUtil.getIdentityErrorMsg()).thenReturn(mockIdentityErrorMsgContext);

        when(mockIdentityErrorMsgContext.getMaximumLoginAttempts()).thenReturn(4);
        when(mockIdentityErrorMsgContext.getFailedLoginAttempts()).thenReturn(2);

        when(obThrottleService.isThrottled(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.doNothing().when(obThrottleService)
                .updateThrottleData(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                redirect = (String) invocation.getArguments()[0];
                return null;
            }
        }).when(mockResponse).sendRedirect(anyString());

        mockStatic(IdentityExtensionsDataHolder.class);
        when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolder);
        when(identityExtensionsDataHolder.getOBThrottleService()).thenReturn(obThrottleService);
        when(mockRequest.getParameter("username")).thenReturn(dummyUserName);

        obIdentifierAuthenticator.initiateAuthenticationRequest(mockRequest, mockResponse, mockAuthnCtxt);
    }

    @Test
    public void processAuthenticationResponseGeneralTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);
        mockSequenceConfig = mock(SequenceConfig.class);
        mockApplicationConfig = mock(ApplicationConfig.class);
        mockRealmService = mock(RealmService.class);
        mockUserRealm = mock(UserRealm.class);
        mockUserStoreManager = mock(UserStoreManager.class);
        authenticatedUser = mock(AuthenticatedUser.class);

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("ValidateUsername", "true");

        authenticatorConfig.setParameterMap(paramMap);

        mockStatic(FileBasedConfigurationBuilder.class);
        mockFileBasedConfigurationBuilder = mock(FileBasedConfigurationBuilder.class);
        when(FileBasedConfigurationBuilder.getInstance()).thenReturn(mockFileBasedConfigurationBuilder);
        when(mockFileBasedConfigurationBuilder.getAuthenticatorBean(anyString())).thenReturn(authenticatorConfig);

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getClientIpAddress(mockRequest)).thenReturn("127.0.0.1");

        when(obThrottleService.isThrottled(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        mockStatic(IdentityExtensionsDataHolder.class);
        when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolder);
        when(identityExtensionsDataHolder.getOBThrottleService()).thenReturn(obThrottleService);
        when(mockRequest.getParameter("username")).thenReturn(dummyUserName);

        when(mockAuthnCtxt.getSequenceConfig()).thenReturn(mockSequenceConfig);
        when(mockSequenceConfig.getApplicationConfig()).thenReturn(mockApplicationConfig);
        when(mockApplicationConfig.isSaaSApp()).thenReturn(true);

        mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(Mockito.anyString())).thenReturn(dummyTenantId);

        when(identityExtensionsDataHolder.getRealmService()).thenReturn(mockRealmService);
        when(mockRealmService.getTenantUserRealm(Mockito.anyInt())).thenReturn(mockUserRealm);
        when(mockUserRealm.getUserStoreManager()).thenReturn(mockUserStoreManager);

        mockStatic(MultitenantUtils.class);
        when(MultitenantUtils.getTenantAwareUsername(Mockito.anyString())).thenReturn(dummyUserName);
        when(mockUserStoreManager.isExistingUser(Mockito.anyString())).thenReturn(true);

        mockStatic(AuthenticatedUser.class);
        when(AuthenticatedUser
                .createLocalAuthenticatedUserFromSubjectIdentifier(Mockito.anyString())).thenReturn(authenticatedUser);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                authenticatedUser = (AuthenticatedUser) invocation.getArguments()[0];
                return null;
            }
        }).when(mockAuthnCtxt).setSubject(authenticatedUser);

        obIdentifierAuthenticator.processAuthenticationResponse(mockRequest, mockResponse, mockAuthnCtxt);
    }

    @Test(expectedExceptions = InvalidCredentialsException.class, priority = 1)
    public void processAuthenticationResponseNotSaasAppAndInvalidTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);
        mockSequenceConfig = mock(SequenceConfig.class);
        mockApplicationConfig = mock(ApplicationConfig.class);

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.isEmailUsernameEnabled()).thenReturn(true);

        when(mockAuthnCtxt.getSequenceConfig()).thenReturn(mockSequenceConfig);
        when(mockSequenceConfig.getApplicationConfig()).thenReturn(mockApplicationConfig);
        when(mockApplicationConfig.isSaaSApp()).thenReturn(false);

        mockStatic(MultitenantUtils.class);
        when(MultitenantUtils.getTenantDomain(anyString())).thenReturn("carbon.super");
        when(MultitenantUtils.getTenantAwareUsername(Mockito.anyString())).thenReturn(dummyUserName);

        obIdentifierAuthenticator.processAuthenticationResponse(mockRequest, mockResponse, mockAuthnCtxt);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class, priority = 1)
    public void processAuthenticationResponseEmaiEnabledUsernameWithThrottleExceptionTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);
        mockSequenceConfig = mock(SequenceConfig.class);
        mockApplicationConfig = mock(ApplicationConfig.class);

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.isEmailUsernameEnabled()).thenReturn(true);
        when(IdentityUtil.getClientIpAddress(mockRequest)).thenReturn("127.0.0.1");
        when(mockRequest.getParameter("username")).thenReturn("admin@wso2.com");

        mockStatic(IdentityExtensionsDataHolder.class);
        when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolder);
        when(identityExtensionsDataHolder.getOBThrottleService()).thenReturn(obThrottleService);

        Mockito.doThrow(OBThrottlerException.class)
                .when(obThrottleService).isThrottled(Mockito.anyString(), Mockito.anyString());

        when(mockAuthnCtxt.getSequenceConfig()).thenReturn(mockSequenceConfig);
        when(mockSequenceConfig.getApplicationConfig()).thenReturn(mockApplicationConfig);
        when(mockApplicationConfig.isSaaSApp()).thenReturn(false);

        mockStatic(MultitenantUtils.class);
        when(MultitenantUtils.getTenantDomain(anyString())).thenReturn("carbon.super");
        when(MultitenantUtils.getTenantAwareUsername(Mockito.anyString())).thenReturn("admin@wso2.com");

        obIdentifierAuthenticator.processAuthenticationResponse(mockRequest, mockResponse, mockAuthnCtxt);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class, priority = 1)
    public void processAuthenticationResponseNonEmailUserNameWithThrottleExceptionTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);
        mockSequenceConfig = mock(SequenceConfig.class);
        mockApplicationConfig = mock(ApplicationConfig.class);

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.isEmailUsernameEnabled()).thenReturn(false);
        when(IdentityUtil.getClientIpAddress(mockRequest)).thenReturn("127.0.0.1");
        when(mockRequest.getParameter("username")).thenReturn(dummyUserName);

        mockStatic(IdentityExtensionsDataHolder.class);
        when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolder);
        when(identityExtensionsDataHolder.getOBThrottleService()).thenReturn(obThrottleService);

        Mockito.doThrow(OBThrottlerException.class)
                .when(obThrottleService).isThrottled(Mockito.anyString(), Mockito.anyString());

        when(mockAuthnCtxt.getSequenceConfig()).thenReturn(mockSequenceConfig);
        when(mockSequenceConfig.getApplicationConfig()).thenReturn(mockApplicationConfig);
        when(mockApplicationConfig.isSaaSApp()).thenReturn(false);

        mockStatic(MultitenantUtils.class);
        when(MultitenantUtils.getTenantDomain(anyString())).thenReturn("carbon.super");
        when(MultitenantUtils.getTenantAwareUsername(Mockito.anyString())).thenReturn(dummyUserName);

        obIdentifierAuthenticator.processAuthenticationResponse(mockRequest, mockResponse, mockAuthnCtxt);
    }

    @Test
    public void retryAuthenticationEnabledTestCase() throws Exception {

        assertEquals(obIdentifierAuthenticator.retryAuthenticationEnabled(), true);
    }

    @Test
    public void getContextIdentifierTestCase() throws Exception {

        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getParameter(Mockito.anyString())).thenReturn("sessionData");

        assertNotNull(obIdentifierAuthenticator.getContextIdentifier(mockRequest));
    }

    @Test
    public void getFriendlyNameTestCase() throws Exception {

        assertEquals(obIdentifierAuthenticator.getFriendlyName(), "ob-identifier-first");
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void getSessionDataExceptionTestCase() throws Exception {

        mockRealmService = mock(RealmService.class);
        mockUserCoreRealm = mock(org.wso2.carbon.user.core.UserRealm.class);
        mockUserStoreManager = mock(UserStoreManager.class);

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("authRequestURL", "someURL");

        authenticatorConfig.setParameterMap(paramMap);

        mockStatic(FileBasedConfigurationBuilder.class);
        mockFileBasedConfigurationBuilder = mock(FileBasedConfigurationBuilder.class);
        when(FileBasedConfigurationBuilder.getInstance()).thenReturn(mockFileBasedConfigurationBuilder);
        when(mockFileBasedConfigurationBuilder.getAuthenticatorBean(anyString())).thenReturn(authenticatorConfig);

        mockStatic(IdentityExtensionsDataHolder.class);
        RealmConfiguration mockRealmConfiguration = mock(RealmConfiguration.class);
        when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolder);
        when(identityExtensionsDataHolder.getRealmService()).thenReturn(mockRealmService);
        when(mockRealmService.getBootstrapRealm()).thenReturn(mockUserCoreRealm);
        when(mockUserCoreRealm.getUserStoreManager()).thenReturn(mockUserStoreManager);
        when(mockUserStoreManager.getRealmConfiguration()).thenReturn(mockRealmConfiguration);

        when(mockRealmConfiguration.getAdminUserName()).thenReturn("adminUserName");
        when(mockRealmConfiguration.getAdminPassword()).thenReturn("adminPassword");

        mockStatic(HTTPClientUtils.class);
        CloseableHttpClient closeableHttpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
        InputStream inputStream = mock(InputStream.class);
        HttpEntity httpEntity  = mock(HttpEntity.class);
        BufferedReader bufferedReader = mock(BufferedReader.class);
        StatusLine statusLine = mock(StatusLine.class);
        final HttpGet[] httpGet = {mock(HttpGet.class)};
        when(HTTPClientUtils.getHttpsClientInstance()).thenReturn(closeableHttpClient);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                httpGet[0] = (HttpGet) invocation.getArguments()[0];
                return closeableHttpResponse;
            }
        }).when(closeableHttpClient).execute(Mockito.anyObject());

        when(closeableHttpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(inputStream);
        when(bufferedReader.readLine()).thenReturn("sessionDataValues");
        when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);

        obIdentifierAuthenticator.getSessionData(dummySessionDataKey);
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void getSessionDataUserStoreExceptionTestCase() throws Exception {

        mockRealmService = mock(RealmService.class);
        mockUserCoreRealm = mock(org.wso2.carbon.user.core.UserRealm.class);
        mockUserStoreManager = mock(UserStoreManager.class);

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("authRequestURL", "someURL");

        authenticatorConfig.setParameterMap(paramMap);

        mockStatic(FileBasedConfigurationBuilder.class);
        mockFileBasedConfigurationBuilder = mock(FileBasedConfigurationBuilder.class);
        when(FileBasedConfigurationBuilder.getInstance()).thenReturn(mockFileBasedConfigurationBuilder);
        when(mockFileBasedConfigurationBuilder.getAuthenticatorBean(anyString())).thenReturn(authenticatorConfig);

        mockStatic(IdentityExtensionsDataHolder.class);
        RealmConfiguration mockRealmConfiguration = mock(RealmConfiguration.class);
        when(IdentityExtensionsDataHolder.getInstance()).thenReturn(identityExtensionsDataHolder);
        when(identityExtensionsDataHolder.getRealmService()).thenReturn(mockRealmService);
        when(mockRealmService.getBootstrapRealm()).thenThrow(UserStoreException.class);

        obIdentifierAuthenticator.getSessionData(dummySessionDataKey);
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }
}
