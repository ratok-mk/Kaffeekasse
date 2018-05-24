package com.zeiss.koch.kaffeekasse;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AbstractNfcActivity implements AdapterView.OnItemClickListener {
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

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout drawerLayout;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        this.drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setVersionInfo();
        backupDatabase();
        this.db = new SqlDatabaseHelper(this);
        updateUserList();
        EditText editText = (EditText) findViewById(R.id.searchFilter);
        editText.setText("");

        startTimerTask();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserList();
        backupDatabase();
        EditText editText = (EditText) findViewById(R.id.searchFilter);
        editText.setText("");

        if (this.timer == null) {
            startTimerTask();
        }
    }

    private void startTimerTask() {
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerHandler.obtainMessage(1).sendToTarget();
            }
        }, 0, 1000);
    }

    private Handler timerHandler = new Handler() {
        public void handleMessage(Message msg) {
            setActionBarTitle();
        }
    };

    /**
     * Checks if the app has permission to write to device storage
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
    protected void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            // In case we would still use the Tag Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String userTagId = NfcHelper.getId(tag);

            this.drawerLayout.closeDrawers();
            SoundManager.getInstance().play(this, SoundManager.SoundType.NFC);
            User user = this.db.getUserByNfcId(userTagId);
            if (user != null) {
                Intent newIntent = new Intent(this, PayActivity.class);
                newIntent.putExtra(EXTRA_MESSAGE_USERID, user.getId());
                startActivity(newIntent);
            } else {
                Intent newIntent = new Intent(this, RegisterUserActivity.class);
                newIntent.putExtra(EXTRA_MESSAGE_NFCID, userTagId);
                startActivity(newIntent);
            }
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        User user = (User)parent.getItemAtPosition(pos);
        if (user != null) {
            this.drawerLayout.closeDrawers();
            SoundManager.getInstance().play(this, SoundManager.SoundType.BUTTON);
            Intent newIntent = new Intent(this, PayActivity.class);
            newIntent.putExtra(EXTRA_MESSAGE_USERID, user.getId());
            startActivity(newIntent);
        }
    }

    private void updateUserList() {
        setupDrawer();

        this.users = this.db.getAllUsers();
        Collections.sort(this.users, new UserComparator());
        userList = (ListView) findViewById(R.id.userList);
        userListAdapter = new CustomUserListAdapter(this, this.users);
        userList.setAdapter(userListAdapter);
        userList.setOnItemClickListener(this);

        EditText editText = (EditText) findViewById(R.id.searchFilter);
        editText.setOnEditorActionListener(new DoneOnEditorActionListener());
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userListAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupDrawer() {
        final String title = getResources().getString(R.string.userlist);
        setActionBarTitle();
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                this.drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                setActionBarTitle();
                EditText editText = (EditText) findViewById(R.id.searchFilter);
                InputMethodManager imm = (InputMethodManager)editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                setActionBarTitle();
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void setActionBarTitle() {
        final String title = getResources().getString(R.string.userlist);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title + " - " + Formater.timeToLocalString(new java.util.Date()));
        }
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
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void settingsButtonClick(View view) {
        SoundManager.getInstance().play(this, SoundManager.SoundType.BUTTON);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void setVersionInfo() {
        TextView versionText = (TextView) findViewById(R.id.versionText);
        TextView gitCommitText = (TextView) findViewById(R.id.gitCommitText);
        TextView buildDateText = (TextView) findViewById(R.id.buildDateText);
        versionText.setText(BuildConfig.VERSION_NAME);
        gitCommitText.setText(BuildConfig.GIT_HASH);
        if (BuildConfig.BUILD_DATE.length() >= 8) {
            String buildDateShort = BuildConfig.BUILD_DATE.substring(0, 8);
            buildDateText.setText(buildDateShort);
        }
    }

    public void clearSearchFilterClick(View view) {
        EditText editText = (EditText) findViewById(R.id.searchFilter);
        editText.setText("");
    }
}
