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

// TODO: Need to be tested with the UK toolkit

import React, { useContext, useState } from "react";

import DialogBox from "../../components/DialogBox.jsx";
import ListView from "../../components/ListView.jsx";

import { ConsentContext } from "../../context/ConsentContext.jsx";

import "../../styles/Consent.scss";

/**
 * Page to display permissions, expiry date and accounts selected for payments consent flow.
 */
const PaymentConsent = (props) => {
    const { currentPage, setCurrentPage } = props;

    // DialogBox properties
    const [open, setOpen] = useState(false);

    // Get consent context from the context provider
    const { getConsentContext, setConsentContext } = useContext(ConsentContext);
    const context = getConsentContext();

    // Get the application details from the context
    const app = context.application;
    const InstructedAmount = Object.entries(context.Data.Initiation.InstructedAmount);
    const DebtorAccount = Object.entries(context.Data.Initiation.DebtorAccount);
    const CreditorAccount = Object.entries(context.Data.Initiation.CreditorAccount);
    const accounts = [];
    context.authorizableResources.forEach((item) => {
        if (context.selectedAccounts.includes(item.accountId)) {
            accounts.push(item.accountName);
        }
    });

    return (
        <>
            <div className="display">
                <div className="title">
                    <strong>{app}</strong> requests account details on your account.
                </div>
                <h4>Data requested for payments</h4>
                <ListView title="Instructed Amount" array={InstructedAmount} />
                <ListView title="Debtor Account" array={DebtorAccount} />
                <ListView title="Creditor Account" array={CreditorAccount} />
                <ListView title="Accounts selected" array={accounts} />
                <div>
                    If you want to stop sharing data, you can request us to stop sharing data on your data sharing
                    dashboard. <br /><br />
                    Do you confirm that we can share your data with {app}?
                </div>
                <div className='btngrp'>
                    <input type="button" value="Confirm" className="btn"
                        onClick={() => setOpen(true)} />
                    <input type="button" value="Back" className="btn"
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

export default PaymentConsent;
