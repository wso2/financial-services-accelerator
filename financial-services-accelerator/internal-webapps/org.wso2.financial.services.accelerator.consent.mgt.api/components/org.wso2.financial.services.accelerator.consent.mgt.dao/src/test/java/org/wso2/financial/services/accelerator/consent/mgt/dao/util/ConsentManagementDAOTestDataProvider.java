/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.dao.util;

import org.testng.annotations.DataProvider;
import org.wso2.financial.services.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;

import java.util.UUID;

public class ConsentManagementDAOTestDataProvider {

    @DataProvider(name = "storeConsentDataProvider")
    public Object[][] storeConsentResourceData() {

        return new Object[][] {
                {
                        UUID.randomUUID().toString(),
                        ConsentMgtDAOTestData.SAMPLE_CONSENT_RECEIPT,
                        ConsentMgtDAOTestData.SAMPLE_CONSENT_TYPE,
                        ConsentMgtDAOTestData.SAMPLE_CONSENT_FREQUENCY,
                        ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                        ConsentMgtDAOTestData.SAMPLE_RECURRING_INDICATOR,
                        ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS,
                }
        };
    }

    @DataProvider(name = "updateConsentStatusDataProvider")
    public Object[][] updateConsentStatusData() {

        return new Object[][] {
            {
                ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS
            }
        };
    }

    @DataProvider(name = "storeAuthorizationDataProvider")
    public Object[][] storeAuthorizationResourceData() {

        return new Object[][] {
            {
                ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_TYPE,
                ConsentMgtDAOTestData.SAMPLE_USER_ID,
                ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_STATUS
            }
        };
    }

    @DataProvider(name = "updateAuthorizationStatusDataProvider")
    public Object[][] updateAuthorizationStatusData() {

        return new Object[][] {
            {
                ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS
            }
        };
    }

    @DataProvider(name = "updateAuthorizationUserDataProvider")
    public Object[][] updateAuthorizationUsersData() {

        return new Object[][] {
            {
                ConsentMgtDAOTestData.SAMPLE_NEW_USER_ID
            }
        };
    }

    @DataProvider(name = "storeConsentMappingDataProvider")
    public Object[][] storeConsentMappingResourceData() {

        return new Object[][] {
            {
                ConsentMgtDAOTestData.SAMPLE_ACCOUNT_ID,
                ConsentMgtDAOTestData.SAMPLE_PERMISSION,
                ConsentMgtDAOTestData.SAMPLE_MAPPING_STATUS
            }
        };
    }

    @DataProvider(name = "updateConsentMappingStatusDataProvider")
    public Object[][] updateConsentMappingStatusData() {

        return new Object[][] {
            {
                ConsentMgtDAOTestData.SAMPLE_NEW_MAPPING_STATUS
            }
        };
    }

    @DataProvider(name = "storeConsentAttributesDataProvider")
    public Object[][] storeConsentAttributesData() {

        return new Object[][] {
            {
                ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP
            }
        };
    }

    @DataProvider(name = "getConsentAttributesDataProvider")
    public Object[][] getConsentAttributesData() {

        return new Object[][] {
            {
                ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS
            }
        };
    }

    @DataProvider(name = "storeConsentFileDataProvider")
    public Object[][] storeConsentFileData() {

        return new Object[][] {
            {
                ConsentMgtDAOTestData.SAMPLE_CONSENT_FILE
            }
        };
    }

    @DataProvider(name = "storeConsentStatusAuditRecordDataProvider")
    public Object[][] storeConsentStatusAuditRecordData() {

        return new Object[][] {
            {
                ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtDAOTestData.SAMPLE_REASON,
                ConsentMgtDAOTestData.SAMPLE_ACTION_BY,
                ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS
            }
        };
    }

    @DataProvider(name = "storeConsentHistoryDataProvider")
    public Object[][] storeConsentHistoryData() {

        /*
         * historyID
         * consentID
         * changedAttributes
         * consentType
         * amendedTimestamp
         * amendmentReason
         */
        return new Object[][] {

                {
                        ConsentMgtDAOTestData.SAMPLE_HISTORY_ID,
                        ConsentMgtDAOConstants.STATUS_AUDIT_ID,
                        ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                        ConsentMgtDAOTestData.SAMPLE_CONSENT_BASIC_DATA_CHANGED_ATTRIBUTES_JSON.toString(),
                        ConsentMgtDAOConstants.TYPE_CONSENT_BASIC_DATA,
                        ConsentMgtDAOTestData.SAMPLE_UPDATED_TIME,
                        ConsentMgtDAOTestData.SAMPLE_AMENDMENT_REASON
                },
                {
                        ConsentMgtDAOTestData.SAMPLE_HISTORY_ID,
                        ConsentMgtDAOConstants.STATUS_AUDIT_ID,
                        ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                        ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_CHANGED_ATTRIBUTES_JSON.toString(),
                        ConsentMgtDAOConstants.TYPE_CONSENT_ATTRIBUTES_DATA,
                        ConsentMgtDAOTestData.SAMPLE_UPDATED_TIME,
                        ConsentMgtDAOTestData.SAMPLE_AMENDMENT_REASON
                },
                {
                        ConsentMgtDAOTestData.SAMPLE_HISTORY_ID,
                        ConsentMgtDAOConstants.STATUS_AUDIT_ID,
                        ConsentMgtDAOTestData.SAMPLE_MAPPING_ID,
                        ConsentMgtDAOTestData.SAMPLE_CONSENT_MAPPINGS_CHANGED_ATTRIBUTES_JSON.toString(),
                        ConsentMgtDAOConstants.TYPE_CONSENT_MAPPING_DATA,
                        ConsentMgtDAOTestData.SAMPLE_UPDATED_TIME,
                        ConsentMgtDAOTestData.SAMPLE_AMENDMENT_REASON
                },
                {
                        ConsentMgtDAOTestData.SAMPLE_HISTORY_ID,
                        ConsentMgtDAOConstants.STATUS_AUDIT_ID,
                        ConsentMgtDAOTestData.SAMPLE_MAPPING_ID_2,
                        ConsentMgtDAOTestData.SAMPLE_CONSENT_MAPPINGS_CHANGED_ATTRIBUTES_JSON.toString(),
                        ConsentMgtDAOConstants.TYPE_CONSENT_MAPPING_DATA,
                        ConsentMgtDAOTestData.SAMPLE_UPDATED_TIME,
                        ConsentMgtDAOTestData.SAMPLE_AMENDMENT_REASON
                },
                {
                        ConsentMgtDAOTestData.SAMPLE_HISTORY_ID,
                        ConsentMgtDAOConstants.STATUS_AUDIT_ID,
                        ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID,
                        "null",
                        ConsentMgtDAOConstants.TYPE_CONSENT_AUTH_RESOURCE_DATA,
                        ConsentMgtDAOTestData.SAMPLE_UPDATED_TIME,
                        ConsentMgtDAOTestData.SAMPLE_AMENDMENT_REASON
                }
        };
    }
}
