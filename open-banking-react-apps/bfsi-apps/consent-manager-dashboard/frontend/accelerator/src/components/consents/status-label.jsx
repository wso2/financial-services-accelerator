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

import React from "react";

/**
 * A custom label component for Consent Status
 */
// todo: need to get this component from the common ui library
const StatusLabel = ({ status }) => {
  let labelColor = "";

  if (
    status.toLowerCase() == "active" ||
    status.toLowerCase() == "authorised"
  ) {
    labelColor = "green";
  } else if (status.toLowerCase() == "expired") {
    labelColor = "yellow";
  } else if (status.toLowerCase() == "revoked") {
    labelColor = "red";
  }

  return <span className={labelColor}>{status}</span>;
};

export default StatusLabel;
