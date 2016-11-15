package com.zeiss.koch.kaffeekasse;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SettingsActivity extends AbstractNfcActivity implements AdapterView.OnItemSelectedListener {


    private String currentNfcTag;
    private List<User> users;
    private List<User.Role> roles;
    private Spinner userSpinner;
    private Spinner userSpinnerTreasurer;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        GridLayout adminLayout = (GridLayout) findViewById(R.id.adminLayout);
        GridLayout treasurerLayout = (GridLayout) findViewById(R.id.treasurerLayout);
        switch (item.getItemId()) {
            case R.id.action_user:
                adminLayout.setVisibility(View.VISIBLE);
                treasurerLayout.setVisibility(View.GONE);
                updateUserSpinner();
                break;
            case R.id.action_account:
                adminLayout.setVisibility(View.GONE);
                treasurerLayout.setVisibility(View.VISIBLE);
                updateUserSpinner();
                break;
        }
        return true;
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
        switch (parent.getId()) {
            case R.id.userSpinner:
            case R.id.userSpinnerTreasurer:
                user = this.users.get(pos);
                UpdateRenameText();
                break;
           case R.id.roleSpinner:
                role = this.roles.get(pos);
                break;
        }
    }

    private void UpdateRenameText() {
        if (this.user != null) {
            TextView renameText = (TextView) this.findViewById(R.id.userRenameText);
            renameText.setText(this.user.getName());
        }
    }

    public void onNothingSelected(AdapterView parent) {
        switch (parent.getId()) {
            case R.id.userSpinner:
            case R.id.userSpinnerTreasurer:
                this.user = null;
                break;
        }
    }

    private void updateUserSpinner() {

        users = db.getAllUsers();
        userSpinner = (Spinner) findViewById(R.id.userSpinner);
        userSpinnerTreasurer = (Spinner) findViewById(R.id.userSpinnerTreasurer);

        List<String> users = new ArrayList<>();
        for (User user : this.users) {
            users.add(user.getName());
        }
        spinnerAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, users);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(spinnerAdapter);
        userSpinner.setOnItemSelectedListener(this);
        userSpinnerTreasurer.setAdapter(spinnerAdapter);
        userSpinnerTreasurer.setOnItemSelectedListener(this);
    }

    @Override
    protected void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            // In case we would still use the Tag Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            this.currentNfcTag = NfcHelper.ConvertByteArrayToHexString(tag.getId());

            TextView nfcTextView = (TextView) this.findViewById(R.id.nfcTextView);
            nfcTextView.setText(this.currentNfcTag);
        }
    }

    public void AddNewUserClick(View view) {
        SqlDatabaseHelper db = new SqlDatabaseHelper(this);
        final EditText userEditText = (EditText) findViewById(R.id.userEditText);
        User newUser = new User(userEditText.getText().toString(), "");
        db.addUser(newUser);
        updateUserSpinner();


        String message = String.format("Nutzer %1s wurde hinzugefügt.", newUser.getName());
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void RenameUserClick(View view) {
        if (this.user != null) {
            SqlDatabaseHelper db = new SqlDatabaseHelper(this);
            final EditText userEditText = (EditText) findViewById(R.id.userRenameText);
            this.user.setName(userEditText.getText().toString());
            db.updateUser(this.user);
            updateUserSpinner();

            String message = String.format("Nutzer %1s wurde umbenannt.", this.user.getName());
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    public void DeleteUserClick(View view) {
        if (this.user != null) {
            String name = this.user.getName();
            db.deleteUser(user);
            this.user = null;
            updateUserSpinner();

            String message = String.format("Nutzer %1s wurde gelöscht.", name);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    public void UpdateUserClick(View view) {
        if (this.user != null && this.currentNfcTag != null && !this.currentNfcTag.isEmpty()) {
            SqlDatabaseHelper db = new SqlDatabaseHelper(this);
            this.user.SetNfcId(this.currentNfcTag);
            db.updateUser(this.user);

            String message = String.format(
                    "NFC ID von %1s wurde auf %2$s geändert.",
                    this.user.getName(),
                    this.currentNfcTag);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
                    Double balance = db.getBalance(this.user);

                    String message = String.format(
                            "Kontostand von %1s wurde um %2$.2f€ verändert. Neuer Kontostand: %3$.2f€",
                            this.user.getName(),
                            amount,
                            balance);
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void UpdateRoleClick(View view) {
        if (this.user != null && this.role != null) {

            roleSpinner = (Spinner) findViewById(R.id.roleSpinner);
            this.user.setRole(this.role);
            db.updateUser(this.user);

            String message = String.format(
                    "Rolle von %1s wurde auf %2$s geändert.",
                    this.user.getName(),
                    User.ConvertRoleToGuiString(this.role));
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    public void RestoreDatabaseClick(View view) {
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

        String message = String.format(
                "Datenbank wurde aus Datei %1$s wiederhergestellt.",
                file.getAbsolutePath());
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void ExitClick(View view) {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
