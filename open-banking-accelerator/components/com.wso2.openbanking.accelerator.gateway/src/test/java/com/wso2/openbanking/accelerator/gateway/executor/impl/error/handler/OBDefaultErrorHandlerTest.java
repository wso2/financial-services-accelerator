/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.gateway.executor.impl.error.handler;

import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test class for OBDefaultErrorHandler.
 */
public class OBDefaultErrorHandlerTest {

    Map<String, String> contextProps = new HashMap<>();

    @Test
    public void testPreRequestFlow() {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        Mockito.when(obapiRequestContext.isError()).thenReturn(true);
        Mockito.when(obapiRequestContext.getErrors()).thenReturn(getErrorList());
        Mockito.when(obapiRequestContext.getContextProps()).thenReturn(contextProps);
        Mockito.when(obapiRequestContext.getAnalyticsData()).thenReturn(new HashMap<>());

        OBDefaultErrorHandler commonReportingDataExecutor = Mockito.spy(OBDefaultErrorHandler.class);
        commonReportingDataExecutor.preProcessRequest(obapiRequestContext);
        verify(obapiRequestContext, times(0)).setError(false);
    }

    @Test
    public void testPostRequestFlow() {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        Mockito.when(obapiRequestContext.isError()).thenReturn(true);
        Mockito.when(obapiRequestContext.getErrors()).thenReturn(getErrorList());
        Mockito.when(obapiRequestContext.getContextProps()).thenReturn(contextProps);
        Mockito.when(obapiRequestContext.getAnalyticsData()).thenReturn(new HashMap<>());

        OBDefaultErrorHandler commonReportingDataExecutor = Mockito.spy(OBDefaultErrorHandler.class);
        commonReportingDataExecutor.postProcessRequest(obapiRequestContext);
        verify(obapiRequestContext, times(0)).setError(false);
    }

    @Test
    public void testPreResponseFlow() {

        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        Mockito.when(obapiResponseContext.isError()).thenReturn(true);
        Mockito.when(obapiResponseContext.getErrors()).thenReturn(getErrorList());
        Mockito.when(obapiResponseContext.getContextProps()).thenReturn(contextProps);
        Mockito.when(obapiResponseContext.getAnalyticsData()).thenReturn(new HashMap<>());

        OBDefaultErrorHandler commonReportingDataExecutor = Mockito.spy(OBDefaultErrorHandler.class);
        commonReportingDataExecutor.preProcessResponse(obapiResponseContext);
        verify(obapiResponseContext, times(0)).setError(false);
    }

    @Test
    public void testPostResponseFlow() {

        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        Mockito.when(obapiResponseContext.isError()).thenReturn(true);
        Mockito.when(obapiResponseContext.getErrors()).thenReturn(getErrorList());
        Mockito.when(obapiResponseContext.getContextProps()).thenReturn(contextProps);
        Mockito.when(obapiResponseContext.getAnalyticsData()).thenReturn(new HashMap<>());

        OBDefaultErrorHandler commonReportingDataExecutor = Mockito.spy(OBDefaultErrorHandler.class);
        commonReportingDataExecutor.postProcessResponse(obapiResponseContext);
        verify(obapiResponseContext, times(0)).setError(false);
    }

    private ArrayList<OpenBankingExecutorError> getErrorList() {

        OpenBankingExecutorError error = new OpenBankingExecutorError("400", "Invalid Request",
                "Mandatory parameter is missing", "400");

        ArrayList<OpenBankingExecutorError> errors = new ArrayList<>();
        errors.add(error);
        return errors;
    }
}
