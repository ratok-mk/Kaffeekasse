package com.zeiss.koch.kaffeekasse;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddUserActivity extends AppCompatActivity {

    private SqlDatabaseHelper db;
    private String nfcId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        Intent intent = getIntent();
        nfcId = intent.getStringExtra(MainActivity.EXTRA_MESSAGE_NFCID);
        db = new SqlDatabaseHelper(this);
    }

    public void AddUserClick(View view)
    {
        if (!nfcId.isEmpty())
        {
            final EditText userEditText = (EditText) findViewById(R.id.userNameTextView);
            User newUser = new User(userEditText.getText().toString(), this.nfcId);
            db.addUser(newUser);
            CharSequence message = String.format("Nutzer %1$s hinzugefügt.", newUser.getName());
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void CancelClick(View view)
    {
        finish();
    }
}
