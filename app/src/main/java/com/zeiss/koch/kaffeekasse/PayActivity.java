package com.zeiss.koch.kaffeekasse;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PayActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{


    private int currentUserIndex;
    private List<Account> accounts;
    private SqlDatabaseHelper db;

    private Spinner userSpinner;
    private ArrayAdapter<String> spinnerAdapter;

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    private NfcAdapter mNfcAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        db = new SqlDatabaseHelper(this);
        accounts = db.getAllAccounts();

        updateUserSpinner();
        if (accounts.size() > 0)
        {
            int currentUserIndex = 0;
            updateBalance(currentUserIndex);
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
        }
        else {
            if (!mNfcAdapter.isEnabled()) {
                Toast.makeText(this, "NFC is disabled.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            // In case we would still use the Tag Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] tagId = tag.getId();
        }

    }

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        //IntentFilter[] filters = new IntentFilter[];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
//        filters[0] = new IntentFilter();
//        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
//        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
//        try {
//            filters[0].addDataType(MIME_TEXT_PLAIN);
//        } catch (IntentFilter.MalformedMimeTypeException e) {
//            throw new RuntimeException("Check your mime type.");
//        }

        adapter.enableForegroundDispatch(activity, pendingIntent, null, techList);
    }

    /**
     * @param activity The corresponding {@link BaseActivity} requesting to stop the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    public void onItemSelected(AdapterView<?> parent,
                               View view, int pos, long id) {
        this.currentUserIndex = pos;
        updateBalance(pos);
    }

    public void onNothingSelected(AdapterView parent) {
        TextView balanceText = (TextView) findViewById(R.id.balanceTextView);
        balanceText.setText("");
    }

    private void updateBalance(int position) {
        if (position < accounts.size()) {
            Double balance = accounts.get(position).getBalance();
            TextView balanceText = (TextView) findViewById(R.id.balanceTextView);

            DecimalFormat round = new DecimalFormat("0.00");
            String formatted = round.format(balance);

            balanceText.setText(formatted);
        }
    }

    private void updateUserSpinner() {
        userSpinner = (Spinner) findViewById(R.id.userSpinner);
        userSpinner.setOnItemSelectedListener(this);

        List<String> users = new ArrayList<>();
        for (Account account:accounts) {
            users.add(account.getUsername());
        }
        spinnerAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, users);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(spinnerAdapter);
    }

    public void pay10Click(View view)
    {
        Account user = accounts.get(currentUserIndex);
        double balanceNew = user.getBalance() - 0.10;
        user.setBalance(balanceNew);
        db.updateAccount(user);
        updateBalance(currentUserIndex);
    }

    public void pay20Click(View view)
    {
        Account user = accounts.get(currentUserIndex);
        double balanceNew = user.getBalance() - 0.20;
        user.setBalance(balanceNew);
        db.updateAccount(user);
        updateBalance(currentUserIndex);
    }

    public void pay40Click(View view)
    {
        Account user = accounts.get(currentUserIndex);
        double balanceNew = user.getBalance() - 0.40;
        user.setBalance(balanceNew);
        db.updateAccount(user);
        updateBalance(currentUserIndex);
    }
}
