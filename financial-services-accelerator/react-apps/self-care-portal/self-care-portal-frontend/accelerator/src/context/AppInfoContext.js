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

import React, { createContext, useContext, useState } from 'react';
import { getApplicationInfo } from '../api';
import { UserContext } from './UserContext';

export const AppInfoContext = createContext();

const AppInfoContextProvider = (props) => {
    const [contextAppInfo , setAppInfo] = useState({
        isGetRequestLoading:false,
        appInfo:[]
    });

    const {setResponseError} = useContext(UserContext)

    const setContextAppInfo = (payload)=> {
        setAppInfo((contextAppInfo)=>({
            ...contextAppInfo,
            appInfo:payload
        }))
    }

    const setContextAppInfoRequestLoadingStatus = (payload) => {
        setAppInfo((contextAppInfo)=>({
            ...contextAppInfo,
            isGetRequestLoading:payload
        }))
    }
    
    const getContextAppInfo = () => {
        setContextAppInfoRequestLoadingStatus(true);
        getApplicationInfo()
            .then((response) => setContextAppInfo(response.data))
            .catch((error) => setResponseError(error.response.data))
            .finally(()=>setContextAppInfoRequestLoadingStatus(false));
    }

    const value = {
        contextAppInfo,
        getContextAppInfo
    }

    return (
        <AppInfoContext.Provider value = {value}>
            {props.children}
        </AppInfoContext.Provider>
    );
}
 
export default AppInfoContextProvider;
