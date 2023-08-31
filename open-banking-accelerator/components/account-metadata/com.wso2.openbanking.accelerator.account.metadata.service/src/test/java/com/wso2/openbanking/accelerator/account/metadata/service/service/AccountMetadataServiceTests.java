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

package com.wso2.openbanking.accelerator.account.metadata.service.service;

import com.wso2.openbanking.accelerator.account.metadata.service.util.AccountMetadataDAOTestData;
import com.wso2.openbanking.accelerator.account.metadata.service.util.DAOUtils;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.DatabaseUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of AccountMetadataServiceTests class.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({DatabaseUtil.class})
public class AccountMetadataServiceTests extends PowerMockTestCase {

    private static final String DB_NAME = "OPENBANKING_DB";
    private AccountMetadataService accountMetadataService;

    @BeforeClass
    public void initTest() throws Exception {
        DAOUtils.initializeDataSource(DB_NAME, DAOUtils.getFilePath("dbScripts/h2.sql"));
        accountMetadataService = AccountMetadataServiceImpl.getInstance();
    }

    @DataProvider(name = "accountMetadataDataProvider")
    public Object[][] accountMetadataData() {
        return AccountMetadataDAOTestData.DataProviders.METADATA_DATA_HOLDER;
    }

    @DataProvider(name = "globalAccountMetadataDataProvider")
    public Object[][] globalAccountMetadataData() {
        return AccountMetadataDAOTestData.DataProviders.GLOBAL_METADATA_DATA_HOLDER;
    }

    @DataProvider(name = "getAccountMetadataDataProvider")
    public Object[][] getAccountMetadataData() {
        return AccountMetadataDAOTestData.DataProviders.GET_METADATA_DATA_HOLDER;
    }

    @Test
    public void testAddOrUpdateAccountMetadata() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String userId = AccountMetadataDAOTestData.SAMPLE_USER_ID;
        Map<String, String> metadataMap = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ATTRIBUTES_MAP;
        int noOfEntries;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            noOfEntries = accountMetadataService.addOrUpdateAccountMetadata(accountId, userId, metadataMap);

        }
        Assert.assertEquals(noOfEntries, 4);
    }

    @Test(dependsOnMethods = {"testAddOrUpdateAccountMetadata"}, priority = 1)
    public void testUpdateExistingAccountMetadata() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String userId = AccountMetadataDAOTestData.SAMPLE_USER_ID;
        String updateMetadataKey = "secondary-account-privilege";
        String updateMetadataValue = "active";
        int noOfEntries;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            noOfEntries = accountMetadataService.addOrUpdateAccountMetadata(accountId, userId, updateMetadataKey,
                    updateMetadataValue);

        }
        Assert.assertEquals(noOfEntries, 1);
    }

    @Test
    public void testAddOrUpdateAccountMetadataForSameAccount() throws Exception {
        int noOfEntries = 0;
        Map<String, String> userAttributeAMp = AccountMetadataDAOTestData.SAMPLE_USER_ID_ATTRIBUTE_VALUE_MAP;
        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String attributeKey = AccountMetadataDAOTestData.SAMPLE_KEY;

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            for (Map.Entry<String, String> entry : userAttributeAMp.entrySet()) {
                String userId = entry.getKey();
                String attributeValue = entry.getValue();
                Map<String, String> metadataMap = Collections.singletonMap(attributeKey, attributeValue);
                noOfEntries += accountMetadataService.addOrUpdateAccountMetadata(accountId, userId,
                        metadataMap);
            }
        }
        Assert.assertEquals(noOfEntries, 4);
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testAddOrUpdateAccountMetadataNullAccountIdError() throws Exception {

        String userId = AccountMetadataDAOTestData.SAMPLE_USER_ID;
        Map<String, String> metadataMap = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ATTRIBUTES_MAP;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.addOrUpdateAccountMetadata(null, userId, metadataMap);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testAddOrUpdateAccountMetadataNullMetadataError() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String userId = AccountMetadataDAOTestData.SAMPLE_USER_ID;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.addOrUpdateAccountMetadata(accountId, userId, "");

        }
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testAddOrUpdateAccountMetadataEmptyMetadataError() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String userId = AccountMetadataDAOTestData.SAMPLE_USER_ID;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.addOrUpdateAccountMetadata(accountId, userId, new HashMap<>());

        }
    }

    @Test
    public void testAddOrUpdateGlobalAccountMetadataMap() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        Map<String, String> metadataMap = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ATTRIBUTES_MAP;
        int noOfEntries;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            noOfEntries = accountMetadataService.addOrUpdateAccountMetadata(accountId, metadataMap);

        }
        Assert.assertEquals(noOfEntries, 4);
    }

    @Test(dependsOnMethods = {"testAddOrUpdateGlobalAccountMetadataMap"})
    public void testUpdateGlobalAccountMetadata() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String updateMetadataKey = "secondary-account-privilege";
        String updateMetadataValue = "active";        int noOfEntries;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            noOfEntries = accountMetadataService.addOrUpdateAccountMetadata(accountId, updateMetadataKey,
                    updateMetadataValue);

        }
        Assert.assertEquals(noOfEntries, 1);
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testAddOrUpdateGlobalAccountMetadataNullAccountIdError() throws Exception {

        Map<String, String> metadataMap = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ATTRIBUTES_MAP;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.addOrUpdateAccountMetadata(null, metadataMap);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testAddOrUpdateGlobalAccountMetadataNullMetadataError() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.addOrUpdateAccountMetadata(accountId, null);

        }
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testAddOrUpdateGlobalAccountMetadataEmptyMetadataError() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.addOrUpdateAccountMetadata(accountId, new HashMap<>());

        }
    }

    @Test(dataProvider = "getAccountMetadataDataProvider", dependsOnMethods = {"testAddOrUpdateAccountMetadata"},
            priority = 1)
    public void testGetAccountMetadataMap(String accountId, String userId) throws Exception {

        Map<String, String> metadataMap;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            metadataMap = accountMetadataService.getAccountMetadataMap(accountId, userId);

        }
        Assert.assertEquals(metadataMap.size(), 4);
    }

    @Test(expectedExceptions = OpenBankingException.class, dataProvider = "getAccountMetadataDataProvider")
    public void testGetAccountMetadataMapNullAccountIdError(String accountId, String userId) throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.getAccountMetadataMap(null, userId);

        }
    }

    @Test(expectedExceptions = OpenBankingException.class, dataProvider = "getAccountMetadataDataProvider")
    public void testGetAccountMetadataMapNullUserIdError(String accountId, String userId) throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.getAccountMetadataMap(accountId, null);

        }
    }

    @Test(dependsOnMethods = {"testAddOrUpdateGlobalAccountMetadataMap"}, priority = 1)
    public void testGetGlobalAccountMetadataMap() throws Exception {

        Map<String, String> metadataMap;
        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            metadataMap = accountMetadataService.getAccountMetadataMap(accountId);

        }
        Assert.assertEquals(metadataMap.size(), 4);
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testGetGlobalAccountMetadataMapNullAccountIdError() throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.getAccountMetadataMap(null);

        }
    }

    @Test(dependsOnMethods = {"testAddOrUpdateAccountMetadataForSameAccount"}, priority = 1)
    public void testGetUserAttributesForAccountIdAndKey() throws Exception {

        Map<String, String> metadataMap;
        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String attributeKey = AccountMetadataDAOTestData.SAMPLE_KEY;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            metadataMap = accountMetadataService.getUserMetadataForAccountIdAndKey(accountId, attributeKey);

        }
        Assert.assertEquals(metadataMap.size(), 4);
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testGetUserAttributesForAccountIdAndKeyNullAccountIdError() throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.getUserMetadataForAccountIdAndKey(null,
                    AccountMetadataDAOTestData.SAMPLE_KEY);

        }
    }

    @Test(dataProvider = "accountMetadataDataProvider", dependsOnMethods = {"testAddOrUpdateAccountMetadata"},
            priority = 1)
    public void testGetAccountMetadataByKey(String accountId, String userId, String key, String value)
            throws Exception {

        String metadataValue;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            metadataValue = accountMetadataService.getAccountMetadataByKey(accountId, userId, key);
        }
        Assert.assertEquals(metadataValue, value);
    }

    @Test(expectedExceptions = OpenBankingException.class, dataProvider = "accountMetadataDataProvider")
    public void testGetAccountMetadataByKeyNullAccountIdError(String accountId, String userId, String key, String value)
            throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.getAccountMetadataByKey(null, userId, key);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class, dataProvider = "accountMetadataDataProvider")
    public void testGetAccountMetadataByKeyNullUserIdError(String accountId, String userId, String key, String value)
            throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.getAccountMetadataByKey(accountId, null, key);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class, dataProvider = "accountMetadataDataProvider")
    public void testGetAccountMetadataByKeyNullKeyError(String accountId, String userId, String key, String value)
            throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.getAccountMetadataByKey(accountId, userId, null);
        }
    }

    @Test(dataProvider = "globalAccountMetadataDataProvider", dependsOnMethods =
            {"testAddOrUpdateGlobalAccountMetadataMap"},
            priority = 1)
    public void testGetGlobalAccountMetadataByKey(String accountId, String userId, String key, String value)
            throws Exception {

        String metadataValue;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            metadataValue = accountMetadataService.getAccountMetadataByKey(accountId, key);
        }
        Assert.assertEquals(metadataValue, value);
    }

    @Test(expectedExceptions = OpenBankingException.class, dataProvider = "globalAccountMetadataDataProvider")
    public void testGetGlobalAccountMetadataByKeyNullAccountIdError(String accountId, String userId, String key,
                                                                    String value) throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.getAccountMetadataByKey(null, key);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class, dataProvider = "globalAccountMetadataDataProvider")
    public void testGetGlobalAccountMetadataByKeyNullKeyError(String accountId, String userId, String key,
                                                              String value) throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.getAccountMetadataByKey(accountId, null);
        }
    }

    @Test(dataProvider = "accountMetadataDataProvider", dependsOnMethods = {"testAddOrUpdateAccountMetadata"},
            priority = 2)
    public void testDeleteAccountMetadataByKey(String accountId, String userId, String key, String value)
            throws Exception {

        int affectedRows;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            affectedRows = accountMetadataService.removeAccountMetadataByKey(accountId, userId, key);
        }
        Assert.assertEquals(affectedRows, 1);
    }

    @Test(expectedExceptions = OpenBankingException.class, dataProvider = "accountMetadataDataProvider")
    public void testDeleteAccountMetadataByKeyNullAccountIdError(String accountId, String userId, String key,
                                                                 String value) throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.removeAccountMetadataByKey(null, userId, key);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class, dataProvider = "accountMetadataDataProvider")
    public void testDeleteAccountMetadataByKeyNullUserIdError(String accountId, String userId, String key,
                                                              String value) throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.removeAccountMetadataByKey(accountId, null, key);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class, dataProvider = "accountMetadataDataProvider")
    public void testDeleteAccountMetadataByKeyNullKeyError(String accountId, String userId, String key,
                                                           String value) throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.removeAccountMetadataByKey(accountId, userId, null);
        }
    }

    @Test(dependsOnMethods = {"testAddOrUpdateAccountMetadata", "testDeleteAccountMetadataByKey"}, priority = 2)
    public void testDeleteAccountMetadata()
            throws Exception {

        int affectedRows;
        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String userId = AccountMetadataDAOTestData.SAMPLE_USER_ID;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            affectedRows = accountMetadataService.removeAccountMetadata(accountId, userId);
        }
        Assert.assertEquals(affectedRows, 3);
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testDeleteAccountMetadataNullAccountIdError() throws Exception {

        String userId = AccountMetadataDAOTestData.SAMPLE_USER_ID;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.removeAccountMetadata(null, userId);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testDeleteAccountMetadataNullUserIdError() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.removeAccountMetadata(accountId, null);
        }
    }

    @Test(dataProvider = "globalAccountMetadataDataProvider", dependsOnMethods =
            {"testAddOrUpdateGlobalAccountMetadataMap"},
            priority = 2)
    public void testDeleteGlobalAccountMetadataByKey(String accountId, String userId, String key, String value)
            throws Exception {

        int affectedRows;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            affectedRows = accountMetadataService.removeAccountMetadataByKey(accountId, key);
        }
        Assert.assertEquals(affectedRows, 1);
    }

    @Test(expectedExceptions = OpenBankingException.class, dataProvider = "globalAccountMetadataDataProvider")
    public void testDeleteGlobalAccountMetadataByKeyNullAccountIdError(String accountId, String userId, String key,
                                                                       String value) throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.removeAccountMetadataByKey(null, key);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class, dataProvider = "globalAccountMetadataDataProvider")
    public void testDeleteGlobalAccountMetadataByKeyNullKeyError(String accountId, String userId, String key,
                                                                 String value) throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.removeAccountMetadataByKey(accountId, null);
        }
    }

    @Test(dependsOnMethods = {"testAddOrUpdateGlobalAccountMetadataMap", "testDeleteGlobalAccountMetadataByKey"},
            priority = 2)
    public void testDeleteGlobalAccountMetadata()
            throws Exception {

        int affectedRows;
        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            affectedRows = accountMetadataService.removeAccountMetadata(accountId);
        }
        Assert.assertEquals(affectedRows, 3);
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testDeleteGlobalAccountMetadataNullAccountIdError() throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.removeAccountMetadata(null);
        }
    }

    @Test(dependsOnMethods = {"testAddOrUpdateAccountMetadataForSameAccount"}, priority = 2)
    public void testDeleteAccountMetadataByKeyForAllUsers() throws Exception {

        int affectedRows;
        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String attributeKey = AccountMetadataDAOTestData.SAMPLE_KEY;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            affectedRows = accountMetadataService.removeAccountMetadataByKeyForAllUsers(accountId, attributeKey);
        }
        Assert.assertEquals(affectedRows, 4);
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testDeleteAccountMetadataByKeyForAllUsersNullUserIdError() throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            PowerMockito.mockStatic(DatabaseUtil.class);
            PowerMockito.when(DatabaseUtil.getDBConnection())
                    .thenReturn(dbConnection);
            accountMetadataService.removeAccountMetadataByKeyForAllUsers(null,
                    AccountMetadataDAOTestData.SAMPLE_KEY);
        }
    }
}
