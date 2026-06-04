package com.elearning.fx.util;

/**
 * Stocke les informations de session utilisateur (token JWT, rôle, etc.).
 */
public class SessionManager {

    private static String token;
    private static Long userId;
    private static String nom;
    private static String prenom;
    private static String email;
    private static String role;
    private static String studyLevel;
    private static Long groupId;
    private static String groupName;

    public static void setSession(String token, Long userId, String nom,
                                   String prenom, String email, String role,
                                   String studyLevel, Long groupId, String groupName) {
        SessionManager.token = token;
        SessionManager.userId = userId;
        SessionManager.nom = nom;
        SessionManager.prenom = prenom;
        SessionManager.email = email;
        SessionManager.role = role;
        SessionManager.studyLevel = studyLevel;
        SessionManager.groupId = groupId;
        SessionManager.groupName = groupName;
    }

    public static void clear() {
        token = null;
        userId = null;
        nom = null;
        prenom = null;
        email = null;
        role = null;
        studyLevel = null;
        groupId = null;
        groupName = null;
    }

    public static boolean isLoggedIn() {
        return token != null && !token.isEmpty();
    }

    public static boolean isAdmin() {
        return "ROLE_ADMIN".equals(role);
    }

    public static boolean isTeacher() {
        return "ROLE_TEACHER".equals(role);
    }

    public static boolean isStudent() {
        return "ROLE_STUDENT".equals(role);
    }

    public static String getToken() { return token; }
    public static Long getUserId() { return userId; }
    public static String getNom() { return nom; }
    public static String getPrenom() { return prenom; }
    public static String getEmail() { return email; }
    public static String getRole() { return role; }
    public static String getFullName() { return prenom + " " + nom; }
    public static String getStudyLevel() { return studyLevel; }
    public static Long getGroupId() { return groupId; }
    public static String getGroupName() { return groupName; }
}
