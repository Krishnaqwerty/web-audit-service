package com.webaudit.validation;

import com.webaudit.util.IpUtils;
import com.webaudit.util.UrlUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

@Slf4j
public class UrlValidatorConstraint implements ConstraintValidator<ValidUrl, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        URI uri = UrlUtils.parseUri(value);
        if (uri == null) {
            return false;
        }

        String scheme = uri.getScheme().toLowerCase();
        // Strictly accept only http and https
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            log.debug("URL '{}' rejected due to unsupported scheme '{}'", value, scheme);
            return false;
        }

        String host = uri.getHost();
        if (host == null || host.trim().isEmpty()) {
            return false;
        }

        // Check for private IPs / SSRF targets / loopback
        if (IpUtils.isRestrictedIpOrHost(host)) {
            log.debug("URL '{}' rejected because host '{}' is a restricted IP or local target", value, host);
            return false;
        }

        return true;
    }
}
