/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.keymanager.util;

import org.wso2.carbon.apimgt.api.model.ConfigurationDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Constants for Key Manager tests.
 */
public class KeyManagerTestConstants {

    private static final Map<String, String> SP_CERTIFICATE_CONFIG = Map.of(
            "default", "",
            "values", "",
            "tooltip", "Application Certificate - Mandatory if private_key_jwt Token method is selected",
            "multiple", "false",
            "label", "Application Certificate",
            "priority", "2",
            "type", "input",
            "required", "false",
            "mask", "false"
    );

    private static final Map<String, String> REGULATORY_CONFIG = Map.of(
            "default", "",
            "values", "true,false",
            "tooltip", "Is this a Regulatory Application?",
            "multiple", "false",
            "label", "Regulatory Application",
            "priority", "1",
            "type", "select",
            "required", "true",
            "mask", "false"
    );

    public static final Map<String, Map<String, String>> KEY_MANAGER_CONFIGS = Map.of("sp_certificate",
            SP_CERTIFICATE_CONFIG, "regulatory", REGULATORY_CONFIG);

    public static final String SP_CERT = "-----BEGIN CERTIFICATE-----" +
            "MIIF0zCCBLugAwIBAgIEWccIBzANBgkqhkiG9w0BAQsFADBTMQsw" +
            "CQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxLjAsBgNVBAMTJU9wZW5CYW5raW5nIFByZS1Qcm9kdWN0aW9uIElzc3Vpbmc" +
            "gQ0EwHhcNMjQwNTI5MDMzMzU4WhcNMjUwNjI5MDQwMzU4WjBzMQswCQYDVQQGEwJHQjEaMBgGA1UEChMRV1NPMiAoVUspIExJTUlURU" +
            "QxKzApBgNVBGETIlBTREdCLU9CLVVua25vd24wMDE1ODAwMDAxSFFRclpBQVgxGzAZBgNVBAMTEjAwMTU4MDAwMDFIUVFyWkFBWDCCA" +
            "SIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMzQLqrJcerlPQxUlG2u2X1YDeAcLLb+agetjHKmoyF3kqn+qMg1ZWt+8iCaYB6u" +
            "F2Kd2DPF9Z6HZu1i157nH6lcVydEDfrML+LtXNYVd5UDTBwTDW+kPZs+Hkb4AX5Gjtw/B/XXqi1caGynZbGSlkF1fX+4O7fXuPI+n4e" +
            "91PXOcIXZN06NkMCjwggDchaj43vYxHujbos4TiwlBq3cfTetGPLCJV5ShRZfVMiyhUuvP1ViORGtzI01C52dngpZGVxxXEZXdPBVSi" +
            "KXcyjvnWBLxRP74sDAWhY3MFzUXAQzyl3aOVoQnVFeMcvTK/b9GIa7oWOAvjBOZuxvEKsRl0ECAwEAAaOCAo0wggKJMA4GA1UdDwEB/" +
            "wQEAwIGwDCBkQYIKwYBBQUHAQMEgYQwgYEwEwYGBACORgEGMAkGBwQAjkYBBgIwagYGBACBmCcCMGAwOTARBgcEAIGYJwECDAZQU1Bf" +
            "UEkwEQYHBACBmCcBAwwGUFNQX0FJMBEGBwQAgZgnAQQMBlBTUF9JQwwbRmluYW5jaWFsIENvbmR1Y3QgQXV0aG9yaXR5DAZHQi1GQ0E" +
            "wFQYDVR0lBA4wDAYKKwYBBAGCNwoDDDCB4AYDVR0gBIHYMIHVMIHSBgsrBgEEAah1gQYBZDCBwjAqBggrBgEFBQcCARYeaHR0cDovL2" +
            "9iLnRydXN0aXMuY29tL3BvbGljaWVzMIGTBggrBgEFBQcCAjCBhgyBg1VzZSBvZiB0aGlzIENlcnRpZmljYXRlIGNvbnN0aXR1dGVzI" +
            "GFjY2VwdGFuY2Ugb2YgdGhlIE9wZW5CYW5raW5nIFJvb3QgQ0EgQ2VydGlmaWNhdGlvbiBQb2xpY2llcyBhbmQgQ2VydGlmaWNhdGUg" +
            "UHJhY3RpY2UgU3RhdGVtZW50MG0GCCsGAQUFBwEBBGEwXzAmBggrBgEFBQcwAYYaaHR0cDovL29iLnRydXN0aXMuY29tL29jc3AwNQY" +
            "IKwYBBQUHMAKGKWh0dHA6Ly9vYi50cnVzdGlzLmNvbS9vYl9wcF9pc3N1aW5nY2EuY3J0MDoGA1UdHwQzMDEwL6AtoCuGKWh0dHA6Ly" +
            "9vYi50cnVzdGlzLmNvbS9vYl9wcF9pc3N1aW5nY2EuY3JsMB8GA1UdIwQYMBaAFFBzkcYhctN39P4AEgaBXHl5bj9QMB0GA1UdDgQWB" +
            "BTBTy2SFwZ0fgzOSSWWBJdEgGHy3TANBgkqhkiG9w0BAQsFAAOCAQEAHaXaR4IKqekWFCRQjhZTkpoyi2mkhJM15Xd0t+cxSekWENoY" +
            "BH7ox7M7Akfoa+oEr58n0DAa/+BlJiFZ2mYScdvLzV81cQU1+LPHhxagWZW2fGV91eBRqdb1j80anF9LN4LJoLDoAObzzturt8MsSzw" +
            "6T/iLneWKqeil85+L6M/LnDWJTY+dq8Co/qAqk239OWiduY4pnEJ/U7PCH8xz/gSPQQO7PgILAZjGrdo40S/PMzZmGmGBMwLubE8exU" +
            "8x/Wzf3uUnSfDiPuAsRcplfXbHUb+NXK6wEJINjFw69/VZD3c/La62eTNPobl/DfqXGjPMjORtQqOiBVnF+AIWWA==" +
            "-----END CERTIFICATE-----";

    public static final String JSON_ADDITIONAL_PROPERTIES = "{\n" +
            "   \"ext_application_token_lifetime\":\"N/A\",\n" +
            "   \"ext_user_token_lifetime\":\"3600\",\n" +
            "   \"ext_refresh_token_lifetime\":\"N/A\",\n" +
            "   \"ext_id_token_lifetime\":\"N/A\",\n" +
            "   \"ext_pkce_mandatory\":\"false\",\n" +
            "   \"ext_pkce_support_plain\":\"false\",\n" +
            "   \"ext_public_client\":\"false\",\n" +
            "   \"sp_certificate\": \"" + SP_CERT + "\",\n" +
            "   \"regulatory\":\"true\"\n" +
            "}";

    public static final String JSON_ADDITIONAL_PROPERTIES_WITH_NULL = "{\n" +
            "   \"ext_application_token_lifetime\":\"3600\",\n" +
            "   \"ext_user_token_lifetime\":null,\n" +
            "   \"ext_refresh_token_lifetime\":\"N/A\",\n" +
            "   \"ext_id_token_lifetime\":\"N/A\",\n" +
            "   \"ext_pkce_mandatory\":\"false\",\n" +
            "   \"ext_pkce_support_plain\":\"false\",\n" +
            "   \"ext_public_client\":\"false\",\n" +
            "   \"sp_certificate\": \"" + SP_CERT + "\",\n" +
            "   \"regulatory\":\"true\"\n" +
            "}";

    public static final String INVALID_JSON_ADDITIONAL_PROPERTIES = "[\"test\"]";

    public static final String JSON_ADDITIONAL_PROPERTIES_INVALID_PKCE_VALUE = "{\n" +
            "   \"ext_application_token_lifetime\":\"N/A\",\n" +
            "   \"ext_user_token_lifetime\":\"3600\",\n" +
            "   \"ext_refresh_token_lifetime\":\"N/A\",\n" +
            "   \"ext_id_token_lifetime\":\"N/A\",\n" +
            "   \"ext_pkce_mandatory\":\"test\",\n" +
            "   \"ext_pkce_support_plain\":\"false\",\n" +
            "   \"ext_public_client\":\"false\",\n" +
            "   \"sp_certificate\": \"" + SP_CERT + "\",\n" +
            "   \"regulatory\":\"true\"\n" +
            "}";

    public static final String JSON_ADDITIONAL_PROPERTIES_INVALID_LIFETIME = "{\n" +
            "   \"ext_application_token_lifetime\":\"N/A\",\n" +
            "   \"ext_user_token_lifetime\":3600,\n" +
            "   \"ext_refresh_token_lifetime\":\"N/A\",\n" +
            "   \"ext_id_token_lifetime\":\"N/A\",\n" +
            "   \"ext_pkce_mandatory\":\"false\",\n" +
            "   \"ext_pkce_support_plain\":\"false\",\n" +
            "   \"ext_public_client\":\"false\",\n" +
            "   \"sp_certificate\": \"" + SP_CERT + "\",\n" +
            "   \"regulatory\":\"true\"\n" +
            "}";

    public static List<ConfigurationDto> getConfigurationDtos() {
        return List.of(
                new ConfigurationDto("ext_application_token_lifetime", "Lifetime of the Application Token",
                        "input", "Type Lifetime of the Application Token in seconds", "N/A",
                        false, false, Collections.EMPTY_LIST, false),
                new ConfigurationDto("ext_user_token_lifetime", "Lifetime of the User Token",
                        "input", "Type Lifetime of the User Token in seconds", "N/A",
                        true, false, Collections.EMPTY_LIST, false),
                new ConfigurationDto("ext_refresh_token_lifetime", "Lifetime of the Refresh Token",
                        "input", "Type Lifetime of the Refresh Token in seconds ", "N/A",
                        false, false, Collections.EMPTY_LIST, false),
                new ConfigurationDto("regulatory", "Regulatory Application",
                        "select", "Is this a Regulatory Application?", "",
                        true, false, List.of("true", "false"), false),
                new ConfigurationDto("sp_certificate", "Application Certificate",
                        "input", "Application Certificate - Mandatory if private_key_jwt Token method " +
                        "is selected", "N/A",
                        false, false, Collections.EMPTY_LIST, false),
                new ConfigurationDto("ext_pkce_mandatory", "ext_pkce_mandatory",
                        "input", "ext_pkce_mandatory", "N/A",
                        false, false, Collections.EMPTY_LIST, false)
        );
    }

    public static final String SP_PROPERTY_ARRAY = "[ " +
            "   {\n" +
            "       \"displayName\" : \"CERTIFICATE\",\n" +
            "       \"name\" : \"CERTIFICATE\",\n" +
            "       \"value\" : \"35\"\n" +
            "   }, " +
            "   {\n" +
            "       \"displayName\" : \"Is B2B Self Service Application\",\n" +
            "       \"name\" : \"isB2BSelfServiceApp\",\n" +
            "       \"value\" : \"false\"\n" +
            "   }, " +
            "   {\n" +
            "       \"name\" : \"isThirdPartyApp\",\n" +
            "       \"value\" : \"true\"\n" +
            "   }, " +
            "   {\n" +
            "       \"displayName\" : \"regulatory\",\n" +
            "       \"name\" : \"regulatory\",\n" +
            "       \"value\" : \"true\"\n" +
            "   }, " +
            "   {\n" +
            "       \"name\" : \"sp_certificate\",\n" +
            "       \"value\" : \"" + SP_CERT + "\"\n" +
            "   } " +
            "]";

    public static final String APP_DATA_RESPONSE = "{\n" +
            "    \"totalResults\": 1,\n" +
            "    \"startIndex\": 1,\n" +
            "    \"count\": 1,\n" +
            "    \"applications\": [\n" +
            "        {\n" +
            "            \"id\": \"2542e9e9-99c7-489a-8168-f9b5ade35158\",\n" +
            "            \"name\": \"am_admin-AT-wso2.com_38d55643-9631-448f-977b-5dd63fad9ac8_PRODUCTION\",\n" +
            "            \"description\": \"Service Provider for application am_admin-AT-wso2.com_38d55643-9631-" +
            "                                   448f-977b-5dd63fad9ac8_PRODUCTION\",\n" +
            "            \"applicationVersion\": \"v2.0.0\",\n" +
            "            \"clientId\": \"rgyZpk8uMCBXqolzjWQyLnmPVd0a\",\n" +
            "            \"realm\": \"\",\n" +
            "            \"access\": \"READ\",\n" +
            "            \"self\": \"/api/server/v1/applications/2542e9e9-99c7-489a-8168-f9b5ade35158\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"links\": []\n" +
            "}";

    public static final String SP_APP_RETRIEVAL_RESPONSE = "{\n" +
            "  \"applicationVersion\" : \"v2.0.0\",\n" +
            "  \"isManagementApp\" : false,\n" +
            "  \"clientId\" : \"fXaH8u55houpCwJKuuuidoOfdKIa\",\n" +
            "  \"claimConfiguration\" : {\n" +
            "    \"dialect\" : \"LOCAL\",\n" +
            "    \"role\" : {\n" +
            "      \"claim\" : {\n" +
            "        \"uri\" : \"http://wso2.org/claims/role\"\n" +
            "      },\n" +
            "      \"includeUserDomain\" : true\n" +
            "    },\n" +
            "    \"requestedClaims\" : [ ],\n" +
            "    \"subject\" : {\n" +
            "      \"includeTenantDomain\" : true,\n" +
            "      \"useMappedLocalSubject\" : false,\n" +
            "      \"mappedLocalSubjectMandatory\" : false,\n" +
            "      \"claim\" : {\n" +
            "        \"uri\" : \"http://wso2.org/claims/userid\"\n" +
            "      },\n" +
            "      \"includeUserDomain\" : true\n" +
            "    },\n" +
            "    \"claimMappings\" : [ ]\n" +
            "  },\n" +
            "  \"access\" : \"WRITE\",\n" +
            "  \"associatedRoles\" : {\n" +
            "    \"allowedAudience\" : \"ORGANIZATION\"\n" +
            "  },\n" +
            "  \"advancedConfigurations\" : {\n" +
            "    \"enableAuthorization\" : false,\n" +
            "    \"fragment\" : false,\n" +
            "    \"enableAPIBasedAuthentication\" : false,\n" +
            "    \"returnAuthenticatedIdpList\" : false,\n" +
            "    \"saas\" : false,\n" +
            "    \"skipLogoutConsent\" : false,\n" +
            "    \"attestationMetaData\" : {\n" +
            "      \"enableClientAttestation\" : false,\n" +
            "      \"androidPackageName\" : \"\",\n" +
            "      \"appleAppId\" : \"\"\n" +
            "    },\n" +
            "    \"certificate\" : {\n" +
            "      \"type\" : \"PEM\",\n" +
            "      \"value\" : \"" + SP_CERT + "\"\n" +
            "    },\n" +
            "    \"useExternalConsentPage\" : false,\n" +
            "    \"discoverableByEndUsers\" : false,\n" +
            "    \"skipLoginConsent\" : false,\n" +
            "    \"additionalSpProperties\" : [ {\n" +
            "      \"displayName\" : \"CERTIFICATE\",\n" +
            "      \"name\" : \"CERTIFICATE\",\n" +
            "      \"value\" : \"37\"\n" +
            "    }, {\n" +
            "      \"name\" : \"DisplayName\",\n" +
            "      \"value\" : \"Test-app\"\n" +
            "    }, {\n" +
            "      \"displayName\" : \"Is B2B Self Service Application\",\n" +
            "      \"name\" : \"isB2BSelfServiceApp\",\n" +
            "      \"value\" : \"false\"\n" +
            "    }, {\n" +
            "      \"name\" : \"isThirdPartyApp\",\n" +
            "      \"value\" : \"true\"\n" +
            "    }, {\n" +
            "      \"displayName\" : \"regulatory\",\n" +
            "      \"name\" : \"regulatory\",\n" +
            "      \"value\" : \"true\"\n" +
            "    }, {\n" +
            "      \"name\" : \"sp_certificate\",\n" +
            "      \"value\" : \"" + SP_CERT + "\"\n" +
            "    } ]\n" +
            "  },\n" +
            "  \"description\" : \"Service Provider for application am_admin-AT-wso2.com_38d55643-" +
            "9631-448f-977b-5dd63fad9ac8_PRODUCTION\",\n" +
            "  \"provisioningConfigurations\" : {\n" +
            "    \"outboundProvisioningIdps\" : [ ]\n" +
            "  },\n" +
            "  \"templateId\" : \"\",\n" +
            "  \"issuer\" : \"\",\n" +
            "  \"applicationEnabled\" : true,\n" +
            "  \"inboundProtocols\" : [ {\n" +
            "    \"self\" : \"/api/server/v1/applications/6de9665e-0231-483c-91ae-f928ebfbb6ab/" +
            "inbound-protocols/oidc\",\n" +
            "    \"type\" : \"oauth2\"\n" +
            "  } ],\n" +
            "  \"name\" : \"am_admin-AT-wso2.com_38d55643-9631-448f-977b-5dd63fad9ac8_PRODUCTION\",\n" +
            "  \"realm\" : \"\",\n" +
            "  \"templateVersion\" : \"\",\n" +
            "  \"id\" : \"6de9665e-0231-483c-91ae-f928ebfbb6ab\",\n" +
            "  \"authenticationSequence\" : {\n" +
            "    \"attributeStepId\" : 1,\n" +
            "    \"requestPathAuthenticators\" : [ ],\n" +
            "    \"type\" : \"USER_DEFINED\",\n" +
            "    \"steps\" : [ {\n" +
            "      \"options\" : [ {\n" +
            "        \"idp\" : \"LOCAL\",\n" +
            "        \"authenticator\" : \"BasicAuthenticator\"\n" +
            "      } ],\n" +
            "      \"id\" : 1\n" +
            "    } ],\n" +
            "    \"script\" : \"var psuChannel = 'Online Banking';\\n\\nvar onLoginRequest = function(context) " +
            "{\\n    executeStep(1, {\\n        onSuccess: function (context) {\\n            " +
            "Log.info(\\\"Authentication Successful\\\");\\n           " +
            " context.selectedAcr = \\\"urn:mace:incommon:iap:silver\\\";\\n        },\\n        " +
            "onFail: function (context) {\\n            Log.info(\\\"Authentication Failed\\\");\\n       " +
            " }\\n    });\\n};\\n\",\n" +
            "    \"subjectStepId\" : 1\n" +
            "  }\n" +
            "}";

    public static Map<String, ConfigurationDto> getFSAdditionalProperties() {
        return Map.of(
                "regulatory", new ConfigurationDto("regulatory", "Regulatory Application",
                        "select", "Is this a Regulatory Application?", "",
                        true, false, List.of("true", "false"), false),
                "sp_certificate", new ConfigurationDto("sp_certificate", "Application Certificate",
                        "input", "Application Certificate - Mandatory if private_key_jwt Token method " +
                        "is selected", "N/A",
                        false, false, Collections.EMPTY_LIST, false)
        );
    }
}
