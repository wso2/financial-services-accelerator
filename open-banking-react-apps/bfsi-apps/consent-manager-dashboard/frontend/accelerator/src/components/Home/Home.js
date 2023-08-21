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
import "./Home.scss";

// Assets
import IMG_ACCOUNT_INFORMATION from "../../assets/icons/accountant.svg";
import IMG_CONFIRMATION_OF_FUNDS from "../../assets/icons/bookkeeping.svg";
import IMG_PAYMENTS from "../../assets/icons/budget.svg";

// Constants
import {
  ACCOUNT_INFORMATION,
  CONFORMATION_OF_FUNDS,
  PAYMENTS,
} from "../../utils/constants/OpenBankingConstants";

// Components
import Navbar from "../../components/Navbar/Navbar";

// Libraries & Packages
import { Link } from "react-router-dom";
import { Button, Card, ThemeContext } from "@bfsi-react/bfsi-ui";

function Home() {
  // State
  const { theme } = useContext(ThemeContext);

  return (
    <div
      className="home"
      style={{ backgroundColor: theme.palette.common.background }}
    >
      <div className="home__navbar">
        <Navbar type="V1" />
      </div>
      <div className="home__body">
        <div className="home__body__container">
          {/* Title */}
          <div className="home__body__title">
            <p style={{ color: theme.palette.miscellaneous.grey.main }}>
              Welcome, admin@wso2.com
            </p>
          </div>
          {/* Cards */}
          <div className="home__body__cards">
            {/* Account Information */}
            <div className="home__body__card">
              <Card height="100%" width="100%" padding="40px">
                <div className="home__body__card__image">
                  <img src={IMG_ACCOUNT_INFORMATION} alt="" />
                </div>
                <div className="home__body__card__button">
                  <Link
                    to={`/${ACCOUNT_INFORMATION}`}
                    style={{ textDecoration: "none" }}
                  >
                    <Button width="100%">Account Information</Button>
                  </Link>
                </div>
              </Card>
            </div>
            {/* Confirmation of Funds */}
            <div className="home__body__card">
              <Card height="100%" width="100%" padding="40px">
                <div className="home__body__card__image">
                  <img src={IMG_CONFIRMATION_OF_FUNDS} alt="" />
                </div>
                <div className="home__body__card__button">
                  <Link
                    to={`/${CONFORMATION_OF_FUNDS}`}
                    style={{ textDecoration: "none" }}
                  >
                    <Button width="100%">Confirmation of Funds</Button>
                  </Link>
                </div>
              </Card>
            </div>
            {/* Payments */}
            <div className="home__body__card">
              <Card height="100%" width="100%" padding="40px">
                <div className="home__body__card__image">
                  <img src={IMG_PAYMENTS} alt="" />
                </div>
                <div className="home__body__card__button">
                  <Link to={`/${PAYMENTS}`} style={{ textDecoration: "none" }}>
                    <Button width="100%">Payments</Button>
                  </Link>
                </div>
              </Card>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Home;
