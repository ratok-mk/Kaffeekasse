package com.zeiss.koch.kaffeekasse;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AbstractNfcActivity implements AdapterView.OnItemSelectedListener{

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public final static String EXTRA_MESSAGE_USERID = "com.zeiss.koch.kaffeekasse.USERID";
    private static final String DATABASE = "DB_Backup";

    private boolean usersInitialized;
    private Spinner userSpinner;
    private List<User> users;
    private ArrayAdapter<String> spinnerAdapter;

    private SqlDatabaseHelper db;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);
        setContentView(R.layout.activity_main);
        DBFileBackupHelper backup = new DBFileBackupHelper(this);

        CheckBackup(backup);


        db = new SqlDatabaseHelper(this);
        updateUserSpinner();
    }

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
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

    @Override
    public void onResume() {
        super.onResume();
        updateUserSpinner();
    }

        @Override
    protected void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            // In case we would still use the Tag Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String userTagId = NfcHelper.ConvertByteArrayToHexString(tag.getId());

            User user = db.getUserByNfcId(userTagId);
            if (user != null)
            {
                Intent newIntent = new Intent(this, PayActivity.class);
                newIntent.putExtra(EXTRA_MESSAGE_USERID, user.getId());
                startActivity(newIntent);
            }
            else
            {
                Toast.makeText(this, "Could not find user with NFC ID: " + userTagId , Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent,
                               View view, int pos, long id) {
        if (usersInitialized == false) {
            user = this.users.get(pos);
            usersInitialized = true;
        }
        else {
            user = this.users.get(pos);
            usersInitialized = true;
            StartPayActivityWithCurrentUser();
        }
    }

    public void onNothingSelected(AdapterView parent) {
        this.user = null;
    }

    private void StartPayActivityWithCurrentUser() {
        if (user != null) {
            Intent newIntent = new Intent(this, PayActivity.class);
            newIntent.putExtra(EXTRA_MESSAGE_USERID, user.getId());
            startActivity(newIntent);
        }
    }

    private void updateUserSpinner() {
        usersInitialized = false;
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

    public void payButtonClick(View view)
    {
        StartPayActivityWithCurrentUser();
    }

    public void settingsButtonClick(View view)
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }


}
