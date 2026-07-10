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

package org.wso2.financial.services.accelerator.scp.webapp.servlet;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.util.JWTUtils;
import org.wso2.financial.services.accelerator.scp.webapp.util.Utils;

import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Unit tests for the permission-checking helpers in ResourceInterceptorServlet.
 */
public class ResourceInterceptorServletTest {

    private static final String CCO_TOKEN_BODY =
            "{\"sub\":\"cco@carbon.super\",\"scope\":\"consents:read_all openid\"}";
    private static final String USER_TOKEN_BODY =
            "{\"sub\":\"alice@carbon.super\",\"scope\":\"openid accounts\"}";
    private static final String DUMMY_ACCESS_TOKEN = "header.body.signature";

    private ResourceInterceptorServlet servlet;
    private HttpServletRequest mockReq;
    private HttpServletResponse mockResp;
    private MockedStatic<JWTUtils> jwtUtilsMock;
    private MockedStatic<Utils> utilsMock;

    @BeforeMethod
    public void setUp() {
        servlet = new ResourceInterceptorServlet();
        mockReq = Mockito.mock(HttpServletRequest.class);
        mockResp = Mockito.mock(HttpServletResponse.class);
        jwtUtilsMock = Mockito.mockStatic(JWTUtils.class);
        utilsMock = Mockito.mockStatic(Utils.class);
    }

    @AfterMethod
    public void tearDown() {
        jwtUtilsMock.close();
        utilsMock.close();
    }

    // --- validateUserPermissions ---

    @Test(description = "CCO user with consents:read_all scope should always be permitted")
    public void testValidateUserPermissions_CCO_alwaysPermitted() throws ParseException {
        jwtUtilsMock.when(() -> JWTUtils.decodeRequestJWT(DUMMY_ACCESS_TOKEN, "body"))
                .thenReturn(CCO_TOKEN_BODY);

        boolean result = servlet.validateUserPermissions(mockReq, DUMMY_ACCESS_TOKEN, mockResp);

        Assert.assertTrue(result);
    }

    @Test(description = "Non-CCO on /search with matching userId should be permitted")
    public void testValidateUserPermissions_nonCCO_search_matchingUserId() throws ParseException {
        jwtUtilsMock.when(() -> JWTUtils.decodeRequestJWT(DUMMY_ACCESS_TOKEN, "body"))
                .thenReturn(USER_TOKEN_BODY);
        Mockito.when(mockReq.getPathInfo()).thenReturn("/search");
        Mockito.when(mockReq.getParameterValues("userIds")).thenReturn(new String[]{"alice@carbon.super"});

        boolean result = servlet.validateUserPermissions(mockReq, DUMMY_ACCESS_TOKEN, mockResp);

        Assert.assertTrue(result);
    }

    @Test(description = "Non-CCO on /search with userId without tenant domain should still match after normalization")
    public void testValidateUserPermissions_nonCCO_search_userIdWithoutTenant() throws ParseException {
        jwtUtilsMock.when(() -> JWTUtils.decodeRequestJWT(DUMMY_ACCESS_TOKEN, "body"))
                .thenReturn(USER_TOKEN_BODY);
        Mockito.when(mockReq.getPathInfo()).thenReturn("/search");
        Mockito.when(mockReq.getParameterValues("userIds")).thenReturn(new String[]{"alice"});

        boolean result = servlet.validateUserPermissions(mockReq, DUMMY_ACCESS_TOKEN, mockResp);

        Assert.assertTrue(result);
    }

    @Test(description = "Non-CCO on /revoke with matching userId should be permitted")
    public void testValidateUserPermissions_nonCCO_revoke_matchingUserId() throws ParseException {
        jwtUtilsMock.when(() -> JWTUtils.decodeRequestJWT(DUMMY_ACCESS_TOKEN, "body"))
                .thenReturn(USER_TOKEN_BODY);
        Mockito.when(mockReq.getPathInfo()).thenReturn("/revoke");
        Mockito.when(mockReq.getParameter("userId")).thenReturn("alice@carbon.super");

        boolean result = servlet.validateUserPermissions(mockReq, DUMMY_ACCESS_TOKEN, mockResp);

        Assert.assertTrue(result);
    }

    @Test(description = "Non-CCO on /search with mismatched userId should be denied")
    public void testValidateUserPermissions_nonCCO_search_mismatchedUserId() throws ParseException {
        jwtUtilsMock.when(() -> JWTUtils.decodeRequestJWT(DUMMY_ACCESS_TOKEN, "body"))
                .thenReturn(USER_TOKEN_BODY);
        Mockito.when(mockReq.getPathInfo()).thenReturn("/search");
        Mockito.when(mockReq.getParameterValues("userIds")).thenReturn(new String[]{"bob@carbon.super"});

        boolean result = servlet.validateUserPermissions(mockReq, DUMMY_ACCESS_TOKEN, mockResp);

        Assert.assertFalse(result);
    }

    @Test(description = "Non-CCO on /search with no userIds param should be denied")
    public void testValidateUserPermissions_nonCCO_search_noUserId() throws ParseException {
        jwtUtilsMock.when(() -> JWTUtils.decodeRequestJWT(DUMMY_ACCESS_TOKEN, "body"))
                .thenReturn(USER_TOKEN_BODY);
        Mockito.when(mockReq.getPathInfo()).thenReturn("/search");
        Mockito.when(mockReq.getParameterValues("userIds")).thenReturn(null);

        boolean result = servlet.validateUserPermissions(mockReq, DUMMY_ACCESS_TOKEN, mockResp);

        Assert.assertFalse(result);
    }

    @Test(description = "Non-CCO on a restricted path should be denied with 403")
    public void testValidateUserPermissions_nonCCO_restrictedPath_denied() throws ParseException {
        jwtUtilsMock.when(() -> JWTUtils.decodeRequestJWT(DUMMY_ACCESS_TOKEN, "body"))
                .thenReturn(USER_TOKEN_BODY);
        Mockito.when(mockReq.getPathInfo()).thenReturn("/status");

        boolean result = servlet.validateUserPermissions(mockReq, DUMMY_ACCESS_TOKEN, mockResp);

        Assert.assertFalse(result);
    }

    @Test(description = "ParseException from JWT decode should return false")
    public void testValidateUserPermissions_parseException_returnsFalse() throws ParseException {
        jwtUtilsMock.when(() -> JWTUtils.decodeRequestJWT(DUMMY_ACCESS_TOKEN, "body"))
                .thenThrow(new ParseException("bad token", 0));

        boolean result = servlet.validateUserPermissions(mockReq, DUMMY_ACCESS_TOKEN, mockResp);

        Assert.assertFalse(result);
    }

    // --- isCustomerCareOfficer ---

    @Test
    public void testIsCustomerCareOfficer_withCCOScope_returnsTrue() {
        Assert.assertTrue(servlet.isCustomerCareOfficer("openid consents:read_all accounts"));
    }

    @Test
    public void testIsCustomerCareOfficer_withoutCCOScope_returnsFalse() {
        Assert.assertFalse(servlet.isCustomerCareOfficer("openid accounts"));
    }

    @Test
    public void testIsCustomerCareOfficer_emptyScopes_returnsFalse() {
        Assert.assertFalse(servlet.isCustomerCareOfficer(""));
    }

    // --- getUserNameWithTenantDomain ---

    @Test
    public void testGetUserNameWithTenantDomain_alreadyHasDomain() {
        Assert.assertEquals(servlet.getUserNameWithTenantDomain("alice@carbon.super"), "alice@carbon.super");
    }

    @Test
    public void testGetUserNameWithTenantDomain_appendsDomain() {
        Assert.assertEquals(servlet.getUserNameWithTenantDomain("alice"), "alice@carbon.super");
    }

    // --- getAdminBasicAuth ---

    @Test
    public void testGetAdminBasicAuth_returnsBase64EncodedCredentials() {
        try (MockedStatic<FinancialServicesConfigParser> configMock =
                     Mockito.mockStatic(FinancialServicesConfigParser.class)) {
            FinancialServicesConfigParser mockParser = Mockito.mock(FinancialServicesConfigParser.class);
            configMock.when(FinancialServicesConfigParser::getInstance).thenReturn(mockParser);
            Mockito.when(mockParser.getAdminUsername()).thenReturn("admin");
            Mockito.when(mockParser.getAdminPassword()).thenReturn("admin");

            String result = servlet.getAdminBasicAuth();

            Assert.assertEquals(result, java.util.Base64.getEncoder()
                    .encodeToString("admin:admin".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        }
    }
}
