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

package org.wso2.financial.services.accelerator.common.policy.filter.common;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.policy.FSPolicyExecutionException;
import org.wso2.financial.services.accelerator.common.policy.filter.FSFilterPolicy;
import org.wso2.financial.services.accelerator.common.policy.utils.FilterPolicyUtils;
import org.wso2.financial.services.accelerator.common.util.JWTUtils;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT Signature Validation Filter Policy.
 */
public class JWTSignatureValidationFilterPolicy extends FSFilterPolicy {

    private static final Log log = LogFactory.getLog(JWTSignatureValidationFilterPolicy.class);

    @Override
    public void processRequest(ServletRequest servletRequest, Map<String, Object> propertyMap)
            throws FSPolicyExecutionException {

        if (servletRequest instanceof HttpServletRequest) {
            String payload = null;
            JSONObject decodedPayload = null;
            try {
                payload = FilterPolicyUtils.getStringPayload((HttpServletRequest) servletRequest);
            } catch (FinancialServicesException e) {
                log.error("Error while retrieving the payload", e);
                throw new FSPolicyExecutionException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "invalid_request",
                        "Error while retrieving the payload", e);
            }
            try {
                if ("public-key".equals(propertyMap.get("type").toString())) {
                    JWTUtils.validateJWTSignatureWithPublicKey(payload, propertyMap.get("key-alias").toString());
                } else if ("jwks-uri".equals(propertyMap.get("type").toString())) {
                    SignedJWT signedJWT = SignedJWT.parse(payload);
                    String alg = signedJWT.getHeader().getAlgorithm().getName();
                    JWTUtils.validateJWTSignature(payload, propertyMap.get("jwks_url").toString(), alg);
                } else {
                    throw new FSPolicyExecutionException(HttpServletResponse.SC_BAD_REQUEST, "invalid_request",
                            "Invalid key type");
                }

                String decodedRequest = JWTUtils.decodeRequestJWT(payload, FinancialServicesConstants.JWT_BODY);
                if (Objects.nonNull(decodedRequest)) {
                    decodedPayload = new JSONObject(decodedRequest);
                    servletRequest.setAttribute("decodedPayload", decodedPayload);
                } else {
                    log.error("Error while decoding the JWT request payload");
                    throw new FSPolicyExecutionException(HttpServletResponse.SC_BAD_REQUEST, "invalid_request",
                            "Error occurred while decoding the request");
                }
            } catch (BadJOSEException | JOSEException | MalformedURLException | FinancialServicesException ex) {
                log.error("Error occurred while validating the signature", ex);
                throw new FSPolicyExecutionException(HttpServletResponse.SC_BAD_REQUEST, "invalid_request",
                        "Error occurred while validating the signature", ex);
            } catch (ParseException ex) {
                log.error("Error occurred while decoding the provided jwt", ex);
                throw new FSPolicyExecutionException(HttpServletResponse.SC_BAD_REQUEST, "invalid_request",
                        "Error occurred while decoding the provided jwt", ex);
            }
        }
    }

    @Override
    public void processResponse(ServletResponse servletResponse, Map<String, Object> propertyMap)
            throws FSPolicyExecutionException {

    }
}
