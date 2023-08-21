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


package com.wso2.openbanking.accelerator.consent.extensions.authorize.utils;


import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;

/**
 * Util class for Consent authorize implementation.
 */
public class ConsentRetrievalUtil {

    private static final Log log = LogFactory.getLog(ConsentRetrievalUtil.class);

    /**
     * Method to extract request object from query params.
     *
     * @param spQueryParams
     * @return
     */
    public static String extractRequestObject(String spQueryParams) {

        if (StringUtils.isNotBlank(spQueryParams)) {
            String requestObject = null;
            String[] spQueries = spQueryParams.split("&");
            for (String param : spQueries) {
                if (param.contains("request=")) {
                    requestObject = (param.substring("request=".length())).replaceAll(
                            "\\r\\n|\\r|\\n|\\%20", "");
                }
            }
            if (requestObject != null) {
                return requestObject;
            }
        }
        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, ErrorConstants.REQUEST_OBJ_EXTRACT_ERROR);
    }

    /**
     * Method to validate the request object and extract consent ID.
     *
     * @param requestObject
     * @return
     */
    public static String extractConsentId(String requestObject) {

        String consentId = null;
        try {

            // validate request object and get the payload
            String requestObjectPayload;
            String[] jwtTokenValues = requestObject.split("\\.");
            if (jwtTokenValues.length == ConsentExtensionConstants.NUMBER_OF_PARTS_IN_JWS) {
                requestObjectPayload = new String(Base64.getUrlDecoder().decode(jwtTokenValues[1]),
                        StandardCharsets.UTF_8);
            } else {
                log.error(ErrorConstants.REQUEST_OBJ_NOT_SIGNED);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.REQUEST_OBJ_NOT_SIGNED);
            }
            Object payload = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(requestObjectPayload);
            if (!(payload instanceof JSONObject)) {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.NOT_JSON_PAYLOAD);
            }
            JSONObject jsonObject = (JSONObject) payload;

            // get consent id from the request object
            if (jsonObject.containsKey(ConsentExtensionConstants.CLAIMS)) {
                JSONObject claims = (JSONObject) jsonObject.get(ConsentExtensionConstants.CLAIMS);
                for (String claim : ConsentExtensionConstants.CLAIM_FIELDS) {
                    if (claims.containsKey(claim)) {
                        JSONObject claimObject = (JSONObject) claims.get(claim);
                        if (claimObject.containsKey(ConsentExtensionConstants.OPENBANKING_INTENT_ID)) {
                            JSONObject intentObject = (JSONObject) claimObject
                                    .get(ConsentExtensionConstants.OPENBANKING_INTENT_ID);
                            if (intentObject.containsKey(ConsentExtensionConstants.VALUE)) {
                                consentId = (String) intentObject.get(ConsentExtensionConstants.VALUE);
                                break;
                            }
                        }
                    }
                }
            }

            if (consentId == null) {
                log.error(ErrorConstants.INTENT_ID_NOT_FOUND);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.INTENT_ID_NOT_FOUND);
            }
            return consentId;

        } catch (ParseException e) {
            log.error(ErrorConstants.REQUEST_OBJ_PARSE_ERROR, e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, ErrorConstants.REQUEST_OBJ_PARSE_ERROR);
        }
    }


    /**
     * Check if the expiry date time of the consent has elapsed.
     *
     * @param expiryDate The expiry date/time of consent
     * @return boolean result of validation
     */
    public static boolean validateExpiryDateTime(String expiryDate) throws ConsentException {

        try {
            OffsetDateTime expDate = OffsetDateTime.parse(expiryDate);
            if (log.isDebugEnabled()) {
                log.debug(String.format(ErrorConstants.DATE_PARSE_MSG, expDate, OffsetDateTime.now()));
            }
            return OffsetDateTime.now().isBefore(expDate);
        } catch (DateTimeParseException e) {
            log.error(ErrorConstants.EXP_DATE_PARSE_ERROR, e);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.ACC_CONSENT_RETRIEVAL_ERROR);
        }
    }


    /**
     * Method to add debtor account details to consent data to send it to the consent page.
     *
     * @param initiation      Initiation object from the request
     * @param consentDataJSON Consent information object
     */
    public static void populateDebtorAccount(JSONObject initiation, JSONArray consentDataJSON) {

        if (initiation.get(ConsentExtensionConstants.DEBTOR_ACC) != null) {
            JSONObject debtorAccount = (JSONObject) initiation.get(ConsentExtensionConstants.DEBTOR_ACC);
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
    }

    /**
     * Method to add debtor account details to consent data to send it to the consent page.
     *
     * @param initiation
     * @param consentDataJSON
     */
    public static void populateCreditorAccount(JSONObject initiation, JSONArray consentDataJSON) {

        if (initiation.get(ConsentExtensionConstants.CREDITOR_ACC) != null) {
            JSONObject creditorAccount = (JSONObject) initiation.get(ConsentExtensionConstants.CREDITOR_ACC);
            JSONArray creditorAccountArray = new JSONArray();
            //Adding Debtor Account Scheme Name
            if (creditorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME) != null) {
                creditorAccountArray.add(ConsentExtensionConstants.SCHEME_NAME_TITLE + " : " +
                        creditorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME));
            }
            //Adding Debtor Account Identification
            if (creditorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION) != null) {
                creditorAccountArray.add(ConsentExtensionConstants.IDENTIFICATION_TITLE + " : " +
                        creditorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION));
            }
            //Adding Debtor Account Name
            if (creditorAccount.getAsString(ConsentExtensionConstants.NAME) != null) {
                creditorAccountArray.add(ConsentExtensionConstants.NAME_TITLE + " : " +
                        creditorAccount.getAsString(ConsentExtensionConstants.NAME));
            }
            //Adding Debtor Account Secondary Identification
            if (creditorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) != null) {
                creditorAccountArray.add(ConsentExtensionConstants.SECONDARY_IDENTIFICATION_TITLE + " : " +
                        creditorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION));
            }

            JSONObject jsonElementCreditor = new JSONObject();
            jsonElementCreditor.appendField(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.CREDITOR_ACC_TITLE);
            jsonElementCreditor.appendField(ConsentExtensionConstants.DATA_SIMPLE, creditorAccountArray);
            consentDataJSON.add(jsonElementCreditor);
        }
    }

    /**
     * Method to append Dummy data for Account ID. Ideally should be separate step calling accounts service
     *
     * @return accountsJSON
     */
    public static JSONArray appendDummyAccountID() {

        JSONArray accountsJSON = new JSONArray();
        JSONObject accountOne = new JSONObject();
        accountOne.appendField("account_id", "12345");
        accountOne.appendField("display_name", "Salary Saver Account");

        accountsJSON.add(accountOne);

        JSONObject accountTwo = new JSONObject();
        accountTwo.appendField("account_id", "67890");
        accountTwo.appendField("account_id", "67890");
        accountTwo.appendField("display_name", "Max Bonus Account");

        accountsJSON.add(accountTwo);

        return accountsJSON;

    }
}
