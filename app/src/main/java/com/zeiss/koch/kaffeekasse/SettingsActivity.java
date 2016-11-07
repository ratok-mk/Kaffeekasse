package com.zeiss.koch.kaffeekasse;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SettingsActivity extends AbstractNfcActivity implements AdapterView.OnItemSelectedListener{


    private String currentNfcTag;
    private List<User> users;
    private List<User.Role> roles;
    private Spinner userSpinner;
    private Spinner roleSpinner;
    private ArrayAdapter<String> spinnerAdapter;
    private User user;
    private User.Role role;

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
        updateRoleSpinner();

        DBFileBackupHelper backup = new DBFileBackupHelper(this);
        CheckBackup(backup);
    }

    private void updateRoleSpinner() {
        roleSpinner = (Spinner) findViewById(R.id.roleSpinner);
        roles = new ArrayList<>();
        List<String> roleStrings = new ArrayList<>();
        for (User.Role role : User.Role.values()) {
            this.roles.add(role);
            roleStrings.add(User.ConvertRoleToGuiString(role));
        }
        spinnerAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, roleStrings);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(spinnerAdapter);
        roleSpinner.setOnItemSelectedListener(this);
    }


    private void CheckBackup(DBFileBackupHelper backup) {
        if (!backup.BackupIsUpToDate()) {
            backup.Backup();
        }

        CheckBox backupCheckBox = (CheckBox) findViewById(R.id.backupCheckBox);
        backupCheckBox.setChecked(backup.BackupIsUpToDate());

        TextView backupTextView = (TextView) findViewById(R.id.dateTextView);
        backupTextView.setText(backup.LastBackupDate());
    }

//    private void showPayments() {
//        List<Payment> payments = db.getAllPayments();
//        TextView paymentsTextView = (TextView)this.findViewById(R.id.paymentsTextView);
//        String paymentsTable = "Id;Date;UserId;Amount\n";
//        for(Payment payment:payments)
//        {
//            paymentsTable += String.format("%1$04d;%2$s;%3$04d;%4$.2f€\n", payment.getId(), payment.getDatetime(), payment.getUserid(), payment.getAmount());
//        }
//
//        paymentsTextView.setText(paymentsTable);
//    }

    public void onItemSelected(AdapterView<?> parent,
                               View view, int pos, long id) {
        switch(parent.getId())
        {
            case R.id.userSpinner:
                user = this.users.get(pos);
                break;
            case R.id.roleSpinner:
                role = this.roles.get(pos);
                break;
        }

    }

    public void onNothingSelected(AdapterView parent) {
        switch(parent.getId()) {
            case R.id.userSpinner:
                this.user = null;
                break;
        }
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

    public void DeleteUserClick(View view) {
        if (this.user != null)
        {
            db.deleteUser(user);
            this.user = null;
            updateUserSpinner();
        }
    }

    public void UpdateUserClick(View view)
    {
        if (this.user != null && !this.currentNfcTag.isEmpty()) {
            SqlDatabaseHelper db = new SqlDatabaseHelper(this);
            this.user.SetNfcId(this.currentNfcTag);
            db.updateUser(this.user);
        }
    }

    public void UpdateAccountClick(View view) {
        if (this.user != null) {
            final EditText amountEditText = (EditText) findViewById(R.id.amountEditText);
            String valueText = amountEditText.getText().toString();
            if (!valueText.isEmpty()) {
                Double amount = Double.parseDouble(valueText);
                if (amount != null) {
                    SqlDatabaseHelper db = new SqlDatabaseHelper(this);
                    Payment payment = new Payment(new Date(), this.user.getId(), amount);
                    db.addPayment(payment);
                }
            }
        }
    }

    public void UpdateRoleClick(View view)
    {
        if (this.user != null && this.role != null) {

            roleSpinner = (Spinner) findViewById(R.id.roleSpinner);
            this.user.setRole(this.role);
            db.updateUser(this.user);
        }
    }

    public void RestoreDatabaseClick(View view)
    {
        File mPath = DBFileBackupHelper.BackupDirectory();
        FileDialog fileDialog = new FileDialog(this, mPath, null);
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                Log.d(getClass().getName(), "selected file " + file.toString());
                restoreDatabase(file);
            }
        });

        fileDialog.showDialog();
    }

    private void restoreDatabase(File file) {
        DBFileBackupHelper backup = new DBFileBackupHelper(this);
        backup.Restore(file);
    }

    public void ExitClick(View view)
    {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
