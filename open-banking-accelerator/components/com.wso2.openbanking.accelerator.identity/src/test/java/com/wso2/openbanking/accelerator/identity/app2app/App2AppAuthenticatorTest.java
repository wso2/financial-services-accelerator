/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.identity.app2app;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.app2app.utils.App2AppAuthUtils;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.exception.PushDeviceHandlerClientException;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.exception.PushDeviceHandlerServerException;
import org.wso2.carbon.user.api.UserStoreException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;

import static org.testng.Assert.assertEquals;

/**
 * Test class for unit testing App2AppAuthenticator.
 */
@PrepareForTest({App2AppAuthUtils.class, JWTUtils.class})
@PowerMockIgnore({"javax.net.ssl.*", "jdk.internal.reflect.*"})
public class App2AppAuthenticatorTest {

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;

    private AuthenticationContext mockAuthenticationContext;
    private App2AppAuthenticator app2AppAuthenticator;

    @BeforeTest
    public void setup(){

        app2AppAuthenticator = new App2AppAuthenticator();
        mockRequest = Mockito.mock(HttpServletRequest.class);
        mockResponse = Mockito.mock(HttpServletResponse.class);
        mockAuthenticationContext = Mockito.mock(AuthenticationContext.class);
    }

    @Test
    public void testGetName() {

        String expectedName = App2AppAuthenticatorConstants.AUTHENTICATOR_NAME;
        String actualName = app2AppAuthenticator.getName();
        assertEquals(actualName, expectedName, "Expected and actual names should match.");

    }

    @Test
    public void testGetFriendlyName() {

        String expectedFriendlyName = App2AppAuthenticatorConstants.AUTHENTICATOR_FRIENDLY_NAME;
        String actualFriendlyName = app2AppAuthenticator.getFriendlyName();
        assertEquals(actualFriendlyName, expectedFriendlyName,
                "Expected and actual friendly names should match");

    }

    @Test(dataProviderClass =App2AppAuthenticatorTestDataProvider.class ,
            dataProvider = "app_auth_identifier_provider")
    public void canHandleTestCase(String secret, String expected) {

        Mockito.when(mockRequest.getParameter(App2AppAuthenticatorConstants.APP_AUTH_VALIDATION_JWT_IDENTIFIER)).thenReturn(secret);
        assertEquals(Boolean.valueOf(expected).booleanValue(), app2AppAuthenticator.canHandle(mockRequest),
                "Invalid can handle response for the request.");
    }

    @Test(expectedExceptions = AuthenticationFailedException.class)
    public void initiateAuthenticationRequest() throws AuthenticationFailedException {

        app2AppAuthenticator.initiateAuthenticationRequest(mockRequest,mockResponse,mockAuthenticationContext);

    }

    @Test(dataProviderClass = App2AppAuthenticatorTestDataProvider.class,
            dataProvider = "sessionDataKeyProvider")
    public void getContextIdentifierTest(String sessionDataKey){

        Mockito.when(mockRequest.getParameter(App2AppAuthenticatorConstants.SESSION_DATA_KEY)).thenReturn(sessionDataKey);
        String output = app2AppAuthenticator.getContextIdentifier(mockRequest);
        assertEquals(sessionDataKey,output);

    }

    @Test(dataProviderClass = App2AppAuthenticatorTestDataProvider.class,
            dataProvider = "JWTProvider")
    public void testProcessAuthenticationResponse_success(String jwtString){

        PowerMockito.mockStatic(App2AppAuthUtils.class);

        // Set up mock behavior for HttpServletRequest
        Mockito.when(mockRequest.getParameter(App2AppAuthenticatorConstants.APP_AUTH_VALIDATION_JWT_IDENTIFIER))
                .thenReturn(jwtString);

        // Mock the authenticated user
        AuthenticatedUser authenticatedUserMock = Mockito.mock(AuthenticatedUser.class);

        // Mock the behavior of App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier() to return a mocked user
        Mockito.when(App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier(Mockito.anyString()))
                .thenReturn(authenticatedUserMock);

        try {;
            app2AppAuthenticator.processAuthenticationResponse(mockRequest, mockResponse, mockAuthenticationContext);

            // Verify that the authentication context subject is set (or any other verification)
            Mockito.verify(mockAuthenticationContext).setSubject(authenticatedUserMock);
        } catch (Exception e) {
            // If any unexpected exception occurs, fail the test
            Assert.fail("Unexpected exception occurred: " + e.getMessage());
        }
    }
    @Test(expectedExceptions = AuthenticationFailedException.class,
            expectedExceptionsMessageRegExp
                    = ".*Failed to create Local Authenticated User from the given subject identifier. " +
                    "Invalid argument. authenticatedSubjectIdentifier : .*",
            dataProviderClass = App2AppAuthenticatorTestDataProvider.class,
            dataProvider = "JWTProvider"
    )
    public void testProcessAuthenticationResponse_IllegalArgumentException(String jwtString)
            throws AuthenticationFailedException {

        PowerMockito.mockStatic(App2AppAuthUtils.class);

        // Mock the behavior of HttpServletRequest to return a value for login hint
        Mockito.when(mockRequest.getParameter(App2AppAuthenticatorConstants.APP_AUTH_VALIDATION_JWT_IDENTIFIER))
                .thenReturn(jwtString);

        // Mock App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier to throw IllegalArgumentException
        Mockito.when(App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier(Mockito.anyString()))
                .thenThrow(new IllegalArgumentException("Failed to create Local Authenticated User from the given " +
                        "subject identifier. Invalid argument. authenticatedSubjectIdentifier : "));

        // Invoke the method under test
        app2AppAuthenticator.processAuthenticationResponse(mockRequest, mockResponse, mockAuthenticationContext);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class,
            dataProviderClass = App2AppAuthenticatorTestDataProvider.class,
            dataProvider = "JWTProvider"
    )
    public void testProcessAuthenticationResponse_ParseException(String jwtString)
            throws AuthenticationFailedException, ParseException {

        PowerMockito.mockStatic(JWTUtils.class);

        // Mock the behavior of HttpServletRequest to return a value for login hint
        Mockito.when(mockRequest.getParameter(App2AppAuthenticatorConstants.APP_AUTH_VALIDATION_JWT_IDENTIFIER))
                .thenReturn(jwtString);

        // Mock App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier to throw IllegalArgumentException
        Mockito.when(JWTUtils.getSignedJWT(Mockito.anyString()))
                .thenThrow(new ParseException("JWT Not parsable.",1));

        // Invoke the method under test
        app2AppAuthenticator.processAuthenticationResponse(mockRequest, mockResponse, mockAuthenticationContext);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class,
            dataProviderClass = App2AppAuthenticatorTestDataProvider.class,
            dataProvider = "JWTProvider"
    )
    public void testProcessAuthenticationResponse_UserStoreException(String jwtString)
            throws AuthenticationFailedException, UserStoreException {

        PowerMockito.mockStatic(App2AppAuthUtils.class);

        // Mock the behavior of HttpServletRequest to return a value for login hint
        Mockito.when(mockRequest.getParameter(App2AppAuthenticatorConstants.APP_AUTH_VALIDATION_JWT_IDENTIFIER))
                .thenReturn(jwtString);

        // Mock the behavior of App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier() to return a mock user
        AuthenticatedUser authenticatedUserMock = Mockito.mock(AuthenticatedUser.class);
        Mockito.when(App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier(Mockito.anyString()))
                .thenReturn(authenticatedUserMock);

        // Mock the behavior of getPublicKeyByDeviceID() to throw UserStoreException
        Mockito.when(App2AppAuthUtils.getUserRealm(Mockito.any(AuthenticatedUser.class)))
                .thenThrow(new UserStoreException(App2AppAuthenticatorConstants.USER_STORE_EXCEPTION_MESSAGE));

        // Invoke the method under test
        app2AppAuthenticator.processAuthenticationResponse(mockRequest, mockResponse, mockAuthenticationContext);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class,
            dataProviderClass = App2AppAuthenticatorTestDataProvider.class,
            dataProvider = "JWTProvider"
    )
    public void testProcessAuthenticationResponse_PushDeviceHandlerServerException(String jwtString)
            throws AuthenticationFailedException,OpenBankingException, PushDeviceHandlerServerException,
            PushDeviceHandlerClientException {

        PowerMockito.mockStatic(App2AppAuthUtils.class);

        // Mock the behavior of HttpServletRequest to return a value for login hint
        Mockito.when(mockRequest.getParameter(App2AppAuthenticatorConstants.APP_AUTH_VALIDATION_JWT_IDENTIFIER))
                .thenReturn(jwtString);

        // Mock the behavior of App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier() to return a mock user
        AuthenticatedUser authenticatedUserMock = Mockito.mock(AuthenticatedUser.class);
        Mockito.when(App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier(Mockito.anyString()))
                .thenReturn(authenticatedUserMock);

        // Mock the behavior of getPublicKeyByDeviceID() to throw UserStoreException
        Mockito.when(App2AppAuthUtils.getPublicKey(Mockito.anyString(),Mockito.anyString(), Mockito.any()))
                .thenThrow(new PushDeviceHandlerServerException(
                        App2AppAuthenticatorConstants.PUSH_DEVICE_HANDLER_SERVER_EXCEPTION_MESSAGE));

        // Invoke the method under test
        app2AppAuthenticator.processAuthenticationResponse(mockRequest, mockResponse, mockAuthenticationContext);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class,
            dataProviderClass = App2AppAuthenticatorTestDataProvider.class,
            dataProvider = "JWTProvider"
    )
    public void testProcessAuthenticationResponse_PushDeviceHandlerClientException(String jwtString)
            throws AuthenticationFailedException,OpenBankingException, PushDeviceHandlerServerException,
            PushDeviceHandlerClientException {

        PowerMockito.mockStatic(App2AppAuthUtils.class);

        // Mock the behavior of HttpServletRequest to return a value for login hint
        Mockito.when(mockRequest.getParameter(App2AppAuthenticatorConstants.APP_AUTH_VALIDATION_JWT_IDENTIFIER))
                .thenReturn(jwtString);

        // Mock the behavior of App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier() to return a mock user
        AuthenticatedUser authenticatedUserMock = Mockito.mock(AuthenticatedUser.class);
        Mockito.when(App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier(Mockito.anyString()))
                .thenReturn(authenticatedUserMock);

        // Mock the behavior of getPublicKeyByDeviceID() to throw UserStoreException
        Mockito.when(App2AppAuthUtils.getPublicKey(Mockito.anyString(),Mockito.anyString(), Mockito.any()))
                .thenThrow(new PushDeviceHandlerClientException(
                        App2AppAuthenticatorConstants.PUSH_DEVICE_HANDLER_CLIENT_EXCEPTION_MESSAGE));

        // Invoke the method under test
        app2AppAuthenticator.processAuthenticationResponse(mockRequest, mockResponse, mockAuthenticationContext);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class,
            dataProviderClass = App2AppAuthenticatorTestDataProvider.class,
            dataProvider = "JWTProvider"
    )
    public void testProcessAuthenticationResponse_OpenBankingException(String jwtString)
            throws AuthenticationFailedException,OpenBankingException, PushDeviceHandlerServerException,
            PushDeviceHandlerClientException {

        PowerMockito.mockStatic(App2AppAuthUtils.class);

        // Mock the behavior of HttpServletRequest to return a value for login hint
        Mockito.when(mockRequest.getParameter(App2AppAuthenticatorConstants.APP_AUTH_VALIDATION_JWT_IDENTIFIER))
                .thenReturn(jwtString);

        // Mock the behavior of App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier() to return a mock user
        AuthenticatedUser authenticatedUserMock = Mockito.mock(AuthenticatedUser.class);
        Mockito.when(App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier(Mockito.anyString()))
                .thenReturn(authenticatedUserMock);

        // Mock the behavior of getPublicKeyByDeviceID() to throw UserStoreException
        Mockito.when(App2AppAuthUtils.getPublicKey(Mockito.anyString(),Mockito.anyString(), Mockito.any()))
                .thenThrow(new OpenBankingException(
                        App2AppAuthenticatorConstants.OPEN_BANKING_EXCEPTION_MESSAGE));

        // Invoke the method under test
        app2AppAuthenticator.processAuthenticationResponse(mockRequest, mockResponse, mockAuthenticationContext);
    }
    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }
}
