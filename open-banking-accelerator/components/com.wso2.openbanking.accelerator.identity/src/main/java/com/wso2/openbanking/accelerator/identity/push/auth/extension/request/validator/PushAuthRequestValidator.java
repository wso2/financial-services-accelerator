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

package com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator;

import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.constants.PushAuthRequestConstants;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.exception.PushAuthRequestValidatorException;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.model.PushAuthErrorResponse;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.util.PushAuthRequestValidatorUtils;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.carbon.identity.oauth2.OAuth2Service;
import org.wso2.carbon.identity.oauth2.dto.OAuth2ClientValidationResponseDTO;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import static org.wso2.carbon.identity.openidconnect.model.Constants.JWT_PART_DELIMITER;
import static org.wso2.carbon.identity.openidconnect.model.Constants.NUMBER_OF_PARTS_IN_JWE;

/**
 * The extension class for enforcing OB Push Auth Request Validations.
 */
public class PushAuthRequestValidator {

    private static final Log log = LogFactory.getLog(PushAuthRequestValidator.class);
    private static PushAuthRequestValidator pushAuthRequestValidator;
    private static final String ERROR_DESCRIPTION = "error_description";
    private static final String ERROR = "error";

    public static PushAuthRequestValidator getPushAuthRequestValidator() {

        return pushAuthRequestValidator;
    }

    public static void setRegistrationValidator(PushAuthRequestValidator pushAuthRequestValidator) {

        PushAuthRequestValidator.pushAuthRequestValidator = pushAuthRequestValidator;
    }

    public final Map<String, Object> validateParams(HttpServletRequest request,
                                                    Map<String, List<String>> parameterMap)
            throws PushAuthRequestValidatorException {

        Map<String, Object> parameters = new HashMap<>();

        for (Map.Entry<String, List<String>> paramEntry : parameterMap.entrySet()) {
            if (paramEntry.getValue().size() > 1) {
                if (log.isDebugEnabled()) {
                    log.debug("Repeated param found:" + paramEntry.getKey());
                }
                throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                        PushAuthRequestConstants.INVALID_REQUEST, "Repeated parameter found in the request");
            }
            parameters.put(paramEntry.getKey(), paramEntry.getValue().get(0));
        }
        // push auth request cannot contain "request_uri" parameter
        if (parameters.containsKey(PushAuthRequestConstants.REQUEST_URI)) {
            log.error("Request does not allow request_uri parameter");
            throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                    PushAuthRequestConstants.INVALID_REQUEST, "Request does not allow request_uri parameter");
        }
        JSONObject requestBodyJson;
        JSONObject requestHeaderJson;
        String requestObjectString;

        // if "request" parameter is available, decode it and put it into parameter map
        if (parameters.containsKey(PushAuthRequestConstants.REQUEST)) {
            // validate form body when "request" parameter is present
            PushAuthRequestValidatorUtils.validateRequestFormBody(parameters);

            try {
                String requestParam = parameters.get(PushAuthRequestConstants.REQUEST).toString();
                // check whether request is of type JWE
                if (requestParam.split(JWT_PART_DELIMITER).length == NUMBER_OF_PARTS_IN_JWE) {
                    // decrypt JWE
                    requestObjectString = PushAuthRequestValidatorUtils.decrypt(requestParam,
                            parameters.get(PushAuthRequestConstants.CLIENT_ID) != null ?
                                    parameters.get(PushAuthRequestConstants.CLIENT_ID).toString() : null);
                } else {
                    requestObjectString = requestParam;
                }
                // decode jwt assuming it is a JWS otherwise, it will throw parse exception
                requestBodyJson = JWTUtils.decodeRequestJWT(requestObjectString, PushAuthRequestConstants.BODY);
                requestHeaderJson = JWTUtils.decodeRequestJWT(requestObjectString, PushAuthRequestConstants.HEADER);
                // add to parameters map
                parameters.put(PushAuthRequestConstants.DECODED_JWT_BODY, requestBodyJson);
                parameters.put(PushAuthRequestConstants.DECODED_JWT_HEADER, requestHeaderJson);

            } catch (ParseException e) {
                log.error("Exception while decoding JWT. Returning error.", e);
                throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                        PushAuthRequestConstants.INVALID_REQUEST_OBJECT,
                        "Unable to decode JWT.", e);
            }
            if (requestBodyJson != null && requestHeaderJson != null) {

                validateRedirectUri(requestBodyJson);

                // validate client id and redirect uri
                OAuth2ClientValidationResponseDTO oAuth2ClientValidationResponseDTO =
                        getClientValidationInfo(requestBodyJson);

                if (!oAuth2ClientValidationResponseDTO.isValidClient()) {
                    log.error(oAuth2ClientValidationResponseDTO.getErrorMsg());
                    throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                            PushAuthRequestConstants.INVALID_REQUEST,
                            oAuth2ClientValidationResponseDTO.getErrorMsg());
                }
                validateSignatureAlgorithm(requestHeaderJson
                        .get(PushAuthRequestConstants.ALG_HEADER));
                validateSignature(requestObjectString, requestBodyJson);
                validateResponseType(requestBodyJson);
                validateNonceParameter(requestBodyJson);
                validateScope(requestBodyJson);
                validateAudience(requestBodyJson);
                validateIssuer(requestBodyJson);
                validateExpirationTime(requestBodyJson);
                validateNotBeforeClaim(requestBodyJson);
                validatePKCEParameters(requestBodyJson);

                if (StringUtils.isNotBlank(requestBodyJson.getAsString(PushAuthRequestConstants.REQUEST))
                        || StringUtils.isNotBlank(requestBodyJson
                        .getAsString(PushAuthRequestConstants.REQUEST_URI))) {
                    log.error("Both request and request_uri parameters are not allowed in the request object");
                    throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                            PushAuthRequestConstants.INVALID_REQUEST_OBJECT,
                            "Both request and request_uri parameters are not allowed in the request object");
                }
            } else {
                log.error("Invalid JWT as request");
                throw new PushAuthRequestValidatorException(HttpStatus.SC_BAD_REQUEST,
                        PushAuthRequestConstants.INVALID_REQUEST, "Invalid JWT as request");
            }
        }

        // call additional validations from toolkit extensions
        validateAdditionalParams(parameters);
        return parameters;
    }

    /**
     * Extend this method to perform additional validations on toolkits.
     *
     * @param parameters parameter map
     */
    public void validateAdditionalParams(Map<String, Object> parameters) throws PushAuthRequestValidatorException {

    }

    /**
     * Extend this method to validate the redirect URI of the request object.
     * @param requestBodyJson request object
     * @throws PushAuthRequestValidatorException if validation fails
     */
    public void validateRedirectUri(JSONObject requestBodyJson)
            throws PushAuthRequestValidatorException {

        PushAuthRequestValidatorUtils.validateRedirectUri(requestBodyJson);
    }

    /**
     * Extend this method to validate scopes of the request object.
     * @param requestBodyJson request object
     * @throws PushAuthRequestValidatorException if validation fails
     */
    public void validateScope(JSONObject requestBodyJson)
            throws PushAuthRequestValidatorException {

        PushAuthRequestValidatorUtils.validateScope(requestBodyJson);
    }

    /**
     * Extend this method to validate signature algorithm of the request object.
     * @param algorithm signature algorithm
     * @throws PushAuthRequestValidatorException if validation fails
     */
    public void validateSignatureAlgorithm(Object algorithm)
            throws PushAuthRequestValidatorException {

        PushAuthRequestValidatorUtils.validateSignatureAlgorithm(algorithm);
    }

    /**
     * Extend this method to validate nonce parameter of the request object.
     * @param requestBodyJson request object
     * @throws PushAuthRequestValidatorException if validation fails
     */
    public void validateNonceParameter(JSONObject requestBodyJson)
            throws PushAuthRequestValidatorException {

        PushAuthRequestValidatorUtils.validateNonceParameter(requestBodyJson);
    }

    /**
     * Extend this method to validate issuer of the request object.
     * @param requestBodyJson request object
     * @throws PushAuthRequestValidatorException if validation fails
     */
    public void validateIssuer(JSONObject requestBodyJson)
            throws PushAuthRequestValidatorException {

        PushAuthRequestValidatorUtils.validateIssuer(requestBodyJson);
    }

    /**
     * Extend this method to validate expiration time of the request object.
     * @param requestBodyJson request object
     * @throws PushAuthRequestValidatorException if validation fails
     */
    public void validateExpirationTime(JSONObject requestBodyJson)
            throws PushAuthRequestValidatorException {

        PushAuthRequestValidatorUtils.validateExpirationTime(requestBodyJson);
    }

    /**
     * Extend this method to validate nbf claim of the request object.
     * @param requestBodyJson request object
     * @throws PushAuthRequestValidatorException if validation fails
     */
    public void validateNotBeforeClaim(JSONObject requestBodyJson)
            throws PushAuthRequestValidatorException {

        PushAuthRequestValidatorUtils.validateNotBeforeClaim(requestBodyJson);
    }

    /**
     * Extend this method to create error response on toolkits. Set necessary status codes and error payloads to
     * PushAuthErrorResponse.
     *
     * @param httpStatusCode Http status code
     * @param errorCode Error code
     * @param errorDescription Error description
     */
    public PushAuthErrorResponse createErrorResponse(int httpStatusCode, String errorCode, String errorDescription) {

        PushAuthErrorResponse pushAuthErrorResponse = new PushAuthErrorResponse();
        JSONObject errorResponse = new JSONObject();
        errorResponse.put(ERROR_DESCRIPTION, errorDescription);
        errorResponse.put(ERROR, errorCode);
        pushAuthErrorResponse.setPayload(errorResponse);
        pushAuthErrorResponse.setHttpStatusCode(httpStatusCode);

        return pushAuthErrorResponse;
    }

    @Generated(message = "Excluding from code coverage since it requires a service call")
    protected OAuth2ClientValidationResponseDTO getClientValidationInfo(JSONObject requestBodyJson) {

        return  new OAuth2Service().validateClientInfo(requestBodyJson.getAsString(PushAuthRequestConstants.CLIENT_ID),
                requestBodyJson.getAsString(PushAuthRequestConstants.REDIRECT_URI));
    }

    @Generated(message = "Excluding from code coverage since it requires a service call")
    protected void validateSignature(String requestObjectString, JSONObject requestBodyJson)
            throws PushAuthRequestValidatorException {

        PushAuthRequestValidatorUtils.validateSignature(requestObjectString, requestBodyJson);
    }

    @Generated(message = "Excluding from code coverage since it requires a service call")
    protected void validateAudience(JSONObject requestBodyJson)
            throws PushAuthRequestValidatorException {

        PushAuthRequestValidatorUtils.validateAudience(requestBodyJson);
    }

    protected void validatePKCEParameters(JSONObject requestBodyJson)
            throws PushAuthRequestValidatorException {

        PushAuthRequestValidatorUtils.validatePKCEParameters(requestBodyJson);
    }

    protected void validateResponseType(JSONObject requestBodyJson)
            throws PushAuthRequestValidatorException {

        PushAuthRequestValidatorUtils.validateResponseType(requestBodyJson);
    }
}
