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

import {specConfigurations, dataTypes} from "../specConfigs";
import moment from "moment";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";

export function getDisplayName(appInfo, clientId) {
    try {
        let disName = appInfo[clientId].metadata[specConfigurations.application.displayNameAttribute];
        if (disName !== undefined && disName != "") {
            return disName;
        } else {
            disName = appInfo.data[clientId].metadata[specConfigurations.application.failOverDisplayNameAttribute];
            return disName;
        }
    } catch (e) {
        return clientId;
    }
}

export function getValueFromConsent(key, consent) {
    try {
        let value = consent;
        key.toString().split(".").map((section) => {
            value = value[section];
        })
        return value;
    } catch (e) {
        return ""
    }
}

export function getValueFromConsentWithFailOver(key, failOverKey, consent) {
    try {
        let valueFromConsent = getValueFromConsent(key, consent);
        if (valueFromConsent !== undefined && valueFromConsent != "") {
            return valueFromConsent;
        } else {
            return getValueFromConsent(failOverKey, consent);
        }
    } catch (e) {
        return ""
    }
}

export function getValueFromApplicationInfo(key, clientId, appInfo) {
    try {
        return appInfo[clientId].metadata[key];
    } catch (e) {
        return ""
    }
}

export function getValueFromApplicationInfoWithFailOver(key, failOverKey, clientId, appInfo) {
    try {
        let valueFromAppInfo = getValueFromApplicationInfo(key, clientId, appInfo);
        if (valueFromAppInfo !== undefined && valueFromAppInfo != "") {
            return valueFromAppInfo;
        } else {
            return getValueFromApplicationInfo(failOverKey, clientId, appInfo);
        }
    } catch (e) {
        return ""
    }
}

export function getLogoURL(appInfo, clientId) {
    try {
        return appInfo.data[clientId].metadata[specConfigurations.application.logoURLAttribute];
    } catch (e) {
        return "";
    }
}

export function getExpireTimeFromConsent(consent, format) {
    try {
        const expirationTime = getValueFromConsent
        (specConfigurations.consent.expirationTimeAttribute, consent);
        if (expirationTime === "" || expirationTime === undefined) {
            return "";
        }
        if (dataTypes.timestamp === specConfigurations.consent.expirationTimeDataType) {
            return moment(new Date(expirationTime * 1000)).format(format)
        }
        return moment(expirationTime).format(format)
    } catch (e) {
        return "";
    }
}

export function generatePDF(consent, applicationName, consentStatus) {

    const pdf = new jsPDF("p", "mm", "a4");
    let content01 = "";
    let content02 = "";
    let content03 = "";

    const input = document.getElementsByClassName('permissionsUL');

    if (input.length > 0) {
        try {
            content01 = input[0].innerHTML.split("<li>");
            content01 = content01.join("").split("</li>");
            for (let i = 0; i < content01.length; i++) {
                content01[i] = content01[i].replace(/(<([^>]+)>)/ig, "");
            }
        } catch (e) {
        }

        try {
            content02 = input[1].innerHTML.split("<li>");
            content02 = content02.join("").split("</li>");
            for (let i = 0; i < content02.length; i++) {
                content02[i] = content02[i].replace(/(<([^>]+)>)/ig, "");
            }
        } catch (e) {
        }
    }

    try {
        let debtorList = [];
        consent.consentMappingResources.map((account) =>
            account.mappingStatus === "active" ? debtorList.push(account.accountId) : <> </>
        );
        content03 = debtorList.join('\n');
    } catch (e) {
    }

    pdf.setFontSize(14);
    pdf.text(20, 20, 'Consent information for consent ID: ' + consent.consentId);

    pdf.setFontSize(11);
    let expireTime = getExpireTimeFromConsent(consent, "DD-MMM-YYYY");
    if (expireTime === "" || expireTime === undefined) {
        expireTime = 'No Expiry';
    }

    pdf.text(20, 30, 'Basic Consent Details: ')
    autoTable(pdf, {
        startY :40,
        margin: {left: 20, right: 20 },
        border: {top: 2, right: 2, bottom: 2, left: 2},
        body: [
          ['Status', consent.currentStatus],
          ['API Consumer Application ', applicationName],
          ['Create date', moment(new Date((consent.createdTimestamp) * 1000)).format("DD-MMM-YYYY")],
          ['Expire date', expireTime],
          ['Accounts', content03]
        ],
    });

    let body = [];

    if (content01.length > 0) {
        body.push(['Your Account Details', content01.join('\n')]);
    }
    if (content02.length > 0) {
        body.push(['Permissions', content02.join('\n')]);
    }

    if (content01.length > 0 || content02.length > 0) {
        pdf.text(20, 90, 'Data we are sharing on: ');
        autoTable(pdf, {
            startY :100,
            margin: { left: 20, right: 20 },
            border: {top: 2, right: 2, bottom: 2, left: 2},
            headStyles: { fillColor: [112, 123, 124] },
            head: [['Data Sharing Category', 'Data Shared']],
            body: body,
        });
    }

    pdf.save("consent.pdf");
}

export function userIdAdjustment(userId) {

    return typeof userId === 'string' ? userId.replace("@carbon.super", "") : userId;
}
