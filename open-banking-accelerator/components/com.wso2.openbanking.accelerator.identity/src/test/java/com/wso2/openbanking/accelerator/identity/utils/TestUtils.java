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

package com.wso2.openbanking.accelerator.identity.utils;

import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.models.FapiRequestObject;
import com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.models.OBRequestObject;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.openidconnect.model.RequestObject;

import java.text.ParseException;

/**
 * Test utils class.
 */
public class TestUtils {

    /**
     * Get OB request object.
     *
     * @param request request
     * @return OBRequestObject
     * @throws ParseException
     * @throws RequestObjectException
     */
    public static OBRequestObject<?> getObRequestObject(String request) throws ParseException, RequestObjectException {
        RequestObject requestObject = new RequestObject();
        JOSEObject jwt = JOSEObject.parse(request);
        if (jwt.getHeader().getAlgorithm() == null || jwt.getHeader().getAlgorithm().equals(JWSAlgorithm.NONE)) {
            requestObject.setPlainJWT(PlainJWT.parse(request));
        } else {
            requestObject.setSignedJWT(SignedJWT.parse(request));
        }
        return new OBRequestObject<>(requestObject);
    }

    /**
     * Get FAPI request object.
     *
     * @param request request
     * @return FapiRequestObject
     * @throws ParseException
     * @throws RequestObjectException
     */
    public static FapiRequestObject getFapiRequestObject(String request) throws ParseException,
            RequestObjectException {
        OBRequestObject<?> requestObject = getObRequestObject(request);
        return new FapiRequestObject(requestObject);
    }

}
