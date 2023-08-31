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

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.sample.aggregator.identity.util.AggregatorConstants;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregator Consent Create Step.
 */
public class AggregatorConsentCreateStep implements ConsentPersistStep {

    private static final Log log = LogFactory.getLog(AggregatorConsentCreateStep.class);
    private static final String AUTHORISED_STATUS = "authorised";
    private static final String REJECTED_STATUS = "rejected";
    private static final String CREATED_STATUS = "created";
    private static final String AUTH_TYPE_AUTHORIZATION = "authorization";
    private static final String AWAITING_AUTH_STATUS = "awaitingAuthorisation";
    public static final String COMMON_AUTH_ID = AggregatorConstants.COMMON_AUTH_ID_TAG;

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

        try {
            ConsentData consentData = consentPersistData.getConsentData();
            ConsentCoreService consentCoreService = new ConsentCoreServiceImpl();
            String consentId = UUID.randomUUID().toString();
            consentData.setConsentId(consentId);
            ConsentResource consentResource = consentPersistData.getConsentData().getConsentResource();
            consentResource.setConsentID(consentId);
            consentResource.setConsentType(AggregatorConstants.ACCOUNT_CONSENT_TYPE);
            consentResource.setCurrentStatus(AWAITING_AUTH_STATUS);
            DetailedConsentResource createdConsent;
            // Add consent expiration timestamp to the it's attributes map
            appendConsentExpirationTimestampAttribute(consentResource);

            String commonAuthId = consentPersistData.getBrowserCookies().get(COMMON_AUTH_ID);
            Map<String, String> consentAttributes = consentResource.getConsentAttributes();
            consentAttributes.put(COMMON_AUTH_ID, commonAuthId);
            consentResource.setConsentAttributes(consentAttributes);
            try {
                createdConsent = consentCoreService.createAuthorizableConsent(consentResource, null,
                                CREATED_STATUS, AUTH_TYPE_AUTHORIZATION, true);
            } catch (ConsentManagementException e) {
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }

            if (consentData.getConsentResource() == null) {
                consentResource = consentCoreService.getConsent(consentData.getConsentId(), false);
            } else {
                consentResource = consentData.getConsentResource();
            }
            consentData.setAuthResource(createdConsent.getAuthorizationResources().get(0));
            if (consentData.getAuthResource() == null) {
                log.error("Auth resource not available in consent data");
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Auth resource not available in consent data");
            }

            JSONObject payload = consentPersistData.getPayload();

            if (payload.get("accountIds") == null || !(payload.get("accountIds") instanceof JSONArray)) {
                log.error("Account IDs not available in persist request");
                throw new ConsentException(ResponseStatus.BAD_REQUEST,
                        "Account IDs not available in persist request");
            }

            JSONArray accountIds = (JSONArray) payload.get("accountIds");
            ArrayList<String> accountIdsString = new ArrayList<>();
            for (Object account : accountIds) {
                if (!(account instanceof String)) {
                    log.error("Account IDs format error in persist request");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST,
                            "Account IDs format error in persist request");
                }
                accountIdsString.add((String) account);
            }
            String consentStatus;
            String authStatus;

            if (consentPersistData.getApproval()) {
                consentStatus = AUTHORISED_STATUS;
                authStatus = AUTHORISED_STATUS;
            } else {
                consentStatus = REJECTED_STATUS;
                authStatus = REJECTED_STATUS;
            }

            consentCoreService.bindUserAccountsToConsent(consentResource, consentData.getUserId(),
                            consentData.getAuthResource().getAuthorizationID(), accountIdsString, authStatus,
                            consentStatus);
        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Exception occured while persisting consent");
        }
    }

    /**
     * Method to append consent expiration timestamp as an attribute
     * @param requestedConsent Requested Consent as a Consent Resource
     */
    private void appendConsentExpirationTimestampAttribute(ConsentResource requestedConsent) {

        Map<String, String> consentAttributes = requestedConsent.getConsentAttributes();
        JSONObject receiptJSON = null;
        try {
            receiptJSON = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).
                    parse(requestedConsent.getReceipt());
            JSONObject data = null;
            if (receiptJSON.containsKey("Data")) {
                data = (JSONObject) receiptJSON.get("Data");
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
}

