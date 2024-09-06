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
import org.wso2.financial.services.accelerator.common.util.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final Map<String, List<String>> allowedScopes = new HashMap<>();
    private final Map<String, List<String>> allowedAPIs = new HashMap<>();
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
     * @return OpenBankingConfigParser object
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
     * Method to read the configuration (in a recursive manner) as a model and put them in the configuration map.
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
            buildOBExecutors();
            buildConsentAuthSteps();
            buildAllowedScopes();
            buildAllowedSubscriptions();
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

    private void buildOBExecutors() {

        OMElement gatewayElement = rootElement.getFirstChildWithName(
                new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                        FinancialServicesConstants.GATEWAY_CONFIG_TAG));

        if (gatewayElement != null) {

            OMElement openBankingGatewayExecutors = gatewayElement.getFirstChildWithName(
                    new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                            FinancialServicesConstants.GATEWAY_EXECUTOR_CONFIG_TAG));

            if (openBankingGatewayExecutors != null) {
                //obtaining each consent type element under OpenBankingGatewayExecutors tag
                Iterator consentTypeElement = openBankingGatewayExecutors.getChildElements();
                while (consentTypeElement.hasNext()) {
                    OMElement consentType = (OMElement) consentTypeElement.next();
                    String consentTypeName = consentType.getLocalName();
                    Map<Integer, String> executors = new HashMap<>();
                    //obtaining each Executor element under each consent type
                    Iterator<OMElement> obExecutor = consentType.getChildrenWithName(
                            new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                                    FinancialServicesConstants.EXECUTOR_CONFIG_TAG));
                    if (obExecutor != null) {
                        while (obExecutor.hasNext()) {
                            OMElement executorElement = obExecutor.next();
                            //Retrieve class name and priority from executor config
                            String obExecutorClass = executorElement.getAttributeValue(new QName("class"));
                            String obExecutorPriority = executorElement.getAttributeValue(new QName("priority"));

                            if (StringUtils.isEmpty(obExecutorClass)) {
                                //Throwing exceptions since we cannot proceed without invalid executor names
                                throw new FinancialServicesRuntimeException("Executor class is not defined " +
                                        "correctly in financial-services.xml");
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
                //obtaining each step type element under AuthorizeSteps tag
                Iterator stepTypeElement = consentAuthorizeSteps.getChildElements();
                while (stepTypeElement.hasNext()) {
                    OMElement stepType = (OMElement) stepTypeElement.next();
                    String consentTypeName = stepType.getLocalName();
                    Map<Integer, String> executors = new HashMap<>();
                    //obtaining each step under each consent type
                    Iterator<OMElement> obExecutor = stepType.getChildrenWithName(
                            new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                                    FinancialServicesConstants.STEP_CONFIG_TAG));
                    if (obExecutor != null) {
                        while (obExecutor.hasNext()) {
                            OMElement executorElement = obExecutor.next();
                            //Retrieve class name and priority from executor config
                            String obExecutorClass = executorElement.getAttributeValue(new QName("class"));
                            String obExecutorPriority = executorElement.getAttributeValue(new QName("priority"));

                            if (StringUtils.isEmpty(obExecutorClass)) {
                                //Throwing exceptions since we cannot proceed without invalid executor names
                                throw new FinancialServicesRuntimeException("Executor class is not defined " +
                                        "correctly in financial-services.xml");
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
            }
            readChildElements(element, nameStack);
            nameStack.pop();
        }
    }

    private void buildAllowedScopes() {
        OMElement gatewayElement = rootElement.getFirstChildWithName(
                new QName(FinancialServicesConstants.FS_CONFIG_QNAME, FinancialServicesConstants.GATEWAY_CONFIG_TAG));

        if (gatewayElement != null) {
            OMElement tppManagementElement = gatewayElement.getFirstChildWithName(
                    new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                            FinancialServicesConstants.TPP_MANAGEMENT_CONFIG_TAG));

            if (tppManagementElement != null) {
                OMElement allowedScopesElement = tppManagementElement.getFirstChildWithName(new QName(
                        FinancialServicesConstants.FS_CONFIG_QNAME,
                        FinancialServicesConstants.ALLOWED_SCOPES_CONFIG_TAG));

                //obtaining each scope under allowed scopes
                Iterator environmentIterator =
                        allowedScopesElement.getChildrenWithLocalName(FinancialServicesConstants.SCOPE_CONFIG_TAG);

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
                new QName(FinancialServicesConstants.FS_CONFIG_QNAME, FinancialServicesConstants.DCR_CONFIG_TAG));

        if (dcrElement != null) {
            OMElement regulatoryAPIs = dcrElement.getFirstChildWithName(
                    new QName(FinancialServicesConstants.FS_CONFIG_QNAME,
                            FinancialServicesConstants.REGULATORY_API_NAMES));

            if (regulatoryAPIs != null) {

                //obtaining each regulatory API under allowed regulatory APIs
                Iterator environmentIterator =
                        regulatoryAPIs.getChildrenWithLocalName(FinancialServicesConstants.REGULATORY_API);

                while (environmentIterator.hasNext()) {
                    OMElement regulatoryAPIElem = (OMElement) environmentIterator.next();
                    String regulatoryAPIName = regulatoryAPIElem.getAttributeValue(new QName(
                            FinancialServicesConstants.API_NAME));
                    String rolesStr = regulatoryAPIElem.getAttributeValue(new QName(
                            FinancialServicesConstants.API_ROLE));
                    if (StringUtils.isNotEmpty(rolesStr)) {
                        List<String> rolesList = Arrays.stream(rolesStr.split(","))
                                .map(String::trim)
                                .collect(Collectors.toList());
                        allowedAPIs.put(regulatoryAPIName, rolesList);
                    } else {
                        allowedAPIs.put(regulatoryAPIName, Collections.emptyList());
                    }
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

    public Map<String, List<String>> getAllowedScopes() {
        return Collections.unmodifiableMap(allowedScopes);
    }

    public Map<String, List<String>> getAllowedAPIs() {
        return Collections.unmodifiableMap(allowedAPIs);
    }

    public String getDataSourceName() {

        Optional<String> source = getConfigurationFromKeyAsString(FinancialServicesConstants.JDBC_PERSISTENCE_CONFIG);
        return source.map(String::trim).orElse("");
    }

    /**
     * Returns the database connection verification timeout in seconds configured in financial-services.xml.
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
                FinancialServicesConstants.DCR_JWKS_CONNECTION_TIMEOUT);
        return source.map(String::trim).orElse("3000");
    }

    public String getJWKSReadTimeOut() {

        Optional<String> source = getConfigurationFromKeyAsString(FinancialServicesConstants.DCR_JWKS_READ_TIMEOUT);
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

    //Event notifications configurations.
    public String getEventNotificationTokenIssuer() {

        Optional<String> source = getConfigurationFromKeyAsString(FinancialServicesConstants.TOKEN_ISSUER);
        return source.map(String::trim).orElse("www.wso2.com");
    }

    public int getNumberOfSetsToReturn() {

        Optional<String> timeout = getConfigurationFromKeyAsString(
                FinancialServicesConstants.MAX_SETS_TO_RETURN);
        return timeout.map(Integer::parseInt).orElse(5);
    }

    /**
     * Returns the expiry time for cache modification.
     *
     * @return String Expiry time.
     */
    public String getCommonCacheModifiedExpiryTime() {
        Optional<String> source = getConfigurationFromKeyAsString(
                FinancialServicesConstants.COMMON_IDENTITY_CACHE_MODIFY_EXPIRY);
        return source.map(String::trim).orElse("60");
    }

    /**
     * Returns the expiry time for cache access.
     *
     * @return String Expiry time.
     */
    public String getCommonCacheAccessExpiryTime() {

        Optional<String> source = getConfigurationFromKeyAsString(
                FinancialServicesConstants.COMMON_IDENTITY_CACHE_ACCESS_EXPIRY);
        return source.map(String::trim).orElse("60");
    }

    /**
     * Method to get software environment identification SSA property name.
     *
     * @return String software environment identification SSA property name.
     */
    public String getSoftwareEnvIdentificationSSAPropertyName() {

        Optional<String> source = getConfigurationFromKeyAsString(
                FinancialServicesConstants.DCR_SOFTWARE_ENV_IDENTIFICATION_PROPERTY_NAME);
        return source.map(String::trim).orElse(null);
    }

    /**
     * Method to get software environment identification value for sandbox in SSA.
     *
     * @return String software environment identification value for sandbox.
     */
    public String getSoftwareEnvIdentificationSSAPropertyValueForSandbox() {

        Optional<String> source = getConfigurationFromKeyAsString(
                FinancialServicesConstants.DCR_SOFTWARE_ENV_IDENTIFICATION_VALUE_FOR_SANDBOX);
        return source.map(String::trim).orElse(null);
    }


    /**
     * Method to get the value Idempotency enable configuration.
     * @return  Whether Idempotency is enabled or not
     */
    public boolean isIdempotencyValidationEnabled() {
        Optional<String> config = getConfigurationFromKeyAsString(FinancialServicesConstants.IDEMPOTENCY_IS_ENABLED);
        return config.map(Boolean::parseBoolean).orElse(false);
    }

    /**
     * Method to get the value Idempotency allowed time configuration.
     * @return  Idempotency allowed time
     */
    public String getIdempotencyAllowedTime() {
        Optional<String> config = getConfigurationFromKeyAsString(FinancialServicesConstants.IDEMPOTENCY_ALLOWED_TIME);
        return config.map(String::trim).orElse("1440");
    }


}
