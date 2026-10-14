package dev.playersarmor;

public class ArmorNameParser {
    public static String extractUsername(String name) {
        if (name == null) return null;
        String username = name.trim().split("\\s+")[0];
        if (!isValidUsername(username)) return null;
        return username;
    }

    public static String extractFlags(String name) {
        if (extractUsername(name) == null) {
            return "";
        }

        String[] parts = name.trim().split("\\s+", 2);
        return parts.length < 2 ? "" : parts[1];
    }

    public static boolean hasFlag(String flags, char flag) {
        if (flags == null || flags.isBlank()) {
            return false;
        }

        String expected = "-" + flag;
        for (String token : flags.trim().split("\\s+")) {
            if (token.equals(expected)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidUsername(String username) {
        return username != null && username.matches("[A-Za-z0-9_]{3,16}");
    }
}
