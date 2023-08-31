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

import React, { Fragment, useState, useEffect, useContext, Suspense } from "react";

import { getConfigs } from "../../../../../configs.js";

import { retrieveConsent } from "../../api/ConsentAPI.js";

import Footer from "../../components/Footer.jsx";
import Header from "../../components/Header.jsx";

import { ConsentContext } from "../../context/ConsentContext.jsx";

/**
 * Display different components based on the index.
 */
const PageBuilder = () => {
    const [pages, setPages] = useState([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [isLoading, setIsLoading] = useState(true);
    const { getConsentContext, setConsentContext } = useContext(ConsentContext);

    // Get the current page component
    const { Component, ...props } = !isLoading 
    && pages.consentPages.find(item => item.Index === currentPage);

    // Get the consent data and the pages
    const getConsentData = async () => {
        retrieveConsent().then(resp => {
            setConsentContext(resp);
            setConsentContext({ selectedAccounts: [] });
            setPages(getConfigs(resp.type));
            setIsLoading(false);
        }).catch(err => console.log(err));
    }

    useEffect(() => {
        getConsentData();
    }, []);

    const Loading = () => {
        return (
            <div>Loading...</div>
        )
    }

    return (
        // For lazy loading the components
        <Suspense fallback={<Loading />}>
            <Fragment key={props.index}>
                <Header />
                {!isLoading &&
                    <Component {...props} currentPage={currentPage} setCurrentPage={setCurrentPage} />
                }
                {
                    // For displaying until consent data is loaded
                    isLoading && <Loading />
                }
                <Footer />
            </Fragment>
        </Suspense>
    );
};

export default PageBuilder;
