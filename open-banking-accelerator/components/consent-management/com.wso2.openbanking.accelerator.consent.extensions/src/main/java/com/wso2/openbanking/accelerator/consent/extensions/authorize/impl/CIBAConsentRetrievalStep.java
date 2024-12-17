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

package com.wso2.openbanking.accelerator.consent.extensions.authorize.impl;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentRetrievalStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Consent retrieval step for CIBA flow.
 */
@Deprecated
public class CIBAConsentRetrievalStep implements ConsentRetrievalStep {

    private static final Log log = LogFactory.getLog(CIBAConsentRetrievalStep.class);
    private static final String OPENBANKING_INTENT_ID = "openbanking_intent_id";
    private static final String VALUE = "value";

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        //If query params are null it should be a CIBA flow
        // Run this step only if it is a CIBA flow
        if (consentData.getSpQueryParams() == null) {
            if (consentData.getSensitiveDataMap().containsKey("request")) {
                String requestObject = (String) consentData.getSensitiveDataMap().get("request");
                String consentId = validateCibaRequestObjectAndExtractConsentId(requestObject);
                consentData.setConsentId(consentId);
            } else {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Request object unavailable");
            }
        }

    }

    private String validateCibaRequestObjectAndExtractConsentId(String requestObject) {

        String consentId = null;
        JSONObject jsonObject = ConsentExtensionUtils.getRequestObjectPayload(requestObject);

        if (jsonObject.containsKey(OPENBANKING_INTENT_ID)) {
            JSONObject intentObject = (JSONObject) jsonObject.get(OPENBANKING_INTENT_ID);
            if (intentObject.containsKey(VALUE)) {
                consentId = (String) intentObject.get(VALUE);
            }
        }

        if (consentId == null) {
            log.error("intent_id not found in request object");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "intent_id not found in request object");
        }
        return consentId;
    }
}
