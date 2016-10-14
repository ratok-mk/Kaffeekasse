package com.zeiss.koch.kaffeekasse;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PayActivity extends AppCompatActivity {


    private User currentUser;

    private SqlDatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        db = new SqlDatabaseHelper(this);

        Intent intent = getIntent();
        int userId = intent.getIntExtra(MainActivity.EXTRA_MESSAGE_USERID, -1);
        if (userId != -1) {
            this.currentUser = db.getUser(userId);
            updateUsername(this.currentUser);
            updateBalance(this.currentUser);
        }
    }

    private void updateUsername(User user) {
        TextView usernameText = (TextView) findViewById(R.id.usernametextView);
        usernameText.setText(user.getName());
    }


    private void updateBalance(User user) {
            Double balance = db.getBalance(user);
            TextView balanceText = (TextView) findViewById(R.id.balanceTextView);

            DecimalFormat round = new DecimalFormat("0.00");
            String formatted = round.format(balance);

            balanceText.setText(formatted);
    }

    public void pay10Click(View view)
    {
        java.util.Date currentDate = new java.util.Date();
        Payment payment = new Payment(new Date(currentDate.getTime()), currentUser.getId(), 0.10);
        db.addPayment(payment);
        updateBalance(currentUser);
    }

    public void pay20Click(View view)
    {
        java.util.Date currentDate = new java.util.Date();
        Payment payment = new Payment(new Date(currentDate.getTime()), currentUser.getId(), 0.20);
        db.addPayment(payment);
        updateBalance(currentUser);
    }

    public void pay40Click(View view)
    {
        java.util.Date currentDate = new java.util.Date();
        Payment payment = new Payment(new Date(currentDate.getTime()), currentUser.getId(), 0.40);
        db.addPayment(payment);
        updateBalance(currentUser);
    }
}
