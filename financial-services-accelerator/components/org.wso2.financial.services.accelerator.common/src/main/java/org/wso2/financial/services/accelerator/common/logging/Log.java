/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
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

package org.wso2.financial.services.accelerator.common.logging;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * CRLF-safe logger. Strips CR ({@code \r}) and LF ({@code \n}) from every message
 * before writing to the underlying Commons Logging logger, preventing log injection
 * (CWE-93). {@code null} messages are logged as the string {@code "null"}.
 *
 * <p>Drop-in replacement for {@code org.apache.commons.logging.Log} — change only
 * the import; all existing call sites work unchanged. Use {@code Supplier<String>}
 * overloads for {@code trace}/{@code debug} to skip string concatenation when those
 * levels are disabled.
 */
public final class Log {

    private static final String CR = "\r";
    private static final String LF = "\n";
    private static final String ESCAPED_CR = "\\r";
    private static final String ESCAPED_LF = "\\n";

    private final org.apache.commons.logging.Log delegate;

    Log(org.apache.commons.logging.Log delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns {@code true} if trace level is enabled.
     */
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    /**
     * Returns {@code true} if debug level is enabled.
     */
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    /**
     * Returns {@code true} if info level is enabled.
     */
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    /**
     * Returns {@code true} if warn level is enabled.
     */
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    /**
     * Returns {@code true} if error level is enabled.
     */
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    /**
     * Logs a constant trace message. No-op if trace is disabled.
     * For messages with variable data use {@link #trace(Supplier)}.
     *
     * @param message the message to log
     */
    public void trace(String message) {
        if (delegate.isTraceEnabled()) {
            delegate.trace(String.valueOf(message).replace(CR, ESCAPED_CR).replace(LF, ESCAPED_LF));
        }
    }

    /**
     * Logs a trace message from {@code messageSupplier}. The supplier is only invoked
     * if trace is enabled, avoiding string concatenation for suppressed levels.
     *
     * @param messageSupplier supplier of the message; must not be {@code null}
     */
    public void trace(Supplier<String> messageSupplier) {
        Objects.requireNonNull(messageSupplier, "messageSupplier");
        if (delegate.isTraceEnabled()) {
            delegate.trace(String.valueOf(messageSupplier.get()).replace(CR, ESCAPED_CR).replace(LF, ESCAPED_LF));
        }
    }

    /**
     * Logs a constant debug message. No-op if debug is disabled.
     * For messages with variable data use {@link #debug(Supplier)}.
     *
     * @param message the message to log
     */
    public void debug(String message) {
        if (delegate.isDebugEnabled()) {
            delegate.debug(String.valueOf(message).replace(CR, ESCAPED_CR).replace(LF, ESCAPED_LF));
        }
    }

    public void debug(String message, Throwable t) {
        if (delegate.isDebugEnabled()) {
            delegate.debug(String.valueOf(message).replace(CR, ESCAPED_CR).replace(LF, ESCAPED_LF), t);
        }
    }

    /**
     * Logs a debug message from {@code messageSupplier}. The supplier is only invoked
     * if debug is enabled, avoiding string concatenation for suppressed levels.
     *
     * @param messageSupplier supplier of the message; must not be {@code null}
     */
    public void debug(Supplier<String> messageSupplier) {
        Objects.requireNonNull(messageSupplier, "messageSupplier");
        if (delegate.isDebugEnabled()) {
            delegate.debug(String.valueOf(messageSupplier.get()).replace(CR, ESCAPED_CR).replace(LF, ESCAPED_LF));
        }
    }

    public void debug(Supplier<String> messageSupplier, Throwable t) {
        Objects.requireNonNull(messageSupplier, "messageSupplier");
        if (delegate.isDebugEnabled()) {
            delegate.debug(String.valueOf(messageSupplier.get()).replace(CR, ESCAPED_CR).replace(LF, ESCAPED_LF), t);
        }
    }

    /**
     * Logs an info message.
     *
     * @param message the message to log
     */
    public void info(String message) {
        delegate.info(String.valueOf(message).replace(CR, ESCAPED_CR).replace(LF, ESCAPED_LF));
    }

    /**
     * Logs a warn message.
     *
     * @param message the message to log
     */
    public void warn(String message) {
        delegate.warn(String.valueOf(message).replace(CR, ESCAPED_CR).replace(LF, ESCAPED_LF));
    }

    /**
     * Logs a warn message with a throwable.
     *
     * @param message the message to log
     * @param t       the throwable; must not be {@code null}
     */
    public void warn(String message, Throwable t) {
        Objects.requireNonNull(t, "t");
        delegate.warn(String.valueOf(message).replace(CR, ESCAPED_CR).replace(LF, ESCAPED_LF), t);
    }

    /**
     * Logs an error message.
     *
     * @param message the message to log
     */
    public void error(String message) {
        delegate.error(String.valueOf(message).replace(CR, ESCAPED_CR).replace(LF, ESCAPED_LF));
    }

    /**
     * Logs an error message with a throwable.
     *
     * @param message the message to log
     * @param t       the throwable; must not be {@code null}
     */
    public void error(String message, Throwable t) {
        Objects.requireNonNull(t, "t");
        delegate.error(String.valueOf(message).replace(CR, ESCAPED_CR).replace(LF, ESCAPED_LF), t);
    }
}
