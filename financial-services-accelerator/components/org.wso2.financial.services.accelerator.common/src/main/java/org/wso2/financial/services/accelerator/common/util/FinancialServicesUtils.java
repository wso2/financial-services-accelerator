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

package org.wso2.financial.services.accelerator.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Financial Services common utility class.
 */
public class FinancialServicesUtils {

    private static final Log log = LogFactory.getLog(FinancialServicesUtils.class);

    /**
     * Get Tenant Domain String for the client id.
     * 
     * @param clientId the client id of the application
     * @return tenant domain of the client
     * @throws FinancialServicesException if an error occurs while retrieving the
     *                                    tenant domain
     */
    @Generated(message = "Ignoring because OAuth2Util cannot be mocked with no constructors")
    public static String getSpTenantDomain(String clientId) throws FinancialServicesException {

        try {
            return OAuth2Util.getTenantDomainOfOauthApp(clientId);
        } catch (InvalidOAuthClientException | IdentityOAuth2Exception e) {
            throw new FinancialServicesException("Error retrieving service provider tenant domain for client_id: "
                    + clientId, e);
        }
    }

    /**
     * Method to obtain the Object when the full class path is given.
     *
     * @param classpath full class path
     * @param className  class name
     * @param <T> type of the class
     * @return new object instance
     */
    @Generated(message = "Ignoring since method contains no logics")
    public static <T> T  getClassInstanceFromFQN(String classpath, Class<T> className) {

        try {
            Object classObj = Class.forName(classpath).getDeclaredConstructor().newInstance();
            return className.cast(classObj);
        } catch (ClassNotFoundException e) {
            log.error(String.format("Class not found: %s", classpath.replaceAll("[\r\n]", "")));
            throw new FinancialServicesRuntimeException("Cannot find the defined class", e);
        } catch (InstantiationException | InvocationTargetException |
                 NoSuchMethodException | IllegalAccessException e) {
            //Throwing a runtime exception since we cannot proceed with invalid objects
            throw new FinancialServicesRuntimeException("Defined class" + classpath + "cannot be instantiated.", e);
        }
    }

    /**
     * Method to obtain the Object when the full class path object config is given.
     *
     * @param configObject full class path config object
     * @return new object instance
     */
    @Generated(message = "Ignoring since method contains no logics")
    public static Object getClassInstanceFromFQN(Object configObject) {

        if (configObject == null || StringUtils.isBlank(configObject.toString())) {
            return null;
        }

        String classpath = configObject.toString();
        try {
            return Class.forName(classpath).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            log.error(String.format("Class not found: %s",  classpath.replaceAll("[\r\n]", "")));
            throw new FinancialServicesRuntimeException("Cannot find the defined class", e);
        } catch (InstantiationException | InvocationTargetException |
                 NoSuchMethodException | IllegalAccessException e) {
            //Throwing a runtime exception since we cannot proceed with invalid objects
            throw new FinancialServicesRuntimeException("Defined class" + classpath + "cannot be instantiated.", e);
        }
    }

    /**
     * Method to reduce string length.
     *
     * @param input     Input for dispute data
     * @param maxLength Max length for dispute data
     * @return String with reduced length
     */
    public static String reduceStringLength(String input, int maxLength) {
        if (StringUtils.isEmpty(input) || input.length() <= maxLength) {
            return input;
        } else {
            return input.substring(0, maxLength);
        }
    }

    /**
     * Method to resolve username from user ID.
     *
     * @param userID   User ID
     * @return Username
     */
    @Generated(message = "Ignoring because OAuth2Util cannot be mocked with no constructors")
    public static String resolveUsernameFromUserId(String userID) {

        if (!startsWithUUID(userID)) {
            // If the user ID is not starting with a UUID that means request has sent the username,
            // return the same user ID as the username.
            return userID;
        }

        String username = null;
        try {
            if (userID.contains(FinancialServicesConstants.TENANT_DOMAIN)) {
                username =  OAuth2Util.resolveUsernameFromUserId(FinancialServicesConstants.TENANT_DOMAIN,
                        userID.split("@" + FinancialServicesConstants.TENANT_DOMAIN)[0]);
            } else {
                username =  OAuth2Util.resolveUsernameFromUserId(FinancialServicesConstants.TENANT_DOMAIN, userID);
            }
        } catch (UserStoreException e) {
            log.debug("Returning null since user ID is not found in the database", e);
            return null;
        }
        return username;
    }

    /**
     * Method to check whether the input string starts with a UUID.
     * @param input Input string
     * @return  true if the input string starts with a UUID
     */
    public static boolean startsWithUUID(String input) {
        Pattern uuidPattern = Pattern.compile("^" + FinancialServicesConstants.UUID_REGEX + ".*$");
        return uuidPattern.matcher(input).matches();
    }

    /**
     * Convert long date values to ISO 8601 format.
     * @param dateValue  Date value in long
     * @return ISO 8601 formatted date
     */
    public static String convertToISO8601(long dateValue) {

        DateFormat simple = new SimpleDateFormat(FinancialServicesConstants.ISO_FORMAT);
        Date simpleDateVal = new Date(dateValue * 1000);
        return simple.format(simpleDateVal);
    }

    /**
     * Method to obtain basic auth header.
     *
     * @param username Username of Auth header
     * @param password Password of Auth header
     * @return basic auth header
     */
    public static String getBasicAuthHeader(String username, char[] password) {

        byte[] authHeader = Base64.getEncoder().encode((username + FinancialServicesConstants.COLON +
                String.valueOf(password)).getBytes(StandardCharsets.UTF_8));
        return FinancialServicesConstants.BASIC_TAG + new String(authHeader, StandardCharsets.UTF_8);
    }

    /**
     * Extracts the consent ID from the essential claims JSON string.
     *
     * @param essentialClaims The essential claims JSON string
     * @return The consent ID extracted from the essential claims
     * @throws JsonProcessingException If an error occurs while processing the JSON
     */
    public static String getConsentIdFromEssentialClaims(String essentialClaims)
            throws JsonProcessingException {

        String jsonPath = FinancialServicesConfigParser.getInstance().getConsentIdExtractionJsonPath();

        if (StringUtils.isBlank(essentialClaims) || StringUtils.isBlank(jsonPath)) {
            return null; // Return null if input is invalid
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(essentialClaims);
        JsonNode targetNode = node.at(jsonPath);
        return extractConsentIdFromRegex(targetNode.asText());
    }

    /**
     * Extracts the consent ID from the given string using a regex pattern.
     * @param value The string to extract the consent ID from
     * @return The extracted consent ID, or null if not found
     */
    public static String extractConsentIdFromRegex(String value) {

        if (StringUtils.isBlank(value)) {
            return value;
        }

        String patternString = FinancialServicesConfigParser.getInstance().getConsentIdExtractionRegexPattern();

        if (StringUtils.isBlank(patternString)) {
            return value;
        }

        Pattern pattern = Pattern.compile(patternString);

        Matcher matcher = pattern.matcher(value);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Get consent ID from the scopes request parameter.
     * Used to extract the consent ID from the scopes request parameter. Eg: "pis ais:123456 cbpii"
     *
     * @param scopes Scopes
     * @return Consent ID
     */
    public static String getConsentIdFromScopesRequestParam(String[] scopes) {

        StringBuilder scopesString = new StringBuilder();
        for (String scope : scopes) {
            scopesString.append(scope).append(FinancialServicesConstants.SPACE_SEPARATOR);
        }

        return extractConsentIdFromRegex(scopesString.toString().trim());
    }

    /**
     * Method to validate the clientId against the SP.
     *
     * @param clientId  Client ID to be validated
     * @return boolean indicating whether the client ID is valid or not
     */
    @Generated(message = "Ignoring because OAuth2Util cannot be mocked with no constructors")
    public static boolean isValidClientId(String clientId) {

        if (StringUtils.isNotEmpty(clientId)) {
            Optional<ServiceProvider> serviceProvider;
            try {
                serviceProvider = Optional.ofNullable(OAuth2Util.getServiceProvider(clientId));
                if (!serviceProvider.isPresent()) {
                    return false;
                }
            } catch (IdentityOAuth2Exception e) {
                return false;
            }
        }
        return true;
    }
}
