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
export const permissionDataLanguage = [
    {
        scope: "ReadAccountsDetail",
        dataCluster: "Your Account Details",
        title:"What we are sharing",
        permissions: [
            "Your account name, number, and sort code",
            "Your account balance",
            "Your card number",
        ],
    },
    {
        scope: "Permissions",
        dataCluster: "Permissions",
        title:"What we are sharing",
        permissions: [
            {
                name: "ReadAccountsDetail",
                description: "Your account identification details",
            },
            {
                name: "ReadBalances",
                description: "All your balance information",
            },
            {
                name: "ReadTransactionsDetail",
                description: "All transaction data elements which may hold silent party details",
            },
        ],
    },
];
