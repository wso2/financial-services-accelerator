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
import com.wso2.openbanking.scp.webapp.util.Utils;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.powermock.reflect.Whitebox;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@PrepareForTest({UserInfoServlet.class, Utils.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class UserInfoServletTest extends PowerMockTestCase {

    @Test(description = "when id token cookies are missing, return an unauthenticated response")
    public void testDoGetWithoutIdToken() throws Exception {
        // mock
        APIMService apimServiceMock = Mockito.mock(APIMService.class);
        PowerMockito.whenNew(APIMService.class).withNoArguments().thenReturn(apimServiceMock);

        HttpServletRequest reqMock = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respMock = Mockito.mock(HttpServletResponse.class);

        // when
        PowerMockito.mockStatic(Utils.class);
        Mockito.when(apimServiceMock.constructIdTokenFromCookies(reqMock)).thenReturn(Optional.empty());

        // assert
        UserInfoServlet servlet = new UserInfoServlet();
        Whitebox.invokeMethod(servlet, "doGet", reqMock, respMock);

        ArgumentCaptor<JSONObject> payloadCaptor = ArgumentCaptor.forClass(JSONObject.class);
        PowerMockito.verifyStatic(Utils.class, Mockito.times(1));
        Utils.returnResponse(Mockito.eq(respMock), Mockito.eq(HttpStatus.SC_UNAUTHORIZED), payloadCaptor.capture());
        Assert.assertFalse(payloadCaptor.getValue().has("email"));
    }

    @Test(description = "when a valid id token is present, return the user's email and role")
    public void testDoGetWithValidIdToken() throws Exception {
        // mock
        APIMService apimServiceMock = Mockito.mock(APIMService.class);
        PowerMockito.whenNew(APIMService.class).withNoArguments().thenReturn(apimServiceMock);

        HttpServletRequest reqMock = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respMock = Mockito.mock(HttpServletResponse.class);

        // when
        PowerMockito.mockStatic(Utils.class);
        String idToken = buildIdToken("jdoe@wso2.com", "admin");
        Mockito.when(apimServiceMock.constructIdTokenFromCookies(reqMock)).thenReturn(Optional.of(idToken));

        // assert
        UserInfoServlet servlet = new UserInfoServlet();
        Whitebox.invokeMethod(servlet, "doGet", reqMock, respMock);

        ArgumentCaptor<JSONObject> payloadCaptor = ArgumentCaptor.forClass(JSONObject.class);
        PowerMockito.verifyStatic(Utils.class, Mockito.times(1));
        Utils.returnResponse(Mockito.eq(respMock), Mockito.eq(HttpStatus.SC_OK), payloadCaptor.capture());

        JSONObject userInfo = payloadCaptor.getValue();
        Assert.assertTrue(userInfo.getBoolean("isLogged"));
        Assert.assertEquals(userInfo.getString("email"), "jdoe@wso2.com");
        Assert.assertEquals(userInfo.getString("role"), "admin");
    }

    @Test(description = "when the id token cannot be parsed, clear cookies and return an unauthenticated response")
    public void testDoGetWithMalformedIdToken() throws Exception {
        // mock
        APIMService apimServiceMock = Mockito.mock(APIMService.class);
        PowerMockito.whenNew(APIMService.class).withNoArguments().thenReturn(apimServiceMock);

        HttpServletRequest reqMock = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respMock = Mockito.mock(HttpServletResponse.class);
        Cookie cookie = new Cookie("OB_SCP_IT_P1", "dummy-cookie");

        // when
        PowerMockito.mockStatic(Utils.class);
        Mockito.when(apimServiceMock.constructIdTokenFromCookies(reqMock)).thenReturn(Optional.of("not-a-jwt"));
        Mockito.when(reqMock.getCookies()).thenReturn(new Cookie[]{cookie});

        // assert
        UserInfoServlet servlet = new UserInfoServlet();
        Whitebox.invokeMethod(servlet, "doGet", reqMock, respMock);

        Mockito.verify(respMock, Mockito.times(1)).addCookie(Mockito.any(Cookie.class));
        PowerMockito.verifyStatic(Utils.class, Mockito.times(1));
        Utils.returnResponse(Mockito.eq(respMock), Mockito.eq(HttpStatus.SC_UNAUTHORIZED),
                Mockito.any(JSONObject.class));
    }

    /**
     * Builds a JWS compact serialization with the given claims. The signature segment is a placeholder since
     * {@code JWTUtils.decodeRequestJWT} only decodes the header/payload segments and never verifies the signature.
     */
    private String buildIdToken(String subject, String role) {
        String header = encodeSegment("{\"alg\":\"HS256\"}");
        String payload = encodeSegment(String.format("{\"sub\":\"%s\",\"user_role\":\"%s\"}", subject, role));
        return header + "." + payload + ".signature";
    }

    private String encodeSegment(String json) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
}
