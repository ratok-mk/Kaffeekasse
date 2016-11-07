package com.zeiss.koch.kaffeekasse;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

public class MainActivity extends AbstractNfcActivity implements AdapterView.OnItemClickListener{

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public final static String EXTRA_MESSAGE_USERID = "com.zeiss.koch.kaffeekasse.USERID";
    public final static String EXTRA_MESSAGE_NFCID = "com.zeiss.koch.kaffeekasse.NFCID";

    private ListView userList;
    private List<User> users;
    private CustomUserListAdapter userListAdapter;

    private SqlDatabaseHelper db;
    private User user;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);
        setContentView(R.layout.activity_main);

        backupDatabase();
        db = new SqlDatabaseHelper(this);
        updateUserList();
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

    private void backupDatabase() {
        DBFileBackupHelper backup = new DBFileBackupHelper(this);
        backup.Backup();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserList();
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
                Intent newIntent = new Intent(this, AddUserActivity.class);
                newIntent.putExtra(EXTRA_MESSAGE_NFCID, userTagId);
                startActivity(newIntent);
            }
        }
    }

    public void onItemClick(AdapterView<?> parent,
                               View view, int pos, long id) {
        user = this.users.get(pos);
        StartPayActivityWithCurrentUser();
    }

    private void StartPayActivityWithCurrentUser() {
        if (user != null) {
            Intent newIntent = new Intent(this, PayActivity.class);
            newIntent.putExtra(EXTRA_MESSAGE_USERID, user.getId());
            startActivity(newIntent);
        }
    }

    private void updateUserList() {
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mTitle = getTitle().toString();
        setupDrawer();

        users = db.getAllUsers();
        userList = (ListView) findViewById(R.id.userList);
        userListAdapter = new CustomUserListAdapter(this, users);
        userList.setAdapter(userListAdapter);
        userList.setOnItemClickListener(this);
    }

    private void setupDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final String title = "Nutzerliste";
        getSupportActionBar().setTitle(title);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(title);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(title);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
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
