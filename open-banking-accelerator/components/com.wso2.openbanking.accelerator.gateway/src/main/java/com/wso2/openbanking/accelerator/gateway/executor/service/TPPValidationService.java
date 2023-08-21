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
package com.wso2.openbanking.accelerator.gateway.executor.service;

import com.wso2.openbanking.accelerator.common.exception.TPPValidationException;
import com.wso2.openbanking.accelerator.common.model.PSD2RoleEnum;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

/**
 * Manager interface to be used for TPP validation using external services.
 */
public interface TPPValidationService {

    /**
     * Validate the status of a TPP.
     *
     * @param peerCertificate   Certificate of the TPP
     * @param requiredPSD2Roles Roles that are required to be validated with the TPP validation service according to
     *                          the current flow
     * @param metadata          Metadata information
     * @return
     * @throws TPPValidationException
     */
    boolean validate(X509Certificate peerCertificate,
                     List<PSD2RoleEnum> requiredPSD2Roles, Map<String, Object> metadata)
            throws TPPValidationException;

    /**
     * Get the cache key used for the caching the response. Implementation should return an appropriate ID that is
     * unique to the API flow.
     *
     * @param peerCertificate   Certificate of the TPP
     * @param requiredPSD2Roles Roles that are required to be validated with the TPP validation service according to
     *                          the current flow
     * @param metadata          Metadata information
     * @return
     * @throws TPPValidationException
     */
    String getCacheKey(X509Certificate peerCertificate,
                       List<PSD2RoleEnum> requiredPSD2Roles, Map<String, Object> metadata)
            throws TPPValidationException;
}
