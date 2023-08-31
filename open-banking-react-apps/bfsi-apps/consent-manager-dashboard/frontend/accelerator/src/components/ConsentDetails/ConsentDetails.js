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
import "./ConsentDetails.scss";

// Assets
import IMG_BANK from "../../assets/general/bank.jpg";
import IMG_ADR from "../../assets/general/ADR.svg";

// Components
import Navbar from "../../components/Navbar/Navbar";

// Libraries & Packages
import {
  Button,
  Card,
  Label,
  Title,
  Text,
  ThemeContext,
} from "@bfsi-react/bfsi-ui";
import { Link } from "react-router-dom";

// Utils
import { getConsentStatus } from "../../utils/common/ConsentStatusUtil";

function ConsentDetails({
  backURL,
  children,
  consent,
  displayPopup,
  setDisplayPopup,
}) {
  // State
  const { theme } = useContext(ThemeContext);

  return (
    <div
      className="consentDetails"
      style={{
        backgroundColor: theme.palette.common.background,
      }}
    >
      <div className="consentDetails__navbar">
        <Navbar type="V1" />
      </div>
      <div className="consentDetails__header">
        <Link to={backURL} style={{ textDecoration: "none" }}>
          <Button variant="outlined">Back</Button>
        </Link>
      </div>
      <div className="consentDetails__body">
        <div className="consentDetails__body__overview">
          <Card width="100%" height="calc(100svh - 180px)" padding="lg">
            {/*  Image */}
            <div className="consentDetails__body__overview__image">
              <img src={IMG_BANK} alt="" />
            </div>
            {/* Name */}
            <div className="consentDetails__body__overview__name">
              <Text
                color={theme.palette.miscellaneous.grey.main}
                fontSize="18px"
                align="center"
              >
                {consent?.application}
              </Text>
            </div>
            {/* Download */}
            <div className="consentDetails__body__overview__download">
              <Text
                color={theme.palette.primary.main}
                fontSize="sm"
                align="center"
                spacing="none"
              >
                View Confirmation of Consent
              </Text>
            </div>
            {/* Status */}
            <div className="consentDetails__body__overview__status">
              <Label type={getConsentStatus(consent?.status)} size="lg">
                {consent?.status}
              </Label>
            </div>
            {/* Accredition */}
            <div className="consentDetails__body__overview__accredition">
              <div className="consentDetails__body__overview__accredition__title">
                <Title text="Accredition" align="center" />
              </div>
              <div className="consentDetails__body__overview__accredition__description">
                <Text
                  color={theme.palette.miscellaneous.grey.main}
                  spacing="md"
                >
                  Service Provider 08 is an accredited data recipient. You can
                  check their accreditation at website.
                </Text>
              </div>
              <div
                className="consentDetails__body__overview__accredition__badge"
                style={{
                  backgroundColor: theme.palette.primary.light,
                }}
              >
                <div className="consentDetails__body__overview__accredition__badge__icon">
                  <img src={IMG_ADR} alt="" />
                </div>
                <div className="consentDetails__body__overview__accredition__badge__text">
                  <div className="consentDetails__body__overview__accredition__badge__text__title1">
                    <Text
                      align="left"
                      color={theme.palette.miscellaneous.grey.main}
                    >
                      Accredited Data Recipient
                    </Text>
                  </div>
                  <div className="consentDetails__body__overview__accredition__badge__text__title2">
                    <p
                      style={{
                        color: theme.palette.primary.main,
                      }}
                    >
                      {consent?.application}
                    </p>
                  </div>
                </div>
              </div>
            </div>
            {/* Other Important Information */}
            <div className="consentDetails__body__overview__information">
              <div className="consentDetails__body__overview__information__title">
                <Title text="Other Important Information" align="center" />
              </div>
              <div className="consentDetails__body__overview__information__description">
                <Text
                  color={theme.palette.miscellaneous.grey.main}
                  spacing="md"
                >
                  There maybe additional important information not shown here.
                  Please check the sharing arrangement of Service Provide 08
                  website/app.
                </Text>
              </div>
            </div>
            {/* Button */}
            {consent?.status === "active" && (
              <div className="consentDetails__body__overview__button">
                <Button
                  variant="outlined"
                  type="error"
                  onClick={() => setDisplayPopup(!displayPopup)}
                >
                  Stop Sharing
                </Button>
              </div>
            )}
          </Card>
        </div>
        <div className="consentDetails__body__details">
          <Card width="100%" height="auto" padding="lg">
            {children}
          </Card>
        </div>
      </div>
    </div>
  );
}

export default ConsentDetails;
