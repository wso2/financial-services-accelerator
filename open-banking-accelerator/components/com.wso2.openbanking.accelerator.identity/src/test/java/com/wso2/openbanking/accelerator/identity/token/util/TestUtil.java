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

package com.wso2.openbanking.accelerator.identity.token.util;

import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;

/**
 * Test utils.
 */
public class TestUtil {

    public static Map<String, String> getResponse(ServletOutputStream outputStream) {

        Map<String, String> response = new HashMap<>();
        JSONObject outputStreamMap = new JSONObject(outputStream);
        JSONObject targetStream = new JSONObject(outputStreamMap.get(TestConstants.TARGET_STREAM).toString());
        response.put(IdentityCommonConstants.OAUTH_ERROR,
                targetStream.get(IdentityCommonConstants.OAUTH_ERROR).toString());
        response.put(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION,
                targetStream.get(IdentityCommonConstants.OAUTH_ERROR_DESCRIPTION).toString());
        return response;
    }

    public static X509Certificate getCertificate(String certificateContent) {

        if (StringUtils.isNotBlank(certificateContent)) {
            // Build the Certificate object from cert content.
            try {
                return (X509Certificate) IdentityUtil.convertPEMEncodedContentToCertificate(certificateContent);
            } catch (CertificateException e) {
                //do nothing
            }
        }
        return null;
    }
}
