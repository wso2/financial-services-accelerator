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

package com.wso2.openbanking.accelerator.identity.auth.extensions.adaptive.function;

import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.json.JSONObject;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.util.HashMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit test class for OpenBankingAuthenticationWorkerFunctionImpl class.
 */
public class OpenBankingAuthenticationWorkerFunctionImplTest {

    private JSONObject customJSON;
    private OpenBankingAuthenticationWorkerFunction workerFunction;
    private AuthenticationContext authenticationContext;
    private JsAuthenticationContext jsAuthenticationContext;

    @BeforeTest
    void beforeClass() {

        customJSON = new JSONObject();
        customJSON.put("custom", "object");
        IdentityExtensionsDataHolder.getInstance().addWorker((context, properties) ->
                customJSON, "customHandlerName");
        workerFunction = new OpenBankingAuthenticationWorkerFunctionImpl();
        authenticationContext = new AuthenticationContext();
        jsAuthenticationContext = new JsAuthenticationContext(authenticationContext);
    }

    @Test
    public void testHandleInvokeWithExistingWorker() {

        assertEquals(workerFunction.handleRequest(jsAuthenticationContext,
                new HashMap<>(), "customHandlerName"), customJSON);
    }

    @Test
    public void testHandleInvokeWithInvalidWorker() {

        JSONObject jsonObject = workerFunction.handleRequest(jsAuthenticationContext,
                new HashMap<>(), "invalidHandlerName");
        assertTrue(jsonObject.has("Error"));
    }
}
