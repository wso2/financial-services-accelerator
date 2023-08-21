/**
 * Copyright (c) 2021-2023, WSO2 LLC. (https://www.wso2.com).
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

import {
  BEARER,
  CARBON_SUPER,
  CONTENT_TYPE_JSON,
  ROLE_CUSTOMER_CARE_OFFICER,
  URL_CONSENT_SEARCH,
} from "../utils/constants.js";
import { get } from "./common-api.js";

/**
 * Get the list of consents from the API.
 */
export const getConsentsFromAPI = async (
  username,
  userRole,
  accessToken,
  consentTypes,
  offset,
  limit
) => {
  const userId = username.endsWith(CARBON_SUPER)
    ? username
    : username + CARBON_SUPER;

  const queryParams = { consentTypes, offset, limit };

  if (userRole !== ROLE_CUSTOMER_CARE_OFFICER) {
    queryParams.userIDs = userId;
  }

  const headers = {
    "Content-Type": CONTENT_TYPE_JSON,
    Authorization: BEARER + accessToken,
  };

  return get(URL_CONSENT_SEARCH, headers, queryParams);
};
