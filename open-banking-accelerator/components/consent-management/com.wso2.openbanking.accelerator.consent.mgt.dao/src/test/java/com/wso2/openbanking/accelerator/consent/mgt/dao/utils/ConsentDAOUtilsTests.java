/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.accelerator.consent.mgt.dao.utils;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Tests class for {@link ConsentDAOUtils}.
 */
public class ConsentDAOUtilsTests {

    @DataProvider(name = "consentAttributesDataProvider")
    public Object[][] consentAttributesDataProvider() {

        return new Object[][]{
                {"", new String[0]},
                {null, new String[0]},
                {"test-key1@@test-@@-value", Collections.singletonList("test-@@-value").toArray(new String[0])},
                {"test-key1@@@test-@-value", Collections.singletonList("@test-@-value").toArray(new String[0])},
                {"test-key1@@test-value1", Collections.singletonList("test-value1").toArray(new String[0])},
                {"test-key1@@test-value1||test-key2@@", Arrays.asList("test-value1", "").toArray(new String[0])},
                {"test-key1@@test-value1||test-key2@@test value 2",
                        Arrays.asList("test-value1", "test value 2").toArray(new String[0])},
                {"test-key1@@test-value1||test-key2@@test-value1",
                        Arrays.asList("test-value1", "test-value1").toArray(new String[0])},
        };
    }

    @Test(dataProvider = "consentAttributesDataProvider")
    public void testExtractAttributeValues(final String input, final String[] expected) {

        Assert.assertEquals(ConsentDAOUtils.extractAttributeValues(input), expected);
    }
}
