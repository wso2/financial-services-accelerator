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

import {persistConsent} from "../api/ConsentAPI.js";

// Generate the table of contents
export const content = (id) => {
    const anchors = document.querySelectorAll(`${id} h2, h3`);
    let list = document.createElement('ul');

    Array.prototype.forEach.call(anchors, function (anchor, index) {
        let listItem = document.createElement('li');
        let anchorLink = document.createElement('a');
        anchorLink.href = '#' + anchor.id;
        anchorLink.innerHTML = anchor.innerHTML;
        if (anchor.tagName === 'H3') {
            listItem.className = 'sub-item';
        }
        listItem.appendChild(anchorLink);
        list.appendChild(listItem);
    });

    document.getElementById('table-of-contents').prepend(list);
}

// Invoke the API call for persisting user consent
export const handleConfirm = async (data) => {
    try {
        await persistConsent(JSON.stringify(data));
    } catch (err) {
        console.log(err);
    }
}
