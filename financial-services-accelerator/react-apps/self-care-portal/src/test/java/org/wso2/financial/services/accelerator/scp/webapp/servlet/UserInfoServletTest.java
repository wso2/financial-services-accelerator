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

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.util.JWTUtils;
import org.wso2.financial.services.accelerator.scp.webapp.util.Constants;
import org.wso2.financial.services.accelerator.scp.webapp.util.Utils;

import java.text.ParseException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Unit tests for UserInfoServlet.
 */
public class UserInfoServletTest {

    private static final String ACCESS_TOKEN_HEADER = "Bearer dummy-access-token";
    private static final String ID_TOKEN_BODY =
            "{\"sub\":\"alice@carbon.super\",\"user_role\":\"customerCareOfficer\"}";

    private UserInfoServlet servlet;
    private HttpServletRequest mockReq;
    private HttpServletResponse mockResp;
    private MockedStatic<JWTUtils> jwtUtilsMock;
    private MockedStatic<Utils> utilsMock;

    @BeforeMethod
    public void setUp() {
        servlet = new UserInfoServlet();
        mockReq = Mockito.mock(HttpServletRequest.class);
        mockResp = Mockito.mock(HttpServletResponse.class);
        jwtUtilsMock = Mockito.mockStatic(JWTUtils.class);
        utilsMock = Mockito.mockStatic(Utils.class, Mockito.CALLS_REAL_METHODS);
        utilsMock.when(() -> Utils.returnResponse(Mockito.any(), Mockito.anyInt(), Mockito.any()))
                .thenAnswer(invocation -> null);
    }

    @AfterMethod
    public void tearDown() {
        jwtUtilsMock.close();
        utilsMock.close();
    }

    private void mockIdTokenCookies(String part1, String part2) {
        Mockito.when(mockReq.getCookies()).thenReturn(new Cookie[]{
                new Cookie(Constants.ID_TOKEN_COOKIE_NAME + "_P1", part1),
                new Cookie(Constants.ID_TOKEN_COOKIE_NAME + "_P2", part2)
        });
    }

    @Test(description = "when Authorization header absent, then respond 401 without decoding any token")
    public void testDoGet_noAuthorizationHeader_returnsUnauthorized() {
        Mockito.when(mockReq.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        servlet.doGet(mockReq, mockResp);

        utilsMock.verify(() -> Utils.returnResponse(Mockito.eq(mockResp),
                Mockito.eq(HttpStatus.SC_UNAUTHORIZED), Mockito.any(JSONObject.class)));
        jwtUtilsMock.verifyNoInteractions();
    }

    @Test(description = "when id token cookies are missing, then respond 401")
    public void testDoGet_missingIdTokenCookies_returnsUnauthorized() {
        Mockito.when(mockReq.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(ACCESS_TOKEN_HEADER);
        Mockito.when(mockReq.getCookies()).thenReturn(new Cookie[]{});

        servlet.doGet(mockReq, mockResp);

        utilsMock.verify(() -> Utils.returnResponse(Mockito.eq(mockResp),
                Mockito.eq(HttpStatus.SC_UNAUTHORIZED), Mockito.any(JSONObject.class)));
        jwtUtilsMock.verifyNoInteractions();
    }

    @Test(description = "when id token cookies are present and valid, then respond 200 with email/role")
    public void testDoGet_validIdToken_returnsEmailAndRole() throws ParseException {
        Mockito.when(mockReq.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(ACCESS_TOKEN_HEADER);
        mockIdTokenCookies("part1", "part2");
        jwtUtilsMock.when(() -> JWTUtils.decodeRequestJWT("part1part2", "body")).thenReturn(ID_TOKEN_BODY);

        servlet.doGet(mockReq, mockResp);

        ArgumentCaptor<JSONObject> captor = ArgumentCaptor.forClass(JSONObject.class);
        utilsMock.verify(() -> Utils.returnResponse(Mockito.eq(mockResp), Mockito.eq(HttpStatus.SC_OK),
                captor.capture()));
        JSONObject body = captor.getValue();
        Assert.assertEquals(body.getString("email"), "alice@carbon.super");
        Assert.assertEquals(body.getString("role"), "customerCareOfficer");
    }

    @Test(description = "when id token is missing claims, then respond 200 with empty strings")
    public void testDoGet_idTokenMissingClaims_returnsEmptyStrings() throws ParseException {
        Mockito.when(mockReq.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(ACCESS_TOKEN_HEADER);
        mockIdTokenCookies("part1", "part2");
        jwtUtilsMock.when(() -> JWTUtils.decodeRequestJWT("part1part2", "body")).thenReturn("{}");

        servlet.doGet(mockReq, mockResp);

        ArgumentCaptor<JSONObject> captor = ArgumentCaptor.forClass(JSONObject.class);
        utilsMock.verify(() -> Utils.returnResponse(Mockito.eq(mockResp), Mockito.eq(HttpStatus.SC_OK),
                captor.capture()));
        JSONObject body = captor.getValue();
        Assert.assertEquals(body.getString("email"), "");
        Assert.assertEquals(body.getString("role"), "");
    }

    @Test(description = "when id token cannot be parsed, then respond 401")
    public void testDoGet_parseException_returnsUnauthorized() throws ParseException {
        Mockito.when(mockReq.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(ACCESS_TOKEN_HEADER);
        mockIdTokenCookies("part1", "part2");
        jwtUtilsMock.when(() -> JWTUtils.decodeRequestJWT("part1part2", "body"))
                .thenThrow(new ParseException("bad token", 0));

        servlet.doGet(mockReq, mockResp);

        utilsMock.verify(() -> Utils.returnResponse(Mockito.eq(mockResp),
                Mockito.eq(HttpStatus.SC_UNAUTHORIZED), Mockito.any(JSONObject.class)));
    }
}
