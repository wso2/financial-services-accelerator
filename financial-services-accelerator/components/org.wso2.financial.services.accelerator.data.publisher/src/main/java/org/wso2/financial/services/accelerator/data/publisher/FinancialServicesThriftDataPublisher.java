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

package org.wso2.financial.services.accelerator.data.publisher;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.data.publisher.constants.DataPublishingConstants;
import org.wso2.financial.services.accelerator.data.publisher.internal.FSAnalyticsDataHolder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Open Banking Thrift Data publisher.
 */
public class FinancialServicesThriftDataPublisher implements FinancialServicesDataPublisher {

    private DataPublisher dataPublisher;
    private Map<String, List<String>> streamAttributeMap = new HashMap<>();
    private Map<String, Map<String, Object>> attributeValidationMap;
    private static final Log log = LogFactory.getLog(FinancialServicesThriftDataPublisher.class);
    private Map<String, Object> obConfigurations;

    public FinancialServicesThriftDataPublisher() {
        log.debug("Initializing FinancialServicesThriftDataPublisher");
        this.init();
    }

    @Override
    public void publish(String streamName, String streamVersion, Map<String, Object> analyticsData) {

        // Set payloads
        Object[] payload = setPayload(streamName, analyticsData);

        // Log error and return if payload is not set
        if (payload.length == 0) {
            log.error("Error while setting payload to publish data.");
            return;
        }

        // Create wso2 event to publish
        Event event = new Event();
        event.setStreamId(DataBridgeCommonsUtils.generateStreamId(streamName, streamVersion));
        event.setMetaData(null);
        event.setCorrelationData(null);
        event.setPayloadData(payload);
        event.setTimeStamp(Instant.now().toEpochMilli());

        try {
            // Try to publish event with timeout
            // If the queue is full, this will wait timeout time and retry to add to queue. If still full this
            // returns false
            boolean published = dataPublisher.tryPublish(event,
                    Long.parseLong((String) obConfigurations.get(DataPublishingConstants.THRIFT_PUBLISHING_TIMEOUT)));
            if (!published) {
                log.error("Unable to publish data for stream: " + streamName.replaceAll("[\r\n]",
                        "") + ". Queue is full.");
            }
        } catch (Exception e) {
            // Catching exception and logging error because data publishing issues should not hinder other flows.
            log.error("Error occurred while publishing the data", e);
        }

    }

    /**
     * Initialize OB Thrift Data publisher.
     * This method initializes data publisher and read the attribute map for each data stream.
     */
    protected void init() {

        log.debug("Initializing the Open Banking Thrift data publisher");
        obConfigurations = FSAnalyticsDataHolder.getInstance().getConfigurationMap();
        String serverUser = (String) obConfigurations.get(DataPublishingConstants.DATA_PUBLISHING_USERNAME);
        String serverPassword = (String) obConfigurations.get(DataPublishingConstants.DATA_PUBLISHING_PASSWORD);
        String serverURL = (String) obConfigurations.get(DataPublishingConstants.DATA_PUBLISHING_SERVER_URL);
        String authURL = (String) obConfigurations.get(DataPublishingConstants.DATA_PUBLISHING_AUTH_URL);

        if (serverURL == null || serverPassword == null || serverUser == null) {
            log.error("Error while retrieving publisher server configs");
            return;
        }

        log.debug("Reading attribute list for data streams");
        buildStreamAttributeMap();
        attributeValidationMap = FSAnalyticsDataHolder.getInstance().getFinancialServicesConfigurationService()
                .getDataPublishingValidationMap();

        try {
            //Create new DataPublisher for the tenant.
            dataPublisher = getDataPublisher(serverURL, authURL, serverUser, serverPassword);
        } catch (DataEndpointConfigurationException e) {
            log.error("Error while creating data publisher with the configurations", e);
        } catch (DataEndpointException | DataEndpointAgentConfigurationException | TransportException |
                DataEndpointAuthenticationException e) {
            log.error("Error while creating data publisher", e);
        }

    }

    @Generated(message = "Method to get new Thrift data publisher")
    protected DataPublisher getDataPublisher(String serverURL, String authURL, String serverUser,
                                             String serverPassword)
            throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException,
            DataEndpointException, DataEndpointConfigurationException {

        return new DataPublisher(null, serverURL, authURL, serverUser, serverPassword);
    }

    protected void setDataPublisher(DataPublisher dataPublisher) {
        this.dataPublisher = dataPublisher;
    }

    /**
     * Create event payload for the given stream.
     *
     * @param streamName    stream name
     * @param analyticsData map of data to be published
     * @return payload of object[]
     */
    protected Object[] setPayload(String streamName, Map<String, Object> analyticsData) {

        if (streamAttributeMap.containsKey(streamName)) {
            List<String> attributes = streamAttributeMap.get(streamName);
            boolean isValid = validateAttributes(streamName, attributes, analyticsData);
            if (isValid) {
                ArrayList<Object> payload = new ArrayList<>();
                for (String attribute : attributes) {
                    payload.add(analyticsData.get(attribute));
                }
                return payload.toArray();
            }
        }
        return new Object[]{};
    }

    /**
     * Validate whether the required parameters are present and are of correct data type.
     * @param streamName stream name
     * @param analyticsData data map
     * @return boolean isValid
     */
    private boolean validateAttributes(String streamName, List<String> attributes, Map<String, Object> analyticsData) {

        Map<String, Map<String, Object>> attributeValidations = getAttributeValidationMap();
        for (String attribute: attributes) {
            String attributeNameKey = streamName + "_" + attribute;
            boolean isRequired = (boolean) attributeValidations.get(attributeNameKey)
                    .get(FinancialServicesConstants.REQUIRED);
            String type = (String) attributeValidations.get(attributeNameKey).
                    get(FinancialServicesConstants.ATTRIBUTE_TYPE);

            // validation for required attributes
            if (isRequired) {
                if (analyticsData.containsKey(attribute)) {
                    if (analyticsData.get(attribute) == null) {
                        log.error(attribute.replaceAll("[\r\n]", "") + " is missing in data map " +
                                "for " + streamName.replaceAll("[\r\n]", "") + ". This event "
                                + "will not be processed further.");
                        return false;
                    }
                } else {
                    log.error(attribute.replaceAll("[\r\n]", "") + " is missing in data map for " + 
                    streamName.replaceAll("[\r\n]", "") + ". This event "
                            + "will not be processed further.");
                    return false;
                }
            }

            // validation for data type
            if (analyticsData.containsKey(attribute) && analyticsData.get(attribute) != null) {
                if (!isValidDataType(type, attribute, analyticsData.get(attribute))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Build a map of attributes to be published for each stream.
     */
    protected void buildStreamAttributeMap() {

        Map<String, Map<Integer, String>> dataStreamAttributes = FSAnalyticsDataHolder.getInstance()
                .getFinancialServicesConfigurationService().getDataPublishingStreams();
        dataStreamAttributes.keySet().forEach(dataStream -> {
            Map<Integer, String> integerStringMap = dataStreamAttributes.get(dataStream);
            List<String> attributeList = integerStringMap.keySet().stream()
                    .map(integerStringMap::get).collect(Collectors.toList());
            streamAttributeMap.put(dataStream, attributeList);
        });
    }

    @Generated(message = "Added for testing purposes")
    protected Map<String, List<String>> getStreamAttributeMap() {

        return streamAttributeMap;
    }

    @Generated(message = "Added for testing purposes")
    protected Map<String, Map<String, Object>> getAttributeValidationMap() {
        return attributeValidationMap;
    }

    @SuppressFBWarnings("IMPROPER_UNICODE")
    // Suppressed content - ype.toLowerCase(Locale.ENGLISH)
    // Suppression reason - False Positive : Since the value is used in switch statements, it cannot be used
    //                      maliciously
    // Suppressed warning count - 1
    private boolean isValidDataType(String type, String attributeName, Object attributeValue) {

        Class<?> attributeClass = attributeValue.getClass();
        switch (type.toLowerCase(Locale.ENGLISH)) {
            case "string" :
                if (!(attributeClass.equals(String.class))) {
                    logInvalidDataTypeError(attributeName, String.class.getName(), attributeClass.getName());
                    return false;
                }
                break;
            case "int" :
                if (!(attributeClass.equals(Integer.class))) {
                    logInvalidDataTypeError(attributeName, Integer.class.getName(), attributeClass.getName());
                    return false;
                }
                break;
            case "long" :
                if (!(attributeClass.equals(Long.class))) {
                    logInvalidDataTypeError(attributeName, Long.class.getName(), attributeClass.getName());
                    return false;
                }
                break;
            case "boolean" :
                if (!(attributeClass.equals(Boolean.class))) {
                    logInvalidDataTypeError(attributeName, Boolean.class.getName(), attributeClass.getName());
                    return false;
                }
                break;
            case "double" :
                if (!(attributeClass.equals(Double.class))) {
                    logInvalidDataTypeError(attributeName, Double.class.getName(), attributeClass.getName());
                    return false;
                }
                break;
            case "float" :
                if (!(attributeClass.equals(Float.class))) {
                    logInvalidDataTypeError(attributeName, Float.class.getName(), attributeClass.getName());
                    return false;
                }
                break;
        }
        return true;
    }

    private void logInvalidDataTypeError(String attributeName, String expectedDataType, String actualDataType) {
        log.error(attributeName.replaceAll("[\r\n]", "") + " is expecting a " + 
        expectedDataType.replaceAll("[\r\n]", "") + " type attribute while attribute of " +
                "type " + actualDataType.replaceAll("[\r\n]", "") + " is present.");
    }
}
