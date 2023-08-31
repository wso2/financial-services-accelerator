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

package com.wso2.openbanking.accelerator.identity.keyidprovider;

import com.nimbusds.jose.JWSAlgorithm;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.keyidprovider.DefaultKeyIDProviderImpl;

import java.security.cert.Certificate;
import java.util.Optional;

/**
 * OB specific Key ID provider implementation.
 */
public class OBKeyIDProvider extends DefaultKeyIDProviderImpl {

    private static final Log log = LogFactory.getLog(OBKeyIDProvider.class);

    /**
     * Method containing the KeyID calculation logic for OB.
     *
     * @param certificate        Signing Certificate.
     * @param signatureAlgorithm Signature Algorithm configured.
     * @param tenantDomain       tenant domain of the user.
     * @return Key ID as a String.
     * @throws IdentityOAuth2Exception When fail to generate the Key ID.
     */
    @Override
    public String getKeyId(Certificate certificate, JWSAlgorithm signatureAlgorithm, String tenantDomain)
            throws IdentityOAuth2Exception {

        String kid;
        Optional<Object> primaryCertKid = Optional.ofNullable(IdentityExtensionsDataHolder.getInstance()
                .getConfigurationMap().get(IdentityCommonConstants.SIGNING_CERT_KID));
        if (primaryCertKid.isPresent()) {
            kid = primaryCertKid.get().toString();
            if (log.isDebugEnabled()) {
                log.debug("KID value is configured in the open-banking.xml. Therefore returning configured value :"
                        + kid + " as the KID");
            }
            return kid;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("KID value is not configured in the open-banking.xml Therefore calling the default Key ID " +
                        "provider implementation");
            }
            return super.getKeyId(certificate, signatureAlgorithm, tenantDomain);
        }
    }
}
