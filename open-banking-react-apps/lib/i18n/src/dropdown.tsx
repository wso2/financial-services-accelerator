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

import React from 'react'
import { DisplayLocale, DropdownProps } from './types';

/**
 * Renders locale options as a dropdown.
 */
const Dropdown = ({ value, onChange, options }: DropdownProps): React.JSX.Element => {
    return (
        <select value={value} onChange={e => onChange(e.target.value)} >
            {options && options.map((option: DisplayLocale, index: number) => {
                return <option key={index} value={option.value}>
                    &#127760; &#160; {option.display}
                </option>
            })}
        </select>
    )
}

export default Dropdown;
