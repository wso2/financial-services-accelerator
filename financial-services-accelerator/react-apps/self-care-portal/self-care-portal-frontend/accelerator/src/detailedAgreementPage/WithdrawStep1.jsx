/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

import React, {useEffect, useContext} from "react";
import {Link, useLocation} from "react-router-dom";
import Container from "react-bootstrap/Container";
import "../css/Buttons.css";
import "../css/DetailedAgreement.css";
import "../css/withdrawal.css";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faExclamationTriangle} from "@fortawesome/free-solid-svg-icons";
import {withdrawLang, specConfigurations} from "../specConfigs";
import ProgressBar from "react-bootstrap/ProgressBar";
import {FourOhFourError} from "../errorPage/index.js";
import {getDisplayName} from "../services";
import { ConsentContext } from "../context/ConsentContext";

export const WithdrawStep1 = ({ match }) => {
  const {allContextConsents} = useContext(ConsentContext);

  const consents = allContextConsents.consents;

  useEffect(() => {
    window.history.pushState(null, "", '/consentmgr');
    window.onpopstate = function () {
      window.location.href='/consentmgr';
    };
  }, []);

  const matchedConsentId = match.params.id;

  var matchedConsent;
  var applicationName;
  var consentStatus;
  var consentConsentId;
  var consent;

  matchedConsent = consents.data.filter(
    (consent) => consent.consentId === matchedConsentId
  );

  consent = matchedConsent[0];
  applicationName = consent.softwareClientName;
  consentStatus = consent.currentStatus;
  consentConsentId= consent.consentId;

  const location = useLocation();

  return (
    <>
      {consentStatus.toLowerCase() === specConfigurations.status.authorised.toLowerCase() ? (
        <Container fluid className="withdrawContainer">
          <div className="withdrawTitle">
            <FontAwesomeIcon
              className="withdrawWarnIcon fa-5x"
              icon={faExclamationTriangle}
            />
            <h4 className="withdrawalHeading">
              Stop sharing data with {applicationName}
            </h4>
            <ProgressBar now={50} label="1" />
            <p className="infoHeading">{withdrawLang.infoHeading}</p>
          </div>
          <div className="withdrawInfo">
            <h6 className="subHeadings">
              <li>{withdrawLang.impactHeading}</li>
            </h6>
            <p>{withdrawLang.impactInfo}</p>
            <h6 className="subHeadings">
              <li>{withdrawLang.sharedDataHandling}</li>
            </h6>
            <p>{withdrawLang.sharedDataHandlingPara1}</p>
            <p>{withdrawLang.sharedDataHandlingPara2}</p>
          </div>

          <div className="actionButtons" id="withdrawStep1ActionBtns">
            <div className="actionBtnDiv">
              <Link
                to = {`/consentmgr/${consentConsentId }`}
                className="comButton"
                id="withdrawFlowBackBtn"
              >
                {withdrawLang.backBtn}
              </Link>
            </div>
            <div className="actionBtnDiv">
              <Link
                className="withdrawBtn"
                id="withdrawBtn1"
                to={{
                  pathname: `/consentmgr/${consentConsentId}/withdrawal-step-2`,
                  state: { prevPath: location.pathname },
                }}
              >
                {withdrawLang.nextBtnStep1}
              </Link>
            </div>
          </div>
        </Container>
      ) : (
        <FourOhFourError />
      )}
    </>
  );
};
