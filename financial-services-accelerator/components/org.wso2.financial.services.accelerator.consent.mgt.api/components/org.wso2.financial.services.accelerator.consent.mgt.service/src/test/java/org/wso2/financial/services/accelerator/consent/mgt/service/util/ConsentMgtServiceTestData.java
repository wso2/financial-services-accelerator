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

package org.wso2.financial.services.accelerator.consent.mgt.service.util;

import net.minidev.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentAttributes;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentFile;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ConsentMgtServiceTestData {

    public static final String SAMPLE_CONSENT_RECEIPT = "{\"validUntil\": \"2020-10-20\", \"frequencyPerDay\": 1," +
            " \"recurringIndicator\": false, \"combinedServiceIndicator\": true}";
    public static final String SAMPLE_CONSENT_TYPE = "accounts";
    public static final String SAMPLE_CLIENT_ID = "sampleClientID";
    public static final String ORG_INFO = "orgA";
    public static final String DEFAULT_ORG = "DEFAULT_ORG";
    public static final int SAMPLE_CONSENT_FREQUENCY = 1;
    public static final Long SAMPLE_CONSENT_VALIDITY_PERIOD = 1638337852L;
    public static final String SAMPLE_CURRENT_STATUS = "Authorised";
    public static final boolean SAMPLE_RECURRING_INDICATOR = true;
    public static final String SAMPLE_AUTH_TYPE = "authorizationType";
    public static final String SAMPLE_USER_ID = "admin@wso2.com";
    public static final String SAMPLE_AUTHORIZATION_STATUS = "awaitingAuthorization";
    public static final String UNMATCHED_CONSENT_ID = "2222";
    public static final String SAMPLE_MAPPING_ID = "sampleMappingId";
    public static final String SAMPLE_ACCOUNT_ID = "123456789";
    public static final String SAMPLE_MAPPING_STATUS = "active";
    public static final String SAMPLE_NEW_MAPPING_STATUS = "inactive";
    public static final JSONObject SAMPLE_RESOURCE = new JSONObject();
    public static final String SAMPLE_PERMISSION = "samplePermission";
    public static final String SAMPLE_REASON = "sample reason";
    public static final String SAMPLE_ACTION_BY = "admin@wso2.com";
    public static final String SAMPLE_PREVIOUS_STATUS = "Received";
    public static final String SAMPLE_CONSENT_FILE = "sample file content";
    public static final String AWAITING_UPLOAD_STATUS = "awaitingUpload";
    public static final String SAMPLE_AUTHORIZATION_ID_1 = "88888";
    public static final String SAMPLE_MAPPING_ID_2 = "sampleMappingId2";
    public static final String CONSENT_ID = "464ef174-9877-4c71-940c-93d6e069eaf9";
    public static final String SAMPLE_CONSUMED_STATUS = "Consumed";
    public static final String UNMATCHED_AUTHORIZATION_ID = "3333";
    public static final long SAMPLE_CONSENT_AMENDMENT_TIMESTAMP = 1638337852;
    public static final String SAMPLE_AMENDMENT_REASON = "sampleReason";
    public static final String SAMPLE_AUTHORIZATION_ID_2 = "99999";
    public static final String SAMPLE_HISTORY_ID = "sampleHistoryID";
    public static final String SAMPLE_NEW_USER_ID = "ann@gold.com";
    public static final Map<String, String> SAMPLE_CONSENT_ATTRIBUTES_MAP = new HashMap<String, String>() {
        {
            put("x-request-id", UUID.randomUUID().toString());
            put("idempotency-key", UUID.randomUUID().toString());
            put("sampleAttributeKey", "sampleAttributeValue");

        }
    };

    public static final Map<String, String> SAMPLE_CHANGED_CONSENT_ATTRIBUTES_MAP = new HashMap<String, String>() {
        {
            put("x-request-id", UUID.randomUUID().toString());
            put("idempotency-key", UUID.randomUUID().toString());
            put("sampleAttributeKey", "sampleAttributeValue");

        }
    };

    public static final ArrayList<String> SAMPLE_ACCOUNT_ID_LIST =
            new ArrayList<String>(Arrays.asList(SAMPLE_ACCOUNT_ID));

    public static final Map<String, ArrayList<String>> SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP =
            new HashMap<String, ArrayList<String>>() {{
                put("accountID1", new ArrayList<>(Arrays.asList("permission1", "permission2")));
                put("accountID2", new ArrayList<>(Arrays.asList("permission3", "permission4")));
            }};

    public static final ArrayList<String> SAMPLE_CONSENT_ATTRIBUTES_KEYS =
            new ArrayList<String>(Arrays.asList("x-request-id", "idempotency-key"));

    public static final ArrayList<String> SAMPLE_USER_IDS_LIST =
            new ArrayList<String>(Arrays.asList("userID1", "userID2", "userID3"));

    public static final ArrayList<String> SAMPLE_CONSENT_IS_ARRAY = new ArrayList<String>(Arrays.asList(CONSENT_ID));

    public static final Map<String, ArrayList<String>> SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP2 =
            new HashMap<String, ArrayList<String>>() {{
                put(SAMPLE_ACCOUNT_ID, new ArrayList<>(Arrays.asList("permission5", "permission6")));
            }};

    public static final ArrayList<String> SAMPLE_CONSENT_TYPES_LIST =
            new ArrayList<String>(Arrays.asList("accounts", "payments", "cof"));

    public static final ArrayList<String> SAMPLE_CONSENT_STATUSES_LIST =
            new ArrayList<String>(Arrays.asList("created", "authorised", "awaitingAuthorization"));

    public static final Map<String, ArrayList<String>> SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP3 =
            new HashMap<String, ArrayList<String>>() {{
                put("mismatching account ID", new ArrayList<>(Arrays.asList("permission5", "permission6")));
            }};

    public static final ArrayList<String> SAMPLE_CLIENT_IDS_LIST =
            new ArrayList<String>(Arrays.asList("clientID1", "clientID2", "clientID3"));
    public static final ArrayList<String> MAPPING_IDS_LIST =
            new ArrayList<String>(Arrays.asList(SAMPLE_MAPPING_ID, SAMPLE_MAPPING_ID_2));
    public static final ArrayList<String> UNMATCHED_MAPPING_IDS = new ArrayList<String>(Arrays.asList("4444", "5555"));

    public static ConsentResource getSampleTestConsentResource() {

        return getSampleTestConsentResource(ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    public static ConsentResource getSampleTestConsentResource(String status) {

        return new ConsentResource(ConsentMgtServiceTestData.ORG_INFO, null, UUID.randomUUID().toString(),
                ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_FREQUENCY,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.SAMPLE_RECURRING_INDICATOR, status,
                System.currentTimeMillis() / 1000, System.currentTimeMillis() / 1000);
    }

    public static ConsentResource getSampleStoredConsentResource() {
        ConsentResource consentResource = getSampleTestConsentResource();
        consentResource.setConsentID(UUID.randomUUID().toString());

        return consentResource;

    }

    public static ConsentResource getSampleStoredConsentResource(String status) {
        ConsentResource consentResource = getSampleTestConsentResource(status);
        consentResource.setConsentID(UUID.randomUUID().toString());

        return consentResource;

    }

    public static ConsentResource getSampleStoredTestConsentResourceWithAttributes() {

        ConsentResource consentResource = getSampleStoredConsentResource();
        consentResource.setConsentAttributes(ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);

        return consentResource;
    }

    public static DetailedConsentResource getSampleDetailedConsentResource(
            ArrayList<AuthorizationResource> authResources, ArrayList<ConsentMappingResource> mappingResources) {

        return new DetailedConsentResource(
                ConsentMgtServiceTestData.ORG_INFO,
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, UUID.randomUUID().toString(),
                ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CONSENT_FREQUENCY,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD, System.currentTimeMillis() / 1000,
                System.currentTimeMillis() / 1000, ConsentMgtServiceTestData.SAMPLE_RECURRING_INDICATOR,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, authResources, mappingResources);
    }

    public static ArrayList<DetailedConsentResource> getSampleDetailedStoredTestConsentResourcesList() {

        ArrayList<DetailedConsentResource> detailedConsentResourcesList = new ArrayList<>();

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null));

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        consentMappingResources.add(ConsentMgtServiceTestData
                .getSampleTestConsentMappingResource(ConsentMgtServiceTestData
                        .getSampleTestAuthorizationResource(null, null).getAuthorizationID()));

        DetailedConsentResource detailedConsentResource = getSampleDetailedConsentResource(authorizationResources,
                consentMappingResources);

        // Adding same resource twice to the list
        detailedConsentResourcesList.add(detailedConsentResource);
        detailedConsentResourcesList.add(detailedConsentResource);

        return detailedConsentResourcesList;
    }

    public static DetailedConsentResource getSampleDetailedStoredTestConsentResource() {

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

        return getSampleDetailedConsentResource(authorizationResources, consentMappingResources);
    }

    public static AuthorizationResource getSampleTestAuthorizationResource(String consentID,
                                                                           String authorizationID) {

        AuthorizationResource authorizationResource = new AuthorizationResource(consentID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE, System.currentTimeMillis() / 1000);
        authorizationResource.setAuthorizationID(authorizationID);

        return authorizationResource;
    }

    // list of authorization resources with consent mapping

    public static ArrayList<AuthorizationResource> getSampleTestAuthorizationResourcesList(String consentID,
                                                                                           String authorizationID) {

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(
                ConsentMgtServiceTestData.getSampleTestAuthorizationResourceWithConsentMapping(consentID,
                        authorizationID));
        authorizationResources.add(
                ConsentMgtServiceTestData.getSampleTestAuthorizationResourceWithConsentMapping(consentID,
                        authorizationID));

        return authorizationResources;
    }

    public static ArrayList<AuthorizationResource>
    getSampleTestAuthorizationResourceListWithAuthIdAndConsentMappingId() {
        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        AuthorizationResource authorizationResource = new AuthorizationResource(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE, System.currentTimeMillis() / 1000);
        authorizationResource.setAuthorizationID(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1);
        authorizationResource.setConsentMappingResource(
                ConsentMgtServiceTestData.getSampleTestConsentMappingResourceList());
        authorizationResources.add(authorizationResource);
        return authorizationResources;
    }


    public static AuthorizationResource getSampleTestAuthorizationResourceWithConsentMapping(String consentID,
                                                                                             String authorizationID) {


        AuthorizationResource authorizationResource = new AuthorizationResource(consentID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE, System.currentTimeMillis() / 1000);
        authorizationResource.setAuthorizationID(authorizationID);
        authorizationResource.setConsentMappingResource(
                ConsentMgtServiceTestData.getSampleTestConsentMappingResourceList());

        return authorizationResource;
    }

    public static AuthorizationResource getSampleStoredTestAuthorizationResource() {

        return getSampleTestAuthorizationResource(UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
    }

    public static ArrayList<AuthorizationResource> getSampleStoredTestAuthorizationResourcesList() {

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource());
        authorizationResources.add(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource());

        return authorizationResources;
    }

    public static ConsentMappingResource getSampleTestConsentMappingResource(String authorizationID) {

        ConsentMappingResource consentMappingResource = new ConsentMappingResource(authorizationID,
                ConsentMgtServiceTestData.SAMPLE_RESOURCE,
                ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);
        consentMappingResource.setMappingID(ConsentMgtServiceTestData.SAMPLE_MAPPING_ID);

        return consentMappingResource;
    }

    // New method to get consent mapping resource without mapping and authorization id
    public static ArrayList<ConsentMappingResource> getSampleTestConsentMappingResourceListWithMappingId() {

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setAccountID(ConsentMgtServiceTestData.SAMPLE_ACCOUNT_ID);
        consentMappingResource.setResource(ConsentMgtServiceTestData.SAMPLE_RESOURCE);
        consentMappingResource.setMappingStatus(ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);
        consentMappingResource.setMappingID(ConsentMgtServiceTestData.SAMPLE_MAPPING_ID);

        consentMappingResources.add(consentMappingResource);
        consentMappingResources.add(consentMappingResource);
        return consentMappingResources;
    }

    // New method to get consent mapping resource without mapping and authorization id
    public static ArrayList<ConsentMappingResource> getSampleTestConsentMappingResourceList() {

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setResource(ConsentMgtServiceTestData.SAMPLE_RESOURCE);
        consentMappingResource.setMappingStatus(ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);

        consentMappingResources.add(consentMappingResource);
        consentMappingResources.add(consentMappingResource);
        return consentMappingResources;
    }

    public static ConsentMappingResource getSampleTestInactiveConsentMappingResource(String authorizationID) {

        ConsentMappingResource consentMappingResource = getSampleTestConsentMappingResource(authorizationID);
        consentMappingResource.setMappingID(ConsentMgtServiceTestData.SAMPLE_MAPPING_ID_2);
        consentMappingResource.setResource(ConsentMgtServiceTestData.SAMPLE_RESOURCE);
        consentMappingResource.setMappingStatus(ConsentMgtServiceTestData.SAMPLE_NEW_MAPPING_STATUS);

        return consentMappingResource;
    }

    public static ConsentStatusAuditRecord getSampleTestConsentStatusAuditRecord(String consentID,
                                                                                 String currentStatus) {

        return new ConsentStatusAuditRecord(String.valueOf(UUID.randomUUID()), consentID, currentStatus,
                System.currentTimeMillis() / 1000, ConsentMgtServiceTestData.SAMPLE_REASON,
                ConsentMgtServiceTestData.SAMPLE_ACTION_BY, ConsentMgtServiceTestData.SAMPLE_PREVIOUS_STATUS);
    }

    public static ConsentStatusAuditRecord getSampleStoredTestConsentStatusAuditRecord(String sampleID,
                                                                                       String currentStatus) {

        ConsentStatusAuditRecord consentStatusAuditRecord = getSampleTestConsentStatusAuditRecord(sampleID,
                currentStatus);
        consentStatusAuditRecord.setStatusAuditID(sampleID);

        return consentStatusAuditRecord;
    }

    public static ConsentFile getSampleConsentFileObject(String fileContent) {

        return new ConsentFile(UUID.randomUUID().toString(), fileContent);
    }

    public static ConsentAttributes getSampleTestConsentAttributesObject(String consentID) {

        return new ConsentAttributes(consentID, ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    public static ArrayList<AuthorizationResource> getSampleAuthorizationResourcesList(ArrayList<String> consentIDs) {

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();

        for (String consentID : consentIDs) {
            for (int j = 0; j < 2; j++) {
                AuthorizationResource authorizationResource = getSampleTestAuthorizationResource(consentID,
                        consentID);
                authorizationResources.add(authorizationResource);
            }
        }
        return authorizationResources;
    }

    public static DetailedConsentResource getSampleDetailedStoredTestConsentResourceWithMultipleAccountIDs() {

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

        return getSampleDetailedConsentResource(authorizationResources, consentMappingResources);
    }

    public static ArrayList<ConsentMappingResource> getSampleConsentMappingResourcesList(ArrayList<String> authIDs) {

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();

        for (String authID : authIDs) {
            for (int j = 0; j < 2; j++) {
                ConsentMappingResource consentMappingResource = getSampleTestConsentMappingResource(authID);
                consentMappingResources.add(consentMappingResource);
            }
        }
        return consentMappingResources;
    }

    public static ConsentMappingResource getSampleStoredTestConsentMappingResource(String authorizationID) {

        ConsentMappingResource consentMappingResource = getSampleTestConsentMappingResource(authorizationID);
        consentMappingResource.setMappingID(UUID.randomUUID().toString());

        return consentMappingResource;
    }

    public static ConsentHistoryResource getSampleTestConsentHistoryResource() {

        ConsentHistoryResource consentHistoryResource = new ConsentHistoryResource();
        consentHistoryResource.setTimestamp(ConsentMgtServiceTestData.SAMPLE_CONSENT_AMENDMENT_TIMESTAMP);
        consentHistoryResource.setReason(ConsentMgtServiceTestData.SAMPLE_AMENDMENT_REASON);
        consentHistoryResource.setDetailedConsentResource(getSampleDetailedConsentHistoryResource());

        return consentHistoryResource;
    }

    public static DetailedConsentResource getSampleDetailedConsentHistoryResource() {

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(
                ConsentMgtServiceTestData.CONSENT_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1));

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        consentMappingResources.add(ConsentMgtServiceTestData
                .getSampleTestConsentHistoryMappingResource(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                        ConsentMgtServiceTestData.SAMPLE_MAPPING_ID));

        return getSampleDetailedConsentResource(authorizationResources, consentMappingResources);
    }

    public static ConsentMappingResource getSampleTestConsentHistoryMappingResource(String authorizationID,
                                                                                    String mappingId) {

        ConsentMappingResource consentMappingResource = getSampleTestConsentMappingResource(authorizationID);
        consentMappingResource.setMappingID(mappingId);

        return consentMappingResource;
    }

    public static DetailedConsentResource getSampleDetailedStoredTestCurrentConsentResource() {

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

        return getSampleDetailedConsentResource(authorizationResources, consentMappingResources);
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
        ArrayList<ConsentMappingResource> consentMappingResourceList = new ArrayList<>();
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
        ArrayList<ConsentMappingResource> consentMappingResourceList = new ArrayList<>();
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
        ArrayList<ConsentMappingResource> consentMappingResourceList = new ArrayList<>();
        consentMappingResourceList.add(consentMappingResource);
        newUserAccountMappings.put(SAMPLE_NEW_USER_ID, consentMappingResourceList);

        additionalAmendmentData
                .put(ConsentCoreServiceConstants.ADDITIONAL_AUTHORIZATION_RESOURCES, newUserAuthResources);
        additionalAmendmentData
                .put(ConsentCoreServiceConstants.ADDITIONAL_MAPPING_RESOURCES, newUserAccountMappings);
        return additionalAmendmentData;
    }

    // sample statusAudit record Ids
    public static ArrayList<String> getSampleConsentStatusAuditRecordIds() {

        ArrayList<String> consentStatusAuditRecordIds = new ArrayList<>();
        consentStatusAuditRecordIds.add(UUID.randomUUID().toString());
        consentStatusAuditRecordIds.add(UUID.randomUUID().toString());
        return consentStatusAuditRecordIds;
    }

    // return Map<string,ConsentHistoryReource> with consent history resource
    public static Map<String, ConsentHistoryResource> getSampleConsentHistoryDataMap() {

        Map<String, ConsentHistoryResource> consentAmendmentHistoryDataMap = new LinkedHashMap<>();
        Map<String, Object> changedAttributesJson = new HashMap<>();
        changedAttributesJson.put("ConsentData", getBasicConsentDataChangedAttributesJson());
        changedAttributesJson.put("ConsentAttributesData", getConsentAttributesDataChangedAttributesJson());
        changedAttributesJson.put("ConsentAuthResourceData", new HashMap<>());
        changedAttributesJson.put("ConsentMappingData", new HashMap<>());
        ConsentHistoryResource consentHistoryResource = new ConsentHistoryResource();
        consentHistoryResource.setChangedAttributesJsonDataMap(changedAttributesJson);
        consentHistoryResource.setReason("SampleReason");
        consentAmendmentHistoryDataMap.put(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID, consentHistoryResource);
        return consentAmendmentHistoryDataMap;
    }


}
