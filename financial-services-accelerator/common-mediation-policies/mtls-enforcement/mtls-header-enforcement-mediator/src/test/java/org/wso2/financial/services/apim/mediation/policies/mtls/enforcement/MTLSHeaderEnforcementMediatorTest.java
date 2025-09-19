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

package org.wso2.financial.services.apim.mediation.policies.mtls.enforcement;

import org.apache.axis2.context.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.apim.mediation.policies.mtls.enforcement.utils.MTLSEnforcementUtils;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for MTLSHeaderEnforcementMediator.
 */
public class MTLSHeaderEnforcementMediatorTest {

    private MTLSHeaderEnforcementMediator mediator;
    private Axis2MessageContext axis2Ctx;
    private MessageContext synapseCtx;
    private Map<String, Object> headers;

    MockedStatic<MTLSEnforcementUtils> mtlsEnforcementUtilsMockedStatic;

    @BeforeClass
    public void setup() {

        mediator = new MTLSHeaderEnforcementMediator();

        synapseCtx = Mockito.mock(MessageContext.class);
        axis2Ctx = Mockito.mock(Axis2MessageContext.class);
        headers = new HashMap<>();

        Mockito.when(axis2Ctx.getAxis2MessageContext()).thenReturn(synapseCtx);

        mtlsEnforcementUtilsMockedStatic = Mockito.mockStatic(MTLSEnforcementUtils.class);
    }

    @AfterClass
    public void tearDown() {

        mtlsEnforcementUtilsMockedStatic.close();
    }

    @Test
    public void testMtlsHeaderEnforcementMediatorValidScenario() {

        headers.put("Certificate-Header", "MockCert");
        Mockito.when(synapseCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);

        Certificate mockedCertificate = Mockito.mock(X509Certificate.class);
        mtlsEnforcementUtilsMockedStatic.when(() -> MTLSEnforcementUtils
                .parseCertificate("MockCert", false)).thenReturn(mockedCertificate);

        mediator.setTransportCertHeaderName("Certificate-Header");
        Assert.assertTrue(mediator.mediate(axis2Ctx));
    }

    @Test(expectedExceptions = SynapseException.class)
    public void testMtlsHeaderEnforcementMediatorNullCertificate() {

        Mockito.when(synapseCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);

        mtlsEnforcementUtilsMockedStatic.when(() -> MTLSEnforcementUtils
                .parseCertificate(null, false)).thenReturn(null);

        mediator.setTransportCertHeaderName("Certificate-Header");
        mediator.mediate(axis2Ctx);
    }

}

