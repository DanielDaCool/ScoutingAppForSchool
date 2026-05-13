package com.example.mainapp.Utils.DatabaseUtils;

import com.google.firebase.database.Exclude;

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

    public String getFullName()  { return fullName; }
    public String getEmail()     { return email; }
    public UserRole getRole()    { return role; }
    public String getUserId()    { return userId; }
    public void setUserId(String userId)     { this.userId = userId; }

    @Exclude
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}