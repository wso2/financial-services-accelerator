/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
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

package org.wso2.financial.services.accelerator.gateway.dpop.util;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.financial.services.accelerator.common.logging.Log;
import org.wso2.financial.services.accelerator.common.logging.LogFactory;
import org.wso2.financial.services.accelerator.gateway.dpop.DPoPConstants;
import org.wso2.financial.services.accelerator.gateway.dpop.proof.DPoPProofException;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils.equalsIgnoreCase;
import static org.wso2.financial.services.accelerator.gateway.dpop.DPoPConstants.DPOP_HEADER;

/**
 * Pure helpers used across the DPoP handler — JWT inspection, transport-header
 * access, and request-URI normalization for {@code htu} comparison.
 */
public final class DPoPUtils {

    private static final Log log = LogFactory.getLog(DPoPUtils.class);

    private static final String PROPERTY_REQUEST_PATH = "REST_FULL_REQUEST_PATH";
    private static final String PROPERTY_TRANSPORT = "TRANSPORT_IN_NAME";
    private static final String HEADER_HOST = "Host";

    private DPoPUtils() {
    }

    /**
     * Extracts {@code cnf.jkt} from a JWT access token without verifying the
     * signature — signature verification is the downstream authenticator's job.
     */
    public static String extractJktFromJwt(String accessToken) throws DPoPProofException {
        try {
            JWT jwt = JWTParser.parse(accessToken);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (claims == null) {
                return null;
            }
            Object cnf = claims.getClaim(DPoPConstants.Claims.CNF_CLAIM);
            if (cnf instanceof Map) {
                @SuppressWarnings("unchecked")
                Object jkt = ((Map<String, Object>) cnf).get(DPoPConstants.Claims.JKT_CLAIM);
                return jkt != null ? jkt.toString() : null;
            }
            return null;
        } catch (ParseException e) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_TOKEN,
                    "Failed to parse JWT access token for cnf.jkt extraction: " + e.getMessage(), e);
        }
    }

    /**
     * Returns true if {@code token} has the three-part JWS compact structure.
     */
    public static boolean isJwtToken(String token) {

        if (StringUtils.isBlank(token)) {
            return false;
        }
        return token.split("\\.").length == 3;
    }

    /**
     * Reconstructs and normalizes the {@code htu} (HTTP target URI) for DPoP proof
     * verification per RFC 9449 §4.3: query string and fragment are stripped.
     *
     * @return {@code scheme://host/path} or {@code null} if the URI cannot be built
     */
    public static String normalizeHtu(org.apache.synapse.MessageContext synCtx, Map<String, String> headers) {
        try {
            final String authority = headers.get(HEADER_HOST);
            final String scheme = (String) synCtx.getProperty(PROPERTY_TRANSPORT);
            final String path = (String) synCtx.getProperty(PROPERTY_REQUEST_PATH);
            return new URI(scheme, authority, path, null, null).toString();
        } catch (URISyntaxException e) {
            log.error("Unable to construct URI from message context properties. Caused by, ", e);
            return null;
        }
    }

    private static Map<String, String> getHeaderMapByKey(org.apache.synapse.MessageContext synCtx, String propertyKey) {

        @SuppressWarnings("unchecked") final Map<String, String> headersMap =
                (Map<String, String>) ((Axis2MessageContext) synCtx).getAxis2MessageContext().getProperty(propertyKey);
        // Use a case-insensitive TreeMap because HTTP header names are case-insensitive
        final Map<String, String> headersTreeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (headersMap != null) {
            headersTreeMap.putAll(headersMap);
        }
        return headersTreeMap;
    }

    /**
     * Returns the request transport headers as a case-insensitive map.
     */
    public static Map<String, String> getTransportHeaders(org.apache.synapse.MessageContext synCtx) {

        return getHeaderMapByKey(synCtx, MessageContext.TRANSPORT_HEADERS);
    }

    /**
     * Returns any excess (duplicate) transport headers as a case-insensitive map.
     * Used to detect multiple {@code DPoP} header fields per RFC 9449 §4.3.
     */
    public static Map<String, String> getExcessTransportHeaders(org.apache.synapse.MessageContext synCtx) {

        return getHeaderMapByKey(synCtx, NhttpConstants.EXCESS_TRANSPORT_HEADERS);
    }

    /**
     * Throws {@link DPoPProofException} if {@code htu} uses plain HTTP.
     */
    public static void checkHttps(final String htu) throws DPoPProofException {

        if (htu != null && htu.startsWith("http://")) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "DPoP is not permitted over plain HTTP");
        }
    }

    /**
     * Returns {@code true} if the message context contains more than one {@code DPoP} header field.
     */
    public static boolean hasMultipleDPopHeaders(org.apache.synapse.MessageContext synCtx) {

        final Map<String, String> excessTransportHeaders = DPoPUtils.getExcessTransportHeaders(synCtx);
        return !excessTransportHeaders.isEmpty() && excessTransportHeaders.containsKey(DPOP_HEADER);
    }

    /**
     * Strips the {@code "<scheme> "} prefix from {@code authHeader} and returns the token value,
     * or {@code null} if the header is absent or does not start with the given scheme.
     */
    public static String extractToken(String authHeader, String scheme) {
        if (authHeader == null) {
            return null;
        }
        String prefix = scheme + " ";
        if (authHeader.regionMatches(true, 0, prefix, 0, prefix.length())) {
            return authHeader.substring(prefix.length()).trim();
        }
        return null;
    }

    /**
     * Removes all entries for {@code name} from {@code headers}, case-insensitively.
     */
    public static void removeHeader(Map<String, String> headers, String name) {
        headers.remove(name);
        headers.entrySet().removeIf(e -> equalsIgnoreCase(name, e.getKey()));
    }

    /**
     * Parses a comma-separated algorithm list. Returns the default set if the input is blank.
     */
    public static Set<String> parseAlgorithms(String csv) {
        String value = StringUtils.isBlank(csv) ? DPoPConstants.Defaults.ACCEPTED_ALGORITHMS : csv;
        Set<String> result = new HashSet<>();
        for (String alg : value.split(",")) {
            String trimmed = alg.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        if (result.isEmpty()) {
            return parseAlgorithms(DPoPConstants.Defaults.ACCEPTED_ALGORITHMS);
        }
        return result;
    }

    /**
     * Parses {@code value} as a boolean. Returns {@code defaultValue} if {@code value} is {@code null}.
     */
    public static boolean parseBool(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(String.valueOf(value).trim());
    }

    /**
     * Parses {@code value} as a long. Returns {@code defaultValue} if {@code value} is {@code null}
     * or not a valid number.
     */
    public static long parseLong(Object value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses {@code value} as a trimmed string. Returns {@code defaultValue} if {@code value} is
     * {@code null} or blank.
     */
    public static String parseString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String s = String.valueOf(value).trim();
        return s.isEmpty() ? defaultValue : s;
    }
}
