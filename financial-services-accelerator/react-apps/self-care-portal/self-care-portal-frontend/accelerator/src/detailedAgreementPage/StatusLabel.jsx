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

import React, {useEffect, useState} from "react";
import Badge from "react-bootstrap/Badge";
import {specConfigurations} from "../specConfigs/specConfigurations";
import "../css/StatusLabel.css";
import moment from "moment";

export const StatusLabel = ({infoLabel, expireDate}) => {

    const date_create = moment().format("YYYY-MM-DDTHH:mm:ss[Z]");
    const [statusForLbl, setStatusForLbl] = useState("Active");
    const [badge, setBadge] = useState("success");


    function defaultStatusLabel() {
        setBadge(infoLabel.labelBadgeVariant);
        setStatusForLbl(infoLabel.label);
    }

    useEffect(() => {
        try {
            if (!expireDate) {
                defaultStatusLabel();
            } else if (infoLabel.id === specConfigurations.status.authorised &&
                !moment(date_create).isBefore(moment(expireDate))) {
                setBadge("secondary");
                setStatusForLbl(specConfigurations.status.expired);
            } else {
                defaultStatusLabel();
            }
        } catch (e) {
            defaultStatusLabel();
        }
    }, [infoLabel])


    return (
        <div className="statuslbl">
            <Badge className="badge" variant={badge}>
                {statusForLbl}
            </Badge>
        </div>
    );
};
