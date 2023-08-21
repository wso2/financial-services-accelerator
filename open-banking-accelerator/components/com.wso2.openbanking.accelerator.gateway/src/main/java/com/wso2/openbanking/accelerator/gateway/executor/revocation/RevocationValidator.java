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
package com.wso2.openbanking.accelerator.gateway.executor.revocation;


import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.gateway.executor.model.RevocationStatus;

import java.security.cert.X509Certificate;

/**
 * This interface needs to be implemented by any certificate revocation validator.
 */
public interface RevocationValidator {

    /**
     * Checks revocation status of the peer certificate.
     *
     * @param peerCert   peer certificate
     * @param issuerCert issuer certificate
     * @return revocation status
     * @throws CertificateValidationException when an error occurs while checking the revocation status
     */
    RevocationStatus checkRevocationStatus(X509Certificate peerCert, X509Certificate issuerCert)
            throws CertificateValidationException;

    /**
     * Get revocation validator retry count.
     *
     * @return validator retry count
     */
    int getRetryCount();
}
