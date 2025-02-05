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

package com.wso2.openbanking.accelerator.identity.listener.application;

import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.dcr.validation.DCRCommonConstants;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.mgt.listener.AbstractApplicationMgtListener;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthAdminServiceImpl;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Application listener.
 */
public class OBApplicationManagementListener extends AbstractApplicationMgtListener {

    private static final Log log = LogFactory.getLog(OBApplicationManagementListener.class);
    private IdentityExtensionsDataHolder identityExtensionsDataHolder = IdentityExtensionsDataHolder.getInstance();

    @Override
    public int getDefaultOrderId() {

        return 1000;
    }

    @Override
    public boolean doPreUpdateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        try {
            boolean isRegulatory = false;
            List<String> regulatoryIssuerList = new ArrayList<>();
            Object regulatoryIssuers = identityExtensionsDataHolder.getConfigurationMap()
                    .get(DCRCommonConstants.REGULATORY_ISSUERS);
            if (regulatoryIssuers != null) {
                if (regulatoryIssuers instanceof List) {
                    regulatoryIssuerList = (List<String>) regulatoryIssuers;
                } else {
                    regulatoryIssuerList.add(regulatoryIssuers.toString());
                }
            }

            List<ServiceProviderProperty> spProperties = new ArrayList<>(Arrays.asList
                    (serviceProvider.getSpProperties()));

            ServiceProviderProperty ssaIssuerProperty = spProperties.stream()
                    .filter(serviceProviderProperty -> serviceProviderProperty.getName()
                            .equalsIgnoreCase("ssaIssuer")).findAny().orElse(null);

            ServiceProviderProperty regulatoryProperty = spProperties.stream()
                    .filter(serviceProviderProperty -> serviceProviderProperty.getName()
                            .equalsIgnoreCase(OpenBankingConstants.REGULATORY)).findAny().orElse(null);

            if (ssaIssuerProperty != null) {
                String ssaIssuer = ssaIssuerProperty.getValue();
                isRegulatory = regulatoryIssuerList.stream().anyMatch(issuer -> issuer.equals(ssaIssuer));
            } else if (regulatoryProperty != null) {
                isRegulatory = Boolean.parseBoolean(regulatoryProperty.getValue());
            }

            //check whether regulatory property is already stored
            if (regulatoryProperty == null && isRegulatory) {

                spProperties.add(IdentityCommonUtil.getServiceProviderProperty(OpenBankingConstants.REGULATORY,
                        "true"));

            } else if (regulatoryProperty == null && !isRegulatory) {

                spProperties.add(IdentityCommonUtil.getServiceProviderProperty(OpenBankingConstants.REGULATORY,
                        "false"));

            } else if (regulatoryProperty != null && isRegulatory) {
                spProperties.remove(regulatoryProperty);
                spProperties.add(IdentityCommonUtil.getServiceProviderProperty(OpenBankingConstants.REGULATORY,
                        "true"));
            }
            serviceProvider.setSpProperties(spProperties.toArray(new ServiceProviderProperty[0]));
            OAuthAdminServiceImpl oAuthAdminService = identityExtensionsDataHolder.getOauthAdminService();

            OAuthConsumerAppDTO oAuthConsumerAppDTO = oAuthAdminService
                    .getOAuthApplicationDataByAppName(serviceProvider.getApplicationName());
            LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = serviceProvider
                    .getLocalAndOutBoundAuthenticationConfig();

            identityExtensionsDataHolder.getAbstractApplicationUpdater()
                    .doPreUpdateApplication(isRegulatory, oAuthConsumerAppDTO, serviceProvider,
                            localAndOutboundAuthenticationConfig, tenantDomain, userName);

        } catch (OpenBankingException e) {
            log.error("Error occurred while updating application.", e);
            return false;
        } catch (IdentityOAuthAdminException e) {
            //returning true here because this error code is returned when there is no oauth app created
            //when running integration tests of IS, test cases fail since apps are update before key generation in tests
            if ("OAUTH-60002".equals(e.getErrorCode())) {
                return true;
            }
            log.error("Error while retrieving oauth application", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean doPostGetServiceProvider(ServiceProvider serviceProvider, String applicationName,
                                            String tenantDomain) throws IdentityApplicationManagementException {

        try {
            identityExtensionsDataHolder.getAbstractApplicationUpdater()
                    .doPostGetApplication(serviceProvider, applicationName, tenantDomain);
        } catch (OpenBankingException e) {
            log.error("Error occurred while updating application.", e);
            return false;
        }
        return true;

    }

    @Override
    public boolean doPreDeleteApplication(String applicationName, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        try {
            identityExtensionsDataHolder.getAbstractApplicationUpdater()
                    .doPreDeleteApplication(applicationName, tenantDomain, userName);
        } catch (OpenBankingException e) {
            log.error("Error occurred while updating application.", e);
            return false;
        }
        return true;

    }

    @Override
    public boolean doPostDeleteApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        try {
            identityExtensionsDataHolder.getAbstractApplicationUpdater()
                    .doPostDeleteApplication(serviceProvider, tenantDomain, userName);
        } catch (OpenBankingException e) {
            log.error("Error occurred while updating application.", e);
            return false;
        }
        return true;

    }
}
