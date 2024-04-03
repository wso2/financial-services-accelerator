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

package com.wso2.openbanking.accelerator.common.util;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * Common utilities related to Certificates.
 */
public class CertificateUtils {

    private static final Log log = LogFactory.getLog(CertificateUtils.class);

    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";
    private static final String X509_CERT_INSTANCE_NAME = "X.509";

    /**
     * Parse the certificate content.
     *
     * @param content the content to be passed
     * @return the parsed certificate
     * @throws OpenBankingException  if an error occurs while parsing the certificate
     */
    public static X509Certificate parseCertificate(String content) throws OpenBankingException {

        try {
            if (StringUtils.isNotBlank(content)) {
                // removing illegal base64 characters before decoding
                content = removeIllegalBase64Characters(content);
                byte[] bytes = Base64.getDecoder().decode(content);

                return (java.security.cert.X509Certificate) CertificateFactory.getInstance(X509_CERT_INSTANCE_NAME)
                        .generateCertificate(new ByteArrayInputStream(bytes));
            }
            log.error("Certificate passed through the request is empty");
            return null;
        } catch (CertificateException | IllegalArgumentException e) {
            throw new OpenBankingException("Certificate passed through the request not valid", e);
        }
    }

    /**
     * Remove illegal base64 characters from input string.
     *
     * @param value certificate as a string
     * @return certificate without illegal base64 characters
     */
    private static String removeIllegalBase64Characters(String value) {
        if (value.contains(BEGIN_CERT)
                && value.contains(END_CERT)) {

            // extracting certificate content
            value = value.substring(value.indexOf(BEGIN_CERT)
                            + BEGIN_CERT.length(),
                    value.indexOf(END_CERT));
        }
        // remove spaces, \r, \\r, \n, \\n, ], [ characters from certificate string
        return value.replaceAll("\\\\r|\\\\n|\\r|\\n|\\[|]| ", StringUtils.EMPTY);
    }

    /**
     * Check whether the certificate is expired.
     *
     * @param peerCertificate the certificate to be checked
     * @return true if the certificate is expired
     */
    public static boolean isExpired(X509Certificate peerCertificate) {
        try {
            peerCertificate.checkValidity();
        } catch (CertificateException e) {
            log.error("Certificate with the serial number " +
                    peerCertificate.getSerialNumber() + " issued by the CA " +
                    peerCertificate.getIssuerDN().toString() + " is expired. Caused by, " + e.getMessage());
            return true;
        }
        return false;
    }

}
