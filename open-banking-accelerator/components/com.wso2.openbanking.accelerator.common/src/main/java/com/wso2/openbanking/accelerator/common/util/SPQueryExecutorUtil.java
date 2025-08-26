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

package com.wso2.openbanking.accelerator.common.util;

import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Util class to handle communications with stream processor.
 */
public class SPQueryExecutorUtil {

    private static Log log = LogFactory.getLog(SPQueryExecutorUtil.class);

    /**
     * Executes the given query in SP.
     *
     * @param appName           Name of the siddhi app.
     * @param query             Name of the query
     * @param spUserName        Username for SP
     * @param spPassword        Password for SP
     * @param spApiHost         Hostname of the SP
     * @return JSON object with result
     * @throws IOException           IO Exception.
     * @throws OpenBankingException  OpenBanking Exception.
     */
    public static JSONObject executeQueryOnStreamProcessor(String appName, String query, String spUserName,
                                                           String spPassword, String spApiHost)
            throws IOException, ParseException, OpenBankingException {
        log.info("Executing query on Stream Processor for app: " + appName);
        byte[] encodedAuth = Base64.getEncoder()
                .encode((spUserName + ":" + spPassword).getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.UTF_8.toString());

        HttpPost httpPost = new HttpPost(spApiHost + OpenBankingConstants.SP_API_PATH);
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(OpenBankingConstants.APP_NAME_CC, appName);
        jsonObject.put(OpenBankingConstants.QUERY, query);
        StringEntity requestEntity = new StringEntity(jsonObject.toJSONString());
        httpPost.setHeader(OpenBankingConstants.CONTENT_TYPE_TAG, OpenBankingConstants.JSON_CONTENT_TYPE);
        httpPost.setEntity(requestEntity);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Executing query %s on SP", query));
        }
        try (CloseableHttpResponse response = HTTPClientUtils.getHttpsClientInstance().execute(httpPost)) {
            if (log.isDebugEnabled()) {
                log.debug("Received response from Stream Processor with status: " +
                        response.getStatusLine().getStatusCode());
            }
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                String error = String.format("Error while invoking SP rest api : %s %s",
                        response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
                log.error(error);
                return null;
            }
            String responseStr = EntityUtils.toString(entity);
            JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
            return (JSONObject) parser.parse(responseStr);
        }
    }
}
