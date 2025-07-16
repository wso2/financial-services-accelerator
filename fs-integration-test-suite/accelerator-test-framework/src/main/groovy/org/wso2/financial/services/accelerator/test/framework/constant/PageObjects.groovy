/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.test.framework.constant

import org.wso2.bfsi.test.framework.constant.CommonPageObjects

/**
 * Class for keeping Accelerator Page Objects.
 */
class PageObjects extends CommonPageObjects{

    //Dev portal page objects
    public static final String TAB_APPLICATIONS = "//span[contains(text(),'Applications')]"
    public static final String TBL_ROWS = "//tbody/tr"
    public static final String TAB_SUBSCRIPTIONS = "//p[text()='Subscriptions']"
    public static final String TAB_PRODUCTION_KEY = "//p[text()='Production Keys']"
    public static final String CHK_PASSWORD = "//input[@id='password']"

    //Identity page objects
    public static final String LNK_SP_LIST =
            "//li[text()='Service Providers']/following-sibling::li//a[contains(@href,'list_service_providers_menu')]"
    public static final String ELE_NEW_SP_NAME = "//table[@id='ServiceProviders']/tbody/tr[1]/td[1]"
    public static final String BTN_EDIT_SP = "//table[@id='ServiceProviders']/tbody/tr[1]/td[1]/following-sibling::td[2]/a[contains(@title,'Edit Service Providers')]"
    public static final String TAB_INBOUND_AUTH_CONFIGURATION = "//a[text()='Inbound Authentication Configuration']"
    public static final String TAB_OPENID_CONNECT_CONFIGURATION = "//a[text()='OAuth/OpenID Connect Configuration']"
    public static final String BTN_EDIT_OPENID_CONNECT = "//a[@title='Edit Service Providers']"
    public static final String CHK_PASSWORD_GRANT = "//input[@id='grant_password']"

    //Auth Flow page objects
    public static final String CHK_SALARY_SAVER_ACC = "//input[@value='Salary Saver Account']"
    public static final String BTN_SIGN_IN = "//button[contains(text(),'Sign In')]";
    public static final String BTN_APPROVE = "//input[@id='approve']"
    public static final String BTN_CONFIRM = "//input[@value='Confirm']"
    public static final String BTN_DENY = "//input[@value='Deny']"
    public static final String LBL_SMSOTP_AUTHENTICATOR = "//h2[text()='Authenticating with SMSOTP']"
    public static final String BTN_AUTHENTICATE = "//input[@id='authenticate']"
    public static final String LBL_INCORRECT_USERNAME = "//div[@id='error-msg']"
    public static final String AUTH_CONTINUE_XPATH = "//input[@value=\"Continue\"]"
    public static final String ACCOUNTS_PAGE_TXT = "//h5[contains(@class,'ui body')]"
    static final String PAYMENTS_SELECT_XPATH = """//option[@value="Salary Saver Account"]"""
    public static final String LBL_REDIRECT_ERROR = "//h3/following-sibling::p"
    static final String SUBMIT_XPATH = """//*[@id="approve"]"""

    //APIM Console
    static final String CONSOLE_USERNAME = """//input[@id="txtUserName"]"""
    static final String CONSOLE_PASSWORD = """//input[@id="txtPassword"]"""
    static final String BTN_CONSOLE_SIGN_IN = """//input[@value="Sign-in"]"""
    static final String BTN_ADD_USERS_ROLES = """//a[contains(@href,'user_mgt_menu_add')]"""
    static final String BTN_ADD_NEW_ROLE = """//a[text()="Add New Role"]"""
    static final String DD_DOMAIN = """//select[@id="domain"]"""
    static final String TXT_ROLE_NAME = """//input[@name="roleName"]"""
    static final String BTN_FINISH = """//input[@value="Finish"]"""

    //APIM Devportal
    static final String ID_DEVPORTAL_SIGN_IN = "itest-devportal-sign-in"
    static final String TXT_DEVPORTAL_USERNAME = "username"
    static final String TXT_DEVPORTAL_PASSWORD = "password"
    static final String BTN_DEVPORTAL_CONTINUE = """//button[contains(text(),"Continue")]"""
    static final String BTN_ADD_NEW_APPLICATION = """//button[contains(text(),"Add New Application")]"""
    static final String TXT_APPLICATION_NAME = "application-name"
    static final String DD_APP_TOKEN_QUOTA = "per-token-quota"
    static final String BTN_CREATE_APP_SAVE = "itest-application-create-save"
    static final String TAB_SANDBOX_KEYS = "sandbox-keys"
    static final String CHK_REFRESH_TOKEN = "refresh_token"
    static final String CHK_CODE = "authorization_code"
    static final String TXT_REDIRECT_URL = "callbackURL"
    static final String DD_REGULATORY_TYPE = "regulatory"
    static final String BTN_GENERATE_KEYS = "generate-keys"
    static final String TXT_SP_CERTIFICATE = "sp_certificate"
    static final String TXT_CONSUMER_KEY = """//input[@id="consumer-key"]"""
    static final String TXT_CONSUMER_SECRET = """//input[@id="consumer-secret"]"""

}
