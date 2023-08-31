/* ----- PaymentsTableMockData.js ----- */
import React from "react";
import { Link } from "react-router-dom";

import CreateRoundedIcon from "@mui/icons-material/CreateRounded";
import { getConsentStatus } from "../../utils/common/ConsentStatusUtil";
import { Label, Text } from "@bfsi-react/bfsi-ui";

// Columns
export const PaymentsPageTableColumns = [
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
export const PaymentsPageTableData = [
  {
    id: "1",
    consentType: "payments",
    application: "Payments 01",
    consentedDate: "2023-05-12",
    status: "authorized",
    action: "/payments/consent-details/1",
    paymentDetails: {
      amount: "100.00 USD",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
      paymentReference: "INV-20230726",
      debtorReference: "DBTR-20230726",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "2",
    consentType: "payments",
    application: "Payments 02",
    consentedDate: "2023-07-31",
    status: "consumed",
    action: "/payments/consent-details/2",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "3",
    consentType: "payments",
    application: "Payments 03",
    consentedDate: "2023-06-25",
    status: "authorized",
    action: "/payments/consent-details/3",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "4",
    consentType: "payments",
    application: "Payments 04",
    consentedDate: "2023-08-10",
    status: "consumed",
    action: "/payments/consent-details/4",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "5",
    consentType: "payments",
    application: "Payments 05",
    consentedDate: "2023-10-09",
    status: "authorized",
    action: "/payments/consent-details/5",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "6",
    consentType: "payments",
    application: "Payments 06",
    consentedDate: "2023-01-28",
    status: "consumed",
    action: "/payments/consent-details/6",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "7",
    consentType: "payments",
    application: "Payments 07",
    consentedDate: "2023-05-04",
    status: "authorized",
    action: "/payments/consent-details/7",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "8",
    consentType: "payments",
    application: "Payments 08",
    consentedDate: "2023-02-11",
    status: "consumed",
    action: "/payments/consent-details/8",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "9",
    consentType: "payments",
    application: "Payments 09",
    consentedDate: "2023-03-22",
    status: "authorized",
    action: "/payments/consent-details/9",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "10",
    consentType: "payments",
    application: "Payments 10",
    consentedDate: "2023-06-13",
    status: "consumed",
    action: "/payments/consent-details/10",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "11",
    consentType: "payments",
    application: "Payments 11",
    consentedDate: "2023-05-20",
    status: "authorized",
    action: "/payments/consent-details/11",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "12",
    consentType: "payments",
    application: "Payments 12",
    consentedDate: "2023-08-02",
    status: "consumed",
    action: "/payments/consent-details/12",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "13",
    consentType: "payments",
    application: "Payments 13",
    consentedDate: "2023-02-25",
    status: "authorized",
    action: "/payments/consent-details/13",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "14",
    consentType: "payments",
    application: "Payments 14",
    consentedDate: "2023-06-07",
    status: "consumed",
    action: "/payments/consent-details/14",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "15",
    consentType: "payments",
    application: "Payments 15",
    consentedDate: "2023-09-16",
    status: "authorized",
    action: "/payments/consent-details/15",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "16",
    consentType: "payments",
    application: "Payments 16",
    consentedDate: "2023-07-02",
    status: "consumed",
    action: "/payments/consent-details/16",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "17",
    consentType: "payments",
    application: "Payments 17",
    consentedDate: "2023-11-21",
    status: "authorized",
    action: "/payments/consent-details/17",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "18",
    consentType: "payments",
    application: "Payments 18",
    consentedDate: "2023-07-07",
    status: "consumed",
    action: "/payments/consent-details/18",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "19",
    consentType: "payments",
    application: "Payments 19",
    consentedDate: "2023-09-02",
    status: "authorized",
    action: "/payments/consent-details/19",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "20",
    consentType: "payments",
    application: "Payments 20",
    consentedDate: "2023-02-15",
    status: "consumed",
    action: "/payments/consent-details/20",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "21",
    consentType: "payments",
    application: "Payments 21",
    consentedDate: "2023-03-27",
    status: "authorized",
    action: "/payments/consent-details/21",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "22",
    consentType: "payments",
    application: "Payments 22",
    consentedDate: "2023-01-13",
    status: "consumed",
    action: "/payments/consent-details/22",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "23",
    consentType: "payments",
    application: "Payments 23",
    consentedDate: "2023-03-09",
    status: "authorized",
    action: "/payments/consent-details/23",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "24",
    consentType: "payments",
    application: "Payments 24",
    consentedDate: "2023-01-22",
    status: "consumed",
    action: "/payments/consent-details/24",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
  {
    id: "25",
    consentType: "payments",
    application: "Payments 25",
    consentedDate: "2023-04-17",
    status: "authorized",
    action: "/payments/consent-details/25",
    paymentDetails: {
      amount: "100.00 USD",
      debtorReference: "DBTR-20230726",
      paymentReference: "INV-20230726",
      payeeName: "John Doe",
      sortCode: "12-34-56",
      accountNumber: "12345678",
    },
    payerInformation: {
      sortCode: "65-43-21",
      accountNumber: "87654321",
      accountName: "Jane Smith",
    },
  },
];
