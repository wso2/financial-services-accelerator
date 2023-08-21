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

package com.wso2.openbanking.accelerator.gateway.mediator;

import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.accelerator.gateway.util.GatewayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Append Basic Authorisation header to the API calls from gateway to be passed on to the
 * key management server. Invoked by the synapse in-sequence.xml files.
 */
public class BasicAuthMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(BasicAuthMediator.class);

    public BasicAuthMediator() {

        log.info("Initializing Basic Authentication Mediator to append basic auth credentials in gateway " +
                "in-sequence.");
    }

    @Override
    public boolean mediate(MessageContext messageContext) {

        messageContext.setProperty("basicAuthentication", ("Basic " + Base64.getEncoder().encodeToString(
                (getAPIMConfigFromKey(GatewayConstants.API_KEY_VALIDATOR_USERNAME) + ":"
                        + getAPIMConfigFromKey(GatewayConstants.API_KEY_VALIDATOR_PASSWORD))
                        .getBytes(StandardCharsets.UTF_8))));
        return true;
    }

    @Generated(message = "Excluded since this method is used for testing purposes")
    String getAPIMConfigFromKey(String key) {

        return GatewayUtils.getAPIMgtConfig(key);
    }
}
