/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model;

import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;

import java.util.List;

/**
 * Model for amended resources.
 */
public class AmendedResources {

    List<AuthorizationResource> amendedAuthResources;
    List<ConsentMappingResource> newMappingResources;
    List<ConsentMappingResource> amendedMappingResources;

    public List<AuthorizationResource> getAmendedAuthResources() {
        return amendedAuthResources;
    }

    public void setAmendedAuthResources(
            List<AuthorizationResource> amendedAuthResources) {
        this.amendedAuthResources = amendedAuthResources;
    }

    public List<ConsentMappingResource> getNewMappingResources() {
        return newMappingResources;
    }

    public void setNewMappingResources(
            List<ConsentMappingResource> newMappingResources) {
        this.newMappingResources = newMappingResources;
    }

    public List<ConsentMappingResource> getAmendedMappingResources() {
        return amendedMappingResources;
    }

    public void setAmendedMappingResources(
            List<ConsentMappingResource> amendedMappingResources) {
        this.amendedMappingResources = amendedMappingResources;
    }
}
