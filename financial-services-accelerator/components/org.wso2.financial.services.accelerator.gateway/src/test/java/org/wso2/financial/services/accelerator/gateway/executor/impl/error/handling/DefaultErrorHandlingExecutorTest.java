/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.gateway.executor.impl.error.handling;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIRequestContext;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIResponseContext;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSExecutorError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test class for OBDefaultErrorHandler.
 */
public class DefaultErrorHandlingExecutorTest {

    Map<String, Object> contextProps = new HashMap<>();

    @Test
    public void testPreRequestFlow() {

        FSAPIRequestContext fsapiRequestContext = Mockito.mock(FSAPIRequestContext.class);
        Mockito.when(fsapiRequestContext.isError()).thenReturn(true);
        Mockito.when(fsapiRequestContext.getErrors()).thenReturn(getErrorList());
        Mockito.when(fsapiRequestContext.getContextProps()).thenReturn(contextProps);

        DefaultErrorHandlingExecutor errorHandlingExecutor = Mockito.spy(DefaultErrorHandlingExecutor.class);
        errorHandlingExecutor.preProcessRequest(fsapiRequestContext);
        verify(fsapiRequestContext, times(0)).setError(false);
    }

    @Test
    public void testPostRequestFlow() {

        FSAPIRequestContext fsapiRequestContext = Mockito.mock(FSAPIRequestContext.class);
        Mockito.when(fsapiRequestContext.isError()).thenReturn(true);
        Mockito.when(fsapiRequestContext.getErrors()).thenReturn(getErrorList());
        Mockito.when(fsapiRequestContext.getContextProps()).thenReturn(contextProps);

        DefaultErrorHandlingExecutor errorHandlingExecutor = Mockito.spy(DefaultErrorHandlingExecutor.class);
        errorHandlingExecutor.postProcessRequest(fsapiRequestContext);
        verify(fsapiRequestContext, times(0)).setError(false);
    }

    @Test
    public void testPreResponseFlow() {

        FSAPIResponseContext fsapiResponseContext = Mockito.mock(FSAPIResponseContext.class);
        Mockito.when(fsapiResponseContext.isError()).thenReturn(true);
        Mockito.when(fsapiResponseContext.getErrors()).thenReturn(getErrorList());
        Mockito.when(fsapiResponseContext.getContextProps()).thenReturn(contextProps);

        DefaultErrorHandlingExecutor errorHandlingExecutor = Mockito.spy(DefaultErrorHandlingExecutor.class);
        errorHandlingExecutor.preProcessResponse(fsapiResponseContext);
        verify(fsapiResponseContext, times(0)).setError(false);
    }

    @Test
    public void testPostResponseFlow() {

        FSAPIResponseContext fsapiResponseContext = Mockito.mock(FSAPIResponseContext.class);
        Mockito.when(fsapiResponseContext.isError()).thenReturn(true);
        Mockito.when(fsapiResponseContext.getErrors()).thenReturn(getErrorList());
        Mockito.when(fsapiResponseContext.getContextProps()).thenReturn(contextProps);

        DefaultErrorHandlingExecutor errorHandlingExecutor = Mockito.spy(DefaultErrorHandlingExecutor.class);
        errorHandlingExecutor.postProcessResponse(fsapiResponseContext);
        verify(fsapiResponseContext, times(0)).setError(false);
    }

    private ArrayList<FSExecutorError> getErrorList() {

        FSExecutorError error = new FSExecutorError("400", "Invalid Request",
                "Mandatory parameter is missing", "400");

        ArrayList<FSExecutorError> errors = new ArrayList<>();
        errors.add(error);
        return errors;
    }
}
