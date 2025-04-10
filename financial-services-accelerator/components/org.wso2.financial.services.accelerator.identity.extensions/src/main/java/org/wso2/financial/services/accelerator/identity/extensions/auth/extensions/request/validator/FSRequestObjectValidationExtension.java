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

package org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator;

import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;
import org.wso2.carbon.identity.openidconnect.RequestObjectValidatorImpl;
import org.wso2.carbon.identity.openidconnect.model.RequestObject;
import org.wso2.financial.services.accelerator.common.constant.ErrorConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.common.validator.FinancialServicesValidator;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.FSRequestObject;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.ValidationResponse;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The extension of RequestObjectValidatorImpl to enforce Open Banking specific validations of the
 * request object.
 */
public class FSRequestObjectValidationExtension extends RequestObjectValidatorImpl {

    private static final Log log = LogFactory.getLog(FSRequestObjectValidationExtension.class);
    // Get extension impl
    static FSRequestObjectValidator fsDefaultRequestObjectValidator =
            IdentityExtensionsDataHolder.getInstance().getObRequestObjectValidator();

    /**
     * Validations related to clientId, response type, exp, redirect URL, mandatory params,
     * issuer, audience are done. Called after signature validation.
     *
     * @param initialRequestObject request object
     * @param oAuth2Parameters     oAuth2Parameters
     * @throws RequestObjectException - RequestObjectException
     */
    @Override
    public boolean validateRequestObject(RequestObject initialRequestObject, OAuth2Parameters oAuth2Parameters)
            throws RequestObjectException {

        try {
            if (isRegulatory(oAuth2Parameters)) {

                FSRequestObject fsRequestObject = new FSRequestObject(initialRequestObject);
                Map<String, Object> dataMap = prepareDataMap(oAuth2Parameters);

                // Perform FS default validations
                validate(defaultValidateRequestObject(fsRequestObject, dataMap));

                if (ServiceExtensionUtils.isInvokeExternalService(
                            ServiceExtensionTypeEnum.VALIDATE_AUTHORIZATION_REQUEST)) {
                    // Perform FS customized validations with service extension
                    validate(validateRequestObjectWithServiceExtension(fsRequestObject, dataMap));
                } else if (fsDefaultRequestObjectValidator != null) {
                    // Perform FS customized validations
                    validate(fsDefaultRequestObjectValidator.validateRequestObject(fsRequestObject, dataMap));
                }
            }
            return validateIAMConstraints(initialRequestObject, oAuth2Parameters);

        } catch (RequestObjectException e) {
            log.error("Error while retrieving regulatory property from sp metadata", e);
            throw new RequestObjectException(RequestObjectException.ERROR_CODE_INVALID_REQUEST, e.getErrorMessage());
        } catch (FinancialServicesException e) {
            log.error(ErrorConstants.EXTERNAL_SERVICE_DEFAULT_ERROR, e);
            throw new RequestObjectException(ErrorConstants.EXTERNAL_SERVICE_DEFAULT_ERROR, e.getMessage());
        }
    }

    /**
     * Validate IAM related logic.
     * @param requestObject        request object
     * @param oAuth2Parameters     oAuth2Parameters
     * @return boolean            true if valid request object
     * @throws RequestObjectException - RequestObjectException
     */
    @Generated(message = "super methods cannot be mocked")
    boolean validateIAMConstraints(RequestObject requestObject,
                                   OAuth2Parameters oAuth2Parameters) throws RequestObjectException {

        return super.validateRequestObject(requestObject, oAuth2Parameters);
    }


    /**
     * Called by validateRequestObject.
     *
     * @param requestObject      request object
     * @param oAuth2Parameters   oAuth2Parameters
     * @return                true if valid audience
     */
    @Generated(message = "Empty method")
    @Override
    protected boolean isValidAudience(RequestObject requestObject, OAuth2Parameters oAuth2Parameters) {

        // converted to validation layer
        return true;
    }

    /**
     * Called by validateRequestObject.
     *
     * @param oAuth2Parameters
     * @return
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    protected String getAllowedScopes(OAuth2Parameters oAuth2Parameters) throws RequestObjectException {

        try {
             String scopesFromSP = IdentityCommonUtils.getAppPropertyFromSPMetaData(oAuth2Parameters.getClientId(),
                     IdentityCommonConstants.SCOPE);
             if (StringUtils.isNotBlank(scopesFromSP)) {
                 return scopesFromSP;
             } else {
                    return "accounts payments fundsconfirmations";
             }
        } catch (FinancialServicesException e) {
            log.error("Error while retrieving scopes property from sp metadata", e);
            throw new RequestObjectException(e.getMessage(), e);
        }
    }

    /**
     * Get regulatory property from sp metadata.
     *
     * @param oAuth2Parameters oAuthParameters
     * @return
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    protected boolean isRegulatory(OAuth2Parameters oAuth2Parameters) throws RequestObjectException {

        return FinancialServicesUtils.isRegulatoryApp(oAuth2Parameters.getClientId());
    }

    /**
     * Prepares a data map containing allowed scopes.
     */
    private Map<String, Object> prepareDataMap(OAuth2Parameters oAuth2Parameters) throws RequestObjectException {

        Map<String, Object> dataMap = new HashMap<>();
        String allowedScopes = getAllowedScopes(oAuth2Parameters);
        if (StringUtils.isNotBlank(allowedScopes)) {
            dataMap.put(IdentityCommonConstants.SCOPE, Arrays.asList(allowedScopes.split(" ")));
        }
        return dataMap;
    }

    /**
     * Validates the response and throws an exception if invalid.
     */
    private void validate(ValidationResponse response) throws RequestObjectException {

        if (!response.isValid()) {
            String sanitizedMessage = response.getViolationMessage().replaceAll("[\r\n]+", " ");
            log.error("Request object validation failed: " + sanitizedMessage);
            throw new RequestObjectException(RequestObjectException.ERROR_CODE_INVALID_REQUEST, sanitizedMessage);
        }
    }

    /**
     * Performs accelerator layer default validations.
     *
     * @param fsRequestObject The request object to be validated.
     * @param dataMap         Additional data required for validation, such as scope-related information.
     * @return A ValidationResponse indicating whether the request is valid.
     */
    private ValidationResponse defaultValidateRequestObject(FSRequestObject fsRequestObject,
                                                            Map<String, Object> dataMap) {

        String fsRequestObjectViolation = FinancialServicesValidator.getInstance().getFirstViolation(fsRequestObject);

        if (StringUtils.isEmpty(fsRequestObjectViolation)) {
            try {
                if (!isClientIdAndScopePresent(fsRequestObject)) {
                    return new ValidationResponse(false, "Client Id and scope must be" +
                            " included in the request object.");
                }
                String scopeViolation = validateScope(fsRequestObject, dataMap);
                if (StringUtils.isEmpty(scopeViolation)) {
                    return new ValidationResponse(true);
                } else {
                    return new ValidationResponse(false, scopeViolation);
                }
            } catch (RequestObjectException e) {
                return new ValidationResponse(false, e.getMessage());
            }
        } else {
            return new ValidationResponse(false, fsRequestObjectViolation);
        }
    }

    private boolean isClientIdAndScopePresent(FSRequestObject fsRequestObject) throws RequestObjectException {
        JSONObject jsonObject = fsRequestObject.getSignedJWT().getPayload().toJSONObject();
        final String clientId = jsonObject.getAsString(IdentityCommonConstants.CLIENT_ID);
        String scope = jsonObject.getAsString(IdentityCommonConstants.SCOPE);
        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(scope)) {
            log.error("Client id or scope cannot be empty");
            throw new RequestObjectException("Client id or scope cannot be empty");
        }
        return true;
    }

    /**
     * Validates scope.
     *
     * @param fsRequestObject
     * @param dataMap
     * @return
     */
    private String validateScope(FSRequestObject fsRequestObject, Map<String, Object> dataMap) {

        try {
            //remove scope claim
            JWTClaimsSet claimsSet = fsRequestObject.getClaimsSet();
            JSONObject claimsSetJsonObject = claimsSet.toJSONObject();
            if (claimsSetJsonObject.containsKey(IdentityCommonConstants.SCOPE)) {
                String scopeClaimString = claimsSetJsonObject.remove(IdentityCommonConstants.SCOPE).toString();
                List allowedScopes = (List) dataMap.get(IdentityCommonConstants.SCOPE);
                List<String> requestedScopes = new ArrayList<>(Arrays.asList(scopeClaimString.split(" ")));
                StringBuilder stringBuilder = new StringBuilder();

                // iterate through requested scopes and remove if not allowed
                for (String scope : requestedScopes) {
                    if (IdentityCommonConstants.OPENID_SCOPE.equals(scope)) {
                        stringBuilder.append(scope).append(" ");
                        log.debug("Adding Openid scope to the request object");
                    }
                    if (allowedScopes.contains(scope)) {
                        stringBuilder.append(scope).append(" ");
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Adding allowed scope %s to the request object",
                                    scope.replaceAll("[\r\n]", "")));
                        }
                    }
                }
                String modifiedScopeString = stringBuilder.toString().trim();
                // throw an error if no valid scopes found or only one scope is found
                if (StringUtils.isBlank(modifiedScopeString) || modifiedScopeString.split(" ").length <= 1) {
                    throw new RequestObjectException("No valid scopes found in the request");
                }
                claimsSetJsonObject.put(IdentityCommonConstants.SCOPE, modifiedScopeString);
                //Set claims set to request object
                JWTClaimsSet validatedClaimsSet = JWTClaimsSet.parse(claimsSetJsonObject);
                fsRequestObject.setClaimSet(validatedClaimsSet);
                log.debug("Successfully set the modified claims-set to the request object");
            }
        } catch (ParseException | RequestObjectException e) {
            log.error("Error while validating scope of request object", e);
            return e.getMessage();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Validates the given FSRequestObject using a service extension.
     *
     * @param fsRequestObject The request object to be validated.
     * @param dataMap         Additional data required for validation, such as scope-related information.
     * @return A ValidationResponse indicating whether the request is valid.
     */
    private ValidationResponse validateRequestObjectWithServiceExtension(FSRequestObject fsRequestObject,
                                                                         Map<String, Object> dataMap)
            throws FinancialServicesException {

        // Construct the payload
        org.json.JSONObject data = new org.json.JSONObject(fsRequestObject.getClaimsSet().toJSONObject());

        ExternalServiceRequest externalServiceRequest = new ExternalServiceRequest(UUID.randomUUID().toString(), data);

        // Invoke external service
        ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(externalServiceRequest,
                ServiceExtensionTypeEnum.VALIDATE_AUTHORIZATION_REQUEST);

        try {
            IdentityCommonUtils.serviceExtensionActionStatusValidation(response);
            return new ValidationResponse(true);
        } catch (IdentityOAuth2Exception e) {
            String errorDescription = e.getMessage();
            log.error(errorDescription.replaceAll("[\r\n]", ""));
            return new ValidationResponse(false, errorDescription);
        }
    }
}
