package com.wso2.openbanking.accelerator.identity.app2app;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.app2app.utils.App2AppAuthUtils;
import org.mockito.Mockito;
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

import static org.powermock.api.mockito.PowerMockito.*;
import static org.testng.Assert.assertEquals;

@PrepareForTest({App2AppAuthUtils.class, JWTUtils.class})
@PowerMockIgnore({"javax.net.ssl.*", "jdk.internal.reflect.*"})
public class App2AppAuthenticatorTest {
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private AuthenticationContext mockAuthnCtxt;
    private App2AppAuthenticator app2AppAuthenticator;

    @BeforeTest
    public void setup(){
        app2AppAuthenticator = new App2AppAuthenticator();
    }

    @Test
    public void testGetName() {
        App2AppAuthenticator authenticator = new App2AppAuthenticator();
        String expectedName = App2AppAuthenticatorConstants.AUTHENTICATOR_NAME;
        String actualName = authenticator.getName();
        assertEquals(actualName, expectedName, "Expected and actual names should match");
    }

    @Test
    public void testGetFriendlyName() {
        App2AppAuthenticator authenticator = new App2AppAuthenticator();
        String expectedFriendlyName = App2AppAuthenticatorConstants.AUTHENTICATOR_FRIENDLY_NAME;
        String actualFriendlyName = authenticator.getFriendlyName();
        assertEquals(actualFriendlyName, expectedFriendlyName, "Expected and actual friendly names should match");
    }
    @Test(dataProviderClass =App2AppAuthenticatorTestDataProvider.class ,
            dataProvider = "UsernameAndPasswordProvider")
    public void canHandleTestCase(String secret, String expected) {

        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getParameter(App2AppAuthenticatorConstants.APP_AUTH_VALIDATION_JWT_IDENTIFIER)).thenReturn(secret);
        assertEquals(Boolean.valueOf(expected).booleanValue(), app2AppAuthenticator.canHandle(mockRequest),
                "Invalid can handle response for the request.");
    }

    @Test
    public void initiateAuthenticationRequest(){
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockAuthnCtxt = mock(AuthenticationContext.class);
        try {
            app2AppAuthenticator.initiateAuthenticationRequest(mockRequest,mockResponse,mockAuthnCtxt);
        }catch (AuthenticationFailedException e){
            AuthenticationFailedException expected =
                    new AuthenticationFailedException("Mandatory parameter secret null or " +
                            "empty in request.");
            assertEquals(e.getMessage(), expected.getMessage(),
                    "Invalid initiateAuthenticationRequest Response.");
        }
    }

    @Test(dataProviderClass = App2AppAuthenticatorTestDataProvider.class,
            dataProvider = "sessionDataKeyProvider")
    public void getContextIdentifierTest(String sessionDataKey){
        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getParameter(App2AppAuthenticatorConstants.SESSION_DATA_KEY)).thenReturn(sessionDataKey);
        String output = app2AppAuthenticator.getContextIdentifier(mockRequest);
        assertEquals(sessionDataKey,output);
    }

    @Test(dataProviderClass = App2AppAuthenticatorTestDataProvider.class,
            dataProvider = "JWTProvider")
    public void testProcessAuthenticationResponse_success(String jwtString){
        mockStatic(App2AppAuthUtils.class);
        // Mock HttpServletRequest, HttpServletResponse, and AuthenticationContext
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        HttpServletResponse responseMock = mock(HttpServletResponse.class);
        AuthenticationContext authContextMock = mock(AuthenticationContext.class);

        // Set up mock behavior for HttpServletRequest
        when(requestMock.getParameter(App2AppAuthenticatorConstants.APP_AUTH_VALIDATION_JWT_IDENTIFIER))
                .thenReturn(jwtString);

        // Mock the authenticated user
        AuthenticatedUser authenticatedUserMock = mock(AuthenticatedUser.class);

        // Mock the behavior of App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier() to return a mocked user
        when(App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier(Mockito.anyString()))
                .thenReturn(authenticatedUserMock);

        try {;
            app2AppAuthenticator.processAuthenticationResponse(requestMock, responseMock, authContextMock);

            // Verify that the authentication context subject is set (or any other verification)
            Mockito.verify(authContextMock).setSubject(authenticatedUserMock);
        } catch (Exception e) {
            // If any unexpected exception occurs, fail the test
            Assert.fail("Unexpected exception occurred: " + e.getMessage());
        }
    }
    @Test(expectedExceptions = AuthenticationFailedException.class,
            expectedExceptionsMessageRegExp
                    = ".*Failed to create Local Authenticated User from the given subject identifier. Invalid argument. authenticatedSubjectIdentifier : .*",
            dataProviderClass = App2AppAuthenticatorTestDataProvider.class,
            dataProvider = "JWTProvider"
    )
    public void testProcessAuthenticationResponse_IllegalArgumentException(String jwtString) throws AuthenticationFailedException {
        mockStatic(App2AppAuthUtils.class);
        // Create mock objects for dependencies
        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        AuthenticationContext authContextMock = Mockito.mock(AuthenticationContext.class);

        // Mock the behavior of HttpServletRequest to return a value for login hint
        Mockito.when(requestMock.getParameter(App2AppAuthenticatorConstants.APP_AUTH_VALIDATION_JWT_IDENTIFIER))
                .thenReturn(jwtString);

        // Mock App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier to throw IllegalArgumentException
        Mockito.when(App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier(Mockito.anyString()))
                .thenThrow(new IllegalArgumentException("Failed to create Local Authenticated User from the given subject identifier. Invalid argument. authenticatedSubjectIdentifier : "));

        // Invoke the method under test
        app2AppAuthenticator.processAuthenticationResponse(requestMock, responseMock, authContextMock);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class,
            dataProviderClass = App2AppAuthenticatorTestDataProvider.class,
            dataProvider = "JWTProvider"
    )
    public void testProcessAuthenticationResponse_ParseException(String jwtString) throws AuthenticationFailedException, ParseException {
        mockStatic(JWTUtils.class);

        // Create mock objects for dependencies
        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        AuthenticationContext authContextMock = Mockito.mock(AuthenticationContext.class);

        // Mock the behavior of HttpServletRequest to return a value for login hint
        Mockito.when(requestMock.getParameter(App2AppAuthenticatorConstants.APP_AUTH_VALIDATION_JWT_IDENTIFIER))
                .thenReturn(jwtString);

        // Mock App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier to throw IllegalArgumentException
        Mockito.when(JWTUtils.getSignedJWT(Mockito.anyString()))
                .thenThrow(new ParseException("JWT Not parsable.",1));

        // Invoke the method under test
        app2AppAuthenticator.processAuthenticationResponse(requestMock, responseMock, authContextMock);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class,
            dataProviderClass = App2AppAuthenticatorTestDataProvider.class,
            dataProvider = "JWTProvider"
    )
    public void testProcessAuthenticationResponse_UserStoreException(String jwtString) throws AuthenticationFailedException, UserStoreException {

        mockStatic(App2AppAuthUtils.class);
        // Create mock objects for dependencies
        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        AuthenticationContext authContextMock = Mockito.mock(AuthenticationContext.class);

        // Mock the behavior of HttpServletRequest to return a value for login hint
        Mockito.when(requestMock.getParameter(App2AppAuthenticatorConstants.APP_AUTH_VALIDATION_JWT_IDENTIFIER))
                .thenReturn(jwtString);

        // Mock the behavior of App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier() to return a mock user
        AuthenticatedUser authenticatedUserMock = Mockito.mock(AuthenticatedUser.class);
        Mockito.when(App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier(Mockito.anyString()))
                .thenReturn(authenticatedUserMock);

        // Mock the behavior of getPublicKeyByDeviceID() to throw UserStoreException
        Mockito.when(App2AppAuthUtils.getUserRealm(Mockito.any(AuthenticatedUser.class)))
                .thenThrow(new UserStoreException(App2AppAuthenticatorConstants.USER_STORE_EXCEPTION_MESSAGE));

        // Invoke the method under test
        app2AppAuthenticator.processAuthenticationResponse(requestMock, responseMock, authContextMock);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class,
            dataProviderClass = App2AppAuthenticatorTestDataProvider.class,
            dataProvider = "JWTProvider"
    )
    public void testProcessAuthenticationResponse_PushDeviceHandlerServerException(String jwtString)
            throws AuthenticationFailedException,OpenBankingException, PushDeviceHandlerServerException,
            PushDeviceHandlerClientException {

        mockStatic(App2AppAuthUtils.class);
        // Create mock objects for dependencies
        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        AuthenticationContext authContextMock = Mockito.mock(AuthenticationContext.class);

        // Mock the behavior of HttpServletRequest to return a value for login hint
        Mockito.when(requestMock.getParameter(App2AppAuthenticatorConstants.APP_AUTH_VALIDATION_JWT_IDENTIFIER))
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
        app2AppAuthenticator.processAuthenticationResponse(requestMock, responseMock, authContextMock);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class,
            dataProviderClass = App2AppAuthenticatorTestDataProvider.class,
            dataProvider = "JWTProvider"
    )
    public void testProcessAuthenticationResponse_PushDeviceHandlerClientException(String jwtString)
            throws AuthenticationFailedException,OpenBankingException, PushDeviceHandlerServerException,
            PushDeviceHandlerClientException {

        mockStatic(App2AppAuthUtils.class);
        // Create mock objects for dependencies
        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        AuthenticationContext authContextMock = Mockito.mock(AuthenticationContext.class);

        // Mock the behavior of HttpServletRequest to return a value for login hint
        Mockito.when(requestMock.getParameter(App2AppAuthenticatorConstants.APP_AUTH_VALIDATION_JWT_IDENTIFIER))
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
        app2AppAuthenticator.processAuthenticationResponse(requestMock, responseMock, authContextMock);
    }

    @Test(expectedExceptions = AuthenticationFailedException.class,
            dataProviderClass = App2AppAuthenticatorTestDataProvider.class,
            dataProvider = "JWTProvider"
    )
    public void testProcessAuthenticationResponse_OpenBankingException(String jwtString)
            throws AuthenticationFailedException,OpenBankingException, PushDeviceHandlerServerException,
            PushDeviceHandlerClientException {

        mockStatic(App2AppAuthUtils.class);
        // Create mock objects for dependencies
        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        AuthenticationContext authContextMock = Mockito.mock(AuthenticationContext.class);

        // Mock the behavior of HttpServletRequest to return a value for login hint
        Mockito.when(requestMock.getParameter(App2AppAuthenticatorConstants.APP_AUTH_VALIDATION_JWT_IDENTIFIER))
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
        app2AppAuthenticator.processAuthenticationResponse(requestMock, responseMock, authContextMock);
    }
    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }
}
