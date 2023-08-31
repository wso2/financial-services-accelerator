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

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * PSD2QCType class.
 */
public class PSD2QCType {

    private final PSPRoles pspRoles;
    private final NcaName nCAName;
    private final NcaId nCAId;

    public PSD2QCType(PSPRoles pspRoles, NcaName nCAName, NcaId nCAId) {

        this.pspRoles = pspRoles;
        this.nCAName = nCAName;
        this.nCAId = nCAId;
    }

    public static PSD2QCType getInstance(ASN1Encodable asn1Encodable) {

        ASN1Sequence sequence = ASN1Sequence.getInstance(asn1Encodable);
        PSPRoles pspRoles = PSPRoles.getInstance(sequence.getObjectAt(0));
        NcaName nCAName = NcaName.getInstance(sequence.getObjectAt(1));
        NcaId nCAId = NcaId.getInstance(sequence.getObjectAt(2));
        return new PSD2QCType(pspRoles, nCAName, nCAId);
    }


    public PSPRoles getPspRoles() {

        return pspRoles;
    }

    public NcaName getnCAName() {

        return nCAName;
    }

    public NcaId getnCAId() {

        return nCAId;
    }
}
