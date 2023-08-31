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

import React, { useContext, useState } from "react";

import DialogBox from "../../components/DialogBox.jsx";
import ListView from "../../components/ListView.jsx";

import { ConsentContext } from "../../context/ConsentContext.jsx";

import "../../styles/Consent.scss";

/**
 * Page to display permissions, expiry date and accounts selected for accounts consent flow.
 */
const AccountConsent = (props) => {
    const { currentPage, setCurrentPage } = props;

    // DialogBox properties
    const [open, setOpen] = useState(false);

    // Get consent context from the context provider
    const { getConsentContext, setConsentContext } = useContext(ConsentContext);
    const context = getConsentContext();

    // Get the application details from the context
    const app = context.application;
    const permissions = context.consentData[0].data.map((permission) => permission.replace(/([A-Z])/g, ' $1').trim());
    const expiry = new Date(context.consentData[1].data[0]);
    const accounts = [];
    context.accounts.forEach((item) => {
        if (context.selectedAccounts.includes(item.account_id)) {
            accounts.push(item.display_name);
        }
    });

    return (
        <>
            <div className="display">
                <div className="title">
                    <strong>{app}</strong> requests account details on your account.
                </div>
                <h4>Data requested</h4>
                <ListView title="Permissions" array={permissions} />
                <ListView title="Expiry date" array={[expiry.toLocaleDateString(), expiry.toLocaleTimeString()]} />
                <ListView title="Accounts selected" array={accounts} />
                <div>
                    If you want to stop sharing data, you can request us to stop sharing data on your data sharing
                    dashboard. <br /><br />
                    Do you confirm that we can share your data with {app}?
                </div>
                <div className='btngrp'>
                    <input type="button" value="Confirm" className="selection-btn"
                        onClick={() => setOpen(true)} />
                    <input type="button" value="Back" className="selection-btn"
                        onClick={() => setCurrentPage(currentPage - 1)} />
                </div>
            </div>
            {open && <DialogBox
                setOpen={setOpen}
                alert={"Do you want to give consent to " + app + "?"}
                dialogtype="confirm"
                consent={true}
            />}
        </>
    );
};

export default AccountConsent;
