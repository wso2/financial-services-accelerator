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

import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationRegistrationRequest;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationUpdateRequest;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;

import java.util.Map;

/**
 * Validator class for validating the issuer of the registration request.
 */
public class IssuerValidator {

    public static void validate(ApplicationRegistrationRequest appRegistrationRequest, Map<String, Object> ssaParams)
            throws FinancialServicesException {

        String issuer = (String) appRegistrationRequest.getAdditionalAttributes().get(IdentityCommonConstants.ISS);
        String softwareId = (String) ssaParams.get(IdentityCommonConstants.SOFTWARE_ID);
        if (softwareId != null && !softwareId.equals(issuer)) {
            throw new FinancialServicesException("Invalid issuer, issuer should be the same as the software id");
        }
    }

    public static void validate(ApplicationUpdateRequest applicationUpdateRequest, Map<String, Object> ssaParams)
            throws FinancialServicesException {

        String issuer = (String) applicationUpdateRequest.getAdditionalAttributes().get(IdentityCommonConstants.ISS);
        String softwareId = (String) ssaParams.get(IdentityCommonConstants.SOFTWARE_ID);
        if (softwareId != null && !softwareId.equals(issuer)) {
            throw new FinancialServicesException("Invalid issuer, issuer should be the same as the software id");
        }
    }
}
