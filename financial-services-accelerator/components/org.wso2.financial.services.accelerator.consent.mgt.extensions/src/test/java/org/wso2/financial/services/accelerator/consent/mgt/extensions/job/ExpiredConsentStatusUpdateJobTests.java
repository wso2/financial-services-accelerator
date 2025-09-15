/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.job;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.util.ArrayList;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test class for ExpiredConsentStatusUpdateJob.
 */
public class ExpiredConsentStatusUpdateJobTests {

    private static MockedStatic<FinancialServicesConfigParser> mockedConfigParser;

    @BeforeClass
    public void initTest() throws ConsentManagementException {

        mockedConfigParser = Mockito.mockStatic(FinancialServicesConfigParser.class);
        FinancialServicesConfigParser mockConfigParser = mock(FinancialServicesConfigParser.class);
        when(mockConfigParser.getStatusWordingForExpiredConsents()).thenReturn("Expired");
        when(mockConfigParser.getEligibleStatusesForConsentExpiry()).thenReturn("Authorised");
        when(mockConfigParser.isConsentAmendmentHistoryEnabled()).thenReturn(true);
        mockedConfigParser.when(FinancialServicesConfigParser::getInstance).thenReturn(mockConfigParser);

        ConsentCoreService consentCoreService = mock(ConsentCoreService.class);

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID("123");
        detailedConsentResource.setValidityPeriod(1746017102);
        detailedConsentResources.add(detailedConsentResource);

        when(consentCoreService.getConsentsEligibleForExpiration(Mockito.any()))
                .thenReturn(detailedConsentResources);
        when(consentCoreService.updateConsentStatus(Mockito.any(), Mockito.any()))
                .thenReturn(detailedConsentResource);
        ConsentExtensionsDataHolder.getInstance().setConsentCoreService(consentCoreService);

    }

    @AfterClass
    public static void afterClass() {
        mockedConfigParser.close();
    }

    @Test
    void testUpdateExpiredStatues() throws ConsentManagementException {
        ExpiredConsentStatusUpdateJob.updateExpiredStatues();
    }

}
