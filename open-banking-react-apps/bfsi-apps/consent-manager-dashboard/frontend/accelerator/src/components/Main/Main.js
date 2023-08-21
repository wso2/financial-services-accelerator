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

import React, { useContext, useEffect, useState } from "react";
import "./Main.scss";

// Components
import Navbar from "../../components/Navbar/Navbar";
import Sidebar from "../../components/Sidebar/Sidebar";

// Libraries & Packages
import Color from "color";
import GlobalStateContext from "../../utils/state/GlobalStateContext";
import { Button, Input, ThemeContext } from "@bfsi-react/bfsi-ui";
import ConsentTable from "../../components/ConsentTable/ConsentTable";

function Main({ data: propData }) {
  const { columns, data } = propData;

  // State
  const { theme } = useContext(ThemeContext);
  const { globalState } = useContext(GlobalStateContext);
  const [sidebarWidth] = useState({ opened: "256px", closed: "56px" });
  const [displaySidebar, setDisplaySidebar] = useState(window.innerWidth);
  const [displayContentOnPageLoad, setDisplayContentOnPageLoad] =
    useState(false);

  // Effect
  useEffect(() => {
    const handleResize = () => {
      setDisplaySidebar(window.innerWidth > 1000);
    };
    handleResize();

    window.addEventListener("resize", handleResize);
    return () => {
      window.removeEventListener("resize", handleResize);
    };
  });

  useEffect(() => {
    setDisplayContentOnPageLoad(true);
  }, []);

  return (
    <>
      {displayContentOnPageLoad && (
        <div
          className="main"
          style={{ backgroundColor: theme.palette.common.background }}
        >
          {/* Sidebar */}
          <div
            className="main__sidebar"
            style={{
              backgroundColor: theme.palette.common.white,
              boxShadow:
                "0px 0px 10px 0px " +
                Color(theme.palette.primary.main).alpha(0.1),
              width: globalState.isSidebarOpen
                ? sidebarWidth.opened
                : sidebarWidth.closed,
              // display: displaySidebar ? "flex" : "none",
              transition: displaySidebar && "width 0.5s ease-in-out",
            }}
          >
            <Sidebar />
          </div>
          {/* Main */}
          <div
            className="main__content"
            style={{
              width: !displaySidebar
                ? "100%"
                : globalState.isSidebarOpen
                ? `calc(100% - ${sidebarWidth.opened})`
                : `calc(100% - ${sidebarWidth.closed})`,
              transition: displaySidebar && "width 0.5s ease-in-out",
            }}
          >
            {/* Navbar */}
            <div className="main__navbar">
              <Navbar type="V2" />
            </div>
            {/* Consent Table */}
            <div className="main__consentTable">
              <ConsentTable columns={columns} data={data} />
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default Main;
