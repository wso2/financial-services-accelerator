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

package org.wso2.financial.services.accelerator.common.test.util;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * Test for Financial Services Utils.
 */
public class FinancialServicesUtilsTest {

    @Mock
    private MockedStatic<FinancialServicesConfigParser> configParser;

    @BeforeClass
    public void initClass() {

        Map<String, Object> configs = new HashMap<>();
        configParser = mockStatic(FinancialServicesConfigParser.class);

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

    }

    @AfterClass
    public void tearDown() {
        // Closing the mockStatic after each test
        configParser.close();
    }


    @Test
    public void testReducingStringLength() throws Exception {

        String body = "String Body";
        Assert.assertEquals(FinancialServicesUtils.reduceStringLength(body, 25), body);
        Assert.assertEquals(FinancialServicesUtils.reduceStringLength(body, 6), "String");
    }
}
