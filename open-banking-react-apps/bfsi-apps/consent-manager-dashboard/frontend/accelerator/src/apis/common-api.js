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

import axios from "axios";
import { CONFIG } from "../config";

/**
 * Executes a HTTP GET request to the provided URL with headers
 *
 * @param {String} url request url
 * @param {Object} headers request headers
 * @param {Object} params request query parameters
 * @returns {Promise} http response as a promise
 */
export const get = async (url, headers, params) => {
  const requestConfig = {
    headers,
    params,
    method: "GET",
    url,
  };

  if (CONFIG.IS_DEVELOPMENT) {
    requestConfig.withCredentials = true;
  }

  return axios
    .request(requestConfig)
    .then((response) => {
      return Promise.resolve(response?.data);
    })
    .catch((error) => {
      return Promise.reject(error);
    });
};
