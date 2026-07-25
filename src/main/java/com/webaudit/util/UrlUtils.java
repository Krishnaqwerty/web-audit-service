package com.webaudit.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@UtilityClass
public class UrlUtils {

    /**
     * Parses a string URL into a URI object safely. Returns null if URI syntax is invalid.
     */
    public static URI parseUri(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return null;
        }

        try {
            URI uri = new URI(urlString.trim());
            if (uri.getScheme() == null) {
                return null;
            }
            return uri;
        } catch (URISyntaxException e) {
            log.debug("Invalid URI syntax for url '{}': {}", urlString, e.getMessage());
            return null;
        }
    }

    /**
     * Extracts host from URL string safely.
     */
    public static String extractHost(String urlString) {
        URI uri = parseUri(urlString);
        return (uri != null) ? uri.getHost() : null;
    }

    /**
     * Normalizes a URL for consistent cache keying and comparison.
     */
    public static String normalizeUrl(String urlString) {
        URI uri = parseUri(urlString);
        if (uri == null) {
            return urlString != null ? urlString.trim().toLowerCase() : "";
        }

        String scheme = uri.getScheme().toLowerCase();
        String host = uri.getHost().toLowerCase();
        int port = uri.getPort();

        // Omit default ports
        if (("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443)) {
            port = -1;
        }

        String path = uri.getPath();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            path = "";
        } else if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        StringBuilder builder = new StringBuilder();
        builder.append(scheme).append("://").append(host);
        if (port != -1) {
            builder.append(":").append(port);
        }
        builder.append(path);

        if (uri.getQuery() != null) {
            builder.append("?").append(uri.getQuery());
        }

        return builder.toString();
    }
}
