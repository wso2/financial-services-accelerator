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

//DataSharedInfo for Default application
import React from "react";
import {specConfigurations} from "../specConfigs/specConfigurations.js";
import {PermissionItem} from "../detailedAgreementPage";
import {getValueFromConsent} from "../services";
import {permissionBindTypes} from "../specConfigs/common";

let id = 0;
export const DataSharedInfo = ({consent, infoLabels}) => {

    let permissions = [];
    if (specConfigurations.consent.permissionsView.permissionBindType ===
        permissionBindTypes.samePermissionSetForAllAccounts) {
        permissions = getValueFromConsent(
            specConfigurations.consent.permissionsView.permissionsAttribute, consent)
        if (permissions === "" || permissions === undefined) {
            permissions = [];
        }
    } else {
        permissions = {};
        let detailedAccountsList = getValueFromConsent("consentMappingResources", consent);
        detailedAccountsList.map((detailedAccount) => {
            if (permissions[detailedAccount.accountId] === undefined) {
                permissions[detailedAccount.accountId] = []
                permissions[detailedAccount.accountId].push(detailedAccount.permission)
            } else {
                permissions[detailedAccount.accountId].push(detailedAccount.permission)
            }
        })
    }
    return (
        <div className="dataSharedBody">
            <h5>{infoLabels.dataSharedLabel}</h5>
            {specConfigurations.consent.permissionsView.permissionBindType ===
            permissionBindTypes.differentPermissionsForEachAccount ?
                (
                    Object.keys(permissions).map((account) => {
                        return <>
                            <h5>Account : {account}</h5>
                            <div className="dataClusters">
                                {permissions[account].map((permission) => (
                                    <PermissionItem permissionScope={permission} key={id = id + 1}/>
                                ))}
                            </div>
                        </>
                    })
                ) : (
                    <div className="dataClusters">
                        {permissions.map((permission) =>(
                            <PermissionItem permissionScope={permission} key={id = id + 1}/>
                        ))}
                    </div>
                )
            }
        </div>
    );
};
