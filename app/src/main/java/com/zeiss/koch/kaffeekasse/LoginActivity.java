package com.zeiss.koch.kaffeekasse;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class LoginActivity extends AppCompatActivity {

    private Date date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.date = new Date();
        TextView textViewDate = (TextView) findViewById(R.id.textViewDate);
        textViewDate.setText(this.date.toString());
    }

    public void cancelButtonClick(View view)
    {
        finish();
    }

    public void loginButtonClick(View view)
    {
        String securePassword = generatePassword(this.date);
        EditText adminPasswordText = (EditText)findViewById(R.id.adminPasswordText);
        String password = adminPasswordText.getText().toString();
        if (password.equals(securePassword))
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        finish();
    }

    private String generatePassword(Date date) {
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);
        String password = String.format(
            "%1$d%2$d%3$d",
            calendar.get(Calendar.YEAR) * 2,
            calendar.get(Calendar.MONTH) * 4,
            calendar.get(Calendar.DAY_OF_MONTH) * 8);

        return password;
    }
}
