package com.zeiss.koch.kaffeekasse;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AbstractNfcActivity {

    public final static String EXTRA_MESSAGE_USERID = "com.zeiss.koch.kaffeekasse.USERID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            // In case we would still use the Tag Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] tagId = tag.getId();

            String userTagId = tagId.toString();
            Intent newIntent = new Intent(this, PayActivity.class);
            newIntent.putExtra(EXTRA_MESSAGE_USERID, userTagId);
            startActivity(newIntent);
        }
    }


    public void payButtonClick(View view)
    {
        Intent intent = new Intent(this, PayActivity.class);
        startActivity(intent);
    }

    public void settingsButtonClick(View view)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


}
