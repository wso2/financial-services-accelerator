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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize;

import org.json.JSONObject;
import org.mockito.Mock;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.impl.NonRegulatoryConsentStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Non regulatory retrieval step test.
 */
public class NonRegulatoryConsentStepTest {

    private static final NonRegulatoryConsentStep nonRegulatoryConsentStep = new NonRegulatoryConsentStep();
    @Mock
    private static ConsentData consentDataMock = mock(ConsentData.class);

    @Test
    public void testGetConsentDataSetForNonRegulatory() {

        JSONObject jsonObject = new JSONObject();
        doReturn(false).when(consentDataMock).isRegulatory();
        doReturn("consentmgt consent:read_all consent:read_self openid")
                .when(consentDataMock).getScopeString();
        nonRegulatoryConsentStep.execute(consentDataMock, jsonObject);

        assertFalse(jsonObject.isEmpty());
        assertTrue(jsonObject.has("openid_scopes"));
    }
}
