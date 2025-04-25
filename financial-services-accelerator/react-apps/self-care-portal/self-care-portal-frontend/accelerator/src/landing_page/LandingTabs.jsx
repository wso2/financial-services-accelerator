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

import React, {useEffect, useState, useContext} from "react";
import {LandingTable} from "./LandingTable.jsx";
import "../css/LandingTabs.css";
import Tab from "react-bootstrap/Tab";
import Tabs from "react-bootstrap/Tabs";
import {lang} from "../specConfigs";
import {PaginationTable} from "./PaginationTable.jsx";
import { UserContext } from "../context/UserContext.js";
import { ConsentContext } from "../context/ConsentContext.js";
import { SearchObjectContext } from "../context/SearchObjectContext.js";


export const LandingTabs = () => {
    const {currentContextUser} = useContext(UserContext);
    const {getContextConsentForSearch} = useContext(ConsentContext);
    const {contextSearchObject,setContextSearchObject} = useContext(SearchObjectContext);

    const [key, setKey] = useState(contextSearchObject.consentStatuses);
    const currentUser = currentContextUser.user;
    const filteredLang =  lang[contextSearchObject.consentTypes];

    useEffect(() => {
        setKey(contextSearchObject.consentStatuses)
    }, [contextSearchObject.consentTypes])

    useEffect(() => {
        let search = {
            ...contextSearchObject,
            consentStatuses: key,
            offset: 0
        }
        setContextSearchObject(search)
        getContextConsentForSearch(search, currentUser, null);
    }, [key])

    return (
        <div>
            <Tabs id="status-tab" activeKey={key} onSelect={(k) => setKey(k)}>
                {filteredLang.map(({label, id, description}) => (
                    <Tab key={id} eventKey={id} title={label}>
                        <LandingTable status={id} description={description} currentTab={{key}} consentType={contextSearchObject.consentTypes}/>
                    </Tab>
                ))}
            </Tabs>
            <PaginationTable
                currentTab={key}
            />
        </div>
    );
};
