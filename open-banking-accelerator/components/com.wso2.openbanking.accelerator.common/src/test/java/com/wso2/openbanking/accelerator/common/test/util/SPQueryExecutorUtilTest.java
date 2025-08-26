/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.common.test.util;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.HTTPClientUtils;
import com.wso2.openbanking.accelerator.common.util.SPQueryExecutorUtil;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Test class for SPQueryExecutorUtil.
 */
@PrepareForTest({HTTPClientUtils.class})
@PowerMockIgnore({"jdk.internal.reflect.*"})
public class SPQueryExecutorUtilTest {
    private static final String spApiHost = "https://localhost:7444";
    private static final String spUsername = "admin@wso2.com@carbon.super";
    private static final String spPassword = "wso2123";
    private static final String appName = "dummyAppName";
    private static final String query = "dummyQuery";
    private static final String records = "records";

    @BeforeClass
    public void initTest() throws ReflectiveOperationException {

        MockitoAnnotations.initMocks(this);

    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test
    public void testSPQueryExecutor() throws OpenBankingException, IOException, ParseException {
        File file = new File("src/test/resources/test-data.json");
        byte[] crlBytes = FileUtils.readFileToString(file, String.valueOf(StandardCharsets.UTF_8))
                .getBytes(StandardCharsets.UTF_8);
        InputStream inStream = new ByteArrayInputStream(crlBytes);

        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        Mockito.doReturn(HttpStatus.SC_OK).when(statusLineMock).getStatusCode();
        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
        Mockito.doReturn(inStream).when(httpEntityMock).getContent();

        CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        Mockito.doReturn(statusLineMock).when(httpResponseMock).getStatusLine();
        Mockito.doReturn(httpEntityMock).when(httpResponseMock).getEntity();
        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);
        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpGet.class));
        PowerMockito.mockStatic(HTTPClientUtils.class);
        Mockito.when(HTTPClientUtils.getHttpsClientInstance()).thenReturn(closeableHttpClientMock);

        JSONObject result = SPQueryExecutorUtil.executeQueryOnStreamProcessor(appName, query, spUsername,
                spPassword, spApiHost);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsKey(records));
    }
}
