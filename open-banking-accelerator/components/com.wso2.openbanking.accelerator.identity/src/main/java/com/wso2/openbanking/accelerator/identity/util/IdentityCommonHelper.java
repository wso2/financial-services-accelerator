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

package com.wso2.openbanking.accelerator.identity.util;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.CertificateUtils;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.ServiceProviderUtils;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.bean.OAuthClientAuthnContext;
import org.wso2.carbon.identity.oauth2.dao.OAuthTokenPersistenceFactory;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Identity common helper class.
 */
public class IdentityCommonHelper {

    private static final Log log = LogFactory.getLog(IdentityCommonHelper.class);

    /**
     * Utility method get the application property from SP Meta Data.
     * @param clientId ClientId of the application
     * @return the service provider certificate
     * @throws OpenBankingException
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public String getCertificateContent(String clientId) throws OpenBankingException {

        Optional<ServiceProvider> serviceProvider;
        try {
            serviceProvider = Optional.ofNullable(IdentityExtensionsDataHolder.getInstance()
                    .getApplicationManagementService().getServiceProviderByClientId(clientId,
                            IdentityApplicationConstants.OAuth2.NAME,
                            ServiceProviderUtils.getSpTenantDomain(clientId)));
            if (serviceProvider.isPresent()) {
                return serviceProvider.get().getCertificateContent();
            }
        } catch (IdentityApplicationManagementException e) {
            log.error(String.format("Error occurred while retrieving OAuth2 application data for clientId %s",
                    clientId), e);
            throw new OpenBankingException("Error occurred while retrieving OAuth2 application data for clientId"
                    , e);
        }
        return "";
    }

    /**
     * Utility method get the application property from SP Meta Data.
     *
     * @param clientId ClientId of the application
     * @param property Property of the application
     * @return the property value from SP metadata
     * @throws OpenBankingException
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public String getAppPropertyFromSPMetaData(String clientId, String property) throws OpenBankingException {

        String spProperty = null;

        if (StringUtils.isNotEmpty(clientId)) {
            Optional<ServiceProvider> serviceProvider;
            try {
                serviceProvider = Optional.ofNullable(IdentityExtensionsDataHolder.getInstance()
                        .getApplicationManagementService().getServiceProviderByClientId(clientId,
                                IdentityApplicationConstants.OAuth2.NAME,
                                ServiceProviderUtils.getSpTenantDomain(clientId)));
                if (serviceProvider.isPresent()) {
                    spProperty = Arrays.stream(serviceProvider.get().getSpProperties())
                            .collect(Collectors.toMap(ServiceProviderProperty::getName,
                                    ServiceProviderProperty::getValue))
                            .get(property);
                }
            } catch (IdentityApplicationManagementException e) {
                log.error(String.format("Error occurred while retrieving OAuth2 application data for clientId %s",
                        clientId), e);
                throw new OpenBankingException("Error occurred while retrieving OAuth2 application data for clientId"
                        , e);
            }
        } else {
            log.error(IdentityCommonConstants.CLIENT_ID_ERROR);
            throw new OpenBankingException(IdentityCommonConstants.CLIENT_ID_ERROR);
        }

        return spProperty;
    }

    /**
     * Validate whether the request follows mtls authentication pattern.
     *
     * @param request servlet request
     * @return whether request fallows MTLS pattern
     */
    public boolean isMTLSAuthentication(ServletRequest request) throws
            OpenBankingException {

        if (request instanceof HttpServletRequest) {
            String oauthClientID =  request.getParameter(IdentityCommonConstants.OAUTH_CLIENT_ID);
            String oauthClientSecret = request.getParameter(IdentityCommonConstants.OAUTH_CLIENT_SECRET);
            String oauthJWTAssertion = request.getParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION);
            String oauthJWTAssertionType = request.getParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION_TYPE);
            HttpServletRequest servletRequest = (HttpServletRequest) request;
            String authorizationHeader = servletRequest.getHeader(IdentityCommonConstants.AUTHORIZATION_HEADER);
            String x509Certificate = servletRequest.getHeader(IdentityCommonUtil.getMTLSAuthHeader());
            return (StringUtils.isNotEmpty(oauthClientID) && StringUtils.isEmpty(oauthClientSecret) &&
                    StringUtils.isEmpty(oauthJWTAssertion) && StringUtils.isEmpty(oauthJWTAssertionType) &&
                    StringUtils.isEmpty(authorizationHeader) && x509Certificate != null &&
                    CertificateUtils.parseCertificate(x509Certificate) != null);
        } else {
            throw new OpenBankingException("Error occurred during request validation, passed request is not a " +
                    "HttpServletRequest");
        }
    }

    /**
     * Get the configured value of the transport cert as header enable.
     *
     * @return value of the transport cert as header enable
     */
    public boolean isTransportCertAsHeaderEnabled() {

        Optional<Object> certAsHeader =
                Optional.ofNullable(IdentityExtensionsDataHolder.getInstance().getConfigurationMap()
                        .get(IdentityCommonConstants.ENABLE_TRANSPORT_CERT_AS_HEADER));
        return certAsHeader.filter(isEnabled -> Boolean.parseBoolean(isEnabled.toString())).isPresent();
    }

    /**
     * Retrieve all Service provider information.
     *
     * @return list of service providers
     * @throws IdentityApplicationManagementException when get application basic info fails
     * @throws UserStoreException when get realm configuration fails
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public List<ServiceProvider> getAllServiceProviders()
            throws IdentityApplicationManagementException, UserStoreException {

        ApplicationManagementService applicationManagementService = IdentityExtensionsDataHolder.getInstance()
                .getApplicationManagementService();

        List<ServiceProvider> serviceProviderList = new ArrayList<>();
        if (applicationManagementService != null) {

            RealmConfiguration realmConfig = IdentityExtensionsDataHolder.getInstance().getRealmService()
                        .getBootstrapRealm().getUserStoreManager().getRealmConfiguration();

            final String adminUsername = realmConfig.getAdminUserName();
            final String tenantDomain = MultitenantUtils.getTenantDomain(adminUsername);
            final int totalApplicationCount = applicationManagementService
                    .getCountOfAllApplications(tenantDomain, adminUsername);

            ApplicationBasicInfo[] applicationBasicInfo = applicationManagementService
                    .getApplicationBasicInfo(tenantDomain, adminUsername, 0, totalApplicationCount);
            // Set tenant domain before calling applicationManagementService
            if (CarbonContext.getThreadLocalCarbonContext().getTenantDomain() == null) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            }
            if (applicationBasicInfo != null && applicationBasicInfo.length > 0) {
                for (ApplicationBasicInfo basicInfo : applicationBasicInfo) {
                    serviceProviderList
                            .add(applicationManagementService.getServiceProvider(basicInfo.getApplicationId()));
                }
            }
        }
        return serviceProviderList;
    }

    /**
     * Encode the certificate content.
     * @param certificate
     * @return
     * @throws CertificateEncodingException
     */
    public String encodeCertificateContent(X509Certificate certificate) throws CertificateEncodingException {
        if (certificate != null) {
            byte[] encodedContent = certificate.getEncoded();
            return IdentityCommonConstants.BEGIN_CERT + new String(Base64.getEncoder().encode(encodedContent),
                    StandardCharsets.UTF_8) + IdentityCommonConstants.END_CERT;
        } else {
            return null;
        }
    }

    /**
     * Revokes access tokens for the given client id.
     *
     * @param clientId consumer key of the application
     * @throws IdentityOAuth2Exception when revoking access tokens fails
     */
    @Generated(message = "Excluding from code coverage since it requires service calls")
    public void revokeAccessTokensByClientId(@Nullable final String clientId) throws IdentityOAuth2Exception {

        if (StringUtils.isEmpty(clientId)) {
            return;
        }

        Set<String> activeTokens = OAuthTokenPersistenceFactory.getInstance().getAccessTokenDAO()
                .getActiveTokensByConsumerKey(clientId);
        if (!activeTokens.isEmpty()) {
            OAuthClientAuthnContext oAuthClientAuthnContext = new OAuthClientAuthnContext();
            oAuthClientAuthnContext.setAuthenticated(true);
            oAuthClientAuthnContext.setClientId(clientId);

            OAuthRevocationRequestDTO revocationRequestDTO = new OAuthRevocationRequestDTO();
            revocationRequestDTO.setOauthClientAuthnContext(oAuthClientAuthnContext);
            revocationRequestDTO.setConsumerKey(clientId);
            revocationRequestDTO.setTokenType(GrantType.REFRESH_TOKEN.toString());

            for (String accessToken : activeTokens) {
                revocationRequestDTO.setToken(accessToken);
                OAuthRevocationResponseDTO oAuthRevocationResponseDTO =  IdentityExtensionsDataHolder.getInstance()
                        .getOAuth2Service().revokeTokenByOAuthClient(revocationRequestDTO);

                if (oAuthRevocationResponseDTO.isError()) {
                    throw new IdentityOAuth2Exception(
                            String.format("Error occurred while revoking access tokens for clientId: %s. Caused by, %s",
                                    clientId, oAuthRevocationResponseDTO.getErrorMsg()));
                }
            }
        }
    }
}
