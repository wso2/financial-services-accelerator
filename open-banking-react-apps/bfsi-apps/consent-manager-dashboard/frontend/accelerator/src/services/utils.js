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

export function getValueFromConsent(key, consent) {
  try {
    let value = consent;
    key
      .toString()
      .split(".")
      .map((section) => {
        value = value[section];
      });
    return value;
  } catch (e) {
    return "";
  }
}

export function getValueFromConsentWithFailOver(key, failOverKey, consent) {
  try {
    let valueFromConsent = getValueFromConsent(key, consent);
    return valueFromConsent ?? getValueFromConsent(failOverKey, consent);
  } catch (e) {
    return "";
  }
}

export function getValueFromApplicationInfo(key, clientId, appInfo) {
  try {
    return appInfo.data[clientId].metadata[key];
  } catch (e) {
    return "";
  }
}

export function getValueFromApplicationInfoWithFailOver(
  key,
  failOverKey,
  clientId,
  appInfo
) {
  try {
    let valueFromAppInfo = getValueFromApplicationInfo(key, clientId, appInfo);
    return (
      valueFromAppInfo ??
      getValueFromApplicationInfo(failOverKey, clientId, appInfo)
    );
  } catch (e) {
    return "";
  }
}
