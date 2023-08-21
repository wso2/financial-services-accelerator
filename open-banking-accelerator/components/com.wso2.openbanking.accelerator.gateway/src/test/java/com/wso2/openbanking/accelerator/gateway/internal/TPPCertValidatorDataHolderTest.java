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
package com.wso2.openbanking.accelerator.gateway.internal;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test for TPP certificate validator data holder.
 */
public class TPPCertValidatorDataHolderTest {

    private TPPCertValidatorDataHolder tppCertValidatorDataHolder;

    @BeforeClass
    public void init() {
        System.setProperty("some.property", "property.value");
        System.setProperty("carbon.home", ".");
        //CommonTestUtil.injectEnvironmentVariable("CARBON_HOME", ".");

        File file = new File("src/test/resources");
        String dummyConfigFile = file.getAbsolutePath() + "/open-banking.xml";
        OpenBankingConfigParser openBankingConfigParser = OpenBankingConfigParser.getInstance(dummyConfigFile);

        OpenBankingConfigurationService openBankingConfigurationServiceMock
                = Mockito.mock(OpenBankingConfigurationService.class);
        Mockito.doReturn(openBankingConfigParser.getConfiguration())
                .when(openBankingConfigurationServiceMock).getConfigurations();

        tppCertValidatorDataHolder = TPPCertValidatorDataHolder.getInstance();
        tppCertValidatorDataHolder.setOpenBankingConfigurationService(openBankingConfigurationServiceMock);
        tppCertValidatorDataHolder.initializeTPPValidationDataHolder();
    }

    @Test
    public void testConfigs() {
        Assert.assertEquals(tppCertValidatorDataHolder.getCertificateRevocationValidationRetryCount(), 3);
        Assert.assertEquals(tppCertValidatorDataHolder.getCertificateRevocationProxyPort(), 8080);
        Assert.assertEquals(tppCertValidatorDataHolder.getTppCertRevocationCacheExpiry(), 3600);
        Assert.assertEquals(tppCertValidatorDataHolder.getTppValidationCacheExpiry(), 3600);
        Assert.assertTrue(tppCertValidatorDataHolder.isCertificateRevocationValidationEnabled());
        Assert.assertTrue(tppCertValidatorDataHolder.isCertificateRevocationProxyEnabled());
        Assert.assertTrue(tppCertValidatorDataHolder.isTransportCertIssuerValidationEnabled());
        Assert.assertTrue(tppCertValidatorDataHolder.isPsd2RoleValidationEnabled());
        Assert.assertFalse(tppCertValidatorDataHolder.isTppValidationEnabled());
        Assert.assertNotNull(tppCertValidatorDataHolder.getCertificateRevocationValidationExcludedIssuers());
        Assert.assertNull(tppCertValidatorDataHolder.getTPPValidationServiceImpl());
        Assert.assertEquals(tppCertValidatorDataHolder.getCertificateRevocationProxyHost(), "PROXY_HOSTNAME");
    }
}
