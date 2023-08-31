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
import "./PageDetails.scss";

// Libraries & Packages
import { Text, ThemeContext } from "@bfsi-react/bfsi-ui";

// Utils
import { setPageAttributes } from "../../utils/common/PageAttributeUtil";

function PageDetails({ globalState, theme }) {
  return (
    <div className="pageDetails">
      {/*  Icon*/}
      <div
        className="pageDetails__icon"
        style={{
          color: theme.palette.primary.main,
        }}
      >
        {setPageAttributes(globalState.page).icon}
      </div>
      {/* Text */}
      <div className="pageDetails__title">
        <Text
          color={theme.palette.primary.main}
          fontSize="md"
          textTransform="uppercase"
        >
          {setPageAttributes(globalState.page).title}
        </Text>
      </div>
    </div>
  );
}

export default PageDetails;
