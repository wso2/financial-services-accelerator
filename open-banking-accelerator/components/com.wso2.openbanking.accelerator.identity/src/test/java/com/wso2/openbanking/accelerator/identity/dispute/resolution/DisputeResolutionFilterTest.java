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

package com.wso2.openbanking.accelerator.identity.dispute.resolution;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.util.ServiceProviderUtils;
import com.wso2.openbanking.accelerator.data.publisher.common.util.OBDataPublisherUtil;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test for dispute resolution filter.
 */
@PrepareForTest({OpenBankingConfigParser.class, OBDataPublisherUtil.class, APIManagementException.class,
        ServiceProviderUtils.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class DisputeResolutionFilterTest  extends PowerMockTestCase {
    MockHttpServletRequest request;
    MockHttpServletResponse response;
    FilterChain filterChain;

    @Mock
    OpenBankingConfigParser openBankingConfigParser;

    Map<String, Object> sampleRequestParams = new HashMap<>();

    Map<String, Object> sampleHeaderMap = new HashMap<>();

    @BeforeMethod
    public void beforeMethod() {

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = Mockito.spy(FilterChain.class);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        openBankingConfigParser = PowerMockito.mock(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);
    }

    public DisputeResolutionFilterTest() {
        sampleRequestParams.put("jsonRequest", "");
        sampleHeaderMap.put("Accept", "application/json");
        sampleHeaderMap.put("Postman-Token", "5c04e832-1a05-4d9a-974b-9e01d4c978f0");
        sampleHeaderMap.put("Host", "api.example.com");
        sampleHeaderMap.put("Accept-Encoding", "gzip, deflate, br");
        sampleHeaderMap.put("Connection", "keep-alive");
        sampleHeaderMap.put("Content-Type", "application/json");
        sampleHeaderMap.put("X-WSO2-Mutual-Auth-Cert", "MIIDczCCAlugAwIBAgIINeDHEkE4dGowDQYJKoZIhvcNAQELBQ" +
                "AwJDEiMCAGA1UEAxMZcG9ydGFsLXByb2R1Y3Rpb24tc2lnbmVy");
    }

    @Test
    public void capturingRequestResponseDataTest() throws Exception {

        when(openBankingConfigParser.isDisputeResolutionEnabled()).thenReturn(true);
        when(openBankingConfigParser.isNonErrorDisputeDataPublishingEnabled()).thenReturn(true);

        DisputeResolutionFilter filter = Mockito.spy(DisputeResolutionFilter.class);

        request.setMethod("GET");
        request.setRequestURI("/register");
        request.setCharacterEncoding("UTF-8");
        request.setParameters(sampleRequestParams);
        response.setStatus(200);
        response.setCharacterEncoding("UTF-8");

        Enumeration<String> enumeration = Collections.enumeration(sampleHeaderMap.keySet());

        PowerMockito.mockStatic(OBDataPublisherUtil.class);
        PowerMockito.doNothing().when(OBDataPublisherUtil.class, "publishData", Mockito.anyString(),
                Mockito.anyString(), Mockito.anyObject());

        filter.doFilter(request, response, filterChain);
        verify(filter, times(1));

    }
}

