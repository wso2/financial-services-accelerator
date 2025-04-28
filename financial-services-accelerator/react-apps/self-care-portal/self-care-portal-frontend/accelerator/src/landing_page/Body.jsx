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

import React, {useState} from "react";
import {Container, Row} from "react-bootstrap";
import "../css/Body.css";

import {LandingTabs} from "../landing_page";
import {AdvanceSearch} from "./AdvanceSearch.jsx";
import {Leftbar} from "./Leftbar.jsx";

export const Body = () => {
    const [title, setTitle] = useState("Account Information");
    return (
            <div className="parent-componenet" >
                <Leftbar setTitle={setTitle}/>
                <Container className="boxContainer">
                    <div className="box">
                        <div className="titleBar">
                            <h4 className="titleName">{title}</h4>
                            <Row className="infoSearchRow">
                                <div className="searchBox">
                                    <AdvanceSearch/>
                                </div>
                            </Row>
                        </div>
                        <LandingTabs/>
                    </div>
                </Container>
            </div>
    );
};
