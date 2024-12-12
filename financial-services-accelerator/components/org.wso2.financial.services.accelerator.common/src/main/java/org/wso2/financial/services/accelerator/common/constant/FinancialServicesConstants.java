/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.common.constant;

/**
 * Class containing the constants for Financial Services Common module.
 */
public class FinancialServicesConstants {

    public static final String FS_CONFIG_FILE = "financial-services.xml";
    public static final String CARBON_HOME = "carbon.home";
    public static final String FS_CONFIG_QNAME = "http://wso2.org/projects/carbon/financial-services.xml";
    public static final String GATEWAY_CONFIG_TAG = "Gateway";
    public static final String GATEWAY_EXECUTOR_CONFIG_TAG = "FinancialServicesGatewayExecutors";
    public static final String EXECUTOR_CONFIG_TAG = "Executor";
    public static final String COMMON_IDENTITY_CACHE_ACCESS_EXPIRY = "Common.Identity.Cache.CacheAccessExpiry";
    public static final String COMMON_IDENTITY_CACHE_MODIFY_EXPIRY = "Common.Identity.Cache.CacheModifiedExpiry";
    public static final String JWKS_CONNECTION_TIMEOUT = "JWKS-Retriever.ConnectionTimeout";
    public static final String JWKS_READ_TIMEOUT = "JWKS-Retriever.ReadTimeout";
    public static final String DCR_SOFTWARE_ENV_IDENTIFICATION_PROPERTY_NAME =
            "DCR.RegistrationRequestParams.SoftwareEnvironmentIdentification.PropertyName";
    public static final String DCR_SOFTWARE_ENV_IDENTIFICATION_VALUE_FOR_SANDBOX =
            "DCR.RegistrationRequestParams.SoftwareEnvironmentIdentification.PropertyValueForSandbox";
    public static final String JDBC_PERSISTENCE_CONFIG = "JDBCPersistenceManager.DataSource.Name";
    public static final String DB_CONNECTION_VERIFICATION_TIMEOUT =
            "JDBCPersistenceManager.ConnectionVerificationTimeout";
    public static final String CONSENT_CONFIG_TAG = "Consent";
    public static final String CONNECTION_POOL_MAX_CONNECTIONS = "HTTPConnectionPool.MaxConnections";
    public static final String CONNECTION_POOL_MAX_CONNECTIONS_PER_ROUTE = "HTTPConnectionPool.MaxConnectionsPerRoute";
    public static final String IS_PSU_FEDERATED = "PSUFederatedAuthentication.Enabled";
    public static final String PSU_FEDERATED_IDP_NAME = "PSUFederatedAuthentication.IDPName";

    public static final String IDEMPOTENCY_IS_ENABLED = "Consent.Idempotency.Enabled";
    public static final String IDEMPOTENCY_ALLOWED_TIME = "Consent.Idempotency.AllowedTimeDuration";
    public static final String MANAGE_HANDLER = "Consent.ManageHandler";
    public static final String AUTHORIZE_STEPS_CONFIG_TAG = "AuthorizeSteps";
    public static final String STEP_CONFIG_TAG = "Step";
    public static final String CONSENT_JWT_PAYLOAD_VALIDATION = "Consent.Validation.JWTPayloadValidation";
    public static final String SIGNATURE_ALIAS = "Consent.Validation.RequestSignatureAlias";
    public static final String CONSENT_VALIDATOR = "Consent.Validation.Validator";
    public static final String ADMIN_HANDLER = "Consent.AdminHandler";
    public static final String PRESERVE_CONSENT = "Consent.PreserveConsentLink";
    public static final String CONSENT_API_USERNAME = "Consent.ConsentAPICredentials.Username";
    public static final String CONSENT_API_PASSWORD = "Consent.ConsentAPICredentials.Password";
    public static final String MAX_INSTRUCTED_AMOUNT = "Consent.Payments.MaximumInstructedAmount";

    public static final String AUTH_SERVLET_EXTENSION = "Identity.AuthenticationWebApp.ServletExtension";
    public static final String REQUEST_VALIDATOR = "Identity.Extensions.RequestObjectValidator";
    public static final String RESPONSE_HANDLER = "Identity.Extensions.ResponseTypeHandler";
    public static final String CLAIM_PROVIDER = "Identity.Extensions.ClaimProvider";
    public static final String CONSENT_ID_CLAIM_NAME = "Identity.ConsentIDClaimName";
    public static final String REMOVE_USER_STORE_DOMAIN_FROM_SUBJECT =
            "Identity.TokenSubject.RemoveUserStoreDomainFromSubject";
    public static final String REMOVE_TENANT_DOMAIN_FROM_SUBJECT =
            "Identity.TokenSubject.RemoveTenantDomainFromSubject";
    public static final String KEYSTORE_LOCATION_TAG = "Security.InternalKeyStore.Location";
    public static final String KEYSTORE_PASSWORD_TAG = "Security.InternalKeyStore.Password";
    public static final String SIGNING_ALIAS_TAG = "Security.InternalKeyStore.KeyAlias";
    public static final String SIGNING_KEY_PASSWORD = "Security.InternalKeyStore.KeyPassword";

    public static final String PUBLISHER_HOSTNAME = "PublisherURL";
    public static final String REQUEST_ROUTER = "Gateway.RequestRouter";
    public static final String GATEWAY_CACHE_EXPIRY = "Gateway.Cache.GatewayCache.CacheAccessExpiry";
    public static final String GATEWAY_CACHE_MODIFIED_EXPIRY = "Gateway.Cache.GatewayCache.CacheModifiedExpiry";
    public static final String CONSENT_VALIDATION_ENDPOINT = "Gateway.ConsentValidationEndpoint";
    public static final String VALIDATE_JWT = "Gateway.DCR.RequestJWTValidation";
    public static final String JWKS_ENDPOINT_NAME = "Gateway.DCR.JWKSEndpointName";
    public static final String SSA_CLIENT_NAME = "Gateway.DCR.SSAClientName";
    public static final String DCR_USE_SOFTWAREID_AS_APPNAME = "Gateway.DCR.UseSoftwareIdAsAppName";

    //Event Notifications Constants
    public static final String EVENT_NOTIFICATION_GENERATOR =
            "EventNotifications.NotificationGeneration.NotificationGenerator";
    public static final String TOKEN_ISSUER = "EventNotifications.NotificationGeneration.TokenIssuer";
    public static final String MAX_SETS_TO_RETURN = "EventNotifications.NotificationGeneration.NumberOfSetsToReturn";
    public static final String SIGNING_ALIAS = "EventNotifications.SigningAlias";
    public static final String IS_SUB_CLAIM_INCLUDED = "EventNotifications.PollingResponseParams.IsSubClaimAvailable";
    public static final String IS_TXN_CLAIM_INCLUDED = "EventNotifications.PollingResponseParams.IsTxnClaimAvailable";
    public static final String IS_TOE_CLAIM_INCLUDED = "EventNotifications.PollingResponseParams.IsToeClaimAvailable";
    public static final String EVENT_CREATION_HANDLER = "EventNotifications.EventCreationHandler";
    public static final String EVENT_POLLING_HANDLER = "EventNotifications.EventPollingHandler";
    public static final String EVENT_SUBSCRIPTION_HANDLER = "EventNotifications.EventSubscriptionHandler";
    public static final String REALTIME_EVENT_NOTIFICATION_ENABLED = "EventNotifications.Realtime.Enable";
    public static final String PERIODIC_CRON_EXPRESSION = "EventNotifications.Realtime.PeriodicCronExpression";
    public static final String TIMEOUT_IN_SECONDS = "EventNotifications.Realtime.TimeoutInSeconds";
    public static final String MAX_RETRIES = "EventNotifications.Realtime.MaxRetries";
    public static final String INITIAL_BACKOFF_TIME_IN_SECONDS =
            "EventNotifications.Realtime.InitialBackoffTimeInSeconds";
    public static final String BACKOFF_FUNCTION = "EventNotifications.Realtime.BackoffFunction";
    public static final String CIRCUIT_BREAKER_OPEN_TIMEOUT_IN_SECONDS =
            "EventNotifications.Realtime.CircuitBreakerOpenTimeoutInSeconds";
    public static final String EVENT_NOTIFICATION_THREAD_POOL_SIZE =
            "EventNotifications.Realtime.EventNotificationThreadPoolSize";
    public static final String REALTIME_EVENT_NOTIFICATION_REQUEST_GENERATOR =
            "EventNotifications.Realtime.RequestGenerator";

    public static final String JWT_HEAD = "head";
    public static final String JWT_BODY = "body";
    public static final String NEW_LINE = "[\r\n]";
    public static final String DOT_SEPARATOR = ".";
    public static final String PRODUCTION = "PRODUCTION";
    public static final String SANDBOX = "SANDBOX";
}
