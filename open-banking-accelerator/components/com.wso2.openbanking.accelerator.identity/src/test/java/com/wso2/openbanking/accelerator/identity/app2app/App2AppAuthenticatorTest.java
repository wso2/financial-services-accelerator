package com.wso2.openbanking.accelerator.identity.app2app;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;

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
        when(mockRequest.getParameter(App2AppAuthenticatorConstants.AppAuthValidationJWTIdentifier)).thenReturn(secret);
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

    @Test(expectedExceptions = AuthenticationFailedException.class)
    public void testProcessAuthenticationResponse_InvalidJWT(String jwtString) throws AuthenticationFailedException {
        // Mock HttpServletRequest, HttpServletResponse, and AuthenticationContext
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        HttpServletResponse responseMock = mock(HttpServletResponse.class);
        AuthenticationContext authContextMock = mock(AuthenticationContext.class);

        // Set up mock behavior for HttpServletRequest
        when(requestMock.getParameter(App2AppAuthenticatorConstants.AppAuthValidationJWTIdentifier)).thenReturn(jwtString);
        // Call the method under test, expecting an exception
        try {
            App2AppAuthenticator authenticator = new App2AppAuthenticator();
            authenticator.processAuthenticationResponse(requestMock, responseMock, authContextMock);
        }catch (AuthenticationFailedException e) {
            throw e;
        }catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}
