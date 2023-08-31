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

import { CONFIG } from "../config";

// common constants
export const CARBON_SUPER = "@carbon.super";
export const BEARER = "Bearer ";
export const CONTENT_TYPE_JSON = "application/json";

// app constants
export const ROLE_CUSTOMER_CARE_OFFICER = "customerCareOfficer";
export const URL_CONSENT_SEARCH = `${CONFIG.BACKEND_URL}/admin/search`;
export const URL_APP_INFO = `${CONFIG.SERVER_URL}/api/openbanking/application/metadata`;
export const URL_ALL_APP_INFO = `${CONFIG.SERVER_URL}/api/openbanking/application/all/metadata`;

export const CACHE_KEY_CONSENT_DATA = "consent-data";
