package com.zeiss.koch.kaffeekasse;

import java.sql.Date;

/**
 * User
 * Created by koch on 01.10.2016.
 */

class Payment {

    private int id;
    private Date datetime;
    private int userid;
    private Double amount;

    public Payment(Date datetime, int userid, Double amount) {
        super();
        this.datetime = datetime;
        this.userid = userid;
        this.amount = amount;
    }

    //getters & setters

    @Override
    public String toString() {
        return "User [id=" + id + ", datetime=" + datetime + ", userid=" + userid + ", amount=" + amount + "]";
    }

    public int getId() {
        return id;
    }

    public Date getDatetime() {
        return datetime;
    }

    public int getUserid() {
        return userid;
    }

    public Double getAmount() {
        return amount;
    }
}
