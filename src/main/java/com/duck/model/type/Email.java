package com.duck.model.type;

public class Email {
    private String address;
    private String domain;

    public Email(String address, String domain) {
        this.address = address;
        this.domain = domain;
    }

    public String getAddress() {
        return this.address;
    }

    public String getDomain() {
        return this.domain;
    }

    public String getEmail() {
        return this.address + this.domain;
    }
}
