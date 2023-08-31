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

package com.wso2.openbanking.accelerator.gateway.executor.impl.selfcare.portal;

import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.accelerator.gateway.util.GatewayUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * UserPermissionValidationExecutorTest.
 * <p>
 * Contains unit tests for UserPermissionValidationExecutor class
 */
@PrepareForTest({GatewayUtils.class})
@PowerMockIgnore({"jdk.internal.reflect.*"})
public class UserPermissionValidationExecutorTest {

    private UserPermissionValidationExecutor uut;

    @BeforeClass
    public void setup() {
        this.uut = new UserPermissionValidationExecutor();
    }

    @Test(description = "when valid url provided, return user IDs")
    public void testGetUserIdsFromQueryParamsWithValidUrl() {
        PowerMockito.mockStatic(GatewayUtils.class);
        when(GatewayUtils.getUserNameWithTenantDomain(anyString())).thenReturn("admin@wso2.com@carbon.super");

        final String url = "https://localhost:9446/api/consent/admin/search?userIDs=admin@wso2.com&limit=25";
        Optional<String> optUserIds = uut.getUserIdsFromQueryParams(url);

        Assert.assertTrue(optUserIds.isPresent());
        Assert.assertEquals(optUserIds.get(), "admin@wso2.com@carbon.super");
    }

    @Test(description = "when invalid url provided, return empty")
    public void testGetUserIdsFromQueryParamsWithInvalidUrl() {
        final String url = "https://localhost:9446/api/consent/admin/search?limit=25";
        Optional<String> optUserIds = uut.getUserIdsFromQueryParams(url);

        Assert.assertFalse(optUserIds.isPresent());
    }

    @Test(description = "when valid customer care officer's scopes received from access token, return true")
    public void testIsCustomerCareOfficerWithValidScope() {
        Assert.assertTrue(uut.isCustomerCareOfficer("consentmgt " +
                GatewayConstants.CUSTOMER_CARE_OFFICER_SCOPE + " openid"));
    }

    @Test(description = "when invalid scope received from access token, return false")
    public void testIsCustomerCareOfficerWithInvalidScope() {
        Assert.assertFalse(uut.isCustomerCareOfficer(" "));
        Assert.assertFalse(uut.isCustomerCareOfficer("consentmgt consents:read_self openid"));
    }

    @Test(description = "when userId is matching with access token subject, return true")
    public void testIsUserIdMatchesTokenSub() {
        Assert.assertTrue(uut.isUserIdMatchesTokenSub("amy@gold.com@carbon.super", "amy@gold.com@carbon.super"));
        Assert.assertTrue(uut.isUserIdMatchesTokenSub("amy@gold.com", "amy@gold.com"));

        Assert.assertFalse(uut.isUserIdMatchesTokenSub("mark@gold.com", "amy@gold.com"));
        Assert.assertFalse(uut.isUserIdMatchesTokenSub("amy@gold.com@carbon.super", "amy@gold.com"));
        Assert.assertFalse(uut.isUserIdMatchesTokenSub("amy@gold.com", "amy@gold.com@carbon.super"));
        Assert.assertFalse(uut.isUserIdMatchesTokenSub(" ", "amy@gold.com@carbon.super"));
        Assert.assertFalse(uut.isUserIdMatchesTokenSub("amy@gold.com", ""));
    }
}
