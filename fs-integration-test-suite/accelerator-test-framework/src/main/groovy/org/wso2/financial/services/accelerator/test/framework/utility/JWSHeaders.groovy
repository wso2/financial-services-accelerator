/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package org.wso2.financial.services.accelerator.test.framework.utility

import org.wso2.bfsi.test.framework.keystore.KeyStore
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService

import java.time.Instant

/**
 *
 * Headers for signing JWS signatures
 */
class JWSHeaders {

    static final ConfigurationService configuration = new ConfigurationService()
    static final def algorithm = configuration.getCommonSigningAlgorithm()
    static final def clientKeyId = configuration.getAppKeyStoreSigningKid()
    static final def clientSubjectDN = KeyStore.getApplicationCertificateSubjectDn()
    static final def trustAnchorDns = "openbanking.org.uk"

    static String getIat() {

        return Instant.now().getEpochSecond()
    }

    final static String jwsHeaderWithInvalidKid = """{
                "alg": "${algorithm}",
                "kid": "abc",
                "crit": [
                    "http://openbanking.org.uk/iat",
                    "http://openbanking.org.uk/tan",
                    "http://openbanking.org.uk/iss"
                ],
                "http://openbanking.org.uk/iat": ${getIat()},
                "http://openbanking.org.uk/tan": "${trustAnchorDns}",
                "http://openbanking.org.uk/iss": "${clientSubjectDN}"
            }""".stripIndent()

    final static String jwsHeaderWithInvalidIat = """{
                "alg": "${algorithm}",
                "kid": "${clientKeyId}",
                "crit": [
                    "http://openbanking.org.uk/iat",
                    "http://openbanking.org.uk/tan",
                    "http://openbanking.org.uk/iss"
                ],
                "http://openbanking.org.uk/iat": "abc",
                "http://openbanking.org.uk/tan": "${trustAnchorDns}",
                "http://openbanking.org.uk/iss": "${clientSubjectDN}"
            }""".stripIndent()

    final static String jwsHeaderWithInvalidTan = """{
                "alg": "${algorithm}",
                "kid": "${clientKeyId}",
                "crit": [
                    "http://openbanking.org.uk/iat",
                    "http://openbanking.org.uk/tan",
                    "http://openbanking.org.uk/iss"
                ],
                "http://openbanking.org.uk/iat": ${getIat()},
                "http://openbanking.org.uk/tan": "abc",
                "http://openbanking.org.uk/iss": "${clientSubjectDN}"
            }""".stripIndent()

    final static String jwsHeaderWithInvalidIss = """{
                "alg": "${algorithm}",
                "kid": "${clientKeyId}",
                "crit": [
                    "http://openbanking.org.uk/iat",
                    "http://openbanking.org.uk/tan",
                    "http://openbanking.org.uk/iss"
                ],
                "http://openbanking.org.uk/iat": ${getIat()},
                "http://openbanking.org.uk/tan": "${trustAnchorDns}",
                "http://openbanking.org.uk/iss": "abc"
            }""".stripIndent()

    final static String jwsHeaderWithUnsupportedClaims = """{
                "alg": "${algorithm}",
                "kid": "${clientKeyId}",
                "crit": [
                    "http://openbanking.org.uk/iat",
                    "http://openbanking.org.uk/tan",
                    "http://openbanking.org.uk/iss",
                    "http://openbanking.org.uk/invalid",
                    "abc"
                ],
                "http://openbanking.org.uk/iat": ${getIat()},
                "http://openbanking.org.uk/tan": "${trustAnchorDns}",
                "http://openbanking.org.uk/iss": "${clientSubjectDN}"
            }""".stripIndent()

    final static String jwsHeaderWithMissingIssInCrit = """{
                "alg": "${algorithm}",
                "kid": "${clientKeyId}",
                "crit": [
                    "http://openbanking.org.uk/iat",
                    "http://openbanking.org.uk/tan"
                ],
                "http://openbanking.org.uk/iat": ${getIat()},
                "http://openbanking.org.uk/tan": "${trustAnchorDns}",
                "http://openbanking.org.uk/iss": "${clientSubjectDN}"
            }""".stripIndent()

    final static String jwsHeaderWithMissingTanInCrit = """{
                "alg": "${algorithm}",
                "kid": "${clientKeyId}",
                "crit": [
                    "http://openbanking.org.uk/iat",
                    "http://openbanking.org.uk/iss"
                ],
                "http://openbanking.org.uk/iat": ${getIat()},
                "http://openbanking.org.uk/tan": "${trustAnchorDns}",
                "http://openbanking.org.uk/iss": "${clientSubjectDN}"
            }""".stripIndent()

    final static String jwsHeaderWithMissingIatInCrit = """{
                "alg": "${algorithm}",
                "kid": "${clientKeyId}",
                "crit": [
                    "http://openbanking.org.uk/tan",
                    "http://openbanking.org.uk/iss"
                ],
                "http://openbanking.org.uk/iat": ${getIat()},
                "http://openbanking.org.uk/tan": "${trustAnchorDns}",
                "http://openbanking.org.uk/iss": "${clientSubjectDN}"
            }""".stripIndent()

    final static String jwsHeaderWithMissingAlg = """{
                "kid": "${clientKeyId}",
                "crit": [
                    "http://openbanking.org.uk/iat",
                    "http://openbanking.org.uk/tan",
                    "http://openbanking.org.uk/iss"
                ],
                "http://openbanking.org.uk/iat": ${getIat()},
                "http://openbanking.org.uk/tan": "${trustAnchorDns}",
                "http://openbanking.org.uk/iss": "${clientSubjectDN}"
            }""".stripIndent()

    final static String jwsHeaderWithMissingKid = """{
                "alg": "${algorithm}",
                "crit": [
                    "http://openbanking.org.uk/iat",
                    "http://openbanking.org.uk/tan",
                    "http://openbanking.org.uk/iss"
                ],
                "http://openbanking.org.uk/iat": ${getIat()},
                "http://openbanking.org.uk/tan": "${trustAnchorDns}",
                "http://openbanking.org.uk/iss": "${clientSubjectDN}"
            }""".stripIndent()
}
