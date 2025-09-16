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

package com.wso2.openbanking.accelerator.common.constant;


/**
 * Class containing the constants for Open Banking Common module.
 */
public class OpenBankingConstants {

    public static final String OB_CONFIG_FILE = "open-banking.xml";
    public static final String CARBON_HOME = "carbon.home";

    public static final String OB_CONFIG_QNAME = "http://wso2.org/projects/carbon/open-banking.xml";
    public static final String GATEWAY_CONFIG_TAG = "Gateway";
    public static final String GATEWAY_EXECUTOR_CONFIG_TAG = "OpenBankingGatewayExecutors";
    public static final String EVENT_CONFIG_TAG = "Event";
    public static final String EVENT_EXECUTOR_CONFIG_TAG = "EventExecutors";
    public static final String EXECUTOR_CONFIG_TAG = "Executor";
    public static final String DCR_CONFIG_TAG = "DCR";
    public static final String DCR_REGISTRATION_CONFIG_TAG = "RegistrationRequestParams";
    public static final String DCR_REGISTRATION_PARAM_ALLOWED_VALUE_TAG = "AllowedValues";
    public static final String REGULATORY = "regulatory";
    public static final String DATA_PUBLISHING_CONFIG_TAG = "DataPublishing";
    public static final String THRIFT_CONFIG_TAG = "Thrift";
    public static final String STREAMS_CONFIG_TAG = "Streams";
    public static final String ATTRIBUTE_CONFIG_TAG = "Attribute";
    public static final String REQUIRED = "required";
    public static final String ATTRIBUTE_TYPE = "type";
    public static final String DEFAULT_MIDNIGHT_CRON = "0 0 0 * * ?";
    public static final String DEFAULT_STATUS_FOR_EXPIRED_CONSENTS = "Expired";
    public static final String DEFAULT_STATUS_FOR_REVOKED_CONSENTS = "Revoked";
    public static final String IS_CONSENT_REVOCATION_FLOW = "IS_CONSENT_REVOCATION_FLOW";

    public static final String SIGNATURE_ALGORITHMS = "SignatureValidation.AllowedAlgorithms.Algorithm";
    public static final String AUTH_SERVLET_EXTENSION = "Identity.AuthenticationWebApp.ServletExtension";
    public static final String COMMON_IDENTITY_CACHE_ACCESS_EXPIRY = "Common.Identity.Cache.CacheAccessExpiry";
    public static final String COMMON_IDENTITY_CACHE_MODIFY_EXPIRY = "Common.Identity.Cache.CacheModifiedExpiry";
    public static final String JWKS_ENDPOINT_NAME = "DCR.JWKSEndpointName";
    public static final String SP_METADATA_FILTER_EXTENSION =
            "Identity.ApplicationInformationEndpoint.SPMetadataFilterExtension";
    public static final String CIBA_SERVLET_EXTENSION = "Identity.CIBAAuthenticationEndpointWebApp.ServletExtension";
    public static final String DCR_JWKS_CONNECTION_TIMEOUT = "DCR.JWKS-Retriever.ConnectionTimeout";
    public static final String DCR_JWKS_READ_TIMEOUT = "DCR.JWKS-Retriever.ReadTimeout";
    public static final String DCR_USE_SOFTWAREID_AS_APPNAME = "DCR.UseSoftwareIdAsAppName";
    public static final String DCR_JWKS_NAME = "DCR.JWKSEndpointName";
    public static final String DCR_APPLICATION_NAME_KEY = "DCR.ApplicationName";
    public static final String OB_KM_NAME = "KeyManagerName";
    public static final String DCR_SOFTWARE_ENV_IDENTIFICATION_PROPERTY_NAME =
            "DCR.RegistrationRequestParams.SoftwareEnvironmentIdentification.PropertyName";
    public static final String DCR_SOFTWARE_ENV_IDENTIFICATION_VALUE_FOR_SANDBOX =
            "DCR.RegistrationRequestParams.SoftwareEnvironmentIdentification.PropertyValueForSandbox";
    public static final String DCR_SOFTWARE_ENV_IDENTIFICATION_VALUE_FOR_PRODUCTION =
            "DCR.RegistrationRequestParams.SoftwareEnvironmentIdentification.PropertyValueForProduction";
    public static final String DCR_SSA_MANDATORY_PARAMETERS =
            "DCR.RegistrationRequestParams.SoftwareStatement.MandatoryParameters.Parameter";
    public static final String APIM_APPCREATION = "DCR.APIMRESTEndPoints.AppCreation";
    public static final String APIM_KEYGENERATION = "DCR.APIMRESTEndPoints.KeyGeneration";
    public static final String APIM_GETAPIS = "DCR.APIMRESTEndPoints.RetrieveAPIS";
    public static final String APIM_SUBSCRIBEAPIS = "DCR.APIMRESTEndPoints.SubscribeAPIs";
    public static final String APIM_GETSUBSCRIPTIONS = "DCR.APIMRESTEndPoints.RetrieveSubscribedAPIs";
    public static final String REGULATORY_API_NAMES = "RegulatoryAPINames";
    public static final String API_NAME = "name";
    public static final String API_ROLE = "roles";
    public static final String API_ID = "id";
    public static final String API_LIST = "list";
    public static final String REGULATORY_API = "API";
    public static final String SOFTWARE_ROLES = "software_roles";
    public static final String SOFTWARE_STATEMENT = "software_statement";
    public static final String SOFTWARE_ID = "software_id";
    public static final String JWT_BODY = "body";
    public static final String SOFTWARE_ENVIRONMENT = "software_environment";
    public static final String TOKEN_ENDPOINT = "DCR.TokenEndpoint";
    public static final String STORE_HOSTNAME = "PublisherURL";

    public static final String JDBC_PERSISTENCE_CONFIG = "JDBCPersistenceManager.DataSource.Name";
    public static final String DB_CONNECTION_VERIFICATION_TIMEOUT =
            "JDBCPersistenceManager.ConnectionVerificationTimeout";
    public static final String JDBC_RETENTION_DATA_PERSISTENCE_CONFIG =
            "JDBCRetentionDataPersistenceManager.DataSource.Name";
    public static final String RETENTION_DATA_DB_CONNECTION_VERIFICATION_TIMEOUT =
            "JDBCRetentionDataPersistenceManager.ConnectionVerificationTimeout";

    public static final String TRUSTSTORE_CONF_TYPE_DEFAULT = "JKS";
    public static final String CLIENT_CERT_CACHE = "ClientCertCache";
    public static final String OB_CACHE_MANAGER = "OB_CERTIFICATE_CACHE";
    public static final String CERTIFICATE_REVOCATION_VALIDATION_RETRY_COUNT = "Gateway" +
            ".CertificateManagement.CertificateRevocationValidationRetryCount";
    public static final String CERTIFICATE_REVOCATION_VALIDATION_CONNECT_TIMEOUT = "Gateway" +
            ".CertificateManagement.CertificateRevocationValidationConnectTimeout";
    public static final String CERTIFICATE_REVOCATION_VALIDATION_CONNECTION_REQUEST_TIMEOUT = "Gateway" +
            ".CertificateManagement.CertificateRevocationValidationConnectionRequestTimeout";
    public static final String CERTIFICATE_REVOCATION_VALIDATION_SOCKET_TIMEOUT = "Gateway" +
            ".CertificateManagement.CertificateRevocationValidationSocketTimeout";
    public static final String CERTIFICATE_REVOCATION_VALIDATION_ENABLED = "Gateway" +
            ".CertificateManagement.CertificateRevocationValidationEnabled";
    public static final String CERTIFICATE_REVOCATION_VALIDATION_EXCLUDED_ISSUERS = "Gateway" +
            ".CertificateManagement.RevocationValidationExcludedIssuers.IssuerDN";
    public static final String TPP_VALIDATION_SERVICE_IMPL_CLASS = "Gateway" +
            ".TPPManagement.TPPValidation.ServiceImplClass";
    public static final String TPP_VALIDATION_ENABLED = "Gateway" +
            ".TPPManagement.TPPValidation.Enabled";
    public static final String PSD2_ROLE_VALIDATION_ENABLED = "Gateway" +
            ".TPPManagement.PSD2RoleValidation.Enabled";
    public static final String CERTIFICATE_REVOCATION_PROXY_ENABLED = "Gateway" +
            ".CertificateManagement.CertificateRevocationProxy.Enabled";
    public static final String CERTIFICATE_REVOCATION_PROXY_HOST = "Gateway" +
            ".CertificateManagement.CertificateRevocationProxy.ProxyHost";
    public static final String CERTIFICATE_REVOCATION_PROXY_PORT = "Gateway" +
            ".CertificateManagement.CertificateRevocationProxy.ProxyPort";
    public static final String TRANSPORT_CERT_ISSUER_VALIDATION_ENABLED = "Gateway" +
            ".CertificateManagement.TransportCertIssuerValidationEnabled";
    public static final String TRUSTSTORE_DYNAMIC_LOADING_INTERVAL = "Gateway" +
            ".CertificateManagement.TrustStoreDynamicLoadingInterval";
    public static final String CLIENT_CERTIFICATE_CACHE_EXPIRY = "Gateway" +
            ".CertificateManagement.ClientCertificateCacheExpiry";
    public static final String TPP_VALIDATION_CACHE_EXPIRY = "Gateway" +
            ".TPPManagement.TPPValidationCacheExpiry";
    public static final String TPP_VALIDATION_SERVICE_AISP_SCOPE_REGEX = "Gateway" +
            ".CertificateManagement.TPPValidationService.ScopeRegexPatterns.AISP";
    public static final String TPP_VALIDATION_SERVICE_PISP_SCOPE_REGEX = "Gateway" +
            ".CertificateManagement.TPPValidationService.ScopeRegexPatterns.PISP";
    public static final String TPP_VALIDATION_SERVICE_CBPII_SCOPE_REGEX = "Gateway" +
            ".CertificateManagement.TPPValidationService.ScopeRegexPatterns.CBPII";
    public static final String CLIENT_TRANSPORT_CERT_HEADER_NAME = "Gateway" +
            ".CertificateManagement.ClientTransportCertHeaderName";
    public static final String URL_ENCODE_CLIENT_TRANSPORT_CERT_HEADER_ENABLED = "Gateway" +
            ".CertificateManagement.UrlEncodeClientTransportCertHeaderEnabled";
    public static final int PAGINATION_LIMIT_DEFAULT = 25;
    public static final int PAGINATION_OFFSET_DEFAULT = 0;
    public static final String CONSENT_CONFIG_TAG = "Consent";
    public static final String AUTHORIZE_STEPS_CONFIG_TAG = "AuthorizeSteps";
    public static final String STEP_CONFIG_TAG = "Step";
    public static final String ALLOWED_SCOPES_CONFIG_TAG = "AllowedScopes";
    public static final String SCOPE_CONFIG_TAG = "Scope";
    public static final String REVOCATION_VALIDATORS_CONFIG_TAG = "RevocationValidators";
    public static final String REVOCATION_VALIDATOR_CONFIG_TAG = "RevocationValidator";
    public static final String TPP_MANAGEMENT_CONFIG_TAG = "TPPManagement";
    public static final String CONNECTION_POOL_MAX_CONNECTIONS = "HTTPConnectionPool.MaxConnections";
    public static final String CONNECTION_POOL_TIME_TO_LIVE = "HTTPConnectionPool.TimeToLive";
    public static final String CONNECTION_POOL_MAX_CONNECTIONS_PER_ROUTE = "HTTPConnectionPool.MaxConnectionsPerRoute";
    public static final String PUSH_AUTH_EXPIRY_TIME = "PushAuthorisation.ExpiryTime";
    public static final String PUSH_AUTH_REQUEST_URI_SUBSTRING = "PushAuthorisation.RequestUriSubString";
    public static final String PUSH_AUTH_MANDATE_OPENID_SCOPE_FOR_REGULATORY_APPS = "PushAuthorisation" +
            ".MandateOpenidScopeForRegulatoryApps";

    public static final String CONSENT_PERIODICAL_EXPIRATION_CRON = "Consent.PeriodicalExpiration.CronValue";
    public static final String STATUS_FOR_EXPIRED_CONSENT = "Consent.PeriodicalExpiration.ExpiredConsentStatusValue";
    public static final String IS_CONSENT_PERIODICAL_EXPIRATION_ENABLED = "Consent.PeriodicalExpiration.Enabled";
    public static final String IS_CONSENT_AMENDMENT_HISTORY_ENABLED = "Consent.AmendmentHistory.Enabled";
    public static final String ELIGIBLE_STATUSES_FOR_CONSENT_EXPIRY =
            "Consent.PeriodicalExpiration.EligibleStatuses";
    public static final String CONSENT_ID_CLAIM_NAME = "Identity.ConsentIDClaimName";

    public static final String EVENT_QUEUE_SIZE = "Event.QueueSize";
    public static final String EVENT_WORKER_THREAD_COUNT = "Event.WorkerThreadCount";
    public static final String EVENT_EXECUTOR = "Event.EventExecutor";

    // Data Retention Constants
    public static final String IS_CONSENT_DATA_RETENTION_ENABLED = "Consent.DataRetention.Enabled";
    public static final String IS_CONSENT_RETENTION_DATA_DB_SYNC_ENABLED = "Consent.DataRetention.DBSyncEnabled";
    public static final String CONSENT_RETENTION_DATA_DB_SYNC_CRON = "Consent.DataRetention.CronValue";

    // Service Activator Constants
    public static final String SERVICE_ACTIVATOR_TAG = "ServiceActivator";
    public static final String SA_SUBSCRIBERS_TAG = "Subscribers";
    public static final String SA_SUBSCRIBER_TAG = "Subscriber";

    //JWS handling related constants
    public static final String JWS_SIG_VALIDATION_ENABLE = "JwsSignatureConfiguration.SignatureValidation.Enable";
    public static final String JWS_SIG_VALIDATION_ALGO =
            "JwsSignatureConfiguration.SignatureValidation.AllowedAlgorithms";
    public static final String JWS_RESP_SIGNING_ENABLE = "JwsSignatureConfiguration.ResponseSigning.Enable";
    public static final String JWS_RESP_SIGNING_ALGO = "JwsSignatureConfiguration.ResponseSigning.AllowedAlgorithm";

    // Open Banking Identity Manager
    public static final String OB_IDN_RETRIEVER_SIG_ALIAS = "OBIdentityRetriever.Server.SigningCertificateAlias";
    public static final String OB_IDN_RETRIEVER_SANDBOX_SIG_ALIAS =
            "OBIdentityRetriever.Server.SandboxSigningCertificateAlias";
    public static final String OB_IDN_RETRIEVER_SIG_KID = "OBIdentityRetriever.Server.SigningCertificateKid";
    public static final String OB_IDN_RETRIEVER_SANDBOX_KID = "OBIdentityRetriever.Server.SandboxCertificateKid";
    public static final String JWKS_RETRIEVER_SIZE_LIMIT = "OBIdentityRetriever.JWKSRetriever.SizeLimit";
    public static final String JWKS_RETRIEVER_CONN_TIMEOUT = "OBIdentityRetriever.JWKSRetriever.ConnectionTimeout";
    public static final String JWKS_RETRIEVER_READ_TIMEOUT = "OBIdentityRetriever.JWKSRetriever.ReadTimeout";

    // Key Manager Additional Property Configs
    public static final String KEY_MANAGER_CONFIG_TAG = "KeyManager";
    public static final String KEY_MANAGER_ADDITIONAL_PROPERTIES_CONFIG_TAG = "KeyManagerAdditionalProperties";
    public static final String PROPERTY_CONFIG_TAG = "Property";
    public static final String OB_KEYMANAGER_EXTENSION_IMPL =
            "KeyManager.KeyManagerExtensionImpl";

    //OB Event Notifications Constants
    public static final String TOKEN_ISSUER = "OBEventNotifications.TokenIssuer";
    public static final String MAX_SETS_TO_RETURN = "OBEventNotifications.NumberOfSetsToReturn";
    public static final String SIGNING_ALIAS = "OBEventNotifications.SigningAlias";
    public static final String IS_SUB_CLAIM_INCLUDED = "OBEventNotifications.PollingResponseParams.IsSubClaimAvailable";
    public static final String IS_TXN_CLAIM_INCLUDED = "OBEventNotifications.PollingResponseParams.IsTxnClaimAvailable";
    public static final String IS_TOE_CLAIM_INCLUDED = "OBEventNotifications.PollingResponseParams.IsToeClaimAvailable";
    public static final String EVENT_CREATION_HANDLER = "OBEventNotifications.EventCreationHandler";
    public static final String EVENT_POLLING_HANDLER = "OBEventNotifications.EventPollingHandler";
    public static final String EVENT_SUBSCRIPTION_HANDLER = "OBEventNotifications.EventSubscriptionHandler";
    public static final String EVENT_NOTIFICATION_GENERATOR = "OBEventNotifications.NotificationGenerator";
    public static final String AUTHENTICATION_WORKER_LIST_TAG = "AuthenticationWorkers";
    public static final String AUTHENTICATION_WORKER_TAG = "AuthenticationWorker";

    // Dispute Resolution Implementation Constants
    public static final String IS_DISPUTE_RESOLUTION_ENABLED = "DataPublishing.DisputeResolution.Enabled";
    public static final String PUBLISH_NON_ERROR_DISPUTE_DATA = "DataPublishing" +
            ".DisputeResolution.PublishNonErrorDisputeResolutionData";
    public static final String MAX_REQUEST_BODY_LENGTH = "DataPublishing.DisputeResolution.MaxRequestBodyLength";
    public static final String MAX_RESPONSE_BODY_LENGTH = "DataPublishing.DisputeResolution.MaxResponseBodyLength";
    public static final String MAX_HEADER_LENGTH = "DataPublishing.DisputeResolution.MaxHeaderLength";
    public static final String DISPUTE_RESOLUTION_STREAM_NAME = "DisputeResolutionStream";
    public static final String DISPUTE_RESOLUTION_STREAM_VERSION = "1.0.0";
    public static final String REQUEST_BODY = "requestBody";
    public static final String HTTP_METHOD = "httpMethod";
    public static final String STATUS_CODE = "statusCode";
    public static final String RESPONSE_BODY = "responseBody";
    public static final String ELECTED_RESOURCE = "electedResource";
    public static final String HEADERS = "headers";
    public static final String TIMESTAMP = "timestamp";

    public static final String CUTOFF_DATE_ENABLED = "ConsentManagement.PaymentRestrictions.CutOffDateTime.Enabled";
    public static final String CUTOFF_DATE_POLICY = "ConsentManagement.PaymentRestrictions.CutOffDateTime" +
            ".CutOffDateTimePolicy";
    public static final String ZONE_ID = "ZoneId";
    public static final String DAILY_CUTOFF = "ConsentManagement.PaymentRestrictions.CutOffDateTime" +
            ".DailyCutOffTime";
    public static final String EXPECTED_EXECUTION_TIME = "ConsentManagement.PaymentRestrictions.CutOffDateTime" +
            ".ExpectedExecutionTime";
    public static final String EXPECTED_SETTLEMENT_TIME = "ConsentManagement.PaymentRestrictions.CutOffDateTime" +
            ".ExpectedSettlementTime";

    // Realtime Event Notification Constants
    public static final String REALTIME_EVENT_NOTIFICATION_ENABLED = "RealtimeEventNotification.Enable";
    public static final String PERIODIC_CRON_EXPRESSION = "RealtimeEventNotification.PeriodicCronExpression";
    public static final String TIMEOUT_IN_SECONDS = "RealtimeEventNotification.TimeoutInSeconds";
    public static final String MAX_RETRIES = "RealtimeEventNotification.MaxRetries";
    public static final String INITIAL_BACKOFF_TIME_IN_SECONDS
            = "RealtimeEventNotification.InitialBackoffTimeInSeconds";
    public static final String BACKOFF_FUNCTION = "RealtimeEventNotification.BackoffFunction";
    public static final String CIRCUIT_BREAKER_OPEN_TIMEOUT_IN_SECONDS
            = "RealtimeEventNotification.CircuitBreakerOpenTimeoutInSeconds";
    public static final String EVENT_NOTIFICATION_THREADPOOL_SIZE
            = "RealtimeEventNotification.EventNotificationThreadPoolSize";
    public static final String REALTIME_EVENT_NOTIFICATION_REQUEST_GENERATOR
            = "RealtimeEventNotification.RequestGenerator";
    public static final String CONTENT_TYPE_TAG = "Content-Type";
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String SP_API_PATH = "/stores/query";
    public static final String APP_NAME_CC = "appName";
    public static final String QUERY = "query";
    public static final String IS_PSU_FEDERATED = "PSUFederatedAuthentication.Enabled";
    public static final String PSU_FEDERATED_IDP_NAME = "PSUFederatedAuthentication.IDPName";
    public static final String IDEMPOTENCY_IS_ENABLED = "Consent.Idempotency.Enabled";
    public static final String IDEMPOTENCY_ALLOWED_TIME = "Consent.Idempotency.AllowedTimeDuration";
    public static final String DOT_SEPARATOR = ".";
    public static final String MANDATE_NBF_CLAIM = "Identity.RequestObject.MandateNBF";
    public static final String ENABLE_SETTING_AUTHENTICATORS_ON_APP_UPDATE =
            "SCA.AuthenticatorConfig.EnableSettingAuthenticatorsOnAppUpdate";

    // CIBA Constants
    public static final String CIBA_AUTHENTICATION_REDIRECT_ENDPOINT =
            "Identity.CIBA.AuthWebLink.AuthenticationRedirectEndpoint";
    public static final String CIBA_WEB_LINK_ALLOWED_PARAMETERS =
            "Identity.CIBA.AuthWebLink.AllowedAuthURLParams.Value";
    public static final String CIBA_WEBLINK_NOTIFICATION_PROVIDER = "Identity.CIBA.AuthWebLink.NotificationProvider";
    public static final String CIBA_WEBLINK_AUTHENTICATOR_EXTENSION =
            "Identity.CIBA.AuthWebLink.AuthenticatorExtension";
    public static final String AUTH_REQ_ID = "auth_req_id";
    public static final String CIBA_WEB_AUTH_LINK_PARAM = "ciba_web_auth_link";
    public static final String CIBA_AUTH_CODE_RESPONSE_TYPE = "cibaAuthCode";

    // CIBA SMS Constants
    public static final String CIBA_WEB_LINK_NOTIFICATION_SMS_SERVICE_URL =
            "Identity.CIBA.AuthWebLink.SMS.SMSUrl";
    public static final String IDENTITY_CONFIG_TAG = "Identity";
    public static final String CIBA_CONFIG_TAG = "CIBA";
    public static final String AUTH_WEB_LINK_CONFIG_TAG = "AuthWebLink";
    public static final String SMS_CONFIG_TAG = "SMS";
    public static final String HEADERS_CONFIG_TAG = "Headers";

    // Accelerator default consent statuses
    public static final String AUTHORISED_STATUS = "authorised";
    public static final String REJECTED_STATUS = "rejected";
    public static final String AWAITING_AUTHORISATION_STATUS = "awaitingAuthorisation";
    public static final String AWAITING_FURTHER_AUTHORISATION_STATUS = "awaitingFurtherAuthorisation";
    public static final String CREATED_AUTHORISATION_RESOURCE_STATE = "created";
    public static final String MULTI_AUTH_AUTHORISATION_TYPE = "multi-authorization";
    public static final String CARBON_SUPER_TENANT_DOMAIN = "@carbon.super";

}

