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

package com.wso2.openbanking.accelerator.account.metadata.service.dao;

import com.wso2.openbanking.accelerator.account.metadata.service.dao.queries.AccountMetadataDBQueriesMySQLImpl;
import com.wso2.openbanking.accelerator.account.metadata.service.util.AccountMetadataDAOTestData;
import com.wso2.openbanking.accelerator.account.metadata.service.util.DAOUtils;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.Map;

/**
 * Implementation of AccountMetadataDAOTests class.
 */
public class AccountMetadataDAOTests {

    private static final String DB_NAME = "OPENBANKING_DB";
    private AccountMetadataDAO accountMetadataDAO;

    @BeforeClass
    public void initTest() throws Exception {

        DAOUtils.initializeDataSource(DB_NAME, DAOUtils.getFilePath("dbScripts/h2.sql"));
        accountMetadataDAO = new AccountMetadataDAOImpl(new AccountMetadataDBQueriesMySQLImpl());
    }

    @DataProvider(name = "accountMetadataDataProvider")
    public Object[][] accountMetadataData() {
        return AccountMetadataDAOTestData.DataProviders.METADATA_DATA_HOLDER;
    }

    @DataProvider(name = "getAccountMetadataDataProvider")
    public Object[][] getAccountMetadataData() {
        return AccountMetadataDAOTestData.DataProviders.GET_METADATA_DATA_HOLDER;
    }

    @Test
    public void testStoreAccountMetadata() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String userId = AccountMetadataDAOTestData.SAMPLE_USER_ID;
        Map<String, String> metadataMap = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ATTRIBUTES_MAP;
        int affectedRows = 0;

        for (Map.Entry<String, String> entry : metadataMap.entrySet()) {
            String metadataKey = entry.getKey();
            String metadataValue = entry.getValue();
            Connection dbConnection = DAOUtils.getConnection(DB_NAME);
            affectedRows += accountMetadataDAO.storeAccountMetadata(dbConnection, accountId, userId, metadataKey,
                    metadataValue);

        }
        Assert.assertEquals(affectedRows, 4);
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testStoreAccountMetadataNullAccountIdAndUserIdError() throws Exception {

        String key = AccountMetadataDAOTestData.SAMPLE_KEY;
        String value = AccountMetadataDAOTestData.SAMPLE_VALUE;

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            accountMetadataDAO.storeAccountMetadata(dbConnection, null, null, key, value);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testStoreAccountMetadataEmptyMetadataMapError() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String userId = AccountMetadataDAOTestData.SAMPLE_USER_ID;

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            accountMetadataDAO.storeAccountMetadata(dbConnection, accountId, userId, "", "");
        }
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testStoreAccountMetadataNullMetadataMapError() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String userId = AccountMetadataDAOTestData.SAMPLE_USER_ID;

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            accountMetadataDAO.storeAccountMetadata(dbConnection, accountId, userId, null, null);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testStoreAccountMetadataNullDBConnectionError() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String userId = AccountMetadataDAOTestData.SAMPLE_USER_ID;
        String key = AccountMetadataDAOTestData.SAMPLE_KEY;
        String value = AccountMetadataDAOTestData.SAMPLE_VALUE;
        accountMetadataDAO.storeAccountMetadata(null, accountId, userId, key, value);
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testUpdateAccountMetadataNullAccountIdAndUserIdError() throws Exception {

        String key = AccountMetadataDAOTestData.SAMPLE_KEY;
        String value = AccountMetadataDAOTestData.SAMPLE_VALUE;

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            accountMetadataDAO.updateAccountMetadata(dbConnection, null, null, key, value);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testUpdateAccountMetadataEmptyMetadataMapError() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String userId = AccountMetadataDAOTestData.SAMPLE_USER_ID;

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            accountMetadataDAO.updateAccountMetadata(dbConnection, accountId, userId, "", "");
        }
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testUpdateAccountMetadataNullMetadataMapError() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String userId = AccountMetadataDAOTestData.SAMPLE_USER_ID;

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            accountMetadataDAO.updateAccountMetadata(dbConnection, accountId, userId, null, null);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testUpdateAccountMetadataNullDBConnectionError() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String userId = AccountMetadataDAOTestData.SAMPLE_USER_ID;
        String key = AccountMetadataDAOTestData.SAMPLE_KEY;
        String value = AccountMetadataDAOTestData.SAMPLE_VALUE;
        accountMetadataDAO.updateAccountMetadata(null, accountId, userId, key, value);
    }

    @Test(dataProvider = "getAccountMetadataDataProvider", dependsOnMethods = {"testStoreAccountMetadata"},
            priority = 1)
    public void testGetAccountMetadata(String accountId, String userId) throws Exception {

        Map<String, String> metadataMap;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            metadataMap = accountMetadataDAO.getAccountMetadataMap(dbConnection, accountId, userId);
        }
        Assert.assertEquals(metadataMap.size(), 4);
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testGetAccountMetadataNullAccountIdAndUserIdError() throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            accountMetadataDAO.getAccountMetadataMap(dbConnection, null, null);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class, dataProvider = "getAccountMetadataDataProvider",
            dependsOnMethods = {"testStoreAccountMetadata"},
            priority = 1)
    public void testGetAccountMetadataNullDBConnectionError(String accountId, String userId) throws Exception {
        accountMetadataDAO.getAccountMetadataMap(null, accountId, userId);
    }

    @Test(dataProvider = "accountMetadataDataProvider", dependsOnMethods = {"testStoreAccountMetadata"},
            priority = 1)
    public void testGetAccountMetadataByKey(String accountId, String userId, String key, String value)
            throws Exception {

        String metadataValue;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            metadataValue = accountMetadataDAO.getAccountMetadataByKey(dbConnection, accountId, userId, key);
        }
        Assert.assertEquals(metadataValue, value);
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testGetAccountMetadataByKeyNullAccountIdUserIdAndKeyError() throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            accountMetadataDAO.getAccountMetadataByKey(dbConnection, null, null, null);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class, dataProvider = "accountMetadataDataProvider")
    public void testGetAccountMetadataByKeyNullDBConnectionError(String accountId, String userId, String key,
                                                                 String value) throws Exception {
        accountMetadataDAO.getAccountMetadataByKey(null, accountId, userId, key);
    }

    @Test(dataProvider = "accountMetadataDataProvider", dependsOnMethods = {"testStoreAccountMetadata"},
            priority = 2)
    public void testDeleteAccountMetadataByKey(String accountId, String userId, String key, String value)
            throws Exception {

        int affectedRows;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            affectedRows = accountMetadataDAO.deleteAccountMetadataByKey(dbConnection, accountId, userId, key);
        }
        Assert.assertEquals(affectedRows, 1);
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testDeleteAccountMetadataByKeyNullAccountIdUserIdAndKeyError() throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            accountMetadataDAO.deleteAccountMetadataByKey(dbConnection, null, null, null);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class, dataProvider = "accountMetadataDataProvider")
    public void testDeleteAccountMetadataByKeyNullDBConnectionError(String accountId, String userId, String key,
                                                                    String value) throws Exception {

        accountMetadataDAO.deleteAccountMetadataByKey(null, accountId, userId, key);
    }

    @Test(dependsOnMethods = {"testStoreAccountMetadata", "testDeleteAccountMetadataByKey"}, priority = 2)
    public void testDeleteAccountMetadata() throws Exception {

        int affectedRows;
        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String userId = AccountMetadataDAOTestData.SAMPLE_USER_ID;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            affectedRows = accountMetadataDAO.deleteAccountMetadata(dbConnection, accountId, userId);
        }
        Assert.assertEquals(affectedRows, 3);
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testDeleteAccountMetadataNullAccountIdAndUserIdError() throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            accountMetadataDAO.deleteAccountMetadata(dbConnection, null, null);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testDeleteAccountMetadataNullDBConnection() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String userId = AccountMetadataDAOTestData.SAMPLE_USER_ID;
        accountMetadataDAO.deleteAccountMetadata(null, accountId, userId);
    }

    @Test
    public void testStoreAccountMetadataForSameAccount() throws Exception {
        int affectedRows = 0;
        Map<String, String> userAttributeAMp = AccountMetadataDAOTestData.SAMPLE_USER_ID_ATTRIBUTE_VALUE_MAP;
        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String attributeKey = AccountMetadataDAOTestData.SAMPLE_KEY;

        for (Map.Entry<String, String> entry : userAttributeAMp.entrySet()) {
            Connection dbConnection = DAOUtils.getConnection(DB_NAME);
            String userId = entry.getKey();
            String attributeValue = entry.getValue();
            affectedRows += accountMetadataDAO.storeAccountMetadata(dbConnection, accountId, userId, attributeKey,
                    attributeValue);
        }
        Assert.assertEquals(affectedRows, 4);
    }

    @Test(dependsOnMethods = {"testStoreAccountMetadataForSameAccount"}, priority = 2)
    public void testDeleteAccountMetadataByKeyForAllUsers() throws Exception {

        int affectedRows;
        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String key = AccountMetadataDAOTestData.SAMPLE_KEY;
        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            affectedRows = accountMetadataDAO.deleteAccountMetadataByKeyForAllUsers(dbConnection, accountId, key);
        }
        Assert.assertEquals(affectedRows, 4);
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testDeleteAccountMetadataByKeyForAllUsersNullAccountIdAndKeyError() throws Exception {

        try (Connection dbConnection = DAOUtils.getConnection(DB_NAME)) {
            accountMetadataDAO.deleteAccountMetadataByKeyForAllUsers(dbConnection, null, null);
        }
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testDeleteAccountMetadataByKeyForAllUsersNullDBConnection() throws Exception {

        String accountId = AccountMetadataDAOTestData.SAMPLE_ACCOUNT_ID;
        String userId = AccountMetadataDAOTestData.SAMPLE_USER_ID;
        accountMetadataDAO.deleteAccountMetadataByKeyForAllUsers(null, accountId, userId);
    }

}
