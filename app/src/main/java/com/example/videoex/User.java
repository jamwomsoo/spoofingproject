package com.example.videoex;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String email;
    private String phone;
    private String approval;
    private String registerDate;
    private String state;

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getApproval() { return approval; }

    public void setApproval(String approval) { this.approval = approval; }

    public String getRegisterDate() { return registerDate; }

    public void setRegisterDate(String registerDate) { this.registerDate = registerDate; }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}