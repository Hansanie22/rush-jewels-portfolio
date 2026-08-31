package lk.dio.rush_jewels.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public final class CustomPayloadEncoder {

    private CustomPayloadEncoder() {}

    /**
     * Encode parts using pipe '|' then Base64 for safe transport as custom_1.
     * Parts should not be null (use empty string "" if missing).
     */
    public static String encode(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i] != null) {
                sb.append(parts[i]);
            }
            if (i < parts.length - 1) sb.append('|');
        }
        return Base64.getEncoder().encodeToString(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decode payload and split by pipe. Returns Optional.empty() on invalid input.
     */
    public static Optional<String[]> decode(String encoded) {
        try {
            if (encoded == null || encoded.trim().isEmpty()) return Optional.empty();
            byte[] decoded = Base64.getDecoder().decode(encoded);
            String joined = new String(decoded, StandardCharsets.UTF_8);
            return Optional.of(joined.split("\\|", -1)); // -1 keeps trailing empty strings
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
