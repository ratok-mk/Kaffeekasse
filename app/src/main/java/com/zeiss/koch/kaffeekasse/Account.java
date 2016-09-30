package com.zeiss.koch.kaffeekasse;

/**
 * Account
 * Created by koch on 01.10.2016.
 */

class Account {

    private int id;
    private String username;
    private String nfcid;
    private Double balance;

    Account(){}

    public Account(String username, String nfcid) {
        super();
        this.username = username;
        this.nfcid = nfcid;
        this.balance = 0.0;
    }

    //getters & setters

    @Override
    public String toString() {
        return "Account [id=" + id + ", username=" + username + ", nfcid=" + nfcid
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

    public void setId(int id) {
        this.id = id;
    }

    void setUsername(String username) {
        this.username = username;
    }

    void setNfcId(String nfcId) {
        this.nfcid = nfcId;
    }

    void setBalance(double balance) {
        this.balance = balance;
    }

    public int getId() {
        return id;
    }
}
