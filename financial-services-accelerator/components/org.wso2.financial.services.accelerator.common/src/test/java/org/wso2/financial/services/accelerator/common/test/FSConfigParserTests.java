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

package org.wso2.financial.services.accelerator.common.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesRuntimeException;
import org.wso2.financial.services.accelerator.common.util.CarbonUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Test class for Config Parser functionality.
 */
public class FSConfigParserTests {

    String absolutePathForTestResources;

    @BeforeClass
    public void beforeClass() throws ReflectiveOperationException {

        //to execute util class initialization
        new CarbonUtils();
        System.setProperty("some.property", "property.value");
        System.setProperty("carbon.home", ".");
        String path = "src/test/resources";
        File file = new File(path);
        absolutePathForTestResources = file.getAbsolutePath();
    }

    //Runtime exception is thrown here because carbon home is not defined properly for an actual carbon product
    @Test(expectedExceptions = FinancialServicesRuntimeException.class, priority = 1)
    public void testConfigParserInitiationWithoutPath() {

        FinancialServicesConfigParser openBankingConfigParser = FinancialServicesConfigParser.getInstance();

    }

    @Test(priority = 4)
    public void testConfigParserInit() {

        System.setProperty("carbon.home", absolutePathForTestResources);
        FinancialServicesConfigParser openBankingConfigParser = FinancialServicesConfigParser.getInstance();
        Assert.assertEquals(openBankingConfigParser.getConfiguration().get("Sample.FSHandler"), "DummyValue");
        Assert.assertEquals(openBankingConfigParser.getConfiguration().get("Sample.FSHandler2"), "property.value");
        Assert.assertNotNull(openBankingConfigParser.getConfiguration().get("Sample.FSHandler4"));
        Map<String, Map<Integer, String>> openBankingExecutors =
                FinancialServicesConfigParser.getInstance().getFinancialServicesExecutors();

        assertEquals(openBankingExecutors.get("CustomType1").get(1),
                "org.wso2.financial.services.accelerator.common.test.CustomHandler2");
        assertEquals(openBankingExecutors.get("CustomType2").get(1),
                "org.wso2.financial.services.accelerator.common.test.CustomHandler");

        Map<String, Map<Integer, String>> stepsConfig =
                FinancialServicesConfigParser.getInstance().getConsentAuthorizeSteps();

        assertEquals(stepsConfig.get("Persist").get(1),
                "org.wso2.financial.services.accelerator.common.test.CustomStep2");
        assertEquals(stepsConfig.get("Retrieve").get(1),
                "org.wso2.financial.services.accelerator.common.test.CustomStep1");

        Map<String, List<String>> apiMap = openBankingConfigParser.getAllowedAPIs();
        List<String> roles = apiMap.get("DynamicClientRegistration");
        Assert.assertNotNull(apiMap);
        Assert.assertNotNull(apiMap.get("DynamicClientRegistration"));
        assertNotNull(apiMap.get("AccountandTransactionAPI"));
        assertTrue(roles.contains("AISP"));
    }

    @Test(priority = 5)
    public void testSingleton() {

        FinancialServicesConfigParser instance1 = FinancialServicesConfigParser.getInstance();
        FinancialServicesConfigParser instance2 = FinancialServicesConfigParser.getInstance();
        Assert.assertEquals(instance2, instance1);
    }

    @Test(priority = 6)
    public void testCarbonPath() {

        String carbonConfigDirPath = CarbonUtils.getCarbonConfigDirPath();
        System.setProperty("carbon.config.dir.path", carbonConfigDirPath);
        Assert.assertEquals(CarbonUtils.getCarbonConfigDirPath(), carbonConfigDirPath);
    }

    @Test(priority = 7)
    public void testGetDatasourceName() {

        String config = FinancialServicesConfigParser.getInstance().getDataSourceName();
        Assert.assertNotNull(config);
    }

    @Test(priority = 8)
    public void testGetConnectionPoolMaxConnections() {

        int maxConnections = FinancialServicesConfigParser.getInstance().getConnectionPoolMaxConnections();
        int maxConnectionsPerRoute = FinancialServicesConfigParser.getInstance()
                .getConnectionPoolMaxConnectionsPerRoute();

        Assert.assertEquals(maxConnections, 1000);
        Assert.assertEquals(maxConnectionsPerRoute, 500);
    }

    @Test (priority = 10)
    public void testGetAuthServletExtension() {

        String authServletExtension = FinancialServicesConfigParser.getInstance().getAuthServletExtension();

        Assert.assertEquals(authServletExtension, "sampleServletExtension");
    }

    @Test (priority = 12)
    public void testGetJWKSConnectionTimeout() {

        String connectionTimeOut = FinancialServicesConfigParser.getInstance()
                .getJWKSConnectionTimeOut();

        Assert.assertEquals(connectionTimeOut, "1000");
    }

    @Test (priority = 13)
    public void testGetConnectionVerificationTimeout() {

        int connectionTimeOut = FinancialServicesConfigParser.getInstance()
                .getConnectionVerificationTimeout();

        Assert.assertEquals(connectionTimeOut, 1000);
    }

    @Test (priority = 14)
    public void testGetJWKSReadTimeout() {

        String connectionTimeOut = FinancialServicesConfigParser.getInstance()
                .getJWKSReadTimeOut();

        Assert.assertEquals(connectionTimeOut, "3000");
    }

    @Test (priority = 16)
    public void testGetEventNotificationTokenIssuer() {

        String tokenIssuer = FinancialServicesConfigParser.getInstance()
                .getEventNotificationTokenIssuer();

        Assert.assertEquals(tokenIssuer, "www.wso2.com");
    }

    @Test (priority = 17)
    public void testGetNumberOfSetsToReturn() {

        int maxEvents = FinancialServicesConfigParser.getInstance().getNumberOfSetsToReturn();

        Assert.assertEquals(maxEvents, 5);
    }

    @Test (priority = 18)
    public void testGetCommonCacheModifiedExpiryTime() {

        String connectionTimeOut = FinancialServicesConfigParser.getInstance()
                .getCommonCacheModifiedExpiryTime();

        Assert.assertEquals(connectionTimeOut, "60");
    }

    @Test (priority = 19)
    public void testGetCommonCacheAccessExpiryTime() {

        String connectionTimeOut = FinancialServicesConfigParser.getInstance()
                .getCommonCacheAccessExpiryTime();

        Assert.assertEquals(connectionTimeOut, "60");
    }
}
