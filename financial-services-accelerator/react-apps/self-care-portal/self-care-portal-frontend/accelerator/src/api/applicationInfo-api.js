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

import axios from "axios";
import {CONFIG} from "../config";

/**
 * Get the service provider application information.
 */
export const getApplicationInfo = async (clientIdList) => {
    var serverURL = CONFIG.SERVER_URL
    const requestConfig = {
        method: "GET",
        headers: {
//            "Authorization": "Basic aXNfYWRtaW5Ad3NvMi5jb206d3NvMjEyMw==",
            "Authorization": "Bearer " + Cookies.get(User.CONST.OB_SCP_ACC_TOKEN_P1)
        },
        url: `${serverURL}/admin/applications?attributes=advancedConfigurations,clientId`
    };

    return await axios
        .request(requestConfig)
        .then((response) => {
            let modifiedAppResponse = constructAppResponse(response.data.applications);
            return Promise.resolve(modifiedAppResponse);
        })
        .catch((error) => {
            return Promise.reject(error);
        });
};

export function constructAppResponse(applications) {
    const data = {};

    applications.forEach(application => {
        const metadata = {};

        application.advancedConfigurations?.additionalSpProperties?.forEach(property => {
            metadata[property.name] = property.value;
        });

        data[application.clientId] = { metadata };
    });

    let appInfo = {
        data: data
    };

    return appInfo;
}
