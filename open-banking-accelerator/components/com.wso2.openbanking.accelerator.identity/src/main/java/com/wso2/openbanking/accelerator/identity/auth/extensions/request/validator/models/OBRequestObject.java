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

package com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.models;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.validator.annotation.RequiredParameter;
import com.wso2.openbanking.accelerator.common.validator.annotation.RequiredParameters;
import com.wso2.openbanking.accelerator.common.validator.annotation.ValidAudience;
import com.wso2.openbanking.accelerator.common.validator.annotation.ValidScopeFormat;
import com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.annotations.ValidSigningAlgorithm;
import com.wso2.openbanking.accelerator.identity.common.annotations.validationgroups.AttributeChecks;
import com.wso2.openbanking.accelerator.identity.common.annotations.validationgroups.MandatoryChecks;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.openidconnect.model.RequestObject;
import org.wso2.carbon.identity.openidconnect.model.RequestedClaim;

import java.util.List;
import java.util.Map;

/**
 * A decorator class of RequestObject to enforce validations. Can add delegate methods as required.
 *
 * @param <T> Any child of this class.
 */
@RequiredParameters({
        @RequiredParameter(param = "signedJWT",
                message = "Only Signed JWS is accepted for request object", groups = MandatoryChecks.class),
        @RequiredParameter(param = "claimsSet.claims.aud",
                message = "aud parameter is missing in the request object", groups = MandatoryChecks.class)
})
@ValidScopeFormat(scope = "claimsSet.claims.scope", groups = AttributeChecks.class)
@ValidAudience(audience = "claimsSet.claims.aud", clientId = "claimsSet.claims.client_id",
        groups = AttributeChecks.class)
@ValidSigningAlgorithm(algorithm = "signedJWT.header.algorithm.name", clientId = "claimsSet.claims.client_id",
        groups = AttributeChecks.class)
public class OBRequestObject<T extends OBRequestObject> extends RequestObject {

    // decorator object
    private RequestObject requestObject;
    private static final long serialVersionUID = -568639546792395972L;

    public OBRequestObject(RequestObject requestObject) throws RequestObjectException {
        if (requestObject == null) {
            throw new RequestObjectException(RequestObjectException.ERROR_CODE_INVALID_REQUEST,
                    "Null request object passed");
        }
        this.requestObject = requestObject;
    }


    // for tool kits to use

    /**
     * Any child object of this class can create object of this class using this constructor.
     *
     * @param childObject Any class extending this class.
     */
    public OBRequestObject(T childObject) {
        this.requestObject = childObject;
    }


    // delegations
    @Override
    public SignedJWT getSignedJWT() {
        return requestObject.getSignedJWT();
    }

    @Override
    public JWTClaimsSet getClaimsSet() {
        return requestObject.getClaimsSet();
    }

    @Override
    public boolean isSignatureValid() {
        return requestObject.isSignatureValid();
    }

    @Override
    public void setIsSignatureValid(boolean isSignatureValid) {
        requestObject.setIsSignatureValid(isSignatureValid);
    }

    @Override
    public boolean isSigned() {
        return requestObject.isSigned();
    }

    @Override
    public PlainJWT getPlainJWT() {
        return requestObject.getPlainJWT();
    }

    @Override
    public void setPlainJWT(PlainJWT plainJWT) throws RequestObjectException {
        requestObject.setPlainJWT(plainJWT);
    }

    @Override
    public Map<String, List<RequestedClaim>> getRequestedClaims() {
        return requestObject.getRequestedClaims();
    }

    @Override
    public void setRequestedClaims(Map<String, List<RequestedClaim>> claimsforRequestParameter) {
        requestObject.setRequestedClaims(claimsforRequestParameter);
    }

    @Override
    public void setSignedJWT(SignedJWT signedJWT) throws RequestObjectException {
        requestObject.setSignedJWT(signedJWT);
    }

    @Override
    public void setClaimSet(JWTClaimsSet claimSet) {
        requestObject.setClaimSet(claimSet);
    }

    @Override
    public String getClaimValue(String claimName) {
        return requestObject.getClaimValue(claimName);
    }

    @Override
    public Object getClaim(String claimName) {
        return requestObject.getClaim(claimName);
    }
}
