package org.example.security;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

public final class EndpointUrlValidator {
    private EndpointUrlValidator() {}

    public static void validatePublicHttpUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalArgumentException("Endpoint URL is required");
        }
        try {
            URI uri = URI.create(rawUrl);
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                    || uri.getHost() == null || uri.getUserInfo() != null) {
                throw new IllegalArgumentException("Endpoint must be an absolute HTTP(S) URL without user-info");
            }
            String host = uri.getHost();
            if (host.equalsIgnoreCase("localhost") || host.endsWith(".localhost")
                    || host.endsWith(".local") || host.endsWith(".internal")) {
                throw new IllegalArgumentException("Monitoring target must use a public hostname");
            }
            InetAddress[] resolved = InetAddress.getAllByName(host);
            if (resolved.length == 0) throw new IllegalArgumentException("Hostname did not resolve");
            for (InetAddress address : resolved) {
                if (isNonPublic(address)) {
                    throw new IllegalArgumentException("Monitoring target resolves to a private or reserved IP address");
                }
            }
        } catch (UnknownHostException | IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsafe endpoint URL: " + ex.getMessage(), ex);
        }
    }

    private static boolean isNonPublic(InetAddress ip) {
        if (ip.isAnyLocalAddress() || ip.isLoopbackAddress() || ip.isLinkLocalAddress()
                || ip.isSiteLocalAddress() || ip.isMulticastAddress()) return true;
        byte[] b = ip.getAddress();
        if (ip instanceof Inet4Address) {
            int a = b[0] & 255, second = b[1] & 255, third = b[2] & 255;
            return a == 0 || a == 10 || a == 127 || a >= 224
                    || (a == 100 && second >= 64 && second <= 127)
                    || (a == 169 && second == 254)
                    || (a == 172 && second >= 16 && second <= 31)
                    || (a == 192 && (second == 0 || second == 168))
                    || (a == 198 && (second == 18 || second == 19
                        || (second == 51 && third == 100)))
                    || (a == 203 && second == 0 && third == 113);
        }
        if (ip instanceof Inet6Address) {
            if (((b[0] & 255) & 0xfe) == 0xfc) return true; // Unique-local
            if ((b[0] & 255) == 0x20 && (b[1] & 255) == 0x01
                    && (b[2] & 255) == 0x0d && (b[3] & 255) == 0xb8) return true; // Documentation
            // IPv4-mapped IPv6 addresses must be checked using their embedded IPv4 address.
            boolean mapped = true;
            for (int i = 0; i < 10; i++) mapped &= b[i] == 0;
            mapped &= (b[10] & 255) == 255 && (b[11] & 255) == 255;
            if (mapped) {
                try {
                    return isNonPublic(InetAddress.getByAddress(new byte[]{b[12], b[13], b[14], b[15]}));
                } catch (UnknownHostException impossible) {
                    return true;
                }
            }
        }
        return false;
    }
}
