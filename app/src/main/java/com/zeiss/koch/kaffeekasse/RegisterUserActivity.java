package com.zeiss.koch.kaffeekasse;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RegisterUserActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    public static final String EXTRA_MESSAGE_NFCID = "com.zeiss.koch.kaffeekasse.NFCID.RegisterUser";
    private SqlDatabaseHelper db;
    private String nfcId;
    private List<User> users;
    private ListView userList;
    private ArrayAdapter userListAdapter;
    private User currentUser;
    private List<String> registeredNfcTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_register_user);

        Intent intent = getIntent();
        nfcId = intent.getStringExtra(MainActivity.EXTRA_MESSAGE_NFCID);

        db = new SqlDatabaseHelper(this);
        updateUsers();
        updateUserList();

        if (registeredNfcTags.contains(nfcId)) {
            CustomToast.showText(this, "NFC Tag ist bereits registriert.", Toast.LENGTH_LONG);
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        this.currentUser = this.users.get(i);
        Button openAddUserButton = (Button) findViewById(R.id.openAddUserButton);
        openAddUserButton.setVisibility(View.INVISIBLE);
    }

    private void updateUsers() {
        this.users = db.getAllUsers();
        Collections.sort(this.users, new UserComparator());
        registeredNfcTags = new ArrayList<>();
        Iterator<User> it = this.users.iterator();
        while (it.hasNext()) {
            User user = it.next();
            if (!user.getNfcId().isEmpty()) {
                registeredNfcTags.add(user.getNfcId());
            }

            if (user.getRole() == User.Role.ADMIN || !user.getNfcId().isEmpty()) {
                it.remove();
            }
        }

        if (this.users != null && this.users.size() == 0)
        {
            OpenAddUserActivity();
            finish();
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


    public void OpenAddUserClick(View view) {
        OpenAddUserActivity();
        finish();
    }

    private void OpenAddUserActivity() {
        Intent newIntent = new Intent(this, AddUserActivity.class);
        newIntent.putExtra(EXTRA_MESSAGE_NFCID, nfcId);
        startActivity(newIntent);
    }

    private void updateUserList() {
        userList = (ListView) findViewById(R.id.userList);
        List<String> userStrings = new ArrayList<>();

        for (User user : users) {
            userStrings.add(user.getName());
        }

        userListAdapter = new ArrayAdapter(this, R.layout.user_list_item, userStrings);
        userList.setAdapter(userListAdapter);
        userList.setOnItemClickListener(this);

        selectCurrentUser();
    }

    private void selectCurrentUser() {
        // select current user
        if (this.currentUser != null) {
            final String userName = this.currentUser.getName();
            for (int i = 0; i < userListAdapter.getCount(); i++) {
                if (userName.equals(userListAdapter.getItem(i).toString())) {
                    userList.performItemClick(userList, i, 0);
                    break;
                }
            }
        }
    }
}
