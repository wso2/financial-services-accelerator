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
 * Unit tests for SelfCarePortalTokenFilter.
 */
public class SelfCarePortalTokenFilterTest {

    private static final String SCP_PATH = "/consentmgr/scp/consents";
    private static final String NON_SCP_PATH = "/consentmgr/login";
    private static final String PART1 = "firstHalfOfToken";
    private static final String PART2 = "secondHalfOfToken";
    private static final String FULL_TOKEN = PART1 + PART2;

    private SelfCarePortalTokenFilter filter;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private FilterChain mockChain;

    @BeforeMethod
    public void setUp() throws ServletException {
        filter = new SelfCarePortalTokenFilter();
        filter.init(Mockito.mock(FilterConfig.class));
        mockRequest = Mockito.mock(HttpServletRequest.class);
        mockResponse = Mockito.mock(HttpServletResponse.class);
        mockChain = Mockito.mock(FilterChain.class);
    }

    @Test
    public void testNonScpPathPassesThrough() throws IOException, ServletException {
        Mockito.when(mockRequest.getRequestURI()).thenReturn(NON_SCP_PATH);

        filter.doFilter(mockRequest, mockResponse, mockChain);

        Mockito.verify(mockChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void testScpPathWithNoAuthHeaderPassesThrough() throws IOException, ServletException {
        Mockito.when(mockRequest.getRequestURI()).thenReturn(SCP_PATH);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(mockRequest, mockResponse, mockChain);

        Mockito.verify(mockChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void testScpPathWithNonBearerAuthHeaderPassesThrough() throws IOException, ServletException {
        Mockito.when(mockRequest.getRequestURI()).thenReturn(SCP_PATH);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilter(mockRequest, mockResponse, mockChain);

        Mockito.verify(mockChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void testScpPathWithNoCookiesPassesThrough() throws IOException, ServletException {
        Mockito.when(mockRequest.getRequestURI()).thenReturn(SCP_PATH);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + PART1);
        Mockito.when(mockRequest.getCookies()).thenReturn(null);

        filter.doFilter(mockRequest, mockResponse, mockChain);

        Mockito.verify(mockChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void testScpPathMissingP2CookiePassesThrough() throws IOException, ServletException {
        Mockito.when(mockRequest.getRequestURI()).thenReturn(SCP_PATH);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + PART1);
        Mockito.when(mockRequest.getCookies()).thenReturn(new Cookie[]{
                new Cookie("OB_SCP_AT_P1", PART1)
        });

        filter.doFilter(mockRequest, mockResponse, mockChain);

        Mockito.verify(mockChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void testScpPathMismatchedP1CookiePassesThrough() throws IOException, ServletException {
        Mockito.when(mockRequest.getRequestURI()).thenReturn(SCP_PATH);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + PART1);
        Mockito.when(mockRequest.getCookies()).thenReturn(new Cookie[]{
                new Cookie("OB_SCP_AT_P1", "differentValue"),
                new Cookie("OB_SCP_AT_P2", PART2)
        });

        filter.doFilter(mockRequest, mockResponse, mockChain);

        Mockito.verify(mockChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void testScpPathValidTokenReconstructed() throws IOException, ServletException {
        Mockito.when(mockRequest.getRequestURI()).thenReturn(SCP_PATH);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + PART1);
        Mockito.when(mockRequest.getCookies()).thenReturn(new Cookie[]{
                new Cookie("OB_SCP_AT_P1", PART1),
                new Cookie("OB_SCP_AT_P2", PART2)
        });
        Mockito.when(mockRequest.getHeaderNames()).thenReturn(
                Collections.enumeration(Collections.singletonList("Authorization")));

        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
        filter.doFilter(mockRequest, mockResponse, mockChain);

        Mockito.verify(mockChain).doFilter(captor.capture(), Mockito.eq(mockResponse));
        HttpServletRequest wrapped = captor.getValue();
        Assert.assertEquals(wrapped.getHeader("Authorization"), "Bearer " + FULL_TOKEN);
    }

    @Test
    public void testWrappedRequestGetHeadersCaseInsensitive() throws IOException, ServletException {
        Mockito.when(mockRequest.getRequestURI()).thenReturn(SCP_PATH);
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + PART1);
        Mockito.when(mockRequest.getCookies()).thenReturn(new Cookie[]{
                new Cookie("OB_SCP_AT_P1", PART1),
                new Cookie("OB_SCP_AT_P2", PART2)
        });
        Mockito.when(mockRequest.getHeaderNames()).thenReturn(
                Collections.enumeration(Collections.singletonList("Authorization")));

        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
        filter.doFilter(mockRequest, mockResponse, mockChain);

        Mockito.verify(mockChain).doFilter(captor.capture(), Mockito.eq(mockResponse));
        HttpServletRequest wrapped = captor.getValue();

        // Case-insensitive header lookup
        Assert.assertEquals(wrapped.getHeader("authorization"), "Bearer " + FULL_TOKEN);
        Assert.assertEquals(wrapped.getHeader("AUTHORIZATION"), "Bearer " + FULL_TOKEN);

        // getHeaders() returns the full token
        Assert.assertEquals(wrapped.getHeaders("Authorization").nextElement(), "Bearer " + FULL_TOKEN);

        // getHeaderNames() includes Authorization
        Assert.assertTrue(Collections.list(wrapped.getHeaderNames()).stream()
                .anyMatch("Authorization"::equalsIgnoreCase));
    }

    @Test
    public void testFilterDestroy() {
        filter.destroy();
    }
}
