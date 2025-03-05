/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesYamlConfigParser;
import org.wso2.financial.services.accelerator.common.policy.FSPolicy;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.ConsentRetrievalStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.policy.retrieval.ConsentRetrievalStepPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consent retrieval step default implementation.
 */
public class ConsentRetrievalStepImpl implements ConsentRetrievalStep {

    private static final Log log = LogFactory.getLog(ConsentRetrievalStepImpl.class);

    private final List<FSPolicy> consentRetrievalStepPolicies;

    public ConsentRetrievalStepImpl() {

        consentRetrievalStepPolicies = FinancialServicesYamlConfigParser.getPolicies(
                "AuthorizeAPI", "/oauth2/authorize", "get", "execution-flow");
    }

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        if (!consentData.isRegulatory()) {
            return;
        }
        Map<String, Object> retrievalContext = new HashMap<>();
        for (FSPolicy fsPolicy : consentRetrievalStepPolicies) {
            if (fsPolicy instanceof ConsentRetrievalStepPolicy) {
                ConsentRetrievalStepPolicy consentRetrievalStepPolicy = (ConsentRetrievalStepPolicy) fsPolicy;
                consentRetrievalStepPolicy.execute(consentData, jsonObject,
                        consentRetrievalStepPolicy.getPropertyMap(), retrievalContext);
            }
        }
    }
}
