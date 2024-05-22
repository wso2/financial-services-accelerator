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
package com.wso2.openbanking.accelerator.identity.dcr.endpoint.impl.service;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.identity.dcr.endpoint.impl.RegistrationConstants;
import com.wso2.openbanking.accelerator.identity.dcr.endpoint.impl.util.RegistrationUtils;
import com.wso2.openbanking.accelerator.identity.dcr.exception.DCRValidationException;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationRequest;
import com.wso2.openbanking.accelerator.identity.dcr.validation.DCRCommonConstants;
import com.wso2.openbanking.accelerator.identity.dcr.validation.RegistrationValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.oauth.dcr.bean.Application;
import org.wso2.carbon.identity.oauth.dcr.exception.DCRMException;
import org.wso2.carbon.identity.oauth.dcr.service.DCRMService;

import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

/**
 * Service class to invoke spec specific validators and manage storing, retrieving, updating and
 * deleting registrations.
 */
public class RegistrationServiceHandler {

    private static final Log log = LogFactory.getLog(RegistrationServiceHandler.class);
    private DCRMService oAuth2DCRMService;
    private OpenBankingConfigurationService openBankingConfigurationService;

    public Response createRegistration(RegistrationRequest registrationRequest,
                                       Map<String, Object> additionalAttributes)
            throws DCRMException, IdentityApplicationManagementException, IllegalArgumentException, ParseException {

        DCRMService dcrmService = getDCRServiceInstance();
        OpenBankingConfigurationService openBankingConfigurationService = getOBConfigService();
        RegistrationValidator registrationValidator = RegistrationValidator.getRegistrationValidator();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        boolean useSoftwareIdAsAppName = false;
        String jwksEndpointName = "";
        if (openBankingConfigurationService != null) {
            Map<String, Object> configurations = openBankingConfigurationService.getConfigurations();
            useSoftwareIdAsAppName = Boolean.parseBoolean(configurations
                    .get(OpenBankingConstants.DCR_USE_SOFTWAREID_AS_APPNAME).toString());
            if (configurations.containsKey(OpenBankingConstants.DCR_JWKS_NAME)) {
                jwksEndpointName = configurations
                        .get(OpenBankingConstants.DCR_JWKS_NAME).toString();
            }
        }
        String applicationName = RegistrationUtils.getApplicationName(registrationRequest, useSoftwareIdAsAppName);
        Application application = dcrmService.registerApplication(RegistrationUtils
                .getApplicationRegistrationRequest(registrationRequest, applicationName));
        if (log.isDebugEnabled()) {
            log.debug("Created application with name :" + application.getClientName());
        }
        ApplicationManagementService applicationManagementService = ApplicationManagementService.getInstance();
        ServiceProvider serviceProvider = applicationManagementService
                .getServiceProvider(application.getClientName(), tenantDomain);

        //get JWKS URI from the request
        String jwksUri = RegistrationUtils.getJwksUriFromRequest(registrationRequest, jwksEndpointName);
        serviceProvider.setJwksUri(jwksUri);

        Long clientIdIssuedTime = Instant.now().getEpochSecond();
        //store the client details as SP meta data
        Map<String, String> registrationRequestData = RegistrationUtils
                .getAlteredApplicationAttributes(registrationRequest);
        registrationRequestData.put(RegistrationConstants.CLIENT_ID_ISSUED_AT, clientIdIssuedTime.toString());
        // Adding SP property to identify create request. Will be removed when setting up authenticators.
        registrationRequestData.put("AppCreateRequest", "true");
        List<ServiceProviderProperty> spMetaData = RegistrationUtils.getServiceProviderPropertyList
                (registrationRequestData);
        serviceProvider.setSpProperties(spMetaData.toArray(new ServiceProviderProperty[0]));
        applicationManagementService.updateApplication(serviceProvider, tenantDomain, userName);

        if (log.isDebugEnabled()) {
            log.debug("Updated Service Provider " + serviceProvider.getApplicationName() + " with the client data");
        }
        Map<String, Object> registrationData = registrationRequest.getRequestParameters();
        registrationData.put(RegistrationConstants.CLIENT_ID, application.getClientId());
        registrationData.put(RegistrationConstants.CLIENT_ID_ISSUED_AT, clientIdIssuedTime.toString());
        if (registrationRequest.getSsaParameters() != null) {
            registrationData.putAll(registrationRequest.getSsaParameters());
        }
        registrationData.putAll(additionalAttributes);
        String registrationResponse = registrationValidator.getRegistrationResponse(registrationData);
        return Response.status(Response.Status.CREATED).entity(registrationResponse).build();
    }

    public Response retrieveRegistration(Map<String, Object> additionalAttributes, String clientId, String accessToken)
            throws DCRMException, IdentityApplicationManagementException {

        DCRMService dcrmService = getDCRServiceInstance();
        Application application = dcrmService.getApplication(clientId);

        if (log.isDebugEnabled()) {
            log.debug("Retrieved Application with name " + application.getClientName());
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        ApplicationManagementService applicationManagementService = ApplicationManagementService.getInstance();
        ServiceProvider serviceProvider = applicationManagementService
                .getServiceProvider(application.getClientName(), tenantDomain);
        ServiceProviderProperty[] serviceProviderProperties = serviceProvider.getSpProperties();

        if (log.isDebugEnabled()) {
            log.debug("Retrieved client meta data for application " + application.getClientName());
        }

        List<ServiceProviderProperty> spPropertyList = Arrays.asList(serviceProviderProperties);
        Map<String, Object> spMetaData = RegistrationUtils.getSpMetaDataMap(spPropertyList);
        spMetaData.put(RegistrationConstants.CLIENT_ID, application.getClientId());
        spMetaData.put(RegistrationConstants.REGISTRATION_ACCESS_TOKEN, accessToken);
        spMetaData.putAll(additionalAttributes);

        String registrationResponseJson = RegistrationValidator.getRegistrationValidator()
                .getRegistrationResponse(spMetaData);
        return Response.status(Response.Status.OK).entity(registrationResponseJson).build();
    }

    public Response updateRegistration(RegistrationRequest request, Map<String, Object> additionalAttributes,
                                       String clientId, String accessToken)
            throws DCRMException, IdentityApplicationManagementException, DCRValidationException, ParseException {

        DCRMService dcrmService = getDCRServiceInstance();
        OpenBankingConfigurationService openBankingConfigurationService = getOBConfigService();
        boolean useSoftwareIdAsAppName = false;
        String jwksEndpointName = "";
        if (openBankingConfigurationService != null) {
            Map<String, Object> configurations = openBankingConfigurationService.getConfigurations();
            useSoftwareIdAsAppName = Boolean.parseBoolean(configurations
                    .get(OpenBankingConstants.DCR_USE_SOFTWAREID_AS_APPNAME).toString());
            if (configurations.containsKey(OpenBankingConstants.DCR_JWKS_NAME)) {
                jwksEndpointName = configurations
                        .get(OpenBankingConstants.DCR_JWKS_NAME).toString();
            }
        }
        Application applicationToUpdate = dcrmService.getApplication(clientId);
        String applicationNameInRequest;
        if (useSoftwareIdAsAppName) {
            applicationNameInRequest = (request.getSoftwareStatement() != null) ?
                    request.getSoftwareStatementBody().getSoftwareId() :
                    request.getSoftwareId();
        } else {
            applicationNameInRequest = request.getSoftwareStatementBody().getClientName();
        }
        if (!applicationToUpdate.getClientName().equals(applicationNameInRequest)) {
            throw new DCRValidationException(DCRCommonConstants.INVALID_META_DATA, "Invalid application name");
        }
        String applicationName = RegistrationUtils.getApplicationName(request, useSoftwareIdAsAppName);
        Application application = dcrmService.updateApplication
                (RegistrationUtils.getApplicationUpdateRequest(request, applicationName), clientId);
        if (log.isDebugEnabled()) {
            log.debug("Updated Application with name " + application.getClientName());
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();

        //retrieve stored client meta data
        ApplicationManagementService applicationManagementService = ApplicationManagementService.getInstance();
        ServiceProvider serviceProvider = applicationManagementService
                .getServiceProvider(application.getClientName(), tenantDomain);

        //get JWKS URI from the request
        String jwksUri = RegistrationUtils.getJwksUriFromRequest(request, jwksEndpointName);
        serviceProvider.setJwksUri(jwksUri);

        ServiceProviderProperty[] serviceProviderProperties = serviceProvider.getSpProperties();
        if (log.isDebugEnabled()) {
            log.debug("Retrieved client meta data for application " + application.getClientName());
        }
        List<ServiceProviderProperty> spPropertyList = Arrays.asList(serviceProviderProperties);
        Map<String, Object> storedSPMetaData = RegistrationUtils.getSpMetaDataMap(spPropertyList);

        String clientIdIssuedAt = "";
        if (storedSPMetaData.containsKey(RegistrationConstants.CLIENT_ID_ISSUED_AT)) {
            clientIdIssuedAt = storedSPMetaData.get(RegistrationConstants.CLIENT_ID_ISSUED_AT).toString();
        }

        //update Service provider with new client data
        Map<String, String> updateRequestData = RegistrationUtils.getAlteredApplicationAttributes(request);
        Map<String, Object> updateRegistrationData = request.getRequestParameters();
        if (request.getSsaParameters() != null) {
            updateRegistrationData.putAll(request.getSsaParameters());
        }
        updateRequestData.put(RegistrationConstants.CLIENT_ID_ISSUED_AT, clientIdIssuedAt);
        // Adding SP property to identify update request. Will be removed when updating authenticators.
        updateRequestData.put("AppCreateRequest", "false");
        List<ServiceProviderProperty> spMetaData = RegistrationUtils.getServiceProviderPropertyList(updateRequestData);
        serviceProvider.setSpProperties(spMetaData.toArray(new ServiceProviderProperty[0]));
        applicationManagementService.updateApplication(serviceProvider, tenantDomain, userName);

        if (log.isDebugEnabled()) {
            log.debug("Updated Service Provider meta data for application " + application.getClientName());
        }

        updateRegistrationData.put(RegistrationConstants.CLIENT_ID, application.getClientId());
        updateRegistrationData.put(RegistrationConstants.CLIENT_ID_ISSUED_AT, clientIdIssuedAt);
        updateRegistrationData.put(RegistrationConstants.REGISTRATION_ACCESS_TOKEN, accessToken);
        updateRegistrationData.putAll(additionalAttributes);
        String registrationResponse = RegistrationValidator.getRegistrationValidator()
                .getRegistrationResponse(updateRegistrationData);
        return Response.status(Response.Status.OK).entity(registrationResponse).build();
    }

    public Response deleteRegistration(String clientId) throws DCRMException {

        DCRMService dcrmService = getDCRServiceInstance();
        dcrmService.deleteApplication(clientId);
        if (log.isDebugEnabled()) {
            log.debug("Deleted application with client Id :" + clientId);
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    public DCRMService getDCRServiceInstance() {

        if (this.oAuth2DCRMService == null) {
            DCRMService oAuth2DCRMService = (DCRMService) PrivilegedCarbonContext.
                    getThreadLocalCarbonContext().getOSGiService(DCRMService.class, null);
            if (oAuth2DCRMService != null) {
                this.oAuth2DCRMService = oAuth2DCRMService;
            }
        }
        return this.oAuth2DCRMService;
    }

    public OpenBankingConfigurationService getOBConfigService() {

        if (this.openBankingConfigurationService == null) {
            OpenBankingConfigurationService openBankingConfigurationService =
                    (OpenBankingConfigurationService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .getOSGiService(OpenBankingConfigurationService.class, null);
            if (openBankingConfigurationService != null) {
                this.openBankingConfigurationService = openBankingConfigurationService;
            }
        }
        return this.openBankingConfigurationService;
    }
}
