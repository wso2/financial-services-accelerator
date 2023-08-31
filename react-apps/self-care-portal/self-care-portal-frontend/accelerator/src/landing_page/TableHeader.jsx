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

import {dataOrigins, lang} from "../specConfigs";
import React, {useState, useEffect} from "react";

export const TableHeader = ({statusTab, consentType}) => {

    const [filteredTab, setFilteredTab] = useState(() => {
        return lang[consentType].filter((lbl) => lbl.id === statusTab)[0];
    });

    useEffect(() => {
        setFilteredTab(lang[consentType].filter((lbl) => lbl.id === statusTab)[0]);
    }, [consentType])

    return (
        <thead>
        <tr>
            <>
                {filteredTab && filteredTab.tableHeaders
                    .map((header) => {
                            if (header.dataOrigin === dataOrigins.action) {
                                return <th className="headerAction">{header.heading}</th>
                            } else {
                                return <th>{header.heading}</th>
                            }
                        }
                    )}
            </>
        </tr>
        </thead>
    );
};
