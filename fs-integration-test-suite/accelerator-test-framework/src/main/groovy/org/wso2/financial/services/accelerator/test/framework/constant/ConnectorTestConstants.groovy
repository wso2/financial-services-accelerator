/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.test.framework.constant

import org.wso2.bfsi.test.framework.constant.Constants
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService

import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

/**
 * Common Constants of IAM scenarios.
 */
class ConnectorTestConstants extends Constants{

    static ConfigurationService configurationService = new ConfigurationService()

    static final String SIGNING_ALGORITHM = configurationService.getCommonSigningAlgorithm()
    public static final Instant DATE_TIME = Instant.now().plus(5, ChronoUnit.DAYS)

    //Content Types
    static final String CONTENT_TYPE_JSON = "application/json"
    static final String ACCESS_TOKEN_CONTENT_TYPE = "application/x-www-form-urlencoded"
    static final String CONTENT_TYPE_JWT = "application/jwt"
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data"

    //Request Header
    public static final String AUTHORIZATION_HEADER = "Authorization"
    public static final String X_FAPI_FINANCIAL_ID = "x-fapi-financial-id"
    public static final String X_FAPI_FINANCIAL_ID_VALUE = "open-bank"
    public static final String X_IDEMPOTENCY_KEY = "x-idempotency-key"
    public static final String X_JWS_SIGNATURE_CAPS = "x-jws-signature"
    public static final String X_FAPI_AUTH_DATE = "x-fapi-auth-date"
    public static final String X_FAPI_IP_ADDRESS = "x-fapi-customer-ip-address"
    public static final String X_CUSTOMER_USER_AGENT = "x-customer-user-agent"
    public static final String CUSTOM_CLIENT_CERT_HEADER = "SSL-CLIENT-CERT"
    public static final String X_WSO2_CLIENT_ID_KEY = "x-wso2-client-id"
    public static final String ACCOUNTS_TYPE = "accounts"
    public static final String PAYMENTS_TYPE = "payments"
    public static final String COF_TYPE = "cof"
    public static final String X_WSO2_MUTUAL_CERT = "x-wso2-mutual-auth-cert"
    public static final String CHARSET = "charset"
    public static final String CHARSET_TYPE = "UTF-8"
    public static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id"
    public static final String X_FAPI_FINANCIAL_ID_CAPS = "X-Fapi-Financial-Id"
    public static final String X_IDEMPOTENCY_KEY_CAPS = "X-Idempotency-Key"
    public static final String X_FAPI_INTERACTION_ID_CAPS = "X-Fapi-Interaction-Id"
    static final String X_FAPI_CUSTOMER_LAST_LOGGED_TIME_CAPS = "X-Fapi-Customer-Last-Logged-time"
    static final String X_FAPI_CUSTOMER_LAST_LOGGED_TIME_VALUE = "Sun, 10 Sep 2017 19:43:31 GMT"
    static final String X_FAPI_AUTH_DATE_VALUE = "Sun, 10 Sep 2017 19:43:31 GMT"

    static final String PKJWT_AUTH_METHOD = "private_key_jwt"
    static final String TLS_AUTH_METHOD = "tls_client_auth"
    static final String ALG_PS256 = "PS256"
    static final String ALG_RS256 = "RS256"
    static final String ALG_RS512 = "RS512"

    //Error Responses
    static final String ERROR = "error"
    static final String ERROR_DESCRIPTION = "error_description"
    static final String ERROR_ERRORS_CODE = "errors[0].code"
    static final String ERROR_ERRORS_MSG = "errors[0].message"
    static final String ERROR_ERRORS_DESCRIPTION = "errors[0].description"
    static final String ERROR_CODE = "errorCode"
    static final String MESSAGE = "message"
    static final String DESCRIPTION = "description"
    static final String ERROR_MESSAGE = "errorMessage"
    static final String HTTP_CODE = "httpCode"
    static final String INVALID_SSA = "invalid_software_statement"
    static final String INVALID_CLIENT_METADATA = "invalid_client_metadata"
    static final String MTLS_HEADER_NOT_FOUND = "MTLS header not found"
    static final String INVALID_TLS_CERTIFICATE = "Invalid transport certificate"
    static final String CLIENT_ID_NOT_RETRIEVED = "Client ID not retrieved"
    static final String SIGNATURE_ALG_VALIDATION_FAILED = "Signature algorithm validation failed"
    static final String INVALID_REQUEST = "invalid_request"
    static final String INVALID_GRANT = "invalid_grant"
    static final String INVALID_CLIENT = "invalid_client"
    static final String CERTIFICATE_NOT_FOUND = "Transport certificate not found in the request"
    static final String MISSING_CREDENTIALS = "Missing Credentials"
    static final String CLIENT_ID_NOT_FOUND = "Client id not found"
    static final String IS_VALID_FALSE= "false"

    //Consent Error Constants
    static final String ERROR_CODE_400 = "400"
    static final String ERROR_CODE_BAD_REQUEST = "Bad Request"
    static final String ERROR_CODE_FORBIDDEN = "Forbidden"
    static final String CONSENT_MGT_ERROR = "Consent Management Error"
    static final String CONSENT_ENFORCEMENT_ERROR = "Consent Enforcement Error"
    static final String MISSING_CREDENTIALS_ERROR = "Missing Credentials"
    static final String INVALID_CREDENTIALS_ERROR = "Invalid Credentials"

    //DCR Constants
    static final String REGISTRATION_URL = "/api/openbanking/dynamic-client-registration/register"
    static final String SOFTWARE_ID = configurationService.getAppDCRSoftwareId()
    static final String REDIRECT_URI = configurationService.getAppDCRRedirectUri()
    static final String ALTERNATE_REDIRECT_URI = configurationService.getAppDCRAlternateRedirectUri()
    static final String SOFTWARE_ID_AU = "cdr-register"
    static final String DCR_API_NAME = "CDR-DynamicClientRegistration"
    static final String SOFTWARE_ID_EXPIRED = "9b5usDpbNtmxDcTzs7GzKp"

    //Token Flow Constants
    static final String BEARER = "Bearer"
    static final String TOKEN_EXPIRY_TIME = "3600"
    static final String SP_NAME = "Mock_Software"
    static final String TOKEN_ENDPOINT_URL = "/oauth2/token"

    static final String IS_CARBON_URL = configurationService.getISServerUrl() + "/carbon"
    static final String DEVPORTAL_URL = configurationService.getApimServerUrl() + "/devportal"

    //Token Payload Constants
    static final String KID = "kid"
    static final String CNF = "cnf"
    static final String GRANT_TYPE = "grant_type"
    static final String CONSENT_ID = "consent_id"
    public static final String DATA_CONSENT_ID = "Data.ConsentId"
    public static final String DATA_STATUS = "Data.Status"

    //Auth Flow Constants
    static final String AUTH_RESPONSE_TYPE = "code id_token"
    static final String AUTH_URL = "/oauth2/authorize"
    static final String INVALID_REDIRECT_URI = "https://google.com"
    static final String INVALID_SCOPE_ERROR = "Invalid scope given in the request"
    static final String INVALID_RESPONSE_TYPE_ERROR = "Invalid response_type parameter value"
    static final String EMPTY_REDIRECT_URI_ERROR = "Redirect URI is not present in the authorization request."
    static final String MISMATCHED_REDIRECT_URI_ERROR = "Your application's callback URL does not match with the registered redirect URLs."
    static final String INVALID_REDIRECT_URI_ERROR = "invalid.redirect.uri"
    static final String PERMISSION_MISMATCH_ERROR = "Permission mismatch. Consent does not contain necessary permissions"
    static final String INVALID_AUTH_URL_SCOPE_ERROR = "Request with 'client_id' = '@@CLIENT_ID' " +
            "has 'response_type' for 'hybrid flow'; but 'openid' scope not found."
    static final String INVALID_REQUEST_OBJECT_ERROR = "invalid_request_object"
    static final String INVALID_SIG_ALGO_ERROR = "Request Object signature verification failed due to an invalid " +
            "signature algorithm."
    static final String INVALID_CERT_ERROR = "Error occurred while validating request object signature using" +
            " jwks endpoint"
    static final String INVALID_PARAM_ERROR = "Invalid parameters found in the Request Object."

    //Auth Flow request parameters
    static final String ENDPOINT_PARAMETER = "endpoint"
    static final String RESPONSE_TYPE_PARAMETER = "response_type"
    static final String CLIENT_ID_PARAMETER = "client_id"
    static final String REDIRECT_URI_PARAMETER = "redirect_uri"
    static final String STATE_PARAMETER = "state"
    static final String NONCE_PARAMETER = "nonce"
    static final String SCOPE_PARAMETER = "scope"
    static final String AUD_PARAMETER = "aud"
    public static final String B64_PARAMETER = "b64"
    public static final String CRIT_IAT_URL = "http://openbanking.org.uk/iat"
    public static final String CRIT_ISS_URL = "http://openbanking.org.uk/iss"
    public static final String CRIT_TAN_URL = "http://openbanking.org.uk/tan"
    public static final String ESSENTIAL = "essential"
    public static final String VALUES = "values"
    public static final String ACR_PARAMETER = "acr"
    public static final String ACR_CA_URL = "urn:openbanking:psd2:ca"
    public static final String ACR_SCA_URL = "urn:openbanking:psd2:sca"
    public static final String VALUE_PARAMETER = "value"
    public static final String ID_TOKEN_PARAMETER = "id_token"
    public static final String INTENT_ID_PARAMETER = "openbanking_intent_id"
    public static final String USER_INFO_PARAMETER = "userinfo"
    public static final String CRIT_PARAMETER = "crit"
    public static final String CLAIMS_PARAMETER = "claims"

    public static final String accountID = "30080012343456"

    //Account Payload Constants
    static final String READ_ACCOUNTS_BASIC = "ReadAccountsBasic"
    static final String READ_ACCOUNTS_DETAIL = "ReadAccountsDetail"
    static final String READ_BALANCES = "ReadBalances"
    static final String READ_TRANSACTIONS_DETAIL = "ReadTransactionsDetail"
    static final String READ_TRANSACTIONS_BASIC = "ReadTransactionsBasic"
    static final String READ_TRANSACTIONS_CREDITS = "ReadTransactionsCredits"
    static final String READ_TRANSACTIONS_DEBITS = "ReadTransactionsDebits"
    static final String INVALID_PERMISSION = "ReadStatements"
    static OffsetDateTime expirationInstant = OffsetDateTime.now().plusDays(5)
    static OffsetDateTime fromInstant = OffsetDateTime.now()
    static OffsetDateTime toInstant = OffsetDateTime.now().plusDays(3)
    static OffsetDateTime firstPaymentDate = OffsetDateTime.now().plusDays(5)
    static OffsetDateTime finalPaymentDate = OffsetDateTime.now().plusDays(50)

    static final String AISP_PATH = "open-banking/v3.1/aisp/"
    static final String CONSENT_API = "/api/fs/consent/manage"
    static final String CONSENT_PATH = CONSENT_API + "/account-access-consents"
    static final String ACCOUNT_CONSENT_PATH = CONSENT_API + "/account-access-consents"
    static final String ACCOUNT_SUBMISSION_PATH = "/accounts/"
    static final String PAYMENT_CONSENT_PATH = CONSENT_API + "/domestic-payment-consents"
    static final String PAYMENT_SUBMISSION_PATH = "/payments"
    static final String COF_CONSENT_PATH = CONSENT_API + "/funds-confirmation-consents"
    static final String COF_SUBMISSION_PATH = "/funds-confirmations"
    static final String INCORRECT_CONSENT_PATH = AISP_PATH + "account-access-consent"
    static final String INCORRECT_ACCOUNT_VALIDATE_PATH = AISP_PATH + "account"
    static final String FILE_UPLOAD_POST = CONSENT_API + "/fileUpload"
    static final String ACCOUNT_VALIDATE_PATH = "/api/fs/consent/validate/validate"
    static final String PAYMENT_VALIDATE_PATH = "/api/fs/consent/validate/validate"
    static final String COF_VALIDATE_PATH = "/api/fs/consent/validate/validate"
    static final String ACCOUNTS_CONFIRMATION_PATH = CONSENT_API + "/account-confirmation"
    static final String ACCOUNTS_PATH = AISP_PATH + "accounts"
    static final String ACCOUNT_CONSENT_DELETE_PATH = "/api/fs/consent/admin/revoke"
    static final String IS_VALID = "isValid"
    static final String CONSENT_ID_INVALID_ERROR = "Consent ID invalid"
    static final String API_REQUEST_NOT_FOUND = "No matching resource found for given API Request"

    //Internal Rest API Url
    static final String SP_INTERNAL_ENDPOINT = "/t/carbon.super/api/server/v1/applications"
    static final String API_INTERNAL_ENDPOINT = "/api/am/devportal/v2/subscriptions"


    //File Path to Payment Resources
    static final FILE_RESOURCE_PATH = System.getProperty("user.dir") + "/src/test/resources/"

    public static final String AUTH_CODE = "authorization_code";
    public static final String PASSWORD = "password";
    public static final String REFRESH_TOKEN = "refresh_token"
    public static final String OAUTH_APP_INTERNAL_ENDPOINT = "/api/am/devportal/v2/applications"
    public static final int DEFAULT_DELAY = 5
    public static String APP_ACCESS_TKN = "APP_ACCESS_TKN"
    public static String USER_ACCESS_TKN = "USER_ACCESS_TKN"

    public static final int STATUS_CODE_200 = 200;
    public static final int STATUS_CODE_201 = 201;
    public static final int STATUS_CODE_204 = 204;
    public static final int STATUS_CODE_400 = 400;
    public static final int STATUS_CODE_401 = 401;
    public static final int STATUS_CODE_403 = 403;
    public static final int STATUS_CODE_500 = 500;
    public static final int STATUS_CODE_404 = 404;
    public static final int STATUS_CODE_405 = 405;
    public static final int STATUS_CODE_406 = 406;
    public static final int STATUS_CODE_415 = 415;

    /**
     * Enum class for keeping api scopes.
     */
    enum ApiScope {

        PAYMENTS("payments"),
        ACCOUNTS("accounts"),
        COF("fundsconfirmations"),
        INVALID_SCOPE("OB_1234"),
        DEFAULT(""),
        CDR_REGISTRATION("cdr:registration"),
        OPEN_ID("openid")

        private String value

        ApiScope(String value) {
            this.value = value
        }

        String getScopeString() {
            return this.value
        }
    }

    public static final String OBIE_ERROR_FIELD_INVALID ="UK.OBIE.Field.Invalid"
    static final String SCHEME_NAME_MAXLENGTH = "department test value 1212121212 department test value 1 department test value 1"
    static final String NAME_MAXLENGTH = "department test value 1212121212 department test value 1 department test value 1" +
            "department test value 1 department test value 1 department test value 1 department test value 1 department test value 1" +
            "department test value 1 department test value 1 department test value 1 department test value 1 department test value 1" +
            "department test value 1 department test value 1 department test value 1 department test value 1 department test value 1 " +
            "department test value 1 department test value 1 department test value 1 department test value 1 department test value 1"

    static final String IDENTIFICATION_MAXLENGTH = "Account1Account1Account1Account1Account1Account1Account1Account1" +
            "Account1Account1Account1Account1Account1Account1Account1Account1Account1Account1Account1Account1Account1" +
            "Account1Account1Account1Account1Account1Account1Account1Account1Account1Account1Account1Account1"
    static final String SECONDARY_IDENTIFICATION_MAXLENGTH = "Account1Account1Account1Account1Account1Account1Account1"

    final static String CONSENT_PATH_DOMESTIC = "/open-banking/v3.1/pisp/domestic-payment-consents"
    final static String CONSENT_PATH_SCHEDULE = "/open-banking/v3.1/pisp/domestic-scheduled-payment-consents"
    final static String CONSENT_PATH_STANDING_ORDERS = "/open-banking/v3.1/pisp/domestic-standing-order-consents"
    final static String CONSENT_PATH_INTERNATIONAL_PAYMENTS = "/open-banking/v3.1/pisp/international-payment-consents"
    final static String CONSENT_PATH_INTERNATIONAL_STANDING_ORDER = "/open-banking/v3.1/pisp/international-standing-order-consents"
    final static String CONSENT_PATH_INTERNATIONAL_SCHEDULE = "/open-banking/v3.1/pisp/international-scheduled-payment-consents"
    final static String CONSENT_PATH_FILE_PAYMENTS = "/open-banking/v3.1/pisp/file-payment-consents"

    final static String SUBMISSION_PATH_DOMESTIC_PAYMENTS = "/open-banking/v3.1/pisp/domestic-payments"
    final static String SUBMISSION_PATH_DOMESTIC_SCHEDULE = "/open-banking/v3.1/pisp/domestic-scheduled-payments"
    final static String SUBMISSION_PATH_DOMESTIC_STANDING_ORDERS = "/open-banking/v3.1/pisp/domestic-standing-orders"
    final static String SUBMISSION_PATH_INTERNATIONAL = "/open-banking/v3.1/pisp/international-payments"
    final static String SUBMISSION_PATH_INTERNATIONAL_STANDING_ORDERS = "/open-banking/v3.1/pisp/international-standing-orders"
    final static String SUBMISSION_PATH_INTERNATIONAL_SCHEDULE = "/open-banking/v3.1/pisp/international-scheduled-payments"
    final static String SUBMISSION_PATH_FILE_PAYMENTS = "/open-banking/v3.1/pisp/file-payments"
}
