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

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;


/**
 * Test for Open Banking Utils.
 */
@PrepareForTest({OpenBankingConfigParser.class})
@PowerMockIgnore({"jdk.internal.reflect.*", "javax.management.*"})
public class OpenBankingUtilsTest extends PowerMockTestCase {

    private static final Log log = LogFactory.getLog(OpenBankingUtilsTest.class);
    @Mock
    OpenBankingConfigParser openBankingConfigParser;

    @BeforeMethod()
    public void before() {

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        openBankingConfigParser = PowerMockito.mock(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);

    }

    @Test
    public void testDisputeDataWhenNonErrorPublishingEnabled() throws Exception {

        when(openBankingConfigParser.isNonErrorDisputeDataPublishingEnabled()).thenReturn(true);

        Assert.assertTrue(OpenBankingUtils.isPublishableDisputeData(400));
        Assert.assertTrue(OpenBankingUtils.isPublishableDisputeData(200));
    }

    @Test
    public void testDisputeDataWhenNonErrorPublishingDisabled() throws Exception {

        when(openBankingConfigParser.isNonErrorDisputeDataPublishingEnabled()).thenReturn(false);

        Assert.assertTrue(OpenBankingUtils.isPublishableDisputeData(400));
        Assert.assertFalse(OpenBankingUtils.isPublishableDisputeData(200));
    }

    @Test
    public void testReducingStringLength() throws Exception {

        String body = "String Body";
        Assert.assertEquals(OpenBankingUtils.reduceStringLength(body, 25), body);
        Assert.assertEquals(OpenBankingUtils.reduceStringLength(body, 6), "String");
    }
}
