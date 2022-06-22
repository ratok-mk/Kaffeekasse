package com.zeiss.koch.kaffeekasse;

import android.nfc.Tag;

/**
 * ${FILENAME}
 * Created by koch on 18.10.2016.
 */

public class NfcHelper {

    public static String getId(Tag nfcTag) {
        byte[] tag = nfcTag.getId();
        StringBuilder tagConverted = new StringBuilder();
        for(int i = 0 ; i < tag.length ; ++i) {
            tagConverted.append(String.format("%02X", tag[i]));
        }

        return tagConverted.toString();
    }
}
