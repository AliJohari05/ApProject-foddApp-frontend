package com.foodapp.food4ufrontend.util;

public class AuthManager {
    private static String jwtToken;
    private static String currentUserRole;
    private static String currentUserId;

    public static String getJwtToken() {
        return jwtToken;
    }

    public static void setJwtToken(String token) {
        jwtToken = token;
    }

    public static String getCurrentUserRole() {
        return currentUserRole;
    }

    public static void setCurrentUserRole(String role) {
        currentUserRole = role;
    }

    public static String getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(String userId) {
        currentUserId = userId;
    }

    public static boolean isLoggedIn() {
        return jwtToken != null && !jwtToken.isEmpty();
    }

    public static void logout() {
        jwtToken = null;
        currentUserRole = null;
        currentUserId = null;
    }
}