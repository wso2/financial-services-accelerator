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

package com.wso2.openbanking.scp.webapp.model;

import com.wso2.openbanking.scp.webapp.util.Constants;

/**
 * CookieAttributes.
 * <p>
 * Represents cookie attributes such as path, maxAge, and httpOnly
 */
public class CookieAttributes {
    private boolean httpOnly;
    private int maxAge;
    private String path;
    private boolean secure;
    private String name;
    private String value;

    public CookieAttributes(String name, String value) {
        this(name, value, false, Constants.DEFAULT_COOKIE_MAX_AGE, 
            Constants.DEFAULT_BASE_PATH, true);
    }

    public CookieAttributes(String name, boolean httpOnly, int maxAge) {
        this(name, "", httpOnly, maxAge, Constants.DEFAULT_BASE_PATH, true);
    }

    public CookieAttributes(String name, String value, boolean httpOnly, int maxAge, String path, boolean secure) {
        this.name = name;
        this.value = value;
        this.httpOnly = httpOnly;
        this.secure = secure;
        this.maxAge = maxAge;
        this.path = path;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public boolean isSecure() {
        return this.secure;
    }

    public int getMaxAge() {
        return this.maxAge;
    }

    public String getPath() {
        return this.path;
    }

    public boolean isHttpOnly() {
        return this.httpOnly;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    @Override
    public String toString() {
        return "CookieAttributes {" + "httpOnly='" + this.httpOnly + '\'' + ", maxAge='" + this.maxAge + '\'' + 
            ", path='" + this.path + '\'' + ", secure='" + this.secure + '\'' + 
            ", name='" + this.name + '\'' + ", value='" + this.value + '\'' + '}';
    }
}
