/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

import React, {useContext, useEffect, useState} from "react";
import "../css/Leftbar.css";
import {Nav, NavLink, NavItem} from "react-bootstrap";
import {SearchObjectContext} from "../context/SearchObjectContext";
import { ConsentContext } from "../context/ConsentContext";
import { AppInfoContext } from "../context/AppInfoContext";
import { UserContext } from "../context/UserContext"
import {lang, consentTypes} from "../specConfigs";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faBars} from '@fortawesome/free-solid-svg-icons';
import Home from "../images/home.png";
import { useHistory } from "react-router-dom";

/**
 * Renders the left side navigation bar component.
 */
export const Leftbar = ({setTitle}) => {

    const [isOpen, setIsOpen] = useState(true);
    const toggle = () => setIsOpen(!isOpen);
    const {contextSearchObject, setContextSearchObject} = useContext(SearchObjectContext);
    const {contextAppInfo} = useContext(AppInfoContext);
    const {currentContextUser} = useContext(UserContext);
    const {getContextConsentForSearch} = useContext(ConsentContext);
    let searchObj = contextSearchObject;
    const appInfo = contextAppInfo.appInfo;
    const [consentTypeKey, setConsentTypeKey] = useState(searchObj.consentTypes);
    const currentUser = currentContextUser.user;

    useEffect(() => {
        let search = {
            ...searchObj,
            dateRange: "",
            consentIDs: "",
            userIDs: "",
            clientIDs: "",
            consentTypes: consentTypeKey,
            consentStatuses: lang[consentTypeKey][0].id,
            offset: 0
        }
        setContextSearchObject(search);
        getContextConsentForSearch(search, currentUser, appInfo);
        // Update the title based on the selected consent type
        setTitle(consentTypes.find(type => type.id === consentTypeKey).label);
    },  [consentTypeKey]);

    const history = useHistory();
    const handleHomeClick = () => {
        // Navigate to the HomeTile page
        history.push("/consentmgr");
    };
    const handleNavItemClick = (id) => {
        history.push(`/consentmgr/consents/${id}`);
    };

    return (
        <div className="leftbar" style={{width: isOpen ? "260px" : "60px"}} >
            <div className="leftbar-options">
                <div className="top-section">
                    <FontAwesomeIcon icon={faBars} className="bars" onClick={toggle}/>
                </div>
                <Nav className="flex-column" activeKey={consentTypeKey} onSelect={(consentKey) => {setConsentTypeKey(consentKey)}}>
                    <NavItem>
                        <NavLink className="list-options"  activeClassName="active" onClick={handleHomeClick}>
                            <img src={Home} className="Lefticon"/>
                            <div style={{display: isOpen ? "block" : "none"}}  className="text-option">Home</div>
                        </NavLink>
                    </NavItem>
                    {consentTypes.map(({label, id, image}) => (
                        <Nav.Item key={id}>
                            <NavLink eventKey={id} className="list-options"  activeClassName="active" onClick={() => handleNavItemClick(id)}>
                                <img src={image} className="Lefticon"/>
                                <div style={{display: isOpen ? "block" : "none"}}  className="text-option">{label}</div>
                            </NavLink>
                        </Nav.Item>
                    ))}
                </Nav>
            </div>
        </div>
    );
}
