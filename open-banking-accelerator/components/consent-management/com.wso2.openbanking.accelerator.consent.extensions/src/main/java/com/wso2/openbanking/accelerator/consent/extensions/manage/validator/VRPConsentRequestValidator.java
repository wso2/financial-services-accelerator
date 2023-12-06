/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package com.wso2.openbanking.accelerator.consent.extensions.manage.validator;
import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.util.ConsentManageUtil;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Iterator;

/**
 * Consent Manage validator class for Variable Recurring Payment Request Validation.
 */
public class VRPConsentRequestValidator {

    private static final Log log = LogFactory.getLog(VRPConsentRequestValidator.class);

    /**
     * Method to validate a variable recurring payment request.
     * This method performs validation on the variable recurring payment request.
     * It checks the validity of the initiation data body, the initiation payload, control parameters,
     * and ensures that the risk information is present. If any validation fails, the method returns a detailed
     * validation response indicating the error. If all validations pass, the returned validation response
     * indicates that the initiation request is valid.
     *
     * @param request The initiation object containing the variable recurring payment initiation request.
     * @return A validation response object indicating whether the initiation request is valid.
     */
    public static JSONObject validateVRPPayload(Object request) {

        JSONObject validationResponse = new JSONObject();
        validationResponse.put(ConsentExtensionConstants.IS_VALID, false);

        //Get the request payload from the ConsentManageData
        if (!(request instanceof JSONObject)) {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.INVALID_REQ_PAYLOAD);
        }
        JSONObject requestBody = (JSONObject) request;
       //Check request body is valid and not empty
        JSONObject dataValidationResult = ConsentManageUtil.validateInitiationDataBody(requestBody);

        if (!(boolean) dataValidationResult.get(ConsentExtensionConstants.IS_VALID)) {
            log.error(dataValidationResult.get(ConsentExtensionConstants.ERRORS));
            return dataValidationResult;
        }

        JSONObject data = (JSONObject) requestBody.get(ConsentExtensionConstants.DATA);

        //Validate initiation in the VRP payload
        if (data.containsKey(ConsentExtensionConstants.INITIATION)) {

            Object initiation = data.get(ConsentExtensionConstants.INITIATION);

            if (!isValidObject(initiation)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                        ErrorConstants.INVALID_PARAMETER,
                        ErrorConstants.PATH_INITIATION);
            }

            JSONObject initiationValidationResult = VRPConsentRequestValidator
                    .validateVRPInitiationPayload((JSONObject) data.get(ConsentExtensionConstants.INITIATION));

            if (!(boolean) initiationValidationResult.get(ConsentExtensionConstants.IS_VALID)) {
                log.error(initiationValidationResult.get(ConsentExtensionConstants.ERRORS));
                return initiationValidationResult;
            }
        } else {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.RESOURCE_INVALID_FORMAT,
                    ErrorConstants.PAYLOAD_FORMAT_ERROR, ErrorConstants.PATH_REQUEST_BODY);
        }

        //Validate the ControlParameter in the payload
        if (data.containsKey(ConsentExtensionConstants.CONTROL_PARAMETERS)) {

            Object controlParameters = data.get(ConsentExtensionConstants.CONTROL_PARAMETERS);

            if (!isValidObject(controlParameters)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                        ErrorConstants.INVALID_PARAMETER,
                        ErrorConstants.PATH_INITIATION);
            }

            JSONObject controlParameterValidationResult =
                    VRPConsentRequestValidator.validateControlParameters((JSONObject)
                            data.get(ConsentExtensionConstants.CONTROL_PARAMETERS));

            if (!(boolean) controlParameterValidationResult.get(ConsentExtensionConstants.IS_VALID)) {
                log.error(controlParameterValidationResult.get(ConsentExtensionConstants.ERRORS));
                return controlParameterValidationResult;
            }
        } else {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.RESOURCE_INVALID_FORMAT,
                    ErrorConstants.PAYLOAD_FORMAT_ERROR, ErrorConstants.PATH_REQUEST_BODY);
        }

        // check risk is mandatory
        if (!requestBody.containsKey(ConsentExtensionConstants.RISK) ||
                !(requestBody.get(ConsentExtensionConstants.RISK) instanceof JSONObject
                        || ((JSONObject) requestBody.get(ConsentExtensionConstants.DATA)).isEmpty())) {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.RESOURCE_INVALID_FORMAT,
                    ErrorConstants.PAYLOAD_FORMAT_ERROR, ErrorConstants.PATH_REQUEST_BODY);
        }

        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }

    /**
     * Checks if the given Object is a non-null and non-empty JSONObject.
     *
     * @param value The Object to be validated.
     * @return value
     */
    public static boolean isValidObject(Object value) {
        return value instanceof JSONObject && !((JSONObject) value).isEmpty();
    }


    /**
     * Checks if the given Object is a non-null and non-empty JSONObject.
     *
     * @param validToDateTime The Object to be validated.
     * @return value
     */
    public static boolean isValidDateTimeObject(Object validFromDateTime, Object validToDateTime) {
        return validFromDateTime instanceof JSONObject && !((JSONObject) validToDateTime).isEmpty() &&
                validFromDateTime instanceof JSONObject && !((JSONObject) validToDateTime).isEmpty();
    }



    /**
     * Method to validate control parameters for variable recurring payments.
     * This method performs comprehensive validation on the control parameters for variable recurring payments.
     * It checks the validity of maximum individual amount, requested execution date-time, and periodic limits.
     * If any validation fails, the method returns a detailed validation response indicating the error.
     * If all validations pass, the returned validation response indicates that the control parameters are valid.
     *
     * @param controlParameters The initiation object containing control parameters for variable recurring payments.
     * @return A validation response object indicating whether the control parameters are valid.
     */

    public static JSONObject validateControlParameters(JSONObject controlParameters) {
        JSONObject validationResponse = new JSONObject();

        //Validate Maximum individual amount in control parameters
        if (controlParameters.containsKey(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT)) {

            Object maximumIndividualAmount = controlParameters.get(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT);
            // Check if the control parameter is valid
            if (!isValidObject(maximumIndividualAmount)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR,
                        ErrorConstants.INVALID_PARAMETER,
                        ErrorConstants.PATH_MAXIMUM_INDIVIDUAL_AMOUNT);
            }

            Object amount = ((JSONObject) maximumIndividualAmount).getAsString(ConsentExtensionConstants.AMOUNT);

            Object currency = ((JSONObject) maximumIndividualAmount).getAsString(ConsentExtensionConstants.CURRENCY);

                // check whether the amount key is present in the payload and its not null
                if (!ConsentManageUtil.validateAmount((JSONObject)
                        maximumIndividualAmount)) {
                    return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR,
                            ErrorConstants.INVALID_AMOUNT,
                            ErrorConstants.PATH_MAXIMUM_INDIVIDUAL_AMOUNT_AMOUNT);
                }

                if (StringUtils.isEmpty((String) amount)) {
                    return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR,
                            ErrorConstants.MAXIMUM_INDIVIDUAL_AMOUNT_IS_MISSING,
                            ErrorConstants.PATH_MAXIMUM_INDIVIDUAL_AMOUNT_AMOUNT);
                }

            // check whether the Currency key is present in the payload and its not null
            if (!ConsentManageUtil.validateCurrency((JSONObject)
                    maximumIndividualAmount)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR,
                        ErrorConstants.INVALID_CURRENCY,
                        ErrorConstants.PATH_MAXIMUM_INDIVIDUAL_AMOUNT_CURRENCY);
            }

            if (StringUtils.isEmpty((String) currency)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR,
                        ErrorConstants.MAXIMUM_INDIVIDUAL_AMOUNT_CURRENCY_IS_MISSING,
                        ErrorConstants.PATH_MAXIMUM_INDIVIDUAL_AMOUNT);
            }
            } else {
                log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.INVALID_REQ_PAYLOAD);
            }

        // Validate ValidToDateTime and ValidFromDateTime
        JSONObject periodicLimits = validateParameterDateTime(controlParameters);
        if (!(boolean) periodicLimits.get(ConsentExtensionConstants.IS_VALID)) {
            log.error(periodicLimits.get(ConsentExtensionConstants.ERRORS));
            return periodicLimits;
        }

        // Validate Periodic Limits
        JSONObject periodicLimitsValidationResult = validatePeriodicLimits(controlParameters);
        if (!(boolean) periodicLimitsValidationResult.get(ConsentExtensionConstants.IS_VALID)) {
            log.error(periodicLimitsValidationResult.get(ConsentExtensionConstants.ERRORS));
            return periodicLimitsValidationResult;
        }

        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }

    /**
     * Checks whether the given object is a valid JSONArray.
     * This method verifies if the provided object is not null and is an instance of JSONArray.
     * It is commonly used to validate whether a given object represents a valid JSON array.
     *
     * @param value The object to be checked for being a valid JSONArray.
     */
    public static boolean isValidJSONArray(Object value) {
        return value instanceof JSONArray;
    }


    /**
     * Checks whether the given string represents a valid date-time in ISO-8601 format.
     *
     * This method uses the ISO_DATE_TIME formatter to parse the provided date-time string.
     * It returns true if the parsing is successful, indicating that the string is in the correct
     * ISO-8601 date-time format. Otherwise, it returns false.
     *
     * @param dateTimeString The string to be checked for ISO-8601 date-time format validity.
     * @return
     */
    public static boolean isValidISODateTimeFormat(String dateTimeString) {

        final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ISO_DATE_TIME;

        try {
            dateTimeFormat.parse(dateTimeString);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Method to validate variable recurring payment periodic limits.
     * This method validates the periodic limits specified in the control parameters for variable recurring payments.
     * It checks if the provided JSON array of periodic limits is valid and then iterates through each limit
     * to ensure that required fields such as amount, currency, period alignment, and period type are present and
     * meet the specified criteria.
     *
     * @param controlParameters Initiation Object containing periodic limits
     * @return validation response object indicating whether the provided periodic limits are valid
     */
    public static JSONObject validatePeriodicLimits(JSONObject controlParameters) {
        JSONObject validationResponse = new JSONObject();

        // Retrieve the periodic limits from the control parameters
        Object periodicLimit = controlParameters.get(ConsentExtensionConstants.PERIODIC_LIMITS);

        // Check if the control parameter is a valid JSON array
        if (!isValidJSONArray(periodicLimit)) {
            return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR,
                    ErrorConstants.INVALID_PARAMETER_PERIODIC_LIMITS,
                    ErrorConstants.PATH_PERIOD_LIMIT);
        }

        // Check if the periodic limits key is present
        if (controlParameters.containsKey(ConsentExtensionConstants.PERIODIC_LIMITS)) {

            // Retrieve the JSON array of periodic limits
            JSONArray periodicLimits = (JSONArray) controlParameters.get(ConsentExtensionConstants.PERIODIC_LIMITS);

            // Iterate through each periodic limit
            Iterator<Object> periodicLimitIterator = periodicLimits.iterator();

            while (periodicLimitIterator.hasNext()) {
                JSONObject limit = (JSONObject) periodicLimitIterator.next();

                // Retrieve values for validation
                Object amount = limit.getAsString(ConsentExtensionConstants.AMOUNT);
                Object currency = limit.getAsString(ConsentExtensionConstants.CURRENCY);
                Object periodType = limit.getAsString(ConsentExtensionConstants.PERIOD_TYPE);

                // validate amount
                if (!ConsentManageUtil.validateAmount(limit)) {
                    return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                            ErrorConstants.INVALID_AMOUNT,
                            ErrorConstants.PATH_PERIOD_LIMIT_AMOUNT);
                }

                if (StringUtils.isEmpty((String) amount)) {
                    return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                            ErrorConstants.MAXIMUM_INDIVIDUAL_AMOUNT_IS_MISSING,
                            ErrorConstants.PATH_PERIOD_LIMIT_AMOUNT);
                }

                // validate currency
                if (!ConsentManageUtil.validateCurrency(limit)) {
                    return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                            ErrorConstants.INVALID_CURRENCY,
                            ErrorConstants.PATH_PERIOD_LIMIT_CURRENCY);
                }

                //validate currency is empty
                if (StringUtils.isEmpty((String) currency)) {
                    return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                            ErrorConstants.MAXIMUM_INDIVIDUAL_AMOUNT_CURRENCY_IS_MISSING,
                            ErrorConstants.PATH_PERIOD_LIMIT_CURRENCY);
                }

                //validate period alignment
                if (ConsentManageUtil.validatePeriodicAlignment(limit)) {
                    return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                            ErrorConstants.INVALID_PERIOD_ALIGNMENT, ErrorConstants.PATH_PERIOD_ALIGNMENT);
                }

                //validate period type
                if (!ConsentManageUtil.validatePeriodicType(limit)) {
                    return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                            ErrorConstants.MISSING_PERIOD_TYPE, ErrorConstants.PATH_PERIOD_TYPE);
                }

                if (StringUtils.isEmpty((String) periodType)) {
                    return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                            ErrorConstants.MISSING_PERIOD_TYPE, ErrorConstants.PATH_PERIOD_TYPE);
                    }
                }
            // If all validations pass, set the overall validation status to true
                validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
            } else {
            // If periodic limits key is missing, return an error
                log.error(ErrorConstants.MISSING_PERIOD_LIMITS);
                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                        ErrorConstants.MISSING_PERIOD_LIMITS,
                        ErrorConstants.PATH_PERIOD_TYPE);
            }
            return validationResponse;
        }

    public static JSONObject validateParameterDateTime(JSONObject controlParameters) {
        JSONObject validationResponse = new JSONObject();

        // Retrieve the validDateTime from the control parameters
        Object validateFromDateTime = controlParameters.get(ConsentExtensionConstants.VALID_FROM_DATE_TIME);
        Object validateToDateTime = controlParameters.get(ConsentExtensionConstants.VALID_TO_DATE_TIME);

        if (!isValidDateTimeObject(validateFromDateTime, validateToDateTime)) {
            return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR,
                    ErrorConstants.INVALID_PARAMETER,
                    ErrorConstants.PATH_VALID_TO_DATE);
        }

        if (controlParameters.containsKey(ConsentExtensionConstants.VALID_TO_DATE_TIME)
                && controlParameters.containsKey(ConsentExtensionConstants.VALID_FROM_DATE_TIME)) {

            String validTo = controlParameters.getAsString(ConsentExtensionConstants.VALID_TO_DATE_TIME);
            String validFrom = controlParameters.getAsString(ConsentExtensionConstants.VALID_FROM_DATE_TIME);

            // Validate ISO datetime format for validTo
            if (!isValidISODateTimeFormat(validTo)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                        ErrorConstants.INVALID_DATE_TIME_FORMAT, ErrorConstants.PATH_VALID_TO_DATE);
            }

            // Validate ISO datetime format for validFrom
            if (!isValidISODateTimeFormat(validFrom)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                        ErrorConstants.INVALID_DATE_TIME_FORMAT, ErrorConstants.PATH_VALID_TO_DATE);
            }

            OffsetDateTime validToDateTime = OffsetDateTime.parse(validTo);
            OffsetDateTime validFromDateTime = OffsetDateTime.parse(validFrom);
            OffsetDateTime currentDateTime = OffsetDateTime.now(validToDateTime.getOffset());

            //If the ValidToDAte is older than current date OR ValidToDAte is older than ValidFromDAte, return error
            if (currentDateTime.isAfter(validToDateTime) || validFromDateTime.isAfter(validToDateTime)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                        ErrorConstants.INVALID_VALID_TO_DATE, ErrorConstants.PATH_VALID_TO_DATE);
            }
        }

        return validationResponse;
    }

    /**
     * Validator class to validate the payload of a variable recurring payment initiation.
     * This method performs validation on the initiation payload for a variable recurring payment.
     * It checks and validates the debtor account and creditor account information if present in the payload.
     * If any validation fails, it returns a JSON object with details about the validation error.
     * If the initiation payload passes all validations, the returned JSON object indicates a valid initiation.
     *
     * @param initiation The JSON object representing the variable recurring payment initiation payload.
     * @return validationResponse
     */
    public static JSONObject validateVRPInitiationPayload(JSONObject initiation) {

        JSONObject validationResponse = new JSONObject();

        //Validate DebtorAccount
        if (initiation.containsKey(ConsentExtensionConstants.DEBTOR_ACC)) {

            Object debtorAccount = initiation.get(ConsentExtensionConstants.DEBTOR_ACC);

            if (debtorAccount instanceof JSONObject) {
                JSONObject validationResult = ConsentManageUtil.validateDebtorAccount((JSONObject) debtorAccount);

                if (!(boolean) validationResult.get(ConsentExtensionConstants.IS_VALID)) {
                    log.error(validationResult.get(ConsentExtensionConstants.ERRORS));
                    return validationResult;
                }
            }
        }
        //Validate CreditorAccount
        if (initiation.containsKey(ConsentExtensionConstants.CREDITOR_ACC)) {

            Object creditorAccount = initiation.get(ConsentExtensionConstants.CREDITOR_ACC);
            if (creditorAccount instanceof JSONObject) {
                JSONObject validationResult = ConsentManageUtil.validateCreditorAccount((JSONObject) creditorAccount);

                if (!(boolean) validationResult.get(ConsentExtensionConstants.IS_VALID)) {
                    log.error(validationResult.get(ConsentExtensionConstants.ERRORS));
                    return validationResult;
                }
            }
        }
        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }
}

