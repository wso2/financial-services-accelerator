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
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;

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
    public static final String ORG_ID = "orgA";
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
    public static final long SAMPLE_EXPIRY_TIME = (System.currentTimeMillis() / 1000) + 100;
    public static final String AUTHORIZATION_ID = "testAuthId";
    public static final String SAMPLE_CONSUMED_STATUS = "Consumed";
    public static final String UNMATCHED_AUTHORIZATION_ID = "3333";
    public static final long SAMPLE_CONSENT_AMENDMENT_TIMESTAMP = 1638337852;
    public static final String SAMPLE_AMENDMENT_REASON = "sampleReason";
    public static final String SAMPLE_AUTHORIZATION_ID_2 = "99999";
    public static final String SAMPLE_HISTORY_ID = "sampleHistoryID";
    public static final String SAMPLE_NEW_USER_ID = "ann@gold.com";
    public static final Map<String, Object> SAMPLE_CONSENT_ATTRIBUTES_MAP = new HashMap<String, Object>() {
        {
            put("x-request-id", UUID.randomUUID().toString());
            put("idempotency-key", UUID.randomUUID().toString());
            put("sampleAttributeKey", "sampleAttributeValue");

        }
    };

    public static final Map<String, Object> SAMPLE_CHANGED_CONSENT_ATTRIBUTES_MAP = new HashMap<String, Object>() {
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

        return new ConsentResource(ConsentMgtServiceTestData.ORG_ID, null, UUID.randomUUID().toString(),
                ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                (System.currentTimeMillis() / 1000) + 100,
                ConsentMgtServiceTestData.SAMPLE_RECURRING_INDICATOR, status,
                (System.currentTimeMillis() / 1000) + 100, System.currentTimeMillis() / 1000);
    }

    public static ConsentResource getSampleStoredConsentResource() {
        ConsentResource consentResource = getSampleTestConsentResource();
        consentResource.setConsentId(UUID.randomUUID().toString());
        consentResource.setExpiryTime(System.currentTimeMillis() / 1000 + 10000);

        return consentResource;

    }

    public static ConsentResource getSampleStoredTestConsentResourceWithAttributes() {

        ConsentResource consentResource = getSampleStoredConsentResource();
        consentResource.setConsentAttributes(ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);

        return consentResource;
    }

    public static DetailedConsentResource getSampleDetailedConsentResource(
            ArrayList<AuthorizationResource> authResources) {

        return new DetailedConsentResource(
                ConsentMgtServiceTestData.ORG_ID,
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, UUID.randomUUID().toString(),
                ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,

                (System.currentTimeMillis() / 1000) + 1000, System.currentTimeMillis() / 1000,
                (System.currentTimeMillis() / 1000) + 1000, ConsentMgtServiceTestData.SAMPLE_RECURRING_INDICATOR,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, authResources);
    }

    public static DetailedConsentResource getSampleDetailedConsentResource() {
        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(ConsentMgtServiceTestData
                .getSampleTestAuthorizationResource(ConsentMgtServiceTestData.CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1));

        return getSampleDetailedConsentResource(authorizationResources);
    }

    public static ArrayList<DetailedConsentResource> getSampleDetailedStoredTestConsentResourcesList() {

        ArrayList<DetailedConsentResource> detailedConsentResourcesList = new ArrayList<>();

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null));

        DetailedConsentResource detailedConsentResource = getSampleDetailedConsentResource(authorizationResources);

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

        return getSampleDetailedConsentResource(authorizationResources);
    }

    public static AuthorizationResource getSampleTestAuthorizationResource(String consentId,
                                                                           String authorizationId) {

        AuthorizationResource authorizationResource = new AuthorizationResource(consentId,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE,
                ConsentMgtServiceTestData.SAMPLE_RESOURCE.toString(), System.currentTimeMillis() / 1000);
        authorizationResource.setAuthorizationId(authorizationId);

        return authorizationResource;
    }

    // list of authorization resources with consent mapping

    public static ArrayList<AuthorizationResource> getSampleTestAuthorizationResourcesList(String consentId,
                                                                                           String authorizationId) {

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(
                ConsentMgtServiceTestData.getSampleTestAuthorizationResourceWithConsentMapping(consentId,
                        authorizationId));
        authorizationResources.add(
                ConsentMgtServiceTestData.getSampleTestAuthorizationResourceWithConsentMapping(consentId,
                        authorizationId));

        return authorizationResources;
    }

    public static AuthorizationResource getSampleTestAuthorizationResourceWithConsentMapping(String consentId,
                                                                                             String authorizationId) {

        AuthorizationResource authorizationResource = new AuthorizationResource(consentId,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE,
                ConsentMgtServiceTestData.SAMPLE_RESOURCE.toString(), System.currentTimeMillis() / 1000);
        authorizationResource.setAuthorizationId(authorizationId);

        return authorizationResource;
    }

    public static AuthorizationResource getSampleStoredTestAuthorizationResource() {

        return getSampleTestAuthorizationResource(UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
    }

    public static ConsentMappingResource getSampleTestConsentMappingResource(String authorizationId) {

        ConsentMappingResource consentMappingResource = new ConsentMappingResource(authorizationId,
                ConsentMgtServiceTestData.SAMPLE_RESOURCE,
                ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);
        consentMappingResource.setMappingId(ConsentMgtServiceTestData.SAMPLE_MAPPING_ID);

        return consentMappingResource;
    }

    // New method to get consent mapping resource without mapping and authorization id
    public static ArrayList<ConsentMappingResource> getSampleTestConsentMappingResourceListWithMappingId() {

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setAccountId(ConsentMgtServiceTestData.SAMPLE_ACCOUNT_ID);
        consentMappingResource.setResource(ConsentMgtServiceTestData.SAMPLE_RESOURCE);
        consentMappingResource.setMappingStatus(ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);
        consentMappingResource.setMappingId(ConsentMgtServiceTestData.SAMPLE_MAPPING_ID);

        consentMappingResources.add(consentMappingResource);
        consentMappingResources.add(consentMappingResource);
        return consentMappingResources;
    }

    public static ConsentMappingResource getSampleTestInactiveConsentMappingResource(String authorizationId) {

        ConsentMappingResource consentMappingResource = getSampleTestConsentMappingResource(authorizationId);
        consentMappingResource.setMappingId(ConsentMgtServiceTestData.SAMPLE_MAPPING_ID_2);
        consentMappingResource.setResource(ConsentMgtServiceTestData.SAMPLE_RESOURCE);
        consentMappingResource.setMappingStatus(ConsentMgtServiceTestData.SAMPLE_NEW_MAPPING_STATUS);

        return consentMappingResource;
    }

    public static ConsentStatusAuditRecord getSampleTestConsentStatusAuditRecord(String consentId,
                                                                                 String currentStatus) {

        return new ConsentStatusAuditRecord(String.valueOf(UUID.randomUUID()), consentId, currentStatus,
                System.currentTimeMillis() / 1000, ConsentMgtServiceTestData.SAMPLE_REASON,
                ConsentMgtServiceTestData.SAMPLE_ACTION_BY, ConsentMgtServiceTestData.SAMPLE_PREVIOUS_STATUS);
    }

    public static ConsentAttributes getSampleTestConsentAttributesObject(String consentId) {

        return new ConsentAttributes(consentId, ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    public static DetailedConsentResource getSampleDetailedStoredTestConsentResourceWithMultipleAccountIDs() {

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(ConsentMgtServiceTestData
                .getSampleStoredTestAuthorizationResource());

        return getSampleDetailedConsentResource(authorizationResources);
    }

    public static ConsentMappingResource getSampleStoredTestConsentMappingResource(String authorizationId) {

        ConsentMappingResource consentMappingResource = getSampleTestConsentMappingResource(authorizationId);
        consentMappingResource.setMappingId(UUID.randomUUID().toString());

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

        return getSampleDetailedConsentResource(authorizationResources);
    }

    public static ConsentMappingResource getSampleTestConsentHistoryMappingResource(String authorizationId,
                                                                                    String mappingId) {

        ConsentMappingResource consentMappingResource = getSampleTestConsentMappingResource(authorizationId);
        consentMappingResource.setMappingId(mappingId);

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

        return getSampleDetailedConsentResource(authorizationResources);
    }

    private static String getBasicConsentDataChangedAttributesJson() {

        JSONObject consentBasicDataJson = new JSONObject();
        consentBasicDataJson.put("RECEIPT", ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT);
        consentBasicDataJson.put("UPDATED_TIME",
                String.valueOf(ConsentMgtServiceTestData.SAMPLE_CONSENT_AMENDMENT_TIMESTAMP));
        consentBasicDataJson.put("EXPIRY_TIME",
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

}
