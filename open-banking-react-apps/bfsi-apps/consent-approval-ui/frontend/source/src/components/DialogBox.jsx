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

import React, { useContext } from "react";

import { ConsentContext } from "../context/ConsentContext.jsx";

import { handleConfirm } from "../utils/functions.js";

import "../styles/DialogBox.scss";

/**
 * Can be used as a dialog box to display alerts and get confirmation.
 * Need to pass dialogtype property to know whether it is an alert or confirm type.
 */
const DialogBox = (props) => {
    const { setOpen, alert, dialogtype, consent } = props;

    // Get consent context from the context provider
    const { getConsentContext, setConsentContext } = useContext(ConsentContext);
    const context = getConsentContext();

    const handleContinue = async () => {
        let data = {
            "approval": consent,
            "accountIds": consent==true? context.selectedAccounts: [""]
        };

        setConsentContext({data: data});

        await handleConfirm(data);
    }

    return (
        <div className="alert_page">
            <div className="alert">
                <p>{alert}</p>
                <div className="btngrp">
                    {dialogtype==="confirm" && <input type="button" className="btn"
                        onClick={() => handleContinue()} value="Continue" />}
                    <input type="button" className="btn"
                        onClick={() => setOpen(false)} value="Close" />
                </div>
            </div>
        </div>
    );
}

export default DialogBox;
