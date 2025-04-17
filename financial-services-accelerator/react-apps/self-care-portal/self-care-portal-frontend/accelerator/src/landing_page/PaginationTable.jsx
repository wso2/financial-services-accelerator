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

import "../css/Pagination.css";
import React, {useContext, useEffect, useState} from "react";
import Col from "react-bootstrap/Col";
import Row from "react-bootstrap/Row";
import Dropdown from "react-bootstrap/Dropdown";
import DropdownButton from "react-bootstrap/DropdownButton";
import ReactPaginate from 'react-paginate';
import { UserContext } from "../context/UserContext";
import { ConsentContext } from "../context/ConsentContext";
import { AppInfoContext } from "../context/AppInfoContext";
import { SearchObjectContext } from "../context/SearchObjectContext";

export const PaginationTable = ({currentTab}) => {
    const {currentContextUser} = useContext(UserContext);
    const {allContextConsents,getContextConsentForSearch} = useContext(ConsentContext);
    const {contextAppInfo} = useContext(AppInfoContext);
    const {contextSearchObject,contextSearchUtilState,setContextSearchObject} = useContext(SearchObjectContext);

    const currentUser = currentContextUser.user;
    let searchObj = contextSearchObject;
    const consentMetadata = allContextConsents.metadata;
    const searchOnClickState = contextSearchUtilState;
    const appInfo = contextAppInfo.appInfo; 

    const [postsPerPage, setPostsPerPage] = useState(searchObj.limit);
    const [noOfPages, setNoOfPages] = useState(1);
    const [currentPage, setCurrentPage] = useState(0);

    useEffect(() => {
        window.localStorage.setItem("postsPerPage", JSON.stringify(postsPerPage));
        let search = {
            ...searchObj,
            limit: postsPerPage,
            offset: 0
        }
        setContextSearchObject(search)
        doSearchConsents(search)
        setCurrentPage(0);
    }, [postsPerPage]);

    useEffect(() => {
        setPostsPerPage(searchObj.limit);
    }, [searchObj]);

    useEffect(() => {
        setCurrentPage(0);
    }, [searchOnClickState]);

    function calculateNoOfPages() {
        if (Math.ceil(consentMetadata.total / postsPerPage) == 0) {
            return 1;
        } else {
            return Math.ceil(consentMetadata.total / postsPerPage);
        }
    }

    useEffect(() => {
        setNoOfPages(calculateNoOfPages());
    }, [consentMetadata.total, consentMetadata.count, postsPerPage]);

    function handlePagination(selectedPage) {
        let offset;
        if (selectedPage == 0) {
            offset = 0;
        } else {
            offset = selectedPage * postsPerPage;
        }
        let search = {
            ...searchObj,
            offset: offset
        }
        setContextSearchObject(search)
        doSearchConsents(search)
        setCurrentPage(selectedPage);
    }

    function doSearchConsents(search) {
        getContextConsentForSearch(search, currentUser, appInfo);
    }

    // to reset the page to 1 when tab changes
    useEffect(() => {
        setCurrentPage(0);
    }, [currentTab]);

    const handleSelectNoOfPostsPerPage = (e) => {
        setPostsPerPage(e);
    };

    return (
        <Row className="paginationRow">
            <Col className="postsPerPageCol">
                <p>Rows per page</p>

                <DropdownButton
                    alignRight
                    title={postsPerPage}
                    id="postsPerPageDropdown"
                    onSelect={handleSelectNoOfPostsPerPage}
                    className="filterDropdown"
                >
                    <Dropdown.Item className="drop" eventKey="5">5</Dropdown.Item>
                    <Dropdown.Item eventKey="10">10</Dropdown.Item>
                    <Dropdown.Item eventKey="15">15</Dropdown.Item>
                    <Dropdown.Item eventKey="20">20</Dropdown.Item>
                </DropdownButton>
            </Col>
            <Col>
                <ReactPaginate
                    pageCount={noOfPages}
                    pageRangeDisplayed={3}
                    marginPagesDisplayed={1}
                    onPageChange={(e) => handlePagination(e.selected)}
                    forcePage={currentPage}
                    containerClassName={"pagination"}
                    previousLinkClassName={"pagination__link"}
                    nextLinkClassName={"pagination__link"}
                    disabledClassName={"pagination__link--disabled"}
                    activeClassName={"pagination__link--active"}
                />
            </Col>
        </Row>
    );
};
