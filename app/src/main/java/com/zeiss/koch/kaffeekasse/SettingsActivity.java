package com.zeiss.koch.kaffeekasse;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
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
    private ViewGroup userCreditLayout;
    private ViewGroup userDetailsLayout;
    private ListView userListView;
    private List<User> users;
    private List<User.Role> roles;
    private Spinner roleSpinner;
    private User currentUser;
    private User.Role role;

    private boolean isInFocus = false;
    private SqlDatabaseHelper db;

    private Double chargeAmount;

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

        userCreditLayout = (ViewGroup) findViewById(R.id.userCreditLayout);
        userDetailsLayout = (ViewGroup) findViewById(R.id.userDetailsLayout);
        userListView = (ListView) findViewById(R.id.userListView);

        this.db = new SqlDatabaseHelper(this);
        setupRoleSpinner();
        updateUserList();
        selectFirstListItem();

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
        ViewGroup userManagementLayout = (ViewGroup) findViewById(R.id.userManagementLayout);
        ViewGroup systemSettingsLayout = (ViewGroup) findViewById(R.id.systemSettingsLayout);
        switch (item.getItemId()) {
            case R.id.action_settings_user:
                showUserDetailsView();
                systemSettingsLayout.setVisibility(View.GONE);
                userManagementLayout.setVisibility(View.VISIBLE);
                updateUserList();
                selectFirstListItem();
                break;
            case R.id.action_settings_system:
                userManagementLayout.setVisibility(View.GONE);
                systemSettingsLayout.setVisibility(View.VISIBLE);
                setTitle(getString(R.string.app_name) + " / Systemeinstellungen" );
                break;
            case R.id.action_exit_application:
                exitApplication();
                break;
        }
        return true;
    }

    private void setupRoleSpinner() {
        roleSpinner = (Spinner) findViewById(R.id.roleSpinner);
        roles = new ArrayList<>();
        List<String> roleStrings = new ArrayList<>();
        for (User.Role role : User.Role.values()) {
            this.roles.add(role);
            roleStrings.add(User.ConvertRoleToGuiString(role));
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter(this,
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
            case R.id.roleSpinner:
                this.role = this.roles.get(pos);
                break;
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        switch (parent.getId()) {
            case R.id.userListView:
                SoundManager.getInstance().play(this, SoundManager.SoundType.BUTTON);
                showUserDetailsView();
                this.currentUser = this.users.get(pos);
                setCurrentUserToView();
                setTitle(getString(R.string.app_name) + " / Nutzer verwalten");
                break;
        }
    }

    private void setCurrentUserToView() {
        if (this.currentUser != null) {
            // update user name
            TextView renameText = (TextView) findViewById(R.id.userRenameText);
            renameText.setText(this.currentUser.getName());

            // update role
            final String role = User.ConvertRoleToGuiString(this.currentUser.getRole());
            SpinnerAdapter spinnerAdapter = roleSpinner.getAdapter();
            for (int i = 0; i < spinnerAdapter.getCount(); i++) {
                if (role.equals(spinnerAdapter.getItem(i).toString())) {
                    roleSpinner.performItemClick(roleSpinner, i, 0);
                    roleSpinner.setSelection(i);
                    break;
                }
            }

            TextView nfcValue = (TextView) findViewById(R.id.textNfcValue);
            final String nfcId = this.currentUser.getNfcId();
            nfcValue.setText(nfcId.isEmpty() ? getString(R.string.nfc_unset) : nfcId);

            // update balance
            Double balance = this.db.getBalance(this.currentUser);
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
    }

    private void updateUserList() {
        this.users = this.db.getAllUsers();
        Collections.sort(this.users, new UserComparator());

        List<String> userList = new ArrayList<>();

        for (User user : this.users) {
            userList.add(user.getName());
        }
        ArrayAdapter userListAdapter = new ArrayAdapter(this, R.layout.user_list_item, userList);
        userListView.setAdapter(userListAdapter);
        userListView.setOnItemClickListener(this);

        // select current user
        if (this.currentUser != null) {
            final String userName = this.currentUser.getName();
            for (int i = 0; i < userListAdapter.getCount(); i++) {
                if (userName.equals(userListAdapter.getItem(i).toString())) {
                    userListView.performItemClick(userListView, i, 0);
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
                newUserClick(null);
            }
        }
    }

    public void newUserClick(View view) {
        this.currentUser = new User("", "");
        setTitle(getString(R.string.app_name) + " / Nutzer anlegen");
        userListView.clearChoices();
        userListView.requestLayout();
        setCurrentUserToView();
        SoundManager.getInstance().play(this, SoundManager.SoundType.BUTTON);
    }

    public void renameUserClick(View view) {
        if (this.currentUser != null) {
            final EditText userEditText = (EditText) findViewById(R.id.userRenameText);
            final String name = userEditText.getText().toString();
            this.currentUser.setName(name);
            String stringFormat;
            SoundManager.SoundType sound;
            // check empty string
            if (this.currentUser.getName().isEmpty()) {
                stringFormat = "Nutzername darf nicht leer sein.";
                sound = SoundManager.SoundType.DENIED;
            } else if (this.db.getUserByName(name) != null) {
                stringFormat = "Nutzername exisitert bereits.";
                sound = SoundManager.SoundType.DENIED;
            }
            // check if user exists: rename only
            else if (this.currentUser.isPersisted()) {
                this.db.updateUser(this.currentUser);
                updateUserList();
                stringFormat = "Nutzer %1s wurde umbenannt.";
                sound = SoundManager.SoundType.BUTTON;
            }
            // new user: store in db
            else {
                this.db.addUser(this.currentUser);
                updateUserList();
                stringFormat = "Nutzer %1s wurde hinzugefügt.";
                sound = SoundManager.SoundType.BUTTON;
            }

            final String message = String.format(stringFormat, name);
            SoundManager.getInstance().play(this, sound);
            CustomToast.showText(this, message, Toast.LENGTH_LONG);
        }
    }

    public void deleteUserClick(View view) {
        if (this.currentUser != null) {
            if (this.currentUser.isPersisted()) {
                SoundManager.getInstance().play(this, SoundManager.SoundType.BUTTON);
                String name = this.currentUser.getName();
                new AlertDialog.Builder(this)
                        .setTitle("Nutzer löschen")
                        .setMessage(String.format("Soll %1s wirklich gelöscht werden?", name))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                SettingsActivity.this.deleteUser();
                            }
                        })
                        .setNegativeButton(R.string.no, null).show();
            }
            else {
                CustomToast.showText(this, getString(R.string.msg_save_user_first), Toast.LENGTH_LONG);
                SoundManager.getInstance().play(this, SoundManager.SoundType.DENIED);
            }
        }
    }

    private void deleteUser() {
        String name = this.currentUser.getName();
        this.db.deleteUser(this.currentUser);
        this.currentUser = null;
        updateUserList();
        selectFirstListItem();

        String message = String.format("Nutzer %1s wurde gelöscht.", name);
        CustomToast.showText(this, message, Toast.LENGTH_LONG);
        SoundManager.getInstance().play(this, SoundManager.SoundType.BUTTON);
    }

    public void updateNfcClick(View view) {
        if (this.currentUser != null && this.currentNfcTag != null && !this.currentNfcTag.isEmpty()) {
            if (this.currentUser.isPersisted()) {
                this.currentUser.setNfcId(this.currentNfcTag);
                this.db.updateUser(this.currentUser);

                String message = String.format(
                        "NFC ID von %1s wurde auf %2$s geändert.",
                        this.currentUser.getName(),
                        this.currentNfcTag);
                CustomToast.showText(this, message, Toast.LENGTH_LONG);
                SoundManager.getInstance().play(this, SoundManager.SoundType.BUTTON);
            }
            else {
                CustomToast.showText(this, getString(R.string.msg_save_user_first), Toast.LENGTH_LONG);
                SoundManager.getInstance().play(this, SoundManager.SoundType.DENIED);
            }
        }
    }

    public void showChargeCreditViewClick(View view) {
        if (this.currentUser != null && this.role != null) {
            if (this.currentUser.isPersisted()) {
                setupChargeCreditView();
                showChargeCreditView();
            }
            else {
                CustomToast.showText(this, getString(R.string.msg_save_user_first), Toast.LENGTH_LONG);
                SoundManager.getInstance().play(this, SoundManager.SoundType.DENIED);
            }
        }
    }

    private void showChargeCreditView() {
        userDetailsLayout.setVisibility(View.GONE);
        userCreditLayout.setVisibility(View.VISIBLE);
    }

    private void showUserDetailsView() {
        userCreditLayout.setVisibility(View.GONE);
        userDetailsLayout.setVisibility(View.VISIBLE);
    }

    public void updateRoleClick(View view) {
        if (this.currentUser != null && this.role != null) {

            if (this.currentUser.isPersisted()) {
                roleSpinner = (Spinner) findViewById(R.id.roleSpinner);
                this.currentUser.setRole(this.role);
                this.db.updateUser(this.currentUser);

                String message = String.format(
                        "Rolle von %1s wurde auf %2$s geändert.",
                        this.currentUser.getName(),
                        User.ConvertRoleToGuiString(this.role));
                CustomToast.showText(this, message, Toast.LENGTH_LONG);
                SoundManager.getInstance().play(this, SoundManager.SoundType.BUTTON);
            }
            else {
                CustomToast.showText(this, getString(R.string.msg_save_user_first), Toast.LENGTH_LONG);
                SoundManager.getInstance().play(this, SoundManager.SoundType.DENIED);
            }
        }
    }

    private void setupChargeCreditView() {
        if (this.currentUser != null) {
            Double balance = this.db.getBalance(this.currentUser);
            if (balance != null) {
                TextView balanceText = (TextView) findViewById(R.id.chargeBalanceTextView);
                final String formatted = Formater.valueToCurrencyString(balance);
                balanceText.setText(formatted);
            }

            chargeAmount = 0.0;
            updateChargeView(false);
        }
    }

    private void updateChargeView(boolean animate) {
        TextView chargeAmountText = (TextView) findViewById(R.id.chargeAmountTextView);
        final String formatted = Formater.valueToCurrencyString(this.chargeAmount);
        chargeAmountText.setText(formatted);
        if (animate) {
            AnimationHandler.highlight(this, chargeAmountText);
        }
        SoundManager.getInstance().play(this, SoundManager.SoundType.BUTTON);
    }

    public void note5Click(View view) {
        chargeAmount += 5.0;
        updateChargeView(true);
    }

    public void note10Click(View view) {
        chargeAmount += 10.0;
        updateChargeView(true);
    }

    public void note20Click(View view) {
        chargeAmount += 20.0;
        updateChargeView(true);
    }

    public void note50Click(View view) {
        chargeAmount += 50.0;
        updateChargeView(true);
    }

    public void manualAmountClick(View view) {
        EditText manualAmountText = (EditText) findViewById(R.id.manualAmount);
        try {
            Double manualAmount = Double.parseDouble(manualAmountText.getText().toString());
            if (manualAmount != null) {
                chargeAmount += manualAmount.doubleValue();
                manualAmountText.setText("");
                updateChargeView(true);
            }
        } catch (NumberFormatException e) {
        }
    }

    public void chargeCreditClick(View view) {
        if (this.currentUser != null) {
            if (this.chargeAmount > 0.0) {
                Payment payment = new Payment(new Date(), this.currentUser.getId(), chargeAmount);
                this.db.addPayment(payment);
                Double balance = this.db.getBalance(this.currentUser);

                String message = String.format(
                        "Guthaben von %1s wurde um %2s aufgeladen.\nNeuer Kontostand: %3s",
                        this.currentUser.getName(),
                        Formater.valueToCurrencyString(chargeAmount),
                        Formater.valueToCurrencyString(balance)
                );
                SoundManager.getInstance().play(this, SoundManager.SoundType.PAY);
                CustomToast.showText(this, message, Toast.LENGTH_LONG);
                // update and got back to user details view
                setCurrentUserToView();
                showUserDetailsView();
                // animate updated balance
                TextView balanceText = (TextView) findViewById(R.id.textBalance);
                AnimationHandler.highlight(this, balanceText);
            } else {
                SoundManager.getInstance().play(this, SoundManager.SoundType.DENIED);
                CustomToast.showText(this, "Betrag muss größer als 0,00 € sein!", Toast.LENGTH_LONG);
            }
        }
    }

    public void restoreDatabaseClick(View view) {
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

    public void exitApplication() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
