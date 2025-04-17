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

import React, { useContext } from "react";
import { Card, Button } from "react-bootstrap";
import { SearchObjectContext } from "../../../accelerator/src/context/SearchObjectContext.js";
import {consentTypes, lang} from "../../../accelerator/src/specConfigs";
import { useHistory } from "react-router-dom";
import "../css/Cards.css";

export const HomeTile = () => {
    const { contextSearchObject, setContextSearchObject } = useContext(SearchObjectContext);
    let searchObj = contextSearchObject;
    const history = useHistory();
    const handleCardClick = (id) => {
        const consentType = consentTypes.find((type) => type.id === id);
        if (consentType) {
            const search = {
                ...searchObj,
                dateRange: "",
                consentIDs: "",
                userIDs: "",
                clientIDs: "",
                consentTypes: id,
                consentStatuses: lang[id][0].id,
                offset: 0,
            };
            setContextSearchObject(search);
            history.push(`/consentmgr/consents/${id}`);
        }
    };

    return (
        <>
            <div className="home-tile">
                {consentTypes.map(({ label, id, image }) => (
                    <Card className="cardView">
                        <Card.Img className="card-image" variant="top"  src={image} />
                        <Card.Body>
                            <Button className="card-button" onClick={() => handleCardClick(id)}> {label}
                            </Button>
                        </Card.Body>
                    </Card>
                ))}
            </div>
        </>
    );
}
