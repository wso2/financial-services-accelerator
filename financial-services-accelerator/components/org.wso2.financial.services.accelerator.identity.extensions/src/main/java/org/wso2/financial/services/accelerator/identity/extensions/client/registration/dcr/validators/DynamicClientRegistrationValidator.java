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

import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationRegistrationRequest;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationUpdateRequest;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesDCRException;

import java.util.Map;

/**
 * Interface for dynamic client registration validator.
 * This interface is used to validate the dynamic client registration requests. Implementations of this interface
 * can be configured in the deployment.toml file.
 */
public interface DynamicClientRegistrationValidator {

    /**
     * Validate the application registration request.
     *
     * @param applicationRegistrationRequest  Application registration request.
     * @param ssaParams                       SSA parameters.
     * @throws FinancialServicesDCRException When an error occurs while validating the request.
     */
    void validatePost(ApplicationRegistrationRequest applicationRegistrationRequest,
                      Map<String, Object> ssaParams) throws FinancialServicesDCRException;

    /**
     * Validate the get request.
     *
     * @param ssaParams  SSA parameters.
     * @throws FinancialServicesDCRException When an error occurs while validating the request.
     */
    void validateGet(Map<String, String> ssaParams) throws FinancialServicesDCRException;

    /**
     * Validate the update request.
     *
     * @param applicationUpdateRequest   Application update request.
     * @param ssaParams                  SSA parameters.
     * @param serviceProviderProperties  Service provider properties.
     * @throws FinancialServicesDCRException When an error occurs while validating the request.
     */
    void validateUpdate(ApplicationUpdateRequest applicationUpdateRequest,
                        Map<String, Object> ssaParams, ServiceProviderProperty[] serviceProviderProperties)
            throws FinancialServicesDCRException;

}
