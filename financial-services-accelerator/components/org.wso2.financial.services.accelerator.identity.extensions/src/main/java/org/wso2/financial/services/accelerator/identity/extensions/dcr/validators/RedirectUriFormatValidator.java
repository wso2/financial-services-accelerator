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
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Validator class to validate Redirect Url formats are valid Urls.
 */
public class RedirectUriFormatValidator implements DynamicClientRegistrationValidator {

    private static final Log log = LogFactory.getLog(RedirectUriFormatValidator.class);

    @Override
    public void validatePost(ApplicationRegistrationRequest applicationRegistrationRequest,
                             Map<String, Object> ssaParams) throws FinancialServicesException {

        AtomicReference<Object> redirectURISoftwareStatement = new AtomicReference<>();
        ssaParams.keySet().forEach(key -> {
            if (key.contains(IdentityCommonConstants.SSA_REDIRECT_URIS)) {
                redirectURISoftwareStatement.set(ssaParams.get(key));
            }
        });
        if (redirectURISoftwareStatement.get() instanceof List) {
            List<String> callbackUrisSoftwareStatementValues = (List<String>) redirectURISoftwareStatement.get();
            if (!validateRedirectURIs(callbackUrisSoftwareStatementValues)) {
                log.debug("Invalid redirect_uris found in the SSA");
                throw new FinancialServicesException("Invalid redirect_uris found in the SSA");
            }
        }
    }

    @Override
    public void validateGet(Map<String, String> ssaParams) throws FinancialServicesException {

    }

    @Override
    public void validateUpdate(ApplicationUpdateRequest applicationUpdateRequest, Map<String, Object> ssaParams,
                               ServiceProviderProperty[] serviceProviderProperties) throws FinancialServicesException {

        AtomicReference<Object> redirectURISoftwareStatement = new AtomicReference<>();
        ssaParams.keySet().forEach(key -> {
            if (key.contains(IdentityCommonConstants.SSA_REDIRECT_URIS)) {
                redirectURISoftwareStatement.set(ssaParams.get(key));
            }
        });
        if (redirectURISoftwareStatement.get() instanceof List) {
            List<String> callbackUrisSoftwareStatementValues = (List<String>) redirectURISoftwareStatement.get();
            if (!validateRedirectURIs(callbackUrisSoftwareStatementValues)) {
                log.error("Invalid redirect_uris found in the SSA");
                throw new FinancialServicesException("Invalid redirect_uris found in the SSA");
            }
        }
    }

    /**
     * Check format of redirect uris.
     *
     * @param redirectURIs redirect uris included in the software statement
     * @return true if the uris are validated
     */
    public static boolean validateRedirectURIs(List<String> redirectURIs) {

        return redirectURIs.stream()
                .anyMatch(redirectURI -> (redirectURI != null && redirectURI.contains("https") &&
                        !redirectURI.contains("localhost")));
    }
}
