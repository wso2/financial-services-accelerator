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

import React, { useContext } from "react";
import "./ListItemV1.scss";

// Libraries & Packages
import { ThemeContext } from "@bfsi-react/bfsi-ui";

function listItemV1({ leftValue, rightValue }) {
  // State
  const { theme } = useContext(ThemeContext);

  return (
    <div className="listItemV1">
      {/* Left Value */}
      <div
        className="listItemV1__leftValue"
        style={{ color: theme.palette.miscellaneous.grey.main }}
      >
        {leftValue}
      </div>
      {/* Seperator */}
      <div className="listItemV1__seperator">:</div>
      {/* Right Value */}
      <div
        className="listItemV1__rightValue"
        style={{ color: theme.palette.primary.main }}
      >
        {rightValue}
      </div>
    </div>
  );
}

export default listItemV1;
