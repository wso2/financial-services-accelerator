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

package com.wso2.openbanking.accelerator.consent.extensions.validate.util;

import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * Consent validate util class for accelerator.
 */
public class ConsentValidatorUtil {

    private static final Log log = LogFactory.getLog(ConsentValidatorUtil.class);

    /**
     * Utility method to validate mandatory parameters.
     *
     * @param str1   First String to validate
     * @param str2   Second String to validate
     * @return Whether mandatory parameters are same
     */
    public static  boolean compareMandatoryParameter(String str1, String str2) {

        return (str1 == null) || (str2 == null) ? false : str1.equals(str2);

    }

    /**
     * Method to construct the validation result.
     *
     * @param errorCode       Error Code
     * @param errorMessage    Error Message
     * @return Validation Result
     */
    public static JSONObject getValidationResult(String errorCode, String errorMessage) {

        JSONObject validationResult = new JSONObject();
        log.error(errorMessage);
        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, false);
        validationResult.put(ConsentExtensionConstants.ERROR_CODE, errorCode);
        validationResult.put(ConsentExtensionConstants.ERROR_MESSAGE, errorMessage);

        return validationResult;
    }
    /**
     * Method to construct the success validation result.
     *
     * @return Validation Result
     */
    public static JSONObject getSuccessValidationResult() {

        JSONObject validationResult = new JSONObject();
        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);

        return validationResult;
    }

    /**
     * Validate whether fields in creditor account from initiation and submission are same.
     *
     * @param subCreditorAccount   Creditor Account from submission request
     * @param initCreditorAccount  Creditor Account from initiation request
     * @return Validation Result
     */
    public static JSONObject validateCreditorAcc(JSONObject subCreditorAccount, JSONObject initCreditorAccount) {

        if (subCreditorAccount.containsKey(ConsentExtensionConstants.SCHEME_NAME)) {
            if (StringUtils.isEmpty(subCreditorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME)) ||
                    !ConsentValidatorUtil.compareMandatoryParameter(
                            subCreditorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME),
                            initCreditorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME))) {

                return ConsentValidatorUtil.getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                        ErrorConstants.CREDITOR_ACC_SCHEME_NAME_MISMATCH);
            }
        } else {
            return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                    ErrorConstants.CREDITOR_ACC_SCHEME_NAME_NOT_FOUND);
        }

        if (subCreditorAccount.containsKey(ConsentExtensionConstants.IDENTIFICATION)) {
            if (StringUtils.isEmpty(subCreditorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION)) ||
                    !ConsentValidatorUtil.compareMandatoryParameter(
                            subCreditorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION),
                            initCreditorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION))) {

                return ConsentValidatorUtil.getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                        ErrorConstants.CREDITOR_ACC_IDENTIFICATION_MISMATCH);
            }
        } else {
            return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                    ErrorConstants.CREDITOR_ACC_IDENTIFICATION_NOT_FOUND);
        }

        if (!ConsentValidatorUtil
                .compareOptionalParameter(subCreditorAccount.getAsString(ConsentExtensionConstants.NAME),
                        initCreditorAccount.getAsString(ConsentExtensionConstants.NAME))) {

            return ConsentValidatorUtil.getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                    ErrorConstants.CREDITOR_ACC_NAME_MISMATCH);
        }

        if (!ConsentValidatorUtil.compareOptionalParameter(subCreditorAccount
                        .getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION),
                initCreditorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION))) {

            return ConsentValidatorUtil
                    .getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                            ErrorConstants.CREDITOR_ACC_SEC_IDENTIFICATION_MISMATCH);
        }

        JSONObject validationResult = new JSONObject();
        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);

        return validationResult;
    }
    /**
     * Utility method to validate optional parameters.
     *
     * @param str1   First String to validate
     * @param str2   Second String to validate
     * @return Whether optional parameters are same
     */
    public static boolean compareOptionalParameter(String str1, String str2) {

        boolean isStr1Empty = StringUtils.isBlank(str1);
        boolean isStr2Empty = StringUtils.isBlank(str2);

        if (!(isStr1Empty || isStr2Empty)) {
            return str1.equals(str2);
        } else {
            return (isStr1Empty && isStr2Empty);
        }
    }

    /**
     * Validate whether fields in debtor account from initiation and submission are same.
     *
     * @param subDebtorAccount   Debtor Account from submission request
     * @param initDebtorAccount  Debtor Account from initiation request
     * @return Validation Result
     */
    public static JSONObject validateDebtorAcc(JSONObject subDebtorAccount, JSONObject initDebtorAccount) {

        if (subDebtorAccount.containsKey(ConsentExtensionConstants.SCHEME_NAME)) {
            if (StringUtils.isEmpty(subDebtorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME)) ||
                    !ConsentValidatorUtil.compareMandatoryParameter(
                            subDebtorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME),
                            initDebtorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME))) {

                return ConsentValidatorUtil.getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                        ErrorConstants.DEBTOR_ACC_SCHEME_NAME_MISMATCH);
            }
        } else {
            return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                    ErrorConstants.DEBTOR_ACC_SCHEME_NAME_NOT_FOUND);
        }

        if (subDebtorAccount.containsKey(ConsentExtensionConstants.IDENTIFICATION)) {
            if (StringUtils.isEmpty(subDebtorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION)) ||
                    !ConsentValidatorUtil.compareMandatoryParameter(
                            subDebtorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION),
                            initDebtorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION))) {

                return ConsentValidatorUtil
                        .getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                                ErrorConstants.DEBTOR_ACC_IDENTIFICATION_MISMATCH);
            }
        } else {
            return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                    ErrorConstants.DEBTOR_ACC_IDENTIFICATION_NOT_FOUND);
        }

        if (!ConsentValidatorUtil.compareOptionalParameter(
                subDebtorAccount.getAsString(ConsentExtensionConstants.NAME),
                initDebtorAccount.getAsString(ConsentExtensionConstants.NAME))) {

            return ConsentValidatorUtil.getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                    ErrorConstants.DEBTOR_ACC_NAME_MISMATCH);
        }

        if (!ConsentValidatorUtil.compareOptionalParameter(subDebtorAccount
                        .getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION),
                initDebtorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION))) {

            return ConsentValidatorUtil
                    .getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                            ErrorConstants.DEBTOR_ACC_SEC_IDENTIFICATION_MISMATCH);
        }

        JSONObject validationResult = new JSONObject();
        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);

        return validationResult;
    }
    /**
     * Method provides API resource paths applicable for Confirmtaion of Funds API.
     *
     * @return map of API Resources.
     */
    public static List<String> getCOFAPIPathRegexArray() {

        List<String> requestUrls = Arrays.asList(ConsentExtensionConstants.COF_CONSENT_INITIATION_PATH,
                ConsentExtensionConstants.COF_CONSENT_CONSENT_ID_PATH,
                ConsentExtensionConstants.COF_SUBMISSION_PATH);

        return requestUrls;

    }


    /**
     * Util method to validate the Confirmation of Funds request URI.
     *
     * @param uri  Request URI
     * @return Whether URI is valid
     */
    public static boolean isCOFURIValid(String uri) {

        List<String> accountPaths = getCOFAPIPathRegexArray();

        for (String entry : accountPaths) {
            if (uri.equals(entry)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validate whether consent is expired.
     *
     * @param expDateVal     Expiration Date Time
     * @return Whether consent is expired
     * @throws ConsentException if an error occurs while parsing expiration date
     */
    public static boolean isConsentExpired(String expDateVal) throws ConsentException {

        if (expDateVal != null && !expDateVal.isEmpty()) {
            try {
                OffsetDateTime expDate = OffsetDateTime.parse(expDateVal);
                return OffsetDateTime.now().isAfter(expDate);
            } catch (DateTimeParseException e) {
                log.error(ErrorConstants.EXP_DATE_PARSE_ERROR + " : " + expDateVal);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        ErrorConstants.EXP_DATE_PARSE_ERROR);
            }
        } else {
            return false;
        }

    }
}
