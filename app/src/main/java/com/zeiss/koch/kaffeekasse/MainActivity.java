package com.zeiss.koch.kaffeekasse;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void payButtonClick(View view)
    {
        Intent intent = new Intent(this, PayActivity.class);
        startActivity(intent);
    }

    public void accountButtonClick(View view)
    {
        Intent intent = new Intent(this, AccountActivity.class);
        startActivity(intent);
    }

    public void settingsButtonClick(View view)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


}
