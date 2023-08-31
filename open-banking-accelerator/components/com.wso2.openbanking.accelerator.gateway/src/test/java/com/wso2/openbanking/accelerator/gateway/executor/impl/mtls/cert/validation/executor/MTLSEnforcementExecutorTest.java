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

package com.wso2.openbanking.accelerator.gateway.executor.impl.mtls.cert.validation.executor;

import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.ArrayList;
/**
 * Test class for MTLSEnforcementExecutor.
 */
public class MTLSEnforcementExecutorTest {

    @Test(description = "When an error occurs, then it should set errors to the obApiRequestContext")
    public void testPreProcessRequest() throws Exception {
        MTLSEnforcementExecutor mtlsEnforcementExecutor = new MTLSEnforcementExecutor();

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        Mockito.when(obapiRequestContext.getErrors()).thenReturn(new ArrayList<>());

        mtlsEnforcementExecutor.preProcessRequest(obapiRequestContext);

        Mockito.verify(obapiRequestContext, Mockito.times(1)).setError(true);
        Mockito.verify(obapiRequestContext, Mockito.times(1)).setErrors(Mockito.any());

    }

}
