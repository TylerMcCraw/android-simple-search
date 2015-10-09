package com.w3bshark.android_simple_search.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.w3bshark.android_simple_search.data.CsvDbContract.CsvDbEntry;

public class CsvDbHelper extends SQLiteOpenHelper {

    // INCREMENT the database version, if you change the database schema.
    private static final int DATABASE_VERSION = 2;
    static final String DATABASE_NAME = "csvdata.db";

    public CsvDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold csv item entries.
        final String SQL_CREATE_CSVDATA_TABLE = "CREATE TABLE " + CsvDbEntry.TABLE_NAME + " (" +
                CsvDbEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CsvDbEntry.COLUMN_CSVID + " INTEGER NOT NULL, " +
                CsvDbEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
        // To assure the application have just one of each unique csv id entry
        " UNIQUE (" + CsvDbEntry.COLUMN_CSVID + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_CSVDATA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CsvDbEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
