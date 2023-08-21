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

import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import { ErrorBoundary } from "react-error-boundary";

import { LocaleProvider, useLocale } from "@bfsi-react/i18n";

import { localeConfig } from "./configs/LocaleConfigs.js";

import ConsentProvider from "./pages/consentPages/ConsentProvider.jsx";
import FourOhFourError from "./pages/errorPages/FourOhFour.jsx";
import GenericError from "./pages/errorPages/GenericError.jsx";
import PolicyProvider from "./pages/policyPages/PolicyProvider.jsx";


import "./styles/App.scss";

function App() {
    const { getActiveLocale } = useLocale(localeConfig);
    const activeLocale = getActiveLocale();

    return (
        <LocaleProvider locale={activeLocale}>
            <ErrorBoundary fallback={<GenericError />}>
                <Router basename="/consentapproval">
                    <div className="App">
                        <Routes>
                            <Route exact path="/" element={<ConsentProvider />} />
                            <Route path="/policy/*" element={<PolicyProvider />} />
                            <Route path="/*" element={<FourOhFourError />} />
                        </Routes>
                    </div>
                </Router>
            </ErrorBoundary>
        </LocaleProvider>
    );
}

export default App;
