/*
 * Copyright (c) 2021-2025, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

import React from 'react'
import { KeyDatesInfo, AccountsInfo, DataSharedInfo } from '../detailedAgreementPage'

import "../css/SharingDetails.css"

export const SharingDetails = ({consent, infoLabels, consentType}) => {
    return(
        <>
            <hr id = "sharingDetailsHr" className = "horizontalLine" />
            <div id = "keyDatesBox" className = "infoBox">
                <KeyDatesInfo consent = {consent} infoLabels = {infoLabels} consentType={consentType}/>
            </div>
            <div id = "accountInfoBox" className = "infoBox">
                <AccountsInfo consent = {consent} infoLabels = {infoLabels} consentType={consentType} />
            </div>
            <hr id = "sharingDetailsHr" className = "horizontalLine" />
            <div id = "dataInfoBox" className = "infoBox">
                <DataSharedInfo consent = {consent} infoLabels = {infoLabels} />
            </div>
        </>
    )
}
