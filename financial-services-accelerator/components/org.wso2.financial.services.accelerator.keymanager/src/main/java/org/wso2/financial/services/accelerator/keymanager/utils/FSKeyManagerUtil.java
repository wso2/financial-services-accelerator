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

package org.wso2.financial.services.accelerator.keymanager.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.CertificateUtils;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.keymanager.FSKeyManagerExtensionInterface;
import org.wso2.financial.services.accelerator.keymanager.dto.ExternalAPIApplicationCreationRequestDTO;
import org.wso2.financial.services.accelerator.keymanager.dto.ExternalAPIApplicationCreationResponseDTO;
import org.wso2.financial.services.accelerator.keymanager.dto.ExternalAPIApplicationUpdateRequestDTO;
import org.wso2.financial.services.accelerator.keymanager.dto.ExternalAPIApplicationUpdateResponseDTO;

import java.lang.reflect.InvocationTargetException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Util class for FS key manager.
 */
public class FSKeyManagerUtil {

    private static final Log log = LogFactory.getLog(FSKeyManagerUtil.class);
    private static final  ObjectMapper mapper = new ObjectMapper();

    /**
     * Obtain FS Key Manager Extension Impl class from config.
     *
     * @return FSKeyManagerExtensionInterface
     */
    public static FSKeyManagerExtensionInterface getKeyManagerExtensionImpl() throws APIManagementException {
        FSKeyManagerExtensionInterface keyManagerExtensionImpl;
        try {
            String keyManagerExtensionImplName = FinancialServicesConfigParser.getInstance()
                    .getKeyManagerExtensionImpl();
            if (!StringUtils.isEmpty(keyManagerExtensionImplName)) {
                keyManagerExtensionImpl = (FSKeyManagerExtensionInterface)
                        Class.forName(keyManagerExtensionImplName).getDeclaredConstructor().newInstance();
                return keyManagerExtensionImpl;
            } else {
                return null;
            }

        } catch (InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new APIManagementException("Failed to obtain FS Key Manager Extension Impl instance", e);
        }
    }

    /**
     * Extract values for additional properties from input.
     *
     * @param oauthAppRequest OAuthAppRequest object
     * @return Additional Property Map
     * @throws APIManagementException
     */
    public static HashMap<String, String> getValuesForAdditionalProperties(OAuthAppRequest oauthAppRequest)
            throws APIManagementException {
        // Get additional properties defined in the config
        Map<String, Map<String, String>> keyManagerAdditionalProperties = FinancialServicesConfigParser.getInstance()
                .getKeyManagerAdditionalProperties();
        HashMap<String, String> additionalProperties = new HashMap<>();
        JSONObject additionalPropertiesJSON;
        try {
            // Get values for additional properties given at key generation step
            additionalPropertiesJSON = new JSONObject((String) oauthAppRequest.getOAuthApplicationInfo()
                    .getParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES));
        } catch (JSONException e) {
            log.error(String.format("%s of the OAuth app %s is not a JSON object",
                    APIConstants.JSON_ADDITIONAL_PROPERTIES.replaceAll("[\r\n]", ""),
                    oauthAppRequest.getOAuthApplicationInfo().getClientId().replaceAll("[\r\n]", "")), e);
            throw new APIManagementException(ExceptionCodes.JSON_PARSE_ERROR.getErrorMessage(), e,
                    ExceptionCodes.JSON_PARSE_ERROR);
        }

        // Add values of additional properties defined in the config to the default additional property list JSON object
        for (String key : keyManagerAdditionalProperties.keySet()) {
            if (additionalPropertiesJSON.has(key)) {
                additionalProperties.put(key, additionalPropertiesJSON.getString(key));
            }
        }
        return additionalProperties;
    }

    /**
     * Obtain Application role name using application name.
     * @param applicationName Application name
     * @return Application role name
     */
    @Generated(message = "excluding from coverage because method doesn't contains any logic")
    protected static String getAppRoleName(String applicationName) {

        return org.wso2.carbon.identity.application.mgt.ApplicationConstants.APPLICATION_DOMAIN +
                UserCoreConstants.DOMAIN_SEPARATOR + applicationName;
    }

    /**
     * Obtain the value from Configuration DTO object.
     * @param fsAdditionalProperties Additional Property Map
     * @param propertyName Property Name
     * @return value for given property
     * @throws APIManagementException if the property is not found or has no values
     */
    public static String getValueForAdditionalProperty(Map<String, ConfigurationDto> fsAdditionalProperties,
                                                       String propertyName) throws APIManagementException {
        ConfigurationDto property = fsAdditionalProperties.get(propertyName);
        if (property != null) {
            List<Object> values = property.getValues();
            if (values.size() > 0) {
                return (String) values.get(0);
            } else {
                String msg = String.format("No value found for additional property: %s",
                        propertyName.replaceAll("[\r\n]", ""));
                log.error(msg);
                throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
            }
        } else {
            String msg = String.format("%s property not found in additional properties",
                    propertyName.replaceAll("[\r\n]", ""));
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }
    }

    /**
     * Validate certificate provided as user input.
     * @param cert Certificate string
     * @throws APIManagementException if the certificate is invalid or expired
     */
    @Generated(message = "Excluding from code coverage since it is covered from other method")
    public static void validateCertificate(String cert) throws APIManagementException {
        X509Certificate certificate;
        try {
            certificate = CertificateUtils.parseCertificate(cert);
        } catch (FinancialServicesException e) {
            String msg = "Certificate unavailable";
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }
        if (CertificateUtils.isExpired(certificate)) {
            String msg = "Provided certificate expired";
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }
        log.debug("Provided certificate successfully validated");
    }

    /**
     * Method to invoke the external service
     * .
     * @param oAuthAppRequest            OAuth Application Details.
     * @param additionalProperties       Additional properties.
     * @param serviceProvider            Service provider details.
     * @param serviceType                  Operation type (CREATE/UPDATE).
     * @throws APIManagementException      When an error occurs while invoking the external service.
     */
    public static void callExternalService(OAuthAppRequest oAuthAppRequest,
                                           HashMap<String, String> additionalProperties,
                                           JSONObject serviceProvider,
                                           ServiceExtensionTypeEnum serviceType)
            throws APIManagementException  {

        try {
            log.debug("Executing external service call");
            ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(
                    getExternalServiceRequest(oAuthAppRequest, additionalProperties, serviceProvider, serviceType),
                    serviceType);
            if (StatusEnum.SUCCESS.equals(response.getStatus())) {
                if (ServiceExtensionTypeEnum.PRE_PROCESS_APPLICATION_CREATION.equals(serviceType)) {
                    ExternalAPIApplicationCreationResponseDTO responseDTO = mapper.convertValue(
                            response.getData(), ExternalAPIApplicationCreationResponseDTO.class);
                    if (responseDTO.getClientId() != null) {
                        log.debug("Setting client id from external service as OAuth App client Id");
                        oAuthAppRequest.getOAuthApplicationInfo().setClientId(responseDTO.getClientId());
                    }
                    additionalProperties.putAll(responseDTO.getAdditionalAppData());
                } else {
                    ExternalAPIApplicationUpdateResponseDTO responseDTO = mapper.convertValue(
                            response.getData(), ExternalAPIApplicationUpdateResponseDTO.class);
                    additionalProperties.putAll(responseDTO.getAdditionalAppData());
                }
            } else {
                String errorMessage = response.getData().path(FinancialServicesConstants.ERROR_MESSAGE)
                        .asText(FinancialServicesConstants.DEFAULT_ERROR_DESCRIPTION);
                throw new APIManagementException(errorMessage);
            }
        } catch (FinancialServicesException e) {
            throw new APIManagementException("Error Occurred while invoking the external service", e);
        }
    }

    /**
     * Method to get the external service request.
     *
     * @param oAuthAppRequest            OAuth Application Details.
     * @param additionalProperties       Additional properties.
     * @param serviceProvider            Service provider details.
     * @return               External service request.
     */
    public static ExternalServiceRequest getExternalServiceRequest(OAuthAppRequest oAuthAppRequest,
                                                                   HashMap<String, String> additionalProperties,
                                                                   JSONObject serviceProvider,
                                                                   ServiceExtensionTypeEnum serviceType) {

        Map<String, Object> oAuthAppRequestMap = mapper.convertValue(oAuthAppRequest, Map.class);
        JSONObject dataObject = new JSONObject();
        if (ServiceExtensionTypeEnum.PRE_PROCESS_APPLICATION_CREATION.equals(serviceType)) {
            ExternalAPIApplicationCreationRequestDTO externalAPIApplicationCreationRequestDTO =
                    new ExternalAPIApplicationCreationRequestDTO(oAuthAppRequestMap, additionalProperties);
            dataObject = new JSONObject(externalAPIApplicationCreationRequestDTO);
        } else {
            ExternalAPIApplicationUpdateRequestDTO externalAPIApplicationUpdateRequestDTO =
                    new ExternalAPIApplicationUpdateRequestDTO(oAuthAppRequestMap, additionalProperties,
                            serviceProvider);
            dataObject = new JSONObject(externalAPIApplicationUpdateRequestDTO);
        }
        return new ExternalServiceRequest(UUID.randomUUID().toString(), dataObject);
    }
}
