package org.example.security;

import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;

@Component
public class EndpointUrlValidator {
    public void validate(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank() || rawUrl.length() > 2048) {
            throw new IllegalArgumentException("Endpoint URL is required and must be at most 2048 characters");
        }

        final URI uri;
        try {
            uri = URI.create(rawUrl.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Endpoint URL is invalid");
        }

        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new IllegalArgumentException("Only HTTP and HTTPS endpoint URLs are allowed");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()
                || uri.getUserInfo() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("Endpoint URL must contain a valid host and cannot include credentials or fragments");
        }
        if (uri.getPort() == 0 || uri.getPort() > 65535) {
            throw new IllegalArgumentException("Endpoint URL port is invalid");
        }

        String host = uri.getHost().toLowerCase(Locale.ROOT);
        if (host.equals("localhost") || host.endsWith(".localhost")
                || host.endsWith(".local") || host.endsWith(".internal")
                || host.equals("metadata.google.internal")) {
            throw new IllegalArgumentException("Private and local network destinations are not allowed");
        }

        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            if (addresses.length == 0) {
                throw new IllegalArgumentException("Endpoint host did not resolve to a public address");
            }
            for (InetAddress address : addresses) {
                if (!isPublicAddress(address)) {
                    throw new IllegalArgumentException("Endpoint host resolves to a private or reserved IP address");
                }
            }
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("Endpoint host could not be resolved");
        }
    }

    private boolean isPublicAddress(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return false;
        }

        byte[] b = address.getAddress();
        if (address instanceof Inet4Address) {
            int a = b[0] & 0xff, second = b[1] & 0xff;
            if (a == 0 || a == 10 || a == 127 || a >= 224) return false;
            if (a == 100 && second >= 64 && second <= 127) return false;
            if (a == 169 && second == 254) return false;
            if (a == 172 && second >= 16 && second <= 31) return false;
            if (a == 192 && second == 168) return false;
            if (a == 192 && second == 0) return false;
            if (a == 198 && (second == 18 || second == 19)) return false;
            if (a == 198 && second == 51 && (b[2] & 0xff) == 100) return false;
            if (a == 203 && second == 0 && (b[2] & 0xff) == 113) return false;
            return true;
        }

        if (address instanceof Inet6Address) {
            int first = b[0] & 0xff;
            int second = b[1] & 0xff;
            // Unique-local (fc00::/7), link-local (fe80::/10), and documentation (2001:db8::/32).
            if ((first & 0xfe) == 0xfc) return false;
            if (first == 0xfe && (second & 0xc0) == 0x80) return false;
            if (first == 0x20 && second == 0x01 && (b[2] & 0xff) == 0x0d
                    && (b[3] & 0xff) == 0xb8) return false;
        }
        return true;
    }
}
