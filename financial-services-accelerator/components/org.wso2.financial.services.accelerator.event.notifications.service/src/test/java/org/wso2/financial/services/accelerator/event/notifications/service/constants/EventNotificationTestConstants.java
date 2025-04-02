/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.event.notifications.service.constants;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Test constant class for EventNotification Tests.
 */
public class EventNotificationTestConstants {

    public static final String SAMPLE_CLIENT_ID = "19_FTbAvbZm9YC9QRBYw8E0hVnAa";
    public static final String SAMPLE_CLIENT_ID_2 = "19_FTbAvbZm9YC9QRBYw8E0hVnAb";
    public static final String SAMPLE_RESOURCE_ID = "85d81bdb-111e-4553-8c0c-0cd2dd780515";
    public static final String SAMPLE_NOTIFICATION_ID = "c2fcb77a-274d-4851-b392-a2c0af312fd7";
    public static final String SAMPLE_NOTIFICATION_ID_2 = "c2fcb77a-274d-4851-b392-a2c0af312fb7";

    public static final String SAMPLE_ERROR_NOTIFICATION_ID = "d3fcb77a-274d-4851-b392-a2c0af312fd8";
    public static final Long UPDATED_TIME = 1646389384L;
    public static final String SAMPLE_NOTIFICATION_EVENT_TYPE_1 = "urn_uk_org_openbanking_events_resource-update";
    public static final String SAMPLE_NOTIFICATION_EVENT_TYPE_2 =
            "urn_uk_org_openbanking_events_consent-authorization-revoked";
    public static final Boolean SAMPLE_RETURN_IMMEDIATETLY = true;
    public static final int SAMPLE_MAX_EVENTS = 5;

    public static final String ERROR_CODE = "authentication_failed";

    public static final String ERROR_DESCRIPTION = "The SET could not be authenticated";

    public static final String SAMPLE_SET = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ4MEpFRzM5VGJ6dmMyRXhvbG1TaWZaR1Np" +
            "TzhhIiwiYXVkIjoieDBKRUczOVRienZjMkV4b2xtU2lmWkdTaU84YSIsImlzcyI6Ind3dy53c28yLmNvbSIsInR4biI6ImUwY2Y2NG" +
            "RlLTlkMGUtNDBmYy04ZWUyLTFhNTNmYzNiOWY4ZiIsInRvZSI6MTY2NDI3MDYzNzAwMCwiaWF0IjoxNjY0Mjc2NzA5LCJqdGkiOi" +
            "I0ZjMxMjAwNy00ZDNmLTQwZTQtYTUyNS0wZjZlZThiYjU0ZDkiLCJldmVudHMiOnsidXJuX3VrX29yZ19vcGVuYmFua2luZ19ldm" +
            "VudHNfcmVzb3VyY2UtdXBkYXRlIjp7ImtleTIiOiJ2YWx1ZSIsInJlc291cmNlSUQiOiJmNmRlMWE3NC0xMmY1LTQ5NWQtYWRjNC0" +
            "xNzI4YWQwMDAyZTAnIiwia2V5MyI6InZhbHVlIn0sInVybl91a19vcmdfb3BlbmJhbmtpbmdfZXZlbnRzX2NvbnNlbnQtYXV0aG9y" +
            "aXphdGlvbi1yZXZva2VkIjp7ImtleTIiOiJ2YWx1ZSIsInJlc291cmNlSUQiOiJmNmRlMWE3NC0xMmY1LTQ5NWQtYWRjNC0xNzI4Y" +
            "WQwMDAyZTAnIiwia2V5MyI6InZhbHVlIn19fQ.VrkFxa2fyRhf4rP1plhedKIXNTjsrbvVveDLLHZotll2GIbxm0lCCElGUXNh463" +
            "R9_HXIjfyi61b0yN2gRZKiwhPftIe9AUdFj2e2hheiE_UTiVDo9RiEvo2drvE_-ri4MN0mKHPx2GdIGKx3WTo84Ike3VZitpi8WTL7" +
            "Ap1mIK1RITOd9QGO2iAwXj5NQPy9iXDV9ynQTmblLeiessUAmI3WyoYEw82P-7M0yHWCf_ztFfg6w_s9uyrak8HFmsmHeQb86frLI" +
            "i4UKGiGvAVM-dBF8BAEq5eFZ2TBYWDrugk4HrSdFz7AblReTzL8vF7XFlEocFQSQ1Y_k1hXQCn4g";

    public static final String INVALID_CLIENT_ERROR = "\"A client was not\" +\n" +
            "                \" found for the client id : '19_FTbAvbZm9YC9QRBYw8E0hVnAa' in the database.\"";

    public static final String SAMPLE_CALLBACK_URL = "https://localhost:8080/callback";

    public static final JSONObject SAMPLE_NOTIFICATION_PAYLOAD =
            new JSONObject("{\"notificationId\": " + SAMPLE_NOTIFICATION_ID + ", \"SET\": " + SAMPLE_SET + "}");

    public static final String SAMPLE_SPEC_VERSION = "3.1";
    public static final List<String> SAMPLE_NOTIFICATION_EVENT_TYPES = Arrays.asList(SAMPLE_NOTIFICATION_EVENT_TYPE_1,
            SAMPLE_NOTIFICATION_EVENT_TYPE_2);
    public static final String SAMPLE_SUBSCRIPTION_ID_1 = "550e8400-e29b-41d4-a716-446655440000";
    public static final String SAMPLE_SUBSCRIPTION_ID_2 = "9e65ebe4-2251-4a89-ba74-54060e76f51d";
    public static final String SUBSCRIPTION_STATUS = "CREATED";
    public static final String SUBSCRIPTION_PAYLOAD = "{\n" +
            "    \"eventTypes\": [\n" +
            "        \"urn_uk_org_openbanking_events_consent-authorization-revoked\",\n" +
            "        \"urn:ietf:params:scim:event:create\"\n" +
            "    ], \n" +
            "    \"callbackUrl\": \"http://localhost:8000/sample-tpp-server\"\n" +
            "}";
}

