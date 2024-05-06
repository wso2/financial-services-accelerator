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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Util class for jwt related functions.
 */
public class JWTUtils {
    private static final String DOT_SEPARATOR = ".";
    private static final long DEFAULT_TIME_SKEW_IN_SECONDS = 300L;
    private static final Log log = LogFactory.getLog(JWTUtils.class);


    /**
     * Decode request JWT.
     *
     * @param jwtToken jwt sent by the tpp
     * @param jwtPart  expected jwt part (header, body)
     * @return json object containing requested jwt part
     * @throws ParseException if an error occurs while parsing the jwt
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
     * @throws ParseException    if an error occurs while parsing the jwt
     * @throws BadJOSEException  if the jwt is invalid
     * @throws JOSEException     if an error occurs while processing the jwt
     * @throws MalformedURLException if an error occurs while creating the URL object
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
        jwtProcessor.process((SignedJWT) jwt, securityContext);
        return true;
    }

    public static SignedJWT getSignedJWT(String jwtString) throws ParseException {
        if (isJWT(jwtString)){
            return SignedJWT.parse(jwtString);
        }else{
            throw new IllegalArgumentException("Provided token identifier is not a parsable JWT.");
        }
    }

    /**
     * Validate legitimacy of JWT.
     *
     * @param jwtString JWT string
     */
    public static boolean isJWT(String jwtString) {
        if (jwtString == null){
            return false;
        }
        if (StringUtils.isBlank(jwtString)) {
            return false;
        }
        if (StringUtils.countMatches(jwtString, DOT_SEPARATOR) != 2) {
            return false;
        }
        try {
            JWTParser.parse(jwtString);
            return true;
        } catch (ParseException e) {
            if (log.isDebugEnabled()) {
                log.debug("Provided token identifier is not a parsable JWT.", e);
            }
            return false;
        }
    }

    public static JWTClaimsSet getJWTClaimsSet(SignedJWT signedJWT) throws ParseException{
        return signedJWT.getJWTClaimsSet();
    }

    public static <T> T getClaim(JWTClaimsSet jwtClaimsSet ,String claim){
        Object claimObj = jwtClaimsSet.getClaim(claim);
        return (T) claimObj;
    }
}
