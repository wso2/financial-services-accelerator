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

import { ManageButton } from "../landing_page";
import { dataOrigins, dataTypes, lang } from "../specConfigs";
import moment from "moment";
import { getValueFromApplicationInfoWithFailOver, getValueFromConsentWithFailOver } from "../services/utils";
import { ConsentContext } from "./../context/ConsentContext";
import React, { useContext, useState, useEffect } from "react";

let id = 0;

export const TableBody = ({ statusTab, consentType }) => {
    let index = 0;
    const { allContextConsents } = useContext(ConsentContext);
    const consents = allContextConsents.consents;

    const [filteredTab, setFilteredTab] = useState(() => {
        return lang[consentType].filter((lbl) => lbl.id === statusTab)[0];
    });

    useEffect(() => {
        setFilteredTab(lang[consentType].filter((lbl) => lbl.id === statusTab)[0]);
    }, [consentType]);

    function renderRespectiveConfiguredValue(header, valueToView) {

        // Add label or background color based on the status
        let status = String(valueToView).toLowerCase();
        let color = "";
        switch (status) {
            case "consumed":
            case "expired":
                color = "#757373";
                break;
            case "authorised":
                color = "#7FC008";
                break;
            case "revoked":
                color = "#fc030f";
                break;
            default:
                color = undefined;
        }

        if (header.dataType === dataTypes.timestamp) {
            // timestamp value to view
            return (
                <td key={index = index + 1}>
                    {moment(new Date(valueToView * 1000)).format(header.dateFormat)}
                </td>
            );
        } else if (header.dataType === dataTypes.date) {
            // date value to view
            return (
                <td key={index = index + 1}>
                    {moment(valueToView).format(header.dateFormat)}
                </td>
            );
        } else {
            // raw text value to view
            return <td key={index = index + 1} style={{color:color, fontWeight:color ? "bold" : undefined}}>{valueToView}</td>;
        }
    }

    return (
        <tbody key={index = index + 1}>
        {consents.data.length <= 0 ? (
            <tr id="noConsentsLbl" key={index = index + 1}>
                <td id="lbl" colSpan={4} key={index = index + 1}>
                    No {filteredTab && filteredTab.label} consents to display
                </td>
            </tr>
        ) : (
            consents.data.map((consent) => (
                <tr key={index = index + 1}>
                    {filteredTab &&
                        filteredTab.tableHeaders.map((header) => {
                            if (header.dataOrigin === dataOrigins.action) {
                                return <ManageButton consentId={consent.consentId} />;
                            } else if (
                                header.dataOrigin === dataOrigins.consent ||
                                header.dataOrigin === dataOrigins.status
                            ) {
                                let valueFromConsent = getValueFromConsentWithFailOver(
                                    header.dataParameterKey,
                                    header.failOverDataParameterKey,
                                    consent
                                );
                                if (!valueFromConsent) {
                                    return <td key={index = index + 1} />;
                                }

                                return renderRespectiveConfiguredValue(header, valueFromConsent)

//                             } else if (header.dataOrigin === dataOrigins.applicationInfo) {
//                                 let valueFromAppInfo = getValueFromApplicationInfoWithFailOver(
//                                     header.dataParameterKey,
//                                     header.failOverDataParameterKey,
//                                     consent.clientId,
//                                     appInfo
//                                 );
//                                 if (!valueFromAppInfo) {
//                                     return <td key={index = index + 1} />;
//                                 }
//                                 return renderRespectiveConfiguredValue(header, valueFromAppInfo)
//

                            } else {
                                return <td key={index = index + 1} />;
                            }
                        })}
                </tr>
            ))
        )}
        </tbody>
    );
};
