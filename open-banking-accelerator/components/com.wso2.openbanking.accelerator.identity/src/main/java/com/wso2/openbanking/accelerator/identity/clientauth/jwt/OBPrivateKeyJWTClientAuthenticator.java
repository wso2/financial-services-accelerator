/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.identity.clientauth.jwt;

import com.wso2.openbanking.accelerator.common.util.Generated;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.bean.OAuthClientAuthnContext;
import org.wso2.carbon.identity.oauth2.token.handler.clientauth.jwt.Constants;
import org.wso2.carbon.identity.oauth2.token.handler.clientauth.jwt.PrivateKeyJWTClientAuthenticator;
import org.wso2.carbon.identity.oauth2.token.handler.clientauth.jwt.validator.JWTValidator;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * OBPrivateKeyJWTClientAuthenticator for authenticating private key jwt requests.
 */
public class OBPrivateKeyJWTClientAuthenticator extends PrivateKeyJWTClientAuthenticator {

    private static final Log log = LogFactory.getLog(OBPrivateKeyJWTClientAuthenticator.class);
    private static final String PAR_ENDPOINT_ALIAS = "ParEndpointAlias";

    @Generated(message = "Used only for testing purpose")
    protected OBPrivateKeyJWTClientAuthenticator(JWTValidator jwtValidator) {
        setJwtValidator(jwtValidator);
    }

    @Generated(message = "Does not contain logic")
    public OBPrivateKeyJWTClientAuthenticator() {

        int rejectBeforePeriod = Constants.DEFAULT_VALIDITY_PERIOD_IN_MINUTES;
        boolean preventTokenReuse = true;
        String endpointAlias = Constants.DEFAULT_AUDIENCE;
        try {
            if (isNotEmpty(properties.getProperty(PAR_ENDPOINT_ALIAS))) {
                endpointAlias = properties.getProperty(PAR_ENDPOINT_ALIAS);
            }
            if (isNotEmpty(properties.getProperty(Constants.PREVENT_TOKEN_REUSE))) {
                preventTokenReuse = Boolean.parseBoolean(properties.getProperty(Constants.PREVENT_TOKEN_REUSE));
            }
            if (isNotEmpty(properties.getProperty(Constants.REJECT_BEFORE_IN_MINUTES))) {
                rejectBeforePeriod = Integer.parseInt(properties.getProperty(Constants.REJECT_BEFORE_IN_MINUTES));
            }
            JWTValidator jwtValidator = createJWTValidator(endpointAlias, preventTokenReuse, rejectBeforePeriod);
            setJwtValidator(jwtValidator);
        } catch (NumberFormatException e) {
            log.warn("Invalid PrivateKeyJWT Validity period found in the configuration. Using default value: " +
                    rejectBeforePeriod);
        }
    }

    @Override
    public boolean canAuthenticate(HttpServletRequest httpServletRequest, Map<String, List> bodyParameters,
                                   OAuthClientAuthnContext oAuthClientAuthnContext) {

        log.debug("Request is being handled by OBPrivateKeyJWTClientAuthenticator");
        return super.canAuthenticate(httpServletRequest, bodyParameters, oAuthClientAuthnContext);
    }

    @Generated(message = "Does not contain logic")
    protected JWTValidator createJWTValidator(String accessedEndpoint, boolean preventTokenReuse, int rejectBefore) {

        String tokenEndpoint = OAuth2Util.OAuthURL.getOAuth2TokenEPUrl();
        String issuer = OAuth2Util.getIDTokenIssuer();

        List<String> acceptedAudienceList = new ArrayList<>();
        acceptedAudienceList.add(accessedEndpoint);
        acceptedAudienceList.add(tokenEndpoint);
        acceptedAudienceList.add(issuer);

        return new JWTValidator(preventTokenReuse, acceptedAudienceList, rejectBefore, null,
                populateMandatoryClaims(), Constants.DEFAULT_ENABLE_JTI_CACHE);
    }

    @Generated(message = "Does not contain logic")
    private List<String> populateMandatoryClaims() {

        List<String> mandatoryClaims = new ArrayList<>();
        mandatoryClaims.add(Constants.ISSUER_CLAIM);
        mandatoryClaims.add(Constants.SUBJECT_CLAIM);
        mandatoryClaims.add(Constants.AUDIENCE_CLAIM);
        mandatoryClaims.add(Constants.EXPIRATION_TIME_CLAIM);
        mandatoryClaims.add(Constants.JWT_ID_CLAIM);
        return mandatoryClaims;
    }
}
