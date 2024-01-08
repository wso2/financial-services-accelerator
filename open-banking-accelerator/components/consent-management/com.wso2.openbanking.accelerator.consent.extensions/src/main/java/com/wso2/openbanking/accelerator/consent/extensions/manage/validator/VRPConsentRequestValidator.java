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
import java.util.Arrays;
import java.util.List;

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
            log.error(dataValidationResult.get(ConsentExtensionConstants.ERRORS));
            return dataValidationResult;
        }

        //Check consent  initiation is valid and not empty
        JSONObject initiationValidationResult = VRPConsentRequestValidator.validateConsentInitiation(requestBody);

        if (!(Boolean.parseBoolean(initiationValidationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
            log.error(initiationValidationResult.get(ConsentExtensionConstants.ERRORS));
            return initiationValidationResult;
        }

        JSONObject controlParameterValidationResult = VRPConsentRequestValidator.
                validateConsentControlParameters(requestBody);

        if (!(Boolean.parseBoolean(controlParameterValidationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
            log.error(controlParameterValidationResult.get(ConsentExtensionConstants.ERRORS));
            return controlParameterValidationResult;
        }

        JSONObject riskValidationResult = VRPConsentRequestValidator.validateConsentRisk(requestBody);

        if (!(Boolean.parseBoolean(riskValidationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
            log.error(riskValidationResult.get(ConsentExtensionConstants.ERRORS));
            return riskValidationResult;
        }

        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
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
    public static boolean isValidDateTimeObject(Object value) {

        final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ISO_DATE_TIME;

        if (value instanceof String && !((String) value).isEmpty()) {
            try {
                dateTimeFormat.parse((String) value);
                return true;
            } catch (DateTimeParseException e) {
                log.error("Invalid date-time format: %d", e);
                return false;
            }
        } else {
            log.debug("date-time not a string or it's value is empty");
            return false;
        }
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
        JSONObject controlParameterValidationResponse = new JSONObject();

        JSONObject maximumIndividualAmountResult = validateMaximumIndividualAmount(controlParameters);
        if (!(Boolean.parseBoolean(maximumIndividualAmountResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
            log.error(maximumIndividualAmountResult.get(ConsentExtensionConstants.ERRORS));
            return maximumIndividualAmountResult;
        }

        JSONObject maximumIndividualAmountResult2 = validateMaximumIndividualAmountCurrency(controlParameters);
        if (!(Boolean.parseBoolean(maximumIndividualAmountResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
            log.error(maximumIndividualAmountResult.get(ConsentExtensionConstants.ERRORS));
            return maximumIndividualAmountResult;
        }

        JSONObject parameterDateTimeValidationResponse = validateParameterDateTime(controlParameters);
        if (!(Boolean.parseBoolean(parameterDateTimeValidationResponse.
                getAsString(ConsentExtensionConstants.IS_VALID)))) {
            log.error(parameterDateTimeValidationResponse.get(ConsentExtensionConstants.ERRORS));
            return parameterDateTimeValidationResponse;
        }

        // Validate Periodic Limits
        JSONObject periodicLimitsValidationResult = validatePeriodicLimits(controlParameters);
        if (!(Boolean.parseBoolean(periodicLimitsValidationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
            log.error(ErrorConstants.PAYLOAD_INVALID);
            return periodicLimitsValidationResult;
        }

        controlParameterValidationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return controlParameterValidationResponse;
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

            if (!validateJsonObjectKey((JSONObject) maximumIndividualAmount, ConsentExtensionConstants.AMOUNT)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.
                                PAYLOAD_FORMAT_ERROR_MAXIMUM_INDIVIDUAL_AMOUNT,
                        ErrorConstants.MAXIMUM_INDIVIDUAL_AMOUNT_IS_MISSING,
                        ErrorConstants.PATH_MAXIMUM_INDIVIDUAL_AMOUNT);
            }

        } else {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR_MAXIMUM_INDIVIDUAL_AMOUNT);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.RESOURCE_INVALID_FORMAT,
                    ErrorConstants.PAYLOAD_FORMAT_ERROR_MAXIMUM_INDIVIDUAL_AMOUNT, ErrorConstants.PATH_REQUEST_BODY);

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

        if (!validateJsonObjectKey((JSONObject) maximumIndividualAmount, ConsentExtensionConstants.CURRENCY)) {
            return ConsentManageUtil.getValidationResponse(ErrorConstants.
                            PAYLOAD_FORMAT_ERROR_MAXIMUM_INDIVIDUAL_AMOUNT,
                    ErrorConstants.MAXIMUM_INDIVIDUAL_AMOUNT_CURRENCY_IS_MISSING,
                    ErrorConstants.PATH_MAXIMUM_INDIVIDUAL_AMOUNT_CURRENCY);
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

            // Retrieve the JSON array of periodic limits
           // JSONArray periodicLimits = (JSONArray) controlParameters.get(ConsentExtensionConstants.PERIODIC_LIMITS);

            // Iterate through each periodic limit
           // Iterator<Object> periodicLimitIterator = periodicLimits.iterator();

         //   while (periodicLimitIterator.hasNext()) {
               // JSONObject limit = (JSONObject) periodicLimitIterator.next();

                JSONObject periodicLimitType = validateAmountCurrencyPeriodicLimit
                        (controlParameters);
                if (!(Boolean.parseBoolean(periodicLimitType.
                        getAsString(ConsentExtensionConstants.IS_VALID)))) {
                    log.error(periodicLimitType.get(ConsentExtensionConstants.ERRORS));
                    return periodicLimitType;
                }

                JSONObject maximumIndividualAmountCurrencyPeriodicLimit = validateMaximumIndividualAmountCurrency
                        (controlParameters);
                if (!(Boolean.parseBoolean(maximumIndividualAmountCurrencyPeriodicLimit.
                        getAsString(ConsentExtensionConstants.IS_VALID)))) {
                    log.error(maximumIndividualAmountCurrencyPeriodicLimit.get(ConsentExtensionConstants.ERRORS));
                    return maximumIndividualAmountCurrencyPeriodicLimit;
                }

                JSONObject periodicAlignment = validatePeriodicAlignments(controlParameters);
                if (!(Boolean.parseBoolean(periodicAlignment.
                        getAsString(ConsentExtensionConstants.IS_VALID)))) {
                    log.error(periodicAlignment.get(ConsentExtensionConstants.ERRORS));
                    return periodicAlignment;
                }

                JSONObject periodicType = validatePeriodicTypes(controlParameters);
                if (!(Boolean.parseBoolean(periodicType.
                        getAsString(ConsentExtensionConstants.IS_VALID)))) {
                    log.error(periodicType.get(ConsentExtensionConstants.ERRORS));
                    return periodicType;
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
    public static JSONObject validateAmountCurrencyPeriodicLimit(JSONObject controlParameters) {

        JSONObject validationResponse = new JSONObject();

        JSONArray periodicLimits = (JSONArray) controlParameters.get(ConsentExtensionConstants.PERIODIC_LIMITS);

        if (!validateAmountCurrencyPeriodicLimits((JSONArray) periodicLimits,
                ConsentExtensionConstants.CURRENCY)) {
            return ConsentManageUtil.getValidationResponse(ErrorConstants.
                            PAYLOAD_FORMAT_ERROR_MAXIMUM_INDIVIDUAL_CURRENCY,
                    ErrorConstants.PERIODIC_LIMIT_CURRENCY_IS_MISSING,
                    ErrorConstants.PATH_MAXIMUM_INDIVIDUAL_AMOUNT);
        }
        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }

    /**
     * Validates the periodicType in periodic limits in the control parameters of a consent request.
     *
     * @param controlParameters The JSON object representing the control parameters of the consent request.
     * @return A JSONArray containing the validation response.
     */
    public static JSONObject validatePeriodicTypes(JSONObject controlParameters) {

        JSONObject validationResponse = new JSONObject();

        JSONArray periodicTypes = (JSONArray) controlParameters.get(ConsentExtensionConstants.PERIODIC_LIMITS);
        JSONArray periodicType = (JSONArray) periodicTypes.get(Integer.parseInt(ConsentExtensionConstants.PERIOD_TYPE));

        if (!validatePeriodicType((JSONArray) periodicType)) {
            return ConsentManageUtil.getValidationResponse(ErrorConstants.
                            PAYLOAD_FORMAT_ERROR_PERIODIC_LIMITS,
                    ErrorConstants.MISSING_PERIOD_TYPE,
                    ErrorConstants.PATH_PERIOD_TYPE);
        }
        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }

    /**
     * Validates the periodicAlignment in periodic limits in the control parameters of a consent request.
     *
     * @param controlParameters The JSON object representing the control parameters of the consent request.
     * @return A JSONArray containing the validation response.
     */
    public static JSONObject validatePeriodicAlignments(JSONObject controlParameters) {

        JSONObject validationResponse = new JSONObject();

        JSONArray periodicAlignments = (JSONArray) controlParameters.get(ConsentExtensionConstants.PERIODIC_LIMITS);
        JSONArray periodicAlignment = (JSONArray) controlParameters.get(ConsentExtensionConstants.PERIOD_ALIGNMENT);

        if (!validatePeriodicAlignment((JSONArray) periodicAlignment)) {
            return ConsentManageUtil.getValidationResponse(ErrorConstants.
                            PAYLOAD_FORMAT_ERROR_PERIODIC_LIMITS,
                    ErrorConstants.MISSING_PERIOD_ALIGNMENT,
                    ErrorConstants.PATH_PERIOD_TYPE);
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

        if (controlParameters.containsKey(ConsentExtensionConstants.VALID_TO_DATE_TIME)) {

            // Retrieve the validDateTime from the control parameters
            Object validateToDateTime = controlParameters.get(ConsentExtensionConstants.VALID_TO_DATE_TIME);

            if (!isValidDateTimeObject(validateToDateTime)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR_VALID_TO_DATE,
                        ErrorConstants.INVALID_DATE_TIME_FORMAT,
                        ErrorConstants.PATH_VALID_TO_DATE);
            }

            OffsetDateTime validToDateTime = OffsetDateTime.parse(controlParameters.
                    getAsString(ConsentExtensionConstants.VALID_TO_DATE_TIME));
            OffsetDateTime validFromDateTime = OffsetDateTime.parse(controlParameters.
                    getAsString(ConsentExtensionConstants.VALID_FROM_DATE_TIME));
            OffsetDateTime currentDateTime = OffsetDateTime.now(validToDateTime.getOffset());

            //If the ValidToDAte is older than current date OR currentDate is older than ValidFromDAte, return error
            if (!validFromDateTime.isBefore(currentDateTime) || !currentDateTime.isBefore(validToDateTime)) {
                log.error(String.format("Invalid date-time range, validToDateTime : %s , validFromDateTime : %s " +
                        "and currentDateTime : %s ", validToDateTime, validFromDateTime, currentDateTime));
                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                        ErrorConstants.INVALID_VALID_TO_DATE, ErrorConstants.PATH_VALID_TO_DATE);
            }
        }

        if (controlParameters.containsKey(ConsentExtensionConstants.VALID_FROM_DATE_TIME)) {

            // Retrieve the validDateTime from the control parameters
            Object validateFromDateTime = controlParameters.get(ConsentExtensionConstants.VALID_FROM_DATE_TIME);

            if (!isValidDateTimeObject(validateFromDateTime)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR_VALID_FROM_DATE,
                        ErrorConstants.INVALID_DATE_TIME_FORMAT,
                        ErrorConstants.PATH_VALID_TO_DATE);
            }

            OffsetDateTime validToDateTime = OffsetDateTime.parse(controlParameters.
                    getAsString(ConsentExtensionConstants.VALID_TO_DATE_TIME));
            OffsetDateTime validFromDateTime = OffsetDateTime.parse(controlParameters.
                    getAsString(ConsentExtensionConstants.VALID_FROM_DATE_TIME));
            OffsetDateTime currentDateTime = OffsetDateTime.now(validToDateTime.getOffset());

            //If the ValidToDAte is older than current date OR currentDate is older than ValidFromDAte, return error
            if (!validFromDateTime.isBefore(currentDateTime) || !currentDateTime.isBefore(validToDateTime)) {
                log.debug("Invalid date-time range.");
                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_REQ_PAYLOAD,
                        ErrorConstants.INVALID_VALID_TO_DATE, ErrorConstants.PATH_VALID_FROM_DATE);
            }
        }
        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
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
    public static boolean  validateJsonObjectKey(JSONObject parentObj, String key) {
        JSONObject validationResponse = new JSONObject();
        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);

        if (parentObj != null) {
            // Check if the specified key is present in the parentObj
            if (parentObj.containsKey(key)) {
                Object value = parentObj.get(key);

                // Check if the value associated with the key is a non-empty string
                if (value instanceof String && !((String) value).isEmpty()) {
                    return true; // Valid: The key is present, and the value is a non-empty String
                } else {
                    return false; // Invalid: The value associated with the key is not a non-empty String
                }
            } else {
                return false; // Invalid: The specified key is not present in parentObj
            }
        }

        return false; // Invalid: parentObj is null
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
    public static boolean validateAmountCurrencyPeriodicLimits(JSONArray parentArray, String key) {
        JSONObject validationResponse = new JSONObject();
        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);

        if (parentArray != null && key != null) {
            // Check if the specified key is present in the parentArray
            for (Object obj : parentArray) {
                if (obj instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) obj;

                    if (jsonObject.containsKey(key)) {
                        Object value = jsonObject.get(key);

                        // Check if the value associated with the key is a non-empty string
                        if (value instanceof String && !((String) value).isEmpty()) {
                            return true; // Valid: The key is present, and the value is a non-empty String
                        } else {
                            return false; // Invalid: The value associated with the key is not a non-empty String
                        }
                    }
                }
            }
        }
        return false; // Invalid: key is not present in parentArray or parentArray is null
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
                log.error(initiationValidationResult.get(ConsentExtensionConstants.ERRORS));
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
                log.error(controlParameterValidationResult.get(ConsentExtensionConstants.ERRORS));
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
     * Validates the  periodic alignments in the VRP request payload.
     *
     * @param periodicAlignment The JSONObject representing the VRP request.
     * @return A JSONObject containing the validation response.
     */

    public static boolean validatePeriodicAlignment(Object periodicAlignment) {

        JSONObject validationResponse = new JSONObject();
        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);

        JSONObject periodAlignmentObject = (JSONObject) periodicAlignment;

        if (periodAlignmentObject.containsKey(ConsentExtensionConstants.PERIODIC_LIMITS)) {
            Object alignment = periodAlignmentObject.get(ConsentExtensionConstants.PERIOD_ALIGNMENT);

            if (alignment != null && alignment instanceof String && !((String) alignment).isEmpty()) {
                String periodType = (String) alignment;

                List<String> validAlignmentTypes = Arrays.asList(
                        ConsentExtensionConstants.CONSENT,
                        ConsentExtensionConstants.CALENDER
                );
                return validAlignmentTypes.contains(periodType);
            }
        }
        return false;
    }
/**
     * Validates the  periodic type in the VRP request payload.
     *
     * @param periodicType The JSONObject representing the VRP request.
     * @return A JSONObject containing the validation response.
     */
    public static boolean validatePeriodicType(Object periodicType) {

        JSONObject validationResponse = new JSONObject();
        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);

        JSONObject limitObject = (JSONObject) periodicType;

        if (limitObject.containsKey(ConsentExtensionConstants.PERIOD_TYPE)) {
            Object periodTypeObject = limitObject.get(ConsentExtensionConstants.PERIOD_TYPE);

            if (periodTypeObject != null && periodTypeObject instanceof String &&
                    !((String) periodTypeObject).isEmpty()) {
                String periodType = (String) periodTypeObject;

                List<String> validPeriodTypes = Arrays.asList(
                        ConsentExtensionConstants.DAY,
                        ConsentExtensionConstants.WEEK,
                        ConsentExtensionConstants.FORTNIGHT,
                        ConsentExtensionConstants.MONTH,
                        ConsentExtensionConstants.HALF_YEAR,
                        ConsentExtensionConstants.YEAR
                );
                return validPeriodTypes.contains(periodType);
            }
        }
        return false;
    }
}
