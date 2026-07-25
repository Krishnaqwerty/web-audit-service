package com.webaudit.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@UtilityClass
public class IpUtils {

    /**
     * Checks if the given IP address string or hostname resolves to a private, loopback, link-local, or multicast address.
     *
     * @param host IP address string or domain name
     * @return true if the IP address is restricted (SSRF risk), false otherwise
     */
    public static boolean isRestrictedIpOrHost(String host) {
        if (host == null || host.trim().isEmpty()) {
            return true;
        }

        String cleanedHost = host.trim().toLowerCase();

        // Immediate check for localhost literals
        if ("localhost".equalsIgnoreCase(cleanedHost) || cleanedHost.endsWith(".localhost") || cleanedHost.endsWith(".local")) {
            return true;
        }

        try {
            InetAddress[] addresses = InetAddress.getAllByName(cleanedHost);
            for (InetAddress address : addresses) {
                if (isRestrictedAddress(address)) {
                    log.warn("Host '{}' resolved to restricted IP address: {}", host, address.getHostAddress());
                    return true;
                }
            }
            return false;
        } catch (UnknownHostException e) {
            log.debug("Host '{}' could not be resolved via DNS: {}", host, e.getMessage());
            return false;
        }
    }

    /**
     * Evaluates whether an InetAddress is private, loopback, link-local, or site-local.
     */
    public static boolean isRestrictedAddress(InetAddress address) {
        if (address == null) {
            return true;
        }

        if (address.isLoopbackAddress() ||
                address.isAnyLocalAddress() ||
                address.isLinkLocalAddress() ||
                address.isSiteLocalAddress() ||
                address.isMulticastAddress()) {
            return true;
        }

        byte[] octets = address.getAddress();
        if (octets.length == 4) { // IPv4
            int first = octets[0] & 0xFF;
            int second = octets[1] & 0xFF;

            // 10.0.0.0/8 (Private)
            if (first == 10) return true;

            // 172.16.0.0/12 (Private 172.16.0.0 - 172.31.255.255)
            if (first == 172 && (second >= 16 && second <= 31)) return true;

            // 192.168.0.0/16 (Private)
            if (first == 192 && second == 168) return true;

            // 127.0.0.0/8 (Loopback)
            if (first == 127) return true;

            // 169.254.0.0/16 (Link Local / AWS Metadata 169.254.169.254)
            if (first == 169 && second == 254) return true;

            // 0.0.0.0/8 (Current network)
            if (first == 0) return true;

            // 100.64.0.0/10 (Shared Address Space / CGNAT)
            if (first == 100 && (second >= 64 && second <= 127)) return true;
        }

        return false;
    }
}
