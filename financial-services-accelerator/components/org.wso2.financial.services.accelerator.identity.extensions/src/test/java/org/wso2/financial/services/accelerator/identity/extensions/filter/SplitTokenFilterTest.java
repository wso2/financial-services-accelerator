/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.identity.extensions.filter;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Unit tests for SplitTokenFilter.
 */
public class SplitTokenFilterTest {

    private static final String P1_COOKIE = "OB_SCP_AT_P1";
    private static final String P2_COOKIE = "OB_SCP_AT_P2";
    private static final String PART1 = "firstHalfOfToken";
    private static final String PART2 = "secondHalfOfToken";
    private static final String FULL_TOKEN = PART1 + PART2;

    private SplitTokenFilter filter;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private FilterChain mockChain;

    @BeforeMethod
    public void setUp() throws ServletException {
        filter = new SplitTokenFilter();
        FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(filterConfig.getInitParameter(SplitTokenFilter.INIT_PARAM_PART1_COOKIE))
                .thenReturn(P1_COOKIE);
        Mockito.when(filterConfig.getInitParameter(SplitTokenFilter.INIT_PARAM_PART2_COOKIE))
                .thenReturn(P2_COOKIE);
        filter.init(filterConfig);

        mockRequest = Mockito.mock(HttpServletRequest.class);
        mockResponse = Mockito.mock(HttpServletResponse.class);
        mockChain = Mockito.mock(FilterChain.class);
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/consentmgr/scp/consents");
    }

    @Test(description = "init with missing init-param should throw ServletException",
            expectedExceptions = ServletException.class)
    public void testInitMissingParams() throws ServletException {
        SplitTokenFilter f = new SplitTokenFilter();
        FilterConfig cfg = Mockito.mock(FilterConfig.class);
        Mockito.when(cfg.getInitParameter(SplitTokenFilter.INIT_PARAM_PART1_COOKIE)).thenReturn(null);
        Mockito.when(cfg.getInitParameter(SplitTokenFilter.INIT_PARAM_PART2_COOKIE)).thenReturn(P2_COOKIE);
        f.init(cfg);
    }

    @Test(description = "no Authorization header - pass through unchanged")
    public void testNoAuthHeaderPassesThrough() throws IOException, ServletException {
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn(null);
        filter.doFilter(mockRequest, mockResponse, mockChain);
        Mockito.verify(mockChain).doFilter(mockRequest, mockResponse);
    }

    @Test(description = "non-Bearer Authorization header - pass through unchanged")
    public void testNonBearerAuthHeaderPassesThrough() throws IOException, ServletException {
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");
        filter.doFilter(mockRequest, mockResponse, mockChain);
        Mockito.verify(mockChain).doFilter(mockRequest, mockResponse);
    }

    @Test(description = "no cookies - pass through unchanged")
    public void testNoCookiesPassesThrough() throws IOException, ServletException {
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + PART1);
        Mockito.when(mockRequest.getCookies()).thenReturn(null);
        filter.doFilter(mockRequest, mockResponse, mockChain);
        Mockito.verify(mockChain).doFilter(mockRequest, mockResponse);
    }

    @Test(description = "P2 cookie missing - pass through unchanged")
    public void testMissingP2CookiePassesThrough() throws IOException, ServletException {
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + PART1);
        Mockito.when(mockRequest.getCookies()).thenReturn(new Cookie[]{new Cookie(P1_COOKIE, PART1)});
        filter.doFilter(mockRequest, mockResponse, mockChain);
        Mockito.verify(mockChain).doFilter(mockRequest, mockResponse);
    }

    @Test(description = "header P1 does not match cookie P1 (CSRF guard) - pass through unchanged")
    public void testMismatchedP1PassesThrough() throws IOException, ServletException {
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + PART1);
        Mockito.when(mockRequest.getCookies()).thenReturn(new Cookie[]{
                new Cookie(P1_COOKIE, "differentValue"),
                new Cookie(P2_COOKIE, PART2)
        });
        filter.doFilter(mockRequest, mockResponse, mockChain);
        Mockito.verify(mockChain).doFilter(mockRequest, mockResponse);
    }

    @Test(description = "valid split token - Authorization header replaced with full token")
    public void testValidSplitTokenReconstructed() throws IOException, ServletException {
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + PART1);
        Mockito.when(mockRequest.getCookies()).thenReturn(new Cookie[]{
                new Cookie(P1_COOKIE, PART1),
                new Cookie(P2_COOKIE, PART2)
        });
        Mockito.when(mockRequest.getHeaderNames()).thenReturn(
                Collections.enumeration(Collections.singletonList("Authorization")));

        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
        filter.doFilter(mockRequest, mockResponse, mockChain);

        Mockito.verify(mockChain).doFilter(captor.capture(), Mockito.eq(mockResponse));
        Assert.assertEquals(captor.getValue().getHeader("Authorization"), "Bearer " + FULL_TOKEN);
    }

    @Test(description = "TokenReplacedRequest overrides Authorization header case-insensitively")
    public void testWrappedRequestCaseInsensitiveHeader() throws IOException, ServletException {
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + PART1);
        Mockito.when(mockRequest.getCookies()).thenReturn(new Cookie[]{
                new Cookie(P1_COOKIE, PART1),
                new Cookie(P2_COOKIE, PART2)
        });
        Mockito.when(mockRequest.getHeaderNames()).thenReturn(
                Collections.enumeration(Collections.singletonList("Authorization")));

        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
        filter.doFilter(mockRequest, mockResponse, mockChain);

        Mockito.verify(mockChain).doFilter(captor.capture(), Mockito.eq(mockResponse));
        HttpServletRequest wrapped = captor.getValue();

        Assert.assertEquals(wrapped.getHeader("authorization"), "Bearer " + FULL_TOKEN);
        Assert.assertEquals(wrapped.getHeader("AUTHORIZATION"), "Bearer " + FULL_TOKEN);
        Assert.assertEquals(wrapped.getHeaders("Authorization").nextElement(), "Bearer " + FULL_TOKEN);
        Assert.assertTrue(Collections.list(wrapped.getHeaderNames()).stream()
                .anyMatch("Authorization"::equalsIgnoreCase));
    }

    @Test(description = "destroy is a no-op")
    public void testDestroy() {
        filter.destroy();
    }
}
