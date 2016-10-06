package com.zeiss.koch.kaffeekasse;

/**
 * Sql Database Helper
 * Created by koch on 01.10.2016.
 */

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SqlDatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "AccountDB";
    private final String CREATE_ACCOUNTS_TABLE = "CREATE TABLE accounts ( " +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "admin BOOLEAN, "+
            "username TEXT, "+
            "nfcid TEXT, "+
            "balance DOUBLE )";

    public SqlDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create books table
        db.execSQL(CREATE_ACCOUNTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS accounts");

        // create fresh table
        this.onCreate(db);
    }

    public void resetDatabase()
    {
        this.getWritableDatabase().execSQL("DROP TABLE IF EXISTS accounts");

        // create books table
        this.getWritableDatabase().execSQL(CREATE_ACCOUNTS_TABLE);
    }

    //---------------------------------------------------------------------

    /**
     * CRUD operations (create "add", read "get", update, delete) book + get all books + delete all books
     */

    // Accounts table name
    private static final String TABLE_ACCOUNTS = "accounts";

    // Accounts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_ADMIN = "admin";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NFCID = "nfcid";
    private static final String KEY_BALANCE = "balance";

    private static final String[] COLUMNS = {KEY_ID, KEY_ADMIN, KEY_USERNAME, KEY_NFCID, KEY_BALANCE};

    public void addAccount(Account account){
        Log.d("addAccount", account.toString());
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_ADMIN, account.getAdmin());
        values.put(KEY_USERNAME, account.getUsername());
        values.put(KEY_NFCID, account.getNfcId());
        values.put(KEY_BALANCE, account.getBalance());

        // 3. insert
        db.insert(TABLE_ACCOUNTS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public Account getAccount(int id){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABLE_ACCOUNTS, // a. table
                        COLUMNS, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build account object
        Account account = new Account(
            Integer.parseInt(cursor.getString(0)),
            Boolean.parseBoolean(cursor.getString(1)),
            cursor.getString(2),
            cursor.getString(3),
            Double.parseDouble(cursor.getString(4)));

        Log.d("getAccount("+id+")", account.toString());

        cursor.close();
        // 5. return account
        return account;
    }

    // Get All Accounts
    public List<Account> getAllAccounts() {
        List<Account> accounts = new LinkedList<>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_ACCOUNTS;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build account and add it to list
        Account account = null;
        if (cursor.moveToFirst()) {
            do {
                account = new Account(
                    Integer.parseInt(cursor.getString(0)),
                    Boolean.parseBoolean(cursor.getString(1)),
                    cursor.getString(2),
                    cursor.getString(3),
                    Double.parseDouble(cursor.getString(4)));


                // Add account to books
                accounts.add(account);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return accounts;
    }

    // Updating single account
    public int updateAccount(Account account) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_ADMIN, account.getAdmin());
        values.put(KEY_USERNAME, account.getUsername());
        values.put(KEY_NFCID, account.getNfcId());
        values.put(KEY_BALANCE, account.getBalance());

        // 3. updating row
        int i = db.update(TABLE_ACCOUNTS, //table
                values, // column/value
                KEY_ID+" = ?", // selections
                new String[] { String.valueOf(account.getId()) }); //selection args

        // 4. close
        db.close();

        return i;

    }

    // Deleting single account
    public void deleteAccount(Account account) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_ACCOUNTS,
                KEY_ID+" = ?",
                new String[] { String.valueOf(account.getId()) });

        // 3. close
        db.close();

        Log.d("deleteAccount", account.toString());

    }
}