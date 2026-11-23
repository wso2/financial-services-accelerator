/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.scp.webapp.servlet;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.scp.webapp.util.Constants;
import org.wso2.financial.services.accelerator.scp.webapp.util.Utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Unit tests for OAuthLogoutServlet.
 */
public class OAuthLogoutServletTest {

    private static final String IAM_BASE_URL = "https://localhost:9446";

    private OAuthLogoutServlet servlet;
    private HttpServletRequest mockReq;
    private HttpServletResponse mockResp;
    private MockedStatic<Utils> utilsMock;

    @BeforeMethod
    public void setUp() {
        servlet = new OAuthLogoutServlet();
        mockReq = Mockito.mock(HttpServletRequest.class);
        mockResp = Mockito.mock(HttpServletResponse.class);
        utilsMock = Mockito.mockStatic(Utils.class, Mockito.CALLS_REAL_METHODS);
        utilsMock.when(() -> Utils.getParameter(Constants.IS_BASE_URL)).thenReturn(IAM_BASE_URL);
    }

    @AfterMethod
    public void tearDown() {
        utilsMock.close();
    }

    @Test(description = "when id token cookies are present, then redirect with id_token_hint")
    public void testDoGet_withIdTokenCookies_redirectsWithIdTokenHint() throws IOException, URISyntaxException {
        Mockito.when(mockReq.getCookies()).thenReturn(new Cookie[]{
                new Cookie(Constants.ID_TOKEN_COOKIE_NAME + "_P1", "part1"),
                new Cookie(Constants.ID_TOKEN_COOKIE_NAME + "_P2", "part2")
        });

        servlet.doGet(mockReq, mockResp);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mockResp).sendRedirect(captor.capture());
        URI logoutUri = new URI(captor.getValue());
        Assert.assertEquals(logoutUri.getPath(), Constants.PATH_LOGOUT);
        Assert.assertTrue(logoutUri.getQuery().contains("id_token_hint=part1part2"));
        Assert.assertTrue(logoutUri.getQuery().contains("post_logout_redirect_uri"));
    }

    @Test(description = "when id token cookies are absent, then redirect without id_token_hint")
    public void testDoGet_withoutIdTokenCookies_redirectsWithoutIdTokenHint() throws IOException, URISyntaxException {
        Mockito.when(mockReq.getCookies()).thenReturn(new Cookie[]{});

        servlet.doGet(mockReq, mockResp);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mockResp).sendRedirect(captor.capture());
        URI logoutUri = new URI(captor.getValue());
        Assert.assertFalse(logoutUri.getQuery().contains("id_token_hint"));
        Assert.assertTrue(logoutUri.getQuery().contains("post_logout_redirect_uri"));
    }

    @Test(description = "when redirect fails, then the exception is handled without propagating")
    public void testDoGet_sendRedirectThrowsIOException_isHandledGracefully() throws IOException {
        Mockito.when(mockReq.getCookies()).thenReturn(new Cookie[]{});
        Mockito.doThrow(new IOException("boom")).when(mockResp).sendRedirect(Mockito.any());

        servlet.doGet(mockReq, mockResp);
        // no exception should propagate out of doGet
    }
}
