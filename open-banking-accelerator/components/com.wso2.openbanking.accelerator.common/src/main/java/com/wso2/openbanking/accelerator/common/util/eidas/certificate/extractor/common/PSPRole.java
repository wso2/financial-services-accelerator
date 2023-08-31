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
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERUTF8String;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * PSPRole enum.
 */
public enum PSPRole {
    PSP_AS(PSD2Constants.PSP_AS_OID, PSD2Constants.PSP_AS, PSD2Constants.ASPSP),
    PSP_PI(PSD2Constants.PSP_PI_OID, PSD2Constants.PSP_PI, PSD2Constants.PISP),
    PSP_AI(PSD2Constants.PSP_AI_OID, PSD2Constants.PSP_AI, PSD2Constants.AISP),
    PSP_IC(PSD2Constants.PSP_IC_OID, PSD2Constants.PSP_IC, PSD2Constants.CBPII);

    private String pspRoleOid;    //Object Identifier in the Certificate
    private String pspRoleName;   //Role Name stated on the certificate
    private String psd2RoleName;  //PSD2 Actor Name related to the role in the certificate

    PSPRole(String pspRoleOid, String pspRoleName, String psd2RoleName) {

        this.pspRoleOid = pspRoleOid;
        this.pspRoleName = pspRoleName;
        this.psd2RoleName = psd2RoleName;
    }

    public static List<PSPRole> getInstance(ASN1Encodable asn1Encodable) {

        List<PSPRole> pspRoleList = new ArrayList<>();
        ASN1Sequence sequence = ASN1Sequence.getInstance(asn1Encodable);

        Iterator it = sequence.iterator();
        while (it.hasNext()) {
            ASN1ObjectIdentifier objectIdentifier = ASN1ObjectIdentifier.getInstance(it.next());
            DERUTF8String instance = DERUTF8String.getInstance(it.next());

            pspRoleList.add(Arrays.stream(PSPRole.values())
                    .filter(role -> role.getPspRoleOid().equals(objectIdentifier.getId())
                            && role.getPspRoleName().equals(instance.getString()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException(
                            "unknown object in getInstance: " + asn1Encodable.getClass().getName())));
        }

        return pspRoleList;
    }


    public String getPspRoleOid() {

        return pspRoleOid;
    }

    public String getPspRoleName() {

        return pspRoleName;
    }

    public String getPsd2RoleName() {

        return psd2RoleName;
    }

}
