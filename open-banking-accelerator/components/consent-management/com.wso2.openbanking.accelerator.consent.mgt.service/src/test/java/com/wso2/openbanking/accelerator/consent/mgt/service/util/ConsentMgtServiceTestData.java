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

package com.wso2.openbanking.accelerator.consent.mgt.service.util;

import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentAttributes;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentFile;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;
import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Test data fore consent management service.
 */
public class ConsentMgtServiceTestData {

    public static final String SAMPLE_CONSENT_RECEIPT = "{\"validUntil\": \"2020-10-20\", \"frequencyPerDay\": 1," +
            " \"recurringIndicator\": false, \"combinedServiceIndicator\": true}";

    public static final String SAMPLE_CONSENT_TYPE = "accounts";

    public static final String SAMPLE_CLIENT_ID = "sampleClientID";

    public static final int SAMPLE_CONSENT_FREQUENCY = 1;

    public static final Long SAMPLE_CONSENT_VALIDITY_PERIOD = 1638337852L;

    public static final long SAMPLE_CONSENT_AMENDMENT_TIMESTAMP = 1638337852;

    public static final String UNMATCHED_CONSENT_ID = "2222";

    public static final String UNMATCHED_AUTHORIZATION_ID = "3333";

    public static final String SAMPLE_MAPPING_ID = "sampleMappingId";

    public static final String SAMPLE_MAPPING_ID_2 = "sampleMappingId2";

    public static final String SAMPLE_AUTHORIZATION_ID_1 = "88888";

    public static final String SAMPLE_AUTHORIZATION_ID_2 = "99999";

    public static final boolean SAMPLE_RECURRING_INDICATOR = true;

    public static final String SAMPLE_CURRENT_STATUS = "Authorized";

    public static final String AWAITING_UPLOAD_STATUS = "awaitingUpload";

    public static final String  SAMPLE_PREVIOUS_STATUS = "Received";

    public static final String SAMPLE_AUTHORIZATION_TYPE = "authorizationType";

    public static final String CONSENT_ID = "464ef174-9877-4c71-940c-93d6e069eaf9";

    public static final String SAMPLE_USER_ID = "admin@wso2.com";

    public static final String SAMPLE_AUDIT_ID = "4321234";

    public static final String SAMPLE_NEW_USER_ID = "ann@gold.com";

    public static final String SAMPLE_AUTHORIZATION_STATUS = "awaitingAuthorization";

    public static final String SAMPLE_ACCOUNT_ID = "123456789";

    public static final String SAMPLE_MAPPING_STATUS = "active";

    public static final String SAMPLE_NEW_MAPPING_STATUS = "inactive";

    public static final String SAMPLE_PERMISSION = "samplePermission";

    public static final String SAMPLE_REASON = "sample reason";

    public static final String SAMPLE_ACTION_BY = "admin@wso2.com";

    public static final String SAMPLE_CONSUMED_STATUS = "Consumed";

    public static final String SAMPLE_AMENDMENT_REASON = "sampleReason";

    public static final String SAMPLE_CONSENT_HISTORY_RECEIPT = "{\"validUntil\": \"2020-10-20\", " +
            "\"frequencyPerDay\": 5, \"recurringIndicator\": true, \"combinedServiceIndicator\": true}";

    public static final Long SAMPLE_CONSENT_HISTORY_VALIDITY_PERIOD = 1538337852L;

    public static final long SAMPLE_CONSENT_HISTORY_AMENDMENT_TIMESTAMP = 1538337852;

    public static final String SAMPLE_HISTORY_ID = "sampleHistoryID";

    public static final Map<String, String> SAMPLE_CONSENT_ATTRIBUTES_MAP = new HashMap<String, String>() {
        {
            put("x-request-id", UUID.randomUUID().toString());
            put("idenpotency-key", UUID.randomUUID().toString());
            put("sampleAttributeKey", "sampleAttributeValue");

        }
    };

    public static final Map<String, String> SAMPLE_CONSENT_HISTORY_ATTRIBUTES_MAP = new HashMap<String, String>() {
        {
            put("sampleAttributeKey", "sampleAttributeValue");
            put("sampleAttributeKey2", "sampleAttributeValue2");
            put("idenpotency-key", UUID.randomUUID().toString());
        }
    };

    public static final Map<String, ArrayList<String>> SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP = new HashMap<String,
            ArrayList<String>>() {
        {
            put("accountID1", new ArrayList<String>() {
                {
                    add("permission1");
                    add("permission2");
                }
            });
            put("accountID2", new ArrayList<String>() {
                {
                    add("permission3");
                    add("permission4");
                }
            });
        }
    };

    public static final Map<String, ArrayList<String>> SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP2 = new HashMap<String,
            ArrayList<String>>() {
        {
            put(SAMPLE_ACCOUNT_ID, new ArrayList<String>() {
                {
                    add("permission5");
                    add("permission6");
                }
            });
        }
    };

    public static final Map<String, ArrayList<String>> SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP3 = new HashMap<String,
            ArrayList<String>>() {
        {
            put("mismatching account ID", new ArrayList<String>() {
                {
                    add("permission5");
                    add("permission6");
                }
            });
        }
    };

    public static final ArrayList<String> SAMPLE_CONSENT_ATTRIBUTES_KEYS = new ArrayList<String>() {
        {
            add("x-request-id");
            add("idenpotency-key");
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
            add("authorized");
            add("awaitingAuthorization");

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

    public static final ArrayList<String> SAMPLE_ACCOUNT_ID_LIST = new ArrayList<String>() {
        {
            add(SAMPLE_ACCOUNT_ID);
        }
    };

    public static final ArrayList<String> SAMPLE_CONSENT_IS_ARRAY = new ArrayList<String>() {
        {
            add(CONSENT_ID);
        }
    };

    public static final String SAMPLE_CONSENT_FILE = "sample file content";

    public static ConsentResource getSampleTestConsentResource() {

        ConsentResource consentResource = new ConsentResource();
        consentResource.setReceipt(ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT);
        consentResource.setClientID(UUID.randomUUID().toString());
        consentResource.setConsentType(ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE);
        consentResource.setCurrentStatus(ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
        consentResource.setConsentFrequency(ConsentMgtServiceTestData.SAMPLE_CONSENT_FREQUENCY);
        consentResource.setValidityPeriod(ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD);
        consentResource.setRecurringIndicator(ConsentMgtServiceTestData.SAMPLE_RECURRING_INDICATOR);

        return consentResource;
    }

    public static ConsentResource getSampleStoredTestConsentResource() {

        ConsentResource consentResource = new ConsentResource();
        consentResource.setConsentID(UUID.randomUUID().toString());
        consentResource.setReceipt(ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT);
        consentResource.setClientID(UUID.randomUUID().toString());
        consentResource.setConsentType(ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE);
        consentResource.setCurrentStatus(ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
        consentResource.setConsentFrequency(ConsentMgtServiceTestData.SAMPLE_CONSENT_FREQUENCY);
        consentResource.setValidityPeriod(ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD);
        consentResource.setRecurringIndicator(ConsentMgtServiceTestData.SAMPLE_RECURRING_INDICATOR);

        return consentResource;
    }

    public static ArrayList<AuthorizationResource> getSampleAuthorizationResourcesList(ArrayList<String> consentIDs) {

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();

        for (int i = 0; i < consentIDs.size(); i++) {
            for (int j = 0; j < 2; j++) {
                AuthorizationResource authorizationResource = new AuthorizationResource();
                authorizationResource.setConsentID(consentIDs.get(i));
                authorizationResource.setAuthorizationType(SAMPLE_AUTHORIZATION_TYPE);
                authorizationResource.setUserID(SAMPLE_USER_IDS_LIST.get(i));
                authorizationResource.setAuthorizationStatus(SAMPLE_AUTHORIZATION_STATUS);
                authorizationResources.add(authorizationResource);
            }
        }
        return authorizationResources;
    }

    public static ArrayList<ConsentMappingResource> getSampleConsentMappingResourcesList(ArrayList<String> authIDs) {

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();

        for (int i = 0; i < authIDs.size(); i++) {
            for (int j = 0; j < 2; j++) {
                ConsentMappingResource consentMappingResource = new ConsentMappingResource();
                consentMappingResource.setAuthorizationID(authIDs.get(i));
                consentMappingResource.setAccountID(SAMPLE_ACCOUNT_ID);
                consentMappingResource.setPermission(SAMPLE_PERMISSION);
                consentMappingResource.setMappingStatus(SAMPLE_MAPPING_STATUS);
                consentMappingResources.add(consentMappingResource);
            }
        }
        return consentMappingResources;
    }

    public static DetailedConsentResource getSampleDetailedStoredTestConsentResource() {

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        detailedConsentResource.setReceipt(ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT);
        detailedConsentResource.setClientID(UUID.randomUUID().toString());
        detailedConsentResource.setConsentType(ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE);
        detailedConsentResource.setCurrentStatus(ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
        detailedConsentResource.setConsentFrequency(ConsentMgtServiceTestData.SAMPLE_CONSENT_FREQUENCY);
        detailedConsentResource.setValidityPeriod(ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD);
        detailedConsentResource.setRecurringIndicator(ConsentMgtServiceTestData.SAMPLE_RECURRING_INDICATOR);
        detailedConsentResource.setCreatedTime(System.currentTimeMillis() / 1000);
        detailedConsentResource.setConsentAttributes(ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(ConsentMgtServiceTestData
                .getSampleTestAuthorizationResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1));

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        consentMappingResources.add(ConsentMgtServiceTestData
                .getSampleTestConsentMappingResource(ConsentMgtServiceTestData
                        .getSampleStoredTestAuthorizationResource().getAuthorizationID()));
        consentMappingResources.add(ConsentMgtServiceTestData
                .getSampleTestInactiveConsentMappingResource(ConsentMgtServiceTestData
                        .getSampleStoredTestAuthorizationResource().getAuthorizationID()));

        detailedConsentResource.setAuthorizationResources(authorizationResources);
        detailedConsentResource.setConsentMappingResources(consentMappingResources);

        return detailedConsentResource;
    }

    public static DetailedConsentResource getSampleDetailedStoredTestConsentResourceWithMultipleAccountIDs() {

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        detailedConsentResource.setReceipt(ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT);
        detailedConsentResource.setClientID(UUID.randomUUID().toString());
        detailedConsentResource.setConsentType(ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE);
        detailedConsentResource.setCurrentStatus(ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
        detailedConsentResource.setConsentFrequency(ConsentMgtServiceTestData.SAMPLE_CONSENT_FREQUENCY);
        detailedConsentResource.setValidityPeriod(ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD);
        detailedConsentResource.setRecurringIndicator(ConsentMgtServiceTestData.SAMPLE_RECURRING_INDICATOR);
        detailedConsentResource.setCreatedTime(System.currentTimeMillis() / 1000);
        detailedConsentResource.setConsentAttributes(ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(ConsentMgtServiceTestData
                .getSampleStoredTestAuthorizationResource());

        ConsentMappingResource mappingResource1 = new ConsentMappingResource();
        mappingResource1.setAccountID(UUID.randomUUID().toString());

        ConsentMappingResource mappingResource2 = new ConsentMappingResource();
        mappingResource2.setAccountID(UUID.randomUUID().toString());

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        consentMappingResources.add(mappingResource1);
        consentMappingResources.add(mappingResource2);

        detailedConsentResource.setAuthorizationResources(authorizationResources);
        detailedConsentResource.setConsentMappingResources(consentMappingResources);

        return detailedConsentResource;
    }

    public static ArrayList<DetailedConsentResource> getSampleDetailedStoredTestConsentResourcesList() {

        ArrayList<DetailedConsentResource> detailedConsentResourcesList = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
            detailedConsentResource.setConsentID(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
            detailedConsentResource.setReceipt(ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT);
            detailedConsentResource.setClientID(UUID.randomUUID().toString());
            detailedConsentResource.setConsentType(ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE);
            detailedConsentResource.setCurrentStatus(ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
            detailedConsentResource.setConsentFrequency(ConsentMgtServiceTestData.SAMPLE_CONSENT_FREQUENCY);
            detailedConsentResource.setValidityPeriod(ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD);
            detailedConsentResource.setRecurringIndicator(ConsentMgtServiceTestData.SAMPLE_RECURRING_INDICATOR);
            detailedConsentResource.setCreatedTime(System.currentTimeMillis() / 1000);
            detailedConsentResource.setConsentAttributes(ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);

            ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
            authorizationResources.add(ConsentMgtServiceTestData
                    .getSampleStoredTestAuthorizationResource());

            ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
            consentMappingResources.add(ConsentMgtServiceTestData
                    .getSampleTestConsentMappingResource(ConsentMgtServiceTestData
                    .getSampleStoredTestAuthorizationResource().getAuthorizationID()));

            detailedConsentResource.setAuthorizationResources(authorizationResources);
            detailedConsentResource.setConsentMappingResources(consentMappingResources);
            detailedConsentResourcesList.add(detailedConsentResource);
        }
        return detailedConsentResourcesList;
    }

    public static ConsentResource getSampleStoredTestConsentResourceWithAttributes() {

        ConsentResource consentResource = new ConsentResource();
        consentResource.setConsentID(UUID.randomUUID().toString());
        consentResource.setReceipt(ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT);
        consentResource.setClientID(UUID.randomUUID().toString());
        consentResource.setConsentType(ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE);
        consentResource.setCurrentStatus(ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
        consentResource.setConsentFrequency(ConsentMgtServiceTestData.SAMPLE_CONSENT_FREQUENCY);
        consentResource.setValidityPeriod(ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD);
        consentResource.setRecurringIndicator(ConsentMgtServiceTestData.SAMPLE_RECURRING_INDICATOR);
        consentResource.setConsentAttributes(ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);

        return consentResource;
    }

    public static AuthorizationResource getSampleTestAuthorizationResource(String consentID) {

        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setConsentID(consentID);
        authorizationResource.setAuthorizationType(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE);
        authorizationResource.setUserID(ConsentMgtServiceTestData.SAMPLE_USER_ID);
        authorizationResource.setAuthorizationStatus(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);

        return authorizationResource;
    }

    public static AuthorizationResource getSampleTestAuthorizationResource(String consentID, String authorizationId) {

        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setConsentID(consentID);
        authorizationResource.setAuthorizationID(authorizationId);
        authorizationResource.setAuthorizationType(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE);
        authorizationResource.setUserID(ConsentMgtServiceTestData.SAMPLE_USER_ID);
        authorizationResource.setAuthorizationStatus(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);

        return authorizationResource;
    }

    public static AuthorizationResource getSampleStoredTestAuthorizationResource() {

        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setConsentID(UUID.randomUUID().toString());
        authorizationResource.setAuthorizationID(UUID.randomUUID().toString());
        authorizationResource.setAuthorizationType(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE);
        authorizationResource.setUserID(ConsentMgtServiceTestData.SAMPLE_USER_ID);
        authorizationResource.setAuthorizationStatus(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
        authorizationResource.setUpdatedTime(System.currentTimeMillis() / 1000);

        return authorizationResource;
    }

    public static ConsentMappingResource getSampleTestConsentMappingResource(String authorizationID) {

        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setMappingID(ConsentMgtServiceTestData.SAMPLE_MAPPING_ID);
        consentMappingResource.setAuthorizationID(authorizationID);
        consentMappingResource.setAccountID(ConsentMgtServiceTestData.SAMPLE_ACCOUNT_ID);
        consentMappingResource.setPermission(ConsentMgtServiceTestData.SAMPLE_PERMISSION);
        consentMappingResource.setMappingStatus(ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);

        return consentMappingResource;
    }

    public static ConsentMappingResource getSampleTestInactiveConsentMappingResource(String authorizationID) {

        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setMappingID(ConsentMgtServiceTestData.SAMPLE_MAPPING_ID_2);
        consentMappingResource.setAuthorizationID(authorizationID);
        consentMappingResource.setAccountID(ConsentMgtServiceTestData.SAMPLE_ACCOUNT_ID);
        consentMappingResource.setPermission(ConsentMgtServiceTestData.SAMPLE_PERMISSION);
        consentMappingResource.setMappingStatus(ConsentMgtServiceTestData.SAMPLE_NEW_MAPPING_STATUS);

        return consentMappingResource;
    }

    public static ConsentMappingResource getSampleTestConsentHistoryMappingResource(String authorizationID,
                                                                                    String mappingId) {

        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setMappingID(mappingId);
        consentMappingResource.setAuthorizationID(authorizationID);
        consentMappingResource.setAccountID(ConsentMgtServiceTestData.SAMPLE_ACCOUNT_ID);
        consentMappingResource.setPermission(ConsentMgtServiceTestData.SAMPLE_PERMISSION);
        consentMappingResource.setMappingStatus(ConsentMgtServiceTestData.SAMPLE_NEW_MAPPING_STATUS);

        return consentMappingResource;
    }

    public static ConsentMappingResource getSampleStoredTestConsentMappingResource(String authorizationID) {

        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setMappingID(UUID.randomUUID().toString());
        consentMappingResource.setAuthorizationID(authorizationID);
        consentMappingResource.setAccountID(ConsentMgtServiceTestData.SAMPLE_ACCOUNT_ID);
        consentMappingResource.setPermission(ConsentMgtServiceTestData.SAMPLE_PERMISSION);
        consentMappingResource.setMappingStatus(ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);

        return consentMappingResource;
    }

    public static ConsentStatusAuditRecord getSampleTestConsentStatusAuditRecord(String consentID,
                                                                                 String currentStatus) {

        ConsentStatusAuditRecord consentStatusAuditRecord = new ConsentStatusAuditRecord();
        consentStatusAuditRecord.setConsentID(consentID);
        consentStatusAuditRecord.setCurrentStatus(currentStatus);
        consentStatusAuditRecord.setReason(ConsentMgtServiceTestData.SAMPLE_REASON);
        consentStatusAuditRecord.setActionBy(ConsentMgtServiceTestData.SAMPLE_ACTION_BY);
        consentStatusAuditRecord.setPreviousStatus(ConsentMgtServiceTestData.SAMPLE_PREVIOUS_STATUS);

        return consentStatusAuditRecord;
    }

    public static ConsentStatusAuditRecord getSampleStoredTestConsentStatusAuditRecord(String sampleID,
                                                                                   String currentStatus) {

        ConsentStatusAuditRecord consentStatusAuditRecord = new ConsentStatusAuditRecord();
        consentStatusAuditRecord.setConsentID(sampleID);
        consentStatusAuditRecord.setStatusAuditID(sampleID);
        consentStatusAuditRecord.setCurrentStatus(currentStatus);
        consentStatusAuditRecord.setReason(ConsentMgtServiceTestData.SAMPLE_REASON);
        consentStatusAuditRecord.setActionBy(ConsentMgtServiceTestData.SAMPLE_ACTION_BY);
        consentStatusAuditRecord.setPreviousStatus(ConsentMgtServiceTestData.SAMPLE_PREVIOUS_STATUS);
        consentStatusAuditRecord.setActionTime(System.currentTimeMillis() / 1000);

        return consentStatusAuditRecord;
    }

    public static ArrayList<ConsentStatusAuditRecord> getSampleStoredTestConsentStatusAuditRecordsList(String sampleID,
                                                                                            String currentStatus) {

        ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            ConsentStatusAuditRecord consentStatusAuditRecord = new ConsentStatusAuditRecord();
            consentStatusAuditRecord.setConsentID(sampleID);
            consentStatusAuditRecord.setStatusAuditID(sampleID);
            consentStatusAuditRecord.setCurrentStatus(currentStatus);
            consentStatusAuditRecord.setReason(ConsentMgtServiceTestData.SAMPLE_REASON);
            consentStatusAuditRecord.setActionBy(ConsentMgtServiceTestData.SAMPLE_ACTION_BY);
            consentStatusAuditRecord.setPreviousStatus(ConsentMgtServiceTestData.SAMPLE_PREVIOUS_STATUS);
            consentStatusAuditRecord.setActionTime(System.currentTimeMillis() / 1000);
        }
        return consentStatusAuditRecords;
    }

    public static ConsentAttributes getSampleTestConsentAttributesObject(String consentID) {

        ConsentAttributes consentAttributes = new ConsentAttributes();
        consentAttributes.setConsentID(consentID);
        consentAttributes.setConsentAttributes(ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);

        return consentAttributes;
    }

    public static ConsentFile getSampleConsentFileObject(String fileContent) {

        ConsentFile consentFile = new ConsentFile();
        consentFile.setConsentID(UUID.randomUUID().toString());
        consentFile.setConsentFile(fileContent);

        return consentFile;
    }

    public static ConsentHistoryResource getSampleTestConsentHistoryResource() {

        ConsentHistoryResource consentHistoryResource = new ConsentHistoryResource();
        consentHistoryResource.setTimestamp(ConsentMgtServiceTestData.SAMPLE_CONSENT_AMENDMENT_TIMESTAMP);
        consentHistoryResource.setReason(ConsentMgtServiceTestData.SAMPLE_AMENDMENT_REASON);
        consentHistoryResource.setDetailedConsentResource(getSampleDetailedConsentHistoryResource());

        return consentHistoryResource;
    }

    public static DetailedConsentResource getSampleDetailedConsentHistoryResource() {

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID(ConsentMgtServiceTestData.CONSENT_ID);
        detailedConsentResource.setReceipt(ConsentMgtServiceTestData.SAMPLE_CONSENT_HISTORY_RECEIPT);
        detailedConsentResource.setClientID(UUID.randomUUID().toString());
        detailedConsentResource.setConsentType(ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE);
        detailedConsentResource.setCurrentStatus(ConsentMgtServiceTestData.SAMPLE_PREVIOUS_STATUS);
        detailedConsentResource.setConsentFrequency(ConsentMgtServiceTestData.SAMPLE_CONSENT_FREQUENCY);
        detailedConsentResource.setValidityPeriod(ConsentMgtServiceTestData.SAMPLE_CONSENT_HISTORY_VALIDITY_PERIOD);
        detailedConsentResource.setUpdatedTime(ConsentMgtServiceTestData.SAMPLE_CONSENT_HISTORY_AMENDMENT_TIMESTAMP);
        detailedConsentResource.setRecurringIndicator(ConsentMgtServiceTestData.SAMPLE_RECURRING_INDICATOR);
        detailedConsentResource.setCreatedTime(System.currentTimeMillis() / 1000);
        detailedConsentResource.setConsentAttributes(ConsentMgtServiceTestData.SAMPLE_CONSENT_HISTORY_ATTRIBUTES_MAP);

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(
                ConsentMgtServiceTestData.CONSENT_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1));

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        consentMappingResources.add(ConsentMgtServiceTestData
                .getSampleTestConsentHistoryMappingResource(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                        ConsentMgtServiceTestData.SAMPLE_MAPPING_ID));

        detailedConsentResource.setAuthorizationResources(authorizationResources);
        detailedConsentResource.setConsentMappingResources(consentMappingResources);

        return detailedConsentResource;
    }

    public static DetailedConsentResource getSampleDetailedStoredTestCurrentConsentResource() {

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID(ConsentMgtServiceTestData.CONSENT_ID);
        detailedConsentResource.setReceipt(ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT);
        detailedConsentResource.setClientID(UUID.randomUUID().toString());
        detailedConsentResource.setConsentType(ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE);
        detailedConsentResource.setCurrentStatus(ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
        detailedConsentResource.setConsentFrequency(ConsentMgtServiceTestData.SAMPLE_CONSENT_FREQUENCY);
        detailedConsentResource.setValidityPeriod(ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD);
        detailedConsentResource.setUpdatedTime(ConsentMgtServiceTestData.SAMPLE_CONSENT_AMENDMENT_TIMESTAMP);
        detailedConsentResource.setRecurringIndicator(ConsentMgtServiceTestData.SAMPLE_RECURRING_INDICATOR);
        detailedConsentResource.setCreatedTime(System.currentTimeMillis() / 1000);
        detailedConsentResource.setConsentAttributes(ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(ConsentMgtServiceTestData
            .getSampleTestAuthorizationResource(ConsentMgtServiceTestData.CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1));
        authorizationResources.add(ConsentMgtServiceTestData
                .getSampleTestAuthorizationResource(ConsentMgtServiceTestData.CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_2));

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        consentMappingResources.add(ConsentMgtServiceTestData
                .getSampleTestConsentHistoryMappingResource(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                        ConsentMgtServiceTestData.SAMPLE_MAPPING_ID));

        // new mapping that is not included in the previous state of the consent
        consentMappingResources.add(ConsentMgtServiceTestData.getSampleTestConsentHistoryMappingResource(
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1, ConsentMgtServiceTestData.SAMPLE_MAPPING_ID_2));

        detailedConsentResource.setAuthorizationResources(authorizationResources);
        detailedConsentResource.setConsentMappingResources(consentMappingResources);

        return detailedConsentResource;
    }

    public static Map<String, ConsentHistoryResource> getSampleDetailedStoredTestConsentHistoryDataMap() {

        Map<String, ConsentHistoryResource> consentAmendmentHistoryDataMap = new LinkedHashMap<>();

        Map<String, Object> changedAttributesJson = new HashMap<>();
        changedAttributesJson.put("ConsentData", getBasicConsentDataChangedAttributesJson());
        changedAttributesJson.put("ConsentAttributesData", getConsentAttributesDataChangedAttributesJson());

        Map<String, Object> consentAuthResources = new HashMap<>();
        consentAuthResources.put(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1, "null");
        changedAttributesJson.put("ConsentAuthResourceData", consentAuthResources);

        Map<String, Object> consentMappingResources = new HashMap<>();
        JSONObject consentMappingDataJson1 = new JSONObject();
        consentMappingDataJson1.put("MAPPING_STATUS", ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);
        consentMappingResources.put(ConsentMgtServiceTestData.SAMPLE_MAPPING_ID, consentMappingDataJson1);

        JSONObject consentMappingDataJson2 = new JSONObject();
        consentMappingDataJson2.put("MAPPING_STATUS", ConsentMgtServiceTestData.SAMPLE_NEW_MAPPING_STATUS);
        consentMappingResources.put(ConsentMgtServiceTestData.SAMPLE_MAPPING_ID_2, consentMappingDataJson2);

        consentMappingResources.put(UUID.randomUUID().toString(), "null");
        changedAttributesJson.put("ConsentMappingData", consentMappingResources);

        ConsentHistoryResource consentHistoryResource = new ConsentHistoryResource();
        consentHistoryResource.setChangedAttributesJsonDataMap(changedAttributesJson);
        consentHistoryResource.setReason("SampleReason");
        consentAmendmentHistoryDataMap.put(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID, consentHistoryResource);
        return consentAmendmentHistoryDataMap;
    }

    private static String getBasicConsentDataChangedAttributesJson() {

        JSONObject consentBasicDataJson = new JSONObject();
        consentBasicDataJson.put("RECEIPT", ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT);
        consentBasicDataJson.put("UPDATED_TIME",
                String.valueOf(ConsentMgtServiceTestData.SAMPLE_CONSENT_AMENDMENT_TIMESTAMP));
        consentBasicDataJson.put("VALIDITY_TIME",
                String.valueOf(ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD));
        consentBasicDataJson.put("CURRENT_STATUS", ConsentMgtServiceTestData.SAMPLE_PREVIOUS_STATUS);
        return consentBasicDataJson.toString();
    }

    private static String getConsentAttributesDataChangedAttributesJson() {

        JSONObject consentAttributesDataJson = new JSONObject();
        consentAttributesDataJson.put("sample_consent_attribute_name", "sample_consent_attribute_value");
        consentAttributesDataJson.put("sampleAttributeKey", null);
        return consentAttributesDataJson.toString();
    }

    public static Map<String, ConsentHistoryResource> getSampleConsentHistoryBasicConsentDataMap() {

        Map<String, ConsentHistoryResource> consentAmendmentHistoryDataMap = new LinkedHashMap<>();
        Map<String, Object> changedAttributesJson = new HashMap<>();
        changedAttributesJson.put("ConsentData", getBasicConsentDataChangedAttributesJson());
        ConsentHistoryResource consentHistoryResource = new ConsentHistoryResource();
        consentHistoryResource.setChangedAttributesJsonDataMap(changedAttributesJson);
        consentHistoryResource.setReason("SampleReason");
        consentAmendmentHistoryDataMap.put(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID, consentHistoryResource);
        return consentAmendmentHistoryDataMap;
    }

    public static Map<String, ConsentHistoryResource> getSampleConsentHistoryConsentAttributesDataMap() {

        Map<String, ConsentHistoryResource> consentAmendmentHistoryDataMap = new LinkedHashMap<>();
        Map<String, Object> changedAttributesJson = new HashMap<>();
        changedAttributesJson.put("ConsentAttributesData", getConsentAttributesDataChangedAttributesJson());
        ConsentHistoryResource consentHistoryResource = new ConsentHistoryResource();
        consentHistoryResource.setChangedAttributesJsonDataMap(changedAttributesJson);
        consentHistoryResource.setReason("SampleReason");
        consentAmendmentHistoryDataMap.put(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID, consentHistoryResource);
        return consentAmendmentHistoryDataMap;
    }

    public static Map<String, ConsentHistoryResource> getSampleConsentHistoryConsentMappingsDataMap() {

        Map<String, ConsentHistoryResource> consentAmendmentHistoryDataMap = new LinkedHashMap<>();

        Map<String, Object> changedAttributesJson = new HashMap<>();

        Map<String, Object> consentMappingResources = new HashMap<>();
        JSONObject consentMappingDataJson1 = new JSONObject();
        consentMappingDataJson1.put("MAPPING_STATUS", ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);
        consentMappingResources.put(ConsentMgtServiceTestData.SAMPLE_MAPPING_ID, consentMappingDataJson1);

        JSONObject consentMappingDataJson2 = new JSONObject();
        consentMappingDataJson2.put("MAPPING_STATUS", "null");
        consentMappingResources.put(ConsentMgtServiceTestData.SAMPLE_MAPPING_ID_2, consentMappingDataJson2);
        changedAttributesJson.put("ConsentMappingData", consentMappingResources);

        ConsentHistoryResource consentHistoryResource = new ConsentHistoryResource();
        consentHistoryResource.setChangedAttributesJsonDataMap(changedAttributesJson);
        consentHistoryResource.setReason("SampleReason");
        consentAmendmentHistoryDataMap.put(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID, consentHistoryResource);
        return consentAmendmentHistoryDataMap;
    }

    public static Map<String, Object> getSampleAdditionalConsentAmendmentDataMap() {

        Map<String, Object> additionalAmendmentData = new HashMap<>();
        Map<String, AuthorizationResource> newUserAuthResources = new HashMap<>();
        Map<String, ArrayList<ConsentMappingResource>> newUserAccountMappings = new HashMap<>();

        AuthorizationResource newAuthResource = getSampleStoredTestAuthorizationResource();
        newUserAuthResources.put(SAMPLE_NEW_USER_ID, newAuthResource);

        ConsentMappingResource consentMappingResource = getSampleStoredTestConsentMappingResource(null);
        ArrayList consentMappingResourceList = new ArrayList();
        consentMappingResourceList.add(consentMappingResource);
        newUserAccountMappings.put(SAMPLE_NEW_USER_ID, consentMappingResourceList);

        additionalAmendmentData
                .put(ConsentCoreServiceConstants.ADDITIONAL_AUTHORIZATION_RESOURCES, newUserAuthResources);
        additionalAmendmentData
                .put(ConsentCoreServiceConstants.ADDITIONAL_MAPPING_RESOURCES, newUserAccountMappings);
        return additionalAmendmentData;
    }

    public static Map<String, Object> getSampleAdditionalConsentAmendmentDataMapWithoutConsentId() {

        Map<String, Object> additionalAmendmentData = new HashMap<>();
        Map<String, AuthorizationResource> newUserAuthResources = new HashMap<>();
        Map<String, ArrayList<ConsentMappingResource>> newUserAccountMappings = new HashMap<>();

        AuthorizationResource newAuthResource = getSampleStoredTestAuthorizationResource();
        newAuthResource.setConsentID(null);
        newUserAuthResources.put(SAMPLE_NEW_USER_ID, newAuthResource);

        ConsentMappingResource consentMappingResource = getSampleStoredTestConsentMappingResource(null);
        ArrayList consentMappingResourceList = new ArrayList();
        consentMappingResourceList.add(consentMappingResource);
        newUserAccountMappings.put(SAMPLE_NEW_USER_ID, consentMappingResourceList);

        additionalAmendmentData
                .put(ConsentCoreServiceConstants.ADDITIONAL_AUTHORIZATION_RESOURCES, newUserAuthResources);
        additionalAmendmentData
                .put(ConsentCoreServiceConstants.ADDITIONAL_MAPPING_RESOURCES, newUserAccountMappings);
        return additionalAmendmentData;
    }

    public static Map<String, Object> getSampleAdditionalConsentAmendmentDataMapWithoutAccountId() {

        Map<String, Object> additionalAmendmentData = new HashMap<>();
        Map<String, AuthorizationResource> newUserAuthResources = new HashMap<>();
        Map<String, ArrayList<ConsentMappingResource>> newUserAccountMappings = new HashMap<>();

        AuthorizationResource newAuthResource = getSampleStoredTestAuthorizationResource();
        newUserAuthResources.put(SAMPLE_NEW_USER_ID, newAuthResource);

        ConsentMappingResource consentMappingResource = getSampleStoredTestConsentMappingResource(null);
        consentMappingResource.setAccountID(null);
        ArrayList consentMappingResourceList = new ArrayList();
        consentMappingResourceList.add(consentMappingResource);
        newUserAccountMappings.put(SAMPLE_NEW_USER_ID, consentMappingResourceList);

        additionalAmendmentData
                .put(ConsentCoreServiceConstants.ADDITIONAL_AUTHORIZATION_RESOURCES, newUserAuthResources);
        additionalAmendmentData
                .put(ConsentCoreServiceConstants.ADDITIONAL_MAPPING_RESOURCES, newUserAccountMappings);
        return additionalAmendmentData;
    }

}
