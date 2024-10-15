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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for consent manage module.
 */
public class ConsentManageUtils {

    private static final Log log = LogFactory.getLog(ConsentManageUtils.class);
    private static final FinancialServicesConfigParser parser = FinancialServicesConfigParser.getInstance();

    public static boolean isConsentExpirationTimeValid(String expDateVal) {

        if (expDateVal == null) {
            return true;
        }
        try {
            OffsetDateTime expDate = OffsetDateTime.parse(expDateVal);
            OffsetDateTime currDate = OffsetDateTime.now(expDate.getOffset());

            if (log.isDebugEnabled()) {
                log.debug(String.format("Provided expiry date is: %s current date is: %s", expDate, currDate));
            }
            return expDate.isAfter(currDate);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isTransactionFromToTimeValid(String fromDateVal, String toDateVal) {

        if (fromDateVal == null || toDateVal == null) {
            return true;
        }
        try {
            OffsetDateTime fromDate = OffsetDateTime.parse(fromDateVal);
            OffsetDateTime toDate = OffsetDateTime.parse(toDateVal);

            // From date is equal or earlier than To date
            return toDate.isEqual(fromDate) || toDate.isAfter(fromDate);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Utility class to check whether the Debtor Account Scheme name matches with Enum values.
     *
     * @param debtorAccSchemeName Debtor Account Scheme Name
     * @return  boolean Whether the Debtor Account Scheme name is valid
     */
    public static boolean isDebtorAccSchemeNameValid(String debtorAccSchemeName) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("debtorAccSchemeName: %s", debtorAccSchemeName.replaceAll("[\r\n]", "")));
        }

        EnumSet<DebtorAccountSchemeNameEnum> set = EnumSet.allOf(DebtorAccountSchemeNameEnum.class);
        boolean result = set.contains(DebtorAccountSchemeNameEnum.fromValue(debtorAccSchemeName));
        if (log.isDebugEnabled()) {
            log.debug(String.format("Result: %s", result));
        }
        return result;
    }

    /**
     * Utility class to check whether the Debtor Account Identification is valid.
     *
     * @param debtorAccIdentification Debtor Account Identification
     * @return  boolean Whether the Debtor Account Identification is valid
     */
    public static boolean isDebtorAccIdentificationValid(String debtorAccIdentification) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("debtorAccIdentification: %s", debtorAccIdentification.replaceAll("[\r\n]", "")));
        }

        return (debtorAccIdentification.length() <= 256);
    }

    /**
     * Utility class to check whether the Debtor Account Name is valid.
     *
     * @param debtorAccName Debtor Account Name
     * @return boolean Whether the Debtor Account Name is valid
     */
    public static boolean isDebtorAccNameValid(String debtorAccName) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("debtorAccName: %s", debtorAccName.replaceAll("[\r\n]", "")));
        }

        return (debtorAccName.length() <= 350);
    }

    /**
     * Utility class to check whether the Debtor AccountSecondary Identification is valid.
     *
     * @param debtorAccSecondaryIdentification Debtor Account Secondary Identification
     * @return boolean Whether the Debtor Account Secondary Identification is valid
     */
    public static boolean isDebtorAccSecondaryIdentificationValid(String debtorAccSecondaryIdentification) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("debtorAccSecondaryIdentification: %s",
                    debtorAccSecondaryIdentification.replaceAll("[\r\n]", "")));
        }

        return (debtorAccSecondaryIdentification.length() <= 34);
    }

    /**
     * Check whether the local instrument is supported.
     *
     * @param localInstrument Local Instrument value to validate
     * @return Whether the local instrument is valid
     */
    public static boolean validateLocalInstrument(String localInstrument) {
        ArrayList<String> defaultLocalInstrumentList = new ArrayList<>(Arrays.asList(
                "OB.BACS",
                "OB.BalanceTransfer", "OB.CHAPS", "OB.Euro1", "OB.FPS", "OB.Link",
                "OB.MoneyTransfer", "OB.Paym", "OB.SEPACreditTransfer",
                "OB.SEPAInstantCreditTransfer", "OB.SWIFT", "OB.Target2"));

        return defaultLocalInstrumentList.contains(localInstrument);

    }

    /**
     * Check whether the amount is higher that the max instructed amount allowed by the bank.
     *
     * @param instructedAmount Instructed Amount to validate
     * @return Whether the instructed amount is valid
     */
    public static boolean validateMaxInstructedAmount(String instructedAmount) {
        //This is a mandatory configuration in finanical-services.xml. Hence can't be null.
        String maxInstructedAmount = (String) parser.getConfiguration().get(
                FinancialServicesConstants.MAX_INSTRUCTED_AMOUNT);
        return Double.parseDouble(instructedAmount) <= Double.parseDouble(maxInstructedAmount);

    }

    /**
     * Specifies the Schema Names of Debtor Account.
     */
    public enum DebtorAccountSchemeNameEnum {

        BBAN("OB.BBAN"),

        IBAN("OB.IBAN"),

        PAN("OB.PAN"),

        PAYM("OB.Paym"),

        SORT_CODE_NUMBER("OB.SortCodeAccountNumber");

        private String value;

        DebtorAccountSchemeNameEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static DebtorAccountSchemeNameEnum fromValue(String text) {

            List<DebtorAccountSchemeNameEnum> accountList = Arrays.asList(DebtorAccountSchemeNameEnum.values());
            Optional<DebtorAccountSchemeNameEnum> accountOpt = accountList
                    .stream()
                    .filter(i -> String.valueOf(i.value).equals(text))
                    .findAny();

            return accountOpt.orElse(null);
        }

    }
}
