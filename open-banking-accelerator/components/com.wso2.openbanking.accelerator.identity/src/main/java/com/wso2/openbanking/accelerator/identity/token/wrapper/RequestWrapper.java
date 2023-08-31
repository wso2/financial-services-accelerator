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

package com.wso2.openbanking.accelerator.identity.token.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Request wrapper implementation.
 */
public class RequestWrapper extends HttpServletRequestWrapper {

    private Map<String, String> headerMap = new HashMap<>();

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request
     * @throws IllegalArgumentException if the request is null.
     */
    private final ByteArrayOutputStream byteArrayOutputStream;

    public RequestWrapper(HttpServletRequest request) {
        super(request);
        byteArrayOutputStream = new ByteArrayOutputStream();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ServletInputStream originalInputStream = super.getInputStream();

        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                int data = originalInputStream.read();
                if (data != -1) {
                    byteArrayOutputStream.write(data);
                }
                return data;
            }

            @Override
            public boolean isFinished() {
                return originalInputStream.isFinished();
            }

            @Override
            public boolean isReady() {
                return originalInputStream.isReady();
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                originalInputStream.setReadListener(readListener);
            }
        };
    }

    public byte[] getCapturedRequest() {
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Set the header map to hold the header values.
     *
     * @param name
     * @param value
     */
    public void setHeader(String name, String value) {

        headerMap.put(name, value);
    }

    @Override
    public String getHeader(String name) {

        String headerValue = super.getHeader(name);
        if (headerMap.containsKey(name)) {
            headerValue = headerMap.get(name);
        }
        return headerValue;
    }

    @Override
    public Enumeration<String> getHeaderNames() {

        List<String> headerNames = Collections.list(super.getHeaderNames());
        for (Map.Entry<String, String> entry: headerMap.entrySet()) {
            // prevent adding duplicate entries to headerNames list
            headerNames.remove(entry.getKey());
        }
        headerNames.addAll(headerMap.keySet());
        return Collections.enumeration(headerNames);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {

        if (headerMap.containsKey(name)) {
            return Collections.enumeration(Collections.singletonList(headerMap.get(name)));
        }
        return Collections.enumeration(Collections.list(super.getHeaders(name)));
    }
}
