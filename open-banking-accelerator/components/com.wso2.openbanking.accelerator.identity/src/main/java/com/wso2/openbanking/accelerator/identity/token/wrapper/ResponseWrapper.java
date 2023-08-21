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

import net.sf.ehcache.constructs.web.filter.FilterServletOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Response wrapper implementation.
 */
public class ResponseWrapper extends HttpServletResponseWrapper {

    private ByteArrayOutputStream output;
    private int contentLength;
    private String contentType;

    public ResponseWrapper(HttpServletResponse response) {

        super(response);
        output = new ByteArrayOutputStream();
    }

    public byte[] getData() {

        return output.toByteArray();
    }

    public ServletOutputStream getOutputStream() {

        return new FilterServletOutputStream(output);
    }

    public PrintWriter getWriter() {

        return new PrintWriter(new OutputStreamWriter(getOutputStream(), StandardCharsets.UTF_8), true);
    }

    public int getContentLength() {

        return contentLength;
    }

    public void setContentLength(int length) {

        this.contentLength = length;
        super.setContentLength(length);
    }

    public String getContentType() {

        return contentType;
    }

    public void setContentType(String type) {

        this.contentType = type;
        super.setContentType(type);
    }
}

