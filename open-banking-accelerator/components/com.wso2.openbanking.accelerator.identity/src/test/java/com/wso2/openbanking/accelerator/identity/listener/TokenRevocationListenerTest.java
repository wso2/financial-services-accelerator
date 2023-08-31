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

package com.wso2.openbanking.accelerator.identity.listener;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import org.junit.Assert;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
/**
 * Test class for TokenRevocationListener.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({OpenBankingConfigParser.class})
public class TokenRevocationListenerTest extends PowerMockTestCase {

    OpenBankingConfigParser openBankingConfigParserMock;
    private Map<String, Object> configMap = new HashMap<>();

    @BeforeClass
    public void init() {
        mockOpenBankingConfigParser();
        configMap.put("Identity.ConsentIDClaimName", "consent_id");
    }

    @Test
    public void testGetConsentIdFromScopes() {
        String[] scopes = {"dummy-scope1", "dummy-scope2", "consent_idConsentId", "dummy-scope3"};
        TokenRevocationListener tokenRevocationListener = new TokenRevocationListener();
        String consentId = tokenRevocationListener.getConsentIdFromScopes(scopes);
        Assert.assertEquals("ConsentId", consentId);
    }

    private void mockOpenBankingConfigParser() {
        openBankingConfigParserMock = Mockito.mock(OpenBankingConfigParser.class);
        Mockito.when(openBankingConfigParserMock.getConfiguration())
                .thenReturn(configMap);
        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
    }
}
