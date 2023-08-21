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

package com.wso2.openbanking.accelerator.gateway.executor.jws;

import com.nimbusds.jose.JOSEException;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.error.OpenBankingErrorCodes;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.exception.OpenBankingExecutorException;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.util.GatewayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Executor class for Signing Responses.
 * @deprecated
 * Use {@link com.wso2.openbanking.accelerator.gateway.handler.JwsResponseSignatureHandler} instead.
 */
@Deprecated
public class JwsResponseSignatureHandlingExecutor implements OpenBankingGatewayExecutor {

    private static final Log log = LogFactory.getLog(JwsResponseSignatureHandlingExecutor.class);

    private String xWso2ApiVersion = null;
    private String xWso2ApiType = null;
    private String signatureHeaderName = getSignatureHeaderName();

    @Generated(message = "Excluding from unit tests since it is covered by other methods")
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

        if (obapiRequestContext.isError()) {
            appendJwsSignatureToRequestContext(obapiRequestContext);
        }
    }

    @Generated(message = "Excluding from unit tests since it is covered by other methods")
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

        if (obapiRequestContext.isError()) {
            appendJwsSignatureToRequestContext(obapiRequestContext);
        }
    }

    /**
     * Method to handle pre-process response.
     *
     * @param obapiResponseContext OB response context object
     */
    @Generated(message = "Excluding from unit tests since it is covered by other methods")
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

        appendJwsSignatureToResponseContext(obapiResponseContext);
    }

    @Generated(message = "Excluding from unit tests since it is covered by other methods")
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

        appendJwsSignatureToResponseContext(obapiResponseContext);
    }

    /**
     * Provide the child classes to decide whether the signature generation is required for requestPath.
     *
     * @param obapiRequestContext OB response Object
     * @return boolean returns if request needs to be signed
     */
    @Generated(message = "Excluding from unit tests since there is a call to a method in Common Module")
    public boolean isApplicableForRequestPath(OBAPIRequestContext obapiRequestContext) {

        return OpenBankingConfigParser.getInstance().isJwsResponseSigningEnabled();
    }

    /**
     * Provide the child classes to decide whether the signature generation is required for error scenarios.
     *
     * @param obapiResponseContext OB response Object
     * @return boolean returns if request needs to be signed
     */
    @Generated(message = "Excluding from unit tests since there is a call to a method in Common Module")
    public boolean isApplicableForResponsePath(OBAPIResponseContext obapiResponseContext) {

        return OpenBankingConfigParser.getInstance().isJwsResponseSigningEnabled();
    }

    /**
     * Method to Generate JWS signature.
     *
     * @param payloadString
     * @return
     */
    public String generateJWSSignature(Optional<String> payloadString)
            throws OpenBankingExecutorException, JOSEException {

        String jwsSignatureHeader = null;
        if (payloadString.isPresent() && StringUtils.isNotBlank(payloadString.get())) {
            HashMap<String, Object> criticalParameters = getCriticalHeaderParameters();
            jwsSignatureHeader = GatewayUtils.constructJWSSignature(payloadString.get(), criticalParameters);
        } else {
            log.debug("Signature cannot be generated as the payload is invalid or authentication context is not " +
                    "available");
        }
        return jwsSignatureHeader;
    }

    /**
     * HashMap to be returned with crit header keys and values.
     * can be extended at toolkit level.
     *
     * @return HashMap crit header parameters
     */
    public HashMap<String, Object> getCriticalHeaderParameters() {

        return new HashMap<>();
    }

    @Generated(message = "Excluding from unit test coverage")
    public void setXWso2ApiVersion(String xWso2ApiVersion) {

        this.xWso2ApiVersion = xWso2ApiVersion;
    }

    @Generated(message = "Excluding from unit test coverage")
    public String getXWso2ApiVersion() {

        return this.xWso2ApiVersion;
    }

    @Generated(message = "Excluding from unit test coverage")
    public String getXWso2ApiType() {

        return xWso2ApiType;
    }

    @Generated(message = "Excluding from unit test coverage")
    public void setXWso2ApiType(String xWso2ApiType) {

        this.xWso2ApiType = xWso2ApiType;
    }

    /**
     * Method to change the expected request header name containing the JWS.
     */
    public String getSignatureHeaderName() {

        return "x-jws-signature";
    }

    /**
     * Method to append Jws Signature To Request Context.
     * @param obapiRequestContext
     */
    @Generated(message = "Excluding from unit tests since it is covered by other methods")
    private void appendJwsSignatureToRequestContext(OBAPIRequestContext obapiRequestContext) {

        setXWso2ApiVersion(obapiRequestContext.getApiRequestInfo().getVersion());
        setXWso2ApiType(obapiRequestContext.getApiRequestInfo().getContext());

        String messageID = obapiRequestContext.getMsgInfo().getMessageId();

        if (!isApplicableForRequestPath(obapiRequestContext)) {
            if (log.isDebugEnabled()) {
                log.debug(String.format(
                        "Signature generation is not applicable for this response " +
                                "with message id : %s", messageID));
            }
            return;
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format(
                        "Generating signature for the response " +
                                "with message id : %s", messageID));
            }
            // Retrieve headers and payload.
            try {
                Map<String, String> responseHeaders = obapiRequestContext.getMsgInfo().getHeaders();
                Optional<String> payload = GatewayUtils.extractRequestPayload(obapiRequestContext,
                        obapiRequestContext.getMsgInfo().getHeaders());
                responseHeaders.put(signatureHeaderName, generateJWSSignature(payload));
                obapiRequestContext.setAddedHeaders(responseHeaders);
            } catch (OpenBankingException | OpenBankingExecutorException | JOSEException e) {
                log.error("Unable to sign response", e);
                GatewayUtils.handleRequestInternalServerError(obapiRequestContext,
                        "Internal Server Error, Unable to sign the response",
                        OpenBankingErrorCodes.SERVER_ERROR_CODE);
            }
        }
    }

    /**
     * Method to append Jws Signature To Response Context.
     * @param obapiResponseContext
     */
    @Generated(message = "Excluding from unit tests since it is covered by other methods")
    private void appendJwsSignatureToResponseContext(OBAPIResponseContext obapiResponseContext) {

        setXWso2ApiVersion(obapiResponseContext.getApiRequestInfo().getVersion());
        setXWso2ApiType(obapiResponseContext.getApiRequestInfo().getContext());

        String messageID = obapiResponseContext.getMsgInfo().getMessageId();

        if (!isApplicableForResponsePath(obapiResponseContext)) {
            if (log.isDebugEnabled()) {
                log.debug(String.format(
                        "Signature generation is not applicable for this response " +
                                "with message id : %s", messageID));
            }
            return;
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format(
                        "Generating signature for the response " +
                                "with message id : %s", messageID));
            }
            // Retrieve headers and payload.
            try {
                Map<String, String> responseHeaders = obapiResponseContext.getMsgInfo().getHeaders();
                Optional<String> payload = GatewayUtils.extractResponsePayload(obapiResponseContext,
                        obapiResponseContext.getMsgInfo().getHeaders());
                responseHeaders.put(signatureHeaderName, generateJWSSignature(payload));
                obapiResponseContext.setAddedHeaders(responseHeaders);
            } catch (OpenBankingException | OpenBankingExecutorException |
                    JOSEException e) {
                log.error("Unable to sign response", e);
                GatewayUtils.handleResponseInternalServerError(obapiResponseContext,
                        "Internal Server Error, Unable to sign the response",
                        OpenBankingErrorCodes.SERVER_ERROR_CODE);
            }
        }
    }

}
