package com.zeiss.koch.kaffeekasse;

import android.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements LoginDialogFragment.NoticeDialogListener{


    public void showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new LoginDialogFragment();
        dialog.show(getFragmentManager(), "LoginDialogFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        EditText adminPasswordText = (EditText)dialog.getDialog().findViewById(R.id.adminPasswordEditText);
        String password = adminPasswordText.getText().toString();
        if (!password.equals("admin"))
        {
            finish();
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        showNoticeDialog();

        updateUserSpinner();
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
