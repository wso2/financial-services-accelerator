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

package com.wso2.openbanking.accelerator.identity.token;

import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.CertificateUtils;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.token.util.TokenFilterException;
import com.wso2.openbanking.accelerator.identity.token.validators.OBIdentityFilterValidator;
import com.wso2.openbanking.accelerator.identity.token.wrapper.RequestWrapper;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonHelper;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter engaged for /token request.
 */
public class TokenFilter implements Filter {

    private static final Log log = LogFactory.getLog(TokenFilter.class);
    private static DefaultTokenFilter defaultTokenFilter;
    private String clientId = null;
    private static List<OBIdentityFilterValidator> validators = new ArrayList<>();

    private static final String BASIC_AUTH_ERROR_MSG = "Unable to find client id in the request. " +
            "Invalid Authorization header found.";

    @Generated(message = "Ignoring because it's a the init method")
    @Override
    public void init(FilterConfig filterConfig) {

        ServletContext context = filterConfig.getServletContext();
        context.log("TokenFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            clientId = this.extractClientId(request);
        } catch (TokenFilterException e) {
            getDefaultTokenFilter().handleValidationFailure((HttpServletResponse) response, e.getErrorCode(),
                    e.getMessage(), e.getErrorDescription());
            return;
        }

        try {
            request = cleanClientCertificateAndAppendTransportHeader(request);
            if (IdentityCommonUtil.getRegulatoryFromSPMetaData(clientId)) {
                request = appendTransportHeader(request, response);
                request = getDefaultTokenFilter().handleFilterRequest(request);
                for (OBIdentityFilterValidator validator : getValidators()) {
                    validator.validate(request, clientId);
                }
                response = getDefaultTokenFilter().handleFilterResponse(response);
            }
            chain.doFilter(request, response);
        } catch (TokenFilterException e) {
            getDefaultTokenFilter().handleValidationFailure((HttpServletResponse) response,
                    e.getErrorCode(), e.getMessage(), e.getErrorDescription());
        } catch (CertificateEncodingException e) {
            throw new ServletException("Certificate not valid", e);
        } catch (OpenBankingException e) {
            if (e.getMessage().contains("Error occurred while retrieving OAuth2 application data")) {
                getDefaultTokenFilter().handleValidationFailure((HttpServletResponse) response,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR, IdentityCommonConstants
                                .OAUTH2_INTERNAL_SERVER_ERROR, "OAuth2 application data retrieval failed."
                                + e.getMessage());
            } else {
                getDefaultTokenFilter().handleValidationFailure((HttpServletResponse) response,
                        HttpServletResponse.SC_BAD_REQUEST, IdentityCommonConstants.OAUTH2_INVALID_REQUEST_MESSAGE,
                        "Service provider metadata retrieval failed. " + e.getMessage());
            }
        }
    }

    /**
     * Append the transport header to the request.
     *
     * @param request
     * @param response
     * @return ServletRequest
     * @throws ServletException
     */
    private ServletRequest appendTransportHeader(ServletRequest request, ServletResponse response) throws
            ServletException, IOException, CertificateEncodingException {

        if (request instanceof HttpServletRequest) {
            Object certAttribute = request.getAttribute(IdentityCommonConstants.JAVAX_SERVLET_REQUEST_CERTIFICATE);
            String x509Certificate = ((HttpServletRequest) request).getHeader(IdentityCommonUtil.getMTLSAuthHeader());
            if (new IdentityCommonHelper().isTransportCertAsHeaderEnabled() && x509Certificate != null) {
                return request;
            } else if (certAttribute != null) {
                RequestWrapper requestWrapper = new RequestWrapper((HttpServletRequest) request);
                X509Certificate certificate = IdentityCommonUtil.getCertificateFromAttribute(certAttribute);
                requestWrapper.setHeader(IdentityCommonUtil.getMTLSAuthHeader(),
                        new IdentityCommonHelper().encodeCertificateContent(certificate));
                return requestWrapper;
            } else {
                getDefaultTokenFilter().handleValidationFailure((HttpServletResponse) response,
                        HttpServletResponse.SC_BAD_REQUEST, IdentityCommonConstants.OAUTH2_INVALID_REQUEST_MESSAGE,
                        "Transport certificate not found in the request");
            }
        } else {
            throw new ServletException("Error occurred when handling the request, passed request is not a " +
                    "HttpServletRequest");
        }
        return request;
    }

    /**
     * Invoked after all execution of the filter has completed and the filter is being taken out of service.
     */
    @Generated(message = "Ignoring because it's a clean up code")
    @Override
    public void destroy() {
        // No special cleanup is required in this filter.
    }

    /**
     * @return Token filter
     */
    @Generated(message = "Ignoring because the method is reading the configuration")
    public DefaultTokenFilter getDefaultTokenFilter() {

        return defaultTokenFilter;
    }

    /**
     * Extracts the client id from the request parameter or from the assertion.
     *
     * @param request servlet request containing the request data
     * @return clientId
     * @throws ParseException
     */
    private String extractClientId(ServletRequest request) throws TokenFilterException {

        try {
            Optional<String> signedObject =
                    Optional.ofNullable(request.getParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION));
            Optional<String> clientIdAsReqParam =
                    Optional.ofNullable(request.getParameter(IdentityCommonConstants.CLIENT_ID));
            if (signedObject.isPresent()) {
                SignedJWT signedJWT = SignedJWT.parse(signedObject.get());
                return signedJWT.getJWTClaimsSet().getIssuer();
            } else if (clientIdAsReqParam.isPresent()) {
                return clientIdAsReqParam.get();
            } else if (((HttpServletRequest) request).getHeader("Authorization") != null) {
                // This added condition will only affect the requests with Basic Authentication and the others will be
                // handled by the above conditions as previously.
                String authorizationHeader = ((HttpServletRequest) request).getHeader("Authorization");
                if (authorizationHeader.split(" ").length == 2) {
                    String authToken = ((HttpServletRequest) request).getHeader("Authorization").split(" ")[1];
                    byte[] decodedBytes = Base64.getUrlDecoder().decode(authToken.getBytes(StandardCharsets.UTF_8));
                    String decodedAuthToken = new String(decodedBytes, StandardCharsets.UTF_8);
                    if (decodedAuthToken.split(":").length == 2) {
                        return decodedAuthToken.split(":")[0];
                    } else {
                        log.error(BASIC_AUTH_ERROR_MSG);
                        throw new TokenFilterException(HttpServletResponse.SC_BAD_REQUEST,
                                "Could not retrieve Client ID", BASIC_AUTH_ERROR_MSG);
                    }
                } else {
                    log.error(BASIC_AUTH_ERROR_MSG);
                    throw new TokenFilterException(HttpServletResponse.SC_BAD_REQUEST, "Could not retrieve Client ID",
                            BASIC_AUTH_ERROR_MSG);
                }
            } else {
                throw new TokenFilterException(HttpServletResponse.SC_BAD_REQUEST, IdentityCommonConstants
                        .OAUTH2_INVALID_REQUEST_MESSAGE, "Unable to find client id in the request");
            }
        } catch (ParseException e) {
            throw new TokenFilterException(HttpServletResponse.SC_UNAUTHORIZED, IdentityCommonConstants
                    .OAUTH2_INVALID_REQUEST_MESSAGE, "Error occurred while parsing the signed assertion", e);
        }
    }

    public static void setDefaultTokenFilter(DefaultTokenFilter tokenFilter) {

        defaultTokenFilter = tokenFilter;
    }

    public static void setValidators(List<OBIdentityFilterValidator> validators) {

        TokenFilter.validators = validators;
    }

    public List<OBIdentityFilterValidator> getValidators() {

        return validators;
    }

    /**
     * validates the transport header certificate and re-add to the header.
     *
     * @param request ServletRequest
     * @return request
     * @throws OpenBankingException
     * @throws ServletException
     */
    private ServletRequest cleanClientCertificateAndAppendTransportHeader(ServletRequest request)
            throws ServletException, OpenBankingException {

        if (request instanceof HttpServletRequest) {
            IdentityCommonHelper identityCommonHelper = new IdentityCommonHelper();
            if (identityCommonHelper.isTransportCertAsHeaderEnabled()) {
                log.debug("Retrieving client transport certificate from header.");
                String x509Certificate = ((HttpServletRequest) request)
                        .getHeader(IdentityCommonUtil.getMTLSAuthHeader());
                if (StringUtils.isNotEmpty(x509Certificate) && isClientCertificateEncoded()) {
                    try {
                        log.debug("Received encoded client certificate. URLDecoding cert.");
                        x509Certificate = URLDecoder.decode(x509Certificate, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new OpenBankingException("Cannot decode the transport certificate passed through " +
                                "the request", e);
                    }
                }

                try {
                    X509Certificate certificate = CertificateUtils.parseCertificate(x509Certificate);
                    if (certificate != null) {
                        RequestWrapper requestWrapper = new RequestWrapper((HttpServletRequest) request);
                        requestWrapper.setHeader(IdentityCommonUtil.getMTLSAuthHeader(),
                                new IdentityCommonHelper().encodeCertificateContent(certificate));
                        return requestWrapper;
                    }
                } catch (CertificateEncodingException e) {
                    throw new ServletException("Certificate not valid", e);
                } catch (OpenBankingException e) {
                    // Ignore the error here, MTLSEnforcementValidator will validate the certificate
                    log.error("Invalid transport certificate received. Caused by, ", e);
                }
            }
        } else {
            throw new ServletException("Error occurred when handling the request, passed request is not a " +
                    "HttpServletRequest");
        }
        return request;
    }

    /**
     * Get the clientCertificateEncode configuration.
     *
     * @return false if clientCertificateEncode configured as false, default true
     */
    public boolean isClientCertificateEncoded() {

        Object isClientCertEncoded = IdentityExtensionsDataHolder.getInstance().getConfigurationMap()
                .getOrDefault(IdentityCommonConstants.CLIENT_CERTIFICATE_ENCODE, true);

        return Boolean.parseBoolean(String.valueOf(isClientCertEncoded));
    }
}
