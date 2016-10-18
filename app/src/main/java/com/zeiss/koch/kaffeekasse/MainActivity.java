package com.zeiss.koch.kaffeekasse;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AbstractNfcActivity implements AdapterView.OnItemSelectedListener{

    public final static String EXTRA_MESSAGE_USERID = "com.zeiss.koch.kaffeekasse.USERID";

    private boolean usersInitialized;
    private Spinner userSpinner;
    private List<User> users;
    private ArrayAdapter<String> spinnerAdapter;

    private SqlDatabaseHelper db;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new SqlDatabaseHelper(this);
        updateUserSpinner();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserSpinner();
    }

        @Override
    protected void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            // In case we would still use the Tag Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String userTagId = NfcHelper.ConvertByteArrayToHexString(tag.getId());

            User user = db.getUserByNfcId(userTagId);
            if (user != null)
            {
                Intent newIntent = new Intent(this, PayActivity.class);
                newIntent.putExtra(EXTRA_MESSAGE_USERID, user.getId());
                startActivity(newIntent);
            }
            else
            {
                Toast.makeText(this, "Could not find user with NFC ID: " + userTagId , Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent,
                               View view, int pos, long id) {
        if (usersInitialized == false) {
            user = this.users.get(pos);
            usersInitialized = true;
        }
        else {
            user = this.users.get(pos);
            usersInitialized = true;
            StartPayActivityWithCurrentUser();
        }
    }

    public void onNothingSelected(AdapterView parent) {
        this.user = null;
    }

    private void StartPayActivityWithCurrentUser() {
        if (user != null) {
            Intent newIntent = new Intent(this, PayActivity.class);
            newIntent.putExtra(EXTRA_MESSAGE_USERID, user.getId());
            startActivity(newIntent);
        }
    }

    private void updateUserSpinner() {
        usersInitialized = false;
        users = db.getAllUsers();
        userSpinner = (Spinner) findViewById(R.id.userSpinner);
        List<String> users = new ArrayList<>();
        for (User user : this.users) {
            users.add(user.getName());
        }
        spinnerAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, users);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(spinnerAdapter);
        userSpinner.setOnItemSelectedListener(this);
    }

    public void payButtonClick(View view)
    {
        StartPayActivityWithCurrentUser();
    }

    public void settingsButtonClick(View view)
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }


}
