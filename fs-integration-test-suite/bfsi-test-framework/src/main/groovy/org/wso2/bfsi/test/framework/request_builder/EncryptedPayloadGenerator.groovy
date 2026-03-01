/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.bfsi.test.framework.request_builder

import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSAEncrypter
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.wso2.bfsi.test.framework.configuration.CommonConfigurationService

import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableKeyException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.interfaces.RSAPublicKey

/**
 * Class for get Signed objects
 */
class EncryptedPayloadGenerator {

    private static final Log log = LogFactory.getLog(EncryptedPayloadGenerator.class)
    private CommonConfigurationService configuration
    private String signingAlgorithm = null

    EncryptedPayloadGenerator() {
        configuration = new CommonConfigurationService()
    }

    public String generateEncryptedPayload(String payload) {

        try (FileInputStream is = new FileInputStream(configuration.getTransportKeystoreLocation())) {

            // 1. Get the public RSA key from the keystore
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, configuration.getTransportKeystorePWD().toCharArray());
            Certificate publicKey = keystore.getCertificate("wso2carbon");
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey.getPublicKey();

            // 2. Build the JWE Header
            // The JWE header specifies the algorithms for key management and content encryption.
            // RSA-OAEP-256 is the recommended algorithm for key encryption.
            // A256GCM is a strong algorithm for content encryption.
            JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                    .build();

            // 3. Create the JWE payload
            Payload payloadData = new Payload(payload);

            // 4. Create the JWE object
            JWEObject jweObject = new JWEObject(header, payloadData);

            // 5. Encrypt the JWE object with the public RSA key
            RSAEncrypter encrypter = new RSAEncrypter(rsaPublicKey);
            jweObject.encrypt(encrypter);

            // 6. Serialize the JWE object to its compact form
            String jweToken = jweObject.serialize()

            System.out.println("Original Payload: " + payloadData)
            System.out.println("JWE Token (Encrypted): " + jweToken)
            return jweToken;
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            log.error("Error occurred while retrieving private key from keystore ", e);
        }
    }
}