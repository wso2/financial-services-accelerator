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

package org.wso2.financial.services.accelerator.test.framework.constant

/**
 * Class for keeping Accelerator Page Objects.
 */
class PageObjects {

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
    public static final String CHK_SALARY_SAVER_ACC = "//input[@id='Salary Saver Account']"
    public static final String BTN_SIGN_IN = "//button[contains(text(),'Sign In')]";
    public static final String BTN_APPROVE = "//input[@id='approve']"
    public static final String BTN_CONFIRM = "//input[@value='Confirm']"
    public static final String BTN_DENY = "//input[@value='Deny']"
    public static final String LBL_SMSOTP_AUTHENTICATOR = "//h2[text()='Authenticating with SMSOTP']"
    public static final String BTN_AUTHENTICATE = "//input[@id='authenticate']"
    public static final String LBL_INCORRECT_USERNAME = "//div[@id='error-msg']"
    public static final String AUTH_CONTINUE_XPATH = "//input[@value=\"Continue\"]"
    public static final String ACCOUNTS_PAGE_TXT = "//h5[contains(@class,'ui body')]"
    static final String PAYMENTS_SELECT_XPATH = """//option[@value="12345"]"""
    public static final String LBL_REDIRECT_ERROR = "//h3/following-sibling::p"
}
