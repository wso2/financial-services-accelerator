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

export const common = {
  footerContent: 'WSO2 Open Banking | 2024',
  complaintHandleLinkText: 'Complaint handling and resolution',
};

export const keyDateTypes = {
  date: 'Date',
  dateRange: 'Date Range',
  text: 'Text',
  value: 'Value',
};

export const permissionBindTypes = {
  // Each account is bind to different different permissions
  samePermissionSetForAllAccounts: 'SamePermissionSetForAllAccounts',
  // All the accounts in the consent bind to same set of permissions
  differentPermissionsForEachAccount: 'DifferentPermissionsForEachAccount',
};

export const dataOrigins = {
  // To fetch data from consent
  consent: 'CONSENT',
  // To fetch data from application information
  applicationInfo: 'APPLICATION_INFO',
  // For table action button
  action: 'ACTION',
  // For table status
  status: 'STATUS',
};

export const dataTypes = {
  // To indicate the dataType is a ISO 8601 date
  date: 'DATE_ISO_8601',
  // To indicate the dataType is a raw text
  rawData: 'APPLICATION_INFO',
  // To indicate the dataType is a ISO 8601 date
  timestamp: 'DATE_TIMESTAMP',
};

export const consentTypes = [
  {
    id: 'accounts',
    label: 'Account Information',
    image: require('../images/accounts.png'),
  },
  {
    id: 'payments',
    label: 'Payments',
    image: require('../images/payments.png'),
  },
  {
    id: 'fundsconfirmations',
    label: 'Confirmation of Funds',
    image: require('../images/cof.png'),
  },
];
