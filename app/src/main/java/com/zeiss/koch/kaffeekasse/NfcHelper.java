package com.zeiss.koch.kaffeekasse;

/**
 * ${FILENAME}
 * Created by koch on 18.10.2016.
 */

public class NfcHelper {

    public static String ConvertByteArrayToHexString(byte[] tag) {
        String tagConverted = "";
        for(int i = 0 ; i < tag.length ; ++i) {
            tagConverted += String.format("%02X", tag[i]);
        }

        return tagConverted;
    }
}
