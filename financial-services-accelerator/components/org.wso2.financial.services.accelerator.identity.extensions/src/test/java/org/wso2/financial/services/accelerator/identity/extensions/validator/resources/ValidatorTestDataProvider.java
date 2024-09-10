package org.wso2.financial.services.accelerator.identity.extensions.validator.resources;
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
import org.testng.annotations.DataProvider;

/**
 * validator test data provider resource.
 */
public class ValidatorTestDataProvider {

    private String claimsTemplate = "{\n" +
            "  \"aud\": \"https://localhost:8243/token\",\n" +
            "  \"response_type\": \"code id_token\",\n" +
            "  \"client_id\": \"iTKOfuqz46Y1HVY2BF0Z7JM18Awa\",\n" +
            "  \"redirect_uri\": \"https://localhost/test/a/app1/callback\",\n" +
            "  \"scope\": \"${SCOPE}\",\n" +
            "  \"state\": \"af0ifjsldkj\",\n" +
            "  \"nonce\": \"n-0S6_WzA2Mj\",\n" +
            "  \"claims\": {\n" +
            "    \"sharing_duration\": \"7200\",\n" +
            "    \"id_token\": {\n" +
            "      \"acr\": {\n" +
            "        \"essential\": true,\n" +
            "        \"values\": [\n" +
            "          \"urn:cds.au:cdr:3\"\n" +
            "        ]\n" +
            "      }\n" +
            "    },\n" +
            "    \"userinfo\": {\n" +
            "      \"given_name\": null,\n" +
            "      \"family_name\": null\n" +
            "    }\n" +
            "  }\n" +
            "}";


    @DataProvider(name = "dp-checkValidScopeFormat")
    public Object[][] dpCheckValidScopeFormat() {

        return new Object[][]{
                {claimsTemplate.replace("${SCOPE}",
                "openid bank:accounts.basic:read bank:accounts.detail:read bank:transactions:read")},
                {claimsTemplate.replace("${SCOPE}",
                        "openid bank:accounts.basic:read")},
                {claimsTemplate.replace("${SCOPE}",
                        "  openid bank:accounts.basic:read  bank:accounts.detail:read bank:transactions:read  ")},
                {claimsTemplate.replace("${SCOPE}",
                        "openid profile bank:accounts.basic:read bank:accounts.detail:read bank:transactions:read")}
        };
    }

    @DataProvider(name = "dp-checkValidSingleScopes")
    public Object[][] dpCheckValidSingleScopes() {

        return new Object[][]{
                {claimsTemplate.replace("${SCOPE}",
                        "openid bank:accounts.basic:read")},
                {claimsTemplate.replace("${SCOPE}",
                        "openid bank:accounts.detail:read")},
                {claimsTemplate.replace("${SCOPE}",
                        "openid bank:transactions:read")},
                {claimsTemplate.replace("${SCOPE}",
                        "openid bank:payees:read")},
                {claimsTemplate.replace("${SCOPE}",
                        "openid bank:regular_payments:read")},
                {claimsTemplate.replace("${SCOPE}",
                        "openid common:customer.basic:read")},
                {claimsTemplate.replace("${SCOPE}",
                        "openid common:customer.detail:read")},

                //
                {claimsTemplate.replace("${SCOPE}",
                        "accounts openid")},
                {claimsTemplate.replace("${SCOPE}",
                        "openid payments")},
                {claimsTemplate.replace("${SCOPE}",
                        "openid fundsconfirmations")},

                //
                {claimsTemplate.replace("${SCOPE}",
                        "ais openid")},
                {claimsTemplate.replace("${SCOPE}",
                        "pis openid")}
        };
    }

    @DataProvider(name = "dp-checkInValidScopeFormat")
    public Object[][] dpCheckInValidScopeFormat() {

        return new Object[][]{
                {claimsTemplate.replace("${SCOPE}",
                        "bank:accounts.basic:read")},
                {claimsTemplate.replace("${SCOPE}",
                        "openid")},
                {claimsTemplate.replace("${SCOPE}",
                        "openid bank:accounts.basic")},
                {claimsTemplate.replace("${SCOPE}",
                        "openid xyz")},
                {claimsTemplate.replace("${SCOPE}",
                        "xyz")},
                {claimsTemplate.replace("${SCOPE}",
                        "openid bank:accounts.basic")},
                {claimsTemplate.replace("${SCOPE}",
                        "openid common:customer.detail:read")},

                //
                {claimsTemplate.replace("${SCOPE}",
                        "Accounts openid")},
                {claimsTemplate.replace("${SCOPE}",
                        "openid Payments")},
                {claimsTemplate.replace("${SCOPE}",
                        "openid FundsConfirmations")},

                //
                {claimsTemplate.replace("${SCOPE}",
                        "AIS openid")},
                {claimsTemplate.replace("${SCOPE}",
                        "PIS openid")}
        };
    }

    @DataProvider(name = "dp-checkValidationsInherited")
    public Object[][] dpCheckValidationsInherited() {

        return new Object[][]{
                {claimsTemplate.replace("${SCOPE}",
                        "sactions:read")}
        };
    }

    @DataProvider(name = "dp-checkMandatoryParamsValidationFailing")
    public Object[][] dpCheckMandatoryParamsValidationFailing() {

        return new Object[][]{
                {claimsTemplate.replace("${SCOPE}",
                        "openid bank:accounts.basic:read")}
        };
    }

}
