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

import axios from 'axios';
import { CONTENT_TYPE_JSON, CONSENT_URL } from "../utils/constants";

var urlParams = new URLSearchParams(window.location.search);
var sessionDataKeyConsent = urlParams.get('sessionDataKeyConsent');

/**
 * Retrieve consent data
 */
export const retrieveConsent = () => {
  const requestConfig = {
    method: "GET",
    headers: {
      "Content-Type": CONTENT_TYPE_JSON,
      "accepts": CONTENT_TYPE_JSON
    },
    url: CONSENT_URL+sessionDataKeyConsent
  };

  return axios
      .request(requestConfig)
      .then((response) => {
        return Promise.resolve(response.data);
      })
      .catch((error) => {
        return Promise.reject(error);
      });
};

/**
 * Persist consent data
 */
export const persistConsent = (data) => {
  const requestConfig = {
    method: "POST",
    headers: {
      "Content-Type": CONTENT_TYPE_JSON,
      "accepts": CONTENT_TYPE_JSON
    },
    data: data,
    url: CONSENT_URL+sessionDataKeyConsent
  };

  return axios
    .request(requestConfig)
    .then((response) => {
      window.location.href = response.data.value;
    })
    .catch((error) => {
      return Promise.reject(error);
    });
};
