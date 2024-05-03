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

package com.wso2.openbanking.accelerator.common.identity.retriever.sp;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.internal.OpenBankingCommonDataHolder;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.ServiceProviderUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class to retrieve Service Provider Data.
 */
public class CommonServiceProviderRetriever {

    private static final Log log = LogFactory.getLog(CommonServiceProviderRetriever.class);

    /**
     * Utility method get the application property from SP Meta Data.
     *
     * @param clientId ClientId of the application
     * @param property Property of the application
     * @return the property value from SP metadata
     * @throws OpenBankingException  if an error occurs while retrieving the property
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public String getAppPropertyFromSPMetaData(String clientId, String property) throws OpenBankingException {

        String spProperty = null;

        if (StringUtils.isNotEmpty(clientId)) {
            Optional<ServiceProvider> serviceProvider;
            try {
                serviceProvider = Optional.ofNullable(OpenBankingCommonDataHolder.getInstance()
                        .getApplicationManagementService().getServiceProviderByClientId(clientId,
                                IdentityApplicationConstants.OAuth2.NAME,
                                ServiceProviderUtils.getSpTenantDomain(clientId)));
                if (serviceProvider.isPresent()) {
                    spProperty = Arrays.stream(serviceProvider.get().getSpProperties())
                            .collect(Collectors.toMap(ServiceProviderProperty::getName,
                                    ServiceProviderProperty::getValue)).get(property);
                }
            } catch (IdentityApplicationManagementException e) {
                log.error(String.format("Error occurred while retrieving OAuth2 application data for clientId %s",
                        clientId.replaceAll("[\r\n]" , "")) , e);
                throw new OpenBankingException("Error occurred while retrieving OAuth2 application data for clientId"
                        , e);
            }
        } else {
            log.error("Client id not found");
            throw new OpenBankingException("Client id not found");
        }

        return spProperty;
    }
}
