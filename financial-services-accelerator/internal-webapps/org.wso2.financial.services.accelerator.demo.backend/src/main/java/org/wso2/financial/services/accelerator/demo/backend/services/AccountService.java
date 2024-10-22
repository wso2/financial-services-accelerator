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

package org.wso2.financial.services.accelerator.demo.backend.services;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.wso2.financial.services.accelerator.demo.backend.BankException;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * AccountService class.
 */
@Path("/accountservice/")
public class AccountService {

    @SuppressFBWarnings("JAXRS_ENDPOINT")
    // Suppressed content - Endpoint
    // Suppression reason - False Positive : This endpoint is a demo endpoint that is not exposed in production
    // Suppressed warning count - 1
    @GET
    @Path("/accounts")
    @Produces("application/json; charset=utf-8")
    public Response getAccounts(@HeaderParam("x-fapi-interaction-id") String xFapiInteractionId,
                                @HeaderParam("Account-Request-Information") String accountRequestInfo) {

        String finalRespose = "{\n" +
                "  \"Data\": {\n" +
                "    \"Account\": [\n" +
                "      {\n" +
                "        \"AccountId\": \"30080012343456\",\n" +
                "        \"Status\": \"Enabled\",\n" +
                "        \"StatusUpdateDateTime\": \"2020-04-16T06:06:06+00:00\",\n" +
                "        \"Currency\": \"GBP\",\n" +
                "        \"AccountType\": \"Personal\",\n" +
                "        \"AccountSubType\": \"CurrentAccount\",\n" +
                "        \"Nickname\": \"Bills\",\n" +
                "        \"OpeningDate\": \"2020-01-16T06:06:06+00:00\",\n" +
                "        \"MaturityDate\": \"2025-04-16T06:06:06+00:00\",\n" +
                "        \"Account\": [{\n" +
                "          \"SchemeName\": \"SortCodeAccountNumber\",\n" +
                "          \"Identification\": \"30080012343456\",\n" +
                "          \"Name\": \"Mr Kevin\",\n" +
                "          \"SecondaryIdentification\": \"00021\"\n" +
                "        }]\n" +
                "      },\n" +
                "      {\n" +
                "        \"AccountId\": \"30080098763459\",\n" +
                "        \"Status\": \"Enabled\",\n" +
                "        \"StatusUpdateDateTime\": \"2020-04-16T06:06:06+00:00\",\n" +
                "        \"Currency\": \"GBP\",\n" +
                "        \"AccountType\": \"Personal\",\n" +
                "        \"AccountSubType\": \"CurrentAccount\",\n" +
                "        \"Nickname\": \"Bills\",\n" +
                "        \"OpeningDate\": \"2020-01-16T06:06:06+00:00\",\n" +
                "        \"MaturityDate\": \"2025-04-16T06:06:06+00:00\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"Links\": {\n" +
                "    \"Self\": \"https://api.alphabank.com/open-banking/v4.0/accounts\"\n" +
                "  },\n" +
                "  \"Meta\": {\n" +
                "    \"TotalPages\": 1\n" +
                "  }\n" +
                "}";

        if (xFapiInteractionId == null) {
            xFapiInteractionId = UUID.randomUUID().toString();
        }
        return Response.status(200).entity(finalRespose)
                .header("x-fapi-interaction-id", xFapiInteractionId).build();
    }

    @SuppressFBWarnings("JAXRS_ENDPOINT")
    // Suppressed content - Endpoint
    // Suppression reason - False Positive : This endpoint is a demo endpoint that is not exposed in production
    // Suppressed warning count - 1
    @GET
    @Path("/accounts/{AccountId}")
    @Produces("application/json; charset=utf-8")
    public Response getOneAccount(@PathParam("AccountId") String accountId,
                                  @HeaderParam("x-fapi-interaction-id") String xFapiInteractionId,
                                  @HeaderParam("Account-Request-Information") String accountRequestInfo)
            throws BankException {

        String response = "{\n" +
                "  \"Data\": {\n" +
                "    \"Account\": [\n" +
                "      {\n" +
                "        \"AccountId\": \"" + accountId + "\",\n" +
                "        \"Status\": \"Enabled\",\n" +
                "        \"StatusUpdateDateTime\": \"2020-04-16T06:06:06+00:00\",\n" +
                "        \"Currency\": \"GBP\",\n" +
                "        \"AccountType\": \"Personal\",\n" +
                "        \"AccountSubType\": \"CurrentAccount\",\n" +
                "        \"Nickname\": \"Bills\",\n" +
                "        \"Account\": [{\n" +
                "          \"SchemeName\": \"SortCodeAccountNumber\",\n" +
                "          \"Identification\": \"" + accountId + "\",\n" +
                "          \"Name\": \"Mr Kevin\",\n" +
                "          \"SecondaryIdentification\": \"00021\"\n" +
                "        }]\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"Links\": {\n" +
                "    \"Self\": \"https://api.alphabank.com/open-banking/v4.0/accounts/" + accountId +
                "\"\n" +
                "  },\n" +
                "  \"Meta\": {\n" +
                "    \"TotalPages\": 1\n" +
                "  }\n" +
                "}";

        if (xFapiInteractionId == null) {
            xFapiInteractionId = UUID.randomUUID().toString();
        }
        return Response.status(200).entity(response)
                .header("x-fapi-interaction-id", xFapiInteractionId).build();

    }

    @SuppressFBWarnings("JAXRS_ENDPOINT")
    // Suppressed content - Endpoint
    // Suppression reason - False Positive : This endpoint is a demo endpoint that is not exposed in production
    // Suppressed warning count - 1
    @GET
    @Path("/accounts/{AccountId}/transactions")
    @Produces("application/json; charset=utf-8")
    public Response getAccountTransactions(@PathParam("AccountId") String accountId,
                                           @HeaderParam("x-fapi-interaction-id") String xFapiInteractionId,
                                           @HeaderParam("Account-Request-Information") String accountRequestInfo)
            throws BankException {

        String response = "{\n" +
                "  \"Data\": {\n" +
                "    \"Transaction\": [\n" +
                "      {\n" +
                "        \"AccountId\": \"" + accountId + "\",\n" +
                "        \"TransactionId\": \"123\",\n" +
                "        \"TransactionReference\": \"Ref 1\",\n" +
                "        \"Amount\": {\n" +
                "          \"Amount\": \"10.00\",\n" +
                "          \"Currency\": \"GBP\"\n" +
                "        },\n" +
                "        \"CreditDebitIndicator\": \"" + "Credit" + "\",\n" +
                "        \"Status\": \"Booked\",\n" +
                "        \"BookingDateTime\": \"2017-04-05T10:43:07+00:00\",\n" +
                "        \"ValueDateTime\": \"2017-04-05T10:45:22+00:00\",\n" +
                "        \"TransactionInformation\": \"Cash from Aubrey\",\n" +
                "        \"BankTransactionCode\": {\n" +
                "          \"Code\": \"str\",\n" +
                "          \"SubCode\": \"str\"\n" +
                "        },\n" +
                "        \"ProprietaryBankTransactionCode\": {\n" +
                "          \"Code\": \"Transfer\",\n" +
                "          \"Issuer\": \"AlphaBank\"\n" +
                "        },\n" +
                "        \"Balance\": {\n" +
                "          \"Amount\": {\n" +
                "            \"Amount\": \"230.00\",\n" +
                "            \"Currency\": \"GBP\"\n" +
                "          },\n" +
                "          \"CreditDebitIndicator\": \"Credit\",\n" +
                "          \"Type\": \"InterimBooked\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"Links\": {\n" +
                "    \"Self\": \"https://api.alphabank.com/open-banking/v4.0/accounts/" + accountId +
                "/transactions/\"\n" +
                "  },\n" +
                "  \"Meta\": {\n" +
                "    \"TotalPages\": 1,\n" +
                "    \"FirstAvailableDateTime\": \"2017-05-03T00:00:00+00:00\",\n" +
                "    \"LastAvailableDateTime\": \"2017-12-03T00:00:00+00:00\"\n" +
                "  }\n" +
                "}";
        if (xFapiInteractionId == null) {
            xFapiInteractionId = UUID.randomUUID().toString();
        }
        return Response.status(200).entity(response)
                .header("x-fapi-interaction-id", xFapiInteractionId).build();
    }

    @SuppressFBWarnings("JAXRS_ENDPOINT")
    // Suppressed content - Endpoint
    // Suppression reason - False Positive : This endpoint is a demo endpoint that is not exposed in production
    // Suppressed warning count - 1
    @GET
    @Path("/accounts/{AccountId}/balances")
    @Produces("application/json; charset=utf-8")
    public Response getAccountBalance(@PathParam("AccountId") String accountId,
                                      @HeaderParam("x-fapi-interaction-id") String xFapiInteractionId,
                                      @HeaderParam("Account-Request-Information") String accountRequestInfo)
            throws BankException {

        String response = "{\n" +
                "  \"Data\": {\n" +
                "    \"Balance\": [\n" +
                "      {\n" +
                "        \"AccountId\": \"" + accountId + "\",\n" +
                "        \"Amount\": {\n" +
                "          \"Amount\": \"1230.00\",\n" +
                "          \"Currency\": \"GBP\"\n" +
                "        },\n" +
                "        \"CreditDebitIndicator\": \"Credit\",\n" +
                "        \"Type\": \"InterimAvailable\",\n" +
                "        \"DateTime\": \"2017-04-05T10:43:07+00:00\",\n" +
                "        \"CreditLine\": [\n" +
                "          {\n" +
                "            \"Included\": true,\n" +
                "            \"Amount\": {\n" +
                "              \"Amount\": \"1000.00\",\n" +
                "              \"Currency\": \"GBP\"\n" +
                "            },\n" +
                "            \"Type\": \"Pre-Agreed\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"Links\": {\n" +
                "    \"Self\": \"https://api.alphabank.com/open-banking/v4.0/accounts/" + accountId +
                "/balances/\"\n" +
                "  },\n" +
                "  \"Meta\": {\n" +
                "    \"TotalPages\": 1\n" +
                "  }\n" +
                "}";
        if (xFapiInteractionId == null) {
            xFapiInteractionId = UUID.randomUUID().toString();
        }
        return Response.status(200).entity(response)
                .header("x-fapi-interaction-id", xFapiInteractionId).build();
    }
}
