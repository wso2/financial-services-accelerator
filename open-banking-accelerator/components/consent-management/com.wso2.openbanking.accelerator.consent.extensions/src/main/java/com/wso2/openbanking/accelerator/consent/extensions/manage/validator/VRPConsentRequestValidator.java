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


        JSONObject maximumIndividualAmountResult = validateJsonObjectKey((JSONObject) maximumIndividualAmount,
                ConsentExtensionConstants.CURRENCY);
        if (!(Boolean.parseBoolean(maximumIndividualAmountResult.getAsString(ConsentExtensionConstants.IS_VALID)))) {
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
                return ConsentManageUtil.getValidationResponse(ErrorConstants.INVALID_PARAMETER_PERIODIC_LIMITS);
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

        // Check if ValidToDateTime key is present
        if (controlParameters.containsKey(ConsentExtensionConstants.VALID_TO_DATE_TIME)) {
            // Get and validate ValidToDateTime
            Object validateToDateTime = controlParameters.get(ConsentExtensionConstants.VALID_TO_DATE_TIME);
            JSONObject toDateTimeValidation = isValidDateTimeObject(validateToDateTime);
            if (!(Boolean.parseBoolean(toDateTimeValidation.getAsString(ConsentExtensionConstants.IS_VALID)))) {
                return toDateTimeValidation;
            }

            String validTo = controlParameters.getAsString(ConsentExtensionConstants.VALID_TO_DATE_TIME);
            OffsetDateTime validToDateTime = OffsetDateTime.parse(validTo);

            // Check if ValidFromDateTime key is present
            if (controlParameters.containsKey(ConsentExtensionConstants.VALID_FROM_DATE_TIME)) {
                // Get and validate ValidFromDateTime
                Object validateFromDateTime = controlParameters.get(ConsentExtensionConstants.VALID_FROM_DATE_TIME);
                JSONObject fromDateTimeValidation = isValidDateTimeObject(validateFromDateTime);
                if (!(Boolean.parseBoolean(fromDateTimeValidation.getAsString(ConsentExtensionConstants.IS_VALID)))) {
                    return fromDateTimeValidation;
                }

                String validFrom = controlParameters.getAsString(ConsentExtensionConstants.VALID_FROM_DATE_TIME);
                OffsetDateTime validFromDateTime = OffsetDateTime.parse(validFrom);
                OffsetDateTime currentDateTime = OffsetDateTime.now(validToDateTime.getOffset());

                // If ValidToDateTime is older than current date OR ValidToDateTime is older than ValidFromDateTime,
                // return error
                if (!validFromDateTime.isBefore(currentDateTime) || !currentDateTime.isBefore(validToDateTime)) {
                    log.error(String.format("Invalid date-time range, " +
                                    "validToDateTime: %s, validFromDateTime: %s, currentDateTime: %s",
                            validToDateTime, validFromDateTime, currentDateTime));

                    String errorMessage = String.format(ErrorConstants.DATE_INVALID_PARAMETER_MESSAGE,
                            validateToDateTime, validateFromDateTime, currentDateTime);

                    return ConsentManageUtil.getValidationResponse(errorMessage);
                }
            } else {
                // If ValidFromDateTime key is not present, return error
                return ConsentManageUtil.getValidationResponse(ErrorConstants.MISSING_VALID_FROM_DATE_TIME);
            }
        } else {
            // If ValidToDateTime key is not present, return error
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

            String errorMessage = String.format(ErrorConstants.INVALID_PARAMETER_MESSAGE,
                    "debtor account", "JSONObject");

            if (!isValidJSONObject(debtorAccount)) {
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

            String errorMessage = String.format(ErrorConstants.INVALID_PARAMETER_MESSAGE,
                    "creditor account", "JSONObject");

            if (!isValidJSONObject(creditorAccount)) {
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
                    validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
                    return validationResponse; // Valid: The key is present, and the value is a non-empty String
                } else {
                    String errorMessage = "The value of '" + key + "'is not a string or the value is empty";
                    return ConsentManageUtil.getValidationResponse(errorMessage);
                    // Invalid: The value associated with the key is not a non-empty String
                }
            } else {
                String errorMessage = "Mandatory parameter '" + key + "' is not present in payload";
                return ConsentManageUtil.getValidationResponse(errorMessage);
                // Invalid: The specified key is not present in parentObj
            }
        }

        String errorMessage = "parameter passed in  is null";
        return ConsentManageUtil.getValidationResponse(errorMessage);
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
                            return ConsentManageUtil.getValidationResponse(errorMessage);
                            // Invalid: The value associated with the key is not a non-empty String
                        }
                    }
                }
            }
        }
        String errorMessage = "Mandatory parameter '" + key + "' of periodic limits is not present in payload";
        return ConsentManageUtil.getValidationResponse(errorMessage);

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

            String errorMessage = String.format(ErrorConstants.INVALID_PARAMETER_MESSAGE,
                    "control parameters", "JSONObject");

            if (!isValidJSONObject(controlParameters)) {
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