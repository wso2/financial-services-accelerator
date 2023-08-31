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

import React, { useContext, useEffect, useRef, useState } from "react";
import "./Navbar.scss";

// Assets
import PROFILE_PICTURE from "../../assets/profile/user-avatar.png";
import IMG_QR_CODE from "../../assets/general/qr-code.png";

// Libraries & Packages
import GlobalStateContext from "../../utils/state/GlobalStateContext";
import { Card, Popup, Text, ThemeContext } from "@bfsi-react/bfsi-ui";
import Hamburger from "hamburger-react";

// Util
import Sidebar from "../../components/Sidebar/Sidebar";
import PageDetails from "../../components/PageDetails/PageDetails";

function Navbar({ type }) {
  // State
  const { globalState } = useContext(GlobalStateContext);
  const { theme } = useContext(ThemeContext);
  const [LOGO_LG] = useState(theme.logo.lg.url);

  const [windowWidth, setWindowWidth] = useState();
  const [isOpen, setOpen] = useState(false);
  const [isQRCodeHovered, setIsQRCodeHovered] = useState(false);
  const [isSignoutHovered, setIsSignoutHovered] = useState(false);

  const [displayUserMenu, setDisplayUserMenu] = useState(false);
  const [displayQRPopup, setDisplayQRPopup] = useState(false);

  // Effect
  useEffect(() => {
    const handleResize = () => {
      setWindowWidth(window.innerWidth);
    };

    window.addEventListener("resize", handleResize);

    return () => {
      window.removeEventListener("resize", handleResize);
    };
  }, []);

  useEffect(() => {
    setWindowWidth(window.innerWidth);
  }, []);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        userMenuRef.current &&
        !userMenuRef.current.contains(event.target) &&
        event.target !== userMenuButtonRef.current
      ) {
        setDisplayUserMenu(false);
      }
    };

    document.addEventListener("click", handleClickOutside);

    return () => {
      document.removeEventListener("click", handleClickOutside);
    };
  }, []);

  // Ref
  const userMenuRef = useRef(null);
const userMenuButtonRef = useRef(null);

  return (
    <>
      <div
        className="navbar"
        style={{ backgroundColor: theme.palette.common.background }}
      >
        <div className="navbar__container">
          {/* Logo */}
          {type === "V1" && (
            <div className="navbar__logo__pageDetails">
              <img src={LOGO_LG} alt="" />
            </div>
          )}
          {/* Menu */}
          {type === "V2" && (
            <>
              <div className="navbar__menu">
                <Hamburger
                  color={theme.palette.miscellaneous.grey.main}
                  duration={0.5}
                  toggled={isOpen}
                  toggle={setOpen}
                  size={24}
                />
              </div>
              {window.innerWidth < 1000 ? (
                <div className="navbar__logo__pageDetails">
                  <img src={LOGO_LG} alt="" />
                </div>
              ) : (
                <div className="navbar__logo__pageDetails">
                  <PageDetails globalState={globalState} theme={theme} />
                </div>
              )}
            </>
          )}

          {/* User Name */}
          <div className="navbar__username">
            <p style={{ color: theme.palette.miscellaneous.grey.main }}>
              admin@wso2.com
            </p>
          </div>
          {/* User Image */}
          <div className="navbar__userimage">
            <img
              src={PROFILE_PICTURE}
              alt=""
              style={{ borderColor: theme.palette.primary.main }}
              onClick={() => setDisplayUserMenu(!displayUserMenu)}
              ref={userMenuButtonRef}
            />
          </div>
          {/* User Menu */}
          <div
            className="navbar__usermenu"
            style={{ display: displayUserMenu ? "flex" : "none" }}
            ref={userMenuRef}
          >
            <Card width="100%" height="auto" padding="none">
              <div
                className="navbar__usermenu__item"
                style={{
                  backgroundColor: isQRCodeHovered
                    ? theme.palette.primary.light
                    : theme.palette.common.white,
                  transition: "background-color 0.5s ease-in-out",
                }}
                onMouseEnter={() => setIsQRCodeHovered(true)}
                onMouseLeave={() => setIsQRCodeHovered(false)}
                onClick={() => {
                  setDisplayUserMenu(!displayUserMenu);
                  setDisplayQRPopup(true);
                }}
              >
                <Text align="center" fontSize="md">
                  QR Code
                </Text>
              </div>
              <div
                className="navbar__usermenu__item"
                style={{
                  backgroundColor: isSignoutHovered
                    ? theme.palette.primary.light
                    : theme.palette.common.white,
                  transition: "background-color 0.5s ease-in-out",
                }}
                onMouseEnter={() => setIsSignoutHovered(true)}
                onMouseLeave={() => setIsSignoutHovered(false)}
                onClick={() => setDisplayUserMenu(!displayUserMenu)}
              >
                <Text align="center" fontSize="md">
                  Signout
                </Text>
              </div>
            </Card>
          </div>
        </div>
      </div>
      {windowWidth <= 1000 && (
        <div
          className="navbar__mobile__menu"
          style={
            isOpen
              ? {
                  backgroundColor: theme.palette.common.background,
                  transform: "translateX(0px)",
                  transition: "all 0.5s ease-in-out",
                }
              : {
                  backgroundColor: theme.palette.common.background,
                  transform: "translateX(-1000px)",
                  transition: "all 0.5s ease-in-out",
                }
          }
        >
          <Sidebar type="mobile" />
        </div>
      )}
      <div className="navbar__QR__popup">
        <Popup visible={displayQRPopup} setVisible={setDisplayQRPopup}>
          <div className="navbar__QR__popup__image">
            <img src={IMG_QR_CODE} alt="" />
          </div>
          <div className="navbar__QR__popup__message">
            <Text align="center" color={theme.palette.miscellaneous.grey.main}>
              Scan the QR Code
            </Text>
          </div>
        </Popup>
      </div>
    </>
  );
}

export default Navbar;
