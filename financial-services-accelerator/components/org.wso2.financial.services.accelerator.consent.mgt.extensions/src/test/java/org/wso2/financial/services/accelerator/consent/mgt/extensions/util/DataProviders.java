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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.util;

import org.testng.annotations.DataProvider;

/**
 * Data Providers for Consent Extension Tests.
 */
public class DataProviders {

    @DataProvider(name = "AccountInitiationDataProvider")
    Object[][] getAccountInitiationDataProvider() {

        return new Object[][]{
                {"{}"},
                {INITIATION_WITHOUT_ACCOUNT_PERMISSION},
                {INITIATION_WITH_UNACCEPTABLE_PERMISSION},
                {INITIATION_WITHOUT_EXPIRATION_DATES},
                {INITIATION_NON_STRING_EXPIRATION_DATES},
                {INITIATION_WITH_INVALID_EXPIRATION_DATES},
                {INITIATION_WITH_PAST_EXPIRATION_DATES},
                {INITIATION_WITHOUT_TRANS_FROM_DATES},
                {INITIATION_NON_STRING_TRANS_FROM_DATES},
                {INITIATION_WITH_INVALID_TRANS_FROM_DATES},
                {INITIATION_WITHOUT_TRANS_TO_DATES},
                {INITIATION_NON_STRING_TRANS_TO_DATES},
                {INITIATION_WITH_INVALID_TRANS_TO_DATES},
                {INITIATION_WITH_PAST_TRANS_TO_DATES}

        };
    }

    public static final String INITIATION_WITHOUT_ACCOUNT_PERMISSION = "{\"Data\": {\"Permissions\": " +
            "[\"ReadBalances\",\"ReadBeneficiariesDetail\",\"ReadDirectDebits\",\"ReadProducts\"," +
            "\"ReadStandingOrdersDetail\",\"ReadTransactionsCredits\",\"ReadTransactionsDebits\"," +
            "\"ReadTransactionsDetail\",\"ReadOffers\",\"ReadPAN\",\"ReadParty\",\"ReadPartyPSU\"," +
            " \"ReadScheduledPaymentsDetail\",\"ReadStatementsDetail\"],\"ExpirationDateTime\": " +
            "\"" + TestConstants.EXP_DATE + "\",\"TransactionFromDateTime\": \"2021-05-03T00:00:00+00:00\"," +
            "\"TransactionToDateTime\": \"2021-12-03T00:00:00+00:00\"},\"Risk\": {}}";

    public static final String INITIATION_WITH_UNACCEPTABLE_PERMISSION = "{\"Data\": {\"Permissions\": " +
            "[\"ReadBalances\",\"ReadBeneficiariesDetail\",\"ReadDirectDebits\",\"ReadProducts\"," +
            "\"ReadStandingOrdersDetail\",\"ReadTransactionsDetail\",\"ReadOffers\",\"ReadPAN\",\"ReadParty\"," +
            "\"ReadPartyPSU\", \"ReadScheduledPaymentsDetail\",\"ReadStatementsDetail\"],\"ExpirationDateTime\": " +
            "\"" + TestConstants.EXP_DATE + "\",\"TransactionFromDateTime\": \"2021-05-03T00:00:00+00:00\"," +
            "\"TransactionToDateTime\": \"2021-12-03T00:00:00+00:00\"},\"Risk\": {}}";

    public static final String INITIATION_WITHOUT_EXPIRATION_DATES = "{\"Data\": {\"Permissions\": " +
            "[\"ReadAccountsDetail\",\"ReadBalances\"]},\"Risk\": {}}";

    public static final String INITIATION_NON_STRING_EXPIRATION_DATES = "{\"Data\": {\"Permissions\": " +
            "[\"ReadAccountsDetail\",\"ReadBalances\"],\"ExpirationDateTime\": " +
            "{},\"TransactionFromDateTime\": {}, \"TransactionToDateTime\": {}},\"Risk\": {}}";

    public static final String INITIATION_WITH_INVALID_EXPIRATION_DATES = "{\"Data\": {\"Permissions\": " +
            "[\"ReadAccountsDetail\",\"ReadBalances\"],\"ExpirationDateTime\": " +
            "\"20220502\",\"TransactionFromDateTime\": \"20210503\"," +
            "\"TransactionToDateTime\": \"20211230\"},\"Risk\": {}}";

    public static final String INITIATION_WITH_PAST_EXPIRATION_DATES = "{\"Data\": {\"Permissions\": " +
            "[\"ReadAccountsDetail\",\"ReadBalances\"],\"ExpirationDateTime\": " +
            "\"2020-05-02T00:00:00+00:00\",\"TransactionFromDateTime\": \"2021-05-03T00:00:00+00:00\"," +
            "\"TransactionToDateTime\": \"2021-03-03T00:00:00+00:00\"},\"Risk\": {}}";

    public static final String INITIATION_WITHOUT_TRANS_FROM_DATES = "{\"Data\": {\"Permissions\": " +
            "[\"ReadAccountsDetail\",\"ReadBalances\"], \"ExpirationDateTime\":" +
            "\"" + TestConstants.EXP_DATE + "\"},\"Risk\": {}}";

    public static final String INITIATION_NON_STRING_TRANS_FROM_DATES = "{\"Data\": {\"Permissions\": " +
            "[\"ReadAccountsDetail\",\"ReadBalances\"],\"ExpirationDateTime\":" +
            "\"" + TestConstants.EXP_DATE + "\",\"TransactionFromDateTime\": {}, \"TransactionToDateTime\": {}}," +
            "\"Risk\": {}}";

    public static final String INITIATION_WITH_INVALID_TRANS_FROM_DATES = "{\"Data\": {\"Permissions\": " +
            "[\"ReadAccountsDetail\",\"ReadBalances\"],\"ExpirationDateTime\":" +
            " \"" + TestConstants.EXP_DATE + "\",\"TransactionFromDateTime\": \"20210503\"," +
            "\"TransactionToDateTime\": \"20211230\"},\"Risk\": {}}";

    public static final String INITIATION_WITHOUT_TRANS_TO_DATES = "{\"Data\": {\"Permissions\": " +
            "[\"ReadAccountsDetail\",\"ReadBalances\"], \"ExpirationDateTime\":" +
            "\"" + TestConstants.EXP_DATE + "\", \"TransactionFromDateTime\": \"2021-05-03T00:00:00+00:00\"}," +
            "\"Risk\": {}}";

    public static final String INITIATION_NON_STRING_TRANS_TO_DATES = "{\"Data\": {\"Permissions\": " +
            "[\"ReadAccountsDetail\",\"ReadBalances\"],\"ExpirationDateTime\":" +
            "\"" + TestConstants.EXP_DATE + "\",\"TransactionFromDateTime\": \"2021-05-03T00:00:00+00:00\", " +
            "\"TransactionToDateTime\": {}},\"Risk\": {}}";

    public static final String INITIATION_WITH_INVALID_TRANS_TO_DATES = "{\"Data\": {\"Permissions\": " +
            "[\"ReadAccountsDetail\",\"ReadBalances\"],\"ExpirationDateTime\":" +
            " \"" + TestConstants.EXP_DATE + "\",\"TransactionFromDateTime\": \"2021-05-03T00:00:00+00:00\"," +
            "\"TransactionToDateTime\": \"20211230\"},\"Risk\": {}}";

    public static final String INITIATION_WITH_PAST_TRANS_TO_DATES = "{\"Data\": {\"Permissions\": " +
            "[\"ReadAccountsDetail\",\"ReadBalances\"],\"ExpirationDateTime\": " +
            "\"" + TestConstants.EXP_DATE + "\",\"TransactionFromDateTime\": \"2021-05-03T00:00:00+00:00\"," +
            "\"TransactionToDateTime\": \"2021-03-03T00:00:00+00:00\"},\"Risk\": {}}";
}
