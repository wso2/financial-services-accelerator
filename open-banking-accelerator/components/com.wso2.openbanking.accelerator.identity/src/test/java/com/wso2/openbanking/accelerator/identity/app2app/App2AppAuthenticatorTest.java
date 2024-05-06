package com.wso2.openbanking.accelerator.identity.app2app;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

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
    @Test(dataProviderClass =App2AppAuthenticatorTestDataProvider.class ,
            dataProvider = "UsernameAndPasswordProvider")
    public void canHandleTestCase(String secret, String expected) {

        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getParameter(App2AppAuthenticatorConstants.SECRET)).thenReturn(secret);
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
    public void getAdditionalInfoTest(String sessionDataKey){
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        try {
            Optional<String> output =
                    app2AppAuthenticator.getAdditionalInfo(mockRequest,mockResponse,sessionDataKey);
            assertEquals(output,Optional.empty());
        } catch (AuthenticationFailedException e) {
            throw new RuntimeException(e);
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
}
