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
package com.wso2.openbanking.accelerator.consent.extensions.manage.impl;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.extensions.util.ConsentManageUtil;
import com.wso2.openbanking.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.handler.EventNotificationPersistenceServiceHandler;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Consent Manage request handler class for Account Request Validation.
 */
public class AccountConsentManageRequestHandler implements ConsentManageRequestHandler {

    private static final Log log = LogFactory.getLog(AccountConsentManageRequestHandler.class);
    private static final String ACCOUNT_CONSENT_GET_PATH = "account-access-consents";
    private static final String UUID_REGEX =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    private static final String REVOKED_STATUS = "revoked";
    private static final String ACCOUNT_CONSENT_CREATE_PATH = "account-access-consents";
    private static final String CREATED_STATUS = "created";
    private static final String AUTH_TYPE_AUTHORIZATION = "authorization";


    /**
     * Method to handle Account Consent Manage Post Request.
     *
     * @param consentManageData  Object containing request details
     */
    @Override
    public void handleConsentManagePost(ConsentManageData consentManageData) {

        //Get the request payload from the ConsentManageData
        Object request = consentManageData.getPayload();

        if (request == null || request instanceof JSONArray) {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.NOT_JSON_OBJECT_ERROR);
        }

        JSONObject requestObject;
        if (consentManageData.getRequestPath().equals(ACCOUNT_CONSENT_CREATE_PATH)) {
            //Validate Account Initiation request
            requestObject = (JSONObject) request;
            if (!validateInitiation(requestObject)) {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.PAYLOAD_INVALID);
            }
        } else {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Request path invalid");
        }

        ConsentResource requestedConsent = new ConsentResource(consentManageData.getClientId(),
                requestObject.toJSONString(), ConsentExtensionConstants.ACCOUNTS,
                ConsentExtensionConstants.AWAITING_AUTH_STATUS);

        //Set request object to the response
        JSONObject response = requestObject;

        DetailedConsentResource createdConsent;

        appendConsentExpirationTimestampAttribute(requestedConsent);

        //create consent
        try {
            createdConsent = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                    .createAuthorizableConsent(requestedConsent, null,
                            CREATED_STATUS, AUTH_TYPE_AUTHORIZATION, true);
            consentManageData.setResponsePayload(ConsentManageUtil.getInitiationResponse(response, createdConsent,
                    consentManageData, ConsentExtensionConstants.ACCOUNTS));
            consentManageData.setResponseStatus(ResponseStatus.CREATED);
        } catch (ConsentManagementException e) {
            log.error(e.getMessage());
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public void handleConsentManageGet(ConsentManageData consentManageData) {

        if (consentManageData.getRequestPath().startsWith(ACCOUNT_CONSENT_GET_PATH)) {
            String consentId = consentManageData.getRequestPath().split("/")[1];
            if (ConsentManageUtil.isConsentIdValid(consentId)) {
                try {
                    ConsentResource consent = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                            .getConsent(consentId, false);
                    if (consent == null) {
                        log.error("No valid consent found for given information");
                        throw new ConsentException(ResponseStatus.BAD_REQUEST,
                                ErrorConstants.NO_CONSENT_FOR_CLIENT_ERROR);
                    }
                    if (!consent.getClientID().equals(consentManageData.getClientId())) {
                        //Throwing same error as null scenario since client will not be able to identify if consent
                        // exists if consent does not belong to them
                        throw new ConsentException(ResponseStatus.BAD_REQUEST,
                                ErrorConstants.NO_CONSENT_FOR_CLIENT_ERROR);
                    }
                    JSONObject receiptJSON = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).
                            parse(consent.getReceipt());
                    JSONObject data = (JSONObject) receiptJSON.get("Data");
                    data.appendField("ConsentId", consent.getConsentID());
                    data.appendField("CreationDateTime", convertEpochDateTime(consent.getCreatedTime()));
                    data.appendField("StatusUpdateDateTime", convertEpochDateTime(consent.getUpdatedTime()));
                    receiptJSON.put("Data", data);
                    consentManageData.setResponsePayload(receiptJSON);
                    consentManageData.setResponseStatus(ResponseStatus.OK);
                } catch (ConsentManagementException | ParseException e) {
                    log.error(e.getMessage());
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
                }
            } else {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent ID invalid");
            }
        } else {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.PATH_INVALID);
        }

    }

    @Override
    public void handleConsentManageDelete(ConsentManageData consentManageData) {

        if (consentManageData.getRequestPath().startsWith(ConsentExtensionConstants.ACCOUNT_CONSENT_DELETE_PATH)) {
            String consentId = consentManageData.getRequestPath().split(
                    ConsentExtensionConstants.ACCOUNT_CONSENT_DELETE_PATH)[1];
            if (ConsentManageUtil.isConsentIdValid(consentId)) {
                try {
                    ConsentResource consentResource = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                            .getConsent(consentId, false);

                    if (!consentResource.getClientID().equals(consentManageData.getClientId())) {
                        //Throwing this error in a generic manner since client will not be able to identify if consent
                        // exists if consent does not belong to them
                        throw new ConsentException(ResponseStatus.BAD_REQUEST,
                                ErrorConstants.NO_CONSENT_FOR_CLIENT_ERROR);
                    }

                    if (REVOKED_STATUS.equals(consentResource.getCurrentStatus())) {
                        throw new ConsentException(ResponseStatus.BAD_REQUEST,
                                "Consent already in revoked state");
                    }

                    boolean success = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                            .revokeConsentWithReason(consentId, REVOKED_STATUS,
                                    ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
                    if (!success) {
                        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                                "Token revocation unsuccessful");
                    }
                    consentManageData.setResponseStatus(ResponseStatus.NO_CONTENT);
                    if (success) {
                        // persist a new notification to the DB
                        // This is a sample event notification persisting. This can be modified in the Toolkit level
                        if (OpenBankingConfigParser.getInstance().isRealtimeEventNotificationEnabled()) {
                            JSONObject notificationInfo = new JSONObject();
                            notificationInfo.put("consentID", consentId);
                            notificationInfo.put("status", "Consent Revocation");
                            notificationInfo.put("timeStamp", System.currentTimeMillis());
                            EventNotificationPersistenceServiceHandler.getInstance().persistRevokeEvent(
                                    consentResource.getClientID(), consentId,
                                    "Consent Revocation", notificationInfo);
                        }
                    }
                } catch (ConsentManagementException e) {
                    log.error(e.getMessage());
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
                }
            } else {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent ID invalid");
            }
        } else {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Request path invalid");
        }
    }

    private boolean validateInitiation(JSONObject initiation) {

        if (!initiation.containsKey("Data") || !(initiation.get("Data") instanceof JSONObject)) {
            return false;
        }

        JSONObject data = (JSONObject) initiation.get("Data");

        if (!data.containsKey("Permissions") || !(data.get("Permissions") instanceof JSONArray)) {
            return false;
        }

        JSONArray permissions = (JSONArray) data.get("Permissions");
        for (Object permission : permissions) {
            if (!(permission instanceof String)) {
                return false;
            }
            String permissionString = (String) permission;
            if (!ConsentExtensionConstants.VALID_PERMISSIONS.contains(permissionString)) {
                return false;
            }
        }

        if (!data.containsKey("ExpirationDateTime") || !(data.get("ExpirationDateTime") instanceof String)) {
            return false;
        }

        if (!isConsentExpirationTimeValid(data.getAsString("ExpirationDateTime"))) {
            return false;
        }

        if (!data.containsKey("TransactionFromDateTime") || !(data.get("TransactionFromDateTime") instanceof String)) {
            return false;
        }

        if (!data.containsKey("TransactionToDateTime") || !(data.get("TransactionToDateTime") instanceof String)) {
            return false;
        }

        if (!isTransactionFromToTimeValid(data.getAsString("TransactionFromDateTime"),
                data.getAsString("TransactionToDateTime"))) {
            return false;
        }

        return true;
    }

    private static boolean isConsentExpirationTimeValid(String expDateVal) {

        if (expDateVal == null) {
            return true;
        }
        try {
            OffsetDateTime expDate = OffsetDateTime.parse(expDateVal);
            OffsetDateTime currDate = OffsetDateTime.now(expDate.getOffset());

            if (log.isDebugEnabled()) {
                log.debug("Provided expiry date is: " + expDate + " current date is: " + currDate);
            }

            return expDate.compareTo(currDate) > 0;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static boolean isTransactionFromToTimeValid(String fromDateVal, String toDateVal) {

        if (fromDateVal == null || toDateVal == null) {
            return true;
        }
        try {
            OffsetDateTime fromDate = OffsetDateTime.parse(fromDateVal);
            OffsetDateTime toDate = OffsetDateTime.parse(toDateVal);

            // From date is earlier than To date
            return (fromDate.compareTo(toDate) <= 0);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Method to append the consent expiration time (UTC) as a consent attribute.
     * @param requestedConsent  Consent Resource
     */
    public static void appendConsentExpirationTimestampAttribute(ConsentResource requestedConsent) {

        Map<String, String> consentAttributes = requestedConsent.getConsentAttributes();
        JSONObject receiptJSON = null;
        try {
            receiptJSON = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).
                    parse(requestedConsent.getReceipt());
            JSONObject data = null;
            if (receiptJSON.containsKey(ConsentExtensionConstants.DATA)) {
                data = (JSONObject) receiptJSON.get(ConsentExtensionConstants.DATA);
            }
            if (data != null && data.containsKey(ConsentMgtDAOConstants.CONSENT_EXPIRY_TIME_ATTRIBUTE)) {
                String expireTime = data.get(ConsentMgtDAOConstants.CONSENT_EXPIRY_TIME_ATTRIBUTE).toString();
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(expireTime);
                // Retrieve the UTC timestamp in long from expiry time.
                long expireTimestamp = Instant.from(zonedDateTime).getEpochSecond();
                if (consentAttributes == null) {
                    consentAttributes = new HashMap<String, String>();
                }
                consentAttributes.put(ConsentMgtDAOConstants.CONSENT_EXPIRY_TIME_ATTRIBUTE,
                        Long.toString(expireTimestamp));
                requestedConsent.setConsentAttributes(consentAttributes);
            }
        } catch (ParseException e) {
            log.error("Invalid consent receipt received to append expiration time. : "
                    + requestedConsent.getConsentID());
        }
    }

    private static String convertEpochDateTime(long epochTime) {

        int nanoOfSecond = 0;
        ZoneOffset offset = ZoneOffset.UTC;
        LocalDateTime ldt = LocalDateTime.ofEpochSecond(epochTime, nanoOfSecond, offset);
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").format(ldt);
    }
}
