package com.zeiss.koch.kaffeekasse;

/**
 * Sql Database Formater
 * Created by koch on 01.10.2016.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class SqlDatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "AccountDB";
    private final String CREATE_USERS_TABLE = "CREATE TABLE users ( " +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT, " +
            "nfcid TEXT, " +
            "role TEXT )";

    private final String CREATE_PAYMENTS_TABLE = "CREATE TABLE payments ( " +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "datetime TEXT, " +
            "userid INTEGER, " +
            "amount DOUBLE, " +
            "FOREIGN KEY(userid) REFERENCES users(id) ON DELETE CASCADE )";

    public SqlDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_PAYMENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // upgrade
        if (newVersion > oldVersion) {
            // update to v2: add altering statements...
            if (oldVersion < 2) {
            }
            // add more statements here on subsequent upgrades
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // downgrade
        if (newVersion < oldVersion) {
        }
    }

    public String getDatabaseName()
    {
        return DATABASE_NAME;
    }


    // Users table name
    private static final String TABLE_USERS = "users";

    // Users Table Columns names
    private static final String USERS_KEY_ID = "id";
    private static final String USERS_KEY_NAME = "name";
    private static final String USERS_KEY_NFCID = "nfcid";
    private static final String USERS_KEY_ROLE = "role";

    private static final String[] COLUMNS_USERS =
        {USERS_KEY_ID, USERS_KEY_NAME, USERS_KEY_NFCID, USERS_KEY_ROLE};

    public void addUser(User user){
        Log.d("addUser", user.toString());
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(USERS_KEY_NAME, user.getName());
        values.put(USERS_KEY_NFCID, user.getNfcId());
        values.put(USERS_KEY_ROLE, user.getDBRole());

        // 3. insert
        db.insert(TABLE_USERS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public User getUser(int id){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_USERS, // a. table
                        COLUMNS_USERS, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        User user = null;
        if (cursor != null && cursor.moveToFirst() == true) {
            user = new User(
                    Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3));

            Log.d("getUser(" + id + ")", user.toString());
        }

        cursor.close();

        return user;
    }

    public User getUserByNfcId(String nfcId){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_USERS, // a. table
                        COLUMNS_USERS, // b. column names
                        " nfcid = ?", // c. selections
                        new String[] { nfcId }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        User user = null;
        if (cursor != null && cursor.moveToFirst() == true)
        {
            user = new User(
                    Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3));

            Log.d("getUserByNfcId("+nfcId+")", user.toString());
        }

        cursor.close();

        return user;
    }

    public User getUserByName(String name) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_USERS, // a. table
                        COLUMNS_USERS, // b. column names
                        " name = ?", // c. selections
                        new String[]{name}, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        User user = null;
        if (cursor != null && cursor.moveToFirst() == true) {
            user = new User(
                    Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3));

            Log.d("getUserByName(" + name + ")", user.toString());
        }

        cursor.close();

        return user;
    }

    // Get All Users
    public List<User> getAllUsers() {
        List<User> users = new LinkedList<>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_USERS;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build user and add it to list
        User user = null;
        if (cursor.moveToFirst()) {
            do {
                user = new User(
                        Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                );


                // Add user to books
                users.add(user);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return users;
    }

    // Updating single user
    public int updateUser(User user) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(USERS_KEY_NAME, user.getName());
        values.put(USERS_KEY_NFCID, user.getNfcId());
        values.put(USERS_KEY_ROLE, user.getDBRole());

        // 3. updating row
        int i = db.update(TABLE_USERS, //table
                values, // column/value
                USERS_KEY_ID +" = ?", // selections
                new String[] { String.valueOf(user.getId()) }); //selection args

        // 4. close
        db.close();

        return i;

    }

    // Deleting single user
    public void deleteUser(User user) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_USERS,
                USERS_KEY_ID +" = ?",
                new String[] { String.valueOf(user.getId()) });

        // 3. close
        db.close();

        Log.d("deleteUser", user.toString());

    }


    // Payments table name
    private static final String TABLE_PAYMENTS = "payments";

    // Users Table Columns names
    private static final String PAYMENTS_KEY_ID = "id";
    private static final String PAYMENTS_KEY_DATETIME = "datetime";
    private static final String PAYMENTS_KEY_USERID = "userid";
    private static final String PAYMENTS_KEY_AMOUNT = "amount";

    private static final String[] COLUMNS_PAYMENTS =
            {PAYMENTS_KEY_ID, PAYMENTS_KEY_DATETIME, PAYMENTS_KEY_USERID, PAYMENTS_KEY_AMOUNT};

    public void addPayment(Payment payment){
        Log.d("addUser", payment.toString());
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(PAYMENTS_KEY_DATETIME, payment.getDatetime());
        values.put(PAYMENTS_KEY_USERID, payment.getUserid());
        values.put(PAYMENTS_KEY_AMOUNT, payment.getAmount());

        // 3. insert
        db.insert(TABLE_PAYMENTS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public Double getBalance(User user){
        String query = "SELECT sum(amount) FROM " + TABLE_PAYMENTS + " WHERE userid='" +user.getId() + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        Double balance = 0.0;
        if (cursor != null && cursor.moveToFirst() == true)
        {
            if (cursor.getString(0) != null) {
                balance = Double.parseDouble(cursor.getString(0));
            }
        }

        cursor.close();
        return balance;
    }

    public List<Payment> getAllPayments() {
        List<Payment> payments = new LinkedList<>();

        String query = "SELECT  * FROM " + TABLE_PAYMENTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        Payment payment = null;
        if (cursor.moveToFirst()) {
            do {
                payment = new Payment(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getDouble(3)
                );


                // Add user to books
                payments.add(payment);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return payments;
    }
}