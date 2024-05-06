package com.wso2.openbanking.accelerator.identity.app2app.exception;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;

/**
 * Secret Object Validation Exception
 */
public class SecretValidationException extends OpenBankingException {


    private static final long serialVersionUID = -2572459527308720228L;

    public SecretValidationException(String message) {
        super(message);
    }

    public SecretValidationException(String message, Throwable e) {
        super(message, e);
    }
}