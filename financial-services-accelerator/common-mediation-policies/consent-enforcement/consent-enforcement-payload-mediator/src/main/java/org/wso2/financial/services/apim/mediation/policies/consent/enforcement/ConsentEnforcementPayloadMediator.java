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

package org.wso2.financial.services.apim.mediation.policies.consent.enforcement;

import com.nimbusds.jose.JOSEException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.financial.services.apim.mediation.policies.consent.enforcement.constants.ConsentEnforcementConstants;
import org.wso2.financial.services.apim.mediation.policies.consent.enforcement.utils.ConsentEnforcementUtils;
import org.wso2.financial.services.apim.mediation.policies.consent.enforcement.utils.Generated;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Mediator to generate the payload required to be sent for the consent validation service.
 */
public class ConsentEnforcementPayloadMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(ConsentEnforcementPayloadMediator.class);

    private String consentIdClaimName;

    @Override
    public boolean mediate(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        Map<String, String> headers = (Map<String, String>)
                axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        String extractedConsentId;
        try {
            extractedConsentId = ConsentEnforcementUtils.extractConsentIdFromJwtToken(headers, consentIdClaimName);
        } catch (UnsupportedEncodingException e) {
            String errorDescription = "Failed to decode the JWT token payload. The token may be malformed or corrupted";
            log.error(errorDescription, e);
            setErrorResponseProperties(messageContext, "Bad Request", errorDescription, "400");
            throw new SynapseException(errorDescription);
        }

        if (StringUtils.isBlank(extractedConsentId)) {
            String errorDescription = consentIdClaimName + " claim is not found in the JWT token";
            log.error(errorDescription);
            setErrorResponseProperties(messageContext, "Unauthorized", errorDescription, "401");
            throw new SynapseException(errorDescription);
        }

        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put(ConsentEnforcementConstants.CONSENT_ID_TAG,
                extractedConsentId);
        additionalParams.put(ConsentEnforcementConstants.ELECTED_RESOURCE_TAG,
                messageContext.getProperty(ConsentEnforcementConstants.API_ELECTED_RESOURCE));
        additionalParams.put(ConsentEnforcementConstants.RESOURCE_PARAMS_TAG,
                ConsentEnforcementUtils.getResourceParamMap(messageContext));
         additionalParams.put(ConsentEnforcementConstants.USER_ID_TAG,
                 messageContext.getProperty(ConsentEnforcementConstants.USER_ID));
         additionalParams.put(ConsentEnforcementConstants.CLIENT_ID_TAG,
                 messageContext.getProperty(ConsentEnforcementConstants.CONSUMER_KEY));

        JSONObject validationRequest;
        try {
            String jsonPayload = JsonUtil.jsonPayloadToString(axis2MessageContext);
            validationRequest = ConsentEnforcementUtils
                    .createValidationRequestPayload(jsonPayload, headers, additionalParams);

            String enforcementJWTPayload = ConsentEnforcementUtils.generateJWT(validationRequest.toString());
            messageContext.setProperty("consentEnforcementJwtPayload", enforcementJWTPayload);
        } catch (JSONException e) {
            String errorDescription = "Invalid JSON payload";
            log.error(errorDescription, e);
            setErrorResponseProperties(messageContext, "Bad Request", errorDescription, "400");
            throw new SynapseException(errorDescription);
        } catch (JOSEException | ParseException e) {
            String errorDescription = "Error while generating JWT payload for consent validation";
            log.error(errorDescription, e);
            setErrorResponseProperties(messageContext, "Internal Server Error", errorDescription, "500");
            throw new SynapseException(errorDescription);
        }

        return true;
    }

    @Generated(message = "No testable logic")
    public String getConsentIdClaimName() {
        return consentIdClaimName;
    }

    @Generated(message = "No testable logic")
    public void setConsentIdClaimName(String consentIdClaimName) {
        this.consentIdClaimName = consentIdClaimName;
    }

    @Generated(message = "No testable logic")
    private static void setErrorResponseProperties(MessageContext messageContext, String errorCode,
                                                   String errorDescription, String httpStatusCode) {

        messageContext.setProperty(ConsentEnforcementConstants.ERROR_CODE, errorCode);
        messageContext.setProperty(ConsentEnforcementConstants.ERROR_TITLE, "Consent Enforcement Error");
        messageContext.setProperty(ConsentEnforcementConstants.ERROR_DESCRIPTION, errorDescription);
        messageContext.setProperty(ConsentEnforcementConstants.CUSTOM_HTTP_SC, httpStatusCode);
    }

}
