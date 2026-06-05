package lims.utils;

import lims.models.User;

public class SessionManager {

    private static User currentUser;

    // Store the logged-in user
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Get the logged-in user
    public static User getCurrentUser() {
        return currentUser;
    }

    // Check if someone is logged in
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    // Log the user out
    public static void logout() {
        currentUser = null;
    }

    // Get the logged-in user's role
    public static String getCurrentUserRole() {
        if (currentUser != null) {
            return currentUser.getRole();
        }
        return null;
    }

    // Get the logged-in user's ID
    public static int getCurrentUserId() {
        if (currentUser != null) {
            return currentUser.getUserId();
        }
        return -1;
    }

    // Get the logged-in user's full name
    public static String getCurrentUserFullName() {
        if (currentUser != null) {
            return currentUser.getFullName();
        }
        return null;
    }
}