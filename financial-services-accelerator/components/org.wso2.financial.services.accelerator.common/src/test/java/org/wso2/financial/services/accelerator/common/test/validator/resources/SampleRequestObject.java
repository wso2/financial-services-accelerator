/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
package org.wso2.financial.services.accelerator.common.test.validator.resources;

import com.nimbusds.jwt.JWTClaimsSet;
import org.wso2.financial.services.accelerator.common.validator.annotation.ValidScopeFormat;
import org.wso2.financial.services.accelerator.common.validator.validationgroups.AttributeChecks;

/**
 * Sample request object resource.
 */
@ValidScopeFormat(scope = "claimsSet.claims.scope", message = "Non Confirming Scope", groups = AttributeChecks.class)
public class SampleRequestObject {

    private JWTClaimsSet claimsSet;

    public SampleRequestObject() {
    }

    public JWTClaimsSet getClaimsSet() {
        return claimsSet;
    }

    public void setClaimSet(JWTClaimsSet claimsSet) {
        this.claimsSet = claimsSet;
    }

    public void setClaimsSet(JWTClaimsSet claimsSet) {
        this.claimsSet = claimsSet;
    }
}
