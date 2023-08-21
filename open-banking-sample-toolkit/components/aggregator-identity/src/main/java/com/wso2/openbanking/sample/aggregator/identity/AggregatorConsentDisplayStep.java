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

package com.wso2.openbanking.sample.aggregator.identity;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentRetrievalStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.sample.aggregator.identity.util.AggregatorConsentExtensionUtil;
import com.wso2.openbanking.sample.aggregator.identity.util.AggregatorConstants;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Aggregator Consent Display Step.
 */
public class AggregatorConsentDisplayStep implements ConsentRetrievalStep {

    private static final Log log = LogFactory.getLog(AggregatorConsentDisplayStep.class);
    private JSONObject errorJSON = new JSONObject();

    public void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        try {
            String requestObj = AggregatorConsentExtensionUtil.extractRequestObject(consentData.getSpQueryParams());
            String consent = AggregatorConsentExtensionUtil.validateRequestObjectAndExtractConsent(requestObj);
            ConsentResource consentResource1 = new ConsentResource();
            consentResource1.setReceipt(consent);
            consentResource1.setClientID(consentData.getClientId());
            consentResource1.setReceipt(consent);

            consentData.setType(AggregatorConstants.ACCOUNT_CONSENT_TYPE);
            consentData.setConsentResource(consentResource1);
            String receiptString = consentResource1.getReceipt();
            Object receiptJSON = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(receiptString);
            if (!(receiptJSON instanceof JSONObject)) {
                log.error("Receipt is not a JSON object");
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Receipt is not a JSON object");
            }
            JSONObject receipt = (JSONObject) receiptJSON;

            JSONArray permissions = (JSONArray) ((JSONObject) receipt.get("Data")).get("Permissions");
            JSONArray consentDataJSON = new JSONArray();

            JSONObject jsonElementPermissions = new JSONObject();
            jsonElementPermissions.appendField("title", "Permissions");
            jsonElementPermissions.appendField("data", permissions);

            consentDataJSON.add(jsonElementPermissions);
            String expiry = ((JSONObject) receipt.get("Data")).getAsString("ExpirationDateTime");
            JSONArray expiryArray = new JSONArray();
            expiryArray.add(expiry);

            JSONObject jsonElementExpiry = new JSONObject();
            jsonElementExpiry.appendField("title", "Expiration Date Time");
            jsonElementExpiry.appendField("data", expiryArray);

            consentDataJSON.add(jsonElementExpiry);

            jsonObject.appendField("consentData", consentDataJSON);

            //Appending Dummy data for Account ID. Ideally should be separate step calling accounts service

            JSONArray accountsJSON = new JSONArray();
            JSONObject accountOne = new JSONObject();
            accountOne.appendField("account_id", "12345");
            accountOne.appendField("display_name", "Salary Saver Account");

            accountsJSON.add(accountOne);

            JSONObject accountTwo = new JSONObject();
            accountTwo.appendField("account_id", "67890");
            accountTwo.appendField("display_name", "Max Bonus Account");

            accountsJSON.add(accountTwo);

            jsonObject.appendField("accounts", accountsJSON);

        } catch (ParseException e) {
            String errorMessage =  "Exception occurred while getting consent data";
            errorJSON.put("Reason", errorMessage);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, errorJSON, e);
        }
    }
}
