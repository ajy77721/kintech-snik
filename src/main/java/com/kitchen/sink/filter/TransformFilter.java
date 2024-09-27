package com.kitchen.sink.filter;

import jakarta.servlet.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
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
@Order(1)
@Slf4j
public class TransformFilter implements Filter {
    private static String hostIpAddress;
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        if (servletRequest instanceof HttpServletRequest request
                && servletResponse instanceof HttpServletResponse response) {

            HttpServletRequest requestToCache = new ContentCachingRequestWrapper(request);
            HttpServletResponse responseToCache = new ContentCachingResponseWrapper(response);

            // Proceed with the next filter
            chain.doFilter(requestToCache, responseToCache);

            // Capture details
            String upstreamIp = requestToCache.getRemoteAddr();
            String hostIp = requestToCache.getLocalAddr();
            String headers = getHeaders(requestToCache);
            String url = requestToCache.getRequestURL().toString();
            String uri = requestToCache.getRequestURI();
            String queryParams = requestToCache.getQueryString() != null ? requestToCache.getQueryString() : "";
            Map<String, String[]> pathVariables = requestToCache.getParameterMap();
            String requestBody = getRequestData(requestToCache);
            int requestBodySize = requestBody != null ? requestBody.length() : 0;
            String responseBody = getResponseData(responseToCache);
            int responseBodySize = responseBody != null ? responseBody.length() : 0;
            int statusCode = responseToCache.getStatus();

            // Create the RequestResponseLog object
            RequestResponseLog logData = new RequestResponseLog(
                    upstreamIp, hostIp, headers, url, uri, queryParams, pathVariables,
                    requestBody, requestBodySize, responseBody, responseBodySize, statusCode
            );

            // Log the data using the toString method
            log.info(logData.toFormatString());
        } else {
            chain.doFilter(servletRequest, servletResponse);
        }
    }

    // Helper methods for extracting request and response data
    private static String getHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        String headers = Collections.list(headerNames).stream()
                .map(headerName -> headerName + ": " + request.getHeader(headerName))
                .collect(Collectors.joining(", "));
        return headers;
    }

    private static String getRequestData(final HttpServletRequest request) throws UnsupportedEncodingException {
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

    private static String getResponseData(final HttpServletResponse response) throws IOException {
        String payload = null;
        ContentCachingResponseWrapper wrapper =
                WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
                wrapper.copyBodyToResponse();
            }
        }
        return payload;
    }

    @Override
    public void destroy() {
    }

    // Inner class to hold the fields
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
                    upstreamIp, hostIp, headers, url, uri, queryParams, formatedString(pathVariables),
                    requestBody, requestBodySize, responseBody, responseBodySize, statusCode
            );
        }

        private String formatedString(Map<String, String[]> pathVariables) {
            return pathVariables.entrySet().stream()
                    .map(entry -> entry.getKey() + ": " + String.join(", ", entry.getValue()))
                    .collect(Collectors.joining(", "));
        }
    }

    public  String getHostIpAddress()
    {
        if (hostIpAddress == null)
        {
            try
            {
                hostIpAddress = InetAddress.getLocalHost().getHostAddress();
            }
            catch (UnknownHostException e)
            {
                log.error("Error while populating hostIpAddress", e);
            }
        }
        return hostIpAddress;
    }
}
