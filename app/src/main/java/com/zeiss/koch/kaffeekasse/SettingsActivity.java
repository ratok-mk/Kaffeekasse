package com.zeiss.koch.kaffeekasse;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SettingsActivity extends AbstractNfcActivity implements AdapterView.OnItemSelectedListener{


    private String currentNfcTag;
    private List<User> users;
    private Spinner userSpinner;
    private ArrayAdapter<String> spinnerAdapter;
    private User user;

    private boolean isInFocus = false;
    private SqlDatabaseHelper db;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        isInFocus = hasFocus;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!isInFocus) finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        db = new SqlDatabaseHelper(this);
        updateUserSpinner();
        showPayments();
    }

    private void showPayments() {
        List<Payment> payments = db.getAllPayments();
        TextView paymentsTextView = (TextView)this.findViewById(R.id.paymentsTextView);
        String paymentsTable = "Id;Date;UserId;Amount\n";
        for(Payment payment:payments)
        {
            paymentsTable += String.format("%1$04d;%2$s;%3$04d;%4$.2f€\n", payment.getId(), payment.getDatetime(), payment.getUserid(), payment.getAmount());
        }

        paymentsTextView.setText(paymentsTable);
    }

    public void onItemSelected(AdapterView<?> parent,
                               View view, int pos, long id) {
        user = this.users.get(pos);
    }

    public void onNothingSelected(AdapterView parent) {
        this.user = null;
    }

    private void updateUserSpinner() {

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

    @Override
    protected void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            // In case we would still use the Tag Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            this.currentNfcTag  = NfcHelper.ConvertByteArrayToHexString(tag.getId());

            TextView nfcTextView = (TextView)this.findViewById(R.id.nfcTextView);
            nfcTextView.setText(this.currentNfcTag);
        }
    }

    public void AddNewUserClick(View view)
    {
        SqlDatabaseHelper db = new SqlDatabaseHelper(this);
        final EditText userEditText = (EditText) findViewById(R.id.userEditText);
        User newUser = new User(userEditText.getText().toString(), "");
        db.addUser(newUser);

        updateUserSpinner();
    }

    public void UpdateUserClick(View view)
    {
        if (this.user != null && !this.currentNfcTag.isEmpty()) {
            SqlDatabaseHelper db = new SqlDatabaseHelper(this);
            this.user.SetNfcId(this.currentNfcTag);
            db.updateUser(this.user);
        }
    }

    public void UpdateAccountClick(View view)
    {
        if (this.user != null) {
            final EditText amountEditText = (EditText) findViewById(R.id.amountEditText);
            Double amount = Double.parseDouble(amountEditText.getText().toString());
            if (amount != null) {
                SqlDatabaseHelper db = new SqlDatabaseHelper(this);
                Payment payment = new Payment(new Date(), this.user.getId(), amount);
                db.addPayment(payment);
                showPayments();
            }
        }
    }
}
