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

import React, { useState } from "react";

import { useQuery } from "react-query";

import { useAuthContext } from "@bfsi-react/auth";

import { getConsentsFromAPI } from "../apis/consent-api";
import { getAllApplicationInfo } from "../apis/applicationInfo-api";
import { CONFIG } from "../config";
import {
  getValueFromApplicationInfoWithFailOver,
  getValueFromConsent,
} from "../services/utils";

/**
 * Fetches consents and application info from backend.
 */
const useConsents = (type) => {
  const [limit, setLimit] = useState(CONFIG.NUMBER_OF_CONSENTS);
  const [offset, setOffset] = useState(0);
  const { state, getPartialAccessToken } = useAuthContext();
  const fetchAllConsents = () =>
    getConsentsFromAPI(
      state.username,
      state.role,
      getPartialAccessToken(),
      type,
      offset,
      limit
    );

  const {
    isLoading: isLoadingConsents,
    data: consents,
    isError: isErrorConsents,
    error: errorConsents,
  } = useQuery(["consent-data", offset, limit], fetchAllConsents);

  const {
    isLoading: isLoadingAppInfo,
    data: appInfo,
    isError: isErrorAppInfo,
    error: errorAppInfo,
  } = useQuery(["app-info-data"], getAllApplicationInfo);

  /**
   * Transform consent data to display in the table.
   *
   * @returns a list of consent data with software ids
   */
  const transformConsentData = () => {
    if (!isLoadingConsents && !isLoadingAppInfo) {
      return consents?.data.map((consent) => {
        return {
          createdTimestamp: getValueFromConsent("createdTimestamp", consent),
          expirationDateTime: getValueFromConsent(
            "receipt.Data.ExpirationDateTime",
            consent
          ),
          currentStatus: getValueFromConsent("currentStatus", consent),
          consentId: getValueFromConsent("consentId", consent),
          clientId: getValueFromConsent("clientId", consent),
          softwareId: getValueFromApplicationInfoWithFailOver(
            "software_client_name",
            "software_id",
            getValueFromConsent("clientId", consent),
            appInfo
          ),
        };
      });
    }
  };

  return {
    limit,
    offset,
    setLimit,
    setOffset,
    getConsents: transformConsentData,
    metadata: consents?.metadata,
    isLoading: isLoadingConsents || isLoadingAppInfo,
    isError: isErrorConsents || isErrorAppInfo,
    error: errorConsents || errorAppInfo,
  };
};

export default useConsents;
