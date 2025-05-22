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
    public static final String DEFAULT_STATUS_FOR_EXPIRED_CONSENTS = "Expired";
    public static final String DEFAULT_MIDNIGHT_CRON = "0 0 0 * * ?";

    public static final String GATEWAY_CONFIG_TAG = "Gateway";
    public static final String GATEWAY_EXECUTOR_CONFIG_TAG = "FinancialServicesGatewayExecutors";
    public static final String EXECUTOR_CONFIG_TAG = "Executor";
    public static final String COMMON_IDENTITY_CACHE_ACCESS_EXPIRY = "Common.Identity.Cache.CacheAccessExpiry";
    public static final String COMMON_IDENTITY_CACHE_MODIFY_EXPIRY = "Common.Identity.Cache.CacheModifiedExpiry";
    public static final String JWKS_CONNECTION_TIMEOUT = "JWKS-Retriever.ConnectionTimeout";
    public static final String JWKS_READ_TIMEOUT = "JWKS-Retriever.ReadTimeout";
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
    public static final String IDEMPOTENCY_HEADER_NAME = "Consent.Idempotency.HeaderName";
    public static final String IDEMPOTENCY_ALLOWED_FOR_ALL_APIS = "Consent.Idempotency.AllowedForAllAPIs";
    public static final String IDEMPOTENCY_ALLOWED_API_RESOURCES = "Consent.Idempotency.AllowedAPIResources" +
            ".AllowedAPIResource";
    public static final String MANAGE_HANDLER = "Consent.ManageHandler";
    public static final String MANAGE_VALIDATOR = "Consent.ManageValidator";
    public static final String AUTHORIZE_STEPS_CONFIG_TAG = "AuthorizeSteps";
    public static final String STEP_CONFIG_TAG = "Step";
    public static final String CONSENT_JWT_PAYLOAD_VALIDATION = "Consent.Validation.JWTPayloadValidation";
    public static final String SIGNATURE_ALIAS = "Consent.Validation.RequestSignatureAlias";
    public static final String CONSENT_VALIDATOR = "Consent.Validation.Validator";
    public static final String ADMIN_HANDLER = "Consent.AdminHandler";
    public static final String PRESERVE_CONSENT = "Consent.PreserveConsentLink";
    public static final String ADMIN_USERNAME = "Admin.Username";
    public static final String ADMIN_PASSWORD = "Admin.Password";
    public static final String MAX_INSTRUCTED_AMOUNT = "Consent.Payments.MaximumInstructedAmount";
    public static final String STATUS_FOR_EXPIRED_CONSENT = "Consent.PeriodicalExpiration.ExpiredConsentStatusValue";
    public static final String ELIGIBLE_STATUSES_FOR_CONSENT_EXPIRY =
            "Consent.PeriodicalExpiration.EligibleStatuses";
    public static final String IS_CONSENT_AMENDMENT_HISTORY_ENABLED = "Consent.AmendmentHistory.Enabled";
    public static final String CONSENT_PERIODICAL_EXPIRATION_CRON = "Consent.PeriodicalExpiration.CronValue";
    public static final String IS_CONSENT_PERIODICAL_EXPIRATION_ENABLED = "Consent.PeriodicalExpiration.Enabled";

    public static final String AUTH_SERVLET_EXTENSION = "Identity.AuthenticationWebApp.ServletExtension";
    public static final String REQUEST_VALIDATOR = "Identity.Extensions.RequestObjectValidator";
    public static final String RESPONSE_HANDLER = "Identity.Extensions.ResponseTypeHandler";
    public static final String GRANT_HANDLER = "Identity.Extensions.GrantHandler";
    public static final String CLAIM_PROVIDER = "Identity.Extensions.ClaimProvider";
    public static final String INTROSPECTION_DATA_PROVIDER = "Identity.Extensions.IntrospectionDataProvider";
    public static final String CONSENT_ID_CLAIM_NAME = "Identity.ConsentIDClaimName";
    public static final String APPEND_CONSENT_ID_TO_TOKEN_ID_TOKEN = "Identity.AppendConsentIdToTokenIdToken";
    public static final String APPEND_CONSENT_ID_TO_AUTHZ_ID_TOKEN = "Identity.AppendConsentIdToAuthzIdToken";
    public static final String APPEND_CONSENT_ID_TO_ACCESS_TOKEN = "Identity.AppendConsentIdToAccessToken";
    public static final String APPEND_CONSENT_ID_TO_TOKEN_INTROSPECT_RESPONSE = "Identity" +
            ".AppendConsentIdToTokenIntrospectResponse";
    public static final String REMOVE_USER_STORE_DOMAIN_FROM_SUBJECT =
            "Identity.TokenSubject.RemoveUserStoreDomainFromSubject";
    public static final String REMOVE_TENANT_DOMAIN_FROM_SUBJECT =
            "Identity.TokenSubject.RemoveTenantDomainFromSubject";
    public static final String APP_REGISTRATION_TAG = "AppRegistration";
    public static final String DCR_TAG = "DCR";
    public static final String DCR_PARAMS_TAG = "Params";
    public static final String DCR_VALIDATORS_TAG = "Validators";
    public static final String DCR_PARAM_ALLOWED_VALUE_TAG = "AllowedValues";
    public static final String DCR_PARAM_NAME_TAG = "Name";
    public static final String POST_APPLICATION_LISTENER = "AppRegistration.ApplicationUpdaterImpl";
    public static final String DCR_SERVICE_EXTENSION = "AppRegistration.DCR.DCRServiceExtension";
    public static final String PRIMARY_AUTHENTICATOR_DISPLAY_NAME = "AppRegistration.SCA.PrimaryAuthenticator" +
            ".DisplayName";
    public static final String PRIMARY_AUTHENTICATOR_NAME = "AppRegistration.SCA.PrimaryAuthenticator.Name";
    public static final String IDENTITY_PROVIDER_NAME = "AppRegistration.SCA.IdpName";
    public static final String IDENTITY_PROVIDER_STEP = "AppRegistration.SCA.IdpStep";
    public static final String CONDITIONAL_AUTH_SCRIPT_NAME = "AppRegistration.ConditionalAuthScriptFileName";
    public static final String JTI_CACHE_ACCESS_EXPIRY = "AppRegistration.DCR.JTICache.CacheAccessExpiry";
    public static final String JTI_CACHE_MODIFY_EXPIRY = "AppRegistration.DCR.JTICache.CacheModifiedExpiry";
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
    public static final String DCR_RESPONSE_PARAMETERS = "Gateway.DCR.ResponseParams.Param";

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
    public static final String REQUIRE_SUBSCRIPTION_TO_POLL =
            "EventNotifications.EventSubscription.RequireSubscriptionToPoll";
    public static final String ALLOW_MULTIPLE_SUBSCRIPTION =
            "EventNotifications.EventSubscription.AllowMultipleSubscription";
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

    // Service Extensions Constants
    public static final String SERVICE_EXTENSIONS_ENDPOINT_ENABLED = "ExtensionsEndpoint.Enable";
    public static final String SERVICE_EXTENSIONS_ENDPOINT_BASE_URL = "ExtensionsEndpoint.BaseURL";
    public static final String CONSENT_MANAGE_EXTENSION_ALLOWED_HEADERS =
            "Consent.ManageExtension.AllowedHeaders.AllowedHeader";
    public static final String SERVICE_EXTENSIONS_ENDPOINT_RETRY_COUNT = "ExtensionsEndpoint.RetryCount";
    public static final String SERVICE_EXTENSIONS_ENDPOINT_CONNECT_TIMEOUT = "ExtensionsEndpoint.ConnectTimeout";
    public static final String SERVICE_EXTENSIONS_ENDPOINT_READ_TIMEOUT = "ExtensionsEndpoint.ReadTimeout";
    public static final String SERVICE_EXTENSIONS_EXTENSION = "ExtensionsEndpoint.AllowedExtensions.AllowedExtension";
    public static final String SERVICE_EXTENSIONS_SECURITY_TYPE = "ExtensionsEndpoint.Security.Type";
    public static final String SERVICE_EXTENSIONS_BASIC_AUTH_USERNAME = "ExtensionsEndpoint.Security.Username";
    public static final String SERVICE_EXTENSIONS_BASIC_AUTH_PASSWORD = "ExtensionsEndpoint.Security.Password";
    public static final String SERVICE_EXTENSIONS_OAUTH2_TOKEN = "ExtensionsEndpoint.Security.Token";
    public static final String CONSENT_TYPE = "consentType";
    public static final String ERROR_DESCRIPTION = "errorDescription";
    public static final String DEFAULT_ERROR_DESCRIPTION = "Unexpected error occurred";
    public static final String ERROR_CODE = "errorCode";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String DEFAULT_ERROR_MESSAGE = "server_error";
    public static final String RESPONSE_DATA = "responseData";;
    public static final String BASIC_AUTH = "Basic-Auth";
    public static final String OAUTH2 = "OAuth2";

    // Consent ID extraction Constants
    public static final String IS_PRE_INITIATED_CONSENT = "Consent.PreInitiatedConsent";
    public static final String AUTH_FLOW_CONSENT_ID_SOURCE = "Consent.AuthFlowConsentIdSource";
    public static final String CONSENT_ID_EXTRACTION_JSON_PATH = "Consent.ConsentIdExtraction.JsonPath";
    public static final String CONSENT_ID_EXTRACTION_KEY = "Consent.ConsentIdExtraction.Key";
    public static final String CONSENT_ID_EXTRACTION_REGEX_PATTERN = "Consent.ConsentIdExtraction.RegexPattern";

    // Key Manager Additional Property Configs
    public static final String KEY_MANAGER_CONFIG_TAG = "KeyManager";
    public static final String KEY_MANAGER_ADDITIONAL_PROPERTIES_CONFIG_TAG = "KeyManagerAdditionalProperties";
    public static final String PROPERTY_CONFIG_TAG = "Property";
    public static final String KEY_MANAGER_EXTENSION_IMPL = "KeyManager.KeyManagerExtensionImpl";

    public static final String JWT_HEAD = "head";
    public static final String JWT_BODY = "body";
    public static final String NEW_LINE = "[\r\n]";
    public static final String DOT_SEPARATOR = ".";
    public static final String PRODUCTION = "PRODUCTION";
    public static final String SANDBOX = "SANDBOX";
    public static final String TENANT_DOMAIN = "carbon.super";
    public static final String UUID_REGEX =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    public static final String CONTENT_TYPE_TAG = "Content-Type";
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String URL_ENCODED_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String ACCEPT = "Accept";
    public static final String COLON = ":";
    public static final String SPACE_SEPARATOR = " ";
    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_TAG = "Bearer ";
    public static final String BASIC_TAG = "Basic ";
    public static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final String SCOPE = "scope";
    public static final String REQUEST_OBJECT = "requestObject";
    public static final String REQUEST_PARAM = "requestParam";
}
