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

import React, {useContext, useEffect, useState } from "react";
import { Home } from "../landing_page/index";
import { CONFIG } from "../config";
import User from "../data/User";
import { UserContext } from "../context/UserContext";

export const Login = () => {
  const {setContextUser} = useContext(UserContext);

  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setLoggedUser] = useState({});

  useEffect(() => {
    // this object contains user details
    let user = new User();
    setLoggedUser(user);
    setIsLoggedIn(user.isLogged);
    setIsLoading(false);
    setContextUser(user);
  },[]);

  const renderLoading = () => {
    return (
      <div className="loaderBackground">
          <div className="loader"></div>  
        </div>
    );
  };

  if (isLoading) {
    // rendering loading spinner
    return renderLoading();
  } else {
    return isLoggedIn ? <Home {...user} /> : (window.location.href =`${CONFIG.AUTHORIZE_ENDPOINT}`);
  }
};
