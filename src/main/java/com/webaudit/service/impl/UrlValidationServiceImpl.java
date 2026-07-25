package com.webaudit.service.impl;

import com.webaudit.exception.InvalidUrlException;
import com.webaudit.exception.SsrfViolationException;
import com.webaudit.service.UrlValidationService;
import com.webaudit.util.IpUtils;
import com.webaudit.util.UrlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;

@Slf4j
@Service
public class UrlValidationServiceImpl implements UrlValidationService {

    @Override
    public void validateUrlOrThrow(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new InvalidUrlException(url, "URL must not be blank");
        }

        URI uri = UrlUtils.parseUri(url);
        if (uri == null) {
            throw new InvalidUrlException(url, "Malformed URI syntax");
        }

        String scheme = uri.getScheme().toLowerCase();
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new InvalidUrlException(url, String.format("Unsupported scheme '%s'. Only http and https are allowed.", scheme));
        }

        String host = uri.getHost();
        if (host == null || host.trim().isEmpty()) {
            throw new InvalidUrlException(url, "Target host is missing");
        }

        if (IpUtils.isRestrictedIpOrHost(host)) {
            throw new SsrfViolationException(host);
        }
    }

    @Override
    public boolean isValid(String url) {
        try {
            validateUrlOrThrow(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
