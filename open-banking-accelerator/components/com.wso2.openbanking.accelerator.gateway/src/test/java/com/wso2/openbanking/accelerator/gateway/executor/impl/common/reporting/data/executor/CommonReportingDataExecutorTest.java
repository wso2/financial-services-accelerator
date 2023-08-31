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

package com.wso2.openbanking.accelerator.gateway.executor.impl.common.reporting.data.executor;

import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * Test for common reporting data executor.
 */
public class CommonReportingDataExecutorTest {

    @Test(priority = 1)
    public void testPreRequestFlow() {

        String userAgent = "testUserAgent";
        String electedResource = "/test";
        String messageId = "test_message_id";
        String apiId = "test_apiId";
        String version = "1.0.0";
        String apiName = "testAPI";
        String httpMethod = "GET";
        String sampleToken = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0cHAiLCJhdXQiOiJBUFBMSUNBVElPTiIsImNvbnNlbnRfaWQiOiJ4eX" +
                "pfY29uc25ldF9pZCJ9.wTIt8KUMRjpWD5WML6GYM1BlBOHzTdroVjuDaeFyb7gmVoCPF9zUjzIS1FDbHCh-xf8n5RvlruTwSDgu" +
                "g3BUMpvy3fJT5C2dQQiXbkH-aofSrWCBjvHSN1D5GJGnd0TmD9cN2nEfFEbhLao8IeJS8Gj-NvMVP7bloZ_USyiD273gLnmWl53" +
                "e2pSYYp7N_b97Ci-nH4WnooHq4HS5f94G85CIJQm6vIUjm0wnzQyVg9Uh_BipUrtV1PMz1h5ugOrv003kBmV10oszj4BSZhscuW" +
                "4xSe07jgIN7xPvsx02hzSVHjTXA4hWhP7YeTzSCvpVcDPREtjIT800cf35akhv0w";

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHttpMethod(httpMethod);

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", sampleToken);
        headers.put("User-Agent", userAgent);
        msgInfoDTO.setHeaders(headers);
        msgInfoDTO.setElectedResource(electedResource);
        msgInfoDTO.setMessageId(messageId);

        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setApiId(apiId);
        apiRequestInfoDTO.setVersion(version);

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        Mockito.when(obapiRequestContext.getAnalyticsData()).thenReturn(new HashMap<>());
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(obapiRequestContext.getApiRequestInfo()).thenReturn(apiRequestInfoDTO);

        CommonReportingDataExecutor commonReportingDataExecutor = Mockito.spy(CommonReportingDataExecutor.class);
        Mockito.doReturn(apiName).when(commonReportingDataExecutor).getApiName(Mockito.any());
        commonReportingDataExecutor.preProcessRequest(obapiRequestContext);

        Assert.assertEquals(obapiRequestContext.getAnalyticsData().size(), 7);
        Assert.assertEquals(obapiRequestContext.getAnalyticsData().get("userAgent"), userAgent);
        Assert.assertEquals(obapiRequestContext.getAnalyticsData().get("electedResource"), electedResource);
        Assert.assertEquals(obapiRequestContext.getAnalyticsData().get("httpMethod"), httpMethod);
        Assert.assertEquals(obapiRequestContext.getAnalyticsData().get("apiName"), apiName);
        Assert.assertEquals(obapiRequestContext.getAnalyticsData().get("apiSpecVersion"), version);
        Assert.assertEquals(obapiRequestContext.getAnalyticsData().get("messageId"), messageId);
        Assert.assertTrue((long) obapiRequestContext.getAnalyticsData().get("timestamp") > 0);
    }

    @Test(priority = 2)
    public void testPostRequestFlow() {

        String username = "tpp@wso2.conm";
        String clientId = "test_client_id";
        String consentId = "test_consent_id";

        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setUsername(username);
        apiRequestInfoDTO.setConsumerKey(clientId);

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        Mockito.when(obapiRequestContext.getApiRequestInfo()).thenReturn(apiRequestInfoDTO);
        Mockito.when(obapiRequestContext.getAnalyticsData()).thenReturn(new HashMap<>());
        Mockito.when(obapiRequestContext.getConsentId()).thenReturn(consentId);

        CommonReportingDataExecutor commonReportingDataExecutor = Mockito.spy(CommonReportingDataExecutor.class);
        commonReportingDataExecutor.postProcessRequest(obapiRequestContext);

        Assert.assertEquals(obapiRequestContext.getAnalyticsData().size(), 3);
        Assert.assertEquals(obapiRequestContext.getAnalyticsData().get("consumerId"), username);
        Assert.assertEquals(obapiRequestContext.getAnalyticsData().get("consentId"), consentId);
        Assert.assertEquals(obapiRequestContext.getAnalyticsData().get("clientId"), clientId);
    }

    @Test(priority = 3)
    public void testPreResponseFlow() {
        CommonReportingDataExecutor commonReportingDataExecutor = new CommonReportingDataExecutor();
        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        Mockito.when(obapiResponseContext.getAnalyticsData()).thenReturn(new HashMap<>());
        Mockito.when(obapiResponseContext.getModifiedPayload()).thenReturn("testPayloadOfLength21");
        Mockito.when(obapiResponseContext.getStatusCode()).thenReturn(200);

        commonReportingDataExecutor.preProcessResponse(obapiResponseContext);
        Assert.assertTrue(obapiResponseContext.getAnalyticsData().size() == 2);
        Assert.assertEquals(obapiResponseContext.getAnalyticsData().get("responsePayloadSize"), (long) 21);
        Assert.assertEquals(obapiResponseContext.getAnalyticsData().get("statusCode"), 200);
    }

}
