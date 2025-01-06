/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.test.gateway.integration.accounts.util

import org.testng.annotations.DataProvider
import org.wso2.financial.services.accelerator.test.framework.constant.AcceleratorTestConstants

/**
 * Data providers for Accounts Test.
 */
class AccountsDataProviders {

    @DataProvider(name = "InvalidAccountsPermissionsForInitiation")
    Object[] getInvalidAccountsPermissionsForInitiation() {

        def invalidPermissions = new ArrayList<Object[]>()
        invalidPermissions.add([] as Object)
        invalidPermissions.add([AcceleratorTestConstants.READ_ACCOUNTS_DETAIL, "xyz"] as Object)
        invalidPermissions.add([AcceleratorTestConstants.INVALID_PERMISSION] as Object)

        return invalidPermissions
    }

    @DataProvider(name = "AccountsResources")
    Object[] accountsResources() {
        def accountsResources = new ArrayList<>()
        accountsResources.add(org.wso2.financial.services.accelerator.test.gateway.integration.accounts.util.AccountConstants.ACCOUNTS_PATH as Object)
        accountsResources.add(org.wso2.financial.services.accelerator.test.gateway.integration.accounts.util.AccountConstants.ACCOUNTS_BULK_PATH as Object)
        accountsResources.add(org.wso2.financial.services.accelerator.test.gateway.integration.accounts.util.AccountConstants.BALANCES_SINGLE_PATH as Object)
        accountsResources.add(org.wso2.financial.services.accelerator.test.gateway.integration.accounts.util.AccountConstants.TRANSACTIONS_SINGLE_PATH as Object)

        return accountsResources
    }

    @DataProvider(name = "AccountsResourcesWithoutPermissions")
    Object[] accountsResourcesWithoutPermissions() {

        def accountsResources = new ArrayList<String[]>()
        accountsResources.add([org.wso2.financial.services.accelerator.test.gateway.integration.accounts.util.AccountConstants.ACCOUNTS_BULK_PATH,
                               AccountPayloads.initiationPayloadWithoutReadAccountBasic] as String[])
        accountsResources.add([org.wso2.financial.services.accelerator.test.gateway.integration.accounts.util.AccountConstants.ACCOUNTS_PATH,
                               AccountPayloads.initiationPayloadWithoutReadAccountDetails] as String[])
        accountsResources.add([org.wso2.financial.services.accelerator.test.gateway.integration.accounts.util.AccountConstants.BALANCES_SINGLE_PATH,
                               AccountPayloads.initiationPayloadWithoutReadAccountBalance] as String[])
        accountsResources.add([org.wso2.financial.services.accelerator.test.gateway.integration.accounts.util.AccountConstants.TRANSACTIONS_SINGLE_PATH,
                               AccountPayloads.initiationPayloadWithoutReadAccountTransactions] as String[])

        return accountsResources
    }
}
