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

package com.wso2.openbanking.accelerator.consent.extensions.manage.impl;

import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.common.factory.AcceleratorConsentExtensionFactory;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.UUID;

/**
 * Consent manage handler default implementation.
 */

public class DefaultConsentManageHandler implements ConsentManageHandler {

    private static final Log log = LogFactory.getLog(DefaultConsentManageHandler.class);
    private static final String INTERACTION_ID_HEADER = "x-fapi-interaction-id";
    private ConsentManageRequestHandler consentManageRequestHandler;

    @Override
    public void handleGet(ConsentManageData consentManageData) throws ConsentException {

        if (consentManageData.getHeaders().containsKey(INTERACTION_ID_HEADER)) {
            consentManageData.setResponseHeader(INTERACTION_ID_HEADER,
                    consentManageData.getHeaders().get(INTERACTION_ID_HEADER));
        } else {
            consentManageData.setResponseHeader(INTERACTION_ID_HEADER,
                    UUID.randomUUID().toString());
        }

        //Check whether client ID exists
        if (StringUtils.isEmpty(consentManageData.getClientId())) {
            log.error(ErrorConstants.MSG_MISSING_CLIENT_ID);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.MSG_MISSING_CLIENT_ID);
        }

        String[] requestPathArray;

        if (consentManageData.getRequestPath() == null) {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.RESOURCE_NOT_FOUND);
        } else {
            requestPathArray = consentManageData.getRequestPath().split("/");
        }

        if (requestPathArray != null && !requestPathArray[0].isEmpty()) {
            //Get consent manage request validator according to the request path
            consentManageRequestHandler = AcceleratorConsentExtensionFactory
                    .getConsentManageRequestValidator(requestPathArray[0]);

            if (consentManageRequestHandler != null) {
                consentManageRequestHandler.handleConsentManageGet(consentManageData);
            } else {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.PATH_INVALID);
            }
        } else {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.PATH_INVALID);
        }
    }

    @Override
    public void handlePost(ConsentManageData consentManageData) throws ConsentException {

        //set consent id aa response header
        String consentID = UUID.randomUUID().toString();
        if (consentManageData.getHeaders().containsKey(INTERACTION_ID_HEADER)) {
            consentManageData.setResponseHeader(INTERACTION_ID_HEADER,
                    consentManageData.getHeaders().get(INTERACTION_ID_HEADER));
        } else {
            consentManageData.setResponseHeader(INTERACTION_ID_HEADER, consentID);
        }

        //Get consent manage request validator according to the request path
        consentManageRequestHandler = AcceleratorConsentExtensionFactory
                .getConsentManageRequestValidator(consentManageData.getRequestPath());

        if (consentManageRequestHandler != null) {
            consentManageRequestHandler.handleConsentManagePost(consentManageData);
        } else {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.PATH_INVALID);
        }
    }

    @Override
    public void handleDelete(ConsentManageData consentManageData) throws ConsentException {

        if (consentManageData.getHeaders().containsKey(ConsentExtensionConstants.INTERACTION_ID_HEADER)) {
            consentManageData.setResponseHeader(ConsentExtensionConstants.INTERACTION_ID_HEADER,
                    consentManageData.getHeaders().get(ConsentExtensionConstants.INTERACTION_ID_HEADER));
        } else {
            consentManageData.setResponseHeader(ConsentExtensionConstants.INTERACTION_ID_HEADER,
                    UUID.randomUUID().toString());
        }

        String[] requestPathArray;
        if (consentManageData.getRequestPath() == null) {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.RESOURCE_NOT_FOUND);
        } else {
            requestPathArray = consentManageData.getRequestPath().split("/");
        }

        if (requestPathArray != null && !requestPathArray[0].isEmpty()) {

            //Get consent manage request validator according to the request path
            consentManageRequestHandler = AcceleratorConsentExtensionFactory
                    .getConsentManageRequestValidator(requestPathArray[0]);

            if (consentManageRequestHandler != null) {
                consentManageRequestHandler.handleConsentManageDelete(consentManageData);
            } else {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.PATH_INVALID);
            }
        }
    }

    @Override
    public void handlePut(ConsentManageData consentManageData) throws ConsentException {

        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, "Method PUT is not supported");
    }

    @Override
    public void handlePatch(ConsentManageData consentManageData) throws ConsentException {

        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, "Method PATCH is not supported");
    }

    @Override
    public void handleFileUploadPost(ConsentManageData consentManageData) throws ConsentException {

        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, "File upload is not supported");
    }

    @Override
    public void handleFileGet(ConsentManageData consentManageData) throws ConsentException {

        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, "File retrieval is not supported");
    }
}
