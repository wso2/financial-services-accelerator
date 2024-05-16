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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.data.publisher.common.util.OBDataPublisherUtil;
import com.wso2.openbanking.accelerator.identity.token.wrapper.RequestWrapper;
import com.wso2.openbanking.accelerator.identity.token.wrapper.ResponseWrapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.wso2.openbanking.accelerator.common.util.OpenBankingUtils.isPublishableDisputeData;
import static com.wso2.openbanking.accelerator.common.util.OpenBankingUtils.reduceStringLength;

/**
 * Dispute Resolution Filter.
 */
public class DisputeResolutionFilter implements Filter {

    private static final Log log = LogFactory.getLog(DisputeResolutionFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        //Checking Dispute Resolution Feature is Enabled
        if (!OpenBankingConfigParser.getInstance().isDisputeResolutionEnabled()) {
            chain.doFilter(request, response);
        } else {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // Create a custom response wrapper to capture the response output
            ResponseWrapper responseWrapper = new ResponseWrapper(httpResponse);

            // Create a custom request wrapper to capture the request
            RequestWrapper requestWrapper = new RequestWrapper(httpRequest);

            // Retrieve the captured request output
            byte[] requestContent = requestWrapper.getCapturedRequest();

            // Convert the request content to JSON
            String jsonRequest = new String(requestContent, httpRequest.getCharacterEncoding());

            //get headers from requestWrapper
            Enumeration<String> headersRequestWrapper = requestWrapper.getHeaderNames();

            // Capture error request information
            String httpMethod = httpRequest.getMethod();
            String resourceURL = httpRequest.getRequestURL().toString();
            Map<String, String[]> requestParams = httpRequest.getParameterMap();

            // Convert requestParams to JSON string representation
            ObjectMapper objectMapper = new ObjectMapper();
            String requestParamsJson = objectMapper.writeValueAsString(requestParams);

            String requestBody = StringUtils.defaultIfEmpty(jsonRequest, requestParamsJson);

            Enumeration<String> headersMap = headersRequestWrapper;

            //Convert the headerMap to a string
            StringJoiner joiner = new StringJoiner(", ");
            while (headersMap.hasMoreElements()) {
                String element = headersMap.nextElement();
                joiner.add(element);
            }
            String headers = joiner.toString();

            chain.doFilter(requestWrapper, responseWrapper);

            // Retrieve the captured response output
            byte[] responseContent = responseWrapper.getData();

            // Convert the response content to JSON
            String jsonResponse = new String(responseContent, httpResponse.getCharacterEncoding());

            Map<String, Object> disputeResolutionData = new HashMap<>();

            // Capture the response information
            int statusCode = httpResponse.getStatus();
            String responseBody = jsonResponse;

            long unixTimestamp = Instant.now().getEpochSecond();

            //reduced Headers, Request and Response Body Lengths
            requestBody = reduceStringLength(requestBody,
                    OpenBankingConfigParser.getInstance().getMaxRequestBodyLength());
            responseBody = reduceStringLength(responseBody,
                    OpenBankingConfigParser.getInstance().getMaxResponseBodyLength());
            headers = reduceStringLength(headers,
                    OpenBankingConfigParser.getInstance().getMaxHeaderLength());

            // Add the captured data put into the disputeResolutionData Map
            disputeResolutionData.put(OpenBankingConstants.REQUEST_BODY, requestBody);
            disputeResolutionData.put(OpenBankingConstants.RESPONSE_BODY, responseBody);
            disputeResolutionData.put(OpenBankingConstants.STATUS_CODE, statusCode);
            disputeResolutionData.put(OpenBankingConstants.HTTP_METHOD, httpMethod);
            disputeResolutionData.put(OpenBankingConstants.ELECTED_RESOURCE, resourceURL);
            disputeResolutionData.put(OpenBankingConstants.TIMESTAMP, unixTimestamp);
            disputeResolutionData.put(OpenBankingConstants.HEADERS, headers);

            //Checking configurations to publish Dispute Data
            if (isPublishableDisputeData(statusCode)) {
                OBDataPublisherUtil.publishData(OpenBankingConstants.DISPUTE_RESOLUTION_STREAM_NAME,
                        OpenBankingConstants.DISPUTE_RESOLUTION_STREAM_VERSION, disputeResolutionData);
            }

            response.getOutputStream().write(responseWrapper.getData());
        }

    }

}
