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

package com.wso2.openbanking.accelerator.common.identity;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Constants required for Server Identity Retriever.
 */
public class IdentityConstants {

    public static final String PRODUCTION = "PRODUCTION";
    public static final String SANDBOX = "SANDBOX";

    public static final String KEYSTORE_LOCATION_CONF_KEY = "Security.KeyStore.Location";
    public static final String KEYSTORE_PASS_CONF_KEY = "Security.KeyStore.Password";

    /**
     * CertificateType enum.
     */
    public enum CertificateType {
        TRANSPORT, SIGNING
    }

    /**
     * EnvironmentType enum.
     */
    public enum EnvironmentType {
        SANDBOX, PRODUCTION, DEFAULT
    }

    /**
     * Use values for JWKS key set retrieval.
     *
     * Default values defined by specification
     * @see <a href="https://tools.ietf.org/html/rfc7517#section-4.2">RFC7517 Key Use Values</a>
     */
    public static final Map<CertificateType, String[]> USE_TYPE_VALUE_MAP;

    static {
        Map<CertificateType, String[]> useMap = new EnumMap<>(CertificateType.class);

        useMap.put(CertificateType.SIGNING, new String[]{"sig"});
        useMap.put(CertificateType.TRANSPORT, new String[]{"enc", "tls"});

        USE_TYPE_VALUE_MAP = Collections.unmodifiableMap(useMap);
    }

    /**
     * Custom Configurations.
     * defines and loads custom configurations from xml.
     */
    public static final Optional<String> PRIMARY_SIGNING_CERT_ALIAS;
    public static final Optional<String> SANDBOX_SIGNING_CERT_ALIAS;
    public static final Optional<String> PRIMARY_SIGNING_CERT_KID;
    public static final Optional<String> SANDBOX_SIGNING_CERT_KID;

    static {

        PRIMARY_SIGNING_CERT_ALIAS = Optional
                .ofNullable(OpenBankingConfigParser.getInstance().getOBIdnRetrieverSigningCertificateAlias());
        SANDBOX_SIGNING_CERT_ALIAS = Optional
                .ofNullable(OpenBankingConfigParser.getInstance().getOBIdnRetrieverSandboxSigningCertificateAlias());
        PRIMARY_SIGNING_CERT_KID = Optional
                .ofNullable(OpenBankingConfigParser.getInstance().getOBIdnRetrieverSigningCertificateKid());
        SANDBOX_SIGNING_CERT_KID = Optional
                .ofNullable(OpenBankingConfigParser.getInstance().getOBIdnRetrieverSandboxCertificateKid());
    }
}
