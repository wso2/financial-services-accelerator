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

import React, { useContext, useEffect, useState } from "react";

// Components
import ConsentDetails from "../../../components/ConsentDetails/ConsentDetails";
import ConsentDetailsPopup from "../../../components/ConsentDetails/utils/ConsentDetailsPopup/ConsentDetailsPopup";
import ListItemV1 from "../../../components/ConsentDetails/utils/ListItemV1/ListItemV1";

// Libraries & Packages
import GlobalStateContext from "../../../utils/state/GlobalStateContext";
import { ListGroup, ListItem, Title, Popup } from "@bfsi-react/bfsi-ui";
import ConfirmationOfFundsConsentDetailsDataShared from "./utils/ConfirmationOfFundsConsentDetailsDataShared";
import { useParams } from "react-router-dom";

// Utils
import { ConfirmationOfFundsPageTableData } from "../../../utils/mock/ConfirmationOfFundsTableMockData";

function ConfirmationOfFundsCosentDetailsPage() {
  // State
  const { globalState, setGlobalState } = useContext(GlobalStateContext);
  const [displayPopup, setDisplayPopup] = useState(false);

  const { ID } = useParams();

  const consent = ConfirmationOfFundsPageTableData.filter(
    (item) => item.id === ID
  )[0];

  // Effect
  useEffect(() => {
    setGlobalState({
      ...globalState,
      page: "account-information/consent-details",
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="accountInformationConsentDetailsPage">
      <ConsentDetails
        backURL={"/account-information"}
        consent={consent}
        displayPopup={displayPopup}
        setDisplayPopup={setDisplayPopup}
      >
        {/* Key Dates */}
        <Title text="KEY DATES" />
        <ListGroup bullets="none">
          <ListItem>
            <ListItemV1
              leftValue="Consent Granted Date"
              rightValue={consent?.consentedDate}
            />
          </ListItem>
          <ListItem>
            <ListItemV1
              leftValue="Consent Expiration Date"
              rightValue={consent?.expirationDate}
            />
          </ListItem>
          <ListItem>
            <ListItemV1
              leftValue="Consent Sharing Period"
              rightValue={`${consent?.consentedDate} / ${consent?.expirationDate}`}
            />
          </ListItem>
          <ListItem>
            <ListItemV1
              leftValue="Data Sharing Frequency"
              rightValue="Ongoing"
            />
          </ListItem>
        </ListGroup>
        {/* Accounts */}
        <Title text="Accounts" />
        <ListGroup bullets="circle">
          <ListItem>Savings account 01</ListItem>
          <ListItem>Savings account 02</ListItem>
          <ListItem>Joint account 01</ListItem>
        </ListGroup>
        {/* Data Shared - Will be needed in the future */}
        {/* <Title text="Data Shared" />
        <ConfirmationOfFundsConsentDetailsDataShared /> */}
        {/* Popup */}
        <div>
          <Popup visible={displayPopup} setVisible={setDisplayPopup}>
            <ConsentDetailsPopup consent={consent} />
          </Popup>
        </div>
      </ConsentDetails>
    </div>
  );
}

export default ConfirmationOfFundsCosentDetailsPage;
