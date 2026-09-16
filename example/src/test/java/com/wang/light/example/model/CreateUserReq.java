package com.wang.light.example.model;

public class CreateUserReq {
    private String name;

    public CreateUserReq() {
    }

    public CreateUserReq(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
