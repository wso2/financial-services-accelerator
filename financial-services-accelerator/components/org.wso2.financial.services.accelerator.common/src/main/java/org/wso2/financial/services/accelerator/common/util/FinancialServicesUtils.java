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

import com.nimbusds.jose.JWSObject;
import net.minidev.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

/**
 * Open Banking common utility class.
 */
public class FinancialServicesUtils {

    private static final Log log = LogFactory.getLog(FinancialServicesUtils.class);

    /**
     * Get Tenant Domain String for the client id.
     * @param clientId the client id of the application
     * @return tenant domain of the client
     * @throws FinancialServicesException  if an error occurs while retrieving the tenant domain
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
     * @return new object instance
     */
    @Generated(message = "Ignoring since method contains no logics")
    public static Object getClassInstanceFromFQN(String classpath) {

        try {
            return Class.forName(classpath).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            log.error("Class not found: " + classpath.replaceAll("[\r\n]", ""));
            throw new FinancialServicesRuntimeException("Cannot find the defined class", e);
        } catch (InstantiationException | InvocationTargetException |
                NoSuchMethodException | IllegalAccessException e) {
            //Throwing a runtime exception since we cannot proceed with invalid objects
            throw new FinancialServicesRuntimeException("Defined class" + classpath + "cannot be instantiated.", e);
        }
    }

    /**
     * Extract software_environment (SANDBOX or PRODUCTION) from SSA.
     *
     * @param softwareStatement software statement (jwt) extracted from request payload
     * @return software_environment
     * @throws ParseException  if an error occurs while parsing the software statement
     */
    public static String getSoftwareEnvironmentFromSSA(String softwareStatement) throws ParseException {

        if (StringUtils.isEmpty(softwareStatement)) {
            return FinancialServicesConstants.PRODUCTION;
        }

        final JSONObject softwareStatementBody = JWTUtils.decodeRequestJWT(softwareStatement,
                FinancialServicesConstants.JWT_BODY);
        // Retrieve the SSA property name used for software environment identification
        final String sandboxEnvIdentificationPropertyName = FinancialServicesConfigParser.getInstance()
                .getSoftwareEnvIdentificationSSAPropertyName();
        // Retrieve the expected value for the sandbox environment
        final String sandboxEnvIdentificationValue = FinancialServicesConfigParser.getInstance()
                .getSoftwareEnvIdentificationSSAPropertyValueForSandbox();
        return sandboxEnvIdentificationValue.equals(softwareStatementBody
                .getAsString(sandboxEnvIdentificationPropertyName))
                ? FinancialServicesConstants.SANDBOX
                : FinancialServicesConstants.PRODUCTION;
    }

    /**
     * Method to reduce string length.
     *
     * @param input        Input for dispute data
     * @param maxLength    Max length for dispute data
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
     * Check whether the client ID belongs to a regulatory app.
     * @param clientId  client ID
     * @return true if the client ID belongs to a regulatory app
     * @throws RequestObjectException If an error occurs while checking the client ID
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public static boolean isRegulatoryApp(String clientId) throws RequestObjectException {

        try {
            return OAuth2Util.isFapiConformantApp(clientId);
        } catch (InvalidOAuthClientException e) {
            throw new RequestObjectException(OAuth2ErrorCodes.INVALID_CLIENT, "Could not find an existing app for " +
                    "clientId: " + clientId, e);
        } catch (IdentityOAuth2Exception e) {
            throw new RequestObjectException(OAuth2ErrorCodes.SERVER_ERROR, "Error while obtaining the service " +
                    "provider for clientId: " + clientId, e);
        }
    }

    /**
     * Decode request JWT.
     *
     * @param jwtToken jwt sent by the tpp
     * @param jwtPart  expected jwt part (header, body)
     * @return json object containing requested jwt part
     * @throws java.text.ParseException if an error occurs while parsing the jwt
     */
    public static JSONObject decodeRequestJWT(String jwtToken, String jwtPart) throws java.text.ParseException {

        JWSObject plainObject = JWSObject.parse(jwtToken);

        if ("head".equals(jwtPart)) {
            return plainObject.getHeader().toJSONObject();
        } else if ("body".equals(jwtPart)) {
            return plainObject.getPayload().toJSONObject();
        }
        return null;
    }
}
