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

window.env = {
    // This option can be retrieved in "src/index.js" with "window.env.API_URL".
    // If the consent manager is deployed in a different server than the IS, USE_DEFAULT_CONFIGS must be set to false
    // and the SERVER_URL needs to be configured.
    USE_DEFAULT_CONFIGS: true,
    SERVER_URL: 'https://localhost:9446',
    TENANT_DOMAIN: 'carbon.super',
    NUMBER_OF_CONSENTS: 20,
    VERSION: '3.0.0',
    IS_DEV_TOOLS_ENABLE: false
};

