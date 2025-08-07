/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.authentication.endpoint.i18n;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Creates new resource bundle that supports UTF-8 encoding.
 */
public class UTF8Control extends ResourceBundle.Control {
    @Override
    public ResourceBundle newBundle(
            String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
            throws IOException {

        String bundleName = toBundleName(baseName, locale);
        String resourceName = toResourceName(bundleName, "properties");

        ResourceBundle bundle = null;
        InputStreamReader reader = null;
        URLConnection connection = null;

        if (reload) {
            URL url = loader.getResource(resourceName);
            if (url != null) {
                connection = url.openConnection();
                if (connection != null) {
                    connection.setUseCaches(false);
                    reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
                }
            }
        } else {
            if (loader.getResourceAsStream(resourceName) != null) {
                reader = new InputStreamReader(loader.getResourceAsStream(resourceName), StandardCharsets.UTF_8);
            }
        }

        if (reader != null) {
            try {
                bundle = new PropertyResourceBundle(reader);
            } finally {
                reader.close();
            }
        }

        return bundle;
    }
}
