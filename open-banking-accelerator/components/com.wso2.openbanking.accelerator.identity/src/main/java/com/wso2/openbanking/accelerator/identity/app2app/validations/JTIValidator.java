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
package com.wso2.openbanking.accelerator.identity.app2app.validations;

import com.wso2.openbanking.accelerator.identity.app2app.model.Secret;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateJTI;
import com.wso2.openbanking.accelerator.identity.cache.IdentityCache;
import com.wso2.openbanking.accelerator.identity.cache.IdentityCacheKey;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for validating the JWT ID.
 */
public class JTIValidator implements ConstraintValidator<ValidateJTI, Secret> {
    private static IdentityCache identityCache;

    @Override
    public boolean isValid(Secret secret, ConstraintValidatorContext constraintValidatorContext) {

        String jti = secret.getJti();
        return validateJTI(jti);

    }

    private boolean validateJTI(String jti) {

        IdentityCacheKey jtiCacheKey = new IdentityCacheKey(jti);

        if (getFromCache(jtiCacheKey) != null) {
            return false;
        }

        //adding to cache to prevent the value from being replayed again
        addToCache(jtiCacheKey, jti);
        return true;

    }

    private Object getFromCache(IdentityCacheKey identityCacheKey) {
        //Lazy initialization
        if (identityCache == null) {
            identityCache = new IdentityCache();
        }

        return identityCache.getFromCache(identityCacheKey);
    }

    private void addToCache(IdentityCacheKey identityCacheKey, Object value) {
        //Lazy initialization
        if (identityCache == null) {
            identityCache = new IdentityCache();
        }

        identityCache.addToCache(identityCacheKey, value);
    }
}
