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

package org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthAdminServiceImpl;
import org.wso2.carbon.identity.oauth.dcr.exception.DCRMClientException;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationService;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.validators.DynamicClientRegistrationValidator;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByKey;

/**
 * DCR Utils Class.
 */
public class DCRUtils {

    private static final Log log = LogFactory.getLog(DCRUtils.class);
    private static final IdentityExtensionsDataHolder identityDataHolder = IdentityExtensionsDataHolder.getInstance();
    private static final FinancialServicesConfigurationService configurationService =
            identityDataHolder.getConfigurationService();

    /**
     * Method to extract Fapi Compliant Property From Service Provider.
     * @param serviceProvider   Service Provider
     *
     * @return Fapi Compliant Property
     * @throws IdentityOAuthAdminException When there is an error while retrieving OAuthConsumerAppDTO
     * @throws RequestObjectException When there is an error while retrieving Fapi compliant property
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public static boolean getFapiCompliantPropertyFromSP(ServiceProvider serviceProvider)
            throws IdentityOAuthAdminException, RequestObjectException {

        OAuthConsumerAppDTO oAuthConsumerAppDTO = getOAuthConsumerAppDTO(serviceProvider.getApplicationName());
        return FinancialServicesUtils.isRegulatoryApp(oAuthConsumerAppDTO.getOauthConsumerKey());
    }

    /**
     * Method to get OAuthConsumerAppDTO using service provider application name.
     *
     * @param spApplicationName    Service provider application name
     * @return OAuthConsumerAppDTO
     * @throws IdentityOAuthAdminException when there is an error
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public static OAuthConsumerAppDTO getOAuthConsumerAppDTO(String spApplicationName)
            throws IdentityOAuthAdminException {

        OAuthAdminServiceImpl oAuthAdminService = identityDataHolder.getOauthAdminService();
        return oAuthAdminService.getOAuthApplicationDataByAppName(spApplicationName);
    }

    /**
     * Get the response parameters from the configuration.
     * @return Set of response parameters.
     */
    public static Set<String> getResponseParamsFromConfig() {

        Set<String> responseParams = new HashSet<>();

        Map<String, Map<String, Object>> dcrConfigs = configurationService.getDCRParamsConfig();

        dcrConfigs.forEach((key, value) -> {
            if (Boolean.parseBoolean(value.get(IdentityCommonConstants.INCLUDE_IN_RESPONSE).toString())) {
                responseParams.add(value.get(IdentityCommonConstants.KEY).toString());
            }
        });

        return responseParams;
    }

    /**
     * Get the enabled DCR validators.
     * @return List of enabled DCR validators.
     */
    public static List<DynamicClientRegistrationValidator> getEnabledDcrValidators() {

        Map<String, Map<String, Object>> dcrValidators = configurationService.getDCRValidatorsConfig();

        Map<Integer, DynamicClientRegistrationValidator> validatorMap = new HashMap<>();

        for (Map<String, Object> validator : dcrValidators.values()) {
            if (Boolean.parseBoolean(validator.get(IdentityCommonConstants.ENABLE).toString())) {
                DynamicClientRegistrationValidator dcrValidator = FinancialServicesUtils.getClassInstanceFromFQN(
                        validator.get(IdentityCommonConstants.CLASS).toString(),
                        DynamicClientRegistrationValidator.class);
                validatorMap.put(Integer.parseInt(validator.get(IdentityCommonConstants.PRIORITY).toString()),
                        dcrValidator);
            }
        }

        LinkedHashMap<Integer, DynamicClientRegistrationValidator> priorityMap = validatorMap.entrySet()
                .stream()
                .sorted(comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));

        return priorityMap.keySet().stream()
                .map(priorityMap::get).collect(Collectors.toList());
    }

    /**
     * Method to invoke the external service
     * .
     * @param appRequest                 Application request.
     * @param ssaParams                  SSA parameters.
     * @param spProperties               Service provider properties.
     * @param serviceExtensionTypeEnum   Service extension type.
     * @return                         Custom attributes to store.
     * @throws DCRMClientException      When an error occurs while getting custom attributes to store.
     */
    public static Map<String, Object> callExternalService(JSONObject appRequest,
                                                           Map<String, Object> ssaParams,
                                                           List<JSONObject> spProperties,
                                                           ServiceExtensionTypeEnum serviceExtensionTypeEnum)
            throws DCRMClientException  {

        try {
            log.debug("Executing external service call to get custom attributes to store");
            ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(
                    getExternalServiceRequest(appRequest, ssaParams, spProperties),
                    serviceExtensionTypeEnum);
            if (StatusEnum.SUCCESS.equals(response.getStatus())) {
                JSONObject attributesToStoreJson = new JSONObject(response.getData()
                        .get(IdentityCommonConstants.CLIENT_DATA)
                        .toString());
                return attributesToStoreJson.toMap();
            } else {
                String dcrErrorCode = response.getData().path(FinancialServicesConstants.ERROR_CODE)
                        .asText(IdentityCommonConstants.INVALID_CLIENT_METADATA);
                String errDesc = response.getData().path(FinancialServicesConstants.ERROR_DESCRIPTION)
                        .asText(FinancialServicesConstants.DEFAULT_ERROR_DESCRIPTION);
                throw new DCRMClientException(dcrErrorCode, errDesc);
            }
        } catch (FinancialServicesException e) {
            throw new DCRMClientException(IdentityCommonConstants.SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Method to invoke the external service
     * .
     * @param ssaParams                  SSA parameters.
     * @return                         Custom attributes to store.
     * @throws DCRMClientException      When an error occurs while getting custom attributes to store.
     */
    public static Map<String, Object> callExternalServiceForRetrieval(Map<String, String> ssaParams)
            throws DCRMClientException  {

        try {
            log.debug("Executing external service call to get custom attributes to store");
            ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(
                    getExternalServiceRequestForRetrieval(ssaParams),
                    ServiceExtensionTypeEnum.PRE_PROCESS_CLIENT_RETRIEVAL);
            if (StatusEnum.SUCCESS.equals(response.getStatus())) {
                JSONObject attributesToStoreJson = new JSONObject(response.getData()
                        .get(IdentityCommonConstants.CLIENT_DATA)
                        .toString());
                return attributesToStoreJson.toMap();
            } else {
                String dcrErrorCode = response.getData().path(FinancialServicesConstants.ERROR_CODE)
                        .asText(IdentityCommonConstants.INVALID_CLIENT_METADATA);
                String errDesc = response.getData().path(FinancialServicesConstants.ERROR_DESCRIPTION)
                        .asText(FinancialServicesConstants.DEFAULT_ERROR_DESCRIPTION);
                throw new DCRMClientException(dcrErrorCode, errDesc);
            }
        } catch (FinancialServicesException e) {
            throw new DCRMClientException(IdentityCommonConstants.SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Method to get the external service request.
     * @param appRequest      Application request.
     * @param ssaParams       SSA parameters.
     * @param spProperties    Service provider properties.
     * @return               External service request.
     */
    public static ExternalServiceRequest getExternalServiceRequest(JSONObject appRequest,
                                                                    Map<String, Object> ssaParams,
                                                                    List<JSONObject> spProperties) {
        JSONObject data = new JSONObject();
        data.put(IdentityCommonConstants.CLIENT_DATA, appRequest);
        data.put(IdentityCommonConstants.SSA_PARAMS, ssaParams);
        if (spProperties != null) {
            data.put(IdentityCommonConstants.EXISTING_CLIENT_DATA, spProperties);
        }

        return new ExternalServiceRequest(UUID.randomUUID().toString(), data);
    }

    /**
     * Method to get the external service request.
     * @param ssaParams       SSA parameters.
     * @return               External service request.
     */
    public static ExternalServiceRequest getExternalServiceRequestForRetrieval(Map<String, String> ssaParams) {
        JSONObject data = new JSONObject();
        data.put(IdentityCommonConstants.SSA_PARAMS, ssaParams);
        return new ExternalServiceRequest(UUID.randomUUID().toString(), data);
    }

    /**
     * Construct the service provider property list.
     * @param serviceProviderProperties  Service provider properties.
     * @return                        List of service provider properties.
     */
    public static List<JSONObject> constructSPPropertyList(ServiceProviderProperty[] serviceProviderProperties) {

        List<JSONObject> spPropertyList = new ArrayList<>();
        for (ServiceProviderProperty property : serviceProviderProperties) {
            JSONObject propertyObject = new JSONObject();
            propertyObject.put("Name", property.getName());
            propertyObject.put("Value", property.getValue());
            propertyObject.put("DisplayName", property.getDisplayName());
            spPropertyList.add(propertyObject);
        }
        return spPropertyList;
    }

    /**
     * Validates the "require request object" value in the payload. This value must be true for
     * FAPI-compliant applications.
     *
     * @param requireRequestObject  The value indicating whether the request object is required.
     * @throws DCRMClientException If the "require request object" value does not meet FAPI requirements.
     */
    public static void validateRequireRequestObject(boolean requireRequestObject) throws DCRMClientException {
        if (Boolean.parseBoolean(IdentityUtil.getProperty("OAuth.DCRM.EnableFAPIEnforcement")) &&
                !requireRequestObject) {
            throw new DCRMClientException(IdentityCommonConstants.INVALID_CLIENT_METADATA,
                    "Require request object value is incompatible with FAPI requirements");
        }
    }
}
