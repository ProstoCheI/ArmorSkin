package dev.playersarmor;

public class ArmorNameParser {
    public static String extractUsername(String name) {
        if (name == null) return null;
        String username = name.trim().split("\\s+")[0];
        if (!isValidUsername(username)) return null;
        return username;
    }

    public static boolean isValidUsername(String username) {
        return username != null && username.matches("[A-Za-z0-9_]{3,16}");
    }
}
