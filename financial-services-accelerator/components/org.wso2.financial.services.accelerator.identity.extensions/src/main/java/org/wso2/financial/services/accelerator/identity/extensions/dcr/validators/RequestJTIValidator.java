/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.identity.extensions.dcr.validators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationRegistrationRequest;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationUpdateRequest;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;

import java.util.Map;

/**
 * Validator class for validating whether the JTI of the software statement is replayed.
 */
public class RequestJTIValidator implements DynamicClientRegistrationValidator {

    private static final Log log = LogFactory.getLog(RequestJTIValidator.class);

    @Override
    public void validatePost(ApplicationRegistrationRequest applicationRegistrationRequest,
                             Map<String, Object> ssaParams) throws FinancialServicesException {

        String requestJtiValue = (String) applicationRegistrationRequest.getAdditionalAttributes().get("jti");
        if (IdentityCommonUtils.isJTIReplayed(String.valueOf(requestJtiValue))) {
            log.debug("Rejected the replayed jti in the registration request");
            throw new FinancialServicesException("Rejected the replayed jti in the registration request");
        }
    }

    @Override
    public void validateGet(Map<String, String> ssaParams) throws FinancialServicesException {

    }

    @Override
    public void validateUpdate(ApplicationUpdateRequest applicationUpdateRequest, Map<String, Object> ssaParams,
                               ServiceProviderProperty[] serviceProviderProperties) throws FinancialServicesException {

        String requestJtiValue = (String) applicationUpdateRequest.getAdditionalAttributes().get("jti");
        if (IdentityCommonUtils.isJTIReplayed(String.valueOf(requestJtiValue))) {
            log.debug("Rejected the replayed jti in the registration request");
            throw new FinancialServicesException("Rejected the replayed jti in the registration request");
        }
    }
}
