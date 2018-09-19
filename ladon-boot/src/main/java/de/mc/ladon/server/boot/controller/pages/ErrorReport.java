package de.mc.ladon.server.boot.controller.pages;


import com.google.common.collect.Maps;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * Debug page for errors
 * Created by Ralf Ulrich on 13.12.15.
 */
public class ErrorReport {

    protected final Throwable error;
    protected final boolean isParseError;
    protected final HttpServletRequest request;


    public ErrorReport(Throwable error, HttpServletRequest request) {
        this.error = error;
        this.request = request;
        this.isParseError = false;

    }

    public String toString() {
        StringBuffer buffer = new StringBuffer(10240);
        Throwable cause = this.getCause();
        buffer.append("<div id=\'errorReport\' class=\'errorReport\'>\n");
        buffer.append("<table border=\'1\' cellspacing=\'1\' cellpadding=\'4\' width=\'100%\' style=\'background-color: white;\'>");

        buffer.append("<tr><td colspan=\'2\' style=\'color:white; background-color: navy; font-weight: bold\'>Exception</td></tr>");
        buffer.append("<tr><td width=\'12%\'><b>Status Code</b></td><td>");
        buffer.append(request.getAttribute("javax.servlet.error.status_code"));
        buffer.append("</td></tr>");
        buffer.append("<tr><td width=\'12%\'><b>Request URI</b></td><td>");
        buffer.append(request.getAttribute("javax.servlet.error.request_uri"));
        buffer.append("</td></tr>");
        buffer.append("<tr><td width=\'12%\'><b>Class</b></td><td>");
        if (cause != null)
            buffer.append(cause.getClass().getName());
        buffer.append("</td></tr>");


        buffer.append("<tr><td valign=\'top\' width=\'12%\'><b>Message</b></td><td>");
        buffer.append(this.getMessage());
        buffer.append("</td></tr>");


        if (!this.isParseError()) {
            buffer.append("<tr><td valign=\'top\' colspan=\'2\'>\n");
            buffer.append(this.getStackTrace());
            buffer.append("</td></tr>");
        }

        buffer.append("</table>");
        buffer.append("<br/>");
        buffer.append("<table border=\'1\' cellspacing=\'1\' cellpadding=\'4\' width=\'100%\' style=\'background-color: white;\'>");
        buffer.append("<tr><td colspan=\'2\' style=\'color:white; background-color: navy; font-weight: bold\'>Request</td></tr>");
        TreeMap requestAttributes = new TreeMap();
        Enumeration attributeNames = this.request.getAttributeNames();

        while (attributeNames.hasMoreElements()) {
            String requestHeaders = attributeNames.nextElement().toString();
            requestAttributes.put(requestHeaders, this.request.getAttribute(requestHeaders));
        }

        buffer.append("<tr><td width=\'12%\' valign=\'top\'><b>Attributes</b></td><td>");
        this.writeMap(requestAttributes, buffer);
        buffer.append("</td></tr>");
        buffer.append("<tr><td width=\'12%\'><b>Auth Type</b></td><td>");
        buffer.append(this.request.getAuthType());
        buffer.append("</td></tr>");
        buffer.append("<tr><td width=\'12%\'><b>Context Path</b></td><td>");
        buffer.append("<a href=\'");
        buffer.append(this.request.getContextPath());
        buffer.append("\'>");
        buffer.append(this.request.getContextPath());
        buffer.append("</a>");
        buffer.append("</td></tr>");
        TreeMap requestHeaders1 = new TreeMap();
        Enumeration headerNames = this.request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String requestParams = headerNames.nextElement().toString();
            requestHeaders1.put(requestParams, this.request.getHeader(requestParams));
        }

        buffer.append("<tr><td width=\'12%\' valign=\'top\'><b>Headers</b></td><td>");
        this.writeMap(requestHeaders1, buffer);
        buffer.append("</td></tr>");
        buffer.append("<tr><td width=\'12%\'><b>Query</b></td><td>");
        buffer.append(this.request.getQueryString());
        buffer.append("</td></tr>");
        buffer.append("<tr><td width=\'12%\'><b>Method</b></td><td>");
        buffer.append(this.request.getMethod());
        buffer.append("</td></tr>");
        TreeMap requestParams1 = new TreeMap();
        Enumeration paramNames = this.request.getParameterNames();

        String requestURI;
        while (paramNames.hasMoreElements()) {
            requestURI = paramNames.nextElement().toString();
            requestParams1.put(requestURI, this.request.getParameter(requestURI));
        }

        buffer.append("<tr><td width=\'12%\' valign=\'top\'><b>Parameters</b></td><td>");
        this.writeMap(requestParams1, buffer);
        buffer.append("</td></tr>");
        buffer.append("<tr><td width=\'12%\'><b>Remote User</b></td><td>");
        buffer.append(this.request.getRemoteUser());
        buffer.append("</td></tr>");
        buffer.append("<tr><td width=\'12%\' valign=\'top\'><b>URI</b></td><td>");
        buffer.append("<a href=\'");
        requestURI = this.request.getRequestURI();
        buffer.append(requestURI);
        buffer.append("\'>");
        buffer.append(requestURI);
        buffer.append("</a>");
        buffer.append("</td></tr>");
        buffer.append("<tr><td><b width=\'12%\'>URL</b></td><td>");
        buffer.append("<a href=\'");
        buffer.append(this.request.getRequestURL());
        buffer.append("\'>");
        buffer.append(this.request.getRequestURL());
        buffer.append("</a>");
        buffer.append("</td></tr>");
        TreeMap sessionAttributes = new TreeMap();
        if (this.request.getSession(false) != null) {
            HttpSession session = this.request.getSession();
            attributeNames = session.getAttributeNames();

            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement().toString();
                sessionAttributes.put(name, session.getAttribute(name));
            }
        }

        buffer.append("<tr><td width=\'12%\' valign=\'top\'><b>Session</b></td><td>");
        this.writeMap(sessionAttributes, buffer);
        buffer.append("</td></tr>");
        buffer.append("</table>\n");
        buffer.append("</div>\n");
        return buffer.toString();

    }

    protected Throwable getCause() {
        Throwable cause = null;
        if (this.error instanceof ServletException) {
            cause = ((ServletException) this.error).getRootCause();
            if (cause == null) {
                cause = this.error.getCause();
            }
        } else {
            if (this.error != null)
                cause = this.error.getCause();
        }

        if (cause != null && cause.getCause() != null) {
            cause = cause.getCause();
        }

        if (cause == null) {
            cause = this.error;
        }

        return cause;
    }

    protected boolean isParseError() {
        return this.isParseError;
    }


    protected String getMessage() {
        String value = "null";
        if (this.isParseError()) {
            String cause1 = this.error.getMessage();
            value = cause1;
            int startIndex = cause1.indexOf(10);
            int endIndex = cause1.lastIndexOf("...");
            if (startIndex != -1 && endIndex > startIndex) {
                value = cause1.substring(startIndex + 1, endIndex);
            }
            value = escapeHtml(value);
            value = value.replace("...", ", &#160;");
            return value;
        } else {
            Throwable cause = this.getCause();
            if (cause != null)
                value = cause.getMessage() != null ? cause.getMessage() : "null";
            return escapeHtml(value);
        }
    }


    protected String getStackTrace() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        if (getCause() != null)
            this.getCause().printStackTrace(pw);
        StringBuffer buffer = new StringBuffer(sw.toString().length() + 80);
        buffer.append("<pre><tt style=\'font-size:10pt;\'>");
        buffer.append(sw.toString().trim());
        buffer.append("</tt></pre>");
        return buffer.toString();
    }

    protected void writeMap(Map<String, Object> map, StringBuffer buffer) {
        buffer.append("<table  cellspacing=\'1\' cellpadding=\'4\' width=\'100%\' style=\'background-color: white;\'>");
        for (Iterator i$ = map.entrySet().iterator(); i$.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i$.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            buffer.append("<tr><td>");
            buffer.append(key);
            buffer.append("</td><td>");
            if (value != null) {
                if (value instanceof Map) {
                    writeMap((Map<String, Object>) value, buffer);
                } else if (value instanceof ConversionService) {
                    buffer.append("filtered from error report");
                } else if (value instanceof SecurityContext) {
                    Map<String, Object> scMap = Maps.newHashMap();
                    Authentication auth = ((SecurityContext) value).getAuthentication();
                    scMap.put("principal", auth.getPrincipal());
                    scMap.put("isAuthenticated", auth.isAuthenticated());
                    scMap.put("credentials", auth.getCredentials());
                    scMap.put("details", auth.getDetails());
                    scMap.put("authorities", auth.getAuthorities());
                    writeMap(scMap, buffer);
                } else {
                    buffer.append(value.toString());
                }
            } else {
                buffer.append("null");
            }
            buffer.append("</td></tr>");
        }

        if (map.isEmpty()) {
            buffer.append("&#160;");
        }

        buffer.append("</table>\n");

    }


}
