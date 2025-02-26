package org.wso2.financial.services.accelerator.common.json.schema.validator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * RequestWrapper class to cache the request body.
 */
public class RequestWrapper extends HttpServletRequestWrapper {

    private static final Log log = LogFactory.getLog(RequestWrapper.class);
    private byte[] cachedBody = null;

    public RequestWrapper(HttpServletRequest request) {
        super(request);
        // Read and cache the request body
        try {
            cachedBody = request.getInputStream().readAllBytes();
        } catch (IOException e) {
            log.error("Error occurred while reading the request body", e);
        }
    }

    @SuppressFBWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);
        return new ServletInputStream() {
            @Override
            public int read() {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    public String getCachedBodyAsString() {
        return new String(cachedBody, StandardCharsets.UTF_8);
    }
}
