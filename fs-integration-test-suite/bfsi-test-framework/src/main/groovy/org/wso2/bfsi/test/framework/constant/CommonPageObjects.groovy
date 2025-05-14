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

package org.wso2.bfsi.test.framework.constant

/**
 * Class for keep Page objects, Xpaths
 */
class CommonPageObjects {

    //Selenium Constants
    public static final String USERNAME_FIELD_ID = "usernameUserInput"
    public static final String USERNAME_FIELD_XPATH_AU_200 = "//form[@id=\"identifierForm\"]/div//input[@id=\"usernameUserInput\"]"
    public static final String PASSWORD_FIELD_ID = "password"
    public static final String ACCOUNT_SELECT_DROPDOWN_XPATH = "//*[@id=\"accselect\"]"
    public static final String AUTH_SIGNIN_XPATH = "//button[contains(text(),'Sign In')]"
    public static final String AUTH_SIGNIN_XPATH_AU_200 = "//input[@value=\"Next\"]"
    public static final String CONSENT_DENY_XPATH = "//input[@value='Deny']"
    public static final String CONSENT_APPROVE_SUBMIT_ID = "approve"
    public static final String IS_USERNAME_ID = "txtUserName"
    public static final String IS_PASSWORD_ID = "txtPassword"
    public static final String BTN_IS_SIGNING = "//input[@value='Sign-in']"
    public static final String BTN_DEVPORTAL_SIGNIN = "//span[contains(text(),'Sign-in')]"
    public static final String BTN_CONTINUE = "//button[contains(text(),'Continue')]"
    public static final String TAB_APPLICATIONS = "//span[contains(text(),'Applications')]"
    public static final String TBL_ROWS = "//tbody/tr"
    public static final String TAB_SUBSCRIPTIONS = "//p[text()='Subscriptions']"
    public static final String CCPORTAL_SIGNIN_XPATH = "//button[contains(text(),'Sign in')]"

    //Second Factor Authenticator
    public static final String LBL_SMSOTP_AUTHENTICATOR = "//h2[text()='Authenticating with SMSOTP']"
    public static final String TXT_OTP_CODE_ID = "OTPcode"
    public static final String BTN_AUTHENTICATE = "//input[@id='authenticate']"

}

