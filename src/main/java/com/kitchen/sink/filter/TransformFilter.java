package com.kitchen.sink.filter;

import jakarta.servlet.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TransformFilter extends OncePerRequestFilter {
    private static String hostIpAddress;

    private String getHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        String headers = Collections.list(headerNames).stream()
                .map(headerName -> headerName + ": " + request.getHeader(headerName))
                .collect(Collectors.joining(", "));
        return headers;
    }

    private String getRequestData(final HttpServletRequest request) throws UnsupportedEncodingException {
        String payload = null;
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
            }
        }
        return payload;
    }

    private String getResponseData(final HttpServletResponse response) throws IOException {
        String payload = null;
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
            }
            wrapper.copyBodyToResponse(); // Ensure response body is written back to the client
        }
        return payload;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(servletRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(servletResponse);

        // Proceed with the next filter
        filterChain.doFilter(requestWrapper, responseWrapper);

        // Capture details
        String upstreamIp = requestWrapper.getRemoteAddr();
        String hostIp = requestWrapper.getLocalAddr();
        String headers = getHeaders(requestWrapper);
        String url = requestWrapper.getRequestURL().toString();
        String uri = requestWrapper.getRequestURI();
        String queryParams = requestWrapper.getQueryString() != null ? requestWrapper.getQueryString() : "";
        Map<String, String[]> pathVariables = requestWrapper.getParameterMap();
        String requestBody = getRequestData(requestWrapper);
        int requestBodySize = requestBody != null ? requestBody.length() : 0;
        String responseBody = getResponseData(responseWrapper);
        int responseBodySize = responseBody != null ? responseBody.length() : 0;
        int statusCode = responseWrapper.getStatus();

        // Create the RequestResponseLog object
        RequestResponseLog logData = new RequestResponseLog(
                upstreamIp, hostIp, headers, url, uri, queryParams, pathVariables,
                requestBody, requestBodySize, responseBody, responseBodySize, statusCode
        );

        // Log the data using the toString method
        log.info(logData.toFormatString());

        // Write the response back to the original response
        responseWrapper.copyBodyToResponse();
    }

    @Getter
    @AllArgsConstructor
    @ToString
    public static class RequestResponseLog {
        private final String upstreamIp;
        private final String hostIp;
        private final String headers;
        private final String url;
        private final String uri;
        private final String queryParams;
        private final Map<String, String[]> pathVariables;
        private final String requestBody;
        private final int requestBodySize;
        private final String responseBody;
        private final int responseBodySize;
        private final int statusCode;

        public String toFormatString() {
            return String.format(
                    "RequestResponseLog {\n" +
                            "  Upstream IP: %s\n" +
                            "  Host IP: %s\n" +
                            "  Headers: %s\n" +
                            "  URL: %s\n" +
                            "  URI: %s\n" +
                            "  Query Params: %s\n" +
                            "  Path Variables: %s\n" +
                            "  Request Body: %s\n" +
                            "  Request Body Size: %d bytes\n" +
                            "  Response Body: %s\n" +
                            "  Response Body Size: %d bytes\n" +
                            "  HTTP Status Code: %d\n" +
                            "}",
                    upstreamIp, hostIp, headers, url, uri, queryParams, formattedString(pathVariables),
                    requestBody, requestBodySize, responseBody, responseBodySize, statusCode
            );
        }

        private String formattedString(Map<String, String[]> pathVariables) {
            return pathVariables.entrySet().stream()
                    .map(entry -> entry.getKey() + ": " + String.join(", ", entry.getValue()))
                    .collect(Collectors.joining(", "));
        }
    }

    public String getHostIpAddress() {
        if (hostIpAddress == null) {
            try {
                hostIpAddress = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                log.error("Error while populating hostIpAddress", e);
            }
        }
        return hostIpAddress;
    }
}
