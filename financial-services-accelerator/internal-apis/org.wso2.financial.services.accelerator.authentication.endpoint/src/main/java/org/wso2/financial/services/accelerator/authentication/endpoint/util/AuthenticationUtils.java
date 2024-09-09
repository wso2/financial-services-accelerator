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

package org.wso2.financial.services.accelerator.authentication.endpoint.util;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.financial.services.accelerator.authentication.endpoint.FSConsentConfirmServlet;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.FSAuthServletInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Utility class for authentication related operations.
 */
public class AuthenticationUtils {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationUtils.class);
    private static final FinancialServicesConfigParser parser = FinancialServicesConfigParser.getInstance();

    private static final String BUNDLE = "org.wso2.bfsi.authentication.endpoint.i18n";

    /**
     * @param data error response received from consent data retrieval endpoint
     * @return formatted error response to be send to call back uri
     */
    public static String getErrorResponseForRedirectURL(JSONObject data) throws UnsupportedEncodingException {

        String errorResponse = "";
        if (data.has(Constants.ERROR)) {
            errorResponse = errorResponse.concat(Constants.ERROR_URI_FRAGMENT)
                    .concat(URLEncoder.encode(data.get(Constants.ERROR).toString(),
                            StandardCharsets.UTF_8.toString()));
        }
        if (data.has(Constants.ERROR_DESCRIPTION)) {
            errorResponse = errorResponse.concat(Constants.ERROR_DESCRIPTION_PARAMETER)
                    .concat(URLEncoder.encode(data.get(Constants.ERROR_DESCRIPTION).toString(),
                            StandardCharsets.UTF_8.toString()));
        }
        if (data.has(Constants.STATE)) {
            errorResponse = errorResponse.concat(Constants.STATE_PARAMETER)
                    .concat(URLEncoder.encode(data.get(Constants.STATE).toString(),
                            StandardCharsets.UTF_8.toString()));
        }
        return errorResponse;
    }

    /**
     * Retrieve the config value for Auth servlet Extension.
     */
    public static FSAuthServletInterface getAuthExtension() {

        try {
            return (FSAuthServletInterface) Class.forName(parser.getAuthServletExtension())
                    .getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
                | ClassNotFoundException e) {
            log.error("Webapp extension not found", e);
            return null;
        }
    }

    @Generated(message = "Encapsulated method for unit test")
    public static ResourceBundle getResourceBundle(Locale locale) {

        return ResourceBundle.getBundle(BUNDLE, locale);
    }

    /**
     * Retrieve admin credentials in Base64 format from webapp properties or OB
     * configs.
     */
    public static String getConsentApiCredentials() {
        String username, password;
        try {
            InputStream configurations = FSConsentConfirmServlet.class.getClassLoader()
                    .getResourceAsStream(Constants.CONFIG_FILE_NAME);
            Properties configurationProperties = new Properties();
            configurationProperties.load(configurations);
            boolean isConfiguredInWebapp = Boolean.parseBoolean(
                    configurationProperties.getProperty(Constants.LOCATION_OF_CREDENTIALS));
            if (!isConfiguredInWebapp) {
                username = parser.getConsentAPIUsername();
                password = parser.getConsentAPIPassword();
            } else {
                username = configurationProperties.getProperty(Constants.USERNAME_IN_WEBAPP_CONFIGS);
                password = configurationProperties.getProperty(Constants.PASSWORD_IN_WEBAPP_CONFIGS);
            }
        } catch (IOException | NullPointerException e) {
            log.error("Error occurred while reading the webapp properties file. Therefore using OB configurations.");
            username = parser.getConsentAPIUsername();
            password = parser.getConsentAPIPassword();
        }
        return Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
