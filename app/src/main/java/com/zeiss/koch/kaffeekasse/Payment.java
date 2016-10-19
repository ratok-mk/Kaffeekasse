package com.zeiss.koch.kaffeekasse;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User
 * Created by koch on 01.10.2016.
 */

class Payment {

    private int id;
    private Date datetime;
    private int userid;
    private Double amount;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public Payment(Date datetime, int userid, Double amount) {
        super();
        this.datetime = datetime;
        this.userid = userid;
        this.amount = amount;
    }

    public Payment(int id, String datetimeString, int userid, Double amount) {
        super();
        Date dateParsed = null;
        try {
            dateParsed = dateFormat.parse(datetimeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.id = id;
        this.datetime = dateParsed;
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

    public String getDatetime() {
        return dateFormat.format(datetime);
    }

    public int getUserid() {
        return userid;
    }

    public Double getAmount() {
        return amount;
    }
}
