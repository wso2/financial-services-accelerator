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

package com.wso2.open.banking.application.info.endpoint.api.utils;

import com.wso2.open.banking.application.info.endpoint.model.ApplicationBulkMetadataSuccessDTO;
import com.wso2.open.banking.application.info.endpoint.model.ApplicationInfoErrorDTO;
import com.wso2.open.banking.application.info.endpoint.model.ApplicationMetadataResourceDTO;
import com.wso2.open.banking.application.info.endpoint.model.ApplicationSingleMetadataSuccessDTO;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.identity.sp.metadata.extension.SPMetadataFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Map internal models to external API DTOs.
 */
public class MappingUtil {

    private static final Log log = LogFactory.getLog(MappingUtil.class);
    private static final SPMetadataFilter metadataFilter = (SPMetadataFilter) OpenBankingUtils.getClassInstanceFromFQN(
            OpenBankingConfigParser.getInstance().getSPMetadataFilterExtension());
    private static final String SOFTWARE_ID = "software_id";

    /**
     * Map Single Metadata API Response DTO from service provider.
     *
     * @param serviceProvider service provider to populate response with.
     * @return
     */
    public static ApplicationSingleMetadataSuccessDTO mapSingleMetadataResponseDTO(ServiceProvider serviceProvider) {

        // Create Target DTO
        ApplicationSingleMetadataSuccessDTO successDTO = new ApplicationSingleMetadataSuccessDTO();

        // Map single metadata resource to DTO
        successDTO.setData(mapApplicationMetadataResourceDTO(serviceProvider));

        return successDTO;
    }

    /**
     * Map bulk Metadata API Response DTO from list of service providers.
     *
     * @param serviceProviderList service provider list to populate response with.
     * @return
     */
    public static ApplicationBulkMetadataSuccessDTO mapBulkMetadataResponseDTO(List<ServiceProvider>
                                                                                       serviceProviderList) {

        ApplicationBulkMetadataSuccessDTO successDTO = new ApplicationBulkMetadataSuccessDTO();

        // Stream list of service providers and map to resource DTOs
        successDTO.setData(serviceProviderList.stream()
                .map(MappingUtil::mapApplicationMetadataResourceDTO)
                .collect(Collectors.toMap(ApplicationMetadataResourceDTO::getId, obj -> obj)));

        return successDTO;
    }

    /**
     * Map Single Application Metadata Resource from service provider.
     *
     * @param serviceProvider service provider to populate response with.
     * @return
     */
    public static ApplicationMetadataResourceDTO mapApplicationMetadataResourceDTO(ServiceProvider serviceProvider) {

        ApplicationMetadataResourceDTO resourceDTO = new ApplicationMetadataResourceDTO();

        // Filter out OAuth2 from array of InboundAuthenticationRequestConfig and get clientId
        Optional<String> clientId = Arrays.stream(serviceProvider.getInboundAuthenticationConfig()
                .getInboundAuthenticationRequestConfigs())
                .filter(conf -> conf.getInboundAuthType().equals(IdentityApplicationConstants.OAuth2.NAME))
                .findFirst().map(InboundAuthenticationRequestConfig::getInboundAuthKey);

        // Set type of resource
        resourceDTO.setType(IdentityApplicationConstants.OAuth2.NAME);

        // If clientId is present set to target DTO
        clientId.ifPresent(resourceDTO::setId);

        // Stream through ServiceProvider property array and map to target attributes
        Map<String, String> metadata = Arrays.stream(serviceProvider.getSpProperties())
                .collect(Collectors.toMap(ServiceProviderProperty::getName, ServiceProviderProperty::getValue));

        // Return default application name for software_id
        if (StringUtils.isEmpty(metadata.get(SOFTWARE_ID))) {
            metadata.put(SOFTWARE_ID, serviceProvider.getApplicationName());
        }

        // filter metadata map using the configured metadata filter logic. (default: DefaultSPMetadataFilter)
        metadata = metadataFilter.filter(metadata);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Application metadata list for client_id %s : %s",
                    clientId.orElse(""), metadata));
        }
        resourceDTO.setMetadata(metadata);

        return resourceDTO;

    }

    /**
     * Build Client Error Reponse.
     *
     * @param status      status code of the error.
     * @param title       summary of the error.
     * @param description human readable descriptive error.
     * @return Reponse object.
     */
    public static ApplicationInfoErrorDTO buildErrorDTO(String status, String title, String description) {

        ApplicationInfoErrorDTO errorDTO = new ApplicationInfoErrorDTO();

        errorDTO.setStatus(status);
        errorDTO.setTitle(title);
        errorDTO.setDescription(description);
        return errorDTO;
    }

}
