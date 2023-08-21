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

package com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.common;

import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.error.CertValidationErrors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.qualified.QCStatement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Optional;

/**
 * PSD2QCStatement class.
 */
public class PSD2QCStatement {

    private static final ASN1ObjectIdentifier psd2QcStatementOid = new ASN1ObjectIdentifier("0.4.0.19495.2");
    private static final Log log = LogFactory.getLog(PSD2QCStatement.class);

    public static PSD2QCType getPsd2QCType(X509Certificate cert) throws CertificateValidationException {

        byte[] extensionValue = cert.getExtensionValue(Extension.qCStatements.getId());
        if (extensionValue == null) {
            log.debug("Extension that contains the QCStatement not found in the certificate");
            throw new CertificateValidationException(CertValidationErrors.EXTENSION_NOT_FOUND.toString());
        }

        QCStatement qcStatement = extractQCStatement(extensionValue);

        ASN1Encodable statementInfo = qcStatement.getStatementInfo();

        return PSD2QCType.getInstance(statementInfo);

    }

    private static QCStatement extractQCStatement(byte[] extensionValue) throws CertificateValidationException {

        ASN1Sequence qcStatements;
        try {
            try (ASN1InputStream derAsn1InputStream = new ASN1InputStream(new ByteArrayInputStream(extensionValue))) {
                DEROctetString oct = (DEROctetString) (derAsn1InputStream.readObject());
                try (ASN1InputStream asn1InputStream = new ASN1InputStream(oct.getOctets())) {
                    qcStatements = (ASN1Sequence) asn1InputStream.readObject();
                }
            }
        } catch (IOException e) {
            log.error("Error reading QCStatement ", e);
            throw new CertificateValidationException(CertValidationErrors.QCSTATEMENT_INVALID.toString());
        }

        if (qcStatements.size() <= 0) {
            log.error("QCStatements not found in the certificate");
            throw new CertificateValidationException(CertValidationErrors.QCSTATEMENTS_NOT_FOUND.toString());
        }

        ASN1Encodable object = qcStatements.getObjectAt(0);
        if (object.toASN1Primitive() instanceof ASN1ObjectIdentifier) {
            return getSingleQcStatement(qcStatements);
        }

        return extractPsd2QcStatement(qcStatements)
                .orElseThrow(() -> new CertificateValidationException(
                        CertValidationErrors.PSD2_QCSTATEMENT_NOT_FOUND.toString()));
    }

    private static QCStatement getSingleQcStatement(ASN1Sequence qcStatements) throws CertificateValidationException {

        QCStatement qcStatement = QCStatement.getInstance(qcStatements);
        if (!psd2QcStatementOid.getId().equals(qcStatement.getStatementId().getId())) {
            log.error("Invalid QC statement type in psd2 certificate. expected [" +
                    psd2QcStatementOid.getId().replaceAll("[\r\n]", "") + "] but found [" +
                    qcStatement.getStatementId().getId().replaceAll("[\r\n]", "") + "]");
            throw new CertificateValidationException(CertValidationErrors.PSD2_QCSTATEMENT_NOT_FOUND.toString());
        }

        return qcStatement;
    }

    private static Optional<QCStatement> extractPsd2QcStatement(ASN1Sequence qcStatements) {

        Iterator iterator = qcStatements.iterator();

        while (iterator.hasNext()) {
            QCStatement qcStatement = QCStatement.getInstance(iterator.next());
            if (qcStatement != null && qcStatement.getStatementId().getId().equals(psd2QcStatementOid.getId())) {
                return Optional.of(qcStatement);
            }
        }

        return Optional.empty();
    }
}
