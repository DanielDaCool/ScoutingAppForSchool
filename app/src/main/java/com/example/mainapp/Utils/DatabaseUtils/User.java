package com.example.mainapp.Utils.DatabaseUtils;

import com.google.firebase.database.Exclude;

/**
 * Represents the User component in the application.
 *
 * This class is responsible for handling the logic, data flow,
 * and interactions related to its specific feature inside the Android app.
 */
public class User {
    private String fullName;
    private String email;
    private UserRole role;
    private String userId;

    // Required empty constructor for Firebase
    public User() {}

    public User(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
        this.role = UserRole.SCOUTER; // default role
    }

    public User(String fullName, String email, UserRole role, String userId) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.userId = userId;
    }

/**
 * Executes the logic associated with the getFullName operation.
 * @return the value produced by this method.
 */
    public String getFullName()  { return fullName; }
/**
 * Executes the logic associated with the getEmail operation.
 * @return the value produced by this method.
 */
    public String getEmail()     { return email; }
/**
 * Executes the logic associated with the getRole operation.
 * @return the value produced by this method.
 */
    public UserRole getRole()    { return role; }
/**
 * Executes the logic associated with the getUserId operation.
 * @return the value produced by this method.
 */
    public String getUserId()    { return userId; }
/**
 * Executes the logic associated with the setUserId operation.
 * @param userId parameter required for this method.
 */
    public void setUserId(String userId)     { this.userId = userId; }

    @Exclude
/**
 * Executes the logic associated with the isAdmin operation.
 * @return the value produced by this method.
 */
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}