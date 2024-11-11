/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.event.notifications.service.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.wso2.financial.services.accelerator.event.notifications.service.EventNotificationGenerator;
import org.wso2.financial.services.accelerator.event.notifications.service.EventSubscriptionService;
import org.wso2.financial.services.accelerator.event.notifications.service.RealtimeNotificationService;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.realtime.service.RealtimeEventNotificationRequestGenerator;

import java.util.Optional;

/**
 * Default event notification validations.
 */
public class EventNotificationServiceUtil {

    private static final Log log = LogFactory.getLog(EventNotificationServiceUtil.class);
    private static volatile ConsentCoreServiceImpl consentCoreService;

    /**
     * This method is used to send the polling generator as per config.
     *
     * @return EventNotificationGenerator
     */
    public static EventNotificationGenerator getEventNotificationGenerator() {

        return (EventNotificationGenerator)
                FinancialServicesUtils.getClassInstanceFromFQN(FinancialServicesConfigParser.getInstance()
                        .getEventNotificationGenerator());
    }

    /**
     * This method is used to send the default realtime event notification request generator.
     *
     * @return RealtimeEventNotificationRequestGenerator
     */
    public static RealtimeEventNotificationRequestGenerator getRealtimeEventNotificationRequestGenerator() {

        RealtimeEventNotificationRequestGenerator realtimeEventNotificationRequestGenerator =
                (RealtimeEventNotificationRequestGenerator) FinancialServicesUtils
                        .getClassInstanceFromFQN(FinancialServicesConfigParser.getInstance().
                        getRealtimeEventNotificationRequestGenerator());
        return realtimeEventNotificationRequestGenerator;
    }

    /**
     * Method to modify event notification payload with custom eventValues.
     *
     * @param jsonNode Json Node to convert
     * @return String eventNotificationPayload
     */
    public static String getCustomNotificationPayload(JsonNode jsonNode) {

        return jsonNode.toString();
    }

    /**
     * Method to get event JSON from eventInformation payload string.
     * @param eventInformation String event Information
     * @return JSONObject converted event json
     * @throws JSONException  Exception when parsing event information
     */
    public static JSONObject getEventJSONFromString(String eventInformation) throws JSONException {

        return new JSONObject(eventInformation);
    }

    /**
     * Validate if the client ID is existing.
     * @param clientId  client ID of the TPP
     * @throws FSEventNotificationException  Exception when validating client ID
     */
    @Generated(message = "Excluded since this needs OAuth2Util service provider")
    public static void validateClientId(String clientId) throws FSEventNotificationException {

        if (StringUtils.isNotEmpty(clientId)) {
            Optional<ServiceProvider> serviceProvider;
            try {
                serviceProvider = Optional.ofNullable(OAuth2Util.getServiceProvider(clientId));
                if (!serviceProvider.isPresent()) {
                    log.error(EventNotificationConstants.INVALID_CLIENT_ID);
                    throw new FSEventNotificationException(EventNotificationConstants.INVALID_CLIENT_ID);
                }
            } catch (IdentityOAuth2Exception e) {
                log.error(EventNotificationConstants.INVALID_CLIENT_ID, e);
                throw new FSEventNotificationException(EventNotificationConstants.INVALID_CLIENT_ID);
            }
        }
    }

    @Generated(message = "Creating a single instance for ConsentCoreService")
    public static synchronized ConsentCoreServiceImpl getConsentCoreServiceImpl() {
        if (consentCoreService == null) {
            synchronized (ConsentCoreServiceImpl.class) {
                if (consentCoreService == null) {
                    consentCoreService = new ConsentCoreServiceImpl();
                }
            }
        }
        return consentCoreService;
    }

    /**
     * Get the callback URL of the TPP from the Subscription Object.
     *
     * @param clientID client ID of the TPP
     * @return callback URL of the TPP
     */
    public static String getCallbackURL(String clientID) {

        return "http://localhost:8080/sample-tpp-server";
    }

    /**
     * Method to map Event subscription Service error to API response.
     *
     * @param error             Error code
     * @param errorDescription  Error description
     * @return String error response
     */
    public static String getErrorDTO(String error, String errorDescription) {
        JSONObject eventNotificationError = new JSONObject();
        eventNotificationError.put(EventNotificationConstants.ERROR_FIELD, error);
        eventNotificationError.put(EventNotificationConstants.ERROR_DESCRIPTION_FIELD, errorDescription);
        return eventNotificationError.toString();
    }

    public static EventSubscriptionService getEventSubscriptionService() {
        return new EventSubscriptionService();
    }

    public static RealtimeNotificationService getRealtimeNotificationService() {
        return new RealtimeNotificationService();
    }

}
