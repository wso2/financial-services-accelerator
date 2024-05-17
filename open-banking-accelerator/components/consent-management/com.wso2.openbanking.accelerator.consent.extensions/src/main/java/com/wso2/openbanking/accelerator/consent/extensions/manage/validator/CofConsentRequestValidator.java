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

package com.wso2.openbanking.accelerator.consent.extensions.manage.validator;

import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.util.ConsentManageUtil;
import net.minidev.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Consent Manage validator class for Confirmation of Funds Request Validation.
 */
public class CofConsentRequestValidator {

    private static final Log log = LogFactory.getLog(CofConsentRequestValidator.class);

    /**
     * Method to validate COF initiation request.
     * @param initiation Initiation Object
     * @return JSONObject Validation Response
     */
    public static JSONObject validateCOFInitiation(JSONObject initiation) {

        JSONObject validationResponse = new JSONObject();

        //Check request body is valid and not empty
        if (!initiation.containsKey(ConsentExtensionConstants.DATA) ||
                !(initiation.get(ConsentExtensionConstants.DATA) instanceof JSONObject)) {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.RESOURCE_INVALID_FORMAT,
                    ErrorConstants.PAYLOAD_FORMAT_ERROR, ErrorConstants.PATH_REQUEST_BODY);
        }

        JSONObject data = (JSONObject) initiation.get(ConsentExtensionConstants.DATA);

        //Validate json payload expirationDateTime is a future date
        if (data.containsKey(ConsentExtensionConstants.EXPIRATION_DATE) && !ConsentManageUtil
                .isConsentExpirationTimeValid(data.getAsString(ConsentExtensionConstants.EXPIRATION_DATE))) {
            log.error(ErrorConstants.EXPIRED_DATE_ERROR);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID_DATE,
                    ErrorConstants.EXPIRED_DATE_ERROR, ErrorConstants.PATH_EXPIRATION_DATE);
        }


        if (data.containsKey(ConsentExtensionConstants.DEBTOR_ACC)) {

            Object debtorAccountObj = data.get(ConsentExtensionConstants.DEBTOR_ACC);
            //Check whether debtor account is a JsonObject
            if (!(debtorAccountObj instanceof JSONObject)) {
                log.error(ErrorConstants.MSG_INVALID_DEBTOR_ACC);
                return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.MSG_INVALID_DEBTOR_ACC, ErrorConstants.PATH_DEBTOR_ACCOUNT);
            }

            JSONObject debtorAccount = (JSONObject) data.get(ConsentExtensionConstants.DEBTOR_ACC);
            //Check whether debtor account is not empty
            if (debtorAccount.isEmpty()) {
                log.error(ErrorConstants.MSG_INVALID_DEBTOR_ACC);
                return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.MSG_INVALID_DEBTOR_ACC, ErrorConstants.PATH_DEBTOR_ACCOUNT);
            }

            //Check Debtor Account Scheme name exists
            if (!debtorAccount.containsKey(ConsentExtensionConstants.SCHEME_NAME) ||
                    StringUtils.isEmpty(debtorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME))) {
                log.error(ErrorConstants.MISSING_DEBTOR_ACC_SCHEME_NAME);
                return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.MISSING_DEBTOR_ACC_SCHEME_NAME, ErrorConstants.COF_PATH_DEBTOR_ACCOUNT_SCHEME);
            }

            //Validate Debtor Account Scheme name
            if (debtorAccount.containsKey(ConsentExtensionConstants.SCHEME_NAME) &&
                    (!(debtorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME) instanceof String) ||
                            !ConsentManageUtil.isDebtorAccSchemeNameValid(debtorAccount
                                    .getAsString(ConsentExtensionConstants.SCHEME_NAME)))) {
                log.error(ErrorConstants.INVALID_DEBTOR_ACC_SCHEME_NAME);
                return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID,
                        ErrorConstants.INVALID_DEBTOR_ACC_SCHEME_NAME, ErrorConstants.COF_PATH_DEBTOR_ACCOUNT_SCHEME);
            }

            //Check Debtor Account Identification existing
            if (!debtorAccount.containsKey(ConsentExtensionConstants.IDENTIFICATION) ||
                    StringUtils.isEmpty(debtorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION))) {
                log.error(ErrorConstants.MISSING_DEBTOR_ACC_IDENTIFICATION);
                return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.MISSING_DEBTOR_ACC_IDENTIFICATION,
                        ErrorConstants.COF_PATH_DEBTOR_ACCOUNT_IDENTIFICATION);
            }

            //Validate Debtor Account Identification
            if (debtorAccount.containsKey(ConsentExtensionConstants.IDENTIFICATION) &&
                    (!(debtorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION) instanceof String) ||
                            !ConsentManageUtil.isDebtorAccIdentificationValid(debtorAccount
                                    .getAsString(ConsentExtensionConstants.IDENTIFICATION)))) {
                log.error(ErrorConstants.INVALID_DEBTOR_ACC_IDENTIFICATION);
                return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID,
                        ErrorConstants.INVALID_DEBTOR_ACC_IDENTIFICATION,
                        ErrorConstants.COF_PATH_DEBTOR_ACCOUNT_IDENTIFICATION);
            }

            //Validate Debtor Account Name
            if (debtorAccount.containsKey(ConsentExtensionConstants.NAME) &&
                    (!(debtorAccount.getAsString(ConsentExtensionConstants.NAME) instanceof String) ||
                            !ConsentManageUtil.isDebtorAccNameValid(debtorAccount
                                    .getAsString(ConsentExtensionConstants.NAME)))) {
                log.error(ErrorConstants.INVALID_DEBTOR_ACC_NAME);
                return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID,
                        ErrorConstants.INVALID_DEBTOR_ACC_NAME, ErrorConstants.COF_PATH_DEBTOR_ACCOUNT_NAME);
            }

            //Validate Debtor Account Secondary Identification
            if (debtorAccount.containsKey(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) &&
                    (!(debtorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION)
                            instanceof String) ||
                            !ConsentManageUtil.isDebtorAccSecondaryIdentificationValid(debtorAccount
                                    .getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION)))) {
                log.error(ErrorConstants.INVALID_DEBTOR_ACC_SEC_IDENTIFICATION);
                return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID,
                        ErrorConstants.INVALID_DEBTOR_ACC_SEC_IDENTIFICATION,
                        ErrorConstants.COF_PATH_DEBTOR_ACCOUNT_SECOND_IDENTIFICATION);
            }
        } else {
            log.error(ErrorConstants.MSG_MISSING_DEBTOR_ACC);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_MISSING,
                    ErrorConstants.MSG_MISSING_DEBTOR_ACC, ErrorConstants.PATH_DEBTOR_ACCOUNT);
        }

        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;

    }
}

