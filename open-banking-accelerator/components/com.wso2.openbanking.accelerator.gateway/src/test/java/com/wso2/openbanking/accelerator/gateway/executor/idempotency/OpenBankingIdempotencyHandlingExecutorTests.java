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

package com.wso2.openbanking.accelerator.gateway.executor.idempotency;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants;
import com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedMember;
import com.wso2.openbanking.accelerator.gateway.cache.OpenBankingIdempotencyCacheKey;
import com.wso2.openbanking.accelerator.gateway.cache.OpenBankingIdempotencyValidationCache;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.accelerator.gateway.util.IdempotencyConstants;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 ** Tests class for OpenBankingIdempotencyHandlingExecutor.
 */
@PowerMockIgnore({"jdk.internal.reflect.*", "javax.management.*"})
@PrepareForTest({OpenBankingConfigParser.class})
public class OpenBankingIdempotencyHandlingExecutorTests extends PowerMockTestCase {

    @Mock
    OBAPIRequestContext obapiRequestContextMock;

    @Mock
    OBAPIResponseContext obapiResponseContextMock;

    @Mock
    MsgInfoDTO msgInfoDTO;

    @Mock
    APIRequestInfoDTO apiRequestInfoDTO;

    @Mock
    OpenBankingConfigParser openBankingConfigParser;

    String sampleIdempotencyKey = "a5ff9494-2a15-48f9-8ab4-05a10b91215b";
    String sampleConsumerKey = "dummykey";
    String sampleElectedResource = "/sampleElectedResource/1234";
    String sampleResponsePayload = "{\"transactionStatus\":\"RCVD\",\"chosenScaMethod\":" +
            "[{\"name\":\"SMS OTP on Mobile\"," +
            "\"authenticationType\":\"SMS_OTP\",\"explanation\":\"SMS based one time password\"," +
            "\"authenticationMethodId\":\"sms-otp\"}],\"_links\":{\"scaStatus\":" +
            "{\"href\":\"/v1/payments/sepa-credit-transfers/beecd66c-82ae-4ac8-9c04-9bd7c886d4a4/" +
            "authorisations/1d5b6e3b-2180-4b4f-bb8c-054c597cb4e3\"},\"scaOAuth\":" +
            "{\"href\":\"https://localhost:8243/.well-known/openid-configuration\"}," +
            "\"self\":{\"href\":\"/v/payments/sepa-credit-transfers/beecd66c-82ae-4ac8-9c04-9bd7c886d4a4\"}," +
            "\"status\":{\"href\":\"/v1/payments/sepa-credit-transfers/beecd66c-82ae-4ac8-9c04-9bd7c886d4a4" +
            "/status\"}},\"paymentId\":\"beecd66c-82ae-4ac8-9c04-9bd7c886d4a4\"}";

    DateTimeFormatter dtf = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    ZonedDateTime zdt = ZonedDateTime.now();
    String sampleCreatedTime = dtf.format(zdt);

    String idempotencyCacheKeyHeader = "x-Idempotency-Key";

    @BeforeClass
    public void initClass() {

        MockitoAnnotations.initMocks(this);
    }

    @Test(priority = 1)
    public void testPostProcessResponse() {

        mockStatic(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParser);

        Map<String, Object> configuration = new HashMap<>();
        configuration.putAll(getDistributedCachingMockConfigurations());
        configuration.putAll(getIdempotencyMockConfigurations());
        Mockito.when(openBankingConfigParser.getConfiguration()).thenReturn(configuration);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);

        // Mocking response payload
        when(obapiResponseContextMock.getResponsePayload()).thenReturn(sampleResponsePayload);

        // Mocking consumer key
        when(obapiResponseContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTO);
        when(apiRequestInfoDTO.getConsumerKey()).thenReturn(sampleConsumerKey);

        // Mocking context props
        Map<String, String> contextProps = new HashMap<>();
        contextProps.put(GatewayConstants.REQUEST_CACHE_KEY, sampleResponsePayload);
        contextProps.put(GatewayConstants.IDEMPOTENCY_KEY_CACHE_KEY, sampleIdempotencyKey);
        when(obapiResponseContextMock.getContextProps()).thenReturn(contextProps);

        // Mocking response headers
        when(obapiResponseContextMock.getMsgInfo()).thenReturn(msgInfoDTO);
        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put(idempotencyCacheKeyHeader, sampleIdempotencyKey);
        responseHeaders.put("CreatedTime", sampleCreatedTime);
        when(msgInfoDTO.getHeaders()).thenReturn(responseHeaders);

        // Mocking elected resource
        when(msgInfoDTO.getResource()).thenReturn(sampleElectedResource);

        OpenBankingIdempotencyHandlingExecutorImpl openBankingIdempotencyHandlingExecutorImpl =
                new OpenBankingIdempotencyHandlingExecutorImpl();
        openBankingIdempotencyHandlingExecutorImpl.postProcessResponse(obapiResponseContextMock);

        String cacheKey = sampleConsumerKey + "_" + sampleElectedResource + "_" + sampleIdempotencyKey;
        HashMap<String, String> expectedFromCache = new HashMap<>();
        expectedFromCache.put(GatewayConstants.REQUEST_CACHE_KEY, sampleResponsePayload);
        expectedFromCache.put(GatewayConstants.RESPONSE_CACHE_KEY, sampleResponsePayload);
        expectedFromCache.put(GatewayConstants.CREATED_TIME_CACHE_KEY, sampleCreatedTime);

        HashMap<String, String> fromCache = OpenBankingIdempotencyValidationCache.getInstance()
                .getFromCache(OpenBankingIdempotencyCacheKey.of(cacheKey));

        Assert.assertEquals(fromCache, expectedFromCache);
    }

    @Test(priority = 2)
    public void testPostProcessRequest() {

        mockStatic(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParser);

        Map<String, Object> configuration = new HashMap<>();
        configuration.putAll(getDistributedCachingMockConfigurations());
        configuration.putAll(getIdempotencyMockConfigurations());
        Mockito.when(openBankingConfigParser.getConfiguration()).thenReturn(configuration);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);

        when(obapiRequestContextMock.getRequestPayload()).thenReturn(sampleResponsePayload);
        // Mocking request headers
        when(obapiRequestContextMock.getMsgInfo()).thenReturn(msgInfoDTO);
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(idempotencyCacheKeyHeader, sampleIdempotencyKey);
        when(msgInfoDTO.getHeaders()).thenReturn(requestHeaders);

        // Mocking elected resource
        when(msgInfoDTO.getResource()).thenReturn(sampleElectedResource);

        // Mocking consumer key
        when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTO);
        when(apiRequestInfoDTO.getConsumerKey()).thenReturn(sampleConsumerKey);

        OpenBankingIdempotencyHandlingExecutorImpl openBankingIdempotencyHandlingExecutorImpl =
                new OpenBankingIdempotencyHandlingExecutorImpl();
        openBankingIdempotencyHandlingExecutorImpl.postProcessRequest(obapiRequestContextMock);

    }

    private Map<String, Object> getDistributedCachingMockConfigurations() {

        Map<String, Object> configuration = new HashMap<>();

        configuration.put(OpenBankingDistributedCacheConstants.ENABLED, "true");
        configuration.put(OpenBankingDistributedCacheConstants.HOST_NAME, "localhost");
        configuration.put(OpenBankingDistributedCacheConstants.PORT, "5721");
        configuration.put(OpenBankingDistributedCacheConstants.DISCOVERY_MECHANISM, "Multicast");
        configuration.put(OpenBankingDistributedCacheConstants.MULTICAST_GROUP, "224.2.2.3");
        configuration.put(OpenBankingDistributedCacheConstants.MULTICAST_PORT, "54321");
        ArrayList<String> interfaces = new ArrayList<>();
        interfaces.add("192.168.1.100-110");
        configuration.put(OpenBankingDistributedCacheConstants.TRUSTED_INTERFACES, interfaces);
        configuration.put(OpenBankingDistributedCacheConstants.HAZELCAST_PROPERTY_MAX_HEARTBEAT, "600");
        configuration.put(OpenBankingDistributedCacheConstants.HAZELCAST_PROPERTY_MAX_MASTER_CONFIRMATION, "900");
        configuration.put(OpenBankingDistributedCacheConstants.HAZELCAST_PROPERTY_MERGE_FIRST_RUN_DELAY, "60");
        configuration.put(OpenBankingDistributedCacheConstants.HAZELCAST_PROPERTY_MERGE_NEXT_RUN_DELAY, "30");
        configuration.put(OpenBankingDistributedCacheConstants.PROPERTY_LOGGING_TYPE, "none");

        return configuration;
    }

    private Map<String, Object> getIdempotencyMockConfigurations() {

        Map<String, Object> configuration = new HashMap<>();
        configuration.put(IdempotencyConstants.IDEMPOTENCY_IS_ENABLED, "true");
        configuration.put(IdempotencyConstants.IDEMPOTENCY_CACHE_TIME_TO_LIVE, "1440");
        configuration.put(IdempotencyConstants.IDEMPOTENCY_KEY_HEADER, idempotencyCacheKeyHeader);
        configuration.put(IdempotencyConstants.IDEMPOTENCY_ALLOWED_TIME, "24");

        return configuration;
    }

    @AfterClass
    public void after() {

        OpenBankingDistributedMember.of().shutdown();
    }

}
