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
 * Validator class for validating the redirect uris of the registration request.
 */
public class RedirectUriMatchValidator implements DynamicClientRegistrationValidator {

    private static final Log log = LogFactory.getLog(RedirectUriMatchValidator.class);

    @Override
    public void validatePost(ApplicationRegistrationRequest applicationRegistrationRequest,
                             Map<String, Object> ssaParams) throws FinancialServicesException {

        validate(applicationRegistrationRequest.getRedirectUris(), ssaParams);
    }

    @Override
    public void validateGet(Map<String, String> ssaParams) throws FinancialServicesException {

    }

    @Override
    public void validateUpdate(ApplicationUpdateRequest applicationUpdateRequest, Map<String, Object> ssaParams,
                               ServiceProviderProperty[] serviceProviderProperties) throws FinancialServicesException {

        validate(applicationUpdateRequest.getRedirectUris(), ssaParams);
    }

    /**
     * Validate the redirect URIs in the request.
     * @param redirectUris   Redirect URIs in the request.
     * @param ssaParams      SSA parameters.
     * @throws FinancialServicesException When an error occurs while validating the request.
     */
    public static void validate(List<String> redirectUris, Map<String, Object> ssaParams)
            throws FinancialServicesException {
        if (redirectUris != null && !redirectUris.isEmpty()) {

            AtomicReference<Object> redirectURISoftwareStatement = new AtomicReference<>();
            ssaParams.keySet().stream().forEach(key -> {
                if (key.contains(IdentityCommonConstants.SSA_REDIRECT_URIS)) {
                    redirectURISoftwareStatement.set(ssaParams.get(key));
                }
            });

            if (!matchRedirectURI(redirectUris, redirectURISoftwareStatement.get())) {
                log.error("Redirect URIs do not match with the software statement");
                throw new FinancialServicesException("Redirect URIs do not match with the software statement");
            }
        }
    }

    /**
     * Check whether the redirect uris in the request are a subset of the redirect uris in the software statement
     * assertion.
     */
    public static boolean matchRedirectURI(List<String> redirectURIRequest, Object redirectURISoftwareStatement) {

        int matchedURis = 0;
        if (redirectURISoftwareStatement instanceof List) {
            List callbackUrisSoftwareStatementValues = (List) redirectURISoftwareStatement;
            for (String requestURI : redirectURIRequest) {
                for (Object callbackUrisSoftwareStatementObject : callbackUrisSoftwareStatementValues) {
                    String softwareStatementURI = (String) callbackUrisSoftwareStatementObject;
                    if (requestURI.equals(softwareStatementURI)) {
                        matchedURis = matchedURis + 1;
                    }
                }
            }
        }
        return matchedURis == redirectURIRequest.size();
    }
}
