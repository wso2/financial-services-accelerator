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
        scope: "ReadAccountsBasic",
        dataCluster: "Account name, type and other details",
        title:"What we are sharing",
        permissions: [
            "Your account name",
            "Your account type",
            "Your account currency"
        ]
    },
    {
        scope: "ReadAccountsDetail",
        dataCluster: "Account balance and details",
        title:"What we are sharing",
        permissions: [
            "Your account name",
            "Your account type",
            "Your account currency",
            "Your account name, number, and sort code"
        ]
    },
    {
        scope: "ReadBalances",
        dataCluster: "Your account balance",
        title:"What we are sharing",
        permissions: [
            "Amount",
            "Currency",
            "Credit/Debit",
            "Type of Balance",
            "Date/Time",
            "Credit Line"
        ]
    },
    {
        scope: "ReadTransactionsBasic",
        dataCluster: "Your transactions",
        title:"What we are sharing",
        permissions: [
            "Basic transaction information on payments for both credits in and debits",
            "Reference",
            "Amount",
            "Status",
            "Booking Data Info",
            "Value Date info",
            "Transaction Code"
        ]
    },
    {
        scope: "ReadTransactionsDetail",
        dataCluster: "Details of your transactions",
        title:"What we are sharing",
        permissions: [
            "Detail transaction information on payments for both credits in and debits",
            "Reference",
            "Amount",
            "Status",
            "Booking Data Info",
            "Value Date info",
            "Transaction Code",
            "Payee Details",
            "Payer Details"
        ]
    }
];
