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

package org.wso2.financial.services.accelerator.identity.extensions.client.registration.application.listener.util;

import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for ApplicationMgtListener.
 */
public class ApplicationMgtListenerUtil {

    /**
     * Get the regulatory property from sp properties or ssa issuer value.
     *
     * @param spProperties  Existing service provider properties.
     * @return true if the application is regulatory, false otherwise.
     */
    public static boolean getRegulatoryProperty(List<ServiceProviderProperty> spProperties) {

        boolean isRegulatory = false;
        List<String> regulatoryIssuerList = new ArrayList<>();
        Object regulatoryIssuers = IdentityExtensionsDataHolder.getInstance().getConfigurationMap()
                .get(FinancialServicesConstants.DCR_REGULATORY_ISSUERS);
        if (regulatoryIssuers != null) {
            if (regulatoryIssuers instanceof List) {
                regulatoryIssuerList = (List<String>) regulatoryIssuers;
            } else {
                regulatoryIssuerList.add(regulatoryIssuers.toString());
            }
        }

        ServiceProviderProperty ssaIssuerProperty = spProperties.stream()
                .filter(serviceProviderProperty -> serviceProviderProperty.getName()
                        .equals("ssaIssuer")).findAny().orElse(null);

        ServiceProviderProperty regulatoryProperty = spProperties.stream()
                .filter(serviceProviderProperty -> serviceProviderProperty.getName()
                        .equals(FinancialServicesConstants.REGULATORY)).findAny().orElse(null);

        if (ssaIssuerProperty != null) {
            String ssaIssuer = ssaIssuerProperty.getValue();
            isRegulatory = regulatoryIssuerList.stream().anyMatch(issuer -> issuer.equals(ssaIssuer));
        } else if (regulatoryProperty != null) {
            isRegulatory = Boolean.parseBoolean(regulatoryProperty.getValue());
        }

        return isRegulatory;
    }

    /**
     * Get the updated service provider properties.
     *
     * @param spProperties  Existing service provider properties.
     * @return Updated service provider properties.
     */
    public static List<ServiceProviderProperty> getUpdatedSpProperties(List<ServiceProviderProperty> spProperties) {

        ServiceProviderProperty regulatoryProperty = spProperties.stream()
                .filter(serviceProviderProperty -> serviceProviderProperty.getName()
                        .equals(FinancialServicesConstants.REGULATORY)).findAny().orElse(null);

        boolean isRegulatory = getRegulatoryProperty(spProperties);
        //check whether regulatory property is already stored
        if (regulatoryProperty == null && isRegulatory) {

            spProperties.add(getServiceProviderProperty(FinancialServicesConstants.REGULATORY, "true"));

        } else if (regulatoryProperty == null && !isRegulatory) {

            spProperties.add(getServiceProviderProperty(FinancialServicesConstants.REGULATORY, "false"));

        } else if (regulatoryProperty != null && isRegulatory) {
            spProperties.remove(regulatoryProperty);
            spProperties.add(getServiceProviderProperty(FinancialServicesConstants.REGULATORY, "true"));
        }
        return spProperties;
    }

    /**
     * Construct the service provider property.
     * @param spPropertyName   service provider property name
     * @param spPropertyValue  service provider property value
     * @return ServiceProviderProperty
     */
    public static ServiceProviderProperty getServiceProviderProperty(String spPropertyName, String spPropertyValue) {

        ServiceProviderProperty serviceProviderProperty = new ServiceProviderProperty();
        serviceProviderProperty.setValue(spPropertyValue);
        serviceProviderProperty.setName(spPropertyName);
        serviceProviderProperty.setDisplayName(spPropertyName);
        return serviceProviderProperty;
    }
}
