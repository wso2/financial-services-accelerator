/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.scp.webapp.servlet;

import com.wso2.openbanking.scp.webapp.service.APIMService;
import com.wso2.openbanking.scp.webapp.util.Constants;
import com.wso2.openbanking.scp.webapp.util.Utils;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.powermock.reflect.Whitebox;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@PrepareForTest({LogoutServlet.class, Utils.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class LogoutServletTest extends PowerMockTestCase {

    private static final String IAM_BASE_URL = "https://localhost:9446";

    @Test(description = "when id token cookies are present, redirect to logout url with id_token_hint")
    public void testDoGetWithIdToken() throws Exception {
        // mock
        APIMService apimServiceMock = Mockito.mock(APIMService.class);
        PowerMockito.whenNew(APIMService.class).withNoArguments().thenReturn(apimServiceMock);

        HttpServletRequest reqMock = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respMock = Mockito.mock(HttpServletResponse.class);

        // when
        PowerMockito.mockStatic(Utils.class);
        PowerMockito.when(Utils.getParameter(Constants.IS_BASE_URL)).thenReturn(IAM_BASE_URL);
        Mockito.when(apimServiceMock.constructIdTokenFromCookies(reqMock)).thenReturn(Optional.of("dummy-id-token"));
        Mockito.when(reqMock.getCookies()).thenReturn(new Cookie[]{});

        // assert
        LogoutServlet servlet = new LogoutServlet();
        Whitebox.invokeMethod(servlet, "doGet", reqMock, respMock);

        Mockito.verify(respMock, Mockito.times(1)).sendRedirect(Mockito.argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(Object argument) {
                try {
                    URI uri = new URI((String) argument);
                    return uri.getPath().equals(Constants.PATH_LOGOUT)
                            && uri.getQuery().contains("id_token_hint=dummy-id-token");
                } catch (URISyntaxException e) {
                    return false;
                }
            }
        }));
    }

    @Test(description = "when id token cookies are missing, redirect to logout url without id_token_hint")
    public void testDoGetWithoutIdToken() throws Exception {
        // mock
        APIMService apimServiceMock = Mockito.mock(APIMService.class);
        PowerMockito.whenNew(APIMService.class).withNoArguments().thenReturn(apimServiceMock);

        HttpServletRequest reqMock = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respMock = Mockito.mock(HttpServletResponse.class);

        // when
        PowerMockito.mockStatic(Utils.class);
        PowerMockito.when(Utils.getParameter(Constants.IS_BASE_URL)).thenReturn(IAM_BASE_URL);
        Mockito.when(apimServiceMock.constructIdTokenFromCookies(reqMock)).thenReturn(Optional.empty());
        Mockito.when(reqMock.getCookies()).thenReturn(new Cookie[]{});

        // assert
        LogoutServlet servlet = new LogoutServlet();
        Whitebox.invokeMethod(servlet, "doGet", reqMock, respMock);

        Mockito.verify(respMock, Mockito.times(1)).sendRedirect(Mockito.argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(Object argument) {
                return !((String) argument).contains("id_token_hint");
            }
        }));
    }

    @Test(description = "when building the logout url fails, send an error to the frontend and clear cookies")
    public void testDoGetWithInvalidIamBaseUrl() throws Exception {
        // mock
        APIMService apimServiceMock = Mockito.mock(APIMService.class);
        PowerMockito.whenNew(APIMService.class).withNoArguments().thenReturn(apimServiceMock);

        HttpServletRequest reqMock = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respMock = Mockito.mock(HttpServletResponse.class);

        // when: an unparsable iam base url causes generateLogoutUrl to throw URISyntaxException
        PowerMockito.mockStatic(Utils.class);
        PowerMockito.when(Utils.getParameter(Constants.IS_BASE_URL)).thenReturn("not a valid uri");
        Mockito.when(apimServiceMock.constructIdTokenFromCookies(reqMock)).thenReturn(Optional.empty());
        Mockito.when(reqMock.getCookies()).thenReturn(new Cookie[]{});
        Mockito.doNothing().when(respMock).sendRedirect(Mockito.anyString());

        // assert
        LogoutServlet servlet = new LogoutServlet();
        Whitebox.invokeMethod(servlet, "doGet", reqMock, respMock);

        PowerMockito.verifyStatic(Utils.class, Mockito.times(1));
        Utils.sendErrorToFrontend(Mockito.any(), Mockito.anyString(), Mockito.eq(respMock));
        Mockito.verify(respMock, Mockito.never()).sendRedirect(Mockito.anyString());
    }

    @Test(description = "regardless of outcome, all self-care-portal cookies are invalidated")
    public void testDoGetInvalidatesCookies() throws Exception {
        // mock
        APIMService apimServiceMock = Mockito.mock(APIMService.class);
        PowerMockito.whenNew(APIMService.class).withNoArguments().thenReturn(apimServiceMock);

        HttpServletRequest reqMock = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respMock = Mockito.mock(HttpServletResponse.class);
        Cookie cookie = new Cookie(Constants.COOKIE_BASE_NAME + "1", "dummy-cookie");

        // when
        PowerMockito.mockStatic(Utils.class);
        PowerMockito.when(Utils.getParameter(Constants.IS_BASE_URL)).thenReturn(IAM_BASE_URL);
        Mockito.when(apimServiceMock.constructIdTokenFromCookies(reqMock)).thenReturn(Optional.empty());
        Mockito.when(reqMock.getCookies()).thenReturn(new Cookie[]{cookie});

        // assert
        LogoutServlet servlet = new LogoutServlet();
        Whitebox.invokeMethod(servlet, "doGet", reqMock, respMock);

        Mockito.verify(respMock, Mockito.times(1)).addCookie(Mockito.any(Cookie.class));
    }
}
