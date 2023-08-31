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
import { FormattedMessage } from "react-intl";

import { Dropdown } from "@bfsi-react/i18n";
import { useAuthContext } from "@bfsi-react/auth";

/**
 * Temporary Header component.
 */
const Header = ({ value, onChange, options }) => {
  const { state, signOut, isAuthenticated } = useAuthContext();

  return (
    <nav className="navbar navbar-expand-lg bg-body-tertiary">
      <div className="container-fluid">
        <div className="collapse navbar-collapse" id="navbarSupportedContent">
          <ul
            className="navbar-nav mb-2 mb-lg-0"
            style={{ marginLeft: "auto" }}
          >
            <li className="nav-item dropdown me-auto">
              <Dropdown value={value} onChange={onChange} options={options} />
            </li>
            {isAuthenticated && (
              <li className="nav-item dropdown">
                <a
                  className="nav-link dropdown-toggle"
                  href="#"
                  role="button"
                  data-bs-toggle="dropdown"
                  aria-expanded="false"
                >
                  {state.username}
                </a>
                <ul className="dropdown-menu">
                  <li>
                    <button
                      type="button"
                      className="btn"
                      onClick={() => signOut()}
                    >
                      <FormattedMessage
                        id="app.common.header.logout"
                        defaultMessage="Logout"
                      />
                    </button>
                  </li>
                </ul>
              </li>
            )}
          </ul>
        </div>
      </div>
    </nav>
  );
};

export default Header;
