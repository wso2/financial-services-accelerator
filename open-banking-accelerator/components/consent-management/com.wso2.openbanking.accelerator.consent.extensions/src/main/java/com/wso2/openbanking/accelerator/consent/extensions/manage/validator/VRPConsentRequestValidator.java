/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
            return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR);
        }

        JSONObject requestBody = (JSONObject) request;
        //Check request body is valid and not empty
        JSONObject dataValidationResult = ConsentManageUtil.validateInitiationDataBody(requestBody);

        if (!(Boolean.parseBoolean(dataValidationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
            return dataValidationResult;
        }

        //Check consent  initiation is valid and not empty
        JSONObject initiationValidationResult = VRPConsentRequestValidator.validateConsentInitiation(requestBody);

        if (!(Boolean.parseBoolean(initiationValidationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
            return initiationValidationResult;
        }

        JSONObject controlParameterValidationResult = VRPConsentRequestValidator.
                validateConsentControlParameters(requestBody);

        if (!(Boolean.parseBoolean(controlParameterValidationResult.
                getAsString(ConsentExtensionConstants.IS_VALID)))) {
            return controlParameterValidationResult;
        }

        JSONObject riskValidationResult = VRPConsentRequestValidator.validateConsentRisk(requestBody);

        if (!(Boolean.parseBoolean(riskValidationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
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

        JSONObject maximumIndividualAmountCurrencyValidationResult = validateMaximumIndividualAmountCurrency
                (controlParameters);
        if (!(Boolean.parseBoolean(maximumIndividualAmountCurrencyValidationResult.
                getAsString(ConsentExtensionConstants.IS_VALID)))) {
            return maximumIndividualAmountCurrencyValidationResult;
        }

        JSONObject parameterDateTimeValidationResult= validateParameterDateTime(controlParameters);
        if (!(Boolean.parseBoolean(parameterDateTimeValidationResult.
                getAsString(ConsentExtensionConstants.IS_VALID)))) {
            return parameterDateTimeValidationResult;
        }

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
        String errorMessage = String.format(ErrorConstants.INVALID_PARAMETER_MESSAGE, "periodic limit",
                "JSONObject");
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
                return ConsentManageUtil.getValidationResponse(errorMessage);
            }

            JSONObject maximumIndividualAmountResult = validateJsonObjectKey((JSONObject) maximumIndividualAmount,
                    ConsentExtensionConstants.AMOUNT);
            if (!(Boolean.parseBoolean(maximumIndividualAmountResult.
                    getAsString(ConsentExtensionConstants.IS_VALID)))) {
                return maximumIndividualAmountResult;
            }
        } else {
            log.error(ErrorConstants.MISSING_MAXIMUM_INDIVIDUAL_AMOUNT);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.MISSING_MAXIMUM_INDIVIDUAL_AMOUNT);

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

        JSONObject maximumIndividualAmountValidationResult = validateJsonObjectKey((JSONObject) maximumIndividualAmount,
                ConsentExtensionConstants.CURRENCY);
        if (!(Boolean.parseBoolean(maximumIndividualAmountValidationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
            return maximumIndividualAmountValidationResult;
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

            // Check if the control parameter is a valid JSON array
            if (!isValidJSONArray(periodicLimit)) {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_PARAMETER_PERIODIC_LIMITS);
            }

            JSONArray periodicLimits = (JSONArray) controlParameters.get(ConsentExtensionConstants.PERIODIC_LIMITS);
            Iterator parameters = periodicLimits.iterator();

            while (parameters.hasNext()) {
                JSONObject limit = (JSONObject) parameters.next();

                JSONObject amountValidationResult = validateAmountPeriodicLimit(controlParameters);
                if (!(Boolean.parseBoolean(amountValidationResult.
                        getAsString(ConsentExtensionConstants.IS_VALID)))) {
                    return amountValidationResult;
                }

                JSONObject currencyValidationResult = validateCurrencyPeriodicLimit(controlParameters);
                if (!(Boolean.parseBoolean(currencyValidationResult.
                        getAsString(ConsentExtensionConstants.IS_VALID)))) {
                    return currencyValidationResult;
                }

                JSONObject periodAlignmentValidationResult = validatePeriodAlignment(limit);
                if (!(Boolean.parseBoolean(periodAlignmentValidationResult.
                        getAsString(ConsentExtensionConstants.IS_VALID)))) {
                    return periodAlignmentValidationResult;
                }

                JSONObject periodTypeValidationResult = validatePeriodType(limit);
                if (!(Boolean.parseBoolean(periodTypeValidationResult.
                        getAsString(ConsentExtensionConstants.IS_VALID)))) {
                    return periodTypeValidationResult;
                }
            }
        } else {
            // If periodic limits key is missing, return an error
            log.error(ErrorConstants.MISSING_PERIOD_LIMITS);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.MISSING_PERIOD_LIMITS);
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
        JSONObject currencyValidationResponse = validateAmountCurrencyPeriodicLimits((JSONArray) periodicLimits,
                ConsentExtensionConstants.CURRENCY);
        if (!(Boolean.parseBoolean(currencyValidationResponse.
                getAsString(ConsentExtensionConstants.IS_VALID)))) {
            return currencyValidationResponse;
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

        JSONObject amountValidationResponse = validateAmountCurrencyPeriodicLimits((JSONArray) periodicLimits,
                ConsentExtensionConstants.AMOUNT);
        if (!(Boolean.parseBoolean(amountValidationResponse.
                getAsString(ConsentExtensionConstants.IS_VALID)))) {
            return amountValidationResponse;
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

            if (!ConsentManageUtil.isValid8601(controlParameters
                    .getAsString(ConsentExtensionConstants.VALID_TO_DATE_TIME))) {
                log.error(" Date and Time is not in  valid ISO 8601 format");
                return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID_DATE);
            }

            Object validToDateTimeRetrieval = controlParameters.get(ConsentExtensionConstants.VALID_TO_DATE_TIME);
            JSONObject validToDateTimeValidationResponse = isValidDateTimeObject(validToDateTimeRetrieval);
            if (!(Boolean.parseBoolean(validToDateTimeValidationResponse.
                    getAsString(ConsentExtensionConstants.IS_VALID)))) {
                return validToDateTimeValidationResponse;
            }

            String validToDateTimeString = controlParameters.getAsString(ConsentExtensionConstants.VALID_TO_DATE_TIME);
            OffsetDateTime validToDateTime = OffsetDateTime.parse(validToDateTimeString);

            if (controlParameters.containsKey(ConsentExtensionConstants.VALID_FROM_DATE_TIME)) {

                if (!ConsentManageUtil.isValid8601(controlParameters
                        .getAsString(ConsentExtensionConstants.VALID_FROM_DATE_TIME))) {
                    log.error("Date and Time is not in  valid ISO 8601 format");
                    return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID_DATE);
                }


                Object validFromDateTimeRetrieval = controlParameters.get
                        (ConsentExtensionConstants.VALID_FROM_DATE_TIME);
                JSONObject validFromDateTimeValidationResponse = isValidDateTimeObject(validFromDateTimeRetrieval);
                if (!(Boolean.parseBoolean(validFromDateTimeValidationResponse.
                        getAsString(ConsentExtensionConstants.IS_VALID)))) {
                    return  validFromDateTimeValidationResponse;
                }

                String validFromoDateTimeString = controlParameters.getAsString
                        (ConsentExtensionConstants.VALID_FROM_DATE_TIME);

                OffsetDateTime validFromDateTime = OffsetDateTime.parse(validFromoDateTimeString);
                OffsetDateTime currentDateTime = OffsetDateTime.now(validToDateTime.getOffset());

                // If ValidToDateTime is older than current date OR ValidToDateTime is older than ValidFromDateTime,
                // return error
                if (!validFromDateTime.isBefore(currentDateTime) || !currentDateTime.isBefore(validToDateTime)) {
                    log.error(String.format("Invalid date-time range, " +
                                    "validToDateTime: %s, validFromDateTime: %s, currentDateTime: %s",
                            validToDateTime, validFromDateTime, currentDateTime));

                    String errorMessage = String.format(ErrorConstants.DATE_INVALID_PARAMETER_MESSAGE);

                    return ConsentManageUtil.getValidationResponse(errorMessage);
                }
            } else {
                log.error("validFromDateTime parameter is missing in the payload");
                return ConsentManageUtil.getValidationResponse(ErrorConstants.MISSING_VALID_FROM_DATE_TIME);
            }
        } else {
            log.error("Missing validToDateTime parameter is missing in the payload");
            return ConsentManageUtil.getValidationResponse(ErrorConstants.MISSING_VALID_TO_DATE_TIME);
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

            if (!isValidJSONObject(debtorAccount)) {
                String errorMessage = String.format(ErrorConstants.INVALID_PARAMETER_MESSAGE,
                        "debtor account", "JSONObject");

                return ConsentManageUtil.getValidationResponse(errorMessage);
            }

            JSONObject validationResult = ConsentManageUtil.validateDebtorAccount((JSONObject) debtorAccount);

            if (!(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
                log.error(validationResult.get(ConsentExtensionConstants.ERRORS));
                return validationResult;
            }

        } else {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR_DEBTOR_ACC);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR_DEBTOR_ACC);
        }

        //Validate CreditorAccount
        if (initiation.containsKey(ConsentExtensionConstants.CREDITOR_ACC)) {

            Object creditorAccount = initiation.get(ConsentExtensionConstants.CREDITOR_ACC);

            if (!isValidJSONObject(creditorAccount)) {
                String errorMessage = String.format(ErrorConstants.INVALID_PARAMETER_MESSAGE,
                        "creditor account", "JSONObject");

                return ConsentManageUtil.getValidationResponse(errorMessage);
            }

            JSONObject validationResult = ConsentManageUtil.validateCreditorAccount((JSONObject) creditorAccount);

            if (!(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
                log.error(validationResult.get(ConsentExtensionConstants.ERRORS));
                return validationResult;
            }

        } else {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR_CREDITOR_ACC);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR);
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
                    if ("Amount".equals(key)) {
                        // For the "amount" key, try parsing as Double allowing letters
                        if (isDouble((String) value)) {
                            validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
                            return validationResponse; // Valid: The key is present, and the value is a valid Double
                        } else {
                            String errorMessage = "The value of '" + key + "' is not a valid number";
                            return ConsentManageUtil.getValidationResponse(errorMessage);
                            // Invalid: The value associated with the key is not a valid Double
                        }
                    } else {
                        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
                        return validationResponse; // Valid: The key is present, and the value is a non-empty String
                    }
                } else {
                    String errorMessage = "The value of '" + key + "' is not a string or the value is empty";
                    return ConsentManageUtil.getValidationResponse(errorMessage);
                    // Invalid: The value associated with the key is not a non-empty String
                }
            } else {
                String errorMessage = "Mandatory parameter '" + key + "' is not present in payload";
                return ConsentManageUtil.getValidationResponse(errorMessage);
                // Invalid: The specified key is not present in parentObj
            }
        } else {
            String errorMessage = "parameter passed in is null";
            return ConsentManageUtil.getValidationResponse(errorMessage);
        }
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
                            // Check if the key is "amount" and the value is a valid double
                            if ("Amount".equals(key) && isDouble((String) value)) {
                                JSONObject validationResponse = new JSONObject();
                                validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
                                return validationResponse;
                                // Valid: The key is "amount", and the value is a valid double
                            } else {
                                String errorMessage = "Mandatory parameter '" + key + "' is not present in periodic" +
                                        " limits or the value is not a valid double";
                                return ConsentManageUtil.getValidationResponse(errorMessage);
                                // Invalid: The value associated with the key is not a valid double
                            }
                        }
                    }
                }
            }
        }
        String errorMessage = "Mandatory parameter '" + key + "' of periodic limits is not present in payload";
        return ConsentManageUtil.getValidationResponse(errorMessage);
    }

    // Helper method to check if a string contains a valid double
    private static boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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

            if (!isValidJSONObject(initiation)) {
                String errorMessage = String.format(ErrorConstants.INVALID_PARAMETER_MESSAGE,
                        "initiation", "JSONObject");

                return ConsentManageUtil.getValidationResponse(errorMessage);
            }

            JSONObject initiationValidationResult = VRPConsentRequestValidator
                    .validateVRPInitiationPayload((JSONObject) initiation);

            if (!(Boolean.parseBoolean(initiationValidationResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
                return initiationValidationResult;
            }
        } else {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR_INITIATION);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR_INITIATION);
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

            if (!isValidJSONObject(controlParameters)) {
                String errorMessage = String.format(ErrorConstants.INVALID_PARAMETER_MESSAGE,
                        "control parameters", "JSONObject");

                return ConsentManageUtil.getValidationResponse(errorMessage);
            }

            JSONObject controlParameterValidationResult =
                    VRPConsentRequestValidator.validateControlParameters((JSONObject)
                            data.get(ConsentExtensionConstants.CONTROL_PARAMETERS));

            if (!(Boolean.parseBoolean(controlParameterValidationResult.
                    getAsString(ConsentExtensionConstants.IS_VALID)))) {
                return controlParameterValidationResult;
            }
        } else {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR_CONTROL_PARAMETER);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR_CONTROL_PARAMETER);
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
            return ConsentManageUtil.getValidationResponse(ErrorConstants.PAYLOAD_FORMAT_ERROR_RISK);
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
                    return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_PERIOD_ALIGNMENT);
                }
            } else {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.
                        PAYLOAD_FORMAT_ERROR_PERIODIC_LIMITS_ALIGNMENT);
            }
        } else {
            return ConsentManageUtil.getValidationResponse(ErrorConstants.MISSING_PERIOD_ALIGNMENT);
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
                    return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_PERIOD_TYPE);
                }
            } else {
                return ConsentManageUtil.getValidationResponse(ErrorConstants.
                        PAYLOAD_FORMAT_ERROR_PERIODIC_LIMITS_PERIOD_TYPE);
            }
        } else {
            return ConsentManageUtil.getValidationResponse(ErrorConstants.MISSING_PERIOD_TYPE);
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
                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_DATE_TIME_FORMAT);
            }
        } else {
            return ConsentManageUtil.getValidationResponse(ErrorConstants.MISSING_DATE_TIME_FORMAT);
        }

        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }

}
