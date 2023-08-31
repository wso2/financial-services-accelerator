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

import React, { useState } from "react";
import { DisplayLocale, Locale, LocaleConfig } from "./types";

/**
 * A hook to return the supported locale values, and functions to update locale and get active locale.
 * 
 * @param config configuration props
 */
const useLocale = (config: LocaleConfig) => {
  const [activeLocale, setActiveLocale] = useState<Locale>();
  const [locales] = useState<Locale[]>(config.locales ?? []);

  const changeLocale = (localeCode: string): void => {
    // if passed locale is supported, return relevant locales json
    const locale = locales.find((l) => l.code === localeCode);
    if (locale) setActiveLocale(locale);
  };

  /**
   * If present return the active locale, else return default locale
   *
   * @returns {Locale} active locale object
   */
  const getActiveLocale = (): Locale => {
    // an active locale present, returns it
    if (activeLocale) return activeLocale;

    // looping all locales to find the default locale
    const locale = locales.find((locale: Locale) => locale.isDefault);
    if (locale) {
      setActiveLocale(locale);
      return locale;
    }

    console.warn("Couldn't find the default locale config");
    return locales[0];
  };

  const supportedLocales: DisplayLocale[] = locales.map((locale) => {
    return {
      value: locale.code,
      display: locale.display,
    };
  });

  return {
    getActiveLocale,
    changeLocale,
    supportedLocales,
  };
};

export default useLocale;
