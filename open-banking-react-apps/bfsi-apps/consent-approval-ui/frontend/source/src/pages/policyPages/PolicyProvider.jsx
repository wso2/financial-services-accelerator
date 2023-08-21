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

import React, { lazy, Suspense } from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";

const CookiePolicy = lazy(() => import("./CookiePolicy.jsx"));
const PrivacyPolicy = lazy(() => import("./PrivacyPolicy.jsx"));

/**
 * Display different components based on the key passed in the state.
 */
const PolicyProvider = () => {
    return (
        <Suspense fallback={<div>Loading...</div>}>
            <div className="Policy">
                <Routes>
                    <Route exact path="/privacy" element={<PrivacyPolicy />} />
                    <Route exact path="/cookie" element={<CookiePolicy />} />
                </Routes>
            </div>
        </Suspense>
    )
};

export default PolicyProvider;
