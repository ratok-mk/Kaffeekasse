package com.zeiss.koch.kaffeekasse;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class Formater {
    public static String valueToCurrencyString(Double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        DecimalFormat round = new DecimalFormat("0.00", symbols);
        String formatted = round.format(value) + " €";
        return formatted;
    }
}
