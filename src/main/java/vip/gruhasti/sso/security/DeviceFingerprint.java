package vip.gruhasti.sso.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Coarse device identification from a User-Agent string, used only for the "new device"
 * login notification — not a security control (User-Agent is trivially spoofable). Uses a
 * (browserFamily, osFamily) tuple rather than the raw UA string so routine browser version
 * updates don't look like a new device every time.
 */
@Component
public class DeviceFingerprint {

    public record Device(String browserFamily, String osFamily) {}

    public Device describe(String userAgent) {
        String ua = userAgent == null ? "" : userAgent;
        return new Device(browserFamily(ua), osFamily(ua));
    }

    public String hash(Device device) {
        String key = device.browserFamily() + "|" + device.osFamily();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String browserFamily(String ua) {
        if (ua.contains("Edg/")) return "Edge";
        if (ua.contains("OPR/") || ua.contains("Opera")) return "Opera";
        if (ua.contains("Chrome/")) return "Chrome";
        if (ua.contains("Firefox/")) return "Firefox";
        if (ua.contains("Safari/")) return "Safari";
        return "Other";
    }

    private String osFamily(String ua) {
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac OS X")) return "macOS";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad") || ua.contains("iOS")) return "iOS";
        if (ua.contains("Linux")) return "Linux";
        return "Other";
    }
}
