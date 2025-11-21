/**
 * Copyright (c) 2023-2024, WSO2 LLC. (https://www.wso2.com).
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
package com.wso2.openbanking.accelerator.common.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import net.minidev.json.JSONObject;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Util class for jwt related functions
 */
public class JWTUtils {


    /**
     * Decode request JWT
     *
     * @param jwtToken jwt sent by the tpp
     * @param jwtPart  expected jwt part (header, body)
     * @return json object containing requested jwt part
     */
    public static JSONObject decodeRequestJWT(String jwtToken, String jwtPart) throws ParseException {

        JSONObject jsonObject = new JSONObject();

        JWSObject plainObject = JWSObject.parse(jwtToken);

        if ("head".equals(jwtPart)) {
            jsonObject = plainObject.getHeader().toJSONObject();
        } else if ("body".equals(jwtPart)) {
            jsonObject = plainObject.getPayload().toJSONObject();
        }

        return jsonObject;

    }

    /**
     * Validate the signed JWT by querying a jwks.
     *
     * @param jwtString signed json web token
     * @param jwksUri   endpoint displaying the key set for the signing certificates
     * @param algorithm the signing algorithm for jwt
     * @return true if signature is valid
     */
    @Generated(message = "Excluding from code coverage since can not call this method due to external https call")
    public static boolean validateJWTSignature(String jwtString, String jwksUri, String algorithm)
            throws ParseException, BadJOSEException, JOSEException, MalformedURLException {

        int defaultConnectionTimeout = 3000;
        int defaultReadTimeout = 3000;
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        JWT jwt = JWTParser.parse(jwtString);
        // set the Key Selector for the jwks_uri.
        Map<String, RemoteJWKSet<SecurityContext>> jwkSourceMap = new ConcurrentHashMap<>();
        RemoteJWKSet<SecurityContext> jwkSet = jwkSourceMap.get(jwksUri);
        if (jwkSet == null) {
            int connectionTimeout = Integer.parseInt(OpenBankingConfigParser.getInstance().getJWKSConnectionTimeOut());
            int readTimeout = Integer.parseInt(OpenBankingConfigParser.getInstance().getJWKSReadTimeOut());
            int sizeLimit = RemoteJWKSet.DEFAULT_HTTP_SIZE_LIMIT;
            if (connectionTimeout == 0 && readTimeout == 0) {
                connectionTimeout = defaultConnectionTimeout;
                readTimeout = defaultReadTimeout;
            }
            DefaultResourceRetriever resourceRetriever = new DefaultResourceRetriever(
                    connectionTimeout,
                    readTimeout,
                    sizeLimit);
            jwkSet = new RemoteJWKSet<>(new URL(jwksUri), resourceRetriever);
            jwkSourceMap.put(jwksUri, jwkSet);
        }
        // The expected JWS algorithm of the access tokens (agreed out-of-band).
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.parse(algorithm);
        //Configure the JWT processor with a key selector to feed matching public RSA keys sourced from the JWK set URL.
        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, jwkSet);
        jwtProcessor.setJWSKeySelector(keySelector);
        // Process the token, set optional context parameters.
        SimpleSecurityContext securityContext = new SimpleSecurityContext();
        JWTClaimsSet claims = jwtProcessor.process((SignedJWT) jwt, securityContext);

        // Validate iat with configured timestamp skew if validation is enabled and iat is sent in the request.
        boolean validateIat = OpenBankingConfigParser.getInstance().isDCRIATValidationEnabled();
        Date iat = claims.getIssueTime();
        if (validateIat && iat != null) {
            long timestampSkew = java.util.concurrent.TimeUnit.SECONDS.toMillis(
                    OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds());
            long now = System.currentTimeMillis();
            if (iat.getTime() > now + Math.max(0, timestampSkew)) {
                throw new BadJOSEException("Invalid 'iat' claim: issued-at is in the future");
            }
        }
        return true;
    }

}
