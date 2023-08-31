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

package com.wso2.open.banking.application.info.endpoint.api.impl;

import com.wso2.open.banking.application.info.endpoint.api.ApplicationInformationApi;
import com.wso2.open.banking.application.info.endpoint.api.data.MetaDataDAOImpl;
import com.wso2.open.banking.application.info.endpoint.api.utils.MappingUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * ApplicationInfoAPI.
 *
 * <p>This specifies a RESTful API for retriving OAuth Application Information
 */
public class ApplicationInformationApiServiceImpl implements ApplicationInformationApi {

    private static final String QUERY_PARAM_BULK_DELIMITER = ",";
    private static final String ERROR_INVALID_REQUEST = "invalid clientIds given in request";
    private static final String ERROR_FETCHING_SP = "Unable to retrieve information," +
            " please contact system administrator";
    private static final String APPLICATION_NOT_EXISTS = "Unavailable Application";

    private static final Log log = LogFactory.getLog(ApplicationInformationApiServiceImpl.class);

    /**
     * Retrieve Bulk Application Metadata.
     *
     * @param clientIds client ID sequences for retrieval.
     * @return client response.
     */
    public Response getBulkApplicationMetadata(List<String> clientIds) {

        // Take List of query params for clientIds and split by delimiter and remove duplicates
        List<String> splitClientIds = clientIds.stream()
                .map(str -> str.split(QUERY_PARAM_BULK_DELIMITER))
                .flatMap(Arrays::stream)
                .distinct()
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());

        // Check if any IDs are set if not send error message
        if (splitClientIds.isEmpty()) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(MappingUtil.buildErrorDTO(
                            String.valueOf(Response.Status.BAD_REQUEST.getStatusCode()),
                            Response.Status.BAD_REQUEST.getReasonPhrase(), ERROR_INVALID_REQUEST))
                    .build();
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("ClientIds %s provided for bulk retrieval",
                    Arrays.toString(splitClientIds.toArray())));
        }

        // Retrieve Service Providers from list
        List<ServiceProvider> serviceProviderList = splitClientIds
                .stream()
                .map(this::getOAuthServiceProvider)
                .collect(Collectors.toList());

        return Response.ok()
                .entity(MappingUtil.mapBulkMetadataResponseDTO(serviceProviderList))
                .build();
    }

    /**
     * Retrieve All Bulk Application Metadata.
     *
     * @return client response.
     */
    public Response getAllApplicationMetadata() {

        MetaDataDAOImpl metaDataDAOImpl = new MetaDataDAOImpl();

        List<String> clientIdList = metaDataDAOImpl.getAllDistinctClientIds();

        return getBulkApplicationMetadata(clientIdList);
    }


    /**
     * Retrieve Single Application Metadata.
     *
     * @param id clientId of application.
     * @return client response.
     */
    public Response getSingleApplicationMetadata(String id) {

        ServiceProvider selectedServiceProvider = getOAuthServiceProvider(id);

        // If Service provider is present map value and return
        return Response.ok()
                .entity(MappingUtil.mapSingleMetadataResponseDTO(selectedServiceProvider))
                .build();
    }

    /**
     * Get Service provider from clientId.
     *
     * @param clientId of application.
     * @return Service Provider.
     * @throws WebApplicationException client error.
     */
    private ServiceProvider getOAuthServiceProvider(String clientId) throws WebApplicationException {

        ApplicationManagementService managementService = this.getApplicationManagementService();
        Optional<ServiceProvider> serviceProvider;
        try {
            serviceProvider = Optional.ofNullable(managementService.getServiceProviderByClientId(clientId,
                    IdentityApplicationConstants.OAuth2.NAME, getTenantDomain()));
        } catch (IdentityApplicationManagementException e) {

            log.error(String.format("Unable to retrieve service provider information for clientId %s", clientId), e);

            // Throw Web Application exception
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(MappingUtil.buildErrorDTO(
                            String.valueOf(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()),
                            Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                            ERROR_FETCHING_SP))
                    .build());
        }

        // Handle empty or default service provider.
        if (!serviceProvider.isPresent() ||
                serviceProvider.get().getApplicationName().equals(IdentityApplicationConstants.DEFAULT_SP_CONFIG)) {

            final String errorMessage = String.format("Unable to find application for clientId %s", clientId);

            if (log.isDebugEnabled()) {
                log.debug(errorMessage);
            }

            return handleSPForDefaultOrNull(clientId, serviceProvider);
        }

        return serviceProvider.get();
    }

    /**
     * Populate serviceProvider with APPLICATION_NOT_EXISTS based on default or empty.
     *  @param clientId
     * @param serviceProvider
     * @return
     */
    private ServiceProvider handleSPForDefaultOrNull(String clientId, Optional<ServiceProvider> serviceProvider) {

        ServiceProvider serviceProviderForErrorScenarios;

        // set new SP and inboundAuthenticationConfig for cases where serviceProvider is not present
        if (!serviceProvider.isPresent()) {
            serviceProviderForErrorScenarios = new ServiceProvider();

            InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
            InboundAuthenticationRequestConfig[] configs = new InboundAuthenticationRequestConfig[1];
            configs[0] = new InboundAuthenticationRequestConfig();
            configs[0].setInboundAuthKey(clientId);
            configs[0].setInboundAuthType(IdentityApplicationConstants.OAuth2.NAME);
            inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(configs);
            serviceProviderForErrorScenarios.setInboundAuthenticationConfig(inboundAuthenticationConfig);
            serviceProvider = Optional.of(serviceProviderForErrorScenarios);
        }

        // continue populating SP with properties
        serviceProviderForErrorScenarios = serviceProvider.get();
        List<ServiceProviderProperty> serviceProviderPropertyList = new ArrayList<>();
        serviceProviderForErrorScenarios.setApplicationName(APPLICATION_NOT_EXISTS);

        ServiceProviderProperty displayName = new ServiceProviderProperty();
        displayName.setName("software_id");
        displayName.setValue(APPLICATION_NOT_EXISTS);

        ServiceProviderProperty clientName = new ServiceProviderProperty();
        clientName.setName("client_name");
        clientName.setValue(APPLICATION_NOT_EXISTS);

        serviceProviderPropertyList.add(displayName);
        serviceProviderPropertyList.add(clientName);
        ServiceProviderProperty[] serviceProviderProperties =
                new ServiceProviderProperty[serviceProviderPropertyList.size()];

        serviceProviderPropertyList.toArray(serviceProviderProperties);
        serviceProviderForErrorScenarios.setSpProperties(serviceProviderProperties);

        InboundAuthenticationRequestConfig[] inboundAuthenticationRequestConfigs = serviceProviderForErrorScenarios
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();
        if (inboundAuthenticationRequestConfigs.length != 0) {
            inboundAuthenticationRequestConfigs[0].setInboundAuthKey(clientId);
            inboundAuthenticationRequestConfigs[0].setInboundAuthType(IdentityApplicationConstants.OAuth2.NAME);
        }
        return serviceProviderForErrorScenarios;
    }

    /**
     * Get WSO2 IS Application Mgt Service from threadlocal carbon context.
     *
     * @return Application Management Service Implementation.
     */
    private ApplicationManagementService getApplicationManagementService() {

        return (ApplicationManagementService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext()
                .getOSGiService(ApplicationManagementService.class, null);

    }

    /**
     * Get Tenant Domain String from carbon context.
     *
     * @return tenant domain of current context.
     */
    private String getTenantDomain() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
    }

}

