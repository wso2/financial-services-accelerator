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

package com.wso2.openbanking.accelerator.consent.extensions.common;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.local.auth.api.core.ParameterResolverService;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


import javax.servlet.http.HttpServletRequest;

/**
 * Util class for consent extensions.
 */
public class ConsentExtensionUtils {

    private static final Log log = LogFactory.getLog(ConsentExtensionUtils.class);
    private static Gson gson = new Gson();
    public static void setCommonDataToResponse(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        if (!jsonObject.containsKey(ConsentExtensionConstants.TYPE)) {
            jsonObject.appendField(ConsentExtensionConstants.TYPE, consentData.getType());
        }
        if (!jsonObject.containsKey(ConsentExtensionConstants.APPLICATION)) {
            jsonObject.appendField(ConsentExtensionConstants.APPLICATION, consentData.getApplication());
        }
    }

    public static JSONObject detailedConsentToJSON(DetailedConsentResource detailedConsentResource) {
        JSONObject consentResource = new JSONObject();

        consentResource.appendField("consentId", detailedConsentResource.getConsentID());
        consentResource.appendField("clientId", detailedConsentResource.getClientID());
        try {
            consentResource.appendField("receipt", (new JSONParser(JSONParser.MODE_PERMISSIVE)).
                    parse(detailedConsentResource.getReceipt()));
        } catch (ParseException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Exception occurred while parsing" +
                    " receipt");
        }
        consentResource.appendField("consentType", detailedConsentResource.getConsentType());
        consentResource.appendField("currentStatus", detailedConsentResource.getCurrentStatus());
        consentResource.appendField("consentFrequency", detailedConsentResource.getConsentFrequency());
        consentResource.appendField("validityPeriod", detailedConsentResource.getValidityPeriod());
        consentResource.appendField("createdTimestamp", detailedConsentResource.getCreatedTime());
        consentResource.appendField("updatedTimestamp", detailedConsentResource.getUpdatedTime());
        consentResource.appendField("recurringIndicator", detailedConsentResource.isRecurringIndicator());
        JSONObject attributes = new JSONObject();
        Map<String, String> attMap = detailedConsentResource.getConsentAttributes();
        for (Map.Entry<String, String> entry : attMap.entrySet()) {
            attributes.appendField(entry.getKey(), entry.getValue());
        }
        consentResource.appendField("consentAttributes", attributes);
        JSONArray authorizationResources = new JSONArray();
        ArrayList<AuthorizationResource> authArray = detailedConsentResource.getAuthorizationResources();
        for (AuthorizationResource resource : authArray) {
            JSONObject resourceJSON = new JSONObject();
            resourceJSON.appendField("authorizationId", resource.getAuthorizationID());
            resourceJSON.appendField("consentId", resource.getConsentID());
            resourceJSON.appendField("userId", resource.getUserID());
            resourceJSON.appendField("authorizationStatus", resource.getAuthorizationStatus());
            resourceJSON.appendField("authorizationType", resource.getAuthorizationType());
            resourceJSON.appendField("updatedTime", resource.getUpdatedTime());
            authorizationResources.add(resourceJSON);
        }
        consentResource.appendField("authorizationResources", authorizationResources);
        JSONArray consentMappingResources = new JSONArray();
        ArrayList<ConsentMappingResource> mappingArray = detailedConsentResource.getConsentMappingResources();
        for (ConsentMappingResource resource : mappingArray) {
            JSONObject resourceJSON = new JSONObject();
            resourceJSON.appendField("mappingId", resource.getMappingID());
            resourceJSON.appendField("authorizationId", resource.getAuthorizationID());
            resourceJSON.appendField("accountId", resource.getAccountID());
            resourceJSON.appendField("permission", resource.getPermission());
            resourceJSON.appendField("mappingStatus", resource.getMappingStatus());
            consentMappingResources.add(resourceJSON);
        }
        consentResource.appendField("consentMappingResources", consentMappingResources);
        return consentResource;
    }

    public static JSONObject getRequestObjectPayload(String requestObject) {
        try {

            // validate request object and get the payload
            String requestObjectPayload;
            String[] jwtTokenValues = requestObject.split("\\.");
            if (jwtTokenValues.length == 3) {
                requestObjectPayload = new String(Base64.getUrlDecoder().decode(jwtTokenValues[1]),
                        StandardCharsets.UTF_8);
            } else {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "request object is not signed JWT");
            }
            Object payload = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(requestObjectPayload);
            if (!(payload instanceof JSONObject)) {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Payload is not a JSON object");
            }
            return (JSONObject) payload;

        } catch (ParseException e) {
            log.error("Exception occurred while getting consent data. Caused by : ",  e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    /**
     * Extract headers from a request object.
     *
     * @param request The request object
     * @return Map of header key value pairs
     */
    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

    /**
     * Get the sensitive data corresponding to the session data consent key.
     *
     * @param sessionDataKeyConsent The session data key corresponding to the data hidden from redirect URLs
     * @return The hidden sensitive data as key-value pairs.
     */
    public static Map<String, Serializable> getSensitiveDataWithConsentKey(String sessionDataKeyConsent) {

        return getSensitiveData(sessionDataKeyConsent);
    }

    /**
     * Get the sensitive data corresponding to the session data key or the session data consent key.
     *
     * @param key The session data key or session data consent key corresponding to the data hidden from redirect URLs
     * @return The hidden sensitive data as key-value pairs.
     */
    public static Map<String, Serializable> getSensitiveData(String key) {

        Map<String, Serializable> sensitiveDataSet = new HashMap<>();

        Object serviceObj = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(ParameterResolverService.class, null);
        if (serviceObj instanceof ParameterResolverService) {
            ParameterResolverService resolverService = (ParameterResolverService) serviceObj;

            Set<String> filter = Collections.emptySet();

            sensitiveDataSet.putAll((resolverService)
                    .resolveParameters(ConsentExtensionConstants.CONSENT_KEY, key, filter));

            if (sensitiveDataSet.isEmpty()) {
                sensitiveDataSet.putAll((resolverService)
                        .resolveParameters(ConsentExtensionConstants.REQUEST_KEY, key, filter));
            }

            if (sensitiveDataSet.isEmpty()) {
                log.error("No available data for key provided");
                sensitiveDataSet.put(ConsentExtensionConstants.IS_ERROR, "No available data for key provided");
                return sensitiveDataSet;
            }

            sensitiveDataSet.put(ConsentExtensionConstants.IS_ERROR, "false");
            return sensitiveDataSet;

        } else {
            log.error("Could not retrieve ParameterResolverService OSGi service");
            sensitiveDataSet.put(ConsentExtensionConstants.IS_ERROR, "Could not retrieve parameter service");
            return sensitiveDataSet;
        }
    }

    /**
     * @param consentDetails json object of consent data
     * @param sessionDataKey  session data key
     * @return  ConsentData object
     * @throws URISyntaxException  if the URI is invalid
     */
    public static ConsentData getConsentDataFromAttributes(JsonObject consentDetails, String sessionDataKey)
            throws URISyntaxException {

        JsonObject sensitiveDataMap = consentDetails.get(ConsentExtensionConstants.SENSITIVE_DATA_MAP)
                .getAsJsonObject();
        ConsentData consentData = new ConsentData(sessionDataKey,
                sensitiveDataMap.get(ConsentExtensionConstants.LOGGED_IN_USER).getAsString(),
                sensitiveDataMap.get(ConsentExtensionConstants.SP_QUERY_PARAMS).getAsString(),
                consentDetails.get(ConsentExtensionConstants.SCOPES).getAsString(),
                sensitiveDataMap.get(ConsentExtensionConstants.APPLICATION).getAsString(),
                gson.fromJson(consentDetails.get(ConsentExtensionConstants.REQUEST_HEADERS), Map.class));
        consentData.setSensitiveDataMap(gson.fromJson(sensitiveDataMap, Map.class));
        URI redirectURI = new URI(consentDetails.get(ConsentExtensionConstants.REQUEST_URI).getAsString());
        consentData.setRedirectURI(redirectURI);
        consentData.setUserId(consentDetails.get(ConsentExtensionConstants.USERID).getAsString());
        consentData.setConsentId(consentDetails.get(ConsentExtensionConstants.CONSENT_ID).getAsString());
        consentData.setClientId(consentDetails.get(ConsentExtensionConstants.CLIENT_ID).getAsString());
        consentData.setRegulatory(Boolean.parseBoolean(consentDetails.get(ConsentExtensionConstants.REGULATORY)
                .getAsString()));
        ConsentResource consentResource = gson.fromJson(consentDetails.get(ConsentExtensionConstants.CONSENT_RESOURCE),
                ConsentResource.class);
        consentData.setConsentResource(consentResource);
        AuthorizationResource authorizationResource =
                gson.fromJson(consentDetails.get(ConsentExtensionConstants.AUTH_RESOURCE), AuthorizationResource.class);
        consentData.setAuthResource(authorizationResource);
        consentData.setMetaDataMap(gson.fromJson(consentDetails.get(ConsentExtensionConstants.META_DATA), Map.class));
        consentData.setType(consentDetails.get(ConsentExtensionConstants.TYPE).getAsString());
        return consentData;
    }

    /**
     * Validates whether Cutoffdatetime is enabled, if the request is arriving past the cut off time and if it
     * should be rejected by policy.
     *
     * @return if the request should be rejected, or not.
     */
    public static boolean shouldInitiationRequestBeRejected() {

        return Boolean.parseBoolean((String) OpenBankingConfigParser.getInstance().getConfiguration().get(
                ConsentExtensionConstants.CUTOFF_DATE_ENABLED)) && isCutOffTimeElapsed()
                && ConsentExtensionConstants.REJECT.equals(OpenBankingConfigParser.getInstance().getConfiguration()
                        .get(ConsentExtensionConstants.CUTOFF_DATE_POLICY));
    }

    /**
     * Validates whether the CutOffTime for the day has elapsed.
     *
     * @return has elapsed
     */
    public static boolean isCutOffTimeElapsed() {

        OffsetTime dailyCutOffTime = OffsetTime.parse((String) OpenBankingConfigParser.getInstance().getConfiguration()
                .get(ConsentExtensionConstants.DAILY_CUTOFF));
        OffsetTime currentTime = LocalTime.now().atOffset(dailyCutOffTime.getOffset());
        if (log.isDebugEnabled()) {
            log.debug("Request received at" + currentTime + " daily cut off time set to " + dailyCutOffTime);
        }
        return currentTime.isAfter(dailyCutOffTime);
    }
    /**
     * validate whether Cutoffdatetime is enabled, if the request is arriving past the cut off time
     * and if it was accepted by policy.git a.
     *
     * @return if request is accepted and cut off date time has passed, or not
     */

    public static boolean isRequestAcceptedPastElapsedTime() {

        if (Boolean.parseBoolean((String) OpenBankingConfigParser.getInstance().getConfiguration()
                .get(OpenBankingConstants.CUTOFF_DATE_ENABLED)) &&
                isCutOffTimeElapsed() && ConsentExtensionConstants.ACCEPT
                .equals(OpenBankingConfigParser.getInstance().getConfiguration()
                        .get(OpenBankingConstants.CUTOFF_DATE_POLICY))) {

            log.debug("Request Accepted but CutOffDateTime has elapsed.");
            return true;
        }
        return false;
    }
    /**
     * Returns the DateTime by adding given number of days and the with the given Time.
     *
     * @param daysToAdd Number of days to add
     * @param time      Time to add
     * @return DateTime value for the day
     */
    public static String constructDateTime(long daysToAdd, String time) {

        String configuredZoneId = (String) OpenBankingConfigParser.getInstance().getConfiguration()
                .get(OpenBankingConstants.ZONE_ID);
        String dateValue = LocalDate.now(ZoneId.of(configuredZoneId)).plusDays(daysToAdd) + "T" +
                (OffsetTime.parse(time));

        OffsetDateTime offSetDateVal = OffsetDateTime.parse(dateValue);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        return dateTimeFormatter.format(offSetDateVal);
    }

    /**
     * Validates whether Cutoffdatetime is enabled, if the request is arriving past the cut off date and if it
     * should be rejected by policy.
     *
     * @param timeStamp Initiation timestamp
     * @return if the request should be rejected, or not.
     */
    public static boolean shouldSubmissionRequestBeRejected(String timeStamp) {

        String isCutOffDateEnabled = (String) OpenBankingConfigParser.getInstance().getConfiguration()
                .get(OpenBankingConstants.CUTOFF_DATE_ENABLED);
        String cutOffDatePolicy = (String) OpenBankingConfigParser.getInstance().getConfiguration()
                .get(OpenBankingConstants.CUTOFF_DATE_POLICY);

        if (Boolean.parseBoolean(isCutOffDateEnabled) && ConsentExtensionConstants.REJECT.equals(cutOffDatePolicy)) {
            if (isCutOffTimeElapsed()) {
                log.debug("Request Rejected as CutOffTime has elapsed.");
                return true;
            }

            if (hasCutOffDateElapsed(timeStamp)) {
                log.debug("Request Rejected as CutOffDate has elapsed.");
                return true;
            }
        }
        return false;
    }
    /**
     * Validates whether the cutOffDate and the initiation date are the same.
     *
     * @return if the request should be rejected, or not.
     */
    private static boolean hasCutOffDateElapsed(String initiationTimestamp) {

        OffsetDateTime initiationDateTime = OffsetDateTime.parse(initiationTimestamp);
        OffsetDateTime currentDateTime = OffsetDateTime.parse(getCurrentCutOffDateTime());
        return initiationDateTime.getMonth() != currentDateTime.getMonth() ||
                initiationDateTime.getDayOfMonth() != currentDateTime.getDayOfMonth();
    }
    /**
     * Returns the CutOffDateTime from the CutOffTime.
     *
     * @return CutOffDateTime value for the day
     */
    public static String getCurrentCutOffDateTime() {

        return LocalDate.now() + "T" + (OffsetTime.parse((String) OpenBankingConfigParser.getInstance()
                .getConfiguration()
                .get(OpenBankingConstants.DAILY_CUTOFF)));
    }
    /**
     * Convert long date values to ISO 8601 format.
     * @param dateValue  Date value in long
     * @return ISO 8601 formatted date
     */
    public static String convertToISO8601(long dateValue) {

        DateFormat simple = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        Date simpleDateVal = new Date(dateValue * 1000);
        return simple.format(simpleDateVal);
    }
    public static ConsentCoreServiceImpl getConsentService() {
        return new ConsentCoreServiceImpl();
    }

    /**
     * Get the mapping status.
     *
     * @param defaultStatus  Default status returned from the accelerator
     * @return Mapping UK status
     */
    public static String getConsentStatus(String defaultStatus) {

        switch (defaultStatus) {
            case ConsentExtensionConstants.AUTHORIZED_STATUS:
                return ConsentExtensionConstants.OB_AUTHORIZED_STATUS;
            case ConsentExtensionConstants.REVOKED_STATUS:
                return ConsentExtensionConstants.OB_REVOKED_STATUS;
            case ConsentExtensionConstants.REJECTED_STATUS:
                return ConsentExtensionConstants.OB_REJECTED_STATUS;
            case ConsentExtensionConstants.AWAITING_UPLOAD_STATUS:
                return ConsentExtensionConstants.OB_AWAITING_UPLOAD_STATUS;
            default:
                return ConsentExtensionConstants.OB_AWAITING_AUTH_STATUS;
        }
    }
}
