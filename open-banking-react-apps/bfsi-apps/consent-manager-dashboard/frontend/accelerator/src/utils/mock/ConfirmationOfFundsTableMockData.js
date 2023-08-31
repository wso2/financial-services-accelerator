/* ----- ConfirmationOfFundsTableMockData.js ----- */
import React from "react";
import { Link } from "react-router-dom";

import CreateRoundedIcon from "@mui/icons-material/CreateRounded";
import { getConsentStatus } from "../../utils/common/ConsentStatusUtil";
import { Label, Text } from "@bfsi-react/bfsi-ui";

// Columns
export const ConfirmationOfFundsPageTableColumns = [
  {
    Header: "Application",
    accessor: "application",
    Cell: (props) => {
      return (
        <Text color={props.color} align="center">
          {props.value}
        </Text>
      );
    },
  },
  {
    Header: "Consented Date",
    accessor: "consentedDate",
    Cell: (props) => {
      return (
        <Text color={props.color} align="center">
          {props.value}
        </Text>
      );
    },
  },
  {
    Header: "Expiration Date",
    accessor: "expirationDate",
    Cell: (props) => {
      return (
        <Text color={props.color} align="center">
          {props.value}
        </Text>
      );
    },
  },
  {
    Header: "Status",
    accessor: "status",
    Cell: (props) => {
      return (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
          }}
        >
          <Label type={getConsentStatus(props.value)}>{props.value}</Label>
        </div>
      );
    },
  },
  {
    Header: "Action",
    accessor: "action",
    Cell: (props) => {
      return (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
          }}
        >
          <Link to={props.value}>
            <CreateRoundedIcon
              style={{ color: props.color, cursor: "pointer" }}
            />
          </Link>
        </div>
      );
    },
  },
];

// Data
export const ConfirmationOfFundsPageTableData = [
  {
    id: "1",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 01",
    consentedDate: "2023-05-12",
    expirationDate: "2023-05-13",
    status: "active",
    action: "/confirmation-of-funds/consent-details/1",
  },
  {
    id: "2",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 02",
    consentedDate: "2023-07-31",
    expirationDate: "2023-08-01",
    status: "expired",
    action: "/confirmation-of-funds/consent-details/2",
  },
  {
    id: "3",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 03",
    consentedDate: "2023-06-25",
    expirationDate: "2023-06-26",
    status: "revoked",
    action: "/confirmation-of-funds/consent-details/3",
  },
  {
    id: "4",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 04",
    consentedDate: "2023-08-10",
    expirationDate: "2023-08-11",
    status: "active",
    action: "/confirmation-of-funds/consent-details/4",
  },
  {
    id: "5",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 05",
    consentedDate: "2023-10-09",
    expirationDate: "2023-10-10",
    status: "expired",
    action: "/confirmation-of-funds/consent-details/5",
  },
  {
    id: "6",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 06",
    consentedDate: "2023-01-28",
    expirationDate: "2023-01-29",
    status: "revoked",
    action: "/confirmation-of-funds/consent-details/6",
  },
  {
    id: "7",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 07",
    consentedDate: "2023-05-04",
    expirationDate: "2023-05-05",
    status: "active",
    action: "/confirmation-of-funds/consent-details/7",
  },
  {
    id: "8",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 08",
    consentedDate: "2023-02-11",
    expirationDate: "2023-02-12",
    status: "expired",
    action: "/confirmation-of-funds/consent-details/8",
  },
  {
    id: "9",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 09",
    consentedDate: "2023-03-22",
    expirationDate: "2023-03-23",
    status: "active",
    action: "/confirmation-of-funds/consent-details/9",
  },
  {
    id: "10",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 10",
    consentedDate: "2023-06-13",
    expirationDate: "2023-06-14",
    status: "revoked",
    action: "/confirmation-of-funds/consent-details/10",
  },
  {
    id: "11",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 11",
    consentedDate: "2023-05-20",
    expirationDate: "2023-05-21",
    status: "active",
    action: "/confirmation-of-funds/consent-details/11",
  },
  {
    id: "12",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 12",
    consentedDate: "2023-08-02",
    expirationDate: "2023-08-03",
    status: "expired",
    action: "/confirmation-of-funds/consent-details/12",
  },
  {
    id: "13",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 13",
    consentedDate: "2023-02-25",
    expirationDate: "2023-02-26",
    status: "revoked",
    action: "/confirmation-of-funds/consent-details/13",
  },
  {
    id: "14",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 14",
    consentedDate: "2023-06-07",
    expirationDate: "2023-06-08",
    status: "active",
    action: "/confirmation-of-funds/consent-details/14",
  },
  {
    id: "15",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 15",
    consentedDate: "2023-09-16",
    expirationDate: "2023-09-17",
    status: "expired",
    action: "/confirmation-of-funds/consent-details/15",
  },
  {
    id: "16",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 16",
    consentedDate: "2023-07-02",
    expirationDate: "2023-07-03",
    status: "active",
    action: "/confirmation-of-funds/consent-details/16",
  },
  {
    id: "17",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 17",
    consentedDate: "2023-11-21",
    expirationDate: "2023-11-22",
    status: "revoked",
    action: "/confirmation-of-funds/consent-details/17",
  },
  {
    id: "18",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 18",
    consentedDate: "2023-07-07",
    expirationDate: "2023-07-08",
    status: "expired",
    action: "/confirmation-of-funds/consent-details/18",
  },
  {
    id: "19",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 19",
    consentedDate: "2023-09-02",
    expirationDate: "2023-09-03",
    status: "active",
    action: "/confirmation-of-funds/consent-details/19",
  },
  {
    id: "20",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 20",
    consentedDate: "2023-02-15",
    expirationDate: "2023-02-16",
    status: "revoked",
    action: "/confirmation-of-funds/consent-details/20",
  },
  {
    id: "21",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 21",
    consentedDate: "2023-03-27",
    expirationDate: "2023-03-28",
    status: "active",
    action: "/confirmation-of-funds/consent-details/21",
  },
  {
    id: "22",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 22",
    consentedDate: "2023-01-13",
    expirationDate: "2023-01-14",
    status: "expired",
    action: "/confirmation-of-funds/consent-details/22",
  },
  {
    id: "23",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 23",
    consentedDate: "2023-03-09",
    expirationDate: "2023-03-10",
    status: "revoked",
    action: "/confirmation-of-funds/consent-details/23",
  },
  {
    id: "24",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 24",
    consentedDate: "2023-01-22",
    expirationDate: "2023-01-23",
    status: "active",
    action: "/confirmation-of-funds/consent-details/24",
  },
  {
    id: "25",
    consentType: "confirmationOfFunds",
    application: "Confirmation of Funds 25",
    consentedDate: "2023-04-17",
    expirationDate: "2023-04-18",
    status: "expired",
    action: "/confirmation-of-funds/consent-details/25",
  },
];
