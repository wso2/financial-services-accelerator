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

import React from "react";
import { IntlProvider } from "react-intl";

import { LocaleProviderProps } from "./types";

/**
 * This component is used to setup the i18n context for a tree. 
 * Usually, this component will wrap an app's root component so that the entire app will be within the 
 * configured i18n context. 
 * 
 * @example
 * ```
 * import { LocaleProvider, useLocale } from "@bfsi-react/i18n";
 * 
 * const { getActiveLocale } = useLocale(localeConfig);
 * const activeLocale = getActiveLocale();
 * 
 * <LocaleProvider locale={activeLocale}>
 *      <div className="App"></div>
 * </LocaleProvider>
 * ```
 * @see https://formatjs.io/docs/react-intl/components#intlprovider
 */
const LocaleProvider = ({ children, locale, ...rest }: LocaleProviderProps): React.JSX.Element => {
    return (
        <IntlProvider locale={locale.code} messages={locale.messages} {...rest}>
            {children}
        </IntlProvider>
    );
};

export default LocaleProvider;
