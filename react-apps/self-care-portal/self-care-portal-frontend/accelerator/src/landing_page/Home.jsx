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

import React, {useEffect, useContext} from "react";
import {Nav, Footer} from "../common/index.js";
import {Body} from "../landing_page";
import {Switch, Route} from "react-router-dom";
import {DetailedAgreement, WithdrawStep1, WithdrawStep2, ProtectedWithdrawRoute} from "../detailedAgreementPage";
import {FourOhFourError} from '../errorPage/index.js'
import {BrowserRouter as Router} from "react-router-dom";
import {ResponseError} from "../errorPage/index.js";
import { UserContext } from "../context/UserContext";
import { ConsentContext } from "../context/ConsentContext";
import { AppInfoContext } from "../context/AppInfoContext";
import { HomeTile } from "./HomeTile.jsx";
import { consentTypes } from '../specConfigs';

export const Home = (user) => {
    const {currentContextUser} = useContext(UserContext); 
    const {allContextConsents,getContextConsents} = useContext(ConsentContext);
    const {getContextAppInfo} = useContext(AppInfoContext);

    const consents = allContextConsents.consents;
    const error = currentContextUser.error;
    // Default consent type to view : accounts
    // We are only supporting the account consents in SCP.
    const consentType = consentTypes[0].id;

    useEffect(() => {
        getContextConsents(user,consentType);
    }, [user]);

    useEffect(() => {
        if (consents.length !== 0) {
            getContextAppInfo();
        }
    }, [consents]);

    if (error) {
        return <ResponseError error={error}/>
    }

return (
    <>
      <div className="home">
        {consents.length === 0 ? (
          <div className="loaderBackground">
            <div className="loader"></div>
          </div>
        ) : (
          <Router>
            <div className="home-content">
              <Nav {...user} />
              <Switch>
                <Route path="/consentmgr" exact component={HomeTile}/>
                <Route path="/consentmgr/consents/:id" exact component={Body}/>
                <Route path="/consentmgr/:id" exact component={DetailedAgreement}/>
                <Route path="/consentmgr/:id/withdrawal-step-1" exact component={WithdrawStep1}/>
                <ProtectedWithdrawRoute path="/consentmgr/:id/withdrawal-step-2" exact component={WithdrawStep2}/>
                <Route path="*">
                  <FourOhFourError/>
                </Route>
              </Switch>
            </div>
          </Router>
        )}
      </div>
      <Footer/>
    </>
  );
}
