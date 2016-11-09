package com.zeiss.koch.kaffeekasse;

import com.github.lzyzsd.circleprogress.DonutProgress;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.util.Timer;
import java.util.TimerTask;

public class PayActivity extends AppCompatActivity {

    private User currentUser;
    private SqlDatabaseHelper db;
    private Timer timer;
    private DonutProgress timeoutProgress;
    private java.util.Date logoffTime;

    final static private int LOGOFF_TIMEOUT_S = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        this.timeoutProgress = (DonutProgress) findViewById(R.id.timeout_progress);

        db = new SqlDatabaseHelper(this);

        Intent intent = getIntent();
        int userId = intent.getIntExtra(MainActivity.EXTRA_MESSAGE_USERID, -1);
        if (userId != -1) {
            this.currentUser = db.getUser(userId);
            updateUsername(this.currentUser);
            updateBalance(this.currentUser);
        }

        this.logoffTime = new java.util.Date(new java.util.Date().getTime() + 1000 * this.LOGOFF_TIMEOUT_S);

        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerHandler.obtainMessage(1).sendToTarget();
            }
        }, 0, 30);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
    }

    private void automaticExit() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }

        Toast.makeText(this, getResources().getString(R.string.automatic_logoff), Toast.LENGTH_LONG).show();
        finish();
    }

    private Handler timerHandler = new Handler() {
        public void handleMessage(Message msg) {
            final float fraction = (logoffTime.getTime() - new java.util.Date().getTime()) / 1000.0f;
            final String caption = "" + (int)Math.ceil(fraction) + " s";

            if (fraction <= 0) {
                automaticExit();
            }

            timeoutProgress.setProgress(fraction / LOGOFF_TIMEOUT_S * timeoutProgress.getMax());
            timeoutProgress.setText(caption);
        }
    };

    private void updateUsername(User user) {
        TextView usernameText = (TextView) findViewById(R.id.usernametextView);
        usernameText.setText(user.getName());
    }


    private void updateBalance(User user) {
        Double balance = db.getBalance(user);
        TextView balanceText = (TextView) findViewById(R.id.balanceTextView);

        String formatted = Formater.valueToCurrencyString(balance);
        if (balance < 0.0) {
            balanceText.setTextAppearance(R.style.balance_minus);
        } else {
            balanceText.setTextAppearance(R.style.balance_plus);
        }

        balanceText.setText(formatted);
    }

    public void pay10Click(View view) {
        payAmount(-0.1);
    }

    public void pay20Click(View view){
        payAmount(-0.2);
    }

    public void pay40Click(View view) {
        payAmount(-0.4);
    }

    private void payAmount(Double amount) {
        java.util.Date currentDate = new java.util.Date();
        Payment payment = new Payment(new Date(currentDate.getTime()), currentUser.getId(), amount);
        db.addPayment(payment);
        updateBalance(currentUser);
        showPaymentToast(currentUser, amount);
        finish();
    }

    private void showPaymentToast(User currentUser, double paymentValue) {
        Double balance = db.getBalance(currentUser);
        String name = currentUser.getName();
        String message =
                String.format("%1s hat %2$.2f€ bezahlt. Neuer Kontostand: %3$.2f€", name, Math.abs(paymentValue), balance);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void finishClick(View view) {
        finish();
    }
}
