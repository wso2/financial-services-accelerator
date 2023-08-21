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

package com.wso2.openbanking.accelerator.gateway.synapse.handler;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.data.publisher.common.util.OBDataPublisherUtil;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.accelerator.gateway.util.GatewayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Dispute Resolution Synapse Handler.
 */
public class DisputeResolutionSynapseHandler extends AbstractSynapseHandler {
    private static final Log log = LogFactory.getLog(DisputeResolutionSynapseHandler.class);

    /**
     * Handle request message coming into the engine.
     *
     * @param messageContext incoming request message context
     * @return whether mediation flow should continue
     */
    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {

        //Checking Dispute Resolution Feature is Enabled
        if (!OpenBankingConfigParser.getInstance().isDisputeResolutionEnabled()) {
            return true;
        }

        org.apache.axis2.context.MessageContext axis2MC =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        Map<String, Object> contextEntries = messageContext.getContextEntries();

        //Extracting Request Body
        Optional<String> requestBody = Optional.empty();
        try {
            requestBody = GatewayUtils.buildMessagePayloadFromMessageContext(axis2MC, headers);
        } catch (OpenBankingException e) {
            log.error("Unable to build request payload", e);
        }

        contextEntries.put(OpenBankingConstants.REQUEST_BODY,
                requestBody.isPresent() ? requestBody.get() : null);
        return true;
    }

    /**
     * Handle request message going out from the engine.
     *
     * @param messageContext outgoing request message context
     * @return whether mediation flow should continue
     */
    @Override
    @Generated(message = "Ignoring since method contains no logics")
    public boolean handleRequestOutFlow(MessageContext messageContext) {
        return true;
    }

    /**
     * Handle response message coming into the engine.
     *
     * @param messageContext incoming response message context
     * @return whether mediation flow should continue
     */
    @Override
    @Generated(message = "Ignoring since method contains no logics")
    public boolean handleResponseInFlow(MessageContext messageContext) {
        return true;
    }

    /**
     * Handle response message going out from the engine.
     *
     * @param messageContext outgoing response message context
     * @return whether mediation flow should continue
     */
    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {

        //Checking Dispute Resolution Feature is Enabled
        if (!OpenBankingConfigParser.getInstance().isDisputeResolutionEnabled()) {
            return true;
        }

        org.apache.axis2.context.MessageContext axis2MessageContext
                = ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        Map<String, Object> disputeResolutionData = new HashMap<>();
        int statusCode = 0;
        String httpMethod = null;
        String headers = null;
        long unixTimestamp  = Instant.now().getEpochSecond();
        String  electedResource = (String) messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE);
        String responseBody = null;
        String requestBody = null;

        //Extracting Status Code
        String stringStatusCode = axis2MessageContext.getProperty(GatewayConstants.HTTP_SC).toString();
        statusCode = Integer.parseInt(stringStatusCode);

        //Extracting Headers
        Map headerMap = (Map) axis2MessageContext
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        headers = headerMap.toString();

        //Extracting HTTP Method
        if (messageContext.getProperty(GatewayConstants.HTTP_METHOD) != null) {
            httpMethod = (String) messageContext.getProperty(GatewayConstants.HTTP_METHOD);
        } else {
            httpMethod = GatewayConstants.UNKNOWN;
        }

        //Extracting Response Body
        Optional<String> response = Optional.empty();
        try {
            response = GatewayUtils.buildMessagePayloadFromMessageContext(axis2MessageContext, headerMap);
        } catch (OpenBankingException e) {
            log.error("Unable to build response payload", e);

        }
        responseBody = response.get();

        //Extracting Request Body
        Map<String, Object> contextEntries = messageContext.getContextEntries();
        requestBody = (String) contextEntries.get(OpenBankingConstants.REQUEST_BODY);

        //reduced Headers, Request and Response Body Lengths
        requestBody = OpenBankingUtils.reduceStringLength(requestBody,
                OpenBankingConfigParser.getInstance().getMaxRequestBodyLength());
        responseBody = OpenBankingUtils.reduceStringLength(responseBody,
                OpenBankingConfigParser.getInstance().getMaxResponseBodyLength());
        headers = OpenBankingUtils.reduceStringLength(headers,
                OpenBankingConfigParser.getInstance().getMaxHeaderLength());

        // Add the captured data put into the disputeResolutionData Map
        disputeResolutionData.put(OpenBankingConstants.STATUS_CODE, statusCode);
        disputeResolutionData.put(OpenBankingConstants.HTTP_METHOD, httpMethod);
        disputeResolutionData.put(OpenBankingConstants.ELECTED_RESOURCE, electedResource);
        disputeResolutionData.put(OpenBankingConstants.TIMESTAMP, unixTimestamp);
        disputeResolutionData.put(OpenBankingConstants.HEADERS, headers);
        disputeResolutionData.put(OpenBankingConstants.RESPONSE_BODY, responseBody);
        disputeResolutionData.put(OpenBankingConstants.REQUEST_BODY, requestBody);

        //Checking configurations to publish Dispute Data
        if (OpenBankingUtils.isPublishableDisputeData(statusCode)) {
            OBDataPublisherUtil.publishData(OpenBankingConstants.DISPUTE_RESOLUTION_STREAM_NAME,
                    OpenBankingConstants.DISPUTE_RESOLUTION_STREAM_VERSION, disputeResolutionData);
        }

        return true;
    }

}

