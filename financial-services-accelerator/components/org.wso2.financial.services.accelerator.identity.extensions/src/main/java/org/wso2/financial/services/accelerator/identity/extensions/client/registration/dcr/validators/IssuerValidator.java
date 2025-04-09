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

package org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.validators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationRegistrationRequest;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationUpdateRequest;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesDCRException;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;

import java.util.Map;

/**
 * Validator class for validating the issuer of the registration request.
 */
public class IssuerValidator implements DynamicClientRegistrationValidator {

    private static final Log log = LogFactory.getLog(IssuerValidator.class);

    @Override
    public void validatePost(ApplicationRegistrationRequest applicationRegistrationRequest,
                             Map<String, Object> ssaParams) throws FinancialServicesDCRException {

        validateSoftwareId(applicationRegistrationRequest.getAdditionalAttributes(), ssaParams);
    }

    @Override
    public void validateGet(Map<String, String> ssaParams) throws FinancialServicesDCRException {

    }

    @Override
    public void validateUpdate(ApplicationUpdateRequest applicationUpdateRequest, Map<String, Object> ssaParams,
                               ServiceProviderProperty[] serviceProviderProperties)
            throws FinancialServicesDCRException {

        validateSoftwareId(applicationUpdateRequest.getAdditionalAttributes(), ssaParams);
    }

    /**
     * Validate whether the issuer of the request is equal to the software id of the SSA
     *
     * @param additionalAttributes      Additional Attributes
     * @param ssaParams                 SSA Parameters
     * @throws FinancialServicesDCRException When the issuer is not equal to the software id
     */
    private static void validateSoftwareId(Map<String, Object> additionalAttributes, Map<String, Object> ssaParams)
            throws FinancialServicesDCRException {

        String issuer = (String) additionalAttributes.get(IdentityCommonConstants.ISS);
        String softwareId = (String) ssaParams.get(IdentityCommonConstants.SOFTWARE_ID);

        if (softwareId != null && !softwareId.equals(issuer)) {
            log.debug(String.format("Invalid issuer, issuer value %s is not same as the software id value %s",
                    issuer.replaceAll("[\r\n]+", ""), softwareId.replaceAll("[\r\n]+", "")));
            throw new FinancialServicesDCRException(IdentityCommonConstants.INVALID_CLIENT_METADATA,
                    "Invalid issuer, issuer should be the same as the software id");
        }
    }

}
