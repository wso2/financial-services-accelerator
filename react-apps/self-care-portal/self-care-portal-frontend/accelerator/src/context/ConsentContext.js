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

import React, { createContext, useContext, useState } from 'react';
import { getConsentsFromAPI, getConsentsFromAPIForSearch } from '../api';
import { UserContext } from './UserContext';
import { getModifiedConsentData } from "../services/extendableUtils";

export const ConsentContext = createContext();
const ConsentContextProvider = (props) => {
    const [allContextConsents,setAllContextConsents] = useState({
        isGetRequestLoading: false,
        consents: [],
        metadata: {
            total: 0,
            count: 0,
        }
    });

    const {setResponseError} = useContext(UserContext);

    const setContextConsents= (payload) => {
        setAllContextConsents((allContextConsents)=>({
            ...allContextConsents,
            consents:payload
        }))
    };

    const setContextConsentsRequestLoadingStatus = (payload) => {
        setAllContextConsents((allContextConsents)=>({
            ...allContextConsents,
            isGetRequestLoading:payload
        }))
    };

    const setContextConsentsMetadata = (payload) => {
        setAllContextConsents((allContextConsents)=>({
            ...allContextConsents,
            metadata: {
                total: payload.metadata.total,
                count: payload.metadata.count
            }
        }))
    };

    const getContextConsents = (user,consentTypes) => {
        setContextConsentsRequestLoadingStatus(true)
        getConsentsFromAPI(user,consentTypes)
            .then((response)=>{
                setContextConsents(response.data)
                setContextConsentsMetadata(response.data)
            })
            .catch((error)=>{
               setResponseError(error.response.data)
            })
            .finally(()=>setContextConsentsRequestLoadingStatus(false))
    };

    const getContextConsentForSearch = (searchObj,user,appInfo)=>{
        setContextConsentsRequestLoadingStatus(true)
        getConsentsFromAPIForSearch(searchObj,user,appInfo)
            .then((response)=>{
                setContextConsents(response.data)
                setContextConsentsMetadata(getModifiedConsentData(response.data))
            })
            .catch((error)=>{
                setResponseError(error.response.data)
            })
            .finally(()=>setContextConsentsRequestLoadingStatus(false))
    };

    const value = {
        allContextConsents,
        setContextConsents,
        setContextConsentsRequestLoadingStatus,
        setContextConsentsMetadata, 
        getContextConsents,
        getContextConsentForSearch
    };

    return (
        <ConsentContext.Provider value = {value}>
            {props.children}
        </ConsentContext.Provider>
    );
}
 
export default ConsentContextProvider;
