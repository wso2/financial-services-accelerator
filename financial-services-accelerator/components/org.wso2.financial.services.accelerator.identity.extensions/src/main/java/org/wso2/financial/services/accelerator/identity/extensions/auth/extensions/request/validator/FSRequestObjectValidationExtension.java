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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;
import org.wso2.carbon.identity.openidconnect.RequestObjectValidatorImpl;
import org.wso2.carbon.identity.openidconnect.model.RequestObject;
import org.wso2.financial.services.accelerator.common.constant.ErrorConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.common.validator.FinancialServicesValidator;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.FSRequestObject;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.ValidationResponse;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityServiceExtensionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * The extension of RequestObjectValidatorImpl to enforce Financial services specific validations of the
 * request object.
 */
@SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS", "REPLACE_STR_LINE_TERMINATORS"},
        justification = "Log messages are sanitized for CRLF injection.")
public class FSRequestObjectValidationExtension extends RequestObjectValidatorImpl {

    private static final Log log = LogFactory.getLog(FSRequestObjectValidationExtension.class);
    // Get extension impl
    static FSRequestObjectValidator fsDefaultRequestObjectValidator =
            IdentityExtensionsDataHolder.getInstance().getObRequestObjectValidator();
    static FinancialServicesValidator fsValidator = FinancialServicesValidator.getInstance();

    /**
     * Validates the request object and throws an exception if invalid.
     *
     * @param initialRequestObject The initial request object to be validated.
     * @param oAuth2Parameters     The OAuth2 parameters associated with the request.
     * @return                true if the request object is valid, false otherwise.
     * @throws RequestObjectException
     */
    @Override
    @Generated(message = "Ignoring since main logics in util methods are tested")
    public boolean validateRequestObject(RequestObject initialRequestObject, OAuth2Parameters oAuth2Parameters)
            throws RequestObjectException {

        try {
            if (IdentityCommonUtils.isRegulatoryApp(oAuth2Parameters.getClientId())) {

                FSRequestObject fsRequestObject = new FSRequestObject(initialRequestObject);

                // Not required now but letting it be there to support older implementations
                // or can use it if required in the future
                Map<String, Object> dataMap = new HashMap<>();

                // Perform FS default validations
                validate(defaultValidateRequestObject(fsRequestObject));

                if (ServiceExtensionUtils.isInvokeExternalService(
                            ServiceExtensionTypeEnum.VALIDATE_AUTHORIZATION_REQUEST)) {
                    // Perform FS customized validations with service extension
                    validate(IdentityServiceExtensionUtils.validateRequestObjectWithServiceExtension(fsRequestObject));
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
     * Validates the response and throws an exception if invalid.
     */
    void validate(ValidationResponse response) throws RequestObjectException {

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
     * @return A ValidationResponse indicating whether the request is valid.
     */
    ValidationResponse defaultValidateRequestObject(FSRequestObject fsRequestObject) {

        String fsRequestObjectViolation = fsValidator.getFirstViolation(fsRequestObject);

        if (StringUtils.isEmpty(fsRequestObjectViolation)) {
            return new ValidationResponse(true);
        } else {
            return new ValidationResponse(false, fsRequestObjectViolation);
        }
    }
}
