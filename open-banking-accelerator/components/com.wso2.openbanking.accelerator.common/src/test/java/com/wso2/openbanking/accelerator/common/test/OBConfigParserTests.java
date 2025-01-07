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

package com.wso2.openbanking.accelerator.common.test;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingRuntimeException;
import com.wso2.openbanking.accelerator.common.test.util.CommonTestUtil;
import com.wso2.openbanking.accelerator.common.util.CarbonUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Test class for Config Parser functionality.
 */
public class OBConfigParserTests {

    String absolutePathForTestResources;

    @BeforeClass
    public void beforeClass() throws ReflectiveOperationException {

        //to execute util class initialization
        new CarbonUtils();
        System.setProperty("some.property", "property.value");
        System.setProperty("carbon.home", ".");
        CommonTestUtil.injectEnvironmentVariable("CARBON_HOME", ".");
        String path = "src/test/resources";
        File file = new File(path);
        absolutePathForTestResources = file.getAbsolutePath();
    }

    //Runtime exception is thrown here because carbon home is not defined properly for an actual carbon product
    @Test(expectedExceptions = OpenBankingRuntimeException.class, priority = 1)
    public void testConfigParserInitiationWithoutPath() {

        OpenBankingConfigParser openBankingConfigParser = OpenBankingConfigParser.getInstance();

    }

    @Test(expectedExceptions = OpenBankingRuntimeException.class, priority = 2)
    public void testRuntimeExceptionInvalidConfigFile() {

        String path = absolutePathForTestResources + "/open-banking-empty.xml";
        OpenBankingConfigParser openBankingConfigParser = OpenBankingConfigParser.getInstance(path);

    }

    @Test(expectedExceptions = OpenBankingRuntimeException.class, priority = 3)
    public void testRuntimeExceptionNonExistentFile() {

        String path = absolutePathForTestResources + "/open-banking.xml" + "/value";
        OpenBankingConfigParser openBankingConfigParser = OpenBankingConfigParser.getInstance(path);

    }

    @Test(priority = 4)
    public void testConfigParserInit() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        OpenBankingConfigParser openBankingConfigParser = OpenBankingConfigParser.getInstance(dummyConfigFile);
        Assert.assertEquals(openBankingConfigParser.getConfiguration().get("Sample.OBHandler"), "DummyValue");
        Assert.assertEquals(openBankingConfigParser.getConfiguration().get("Sample.OBHandler2"), "property.value");
        Assert.assertNotNull(openBankingConfigParser.getConfiguration().get("Sample.OBHandler4"));
        Map<String, Map<Integer, String>> openBankingExecutors =
                OpenBankingConfigParser.getInstance().getOpenBankingExecutors();

        openBankingExecutors.get("CustomType1").get(1).
                equals("com.wso2.openbanking.accelerator.common.test.CustomHandler2");
        openBankingExecutors.get("CustomType2").get(1).
                equals("com.wso2.openbanking.accelerator.common.test.CustomHandler");

        Map<String, Map<String, Object>> dcrRegistrationConfigs = OpenBankingConfigParser.getInstance()
                .getOpenBankingDCRRegistrationParams();

        dcrRegistrationConfigs.get("ParameterType").get("Required").equals("true");
        Assert.assertTrue(((List) dcrRegistrationConfigs.get("ParameterType").get("AllowedValues")).contains("Sample"));

        Map<String, Map<Integer, String>> stepsConfig =
                OpenBankingConfigParser.getInstance().getConsentAuthorizeSteps();

        stepsConfig.get("Persist").get(1).equals("com.wso2.openbanking.accelerator.common.test.CustomStep2");
        stepsConfig.get("Retrieve").get(1).equals("com.wso2.openbanking.accelerator.common.test.CustomStep1");

        Map<String, List<String>> apiMap = openBankingConfigParser.getAllowedAPIs();
        List<String> roles = apiMap.get("DynamicClientRegistration");
        Assert.assertNotNull(apiMap);
        Assert.assertNotNull(apiMap.get("DynamicClientRegistration"));
        Assert.assertTrue(apiMap.get("AccountandTransactionAPI") instanceof List);
        Assert.assertTrue(roles.contains("AISP"));
        Assert.assertFalse(openBankingConfigParser.getServiceActivatorSubscribers().isEmpty());

        Map<Integer, String> openBankingEventExecutors =
                OpenBankingConfigParser.getInstance().getOpenBankingEventExecutors();

        openBankingEventExecutors.get(1).
                equals("com.wso2.openbanking.accelerator.common.test.CustomEventExecutor1");
        openBankingEventExecutors.get(2).
                equals("com.wso2.openbanking.accelerator.common.test.CustomEventExecutor2");

    }

    @Test(priority = 5)
    public void testSingleton() {

        OpenBankingConfigParser instance1 = OpenBankingConfigParser.getInstance();
        OpenBankingConfigParser instance2 = OpenBankingConfigParser.getInstance();
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

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        String config = OpenBankingConfigParser.getInstance(dummyConfigFile).getDataSourceName();
        Assert.assertNotNull(config);
    }

    @Test(priority = 8)
    public void testGetConnectionPoolMaxConnections() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        int maxConnections = OpenBankingConfigParser.getInstance(dummyConfigFile).getConnectionPoolMaxConnections();
        int maxConnectionsPerRoute = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getConnectionPoolMaxConnectionsPerRoute();

        Assert.assertEquals(maxConnections, 1000);
        Assert.assertEquals(maxConnectionsPerRoute, 500);
    }

    @Test(priority = 8)
    public void testConsentPeriodicalExpirationConfigs() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        String expirationCronValue = OpenBankingConfigParser.getInstance(dummyConfigFile).
                getConsentExpiryCronExpression();
        String wordingForExpiredConsents = OpenBankingConfigParser.getInstance(dummyConfigFile).
                getStatusWordingForExpiredConsents();
        String eligibleStatusesForConsentExpiry = OpenBankingConfigParser.getInstance(dummyConfigFile).
                getEligibleStatusesForConsentExpiry();
        boolean periodicalJobEnabled = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .isConsentExpirationPeriodicalJobEnabled();

        Assert.assertEquals(expirationCronValue, "0 0 0 * * ?");
        Assert.assertEquals(wordingForExpiredConsents, "Expired");
        Assert.assertEquals(periodicalJobEnabled, false);
        Assert.assertEquals(eligibleStatusesForConsentExpiry, "authorised");
    }

    @Test (priority = 9)
    public void testGetTrustStoreDynamicLoadingInterval() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        long dynamicLoadingInterval = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getTruststoreDynamicLoadingInterval();

        Assert.assertEquals(dynamicLoadingInterval, Long.parseLong("86400"));
    }

    @Test (priority = 10)
    public void testGetAuthServletExtension() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        String authServletExtension = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getAuthServletExtension();

        Assert.assertEquals(authServletExtension, "sampleServletExtension");
    }

    @Test (priority = 12)
    public void testGetJWKSConnectionTimeout() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        String connectionTimeOut = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getJWKSConnectionTimeOut();

        Assert.assertEquals(connectionTimeOut, "1000");
    }

    @Test (priority = 13)
    public void testGetConnectionVerificationTimeout() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        int connectionTimeOut = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getConnectionVerificationTimeout();

        Assert.assertEquals(connectionTimeOut, 1000);
    }

    @Test (priority = 14)
    public void testGetJWKSReadTimeout() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        String connectionTimeOut = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getJWKSReadTimeOut();

        Assert.assertEquals(connectionTimeOut, "3000");
    }

    @Test (priority = 15)
    public void testGetSPMetadataFilterExtension() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        String connectionTimeOut = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getSPMetadataFilterExtension();

        Assert.assertEquals(connectionTimeOut, "sampleSPMetadataFilterExtension");
    }

    @Test (priority = 16)
    public void testGetEventNotificationTokenIssuer() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        String tokenIssuer = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getEventNotificationTokenIssuer();

        Assert.assertEquals(tokenIssuer, "www.wso2.com");
    }

    @Test (priority = 17)
    public void testGetNumberOfSetsToReturn() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        int maxEvents = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getNumberOfSetsToReturn();

        Assert.assertEquals(maxEvents, 5);
    }

    @Test (priority = 18)
    public void testGetCommonCacheModifiedExpiryTime() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        String connectionTimeOut = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getCommonCacheModifiedExpiryTime();

        Assert.assertEquals(connectionTimeOut, "60");
    }

    @Test (priority = 19)
    public void testGetCommonCacheAccessExpiryTime() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        String connectionTimeOut = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getCommonCacheAccessExpiryTime();

        Assert.assertEquals(connectionTimeOut, "60");
    }

    @Test (priority = 20)
    public void testGetJwsRequestSigningAlgorithms() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        List<String> algorithmConstraints = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getJwsRequestSigningAlgorithms();

        Assert.assertEquals(algorithmConstraints, Arrays.asList("PS256"));
    }

    @Test (priority = 20)
    public void testIsJwsSignatureValidationEnabled() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        boolean isEnabled = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .isJwsSignatureValidationEnabled();

        Assert.assertFalse(isEnabled);
    }

    @Test (priority = 21)
    public void testGetOBIdnRetrieverSigningCertificateAlias() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        String certificateAlias = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getOBIdnRetrieverSigningCertificateAlias();

        Assert.assertEquals(certificateAlias, "wso2carbon");
    }

    @Test (priority = 22)
    public void testOBIdnRetrieverSandboxSigningCertificateAlias() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        String certificateAlias = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getOBIdnRetrieverSandboxSigningCertificateAlias();

        Assert.assertEquals(certificateAlias, "wso2carbon-sandbox");
    }

    @Test (priority = 23)
    public void testGetOBIdnRetrieverSigningCertificateKid() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        String certificateAlias = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getOBIdnRetrieverSigningCertificateKid();

        Assert.assertEquals(certificateAlias, "1234");
    }

    @Test (priority = 24)
    public void testGetJwsResponseSigningAlgorithm() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        String certificateAlias = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getJwsResponseSigningAlgorithm();

        Assert.assertEquals(certificateAlias, "PS256");
    }

    @Test (priority = 25)
    public void testIsJwsResponseSigningEnabled() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        boolean isEnabled = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .isJwsResponseSigningEnabled();

        Assert.assertEquals(isEnabled, false);
    }

    @Test (priority = 26)
    public void testGetJwksRetrieverSizeLimit() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        String sizeLimit = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getJwksRetrieverSizeLimit();

        Assert.assertEquals(sizeLimit, "51200");
    }

    @Test (priority = 27)
    public void testGetJwksRetrieverConnectionTimeout() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        String timeout = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getJwksRetrieverConnectionTimeout();

        Assert.assertEquals(timeout, "2000");
    }

    @Test (priority = 28)
    public void testGetJwksRetrieverReadTimeout() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        String timeout = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getJwksRetrieverReadTimeout();

        Assert.assertEquals(timeout, "2000");
    }

    @Test (priority = 29)
    public void testIsToeClaimIncluded() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        Boolean isToeClaimIncluded = OpenBankingConfigParser.getInstance(dummyConfigFile).isToeClaimIncluded();

        Assert.assertTrue(isToeClaimIncluded);
    }

    @Test (priority = 30)
    public void testWithRetentionConfigs() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        OpenBankingConfigParser openBankingConfigParser = OpenBankingConfigParser.getInstance(dummyConfigFile);

        Assert.assertTrue(openBankingConfigParser.isConsentDataRetentionEnabled());
        Assert.assertTrue(openBankingConfigParser.isRetentionDataDBSyncEnabled());
        Assert.assertNotNull(openBankingConfigParser.getRetentionDataDBSyncCronExpression());
        Assert.assertNotNull(openBankingConfigParser.getRetentionDataSourceName());
        Assert.assertEquals(openBankingConfigParser.getRetentionDataSourceConnectionVerificationTimeout(), 1);
    }

    @Test (priority = 31)
    public void testIsDisputeResolutionEnabled() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        boolean isEnabled = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .isDisputeResolutionEnabled();

        Assert.assertTrue(isEnabled);
    }

    @Test (priority = 32)
    public void testIsNonErrorDisputeDataPublishingEnabled() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        boolean isEnabled = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .isNonErrorDisputeDataPublishingEnabled();

        Assert.assertTrue(isEnabled);
    }

    @Test (priority = 33)
    public void testRealtimeEventNotificationConfigs() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        OpenBankingConfigParser openBankingConfigParser = OpenBankingConfigParser.getInstance(dummyConfigFile);

        Assert.assertTrue(openBankingConfigParser.isRealtimeEventNotificationEnabled());
        Assert.assertEquals(openBankingConfigParser.getRealtimeEventNotificationSchedulerCronExpression(),
                "0 0/1 0 ? * * *");
        Assert.assertEquals(openBankingConfigParser.getRealtimeEventNotificationTimeoutInSeconds(), 60);
        Assert.assertEquals(openBankingConfigParser.getRealtimeEventNotificationMaxRetries(), 5);
        Assert.assertEquals(openBankingConfigParser.getRealtimeEventNotificationInitialBackoffTimeInSeconds(),
                60);
        Assert.assertEquals(openBankingConfigParser.getRealtimeEventNotificationBackoffFunction(), "EX");
        Assert.assertEquals(openBankingConfigParser.getRealtimeEventNotificationCircuitBreakerOpenTimeoutInSeconds(),
                600);
        Assert.assertEquals(openBankingConfigParser.getEventNotificationThreadpoolSize(), 20);
        Assert.assertEquals(openBankingConfigParser.getRealtimeEventNotificationRequestGenerator(),
                "com.wso2.openbanking.accelerator.event.notifications.service.realtime" +
                        ".service.DefaultRealtimeEventNotificationRequestGenerator");

    }

    @Test (priority = 34)
    public void testIsConsentAmendmentHistoryEnabled() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        boolean isEnabled = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .isConsentAmendmentHistoryEnabled();

        Assert.assertTrue(isEnabled);
    }

    @Test (priority = 35)
    public void testGetOBKeyManagerExtensionImpl() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        String className = OpenBankingConfigParser.getInstance(dummyConfigFile)
                .getOBKeyManagerExtensionImpl();

        Assert.assertEquals(className, "com.wso2.openbanking.accelerator.keymanager.OBKeyManagerImpl");
    }

    @Test(priority = 36)
    public void testNbfClaimMandatory() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        OpenBankingConfigParser openBankingConfigParser = OpenBankingConfigParser.getInstance(dummyConfigFile);

        boolean nbfClaimMandatory = openBankingConfigParser.isNbfClaimMandatory();
        Assert.assertTrue(nbfClaimMandatory);
    }

    @Test(priority = 37)
    public void testCibaWebLinkConfigs() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        OpenBankingConfigParser openBankingConfigParser = OpenBankingConfigParser.getInstance(dummyConfigFile);

        List<String> cibaWebLinkAllowedParams = openBankingConfigParser.getCibaWebLinkAllowedParams();
        Assert.assertEquals(cibaWebLinkAllowedParams.size(), 6);
        Assert.assertEquals(cibaWebLinkAllowedParams.get(0), "client_id");
        Assert.assertEquals(openBankingConfigParser.getCibaWebLinkNotificationProvider(),
                "com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator.weblink." +
                        "notification.provider.SMSNotificationProvider");
        Assert.assertEquals(openBankingConfigParser.getCibaAuthenticationRedirectEndpoint(),
                "https://localhost:9446/authenticationendpoint/ciba.jsp");
    }

    @Test(priority = 38)
    public void testCibaWebLinkSMSConfigs() {
        String dummyConfigFile = absolutePathForTestResources + "/open-banking.xml";
        OpenBankingConfigParser openBankingConfigParser = OpenBankingConfigParser.getInstance(dummyConfigFile);

        String cibaWebLinkAllowedParams = openBankingConfigParser.getCibaWebLinkSMSNotificationServiceURL();
        Assert.assertEquals(cibaWebLinkAllowedParams, "https://localhost:9446/sample/sms");
        Map<String, String> headerMap = openBankingConfigParser.getCibaWebLinkSMSNotificationRequestHeaders();
        Assert.assertEquals(headerMap.get("Authorization"), "abc");
        Assert.assertEquals(headerMap.get("Accept"), "application/json");
    }

}
