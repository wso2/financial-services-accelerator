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

import java.util.Date;
import java.util.List;

/**
 * Class That Contains Extracted PSD2  Attributes from the certificate.
 */
public class CertificateContent {
    private String pspAuthorisationNumber;
    private List<String> pspRoles;
    private List<String> psd2Roles;
    private String name;
    private String ncaName;
    private String ncaId;
    private Date notAfter = null;
    private Date notBefore = null;


    public String getPspAuthorisationNumber() {

        return pspAuthorisationNumber;
    }

    public void setPspAuthorisationNumber(String pspAuthorisationNumber) {

        this.pspAuthorisationNumber = pspAuthorisationNumber;
    }

    public List<String> getPspRoles() {

        return pspRoles;
    }

    public void setPspRoles(List<String> pspRoles) {

        this.pspRoles = pspRoles;
    }

    public List<String> getPsd2Roles() {
        return psd2Roles;
    }

    public void setPsd2Roles(List<String> psd2Roles) {
        this.psd2Roles = psd2Roles;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getNcaName() {

        return ncaName;
    }

    public void setNcaName(String ncaName) {

        this.ncaName = ncaName;
    }

    public String getNcaId() {

        return ncaId;
    }

    public void setNcaId(String ncaId) {

        this.ncaId = ncaId;
    }

    public Date getNotAfter() {

        return new Date(notAfter.getTime());
    }

    public void setNotAfter(Date notAfter) {

        this.notAfter = new Date(notAfter.getTime());
    }

    public Date getNotBefore() {

        return new Date(notBefore.getTime());
    }

    public void setNotBefore(Date notBefore) {

        this.notBefore = new Date(notBefore.getTime());
    }
}
