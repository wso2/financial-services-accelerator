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

package org.wso2.financial.services.accelerator.identity.extensions.valve;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Unit tests for SplitTokenValve.reconstructToken() — the core token-assembly logic.
 * invoke() wiring (URI gating, MimeHeader mutation) is covered by integration tests in the pack.
 */
public class SplitTokenValveTest {

    private static final String PART1 = "firstHalfOfToken";
    private static final String PART2 = "secondHalfOfToken";
    private static final String FULL_TOKEN = PART1 + PART2;

    private SplitTokenValve valve;
    private HttpServletRequest mockRequest;

    @BeforeMethod
    public void setUp() {
        valve = new SplitTokenValve();
        mockRequest = Mockito.mock(HttpServletRequest.class);
    }

    @Test(description = "no Authorization header returns null")
    public void testNoAuthHeaderReturnsNull() {
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn(null);
        Assert.assertNull(valve.reconstructToken(mockRequest));
    }

    @Test(description = "non-Bearer Authorization header returns null")
    public void testBasicAuthHeaderReturnsNull() {
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");
        Assert.assertNull(valve.reconstructToken(mockRequest));
    }

    @Test(description = "empty Bearer value returns null")
    public void testEmptyBearerValueReturnsNull() {
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer ");
        Assert.assertNull(valve.reconstructToken(mockRequest));
    }

    @Test(description = "null cookies returns null")
    public void testNullCookiesReturnsNull() {
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + PART1);
        Mockito.when(mockRequest.getCookies()).thenReturn(null);
        Assert.assertNull(valve.reconstructToken(mockRequest));
    }

    @Test(description = "empty cookies array returns null")
    public void testEmptyCookiesReturnsNull() {
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + PART1);
        Mockito.when(mockRequest.getCookies()).thenReturn(new Cookie[]{});
        Assert.assertNull(valve.reconstructToken(mockRequest));
    }

    @Test(description = "P1 cookie present but P2 missing returns null")
    public void testMissingP2CookieReturnsNull() {
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + PART1);
        Mockito.when(mockRequest.getCookies())
                .thenReturn(new Cookie[]{new Cookie(SplitTokenValve.PART1_COOKIE_NAME, PART1)});
        Assert.assertNull(valve.reconstructToken(mockRequest));
    }

    @Test(description = "P2 cookie present but P1 missing returns null")
    public void testMissingP1CookieReturnsNull() {
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + PART1);
        Mockito.when(mockRequest.getCookies())
                .thenReturn(new Cookie[]{new Cookie(SplitTokenValve.PART2_COOKIE_NAME, PART2)});
        Assert.assertNull(valve.reconstructToken(mockRequest));
    }

    @Test(description = "CSRF check: header P1 differs from P1 cookie returns null")
    public void testCsrfMismatchReturnsNull() {
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + PART1);
        Mockito.when(mockRequest.getCookies()).thenReturn(new Cookie[]{
                new Cookie(SplitTokenValve.PART1_COOKIE_NAME, "tampered"),
                new Cookie(SplitTokenValve.PART2_COOKIE_NAME, PART2)
        });
        Assert.assertNull(valve.reconstructToken(mockRequest));
    }

    @Test(description = "valid split token returns concatenated P1+P2")
    public void testValidSplitTokenReturnsConcatenation() {
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + PART1);
        Mockito.when(mockRequest.getCookies()).thenReturn(new Cookie[]{
                new Cookie(SplitTokenValve.PART1_COOKIE_NAME, PART1),
                new Cookie(SplitTokenValve.PART2_COOKIE_NAME, PART2)
        });
        Assert.assertEquals(valve.reconstructToken(mockRequest), FULL_TOKEN);
    }

    @Test(description = "extra unrelated cookies do not interfere")
    public void testExtraCookiesIgnored() {
        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + PART1);
        Mockito.when(mockRequest.getCookies()).thenReturn(new Cookie[]{
                new Cookie("JSESSIONID", "abc123"),
                new Cookie(SplitTokenValve.PART1_COOKIE_NAME, PART1),
                new Cookie("OtherCookie", "val"),
                new Cookie(SplitTokenValve.PART2_COOKIE_NAME, PART2)
        });
        Assert.assertEquals(valve.reconstructToken(mockRequest), FULL_TOKEN);
    }
}
