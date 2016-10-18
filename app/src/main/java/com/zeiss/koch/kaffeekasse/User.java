package com.zeiss.koch.kaffeekasse;

/**
 * User
 * Created by koch on 01.10.2016.
 */

class User {

    private int id;
    private String role;
    private String name;
    private String nfcid;

    public User(String name, String nfcid) {
        super();
        this.role = "user";
        this.name = name;
        this.nfcid = nfcid;
    }

    public User(int id, String name, String nfcid, String role) {
        super();
        this.role = role;
        this.id = id;
        this.name = name;
        this.nfcid = nfcid;
    }

    //getters & setters

    @Override
    public String toString() {
        return "User [id=" + id + ", role=" + role + ", name=" + name + ", nfcid=" + nfcid + "]";
    }

    String getName() {
        return name;
    }

    String getNfcId() {
        return nfcid;
    }

    public int getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public void SetNfcId(String nfcId) {
        this.nfcid = nfcId;
    }
}
