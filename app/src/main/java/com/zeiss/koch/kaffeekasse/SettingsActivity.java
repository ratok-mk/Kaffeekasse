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
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SettingsActivity extends AbstractNfcActivity
        implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {

    private String currentNfcTag;
    private ListView userListView;
    private ArrayAdapter userListAdapter;
    private List<User> users;
    private List<User.Role> roles;
    private Spinner roleSpinner;
    private ArrayAdapter<String> spinnerAdapter;
    private User currentUser;
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

        userListView = (ListView) findViewById(R.id.userListView);

        db = new SqlDatabaseHelper(this);
        updateUserList();
        selectFirstListItem();
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
                updateUserList();
                break;
            case R.id.action_account:
                adminLayout.setVisibility(View.GONE);
                treasurerLayout.setVisibility(View.VISIBLE);
                updateUserList();
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
//        if (!backup.BackupIsUpToDate()) {
//            backup.Backup();
//        }
//
//        CheckBox backupCheckBox = (CheckBox) findViewById(R.id.backupCheckBox);
//        backupCheckBox.setChecked(backup.BackupIsUpToDate());
//
//        TextView backupTextView = (TextView) findViewById(R.id.dateTextView);
//        backupTextView.setText(backup.LastBackupDate());
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
            case R.id.roleSpinner:
                this.role = this.roles.get(pos);
                break;
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        switch (parent.getId()) {
            case R.id.userListView:
                this.currentUser = this.users.get(pos);
                updateView();
                break;
        }
    }

    private void updateView() {
        if (this.currentUser != null) {
            // update user name
            TextView renameText = (TextView) findViewById(R.id.userRenameText);
            renameText.setText(this.currentUser.getName());

            // TODO: update role

            TextView nfcValue = (TextView) findViewById(R.id.textNfcValue);
            final String nfcId = this.currentUser.getNfcId();
            nfcValue.setText(nfcId.isEmpty() ? getString(R.string.nfc_unset) : nfcId);

            // update balance
            Double balance = db.getBalance(this.currentUser);
            TextView balanceText = (TextView) findViewById(R.id.textBalance);

            final String formatted = Formater.valueToCurrencyString(balance);
            if (balance < 0.0) {
                balanceText.setTextAppearance(R.style.balance_minus);
            } else {
                balanceText.setTextAppearance(R.style.balance_plus);
            }
            balanceText.setText(formatted);
        }
    }

    public void onNothingSelected(AdapterView parent) {
        switch (parent.getId()) {
            case R.id.userSpinnerTreasurer:
                this.currentUser = null;
                break;
        }
    }

    private void updateUserList() {
        this.users = db.getAllUsers();
        Collections.sort(this.users, new UserComparator());

        List<String> userList = new ArrayList<>();

        for (User user : this.users) {
            userList.add(user.getName());
        }
        userListAdapter = new ArrayAdapter(this, R.layout.user_list_item, userList);
        userListView.setAdapter(userListAdapter);
        userListView.setOnItemClickListener(this);

        // select current user
        if (this.currentUser != null) {
            final String userName = this.currentUser.getName();
            for (int i = 0; i < userListAdapter.getCount(); i++) {
                if (userName.equals(userListAdapter.getItem(i).toString())) {
                    userListView.setItemChecked(i, true);
                    break;
                }
            }
        }
    }

    @Override
    protected void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            // In case we would still use the Tag Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            this.currentNfcTag = NfcHelper.getId(tag);

            TextView nfcTextView = (TextView) this.findViewById(R.id.textNfcValue);
            nfcTextView.setText(this.currentNfcTag);
        }
    }

    private void selectFirstListItem() {
        if (userListView != null) {
            if (userListView.getCount() > 0) {
                userListView.performItemClick(userListView, 0, 0);
            } else {
                AddNewUserClick(null);
            }
        }
    }

    public void AddNewUserClick(View view) {

        this.currentUser = new User("", "");
        userListView.clearChoices();
        updateView();
    }

    public void RenameUserClick(View view) {
        if (this.currentUser != null) {
            SqlDatabaseHelper db = new SqlDatabaseHelper(this);
            final EditText userEditText = (EditText) findViewById(R.id.userRenameText);
            this.currentUser.setName(userEditText.getText().toString());
            String stringFormat;
            // check empty string
            if (this.currentUser.getName().isEmpty()) {
                stringFormat = "Nutzername darf nicht leer sein.";
            }
            // TODO: check existing username
            else if (false) {
            }
            // check if user exists: rename only
            else if (this.currentUser.isPersisted()) {
                db.updateUser(this.currentUser);
                stringFormat = "Nutzer %1s wurde umbenannt.";
            }
            // new user: store in db
            else {
                db.addUser(this.currentUser);
                stringFormat = "Nutzer %1s wurde hinzugefügt.";
            }

            final String message = String.format(stringFormat, this.currentUser.getName());
            CustomToast.showText(this, message, Toast.LENGTH_LONG);
            updateUserList();
        }
    }

    public void DeleteUserClick(View view) {
        if (this.currentUser != null) {
            // TODO: show confirmation dialog
            String name = this.currentUser.getName();
            db.deleteUser(currentUser);
            this.currentUser = null;
            updateUserList();
            selectFirstListItem();

            String message = String.format("Nutzer %1s wurde gelöscht.", name);
            CustomToast.showText(this, message, Toast.LENGTH_LONG);
        }
    }

    public void UpdateUserClick(View view) {
        if (this.currentUser != null && this.currentNfcTag != null && !this.currentNfcTag.isEmpty()) {
            SqlDatabaseHelper db = new SqlDatabaseHelper(this);
            this.currentUser.setNfcId(this.currentNfcTag);
            db.updateUser(this.currentUser);

            String message = String.format(
                    "NFC ID von %1s wurde auf %2$s geändert.",
                    this.currentUser.getName(),
                    this.currentNfcTag);
            CustomToast.showText(this, message, Toast.LENGTH_LONG);
        }
    }

    public void UpdateAccountClick(View view) {
        if (this.currentUser != null) {
            final EditText amountEditText = (EditText) findViewById(R.id.amountEditText);
            String valueText = amountEditText.getText().toString();
            if (!valueText.isEmpty()) {
                Double amount = Double.parseDouble(valueText);
                if (amount != null) {
                    SqlDatabaseHelper db = new SqlDatabaseHelper(this);
                    Payment payment = new Payment(new Date(), this.currentUser.getId(), amount);
                    db.addPayment(payment);
                    Double balance = db.getBalance(this.currentUser);

                    String message = String.format(
                            "Kontostand von %1s wurde um %2$.2f€ verändert. Neuer Kontostand: %3$.2f€",
                            this.currentUser.getName(),
                            amount,
                            balance);
                    CustomToast.showText(this, message, Toast.LENGTH_LONG);
                    updateView();
                }
            }
        }
    }

    public void UpdateRoleClick(View view) {
        if (this.currentUser != null && this.role != null) {

            roleSpinner = (Spinner) findViewById(R.id.roleSpinner);
            this.currentUser.setRole(this.role);
            db.updateUser(this.currentUser);

            String message = String.format(
                    "Rolle von %1s wurde auf %2$s geändert.",
                    this.currentUser.getName(),
                    User.ConvertRoleToGuiString(this.role));
            CustomToast.showText(this, message, Toast.LENGTH_LONG);
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
        CustomToast.showText(this, message, Toast.LENGTH_LONG);
    }

    public void ExitClick(View view) {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
