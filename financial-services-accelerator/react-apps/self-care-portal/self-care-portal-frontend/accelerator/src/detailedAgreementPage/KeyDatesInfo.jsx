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

import {lang, specConfigurations} from "../specConfigs/specConfigurations";
import {keyDateTypes} from "../specConfigs/common";
import React from "react";
import moment from "moment";
import {getValueFromConsent} from "../services";
import {getExpireTimeFromConsent} from "../services/utils";

export const KeyDatesInfo = ({consent, infoLabels, consentType}) => {

    let keyDatesConfig = infoLabels;
    const consentStatus = consent.currentStatus;
    const currentDate = moment().format("YYYY-MM-DDTHH:mm:ss[Z]");
    const expirationDateTime = getExpireTimeFromConsent(consent, "YYYY-MM-DDTHH:mm:ss[Z]")
    let isExpired = (expirationDateTime !== "") ? moment(currentDate).isAfter(moment(expirationDateTime)) : false;

    if (consentStatus === specConfigurations.status.authorised && isExpired) {
        keyDatesConfig = lang[consentType].filter((lbl) =>
            lbl.id.toLowerCase().includes(specConfigurations.status.expired.toLowerCase()))[0];
    }

    let keyDatesMap = keyDatesConfig.keyDates.map((keyDate) => {
        if (keyDate.type == keyDateTypes.date) {
            try {
                let timestamp = getValueFromConsent(keyDate.dateParameterKey, consent);
                // Get timestamp in millis
                timestamp = getLongTimestampInMillis(timestamp);
                return (
                    <>
                        <h6 className="keyDateTitle">{keyDate.title}</h6>
                        <li className="infoItem keyDateValue">{(timestamp !== 0)? moment(timestamp).format(keyDate.dateFormat) : "N/A"}</li>
                    </>
                )
            } catch (e) {
                return (
                    <>
                        <h6 className="keyDateTitle">{keyDate.title}</h6>
                        <li className="infoItem keyDateValue"></li>
                    </>
                )
            }
        } else if (keyDate.type == keyDateTypes.dateRange) {
            try {
                let timeRanges = keyDate.dateParameterKey.split(",")
                let fromTime = getValueFromConsent(timeRanges[0], consent);
                let toTime = getValueFromConsent(timeRanges[1], consent);

                // Get timestamp in millis
                fromTime = getLongTimestampInMillis(fromTime);
                toTime = getLongTimestampInMillis(toTime);

                return (
                    <>
                        <h6 className="keyDateTitle">{keyDate.title}</h6>
                        <li className="infoItem keyDateValue">{moment(fromTime).format(keyDate.dateFormat)} - {(toTime !== 0)?moment(toTime).format(keyDate.dateFormat):"N/A"}</li>
                    </>
                )
            } catch (e) {
                return (
                    <>
                        <h6 className="keyDateTitle">{keyDate.title}</h6>
                        <li className="infoItem keyDateValue"></li>
                    </>
                )
            }
        } else if (keyDate.type == keyDateTypes.value) {
            try {
                let valueParameterKey = keyDate.valueParameterKey;
                let valueFromConsent = getValueFromConsent(valueParameterKey, consent);

                if (!valueFromConsent || Object.keys(valueFromConsent).length === 0) {
                    valueFromConsent = "N/A";
                } else if (valueParameterKey === "receipt.Data.Initiation.InstructedAmount") {
                    valueFromConsent = `${valueFromConsent.Amount} ${valueFromConsent.Currency}`;
                } else if (valueParameterKey === "consentAttributes.sharing_duration_value") {
                    const intValue = parseInt(valueFromConsent, 10);
                    if (intValue >= 0 && intValue <= 86400) {
                        valueFromConsent = "OnceOff";
                    } else {
                        valueFromConsent = "Ongoing";
                    }
                }

                return (
                    <>
                        <h6 className="keyDateTitle">{keyDate.title}</h6>
                        <li className="infoItem keyDateValue">{valueFromConsent}</li>
                    </>
                )
            } catch (e) {
                return (
                    <>
                        <h6 className="keyDateTitle">{keyDate.title}</h6>
                        <li className="infoItem keyDateValue"></li>
                    </>
                )
            }
        } else {
            return (
                <>
                    <h6 className="keyDateTitle">{keyDate.title}</h6>
                    <li className="infoItem keyDateValue">{keyDate.text}</li>
                </>
            )
        }
    });

    // Method to convert epoch second timestamps to epoch millis
    function getLongTimestampInMillis(timestamp) {
        if (timestamp.toString().length === 10) {
            timestamp = timestamp * 1000;
        }
        return timestamp;
    }

    return (
        <div className="keyDatesBody">
            <h5>{keyDatesConfig.keyDatesInfoLabel}</h5>
            <div className="row justify-content-between">
                {keyDatesMap.map((item, index) => (
                    <div className="col-sm-6 pl-0" key={index}>
                    {item}
                    </div>
                ))}
          </div>
        </div>
    );
};
