package com.zeiss.koch.kaffeekasse;

/**
 * Sql Database Helper
 * Created by koch on 01.10.2016.
 */

import java.sql.Date;
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
    private static final int DATABASE_VERSION = 2;
    // Database Name
    private static final String DATABASE_NAME = "AccountDB";
    private final String CREATE_USERS_TABLE = "CREATE TABLE users ( " +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT, "+
            "nfcid TEXT, "+
            "role string )";

    private final String CREATE_PAYMENTS_TABLE = "CREATE TABLE payments ( " +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "datetime DATETIME, " +
            "userid INTEGER, "+
            "amount DOUBLE )";

    public SqlDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create books table
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_PAYMENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS accounts");
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS payments");

        // create fresh table
        this.onCreate(db);
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
        values.put(USERS_KEY_ROLE, user.getRole());

        // 3. insert
        db.insert(TABLE_USERS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public User getUser(int id){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABLE_USERS, // a. table
                        COLUMNS_USERS, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build user object
        User user = new User(
            Integer.parseInt(cursor.getString(0)),
            cursor.getString(1),
            cursor.getString(2),
            cursor.getString(3));

        Log.d("getUser("+id+")", user.toString());

        cursor.close();
        // 5. return user
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
        values.put(USERS_KEY_ROLE, user.getRole());

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
        values.put(PAYMENTS_KEY_DATETIME, payment.getDatetime().toString());
        values.put(PAYMENTS_KEY_USERID, payment.getUserid());
        values.put(PAYMENTS_KEY_AMOUNT, payment.getAmount());

        // 3. insert
        db.insert(TABLE_PAYMENTS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }
}