package com.zeiss.koch.kaffeekasse;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class PayActivity extends AppCompatActivity {


    private User currentUser;
    private int secondsToFinish;
    private SqlDatabaseHelper db;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        db = new SqlDatabaseHelper(this);

        Intent intent = getIntent();
        int userId = intent.getIntExtra(MainActivity.EXTRA_MESSAGE_USERID, -1);
        if (userId != -1) {
            this.currentUser = db.getUser(userId);
            updateUsername(this.currentUser);
            updateBalance(this.currentUser);
        }


        secondsToFinish = 15;

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerHandler.obtainMessage(1).sendToTarget();
            }
        }, 0, 1000);
    }

    private void automaticExit() {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }

        Toast.makeText(this, "Automatische Abmeldung!", Toast.LENGTH_LONG).show();
        finish();
    }

    private Handler timerHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (secondsToFinish <= 0)
            {
                automaticExit();
            }

            TextView timeToFinishText = (TextView) findViewById(R.id.timeToFinishTextView);
            timeToFinishText.setText(
                    String.format("Automatische Abmeldung in %1$d Sekunden!", secondsToFinish));
            secondsToFinish--;
        }
    };

    private void updateUsername(User user) {
        TextView usernameText = (TextView) findViewById(R.id.usernametextView);
        usernameText.setText(user.getName());
    }


    private void updateBalance(User user) {
            Double balance = db.getBalance(user);
            TextView balanceText = (TextView) findViewById(R.id.balanceTextView);

            DecimalFormat round = new DecimalFormat("0.00");
            String formatted = round.format(balance);
            if (balance < 0.0)
            {
                balanceText.setTextAppearance(R.style.TextAppearance_Red);
            }
            else
            {
                balanceText.setTextAppearance(android.R.style.TextAppearance_Medium);
            }

            balanceText.setText(formatted);
    }

    public void pay10Click(View view)
    {
        java.util.Date currentDate = new java.util.Date();
        Payment payment = new Payment(new Date(currentDate.getTime()), currentUser.getId(), -0.10);
        db.addPayment(payment);
        updateBalance(currentUser);
        showPaymentToast(currentUser, -0.10);
        finish();
    }

    public void pay20Click(View view)
    {
        java.util.Date currentDate = new java.util.Date();
        Payment payment = new Payment(new Date(currentDate.getTime()), currentUser.getId(), -0.20);
        db.addPayment(payment);
        updateBalance(currentUser);
        showPaymentToast(currentUser, -0.20);
        finish();
    }

    public void pay40Click(View view)
    {
        java.util.Date currentDate = new java.util.Date();
        Payment payment = new Payment(new Date(currentDate.getTime()), currentUser.getId(), -0.40);
        db.addPayment(payment);
        updateBalance(currentUser);
        showPaymentToast(currentUser, -0.40);
        finish();
    }

    private void showPaymentToast(User currentUser, double paymentValue) {
        Double balance = db.getBalance(currentUser);
        String name = currentUser.getName();
        String message =
            String.format("%1s hat %2$.2f€ bezahlt. Neuer Kontostand: %3$.2f€", name, Math.abs(paymentValue), balance);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void finishClick(View view)
    {
        finish();
    }
}
