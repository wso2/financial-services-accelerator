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

package com.wso2.openbanking.accelerator.common.test.util;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.HTTPClientUtils;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Http Client util test.
 */
public class HTTPClientUtilsTest {

    String path = "src/test/resources";
    File file = new File(path);
    String absolutePathForTestResources = file.getAbsolutePath();

    @Test
    public void testLoadKeystore() throws OpenBankingException {

        Assert.assertNotNull(HTTPClientUtils.loadKeyStore(absolutePathForTestResources + "/wso2carbon.jks",
                "wso2carbon"));
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testLoadInvalidKeystore() throws OpenBankingException {

        HTTPClientUtils.loadKeyStore(absolutePathForTestResources + "/wso2carbon2.jks",
                "wso2carbon");
    }

    @Test
    public void testHostNameVerifier() throws OpenBankingException {

        Assert.assertEquals(HTTPClientUtils.getX509HostnameVerifier(),
                SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

        System.setProperty(HTTPClientUtils.HOST_NAME_VERIFIER,
                String.valueOf(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER));

        Assert.assertEquals(HTTPClientUtils.getX509HostnameVerifier(),
                SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

        System.setProperty(HTTPClientUtils.HOST_NAME_VERIFIER,
                String.valueOf(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER));

        Assert.assertEquals(HTTPClientUtils.getX509HostnameVerifier(),
                SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
    }

}
