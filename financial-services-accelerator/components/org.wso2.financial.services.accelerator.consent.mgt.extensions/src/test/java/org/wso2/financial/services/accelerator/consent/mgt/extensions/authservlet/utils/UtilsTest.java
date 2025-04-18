/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class UtilsTest {

    @Test
    public void testSplitClaims_withValidClaims() {
        String[] input = {"email_Email Address", "dob_Date of Birth"};
        List<Map<String, String>> result = Utils.splitClaims(input);

        assertEquals(result.size(), 2);
        assertEquals(result.get(0).get("claimId"), "email");
        assertEquals(result.get(0).get("displayName"), "Email Address");
        assertEquals(result.get(1).get("claimId"), "dob");
        assertEquals(result.get(1).get("displayName"), "Date of Birth");
    }

    @Test
    public void testSplitClaims_withInvalidClaims() {
        String[] input = {"email_Email Address", "invalidClaim"};
        List<Map<String, String>> result = Utils.splitClaims(input);

        assertEquals(result.size(), 1);
        assertEquals(result.get(0).get("claimId"), "email");
    }

    @Test
    public void testPopulateResourceData_shouldSetAttributesAndReturnData() {
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);

        JSONObject dataEntry = new JSONObject();
        dataEntry.put("title", "Account Info");
        dataEntry.put("data", new JSONArray().put("account1").put("account2"));

        JSONArray consentData = new JSONArray().put(dataEntry);

        JSONObject accountEntry = new JSONObject();
        accountEntry.put(ConsentExtensionConstants.ACCOUNT_ID, "A123");
        accountEntry.put(ConsentExtensionConstants.DISPLAY_NAME, "My Savings Account");

        JSONArray accounts = new JSONArray().put(accountEntry);

        JSONObject dataset = new JSONObject();
        dataset.put(ConsentExtensionConstants.CONSENT_DATA, consentData);
        dataset.put("accounts", accounts);

        Map<String, Object> result = Utils.populateResourceData(mockRequest, dataset);

        // Result assertions
        assertNotNull(result);
        assertEquals(result.size(), 1);
        assertNotNull(result.get(ConsentExtensionConstants.DATA_REQUESTED));
        Map<String, List<String>> dataRequested =
                (Map<String, List<String>>) result.get(ConsentExtensionConstants.DATA_REQUESTED);
        assertEquals(dataRequested.get("Account Info"), List.of("account1", "account2"));

        // Proper matchers
        verify(mockRequest).setAttribute(eq(ConsentExtensionConstants.ACCOUNT_DATA), any());
        verify(mockRequest).setAttribute(eq(ConsentExtensionConstants.CONSENT_TYPE),
                eq(ConsentExtensionConstants.DEFAULT));
    }
}
