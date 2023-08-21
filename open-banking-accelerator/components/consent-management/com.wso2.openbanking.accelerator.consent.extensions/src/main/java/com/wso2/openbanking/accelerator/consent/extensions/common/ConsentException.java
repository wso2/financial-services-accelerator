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

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Consent exception class to be used in consent components and extensions.
 */
public class ConsentException extends RuntimeException {

    private static final Log log = LogFactory.getLog(ConsentException.class);

    private JSONObject payload;
    private ResponseStatus status;
    private URI errorRedirectURI;

    public ConsentException(ResponseStatus status, JSONObject payload, Throwable cause) {

        super(cause);
        this.status = status;
        this.payload = payload;
    }

    public ConsentException(ResponseStatus status, JSONObject payload) {

        this.status = status;
        this.payload = payload;
    }

    public ConsentException(ResponseStatus status, String description) {

        this.status = status;
        this.payload = createDefaultErrorObject(this.status, description);
    }

    /**
     * This method is created to send error redirects in in the authorization flow. The parameter validations are done
     * in compliance with the OAuth2 and OIDC specifications.
     *
     * @param errorRedirectURI REQUIRED The base URI which the redirect should go to.
     * @param error            REQUIRED The error code of the error. Should be a supported value in OAuth2/OIDC
     * @param errorDescription OPTIONAL The description of the error.
     * @param state            REQUIRED if a "state" parameter was present in the client authorization request.
     */
    public ConsentException(URI errorRedirectURI, AuthErrorCode error, String errorDescription, String state) {

        if (errorRedirectURI != null && error != null) {
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
                this.errorRedirectURI = new URI(errorRedirectURI.toString().concat(errorResponse));

            } catch (URISyntaxException | UnsupportedEncodingException e) {
                log.error("Error while building the uri", e);
            }
        }
    }

    public JSONObject createDefaultErrorObject(ResponseStatus status, String description) {

        JSONObject error = new JSONObject();
        JSONArray errorList = new JSONArray();
        JSONObject errorObj = new JSONObject();
        error.appendField("Code", String.valueOf(status.getStatusCode()));
        error.appendField("Message", status.getReasonPhrase());
        errorObj.appendField("ErrorCode", String.valueOf(status.getStatusCode()));
        errorObj.appendField("Message", description);
        errorList.appendElement(errorObj);
        error.appendField("Errors", errorList);
        return error;
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
