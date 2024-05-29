package com.example.kozinti.tables;

import java.util.Date;

public class Users {
    public String id, fName, lName, birthdate, email, password, userImg;
    public static String ConnectedUser;
    public Date joiningDate;

    public Users() {
    }

    public Users(String id, String fName, String lName, String birthdate, String email, String password, String userImg, Date joiningDate) {
        this.id = id;
        this.fName = fName;
        this.lName = lName;
        this.birthdate = birthdate;
        this.email = email;
        this.password = password;
        this.userImg = userImg;
        this.joiningDate = joiningDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserImg() {
        return userImg;
    }

    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }

    public static String getConnectedUser() {
        return ConnectedUser;
    }

    public static void setConnectedUser(String connectedUser) {
        ConnectedUser = connectedUser;
    }

    public Date getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(Date joiningDate) {
        this.joiningDate = joiningDate;
    }
}
