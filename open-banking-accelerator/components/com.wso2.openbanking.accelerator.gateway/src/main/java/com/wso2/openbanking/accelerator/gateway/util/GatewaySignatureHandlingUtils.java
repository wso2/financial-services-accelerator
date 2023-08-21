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

package com.wso2.openbanking.accelerator.gateway.util;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.util.Base64URL;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.identity.IdentityConstants;
import com.wso2.openbanking.accelerator.common.identity.retriever.ServerIdentityRetriever;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.gateway.executor.exception.OpenBankingExecutorException;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;
import java.util.Optional;

/**
 * Utility class for Signature Handling.
 */
public class GatewaySignatureHandlingUtils {

    private static final String B64_CLAIM_KEY = "b64";

    /**
     * Returns the JWS Header.
     * @param kid Key id of the signing certificate.
     * @param criticalParameters Hashmap of critical paramters
     * @param algorithm Signing algorithm
     * @return JWSHeader returns Jws Header
     */
    public static JWSHeader constructJWSHeader(String kid, HashMap<String, Object> criticalParameters,
                                               JWSAlgorithm algorithm) {
        return new JWSHeader.Builder(algorithm)
                .keyID(kid)
                .type(JOSEObjectType.JOSE)
                .criticalParams(criticalParameters.keySet())
                .customParams(criticalParameters)
                .build();
    }

    /**
     * Creates a JWS Object.
     * @param header JWS header
     * @param responsePayload response payload as a string
     * @return JWSObject jws object created
     */
    public static JWSObject constructJWSObject(JWSHeader header, String responsePayload) {
        return new JWSObject(header, new Payload(responsePayload));
    }

    /**
     * Returns the signing input with encoded jws header and un-encoded payload.
     * @param jwsHeader JWS Header
     * @param payloadString Response payload
     * @return signing input
     * @throws UnsupportedEncodingException throws UnsupportedEncodingException Exception
     */
    public static byte[] getSigningInput(JWSHeader jwsHeader, String payloadString)
            throws UnsupportedEncodingException {

        String combinedInput = jwsHeader.toBase64URL().toString() + "." + payloadString;
        return combinedInput.getBytes(StandardCharsets.UTF_8);
    }

    /**
     *  Method to create a detached jws.
     * @param jwsHeader header part of the JWS
     * @param signature signature part of the JWS
     * @return String Detached JWS
     */
    public static String createDetachedJws(JWSHeader jwsHeader, Base64URL signature) {

        return jwsHeader.toBase64URL().toString() + ".." + signature.toString();
    }

    /**
     * Loads the KID of the signing certificate.
     * @return String Key ID of the public key
     */
    @Generated(message = "Excluding from unit tests since there is a call to a method " +
            "in Common Module")
    public static String getSigningKeyId() {

        return OpenBankingConfigParser.getInstance().getOBIdnRetrieverSigningCertificateKid();
    }

    /**
     * Returns the signing key.
     *
     * @return Key Signing key
     * @throws OpenBankingExecutorException throws OpenBanking Exception
     */
    @Generated(message = "Excluding from unit tests since there is a call to a method " +
            "in Common Module")
    public static Optional<Key> getSigningKey() throws OpenBankingExecutorException {

        try {
            return ServerIdentityRetriever.getPrimaryCertificate(IdentityConstants.CertificateType.SIGNING);
        } catch (OpenBankingException e) {
            throw new OpenBankingExecutorException("Unable to load primary signing certificate", e);
        }
    }

    @Generated(message = "Excluding from unit tests since a signer is required to create a valid JWSObject")
    public static String createDetachedJws(String serializedJws) {

        String[] jwsParts = StringUtils.split(serializedJws, ".");
        return jwsParts[0] + ".." + jwsParts[2];
    }

    /**
     * JWSAlgorithm to be returned in JWS header when signing.
     * can be extended at toolkit level.
     *
     * @return JWSAlgorithm the signing algorithm defined to use in configs
     */
    @Generated(message = "Excluding from unit tests since there is a call to a method " +
            "in Common Module")
    public static JWSAlgorithm getSigningAlgorithm()  {

        String alg = OpenBankingConfigParser.getInstance().getJwsResponseSigningAlgorithm();
        return JWSAlgorithm.parse(alg);
    }

    /**
     * If the b64 header is not available or is true, it is verifiable.
     *
     * @param jwsObject The reconstructed jws object parsed from x-jws-signature
     * @return Boolean
     */
    public static boolean isB64HeaderVerifiable(JWSObject jwsObject) {

        JWSHeader jwsHeader = jwsObject.getHeader();
        Object b64Value = jwsHeader.getCustomParam(B64_CLAIM_KEY);
        return b64Value != null ? ((Boolean) b64Value) : true;
    }
}
