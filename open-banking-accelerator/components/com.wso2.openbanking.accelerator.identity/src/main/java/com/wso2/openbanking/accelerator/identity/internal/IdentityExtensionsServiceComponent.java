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
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.identity.auth.extensions.adaptive.function.OpenBankingAuthenticationWorkerFunction;
import com.wso2.openbanking.accelerator.identity.auth.extensions.adaptive.function.OpenBankingAuthenticationWorkerFunctionImpl;
import com.wso2.openbanking.accelerator.identity.authenticator.OBIdentifierAuthenticator;
import com.wso2.openbanking.accelerator.identity.claims.OBClaimProvider;
import com.wso2.openbanking.accelerator.identity.claims.RoleClaimProviderImpl;
import com.wso2.openbanking.accelerator.identity.clientauth.OBMutualTLSClientAuthenticator;
import com.wso2.openbanking.accelerator.identity.clientauth.jwt.OBPrivateKeyJWTClientAuthenticator;
import com.wso2.openbanking.accelerator.identity.interceptor.OBIntrospectionDataProvider;
import com.wso2.openbanking.accelerator.identity.keyidprovider.OBKeyIDProvider;
import com.wso2.openbanking.accelerator.identity.listener.TokenRevocationListener;
import com.wso2.openbanking.accelerator.identity.listener.application.OBApplicationManagementListener;
import com.wso2.openbanking.accelerator.throttler.service.OBThrottleService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationMgtListener;
import org.wso2.carbon.identity.oauth.OAuthAdminServiceImpl;
import org.wso2.carbon.identity.oauth.event.OAuthEventInterceptor;
import org.wso2.carbon.identity.oauth2.IntrospectionDataProvider;
import org.wso2.carbon.identity.oauth2.OAuth2Service;
import org.wso2.carbon.identity.oauth2.client.authentication.OAuthClientAuthenticator;
import org.wso2.carbon.identity.oauth2.client.authentication.OAuthClientAuthnService;
import org.wso2.carbon.identity.oauth2.keyidprovider.KeyIDProvider;
import org.wso2.carbon.identity.openidconnect.ClaimProvider;
import org.wso2.carbon.identity.openidconnect.RequestObjectService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Identity open banking common data holder.
 */
@Component(
        name = "com.wso2.openbanking.accelerator.identity.IdentityExtensionsServiceComponent",
        immediate = true
)
public class IdentityExtensionsServiceComponent {

    private static Log log = LogFactory.getLog(IdentityExtensionsServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        BundleContext bundleContext = context.getBundleContext();
        log.debug("Registering OB related Identity services.");
        bundleContext.registerService(ApplicationMgtListener.class, new OBApplicationManagementListener(), null);
        bundleContext.registerService(OAuthClientAuthenticator.class.getName(),
                new OBMutualTLSClientAuthenticator(), null);
        bundleContext.registerService(OAuthClientAuthenticator.class.getName(),
                new OBPrivateKeyJWTClientAuthenticator(), null);
        bundleContext.registerService(ApplicationManagementService.class, ApplicationManagementService.getInstance(),
                null);
        bundleContext.registerService(ClaimProvider.class.getName(), new OBClaimProvider(), null);
        bundleContext.registerService(IntrospectionDataProvider.class.getName(), new OBIntrospectionDataProvider(),
                null);
        bundleContext.registerService(KeyIDProvider.class.getName(), new OBKeyIDProvider(), null);
        bundleContext.registerService(ApplicationAuthenticator.class.getName(),
                new OBIdentifierAuthenticator(), null);
        bundleContext.registerService(ClaimProvider.class.getName(), new RoleClaimProviderImpl(), null);
        bundleContext.registerService(OAuthEventInterceptor.class, new TokenRevocationListener(), null);
        //Todo: Uncomment this after fixing the issue with the App2App authenticator
        // https://github.com/wso2/financial-services-accelerator/issues/323
//        App2AppAuthenticator app2AppAuthenticator = new App2AppAuthenticator();
//        bundleContext.registerService(ApplicationAuthenticator.class.getName(),
//                app2AppAuthenticator, null);

        if (IdentityExtensionsDataHolder.getInstance().getJsFunctionRegistry() != null) {
            JsFunctionRegistry jsFunctionRegistry = IdentityExtensionsDataHolder.getInstance().getJsFunctionRegistry();
            OpenBankingAuthenticationWorkerFunction worker = new OpenBankingAuthenticationWorkerFunctionImpl();
            jsFunctionRegistry.register(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, "OBAuthenticationWorker",
                    worker);
        }

    }

    @Reference(
            name = "ApplicationManagementService",
            service = ApplicationManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationManagementService"
    )
    protected void setApplicationManagementService(ApplicationManagementService mgtService) {

        IdentityExtensionsDataHolder.getInstance().setApplicationManagementService(mgtService);
    }

    protected void unsetApplicationManagementService(ApplicationManagementService mgtService) {

        IdentityExtensionsDataHolder.getInstance().setApplicationManagementService(null);
    }

    @Reference(
            name = "RequestObjectService",
            service = RequestObjectService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRequestObjectService"
    )
    protected void setRequestObjectService(RequestObjectService requestObjectService) {

        IdentityExtensionsDataHolder.getInstance().setRequestObjectService(requestObjectService);
    }

    protected void unsetRequestObjectService(RequestObjectService requestObjectService) {

        IdentityExtensionsDataHolder.getInstance().setRequestObjectService(null);
    }

    @Reference(
            service = OAuthAdminServiceImpl.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOauthAdminService"
    )
    protected void setOauthAdminService(OAuthAdminServiceImpl oauthAdminService) {

        IdentityExtensionsDataHolder.getInstance().setOauthAdminService(oauthAdminService);
    }

    protected void unsetOauthAdminService(OAuthAdminServiceImpl oAuthAdminService) {

        IdentityExtensionsDataHolder.getInstance().setOauthAdminService(null);
    }

    @Reference(
            service = OpenBankingConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigService"
    )
    public void setConfigService(OpenBankingConfigurationService openBankingConfigurationService) {

        IdentityExtensionsDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);
    }

    public void unsetConfigService(OpenBankingConfigurationService openBankingConfigurationService) {

        IdentityExtensionsDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);
    }

    @Reference(
            name = "realm.service",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {

        log.debug("Setting the Realm Service");
        IdentityExtensionsDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        log.debug("UnSetting the Realm Service");
        IdentityExtensionsDataHolder.getInstance().setRealmService(null);
    }

    @Reference(name = "open.banking.throttle.service",
            service = OBThrottleService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOBThrottleService"
    )
    protected void setOBThrottleService(OBThrottleService throttleService) {

        log.debug("OBThrottleService bound to the ob-identifier-authenticator");
        IdentityExtensionsDataHolder.getInstance().setOBThrottleService(throttleService);
    }

    protected void unsetOBThrottleService(OBThrottleService throttleService) {

        log.debug("OBThrottleService unbound from the ob-identifier-authenticator");
        IdentityExtensionsDataHolder.getInstance().setOBThrottleService(null);
    }

    @Reference(
            service = com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConsentCoreService"
    )
    public void setConsentCoreService(ConsentCoreService consentCoreService) {

        log.debug("Setting the Consent Core Service");
        IdentityExtensionsDataHolder.getInstance().setConsentCoreService(consentCoreService);
    }

    public void unsetConsentCoreService(ConsentCoreService consentCoreService) {

        log.debug("UnSetting the Consent Core Service");
        IdentityExtensionsDataHolder.getInstance().setConsentCoreService(null);
    }

    @Reference(name = "oauth.client.authn.service",
            service = OAuthClientAuthnService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOAuthClientAuthnService"
    )
    protected void setOAuthClientAuthnService(OAuthClientAuthnService oAuthClientAuthnService) {
        IdentityExtensionsDataHolder.getInstance().setOAuthClientAuthnService(oAuthClientAuthnService);
    }

    protected void unsetOAuthClientAuthnService(OAuthClientAuthnService oAuthClientAuthnService) {
        IdentityExtensionsDataHolder.getInstance().setOAuthClientAuthnService(null);
    }

    @Reference(
            service = OAuth2Service.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOAuth2Service"
    )
    public void setOAuth2Service(OAuth2Service oAuth2Service) {
        log.debug("Setting the OAuth2 Service");
        IdentityExtensionsDataHolder.getInstance().setOAuth2Service(oAuth2Service);
    }

    public void unsetOAuth2Service(OAuth2Service oAuth2Service) {
        log.debug("UnSetting the OAuth2 Service");
        IdentityExtensionsDataHolder.getInstance().setOAuth2Service(null);
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        if (IdentityExtensionsDataHolder.getInstance().getJsFunctionRegistry() != null) {
            JsFunctionRegistry jsFunctionRegistry = IdentityExtensionsDataHolder.getInstance().getJsFunctionRegistry();
            jsFunctionRegistry.register(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, "OBAuthenticationWorker",
                    null);
        }
        log.debug("Open banking Key Manager Extensions component is deactivated");
    }

    @Reference(
            service = JsFunctionRegistry.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetJsFunctionRegistry"
    )
    public void setJsFunctionRegistry(JsFunctionRegistry jsFunctionRegistry) {

        IdentityExtensionsDataHolder.getInstance().setJsFunctionRegistry(jsFunctionRegistry);
    }

    public void unsetJsFunctionRegistry(JsFunctionRegistry jsFunctionRegistry) {

        IdentityExtensionsDataHolder.getInstance().setJsFunctionRegistry(null);
    }
}
