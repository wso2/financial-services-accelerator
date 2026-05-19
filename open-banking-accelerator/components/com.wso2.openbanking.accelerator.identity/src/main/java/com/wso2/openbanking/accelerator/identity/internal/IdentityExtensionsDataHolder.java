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

package com.wso2.openbanking.accelerator.identity.internal;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.identity.auth.extensions.adaptive.function.OpenBankingAuthenticationWorker;
import com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.OBRequestObjectValidator;
import com.wso2.openbanking.accelerator.identity.auth.extensions.response.handler.OBResponseTypeHandler;
import com.wso2.openbanking.accelerator.identity.claims.OBClaimProvider;
import com.wso2.openbanking.accelerator.identity.common.IdentityServiceExporter;
import com.wso2.openbanking.accelerator.identity.dcr.validation.DCRCommonConstants;
import com.wso2.openbanking.accelerator.identity.dcr.validation.RegistrationValidator;
import com.wso2.openbanking.accelerator.identity.interceptor.OBIntrospectionDataProvider;
import com.wso2.openbanking.accelerator.identity.listener.application.AbstractApplicationUpdater;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.PushAuthRequestValidator;
import com.wso2.openbanking.accelerator.identity.token.DefaultTokenFilter;
import com.wso2.openbanking.accelerator.identity.token.TokenFilter;
import com.wso2.openbanking.accelerator.identity.token.validators.OBIdentityFilterValidator;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.throttler.service.OBThrottleService;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.oauth.OAuthAdminServiceImpl;
import org.wso2.carbon.identity.oauth2.IntrospectionDataProvider;
import org.wso2.carbon.identity.oauth2.OAuth2Service;
import org.wso2.carbon.identity.oauth2.client.authentication.OAuthClientAuthnService;
import org.wso2.carbon.identity.openidconnect.ClaimProvider;
import org.wso2.carbon.identity.openidconnect.RequestObjectService;
import org.wso2.carbon.user.core.service.RealmService;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wso2.openbanking.accelerator.common.util.OpenBankingUtils.getClassInstanceFromFQN;
import static com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants.PUSH_AUTH_REQUEST_VALIDATOR;
import static com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants.REQUEST_VALIDATOR;
import static com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants.RESPONSE_HANDLER;

/**
 * Data Holder for Open Banking Common.
 */
public class IdentityExtensionsDataHolder {

    private static volatile IdentityExtensionsDataHolder instance = new IdentityExtensionsDataHolder();
    private ApplicationManagementService applicationManagementService;
    private RequestObjectService requestObjectService;
    private OAuthAdminServiceImpl oAuthAdminService;
    private OpenBankingConfigurationService openBankingConfigurationService;
    private Map<String, Object> configurationMap;
    private Map<String, Map<String, Object>> dcrRegistrationConfigMap;
    private List<OBIdentityFilterValidator> tokenValidators = new ArrayList<>();
    private List<String> scopeRestrictedGrantTypes = new ArrayList<>();
    private DefaultTokenFilter defaultTokenFilter;
    private RegistrationValidator registrationValidator;
    private ClaimProvider claimProvider;
    private IntrospectionDataProvider introspectionDataProvider;
    private OBRequestObjectValidator obRequestObjectValidator;
    private PushAuthRequestValidator pushAuthRequestValidator;
    private KeyStore trustStore = null;
    private OBResponseTypeHandler obResponseTypeHandler;
    private AbstractApplicationUpdater abstractApplicationUpdater;
    private int identityCacheAccessExpiry;
    private int identityCacheModifiedExpiry;
    private RealmService realmService;
    private OBThrottleService obThrottleService;
    private ConsentCoreService consentCoreService;
    private OAuthClientAuthnService oAuthClientAuthnService;
    private OAuth2Service oAuth2Service;
    private JsFunctionRegistry jsFunctionRegistry;

    private Map<String, OpenBankingAuthenticationWorker> workers = new HashMap<>();

    private IdentityExtensionsDataHolder() {

    }

    public static IdentityExtensionsDataHolder getInstance() {

        if (instance == null) {
            synchronized (IdentityExtensionsDataHolder.class) {
                if (instance == null) {
                    instance = new IdentityExtensionsDataHolder();
                }
            }
        }
        return instance;
    }

    /**
     * To get the the instance of {@link ApplicationManagementService}.
     *
     * @return applicationManagementService
     */
    public ApplicationManagementService getApplicationManagementService() {

        return applicationManagementService;
    }

    /**
     * To set the RequestObjectService.
     *
     * @param requestObjectService instance of {@link RequestObjectService}
     */
    public void setRequestObjectService(RequestObjectService requestObjectService) {

        this.requestObjectService = requestObjectService;
    }

    public RequestObjectService getRequestObjectService() {

        return requestObjectService;
    }


    /**
     * To set the ApplicationManagementService.
     *
     * @param applicationManagementService instance of {@link ApplicationManagementService}
     */
    public void setApplicationManagementService(ApplicationManagementService applicationManagementService) {

        this.applicationManagementService = applicationManagementService;
    }

    /**
     * To get the the instance of {@link OAuthAdminServiceImpl}.
     *
     * @return oauthAdminService
     */
    public OAuthAdminServiceImpl getOauthAdminService() {

        return oAuthAdminService;
    }

    /**
     * To set the OauthAdminService.
     *
     * @param oauthAdminService instance of {@link OAuthAdminServiceImpl}
     */
    public void setOauthAdminService(OAuthAdminServiceImpl oauthAdminService) {

        this.oAuthAdminService = oauthAdminService;
    }

    public OpenBankingConfigurationService getOpenBankingConfigurationService() {

        return openBankingConfigurationService;
    }

    public void setConfigurationMap(Map<String, Object> confMap) {

        configurationMap = confMap;
    }

    public Map<String, Object> getConfigurationMap() {

        return configurationMap;
    }

    public void setOpenBankingConfigurationService(
            OpenBankingConfigurationService openBankingConfigurationService) {

        this.openBankingConfigurationService = openBankingConfigurationService;
        this.configurationMap = openBankingConfigurationService.getConfigurations();
        this.dcrRegistrationConfigMap = openBankingConfigurationService.getDCRRegistrationConfigurations();
        this.setTokenFilterValidators();
        TokenFilter.setValidators(getTokenFilterValidators());
        this.setDefaultTokenFilterImpl();
        TokenFilter.setDefaultTokenFilter(getDefaultTokenFilterImpl());
        RegistrationValidator dcrValidator =
                (RegistrationValidator) OpenBankingUtils.getClassInstanceFromFQN(openBankingConfigurationService
                        .getConfigurations().get(DCRCommonConstants.DCR_VALIDATOR).toString());
        this.setRegistrationValidator(dcrValidator);
        RegistrationValidator.setRegistrationValidator(dcrValidator);
        obRequestObjectValidator = (OBRequestObjectValidator)
                getClassInstanceFromFQN(IdentityExtensionsDataHolder.getInstance()
                        .getConfigurationMap().get(REQUEST_VALIDATOR).toString());
        PushAuthRequestValidator pushAuthRequestValidatorImpl = (PushAuthRequestValidator)
                getClassInstanceFromFQN(IdentityExtensionsDataHolder.getInstance()
                        .getConfigurationMap().get(PUSH_AUTH_REQUEST_VALIDATOR).toString());
        this.setPushAuthRequestValidator(pushAuthRequestValidatorImpl);
        PushAuthRequestValidator.setRegistrationValidator(pushAuthRequestValidatorImpl);
        obResponseTypeHandler = (OBResponseTypeHandler) getClassInstanceFromFQN(openBankingConfigurationService
                .getConfigurations().get(RESPONSE_HANDLER).toString());
        abstractApplicationUpdater = (AbstractApplicationUpdater) OpenBankingUtils.getClassInstanceFromFQN
                (openBankingConfigurationService.getConfigurations().get(DCRCommonConstants.POST_APPLICATION_LISTENER)
                        .toString());
        this.setClaimProvider((ClaimProvider) OpenBankingUtils.getClassInstanceFromFQN(openBankingConfigurationService
                .getConfigurations().get(IdentityCommonConstants.CLAIM_PROVIDER).toString()));
        OBClaimProvider.setClaimProvider(getClaimProvider());
        this.setIntrospectionDataProvider((IntrospectionDataProvider) OpenBankingUtils
                .getClassInstanceFromFQN(openBankingConfigurationService.getConfigurations()
                        .get(IdentityCommonConstants.INTROSPECTION_DATA_PROVIDER).toString()));
        OBIntrospectionDataProvider.setIntrospectionDataProvider(getIntrospectionDataProvider());
        setIdentityCacheAccessExpiry((String) openBankingConfigurationService
                .getConfigurations().get("Identity.Cache.IdentityCache.CacheAccessExpiry"));
        setIdentityCacheModifiedExpiry((String) openBankingConfigurationService
                .getConfigurations().get("Identity.Cache.IdentityCache.CacheModifiedExpiry"));
        setScopeRestrictedGrantTypes(extractScopeRestrictedGrantTypes());

        Map<String, String> authenticationWorkers = openBankingConfigurationService.getAuthenticationWorkers();
        authenticationWorkers.forEach((key, value) ->
                addWorker((OpenBankingAuthenticationWorker) OpenBankingUtils.getClassInstanceFromFQN(value), key));
    }

    public List<OBIdentityFilterValidator> getTokenFilterValidators() {

        return tokenValidators;

    }

    public void setTokenFilterValidators() {

        for (Object element : extractTokenFilterValidators()) {
            tokenValidators
                    .add((OBIdentityFilterValidator) OpenBankingUtils.getClassInstanceFromFQN(element.toString()));
        }
    }

    public List<String> getScopeRestrictedGrantTypes() {

        return scopeRestrictedGrantTypes;
    }

    public void setScopeRestrictedGrantTypes(List<String> scopeRestrictedGrantTypes) {

        this.scopeRestrictedGrantTypes = scopeRestrictedGrantTypes;
    }

    private List extractTokenFilterValidators() {

        Object validators = configurationMap.get(IdentityCommonConstants.TOKEN_VALIDATORS);

        if (validators != null) {
            if (validators instanceof List) {
                return (List) configurationMap.get(IdentityCommonConstants.TOKEN_VALIDATORS);
            } else {
                return Arrays.asList(validators);
            }
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Get grant types which the token scopes should be restricted based on scopes allowed for the application.
     *
     * @return List of grant types
     */
    public List<String> extractScopeRestrictedGrantTypes() {

        Object grantTypes = configurationMap.get(IdentityCommonConstants.APPLICATION_SCOPE_RESTRICTED_GRANT_TYPES);

        if (grantTypes instanceof List) {
            return (List<String>) grantTypes;
        }

        if (grantTypes instanceof String) {
            return Collections.singletonList((String) grantTypes);
        }

        return Collections.emptyList();
    }

    public DefaultTokenFilter getDefaultTokenFilterImpl() {

        return defaultTokenFilter;

    }

    public void setDefaultTokenFilterImpl() {

        defaultTokenFilter =
                (DefaultTokenFilter) OpenBankingUtils.getClassInstanceFromFQN(configurationMap
                        .get(IdentityCommonConstants.TOKEN_FILTER).toString());
    }

    public OBResponseTypeHandler getObResponseTypeHandler() {
        return obResponseTypeHandler;
    }

    public RegistrationValidator getRegistrationValidator() {

        return registrationValidator;
    }

    public void setRegistrationValidator(RegistrationValidator registrationValidator) {

        this.registrationValidator = registrationValidator;
    }

    public ClaimProvider getClaimProvider() {

        return claimProvider;
    }

    public void setClaimProvider(ClaimProvider claimProvider) {

        this.claimProvider = claimProvider;
    }

    public IntrospectionDataProvider getIntrospectionDataProvider() {

        return introspectionDataProvider;
    }

    public void setIntrospectionDataProvider(IntrospectionDataProvider introspectionDataProvider) {

        this.introspectionDataProvider = introspectionDataProvider;
    }

    public OBRequestObjectValidator getObRequestObjectValidator() {
        return obRequestObjectValidator;
    }

    public PushAuthRequestValidator getPushAuthRequestValidator() {

        return pushAuthRequestValidator;
    }

    public void setPushAuthRequestValidator(PushAuthRequestValidator pushAuthRequestValidator) {

        this.pushAuthRequestValidator = pushAuthRequestValidator;
    }

    public void setDcrRegistrationConfigMap(Map<String, Map<String, Object>> dcrRegConfigMap) {

        dcrRegistrationConfigMap = dcrRegConfigMap;
    }

    public Map<String, Map<String, Object>> getDcrRegistrationConfigMap() {

        return dcrRegistrationConfigMap;
    }

    public AbstractApplicationUpdater getAbstractApplicationUpdater() {

        return abstractApplicationUpdater;
    }

    public void setAbstractApplicationUpdater(AbstractApplicationUpdater abstractApplicationUpdater) {

        this.abstractApplicationUpdater = abstractApplicationUpdater;
    }


    public int getIdentityCacheAccessExpiry() {

        return identityCacheAccessExpiry;
    }

    public void setIdentityCacheAccessExpiry(String identityCacheAccessExpiry) {

        this.identityCacheAccessExpiry = identityCacheAccessExpiry == null ? 60 :
                Integer.parseInt(identityCacheAccessExpiry);
    }

    public int getIdentityCacheModifiedExpiry() {

        return identityCacheModifiedExpiry;
    }

    public void setIdentityCacheModifiedExpiry(String identityCacheModifiedExpiry) {

        this.identityCacheModifiedExpiry = identityCacheModifiedExpiry == null ? 60 :
                Integer.parseInt(identityCacheModifiedExpiry);
    }

    public KeyStore getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(KeyStore trustStore) {
        this.trustStore = trustStore;
    }

    public RealmService getRealmService() {

        if (realmService == null) {
            throw new RuntimeException("Realm Service is not available. Component did not start correctly.");
        }
        return realmService;
    }

    void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    /**
     * Return OBThrottleService.
     *
     * @return OBThrottleService
     */
    public OBThrottleService getOBThrottleService() {
        return obThrottleService;
    }

    /**
     * Set OBThrottleService.
     */
    public void setOBThrottleService(OBThrottleService obThrottleService) {
        this.obThrottleService = obThrottleService;
    }
    public ConsentCoreService getConsentCoreService() {

        return consentCoreService;
    }

    public void setConsentCoreService(ConsentCoreService consentCoreService) {

        this.consentCoreService = consentCoreService;
    }

    /**
     * Return OAuthClientAuthnService.
     *
     * @return OAuthClientAuthnService
     */
    public OAuthClientAuthnService getOAuthClientAuthnService() {
        return oAuthClientAuthnService;
    }

    /**
     * Set OAuthClientAuthnService.
     */
    public void setOAuthClientAuthnService(OAuthClientAuthnService oAuthClientAuthnService) {
        this.oAuthClientAuthnService = oAuthClientAuthnService;
        IdentityServiceExporter.setOAuthClientAuthnService(oAuthClientAuthnService);
    }

    /**
     * To get the instance of {@link OAuth2Service}.
     *
     * @return OAuth2Service
     */
    public OAuth2Service getOAuth2Service() {

        return oAuth2Service;
    }

    /**
     * To set the OAuth2Service.
     *
     * @param oAuth2Service instance of {@link OAuth2Service}
     */
    public void setOAuth2Service(OAuth2Service oAuth2Service) {

        this.oAuth2Service = oAuth2Service;
    }

    public void setJsFunctionRegistry(JsFunctionRegistry jsFunctionRegistry) {

        this.jsFunctionRegistry = jsFunctionRegistry;
    }

    public JsFunctionRegistry getJsFunctionRegistry() {

        return jsFunctionRegistry;
    }

    public Map<String, OpenBankingAuthenticationWorker> getWorkers() {
        return workers;
    }

    public void addWorker(OpenBankingAuthenticationWorker worker, String workerName) {
        this.workers.put(workerName, worker);
    }
}
