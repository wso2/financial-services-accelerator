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

    public static final String SP_CERT = "------BEGIN CERTIFICATE-----" +
            "MIIFLTCCBBWgAwIBAgIEWcdQAzANBgkqhkiG9w0BAQsFADBTMQswCQYDVQQGEwJH" +
            "QjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxLjAsBgNVBAMTJU9wZW5CYW5raW5nIFBy" +
            "ZS1Qcm9kdWN0aW9uIElzc3VpbmcgQ0EwHhcNMjUwNjIzMDYxNDU5WhcNMjYwNzIz" +
            "MDY0NDU5WjBhMQswCQYDVQQGEwJHQjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxGzAZ" +
            "BgNVBAsTEjAwMTU4MDAwMDFIUVFyWkFBWDEfMB0GA1UEAxMWb1E0S29hYXZwT3Vv" +
            "RTdydlFzWkVPVjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKf6ll7D" +
            "m33XV80dBexvXUCkoZlnNDIGl9aS08TW40NWDKnQqyjh9okMSxLxYad67A3p1zeR" +
            "WhezlEWbg7KJpFA/2K+ncPpS+w2y/Cy0UD875N4PbAyiuTL7ghWcDF8D7fnkhCH4" +
            "gtWd+LITRQwBsBkDQCnvlUoIQfqiULxLvw1g5eJCr3u/EID9/lq9P7CCwU+/gUgb" +
            "Hv+H74+gm2ISAx3zdBCzyaWTEp+v0Lio02Exs/+MAcDOBVnBbkt+hSchlgt1r+s0" +
            "XZGwS0c8YoBa75GHzhDQPhl8zlV1O2VTSDzwMVyUVEMHPesIeNA+G58b3EIuOhj/" +
            "1m/macR4U4U3WDsCAwEAAaOCAfkwggH1MA4GA1UdDwEB/wQEAwIGwDAVBgNVHSUE" +
            "DjAMBgorBgEEAYI3CgMMMIHgBgNVHSAEgdgwgdUwgdIGCysGAQQBqHWBBgFkMIHC" +
            "MCoGCCsGAQUFBwIBFh5odHRwOi8vb2IudHJ1c3Rpcy5jb20vcG9saWNpZXMwgZMG" +
            "CCsGAQUFBwICMIGGDIGDVXNlIG9mIHRoaXMgQ2VydGlmaWNhdGUgY29uc3RpdHV0" +
            "ZXMgYWNjZXB0YW5jZSBvZiB0aGUgT3BlbkJhbmtpbmcgUm9vdCBDQSBDZXJ0aWZp" +
            "Y2F0aW9uIFBvbGljaWVzIGFuZCBDZXJ0aWZpY2F0ZSBQcmFjdGljZSBTdGF0ZW1l" +
            "bnQwbQYIKwYBBQUHAQEEYTBfMCYGCCsGAQUFBzABhhpodHRwOi8vb2IudHJ1c3Rp" +
            "cy5jb20vb2NzcDA1BggrBgEFBQcwAoYpaHR0cDovL29iLnRydXN0aXMuY29tL29i" +
            "X3BwX2lzc3VpbmdjYS5jcnQwOgYDVR0fBDMwMTAvoC2gK4YpaHR0cDovL29iLnRy" +
            "dXN0aXMuY29tL29iX3BwX2lzc3VpbmdjYS5jcmwwHwYDVR0jBBgwFoAUUHORxiFy" +
            "03f0/gASBoFceXluP1AwHQYDVR0OBBYEFHRbhCo/PG1Iuu3Z7RZsoHvhhfU0MA0G" +
            "CSqGSIb3DQEBCwUAA4IBAQBVQyBgWk+ZRPXowINWtPwUnn4o6Njzq4Bg43mVZsqu" +
            "9QwD/FcwQOluv9WvmO38QK6a+zjkI1GoNPnBBkff9m1ItS3kXKfPb5DYwC+wMYHk" +
            "pLDw0qSy/ruurHcG7dtp50AwN0qiSDz+m2LgNo//Bf4GX1gSmK21P1pgJesAmduh" +
            "k1jXf5OOlWIEn7ARHiNytKa/G4kcnRN4cImwRMu3Nhr+9Me6E3Dy3V6e8gAjfYxI" +
            "88BuLUouDewz0PD5pxs1JrfolqnzYZ+bKGM4af/lhrpJ1qVoL7UdxpXIpzWRU2cv" +
            "KD7hrKocQpPsgrsj1FkUJHf0k0ZhYgcTw1VGxf1jCVpN" +
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
