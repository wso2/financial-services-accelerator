/* ----- ConfirmationOfFundsCosentDetailsDataShared.js ----- */
import React from "react";

// Components
import ConsentDetailsDataShared from "../../../../components/ConsentDetails/utils/ConsentDetailsDataShared/ConsentDetailsDataShared";

// Libraries & Packages
import {
  Accordion,
  ListGroup,
  ListItem,
} from "@bfsi-react/bfsi-ui";

function ConfirmationOfFundsConsentDetailsDataShared() {
  return (
    <>
      <ConsentDetailsDataShared>
        <Accordion title="Read Accounts Detail">
          <ListGroup bullets="circle">
            <ListItem>Your account identification details</ListItem>
          </ListGroup>
        </Accordion>
        <Accordion title="Read Balances">
          <ListGroup bullets="circle">
            <ListItem>All your balance information</ListItem>
          </ListGroup>
        </Accordion>
        <Accordion title="Read Transactions Detail">
          <ListGroup bullets="circle">
            <ListItem>
              All transaction data elements which may hold silent party details
            </ListItem>
          </ListGroup>
        </Accordion>
      </ConsentDetailsDataShared>
    </>
  );
}

export default ConfirmationOfFundsConsentDetailsDataShared;
