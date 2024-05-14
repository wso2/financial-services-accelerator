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

package com.wso2.openbanking.accelerator.common.config;

import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingRuntimeException;
import com.wso2.openbanking.accelerator.common.util.CarbonUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;


import static java.util.Map.Entry.comparingByKey;

/**
 * Config parser for open-banking.xml.
 */
public class OpenBankingConfigParser {

    // To enable attempted thread-safety using double-check locking
    private static final Object lock = new Object();
    private static final Log log = LogFactory.getLog(OpenBankingConfigParser.class);
    private static final Map<String, Object> configuration = new HashMap<>();
    private static final Map<String, Map<Integer, String>> obExecutors = new HashMap<>();
    private static final Map<String, Map<Integer, String>> dataPublishingStreams = new HashMap<>();
    private static final Map<String, Map<String, Object>> dataPublishingValidationMap = new HashMap<>();
    private static final Map<String, Map<String, Object>> dcrRegistrationConfigs = new HashMap<>();
    private static final Map<String, Map<Integer, String>> authorizeSteps = new HashMap<>();
    private static final Map<String, List<String>> allowedScopes = new HashMap<>();
    private static final Map<String, List<String>> allowedAPIs = new HashMap<>();
    private static final Map<Integer, String> revocationValidators = new HashMap<>();
    private static final List<String> serviceActivatorSubscribers = new ArrayList<>();
    private static final Map<String, Map<String, String>> keyManagerAdditionalProperties
            = new HashMap<>();
    private static Map<Integer, String> obEventExecutors = new HashMap<>();
    private static OpenBankingConfigParser parser;
    private static String configFilePath;
    private static SecretResolver secretResolver;
    private OMElement rootElement;

    private Map<String, String> authWorkerConfig = new HashMap<>();

    /**
     * Private Constructor of config parser.
     */
    private OpenBankingConfigParser() {

        buildConfiguration();
    }

    /**
     * Singleton getInstance method to create only one object.
     *
     * @return OpenBankingConfigParser object
     */
    public static OpenBankingConfigParser getInstance() {

        if (parser == null) {
            synchronized (lock) {
                if (parser == null) {
                    parser = new OpenBankingConfigParser();
                }
            }
        }
        return parser;
    }

    /**
     * Method to get an instance of ConfigParser when custom file path is provided.
     *
     * This method is deprecated as it allows custom absolute file paths which could result in
     * path traversal attacks. Do not use this method unless the custom path is trusted.
     *
     * @param filePath Custom file path
     * @return OpenBankingConfigParser object
     * &#064;Deprecated  use OpenBankingConfigParser.getInstance()
     */
    @Deprecated
    public static OpenBankingConfigParser getInstance(String filePath) {

        configFilePath = filePath;
        return getInstance();
    }

    /**
     * Method to obtain map of configs.
     *
     * @return Config map
     */
    public Map<String, Object> getConfiguration() {

        return configuration;
    }

    /**
     * Method to read the configuration (in a recursive manner) as a model and put them in the configuration map.
     */
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    // Suppressed content - new FileInputStream(configFilePath)
    // Suppression reason - False Positive : Method for passing configFilePath is deprecated and is used for testing
    //                      purposes only. Therefore, it can be assumed that configFilePath is a trusted filepath
    // Suppressed warning count - 1
    private void buildConfiguration() {

        InputStream inStream = null;
        StAXOMBuilder builder;
        String warningMessage = "";
        try {
            if (configFilePath != null) {
                File openBankingConfigXml = new File(configFilePath);
                if (openBankingConfigXml.exists()) {
                    inStream = new FileInputStream(openBankingConfigXml);
                }
            } else {
                File openBankingConfigXml = new File(CarbonUtils.getCarbonConfigDirPath(),
                        OpenBankingConstants.OB_CONFIG_FILE);
                if (openBankingConfigXml.exists()) {
                    inStream = new FileInputStream(openBankingConfigXml);
                }
            }
            if (inStream == null) {
                String message =
                        "open-banking configuration not found at: " + configFilePath + " . Cause - " + warningMessage;
                if (log.isDebugEnabled()) {
                    log.debug(message.replaceAll("[\r\n]", ""));
                }
                throw new FileNotFoundException(message);
            }
            builder = new StAXOMBuilder(inStream);
            builder.setDoDebug(false);
            rootElement = builder.getDocumentElement();
            Stack<String> nameStack = new Stack<>();
            secretResolver = SecretResolverFactory.create(rootElement, true);
            readChildElements(rootElement, nameStack);
            buildOBExecutors();
            buildDataPublishingStreams();
            buildDCRParameters();
            buildConsentAuthSteps();
            buildAllowedScopes();
            buildAllowedSubscriptions();
            buildServiceActivatorSubscribers();
            buildKeyManagerProperties();
            buildOBEventExecutors();
            buildWorkers();
        } catch (IOException | XMLStreamException | OMException e) {
            throw new OpenBankingRuntimeException("Error occurred while building configuration from open-banking.xml",
                    e);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                log.error("Error closing the input stream for open-banking.xml", e);
            }
        }
    }

    private void buildOBExecutors() {

        OMElement gatewayElement = rootElement.getFirstChildWithName(
                new QName(OpenBankingConstants.OB_CONFIG_QNAME,
                        OpenBankingConstants.GATEWAY_CONFIG_TAG));

        if (gatewayElement != null) {

            OMElement openBankingGatewayExecutors = gatewayElement.getFirstChildWithName(
                    new QName(OpenBankingConstants.OB_CONFIG_QNAME,
                            OpenBankingConstants.GATEWAY_EXECUTOR_CONFIG_TAG));

            if (openBankingGatewayExecutors != null) {
                //obtaining each consent type element under OpenBankingGatewayExecutors tag
                Iterator consentTypeElement = openBankingGatewayExecutors.getChildElements();
                while (consentTypeElement.hasNext()) {
                    OMElement consentType = (OMElement) consentTypeElement.next();
                    String consentTypeName = consentType.getLocalName();
                    Map<Integer, String> executors = new HashMap<>();
                    //obtaining each Executor element under each consent type
                    Iterator<OMElement> obExecutor = consentType.getChildrenWithName(
                            new QName(OpenBankingConstants.OB_CONFIG_QNAME, OpenBankingConstants.EXECUTOR_CONFIG_TAG));
                    if (obExecutor != null) {
                        while (obExecutor.hasNext()) {
                            OMElement executorElement = obExecutor.next();
                            //Retrieve class name and priority from executor config
                            String obExecutorClass = executorElement.getAttributeValue(new QName("class"));
                            String obExecutorPriority = executorElement.getAttributeValue(new QName("priority"));

                            if (StringUtils.isEmpty(obExecutorClass)) {
                                //Throwing exceptions since we cannot proceed without invalid executor names
                                throw new OpenBankingRuntimeException("Executor class is not defined " +
                                        "correctly in open-banking.xml");
                            }
                            int priority = Integer.MAX_VALUE;
                            if (!StringUtils.isEmpty(obExecutorPriority)) {
                                priority = Integer.parseInt(obExecutorPriority);
                            }
                            executors.put(priority, obExecutorClass);
                        }
                    }
                    //Ordering the executors based on the priority number
                    LinkedHashMap<Integer, String> priorityMap = executors.entrySet()
                            .stream()
                            .sorted(comparingByKey())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                    LinkedHashMap::new));
                    obExecutors.put(consentTypeName, priorityMap);
                }
            }
        }
    }

    protected void buildKeyManagerProperties() {

        OMElement keyManagerElement = rootElement.getFirstChildWithName(
                new QName(OpenBankingConstants.OB_CONFIG_QNAME,
                        OpenBankingConstants.KEY_MANAGER_CONFIG_TAG));

        if (keyManagerElement != null) {
            OMElement keyManagerProperties = keyManagerElement.getFirstChildWithName(
                    new QName(OpenBankingConstants.OB_CONFIG_QNAME,
                            OpenBankingConstants.KEY_MANAGER_ADDITIONAL_PROPERTIES_CONFIG_TAG));

            if (keyManagerProperties != null) {
                Iterator<OMElement> properties = keyManagerProperties.getChildrenWithName(
                        new QName(OpenBankingConstants.OB_CONFIG_QNAME, OpenBankingConstants.PROPERTY_CONFIG_TAG));
                if (properties != null) {
                    while (properties.hasNext()) {
                        OMElement propertyElement = properties.next();

                        //Retrieve attributes from key manager config
                        Map<String, String> property = new HashMap<>();
                        property.put("priority", propertyElement.getAttributeValue(new QName("priority")));
                        property.put("label", propertyElement.getAttributeValue(new QName("label")));
                        property.put("type", propertyElement.getAttributeValue(new QName("type")));
                        property.put("tooltip", propertyElement.getAttributeValue(new QName("tooltip")));
                        property.put("default", propertyElement.getAttributeValue(new QName("default")));
                        property.put("required", propertyElement.getAttributeValue(new QName("required")));
                        property.put("mask", propertyElement.getAttributeValue(new QName("mask")));
                        property.put("multiple", propertyElement.getAttributeValue(new QName("multiple")));
                        property.put("values", propertyElement.getAttributeValue(new QName("values")));
                        String propertyName = propertyElement.getAttributeValue(new QName("name"));

                        if (StringUtils.isBlank(propertyName)) {
                            //Throwing exceptions since we cannot proceed without property names
                            throw new OpenBankingRuntimeException("Additional property name is not defined " +
                                    "correctly in open-banking.xml");
                        }

                        keyManagerAdditionalProperties.put(propertyName, property);
                    }
                }
            }
        }
    }

    protected void buildDataPublishingStreams() {

        OMElement dataPublishingElement = rootElement.getFirstChildWithName(
                new QName(OpenBankingConstants.OB_CONFIG_QNAME,
                        OpenBankingConstants.DATA_PUBLISHING_CONFIG_TAG));

        if (dataPublishingElement != null) {
            OMElement thriftElement = dataPublishingElement.getFirstChildWithName(
                    new QName(OpenBankingConstants.OB_CONFIG_QNAME,
                            OpenBankingConstants.THRIFT_CONFIG_TAG));

            if (thriftElement != null) {
                OMElement streams = thriftElement.getFirstChildWithName(
                        new QName(OpenBankingConstants.OB_CONFIG_QNAME,
                                OpenBankingConstants.STREAMS_CONFIG_TAG));

                if (streams != null) {
                    Iterator dataStreamElement = streams.getChildElements();
                    while (dataStreamElement.hasNext()) {
                        OMElement dataStream = (OMElement) dataStreamElement.next();
                        String dataStreamName = dataStream.getLocalName();
                        Map<Integer, String> attributes = new HashMap<>();
                        //obtaining attributes under each stream
                        Iterator<OMElement> attribute = dataStream.getChildrenWithName(
                                new QName(OpenBankingConstants.OB_CONFIG_QNAME,
                                        OpenBankingConstants.ATTRIBUTE_CONFIG_TAG));
                        if (attribute != null) {
                            while (attribute.hasNext()) {
                                OMElement attributeElement = attribute.next();
                                //Retrieve attribute name and priority from config
                                String attributeName = attributeElement.getAttributeValue(new QName("name"));
                                String attributePriority = attributeElement.getAttributeValue(new QName("priority"));
                                String isRequired = attributeElement.getAttributeValue(new QName("required"));
                                String type = attributeElement.getAttributeValue(new QName("type"));

                                if (StringUtils.isEmpty(attributeName)) {
                                    //Throwing exceptions since we cannot proceed without valid attribute names
                                    throw new OpenBankingRuntimeException(
                                            "Data publishing attribute name is not defined " +
                                                    "correctly in open-banking.xml");
                                }
                                int priority = Integer.MAX_VALUE;
                                if (!StringUtils.isEmpty(attributePriority)) {
                                    priority = Integer.parseInt(attributePriority);
                                }
                                boolean required = false;
                                if (!StringUtils.isEmpty(isRequired)) {
                                    required = Boolean.parseBoolean(isRequired);
                                }

                                String attributeType = "string";
                                if (!StringUtils.isEmpty(type)) {
                                    attributeType = type;
                                }

                                Map<String, Object> metadata = new HashMap<>();
                                metadata.put(OpenBankingConstants.REQUIRED, required);
                                metadata.put(OpenBankingConstants.ATTRIBUTE_TYPE, attributeType);

                                attributes.put(priority, attributeName);
                                String attributeKey = dataStreamName + "_" + attributeName;
                                dataPublishingValidationMap.put(attributeKey, metadata);
                            }
                        }
                        //Ordering the attributes based on the priority number
                        LinkedHashMap<Integer, String> priorityMap = attributes.entrySet()
                                .stream()
                                .sorted(comparingByKey())
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                        LinkedHashMap::new));
                        dataPublishingStreams.put(dataStreamName, priorityMap);
                    }
                }
            }
        }
    }

    private void buildDCRParameters() {

        OMElement dcrElement = rootElement.getFirstChildWithName(
                new QName(OpenBankingConstants.OB_CONFIG_QNAME, OpenBankingConstants.DCR_CONFIG_TAG));

        if (dcrElement != null) {
            OMElement registrationElement = dcrElement.getFirstChildWithName(
                    new QName(OpenBankingConstants.OB_CONFIG_QNAME, OpenBankingConstants.DCR_REGISTRATION_CONFIG_TAG));

            if (registrationElement != null) {
                //obtaining each parameter type element under RegistrationRequestPrams tag
                Iterator parameterTypeElement = registrationElement.getChildElements();
                while (parameterTypeElement.hasNext()) {
                    OMElement parameterType = (OMElement) parameterTypeElement.next();
                    String parameterTypeName = parameterType.getLocalName();
                    Map<String, Object> parameterValues = new HashMap<>();
                    //obtaining each element under each parameter type
                    Iterator childValues = parameterType.getChildElements();
                    while (childValues.hasNext()) {
                        OMElement child = (OMElement) childValues.next();
                        if (OpenBankingConstants.DCR_REGISTRATION_PARAM_ALLOWED_VALUE_TAG
                                .equalsIgnoreCase(child.getLocalName())) {

                            OMElement allowedValuesElement = parameterType.getFirstChildWithName(
                                    new QName(OpenBankingConstants.OB_CONFIG_QNAME,
                                            OpenBankingConstants.DCR_REGISTRATION_PARAM_ALLOWED_VALUE_TAG));

                            List<String> values = new ArrayList<>();
                            if (allowedValuesElement != null) {
                                Iterator allowedValues = allowedValuesElement.getChildElements();
                                while (allowedValues.hasNext()) {
                                    OMElement value = (OMElement) allowedValues.next();
                                    values.add(value.getText());
                                }
                                parameterValues.put(child.getLocalName(), values);
                            }
                        } else {
                            parameterValues.put(child.getLocalName(), child.getText());
                        }
                    }
                    dcrRegistrationConfigs.put(parameterTypeName, parameterValues);
                }
            }
        }

    }

    private void buildConsentAuthSteps() {

        OMElement consentElement = rootElement.getFirstChildWithName(
                new QName(OpenBankingConstants.OB_CONFIG_QNAME,
                        OpenBankingConstants.CONSENT_CONFIG_TAG));

        if (consentElement != null) {
            OMElement consentAuthorizeSteps = consentElement.getFirstChildWithName(
                    new QName(OpenBankingConstants.OB_CONFIG_QNAME,
                            OpenBankingConstants.AUTHORIZE_STEPS_CONFIG_TAG));

            if (consentAuthorizeSteps != null) {
                //obtaining each step type element under AuthorizeSteps tag
                Iterator stepTypeElement = consentAuthorizeSteps.getChildElements();
                while (stepTypeElement.hasNext()) {
                    OMElement stepType = (OMElement) stepTypeElement.next();
                    String consentTypeName = stepType.getLocalName();
                    Map<Integer, String> executors = new HashMap<>();
                    //obtaining each step under each consent type
                    Iterator<OMElement> obExecutor = stepType.getChildrenWithName(
                            new QName(OpenBankingConstants.OB_CONFIG_QNAME, OpenBankingConstants.STEP_CONFIG_TAG));
                    if (obExecutor != null) {
                        while (obExecutor.hasNext()) {
                            OMElement executorElement = obExecutor.next();
                            //Retrieve class name and priority from executor config
                            String obExecutorClass = executorElement.getAttributeValue(new QName("class"));
                            String obExecutorPriority = executorElement.getAttributeValue(new QName("priority"));

                            if (StringUtils.isEmpty(obExecutorClass)) {
                                //Throwing exceptions since we cannot proceed without invalid executor names
                                throw new OpenBankingRuntimeException("Executor class is not defined " +
                                        "correctly in open-banking.xml");
                            }
                            int priority = Integer.MAX_VALUE;
                            if (!StringUtils.isEmpty(obExecutorPriority)) {
                                priority = Integer.parseInt(obExecutorPriority);
                            }
                            executors.put(priority, obExecutorClass);
                        }
                    }
                    //Ordering the executors based on the priority number
                    LinkedHashMap<Integer, String> priorityMap = executors.entrySet()
                            .stream()
                            .sorted(comparingByKey())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                    LinkedHashMap::new));
                    authorizeSteps.put(consentTypeName, priorityMap);
                }
            }
        }
    }

    /**
     * Method to read text configs from xml when root element is given.
     *
     * @param serverConfig XML root element object
     * @param nameStack    stack of config names
     */
    private void readChildElements(OMElement serverConfig, Stack<String> nameStack) {

        for (Iterator childElements = serverConfig.getChildElements(); childElements.hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            nameStack.push(element.getLocalName());
            if (elementHasText(element)) {
                String key = getKey(nameStack);
                Object currentObject = configuration.get(key);
                String value = replaceSystemProperty(element.getText());
                if (secretResolver != null && secretResolver.isInitialized() &&
                        secretResolver.isTokenProtected(key)) {
                    value = secretResolver.resolve(key);
                }
                if (currentObject == null) {
                    configuration.put(key, value);
                } else if (currentObject instanceof ArrayList) {
                    ArrayList list = (ArrayList) currentObject;
                    if (!list.contains(value)) {
                        list.add(value);
                        configuration.put(key, list);
                    }
                } else {
                    if (!value.equals(currentObject)) {
                        ArrayList<Object> arrayList = new ArrayList<>(2);
                        arrayList.add(currentObject);
                        arrayList.add(value);
                        configuration.put(key, arrayList);
                    }
                }
            } else if (OpenBankingConstants.REVOCATION_VALIDATORS_CONFIG_TAG.equalsIgnoreCase(element.getLocalName())) {
                Iterator environmentIterator = element
                        .getChildrenWithLocalName(OpenBankingConstants.REVOCATION_VALIDATOR_CONFIG_TAG);

                while (environmentIterator.hasNext()) {
                    OMElement environmentElem = (OMElement) environmentIterator.next();
                    String revocationType = environmentElem.getAttributeValue(new QName("type"));
                    Integer priority;
                    try {
                        priority = Integer.parseInt(environmentElem.getAttributeValue(new QName("priority")));
                    } catch (NumberFormatException e) {
                        log.warn("Consent retrieval RevocationValidator " + revocationType.replaceAll("[\r\n]", "")
                                + " priority invalid. Hence skipped");
                        continue;
                    }
                    revocationValidators.put(priority, revocationType);
                }
            }
            readChildElements(element, nameStack);
            nameStack.pop();
        }
    }

    private void buildAllowedScopes() {
        OMElement gatewayElement = rootElement.getFirstChildWithName(
                new QName(OpenBankingConstants.OB_CONFIG_QNAME, OpenBankingConstants.GATEWAY_CONFIG_TAG));

        if (gatewayElement != null) {
            OMElement tppManagementElement = gatewayElement.getFirstChildWithName(
                    new QName(OpenBankingConstants.OB_CONFIG_QNAME, OpenBankingConstants.TPP_MANAGEMENT_CONFIG_TAG));

            if (tppManagementElement != null) {
                OMElement allowedScopesElement = tppManagementElement.getFirstChildWithName(new QName(
                        OpenBankingConstants.OB_CONFIG_QNAME, OpenBankingConstants.ALLOWED_SCOPES_CONFIG_TAG));

                //obtaining each scope under allowed scopes
                Iterator environmentIterator =
                        allowedScopesElement.getChildrenWithLocalName(OpenBankingConstants.SCOPE_CONFIG_TAG);

                while (environmentIterator.hasNext()) {
                    OMElement scopeElem = (OMElement) environmentIterator.next();
                    String scopeName = scopeElem.getAttributeValue(new QName("name"));
                    String rolesStr = scopeElem.getAttributeValue(new QName("roles"));
                    if (StringUtils.isNotEmpty(rolesStr)) {
                        List<String> rolesList = Arrays.stream(rolesStr.split(","))
                                .map(String::trim)
                                .collect(Collectors.toList());
                        allowedScopes.put(scopeName, rolesList);
                    }
                }
            }
        }
    }

    private void buildAllowedSubscriptions() {

        OMElement dcrElement = rootElement.getFirstChildWithName(
                new QName(OpenBankingConstants.OB_CONFIG_QNAME, OpenBankingConstants.DCR_CONFIG_TAG));

        if (dcrElement != null) {
            OMElement regulatoryAPINames = dcrElement.getFirstChildWithName(
                    new QName(OpenBankingConstants.OB_CONFIG_QNAME, OpenBankingConstants.REGULATORY_APINAMES));

            if (regulatoryAPINames != null) {

                //obtaining each scope under allowed scopes
                Iterator environmentIterator =
                        regulatoryAPINames.getChildrenWithLocalName(OpenBankingConstants.REGULATORY_API);

                while (environmentIterator.hasNext()) {
                    OMElement scopeElem = (OMElement) environmentIterator.next();
                    String scopeName = scopeElem.getAttributeValue(new QName("name"));
                    String rolesStr = scopeElem.getAttributeValue(new QName("roles"));
                    if (StringUtils.isNotEmpty(rolesStr)) {
                        List<String> rolesList = Arrays.stream(rolesStr.split(","))
                                .map(String::trim)
                                .collect(Collectors.toList());
                        allowedAPIs.put(scopeName, rolesList);
                    }
                    else if(StringUtils.isEmpty(rolesStr))  {
                        allowedAPIs.put(scopeName, Collections.emptyList());
                    }
                }
            }
        }

    }

    private void buildOBEventExecutors() {

        OMElement eventElement = rootElement.getFirstChildWithName(
                new QName(OpenBankingConstants.OB_CONFIG_QNAME,
                        OpenBankingConstants.EVENT_CONFIG_TAG));

        if (eventElement != null) {

            OMElement openBankingEventExecutors = eventElement.getFirstChildWithName(
                    new QName(OpenBankingConstants.OB_CONFIG_QNAME,
                            OpenBankingConstants.EVENT_EXECUTOR_CONFIG_TAG));

            if (openBankingEventExecutors != null) {
                //obtaining each executor element under EventExecutors tag
                //Ordering the executors based on the priority number
                Iterator<OMElement> eventExecutor = openBankingEventExecutors.getChildrenWithName(
                        new QName(OpenBankingConstants.OB_CONFIG_QNAME, OpenBankingConstants.EXECUTOR_CONFIG_TAG));
                if (eventExecutor != null) {
                    while (eventExecutor.hasNext()) {
                        OMElement executorElement = eventExecutor.next();
                        //Retrieve class name and priority from executor config
                        String obExecutorClass = executorElement.getAttributeValue(new QName("class"));
                        String obExecutorPriority = executorElement.getAttributeValue(new QName("priority"));

                        if (StringUtils.isEmpty(obExecutorClass)) {
                            //Throwing exceptions since we cannot proceed without invalid executor names
                            throw new OpenBankingRuntimeException("Event Executor class is not defined " +
                                    "correctly in open-banking.xml");
                        }
                        int priority = Integer.MAX_VALUE;
                        if (!StringUtils.isEmpty(obExecutorPriority)) {
                            priority = Integer.parseInt(obExecutorPriority);
                        }
                        obEventExecutors.put(priority, obExecutorClass);
                    }
                }
                //Ordering the executors based on the priority number
                obEventExecutors = obEventExecutors.entrySet()
                        .stream()
                        .sorted(comparingByKey())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
            }
        }
    }

    /**
     * Method to build configurations for Authentication Worker Extension point.
     */
    private void buildWorkers() {

        OMElement workersOMEList = rootElement.getFirstChildWithName(
                new QName(OpenBankingConstants.OB_CONFIG_QNAME, OpenBankingConstants.AUTHENTICATION_WORKER_LIST_TAG));

        if (workersOMEList != null) {
            Iterator<OMElement> workerConfigs = workersOMEList.getChildrenWithName(
                    new QName(OpenBankingConstants.OB_CONFIG_QNAME, OpenBankingConstants.AUTHENTICATION_WORKER_TAG));
            if (workerConfigs != null) {
                while (workerConfigs.hasNext()) {
                    OMElement executorElement = workerConfigs.next();
                    //Retrieve class name and implementation from executor config
                    String workerClass = executorElement.getAttributeValue(new QName("class"));
                    String workerName = executorElement.getAttributeValue(new QName("name"));

                    if (StringUtils.isEmpty(workerClass) || StringUtils.isEmpty(workerName)) {
                        //Throwing exceptions since we cannot proceed without invalid worker names
                        throw new OpenBankingRuntimeException("Authentication worker class is not defined " +
                                "correctly in open-banking.xml");
                    }
                    authWorkerConfig.put(workerName, workerClass);
                }
            }
        }
    }

    /**
     * Method to obtain config key from stack.
     *
     * @param nameStack Stack of strings with names.
     * @return key as a String
     */
    private String getKey(Stack<String> nameStack) {

        StringBuilder key = new StringBuilder();
        for (int index = 0; index < nameStack.size(); index++) {
            String name = nameStack.elementAt(index);
            key.append(name).append(".");
        }
        key.deleteCharAt(key.lastIndexOf("."));
        return key.toString();
    }

    /**
     * Method to replace system properties in configs.
     *
     * @param text String that may require modification
     * @return modified string
     */
    private String replaceSystemProperty(String text) {

        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        StringBuilder textBuilder = new StringBuilder(text);
        while (indexOfStartingChars < textBuilder.indexOf("${")
                && (indexOfStartingChars = textBuilder.indexOf("${")) != -1
                && (indexOfClosingBrace = textBuilder.indexOf("}")) != -1) { // Is a property used?
            String sysProp = textBuilder.substring(indexOfStartingChars + 2, indexOfClosingBrace);
            String propValue = System.getProperty(sysProp);
            if (propValue != null) {
                textBuilder = new StringBuilder(textBuilder.substring(0, indexOfStartingChars) + propValue
                        + textBuilder.substring(indexOfClosingBrace + 1));
            }
            if (sysProp.equals(OpenBankingConstants.CARBON_HOME) &&
                    System.getProperty(OpenBankingConstants.CARBON_HOME).equals(".")) {
                textBuilder.insert(0, new File(".").getAbsolutePath() + File.separator);
            }
        }
        return textBuilder.toString();
    }

    /**
     * Method to check whether config element has text value.
     *
     * @param element root element as a object
     * @return availability of text in the config
     */
    private boolean elementHasText(OMElement element) {

        String text = element.getText();
        return text != null && text.trim().length() != 0;
    }

    public Map<String, Map<Integer, String>> getOpenBankingExecutors() {

        return obExecutors;
    }

    public Map<Integer, String> getOpenBankingEventExecutors() {

        return obEventExecutors;
    }

    public Map<String, Map<Integer, String>> getDataPublishingStreams() {

        return dataPublishingStreams;
    }

    public Map<String, Map<String, Object>> getDataPublishingValidationMap() {

        return dataPublishingValidationMap;
    }

    public Map<String, Map<Integer, String>> getConsentAuthorizeSteps() {

        return authorizeSteps;
    }

    public Map<String, Map<String, String>> getKeyManagerAdditionalProperties() {

        return keyManagerAdditionalProperties;
    }

    /**
     * Returns the element with the provided key.
     *
     * @param key local part name
     * @return Corresponding value for key
     */
    public Object getConfigElementFromKey(String key) {

        return configuration.get(key);
    }

    public String getDataSourceName() {

        return getConfigElementFromKey(OpenBankingConstants.JDBC_PERSISTENCE_CONFIG) == null ? "" :
                ((String) getConfigElementFromKey(OpenBankingConstants.JDBC_PERSISTENCE_CONFIG)).trim();
    }

    /**
     * Returns the database connection verification timeout in seconds configured in open-banking.xml.
     *
     * @return 1 if nothing is configured
     */
    public int getConnectionVerificationTimeout() {

        return getConfigElementFromKey(OpenBankingConstants.DB_CONNECTION_VERIFICATION_TIMEOUT) == null ? 1 :
                Integer.parseInt(getConfigElementFromKey(
                        OpenBankingConstants.DB_CONNECTION_VERIFICATION_TIMEOUT).toString().trim());
    }

    /**
     * Returns the retention datasource name configured in open-banking.xml.
     * @return  retention datasource name or empty string if nothing is configured
     */
    public String getRetentionDataSourceName() {

        return getConfigElementFromKey(OpenBankingConstants.JDBC_RETENTION_DATA_PERSISTENCE_CONFIG) == null ? "" :
                ((String) getConfigElementFromKey(OpenBankingConstants.JDBC_RETENTION_DATA_PERSISTENCE_CONFIG)).trim();
    }

    /**
     * Returns the retention database connection verification timeout in seconds configured in open-banking.xml.
     *
     * @return 1 if nothing is configured
     */
    public int getRetentionDataSourceConnectionVerificationTimeout() {

        return getConfigElementFromKey(OpenBankingConstants.RETENTION_DATA_DB_CONNECTION_VERIFICATION_TIMEOUT)
                == null ? 1 : Integer.parseInt(getConfigElementFromKey(
                OpenBankingConstants.RETENTION_DATA_DB_CONNECTION_VERIFICATION_TIMEOUT).toString().trim());
    }

    /**
     * Method to get isEnabled config for consent data retention feature.
     * @return consent data retention is enabled
     */
    public boolean isConsentDataRetentionEnabled() {

        return getConfigElementFromKey(OpenBankingConstants.IS_CONSENT_DATA_RETENTION_ENABLED) == null ? false :
                (Boolean.parseBoolean(getConfigElementFromKey(
                        OpenBankingConstants.IS_CONSENT_DATA_RETENTION_ENABLED).toString().trim()));
    }


    /**
     * Method to get isEnabled config for consent data retention periodical job.
     * @return consent data retention is enabled
     */
    public boolean isRetentionDataDBSyncEnabled() {

        return getConfigElementFromKey(OpenBankingConstants.IS_CONSENT_RETENTION_DATA_DB_SYNC_ENABLED) == null ? false :
                (Boolean.parseBoolean(getConfigElementFromKey(
                        OpenBankingConstants.IS_CONSENT_RETENTION_DATA_DB_SYNC_ENABLED).toString().trim()));
    }


    /**
     * Method to get configs for data retention db sync periodical job's cron value.
     * @return data retention job's cron string
     */
    public String getRetentionDataDBSyncCronExpression() {

        return getConfigElementFromKey(OpenBankingConstants.CONSENT_RETENTION_DATA_DB_SYNC_CRON) == null
                ? OpenBankingConstants.DEFAULT_MIDNIGHT_CRON :
                ((String) getConfigElementFromKey(OpenBankingConstants.CONSENT_RETENTION_DATA_DB_SYNC_CRON)).trim();
    }

    /**
     * Truststore dynamic loading interval.
     *
     * @return truststore dynamic loading time in seconds
     */
    public Long getTruststoreDynamicLoadingInterval() {
        try {
            Object truststoreDynamicLoadingInterval =
                    getConfigElementFromKey(OpenBankingConstants.TRUSTSTORE_DYNAMIC_LOADING_INTERVAL);
            if (truststoreDynamicLoadingInterval != null) {
                return Long.parseLong((String) truststoreDynamicLoadingInterval);
            } else {
                return Long.parseLong("86400");
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Error occurred while reading the truststore dynamic loading interval " +
                    "value in open-banking.xml. " + e.getMessage());
        }
    }

    /**
     * Returns the revocation validators map.
     * <p>
     * The revocation validator map contains revocation type (OCSP/CRL) and its executing priority.
     * The default priority value has set as 1 for OCSP type, as OCSP validation is faster than the CRL validation
     *
     * @return certificate revocation validators map
     */
    public Map<Integer, String> getCertificateRevocationValidators() {
        return revocationValidators;
    }

    public Map<String, Map<String, Object>> getOpenBankingDCRRegistrationParams() {
        return dcrRegistrationConfigs;
    }

    public String getAuthServletExtension() {
        return getConfigElementFromKey(OpenBankingConstants.AUTH_SERVLET_EXTENSION) == null ? "" :
                ((String) getConfigElementFromKey(OpenBankingConstants.AUTH_SERVLET_EXTENSION)).trim();
    }

    public String getCibaServletExtension() {
        return getConfigElementFromKey(OpenBankingConstants.CIBA_SERVLET_EXTENSION) == null ? "" :
                ((String) getConfigElementFromKey(OpenBankingConstants.CIBA_SERVLET_EXTENSION)).trim();
    }

    public String getJWKSConnectionTimeOut() {

        return getConfigElementFromKey(OpenBankingConstants.DCR_JWKS_CONNECTION_TIMEOUT) == null ? "3000" :
                ((String) getConfigElementFromKey(OpenBankingConstants.DCR_JWKS_CONNECTION_TIMEOUT)).trim();
    }

    public String getJWKSReadTimeOut() {

        return getConfigElementFromKey(OpenBankingConstants.DCR_JWKS_READ_TIMEOUT) == null ? "3000" :
                ((String) getConfigElementFromKey(OpenBankingConstants.DCR_JWKS_READ_TIMEOUT)).trim();
    }

    public String getSPMetadataFilterExtension() {
        return getConfigElementFromKey(OpenBankingConstants.SP_METADATA_FILTER_EXTENSION) == null ? "" :
                ((String) getConfigElementFromKey(OpenBankingConstants.SP_METADATA_FILTER_EXTENSION)).trim();
    }

    public Map<String, List<String>> getAllowedScopes() {
        return allowedScopes;
    }

    public Map<String, List<String>> getAllowedAPIs() {
        return allowedAPIs;
    }

    /**
     * Method to get configs for periodical consent expiration job's cron value.
     * @return consent expiration job's cron string
     */
    public String getConsentExpiryCronExpression() {

        return getConfigElementFromKey(OpenBankingConstants.CONSENT_PERIODICAL_EXPIRATION_CRON) == null
                ? OpenBankingConstants.DEFAULT_MIDNIGHT_CRON :
                ((String) getConfigElementFromKey(OpenBankingConstants.CONSENT_PERIODICAL_EXPIRATION_CRON)).trim();
    }

    /**
     * Method to get statue for expired consents.
     * @return statue for expired consents
     */
    public String getStatusWordingForExpiredConsents() {

        return getConfigElementFromKey(OpenBankingConstants.STATUS_FOR_EXPIRED_CONSENT) == null
                ? OpenBankingConstants.DEFAULT_STATUS_FOR_EXPIRED_CONSENTS :
                ((String) getConfigElementFromKey(OpenBankingConstants.STATUS_FOR_EXPIRED_CONSENT)).trim();
    }

    /**
     * Method to get eligible statues for evaluate expiration logic.
     * @return eligible statues for evaluate expiration logic
     */
    public String getEligibleStatusesForConsentExpiry() {

        return getConfigElementFromKey(OpenBankingConstants.ELIGIBLE_STATUSES_FOR_CONSENT_EXPIRY) == null ? "" :
                ((String) getConfigElementFromKey(OpenBankingConstants.ELIGIBLE_STATUSES_FOR_CONSENT_EXPIRY)).trim();
    }

    /**
     * Method to get isEnabled config for periodical consent expiration job.
     * @return consent expiration job is enabled
     */
    public boolean isConsentExpirationPeriodicalJobEnabled() {

        return getConfigElementFromKey(OpenBankingConstants.IS_CONSENT_PERIODICAL_EXPIRATION_ENABLED) == null ? false :
                (Boolean.parseBoolean(getConfigElementFromKey(
                        OpenBankingConstants.IS_CONSENT_PERIODICAL_EXPIRATION_ENABLED).toString().trim()));
    }

    public boolean isConsentAmendmentHistoryEnabled() {

        return getConfigElementFromKey(OpenBankingConstants.IS_CONSENT_AMENDMENT_HISTORY_ENABLED) == null ? false :
                (Boolean.parseBoolean(getConfigElementFromKey(
                        OpenBankingConstants.IS_CONSENT_AMENDMENT_HISTORY_ENABLED).toString().trim()));
    }

    public String getOBKeyManagerExtensionImpl() {
        return getConfigElementFromKey(OpenBankingConstants.OB_KEYMANAGER_EXTENSION_IMPL) == null ? "" :
                ((String) getConfigElementFromKey(OpenBankingConstants.OB_KEYMANAGER_EXTENSION_IMPL))
                        .trim();
    }

    /**
     * ConnectionPool maximum connection count.
     *
     * @return maximum connections count, default value is 2000
     */
    public int getConnectionPoolMaxConnections() {
        try {
            Object maxConnectionsCount =
                    getConfigElementFromKey(OpenBankingConstants.CONNECTION_POOL_MAX_CONNECTIONS);
            if (maxConnectionsCount != null) {
                return Integer.parseInt(String.valueOf(maxConnectionsCount));
            } else {
                return 2000;
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Error occurred while reading the MaxConnections " +
                    "value in open-banking.xml. " + e.getMessage());
        }
    }

    /**
     * ConnectionPool maximum connection per route count.
     *
     * @return maximum connections per route value, default value is 1500
     */
    public int getConnectionPoolMaxConnectionsPerRoute() {
        try {
            Object maxConnectionsPerRouteCount =
                    getConfigElementFromKey(OpenBankingConstants.CONNECTION_POOL_MAX_CONNECTIONS_PER_ROUTE);
            if (maxConnectionsPerRouteCount != null) {
                return Integer.parseInt(String.valueOf(maxConnectionsPerRouteCount));
            } else {
                return 1500;
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Error occurred while reading the MaxConnectionsPerRoute " +
                    "value in open-banking.xml. " + e.getMessage());
        }
    }

    private void buildServiceActivatorSubscribers() {
        OMElement serviceActivatorElement = rootElement.getFirstChildWithName(
                new QName(OpenBankingConstants.OB_CONFIG_QNAME, OpenBankingConstants.SERVICE_ACTIVATOR_TAG));

        if (serviceActivatorElement != null) {
            OMElement subscribers = serviceActivatorElement.getFirstChildWithName(
                    new QName(OpenBankingConstants.OB_CONFIG_QNAME, OpenBankingConstants.SA_SUBSCRIBERS_TAG));

            if (subscribers != null) {
                Iterator<OMElement> subscriber = subscribers.getChildrenWithName(
                        new QName(OpenBankingConstants.OB_CONFIG_QNAME, OpenBankingConstants.SA_SUBSCRIBER_TAG));
                if (subscriber != null) {
                    while (subscriber.hasNext()) {
                        OMElement executorElement = subscriber.next();
                        //Retrieve subscriber class name from service activator configs
                        final String subscriberClass = executorElement.getText();

                        if (!StringUtils.isEmpty(subscriberClass)) {
                            serviceActivatorSubscribers.add(subscriberClass);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns a list of FQNs of the OBServiceObserver interface implementations.
     *
     * @return ServiceActivator subscribers FQNs.
     */
    public List<String> getServiceActivatorSubscribers() {
        return serviceActivatorSubscribers;
    }

    //Event notifications configurations.
    public String getEventNotificationTokenIssuer() {

        return getConfigElementFromKey(OpenBankingConstants.TOKEN_ISSUER) == null ? "www.wso2.com" :
                ((String) getConfigElementFromKey(OpenBankingConstants.TOKEN_ISSUER)).trim();
    }

    public int getNumberOfSetsToReturn() {

        return getConfigElementFromKey(OpenBankingConstants.MAX_SETS_TO_RETURN) == null ? 5 :
                Integer.parseInt((String) getConfigElementFromKey(OpenBankingConstants.MAX_SETS_TO_RETURN));
    }

    public boolean isSubClaimIncluded() {

        return getConfigElementFromKey(OpenBankingConstants.IS_SUB_CLAIM_INCLUDED) == null ? false :
                (Boolean.parseBoolean(getConfigElementFromKey(
                        OpenBankingConstants.IS_SUB_CLAIM_INCLUDED).toString().trim()));
    }

    public boolean isToeClaimIncluded() {
        return getConfigElementFromKey(OpenBankingConstants.IS_TOE_CLAIM_INCLUDED) == null ? false :
                (Boolean.parseBoolean(getConfigElementFromKey(
                        OpenBankingConstants.IS_TOE_CLAIM_INCLUDED).toString().trim()));
    }

    public boolean isTxnClaimIncluded() {
        return getConfigElementFromKey(OpenBankingConstants.IS_TXN_CLAIM_INCLUDED) == null ? false :
                (Boolean.parseBoolean(getConfigElementFromKey(
                        OpenBankingConstants.IS_TXN_CLAIM_INCLUDED).toString().trim()));
    }

    /**
     * Returns the expiry time for cache modification.
     *
     * @return String Expiry time.
     */
    public String getCommonCacheModifiedExpiryTime() {

        return getConfigElementFromKey(OpenBankingConstants.COMMON_IDENTITY_CACHE_MODIFY_EXPIRY) == null ? "60" :
                ((String) getConfigElementFromKey(OpenBankingConstants.COMMON_IDENTITY_CACHE_MODIFY_EXPIRY)).trim();
    }

    /**
     * Returns the expiry time for cache access.
     *
     * @return String Expiry time.
     */
    public String getCommonCacheAccessExpiryTime() {
        return getConfigElementFromKey(OpenBankingConstants.COMMON_IDENTITY_CACHE_ACCESS_EXPIRY) == null ? "60" :
                ((String) getConfigElementFromKey(OpenBankingConstants.COMMON_IDENTITY_CACHE_ACCESS_EXPIRY)).trim();
    }

    /**
     * Alias of the signing certificate in Production Environment.
     *
     * @return signing certificate alias
     */
    public String getOBIdnRetrieverSigningCertificateAlias() {

        return getConfigElementFromKey(OpenBankingConstants.OB_IDN_RETRIEVER_SIG_ALIAS) == null ? "wso2carbon" :
                ((String) getConfigElementFromKey(OpenBankingConstants.OB_IDN_RETRIEVER_SIG_ALIAS)).trim();
    }

    /**
     * Alias of the signing certificate in Sandbox Environment.
     *
     * @return signing certificate alias
     */
    public String getOBIdnRetrieverSandboxSigningCertificateAlias() {

        return getConfigElementFromKey(OpenBankingConstants.OB_IDN_RETRIEVER_SANDBOX_SIG_ALIAS) == null ? "wso2carbon" :
                ((String) getConfigElementFromKey(OpenBankingConstants.OB_IDN_RETRIEVER_SANDBOX_SIG_ALIAS)).trim();
    }

    /**
     * Key ID of the public key of the corresponding private key used for signing.
     *
     * @return signing certificate Kid in Production environment
     */
    public String getOBIdnRetrieverSigningCertificateKid() {

        return getConfigElementFromKey(OpenBankingConstants.OB_IDN_RETRIEVER_SIG_KID) == null ? "1234" :
                ((String) getConfigElementFromKey(OpenBankingConstants.OB_IDN_RETRIEVER_SIG_KID)).trim();
    }

    /**
     * Key ID of the public key of the corresponding private key used for signing.
     *
     * @return signing certificate Kid in sandbox environment
     */
    public String getOBIdnRetrieverSandboxCertificateKid() {

        return getConfigElementFromKey(OpenBankingConstants.OB_IDN_RETRIEVER_SANDBOX_KID) == null ? "5678" :
                ((String) getConfigElementFromKey(OpenBankingConstants.OB_IDN_RETRIEVER_SANDBOX_KID)).trim();
    }

    /**
     * JWKS Retriever Size Limit for JWS Signature Handling.
     *
     * @return  JWKS Retriever Size Limit
     */
    public String getJwksRetrieverSizeLimit() {

        return getConfigElementFromKey(OpenBankingConstants.JWKS_RETRIEVER_SIZE_LIMIT) == null ? "51200" :
                ((String) getConfigElementFromKey(OpenBankingConstants.JWKS_RETRIEVER_SIZE_LIMIT)).trim();
    }

    /**
     * JWKS Retriever Connection Timeout for JWS Signature Handling.
     *
     * @return  JWKS Retriever Connection Timeout
     */
    public String getJwksRetrieverConnectionTimeout() {

        return getConfigElementFromKey(OpenBankingConstants.JWKS_RETRIEVER_CONN_TIMEOUT) == null ? "2000" :
                ((String) getConfigElementFromKey(OpenBankingConstants.JWKS_RETRIEVER_CONN_TIMEOUT)).trim();
    }

    /**
     * JWKS Retriever Read Timeout for JWS Signature Handling.
     *
     * @return  JWKS Retriever Read Timeout
     */
    public String getJwksRetrieverReadTimeout() {

        return getConfigElementFromKey(OpenBankingConstants.JWKS_RETRIEVER_READ_TIMEOUT) == null ? "2000" :
                ((String) getConfigElementFromKey(OpenBankingConstants.JWKS_RETRIEVER_READ_TIMEOUT)).trim();
    }

    /**
     * Check if Jws Signature Validation is enabled.
     *
     * @return if Jws Signature Validation is enabled
     */
    public boolean isJwsSignatureValidationEnabled() {

        return getConfigElementFromKey(OpenBankingConstants.JWS_SIG_VALIDATION_ENABLE) != null &&
                Boolean.parseBoolean(((String) getConfigElementFromKey(OpenBankingConstants.JWS_SIG_VALIDATION_ENABLE))
                        .trim());
    }

    /**
     * Check if Jws Response signing is enabled.
     *
     * @return if Jws message Response is enabled
     */
    public boolean isJwsResponseSigningEnabled() {

        return getConfigElementFromKey(OpenBankingConstants.JWS_RESP_SIGNING_ENABLE) != null &&
                Boolean.parseBoolean(((String) getConfigElementFromKey(OpenBankingConstants.JWS_RESP_SIGNING_ENABLE))
                        .trim());
    }

    /**
     * Jws Request Signing allowed algorithms.
     *
     * @return  Jws Request Signing allowed algorithms
     */
    public List<String> getJwsRequestSigningAlgorithms() {

        Object allowedAlgorithmsElement = getConfigElementFromKey(
                OpenBankingConstants.JWS_SIG_VALIDATION_ALGO) == null ? new String[] {"PS256"} :
                (getConfigElementFromKey(OpenBankingConstants.JWS_SIG_VALIDATION_ALGO));
        List<String> allowedAlgorithmsList = new ArrayList<>();
        if (allowedAlgorithmsElement instanceof ArrayList) {
            allowedAlgorithmsList.addAll((ArrayList) allowedAlgorithmsElement);
        } else if (allowedAlgorithmsElement instanceof String) {
            allowedAlgorithmsList.add((String) allowedAlgorithmsElement);
        }
        return allowedAlgorithmsList.isEmpty() ? Arrays.asList("PS256") : allowedAlgorithmsList;
    }

    /**
     * Jws Response Signing allowed algorithm.
     *
     * @return   Jws Response Signing allowed algorithm
     */
    public String getJwsResponseSigningAlgorithm() {

        return getConfigElementFromKey(OpenBankingConstants.JWS_RESP_SIGNING_ALGO) == null ? "PS256" :
                ((String) getConfigElementFromKey(OpenBankingConstants.JWS_RESP_SIGNING_ALGO)).trim();
    }

    public Map<String, String> getAuthWorkerConfig() {
        return authWorkerConfig;
    }

    /**
     * Method to check if the Dispute Resolution feature is enabled.
     * @return true if Dispute Resolution is enabled.
     */
    public boolean isDisputeResolutionEnabled() {

        return getConfigElementFromKey(OpenBankingConstants.IS_DISPUTE_RESOLUTION_ENABLED) == null ? false :
                (Boolean.parseBoolean(getConfigElementFromKey(
                        OpenBankingConstants.IS_DISPUTE_RESOLUTION_ENABLED).toString().trim()));
    }

    /**
     * Method to check if the Dispute Resolution feature is enabled for Non Error Scenarios.
     * @return true if Dispute Resolution feature is enabled for Non Error scenarios
     */
    public boolean isNonErrorDisputeDataPublishingEnabled() {

        return getConfigElementFromKey(OpenBankingConstants.PUBLISH_NON_ERROR_DISPUTE_DATA) == null ? false :
                (Boolean.parseBoolean(getConfigElementFromKey(
                        OpenBankingConstants.PUBLISH_NON_ERROR_DISPUTE_DATA).toString().trim()));
    }

    /**
     * Method to get maximum length for publish response body in Dispute Resolution Feature.
     * @return maximum length for response body.
     */
    public int getMaxResponseBodyLength() {

        return getConfigElementFromKey(OpenBankingConstants.MAX_RESPONSE_BODY_LENGTH)
                == null ? 4096 : (Integer.parseInt(getConfigElementFromKey(
                OpenBankingConstants.MAX_RESPONSE_BODY_LENGTH).toString().trim()));
    }

    /**
     * Method to get maximum length for publish request body in Dispute Resolution Feature.
     * @return maximum length for request body.
     */
    public int getMaxRequestBodyLength() {

        return getConfigElementFromKey(OpenBankingConstants.MAX_REQUEST_BODY_LENGTH)
                == null ? 4096 : (Integer.parseInt(getConfigElementFromKey(
                OpenBankingConstants.MAX_REQUEST_BODY_LENGTH).toString().trim()));
    }

    /**
     *Method to get maximum length for publish headers in Dispute Resolution Feature.
     * @return maximum length for headers.
     */
    public int getMaxHeaderLength() {

        return getConfigElementFromKey(OpenBankingConstants.MAX_HEADER_LENGTH)
                == null ? 2048 : (Integer.parseInt(getConfigElementFromKey(
                OpenBankingConstants.MAX_HEADER_LENGTH).toString().trim()));
    }

    /**
     * Method to determine real-time event notification feature is enabled or not from the configurations.
     *
     * @return boolean value indicating the state
     */
    public boolean isRealtimeEventNotificationEnabled() {
        return getConfigElementFromKey(OpenBankingConstants.REALTIME_EVENT_NOTIFICATION_ENABLED) != null
                && (Boolean.parseBoolean(getConfigElementFromKey(
                OpenBankingConstants.REALTIME_EVENT_NOTIFICATION_ENABLED).toString().trim()));
    }

    /**
     * Method to get periodic Cron expression config for realtime event notifications scheduler.
     *
     * @return String Cron expression to trigger the Cron job for real-time event notification
     */
    public String getRealtimeEventNotificationSchedulerCronExpression() {
        return getConfigElementFromKey(OpenBankingConstants.PERIODIC_CRON_EXPRESSION)
                == null ? "0 0/1 0 ? * * *" : (String) getConfigElementFromKey(
                OpenBankingConstants.PERIODIC_CRON_EXPRESSION);
    }

    /**
     * Method to get TIMEOUT_IN_SECONDS config for realtime event notifications.
     *
     * @return integer timeout for the HTTP Client's POST requests
     */
    public int getRealtimeEventNotificationTimeoutInSeconds() {
        return getConfigElementFromKey(OpenBankingConstants.TIMEOUT_IN_SECONDS)
                == null ? 60 : (Integer.parseInt(getConfigElementFromKey(
                OpenBankingConstants.TIMEOUT_IN_SECONDS).toString().trim()));
    }

    /**
     * Method to get MAX_RETRIES config for realtime event notifications.
     *
     * @return integer maximum number of retries to the retry policy in real-time notification sender
     */
    public int getRealtimeEventNotificationMaxRetries() {
        return getConfigElementFromKey(OpenBankingConstants.MAX_RETRIES)
                == null ? 5 : (Integer.parseInt(getConfigElementFromKey(
                OpenBankingConstants.MAX_RETRIES).toString().trim()));
    }

    /**
     * Method to get INITIAL_BACKOFF_TIME_IN_SECONDS config for realtime event notifications.
     *
     * @return integer start waiting time for the retry policy before the first retry
     */
    public int getRealtimeEventNotificationInitialBackoffTimeInSeconds() {
        return getConfigElementFromKey(OpenBankingConstants.INITIAL_BACKOFF_TIME_IN_SECONDS)
                == null ? 60 : (Integer.parseInt(getConfigElementFromKey(
                OpenBankingConstants.INITIAL_BACKOFF_TIME_IN_SECONDS).toString().trim()));
    }

    /**
     * Method to get BACKOFF_FUNCTION config for realtime event notifications.
     * Function name should be "EX", "CONSTANT" or "LINEAR".
     *
     * @return string indicating the retry function
     */
    public String getRealtimeEventNotificationBackoffFunction() {
        return getConfigElementFromKey(OpenBankingConstants.BACKOFF_FUNCTION)
                == null ? "EX" : (String) getConfigElementFromKey(
                OpenBankingConstants.BACKOFF_FUNCTION);
    }

    /**
     * Method to get CIRCUIT_BREAKER_OPEN_TIMEOUT_IN_SECONDS config for realtime event notifications.
     *
     * @return integer timeout to break the retrying process and make that notification as ERR
     */
    public int getRealtimeEventNotificationCircuitBreakerOpenTimeoutInSeconds() {
        return getConfigElementFromKey(OpenBankingConstants.CIRCUIT_BREAKER_OPEN_TIMEOUT_IN_SECONDS)
                == null ? 600 : (Integer.parseInt(getConfigElementFromKey(
                OpenBankingConstants.CIRCUIT_BREAKER_OPEN_TIMEOUT_IN_SECONDS).toString().trim()));
    }

    /**
     * Method to get EVENT_NOTIFICATION_THREADPOOL_SIZE config for realtime event notifications.
     *
     * @return integer fix size to set the Thread Pool size in the real-time event notification sender
     */
    public int getEventNotificationThreadpoolSize() {
        return getConfigElementFromKey(OpenBankingConstants.EVENT_NOTIFICATION_THREADPOOL_SIZE)
                == null ? 20 : (Integer.parseInt(getConfigElementFromKey(
                OpenBankingConstants.EVENT_NOTIFICATION_THREADPOOL_SIZE).toString().trim()));
    }

    /**
     * Method to get EVENT_NOTIFICATION_GENERATOR config for event notifications.
     *
     * @return String class name of the event notification generator to generate the event notification payload
     */
    public String getEventNotificationGenerator() {
        return getConfigElementFromKey(OpenBankingConstants.EVENT_NOTIFICATION_GENERATOR) == null ?
                "com.wso2.openbanking.accelerator.event.notifications.service.service.DefaultEventNotificationGenerator"
                : (String) getConfigElementFromKey(OpenBankingConstants.EVENT_NOTIFICATION_GENERATOR);
    }

    /**
     * Method to get REALTIME_EVENT_NOTIFICATION_REQUEST_GENERATOR config for realtime event notifications.
     *
     * @return String class path of the realtime event notification payload generator
     */
    public String getRealtimeEventNotificationRequestGenerator() {
        return getConfigElementFromKey(OpenBankingConstants.REALTIME_EVENT_NOTIFICATION_REQUEST_GENERATOR) == null ?
                "com.wso2.openbanking.accelerator.event.notifications.service." +
                        "realtime.service.DefaultRealtimeEventNotificationRequestGenerator"
                : (String) getConfigElementFromKey(OpenBankingConstants.REALTIME_EVENT_NOTIFICATION_REQUEST_GENERATOR);
    }

    /**
     * Method to get software environment identification SSA property name.
     *
     * @return String software environment identification SSA property name.
     */
    public String getSoftwareEnvIdentificationSSAPropertyName() {
        return getConfigElementFromKey(OpenBankingConstants.DCR_SOFTWARE_ENV_IDENTIFICATION_PROPERTY_NAME) == null ?
                OpenBankingConstants.SOFTWARE_ENVIRONMENT : (String) getConfigElementFromKey(
                OpenBankingConstants.DCR_SOFTWARE_ENV_IDENTIFICATION_PROPERTY_NAME);
    }

    /**
     * Method to get software environment identification value for sandbox in SSA.
     *
     * @return String software environment identification value for sandbox.
     */
    public String getSoftwareEnvIdentificationSSAPropertyValueForSandbox() {
        return getConfigElementFromKey(OpenBankingConstants.DCR_SOFTWARE_ENV_IDENTIFICATION_VALUE_FOR_SANDBOX) == null ?
                "sandbox" : (String) getConfigElementFromKey(
                        OpenBankingConstants.DCR_SOFTWARE_ENV_IDENTIFICATION_VALUE_FOR_SANDBOX);
    }

    /**
     * Method to get software environment identification value for production in SSA.
     *
     * @return String software environment identification value for production.
     */
    public String getSoftwareEnvIdentificationSSAPropertyValueForProduction() {
        return getConfigElementFromKey(
                OpenBankingConstants.DCR_SOFTWARE_ENV_IDENTIFICATION_VALUE_FOR_PRODUCTION) == null ?
                "production" : (String) getConfigElementFromKey(
                OpenBankingConstants.DCR_SOFTWARE_ENV_IDENTIFICATION_VALUE_FOR_PRODUCTION);
    }

    /**
     * Get config related for checking whether PSU is a federated user or not.
     *
     * @return Boolean value indicating whether PSU is a federated user or not
     */
    public boolean isPSUFederated() {

        Object isPSUFederated = getConfigElementFromKey(OpenBankingConstants.IS_PSU_FEDERATED);
        if (isPSUFederated != null) {
            return Boolean.parseBoolean((String) isPSUFederated);
        } else {
            return false;
        }
    }

    /**
     * Get Federated PSU IDP Name.
     *
     * @return String Federated IDP name
     */
    public String getFederatedIDPName() {

        return getConfigElementFromKey(OpenBankingConstants.PSU_FEDERATED_IDP_NAME) == null ? "" :
                ((String) getConfigElementFromKey(OpenBankingConstants.PSU_FEDERATED_IDP_NAME)).trim();
    }

    /**
     * Method to get the value Idempotency enable configuration.
     * @return  Whether Idempotency is enabled or not
     */
    public boolean isIdempotencyValidationEnabled() {
        return getConfigElementFromKey(OpenBankingConstants.IDEMPOTENCY_IS_ENABLED) != null &&
                Boolean.parseBoolean(((String)
                        getConfigElementFromKey(OpenBankingConstants.IDEMPOTENCY_IS_ENABLED)).trim());
    }

    /**
     * Method to get the value Idempotency allowed time configuration.
     * @return  Idempotency allowed time
     */
    public String getIdempotencyAllowedTime() {
        return getConfigElementFromKey(OpenBankingConstants.IDEMPOTENCY_ALLOWED_TIME) == null ? "1440" :
                (String) getConfigElementFromKey(OpenBankingConstants.IDEMPOTENCY_ALLOWED_TIME);
    }

}
