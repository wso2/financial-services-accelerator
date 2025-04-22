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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Consent exception class to be used in consent components and extensions.
 */
public class ConsentException extends RuntimeException {

    private JSONObject payload;
    private ResponseStatus status;
    private URI errorRedirectURI;
    private static final Log log = LogFactory.getLog(ConsentException.class);

    public ConsentException(ResponseStatus status, String errorMessage, Throwable cause) {

        super(cause);
        this.status = status;
        this.payload = createDefaultErrorObject(null, String.valueOf(this.status.getStatusCode()),
                ConsentOperationEnum.CONSENT_DEFAULT, errorMessage, null);
    }

    public ConsentException(ResponseStatus status, String errorCode, String errorMessage) {

        this.status = status;
        this.payload = createDefaultErrorObject(null, errorCode, ConsentOperationEnum.CONSENT_DEFAULT,
                errorMessage, null);
    }

    public ConsentException(ResponseStatus status, String errorMessage, ConsentOperationEnum operationEnum) {

        this.status = status;
        this.payload = createDefaultErrorObject(null, String.valueOf(this.status.getStatusCode()),
                operationEnum, errorMessage, null);
    }

    public ConsentException(ResponseStatus status, String errorMessage) {

        this.status = status;
        this.payload = createDefaultErrorObject(null, String.valueOf(this.status.getStatusCode()),
                ConsentOperationEnum.CONSENT_DEFAULT, errorMessage, null);
    }

    public ConsentException(ResponseStatus status, JSONObject payload) {

        this.status = status;
        this.payload = payload;
    }

    /**
     * This method is created to send error redirects in the authorization flow. The parameter validations are done
     * in compliance with the OAuth2 and OIDC specifications.
     *
     * @param errorURI         REQUIRED The base URI which the redirect should go to.
     * @param error            REQUIRED The error code of the error. Should be a supported value in OAuth2/OIDC
     * @param errorDescription OPTIONAL The description of the error.
     * @param state            REQUIRED if a "state" parameter was present in the client authorization request.
     */
    public ConsentException(URI errorURI, AuthErrorCode error, String errorDescription, String state) {

        if (errorURI != null && error != null) {
            try {
                //add 302 as error code since this will be a redirect
                this.status = ResponseStatus.FOUND;
                //set parameters as uri fragments
                //https://openid.net/specs/oauth-v2-multiple-response-types-1_0.html#rfc.section.5
                String errorResponse = ConsentExtensionConstants.ERROR_URI_FRAGMENT
                        .concat(URLEncoder.encode(error.toString(), StandardCharsets.UTF_8.toString()));
                if (errorDescription != null) {
                    errorResponse = errorResponse.concat(ConsentExtensionConstants.ERROR_DESCRIPTION_PARAMETER)
                            .concat(URLEncoder.encode(errorDescription, StandardCharsets.UTF_8.toString()));
                }
                if (state != null) {
                    errorResponse = errorResponse.concat(ConsentExtensionConstants.STATE_PARAMETER)
                            .concat(URLEncoder.encode(state, StandardCharsets.UTF_8.toString()));
                }
                this.errorRedirectURI = new URI(errorURI.toString().concat(errorResponse));

            } catch (URISyntaxException | UnsupportedEncodingException e) {
                log.error("Error while building the uri", e);
            }
        }
    }

    public JSONObject createDefaultErrorObject(URI redirectURI, String errorCode,
                                               ConsentOperationEnum consentOperationEnum, String errorDescription,
                                               String state) {

        JSONObject error = new JSONObject();
        error.put(ConsentExtensionConstants.ERROR_CODE, errorCode);
        error.put(ConsentExtensionConstants.ERROR_MSG, consentOperationEnum.toString());
        error.put(ConsentExtensionConstants.ERROR_DESCRIPTION, errorDescription);
        if (state != null) {
            error.put(ConsentExtensionConstants.STATE, state);
        }
        if (redirectURI != null) {
            error.put(ConsentExtensionConstants.REDIRECT_URI, redirectURI.toString());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ConsentExtensionConstants.ERROR, error);
        return jsonObject;
    }

    public JSONObject getPayload() {

        return payload;
    }

    public ResponseStatus getStatus() {

        return status;
    }

    public URI getErrorRedirectURI() {

        return errorRedirectURI;
    }

    public void setErrorRedirectURI(URI errorRedirectURI) {

        this.errorRedirectURI = errorRedirectURI;
    }
}
