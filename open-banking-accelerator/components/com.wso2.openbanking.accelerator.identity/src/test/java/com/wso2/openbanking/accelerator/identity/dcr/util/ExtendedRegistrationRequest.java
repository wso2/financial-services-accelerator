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
package com.wso2.openbanking.accelerator.identity.dcr.util;

import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationRequest;
import com.wso2.openbanking.accelerator.identity.dcr.model.SoftwareStatementBody;
import com.wso2.openbanking.accelerator.identity.dcr.validation.DCRCommonConstants;
import com.wso2.openbanking.accelerator.identity.common.annotations.validationGroups.MandatoryChecks;

import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * Extended registration request.
 */
public class ExtendedRegistrationRequest extends RegistrationRequest {

    private RegistrationRequest registrationRequest;

    ExtendedRegistrationRequest(RegistrationRequest registrationRequest) {

        this.registrationRequest = registrationRequest;
    }

    @Override
    @NotNull(message = "Redirect URIs can not be null:" + DCRCommonConstants.INVALID_META_DATA,
            groups = MandatoryChecks.class)
    public List<String> getCallbackUris() {

        return registrationRequest.getCallbackUris();
    }

    @Override
    public String getIssuer() {

        return registrationRequest.getIssuer();
    }

    @Override
    public String getTokenEndPointAuthentication() {

        return registrationRequest.getTokenEndPointAuthentication();
    }

    @Override
    public List<String> getGrantTypes() {

        return registrationRequest.getGrantTypes();
    }

    @Override
    public String getSoftwareStatement() {

        return registrationRequest.getSoftwareStatement();
    }

    @Override
    public String getIdTokenSignedResponseAlg() {

        return registrationRequest.getIdTokenSignedResponseAlg();
    }

    @Override
    public SoftwareStatementBody getSoftwareStatementBody() {

        return registrationRequest.getSoftwareStatementBody();
    }

}
