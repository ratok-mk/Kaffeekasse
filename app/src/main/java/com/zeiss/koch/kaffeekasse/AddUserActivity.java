package com.zeiss.koch.kaffeekasse;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AddUserActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    private SqlDatabaseHelper db;
    private String nfcId;
    private List<User> users;
    private Spinner userSpinner;
    private ArrayAdapter spinnerAdapter;
    private User currentUser;
    private List<String> registeredNfcTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        Intent intent = getIntent();
        nfcId = intent.getStringExtra(MainActivity.EXTRA_MESSAGE_NFCID);

        db = new SqlDatabaseHelper(this);
        updateUsers();
        updateUserSpinner();

        if (registeredNfcTags.contains(nfcId))
        {
            CustomToast.showText(this, "NFC Tag ist bereits registriert.", Toast.LENGTH_LONG);
            finish();
        }
    }

    private void updateUsers() {
        this.users = db.getAllUsers();
        Collections.sort(this.users, new UserComparator());
        registeredNfcTags = new ArrayList<>();
        Iterator<User> it = this.users.iterator();
        while(it.hasNext())
        {
            User user = it.next();
            if (!user.getNfcId().isEmpty()) {
                registeredNfcTags.add(user.getNfcId());
            }

            if (user.getRole() == User.Role.ADMIN || !user.getNfcId().isEmpty()) {
               it.remove();
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent,
                               View view, int pos, long id) {
        switch (parent.getId()) {
            case R.id.userSpinner:
                this.currentUser = this.users.get(pos);
                break;
        }
    }

    public void onNothingSelected(AdapterView parent) {
        switch (parent.getId()) {
            case R.id.userSpinner:
                this.currentUser = null;
                break;
        }
    }

    public void RegisterUserClick(View view) {
        if (!nfcId.isEmpty() && this.currentUser != null) {
            this.currentUser.setNfcId(nfcId);
            db.updateUser(this.currentUser);
            CharSequence message =
                String.format("Nutzer %1$s mit NFC Tag registriert.", this.currentUser.getName());
            CustomToast.showText(this, message, Toast.LENGTH_LONG);
            finish();
        }
    }

    public void AddUserClick(View view) {
        if (!nfcId.isEmpty()) {
            final EditText userEditText = (EditText) findViewById(R.id.userNameTextView);
            User newUser = new User(userEditText.getText().toString(), this.nfcId);
            db.addUser(newUser);
            CharSequence message = String.format("Nutzer %1$s hinzugefügt.", newUser.getName());
            CustomToast.showText(this, message, Toast.LENGTH_LONG);
            finish();
        }
    }

    public void CancelClick(View view) {
        finish();
    }

    private void updateUserSpinner() {
        userSpinner = (Spinner) findViewById(R.id.userSpinner);
        List<String> userStrings = new ArrayList<>();

        for (User user : users) {
            userStrings.add(user.getName());
        }

        spinnerAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, userStrings);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(spinnerAdapter);
        userSpinner.setOnItemSelectedListener(this);
    }
}
