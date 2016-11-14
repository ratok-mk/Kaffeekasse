package com.zeiss.koch.kaffeekasse;

import com.github.lzyzsd.circleprogress.DonutProgress;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
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
    private Double totalPurchase;

    final static private int INITIAL_TIMEOUT_S = 15;
    final static private int RESET_TIMEOUT_S = 5;
    final static private int WARN_LIMIT_S = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        this.timeoutProgress = (DonutProgress) findViewById(R.id.timeout_progress);

        db = new SqlDatabaseHelper(this);

        this.totalPurchase = 0.0;

        Intent intent = getIntent();
        final int userId = intent.getIntExtra(MainActivity.EXTRA_MESSAGE_USERID, -1);
        if (userId == -1) {
            finish();
        }

        this.currentUser = db.getUser(userId);
        updateUsername(this.currentUser);
        updateBalance(this.currentUser, false);
        updatePurchase(false);

        setLogoffTime(this.INITIAL_TIMEOUT_S);

        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerHandler.obtainMessage(1).sendToTarget();
            }
        }, 0, 100);
    }

    private void setLogoffTime(int timeoutInS) {
        this.logoffTime = new java.util.Date(new java.util.Date().getTime() + 1000 * timeoutInS);
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

        SoundManager.getInstance().play(this, SoundManager.SoundType.BACK);
        Toast.makeText(this, getResources().getString(R.string.automatic_logoff), Toast.LENGTH_LONG).show();
        finish();
    }

    private Handler timerHandler = new Handler() {
        public void handleMessage(Message msg) {
            final float fraction = (logoffTime.getTime() - new java.util.Date().getTime()) / 1000.0f;
            final String caption = "" + (int) Math.ceil(fraction) + " s";

            if (fraction <= 0) {
                automaticExit();
            }

            timeoutProgress.setProgress(fraction / INITIAL_TIMEOUT_S * timeoutProgress.getMax());
            timeoutProgress.setText(caption);

            int colorCountDownText;
            int colorCountDownCircle;

            if (WARN_LIMIT_S >= (int) Math.ceil(fraction)) {
                colorCountDownCircle = R.color.warning;
                colorCountDownText = R.color.warning;
            } else {
                colorCountDownCircle = R.color.light_grey;
                colorCountDownText = R.color.text;
            }
            timeoutProgress.setFinishedStrokeColor(getColor(colorCountDownCircle));
            timeoutProgress.setTextColor(getColor(colorCountDownText));
        }
    };

    private void updateUsername(User user) {
        TextView usernameText = (TextView) findViewById(R.id.usernameTextView);
        usernameText.setText(user.getName());
    }


    private void updateBalance(User user, Boolean animate) {
        Double balance = db.getBalance(user);
        TextView balanceText = (TextView) findViewById(R.id.balanceTextView);

        final String formatted = Formater.valueToCurrencyString(balance);
        if (balance < 0.0) {
            balanceText.setTextAppearance(R.style.balance_minus);
        } else {
            balanceText.setTextAppearance(R.style.balance_plus);
        }
        balanceText.setText(formatted);

        if (animate) {
            AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.highlight);
            set.setTarget(balanceText);
            set.start();
        }
    }

    private void updatePurchase(Boolean animate) {
        TextView purchaseText = (TextView) findViewById(R.id.purchaseTextView);
        final String formatted = Formater.valueToCurrencyString(this.totalPurchase);
        purchaseText.setText(formatted);

        if (animate) {
            AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.highlight);
            set.setTarget(purchaseText);
            set.start();
        }
    }

    public void pay10Click(View view) {
        payAmount(-0.1);
    }

    public void pay20Click(View view) {
        payAmount(-0.2);
    }

    public void pay40Click(View view) {
        payAmount(-0.4);
    }

    private void payAmount(Double amount) {
        SoundManager.getInstance().play(this, SoundManager.SoundType.PAY);
        java.util.Date currentDate = new java.util.Date();
        Payment payment = new Payment(new Date(currentDate.getTime()), currentUser.getId(), amount);
        db.addPayment(payment);
        updateBalance(currentUser, true);
        setLogoffTime(this.RESET_TIMEOUT_S);

        this.totalPurchase -= amount;
        updatePurchase(true);
    }

    public void finishClick(View view) {
        SoundManager.getInstance().play(this, SoundManager.SoundType.BACK);
        finish();
    }
}
