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

// TODO: Need to invoked based on the error response from servlet like timeout or failure in update

/**
 * Page to redirect in case of timeout to resubmit the form
 */

import React, { useContext } from "react";

import { ConsentContext } from "../../context/ConsentContext.jsx";

import { handleConfirm } from "../../utils/functions.js";

import "../../styles/Consent.scss";

const ConfirmConsent = () => {
    // Get consent context from the context provider
    const { getConsentContext, setConsentContext } = useContext(ConsentContext);
    const context = getConsentContext();

    const handleApprove = async () => {
        await handleConfirm(context.data);
    };

    return (
        <div className="display">
            <p>You will be redirected back to the {context.application}.
                If the redirection fails, please click the post button...</p>
            <div className='btngrp'>
                <input type="button" className="btn"
                       onClick={() => handleApprove()} value="POST" />
            </div>
        </div>
    );
};

export default ConfirmConsent;
