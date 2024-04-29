package com.wso2.openbanking.accelerator.demo.backend.services;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.demo.backend.BankException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
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
 * Vrp Service class.
 */
@Path("/vrpservice/")
public class VrpService {

    public static final String EXPECTED_EXECUTION_TIME = "ExpectedExecutionDateTime";
    public static final String EXPECTED_SETTLEMENT_TIME = "ExpectedSettlementDateTime";
    private static final int MAX_LIMIT = 500;
    private static final Queue<String> domesticVRPsIdQueue = new LinkedList<>();
    private static final Map<String, JSONObject> domesticVRPs = new HashMap<>();


    @SuppressFBWarnings("JAXRS_ENDPOINT")
    // Suppressed content - Endpoint
    // Suppression reason - False Positive : This endpoint is a demo endpoint that is not exposed in production
    // Suppressed warning count - 1
    @GET
    @Path("/domestic-vrp-consents/{ConsentId}/funds-confirmation")
    @Produces("application/json; charset=utf-8")
    public Response getPaymentTypeFundsConfirmation(@PathParam("ConsentId") String domesticVRPId) {

        Instant currentDate = Instant.now();

        String response = "{\n" +
                "    \"Data\": {\n" +
                "        \"FundsAvailableResult\": {\n" +
                "            \"FundsAvailableDateTime\": \"" + currentDate.toString() + "\",\n" +
                "            \"FundsAvailable\": true\n" +
                "        }\n" +
                "    },\n" +
                "    \"Links\": {\n" +
                "        \"Self\": \"/vrp/domestic-vrps/" + domesticVRPId + "/funds-confirmation\"\n" +
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
    @Path("/domestic-vrps")
    @Produces("application/json; charset=utf-8")
    public Response paymentSubmission(String requestString, @PathParam("paymentType") String paymentType,
                                      @HeaderParam("x-fapi-interaction-id") String fid,
                                      @HeaderParam("Account-Request-Information") String accountRequestInfo)
            throws BankException {

        JSONObject jsonObject;
        JSONObject accountRequestInformation;

        try {
            accountRequestInformation = getRequest(paymentType, accountRequestInfo);
            JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
            jsonObject = (JSONObject) parser.parse(requestString);
        } catch (ParseException e) {
            throw new BankException("Error in casting JSON body " + e);
        }

        JSONObject additionalConsentInfo = (JSONObject) accountRequestInformation.get("additionalConsentInfo");

        JSONObject response = cacheAndGetPaymentResponse(paymentType, jsonObject, additionalConsentInfo);
        return Response.status(201).entity(response.toString())
                .header("x-fapi-interaction-id", fid)
                .build();

    }

    @SuppressFBWarnings("JAXRS_ENDPOINT")
    // Suppressed content - Endpoint
    // Suppression reason - False Positive : This endpoint is a demo endpoint that is not exposed in production
    // Suppressed warning count - 1

    @GET
    @Path("/domestic-vrps/{domesticVRPId}")
    @Produces("application/json; charset=utf-8")
    public Response getPaymentTypePayment(@PathParam("domesticVRPId") String domesticVRPId) {

        JSONObject responseObject = null;
        if (StringUtils.isNotBlank(domesticVRPId)) {

            responseObject = domesticVRPs.get(domesticVRPId);

        }
        if (responseObject == null) {
            responseObject = new JSONObject();
        }


        return Response.status(200).entity(responseObject.toString())
                .header("x-fapi-interaction-id", "93bac548-d2de-4546-b106-880a5018460d")
                .build();
    }


    private static JSONObject getRequest(String paymentType, String json) throws ParseException {

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
    private JSONObject cacheAndGetPaymentResponse(String paymentType, JSONObject requestObject,
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

            dataObject.put("DomesticVRPId", paymentIdValue);
            dataObject.put("Status", status);
            dataObject.put("CreationDateTime", currentDate);
            dataObject.put("StatusUpdateDateTime", currentDate);

            if ("domestic-vrps".equals(paymentType)) {
                JSONObject debtorAccount = new JSONObject();
                debtorAccount.put("SchemeName", "SortCodeAccountNumber");
                debtorAccount.put("SecondaryIdentification", "Roll 2901");
                debtorAccount.put("Name", "Deb Mal");
                debtorAccount.put("Identification", additionalConsentInfo.getAsString("AccountIds")
                        .split(":")[0].replace("[\"", ""));

                dataObject.put("DebtorAccount", debtorAccount);

            }

            // Add refund account details if requested during consent initiation
            if (Boolean.parseBoolean(readRefundAccount)) {
                addRefundAccount(dataObject);
            }

            if (Boolean.parseBoolean(cutOffTimeAcceptable)) {
                dataObject.put(EXPECTED_EXECUTION_TIME, constructDateTime(1L,
                        OpenBankingConstants.EXPECTED_EXECUTION_TIME));
                dataObject.put(EXPECTED_SETTLEMENT_TIME, constructDateTime(1L,
                        OpenBankingConstants.EXPECTED_SETTLEMENT_TIME));
            }

            JSONObject linksObject = new JSONObject();
            linksObject.put("Self", "/domestic-vrps/" + paymentIdValue);
            responseObject.put("Links", linksObject);

            JSONObject metaObject = new JSONObject();
            responseObject.put("Meta", metaObject);

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

    public static String constructDateTime(long daysToAdd, String configToRead) {

        OpenBankingConfigParser parser = OpenBankingConfigParser.getInstance();
        String time = (String) parser.getConfiguration().get(configToRead);
        String dateValue = LocalDate.now().plusDays(daysToAdd) + "T" + (OffsetTime.parse(time));

        OffsetDateTime offSetDateVal = OffsetDateTime.parse(dateValue);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        return dateTimeFormatter.format(offSetDateVal);
    }

    private void addToCache(String paymentIdValue, JSONObject responseObject) {

        if (domesticVRPs.size() > MAX_LIMIT) {
            // Max limit reached
            domesticVRPs.remove(domesticVRPsIdQueue.poll());
        }
        domesticVRPs.put(paymentIdValue, responseObject);
        domesticVRPsIdQueue.add(paymentIdValue);
    }
}
