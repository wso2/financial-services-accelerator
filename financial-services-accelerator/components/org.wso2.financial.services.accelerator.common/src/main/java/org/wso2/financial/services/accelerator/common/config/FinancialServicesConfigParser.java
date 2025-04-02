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

package org.wso2.financial.services.accelerator.common.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesRuntimeException;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.util.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static java.util.Map.Entry.comparingByKey;

/**
 * Config parser for financial-services.xml.
 */
public final class FinancialServicesConfigParser {

    // To enable attempted thread-safety using double-check locking
    private static final Object lock = new Object();
    private static final Log log = LogFactory.getLog(FinancialServicesConfigParser.class);
    private final Map<String, Object> configuration = new HashMap<>();
    private final Map<String, Map<Integer, String>> fsExecutors = new HashMap<>();
    private final Map<String, Map<Integer, String>> authorizeSteps = new HashMap<>();
    private final Map<String, Map<String, Object>> dcrParams = new HashMap<>();
    private final Map<String, Map<String, Object>> dcrValidators = new HashMap<>();
    private SecretResolver secretResolver;
    private OMElement rootElement;
    private static FinancialServicesConfigParser parser;

    /**
     * Private Constructor of config parser.
     */
    private FinancialServicesConfigParser() {

        buildConfiguration();
    }

    /**
     * Singleton getInstance method to create only one object.
     *
     * @return FinancialServicesConfigParser object
     */
    public static FinancialServicesConfigParser getInstance() {

        synchronized (lock) {
            if (parser == null) {
                parser = new FinancialServicesConfigParser();
            }
        }
        return parser;
    }

    /**
     * Method to obtain map of configs.
     *
     * @return Config map
     */
    public Map<String, Object> getConfiguration() {

        return Collections.unmodifiableMap(configuration);
    }

    /**
     * Method to read the configuration (in a recursive manner) as a model and put
     * them in the configuration map.
     */
    private void buildConfiguration() {

        InputStream inStream = null;
        StAXOMBuilder builder;
        String warningMessage = "";
        File configXml = new File(CarbonUtils.getCarbonConfigDirPath(), FinancialServicesConstants.FS_CONFIG_FILE);

        try {
            if (configXml.exists()) {
                inStream = Files.newInputStream(configXml.toPath());
            }
            if (inStream == null) {
                String message = "Financial-services configuration not found at: " + configXml + " . " +
                        "Cause - " + warningMessage;
                if (log.isDebugEnabled()) {
                    log.debug(message.replaceAll("[\r\n]", ""));
                }
                throw new FileNotFoundException(message);
            }
            builder = new StAXOMBuilder(inStream);
            rootElement = builder.getDocumentElement();
            Stack<String> nameStack = new Stack<>();
            secretResolver = SecretResolverFactory.create(rootElement, true);
            readChildElements(rootElement, nameStack);
            buildFSExecutors();
            buildConsentAuthSteps();
            buildDCRConfigs();
        } catch (IOException | XMLStreamException | OMException e) {
            throw new FinancialServicesRuntimeException("Error occurred while building configuration from " +
                    "financial-services.xml", e);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                log.error("Error closing the input stream for financial-services.xml", e);
            }
        }
    }

    private void buildFSExecutors() {

        OMElement gatewayElement = rootElement.getFirstChildWithName(
                new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                        FinancialServicesConstants.GATEWAY_CONFIG_TAG));

        if (gatewayElement != null) {

            OMElement financialServicesGatewayExecutors = gatewayElement.getFirstChildWithName(
                    new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                            FinancialServicesConstants.GATEWAY_EXECUTOR_CONFIG_TAG));

            if (financialServicesGatewayExecutors != null) {
                // obtaining each consent type element under FinancialServicesGatewayExecutors
                // tag
                Iterator consentTypeElement = financialServicesGatewayExecutors.getChildElements();
                while (consentTypeElement.hasNext()) {
                    OMElement consentType = (OMElement) consentTypeElement.next();
                    String consentTypeName = consentType.getLocalName();
                    Map<Integer, String> executors = new HashMap<>();
                    // obtaining each Executor element under each consent type
                    Iterator<OMElement> fsExecutor = consentType.getChildrenWithName(
                            new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                                    FinancialServicesConstants.EXECUTOR_CONFIG_TAG));
                    if (fsExecutor != null) {
                        while (fsExecutor.hasNext()) {
                            OMElement executorElement = fsExecutor.next();
                            // Retrieve class name and priority from executor config
                            String fsExecutorClass = executorElement.getAttributeValue(new QName("class"));
                            String fsExecutorPriority = executorElement.getAttributeValue(new QName("priority"));

                            if (StringUtils.isEmpty(fsExecutorClass)) {
                                // Throwing exceptions since we cannot proceed without invalid executor names
                                throw new FinancialServicesRuntimeException("Executor class is not defined " +
                                        "correctly in financial-services.xml");
                            }
                            int priority = Integer.MAX_VALUE;
                            if (!StringUtils.isEmpty(fsExecutorPriority)) {
                                priority = Integer.parseInt(fsExecutorPriority);
                            }
                            executors.put(priority, fsExecutorClass);
                        }
                    }
                    // Ordering the executors based on the priority number
                    LinkedHashMap<Integer, String> priorityMap = executors.entrySet()
                            .stream()
                            .sorted(comparingByKey())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                    LinkedHashMap::new));
                    fsExecutors.put(consentTypeName, priorityMap);
                }
            }
        }
    }

    private void buildConsentAuthSteps() {

        OMElement consentElement = rootElement.getFirstChildWithName(
                new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                        FinancialServicesConstants.CONSENT_CONFIG_TAG));

        if (consentElement != null) {
            OMElement consentAuthorizeSteps = consentElement.getFirstChildWithName(
                    new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                            FinancialServicesConstants.AUTHORIZE_STEPS_CONFIG_TAG));

            if (consentAuthorizeSteps != null) {
                // obtaining each step type element under AuthorizeSteps tag
                Iterator stepTypeElement = consentAuthorizeSteps.getChildElements();
                while (stepTypeElement.hasNext()) {
                    OMElement stepType = (OMElement) stepTypeElement.next();
                    String consentTypeName = stepType.getLocalName();
                    Map<Integer, String> executors = new HashMap<>();
                    // obtaining each step under each consent type
                    Iterator<OMElement> fsExecutor = stepType.getChildrenWithName(
                            new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                                    FinancialServicesConstants.STEP_CONFIG_TAG));
                    if (fsExecutor != null) {
                        while (fsExecutor.hasNext()) {
                            OMElement executorElement = fsExecutor.next();
                            // Retrieve class name and priority from executor config
                            String fsExecutorClass = executorElement.getAttributeValue(new QName("class"));
                            String fsExecutorPriority = executorElement.getAttributeValue(new QName("priority"));

                            if (StringUtils.isEmpty(fsExecutorClass)) {
                                // Throwing exceptions since we cannot proceed without invalid executor names
                                throw new FinancialServicesRuntimeException("Executor class is not defined " +
                                        "correctly in financial-services.xml");
                            }
                            int priority = Integer.MAX_VALUE;
                            if (!StringUtils.isEmpty(fsExecutorPriority)) {
                                priority = Integer.parseInt(fsExecutorPriority);
                            }
                            executors.put(priority, fsExecutorClass);
                        }
                    }
                    // Ordering the executors based on the priority number
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
     * Method to build DCR configurations.
     */
    private void buildDCRConfigs() {

        OMElement appRegElement = rootElement.getFirstChildWithName(
                new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                        FinancialServicesConstants.APP_REGISTRATION_TAG));

        if (appRegElement != null) {
            OMElement dcrConfigs = appRegElement.getFirstChildWithName(
                    new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                            FinancialServicesConstants.DCR_TAG));

            if (dcrConfigs != null) {
                buildDCRParamConfigs(dcrConfigs);
                buildDCRValidatorConfigs(dcrConfigs);
            }
        }
    }

    /**
     * Method to build DCR parameter configurations.
     */
    private void buildDCRParamConfigs(OMElement dcrConfigs) {

        OMElement dcrParamConfigs = dcrConfigs.getFirstChildWithName(
                new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                        FinancialServicesConstants.DCR_PARAMS_TAG));
        if (dcrParamConfigs != null) {
            Iterator dcrParamsElements = dcrParamConfigs.getChildElements();
            while (dcrParamsElements.hasNext()) {
                OMElement dcrParamsElement = (OMElement) dcrParamsElements.next();
                Iterator dcrParamIterator = dcrParamsElement.getChildElements();
                Map<String, Object> paramValues = new HashMap<>();
                String paramName = null;
                while (dcrParamIterator.hasNext()) {
                    OMElement paramObj = (OMElement) dcrParamIterator.next();
                    if (FinancialServicesConstants.DCR_PARAM_NAME_TAG.equals(paramObj.getLocalName())) {
                        paramName = paramObj.getText();
                    }
                    if (FinancialServicesConstants.DCR_PARAM_ALLOWED_VALUE_TAG
                            .equals(paramObj.getLocalName())) {
                        OMElement allowedValuesElement = dcrParamsElement.getFirstChildWithName(
                                new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                                        FinancialServicesConstants.DCR_PARAM_ALLOWED_VALUE_TAG));

                        List<String> values = new ArrayList<>();
                        if (allowedValuesElement != null) {
                            Iterator allowedValues = allowedValuesElement.getChildElements();
                            while (allowedValues.hasNext()) {
                                OMElement value = (OMElement) allowedValues.next();
                                values.add(value.getText());
                            }
                            paramValues.put(paramObj.getLocalName(), values);
                        }
                    } else {
                        paramValues.put(paramObj.getLocalName(), paramObj.getText());
                    }
                }
                dcrParams.put(paramName,  paramValues);
            }
        }
    }

    /**
     * Method to build DCR validator configurations.
     */
    private void buildDCRValidatorConfigs(OMElement dcrConfigs) {

        OMElement dcrValidatorConfigs = dcrConfigs.getFirstChildWithName(
                new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                        FinancialServicesConstants.DCR_VALIDATORS_TAG));
        if (dcrValidatorConfigs != null) {
            Iterator dcrValidatorElements = dcrValidatorConfigs.getChildElements();
            while (dcrValidatorElements.hasNext()) {
                OMElement dcrValidatorElement = (OMElement) dcrValidatorElements.next();
                Iterator dcrValidatorIterator = dcrValidatorElement.getChildElements();
                Map<String, Object> paramValues = new HashMap<>();
                String paramName = null;
                while (dcrValidatorIterator.hasNext()) {
                    OMElement paramObj = (OMElement) dcrValidatorIterator.next();
                    if (FinancialServicesConstants.DCR_PARAM_NAME_TAG.equals(paramObj.getLocalName())) {
                        paramName = paramObj.getText();
                    }
                    if (FinancialServicesConstants.DCR_PARAM_ALLOWED_VALUE_TAG
                            .equals(paramObj.getLocalName())) {
                        OMElement allowedValuesElement = dcrValidatorElement.getFirstChildWithName(
                                new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                                        FinancialServicesConstants.DCR_PARAM_ALLOWED_VALUE_TAG));

                        List<String> values = new ArrayList<>();
                        if (allowedValuesElement != null) {
                            Iterator allowedValues = allowedValuesElement.getChildElements();
                            while (allowedValues.hasNext()) {
                                OMElement value = (OMElement) allowedValues.next();
                                values.add(value.getText());
                            }
                            paramValues.put(paramObj.getLocalName(), values);
                        }
                    } else {
                        paramValues.put(paramObj.getLocalName(), paramObj.getText());
                    }
                }
                dcrValidators.put(paramName,  paramValues);
            }
        }
    }

    /**
     * Returns the list of DCR response parameters.
     *
     * @return List of DCR response parameters.
     */
    public List<String> getDCRResponseParameters() {

        Object dcrResponseParamObj = configuration.get(FinancialServicesConstants.DCR_RESPONSE_PARAMETERS);
        List<String> dcrResponseParamList = new ArrayList<>();
        if (dcrResponseParamObj instanceof ArrayList) {
            dcrResponseParamList.addAll((ArrayList) dcrResponseParamObj);
        } else if (dcrResponseParamObj instanceof String) {
            dcrResponseParamList.add((String) dcrResponseParamObj);
        }

        return dcrResponseParamList;
    }

    /**
     * Method to read text configs from xml when root element is given.
     *
     * @param serverConfig XML root element object
     * @param nameStack    stack of config names
     */
    private void readChildElements(OMElement serverConfig, Stack<String> nameStack) {

        for (Iterator childElements = serverConfig.getChildElements(); childElements.hasNext();) {
            OMElement element = (OMElement) childElements.next();
            nameStack.push(element.getLocalName());
            if (elementHasText(element)) {
                String key = getKey(nameStack);
                Object currentObject = configuration.get(key);
                String value = replaceSystemProperty(element.getText());
                if (secretResolver != null && secretResolver.isInitialized()) {
                    value = MiscellaneousUtil.resolve(element, secretResolver);
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
            }
            readChildElements(element, nameStack);
            nameStack.pop();
        }
    }

    /**
     * Method to obtain config key from stack.
     *
     * @param nameStack Stack of strings with names.
     * @return key as a String
     */
    private String getKey(Stack<String> nameStack) {

        return String.join(".", nameStack);
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
            if (sysProp.equals(FinancialServicesConstants.CARBON_HOME) &&
                    System.getProperty(FinancialServicesConstants.CARBON_HOME).equals(".")) {
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

    /**
     * Returns the element with the provided key as a String.
     *
     * @param key local part name
     * @return Corresponding value for key
     */
    private Optional<String> getConfigurationFromKeyAsString(final String key) {
        return Optional.ofNullable((String) configuration.get(key));
    }

    public Map<String, Map<Integer, String>> getFinancialServicesExecutors() {

        return Collections.unmodifiableMap(fsExecutors);
    }

    public Map<String, Map<Integer, String>> getConsentAuthorizeSteps() {

        return Collections.unmodifiableMap(authorizeSteps);
    }

    public Map<String, Map<String, Object>> getDCRParamsConfig() {

        return Collections.unmodifiableMap(dcrParams);
    }

    public Map<String, Map<String, Object>> getDCRValidatorsConfig() {

        return Collections.unmodifiableMap(dcrValidators);
    }

    public String getDataSourceName() {

        Optional<String> source = getConfigurationFromKeyAsString(FinancialServicesConstants.JDBC_PERSISTENCE_CONFIG);
        return source.map(String::trim).orElse("");
    }

    /**
     * Returns the database connection verification timeout in seconds configured in
     * financial-services.xml.
     *
     * @return 1 if nothing is configured
     */
    public int getConnectionVerificationTimeout() {

        Optional<String> timeout = getConfigurationFromKeyAsString(
                FinancialServicesConstants.DB_CONNECTION_VERIFICATION_TIMEOUT);
        return timeout.map(Integer::parseInt).orElse(1);
    }

    public String getAuthServletExtension() {
        Optional<String> source = getConfigurationFromKeyAsString(FinancialServicesConstants.AUTH_SERVLET_EXTENSION);
        return source.map(String::trim).orElse("");
    }

    public String getJWKSConnectionTimeOut() {

        Optional<String> source = getConfigurationFromKeyAsString(
                FinancialServicesConstants.JWKS_CONNECTION_TIMEOUT);
        return source.map(String::trim).orElse("3000");
    }

    public String getJWKSReadTimeOut() {

        Optional<String> source = getConfigurationFromKeyAsString(FinancialServicesConstants.JWKS_READ_TIMEOUT);
        return source.map(String::trim).orElse("3000");
    }

    /**
     * ConnectionPool maximum connection count.
     *
     * @return maximum connections count, default value is 2000
     */
    public int getConnectionPoolMaxConnections() {

        Optional<String> timeout = getConfigurationFromKeyAsString(
                FinancialServicesConstants.CONNECTION_POOL_MAX_CONNECTIONS);
        return timeout.map(Integer::parseInt).orElse(2000);
    }

    /**
     * ConnectionPool maximum connection per route count.
     *
     * @return maximum connections per route value, default value is 1500
     */
    public int getConnectionPoolMaxConnectionsPerRoute() {

        Optional<String> timeout = getConfigurationFromKeyAsString(
                FinancialServicesConstants.CONNECTION_POOL_MAX_CONNECTIONS_PER_ROUTE);
        return timeout.map(Integer::parseInt).orElse(1500);
    }

    /**
     * Returns the expiry time for cache modification.
     *
     * @return int Expiry time.
     */
    public int getCommonCacheModifiedExpiryTime() {
        Optional<String> expiryTime = getConfigurationFromKeyAsString(
                FinancialServicesConstants.COMMON_IDENTITY_CACHE_MODIFY_EXPIRY);
        return expiryTime.map(Integer::parseInt).orElse(60);
    }

    /**
     * Returns the expiry time for cache access.
     *
     * @return int Expiry time.
     */
    public int getCommonCacheAccessExpiryTime() {

        Optional<String> expiryTime = getConfigurationFromKeyAsString(
                FinancialServicesConstants.COMMON_IDENTITY_CACHE_ACCESS_EXPIRY);
        return expiryTime.map(Integer::parseInt).orElse(60);
    }


    /**
     * Method to get the value Idempotency enable configuration.
     * 
     * @return Whether Idempotency is enabled or not
     */
    public boolean isIdempotencyValidationEnabled() {
        Optional<String> config = getConfigurationFromKeyAsString(FinancialServicesConstants.IDEMPOTENCY_IS_ENABLED);
        return config.map(Boolean::parseBoolean).orElse(false);
    }

    /**
     * Method to get the value Idempotency allowed time configuration.
     * 
     * @return Idempotency allowed time
     */
    public String getIdempotencyAllowedTime() {
        Optional<String> config = getConfigurationFromKeyAsString(FinancialServicesConstants.IDEMPOTENCY_ALLOWED_TIME);
        return config.map(String::trim).orElse("1440");
    }

    public String getConsentAPIUsername() {

        Optional<String> source = getConfigurationFromKeyAsString(FinancialServicesConstants.CONSENT_API_USERNAME);
        return source.map(String::trim).orElse("admin");
    }

    public String getConsentAPIPassword() {

        Optional<String> source = getConfigurationFromKeyAsString(FinancialServicesConstants.CONSENT_API_PASSWORD);
        return source.map(String::trim).orElse("admin");
    }

    public String getPreserveConsent() {

        Optional<String> source = getConfigurationFromKeyAsString(FinancialServicesConstants.PRESERVE_CONSENT);
        return source.map(String::trim).orElse("false");
    }

    public String getConsentValidationConfig() {

        Optional<String> source = getConfigurationFromKeyAsString(
                FinancialServicesConstants.CONSENT_JWT_PAYLOAD_VALIDATION);
        return source.map(String::trim).orElse("");
    }

    //Event notifications configurations.
    public String getEventNotificationTokenIssuer() {

        Optional<String> source = getConfigurationFromKeyAsString(FinancialServicesConstants.TOKEN_ISSUER);
        return source.map(String::trim).orElse("www.wso2.com");
    }

    public int getNumberOfSetsToReturn() {

        Optional<String> config = getConfigurationFromKeyAsString(FinancialServicesConstants.MAX_SETS_TO_RETURN);
        return config.map(Integer::parseInt).orElse(5);
    }

    public boolean isSubClaimIncluded() {

        Optional<String> config = getConfigurationFromKeyAsString(FinancialServicesConstants.IS_SUB_CLAIM_INCLUDED);
        return config.map(Boolean::parseBoolean).orElse(false);
    }

    public boolean isToeClaimIncluded() {

        Optional<String> config = getConfigurationFromKeyAsString(FinancialServicesConstants.IS_TOE_CLAIM_INCLUDED);
        return config.map(Boolean::parseBoolean).orElse(false);
    }

    public boolean isTxnClaimIncluded() {

        Optional<String> config = getConfigurationFromKeyAsString(FinancialServicesConstants.IS_TXN_CLAIM_INCLUDED);
        return config.map(Boolean::parseBoolean).orElse(false);
    }

    /**
     * Method to determine real-time event notification feature is enabled or not from the configurations.
     *
     * @return boolean value indicating the state
     */
    public boolean isRealtimeEventNotificationEnabled() {

        Optional<String> config = getConfigurationFromKeyAsString(
                FinancialServicesConstants.REALTIME_EVENT_NOTIFICATION_ENABLED);
        return config.map(Boolean::parseBoolean).orElse(false);
    }

    /**
     * Method to get periodic Cron expression config for realtime event notifications scheduler.
     *
     * @return String Cron expression to trigger the Cron job for real-time event notification
     */
    public String getRealtimeEventNotificationSchedulerCronExpression() {

        Optional<String> source = getConfigurationFromKeyAsString(FinancialServicesConstants.PERIODIC_CRON_EXPRESSION);
        return source.map(String::trim).orElse("0 0/1 0 ? * * *");
    }

    /**
     * Method to get TIMEOUT_IN_SECONDS config for realtime event notifications.
     *
     * @return integer timeout for the HTTP Client's POST requests
     */
    public int getRealtimeEventNotificationTimeoutInSeconds() {

        Optional<String> config = getConfigurationFromKeyAsString(FinancialServicesConstants.TIMEOUT_IN_SECONDS);
        return config.map(Integer::parseInt).orElse(60);
    }

    /**
     * Method to get MAX_RETRIES config for realtime event notifications.
     *
     * @return integer maximum number of retries to the retry policy in real-time notification sender
     */
    public int getRealtimeEventNotificationMaxRetries() {

        Optional<String> config = getConfigurationFromKeyAsString(FinancialServicesConstants.MAX_RETRIES);
        return config.map(Integer::parseInt).orElse(5);
    }

    /**
     * Method to get INITIAL_BACKOFF_TIME_IN_SECONDS config for realtime event notifications.
     *
     * @return integer start waiting time for the retry policy before the first retry
     */
    public int getRealtimeEventNotificationInitialBackoffTimeInSeconds() {

        Optional<String> config = getConfigurationFromKeyAsString(
                FinancialServicesConstants.INITIAL_BACKOFF_TIME_IN_SECONDS);
        return config.map(Integer::parseInt).orElse(60);
    }

    /**
     * Method to get BACKOFF_FUNCTION config for realtime event notifications.
     * Function name should be "EX", "CONSTANT" or "LINEAR".
     *
     * @return string indicating the retry function
     */
    public String getRealtimeEventNotificationBackoffFunction() {

        Optional<String> source = getConfigurationFromKeyAsString(FinancialServicesConstants.BACKOFF_FUNCTION);
        return source.map(String::trim).orElse("EX");
    }

    /**
     * Method to get CIRCUIT_BREAKER_OPEN_TIMEOUT_IN_SECONDS config for realtime event notifications.
     *
     * @return integer timeout to break the retrying process and make that notification as ERR
     */
    public int getRealtimeEventNotificationCircuitBreakerOpenTimeoutInSeconds() {

        Optional<String> config = getConfigurationFromKeyAsString(
                FinancialServicesConstants.CIRCUIT_BREAKER_OPEN_TIMEOUT_IN_SECONDS);
        return config.map(Integer::parseInt).orElse(600);
    }

    /**
     * Method to get EVENT_NOTIFICATION_THREAD_POOL_SIZE config for realtime event notifications.
     *
     * @return integer fix size to set the Thread Pool size in the real-time event notification sender
     */
    public int getEventNotificationThreadPoolSize() {

        Optional<String> config = getConfigurationFromKeyAsString(
                FinancialServicesConstants.EVENT_NOTIFICATION_THREAD_POOL_SIZE);
        return config.map(Integer::parseInt).orElse(20);
    }

    /**
     * Method to get EVENT_NOTIFICATION_GENERATOR config for event notifications.
     *
     * @return String class name of the event notification generator to generate the event notification payload
     */
    public String getEventNotificationGenerator() {

        Optional<String> source = getConfigurationFromKeyAsString(
                FinancialServicesConstants.EVENT_NOTIFICATION_GENERATOR);
        return source.map(String::trim).orElse("com.wso2.openbanking.accelerator.event.notifications." +
                "service.service.DefaultEventNotificationGenerator");
    }

    /**
     * Method to get REALTIME_EVENT_NOTIFICATION_REQUEST_GENERATOR config for realtime event notifications.
     *
     * @return String class path of the realtime event notification payload generator
     */
    public String getRealtimeEventNotificationRequestGenerator() {

        Optional<String> source = getConfigurationFromKeyAsString(
                FinancialServicesConstants.REALTIME_EVENT_NOTIFICATION_REQUEST_GENERATOR);
        return source.map(String::trim).orElse("com.wso2.openbanking.accelerator.event.notifications.service." +
                "realtime.service.DefaultRealtimeEventNotificationRequestGenerator");
    }

    /**
     * Method to determine service extensions feature is enabled or not from the configurations.
     *
     * @return boolean value indicating the state
     */
    public boolean isServiceExtensionsEndpointEnabled() {

        Optional<String> config = getConfigurationFromKeyAsString(
                FinancialServicesConstants.SERVICE_EXTENSIONS_ENDPOINT_ENABLED);
        return config.map(Boolean::parseBoolean).orElse(false);
    }

    /**
     * Method to get service extensions endpoint base url.
     *
     * @return String service extensions endpoint base url
     */
    public String getServiceExtensionsEndpointBaseUrl() {

        Optional<String> config = getConfigurationFromKeyAsString(
                FinancialServicesConstants.SERVICE_EXTENSIONS_ENDPOINT_BASE_URL);
        return config.map(String::trim).orElse(null);
    }

    /**
     * Method to get service extension types.
     *
     * @return List of service extensions
     */
    public List<ServiceExtensionTypeEnum> getServiceExtensionTypes() {
        Object serviceExtensionTypesObj = configuration.get(
                FinancialServicesConstants.SERVICE_EXTENSIONS_EXTENSION);

        List<String> serviceExtensionTypes = serviceExtensionTypesObj instanceof List<?>
                ? (List<String>) serviceExtensionTypesObj
                : Collections.singletonList((String) serviceExtensionTypesObj);

        return serviceExtensionTypes.stream()
                .map(ServiceExtensionTypeEnum::fromString)
                .collect(Collectors.toList());
    }

    /**
     * Method to get service extensions endpoint security type.
     *
     * @return String service extensions endpoint security type
     */
    public String getServiceExtensionsEndpointSecurityType() {

        Optional<String> config = getConfigurationFromKeyAsString(
                FinancialServicesConstants.SERVICE_EXTENSIONS_SECURITY_TYPE);
        return config.map(String::trim).orElse(null);
    }

    /**
     * Method to get service extensions endpoint security basic auth username.
     *
     * @return String service extensions endpoint security basic auth username
     */
    public String getServiceExtensionsEndpointSecurityBasicAuthUsername() {

        Optional<String> config = getConfigurationFromKeyAsString(
                FinancialServicesConstants.SERVICE_EXTENSIONS_BASIC_AUTH_USERNAME);
        return config.map(String::trim).orElse(null);
    }

    /**
     * Method to get service extensions endpoint security basic auth password.
     *
     * @return String service extensions endpoint security basic auth password
     */
    public String getServiceExtensionsEndpointSecurityBasicAuthPassword() {

        Optional<String> config = getConfigurationFromKeyAsString(
                FinancialServicesConstants.SERVICE_EXTENSIONS_BASIC_AUTH_PASSWORD);
        return config.map(String::trim).orElse(null);
    }

    /**
     * Method to get service extensions endpoint security oauth2 token.
     *
     * @return String service extensions endpoint security oauth2 token
     */
    public String getServiceExtensionsEndpointSecurityOauth2Token() {

        Optional<String> config = getConfigurationFromKeyAsString(
                FinancialServicesConstants.SERVICE_EXTENSIONS_OAUTH2_TOKEN);
        return config.map(String::trim).orElse(null);
    }

    /**
     * Get config related for checking whether PSU is a federated user or not.
     *
     * @return Boolean value indicating whether PSU is a federated user or not
     */
    public boolean isPSUFederated() {

        Optional<String> config = getConfigurationFromKeyAsString(FinancialServicesConstants.IS_PSU_FEDERATED);
        return config.map(Boolean::parseBoolean).orElse(false);
    }

    /**
     * Get Federated PSU IDP Name.
     *
     * @return String Federated IDP name
     */
    public String getFederatedIDPName() {

        Optional<String> config = getConfigurationFromKeyAsString(
                FinancialServicesConstants.PSU_FEDERATED_IDP_NAME);
        return config.map(String::trim).orElse(null);
    }
}
