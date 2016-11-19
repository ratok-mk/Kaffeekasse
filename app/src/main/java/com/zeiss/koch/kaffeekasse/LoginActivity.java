package com.zeiss.koch.kaffeekasse;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class LoginActivity extends AbstractNfcActivity {

    private Date date;
    private SqlDatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.date = new Date();
        String dateString = Formater.dateToLocalString(this.date);
        TextView textViewDate = (TextView) findViewById(R.id.textViewDate);
        textViewDate.setText(dateString);
        this.db = new SqlDatabaseHelper(this);
    }

    @Override
    protected void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            // In case we would still use the Tag Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String userTagId = NfcHelper.getId(tag);

            User user = db.getUserByNfcId(userTagId);
            if (user != null && user.isAdmin()) {
                Intent newIntent = new Intent(this, SettingsActivity.class);
                startActivity(newIntent);
            } else {
                CustomToast.showText(this, "Could not find admin with NFC ID: " + userTagId, Toast.LENGTH_LONG);
                SoundManager.getInstance().play(this, SoundManager.SoundType.DENIED);
            }
        }
    }

    public void cancelButtonClick(View view) {
        finish();
    }

    public void loginButtonClick(View view) {
        String securePassword = generatePassword(this.date);
        EditText adminPasswordText = (EditText) findViewById(R.id.adminPasswordText);
        String password = adminPasswordText.getText().toString();
//TODO        if (password.equals(securePassword))
//        {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
//        }
        finish();
    }

    private String generatePassword(Date date) {
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);
        String password = String.format(
                "%1$d%2$d%3$d",
                calendar.get(Calendar.YEAR) * 2,
                calendar.get(Calendar.MONTH) * 4,
                calendar.get(Calendar.DAY_OF_MONTH) * 8);

        return password;
    }
}
