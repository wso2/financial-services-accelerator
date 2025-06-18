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
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.wso2.financial.services.accelerator.demo.backend.BankException;

import java.util.UUID;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * FundsConfirmationService class.
 */
@Path("/fundsconfirmationservice/")
public class FundsConfirmationService {

    @SuppressFBWarnings("JAXRS_ENDPOINT")
    // Suppressed content - JAXRS_ENDPOINT
    // Suppression reason - False Positive : This endpoint is a demo endpoint that is not exposed in production
    // Suppressed warning count - 1

    @POST
    @Path("/funds-confirmations")
    @Produces("application/json; charset=utf-8")
    public Response getAccountBalance(String requestString,
                                      @HeaderParam("x-fapi-interaction-id") String xFapiInteractionId)
            throws BankException {

        JSONObject request;
        try {
            JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
            request = (JSONObject) parser.parse(requestString);
        } catch (ParseException e) {
            throw new BankException("Error in casting JSON body " + e);
        }

        if (xFapiInteractionId == null || xFapiInteractionId.isEmpty()) {
            xFapiInteractionId = UUID.randomUUID().toString();
        }

        String consentId = ((JSONObject) request.get("Data")).getAsString("ConsentId");
        String response = "{\n" +
                "  \"Data\": {\n" +
                "    \"FundsConfirmationId\": \"836403\",\n" +
                "    \"ConsentId\": \"" + consentId + "\",\n" +
                "    \"CreationDateTime\": \"2017-06-02T00:00:00+00:00\",\n" +
                "    \"FundsAvailable\": true,\n" +
                "    \"Reference\": \"Purchase02\",\n" +
                "    \"InstructedAmount\": {\n" +
                "       \"Amount\": \"20.00\",\n" +
                "       \"Currency\": \"USD\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"Links\": {\n" +
                "    \"Self\": \"https://api.alphabank.com/open-banking/v4.0/funds-confirmations/836403\"\n" +
                "  },\n" +
                "  \"Meta\": {\n" +
                "  }\n" +
                "}";
        return Response.status(201).entity(response)
                .header("x-fapi-interaction-id", xFapiInteractionId).build();
    }
}

