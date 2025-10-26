package com.example.mainapp.Utils;

public class User {
    private String fullName;
    private int userID;
    private String password;
    private String userName;

    public User(){}
    public User(String fullName, int userID, String password, String userName){
        this.fullName = fullName;
        this.userID = userID;
        this.password = password;
        this.userName = userName;
    }
    public void setFullName(String fullName){this.fullName = fullName;}
    private void setUserID(int userID){this.userID = userID;}
    private void setPassword(String password){this.password = password;}
    public void setUserName(String userName){this.userName = userName;}

    public String getFullName(){return this.fullName;}
    public int getUserID(){return this.userID;}
    public String getPassword(){return this.password;}
    public String getUserName(){return  this.userName;}
}
