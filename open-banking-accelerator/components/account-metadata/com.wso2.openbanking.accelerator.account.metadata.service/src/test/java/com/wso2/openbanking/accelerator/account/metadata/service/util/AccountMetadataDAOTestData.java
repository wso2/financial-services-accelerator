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
package com.wso2.openbanking.accelerator.account.metadata.service.util;


import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of AccountMetadataDAOTestData class.
 */
public class AccountMetadataDAOTestData {

    public static final String SAMPLE_ACCOUNT_ID = "account-1";
    public static final String SAMPLE_USER_ID = "ann@gold.com";
    public static final String SAMPLE_KEY = "bnr-permission";
    public static final String SAMPLE_VALUE = "active";
    public static final String GLOBAL = "GLOBAL";

    public static final Map<String, String> SAMPLE_ACCOUNT_ATTRIBUTES_MAP = new HashMap<String, String>() {
        {
            put("disclosure-option", "pre-approved");
            put("other-accounts-availability", "true");
            put("secondary-account-instruction", "active");
            put("secondary-account-privilege", "inactive");
        }
    };

    public static final Map<String, String> SAMPLE_USER_ID_ATTRIBUTE_VALUE_MAP = new HashMap<String, String>() {
        {
            put("sample_user_id_1", "active");
            put("sample_user_id_2", "inactive");
            put("sample_user_id_3", "active");
            put("sample_user_id_4", "inactive");
        }
    };

    /**
     * Implementation of AccountMetadataServiceTests class.
     */
    public static final class DataProviders {

        public static final Object[][] METADATA_DATA_HOLDER = new Object[][]{

                {
                        SAMPLE_ACCOUNT_ID,
                        SAMPLE_USER_ID,
                        "disclosure-option",
                        "pre-approved",
                }
        };

        public static final Object[][] GLOBAL_METADATA_DATA_HOLDER = new Object[][]{

                {
                        SAMPLE_ACCOUNT_ID,
                        GLOBAL,
                        "disclosure-option",
                        "pre-approved",
                }
        };

        public static final Object[][] GET_METADATA_DATA_HOLDER = new Object[][]{

                {
                        SAMPLE_ACCOUNT_ID,
                        SAMPLE_USER_ID,
                }
        };
    }
}
