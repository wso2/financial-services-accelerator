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
import "../css/Nav.css";
import wso2Logo from "../images/wso2Logo.png";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Container from "react-bootstrap/Col";
import userAvatar from "../images/userAvatar.png";
import Image from "react-bootstrap/Image";
import NavDropdown from "react-bootstrap/NavDropdown";
import { Link } from "react-router-dom";
import { logout } from "../login/logout.js";
import { QRButton } from "../landing_page/Popup.jsx";
import Popup from "reactjs-popup";

export const Nav = (user) => {

  const handleLogout = () => {
    logout(user.idToken)
  };

  const showQR = () => {
    return (<div> Show QR </div>)
  }

  return (
    <Container className = "nv">
      <Row className="Navbar">
        <Link to="/consentmgr/" id="navLinkStyle">
          {
            <Col className="branding">
              <img
                alt="Logo"
                src={wso2Logo}
                className="d-inline-block align-top navLogoImage"
              />
              <span className="navAppName"> Consent Manager </span>
            </Col>
          }
        </Link>
        <Col className="NavDropdown">
          <NavDropdown
            id="dropdown-custom-components"
            title={
              <span>
                <Image
                  src={userAvatar}
                  alt="User Avatar"
                  className="navUserImage"
                  rounded
                />
                <span className="dropdown-userId">{
                    (user.email && user.email.indexOf("@" + CONFIG.TENANT_DOMAIN) !== -1) ?
                    user.email.replace("@" + CONFIG.TENANT_DOMAIN, "") : user.email
                }</span>
              </span>
            }
          >
            <NavDropdown.Item id="dropdown-menu-items">
              <Popup
                modal
                overlayStyle={{ background: "rgba(255,255,255,0.98" }}
                closeOnDocumentClick={true}
                trigger={showQR}
              >
              <QRButton/>
              </Popup>
            </NavDropdown.Item>
            <NavDropdown.Item onClick={handleLogout} id="dropdown-menu-items">
              Log out
            </NavDropdown.Item>
          </NavDropdown>
        </Col>
      </Row>
    </Container>
  );
};
