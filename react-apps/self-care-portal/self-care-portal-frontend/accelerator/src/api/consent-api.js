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

import axios from "axios";
import { CONFIG } from "../config";
import moment from "moment";
import User from "../data/User";
import Cookies from "js-cookie";
import { specConfigurations } from "../specConfigs";

/**
 * Get the list of consents from the API.
 */
export const getConsentsFromAPI = (user, consentTypes) => {
    var adminUrl;
    var defaultUrl;

    var userId = user.email

    // Accelerator only supporting the account consents type in SCP.
    adminUrl = `${CONFIG.BACKEND_URL}/admin/search?consentTypes=${consentTypes}`
    defaultUrl = `${CONFIG.BACKEND_URL}/admin/search?consentTypes=${consentTypes}&userIDs=${userId}`

    var selectedUrl
    if (user.role === "customerCareOfficer") {
        selectedUrl = adminUrl;
    } else {
        selectedUrl = defaultUrl
    }

    const requestConfig = {
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + Cookies.get(User.CONST.OB_SCP_ACC_TOKEN_P1),
        },
        method: "GET",
        url: `${selectedUrl}`,
    };
    return axios
        .request(requestConfig)
        .then((response) => {
            return Promise.resolve(response);
        })
        .catch((error) => {
            return Promise.reject(error);
        });
};


function getFromTimeFromSearchObject(dateRange) {
    if (dateRange.replace(/ /g, "") !== "") {
        let fromTime = dateRange.split("/")[0].replace(/ /g, "");
        return moment(fromTime, "DD-MMM-YYYY")
            .startOf("day")
            .unix();
    } else {
        return "";
    }
}

function getToTimeFromSearchObject(dateRange) {
    if (dateRange.replace(/ /g, "") !== "") {
        let toTime = dateRange.split("/")[1].replace(/ /g, "");
        return moment(toTime, "DD-MMM-YYYY")
            .endOf("day")
            .unix();
    } else {
        return "";
    }
}

function getClientIdsFromSoftwareProvider(softwareProvider, appInfo) {
    for (let clientId in appInfo.data) {
        if (appInfo.data.hasOwnProperty(clientId)) {
            let softwareClientName =
                appInfo.data[clientId].metadata[specConfigurations.application.displayNameAttribute];
            if (softwareProvider.toString().toLowerCase().trim() ===
                softwareClientName.toString().toLowerCase()) {
                return clientId;
            }
        }
    }
    return "*";
}

export const getConsentsFromAPIForSearch = (searchObj, user, appInfo) => {

    let currentUserEmail = user.email;

    const serverURL = `${CONFIG.BACKEND_URL}/admin/search`;

    let defaultUrl = `${serverURL}?`;
    let searchUrl
    let paramList = [
        "offset",
        "limit",
        "consentIDs",
        "accountIDs",
        "userIDs",
        "clientIDs",
        "consentStatuses",
        "consentTypes"
    ];

    // Accelerator only supporting the account consents type in SCP.
    if (user.role === "customerCareOfficer") {
        searchUrl = defaultUrl;
    } else {
        searchUrl = defaultUrl + `&userIDs=${currentUserEmail}`;
    }

    paramList.forEach(function (key, index) {
        if (searchObj.hasOwnProperty(key) && searchObj[key] !== "") {
            if (key === 'userIDs') {
                if (user.role === "customerCareOfficer") {
                    searchUrl = searchUrl + "&" + key + "=" + searchObj[key];
                } else {
                    searchUrl = searchUrl + "&" + key + "=" + currentUserEmail;
                }
            } else if (key === 'clientIDs') {
                searchUrl = searchUrl + "&" + key + "=" +
                    getClientIdsFromSoftwareProvider(searchObj[key], appInfo);
            } else {
                searchUrl = searchUrl + "&" + key + "=" + searchObj[key];
            }
        }
    });

    let fromTime = getFromTimeFromSearchObject(searchObj.dateRange);
    let toTime = getToTimeFromSearchObject(searchObj.dateRange);
    //Appending fromTime to search query
    if (fromTime !== "") {
        searchUrl = searchUrl + "&" + 'fromTime' + "=" + fromTime;
    }
    //Appending toTime to search query
    if (toTime !== "") {
        searchUrl = searchUrl + "&" + 'toTime' + "=" + toTime;
    }

    const requestConfig = {
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + Cookies.get(User.CONST.OB_SCP_ACC_TOKEN_P1),
        },
        method: "GET",
        url: `${searchUrl}`,
    };
    return axios
        .request(requestConfig)
        .then((response) => {
            // set pagination obj
            return Promise.resolve(response);
        })
        .catch((error) => {
            return Promise.reject(error);
        });
};

export const getRevokeUrl = (consentId, user) => {
    const adminUrl = `${CONFIG.BACKEND_URL}/admin/revoke?consentID=${consentId}`;
    const defaultUrl = `${CONFIG.BACKEND_URL}/admin/revoke?consentID=${consentId}&userID=${user.email}`;

    return user.role === 'customerCareOfficer' ? adminUrl : defaultUrl;
};

export const revokeConsent = (clientId, consentId, user) => {
    return axios.delete(getRevokeUrl(consentId, user), {
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${Cookies.get(User.CONST.OB_SCP_ACC_TOKEN_P1)}`,
            'x-wso2-client-id': clientId,
            'x-fapi-financial-id': 'open-bank'
        },
    });
};
