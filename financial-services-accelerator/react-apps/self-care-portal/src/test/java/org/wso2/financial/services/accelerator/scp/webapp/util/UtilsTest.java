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

package org.wso2.financial.services.accelerator.scp.webapp.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.util.HTTPClientUtils;
import org.wso2.financial.services.accelerator.scp.webapp.exception.TokenGenerationException;
import org.wso2.financial.services.accelerator.scp.webapp.model.SCPError;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UtilsTest {


    private static final String RESPONSE_STRING = "{\"access_token\":\"dummy-token\"}";
    private static final String REQUEST_URL = "http://localhost:9446";
    private static final String DUMMY_COOKIE = "dummy-cookie";
    private static MockedStatic<HTTPClientUtils> mockedStatic;

    @BeforeClass
    public void setUp() {

        mockedStatic = Mockito.mockStatic(HTTPClientUtils.class);
    }

    @AfterClass
    public void tearDown() {
        // Close the mocked static
        mockedStatic.close();
    }

    @Test(description = "if valid request sent, return json response")
    public void testSendRequestWithValidRequest() throws TokenGenerationException, IOException {


        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);

        Mockito.doReturn(HttpStatus.SC_OK).when(statusLineMock).getStatusCode();

        InputStream inStream = new ByteArrayInputStream(RESPONSE_STRING.getBytes(StandardCharsets.UTF_8));
        Mockito.doReturn(inStream).when(httpEntityMock).getContent();

        Mockito.doReturn(statusLineMock).when(httpResponseMock).getStatusLine();
        Mockito.doReturn(httpEntityMock).when(httpResponseMock).getEntity();

        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpGet.class));
        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpPost.class));

        mockedStatic.when(HTTPClientUtils::getHttpsClient).thenReturn(closeableHttpClientMock);
        // assert
        JSONObject responseJson = Utils.sendRequest(new HttpGet(REQUEST_URL));
        Assert.assertEquals(responseJson.get("access_token"), "dummy-token");
    }

    @Test(description = "if invalid response received, throw TokenGenerationException",
            expectedExceptions = TokenGenerationException.class)
    public void testSendRequestWithInvalidRequest() throws TokenGenerationException, IOException {

        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);

        Mockito.doReturn(HttpStatus.SC_BAD_REQUEST).when(statusLineMock).getStatusCode();

        InputStream inStream = new ByteArrayInputStream(RESPONSE_STRING.getBytes(StandardCharsets.UTF_8));
        Mockito.doReturn(inStream).when(httpEntityMock).getContent();

        Mockito.doReturn(statusLineMock).when(httpResponseMock).getStatusLine();
        Mockito.doReturn(httpEntityMock).when(httpResponseMock).getEntity();

        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpGet.class));
        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpPost.class));

        mockedStatic.when(HTTPClientUtils::getHttpsClient).thenReturn(closeableHttpClientMock);

        // assert
        JSONObject responseJson = Utils.sendRequest(new HttpGet(REQUEST_URL));
        Assert.assertEquals(responseJson.get("access_token"), "dummy-token");
    }

    @Test(description = "if valid request, return json response")
    public void testSendTokenRequestWithValidReq() throws TokenGenerationException, IOException {


        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);

        Mockito.doReturn(HttpStatus.SC_OK).when(statusLineMock).getStatusCode();

        InputStream inStream = new ByteArrayInputStream(RESPONSE_STRING.getBytes(StandardCharsets.UTF_8));
        Mockito.doReturn(inStream).when(httpEntityMock).getContent();

        Mockito.doReturn(statusLineMock).when(httpResponseMock).getStatusLine();
        Mockito.doReturn(httpEntityMock).when(httpResponseMock).getEntity();

        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpGet.class));
        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpPost.class));

        mockedStatic.when(HTTPClientUtils::getHttpsClient).thenReturn(closeableHttpClientMock);
        // assert
        JSONObject responseJson = Utils.sendTokenRequest(new HttpPost(REQUEST_URL));
        Assert.assertEquals(responseJson.get("access_token"), "dummy-token");
    }

    @Test(description = "if invalid request, throw TokenGenerationException",
            expectedExceptions = TokenGenerationException.class)
    public void testSendTokenRequestWithInvalidReq()
            throws TokenGenerationException, IOException {

        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
        CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);

        Mockito.doReturn(HttpStatus.SC_BAD_REQUEST).when(statusLineMock).getStatusCode();

        InputStream inStream = new ByteArrayInputStream(RESPONSE_STRING.getBytes(StandardCharsets.UTF_8));
        Mockito.doReturn(inStream).when(httpEntityMock).getContent();

        Mockito.doReturn(statusLineMock).when(httpResponseMock).getStatusLine();
        Mockito.doReturn(httpEntityMock).when(httpResponseMock).getEntity();

        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpGet.class));
        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpPost.class));

        mockedStatic.when(HTTPClientUtils::getHttpsClient).thenReturn(closeableHttpClientMock);
        // assert
        Utils.sendTokenRequest(new HttpPost(REQUEST_URL));
    }

    @Test
    public void testGetHttpUriRequest() {
        Assert.assertTrue(Utils
                .getHttpUriRequest(REQUEST_URL, "GET", "/consentmgr/scp/admin/search",
                        "?query=query") instanceof HttpGet);
        Assert.assertTrue(Utils
                .getHttpUriRequest(REQUEST_URL, "DELETE", "/consentmgr/scp/admin/revoke",
                        "?query=query") instanceof HttpDelete);
    }

    @Test
    public void testGetCookieFromRequest() {
        // mock
        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);

        // when
        Cookie cookie = new Cookie(DUMMY_COOKIE, DUMMY_COOKIE);
        Mockito.when(requestMock.getCookies()).thenReturn(new Cookie[]{cookie});

        // assert
        Assert.assertTrue(Utils.getCookieFromRequest(requestMock, DUMMY_COOKIE).isPresent());
        Assert.assertFalse(Utils.getCookieFromRequest(requestMock, "invalid cookie name").isPresent());
    }

    @Test
    public void testFormatDateToEncodedString() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String expected = localDateTime.format(DateTimeFormatter.ofPattern(Constants.SCP_TOKEN_VALIDITY_DATE_FORMAT));
        String actual = Utils.formatDateToEncodedString(localDateTime);

        Assert.assertEquals(new String(Base64.getDecoder().decode(actual)), expected);
    }

    @Test
    public void testParseEncodedStringToDate() {
        LocalDateTime expectedDate = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        String encodedDate = Utils.formatDateToEncodedString(expectedDate);

        Assert.assertEquals(Utils.parseEncodedStringToDate(encodedDate), expectedDate);
    }

    @Test
    public void testSendErrorToFrontend() throws IOException {
        // mock
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);

        // when
        Mockito.doNothing().when(resp).sendRedirect(Mockito.anyString());

        // assert
        SCPError error = new SCPError("Error Message!", "Error Description");
        final String errorUrlFormat = "https://localhost:9446/consentmgr/error?message=%s&description=%s";
        Utils.sendErrorToFrontend(error, errorUrlFormat, resp);

        Mockito.verify(resp, Mockito.times(1)).sendRedirect(Mockito.anyString());
    }
}
