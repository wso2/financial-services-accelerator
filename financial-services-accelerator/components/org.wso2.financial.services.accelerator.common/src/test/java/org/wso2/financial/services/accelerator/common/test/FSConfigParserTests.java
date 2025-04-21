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
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Test class for Config Parser functionality.
 */
public class FSConfigParserTests {

    String absolutePathForTestResources;

    @BeforeClass
    public void beforeClass() throws ReflectiveOperationException {

        // to execute util class initialization
        new CarbonUtils();
        System.setProperty("some.property", "property.value");
        System.setProperty("carbon.home", ".");
        String path = "src/test/resources";
        File file = new File(path);
        absolutePathForTestResources = file.getAbsolutePath();
    }

    // Runtime exception is thrown here because carbon home is not defined properly
    // for an actual carbon product
    @Test(expectedExceptions = FinancialServicesRuntimeException.class, priority = 1)
    public void testConfigParserInitiationWithoutPath() {

        FinancialServicesConfigParser configParser = FinancialServicesConfigParser.getInstance();

    }

    @Test(priority = 4)
    public void testConfigParserInit() {

        System.setProperty("carbon.home", absolutePathForTestResources);
        FinancialServicesConfigParser configParser = FinancialServicesConfigParser.getInstance();
        Assert.assertEquals(configParser.getConfiguration().get("Sample.FSHandler"), "DummyValue");
        Assert.assertEquals(configParser.getConfiguration().get("Sample.FSHandler2"), "property.value");
        Assert.assertNotNull(configParser.getConfiguration().get("Sample.FSHandler4"));
        Map<String, Map<Integer, String>> fsExecutors = FinancialServicesConfigParser.getInstance()
                .getFinancialServicesExecutors();

        assertEquals(fsExecutors.get("CustomType1").get(1),
                "org.wso2.financial.services.accelerator.common.test.CustomHandler2");
        assertEquals(fsExecutors.get("CustomType2").get(1),
                "org.wso2.financial.services.accelerator.common.test.CustomHandler");

        Map<String, Map<Integer, String>> stepsConfig = FinancialServicesConfigParser.getInstance()
                .getConsentAuthorizeSteps();

        assertEquals(stepsConfig.get("Persist").get(1),
                "org.wso2.financial.services.accelerator.common.test.CustomStep2");
        assertEquals(stepsConfig.get("Retrieve").get(1),
                "org.wso2.financial.services.accelerator.common.test.CustomStep1");

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

    @Test(priority = 10)
    public void testGetAuthServletExtension() {

        String authServletExtension = FinancialServicesConfigParser.getInstance().getAuthServletExtension();

        Assert.assertEquals(authServletExtension, "sampleServletExtension");
    }

    @Test(priority = 12)
    public void testGetJWKSConnectionTimeout() {

        String connectionTimeOut = FinancialServicesConfigParser.getInstance()
                .getJWKSConnectionTimeOut();

        Assert.assertEquals(connectionTimeOut, "3000");
    }

    @Test(priority = 13)
    public void testGetConnectionVerificationTimeout() {

        int connectionTimeOut = FinancialServicesConfigParser.getInstance()
                .getConnectionVerificationTimeout();

        Assert.assertEquals(connectionTimeOut, 1000);
    }

    @Test(priority = 14)
    public void testGetJWKSReadTimeout() {

        String connectionTimeOut = FinancialServicesConfigParser.getInstance()
                .getJWKSReadTimeOut();

        Assert.assertEquals(connectionTimeOut, "3000");
    }

    @Test(priority = 18)
    public void testGetCommonCacheModifiedExpiryTime() {

        int connectionTimeOut = FinancialServicesConfigParser.getInstance()
                .getCommonCacheModifiedExpiryTime();

        Assert.assertEquals(connectionTimeOut, 60);
    }

    @Test(priority = 19)
    public void testGetCommonCacheAccessExpiryTime() {

        int connectionTimeOut = FinancialServicesConfigParser.getInstance()
                .getCommonCacheAccessExpiryTime();

        Assert.assertEquals(connectionTimeOut, 60);
    }

    @Test(priority = 20)
    public void testGetConsentAPIUsername() {

        Assert.assertEquals(FinancialServicesConfigParser.getInstance().getConsentAPIUsername(), "admin");
    }

    @Test(priority = 21)
    public void testGetConsentAPIPassword() {

        Assert.assertEquals(FinancialServicesConfigParser.getInstance().getConsentAPIPassword(), "admin");
    }

    @Test(priority = 22)
    public void testGetPreserveConsent() {

        Assert.assertEquals(FinancialServicesConfigParser.getInstance().getPreserveConsent(), "false");
    }

    @Test(priority = 23)
    public void testGetConsentValidationConfig() {

        Assert.assertNotNull(FinancialServicesConfigParser.getInstance().getConsentValidationConfig());
    }

    @Test(priority = 24)
    public void testGetEventNotificationTokenIssuer() {

        Assert.assertNotNull(FinancialServicesConfigParser.getInstance().getEventNotificationTokenIssuer());
    }

    @Test(priority = 25)
    public void testGetNumberOfSetsToReturn() {

        Assert.assertEquals(FinancialServicesConfigParser.getInstance().getNumberOfSetsToReturn(), 5);
    }

    @Test(priority = 26)
    public void testIsSubClaimIncluded() {

        Assert.assertFalse(FinancialServicesConfigParser.getInstance().isSubClaimIncluded());
    }

    @Test(priority = 27)
    public void testIsToeClaimIncluded() {

        Assert.assertFalse(FinancialServicesConfigParser.getInstance().isToeClaimIncluded());
    }

    @Test(priority = 28)
    public void testIsTxnClaimIncluded() {

        Assert.assertFalse(FinancialServicesConfigParser.getInstance().isTxnClaimIncluded());
    }

    @Test(priority = 29)
    public void testIsRealtimeEventNotificationEnabled() {

        Assert.assertFalse(FinancialServicesConfigParser.getInstance().isRealtimeEventNotificationEnabled());
    }

    @Test(priority = 30)
    public void testGetRealtimeEventNotificationSchedulerCronExpression() {

        Assert.assertNotNull(FinancialServicesConfigParser.getInstance()
                .getRealtimeEventNotificationSchedulerCronExpression());
    }

    @Test(priority = 31)
    public void testGetRealtimeEventNotificationTimeoutInSeconds() {

        Assert.assertEquals(FinancialServicesConfigParser.getInstance()
                .getRealtimeEventNotificationTimeoutInSeconds(), 60);
    }

    @Test(priority = 32)
    public void testGetRealtimeEventNotificationMaxRetries() {

        Assert.assertEquals(FinancialServicesConfigParser.getInstance()
                .getRealtimeEventNotificationMaxRetries(), 5);
    }

    @Test(priority = 33)
    public void testGetRealtimeEventNotificationInitialBackoffTimeInSeconds() {

        Assert.assertEquals(FinancialServicesConfigParser.getInstance()
                .getRealtimeEventNotificationInitialBackoffTimeInSeconds(), 60);
    }

    @Test(priority = 34)
    public void testGetRealtimeEventNotificationBackoffFunction() {

        Assert.assertNotNull(FinancialServicesConfigParser.getInstance()
                .getRealtimeEventNotificationBackoffFunction());
    }

    @Test(priority = 35)
    public void testGetRealtimeEventNotificationCircuitBreakerOpenTimeoutInSeconds() {

        Assert.assertEquals(FinancialServicesConfigParser.getInstance()
                .getRealtimeEventNotificationCircuitBreakerOpenTimeoutInSeconds(), 600);
    }
    @Test(priority = 36)
    public void testGetEventNotificationThreadPoolSize() {

        Assert.assertEquals(FinancialServicesConfigParser.getInstance()
                .getEventNotificationThreadPoolSize(), 20);
    }

    @Test(priority = 37)
    public void testGetEventNotificationGenerator() {

        Assert.assertNotNull(FinancialServicesConfigParser.getInstance()
                .getEventNotificationGenerator());
    }

    @Test(priority = 38)
    public void testGetRealtimeEventNotificationRequestGenerator() {

        Assert.assertNotNull(FinancialServicesConfigParser.getInstance()
                .getRealtimeEventNotificationRequestGenerator());
    }

    @Test(priority = 39)
    public void testGetDCRParamsConfig() {

        Assert.assertNotNull(FinancialServicesConfigParser.getInstance()
                .getDCRParamsConfig());
    }

    @Test(priority = 40)
    public void testGetDCRResponseParameters() {

        Assert.assertNotNull(FinancialServicesConfigParser.getInstance()
                .getDCRResponseParameters());
    }

    @Test(priority = 41)
    public void testIsServiceExtensionsEndpointEnabled() {

        Assert.assertTrue(FinancialServicesConfigParser.getInstance()
                .isServiceExtensionsEndpointEnabled());
    }

    @Test(priority = 42)
    public void testGetServiceExtensionsEndpointBaseUrl() {

        Assert.assertNotNull(FinancialServicesConfigParser.getInstance()
                .getServiceExtensionsEndpointBaseUrl());
    }

    @Test(priority = 43)
    public void testGetServiceExtensionsEndpointRetryCount() {

        Assert.assertEquals(FinancialServicesConfigParser.getInstance()
                .getServiceExtensionsEndpointRetryCount(), 5);
    }

    @Test(priority = 44)
    public void testGetServiceExtensionsEndpointConnectTimeoutInSeconds() {

        Assert.assertEquals(FinancialServicesConfigParser.getInstance()
                .getServiceExtensionsEndpointConnectTimeoutInSeconds(), 5);
    }

    @Test(priority = 45)
    public void testGetServiceExtensionsEndpointReadTimeoutInSeconds() {

        Assert.assertEquals(FinancialServicesConfigParser.getInstance()
                .getServiceExtensionsEndpointReadTimeoutInSeconds(), 5);
    }

    @Test(priority = 46)
    public void testGetServiceExtensionTypes() {

        Assert.assertEquals(FinancialServicesConfigParser.getInstance()
                .getServiceExtensionTypes().size(), 3);
    }

    @Test(priority = 47)
    public void testGetServiceExtensionsEndpointSecurityType() {

        Assert.assertNotNull(FinancialServicesConfigParser.getInstance()
                .getServiceExtensionsEndpointSecurityType());
    }

    @Test(priority = 48)
    public void testGetServiceExtensionsEndpointSecurityBasicAuthUsername() {

        Assert.assertNotNull(FinancialServicesConfigParser.getInstance()
                .getServiceExtensionsEndpointSecurityBasicAuthUsername());
    }

    @Test(priority = 49)
    public void testGetServiceExtensionsEndpointSecurityBasicAuthPassword() {

        Assert.assertNotNull(FinancialServicesConfigParser.getInstance()
                .getServiceExtensionsEndpointSecurityBasicAuthPassword());
    }

    @Test(priority = 50)
    public void testIsPreInitiatedConsent() {

        Assert.assertTrue(FinancialServicesConfigParser.getInstance()
                .isPreInitiatedConsent());
    }

}
