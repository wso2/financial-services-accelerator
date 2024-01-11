/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.wso2.openbanking.accelerator.consent.extensions.manage.validator;

import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.util.ConsentManageUtil;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
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
     * It checks the validity of the  data body, the initiation payload, control parameters,
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
            return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                    ErrorConstants.PAYLOAD_FORMAT_ERROR, ErrorConstants.PATH_REQUEST_BODY);
        }

        JSONObject requestBody = (JSONObject) request;
        //Check request body is valid and not empty
        JSONObject dataValidationResult = ConsentManageUtil.validateInitiationDataBody(requestBody);

        if (!(Boolean.parseBoolean(dataValidationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
            // log.error(dataValidationResult.get(ConsentExtensionConstants.ERRORS));
            return dataValidationResult;
        }

        //Check consent  initiation is valid and not empty
        JSONObject initiationValidationResult = VRPConsentRequestValidator.validateConsentInitiation(requestBody);

        if (!(Boolean.parseBoolean(initiationValidationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
            //log.error(initiationValidationResult.get(ConsentExtensionConstants.ERRORS));
            return initiationValidationResult;
        }

        JSONObject controlParameterValidationResult = VRPConsentRequestValidator.
                validateConsentControlParameters(requestBody);

        if (!(Boolean.parseBoolean(controlParameterValidationResult.
                getAsString(ConsentExtensionConstants.IS_VALID)))) {
            // log.error(controlParameterValidationResult.get(ConsentExtensionConstants.ERRORS));
            return controlParameterValidationResult;
        }

        JSONObject riskValidationResult = VRPConsentRequestValidator.validateConsentRisk(requestBody);

        if (!(Boolean.parseBoolean(riskValidationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
            //log.error(riskValidationResult.get(ConsentExtensionConstants.ERRORS));
            return riskValidationResult;
        }

        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }


    /**
     * Method to validate control parameters for variable recurring payments.
     * This method performs  validation on the control parameters for variable recurring payments.
     * It checks the validity of maximum individual amount, requested execution date-time, and periodic limits.
     * If any validation fails, the method returns a detailed validation response indicating the error.
     * If all validations pass, the returned validation response indicates that the control parameters are valid.
     *
     * @param controlParameters The initiation object containing control parameters for variable recurring payments.
     * @return A validation response object indicating whether the control parameters are valid.
     */
    public static JSONObject validateControlParameters(JSONObject controlParameters) {
        JSONObject validationResponse = new JSONObject();

        JSONObject maximumIndividualAmountResult = validateMaximumIndividualAmount(controlParameters);
        if (!(Boolean.parseBoolean(maximumIndividualAmountResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
            return maximumIndividualAmountResult;
        }

        JSONObject maximumIndividualAmountCurrencyResult = validateMaximumIndividualAmountCurrency(controlParameters);
        if (!(Boolean.parseBoolean(maximumIndividualAmountCurrencyResult.
                getAsString(ConsentExtensionConstants.IS_VALID)))) {
            return maximumIndividualAmountCurrencyResult;
        }

        JSONObject parameterDateTimeValidationResponse = validateParameterDateTime(controlParameters);
        if (!(Boolean.parseBoolean(parameterDateTimeValidationResponse.
                getAsString(ConsentExtensionConstants.IS_VALID)))) {
            return parameterDateTimeValidationResponse;
        }

//        JSONObject parameterDateTimeValidationResponses = validateParameterDateTimes(controlParameters);
//        if (!(Boolean.parseBoolean(parameterDateTimeValidationResponses.
//                getAsString(ConsentExtensionConstants.IS_VALID)))) {
//            return parameterDateTimeValidationResponses;
//        }

        // Validate Periodic Limits
        JSONObject periodicLimitsValidationResult = validatePeriodicLimits(controlParameters);
        if (!(Boolean.parseBoolean(periodicLimitsValidationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
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
     * Validates the Maximum Individual Amount in the control parameters of a consent request.
     *
     * @param controlParameters The JSON object representing the control parameters of the consent request.
     * @return A JSON object containing the validation response.
     */
    public static JSONObject validateMaximumIndividualAmount(JSONObject controlParameters) {

        JSONObject validationResponse = new JSONObject();

        //Validate Maximum individual amount in control parameters
        if (controlParameters.containsKey(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT)) {

            Object maximumIndividualAmount = controlParameters.
                    get(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT);

            String errorMessage = String.format(ErrorConstants.INVALID_PARAMETER_MESSAGE,
                    "maximum individual amount", "JSONObject");

            // Check if the control parameter is valid
            if (!isValidJSONObject(maximumIndividualAmount)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.
                                PAYLOAD_FORMAT_ERROR_MAXIMUM_INDIVIDUAL_AMOUNT,
                        errorMessage,
                        ErrorConstants.PATH_MAXIMUM_INDIVIDUAL_AMOUNT);
            }

            JSONObject maximumIndividualAmountResult = validateJsonObjectKey((JSONObject) maximumIndividualAmount,
                    ConsentExtensionConstants.AMOUNT);
            if (!(Boolean.parseBoolean(maximumIndividualAmountResult.
                    getAsString(ConsentExtensionConstants.IS_VALID)))) {
                // log.error(maximumIndividualAmountResult.get(ConsentExtensionConstants.ERRORS));
                return maximumIndividualAmountResult;
            }
        } else {
            log.error(ErrorConstants.MISSING_MAXIMUM_INDIVIDUAL_AMOUNT);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.RESOURCE_INVALID_FORMAT,
                    ErrorConstants.MISSING_MAXIMUM_INDIVIDUAL_AMOUNT, ErrorConstants.PATH_REQUEST_BODY);

        }
        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }

    /**
     * Validates the Currency in Maximum Individual Amount in the control parameters of a consent request.
     *
     * @param controlParameters The JSON object representing the control parameters of the consent request.
     * @return A JSON object containing the validation response.
     */
    public static JSONObject validateMaximumIndividualAmountCurrency(JSONObject controlParameters) {
        JSONObject validationResponse = new JSONObject();

        Object maximumIndividualAmount = controlParameters.
                get(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT);


        JSONObject maximumIndividualAmountResult = validateJsonObjectKey((JSONObject) maximumIndividualAmount,
                ConsentExtensionConstants.CURRENCY);
        if (!(Boolean.parseBoolean(maximumIndividualAmountResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
            // log.error(maximumIndividualAmountResult.get(ConsentExtensionConstants.ERRORS));
            return maximumIndividualAmountResult;
        }
        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
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

        // Check if the periodic limits key is present
        if (controlParameters.containsKey(ConsentExtensionConstants.PERIODIC_LIMITS)) {

            // Retrieve the periodic limits from the control parameters
            Object periodicLimit = controlParameters.get(ConsentExtensionConstants.PERIODIC_LIMITS);

            String errorMessage = String.format(ErrorConstants.INVALID_PARAMETER_MESSAGE, "periodic limit",
                    "JSONObject");

            // Check if the control parameter is a valid JSON array
            if (!isValidJSONArray(periodicLimit)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR_PERIODIC_LIMITS,
                        ErrorConstants.INVALID_PARAMETER_PERIODIC_LIMITS,
                        ErrorConstants.PATH_PERIOD_LIMIT);
            }

            JSONArray periodicLimits = (JSONArray) controlParameters.get(ConsentExtensionConstants.PERIODIC_LIMITS);
            Iterator it = periodicLimits.iterator();

            while (it.hasNext()) {
                JSONObject limit = (JSONObject) it.next();

                JSONObject amount = validateAmountPeriodicLimit(controlParameters);
                if (!(Boolean.parseBoolean(amount.
                        getAsString(ConsentExtensionConstants.IS_VALID)))) {
                    return amount;
                }

                JSONObject currency = validateCurrencyPeriodicLimit(controlParameters);
                if (!(Boolean.parseBoolean(currency.
                        getAsString(ConsentExtensionConstants.IS_VALID)))) {
                    return currency;
                }

                JSONObject validationResults = validatePeriodAlignment(limit);
                if (!(Boolean.parseBoolean(validationResults.
                        getAsString(ConsentExtensionConstants.IS_VALID)))) {
                    return validationResults;
                }

                JSONObject periodType = validatePeriodType(limit);
                if (!(Boolean.parseBoolean(periodType.
                        getAsString(ConsentExtensionConstants.IS_VALID)))) {
                    return periodType;
                }
            }
        } else {
            // If periodic limits key is missing, return an error
            log.error(ErrorConstants.MISSING_PERIOD_LIMITS);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                    ErrorConstants.MISSING_PERIOD_LIMITS,
                    ErrorConstants.PATH_PERIOD_TYPE);
        }
        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }

    /**
     * Validates the Currency in periodic limits in the control parameters of a consent request.
     *
     * @param controlParameters The JSON object representing the control parameters of the consent request.
     * @return A JSON object containing the validation response.
     */
    public static JSONObject validateCurrencyPeriodicLimit(JSONObject controlParameters) {

        JSONObject validationResponse = new JSONObject();

        JSONArray periodicLimits = (JSONArray) controlParameters.get(ConsentExtensionConstants.PERIODIC_LIMITS);
        JSONObject currency = validateAmountCurrencyPeriodicLimits((JSONArray) periodicLimits,
                ConsentExtensionConstants.CURRENCY);
        if (!(Boolean.parseBoolean(currency.
                getAsString(ConsentExtensionConstants.IS_VALID)))) {
            return currency;
        }
        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }


    /**
     * Validates the Amount in periodic limits in the control parameters of a consent request.
     *
     * @param controlParameters The JSON object representing the control parameters of the consent request.
     * @return A JSON object containing the validation response.
     */
    public static JSONObject validateAmountPeriodicLimit(JSONObject controlParameters) {

        JSONObject validationResponse = new JSONObject();

        JSONArray periodicLimits = (JSONArray) controlParameters.get(ConsentExtensionConstants.PERIODIC_LIMITS);

        JSONObject amount = validateAmountCurrencyPeriodicLimits((JSONArray) periodicLimits,
                ConsentExtensionConstants.AMOUNT);
        if (!(Boolean.parseBoolean(amount.
                getAsString(ConsentExtensionConstants.IS_VALID)))) {
            return amount;
        }
        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }


    /**
     * Validates the date-time parameters in the control parameters of a consent request.
     *
     * @param controlParameters The JSON object representing the control parameters of the consent request.
     * @return A JSON object containing the validation response. If the date-time parameters are valid,
     * it sets the "IS_VALID" field to true; otherwise, it contains an error response.
     */

    public static JSONObject validateParameterDateTime(JSONObject controlParameters) {
        JSONObject validationResponse = new JSONObject();

        // Check if ValidToDateTime and ValidFromDateTime keys are present
        if (controlParameters.containsKey(ConsentExtensionConstants.VALID_TO_DATE_TIME)
                && controlParameters.containsKey(ConsentExtensionConstants.VALID_FROM_DATE_TIME)) {

            // Get and validate ValidToDateTime
            Object validateToDateTime = controlParameters.get(ConsentExtensionConstants.VALID_TO_DATE_TIME);
            JSONObject toDateTimeValidation = isValidDateTimeObject(validateToDateTime);
            if (!(Boolean.parseBoolean(toDateTimeValidation.getAsString(ConsentExtensionConstants.IS_VALID)))) {
                return toDateTimeValidation;
            }

            // Get and validate ValidFromDateTime
            Object validateFromDateTime = controlParameters.get(ConsentExtensionConstants.VALID_FROM_DATE_TIME);
            JSONObject fromDateTimeValidation = isValidDateTimeObject(validateFromDateTime);
            if (!(Boolean.parseBoolean(fromDateTimeValidation.getAsString(ConsentExtensionConstants.IS_VALID)))) {
                return fromDateTimeValidation;
            }

            String validTo = controlParameters.getAsString(ConsentExtensionConstants.VALID_TO_DATE_TIME);
            String validFrom = controlParameters.getAsString(ConsentExtensionConstants.VALID_FROM_DATE_TIME);

            OffsetDateTime validToDateTime = OffsetDateTime.parse(validTo);
            OffsetDateTime validFromDateTime = OffsetDateTime.parse(validFrom);
            OffsetDateTime currentDateTime = OffsetDateTime.now(validToDateTime.getOffset());

            // If ValidToDateTime is older than current date OR ValidToDateTime is
            // older than ValidFromDateTime, return error
            if (!validFromDateTime.isBefore(currentDateTime) || !currentDateTime.isBefore(validToDateTime)) {
                log.error(String.format("Invalid date-time range, " +
                                "validToDateTime: %s, validFromDateTime: %s, currentDateTime: %s",
                        validToDateTime, validFromDateTime, currentDateTime));
                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                        ErrorConstants.INVALID_REQ_PAYLOAD, ErrorConstants.PATH_VALID_TO_DATE);
            }
        } else {
            // If ValidToDateTime or ValidFromDateTime keys are not present, return error
            return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID,
                    ErrorConstants.MISSING_DATE_TIME, ErrorConstants.PATH_VALID_TO_DATE);
        }

        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }

//    public static JSONObject validateParameterDateTime(JSONObject controlParameters) {
//        JSONObject validationResponse = new JSONObject();
//
//        if (controlParameters.containsKey(ConsentExtensionConstants.VALID_TO_DATE_TIME)) {
//
//            Object validateToDateTime = controlParameters.get(ConsentExtensionConstants.VALID_TO_DATE_TIME);
//            JSONObject date = isValidDateTimeObject(validateToDateTime);
//            if (!(Boolean.parseBoolean(date.getAsString(ConsentExtensionConstants.IS_VALID)))) {
//                // log.error(currency.get(ConsentExtensionConstants.ERRORS));
//                return date;
//            }
//
//            String validTo = controlParameters.getAsString(ConsentExtensionConstants.VALID_TO_DATE_TIME);
//            String validFrom = controlParameters.getAsString(ConsentExtensionConstants.VALID_FROM_DATE_TIME);
//
//            OffsetDateTime validToDateTime = OffsetDateTime.parse(validTo);
//            OffsetDateTime validFromDateTime = OffsetDateTime.parse(validFrom);
//            OffsetDateTime currentDateTime = OffsetDateTime.now(validToDateTime.getOffset());
//
//            //If the ValidToDAte is older than current date OR ValidToDAte is older than ValidFromDAte, return error
//            if (!validFromDateTime.isBefore(currentDateTime) || !currentDateTime.isBefore(validToDateTime)) {
//                 log.error(String.format("Invalid date-time range, " +
//                    "validToDateTime: %s, validFromDateTime: %s, currentDateTime: %s",
//                 validToDateTime, validFromDateTime, currentDateTime));
//                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
//                        ErrorConstants.INVALID_REQ_PAYLOAD, ErrorConstants.PATH_VALID_TO_DATE);
//            }
//        } else {
//            return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID,
//                    ErrorConstants.INVALID_VALID_TO_DATE, ErrorConstants.PATH_VALID_TO_DATE);
//        }
//
//        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
//        return validationResponse;
//    }

//    public static JSONObject validateParameterDateTimes(JSONObject controlParameters) {
//        JSONObject validationResponse = new JSONObject();
//
//        if (controlParameters.containsKey(ConsentExtensionConstants.VALID_FROM_DATE_TIME)) {
//
//            Object validateFromDateTime = controlParameters.get(ConsentExtensionConstants.VALID_FROM_DATE_TIME);
//            JSONObject date = isValidDateTimeObject(validateFromDateTime);
//            if (!(Boolean.parseBoolean(date.getAsString(ConsentExtensionConstants.IS_VALID)))) {
//                return date;
//            }
//
//            String validTo = controlParameters.getAsString(ConsentExtensionConstants.VALID_TO_DATE_TIME);
//            String validFrom = controlParameters.getAsString(ConsentExtensionConstants.VALID_FROM_DATE_TIME);
//
//            OffsetDateTime validToDateTime = OffsetDateTime.parse(validTo);
//            OffsetDateTime validFromDateTime = OffsetDateTime.parse(validFrom);
//            OffsetDateTime currentDateTime = OffsetDateTime.now(validToDateTime.getOffset());
//
//            //If the ValidToDAte is older than current date OR ValidToDAte is older than ValidFromDAte, return error
//            if (!validFromDateTime.isBefore(currentDateTime) || !currentDateTime.isBefore(validToDateTime)) {
//                 log.error(String.format("Invalid date-time range, " +
//                    "validToDateTime: %s, validFromDateTime: %s, currentDateTime: %s",
//                 validToDateTime, validFromDateTime, currentDateTime));
//                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
//                        ErrorConstants.INVALID_REQ_PAYLOAD, ErrorConstants.PATH_VALID_FROM_DATE);
//            }
//        } else {
//            return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID,
//                    ErrorConstants.INVALID_VALID_TO_DATE, ErrorConstants.PATH_VALID_FROM_DATE);
//        }
//
//        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
//        return validationResponse;
//    }


//    public static JSONObject validateParameterDateTime(JSONObject controlParameters) {
//        JSONObject validationResponse = new JSONObject();
//
//        if (controlParameters.containsKey(ConsentExtensionConstants.VALID_TO_DATE_TIME)) {
//            Object validateToDateTime = controlParameters.get(ConsentExtensionConstants.VALID_TO_DATE_TIME);
//
////            JSONObject dateValidation = isValidDateTimeObject(validateToDateTime);
////
////            if (!(Boolean.parseBoolean(dateValidation.getAsString(ConsentExtensionConstants.IS_VALID)))) {
////                return dateValidation;
////            }
//
//            OffsetDateTime validToDateTime = OffsetDateTime.parse(controlParameters.
//                    getAsString(ConsentExtensionConstants.VALID_TO_DATE_TIME));
//            OffsetDateTime validFromDateTime = OffsetDateTime.parse(controlParameters.
//                    getAsString(ConsentExtensionConstants.VALID_FROM_DATE_TIME));
//            OffsetDateTime currentDateTime = OffsetDateTime.now(validToDateTime.getOffset());
//
//
//            if (!validFromDateTime.isBefore(currentDateTime) || !currentDateTime.isBefore(validToDateTime)) {
//                log.error(String.format("Invalid date-time range, " +
//                                "validToDateTime: %s, validFromDateTime: %s, currentDateTime: %s",
//                        validToDateTime, validFromDateTime, currentDateTime));
//                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
//                        ErrorConstants.INVALID_REQ_PAYLOAD, ErrorConstants.PATH_VALID_TO_DATE);
//            }
//        } else {
//            return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
//                    ErrorConstants.INVALID_REQ_PAYLOAD, ErrorConstants.PATH_VALID_TO_DATE);
//        }
//
//        if (controlParameters.containsKey(ConsentExtensionConstants.VALID_FROM_DATE_TIME)) {
//            Object validateFromDateTime = controlParameters.get(ConsentExtensionConstants.VALID_FROM_DATE_TIME);
//            JSONObject dateValidation = isValidDateTimeObject(validateFromDateTime);
//
//            if (!(Boolean.parseBoolean(dateValidation.getAsString(ConsentExtensionConstants.IS_VALID)))) {
//                return dateValidation;
//            }
//
//            OffsetDateTime validToDateTime = OffsetDateTime.parse(controlParameters.
//                    getAsString(ConsentExtensionConstants.VALID_TO_DATE_TIME));
//            OffsetDateTime validFromDateTime = OffsetDateTime.parse(controlParameters.
//                    getAsString(ConsentExtensionConstants.VALID_FROM_DATE_TIME));
//            OffsetDateTime currentDateTime = OffsetDateTime.now(validToDateTime.getOffset());
//
//            if (!validFromDateTime.isBefore(currentDateTime) || !currentDateTime.isBefore(validToDateTime)) {
//                log.debug("Invalid date-time range.");
//                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
//                        ErrorConstants.INVALID_PARAMETER, ErrorConstants.PATH_VALID_FROM_DATE);
//            }
//        } else {
//            return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
//                    ErrorConstants.INVALID_REQ_PAYLOAD, ErrorConstants.PATH_VALID_TO_DATE);
//        }
//
//        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
//        return validationResponse;
//    }


//    public static JSONObject validateParameterDateTime(JSONObject controlParameters) {
//        JSONObject validationResponse = new JSONObject();
//
//        if (controlParameters.containsKey(ConsentExtensionConstants.VALID_TO_DATE_TIME)) {
//
//            // Retrieve the validDateTime from the control parameters
//            Object validateToDateTime = controlParameters.get(ConsentExtensionConstants.VALID_TO_DATE_TIME);
//
////            if (!isValidDateTimeObject(validateToDateTime)) {
////                return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR_VALID_TO_DATE,
////                        ErrorConstants.INVALID_DATE_TIME_FORMAT,
////                        ErrorConstants.PATH_VALID_TO_DATE);
////            }
//
//            JSONObject date = isValidDateTimeObject(validateToDateTime);
//            if (!(Boolean.parseBoolean(date.
//                    getAsString(ConsentExtensionConstants.IS_VALID)))) {
//                // log.error(currency.get(ConsentExtensionConstants.ERRORS));
//                return date;
//            }
//
//            OffsetDateTime validToDateTime = OffsetDateTime.parse(controlParameters.
//                    getAsString(ConsentExtensionConstants.VALID_TO_DATE_TIME));
//            OffsetDateTime validFromDateTime = OffsetDateTime.parse(controlParameters.
//                    getAsString(ConsentExtensionConstants.VALID_FROM_DATE_TIME));
//            OffsetDateTime currentDateTime = OffsetDateTime.now(validToDateTime.getOffset());
//
//            //If the ValidToDAte is older than current date OR currentDate is older than ValidFromDAte, return error
//            if (!validFromDateTime.isBefore(currentDateTime) || !currentDateTime.isBefore(validToDateTime)) {
//                log.error(String.format("Invalid date-time range, validToDateTime : %s , validFromDateTime : %s " +
//                        "and currentDateTime : %s ", validToDateTime, validFromDateTime, currentDateTime));
//                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
//                        ErrorConstants.INVALID_VALID_TO_DATE, ErrorConstants.PATH_VALID_TO_DATE);
//            }
//        }
//
//        if (controlParameters.containsKey(ConsentExtensionConstants.VALID_FROM_DATE_TIME)) {
//
//            // Retrieve the validDateTime from the control parameters
//            Object validateFromDateTime = controlParameters.get(ConsentExtensionConstants.VALID_FROM_DATE_TIME);
//
////            if (!isValidDateTimeObject(validateFromDateTime)) {
////                return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR_VALID_FROM_DATE,
////                        ErrorConstants.INVALID_DATE_TIME_FORMAT,
////                        ErrorConstants.PATH_VALID_TO_DATE);
////            }
//            JSONObject date = isValidDateTimeObject(validateFromDateTime);
//            if (!(Boolean.parseBoolean(date.
//                    getAsString(ConsentExtensionConstants.IS_VALID)))) {
//                // log.error(currency.get(ConsentExtensionConstants.ERRORS));
//                return date;
//            }
//
//            OffsetDateTime validToDateTime = OffsetDateTime.parse(controlParameters.
//                    getAsString(ConsentExtensionConstants.VALID_TO_DATE_TIME));
//            OffsetDateTime validFromDateTime = OffsetDateTime.parse(controlParameters.
//                    getAsString(ConsentExtensionConstants.VALID_FROM_DATE_TIME));
//            OffsetDateTime currentDateTime = OffsetDateTime.now(validToDateTime.getOffset());
//
//            //If the ValidToDAte is older than current date OR currentDate is older than ValidFromDAte, return error
//            if (!validFromDateTime.isBefore(currentDateTime) || !currentDateTime.isBefore(validToDateTime)) {
//                log.debug("Invalid date-time range.");
//                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
//                        ErrorConstants.INVALID_VALID_TO_DATE, ErrorConstants.PATH_VALID_FROM_DATE);
//            }
//        }
//        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
//        return validationResponse;
//    }

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

            String errorMessage = String.format(ErrorConstants.INVALID_PARAMETER_MESSAGE,
                    "debtor account", "JSONObject");

            if (!isValidJSONObject(debtorAccount)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR_DEBTOR_ACC,
                        errorMessage,
                        ErrorConstants.PATH_DEBTOR_ACCOUNT);
            }

            JSONObject validationResult = ConsentManageUtil.validateDebtorAccount((JSONObject) debtorAccount);

            if (!(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
                log.error(validationResult.get(ConsentExtensionConstants.ERRORS));
                return validationResult;
            }

        } else {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR_DEBTOR_ACC);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.RESOURCE_INVALID_FORMAT,
                    ErrorConstants.PAYLOAD_FORMAT_ERROR_DEBTOR_ACC, ErrorConstants.PATH_REQUEST_BODY);
        }

        //Validate CreditorAccount
        if (initiation.containsKey(ConsentExtensionConstants.CREDITOR_ACC)) {

            Object creditorAccount = initiation.get(ConsentExtensionConstants.CREDITOR_ACC);

            String errorMessage = String.format(ErrorConstants.INVALID_PARAMETER_MESSAGE,
                    "creditor account", "JSONObject");

            if (!isValidJSONObject(creditorAccount)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR_CREDITOR_ACC,
                        errorMessage,
                        ErrorConstants.PATH_CREDIT_ACCOUNT);
            }

            JSONObject validationResult = ConsentManageUtil.validateCreditorAccount((JSONObject) creditorAccount);

            if (!(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
                log.error(validationResult.get(ConsentExtensionConstants.ERRORS));
                return validationResult;
            }

        } else {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR_CREDITOR_ACC);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.RESOURCE_INVALID_FORMAT,
                    ErrorConstants.PAYLOAD_FORMAT_ERROR, ErrorConstants.PATH_REQUEST_BODY);
        }

        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }

    /**
     * Validates the presence of a specified key in a JSONObject (either the amount or the currency)
     * and checks if the associated value is a non-empty string.
     *
     * @param parentObj The JSONObject to be validated.
     * @param key       The key to be checked for presence in the parentObj.
     * @return true if the specified key is present in the parentObj and the associated value is a
     * non-empty string.
     */
    public static JSONObject validateJsonObjectKey(JSONObject parentObj, String key) {
        JSONObject validationResponse = new JSONObject();

        if (parentObj != null) {
            // Check if the specified key is present in the parentObj
            if (parentObj.containsKey(key)) {
                Object value = parentObj.get(key);

                // Check if the value associated with the key is a non-empty string
                if (value instanceof String && !((String) value).isEmpty()) {
                    validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
                    return validationResponse; // Valid: The key is present, and the value is a non-empty String
                } else {
                    String errorMessage = "The value associated is not a string or the value is empty'" + key + "'";
                    return ConsentManageUtil.getValidationResponse(
                            ErrorConstants.INVALID_PARAMETER,
                            errorMessage,
                            ErrorConstants.PATH_REQUEST_BODY
                    );
                    // Invalid: The value associated with the key is not a non-empty String
                }
            } else {
                String errorMessage = "Mandatory parameter '" + key + "' is not present in payload";
                return ConsentManageUtil.getValidationResponse(
                        ErrorConstants.INVALID_PARAMETER,
                        errorMessage,
                        ErrorConstants.PATH_REQUEST_BODY
                );
                // Invalid: The specified key is not present in parentObj
            }
        }

        String errorMessage = "parameter passed in  is null";
        return ConsentManageUtil.getValidationResponse(
                ErrorConstants.INVALID_PARAMETER,
                errorMessage,
                ErrorConstants.PATH_REQUEST_BODY
        );
    }

    /**
     * Validates the presence of a specified key in a JSONArray (either the amount or the currency)
     * in periodiclimits and checks if the associated value is a non-empty string.
     *
     * @param parentArray The JSONObject to be validated.
     * @param key         The key to be checked for presence in the parentObj.
     * @return true if the specified key is present in the parentObj and the associated value is a
     * non-empty string.
     */

    public static JSONObject validateAmountCurrencyPeriodicLimits(JSONArray parentArray, String key) {
        if (parentArray != null && key != null) {
            // Check if the specified key is present in the parentArray
            for (Object obj : parentArray) {
                if (obj instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) obj;

                    if (jsonObject.containsKey(key)) {
                        Object value = jsonObject.get(key);

                        // Check if the value associated with the key is a non-empty string
                        if (value instanceof String && !((String) value).isEmpty()) {
                            JSONObject validationResponse = new JSONObject();
                            validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
                            return validationResponse; // Valid: The key is present, and the value is a non-empty String
                        } else {
                            String errorMessage = "Mandatory parameter '" + key + "' is not present in periodic" +
                                    " limits or the value is not a string";
                            return ConsentManageUtil.getValidationResponse(
                                    ErrorConstants.RESOURCE_INVALID_FORMAT,
                                    errorMessage,
                                    ErrorConstants.PATH_REQUEST_BODY
                            );
                            // Invalid: The value associated with the key is not a non-empty String
                        }
                    }
                }
            }
        }
        String errorMessage = "Mandatory parameter '" + key + "' of periodic limits is not present in payload";
        return ConsentManageUtil.getValidationResponse(
                ErrorConstants.RESOURCE_INVALID_FORMAT,
                errorMessage,
                ErrorConstants.PATH_REQUEST_BODY
        );

    }

    /**
     * Validates the consent initiation payload in the VRP request.
     *
     * @param request The JSONObject representing the VRP request.
     * @return A JSONObject containing the validation response.
     */
    public static JSONObject validateConsentInitiation(JSONObject request) {

        JSONObject validationResponse = new JSONObject();

        JSONObject requestBody = (JSONObject) request;
        JSONObject data = (JSONObject) requestBody.get(ConsentExtensionConstants.DATA);

        //Validate initiation in the VRP payload
        if (data.containsKey(ConsentExtensionConstants.INITIATION)) {

            Object initiation = data.get(ConsentExtensionConstants.INITIATION);

            String errorMessage = String.format(ErrorConstants.INVALID_PARAMETER_MESSAGE,
                    "initiation", "JSONObject");

            if (!isValidJSONObject(initiation)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD_INITIATION,
                        errorMessage, ErrorConstants.PATH_INITIATION);
            }

            JSONObject initiationValidationResult = VRPConsentRequestValidator
                    .validateVRPInitiationPayload((JSONObject) initiation);

            if (!(Boolean.parseBoolean(initiationValidationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
                // log.error(initiationValidationResult.get(ConsentExtensionConstants.ERRORS));
                return initiationValidationResult;
            }
        } else {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR_INITIATION);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.RESOURCE_INVALID_FORMAT,
                    ErrorConstants.PAYLOAD_FORMAT_ERROR_INITIATION, ErrorConstants.PATH_REQUEST_BODY);
        }

        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }


    /**
     * Validates the consent control parameters in the VRP request payload.
     *
     * @param request The JSONObject representing the VRP request.
     * @return A JSONObject containing the validation response.
     */
    public static JSONObject validateConsentControlParameters(JSONObject request) {

        JSONObject validationResponse = new JSONObject();

        JSONObject requestBody = (JSONObject) request;
        JSONObject data = (JSONObject) requestBody.get(ConsentExtensionConstants.DATA);

        //Validate the ControlParameter in the payload
        if (data.containsKey(ConsentExtensionConstants.CONTROL_PARAMETERS)) {

            Object controlParameters = data.get(ConsentExtensionConstants.CONTROL_PARAMETERS);

            String errorMessage = String.format(ErrorConstants.INVALID_PARAMETER_MESSAGE,
                    "control parameters", "JSONObject");

            if (!isValidJSONObject(controlParameters)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD_CONTROL_PARAMETERS,
                        errorMessage,
                        ErrorConstants.PATH_CONTROL_PARAMETERS);
            }

            JSONObject controlParameterValidationResult =
                    VRPConsentRequestValidator.validateControlParameters((JSONObject)
                            data.get(ConsentExtensionConstants.CONTROL_PARAMETERS));

            if (!(Boolean.parseBoolean(controlParameterValidationResult.
                    getAsString(ConsentExtensionConstants.IS_VALID)))) {
                //log.error(controlParameterValidationResult.get(ConsentExtensionConstants.ERRORS));
                return controlParameterValidationResult;
            }
        } else {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR_CONTROL_PARAMETER);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.RESOURCE_INVALID_FORMAT,
                    ErrorConstants.PAYLOAD_FORMAT_ERROR_CONTROL_PARAMETER, ErrorConstants.PATH_REQUEST_BODY);
        }
        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }


    /**
     * Validates the  risk information in the VRP request payload.
     *
     * @param request The JSONObject representing the VRP request.
     * @return A JSONObject containing the validation response.
     */
    public static JSONObject validateConsentRisk(JSONObject request) {

        JSONObject validationResponse = new JSONObject();

        JSONObject requestBody = (JSONObject) request;
        JSONObject data = (JSONObject) requestBody.get(ConsentExtensionConstants.DATA);

        // Check Risk key is mandatory
        if (!requestBody.containsKey(ConsentExtensionConstants.RISK) ||
                !(requestBody.get(ConsentExtensionConstants.RISK) instanceof JSONObject
                        || ((JSONObject) requestBody.get(ConsentExtensionConstants.DATA)).isEmpty())) {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR_RISK);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.RESOURCE_INVALID_FORMAT,
                    ErrorConstants.PAYLOAD_FORMAT_ERROR_RISK, ErrorConstants.PATH_RISK);
        }
        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }

    /**
     * Validates the  periodic alignment in the VRP request payload.
     *
     * @param limit The JSONObject representing the VRP request.
     * @return A JSONObject containing the validation response.
     */
    public static JSONObject validatePeriodAlignment(JSONObject limit) {
        JSONObject validationResponse = new JSONObject();

        if (limit.containsKey(ConsentExtensionConstants.PERIOD_ALIGNMENT)) {
            Object periodAlignmentObj = limit.get(ConsentExtensionConstants.PERIOD_ALIGNMENT);

            if (periodAlignmentObj instanceof String && !((String) periodAlignmentObj).isEmpty()) {
                String periodAlignment = (String) periodAlignmentObj;

                if (ConsentExtensionConstants.CONSENT.equals(periodAlignment) ||
                        ConsentExtensionConstants.CALENDAR.equals(periodAlignment)) {

                    validationResponse.put("isValid", true);
                    validationResponse.put("periodAlignment", periodAlignment);
                } else {
                    return ConsentManageUtil.getValidationResponse(
                            ErrorConstants.PAYLOAD_FORMAT_ERROR_PERIODIC_LIMITS,
                            ErrorConstants.INVALID_PERIOD_ALIGNMENT,
                            ErrorConstants.PATH_PERIOD_TYPE
                    );
                }
            } else {
                return ConsentManageUtil.getValidationResponse(
                        ErrorConstants.PAYLOAD_FORMAT_ERROR_PERIODIC_LIMITS,
                        ErrorConstants.PAYLOAD_FORMAT_ERROR_PERIODIC_LIMITS_ALIGNMENT,
                        ErrorConstants.PATH_PERIOD_TYPE
                );
            }
        } else {
            return ConsentManageUtil.getValidationResponse(
                    ErrorConstants.PAYLOAD_FORMAT_ERROR_PERIODIC_LIMITS,
                    ErrorConstants.MISSING_PERIOD_ALIGNMENT,
                    ErrorConstants.PATH_PERIOD_TYPE
            );
        }
        return validationResponse;
    }

    /**
     * Validates the  periodic type in the VRP request payload.
     *
     * @param limit The JSONObject representing the VRP request.
     * @return A JSONObject containing the validation response.
     */
    public static JSONObject validatePeriodType(JSONObject limit) {
        JSONObject validationResponse = new JSONObject();

        if (limit.containsKey(ConsentExtensionConstants.PERIOD_TYPE)) {
            Object periodTypeObj = limit.get(ConsentExtensionConstants.PERIOD_TYPE);

            if (periodTypeObj instanceof String && !((String) periodTypeObj).isEmpty()) {
                String periodType = (String) periodTypeObj;

                if (ConsentExtensionConstants.DAY.equals(periodType) ||
                        ConsentExtensionConstants.WEEK.equals(periodType) ||
                        ConsentExtensionConstants.FORTNIGHT.equals(periodType) ||
                        ConsentExtensionConstants.MONTH.equals(periodType) ||
                        ConsentExtensionConstants.HALF_YEAR.equals(periodType) ||
                        ConsentExtensionConstants.YEAR.equals(periodType)) {

                    validationResponse.put("isValid", true);
                    validationResponse.put("periodAlignment", periodType);
                } else {
                    return ConsentManageUtil.getValidationResponse(
                            ErrorConstants.PAYLOAD_FORMAT_ERROR_PERIODIC_LIMITS,
                            ErrorConstants.INVALID_PERIOD_TYPE,
                            ErrorConstants.PATH_PERIOD_TYPE
                    );
                }
            } else {
                return ConsentManageUtil.getValidationResponse(
                        ErrorConstants.PAYLOAD_FORMAT_ERROR_PERIODIC_LIMITS,
                        ErrorConstants.PAYLOAD_FORMAT_ERROR_PERIODIC_LIMITS_PERIOD_TYPE,
                        ErrorConstants.PATH_PERIOD_TYPE
                );
            }
        } else {
            return ConsentManageUtil.getValidationResponse(
                    ErrorConstants.PAYLOAD_FORMAT_ERROR_PERIODIC_LIMITS,
                    ErrorConstants.MISSING_PERIOD_TYPE,
                    ErrorConstants.PATH_PERIOD_TYPE
            );
        }
        return validationResponse;
    }

    /**
     * Checks if the given Object is a JSONObject and the JSONObject is non-empty .
     *
     * @param value The Object to be validated.
     * @return true if the object is a non-null and non-empty JSONObject.
     */
    public static boolean isValidJSONObject(Object value) {
        return value instanceof JSONObject && !((JSONObject) value).isEmpty();
    }

    /**
     * Checks if the given object is a valid date-time string and it is non empty.
     *
     * @param value The object to be checked for a valid date-time format.
     * @return True if the object is a non-empty string in ISO date-time format, false otherwise.
     */
    private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ISO_DATE_TIME;

    public static JSONObject isValidDateTimeObject(Object value) {
        JSONObject validationResponse = new JSONObject();

        if (value instanceof String && !((String) value).isEmpty()) {
            try {
                String dateTimeString = (String) value;
                dateTimeFormat.parse(dateTimeString);
            } catch (DateTimeParseException e) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                        ErrorConstants.INVALID_DATE_TIME_FORMAT, ErrorConstants.PATH_VALID_TO_DATE);
            }
        } else {
            return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                    ErrorConstants.MISSING_DATE_TIME_FORMAT, ErrorConstants.PATH_VALID_TO_DATE);
        }

        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }

}
