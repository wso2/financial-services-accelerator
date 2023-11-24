/**
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.accelerator.consent.extensions.manage.validator;
import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.util.ConsentManageUtil;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.OffsetDateTime;
import java.util.Iterator;


/**
 * Consent Manage validator class for Variable Recurring Payment Request Validation.
 */
public class VRPConsentRequestValidator {

    private static final Log log = LogFactory.getLog(VRPConsentRequestValidator.class);

    public static final String MAXIMUM_INDIVIDUAL_AMOUNT_NOT_FOUND = "Instructed Amount isn't present in the payload";

    public static final String PATH_MAXIMUM_INDIVIDUAL_CURRENCY = "Data.ControlParameters." +
            "MaximumIndividualAmount.Currency";

    public static final String MAXIMUM_INDIVIDUAL_AMOUNT_CURRENCY_NOT_FOUND = "Instructed currency isn't " +
            "present in the payload";

    public static final String INVALID_PERIOD_ALIGNMENT = "Invalid value for period alignment in PeriodicLimits";

    public static final String PATH_PERIOD_ALIGNMENT = "Data.ControlParameters.PeriodLimits.PeriodAlignment";

    /**
     * Method to validate variable recurring payment control parameters.
     *
     * @param controlParameters Initiation Object
     * @return validation response object
     */
    public static JSONObject validatecontrolParameters(JSONObject controlParameters) {
        JSONObject validationResponse = new JSONObject();

        //Validate Maximum individual amount in control parameters
        if (controlParameters.containsKey(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT)) {
            JSONObject maximumIndividualAmount = (JSONObject) controlParameters
                    .get(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT);
            Object amount = maximumIndividualAmount.get(ConsentExtensionConstants.AMOUNT);
            Object currency = maximumIndividualAmount.get(ConsentExtensionConstants.CURRENCY);

            // validate amount
            if (!ConsentManageUtil.validateAmount(maximumIndividualAmount)) {
                log.error(ErrorConstants.INVALID_MAXIMUM_INDIVIDUAL_AMOUNT);
                return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID,
                        ErrorConstants.INVALID_MAXIMUM_INDIVIDUAL_AMOUNT,
                        ErrorConstants.PATH_MAXIMUM_INDIVIDUAL_AMOUNT);
            }

            if (amount == null || StringUtils.isEmpty(amount.toString())) {
                log.error(MAXIMUM_INDIVIDUAL_AMOUNT_NOT_FOUND);
                validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
                validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
                validationResponse.put(ConsentExtensionConstants.ERRORS, MAXIMUM_INDIVIDUAL_AMOUNT_NOT_FOUND);
                return validationResponse;
            }

            //validate currency
            if (!ConsentManageUtil.validateCurrency(maximumIndividualAmount)) {
                log.error(ErrorConstants.INVALID_CURRENCY);
                validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
                validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
                validationResponse.put(ConsentExtensionConstants.ERRORS,
                        ErrorConstants.PATH_MAXIMUM_INDIVIDUAL_AMOUNT);
                return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID,
                        ErrorConstants.INVALID_CURRENCY, ErrorConstants.PATH_MAXIMUM_INDIVIDUAL_AMOUNT);
            }

            if (currency == null || StringUtils.isEmpty(currency.toString())) {
                log.error(MAXIMUM_INDIVIDUAL_AMOUNT_CURRENCY_NOT_FOUND);
                validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
                validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
                validationResponse.put(ConsentExtensionConstants.ERRORS,
                        MAXIMUM_INDIVIDUAL_AMOUNT_CURRENCY_NOT_FOUND);
                return validationResponse;
            }
        }

        //Validate RequestedExecutionDateTime in controlParameters
        if (controlParameters.containsKey(ConsentExtensionConstants.VALID_TO_DATE_TIME)) {

            String validTo = controlParameters.getAsString(ConsentExtensionConstants.VALID_TO_DATE_TIME);
            String validFrom = controlParameters.getAsString(ConsentExtensionConstants.VALID_FROM_DATE_TIME);

            OffsetDateTime validToDateTime = OffsetDateTime.parse(validTo);
            OffsetDateTime validFromDateTime = OffsetDateTime.parse(validFrom);
            OffsetDateTime currentDateTime = OffsetDateTime.now(validToDateTime.getOffset());

            //If the ValidToDAte is older than current date OR ValidToDAte is older than ValidFromDAte, return error
            if (currentDateTime.isAfter(validToDateTime) || validFromDateTime.isAfter(validToDateTime)) {
                log.error(ErrorConstants.INVALID_VALID_TO_DATE);
                return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID,
                        ErrorConstants.INVALID_VALID_TO_DATE, ErrorConstants.PATH_VALID_TO_DATE);
            }
        }

        //Validate periodic limits in control parameters
        if (controlParameters.containsKey(ConsentExtensionConstants.PERIODIC_LIMITS)) {
            JSONArray periodicLimits = (JSONArray) controlParameters.get(ConsentExtensionConstants.PERIODIC_LIMITS);
            Iterator it = periodicLimits.iterator();

            while (it.hasNext()) {
                JSONObject limit = (JSONObject) it.next();

                Object amount =  limit.get(ConsentExtensionConstants.AMOUNT);
                Object currency = limit.get(ConsentExtensionConstants.CURRENCY);
                Object periodType = limit.get(ConsentExtensionConstants.PERIOD_TYPE);

                // validate amount
                if (!ConsentManageUtil.validateAmount(limit)) {
                    log.error(ErrorConstants.INVALID_MAXIMUM_INDIVIDUAL_AMOUNT);
                    return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID,
                            ErrorConstants.INVALID_MAXIMUM_INDIVIDUAL_AMOUNT,
                            ErrorConstants.PATH_MAXIMUM_INDIVIDUAL_AMOUNT);
                }

                if (amount == null || StringUtils.isEmpty(amount.toString())) {
                    log.error(MAXIMUM_INDIVIDUAL_AMOUNT_NOT_FOUND);
                    validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
                    validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
                    validationResponse.put(ConsentExtensionConstants.ERRORS,
                            MAXIMUM_INDIVIDUAL_AMOUNT_NOT_FOUND);
                    return validationResponse;
                }

                //validate currency
                if (!ConsentManageUtil.validateCurrency(limit)) {
                    log.error(ErrorConstants.INVALID_CURRENCY);
                    return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID,
                            ErrorConstants.INVALID_CURRENCY, ErrorConstants.PATH_MAXIMUM_INDIVIDUAL_AMOUNT);
                }

                if (currency == null || StringUtils.isEmpty(currency.toString())) {
                    log.error(MAXIMUM_INDIVIDUAL_AMOUNT_CURRENCY_NOT_FOUND);
                    validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
                    validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
                    validationResponse.put(ConsentExtensionConstants.ERRORS,
                           MAXIMUM_INDIVIDUAL_AMOUNT_CURRENCY_NOT_FOUND);
                    return validationResponse;
                }

                //validate period alignment
                if (ConsentManageUtil.validatePeriodicAlignment(limit)) {
                    log.error(ErrorConstants.INVALID_PERIOD_ALIGNMENT);
                    return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID,
                            INVALID_PERIOD_ALIGNMENT, PATH_PERIOD_ALIGNMENT);
                }

                //validate period type
                if (!ConsentManageUtil.validatePeriodicType(limit)) {
                    log.error(ErrorConstants.INVALID_PERIOD_TYPE);
                    return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID,
                            ErrorConstants.INVALID_PERIOD_TYPE, ErrorConstants.PATH_PERIOD_TYPE);
                }

                if (periodType == null || StringUtils.isEmpty(periodType.toString())) {
                    log.error(ErrorConstants.INVALID_PERIOD_TYPE);
                    validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
                    validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
                    validationResponse.put(ConsentExtensionConstants.ERRORS, ErrorConstants.INVALID_PERIOD_TYPE);
                    return validationResponse;
                }
            }

            validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
            return validationResponse;
        }
        return validationResponse;
    }

    /**
     * Method to validate variable recurring payment initiation request.
     *
     * @param initiation Initiation Object
     * @return validation response object
     */
    public static JSONObject validatePaymentInitiation(JSONObject initiation) {

        JSONObject validationResponse = new JSONObject();
        validationResponse.put(ConsentExtensionConstants.IS_VALID, false);

        //Check request body is valid and not empty
        JSONObject dataValidationResult = ConsentManageUtil.validateInitiationDataBody(initiation);
        if (!(boolean) dataValidationResult.get(ConsentExtensionConstants.IS_VALID)) {
            return dataValidationResult;
        }

        JSONObject data = (JSONObject) initiation.get(ConsentExtensionConstants.DATA);

        //Validate initiation in the VRP payload
        if (data.containsKey(ConsentExtensionConstants.INITIATION)) {
            JSONObject initiationValidationResult = VRPConsentRequestValidator
                    .validateVRPInitiationPayload((JSONObject) data.get(ConsentExtensionConstants.INITIATION));

            if (!(boolean) initiationValidationResult.get(ConsentExtensionConstants.IS_VALID)) {
                return initiationValidationResult;
            }
        } else {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.RESOURCE_INVALID_FORMAT,
                    ErrorConstants.PAYLOAD_FORMAT_ERROR, ErrorConstants.PATH_REQUEST_BODY);
        }

        //Validate the ControlParameter in the payload
        if (data.containsKey(ConsentExtensionConstants.CONTROL_PARAMETERS)) {
            JSONObject controlParameterValidationResult =
                    VRPConsentRequestValidator.validatecontrolParameters((JSONObject)
                            data.get(ConsentExtensionConstants.CONTROL_PARAMETERS));

            if (!(boolean) controlParameterValidationResult.get(ConsentExtensionConstants.IS_VALID)) {
                return controlParameterValidationResult;
            }
        } else {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.RESOURCE_INVALID_FORMAT,
                    ErrorConstants.PAYLOAD_FORMAT_ERROR, ErrorConstants.PATH_REQUEST_BODY);
        }

        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }


    /**
     * Validator class to validate variable recurring payment initiation payload.
     * @param initiation
     * @return validationResponse
     */
    public static JSONObject validateVRPInitiationPayload(JSONObject initiation) {

        JSONObject validationResponse = new JSONObject();

        //Validate DebtorAccount
        if (initiation.containsKey(ConsentExtensionConstants.DEBTOR_ACC)) {

            JSONObject debtorAccount = (JSONObject) initiation.get(ConsentExtensionConstants.DEBTOR_ACC);
            JSONObject validationResult = ConsentManageUtil.validateVRPDebtorAccount(debtorAccount);

            if (!(boolean) validationResult.get(ConsentExtensionConstants.IS_VALID)) {
                return validationResult;
            }
        }

        //Validate CreditorAccount
        if (initiation.containsKey(ConsentExtensionConstants.CREDITOR_ACC)) {
            JSONObject creditorAccount = (JSONObject) initiation.get(ConsentExtensionConstants.CREDITOR_ACC);

            JSONObject validationResult = ConsentManageUtil.validateVRPCreditorAccount(creditorAccount);

            if (!(boolean) validationResult.get(ConsentExtensionConstants.IS_VALID)) {
                return validationResult;
            }
        }

        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }

}








