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
// import PaymentsConsentDetailsDataShared from "./utils/PaymentsConsentDetailsDataShared";
import { useParams } from "react-router-dom";

// Utils
import { PaymentsPageTableData } from "../../../utils/mock/PaymentsTableMockData";

function PaymentsCosentDetailsPage() {
  // State
  const { globalState, setGlobalState } = useContext(GlobalStateContext);
  const [displayPopup, setDisplayPopup] = useState(false);

  const { ID } = useParams();

  const consent = PaymentsPageTableData.filter((item) => item.id === ID)[0];

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
        backURL={"/payments"}
        consent={consent}
        displayPopup={displayPopup}
        setDisplayPopup={setDisplayPopup}
      >
        {/* Payment Details */}
        <Title text="Payment Details" />
        <ListGroup bullets="none">
          <ListItem>
            <ListItemV1
              leftValue="Amount"
              rightValue={consent?.paymentDetails.amount}
            />
          </ListItem>
          <ListItem>
            <ListItemV1
              leftValue="Debtor Reference"
              rightValue={consent?.paymentDetails.debtorReference}
            />
          </ListItem>
          <ListItem>
            <ListItemV1
              leftValue="Payment Reference"
              rightValue={consent?.paymentDetails.paymentReference}
            />
          </ListItem>
          <ListItem>
            <ListItemV1
              leftValue="Payee Name"
              rightValue={consent?.paymentDetails.payeeName}
            />
          </ListItem>
          <ListItem>
            <ListItemV1
              leftValue="Sort Code"
              rightValue={consent?.paymentDetails.sortCode}
            />
          </ListItem>
          <ListItem>
            <ListItemV1
              leftValue="Account Number"
              rightValue={consent?.paymentDetails.accountNumber}
            />
          </ListItem>
        </ListGroup>
        {/* Payer Information */}
        <Title text="Payer Information" />
        <ListGroup bullets="none">
          <ListItem>
            <ListItemV1
              leftValue="Sort Code"
              rightValue={consent?.payerInformation.sortCode}
            />
          </ListItem>
          <ListItem>
            <ListItemV1
              leftValue="Account Number"
              rightValue={consent?.payerInformation.accountNumber}
            />
          </ListItem>
          <ListItem>
            <ListItemV1
              leftValue="Account Name"
              rightValue={consent?.payerInformation.accountName}
            />
          </ListItem>
        </ListGroup>

        <div>
          <Popup visible={displayPopup} setVisible={setDisplayPopup}>
            <ConsentDetailsPopup consent={consent} />
          </Popup>
        </div>
      </ConsentDetails>
    </div>
  );
}

export default PaymentsCosentDetailsPage;
