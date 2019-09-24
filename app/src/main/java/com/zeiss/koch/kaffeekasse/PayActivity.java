package com.zeiss.koch.kaffeekasse;

import com.github.lzyzsd.circleprogress.DonutProgress;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PayActivity extends AppCompatActivity {

    private User currentUser;
    private SqlDatabaseHelper db;
    private Timer timer;
    private DonutProgress timeoutProgress;
    private java.util.Date logoffTime;
    private Double totalPurchase;

    private int purchase10 = 0;
    private int purchase25 = 0;
    private int purchase40 = 0;

    final static private int INITIAL_TIMEOUT_S = 15;
    final static private int RESET_TIMEOUT_S = 5;
    final static private int WARN_LIMIT_S = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_pay);
        setActionBarTitle();

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
        }, 0, 100);
    }

    private void setActionBarTitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Bezahlung - " + Formater.dateToLocalString(new java.util.Date()));
        }
    }

    private void setLogoffTime(int timeoutInS) {
        this.logoffTime = new java.util.Date(new java.util.Date().getTime() + 1000 * timeoutInS);
    }

    private Handler timerHandler = new Handler() {
        public void handleMessage(Message msg) {
            setActionBarTitle();
            final float fraction = (logoffTime.getTime() - new java.util.Date().getTime()) / 1000.0f;
            final String caption = "" + (int) Math.ceil(fraction) + " s";

            if (fraction <= 0) {
                exitView();
            }

            timeoutProgress.setProgress(fraction / INITIAL_TIMEOUT_S * timeoutProgress.getMax());
            timeoutProgress.setText(caption);

            int colorCountDownText;
            int colorCountDownCircle;

            if (WARN_LIMIT_S >= (int) Math.ceil(fraction)) {
                colorCountDownCircle = R.color.warning_red;
                colorCountDownText = R.color.warning_red;
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
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_pay);

        final String formatted = Formater.valueToCurrencyString(balance);
        if (balance <= 1.0 && balance >= 0.0)
        {
            balanceText.setTextAppearance(R.style.balance_low);
            layout.setBackgroundColor(getResources().getColor(R.color.warning_yellow));
            SoundManager.getInstance().play(this, SoundManager.SoundType.WARNING);
        }
        else if (balance < 0.0) {
            balanceText.setTextAppearance(R.style.balance_minus);
            layout.setBackgroundColor(getResources().getColor(R.color.warning_red));
            SoundManager.getInstance().play(this, SoundManager.SoundType.ALERT);
        } else {
            balanceText.setTextAppearance(R.style.balance_plus);
            layout.setBackgroundColor(getResources().getColor(R.color.background_grey));
        }
        balanceText.setText(formatted);

        if (animate) {
            AnimationHandler.highlight(this, balanceText);
        }
    }

    private void updatePurchase(Boolean animate) {
        TextView purchaseText = (TextView) findViewById(R.id.purchaseTextView);
        final String formatted = Formater.valueToCurrencyString(this.totalPurchase);
        purchaseText.setText(formatted);

        if (animate) {
            AnimationHandler.highlight(this, purchaseText);
        }
    }

    public void pay10Click(View view) {
        payAmount(-0.1);
        purchase10++;
        TextView purchaseCount10Text = (TextView)findViewById(R.id.textViewPurchaseCount10);
        purchaseCount10Text.setText(String.format("%1d", purchase10));
    }

    public void pay20Click(View view) {
        payAmount(-0.25);
        purchase25++;
        TextView purchaseCount20Text = (TextView)findViewById(R.id.textViewPurchaseCount20);
        purchaseCount20Text.setText(String.format("%1d", purchase25));
    }

    public void pay40Click(View view) {
        payAmount(-0.4);
        purchase40++;
        TextView purchaseCount40Text = (TextView)findViewById(R.id.textViewPurchaseCount40);
        purchaseCount40Text.setText(String.format("%1d", purchase40));
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
        exitView();
    }

    private void exitView() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
        SoundManager.getInstance().play(this, SoundManager.SoundType.BACK);
        ShowTotalPurchase();
        finish();
    }

    private void ShowTotalPurchase() {
        String text = String.format("Einkauf von %1s: %2s",
                this.currentUser.getName(),
                Formater.valueToCurrencyString(this.totalPurchase));
        CustomToast.showText(this, text, Toast.LENGTH_LONG);
    }

    public void historyClick(View view) {
        List<Payment> userPayments = db.getUserPayments(this.currentUser);
        int payments = 0;
        int index = userPayments.size()-1;
        String text = String.format("Letzte Einkäufe von %1s:",this.currentUser.getName());
        while (index > 0 && payments < 5)
        {
            Payment payment = userPayments.get(index);
            text += String.format("\n%1s:   %2s",
                    payment.getDatetimeFormated(),
                    Formater.valueToCurrencyString(payment.getAmount()));
            index--;
            payments++;
        }

        CustomToast.showText(this, text, Toast.LENGTH_LONG);
    }
}
