package com.zeiss.koch.kaffeekasse;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PayActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private int currentUserIndex;
    private List<Account> accounts;
    private SqlDatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        db = new SqlDatabaseHelper(this);
        accounts = db.getAllAccounts();

        updateUserSpinner();
        if (accounts.size() > 0)
        {
            int currentUserIndex = 0;
            updateBalance(currentUserIndex);
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
        final Spinner userSpinner = (Spinner) findViewById(R.id.userSpinner);
        userSpinner.setOnItemSelectedListener(this);

        List<String> users = new ArrayList<>();
        for (Account account:accounts) {
            users.add(account.getUsername());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, users);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(adapter);
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
