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

import { lazy } from "react";

const AccountConsent = lazy(() => import("./frontend/source/src/pages/consentPages/AccountConsent.jsx"));
const AccountSelection = lazy(() => import("./frontend/source/src/pages/consentPages/AccountSelection.jsx"));
const PaymentConsent = lazy(() => import("./frontend/source/src/pages/consentPages/PaymentConsent.jsx"));

/**
 * File to configure the consent pages and their order.
 */
export const getConfigs = (consentType) => {
  return {
    consentPages: [
      {
        Index: 1,
        Component: AccountSelection
      },
      {
        Index: 2,
        Component: consentType==="accounts"? AccountConsent:PaymentConsent
      }
    ]
  };
};
