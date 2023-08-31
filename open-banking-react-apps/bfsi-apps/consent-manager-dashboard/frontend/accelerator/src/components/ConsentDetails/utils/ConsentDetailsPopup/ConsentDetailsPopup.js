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
import "./ConsentDetailsPopup.scss";

// Assets
import IMG_BANK from "../../../../assets/general/bank.jpg";

// Libraries & Packages
import { Button, Title, Text, ThemeContext } from "@bfsi-react/bfsi-ui";

function ConsentDetailsPopup({ consent, dataShared }) {
  // State
  const { theme } = useContext(ThemeContext);
  return (
    <div className="consentDetailsPopup">
      {/*  Image */}
      <div className="consentDetailsPopup__image">
        <img src={IMG_BANK} alt="" />
      </div>
      {/* Name */}
      <div className="consentDetailsPopup__name">
        <Text
          color={theme.palette.miscellaneous.grey.main}
          fontSize="18px"
          align="center"
        >
          {consent?.application}
        </Text>
      </div>
      <Text
        color={theme.palette.miscellaneous.grey.main}
        align="center"
        spacing="md"
      >
        {`Do you want to stop sharing data with ${consent?.application}?`}
      </Text>
      {/* Impact to your service */}
      <Title text="Impact to your service" />
      <Text color={theme.palette.miscellaneous.grey.main} spacing="md">
        Your service may be impacted when yoy stop sharing. You should check
        with the service provider before perform this action.
      </Text>
      {/* What happens to your shared data */}
      <Title text="What happens to your shared data" />
      <Text color={theme.palette.miscellaneous.grey.main} spacing="md">
        When you stop sharing, the service provider will either DE-identify or
        delete your data once it's no longer required. If you haven't already,
        you can ask them to delete your data on their website/app, but you must
        do this before you stop sharing.
      </Text>
      {dataShared && (
        <Text align="center" color={theme.palette.primary.main} spacing="md">
          The service provider will stop collecting and using the data you
          shared with them below.
        </Text>
      )}
      {/* Data Shared */}
      {dataShared && dataShared}
      <Text align="center" color={theme.palette.primary.main} spacing="md">
        Do you want to stop sharing?
      </Text>
      <Text
        align="center"
        color={theme.palette.miscellaneous.grey.main}
        spacing="md"
      >
        You should check with the service provider to understand the
        consequences
      </Text>
      <div className="consentDetailsPopup__buttons">
        <div className="consentDetailsPopup__button__cancel">
          <Button variant="outlined">Cancel</Button>
        </div>
        <div className="consentDetailsPopup__button__stop">
          <Button variant="outlined" type="error">
            Stop Sharing
          </Button>
        </div>
      </div>
    </div>
  );
}

export default ConsentDetailsPopup;
