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
import org.apache.commons.lang3.StringUtils;
import org.wso2.financial.services.accelerator.demo.backend.BankException;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Payments Service class.
 */
@Path("/paymentservice/")
public class PaymentService {

    public static final String EXPECTED_EXECUTION_TIME = "ExpectedExecutionDateTime";
    public static final String EXPECTED_SETTLEMENT_TIME = "ExpectedSettlementDateTime";
    private static final Map<String, JSONObject> domesticPayments = new HashMap<>();
    private static final int MAX_LIMIT = 500;
    private static final Queue<String> domesticPaymentsIdQueue = new LinkedList<>();

    @SuppressFBWarnings("JAXRS_ENDPOINT")
    // Suppressed content - Endpoint
    // Suppression reason - False Positive : This endpoint is a demo endpoint that is not exposed in production
    // Suppressed warning count - 1
    @GET
    @Path("/payment-consents/{ConsentId}/funds-confirmation")
    @Produces("application/json; charset=utf-8")
    public Response getPaymentTypeFundsConfirmation(@PathParam("ConsentId") String paymentId) {

        Instant currentDate = Instant.now();

        String response = "{\n" +
                "    \"Data\": {\n" +
                "        \"FundsAvailableResult\": {\n" +
                "            \"FundsAvailableDateTime\": \"" + currentDate.toString() + "\",\n" +
                "            \"FundsAvailable\": true\n" +
                "        }\n" +
                "    },\n" +
                "    \"Links\": {\n" +
                "        \"Self\": \"/pisp/payments/" + paymentId + "/funds-confirmation\"\n" +
                "    },\n" +
                "    \"Meta\": {}\n" +
                "}";

        return Response.status(200).entity(response)
                .header("x-fapi-interaction-id", UUID.randomUUID().toString())
                .build();
    }

    @SuppressFBWarnings("JAXRS_ENDPOINT")
    // Suppressed content - Endpoint
    // Suppression reason - False Positive : This endpoint is a demo endpoint that is not exposed in production
    // Suppressed warning count - 1

    @POST
    @Path("/payments")
    @Produces("application/json; charset=utf-8")
    public Response paymentSubmission(String requestString, @HeaderParam("x-fapi-interaction-id") String fid,
                                      @HeaderParam("Account-Request-Information") String accountRequestInfo)
            throws BankException {

        JSONObject jsonObject;
        JSONObject accountRequestInformation;

        try {
            accountRequestInformation = getRequest(accountRequestInfo);
            JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
            jsonObject = (JSONObject) parser.parse(requestString);
        } catch (ParseException e) {
            throw new BankException("Error in casting JSON body " + e);
        }

        if (fid == null || fid.isEmpty()) {
            fid = UUID.randomUUID().toString();
        }

        JSONObject additionalConsentInfo = (JSONObject) accountRequestInformation.get("additionalConsentInfo");

        JSONObject response = cacheAndGetPaymentResponse(jsonObject, additionalConsentInfo);
        return Response.status(201).entity(response.toString())
                .header("x-fapi-interaction-id", fid)
                .build();

    }

    @SuppressFBWarnings("JAXRS_ENDPOINT")
    // Suppressed content - Endpoint
    // Suppression reason - False Positive : This endpoint is a demo endpoint that is not exposed in production
    // Suppressed warning count - 1

    @GET
    @Path("/payments/{paymentId}")
    @Produces("application/json; charset=utf-8")
    public Response getPaymentTypePayment(@PathParam("paymentId") String paymentId) {

        JSONObject responseObject = null;
        if (StringUtils.isNotBlank(paymentId)) {

            responseObject = domesticPayments.get(paymentId);

        }
        if (responseObject == null) {
            responseObject = new JSONObject();
        }


        return Response.status(200).entity(responseObject.toString())
                .header("x-fapi-interaction-id", "93bac548-d2de-4546-b106-880a5018460d")
                .build();
    }

    private static JSONObject getRequest(String json) throws ParseException {

        String[] splitString = json.split("\\.");
        String base64EncodedBody = splitString[1];
        String decodedString = null;
        decodedString = new String(Base64.getUrlDecoder()
                .decode(base64EncodedBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(decodedString);
        return jsonObject;
    }

    @SuppressFBWarnings("PREDICTABLE_RANDOM")
    // Suppressed content - PREDICTABLE_RANDOM
    // Suppression reason - False Positive : This endpoint is a demo endpoint that is not exposed in production
    // Suppressed warning count - 1
    private JSONObject cacheAndGetPaymentResponse(JSONObject requestObject,
                                                  JSONObject additionalConsentInfo)
            throws BankException {

        JSONObject responseObject;

        int randomPIN = new Random().nextInt(100);

        String status;
        String paymentIdValue;

        paymentIdValue = ((JSONObject) requestObject.get("Data")).getAsString("ConsentId");
        paymentIdValue = paymentIdValue + "-" + randomPIN;

        status = "AcceptedSettlementCompleted";


        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        Date date = new Date();
        String currentDate = dateFormat.format(date);

        String readRefundAccount = additionalConsentInfo.getAsString("ReadRefundAccount");
        String cutOffTimeAcceptable = additionalConsentInfo.getAsString("CutOffTimeAcceptable");

        try {
            JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
            responseObject = (JSONObject) parser.parse(requestObject.toString());

            JSONObject dataObject = (JSONObject) responseObject.get("Data");

            dataObject.put("PaymentId", paymentIdValue);
            dataObject.put("Status", status);
            dataObject.put("CreationDateTime", currentDate);
            dataObject.put("StatusUpdateDateTime", currentDate);

            // Add refund account details if requested during consent initiation
            if (Boolean.parseBoolean(readRefundAccount)) {
                addRefundAccount(dataObject);
            }

            JSONObject linksObject = new JSONObject();
            linksObject.put("Self", "/payments/" + paymentIdValue);
            responseObject.put("Links", linksObject);

            JSONObject metaObject = new JSONObject();
            responseObject.put("Meta", metaObject);

            responseObject.remove("Risk");

        } catch (ParseException e) {
            throw new BankException(e);
        }
        addToCache(paymentIdValue, responseObject);
        return responseObject;
    }

    /**
     * Add Refund account details to the response.
     *
     * @param dataObject
     */
    private void addRefundAccount(JSONObject dataObject) {

        String schemeName = "OB.SortCodeAccountNumber";
        String identification = "Identification";
        String name = "NTPC Inc";

        JSONObject accountData = new JSONObject();
        accountData.put("SchemeName", schemeName);
        accountData.put("Identification", identification);
        accountData.put("Name", name);

        JSONObject account = new JSONObject();
        account.put("Account", accountData);

        dataObject.put("Refund", account);
    }

    private void addToCache(String paymentIdValue, JSONObject responseObject) {

        if (domesticPayments.size() > MAX_LIMIT) {
            // Max limit reached
            domesticPayments.remove(domesticPaymentsIdQueue.poll());
        }
        domesticPayments.put(paymentIdValue, responseObject);
        domesticPaymentsIdQueue.add(paymentIdValue);

    }
}
