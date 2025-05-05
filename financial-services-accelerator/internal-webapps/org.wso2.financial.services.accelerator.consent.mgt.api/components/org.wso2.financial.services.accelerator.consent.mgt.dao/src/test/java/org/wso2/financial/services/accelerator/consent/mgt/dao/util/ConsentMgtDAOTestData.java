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

package org.wso2.financial.services.accelerator.consent.mgt.dao.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentAttributes;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentFile;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Consent management DAO test data.
 */
public class ConsentMgtDAOTestData {

    public static final String SAMPLE_CONSENT_RECEIPT = "{\"validUntil\": \"2020-10-20\", \"frequencyPerDay\": 1," +
            " \"recurringIndicator\": false, \"combinedServiceIndicator\": true}";
    public static final String SAMPLE_CONSENT_TYPE = "accounts";
    public static final String SAMPLE_ORG_ID = "asgradeo";
    public static final int SAMPLE_CONSENT_FREQUENCY = 1;
    public static final Long SAMPLE_CONSENT_VALIDITY_PERIOD = 1638337852L;
    public static final String SAMPLE_CONSENT_ID = "2222";
    public static final String SAMPLE_AUTHORIZATION_ID = "3333";
    public static final boolean SAMPLE_RECURRING_INDICATOR = true;
    public static final String SAMPLE_CURRENT_STATUS = "authorised";
    public static final String SAMPLE_PREVIOUS_STATUS = "Received";
    public static final String SAMPLE_AUTHORIZATION_TYPE = "authorizationType";
    public static final String SAMPLE_USER_ID = "admin@wso2.com";
    public static final String SAMPLE_AUDIT_ID = "4321234";
    public static final String SAMPLE_NEW_USER_ID = "ann@gold.com";
    public static final String SAMPLE_AUTHORIZATION_STATUS = "awaitingAuthorization";
    public static final String SAMPLE_EXPIRED_STATUS = "Expired";
    public static final String SAMPLE_ACCOUNT_ID = "123456789";
    public static final String SAMPLE_MAPPING_ID = "12345";
    public static final String SAMPLE_MAPPING_ID_2 = "67890";
    public static final String SAMPLE_MAPPING_STATUS = "active";
    public static final String SAMPLE_NEW_MAPPING_STATUS = "inactive";
    public static final String SAMPLE_PERMISSION = "samplePermission";

    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final JSONObject SAMPLE_RESOURCE;

    static {
        try {
            SAMPLE_RESOURCE = new JSONObject(new JSONObject(objectMapper.readValue(
                    "{\"resourceId\": \"1234\", \"resourceType\": \"account\"}"
                    , new TypeReference<Map<String, Object>>() {
                    })));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String SAMPLE_REASON = "sample reason";
    public static final String SAMPLE_ACTION_BY = "admin@wso2.com";
    public static final String SAMPLE_HISTORY_ID = "1234";
    public static final Long SAMPLE_UPDATED_TIME = 1638337892L;
    public static final String SAMPLE_AMENDMENT_REASON = "sampleReason";
    public static final String SAMPLE_CONSENT_FILE = "sample file content";
    public static final Map<String, String> SAMPLE_CONSENT_ATTRIBUTES_MAP = new HashMap<String, String>() {
        {
            put("x-request-id", UUID.randomUUID().toString());
            put("idempotency-key", UUID.randomUUID().toString());
            put("payment-type", "domestic-payments");
            put("sessionDataKey", "{\"sessionDataKey\":\"a0c8cd6d-eca0-4c4d-9544-2b39e7e1c180\",\"userId\":\"01Z79\"}");
        }
    };
    public static final Map<String, String> CONSENT_ATTRIBUTES_MAP_FOR_UPDATE = new HashMap<String, String>() {
        {
            put("payment-type", "international-payments");
        }
    };
    public static final ArrayList<String> SAMPLE_CONSENT_ATTRIBUTES_KEYS = new ArrayList<String>() {
        {
            add("x-request-id");
            add("idempotency-key");
        }
    };
    public static final ArrayList<String> UNMATCHED_MAPPING_IDS = new ArrayList<String>() {
        {
            add("4444");
            add("5555");
        }
    };
    private static final ArrayList<String> SAMPLE_CONSENT_RECEIPTS_LIST = new ArrayList<String>() {
        {
            add("{\"element1\": \"value1\"}");
            add("{\"element2\": \"value2\"}");
            add("{\"element3\": \"value3\"}");
        }
    };
    public static final ArrayList<String> SAMPLE_CONSENT_TYPES_LIST = new ArrayList<String>() {
        {
            add("accounts");
            add("payments");
            add("cof");
        }
    };
    public static final ArrayList<String> SAMPLE_CONSENT_STATUSES_LIST = new ArrayList<String>() {
        {
            add("created");
            add("authorised");
            add("awaitingAuthorisation");

        }
    };
    public static final ArrayList<String> SAMPLE_CLIENT_IDS_LIST = new ArrayList<String>() {
        {
            add("clientID1");
            add("clientID2");
            add("clientID3");

        }
    };
    public static final ArrayList<String> SAMPLE_USER_IDS_LIST = new ArrayList<String>() {
        {
            add("userID1");
            add("userID2");
            add("userID3");
        }
    };
    private static final ArrayList<Long> SAMPLE_VALIDITY_PERIOD_LIST = new ArrayList<Long>() {
        {
            add(1613454661L);
            add(1623654661L);
            add(1633654671L);
        }
    };
    public static final JSONObject SAMPLE_CONSENT_BASIC_DATA_CHANGED_ATTRIBUTES_JSON = new JSONObject() {
        {
            put("RECEIPT", SAMPLE_CONSENT_RECEIPT);
            put("EXPIRY_TIME", SAMPLE_CONSENT_VALIDITY_PERIOD);
            put("UPDATED_TIME", SAMPLE_UPDATED_TIME);
        }
    };
    public static final JSONObject SAMPLE_CONSENT_ATTRIBUTES_CHANGED_ATTRIBUTES_JSON = new JSONObject() {
        {
            put("x-request-id", UUID.randomUUID().toString());
            put("idempotency-key", UUID.randomUUID().toString());
        }
    };
    public static final JSONObject SAMPLE_CONSENT_MAPPINGS_CHANGED_ATTRIBUTES_JSON = new JSONObject() {
        {
            put("MAPPING_STATUS", SAMPLE_MAPPING_STATUS);
        }
    };

    public static List<String> getRecordIDListOfSampleConsentHistory() {
        return new ArrayList<String>() {
            {
                add(ConsentMgtDAOTestData.SAMPLE_CONSENT_ID);
                add(ConsentMgtDAOTestData.SAMPLE_MAPPING_ID);
                add(ConsentMgtDAOTestData.SAMPLE_MAPPING_ID_2);
                add(ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID);
            }
        };
    }

    public static ConsentResource getSampleTestConsentResource() {

        ConsentResource consentResource = new ConsentResource();
        consentResource.setOrgID(ConsentMgtDAOConstants.DEFAULT_ORG);
        consentResource.setReceipt(ConsentMgtDAOTestData.SAMPLE_CONSENT_RECEIPT);
        consentResource.setClientID(UUID.randomUUID().toString());
        consentResource.setConsentType(ConsentMgtDAOTestData.SAMPLE_CONSENT_TYPE);
        consentResource.setCurrentStatus(ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);
        consentResource.setExpiryTime(ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD);
        consentResource.setRecurringIndicator(ConsentMgtDAOTestData.SAMPLE_RECURRING_INDICATOR);

        return consentResource;
    }

    public static ConsentResource getSampleStoredTestConsentResource() {

        ConsentResource consentResource = new ConsentResource();
        consentResource.setConsentID(UUID.randomUUID().toString());
        consentResource.setReceipt(ConsentMgtDAOTestData.SAMPLE_CONSENT_RECEIPT);
        consentResource.setClientID(UUID.randomUUID().toString());
        consentResource.setConsentType(ConsentMgtDAOTestData.SAMPLE_CONSENT_TYPE);
        consentResource.setCurrentStatus(ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);
        consentResource.setExpiryTime(ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD);
        consentResource.setRecurringIndicator(ConsentMgtDAOTestData.SAMPLE_RECURRING_INDICATOR);

        return consentResource;
    }

    /**
     * Generated three sample consent resources for testing purposes.
     *
     * @return sample consent resources list
     */
    public static ArrayList<ConsentResource> getSampleConsentResourcesList() {

        ArrayList<ConsentResource> consentResources = new ArrayList<ConsentResource>();

        for (int i = 0; i < 3; i++) {
            ConsentResource consentResource = new ConsentResource();
            consentResource.setReceipt(SAMPLE_CONSENT_RECEIPTS_LIST.get(i));
            consentResource.setClientID(SAMPLE_CLIENT_IDS_LIST.get(i));
            consentResource.setConsentType(SAMPLE_CONSENT_TYPES_LIST.get(i));
            consentResource.setCurrentStatus(SAMPLE_CONSENT_STATUSES_LIST.get(i));
            consentResource.setExpiryTime(SAMPLE_VALIDITY_PERIOD_LIST.get(i));
            consentResource.setRecurringIndicator(false);
            consentResources.add(consentResource);
        }
        return consentResources;
    }

    public static ArrayList<AuthorizationResource> getSampleAuthorizationResourcesList(ArrayList<String> consentIDs) {

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<AuthorizationResource>();

        for (int i = 0; i < consentIDs.size(); i++) {
            for (int j = 0; j < 2; j++) {
                AuthorizationResource authorizationResource = new AuthorizationResource();
                authorizationResource.setConsentID(consentIDs.get(i));
                authorizationResource.setAuthorizationType(SAMPLE_AUTHORIZATION_TYPE);
                authorizationResource.setUserID(SAMPLE_USER_IDS_LIST.get(i));
                authorizationResource.setAuthorizationStatus(SAMPLE_AUTHORIZATION_STATUS);
                authorizationResource.setResource(SAMPLE_RESOURCE.toString());
                authorizationResources.add(authorizationResource);
            }
        }
        return authorizationResources;
    }

    public static ArrayList<ConsentMappingResource> getSampleConsentMappingResourcesList(ArrayList<String> authIDs) {

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<ConsentMappingResource>();

        for (int i = 0; i < authIDs.size(); i++) {
            for (int j = 0; j < 2; j++) {
                ConsentMappingResource consentMappingResource = new ConsentMappingResource();
                consentMappingResource.setAuthorizationID(authIDs.get(i));
                consentMappingResource.setAccountID(SAMPLE_ACCOUNT_ID);
                consentMappingResource.setResource(SAMPLE_RESOURCE);
                consentMappingResource.setMappingStatus(SAMPLE_MAPPING_STATUS);
                consentMappingResources.add(consentMappingResource);
            }
        }
        return consentMappingResources;
    }

    public static AuthorizationResource getSampleTestAuthorizationResource(String consentID) {

        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setConsentID(consentID);
        authorizationResource.setAuthorizationType(ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_TYPE);
        authorizationResource.setUserID(ConsentMgtDAOTestData.SAMPLE_USER_ID);
        authorizationResource.setAuthorizationStatus(ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_STATUS);

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<ConsentMappingResource>();
        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setAuthorizationID(consentID);
        consentMappingResource.setResource(ConsentMgtDAOTestData.SAMPLE_RESOURCE);
        consentMappingResource.setMappingStatus(ConsentMgtDAOTestData.SAMPLE_MAPPING_STATUS);
        consentMappingResources.add(consentMappingResource);


        return authorizationResource;
    }

    public static AuthorizationResource getSampleStoredTestAuthorizationResource() {

        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setConsentID(UUID.randomUUID().toString());
        authorizationResource.setAuthorizationID(UUID.randomUUID().toString());
        authorizationResource.setAuthorizationType(ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_TYPE);
        authorizationResource.setUserID(ConsentMgtDAOTestData.SAMPLE_USER_ID);
        authorizationResource.setAuthorizationStatus(ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_STATUS);
        authorizationResource.setUpdatedTime(System.currentTimeMillis() / 1000);

        return authorizationResource;
    }

    public static ConsentMappingResource getSampleTestConsentMappingResource(String authorizationID) {

        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setAuthorizationID(authorizationID);
        consentMappingResource.setResource(ConsentMgtDAOTestData.SAMPLE_RESOURCE);
        consentMappingResource.setMappingStatus(ConsentMgtDAOTestData.SAMPLE_MAPPING_STATUS);

        return consentMappingResource;
    }

    public static ConsentMappingResource getSampleTestConsentMappingResourceWithResource(String authorizationID,
                                                                                         String accountID) {

        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setAuthorizationID(authorizationID);
        consentMappingResource.setResource(ConsentMgtDAOTestData.SAMPLE_RESOURCE);
        consentMappingResource.setMappingStatus(ConsentMgtDAOTestData.SAMPLE_MAPPING_STATUS);

        return consentMappingResource;
    }

    public static ConsentStatusAuditRecord getSampleTestConsentStatusAuditRecord(String consentID,
                                                                                 String currentStatus) {

        ConsentStatusAuditRecord consentStatusAuditRecord = new ConsentStatusAuditRecord();
        consentStatusAuditRecord.setConsentID(consentID);
        consentStatusAuditRecord.setCurrentStatus(currentStatus);
        consentStatusAuditRecord.setReason(ConsentMgtDAOTestData.SAMPLE_REASON);
        consentStatusAuditRecord.setActionBy(ConsentMgtDAOTestData.SAMPLE_ACTION_BY);
        consentStatusAuditRecord.setPreviousStatus(ConsentMgtDAOTestData.SAMPLE_PREVIOUS_STATUS);

        return consentStatusAuditRecord;
    }

    public static ConsentAttributes getSampleTestConsentAttributesObject(String consentID) {

        ConsentAttributes consentAttributes = new ConsentAttributes();
        consentAttributes.setConsentID(consentID);
        consentAttributes.setConsentAttributes(ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);

        return consentAttributes;
    }

    public static ConsentFile getSampleConsentFileObject(String fileContent) {

        ConsentFile consentFile = new ConsentFile();
        consentFile.setConsentID(UUID.randomUUID().toString());
        consentFile.setConsentFile(fileContent);

        return consentFile;
    }
}
