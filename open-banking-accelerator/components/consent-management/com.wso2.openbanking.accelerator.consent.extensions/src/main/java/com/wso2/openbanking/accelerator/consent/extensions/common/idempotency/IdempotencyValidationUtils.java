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

package com.wso2.openbanking.accelerator.consent.extensions.common.idempotency;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Class to hold idempotency validation utils.
 */
public class IdempotencyValidationUtils {

    private static final Log log = LogFactory.getLog(IdempotencyValidationUtils.class);
    private static final OpenBankingConfigParser parser = OpenBankingConfigParser.getInstance();
    private static final ConsentCoreService consentCoreService = ConsentExtensionsDataHolder.getInstance()
            .getConsentCoreService();

    /**
     * Method to check whether the required parameters for idempotency validation are present.
     *
     * @param consentManageData Consent Manage Data
     * @return Whether the required parameters are present
     */
    static boolean isMandatoryParamsPresent(ConsentManageData consentManageData, String idempotencyKeyName) {
        String idempotencyKeyValue = consentManageData.getHeaders().get(idempotencyKeyName);
        return idempotencyKeyValue != null && consentManageData.getPayload() != null &&
                consentManageData.getClientId() != null;
    }

    /**
     * Method to check whether Idempotency handling is required.
     *
     * @return True if idempotency is required, else False.
     */
    static boolean isIdempotencyEnabledFromConfig() {
        return parser.isIdempotencyValidationEnabled();
    }

    /**
     * Method to retrieve the consent ids that have the idempotency key name and value as attribute.
     *
     * @param idempotencyKeyName     Idempotency Key Name
     * @param idempotencyKeyValue    Idempotency Key Value
     * @return   List of consent ids
     */
    static ArrayList<String> getConsentIdsFromIdempotencyKey(String idempotencyKeyName,
                                                             String idempotencyKeyValue) {
        try {
            return consentCoreService.getConsentIdByConsentAttributeNameAndValue(
                    idempotencyKeyName, idempotencyKeyValue);
        } catch (ConsentManagementException e) {
            log.debug("No consent ids found for the idempotency key value");
            return new ArrayList<>();
        }
    }

    /**
     * Method to compare the client ID sent in the request and client id retrieved from the database.
     *
     * @param requestClientID     Client ID sent in the request
     * @param dbClientId          client ID retrieved from the database
     * @return   Whether JSON client Ids are equal
     */
    static boolean isClientIdsMatching(String requestClientID, String dbClientId) {
        return requestClientID.equals(dbClientId);
    }

    /**
     * Method to check whether difference between two dates is less than the configured time.
     *
     * @param createdTime Created Time of the request
     * @return  Whether the request is received within allowed time
     */
    static boolean isRequestReceivedWithinAllowedTime(long createdTime) {

        if (createdTime == 0L) {
            return false;
        }
        String allowedTimeDuration = parser.getIdempotencyAllowedTime();
        if (allowedTimeDuration != null) {
            OffsetDateTime createdDate = OffsetDateTime.parse(convertToISO8601DateTimeFormat(createdTime));
            OffsetDateTime currDate = OffsetDateTime.now(createdDate.getOffset());

            long diffInHours = Duration.between(createdDate, currDate).toMinutes();
            return diffInHours <= Long.parseLong(allowedTimeDuration);
        } else {
            log.error("Idempotency Allowed duration is null");
            return false;
        }
    }

    /**
     * Convert long date values to ISO 8601 format.
     * @param dateValue     Date value
     * @return ISO 8601 formatted date
     */
    private static String convertToISO8601DateTimeFormat(long dateValue) {

        DateFormat simple = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        Date simpleDateVal = new Date(dateValue * 1000);
        return simple.format(simpleDateVal);
    }
}
