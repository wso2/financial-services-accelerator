package com.wso2.openbanking.accelerator.identity.app2app.validations;

import com.wso2.openbanking.accelerator.identity.app2app.model.Secret;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateJTI;
import com.wso2.openbanking.accelerator.identity.cache.IdentityCache;
import com.wso2.openbanking.accelerator.identity.cache.IdentityCacheKey;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class JTIValidator implements ConstraintValidator<ValidateJTI, Secret> {
    private static IdentityCache identityCache;
    @Override
    public boolean isValid(Secret secret, ConstraintValidatorContext constraintValidatorContext) {
        String jti = secret.getJti();
        return validateJTI(jti);

    }

    private boolean validateJTI(String jti){
        IdentityCacheKey jtiCacheKey = new IdentityCacheKey(jti);
        if (getFromCache(jtiCacheKey) != null){
            return false;
        }
        addToCache(jtiCacheKey,jti);
        return true;

    }

    private Object getFromCache(IdentityCacheKey identityCacheKey){
        if (identityCache == null){
            identityCache = new IdentityCache();
        }
        return identityCache.getFromCache(identityCacheKey);
    }

    private void addToCache(IdentityCacheKey identityCacheKey, Object value){
        if (identityCache == null){
            identityCache = new IdentityCache();
        }
        identityCache.addToCache(identityCacheKey,value);
    }
}
