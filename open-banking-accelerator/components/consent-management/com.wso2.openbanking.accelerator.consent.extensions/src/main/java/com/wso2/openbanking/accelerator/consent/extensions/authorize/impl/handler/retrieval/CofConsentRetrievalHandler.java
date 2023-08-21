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


package com.wso2.openbanking.accelerator.consent.extensions.authorize.impl.handler.retrieval;

import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.utils.ConsentRetrievalUtil;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to handle Confirmation of Funds Consent data retrieval for Authorize.
 */
public class CofConsentRetrievalHandler implements ConsentRetrievalHandler {

    private static final Log log = LogFactory.getLog(CofConsentRetrievalHandler.class);

    /**
     * Method defined to retrieve the COF consent related data in the authorization flow to send them to the
     * consent page to get PSU consent.
     *
     * @param consentResource Consent Resource parameter containing consent related information retrieved from database
     * @return
     * @throws ConsentException
     */
    @Override
    public JSONArray getConsentDataSet(ConsentResource consentResource) throws ConsentException {

        try {
            String receiptString = consentResource.getReceipt();
            Object receiptJSON = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(receiptString);

            //Checking whether the request body is in JSON format
            if (!(receiptJSON instanceof JSONObject)) {
                log.error(ErrorConstants.NOT_JSON_OBJECT_ERROR);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, ErrorConstants.NOT_JSON_OBJECT_ERROR);
            }

            //Checking whether the consent status is valid
            if (!consentResource.getCurrentStatus().equals(ConsentExtensionConstants.AWAITING_AUTH_STATUS) &&
                    !consentResource.getCurrentStatus().equals(ConsentExtensionConstants.AUTHORIZED_STATUS)) {
                log.error(ErrorConstants.STATE_INVALID_ERROR);
                //Currently throwing error as 400 response. Developer also have the option of appending a field IS_ERROR
                // to the jsonObject and showing it to the user in the webapp. If so, the IS_ERROR have to be checked in
                // any later steps.
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.STATE_INVALID_ERROR);
            }

            JSONArray consentDataJSON = new JSONArray();
            JSONObject receipt = (JSONObject) receiptJSON;
            JSONObject data = (JSONObject) receipt.get(ConsentExtensionConstants.DATA);

            //Adding Expiration Date Time
            if (data.getAsString(ConsentExtensionConstants.EXPIRATION_DATE) != null) {

                if (!ConsentRetrievalUtil
                        .validateExpiryDateTime(data.getAsString(ConsentExtensionConstants.EXPIRATION_DATE))) {
                    log.error(ErrorConstants.CONSENT_EXPIRED);
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.CONSENT_EXPIRED);
                }

                String expiry = data.getAsString(ConsentExtensionConstants.EXPIRATION_DATE);
                JSONArray expiryArray = new JSONArray();
                expiryArray.add(expiry);

                JSONObject jsonElementExpiry = new JSONObject();
                jsonElementExpiry.appendField(ConsentExtensionConstants.TITLE,
                        ConsentExtensionConstants.EXPIRATION_DATE_TITLE);
                jsonElementExpiry.appendField(ConsentExtensionConstants.DATA_SIMPLE, expiryArray);
                consentDataJSON.add(jsonElementExpiry);
            } else {
                JSONArray expiryArray = new JSONArray();
                expiryArray.add(ConsentExtensionConstants.OPEN_ENDED_AUTHORIZATION);

                JSONObject jsonElementExpiry = new JSONObject();
                jsonElementExpiry.appendField(ConsentExtensionConstants.TITLE,
                        ConsentExtensionConstants.EXPIRATION_DATE_TITLE);
                jsonElementExpiry.appendField(ConsentExtensionConstants.DATA_SIMPLE, expiryArray);
                consentDataJSON.add(jsonElementExpiry);
            }

            //Adding Debtor Account
            if (data.get(ConsentExtensionConstants.DEBTOR_ACC) != null) {
                JSONObject debtorAccount = (JSONObject) data.get(ConsentExtensionConstants.DEBTOR_ACC);
                JSONArray debtorAccountArray = new JSONArray();
                //Adding Debtor Account Scheme Name
                if (debtorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME) != null) {
                    debtorAccountArray.add(ConsentExtensionConstants.SCHEME_NAME_TITLE + " : " +
                            debtorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME));
                }
                //Adding Debtor Account Identification
                if (debtorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION) != null) {
                    debtorAccountArray.add(ConsentExtensionConstants.IDENTIFICATION_TITLE + " : " +
                            debtorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION));
                }
                //Adding Debtor Account Name
                if (debtorAccount.getAsString(ConsentExtensionConstants.NAME) != null) {
                    debtorAccountArray.add(ConsentExtensionConstants.NAME_TITLE + " : " +
                            debtorAccount.getAsString(ConsentExtensionConstants.NAME));
                }
                //Adding Debtor Account Secondary Identification
                if (debtorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) != null) {
                    debtorAccountArray.add(ConsentExtensionConstants.SECONDARY_IDENTIFICATION_TITLE + " : " +
                            debtorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION));
                }

                JSONObject jsonElementDebtor = new JSONObject();
                jsonElementDebtor.appendField(ConsentExtensionConstants.TITLE,
                        ConsentExtensionConstants.DEBTOR_ACC_TITLE);
                jsonElementDebtor.appendField(ConsentExtensionConstants.DATA_SIMPLE, debtorAccountArray);
                consentDataJSON.add(jsonElementDebtor);
            }

            if (log.isDebugEnabled()) {
                log.debug("Returned consent data to get the PSU consent " + consentDataJSON);
            }

            return consentDataJSON;
        } catch (ParseException e) {
            log.error("Exception occurred while getting consent data. Caused by : ", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, ErrorConstants.CONSENT_RETRIEVAL_ERROR);
        }
    }

}
