/*
 * Copyright (c) 2021-2025, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

//AccountsInfo for Default Application
import React from "react";
import {lang, specConfigurations} from "../specConfigs/specConfigurations.js";
import {permissionBindTypes} from "../specConfigs/common";

export const AccountsInfo = ({consent, consentType}) => {

    const consentStatus = consent.currentStatus;
    const debtorAccounts = consent.consentMappingResources;
    let keyDatesConfig = lang[consentType].filter((lbl) =>
        lbl.id.toLowerCase().includes(consent.currentStatus.toLowerCase()))[0];
    return (
        <div className="accountsInfoBody">
            {specConfigurations.consent.permissionsView.permissionBindType ===
            permissionBindTypes.samePermissionSetForAllAccounts ? (
                <>
                    <h5>{keyDatesConfig.accountsInfoLabel}</h5>
                    {debtorAccounts.map((account, index) => (
                        account.mappingStatus === "active" ?
                            <li key={index}>{account.accountId}</li>
                            :
                            <> </>
                    ))}
                    <h5> {""}</h5>
                </>
            ) : (
                <></>
            )
            }

        </div>
    );
};
