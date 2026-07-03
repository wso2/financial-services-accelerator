/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
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

package com.wso2.openbanking.accelerator.gateway.dpop.util;

import com.wso2.openbanking.accelerator.gateway.dpop.DPoPConstants;
import com.wso2.openbanking.accelerator.gateway.dpop.proof.DPoPProofException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;

import java.util.Map;
import java.util.TreeMap;

import static com.wso2.openbanking.accelerator.gateway.dpop.DPoPConstants.DPOP_NONCE_HEADER;

/**
 * Builds the RFC 9449 §7.1 {@code WWW-Authenticate: DPoP} challenge and dispatches a 401 Unauthorized response.
 */
public class Challenge {

    private static final Log log = LogFactory.getLog(Challenge.class);

    private final String acceptedAlgorithms;

    public Challenge(String acceptedAlgorithms) {
        this.acceptedAlgorithms = acceptedAlgorithms;
    }

    /**
     * Sends a 401 response with a {@code WWW-Authenticate: DPoP} challenge per RFC 9449 §7.1.
     *
     * @param synCtx  current message context
     * @param error   the RFC 9449 error code
     * @param message human-readable description
     * @param nonce   server-issued nonce to include, or {@code null}
     */
    public void send401(org.apache.synapse.MessageContext synCtx,
                        DPoPProofException.ErrorCode error, String message, String nonce) {

        MessageContext axis2MC = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
        try {
            RelayUtils.consumeAndDiscardMessage(axis2MC);
        } catch (AxisFault axisFault) {
            log.error("Error consuming request message during DPoP 401 response", axisFault);
        }

        StringBuilder challenge = new StringBuilder("DPoP");
        challenge.append(" error=\"").append(error.getValue()).append("\"");
        if (message != null) {
            challenge.append(", error_description=\"")
                    .append(message.replace("\"", "'")).append("\"");
        }
        if (acceptedAlgorithms != null) {
            challenge.append(", algs=\"").append(acceptedAlgorithms).append("\"");
        }

        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.put(HttpHeaders.WWW_AUTHENTICATE, challenge.toString());
        if (nonce != null) {
            headers.put(DPOP_NONCE_HEADER, nonce);
            // RFC 9449 §8.2: responses carrying a nonce must not be cached.
            headers.put(HttpHeaders.CACHE_CONTROL, DPoPConstants.CACHE_CONTROL_NO_STORE);
        }
        axis2MC.setProperty(MessageContext.TRANSPORT_HEADERS, headers);

        synCtx.setProperty(APIMgtGatewayConstants.HTTP_RESPONSE_STATUS_CODE, 401);
        Utils.send(synCtx, 401);
    }
}
