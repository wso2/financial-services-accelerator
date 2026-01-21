/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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


package org.wso2.financial.services.accelerator.data.publisher.thrift;

import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsCustomDataProvider;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import static org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS;

/**
 * This is the thrift based data provider class for apim
 */
public class ThriftAPIMAnalyticsDataProvider implements AnalyticsCustomDataProvider {

    private static final Log log = LogFactory.getLog(ThriftAPIMAnalyticsDataProvider.class);
    private static final String UNKNOWN_USER_TENANT_DOMAIN = "UNKNOWN";

    public ThriftAPIMAnalyticsDataProvider() {
        log.debug("Successfully initialized ThriftAnalyticsDataProvider");
    }

    @Override
    public Map<String, Object> getCustomProperties(Object context) {
        Map<String, Object> customProperties = new HashMap<>();
        try {
            if (context instanceof MessageContext) {
                MessageContext messageContext = (MessageContext) context;

                AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext((MessageContext) context);
                customProperties.put(PropertyKeys.APPLICATION_CONSUMER_KEY, getApplicationConsumerKey(authContext));
                customProperties.put(PropertyKeys.APPLICATION_ID, getApplicationId(authContext));
                customProperties.put(PropertyKeys.USER_ID, getUserId(authContext));
                customProperties.put(PropertyKeys.CONSENT_ID, getConsentId(messageContext));
                customProperties.put(PropertyKeys.USER_TENANT_DOMAIN, getUserTenantDomain(authContext));
                customProperties.put(PropertyKeys.API_RESOURCE_PATH, getApiResourcePath(messageContext));
                customProperties.put(PropertyKeys.REQUEST_HEADERS, getRequestHeaders(messageContext));
                customProperties.put(PropertyKeys.RESPONSE_BODY, getResponseBodyDetails(messageContext).
                        get(PropertyKeys.RESPONSE_BODY));
                customProperties.put(PropertyKeys.RESPONSE_BODY_SIZE, getResponseBodyDetails(messageContext).
                        get(PropertyKeys.RESPONSE_BODY_SIZE));

            }
        } catch (Exception e) {
            log.error("Cannot build custom properties, hence returning an empty map for custom properties.", e);
        }
        return customProperties;
    }

    public String getApplicationConsumerKey(AuthenticationContext authenticationContext) {
        if (authenticationContext != null) {
            return authenticationContext.getConsumerKey();
        }
        return null;
    }

    public String getConsentId(MessageContext messageContext) {
        if (messageContext != null) {
            return (String) messageContext.getProperty("CONSENT_ID");
        }
        return null;
    }

    public String getApplicationId(AuthenticationContext authenticationContext) {
        if (authenticationContext != null) {
            return authenticationContext.getApplicationId();
        }
        return null;
    }

    private String getApiResourcePath(MessageContext messageContext) {
        return GatewayUtils.extractResource(messageContext);
    }

    private String getTier(AuthenticationContext authenticationContext) {
        if (authenticationContext != null) {
            return authenticationContext.getTier();
        }
        return null;
    }

    public String getUserId(AuthenticationContext authenticationContext) {
        if (authenticationContext != null) {
            return authenticationContext.getUsername();
        }
        return "";
    }


    private String getUserTenantDomain(AuthenticationContext authenticationContext) {
        if (authenticationContext != null) {
            return MultitenantUtils.getTenantDomain(authenticationContext.getUsername());
        }
        return UNKNOWN_USER_TENANT_DOMAIN;
    }

    public Object getRequestHeaders(MessageContext messageContext) {
        if (messageContext != null) {
            String excludedHeaders = null;
            Map headers = (Map) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                    .getProperty(TRANSPORT_HEADERS);
            Map<String, String> configs = APIManagerConfiguration.getAnalyticsProperties();
            if (configs.containsKey("EXCLUDED_HEADERS")) {
                excludedHeaders = configs.get("EXCLUDED_HEADERS");
            }
            if (excludedHeaders != null) {
                String[] keysToRemove = excludedHeaders.split("\\s*,\\s*");
                // 2. Iterate through the keys and remove them from the map.
                for (String key : keysToRemove) {
                    String trimmedKey = key.trim();
                    // Check if the key exists in the map (optional, but good practice)
                    if (headers.containsKey(trimmedKey)) {
                        headers.remove(trimmedKey);
                    }
                }
            }

            return headers;
        }
        return null;
    }

    private Map<String, String> getResponseBodyDetails(MessageContext messageContext) {
        long responseSize = 0L;
        String responseBody = null;
        Map<String, String> responseBodyDetails = new HashMap<>();

        try {
            RelayUtils.buildMessage(((Axis2MessageContext) messageContext).getAxis2MessageContext());
        } catch (IOException ex) {
            //In case of an exception, it won't be propagated up,and set response size to 0
            log.error("Error occurred while building the message to" +
                    " calculate the response body size", ex);
        } catch (XMLStreamException ex) {
            log.error("Error occurred while building the message to calculate the response" +
                    " body size", ex);
        }

        SOAPEnvelope env = messageContext.getEnvelope();
        if (env != null) {
            SOAPBody soapbody = env.getBody();
            if (soapbody != null) {
                responseBody = soapbody.toString();
                byte[] size = responseBody.getBytes(Charset.defaultCharset());
                responseSize = size.length;


            }
        }
        responseBodyDetails.put(PropertyKeys.RESPONSE_BODY_SIZE, String.valueOf(responseSize));
        responseBodyDetails.put(PropertyKeys.RESPONSE_BODY, responseBody);
        return responseBodyDetails;
    }


    private static class PropertyKeys {
        static final String APPLICATION_CONSUMER_KEY = "applicationConsumerKey";
        static final String APPLICATION_ID = "applicationId";
        static final String USER_ID = "userId";
        static final String CONSENT_ID = "consentId";
        static final String API_RESOURCE_PATH = "apiResourcePath";
        static final String REQUEST_HEADERS = "requestHeaders";
        static final String USER_TENANT_DOMAIN = "userTenantDomain";
        static final String RESPONSE_BODY = "responseBody";
        static final String RESPONSE_BODY_SIZE = "responseBodySize";

    }

}


