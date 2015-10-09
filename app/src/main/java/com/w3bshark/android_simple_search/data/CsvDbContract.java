package com.w3bshark.android_simple_search.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the CSV item database.
 */
public class CsvDbContract {

    public static final String CONTENT_AUTHORITY = "com.w3bshark.android_simple_search";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_CSVDATA = "csvdata";

    /* Inner class that defines the table contents of the CSV data table */
    public static final class CsvDbEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CSVDATA).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CSVDATA;

        public static final String TABLE_NAME = "csvdata";

        public static final String COLUMN_CSVID = "csvid";
        public static final String COLUMN_DESCRIPTION = "description";

        public static Uri buildCsvDbUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildAllCsvItemsUri() {
            return CONTENT_URI;
        }
    }
}
