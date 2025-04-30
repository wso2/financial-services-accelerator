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

import { permissionDataLanguage } from "../specConfigs/permissionDataLanguage.js";
import React, { useEffect, useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCaretDown, faCaretUp } from "@fortawesome/free-solid-svg-icons";
import { Accordion, Card, Col } from "react-bootstrap";

export const PermissionItem = ({ permissionScope }) => {
    console.log("permissionScope", permissionScope);
    const [showDetailedPermissions, setShowDetailedPermissions] = useState(false);
    const [permissionsData, setPermissionsData] = useState([]);

    useEffect(() => {
        const hasPermissions = permissionScope.length > 0;

        if (hasPermissions) {
            const updatedDataLanguage = permissionDataLanguage.map((data) => {
//                 data.dataCluster === "Permissions"  && Array.isArray(permissionScope)
                if (typeof permissionScope === 'string') {
                    console.log("Array-Permissions", permissionScope);
//                     const permissionsWithDescriptions = permissionScope.map((permission) => {
//                         const permissionData = {
//                             name: permission,
//                             description: getPermissionDescription(permission),
//                         };
//
//                         console.log("Array-permissionData", permissionData);
//                         return permissionData;
//
//                     });
                    const permissionData = {
                        name: permissionScope,
                        description: getPermissionDescription(permissionScope),
                    };
                    console.log("permissionsWithDescriptions", permissionData);
                    return {
                        ...data,
                        permissions: permissionData,
                    };
                }
                return data;
            });
            console.log("updatedDataLanguage", updatedDataLanguage);
            setPermissionsData(updatedDataLanguage);
        } else {
            setPermissionsData([]);
        }
    }, [permissionScope]);

    const getPermissionDescription = (permission) => {
        console.log("getPermissionDescription", permission)
        const permissionObject = permissionDataLanguage.find(
            (data) => data.scope === permission
        );

        if (permissionObject) {

            console.log("permissionObject.description", permissionObject.permissions);
            return permissionObject.permissions;
        }

        return ""; // Default description if no match found
    };
    const toggle = () => setShowDetailedPermissions(!showDetailedPermissions);

    // must add  conditional statements for data clusters and permissions
    // when response is adjusted to receive the customer type (business, individual)

    return (
        <>
            <Accordion>
                {permissionsData.map((data, index) => (
                    <Card key={index} className="clusterContainer">
                        <Accordion.Toggle
                            className="clusterRow"
                            onClick={toggle}
                            as={Card.Header}
                            eventKey={index.toString()}
                        >
                            <Col className="clusterLabel">
                                <h6>{data.dataCluster}</h6>
                            </Col>
                            <Col className="arrow">
                                <FontAwesomeIcon
                                    className="clusToggle fa-lg"
                                    id="clusterToggle"
                                    icon={showDetailedPermissions ? faCaretDown : faCaretUp}
                                />
                            </Col>
                        </Accordion.Toggle>
                        <Accordion.Collapse eventKey={index.toString()}>
                            <Card.Body>
                                <h6>{data.title}</h6>
                                {data.permissions.length > 0 ? (
                                    <ul className="permissionsUL">
                                        {data.permissions.map((permission, permissionIndex) => (
                                            <li key={permissionIndex}>
                                                {permission.description && permission.description != null
                                                    && (<span className="permissionDescription">{permission.description}</span>
                                                )}
                                                {data.dataCluster === "Your Account Details" && (
                                                    <>
                                                        <span>{permission}</span>
                                                    </>
                                                )}
                                            </li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p>No permissions available.</p>
                                )}
                            </Card.Body>
                        </Accordion.Collapse>
                    </Card>
                ))}
            </Accordion>
        </>
    );
};
