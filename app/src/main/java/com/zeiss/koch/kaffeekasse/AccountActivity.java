package com.zeiss.koch.kaffeekasse;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AccountActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        updateUserSpinner();
        updateBalance(0);
    }

    public void onItemSelected(AdapterView<?> parent,
                               View view, int pos, long id) {
        updateBalance(pos);
    }

    public void onNothingSelected(AdapterView parent) {
        TextView balanceText = (TextView) findViewById(R.id.balanceTextView);
        balanceText.setText("");
    }

    private void updateBalance(int position) {
        final Spinner userSpinner = (Spinner) findViewById(R.id.userSpinner);
        SqlDatabaseHelper db = new SqlDatabaseHelper(this);
        List<Account> accounts = db.getAllAccounts();

        if (position < accounts.size()) {
            Double balance = accounts.get(position).getBalance();
            TextView balanceText = (TextView) findViewById(R.id.balanceTextView);
            balanceText.setText(balance.toString());
        }
    }

    private void updateUserSpinner() {
        SqlDatabaseHelper db = new SqlDatabaseHelper(this);
        final Spinner userSpinner = (Spinner) findViewById(R.id.userSpinner);

        List<Account> accounts = db.getAllAccounts();
        List<String> users = new ArrayList<>();
        for (Account account:accounts
                ) {
            users.add(account.getUsername());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, users);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(adapter);
    }
}
