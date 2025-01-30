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

import React, { createContext, useState } from 'react';
import {CONFIG} from "../config"
import { specConfigurations } from '../specConfigs';
import { consentTypes } from '../specConfigs';

export const SearchObjectContext = createContext();

const SearchObjectContextProvider = (props) => {
    const [contextSearchObject,setContextSearchObject] = useState({
        limit: JSON.parse(window.localStorage.getItem("postsPerPage")) || CONFIG.NUMBER_OF_CONSENTS,
        offset: 0,
        dateRange: "",
        consentIDs: "",
        userIDs: "",
        clientIDs: "",
        consentStatuses: specConfigurations.status.authorised,
        consentTypes: consentTypes[0].id, // Accelerator only supporting the account consents type in SCP.
        hideAdvanceSearchOptions: true
    });

    const [contextSearchUtilState,setContextSearchUtilState] = useState({
        searchOnClick:true
    }) 

    const value ={
        contextSearchObject,
        setContextSearchObject,
        contextSearchUtilState,
        setContextSearchUtilState
    }

    return (
        <SearchObjectContext.Provider value = {value} > 
            {props.children}
        </SearchObjectContext.Provider>
    );
}
 
export default SearchObjectContextProvider;
