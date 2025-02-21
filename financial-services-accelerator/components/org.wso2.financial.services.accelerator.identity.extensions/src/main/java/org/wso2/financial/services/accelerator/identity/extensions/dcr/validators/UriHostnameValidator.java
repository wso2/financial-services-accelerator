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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationRegistrationRequest;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationUpdateRequest;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Validator class to validate Redirect Urls are valid Urls.
 */
public class UriHostnameValidator implements DynamicClientRegistrationValidator {

    private static final Log log = LogFactory.getLog(UriHostnameValidator.class);

    @Override
    public void validatePost(ApplicationRegistrationRequest applicationRegistrationRequest,
                             Map<String, Object> ssaParams) throws FinancialServicesException {

        if (!validateURIHostNames(ssaParams)) {
            log.debug("Host names of logo_uri/tos_uri/policy_uri/client_uri does not match with the " +
                    "redirect_uris");
            throw new FinancialServicesException("Host names of logo_uri/tos_uri/policy_uri/client_uri does not " +
                    "match with the redirect_uris");
        }
    }

    @Override
    public void validateGet(Map<String, String> ssaParams) throws FinancialServicesException {

    }

    @Override
    public void validateUpdate(ApplicationUpdateRequest applicationUpdateRequest, Map<String, Object> ssaParams,
                               ServiceProviderProperty[] serviceProviderProperties) throws FinancialServicesException {


        if (!validateURIHostNames(ssaParams)) {
            log.debug("Host names of logo_uri/tos_uri/policy_uri/client_uri does not match with the " +
                    "redirect_uris");
            throw new FinancialServicesException("Host names of logo_uri/tos_uri/policy_uri/client_uri does not " +
                    "match with the redirect_uris");
        }
    }

    /**
     * Check the hostnames of redirect uris and other uris.
     *
     * @param ssaParams    software statement parameters
     * @return true if the uris are validated
     */
    public static boolean validateURIHostNames(Map<String, Object> ssaParams) {

        AtomicReference<String> logoURI = new AtomicReference<>(StringUtils.EMPTY);
        AtomicReference<String> clientURI = new AtomicReference<>(StringUtils.EMPTY);
        AtomicReference<String> policyURI = new AtomicReference<>(StringUtils.EMPTY);
        AtomicReference<String> termsOfServiceURI = new AtomicReference<>(StringUtils.EMPTY);
        AtomicReference<Object> redirectURIs = new AtomicReference<>();
        try {

            ssaParams.keySet().forEach(key -> {
                if (key.contains(IdentityCommonConstants.SSA_LOGO_URI)) {
                    logoURI.set((String) ssaParams.get(key));
                } else if (key.contains(IdentityCommonConstants.SSA_CLIENT_URI)) {
                    clientURI.set((String) ssaParams.get(key));
                } else if (key.contains(IdentityCommonConstants.SSA_POLICY_URI)) {
                    policyURI.set((String) ssaParams.get(key));
                } else if (key.contains(IdentityCommonConstants.SSA_TOS_URI)) {
                    termsOfServiceURI.set((String) ssaParams.get(key));
                } else if (key.contains(IdentityCommonConstants.SSA_REDIRECT_URIS)) {
                    redirectURIs.set(ssaParams.get(key));
                }
            });

            String logoURIHost = new URI(logoURI.get()).getHost();
            String clientURIHost = new URI(clientURI.get()).getHost();
            String policyURIHost = new URI(policyURI.get()).getHost();
            String termsOfServiceURIHost = new URI(termsOfServiceURI.get()).getHost();
            //check whether the hostnames of policy,logo,client and terms of service uris match with redirect uri
            //hostname if the validation is set to true
            for (String redirectURI : (List<String>) redirectURIs.get()) {
                //check whether the redirect uris and other given uris have same host name
                String uriHost = new URI(redirectURI).getHost();
                if (!(logoURIHost.equals(uriHost) && clientURIHost.equals(uriHost)
                        && policyURIHost.equals(uriHost) && termsOfServiceURIHost.equals(uriHost))) {
                    log.debug("URL host names do not match");
                    return false;
                }
            }
        } catch (URISyntaxException e) {
            log.debug("Malformed redirect uri", e);
            return false;
        }
        return true;
    }
}
