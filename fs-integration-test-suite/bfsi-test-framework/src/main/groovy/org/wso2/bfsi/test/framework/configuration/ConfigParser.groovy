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

package org.wso2.bfsi.test.framework.configuration

import org.apache.axiom.om.OMElement
import org.apache.axiom.om.OMException
import org.apache.axiom.om.impl.builder.StAXOMBuilder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.wso2.bfsi.test.framework.constant.ConfigConstants
import org.wso2.bfsi.test.framework.constant.Constants
import org.wso2.bfsi.test.framework.exception.TestFrameworkException
import org.wso2.bfsi.test.framework.exception.TestFrameworkRuntimeException
import org.wso2.securevault.SecretResolver
import org.wso2.securevault.SecretResolverFactory

import javax.xml.namespace.QName
import javax.xml.stream.XMLStreamException

/**
 * Class for read configuration file and keep configuration data
 */
class ConfigParser {

    // To enable attempted thread-safety using double-checklocking
    private static final Object lock = new Object()

    private static ConfigParser parser

    static Logger log = LogManager.getLogger(ConfigParser.class.getName())

    // Location of Configuration file
    private static String configurationLocation

    // Configuration File
    private File configurationXml

    // Root element tag of Configuration file
    private OMElement rootElement

    private int tppNumber

    private int psuNumber

    private static SecretResolver secretResolver


    // Data map for keep all configuration data
    private static final Map<String, Object> configuration = new HashMap<>()

    /**
     * Keep list of applications
     * Each application is a java map
     * populate by buildApplicationConfig() method
     */
    private List<Object> applicationConfig

    /**
     * Keep list of psu
     * Each psu is a java map
     * populate by buildPsuConfig() method
     */
    private List<Object> psuConfig

    /**
     * Child Tag Arrays
     * Used to iterate through application tag elements
     */
    private String[] appKeystoreChildTagArray = new String[]{ConfigConstants.APPLICATION_KEYSTORE_LOCATION
            , ConfigConstants.APPLICATION_KEYSTORE_ALIAS
            , ConfigConstants.APPLICATION_KEYSTORE_PWD, ConfigConstants.APPLICATION_KEYSTORE_DOMAIN_NAME
            , ConfigConstants.APPLICATION_KEYSTORE_SIGNING_KID}

    private String[] appTransportKeystoreChildTagArray = new String[]{ConfigConstants.APPLICATION_TRANSPORT_KEYSTORE_LOCATION
            , ConfigConstants.APPLICATION_TRANSPORT_KEYSTORE_TYPE, ConfigConstants.APPLICATION_TRANSPORT_KEYSTORE_PWD,
            ConfigConstants.APPLICATION_TRANSPORT_KEYSTORE_ALIAS}

    private String[] appDCRChildTagArray = new String[]{ConfigConstants.APPLICATION_DCR_SSA_PATH
            , ConfigConstants.APPLICATION_DCR_SELF_SIGNED_SSA
            , ConfigConstants.APPLICATION_DCR_SOFTWARE_ID, ConfigConstants.APPLICATION_DCR_REDIRECT_URL
            , ConfigConstants.APPLICATION_DCR_ALT_REDIRECT_URL, ConfigConstants.APPLICATION_DCR_API_VERSION}

    private String[] appInfoChildTagArray = new String[]{ConfigConstants.APPLICATION_APP_INFO_CLIENT_ID
            , ConfigConstants.APPLICATION_APP_INFO_CLIENT_SECRET, ConfigConstants.APPLICATION_APP_INFO_REDIRECT_URL}

    /**
     * Private Constructor of config parser.
     */
    private ConfigParser(String filePath = null) throws TestFrameworkException {
        buildConfiguration(filePath)
        this.tppNumber = 0
    }

    /**
     * Get OB Configuration file
     * @return
     */
    File getOBXMLFile() {
        return configurationXml
    }

    void refreshConfiguration() {
        applicationConfig = null
        buildConfiguration()
    }

    /**
     * Singleton getInstance method to create only one object.
     *
     * @return ConfigParser object
     */
    static ConfigParser getInstance(String filePath = null) throws TestFrameworkException {
        if (parser == null) {
            synchronized (lock) {
                if (parser == null) {
                    try {
                        parser = new ConfigParser(filePath)
                        return parser
                    } catch (TestFrameworkException e) {
                        log.error("Failed to initiate config parser", e);
                        parser = null;
                    }
                }
            }
        }
        return parser;
    }

    void setTppNumber(int tpp) {
        this.tppNumber = tpp
    }

    int getTppNumber() {
        return this.tppNumber
    }

    void setPsuNumber(int psu) {
        this.psuNumber = psu
    }

    int getPsuNumber() {
        return this.psuNumber
    }

    /**
     * Method to read the configuration as a model and put them in the configuration maps.
     * Run when initiating Config Parser class
     */
    private void buildConfiguration(String filePath = null) {
        InputStream inStream = null;
        StAXOMBuilder builder;
        String warningMessage = "";
        configurationLocation = filePath
        try {
            if (configurationLocation != null) {
                configurationXml = new File(configurationLocation);
                if (configurationXml.exists()) {
                    inStream = new FileInputStream(configurationXml);
                }
            } else {
                configurationXml = new File(ConfigConstants.OB_CONFIG_FILE_LOCATION);
                if (configurationXml.exists()) {
                    inStream = new FileInputStream(configurationXml);
                }
            }

            if (inStream == null) {
                String message =
                        "Open Banking common configuration not found at: " + configurationLocation + " . Cause - FileNotFoundException"
                log.error("Configuration cannot read", message)
                throw new TestFrameworkException(message);
            }

            builder = new StAXOMBuilder(inStream);
            builder.setDoDebug(false);
            Stack<String> nameStack = new Stack<>();
            rootElement = builder.getDocumentElement();
            secretResolver = SecretResolverFactory.create(rootElement, true);
            readChildElements(rootElement, nameStack)
            applicationConfig = buildApplicationConfig()
            psuConfig = buildPsuConfig()

        } catch (IOException | XMLStreamException | OMException e) {
            throw new TestFrameworkRuntimeException("Error occurred while building configuration from open-banking.xml",
                    e as Throwable);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                log.warn("Cannot stop input stream", e)
            }
        }
    }

    /**
     * Method to read text configs from xml when root element is given.
     *
     * @param serverConfig XML root element object
     * @param nameStack stack of config names
     */
    private void readChildElements(OMElement serverConfig, Stack<String> nameStack) {
        for (Iterator childElements = serverConfig.getChildElements(); childElements.hasNext();) {
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
     * Method to obtain config key from stack.
     *
     * @param nameStack Stack of strings with names
     * @return key as a String
     */
    private String getKey(Stack<String> nameStack) {

        StringBuilder key = new StringBuilder();
        for (int index = 0; index < nameStack.size(); index++) {
            String name = nameStack.elementAt(index)
            key.append(name).append(".")
        }
        key.deleteCharAt(key.lastIndexOf("."))
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
        while (indexOfStartingChars < textBuilder.indexOf('${')
                && (indexOfStartingChars = textBuilder.indexOf('${')) != -1
                && (indexOfClosingBrace = textBuilder.indexOf('}')) != -1) { // Is a property used?

            String sysProp = textBuilder.substring(indexOfStartingChars + 2, indexOfClosingBrace);
            String propValue = System.getProperty(sysProp)
            if (propValue != null) {
                textBuilder = new StringBuilder(textBuilder.substring(0, indexOfStartingChars) + propValue
                        + textBuilder.substring(indexOfClosingBrace + 1))
            }
            if (sysProp.equals(Constants.CARBON_HOME) && System.getProperty(Constants.CARBON_HOME).equals(".")) {
                textBuilder.insert(0, new File(".").getAbsolutePath() + File.separator)
            }
        }
        return textBuilder.toString();
    }

    /**
     *  Read Configuration file and build a Map when only have 2 level tags.
     *  Read Parent Tag and child tags, and create Map.
     *  Upper Tag of parent tag should be given
     * @param root
     * @param parentTag
     * @param childNameArray
     * @return
     */
    protected Map<String, Object> buildMapWithTwoElement(OMElement root, String parentTag, String[] childNameArray) {

        Map<String, Object> tempMap = new HashMap<>();
        OMElement serverElement = root.getFirstChildWithName(
                new QName(ConfigConstants.TEST_CONFIG_QNAME, parentTag));
        if (serverElement != null) {

            for (String child : childNameArray) {
                OMElement serverChildElement = serverElement.getFirstChildWithName(
                        new QName(ConfigConstants.TEST_CONFIG_QNAME, child))
                if (serverChildElement != null) {
                    tempMap.put(child, serverChildElement.getText())
                }
            }
        }
        return tempMap
    }


    /**
     *  Method for build Application configuration map.
     *  Iterate through the every app-config and build a list of application
     *  List contains Maps for each Application
     * @return
     */
    private List<Object> buildApplicationConfig() {
        List<Object> appList = new ArrayList<>();
        OMElement appConfigElement = rootElement.getFirstChildWithName(
                new QName(ConfigConstants.TEST_CONFIG_QNAME, ConfigConstants.APPLICATION_LIST_CONFIG));

        if (appConfigElement != null) {
            Iterator application = appConfigElement.getChildrenWithLocalName(ConfigConstants.APPLICATION_CONFIG);
            while (application.hasNext()) {
                OMElement applicationElement = (OMElement) application.next();
                appList.add(buildApplicationInfoMap(applicationElement))
            }
        }
        return appList;
    }

    /**
     * Build Application Information Map which used in buildApplicationConfig() method
     * @param app
     * @return
     */
    private Map<String, Object> buildApplicationInfoMap(OMElement app) {
        Map<String, Object> tempMap = new HashMap<>()

        // Get Application Signing Keystore as a Map
        Map<String, Object> keyStore = buildMapWithTwoElement(app, ConfigConstants.APPLICATION_KEYSTORE
                , appKeystoreChildTagArray)
        if (keyStore != null) {
            tempMap.put(ConfigConstants.APPLICATION_KEYSTORE, keyStore)
        }

        //  Get Application Transport data
        Map<String, Object> tempMapTransport = new HashMap<>()
        OMElement transportElement = app.getFirstChildWithName(
                new QName(ConfigConstants.TEST_CONFIG_QNAME, ConfigConstants.APPLICATION_TRANSPORT))
        if (transportElement != null) {
            OMElement mlts = transportElement.getFirstChildWithName(
                    new QName(ConfigConstants.TEST_CONFIG_QNAME, ConfigConstants.APPLICATION_TRANSPORT_MLTS))
            Map<String, Object> transportKeyStore = buildMapWithTwoElement(transportElement
                    , ConfigConstants.APPLICATION_TRANSPORT_KEYSTORE
                    , appTransportKeystoreChildTagArray)
            tempMapTransport.put(ConfigConstants.APPLICATION_TRANSPORT_MLTS, mlts.getText())
            tempMapTransport.put(ConfigConstants.APPLICATION_TRANSPORT_KEYSTORE, transportKeyStore)
            tempMap.put(ConfigConstants.APPLICATION_TRANSPORT, tempMapTransport)
        }

        // Get Application DCR data
        Map<String, Object> dcr = buildMapWithTwoElement(app, ConfigConstants.APPLICATION_DCR, appDCRChildTagArray)
        if (dcr != null) {
            tempMap.put(ConfigConstants.APPLICATION_DCR, dcr)
        }

        // Get Application information
        Map<String, Object> appInfo = buildMapWithTwoElement(app, ConfigConstants.APPLICATION_APP_INFO, appInfoChildTagArray)
        if (appInfo != null) {
            tempMap.put(ConfigConstants.APPLICATION_APP_INFO, appInfo)
        }

        return tempMap
    }

    /**
     * Get Configuration Map
     * @return
     */
    Map getConfigurationMap() {
        return configuration
    }

    /**
     * Get Application
     * @return
     */
    List<Object> getApplicationConfig() {
        return applicationConfig
    }

    /**
     * Get PSU
     * @return
     */
    List<Object> getPsuConfig() {
        return psuConfig
    }

    /**
     *  Method for building PSU List configuration map.
     *  Iterate through the every PSUInfo and build a list of PSUs
     *  List contains Maps for each PSU
     * @return
     */
    private List<Object> buildPsuConfig() {
        List<Object> psuList = new ArrayList<>()

        OMElement psuConfigElement = rootElement.getFirstChildWithName(
                new QName(ConfigConstants.TEST_CONFIG_QNAME, ConfigConstants.PSU_LIST_CONFIG))

        if (psuConfigElement != null) {
            Iterator psu = psuConfigElement.getChildrenWithLocalName(ConfigConstants.PSU_INFO)
            // Iterate through the every PSUInfo tags in the configuration file and build a list of PSUs
            while (psu.hasNext()) {
                OMElement psuElement = (OMElement) psu.next()
                psuList.add(buildPsuInfoMap(psuElement))
            }
        }
        return psuList
    }

    /**
     * Child Tag Arrays
     * Used to iterate through PSUInfo tag elements
     */
    private String[] psuInfoChildTagArray = new String[]{ConfigConstants.USERS_USER_NAME
            , ConfigConstants.USERS_PWD}

    /**
     * Build PSU Information Map which used in buildPsuConfig() method
     * @param app
     * @return
     */
    private Map<String, Object> buildPsuInfoMap(OMElement psu) {
        Map<String, Object> psuInfoMap = new HashMap<>()

        // Get PSU Info as a Map
        Map<String, Object> psuInfo = buildMapWithTwoElement(psu, ConfigConstants.CREDENTIALS
                , psuInfoChildTagArray)
        if (psuInfo != null) {
            psuInfoMap.put(ConfigConstants.CREDENTIALS, psuInfo)
        }
        return psuInfoMap
    }
}

