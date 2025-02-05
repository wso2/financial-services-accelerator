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

import React, {useContext, useEffect, useState} from "react";
import {Link} from "react-router-dom";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import {Profile, SharingDetails} from "../detailedAgreementPage";
import "../css/Buttons.css";
import "../css/DetailedAgreement.css";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCaretSquareLeft} from "@fortawesome/free-solid-svg-icons";
import {lang, specConfigurations} from "../specConfigs/specConfigurations";
import {getDisplayName} from "../services";
import {getLogoURL} from "../services/utils";
import { UserContext } from "../context/UserContext";
import { ConsentContext } from "../context/ConsentContext";
import { AppInfoContext } from "../context/AppInfoContext";
import { SearchObjectContext } from "../context/SearchObjectContext";

export const DetailedAgreement = ({match}) => {
    const {currentContextUser} = useContext(UserContext);
    const {getContextConsentForSearch,allContextConsents} = useContext(ConsentContext);
    const {contextAppInfo} = useContext(AppInfoContext);
    const {contextSearchObject} = useContext(SearchObjectContext)

    let searchObj = contextSearchObject;
    const currentUser = currentContextUser.user;
    const consents = allContextConsents.consents;
    const appInfo = contextAppInfo.appInfo;

    const [consentTypeKey, setConsentTypeKey] = useState(searchObj.consentTypes);
    const [consent, setConsent] = useState(() => {
        setConsentTypeKey(searchObj.consentTypes);
        let search = {
            ...searchObj,
            limit: 1,
            offset: 0,
            dateRange: "",
            consentIDs: match.params.id,
            userIDs: "",
            clientIDs: "",
            consentStatuses: "",
            consentTypes: ""
        }
        getContextConsentForSearch(search,currentUser,appInfo);
        const matchedConsentId = match.params.id;
        let matchedConsent = consents.data.filter(
            (consent) => consent.consentId === matchedConsentId
        );
        return matchedConsent[0];
    });

    const [applicationName, setApplicationName] = useState(() => {
        return getDisplayName(appInfo, consent.clientId);
    });
    const [logoURL, setLogoURL] = useState(() => {
        return getLogoURL(appInfo, consent.clientId);
    });
    const [infoLabel, setInfoLabel] = useState(() => {
        const labels = lang[consentTypeKey].filter((lbl) =>
            lbl.id.split(",").some(x => x.toLowerCase() === consent.currentStatus.toLowerCase()));
        return getInfoLabel(labels[0], consent);
    });

    useEffect(() => {
        setApplicationName(getDisplayName(appInfo, consent.clientId));
    }, [appInfo]);

    useEffect(() => {
        const matchedConsentId = match.params.id;
        let matchedConsent = consents.data.filter(
            (consent) => consent.consentId === matchedConsentId
        );
        setConsent(matchedConsent[0]);
    }, [consents]);

    useEffect(() => {
        const labels = lang[consentTypeKey].filter((lbl) =>
            lbl.id.split(",").some(x => x.toLowerCase() === consent.currentStatus.toLowerCase()));
        setInfoLabel(getInfoLabel(labels[0], consent));
    }, [consent]);

    function getInfoLabel(currentLabel, consent) {
        // check consent's mapping statuses are active
        const authResources = consent.authorizationResources;
        if (Array.isArray(authResources) && authResources.length) {
            // consent has more than one authorization resources
            const currentAuthResources = authResources.filter(ar => ar.userId === currentUser.email);
            if (Array.isArray(currentAuthResources) && currentAuthResources.length > 0) {
                const currentAuthResource = currentAuthResources[0];
                const mappings = consent.consentMappingResources
                    .filter(mapping => mapping.authorizationId === currentAuthResource.authorizationId);
                if (mappings.every(consentMapping => consentMapping.mappingStatus === "inactive")) {
                    // every consent mapping is inactive
                    return lang[consentTypeKey].filter((lbl) =>
                        lbl.id.split(",").some(x => x.toLowerCase() === consent.currentStatus.toLowerCase()))[0];
                }
            }
        }
        return currentLabel;
    }

    return (
        <Container fluid className="body">
            <Row>
                <Link to="/consentmgr/consents/:id" id="detailedPageBackBtn">
                    <FontAwesomeIcon
                        className="pageBackBtn fa-2x"
                        icon={faCaretSquareLeft}
                    />
                    {/* Back */}
                </Link>
            </Row>

            <Row id="detailRow">
                <Col sm={4} id="profileCol">
                    <Profile consent={consent} infoLabel={infoLabel} appicationName={applicationName}
                             logoURL={logoURL} consentType={consentTypeKey}/>
                </Col>
                <Col id="consentDetailCol">
                    <SharingDetails consent={consent} infoLabels={infoLabel} consentType={consentTypeKey}/>
                </Col>
            </Row>
        </Container>
    );
};
