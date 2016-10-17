package com.zeiss.koch.kaffeekasse;

import android.app.DialogFragment;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AbstractNfcActivity {


    private String currentNfcTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        updateUserSpinner();
    }

    @Override
    protected void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            // In case we would still use the Tag Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] tagId = tag.getId();

            this.currentNfcTag  = tagId.toString();

            TextView nfcTextView = (TextView)this.findViewById(R.id.nfcTextView);
            nfcTextView.setText(this.currentNfcTag);
        }
    }

    private void updateUserSpinner() {
        SqlDatabaseHelper db = new SqlDatabaseHelper(this);
        final Spinner userSpinner = (Spinner) findViewById(R.id.userSpinner);

        List<User> accounts = db.getAllUsers();
        List<String> users = new ArrayList<>();
        for (User user :accounts
                ) {
            users.add(user.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, users);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(adapter);
    }

    public void onAddUserClick( View view)
    {
        SqlDatabaseHelper db = new SqlDatabaseHelper(this);
        final EditText userEditText = (EditText) findViewById(R.id.userEditText);
        User newUser = new User(userEditText.getText().toString(), "");
        db.addUser(newUser);

        updateUserSpinner();
    }
}
