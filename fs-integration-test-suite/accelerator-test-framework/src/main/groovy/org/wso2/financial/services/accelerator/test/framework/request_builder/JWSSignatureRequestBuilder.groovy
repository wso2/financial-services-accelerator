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

package org.wso2.financial.services.accelerator.test.framework.request_builder

import org.wso2.bfsi.test.framework.keystore.KeyStore
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService

import java.time.Instant

/**
 * JWSSignatureRequestBuilder is used to build JWS signature request headers.
 * It allows setting scopes and signing algorithms before generating the payload.
 */
class JWSSignatureRequestBuilder {

    private ConfigurationService acceleratorConfiguration
    private List<String> scopesList = null // Scopes can be set before generate payload
    private String signingAlgorithm
    static ConfigurationService configurationService = new ConfigurationService()

    static String getRequestHeader(String alg = configurationService.getCommonSigningAlgorithm(),
                                   String kid = configurationService.getAppKeyStoreSigningKid(),
                                   String iss = KeyStore.getApplicationCertificateSubjectDn(),
                                   String tan = "openbanking.org.uk",
                                   String iat = Instant.now().getEpochSecond().minus(2),
                                   String typ = "JOSE",
                                   String cty = "application/json") {

        def algorithm = alg
        def clientKeyId = kid
        def clientSubjectDN = iss
        def trustAnchorDns = tan
        def issuedAt = iat
        def type = typ
        def contentType = cty

        def REQUEST_HEADER

        REQUEST_HEADER = "{\n" +
                "\"alg\": \"${algorithm}\",\n" +
                "\"kid\": \"${clientKeyId}\",\n" +
                "\"typ\": \"${type}\",\n" +
                "\"cty\": \"${contentType}\",\n" +
                "\"http://openbanking.org.uk/iat\": ${issuedAt},\n" +
                "\"http://openbanking.org.uk/iss\": \"${clientSubjectDN}\", \n" +
                "\"http://openbanking.org.uk/tan\": \"${trustAnchorDns}\",\n" +
                "\"crit\": [ \"http://openbanking.org.uk/iat\",\n" +
                "\"http://openbanking.org.uk/iss\", \"http://openbanking.org.uk/tan\"] \n" +
                "}"

        return REQUEST_HEADER
    }
}
