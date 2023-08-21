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

package com.wso2.openbanking.accelerator.gateway.executor.impl.tpp.validation.executor;

import com.wso2.openbanking.accelerator.common.model.PSD2RoleEnum;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test for API TPP validation executor.
 */
public class APITPPValidationExecutorTest {

    private APITPPValidationExecutor apitppValidationExecutor;
    private Map<String, List<String>> allowedScopes;
    private SecurityRequirement securityRequirement;

    @BeforeClass
    public void init() {
        this.allowedScopes = new HashMap<>();
        allowedScopes.put("accounts", Arrays.asList("AISP", "PISP"));
        allowedScopes.put("payments", Collections.singletonList("PISP"));

        securityRequirement = new SecurityRequirement();
        securityRequirement.put("PSUOAuth2Security", Arrays.asList("accounts", "payments"));
        securityRequirement.put("default", Collections.singletonList("accounts"));

        this.apitppValidationExecutor = new APITPPValidationExecutor();
    }

    @Test(description = "when valid scopes provided, then requiredPSD2Roles list should contain roles")
    public void testGetRolesFromScopesWithValidScopes() throws Exception {
        Set<String> scopes = new HashSet<>();
        scopes.add("accounts");
        scopes.add("payments");

        List<PSD2RoleEnum> roleList = WhiteboxImpl.invokeMethod(this.apitppValidationExecutor,
                "getRolesFromScopes", allowedScopes, scopes);

        Assert.assertTrue(roleList.size() != 0);
        Assert.assertTrue(roleList.contains(PSD2RoleEnum.AISP));
    }

    @Test(description = "when invalid scopes provided, then requiredPSD2Roles list should be empty")
    public void testGetRolesFromScopesWithInvalidScopes() throws Exception {
        Set<String> scopes = new HashSet<>();
        scopes.add("default");

        List<PSD2RoleEnum> roleList = WhiteboxImpl.invokeMethod(this.apitppValidationExecutor,
                "getRolesFromScopes", allowedScopes, scopes);

        Assert.assertEquals(roleList.size(), 0);
    }

    @Test(description = "when security requirement provided for GET API, then set of scopes should return")
    public void testExtractScopesFromSwaggerAPIWithGet() throws Exception {
        Operation get = new Operation();
        get.setSecurity(Collections.singletonList(securityRequirement));

        PathItem pathItem = new PathItem();
        pathItem.setGet(get);

        Set<String> scopes = WhiteboxImpl.invokeMethod(this.apitppValidationExecutor,
                "extractScopesFromSwaggerAPI", pathItem, "GET");

        Assert.assertTrue(scopes.size() != 0);
        Assert.assertTrue(scopes.contains("accounts"));
    }

    @Test(description = "when security requirement provided for POST API, then set of scopes should return")
    public void testExtractScopesFromSwaggerAPIWithPost() throws Exception {
        Operation post = new Operation();
        post.setSecurity(Collections.singletonList(securityRequirement));

        PathItem pathItem = new PathItem();
        pathItem.setPost(post);

        Set<String> scopes = WhiteboxImpl.invokeMethod(this.apitppValidationExecutor,
                "extractScopesFromSwaggerAPI", pathItem, "POST");

        Assert.assertTrue(scopes.size() != 0);
        Assert.assertTrue(scopes.contains("accounts"));
    }

    @Test(description = "when security requirement provided for PUT API, then set of scopes should return")
    public void testExtractScopesFromSwaggerAPIWithPut() throws Exception {
        Operation put = new Operation();
        put.setSecurity(Collections.singletonList(securityRequirement));

        PathItem pathItem = new PathItem();
        pathItem.setPut(put);

        Set<String> scopes = WhiteboxImpl.invokeMethod(this.apitppValidationExecutor,
                "extractScopesFromSwaggerAPI", pathItem, "PUT");

        Assert.assertTrue(scopes.size() != 0);
        Assert.assertTrue(scopes.contains("accounts"));
    }

    @Test(description = "when security requirement provided for PATCH API, then set of scopes should return")
    public void testExtractScopesFromSwaggerAPIWithPatch() throws Exception {
        Operation patch = new Operation();
        patch.setSecurity(Collections.singletonList(securityRequirement));

        PathItem pathItem = new PathItem();
        pathItem.setPatch(patch);

        Set<String> scopes = WhiteboxImpl.invokeMethod(this.apitppValidationExecutor,
                "extractScopesFromSwaggerAPI", pathItem, "PATCH");

        Assert.assertTrue(scopes.size() != 0);
        Assert.assertTrue(scopes.contains("accounts"));
    }

    @Test(description = "when security requirement provided for DELETE API, then set of scopes should return")
    public void testExtractScopesFromSwaggerAPIWithDelete() throws Exception {
        Operation delete = new Operation();
        delete.setSecurity(Collections.singletonList(securityRequirement));

        PathItem pathItem = new PathItem();
        pathItem.setDelete(delete);

        Set<String> scopes = WhiteboxImpl.invokeMethod(this.apitppValidationExecutor,
                "extractScopesFromSwaggerAPI", pathItem, "DELETE");

        Assert.assertTrue(scopes.size() != 0);
        Assert.assertTrue(scopes.contains("accounts"));
    }

}
