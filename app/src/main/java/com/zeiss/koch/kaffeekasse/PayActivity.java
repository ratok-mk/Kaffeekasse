package com.zeiss.koch.kaffeekasse;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PayActivity extends AbstractNfcActivity implements AdapterView.OnItemSelectedListener{


    private int currentUserIndex;
    private List<Account> accounts;
    private SqlDatabaseHelper db;

    private Spinner userSpinner;
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        db = new SqlDatabaseHelper(this);
        accounts = db.getAllAccounts();

        updateUserSpinner();

        Intent intent = getIntent();
        String nfcUserId = intent.getStringExtra(MainActivity.EXTRA_MESSAGE_USERID);
        if (nfcUserId != null){
            updateCurrentUser(nfcUserId);
        }
    }

    private void updateCurrentUser(String nfcUserId) {
        if (accounts.size() > 0)
        {
            boolean userFound = false;
            int i = 0;
            for (Account account:accounts)
            {
                if (nfcUserId.equals(account.getNfcId()))
                {
                    this.currentUserIndex = i;
                    userFound = true;
                }

                i++;
            }

            if (userFound) {
                updateBalance(this.currentUserIndex);
            }
            else
            {
                Toast.makeText(this, "Could not find user with NFC ID: " + nfcUserId , Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            // In case we would still use the Tag Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] tagId = tag.getId();
            String userTagId = tagId.toString();
            Toast.makeText(this, "NFC ID found: " + userTagId , Toast.LENGTH_LONG).show();
            updateCurrentUser(userTagId);
        }
    }

    public void onItemSelected(AdapterView<?> parent,
                               View view, int pos, long id) {
        this.currentUserIndex = pos;
        updateBalance(pos);
    }

    public void onNothingSelected(AdapterView parent) {
        TextView balanceText = (TextView) findViewById(R.id.balanceTextView);
        balanceText.setText("");
    }

    private void updateBalance(int position) {
        if (position < accounts.size()) {
            Double balance = accounts.get(position).getBalance();
            TextView balanceText = (TextView) findViewById(R.id.balanceTextView);

            DecimalFormat round = new DecimalFormat("0.00");
            String formatted = round.format(balance);

            balanceText.setText(formatted);
        }
    }

    private void updateUserSpinner() {
        userSpinner = (Spinner) findViewById(R.id.userSpinner);
        userSpinner.setOnItemSelectedListener(this);

        List<String> users = new ArrayList<>();
        for (Account account:accounts) {
            users.add(account.getUsername());
        }
        spinnerAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, users);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(spinnerAdapter);
    }

    public void pay10Click(View view)
    {
        Account user = accounts.get(currentUserIndex);
        double balanceNew = user.getBalance() - 0.10;
        user.setBalance(balanceNew);
        db.updateAccount(user);
        updateBalance(currentUserIndex);
    }

    public void pay20Click(View view)
    {
        Account user = accounts.get(currentUserIndex);
        double balanceNew = user.getBalance() - 0.20;
        user.setBalance(balanceNew);
        db.updateAccount(user);
        updateBalance(currentUserIndex);
    }

    public void pay40Click(View view)
    {
        Account user = accounts.get(currentUserIndex);
        double balanceNew = user.getBalance() - 0.40;
        user.setBalance(balanceNew);
        db.updateAccount(user);
        updateBalance(currentUserIndex);
    }
}
