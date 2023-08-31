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
import { Link } from "react-router-dom";

import CheckList from "../../components/CheckList.jsx";
import DialogBox from "../../components/DialogBox.jsx";

import { ConsentContext } from "../../context/ConsentContext.jsx";

import "../../styles/Consent.scss";

/**
 * Page for account selection.
 */
const AccountSelection = (props) => {
    const { currentPage, setCurrentPage } = props;

    // DialogBox properties
    const [open, setOpen] = useState(false);
    const [alert, setAlert] = useState("");
    const [dialogtype, setDialogtype] = useState("");

    // Get consent context from the context provider
    const { getConsentContext, setConsentContext } = useContext(ConsentContext);
    const context = getConsentContext();

    // Get the application details from the context
    const app = context.application;
    const accounts = [];
    context.accounts.forEach((item) => {
        accounts.push({
            id: item.account_id,
            name: item.display_name,
            checked: context.selectedAccounts.includes(item.account_id)
        });
    });

    const handleApprove = async () => {
        context.selectedAccounts = [];

        // Get the selected accounts
        document.getElementsByName("chkAccounts").forEach((account) => {
            if (account.checked) {
                context.selectedAccounts.push(account.id);
            }
        });

        // Check if any account is selected
        if (context.selectedAccounts.length === 0) {
            setOpen(true);
            setAlert("Please select the required accounts to approve");
            setDialogtype("alert");
        } else {
            setCurrentPage(currentPage + 1);
        }
    };

    const handleDeny = () => {
        setOpen(true);
        setAlert("Do you want to Deny giving consent to " + app + "?");
        setDialogtype("confirm");
    }

    return (
        <>
            <div className="display" >
                <div className="title">
                    <strong>{app}</strong> requests account details on your account.
                </div>
                <p>Select the accounts you wish to authorise:</p>
                <CheckList checkboxOptions={accounts} />
                <div className="btngrp">
                    <input type="button" className="btn"
                        onClick={() => handleApprove()} value="Approve" />
                    <input type="reset" value="Deny" className="btn"
                        onClick={() => handleDeny()} />
                </div>
                <div>
                    By approving you agree to our {" "}
                    <Link to='/policy/privacy' className="link">
                        Privacy Policy
                    </Link>
                </div>
            </div>
            {open && <DialogBox
                setOpen={setOpen}
                alert={alert}
                dialogtype={dialogtype}
                consent={false}
            />}
        </>
    );
};

export default AccountSelection;
