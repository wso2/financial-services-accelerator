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

package org.wso2.financial.services.accelerator.common.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.util.CarbonUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Common class to read a text file.
 */
public class TextFileReader {

    private String directoryPath;
    private final Map<String, String> files = new HashMap<>();
    private static volatile TextFileReader textFileReader;
    private static final Log logger = LogFactory.getLog(TextFileReader.class);
    private static final Object lock = new Object();

    private TextFileReader() {

    }

    public static TextFileReader getInstance() {

        synchronized (lock) {
            if (textFileReader == null) {
                textFileReader = new TextFileReader();
            }
        }
        return textFileReader;
    }

    public String getDirectoryPath() {

        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {

        this.directoryPath = directoryPath;
    }

    /**
     * To read the auth textFile from the given file path.
     *
     * @param fileName Path of the file.
     * @return file content as a String
     * @throws IOException IO Exception.
     */
    @SuppressFBWarnings({"WEAK_FILENAMEUTILS", "PATH_TRAVERSAL_IN"})
    // Suppressed content - FilenameUtils.getName()
    // Suppression reason -
    //      WEAK_FILENAMEUTILS - False positive: The vulnerability is fixed from Java 7 update 40 and Java 8+ versions
    //      PATH_TRAVERSAL_IN  - False positive: The user input value is only filename and it is secured using
    //                                           FilenameUtils. This could be a true positive if directory path is sent
    //                                           as a user input in the future.
    // Suppressed warning count - 2
    public String readFile(String fileName) throws IOException {

        String filePath;
        if (files.containsKey(fileName)) {
            return files.get(FilenameUtils.getName(fileName));
        }
        if (StringUtils.isNotEmpty(directoryPath)) {
            filePath = directoryPath + File.separator + FilenameUtils.getName(fileName);
        } else {
            filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + FilenameUtils.getName(fileName);
        }
        File file = new File(filePath);
        if (file.exists()) {
            try (InputStream resourceAsStream = Files.newInputStream(Paths.get(filePath));
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(resourceAsStream)) {

                StringBuilder resourceFile = new StringBuilder();
                int c;
                while ((c = bufferedInputStream.read()) != -1) {
                    char val = (char) c;
                    resourceFile.append(val);
                }
                files.put(fileName, resourceFile.toString());
                if (logger.isDebugEnabled()) {
                    logger.debug("File " + fileName.replaceAll("[\r\n]", "") + "read and stored in memory");
                }
                return resourceFile.toString();
            }
        }
        return "";
    }
}
