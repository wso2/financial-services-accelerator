/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.test.framework.utility

import org.testng.annotations.DataProvider
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants

class AccountsDataProviders {

    @DataProvider(name = "InvalidAccountsPermissionsForInitiation")
    Object[] getInvalidAccountsPermissionsForInitiation() {

        def invalidPermissions = new ArrayList<Object[]>()
        invalidPermissions.add([ConnectorTestConstants.READ_ACCOUNTS_BASIC, "xyz"] as Object)
        invalidPermissions.add([ConnectorTestConstants.READ_ACCOUNTS_BASIC, ConnectorTestConstants.INVALID_PERMISSION] as Object)

        return invalidPermissions
    }

    @DataProvider(name = "ValidAccountsPermissionsForInitiation")
    Object[] getValidAccountsPermissionsForInitiation() {

        def validPermissions = new ArrayList<Object[]>()
        validPermissions.add([ConnectorTestConstants.READ_ACCOUNTS_BASIC] as Object)
        validPermissions.add([ConnectorTestConstants.READ_ACCOUNTS_DETAIL] as Object)
        validPermissions.add([ConnectorTestConstants.READ_ACCOUNTS_DETAIL, ConnectorTestConstants.READ_TRANSACTIONS_BASIC] as Object)

        return validPermissions
    }
}
