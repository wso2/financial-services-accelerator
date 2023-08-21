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
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.gateway.executor.util.TestValidationUtil;
import net.minidev.json.JSONObject;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test for DCR TPP validation executor.
 */
@PowerMockIgnore({"org.mockito.*", "javax.script.*"})
public class DCRTPPValidationExecutorTest {

    private static final String BODY = "body";
    private static final String SOFTWARE_STATEMENT = "software_statement";

    private DCRTPPValidationExecutor dcrtppValidationExecutor;

    @BeforeClass
    public void init() {
        this.dcrtppValidationExecutor = new DCRTPPValidationExecutor();
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test(description = "when valid SSA provided, then return extracted software roles as string")
    public void testGetRolesFromSsaAsString() throws Exception {
        JSONObject requestBody = JWTUtils.decodeRequestJWT(TestValidationUtil.REQUEST_BODY_WITH_SSA, BODY);
        String expectedSSA = requestBody.getAsString(SOFTWARE_STATEMENT);

        String actualSSA = WhiteboxImpl.invokeMethod(this.dcrtppValidationExecutor,
                "getSSAFromPayload", TestValidationUtil.REQUEST_BODY_WITH_SSA);

        Assert.assertEquals(actualSSA, expectedSSA);
    }

    @Test(description = "when valid software statement provided, then requiredPSD2Roles list should return")
    public void testGetRolesFromSSAWithValidSSA() throws Exception {
        JSONObject requestBody = JWTUtils.decodeRequestJWT(TestValidationUtil.REQUEST_BODY_WITH_SSA, BODY);
        // extract software statement
        String softwareStatement = requestBody.getAsString(SOFTWARE_STATEMENT);

        List<PSD2RoleEnum> rolesFromSSA = this.dcrtppValidationExecutor.getRolesFromSSA(softwareStatement);

        Assert.assertEquals(rolesFromSSA.size(), 2);
        Assert.assertTrue(rolesFromSSA.contains(PSD2RoleEnum.AISP));
    }

    @Test(description = "when invalid software roles provided, then empty requiredPSD2Roles list should return")
    public void testGetRolesFromSSAWithInvalidRoles() throws Exception {

        JSONObject requestBody = JWTUtils.decodeRequestJWT(TestValidationUtil.REQUEST_BODY_WITH_SSA_SINGLE_ROLE, BODY);
        // extract software statement
        String softwareStatement = requestBody.getAsString(SOFTWARE_STATEMENT);
        List<PSD2RoleEnum> rolesFromSSA = this.dcrtppValidationExecutor.getRolesFromSSA(softwareStatement);

        Assert.assertTrue(rolesFromSSA.isEmpty());
    }

}
