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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementRuntimeException;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Consent extension utils class.
 */
public class ConsentExtensionUtils {

    private static final Log log = LogFactory.getLog(ConsentExtensionUtils.class);

    /**
     * Method to get the consent type based on the request path.
     * @param requestPath  Request path of the request
     * @return Consent Type
     */
    public static String getConsentType(String requestPath) throws ConsentManagementException {
        if (requestPath.contains(ConsentExtensionConstants.ACCOUNT_CONSENT_PATH)) {
            return ConsentExtensionConstants.ACCOUNTS;
        } else if (requestPath.contains(ConsentExtensionConstants.COF_CONSENT_PATH)) {
            return ConsentExtensionConstants.FUNDS_CONFIRMATIONS;
        } else if (requestPath.contains(ConsentExtensionConstants.PAYMENT_CONSENT_PATH)) {
            return ConsentExtensionConstants.PAYMENTS;
        } else {
            throw new ConsentManagementException("Invalid consent type");
        }
    }

    /**
     * Validate the consent ID.
     *
     * @param consentId Consent Id to validate
     * @return Whether the consent ID is valid
     */
    public static boolean isConsentIdValid(String consentId) {
        return (Pattern.matches(ConsentExtensionConstants.UUID_REGEX, consentId));
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

    /**
     * Method to construct Initiation response.
     *
     * @param response       Response of the request
     * @param createdConsent Consent response received from service layer
     * @return  JSONObject Initiation Response
     */
    public static JSONObject getInitiationResponse(JSONObject response, DetailedConsentResource createdConsent) {
        JSONObject dataObject = response.getJSONObject(ConsentExtensionConstants.DATA);
        dataObject.put(ConsentExtensionConstants.CONSENT_ID, createdConsent.getConsentID());
        dataObject.put(ConsentExtensionConstants.CREATION_DATE_TIME, convertToISO8601(createdConsent.getCreatedTime()));
        dataObject.put(ConsentExtensionConstants.STATUS_UPDATE_DATE_TIME,
                convertToISO8601(createdConsent.getUpdatedTime()));
        dataObject.put(ConsentExtensionConstants.STATUS, createdConsent.getCurrentStatus());

        response.remove(ConsentExtensionConstants.DATA);
        response.put(ConsentExtensionConstants.DATA, dataObject);

        return response;
    }

    /**
     * Method to construct Retrieval Initiation response.
     *
     * @param receiptJSON Initiation of the request
     * @param consent     Consent response received from service layer
     * @return  JSONObject Initiation Response
     */
    public static JSONObject getInitiationRetrievalResponse(JSONObject receiptJSON, ConsentResource consent) {

        JSONObject dataObject = receiptJSON.getJSONObject(ConsentExtensionConstants.DATA);
        dataObject.put(ConsentExtensionConstants.CONSENT_ID, consent.getConsentID());
        dataObject.put(ConsentExtensionConstants.CREATION_DATE_TIME, convertToISO8601(consent.getCreatedTime()));
        dataObject.put(ConsentExtensionConstants.STATUS_UPDATE_DATE_TIME,
                convertToISO8601(consent.getUpdatedTime()));
        dataObject.put(ConsentExtensionConstants.STATUS, consent.getCurrentStatus());

        receiptJSON.remove(ConsentExtensionConstants.DATA);
        receiptJSON.put(ConsentExtensionConstants.DATA, dataObject);

        return receiptJSON;
    }

    @Generated(message = "Ignoring since method contains no logics")
    public static <T> T  getClassInstanceFromFQN(String classpath, Class<T> className) {

        try {
            Object classObj = Class.forName(classpath).getDeclaredConstructor().newInstance();
            return className.cast(classObj);
        } catch (ClassNotFoundException e) {
            log.error(String.format("Class not found: %s", classpath.replaceAll("[\r\n]", "")));
            throw new ConsentManagementRuntimeException("Cannot find the defined class", e);
        } catch (InstantiationException | InvocationTargetException |
                 NoSuchMethodException | IllegalAccessException e) {
            //Throwing a runtime exception since we cannot proceed with invalid objects
            throw new ConsentManagementRuntimeException("Defined class" + classpath + "cannot be instantiated.", e);
        }
    }
}
