package com.zeiss.koch.kaffeekasse;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Formater {
    public static String valueToCurrencyString(Double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        DecimalFormat round = new DecimalFormat("0.00", symbols);
        String formatted = round.format(value) + " €";
        return formatted;
    }

    public static String dateToLocalString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        return formatter.format(date);
    }
}
