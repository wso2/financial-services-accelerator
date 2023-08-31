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

import React from "react";
import { Routes, Route } from "react-router-dom";
import { ReactQueryDevtools } from "react-query/devtools";
import { QueryClientProvider, QueryClient } from "react-query";

import { LocaleProvider, useLocale } from "@bfsi-react/i18n";
import { AuthProvider, SecureRoutes } from "@bfsi-react/auth";
import { ThemeProvider } from "@bfsi-react/bfsi-ui";

import Login from "./components/login.jsx";
import ConsentPage from "./pages/consent-page.jsx";

import { authConfig } from "./configs/auth-configs.js";
import { localeConfig } from "./configs/locale-configs.js";

// Pages
import HomePage from "./pages/HomePage/HomePage";
import AccountInformationPage from "./pages/AccountInformation/AccountInformationPage/AccountInformationPage";
import ConfirmationOfFundsPage from "./pages/ConfirmationOfFunds/ConfirmationOfFundsPage/ConfirmationOfFundsPage";
import PaymentsPage from "./pages/Payments/PaymentsPage/PaymentsPage";
import AccountInformationConsentDetailsPage from "./pages/AccountInformation/AccountInformationConsentDetailsPage/AccountInformationConsentDetailsPage";
import ConfirmationOfFundsCosentDetailsPage from "./pages/ConfirmationOfFunds/ConfirmationOfFundsConsentDetailsPage/ConfirmationOfFundsConsentDetailsPage";
import PaymentsCosentDetailsPage from "./pages/Payments/PaymentsConsentDetailsPage/PaymentsConsentDetailsPage";
import GlobalStateProvider from "./utils/state/GlobalStateProvider.js";

function App() {
  const { getActiveLocale, changeLocale, supportedLocales } =
    useLocale(localeConfig);
  const activeLocale = getActiveLocale();

  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        refetchOnWindowFocus: false,
        keepPreviousData: true,
      },
    },
  });

  return (
    <LocaleProvider locale={activeLocale}>
      <AuthProvider config={authConfig}>
        <QueryClientProvider client={queryClient}>
          <ThemeProvider>
            <GlobalStateProvider>
              <div className="App">
                <Routes>
                  {/* Protected Routes */}
                  <Route
                    element={<SecureRoutes redirectPath="/consentmgr/login" />}
                  >
                    <Route
                      element={
                        <ConsentPage
                          activeLocale={activeLocale.code}
                          supportedLocales={supportedLocales}
                          changeLocale={changeLocale}
                        />
                      }
                      path="/consentmgr"
                      exact
                    />
                  </Route>

                  {/* Common Routes */}
                  <Route element={<Login />} path="/consentmgr/login" />

                  {/* Payments */}
                  <Route path="/payments" element={<PaymentsPage />} />
                  <Route
                    path="/payments/consent-details/:ID"
                    element={<PaymentsCosentDetailsPage />}
                  />

                  {/* Confirmation of Funds */}
                  <Route
                    path="/confirmation-of-funds"
                    element={<ConfirmationOfFundsPage />}
                  />
                  {/* Confirmation of Funds - Consent Details*/}
                  <Route
                    path="/confirmation-of-funds/consent-details/:ID"
                    element={<ConfirmationOfFundsCosentDetailsPage />}
                  />
                  {/* Account Information */}
                  <Route
                    path="/account-information"
                    element={<AccountInformationPage />}
                  />
                  {/* Account Information - Consent Details*/}
                  <Route
                    path="/account-information/consent-details/:ID"
                    element={<AccountInformationConsentDetailsPage />}
                  />
                  {/* Home */}
                  <Route path="/" element={<HomePage />} />
                </Routes>
              </div>
            </GlobalStateProvider>
          </ThemeProvider>
          {/* By default, React Query Devtools are only included in bundles
            when process.env.NODE_ENV === 'development' */}
          <ReactQueryDevtools initialIsOpen={false} />
        </QueryClientProvider>
      </AuthProvider>
    </LocaleProvider>
  );
}

export default App;
