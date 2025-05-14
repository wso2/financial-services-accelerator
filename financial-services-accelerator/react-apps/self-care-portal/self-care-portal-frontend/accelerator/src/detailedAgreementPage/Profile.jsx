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

import React, {useEffect, useState} from "react";
import {Accreditation, ProfileMain, StatusLabel} from "../detailedAgreementPage";

import "../css/Profile.css";
import "../css/Buttons.css";
import {getExpireTimeFromConsent} from "../services/utils";

export const Profile = ({consent, infoLabel, appicationName, logoURL, consentType}) => {


    const [expireTime, setExpireTime] = useState(() => {
        return getExpireTimeFromConsent(consent, "YYYY-MM-DDTHH:mm:ss[Z]");
    });

    useEffect(() => {
        setExpireTime(getExpireTimeFromConsent(consent, "YYYY-MM-DDTHH:mm:ss[Z]"));
    }, [consent]);


    return (
        <>
            <div className="profileBody">
                <StatusLabel
                    infoLabel={infoLabel}
                    expireDate={expireTime}
                />
                <ProfileMain consent={consent} infoLabel={infoLabel} appicationName={appicationName}
                             logoURL={logoURL}/>
                <hr className="horizontalLine"/>
                <div className="infoBox">
                    <Accreditation infoLabel={infoLabel} accreditationNumber={appicationName} applicationName={appicationName}/>
                </div>
                <div className="infoBox">
                    <h6>Other important information</h6>
                    <p>
                        There may be additional important information not shown here. Please
                        check this sharing arrangement of {appicationName}’s
                        website/app.
                    </p>
                </div>
            </div>
        </>
    );
};
