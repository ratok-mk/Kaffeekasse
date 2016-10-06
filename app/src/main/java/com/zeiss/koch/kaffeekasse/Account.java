package com.zeiss.koch.kaffeekasse;

/**
 * Account
 * Created by koch on 01.10.2016.
 */

class Account {

    private int id;
    private boolean admin;
    private String username;
    private String nfcid;
    private Double balance;

    public Account(String username, String nfcid) {
        super();
        this.admin = false;
        this.username = username;
        this.nfcid = nfcid;
        this.balance = 0.0;
    }

    public Account(int id, boolean admin, String username, String nfcid, double balance) {
        super();
        this.admin = admin;
        this.id = id;
        this.username = username;
        this.nfcid = nfcid;
        this.balance = balance;
    }

    //getters & setters

    @Override
    public String toString() {
        return "Account [id=" + id + ", admin=" + admin + ", username=" + username + ", nfcid=" + nfcid
                + ", balance=" + balance
                + "]";
    }

    String getUsername() {
        return username;
    }

    String getNfcId() {
        return nfcid;
    }

    Double getBalance() {
        return balance;
    }

    public int getId() {
        return id;
    }

    public boolean getAdmin() {
        return admin;
    }
}
