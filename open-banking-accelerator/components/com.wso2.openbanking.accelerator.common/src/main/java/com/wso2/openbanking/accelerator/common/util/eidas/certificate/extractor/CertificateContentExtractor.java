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

package com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor;

import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.common.PSD2QCStatement;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.common.PSD2QCType;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.common.PSPRole;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.common.PSPRoles;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.error.CertValidationErrors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that extracts the PSD2 attributes from v3 extensions of X509 certificates.
 */
public class CertificateContentExtractor {

    private static final Log log = LogFactory.getLog(CertificateContentExtractor.class);

    private CertificateContentExtractor() {
    }

    public static CertificateContent extract(X509Certificate cert)
            throws CertificateValidationException {

        if (cert == null) {
            log.error("Error reading certificate ");
            throw new CertificateValidationException(
                    CertValidationErrors.CERTIFICATE_INVALID.toString());
        }

        CertificateContent tppCertData = new CertificateContent();

        tppCertData.setNotAfter(cert.getNotAfter());
        tppCertData.setNotBefore(cert.getNotBefore());

        PSD2QCType psd2QcType = PSD2QCStatement.getPsd2QCType(cert);
        PSPRoles pspRoles = psd2QcType.getPspRoles();
        List<PSPRole> rolesArray = pspRoles.getRoles();

        // Roles as defined in the certificate (PSP_AI, PSP_PI, etc)
        List<String> roles = new ArrayList<>();
        // Roles relative PSD2 role names (AISP, PISP, etc)
        List<String> psd2Roles = new ArrayList<>();

        for (PSPRole pspRole : rolesArray) {
            roles.add(pspRole.getPsd2RoleName());
            psd2Roles.add(pspRole.getPsd2RoleName());
        }
        tppCertData.setPspRoles(roles);
        tppCertData.setPsd2Roles(psd2Roles);

        tppCertData.setNcaName(psd2QcType.getnCAName().getString());
        tppCertData.setNcaId(psd2QcType.getnCAId().getString());

        try {
            X500Name x500name = new JcaX509CertificateHolder(cert).getSubject();

            tppCertData.setPspAuthorisationNumber(getNameValueFromX500Name(x500name, BCStyle.ORGANIZATION_IDENTIFIER));
            tppCertData.setName(getNameValueFromX500Name(x500name, BCStyle.CN));

            if (log.isDebugEnabled()) {
                log.debug("Extracted TPP eIDAS certificate data: " + "[ " + tppCertData.toString() + " ]");
            }

        } catch (CertificateEncodingException e) {
            log.error("Certificate read error. caused by, ", e);
            throw new CertificateValidationException(CertValidationErrors.CERTIFICATE_INVALID.toString(), e);

        }
        return tppCertData;

    }

    private static String getNameValueFromX500Name(X500Name x500Name, ASN1ObjectIdentifier asn1ObjectIdentifier) {

        if (ArrayUtils.contains(x500Name.getAttributeTypes(), asn1ObjectIdentifier)) {
            return IETFUtils.valueToString(x500Name.getRDNs(asn1ObjectIdentifier)[0].getFirst().getValue());
        } else {
            return "";
        }
    }

}
