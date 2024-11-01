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

package com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.models;

import com.wso2.openbanking.accelerator.common.validator.annotation.RequiredParameter;
import com.wso2.openbanking.accelerator.common.validator.annotation.RequiredParameters;
import com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.annotations.ValidExpiration;
import com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.annotations.ValidNbfExpClaims;

/**
 * Model class for FAPI request object.
 */
@RequiredParameters({
        @RequiredParameter(param = "claimsSet.claims.redirect_uri",
                message = "Mandatory parameter redirect_uri, not found in the request"),
        @RequiredParameter(param = "claimsSet.claims.nonce",
                message = "nonce parameter is missing in the request object"),
        @RequiredParameter(param = "claimsSet.claims.exp",
                message = "exp parameter is missing in the request object")
})
@ValidExpiration(expiration = "claimsSet.claims.exp")
@ValidNbfExpClaims(notBefore = "claimsSet.claims.nbf", expiration = "claimsSet.claims.exp")
public class FapiRequestObject extends OBRequestObject {

    private static final long serialVersionUID = -83973857804232423L;

    public FapiRequestObject(OBRequestObject obRequestObject) {

        super(obRequestObject);
    }
}
