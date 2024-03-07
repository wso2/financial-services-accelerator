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

import React from 'react';
import moment from 'moment';
import { Container } from 'react-bootstrap';
import { Download } from 'react-bootstrap-icons';
import { Link } from 'react-router-dom';

import { generatePDF, getExpireTimeFromConsent } from '../services/utils';
import ADRLogo from '../images/ADRLogo.png';
import { specConfigurations } from '../specConfigs/specConfigurations';
import { withdrawLang } from '../specConfigs';

export const ProfileMain = ({ consent, infoLabel, appicationName, logoURL }) => {
  const consentConsentId = consent.consentId;
  const currentDate = moment().format('YYYY-MM-DDTHH:mm:ss[Z]');

  if (logoURL === undefined || logoURL === '') {
    logoURL = ADRLogo;
  }

  function isNotExpired() {
    try {
      let expireTimeFromConsent = getExpireTimeFromConsent(consent, 'YYYY-MM-DDTHH:mm:ss[Z]');
      if (!expireTimeFromConsent) {
        return true;
      }
      return moment(currentDate).isBefore(expireTimeFromConsent);
    } catch (e) {
      return true;
    }
  }

  const consentStatusLabel =
    consent.currentStatus.toLowerCase() === specConfigurations.status.authorised.toLowerCase() &&
    !isNotExpired()
      ? specConfigurations.status.expired
      : infoLabel.label;
  return (
    <Container className="profileMain">
      <img id="profileLogo" src={logoURL} width="50" height="50" alt="new" />
      <h4 className="mt-3">{appicationName}</h4>
      <>
        <div className="confirmLink">
          <a
            id="confirmationReportLink"
            href="javascript:void(0);"
            onClick={() => generatePDF(consent, appicationName, consentStatusLabel)}
          >
            {`${infoLabel.profile.confirmation} `}
            <Download />
          </a>
        </div>
        {consent.currentStatus.toLowerCase() ===
          specConfigurations.status.authorised.toLowerCase() && isNotExpired() ? (
          <div className="actionButtons">
            <div className="actionBtnDiv">
              <Link
                to={`/consentmgr/${consentConsentId}/withdrawal-step-1`}
                className="withdrawBtn"
              >
                {withdrawLang.detailedConsentPageStopSharingBtn}
              </Link>
            </div>
          </div>
        ) : (
          <div className="actionButtons"></div>
        )}
      </>
    </Container>
  );
};
