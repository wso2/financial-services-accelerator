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

package com.wso2.openbanking.accelerator.authentication.data.publisher.service;

import com.wso2.openbanking.accelerator.authentication.data.publisher.constant.AuthPublisherConstants;
import com.wso2.openbanking.accelerator.authentication.data.publisher.extension.DefaultAuthDataPublisher;
import com.wso2.openbanking.accelerator.authentication.data.publisher.internal.AuthenticationDataPublisherDataHolder;
import com.wso2.openbanking.accelerator.data.publisher.common.util.OBDataPublisherUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test for Open Banking default Authentication Data Publisher.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({OBDataPublisherUtil.class})
public class OBAuthDataPublisherTest {

    public static final Map<String, String> SCRIPT_DATA_MAP = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>("key1", "value1"),
            new AbstractMap.SimpleImmutableEntry<>("key2", "value2"),
            new AbstractMap.SimpleImmutableEntry<>("key3", "value3"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    public static final Map<String, String> ADDITIONAL_DATA_MAP = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>("_key1", "_value1"),
            new AbstractMap.SimpleImmutableEntry<>("_key2", "_value2"),
            new AbstractMap.SimpleImmutableEntry<>("_key3", "_value3"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    public static final Map<String, String> ASSERT_DATA_MAP = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>("_key1", "_value1"),
            new AbstractMap.SimpleImmutableEntry<>("_key2", "_value2"),
            new AbstractMap.SimpleImmutableEntry<>("_key3", "_value3"),
            new AbstractMap.SimpleImmutableEntry<>("key1", "value1"),
            new AbstractMap.SimpleImmutableEntry<>("key2", "value2"),
            new AbstractMap.SimpleImmutableEntry<>("key3", "value3"),
            new AbstractMap.SimpleImmutableEntry<>(AuthPublisherConstants.USER_ID, "bob@wso2.com"),
            new AbstractMap.SimpleImmutableEntry<>(AuthPublisherConstants
                    .AUTHENTICATION_APPROACH, AuthPublisherConstants.REDIRECT),
            new AbstractMap.SimpleImmutableEntry<>(AuthPublisherConstants
                    .AUTHENTICATION_STATUS, AuthPublisherConstants.AUTHENTICATION_SUCCESSFUL),
            new AbstractMap.SimpleImmutableEntry<>(AuthPublisherConstants
                    .AUTHENTICATION_STEP, AuthPublisherConstants.BASIC_AUTHENTICATOR))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    private static ByteArrayOutputStream outContent;
    private static Logger logger = null;
    private static PrintStream printStream;

    @BeforeClass
    public void initializeConfigurations() {

        OBAuthDataPublisherTest.outContent = new ByteArrayOutputStream();
        OBAuthDataPublisherTest.printStream = new PrintStream(OBAuthDataPublisherTest.outContent);
        System.setOut(OBAuthDataPublisherTest.printStream);
        OBAuthDataPublisherTest.logger = LogManager.getLogger(OBAuthDataPublisherTest.class);
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test
    public void invokeFunctionWithoutUserID() throws Exception {

        outContent.reset();
        PowerMockito.mockStatic(OBDataPublisherUtil.class);
        AuthenticationDataPublisherServiceImpl
                authenticationDataPublisherService = Mockito.spy(AuthenticationDataPublisherServiceImpl.class);
        DefaultAuthDataPublisher authDataPublisher = Mockito.mock(DefaultAuthDataPublisher.class);
        AuthenticationDataPublisherDataHolder
                authenticationDataPublisherDataHolder = Mockito.spy(AuthenticationDataPublisherDataHolder.class);
        authenticationDataPublisherDataHolder.setAuthDataPublisher(authDataPublisher);
        Mockito.doReturn(authenticationDataPublisherDataHolder).when(authenticationDataPublisherService)
                .getAuthenticationDataPublisherDataHolder();
        PowerMockito.doNothing().when(OBDataPublisherUtil.class,
                "publishData", Mockito.anyString(), Mockito.anyString(), Mockito.any());
        AuthenticationContext authContext = new AuthenticationContext();

        //Setting null values for all possible user ID data
        authContext.addParameter(AuthPublisherConstants.AUTHENTICATED_USER, null);
        authContext.addParameter(AuthPublisherConstants.LAST_LOGIN_FAILED_USER, null);
        authContext.setCurrentAuthenticator(AuthPublisherConstants.BASIC_AUTHENTICATOR);
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setAuthenticatedSubjectIdentifier(null);
        authContext.setSubject(authenticatedUser);
        JsAuthenticationContext context = new JsAuthenticationContext(authContext);
        authenticationDataPublisherService
                .authDataExtractor(context, AuthPublisherConstants.AUTHENTICATION_FAILED, SCRIPT_DATA_MAP);

        Assert.assertTrue(OBAuthDataPublisherTest.outContent.toString().contains("Failed to retrieve the " +
                "user name relating to the authentication"));
    }

    @Test
    public void invokeFunctionWithoutAuthenticationStep() throws Exception {

        outContent.reset();
        PowerMockito.mockStatic(OBDataPublisherUtil.class);
        AuthenticationDataPublisherServiceImpl
                authenticationDataPublisherService = Mockito.spy(AuthenticationDataPublisherServiceImpl.class);
        DefaultAuthDataPublisher authDataPublisher = Mockito.mock(DefaultAuthDataPublisher.class);
        AuthenticationDataPublisherDataHolder
                authenticationDataPublisherDataHolder = Mockito.spy(AuthenticationDataPublisherDataHolder.class);
        authenticationDataPublisherDataHolder.setAuthDataPublisher(authDataPublisher);
        Mockito.doReturn(authenticationDataPublisherDataHolder).when(authenticationDataPublisherService)
                .getAuthenticationDataPublisherDataHolder();
        PowerMockito.doNothing().when(OBDataPublisherUtil.class,
                "publishData", Mockito.anyString(), Mockito.anyString(), Mockito.any());
        AuthenticationContext authContext = new AuthenticationContext();

        authContext.addParameter(AuthPublisherConstants.AUTHENTICATED_USER, "mark@wso2.com");
        authContext.addParameter(AuthPublisherConstants.LAST_LOGIN_FAILED_USER, "anne@wso2.com");

        //Setting null value for authentication step
        authContext.setCurrentAuthenticator(null);
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setAuthenticatedSubjectIdentifier("bob@wso2.com");
        authContext.setSubject(authenticatedUser);
        JsAuthenticationContext context = new JsAuthenticationContext(authContext);
        authenticationDataPublisherService
                .authDataExtractor(context, AuthPublisherConstants.AUTHENTICATION_SUCCESSFUL, SCRIPT_DATA_MAP);

        Assert.assertTrue(OBAuthDataPublisherTest.outContent.toString().contains("Failed to retrieve the " +
                "authentication step relating to the authentication"));
    }

    @Test
    public void publishData() throws Exception {

        outContent.reset();
        PowerMockito.mockStatic(OBDataPublisherUtil.class);

        //Mocking classes
        AuthenticationDataPublisherServiceImpl
                authenticationDataPublisherService = Mockito.spy(AuthenticationDataPublisherServiceImpl.class);
        DefaultAuthDataPublisher authDataPublisher = Mockito.mock(DefaultAuthDataPublisher.class);
        AuthenticationDataPublisherDataHolder
                authenticationDataPublisherDataHolder = Mockito.spy(AuthenticationDataPublisherDataHolder.class);
        Mockito.doReturn(ADDITIONAL_DATA_MAP).when(authDataPublisher).getAdditionalData(Mockito.any(), Mockito.any());
        authenticationDataPublisherDataHolder.setAuthDataPublisher(authDataPublisher);
        Mockito.doReturn(authenticationDataPublisherDataHolder).when(authenticationDataPublisherService)
                .getAuthenticationDataPublisherDataHolder();

        //Invoking the method
        AuthenticationContext authContext = new AuthenticationContext();
        authContext.addParameter(AuthPublisherConstants.AUTHENTICATED_USER, "mark@wso2.com");
        authContext.addParameter(AuthPublisherConstants.LAST_LOGIN_FAILED_USER, "anne@wso2.com");
        authContext.setCurrentAuthenticator(AuthPublisherConstants.BASIC_AUTHENTICATOR);
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setAuthenticatedSubjectIdentifier("bob@wso2.com");
        authContext.setSubject(authenticatedUser);
        JsAuthenticationContext context = new JsAuthenticationContext(authContext);
        ArgumentCaptor<String> argumentStream = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentVersion = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> argumentData = ArgumentCaptor.forClass(Map.class);
        PowerMockito.doNothing().when(OBDataPublisherUtil.class,
                "publishData", argumentStream.capture(), argumentVersion.capture(), argumentData.capture());
        authenticationDataPublisherService
                .authDataExtractor(context, AuthPublisherConstants.AUTHENTICATION_SUCCESSFUL, SCRIPT_DATA_MAP);

        Map<String, Object> receivedMap = argumentData.getValue();
        receivedMap.remove(AuthPublisherConstants.TIMESTAMP);

        //Assert the values passed to the publish() method
        Assert.assertEquals(argumentStream.getValue(), AuthPublisherConstants.AUTHENTICATION_INPUT_STREAM);
        Assert.assertEquals(argumentVersion.getValue(), AuthPublisherConstants.STREAM_VERSION);
        Assert.assertEquals(receivedMap, ASSERT_DATA_MAP);
    }
}
