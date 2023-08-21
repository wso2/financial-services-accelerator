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

package com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.error;

/**
 * CertValidationErrors class.
 */
public enum CertValidationErrors {
    CERTIFICATE_INVALID("Content of the certificate is invalid."),
    EXTENSION_NOT_FOUND("X509 V3 Extensions not found in the certificate."),
    QCSTATEMENT_INVALID("Invalid QCStatement in the certificate."),
    QCSTATEMENTS_NOT_FOUND("QCStatements not found in the certificate."),
    PSD2_QCSTATEMENT_NOT_FOUND("No PSD2 QCStatement found in the certificate.");

    private String description;

    CertValidationErrors(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

}
