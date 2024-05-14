/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
package com.wso2.openbanking.accelerator.consent.extensions.utils;

/**
 * Constant class for OB Auth Servlet tests.
 */
public class AuthServletTestConstants {
    public static final String ACCOUNT_DATA = "{" +
            "   \"consentData\": [" +
            "       {" +
            "           \"data\":[" +
            "               \"ReadAccountsBasic\"," +
            "               \"ReadAccountsDetail\"," +
            "               \"ReadBalances\"," +
            "           ]," +
            "           \"title\":\"Permissions\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"2021-07-19T13:51:43.347+05:30\"]," +
            "           \"title\":\"Expiration Date Time\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"2021-07-14T13:51:43.397+05:30\"]," +
            "           \"title\":\"Transaction From Date Time\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"2021-07-17T13:51:43.397+05:30\"]," +
            "           \"title\":\"Transaction To Date Time\"}," +
            "   ]," +
            "   \"application\":\"9b5usDpbNtmxDcTzs7GzKp\"," +
            "   \"accounts\":[" +
            "       {" +
            "           \"accountId\":\"30080012343456\"," +
            "           \"account_id\":\"30080012343456\"," +
            "           \"authorizationMethod\":\"single\"," +
            "           \"accountName\":\"account_1\"," +
            "           \"nickName\":\"not-working\"," +
            "           \"display_name\":\"account_1\"" +
            "       }," +
            "       {" +
            "           \"accountId\":\"30080098763459\"," +
            "           \"account_id\":\"30080098763459\"," +
            "           \"authorizationMethod\":\"single\"," +
            "           \"accountName\":\"account_2\"," +
            "           \"display_name\":\"account_2\"" +
            "       }" +
            "   ]," +
            "   \"type\":\"accounts\"" +
            "}";

    public static final String COF_DATA = "{" +
            "   \"consentData\":[" +
            "       {" +
            "           \"data\":[\"2021-07-19T20:14:11.069+05:30\"]," +
            "           \"title\":\"Expiration Date Time\"" +
            "       }," +
            "       {" +
            "           \"data\":[" +
            "               \"Scheme Name : OB.SortCodeAccountNumber\"," +
            "               \"Identification : 1234\"," +
            "               \"Name : Account1\"," +
            "               \"Secondary Identification : Account1\"" +
            "           ]," +
            "           \"title\":\"Debtor Account\"" +
            "       }," +
            "   ]," +
            "   \"application\":\"9b5usDpbNtmxDcTzs7GzKp\"," +
            "   \"type\":\"fundsconfirmations\"," +
            "   \"debtor_account\":\"1234\"" +
            "}";

    public static final String PAYMENT_DATA = "{" +
            "   \"consentData\":[" +
            "       {" +
            "           \"data\":[\"Domestic Payments\"]," +
            "           \"title\":\"Payment Type\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"ACME412\"]," +
            "           \"title\":\"Instruction Identification\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"FRESCO.21302.GFX.20\"]," +
            "           \"title\":\"End to End Identification\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"Amount : 30.80\",\"Currency : GBP\"]," +
            "           \"title\":\"Instructed Amount\"" +
            "       }," +
            "       {" +
            "           \"data\":[" +
            "               \"Scheme Name : OB.SortCodeAccountNumber\"," +
            "               \"Identification : 30080012343456\"," +
            "               \"Name : Andrea Smith\"," +
            "               \"Secondary Identification : 30080012343456\"" +
            "           ]," +
            "           \"title\":\"Debtor Account\"" +
            "       }," +
            "       {" +
            "           \"data\":[" +
            "               \"Scheme Name : OB.SortCodeAccountNumber\"," +
            "               \"Identification : 08080021325698\"," +
            "               \"Name : ACME Inc\"," +
            "               \"Secondary Identification : 0002\"" +
            "           ]," +
            "           \"title\":\"Creditor Account\"" +
            "       }," +
            "   ]," +
            "   \"application\":\"9b5usDpbNtmxDcTzs7GzKp\"," +
            "   \"type\":\"payments\"," +
            "   \"debtor_account\":\"30080012343456\"" +
            "}";

    public static final String PAYMENT_DATA_WITHOUT_DEBTOR_ACC = "{\n" +
            "   \"consentData\":[\n" +
            "      {\n" +
            "         \"data\":[\n" +
            "            \"Domestic Payments\"\n" +
            "         ],\n" +
            "         \"title\":\"Payment Type\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"data\":[\n" +
            "            \"ACME412\"\n" +
            "         ],\n" +
            "         \"title\":\"Instruction Identification\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"data\":[\n" +
            "            \"FRESCO.21302.GFX.20\"\n" +
            "         ],\n" +
            "         \"title\":\"End to End Identification\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"data\":[\n" +
            "            \"Amount : 30.80\",\n" +
            "            \"Currency : GBP\"\n" +
            "         ],\n" +
            "         \"title\":\"Instructed Amount\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"data\":[\n" +
            "            \"Scheme Name : OB.SortCodeAccountNumber\",\n" +
            "            \"Identification : 08080021325698\",\n" +
            "            \"Name : ACME Inc\"\n" +
            "         ],\n" +
            "         \"title\":\"Creditor Account\"\n" +
            "      },\n" +
            "   ]," +
            "   \"type\":\"Payments\"\n" +
            "}";


    public static final String VRP_DATA = "{" +
            "   \"consentData\":[" +
            "       {" +
            "           \"data\":[\"Domestic VRP\"]," +
            "           \"title\":\"Payment Type\"" +
            "       }," +
            "       {" +
            "           \"data\":[" +
            "               \"Scheme Name : OB.SortCodeAccountNumber\"," +
            "               \"Identification : 30080012343456\"," +
            "               \"Name : Andrea Smith\"," +
            "               \"Secondary Identification : 30080012343456\"" +
            "           ]," +
            "           \"title\":\"Debtor Account\"" +
            "       }," +
            "       {" +
            "           \"data\":[" +
            "               \"Scheme Name : OB.SortCodeAccountNumber\"," +
            "               \"Identification : 08080021325698\"," +
            "               \"Name : ACME Inc\"," +
            "               \"Secondary Identification : 0002\"" +
            "           ]," +
            "           \"title\":\"Creditor Account\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"100\"]," +
            "           \"title\":\"Maximum amount per payment\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"Consent\"]," +
            "           \"title\":\"Period Alignment\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"200\"]," +
            "           \"title\":\"Maximum payment amount per Week\"" +
            "       }," +
            "   ]," +
            "   \"application\":\"9b5usDpbNtmxDcTzs7GzKp\"," +
            "   \"type\":\"vrp\"," +
            "   \"debtor_account\":\"30080012343456\"" +
            "}";


    public static final String VRP_DATA_WITHOUT_DEBTOR_ACC = "{" +
            "   \"consentData\":[" +
            "       {" +
            "           \"data\":[\"Domestic VRP\"]," +
            "           \"title\":\"Payment Type\"" +
            "       }," +
            "       {" +
            "           \"data\":[" +
            "               \"Scheme Name : OB.SortCodeAccountNumber\"," +
            "               \"Identification : 30080012343456\"," +
            "               \"Name : Andrea Smith\"," +
            "               \"Secondary Identification : 30080012343456\"" +
            "           ]," +
            "           \"title\":\"Debtor Account\"" +
            "       }," +
            "       {" +
            "           \"data\":[" +
            "               \"Scheme Name : OB.SortCodeAccountNumber\"," +
            "               \"Identification : 08080021325698\"," +
            "               \"Name : ACME Inc\"," +
            "               \"Secondary Identification : 0002\"" +
            "           ]," +
            "           \"title\":\"Creditor Account\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"100\"]," +
            "           \"title\":\"Maximum amount per payment\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"Consent\"]," +
            "           \"title\":\"Period Alignment\"" +
            "       }," +
            "       {" +
            "           \"data\":[\"200\"]," +
            "           \"title\":\"Maximum payment amount per Week\"" +
            "       }," +
            "   ]," +
            "   \"type\":\"vrp\"," +
            "}";

    public static final String JSON_WITH_TYPE = "{" +
            "   \"type\":\"test\"" +
            "}";
}
