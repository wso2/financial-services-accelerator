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
package org.wso2.financial.services.accelerator.consent.mgt.extensions.policy.retrieval;

import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.policy.FSPolicy;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;

import java.util.Map;

/**
 * Base class for Financial Services Consent Retrieval Step Policy.
 */
public abstract class ConsentRetrievalStepPolicy extends FSPolicy {

    /**
     * Execute the consent step policy.
     *
     * @param consentData - Includes all the data that is received to the consent page.
     * @param jsonObject  - Passed on through each policy.
     * @throws ConsentException - ConsentException thrown in case of failure.
     */
    public abstract void execute(ConsentData consentData, JSONObject jsonObject, Map<String, Object> propertyMap,
                                 Map<String, Object> retrievalContext) throws ConsentException;

}
