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

package org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.openidconnect.model.RequestObject;
import org.wso2.carbon.identity.openidconnect.model.RequestedClaim;
import org.wso2.financial.services.accelerator.identity.extensions.validator.annotation.RequiredParameter;
import org.wso2.financial.services.accelerator.identity.extensions.validator.annotation.RequiredParameters;
import org.wso2.financial.services.accelerator.identity.extensions.validator.annotation.ValidScopeFormat;

import java.util.List;
import java.util.Map;

/**
 * A decorator class of RequestObject to enforce validations. Can add delegate methods as required.
 *
 * @param <T> Any child of this class.
 */
@RequiredParameters({
        @RequiredParameter(param = "signedJWT",
                message = "Only Signed JWS is accepted for request object"),
        @RequiredParameter(param = "claimsSet.claims.aud",
                message = "aud parameter is missing in the request object")
})
@ValidScopeFormat(scope = "claimsSet.claims.scope")
public class FSRequestObject<T extends FSRequestObject> extends RequestObject {

    // decorator object
    private RequestObject requestObject;
    private static final long serialVersionUID = -568639546792395972L;

    public FSRequestObject(RequestObject requestObject) throws RequestObjectException {
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
    public FSRequestObject(T childObject) {
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
