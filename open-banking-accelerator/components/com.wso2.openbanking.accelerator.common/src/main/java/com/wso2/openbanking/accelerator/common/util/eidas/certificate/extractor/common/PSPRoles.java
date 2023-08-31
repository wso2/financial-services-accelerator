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
import org.bouncycastle.asn1.DERSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Model to hold PSP roles.
 */
public class PSPRoles {

    private final List<PSPRole> roles;

    public PSPRoles(List<PSPRole> roles) {

        this.roles = roles;
    }

    public static PSPRoles getInstance(Object obj) {

        if (obj instanceof PSPRoles) {
            return (PSPRoles) obj;
        }

        ASN1Encodable[] array = DERSequence.getInstance(obj).toArray();

        List<PSPRole> pspRoles = new ArrayList<>();
        List<ASN1Encodable> arrayList = Arrays.asList(array);
        for (ASN1Encodable asn1Encodable : arrayList) {
            pspRoles.addAll(PSPRole.getInstance(asn1Encodable));
        }
        return new PSPRoles(pspRoles);
    }


    public List<PSPRole> getRoles() {

        return roles;
    }

}
