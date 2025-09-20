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

package org.wso2.financial.services.apim.mediation.policies.mtls.enforcement.utils;

import org.apache.commons.codec.binary.Base64;
import org.wso2.financial.services.apim.mediation.policies.mtls.enforcement.constants.MTLSEnforcementConstants;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/**
 * Utility class for the MTLS Enforcement Policy.
 */
public class MTLSEnforcementUtils {

    /**
     * Convert the client certificate from the header to a Certificate object.
     *
     * @param certificate certificate string from the header
     * @param isClientCertificateEncoded indicates whether the certificate is encoded
     * @return the client certificate as a {@link Certificate} object
     * @throws UnsupportedEncodingException if the encoding is not supported
     * @throws CertificateException if there is an error in generating the certificate
     */
    public static Certificate parseCertificate(String certificate, boolean isClientCertificateEncoded)
            throws UnsupportedEncodingException, CertificateException {

        byte[] bytes;
        if (certificate != null) {
            if (!isClientCertificateEncoded) {
                // Remove invalid characters, restructure line separators, and reconstruct the certificate
                certificate = certificate
                        .replaceAll(MTLSEnforcementConstants.BEGIN_CERTIFICATE_STRING
                                .concat(System.lineSeparator()), "")
                        .replaceAll(MTLSEnforcementConstants.BEGIN_CERTIFICATE_STRING.concat("\n"), "")
                        .replaceAll(MTLSEnforcementConstants.BEGIN_CERTIFICATE_STRING, "")
                        .replaceAll(System.lineSeparator().concat(MTLSEnforcementConstants.END_CERTIFICATE_STRING), "")
                        .replaceAll("\n".concat(MTLSEnforcementConstants.END_CERTIFICATE_STRING), "")
                        .replaceAll(MTLSEnforcementConstants.END_CERTIFICATE_STRING, "")
                        .trim()
                        .replaceAll(" ", System.lineSeparator())
                        .trim();
                certificate = MTLSEnforcementConstants.BEGIN_CERTIFICATE_STRING
                        .concat(System.lineSeparator())
                        .concat(certificate)
                        .concat(System.lineSeparator())
                        .concat(MTLSEnforcementConstants.END_CERTIFICATE_STRING);
                bytes = certificate.getBytes(StandardCharsets.UTF_8);
            } else {
                certificate = URLDecoder.decode(certificate, "UTF-8");
                certificate = getX509certificateContent(certificate);
                bytes = Base64.decodeBase64(certificate);
            }

            InputStream inputStream = new ByteArrayInputStream(bytes);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return cf.generateCertificate(inputStream);
        }

        return null;
    }

    private static String getX509certificateContent(String certificate) {
        String content = certificate.replaceAll(MTLSEnforcementConstants.BEGIN_CERTIFICATE_STRING, "")
                .replaceAll(MTLSEnforcementConstants.END_CERTIFICATE_STRING, "");

        return content.trim();
    }

}
