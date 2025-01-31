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

import {specConfigurations, dataTypes} from "../specConfigs";
import moment from "moment";
import jsPDF from "jspdf";

export function getDisplayName(appInfo, clientId) {
    try {
        let disName = appInfo.data[clientId].metadata[specConfigurations.application.displayNameAttribute];
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
        return appInfo.data[clientId].metadata[key];
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

    const pdf = new jsPDF("l", "mm", "a4");
    let content01 = "";
    let content02 = "";
    let content03 = "";
    pdf.setFontSize(11);

    const input = document.getElementsByClassName('permissionsUL');

    try {
        content01 = input[0].innerHTML.split("<li>");
        content01 = content01.join("").split("</li>").join(", ");
        content01 = content01.slice(0, -2);
    } catch (e) {
    }

    try {
        content02 = input[1].innerHTML.split("<li>");
        content02 = content02.join("").split("</li>").join(", ");
        content02 = content02.slice(0, -2);
    } catch (e) {
    }

    try {
        let debtorList = [];
        consent.consentMappingResources.map((account) =>
            account.mappingStatus === "active" ? debtorList.push(account.accountId) : <> </>
        );
        content03 = debtorList.join(",");
    } catch (e) {
    }

    pdf.text(20, 20, 'Consent infomation for consent ID: ' + consent.consentId)
    pdf.rect(15, 10, 265, 190);
    pdf.text(20, 30, "Status: " + consentStatus)
    pdf.text(20, 40, 'API Consumer Application : ' + applicationName)
    pdf.text(20, 50, 'Create date: ' + moment(new Date((consent.createdTimestamp) * 1000)).format("DD-MMM-YYYY"))
    pdf.text(20, 60, 'Expire date: ' + getExpireTimeFromConsent(consent, "DD-MMM-YYYY"));
    pdf.text(20, 70, 'Data we are sharing on: ')
    pdf.text(30, 80, 'Account name, type and balance: ')
    pdf.text(40, 90, content01)
    pdf.text(30, 100, 'Account numbers and features: ')
    pdf.text(40, 110, content02)
    pdf.text(20, 120, 'Accounts: ' + content03)
    pdf.save("consent.pdf");
}

export function userIdAdjustment(userId) {

    return userId.replace("@carbon.super", "");
}
