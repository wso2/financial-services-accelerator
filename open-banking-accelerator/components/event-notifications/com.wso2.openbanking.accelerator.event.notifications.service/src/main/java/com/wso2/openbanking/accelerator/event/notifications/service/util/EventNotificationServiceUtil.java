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

package com.wso2.openbanking.accelerator.event.notifications.service.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.handler.DefaultEventCreationServiceHandler;
import com.wso2.openbanking.accelerator.event.notifications.service.realtime.service.RealtimeEventNotificationRequestGenerator;
import com.wso2.openbanking.accelerator.event.notifications.service.service.EventNotificationGenerator;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

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

        EventNotificationGenerator eventNotificationGenerator = (EventNotificationGenerator)
                OpenBankingUtils.getClassInstanceFromFQN(OpenBankingConfigParser.getInstance()
                        .getEventNotificationGenerator());
        return eventNotificationGenerator;
    }

    /**
     * This method is used to send the default realtime event notification request generator.
     *
     * @return RealtimeEventNotificationRequestGenerator
     */
    public static RealtimeEventNotificationRequestGenerator getRealtimeEventNotificationRequestGenerator() {

        RealtimeEventNotificationRequestGenerator realtimeEventNotificationRequestGenerator =
                (RealtimeEventNotificationRequestGenerator) OpenBankingUtils
                        .getClassInstanceFromFQN(OpenBankingConfigParser.getInstance().
                        getRealtimeEventNotificationRequestGenerator());
        return realtimeEventNotificationRequestGenerator;
    }

    /**
     * Method to modify event notification payload with custom eventValues.
     * @return String eventNotificationPayload
     */
    public static String getCustomNotificationPayload(JsonNode jsonNode) {

        String payload = jsonNode.toString();
        return payload;
    }

    /**
     * Method to get event JSON from eventInformation payload string.
     * @param eventInformation
     * @return
     * @throws ParseException
     */
    public static JSONObject getEventJSONFromString(String eventInformation) throws ParseException {

        JSONParser parser = new JSONParser();
        return  (JSONObject) parser.parse(eventInformation);
    }

    /**
     * Validate if the client ID is existing.
     * @param clientId
     * @throws OBEventNotificationException
     */
    @Generated(message = "Excluded since this needs OAuth2Util service provider")
    public static void validateClientId(String clientId) throws OBEventNotificationException {

        if (StringUtils.isNotEmpty(clientId)) {
            Optional<ServiceProvider> serviceProvider;
            try {
                serviceProvider = Optional.ofNullable(OAuth2Util.getServiceProvider(clientId));
                if (!serviceProvider.isPresent()) {
                    log.error(EventNotificationConstants.INVALID_CLIENT_ID);
                    throw new OBEventNotificationException(EventNotificationConstants.INVALID_CLIENT_ID);
                }
            } catch (IdentityOAuth2Exception e) {
                log.error(EventNotificationConstants.INVALID_CLIENT_ID, e);
                throw new OBEventNotificationException(EventNotificationConstants.INVALID_CLIENT_ID);
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
     * Get the default event creation service handler.
     *
     * @return DefaultEventCreationServiceHandler
     */
    public static DefaultEventCreationServiceHandler getDefaultEventCreationServiceHandler() {
        return new DefaultEventCreationServiceHandler();
    }
}
