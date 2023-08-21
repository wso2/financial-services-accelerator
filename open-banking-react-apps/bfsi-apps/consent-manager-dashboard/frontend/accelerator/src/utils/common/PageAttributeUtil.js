/* ----- PageAttributeUtil.js ----- */
import React from "react";

// Constants
import {
  ACCOUNT_INFORMATION,
  CONFORMATION_OF_FUNDS,
  PAYMENTS,
} from "../constants/OpenBankingConstants";

// Icons
import AccountBalanceRoundedIcon from "@mui/icons-material/AccountBalanceRounded";
import AccountBalanceWalletRoundedIcon from "@mui/icons-material/AccountBalanceWalletRounded";
import PaymentsRoundedIcon from "@mui/icons-material/PaymentsRounded";

export const setPageAttributes = (page) => {
  // Account Informations
  if (page === ACCOUNT_INFORMATION) {
    return {
      icon: <AccountBalanceRoundedIcon />,
      title: "Account Information",
    };
  }

  // Confirmation of Funds
  if (page === CONFORMATION_OF_FUNDS) {
    return {
      icon: <AccountBalanceWalletRoundedIcon />,
      title: "Confirmation Of Funds",
    };
  }

  // Payments
  if (page === PAYMENTS) {
    return {
      icon: <PaymentsRoundedIcon />,
      title: "Payments",
    };
  }

  // Fallback Data
  return {};
};
