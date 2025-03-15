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

package org.wso2.financial.services.accelerator.event.notifications.endpoint.util;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.event.notifications.service.handler.EventSubscriptionServiceHandler;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventSubscriptionResponse;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

/**
 * Events Notification Subscription API Utils.
 */
public class EventSubscriptionUtils {

    private EventSubscriptionUtils() {

    }

    /**
     * Extract string payload from request object.
     */
    public static EventSubscriptionServiceHandler getEventSubscriptionServiceHandler() {

        return FinancialServicesUtils.getClassInstanceFromFQN(FinancialServicesConfigParser.getInstance()
                        .getConfiguration().get(FinancialServicesConstants.EVENT_SUBSCRIPTION_HANDLER).toString(),
                EventSubscriptionServiceHandler.class);
    }

    /**
     * Extract string payload from request object.
     *
     * @param request The request object
     * @return String payload
     */
    public static JSONObject getJSONObjectPayload(HttpServletRequest request) throws IOException {
        return new JSONObject(IOUtils.toString(request.getInputStream()));
    }

    /**
     * Method to map the Event Creation Service Response to API response.
     *
     * @param eventSubscriptionResponse  Event Subscription Response
     * @return Response
     */
    public static Response mapEventSubscriptionServiceResponse(EventSubscriptionResponse eventSubscriptionResponse) {
        if (eventSubscriptionResponse.getResponseBody() != null) {
            return Response.status(eventSubscriptionResponse.getResponseStatus())
                    .entity(eventSubscriptionResponse.getResponseBody().toString())
                    .build();
        } else {
            return Response.status(eventSubscriptionResponse.getResponseStatus())
                    .build();
        }
    }
}
