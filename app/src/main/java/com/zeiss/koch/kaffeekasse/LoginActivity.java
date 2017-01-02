package com.zeiss.koch.kaffeekasse;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.DonutProgress;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AbstractNfcActivity {

    private Date date;
    private SqlDatabaseHelper db;
    private Timer timer;
    private DonutProgress timeoutProgress;
    private java.util.Date logoffTime;

    final static private int INITIAL_TIMEOUT_S = 30;
    final static private int WARN_LIMIT_S = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_login);
        this.timeoutProgress = (DonutProgress) findViewById(R.id.timeout_progress);

        this.date = new Date();
        String dateString = Formater.dateToLocalString(this.date);
        TextView textViewDate = (TextView) findViewById(R.id.textViewDate);
        textViewDate.setText(dateString);
        this.db = new SqlDatabaseHelper(this);

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

    private Handler timerHandler = new Handler() {
        public void handleMessage(Message msg) {
            final float fraction = (logoffTime.getTime() - new java.util.Date().getTime()) / 1000.0f;
            final String caption = "" + (int) Math.ceil(fraction) + " s";

            if (fraction <= 0) {
                exitView(false);
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

    @Override
    protected void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            // In case we would still use the Tag Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String userTagId = NfcHelper.getId(tag);

            User user = db.getUserByNfcId(userTagId);
            if (user != null && user.isAdmin()) {
                SoundManager.getInstance().play(this, SoundManager.SoundType.NFC);
                Intent newIntent = new Intent(this, SettingsActivity.class);
                startActivity(newIntent);
            } else {
                CustomToast.showText(this, "Could not find admin with NFC ID: " + userTagId, Toast.LENGTH_LONG);
                SoundManager.getInstance().play(this, SoundManager.SoundType.DENIED);
            }
        }
    }

    public void cancelButtonClick(View view) {
        exitView(false);
    }

    public void loginButtonClick(View view) {
        String securePassword = generatePassword(this.date);
        EditText adminPasswordText = (EditText) findViewById(R.id.adminPasswordText);
        String password = adminPasswordText.getText().toString();
        if (password.equals(securePassword))
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            exitView(false);
        }
        else {
            exitView(true);
        }
    }

    private String generatePassword(Date date) {
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);
        String password = String.format(
                "%1$d%2$d%3$d",
                calendar.get(Calendar.YEAR) * 2,
                (calendar.get(Calendar.MONTH) + 1)* 4, // zero index
                calendar.get(Calendar.DAY_OF_MONTH) * 8);

        return password;
    }

    private void exitView(boolean playDeniedSound) {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
        SoundManager.getInstance().play(this, playDeniedSound ? SoundManager.SoundType.DENIED : SoundManager.SoundType.BACK);
        finish();
    }
}
