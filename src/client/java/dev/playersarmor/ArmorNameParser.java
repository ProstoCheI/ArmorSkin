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

    public static String removeFlags(String name) {
        if (name == null) {
            return null;
        }

        String[] tokens = name.trim().split("\\s+");
        StringBuilder visible = new StringBuilder();
        boolean removed = false;
        for (String token : tokens) {
            if (token.matches("-[A-Za-z]")) {
                removed = true;
                continue;
            }
            if (!visible.isEmpty()) {
                visible.append(' ');
            }
            visible.append(token);
        }
        return removed ? visible.toString() : name;
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

    public static String setFlag(String name, char flag, boolean enabled) {
        if (extractUsername(name) == null || !Character.toString(flag).matches("[A-Za-z]")) {
            return name;
        }
        String token = "-" + flag;
        StringBuilder result = new StringBuilder();
        for (String part : name.trim().split("\\s+")) {
            if (!part.equals(token)) {
                if (!result.isEmpty()) {
                    result.append(' ');
                }
                result.append(part);
            }
        }
        if (enabled) {
            result.append(' ').append(token);
        }
        return result.toString();
    }
}
